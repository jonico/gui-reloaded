package com.collabnet.ccf.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.ICcfParticipant;
import com.collabnet.ccf.editors.CcfEditor;
import com.collabnet.ccf.editors.CcfEditorInput;
import com.collabnet.ccf.wizards.CustomWizardDialog;
import com.collabnet.ccf.wizards.NewLandscapeWizard;

public class NewLandscapeAction extends Action {

	public NewLandscapeAction(String text) {
		super(text);
	}

	@Override
	public String getToolTipText() {
		return "New CCF landscape";
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return Activator.getDefault().getImageDescriptor(Activator.IMAGE_NEW_LANDSCAPE);
	}

	@Override
	public void run() {
		try {
			ICcfParticipant[] ccfParticipants = Activator.getCcfParticipants();
			if (null == ccfParticipants || 0 == ccfParticipants.length) {
				MessageDialog.openError(Display.getDefault().getActiveShell(), "New CCF Landscape", "A CCF Landscape cannot be created because there are no CCF participants installed.");
				return;
			}
		} catch (Exception e) {
			Activator.handleError(e);
		}
		NewLandscapeWizard wizard = new NewLandscapeWizard();
		WizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), wizard);
		dialog.open();
		if (null != wizard.getNewLandscape()) {
			CcfEditorInput editorInput = new CcfEditorInput(wizard.getNewLandscape(), CcfEditorInput.EDITOR_TYPE_LANDSCAPE);
			IWorkbenchPage page = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				page.openEditor(editorInput, CcfEditor.ID);
			} catch (PartInitException e) {
				Activator.handleError(e);
			}			
		}
	}

}
