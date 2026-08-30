package com.collabnet.ccf.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.part.FileEditorInput;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.core.GenericArtifactHelper;
import com.collabnet.ccf.core.GenericArtifactParsingException;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.db.Filter;
import com.collabnet.ccf.db.Update;
import com.collabnet.ccf.model.Patient;
import com.collabnet.ccf.views.HospitalView;

public class ExaminePayloadAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	private IFile quarantineFile;
	private IEditorInput input;

	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof Patient patient) {
				IWorkbenchPage page = Activator.getDefault().getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				if (patient.getGenericArtifact() != null
						&& patient.getGenericArtifact().trim().length() > 0) {
					try {
						
						IFile checkFile = Activator.getQuarantinedArtifactFile(patient, false);
						IEditorInput checkInput = new FileEditorInput(checkFile);
						if (page.findEditor(checkInput) != null) {
							// If editor is already open for selected hospital entry, reuse it.
							quarantineFile = checkFile;
							input = checkInput;
						} else {
							// Otherwise create the file and open new editor.
							quarantineFile = Activator.getQuarantinedArtifactFile(patient, true);							
							input = new FileEditorInput(quarantineFile);							
						}
						IEditorRegistry registry = Activator.getDefault()
								.getWorkbench().getEditorRegistry();
						IEditorDescriptor descriptor = registry
								.getDefaultEditor("file.xml");

						String id;
						if (descriptor == null) {
							id = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
						} else {
							id = descriptor.getId();
						}
						try {
							final IEditorPart editorPart = page.openEditor(
									input, id);
							page.addPartListener(new CloseListener(editorPart, quarantineFile));
							editorPart
									.addPropertyListener(new IPropertyListener() {

										public void propertyChanged(
												Object arg0, int arg1) {
											if (!editorPart.isDirty()) {
												try {
													final String updatedPayload = readFileAsString(quarantineFile.
															getLocation().toString());
													GenericArtifactHelper
															.createGenericArtifactJavaObject(DocumentHelper
																	.parseText(updatedPayload));
													BusyIndicator
															.showWhile(
																	Display
																			.getDefault(),
																	new Runnable() {
																		public void run() {
																			Filter filter = new Filter(
																					CcfDataProvider.HOSPITAL_ID,
																					Integer
																							.toString(patient
																									.getId()),
																					false);
																			Filter[] filters = { filter };
																			Update update = new Update(
																					CcfDataProvider.HOSPITAL_GENERIC_ARTIFACT,
																					updatedPayload);
																			Update[] updates = { update };
																			CcfDataProvider dataProvider = new CcfDataProvider();
																			try {
																				dataProvider
																						.updatePatients(
																								patient
																										.getLandscape(),
																								updates,
																								filters);
																				if (HospitalView
																						.getView() != null) {
																					HospitalView
																							.getView()
																							.refresh();
																				}
																			} catch (Exception e) {
																				Activator
																						.handleError(e);
																			}
																		}
																	});
												} catch (GenericArtifactParsingException e) {
													String errorMessage = "Could not save payload because it does not comply to the generic artifact format: "
															+ e.getMessage();
													Activator.handleError(
															errorMessage, e);
													MessageDialog
															.openError(
																	Display
																			.getDefault()
																			.getActiveShell(),
																	"Examine artifact payload",
																	errorMessage);
												} catch (DocumentException e) {
													String errorMessage = "Could not save payload because it is not a valid XML document: "
															+ e.getMessage();
													Activator.handleError(
															errorMessage, e);
													MessageDialog
															.openError(
																	Display
																			.getDefault()
																			.getActiveShell(),
																	"Examine artifact payload",
																	errorMessage);
												} catch (IOException e) {
													Activator.handleError(e);
												}
											}
										}

									});
						} catch (PartInitException e) {
							Activator
									.handleError("Examine Hospital Payload", e);
							break;
						}
					} catch (Exception e) {
						Activator.handleError(e);
					}
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection selection) {
			fSelection = selection;
			if (action != null)
				action.setEnabled(isEnabledForSelection());
		}
	}

	private static String readFileAsString(String filePath)
			throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			fileData.append(buf, 0, numRead);
		}
		reader.close();
		return fileData.toString();
	}

	@SuppressWarnings("unchecked")
	private boolean isEnabledForSelection() {
		if (fSelection == null
				|| !Activator.getDefault().getActiveRole()
						.isEditQuarantinedArtifact())
			return false;
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof Patient patient) {
				if (patient.getGenericArtifact() == null
						|| patient.getGenericArtifact().trim().length() == 0)
					return false;
			}
		}
		return true;
	}
	
	class CloseListener implements IPartListener2 {
		private IEditorPart editorPart;
		private IFile file;
		
		public CloseListener(IEditorPart editorPart, IFile file) {
			this.editorPart = editorPart;
			this.file = file;
		}

		public void partActivated(IWorkbenchPartReference partRef) {}
		public void partBroughtToTop(IWorkbenchPartReference partRef) {}
		public void partClosed(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(true);
			if (part != null && part.equals(editorPart)) {
				try {
					file.delete(true, null);
				} catch (CoreException e) {
					// If we were unable to delete the file for some reason,
					// try to delete it when the JVM closes.
					File tempFile = new File(quarantineFile.getLocation().toString());
					tempFile.deleteOnExit();
				}
			}
		}
		public void partDeactivated(IWorkbenchPartReference partRef) {}
		public void partHidden(IWorkbenchPartReference partRef) {}
		public void partInputChanged(IWorkbenchPartReference partRef) {}
		public void partOpened(IWorkbenchPartReference partRef) {}
		public void partVisible(IWorkbenchPartReference partRef) {}		
	}

}
