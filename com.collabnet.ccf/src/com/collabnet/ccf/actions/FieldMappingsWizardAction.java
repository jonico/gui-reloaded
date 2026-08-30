package com.collabnet.ccf.actions;

import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.wizards.CustomWizardDialog;
import com.collabnet.ccf.wizards.EditFieldMappingsWizard;

public class FieldMappingsWizardAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof SynchronizationStatus) {
				SynchronizationStatus status = (SynchronizationStatus)object;
				EditFieldMappingsWizard wizard = new EditFieldMappingsWizard(status);
				if (Activator.getDefault().getPreferenceStore().getBoolean(Activator.PREFERENCES_GRAPHICAL_MAPPING_AVAILABLE)) {
					WizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), wizard);
					dialog.open();								
				} else {
					if (!wizard.edit(true)) return;
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fSelection= (IStructuredSelection) sel;
		}
		if (null != action) {
			action.setEnabled(Activator.getDefault().getActiveRole().isEditFieldMappings());
		}
	}	
}
