package com.collabnet.ccf.actions;

import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.ICcfParticipant;
import com.collabnet.ccf.dialogs.ReverseProjectMappingDialog;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.views.CcfExplorerView;

public class ReverseSynchronizationStatusAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof SynchronizationStatus) {
				SynchronizationStatus status = (SynchronizationStatus)object;
			
				try {
					ICcfParticipant p1 = Activator.getCcfParticipantForType(status.getSourceSystemKind());
					if (!p1.allowAsTargetRepository(status.getSourceRepositoryId())) {
						showReverseNotAllowedDialog(status);
						break;
					}
					ICcfParticipant p2 = Activator.getCcfParticipantForType(status.getTargetSystemKind());
					if (!p2.allowAsSourceRepository(status.getTargetRepositoryId())) {
						showReverseNotAllowedDialog(status);
						break;
					}
				} catch (Exception e) {}
				
				ProjectMappings projectMappings = status.getProjectMappings();
				ReverseProjectMappingDialog dialog = new ReverseProjectMappingDialog(Display.getDefault().getActiveShell(), projectMappings, status);
				if (ReverseProjectMappingDialog.OK == dialog.open() && null != CcfExplorerView.getView()) {
					Activator.notifyChanged(projectMappings);
				}
			}
		}
	}
	
	private void showReverseNotAllowedDialog(SynchronizationStatus status) {
		MessageDialog.openError(Display.getDefault().getActiveShell(), "Create Reverse Mapping", status.getTargetRepositoryId() + "=>" + status.getSourceRepositoryId()	+ " is not a supported project mapping.");
	}
	
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fSelection= (IStructuredSelection) sel;
		}
	}	

}
