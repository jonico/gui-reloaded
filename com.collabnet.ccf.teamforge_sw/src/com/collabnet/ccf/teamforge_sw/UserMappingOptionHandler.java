package com.collabnet.ccf.teamforge_sw;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.db.Filter;
import com.collabnet.ccf.db.Update;
import com.collabnet.ccf.model.MappingGroup;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.sw.ScrumWorksCcfParticipant;
import com.collabnet.ccf.sw.ScrumWorksMappingSection;
import com.collabnet.ccf.teamforge_sw.dialogs.SetTaskPointPersonMappingOptionDialog;

public class UserMappingOptionHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection structuredSelection) {
			Object[] items = structuredSelection.toArray();
			for (Object item : items) {
				SynchronizationStatus projectMapping = null;
				if (item instanceof SynchronizationStatus status) {
					projectMapping = status;
				}
				if (item instanceof MappingGroup mappingGroup) {
					projectMapping = mappingGroup.getFirstMapping();
				}
				if (projectMapping != null) {
					CcfDataProvider ccfDataProvider = new CcfDataProvider();
					String product = ScrumWorksCcfParticipant.getProduct(projectMapping);
					try {
						SynchronizationStatus[] projectMappings = ccfDataProvider.getSynchronizationStatuses(projectMapping.getLandscape(), projectMapping.getProjectMappings());
						if (projectMappings != null) {
							SynchronizationStatus taskMapping = null;
							for (SynchronizationStatus mapping : projectMappings) {
								if (mapping.getSourceRepositoryId().equals(product + "-Task") || mapping.getTargetRepositoryId().equals(product + "-Task")) {
									taskMapping = mapping;									
									break;
								}
							}
							if (taskMapping != null) {
								boolean mapToPointPerson = taskMapping.getSourceRepositoryKind().equals(ScrumWorksMappingSection.TEMPLATE_TASKS_FLEX_FIELD);
								SetTaskPointPersonMappingOptionDialog dialog = new SetTaskPointPersonMappingOptionDialog(Display.getDefault().getActiveShell(), !mapToPointPerson);
								if (dialog.open() == SetTaskPointPersonMappingOptionDialog.OK) {
									for (SynchronizationStatus mapping : projectMappings) {
										if (mapping.getSourceRepositoryId().equals(product + "-Task") || mapping.getTargetRepositoryId().equals(product + "-Task")) {
											Filter sourceSystemFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_SYSTEM_ID, mapping.getSourceSystemId(), true);
											Filter sourceRepositoryFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_REPOSITORY_ID, mapping.getSourceRepositoryId(), true);
											Filter targetSystemFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_TARGET_SYSTEM_ID, mapping.getTargetSystemId(), true);
											Filter targetRepositoryFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_TARGET_REPOSITORY_ID, mapping.getTargetRepositoryId(), true);
											Filter[] filters = { sourceSystemFilter, sourceRepositoryFilter, targetSystemFilter, targetRepositoryFilter };	
											String template;
											if (dialog.isMapToAssignedToUser()) {
												template = ScrumWorksMappingSection.TEMPLATE_TASKS;
											} else {
												template = ScrumWorksMappingSection.TEMPLATE_TASKS_FLEX_FIELD;
											}
											mapping.setSourceRepositoryKind(template);
											Update mappingUpdate = new Update(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_REPOSITORY_KIND, template);
											Update[] updates = { mappingUpdate };						
											ccfDataProvider.updateSynchronizationStatuses(mapping.getLandscape(), updates, filters);
											Activator.notifyProjectMappingChangeListeners(mapping);
										}										
									}
								}
							} else {
								MessageDialog.openError(Display.getDefault().getActiveShell(), "Set Task Point Person Mapping Option", "No Task mapping found.");
							}
						}
						Activator.notifyChanged(projectMapping.getProjectMappings());
					} catch (Exception e) {
						Activator.handleError(e);
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Set Task Point Person Mapping Option", e.getMessage());
					}
				}
			}
		}
		return null;
	}

}
