package com.collabnet.ccf.actions;

import java.util.Iterator;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.ide.FileStoreEditorInput;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.model.Log;

public class EditLogAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof Log log) {
				IWorkbenchPage page = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IEditorInput input = new FileStoreEditorInput(EFS.getLocalFileSystem().getStore(log.getLogFile().toURI()));
				try {
					page.openEditor(input, "org.eclipse.ui.DefaultTextEditor");
				} catch (PartInitException e) {
					Activator.handleError("Open Log", e);
					break;
				}
			}
		}
	}	
	
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection selection) {
			fSelection= selection;
		}
	}

}
