package com.collabnet.ccf.actions;

import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.MappingGroup;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.views.CcfExplorerView;
import com.collabnet.ccf.wizards.CustomWizardDialog;
import com.collabnet.ccf.wizards.NewProjectMappingWizard;

public class AddSynchronizationStatusAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof ProjectMappings || object instanceof MappingGroup) {
				NewProjectMappingWizard wizard;
				ProjectMappings projectMappings;
				if (object instanceof MappingGroup mappingGroup) {	
					projectMappings = mappingGroup.getProjectMappingsParent();
					wizard = new NewProjectMappingWizard(mappingGroup);					
				} else {
					projectMappings = (ProjectMappings)object;	
					wizard = new NewProjectMappingWizard(projectMappings);
				}
				String dialogId = null;
				if (projectMappings != null) {
					Landscape landscape = projectMappings.getLandscape();
					if (landscape != null) {
						dialogId = landscape.getType1() + "_" + landscape.getType2();
					}
				}
				WizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), wizard, dialogId);
				if (dialog.open() == WizardDialog.OK && CcfExplorerView.getView() != null) {
					Activator.notifyChanged(projectMappings);
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection selection) {
			fSelection= selection;
		}
		if (action != null) {
			action.setEnabled(Activator.getDefault().getActiveRole().isAddProjectMapping());
		}
	}	
}
