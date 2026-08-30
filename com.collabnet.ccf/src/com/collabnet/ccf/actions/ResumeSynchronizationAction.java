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

public class ResumeSynchronizationAction extends ActionDelegate {
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
					if (object instanceof SynchronizationStatus) {
						SynchronizationStatus status = (SynchronizationStatus)object;				
						try {						
							dataProvider.resumeSynchronization(status);
							if (!projectMappingsList.contains(status.getProjectMappings())) {
								projectMappingsList.add(status.getProjectMappings());
							}							
						} catch (Exception e) {
							Activator.handleDatabaseError(e, false, true, "Resume Synchronization");
							break;
						}
					}
					if (object instanceof MappingGroup) {
						MappingGroup mappingGroup = (MappingGroup)object;
						try {
							resumeSynchronization(mappingGroup);
						} catch (Exception e) {
							Activator.handleDatabaseError(e, false, true, "Resume Synchronization");
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
		if (sel instanceof IStructuredSelection) {
			fSelection= (IStructuredSelection) sel;
			if (null != action) action.setEnabled(isEnabledForSelection());
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean isEnabledForSelection() {
		if (null == fSelection || !Activator.getDefault().getActiveRole().isResumeSynchronization()) return false;
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof SynchronizationStatus) {
				SynchronizationStatus status = (SynchronizationStatus)object;
				if (!status.isPaused() || status.isHiddenMapping())
					return false;
			}
			if (object instanceof MappingGroup) {
				MappingGroup mappingGroup = (MappingGroup)object;
				if (!hasPausedMappings(mappingGroup)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean hasPausedMappings(MappingGroup mappingGroup) {
		SynchronizationStatus[] childMappings = mappingGroup.getChildMappings();
		if (null != childMappings) {
			for (SynchronizationStatus status : childMappings) {
				if (status.isPaused() && !status.isHiddenMapping()) {
					return true;
				}
			}
		}
		MappingGroup[] childGroups = mappingGroup.getChildGroups();
		if (null != childGroups) {
			for (MappingGroup childGroup : childGroups) {
				if (hasPausedMappings(childGroup)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void resumeSynchronization(MappingGroup mappingGroup) throws Exception {
		SynchronizationStatus[] childMappings = mappingGroup.getChildMappings();
		if (null != childMappings) {
			for (SynchronizationStatus status : childMappings) {
				if (status.isPaused() && !status.isHiddenMapping()) {
					dataProvider.resumeSynchronization(status);
					if (!projectMappingsList.contains(status.getProjectMappings())) {
						projectMappingsList.add(status.getProjectMappings());
					}
				}
			}
		}
		MappingGroup[] childGroups = mappingGroup.getChildGroups();
		if (null != childGroups) {
			for (MappingGroup childGroup : childGroups) {
				resumeSynchronization(childGroup);
			}
		}		
	}
}
