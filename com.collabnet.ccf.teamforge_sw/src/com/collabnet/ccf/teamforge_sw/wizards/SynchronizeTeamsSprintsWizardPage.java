package com.collabnet.ccf.teamforge_sw.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.teamforge.api.tracker.TrackerFieldDO;
import com.collabnet.teamforge.api.tracker.TrackerFieldValueDO;
import com.danube.scrumworks.api2.client.Product;
import com.danube.scrumworks.api2.client.ScrumWorksException;
import com.danube.scrumworks.api2.client.Sprint;
import com.danube.scrumworks.api2.client.Team;

public class SynchronizeTeamsSprintsWizardPage extends WizardPage {
	private List<String> productTeamsSprints;
	private TrackerFieldDO teamsSprintsField;
	private TrackerFieldValueDO[] trackerTeamsSprints;
	private Exception getProductTeamsSprintsError;
	private Exception getTrackerTeamsSprintsError;	
	private Exception unknownError;
	private List<TrackerFieldValueDO> deletedValues;
	private List<String> addedValues;
	private Map<String, String> oldValuesMap;
	private Group addGroup;
	private Group deleteGroup;
	private org.eclipse.swt.widgets.List addedValuesList;
	private org.eclipse.swt.widgets.List deletedValuesList;

	public SynchronizeTeamsSprintsWizardPage() {
		super("mainPage", "Synchronize Teams/Sprints", Activator.getDefault().getImageDescriptor(Activator.IMAGE_NEW_PROJECT_MAPPING_WIZBAN));
	}
	
	public void createControl(Composite parent) {
		Composite outerContainer = new Composite(parent,SWT.NONE);
		outerContainer.setLayout(new GridLayout());
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
				
		getTeamsSprints();
		
		if (0 < addedValues.size()) {
			addGroup = new Group(outerContainer,SWT.NONE);
			addGroup.setLayout(new GridLayout());
			addGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
			addGroup.setText("Add Teams/Sprints to tracker:");
			addedValuesList = new org.eclipse.swt.widgets.List(addGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			addedValuesList.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
		}
		
		if (0 < deletedValues.size()) {
			deleteGroup = new Group(outerContainer,SWT.NONE);
			deleteGroup.setLayout(new GridLayout());
			deleteGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
			deleteGroup.setText("Remove Teams/Sprints from tracker:");
			deletedValuesList = new org.eclipse.swt.widgets.List(deleteGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			deletedValuesList.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
		}
		
		if (0 < addedValues.size() || 0 < deletedValues.size()) {
			refresh(false);
		}

		setControl(outerContainer);
	}
	
	public void refresh(boolean getTeamsSprints) {
		if (getTeamsSprints) {
			getTeamsSprints();
		}
		if (null != addGroup) {
			addedValuesList.removeAll();
			for (String teamSprint : addedValues) {
				try {
					addedValuesList.add(teamSprint);
				} catch (Exception e) {}
			}
		}
		if (null != deleteGroup) {
			deletedValuesList.removeAll();
			for (TrackerFieldValueDO fieldValue : deletedValues) {
				deletedValuesList.add(fieldValue.getValue());
			}
		}
	}
	
	private void getTeamsSprints() {
		getProductTeamsSprintsError = null;
		getTrackerTeamsSprintsError = null;
		unknownError = null;
		deletedValues = new ArrayList<TrackerFieldValueDO>();	
		addedValues = new ArrayList<String>();
		oldValuesMap = new HashMap<String, String>();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				SynchronizeTeamsSprintsWizard wizard = (SynchronizeTeamsSprintsWizard)getWizard();
				String taskName = "Retrieving teams/sprints";
				monitor.setTaskName(taskName);
				monitor.beginTask(taskName, 3);
				monitor.subTask("SWP product teams/sprints");
				try {
					Product product = null;
					product =  wizard.getScrumWorksEndpoint().getProductById(getProductId());
					if (null == product) {
						product =  wizard.getScrumWorksEndpoint().getProductByName(getProduct());
					}
					monitor.worked(1);
					productTeamsSprints = getTeamSprintValues(product);
					monitor.worked(1);
				} catch (Exception e) {
					Activator.handleError(e);
					getProductTeamsSprintsError = e;
					return;
				}	
				monitor.subTask("TeamForge tracker teams/sprints");
				try {
					TrackerFieldDO[] fields = ((SynchronizeTeamsSprintsWizard)getWizard()).getSoapClient().getFields(getTracker());
					monitor.worked(1);
					teamsSprintsField = null;
					for (TrackerFieldDO field : fields) {
						if (field.getName().equals("Team/Sprint")) {
							teamsSprintsField = field;
						}
					}
					if (null == teamsSprintsField) {
						setErrorMessage("Team/Sprint field not defined for tracker " + getTracker() + ".");
						return;
					}
					trackerTeamsSprints = teamsSprintsField.getFieldValues();
					
					List<String> newValuesList = new ArrayList<String>();	
					if (null != productTeamsSprints) {
						for (String productTeamSprint : productTeamsSprints) {
							newValuesList.add(productTeamSprint);
						}
					}
					for (TrackerFieldValueDO oldValue : teamsSprintsField.getFieldValues()) {
						oldValuesMap.put(oldValue.getValue(), oldValue.getId());
						if (!newValuesList.contains(oldValue.getValue())) {
							deletedValues.add(oldValue);
						}
					}
					if (null != productTeamsSprints) {
						for (String productTeamSprint : productTeamsSprints) {
							if (null == oldValuesMap.get(productTeamSprint)) {
								addedValues.add(productTeamSprint);
							}
						}
					}
					
				} catch (Exception e) {
					Activator.handleError(e);
					getTrackerTeamsSprintsError = e;
					return;
				}								
				monitor.done();
			}		
		};
		
		try {
			getContainer().run(true, false, runnable);
		} catch (Exception e) {
			Activator.handleError(e);
			unknownError = e;
		}
		
		if (null != getProductTeamsSprintsError) {
			setErrorMessage("An unexpected error occurred while getting SWP product teams/sprints.  See error log for details.");
		}
		else if (null != getTrackerTeamsSprintsError) {
			setErrorMessage("An unexpected error occurred while getting TeamForge tracker teams/sprints.  See error log for details.");
		}
		else if (null != unknownError) {
			setErrorMessage("An unexpected error occurred while getting teams/sprints.  See error log for details.");
		}
		else if (0 == addedValues.size() && 0 == deletedValues.size()) {
			setMessage("No differences found between TeamForge tracker teams/sprints and SWP product teams/sprints.");
		} else {
			setMessage("Synchronize TeamForge tracker teams/sprints with SWP product teams/sprints.");
		}
		setPageComplete(productTeamsSprints != null && trackerTeamsSprints != null);
	}
	
