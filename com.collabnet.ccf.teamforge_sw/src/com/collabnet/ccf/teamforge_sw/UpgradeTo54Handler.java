package com.collabnet.ccf.teamforge_sw;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.teamforge_sw.wizards.UpgradeTo54Wizard;
import com.collabnet.ccf.wizards.CustomWizardDialog;

public class UpgradeTo54Handler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection structuredSelection) {
			Object[] items = structuredSelection.toArray();
			for (Object item : items) {
				if (item instanceof Landscape || item instanceof ProjectMappings) {
					Landscape landscape = null;
					if (item instanceof ProjectMappings mappings) {
						landscape = mappings.getLandscape();
					} else {
						landscape = (Landscape)item;
					}
					UpgradeTo54Wizard wizard = new UpgradeTo54Wizard(landscape);
					WizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), wizard);
					if (dialog.open() != WizardDialog.OK) {
						break;
					}
				}
			}
		}
		return null;
	}

}
