package com.collabnet.ccf.actions;

import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.editors.CcfEditor;
import com.collabnet.ccf.editors.CcfEditorInput;
import com.collabnet.ccf.model.Landscape;

public class EditLandscapeAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof Landscape) {
				Landscape landscape = (Landscape)object;
				CcfEditorInput editorInput = new CcfEditorInput(landscape, CcfEditorInput.EDITOR_TYPE_LANDSCAPE);
				IWorkbenchPage page = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					page.openEditor(editorInput, CcfEditor.ID);
				} catch (PartInitException e) {
					Activator.handleError(e);
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fSelection= (IStructuredSelection) sel;
		}
		if (null != action) {
			action.setEnabled(Activator.getDefault().getActiveRole().isEditLandscape());
		}
	}	
}
