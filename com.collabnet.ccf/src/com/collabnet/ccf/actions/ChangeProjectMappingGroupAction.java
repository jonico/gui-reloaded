package com.collabnet.ccf.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.dialogs.ChangeProjectMappingGroupDialog;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;

public class ChangeProjectMappingGroupAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		List<ProjectMappings> projectMappingsList = new ArrayList<ProjectMappings>();
		List<SynchronizationStatus> projectMappingList = new ArrayList<SynchronizationStatus>();
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof SynchronizationStatus status) {
				projectMappingList.add(status);
				if (!projectMappingsList.contains(status.getProjectMappings())) {
					projectMappingsList.add(status.getProjectMappings());
				}
			}
		}
		SynchronizationStatus[] projectMappingArray = new SynchronizationStatus[projectMappingList.size()];
		projectMappingList.toArray(projectMappingArray);
		ChangeProjectMappingGroupDialog dialog = new ChangeProjectMappingGroupDialog(Display.getDefault().getActiveShell(), projectMappingArray);
		if (dialog.open() == ChangeProjectMappingGroupDialog.CANCEL) {
			return;
		}
		for (ProjectMappings projectMappings: projectMappingsList) {
			Activator.notifyChanged(projectMappings);
		}
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection selection) {
			fSelection= selection;
		}
		if (action != null) {
			action.setEnabled(Activator.getDefault().getActiveRole().isChangeProjectMapping());
		}
	}	
}
