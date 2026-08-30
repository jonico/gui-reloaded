package com.collabnet.ccf.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.db.Filter;
import com.collabnet.ccf.model.MappingGroup;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;

public class DeleteSynchronizationStatusAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	private boolean mappingsDeleted;
	private CcfDataProvider dataProvider = new CcfDataProvider();
	private List<ProjectMappings> projectMappingsList;
	
	public void run(IAction action) {
		if (!MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Delete Project Mapping", "Delete the selected project mappings?")) return;
		projectMappingsList = new ArrayList<ProjectMappings>();
		mappingsDeleted = false;
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				Iterator iter = fSelection.iterator();
				while (iter.hasNext()) {
					Object object = iter.next();
					if (object instanceof SynchronizationStatus) {
						SynchronizationStatus status = (SynchronizationStatus)object;
						try {
							deleteProjectMapping(status);
							mappingsDeleted = true;
							if (!projectMappingsList.contains(status.getProjectMappings())) {
								projectMappingsList.add(status.getProjectMappings());
							}
						} catch (Exception e) {
							Activator.handleDatabaseError(e, false, true, "Delete Project Mapping");
							break;
						}
					}
					if (object instanceof MappingGroup) {
						MappingGroup mappingGroup = (MappingGroup)object;
						try {
							deleteProjectMapping(mappingGroup);
						} catch (Exception e) {
							Activator.handleError(e);
							break;							
						}
					}
				}
			}			
		});
		if (mappingsDeleted) {
			for (ProjectMappings projectMappings: projectMappingsList) {
				Activator.notifyChanged(projectMappings);
			}
		}
	}
	
	private void deleteProjectMapping(MappingGroup mappingGroup) throws Exception {
		SynchronizationStatus[] childMappings = mappingGroup.getChildMappings();
		if (null != childMappings) {
			for (SynchronizationStatus status : childMappings) {
				deleteProjectMapping(status);
				mappingsDeleted = true;
				if (!projectMappingsList.contains(status.getProjectMappings())) {
					projectMappingsList.add(status.getProjectMappings());
				}
			}
		}
		SynchronizationStatus[] hiddenChildMappings = mappingGroup.getHiddenChildMappings();
		if (null != hiddenChildMappings) {
			for (SynchronizationStatus status : hiddenChildMappings) {
				deleteProjectMapping(status);
				mappingsDeleted = true;
				if (!projectMappingsList.contains(status.getProjectMappings())) {
					projectMappingsList.add(status.getProjectMappings());
				}
			}
		}
		MappingGroup[] childGroups = mappingGroup.getChildGroups();
		if (null != childGroups) {
			for (MappingGroup childGroup : childGroups) {
				deleteProjectMapping(childGroup);
			}
		}		
	}
	
	private void deleteProjectMapping(SynchronizationStatus status) throws Exception {
		Filter sourceSystemFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_SYSTEM_ID, status.getSourceSystemId(), true);
		Filter sourceRepositoryFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_REPOSITORY_ID, status.getSourceRepositoryId(), true);
		Filter targetSystemFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_TARGET_SYSTEM_ID, status.getTargetSystemId(), true);
		Filter targetRepositoryFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_TARGET_REPOSITORY_ID, status.getTargetRepositoryId(), true);
		Filter[] filters = { sourceSystemFilter, sourceRepositoryFilter, targetSystemFilter, targetRepositoryFilter };		
		dataProvider.deleteSynchronizationStatuses(status.getLandscape(), filters);
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fSelection= (IStructuredSelection) sel;
		}
		if (null != action) {
			action.setEnabled(Activator.getDefault().getActiveRole().isDeleteProjectMapping());
		}
	}	
}
