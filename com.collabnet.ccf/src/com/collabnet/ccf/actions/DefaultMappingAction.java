package com.collabnet.ccf.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.dialogs.ExceptionDetailsErrorDialog;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.schemageneration.XSLTInitialMFDGeneratorScriptGenerator;

public class DefaultMappingAction extends ActionDelegate {
	private IStructuredSelection fSelection;

	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof SynchronizationStatus projectMapping) {
				File modelFile = null;
				String sourceSchemaName = null;
				String targetSchemaName = null;
				if (projectMapping.usesGraphicalMapping()) {
					modelFile = projectMapping.getMappingFile(projectMapping
							.getMFDFileName());
					sourceSchemaName = projectMapping.getSourceRepositorySchemaFileName();
					targetSchemaName = projectMapping.getTargetRepositorySchemaFileName();
				} else {
					modelFile = projectMapping.getXslFile();
				}
				if (modelFile == null || !modelFile.exists()) {
					MessageDialog
							.openInformation(
									Display.getDefault().getActiveShell(),
									"Set Default Mapping Template",
									"Mapping rules have not been defined for this project mapping.  You must first define mapping for this project mapping before it can be used as a template for new mappings.\n\nTo do this, use the Edit Field Mappings option.");
					return;
				}
				if (!MessageDialog
						.openConfirm(
								Display.getDefault().getActiveShell(),
								"Set Default Mapping Template",
								"This option will cause the "
										+ (projectMapping
												.usesGraphicalMapping() ? "graphical"
												: "non-graphical")
										+ " mapping rules for this project mapping to be used as the default mapping for newly created mappings.")) {
					return;
				}
				try {
					if (projectMapping.usesGraphicalMapping()) {
						XSLTInitialMFDGeneratorScriptGenerator generator = new XSLTInitialMFDGeneratorScriptGenerator();
						Document mfdDocument = generator
								.generateCreateInitialMFDScript(modelFile
										.getAbsolutePath(), sourceSchemaName, targetSchemaName);
						File createInitialMFDFile = projectMapping
								.getCreateInitialMFDFile();
						writeFile(createInitialMFDFile, mfdDocument);
					} else {
						File sampleFile = projectMapping.getSampleXslFile();
						copyFile(modelFile, sampleFile);
					}
				} catch (Exception e) {
					Activator.handleError(e);
					ExceptionDetailsErrorDialog.openError(Display.getDefault().getActiveShell(), "Set Default Mapping Template", e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection selection) {
			fSelection = selection;
		}
		if (action != null) {
			action.setEnabled(Activator.getDefault().getActiveRole()
					.isEditFieldMappings());
		}
	}

	private static void writeFile(File file, Document content)
			throws IOException {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8"));
			XMLWriter writer = new XMLWriter(out, OutputFormat
					.createPrettyPrint());
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			throw e;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
				}
			}
		}
	}

	private static void copyFile(File fromFile, File toFile) throws IOException {
		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytes_read;
			while ((bytes_read = from.read(buffer)) != -1)
				to.write(buffer, 0, bytes_read);
		} finally {
			if (from != null)
				try {
					from.close();
				} catch (IOException e) {
				}
			if (to != null)
				try {
					to.close();
				} catch (IOException e) {
				}
		}
	}

}
