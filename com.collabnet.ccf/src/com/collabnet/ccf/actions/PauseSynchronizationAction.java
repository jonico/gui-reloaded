package com.collabnet.ccf.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.model.MappingGroup;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;

public class PauseSynchronizationAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	private CcfDataProvider dataProvider;
	private List<ProjectMappings> projectMappingsList;
	
	public void run(IAction action) {
		projectMappingsList = new ArrayList<ProjectMappings>();
		dataProvider = new CcfDataProvider();
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				Iterator iter = fSelection.iterator();
				while (iter.hasNext()) {
					Object object = iter.next();
					if (object instanceof SynchronizationStatus status) {		
						try {
							dataProvider.pauseSynchronization(status);
							if (!projectMappingsList.contains(status.getProjectMappings())) {
								projectMappingsList.add(status.getProjectMappings());
							}
						} catch (Exception e) {
							Activator.handleDatabaseError(e, false, true, "Pause Synchronization");
							break;
						}
					}
					if (object instanceof MappingGroup mappingGroup) {
						try {
							pauseSynchronization(mappingGroup);
						} catch (Exception e) {
							Activator.handleDatabaseError(e, false, true, "Pause Synchronization");
							break;							
						}
					}
				}
			}			
		});
		for (ProjectMappings projectMappings: projectMappingsList) {
			Activator.notifyChanged(projectMappings);
		}
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection selection) {
			fSelection= selection;
			if (action != null) action.setEnabled(isEnabledForSelection());
		}
	}	
	
	@SuppressWarnings("unchecked")
	private boolean isEnabledForSelection() {
		if (fSelection == null || !Activator.getDefault().getActiveRole().isPauseSynchronization()) return false;
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof SynchronizationStatus status) {
				if (status.isPaused())
					return false;
			}
			if (object instanceof MappingGroup mappingGroup) {
				if (!hasRunningMappings(mappingGroup)) {
					return false;
				}
			}			
		}
		return true;
	}	
	
	private boolean hasRunningMappings(MappingGroup mappingGroup) {
		SynchronizationStatus[] childMappings = mappingGroup.getChildMappings();
		if (childMappings != null) {
			for (SynchronizationStatus status : childMappings) {
				if (!status.isPaused()) {
					return true;
				}
			}
		}
		MappingGroup[] childGroups = mappingGroup.getChildGroups();
		if (childGroups != null) {
			for (MappingGroup childGroup : childGroups) {
				if (hasRunningMappings(childGroup)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void pauseSynchronization(MappingGroup mappingGroup) throws Exception {
		SynchronizationStatus[] childMappings = mappingGroup.getChildMappings();
		if (childMappings != null) {
			for (SynchronizationStatus status : childMappings) {
				if (!status.isPaused()) {
					dataProvider.pauseSynchronization(status);
					if (!projectMappingsList.contains(status.getProjectMappings())) {
						projectMappingsList.add(status.getProjectMappings());
					}
				}
			}
		}
		MappingGroup[] childGroups = mappingGroup.getChildGroups();
		if (childGroups != null) {
			for (MappingGroup childGroup : childGroups) {
				pauseSynchronization(childGroup);
			}
		}		
	}
}
