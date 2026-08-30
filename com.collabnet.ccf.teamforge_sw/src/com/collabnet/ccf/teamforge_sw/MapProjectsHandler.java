package com.collabnet.ccf.teamforge_sw;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.teamforge_sw.wizards.ProjectMappingWizard;
import com.collabnet.ccf.views.CcfExplorerView;
import com.collabnet.ccf.wizards.CustomWizardDialog;

public class MapProjectsHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			Object[] items = ((IStructuredSelection) selection).toArray();
			for (Object item : items) {
				if (item instanceof Landscape || item instanceof ProjectMappings) {
					Landscape landscape = null;
					ProjectMappings projectMappings = null;
					if (item instanceof ProjectMappings) {
						projectMappings = (ProjectMappings)item;
						landscape = projectMappings.getLandscape();
					} else {
						landscape = (Landscape)item;
					}
					ProjectMappingWizard wizard = new ProjectMappingWizard(landscape, projectMappings);
					WizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), wizard);
					if (WizardDialog.OK == dialog.open() && null != CcfExplorerView.getView()) {
						Activator.notifyChanged(projectMappings);
					}
				}
			}
		}
		return null;
	}

}
