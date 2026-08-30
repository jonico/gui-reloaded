package com.collabnet.ccf.wizards;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;

import org.dom4j.Document;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.ICcfParticipant;
import com.collabnet.ccf.core.CCFRuntimeException;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.dialogs.ExceptionDetailsErrorDialog;
import com.collabnet.ccf.editors.ExternalFileEditorInput;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.schemageneration.CCFSchemaAndXSLTFileGenerator;
import com.collabnet.ccf.schemageneration.CCFXSLTSchemaAndXSLTFileGenerator;
import com.collabnet.ccf.schemageneration.XSLTInitialMFDGenerator;

public class EditFieldMappingsWizard extends Wizard {
	private SynchronizationStatus projectMapping;

	private EditFieldMappingsWizardMainPage mainPage;
	
	private Exception mapForceException;
	private boolean canceled;
	

	public EditFieldMappingsWizard(SynchronizationStatus projectMapping) {
		super();
		this.projectMapping = projectMapping;
	}
	
	@Override
	public void addPages() {
		super.addPages();
		setWindowTitle("Edit Field Mappings");
		mainPage = new EditFieldMappingsWizardMainPage("main", "Choices", Activator.getDefault().getImageDescriptor(Activator.IMAGE_EDIT_FIELD_MAPPINGS_WIZBAN));
		addPage(mainPage);
	}

	@Override
	public boolean performFinish() {
		if (mainPage.isEdit()) {
			return edit(false);
		}
		if (mainPage.isMapForceEdit()) {
			return editWithMapForce();
		}
		if (mainPage.isSwitchOnly()) {
			return switchOnly();
		}
		return true;
	}
	
	public boolean needsProgressMonitor() {
		return true;
	}
	
	public SynchronizationStatus getProjectMapping() {
		return projectMapping;
	}
	
	private String getMapForcePath() {
		return mainPage.getMapForcePath();
	}
	
	private boolean switchOnly() {
		String message = null;
		if (projectMapping.usesGraphicalMapping()) {
			projectMapping.switchToNonGraphicalMapping();
			message = "Switched to non-graphical mapping.";
		} else {
			projectMapping.switchToGraphicalMapping();
			message = "Switched to graphical mapping.";
		}
		CcfDataProvider dataProvider = new CcfDataProvider();
		try {
			dataProvider.setFieldMappingMode(projectMapping);
		} catch (Exception e) {
			Activator.handleDatabaseError(e, false, true, "Edit Field Mappings");
			return false;
		}
		MessageDialog.openInformation(getShell(), "Edit Field Mappings", message);
		return true;
	}
	
