package com.collabnet.ccf.actions;

import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.editors.IdentityMappingEditor;
import com.collabnet.ccf.editors.IdentityMappingEditorInput;
import com.collabnet.ccf.model.IdentityMapping;

public class IdentityMappingEditAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		CcfDataProvider dataProvider = new CcfDataProvider();
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof IdentityMapping) {
				IdentityMapping identityMapping = (IdentityMapping)object;
				try {
					// Make sure we have latest.
					identityMapping = dataProvider.getIdentityMapping(identityMapping);
					IdentityMapping reverseMapping = dataProvider.getReverseIdentityMapping(identityMapping);
					IdentityMappingEditorInput editorInput = new IdentityMappingEditorInput(identityMapping, reverseMapping);
					IWorkbenchPage page = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
					page.openEditor(editorInput, IdentityMappingEditor.ID);
				} catch (Exception e) {
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
			action.setEnabled(Activator.getDefault().getActiveRole().isEditIdentityMapping());
		}
	}

}
