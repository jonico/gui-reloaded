package com.collabnet.ccf.teamforge_sw.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.model.MappingGroup;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.teamforge.api.tracker.TrackerFieldDO;
import com.collabnet.teamforge.api.tracker.TrackerFieldValueDO;

public class SynchronizeTeamsSprintsWizard extends AbstractMappingWizard {
	private SynchronizeTeamsSprintsWizardPage wizardPage;
	private Exception error;

	public SynchronizeTeamsSprintsWizard(SynchronizationStatus projectMapping) {
		super(projectMapping);
	}

	public SynchronizeTeamsSprintsWizard(MappingGroup mappingGroup) {
		super(mappingGroup);
	}

	@Override
	public void addPages() {
		super.addPages();
		setWindowTitle("Synchronize Teams/Sprints");
		wizardPage = new SynchronizeTeamsSprintsWizardPage();
		addPage(wizardPage);
	}

	@Override
	public boolean performFinish() {
		if (0 == wizardPage.getAddedValues().size() && 0 == wizardPage.getDeletedValues().size()) {
			return true;
		}
		error = null;
		final List<TrackerFieldValueDO> couldNotBeDeletedList = new ArrayList<TrackerFieldValueDO>();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					List<String> productTeamSprints = wizardPage.getProductTeamsSprints();
					TrackerFieldDO teamsSprintsField = wizardPage.getTeamsSprintsField();		
					List<TrackerFieldValueDO> updatedValuesList = new ArrayList<TrackerFieldValueDO>();
					for (String productTeamSprint : productTeamSprints) {
						TrackerFieldValueDO fieldValue = new TrackerFieldValueDO(getSoapClient().supports60(), getSoapClient().supports50());
						fieldValue.setIsDefault(false);
						fieldValue.setValue(productTeamSprint);
						fieldValue.setId(wizardPage.getOldValuesMap().get(productTeamSprint));
						updatedValuesList.add(fieldValue);			
					}
					String taskName = "Synchronizing teams/sprints";
					int totalWork = 1 + wizardPage.getDeletedValues().size();
					monitor.setTaskName(taskName);
					monitor.beginTask(taskName, totalWork);
					for (TrackerFieldValueDO deletedValue : wizardPage.getDeletedValues()) {
						monitor.subTask("Checking deleted team/sprint ''" + deletedValue.getValue() + "''");
						if (getSoapClient().isFieldValueUsed(getTracker(), teamsSprintsField, deletedValue)) {
							int insertIndex = getInsertIndex(updatedValuesList, deletedValue);
							updatedValuesList.add(insertIndex, deletedValue);
							couldNotBeDeletedList.add(deletedValue);
						}			
						monitor.worked(1);
					}			
					TrackerFieldValueDO[] fieldValues = new TrackerFieldValueDO[updatedValuesList.size()];
					updatedValuesList.toArray(fieldValues);
					teamsSprintsField.setFieldValues(fieldValues);
					monitor.subTask("Updating tracker teams/sprints");
					getSoapClient().setField(getTracker(), teamsSprintsField);
					monitor.worked(1);
				} catch (Exception e) {
					error = e;
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, runnable);
		} catch (Exception e) {
			Activator.handleError(e);
			MessageDialog.openError(getShell(), "Synchronize Teams/Sprints", e.getMessage());
			return false;
		}
		if (null != error) {
			Activator.handleError(error);
			MessageDialog.openError(getShell(), "Synchronize Teams/Sprints", error.getMessage());
			return false;
		}
		wizardPage.refresh(true);
		if (0 < couldNotBeDeletedList.size()) {
			MessageDialog.openWarning(getShell(), "Synchronize Teams/Sprints", "One or more team/sprint could not be removed from tracker because it is used by one or more artifact.");
			return false;
		}
		return true;
	}
	
	private int getInsertIndex(List<TrackerFieldValueDO> updatedValuesList, TrackerFieldValueDO insertedValue) {
		int index = 0;
		for (TrackerFieldValueDO fieldValue : updatedValuesList) {
			if (0 < fieldValue.getValue().compareTo(insertedValue.getValue())) {
				break;
			}
			index++;
		}
		return index;
	}

}
