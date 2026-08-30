package com.collabnet.ccf.actions;

import java.io.File;
import java.util.Iterator;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.editors.ExternalFileEditorInput;
import com.collabnet.ccf.model.Landscape;

public class EditLog4jAction extends ActionDelegate {
	private IStructuredSelection fSelection;

	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof Landscape landscape) {
				File log4jFile = landscape.getLog4jFile();
				if (log4jFile == null) {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Edit CCF Log Settings", "Log settings file not found.");
					return;
				}
				if (!log4jFile.exists()) {
					File log4jRenameFile = landscape.getLog4jRenameFile();
					if (log4jRenameFile == null || !log4jRenameFile.exists()) {
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Edit CCF Log Settings", "Log settings file not found.");
						return;						
					}
					if (!MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "Edit CCF Log Settings", "File log4j.xml does not exist.  Do you want to rename file log4j.xml.rename_me?")) {
						return;
					}
					log4jRenameFile.renameTo(log4jFile);
				}
				IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path(log4jFile.getAbsolutePath()));
				final IEditorInput input = new ExternalFileEditorInput(fileStore, log4jFile.getName());
				IEditorRegistry registry = Activator.getDefault().getWorkbench().getEditorRegistry();
				IEditorDescriptor descriptor = registry.getDefaultEditor(log4jFile.getName());
				String id;
				if (descriptor == null) {
					id = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
				} else {
					id = descriptor.getId();
				}
				IWorkbenchPage page = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					page.openEditor(input, id);
				} catch (PartInitException e) {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Edit CCF Log Settings", "Unable to open editor:\n\n" + e.getMessage());
					Activator.handleError(e);
					return;
				}
				
			}
		}
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection selection) {
			fSelection= selection;
		}
		if (action != null) {
			action.setEnabled(Activator.getDefault().getActiveRole().isEditLogSettings());
		}
	}	
}