	private List<String> getTeamSprintValues(Product product) throws MalformedURLException, ScrumWorksException {
		SynchronizeTeamsSprintsWizard wizard = (SynchronizeTeamsSprintsWizard)getWizard();
		Landscape landscape = wizard.getProjectMapping().getLandscape();
		String swpTimezone;
		if (landscape.getType1().equals("SWP")) {
			swpTimezone = landscape.getTimezone1();
		} else {
			swpTimezone = landscape.getTimezone2();
		}
		Map<Long, Team> teamMap = new HashMap<Long, Team>();
		List<String> teamSprintList = new ArrayList<String>();
		List<Sprint> sprints = getSprints(product);
		if (null != sprints) {
			for (Sprint sprint : sprints) {
				Team team = teamMap.get(sprint.getTeamId());
				if (null == team) {
					team = getTeam(sprint.getTeamId());
					if (null != team) {
						teamMap.put(sprint.getTeamId(), team);
					}
				}
				if (null != team) {
					teamSprintList.add(com.collabnet.ccf.teamforge_sw.Activator.getTeamSprintStringRepresentation(sprint, team, swpTimezone));
				}
			}
		}
		return teamSprintList;
	}
	
	private List<Sprint> getSprints(Product product) throws MalformedURLException, ScrumWorksException {
		SynchronizeTeamsSprintsWizard wizard = (SynchronizeTeamsSprintsWizard)getWizard();
		return wizard.getScrumWorksEndpoint().getSprintsForProduct(product.getId());
	}
	
	private Team getTeam(Long teamId) throws MalformedURLException, ScrumWorksException {
		SynchronizeTeamsSprintsWizard wizard = (SynchronizeTeamsSprintsWizard)getWizard();
		return wizard.getScrumWorksEndpoint().getTeamById(teamId);
	}
	
	private String getProduct() {
		return ((AbstractMappingWizard)getWizard()).getProduct();
	}
	
	private Long getProductId() {
		return ((AbstractMappingWizard)getWizard()).getProductId();
	}
	
	private String getTracker() {
		return ((AbstractMappingWizard)getWizard()).getTracker();
	}
	
	public List<String> getProductTeamsSprints() {
		return productTeamsSprints;
	}
	
	public TrackerFieldDO getTeamsSprintsField() {
		return teamsSprintsField;
	}

	public List<TrackerFieldValueDO> getDeletedValues() {
		return deletedValues;
	}

	public List<String> getAddedValues() {
		return addedValues;
	}

	public Map<String, String> getOldValuesMap() {
		return oldValuesMap;
	}

}