	public boolean editWithMapForce() {
		final File mfdFile = projectMapping.getMappingFile(projectMapping
				.getMFDFileName());
		final boolean restoreDefaultMapping = mainPage.isRestoreDefaultMapping();
		if (restoreDefaultMapping && mfdFile.exists()) {
			if (!confirmRestoreDefaultMapping()) {
				return false;
			}
			mfdFile.delete();
		}
		canceled = false;
		mapForceException = null;
		final CcfDataProvider dataProvider = new CcfDataProvider();
		final String mapForcePath = getMapForcePath();
		final boolean generate = mainPage.isGenerate();
		final boolean switchToGraphical = mainPage.isSwitchToGraphicalMapping();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				if (switchToGraphical) {
					projectMapping.switchToGraphicalMapping();
					try {
						dataProvider.setFieldMappingMode(projectMapping);
					} catch (Exception e) {
						mapForceException = e;
						Activator.handleError(e);
						return;
					}
				}
				if (generate || restoreDefaultMapping) {
					monitor.beginTask("Generate Files", 7);
					try {
						CCFSchemaAndXSLTFileGenerator xmlFileGenerator = new CCFXSLTSchemaAndXSLTFileGenerator(
								projectMapping.getXSLTFolder().getAbsolutePath());
						ICcfParticipant ccfParticipant = Activator.getCcfParticipantForType(projectMapping.getLandscape().getType2());
						ccfParticipant.extract(projectMapping, xmlFileGenerator, monitor);
						if (monitor.isCanceled()) {
							canceled = true;
							return;
						}
						ccfParticipant = Activator.getCcfParticipantForType(projectMapping.getLandscape().getType1());
						ccfParticipant.extract(projectMapping, xmlFileGenerator, monitor);
						if (monitor.isCanceled()) {
							canceled = true;
							return;
						}
						if (null != mfdFile && !mfdFile.exists()) {
							monitor.subTask("Generating MFD-File");
							File createInitialMFDFile = projectMapping.getCreateInitialMFDFile();
							if (!createInitialMFDFile.exists()) {
								createInitialMFDFile = projectMapping.getFallbackCreateInitialMFDFile();
							}
							XSLTInitialMFDGenerator mfdFileGenerator = new XSLTInitialMFDGenerator(
									createInitialMFDFile);
							Document mfdDocument = mfdFileGenerator
									.generateInitialMFD(
											projectMapping
													.getSourceRepositorySchemaFileName(),
											projectMapping
													.getTargetRepositorySchemaFileName());
							writeFile(mfdFile, mfdDocument);
						}
						monitor.worked(1);

						if (monitor.isCanceled()) {
							canceled = true;
							return;	
						}
					} catch (Exception e) {
						mapForceException = e;
						Activator.handleError(e);
					}
				}	
				if (canceled || null != mapForceException) {
					return;
				}
				try {
					monitor.beginTask(
							"Use external application for mapping ...",
							4);
					monitor
							.subTask("Launch MapForce and wait until MapForce is closed ...");

					// start mapforce
					File mfdFile = projectMapping.getMappingFile(projectMapping
							.getMFDFileName());
					String interactiveMapforce[] = { mapForcePath,
							mfdFile.getCanonicalPath() };
					Process process = Runtime.getRuntime().exec(
							interactiveMapforce);
					process.waitFor();
					monitor.worked(1);

					if (monitor.isCanceled())
						return;

					monitor
							.subTask("Launch MapForce in background again to generate XSLT file ...");

					String scriptedMapforce[] = { mapForcePath,
							mfdFile.getCanonicalPath(), "/XSLT",
							mfdFile.getParentFile().getCanonicalPath() };
					process = Runtime.getRuntime().exec(
							scriptedMapforce);
					process.getOutputStream().close();
					process.getInputStream().close();
					process.getErrorStream().close();
					process.waitFor();
					monitor.worked(1);

					if (monitor.isCanceled())
						return;

					monitor.subTask("Rename generated XSLT file ...");
					File mfXSLFile = projectMapping.getMappingFile(projectMapping
							.getMFXslFilename());
					File graphicalXSLFile = projectMapping
							.getMappingFile(projectMapping
									.getGraphicalXslFileName());
					if (!mfXSLFile.exists()) {
						throw new CCFRuntimeException(
								"Could not find file "
										+ mfXSLFile.getCanonicalPath()
										+ " that should have been generated by MapForce.\nMaybe you have not designed a valid mapping?\nPlease try to at least connect the root nodes of your systems.");
					} else {
						if (graphicalXSLFile.exists()) {
							graphicalXSLFile.delete();
						}
						if (!mfXSLFile.renameTo(graphicalXSLFile) || !graphicalXSLFile.exists()) {
							throw new CCFRuntimeException(
									"Could not rename MapForce file "
											+ mfXSLFile
													.getCanonicalPath()
											+ " to CCF file "
											+ graphicalXSLFile
													.getCanonicalPath());
						}
					}
					monitor.worked(1);					
				} catch (Exception e) {
					mapForceException = e;
					Activator.handleError(e);					
				}
				monitor.done();
			}		
		};
		try {
			getContainer().run(true, true, runnable);
		} catch (Exception e) {
			mapForceException = e;
			Activator.handleError(e);
		}
		if (null != mapForceException) {
			String errorMessage = null;
			if (mapForceException instanceof InvocationTargetException) {
				errorMessage = mapForceException.getCause().getMessage();
			} else {
				if (null == mapForceException.getMessage()
						|| 0 == mapForceException.getMessage().trim().length()) {
					errorMessage = mapForceException.toString();
				} else {
					errorMessage = mapForceException.getMessage();
				}
			}
			Activator.handleError(mapForceException);
			MessageDialog.openError(Display.getDefault()
					.getActiveShell(), "Edit Field Mappings",
					errorMessage);
		}
		boolean success = !canceled && mapForceException == null;
		if (switchToGraphical && !success) {
			projectMapping.switchToNonGraphicalMapping();
			try {
				dataProvider.setFieldMappingMode(projectMapping);
			} catch (Exception e) {
				Activator.handleError(e);
			}			
		}
		return success;
	}
	
	public boolean edit(boolean prompt) {
		if (null != mainPage && mainPage.isSwitchToNonGraphicalMapping()) {
			projectMapping.switchToNonGraphicalMapping();
			CcfDataProvider dataProvider = new CcfDataProvider();
			try {
				dataProvider.setFieldMappingMode(projectMapping);
			} catch (Exception e) {
				Activator.handleDatabaseError(e, false, true, "Edit Field Mappings");
				return false;
			}
		}
		File xslFile = projectMapping.getXslFile();
		if (null != mainPage && mainPage.isRestoreDefaultMapping() && xslFile.exists()) {
			if (!confirmRestoreDefaultMapping()) {
				return false;
			}
			xslFile.delete();
		}
		if (!xslFile.exists()) {
			if (prompt && !MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "Edit Field Mappings", "File " + xslFile.getName() + " does not exist.\n\nDo you wish to create it by copying sample.xsl?")) {
				return false;
			}
			try {
				xslFile.createNewFile();
				File sampleFile = projectMapping.getSampleXslFile();
				if (null != sampleFile && sampleFile.exists()) {
					CcfDataProvider.copyFile(sampleFile, xslFile);
				}
			} catch (IOException e) {
				ExceptionDetailsErrorDialog.openError(getShell(), "Edit Field Mappings", e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
				Activator.handleError(e);
				return false;
			}
		}
		IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path(xslFile.getAbsolutePath()));
		final IEditorInput input = new ExternalFileEditorInput(fileStore, xslFile.getName());
		IEditorRegistry registry = Activator.getDefault().getWorkbench().getEditorRegistry();
		IEditorDescriptor descriptor = registry.getDefaultEditor(xslFile.getName());
		String id;
		if (null == descriptor) {
			id = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
		} else {
			id = descriptor.getId();
		}
		IWorkbenchPage page = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			page.openEditor(input, id);
		} catch (PartInitException e) {
			ExceptionDetailsErrorDialog.openError(getShell(), "Edit Field Mappings", e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
			Activator.handleError(e);
			return false;
		}
		return true;
	}

	private boolean confirmRestoreDefaultMapping() {
		return MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Edit Field Mappings", "WARNING:  Restoring default mapping will cause any previously-defined field mapping to be overlaid.\n\nAre you sure you wish to restore default mapping?");
	}

	private static void writeFile(File file, Document content)
			throws IOException {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8"));
			out.write(content.asXML());
			/*XMLWriter writer = new XMLWriter(out, format);
			writer.write(content);
			writer.close();*/
		} catch (IOException e) {
			throw e;
		} finally {
			if (null != out) {
				try {
					out.close();
				} catch (Exception e) {
				}
			}
		}
	}

}
