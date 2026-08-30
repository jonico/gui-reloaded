package com.collabnet.ccf.teamforge_sw.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.sw.ScrumWorksMappingSection;
import com.collabnet.ccf.teamforge.schemageneration.TFSoapClient;
import com.collabnet.teamforge.api.tracker.TrackerDO;
import com.collabnet.teamforge.api.tracker.TrackerFieldDO;
import com.collabnet.teamforge.api.tracker.TrackerFieldValueDO;
import com.danube.scrumworks.api2.client.Program;
import com.danube.scrumworks.api2.client.ScrumWorksAPIService;
import com.danube.scrumworks.api2.client.ScrumWorksException;
import com.danube.scrumworks.api2.client.Sprint;
import com.danube.scrumworks.api2.client.Team;
import com.danube.scrumworks.api2.client.Theme;

public class FixTrackersWizardPage extends WizardPage {
	private TrackerDO pbiTracker;
	private TrackerDO taskTracker;
	private TrackerFieldDO backlogEffortField;
	private TrackerFieldDO benefitField;
	private TrackerFieldDO penaltyField;
	private TrackerFieldDO pointPersonField;
	private TrackerFieldDO sprintEndField;
	private TrackerFieldDO sprintStartField;
	private TrackerFieldDO swpKeyField;
	private TrackerFieldDO teamSprintField;
	private TrackerFieldDO themeField;
	
	private TrackerFieldDO pbiStatusField;
	private TrackerFieldDO pbiGroupField;
	private TrackerFieldDO pbiCustomerField;
	private TrackerFieldDO pbiReportedInReleaseField;
	private TrackerFieldDO pbiResolvedInReleaseField;
	private TrackerFieldDO pbiEstimatedEffortField;
	private TrackerFieldDO pbiActualEffortField;
	
	private TrackerFieldDO taskStatusField;
	private TrackerFieldDO taskGroupField;
	private TrackerFieldDO taskCustomerField;
	private TrackerFieldDO taskReportedInReleaseField;
	private TrackerFieldDO taskResolvedInReleaseField;
	private TrackerFieldDO taskAutosummingField;
	private TrackerFieldDO taskActualEffortField;
	
	private boolean pbiGroupEnabled;
	private boolean pbiCustomerEnabled;
	private boolean pbiReportedInReleaseEnabled;
	private boolean pbiResolvedInReleaseEnabled;
	private boolean pbiEstimatedEffortEnabled;
	private boolean pbiActualEffortEnabled;
	private boolean taskGroupEnabled;
	private boolean taskCustomerEnabled;
	private boolean taskReportedInReleaseEnabled;
	private boolean taskResolvedInReleaseEnabled;
	private boolean taskAutosummingEnabled;
	private boolean taskActualEffortEnabled;
	private boolean benefitExists;
	private boolean benefitTypeCorrect;
	private boolean penaltyExists;
	private boolean penaltyTypeCorrect;
	private boolean backlogEffortExists;
	private boolean backlogEffortTypeCorrect;
	private boolean swpKeyExists;
	private boolean swpKeyTypeCorrect;
	private boolean teamSprintExists;
	private boolean teamSprintTypeCorrect;
	private boolean sprintStartExists;
	private boolean sprintStartTypeCorrect;
	private boolean sprintEndExists;
	private boolean sprintEndTypeCorrect;
	private boolean themeExists;
	private boolean themeTypeCorrect;
	private boolean pointPersonExists;
	private boolean pointPersonTypeCorrect;
	private boolean pbiStatusValuesCorrect;
	private boolean taskStatusValuesCorrect;
	
	private boolean mapToAssignedToUser;
	
	private List<TrackerProblem> pbiProblems;
	private List<TrackerProblem> taskProblems;
	private List<TrackerProblem> pbiProblemsToFix;
	private List<TrackerProblem> taskProblemsToFix;

	private Exception checkTrackersError;
	private Exception fixProblemsError;
	
	private Table pbiTable;
	private TableViewer pbiViewer;
	private Table taskTable;
	private TableViewer taskViewer;
	private String[] columnHeaders = {"Field", "Problem"};
	private ColumnLayoutData columnLayouts[] = {
	    new ColumnWeightData(150, 150, true),
		new ColumnWeightData(650, 650, true)};
	
	private Map<Long, Program> programMap;
	
	private ScrumWorksAPIService scrumWorksEndpoint;
	
	private final static String TEXT = "text";
	private final static String SINGLE_SELECT = "single-select";
	private final static String DATE = "date";
	private final static String MULTI_SELECT = "multi-select";
	private final static String FIELD_DOES_NOT_EXIST = "Field does not exist in tracker";
	private final static String FIELD_NOT_DISABLED = "Field is enabled when it should be disabled (warning only - will not break integration)";
	private final static String FIELD_VALUES_WRONG = "Field values are either missing or incorrectly defined (wrong open/close status class)";
	
	public FixTrackersWizardPage() {
		super("mainPage", "Fix TeamForge Trackers", Activator.getDefault().getImageDescriptor(Activator.IMAGE_NEW_PROJECT_MAPPING_WIZBAN));
	}

	public void createControl(Composite parent) {
		Composite outerContainer = new Composite(parent,SWT.NONE);
		outerContainer.setLayout(new GridLayout());
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		checkTrackers();
		
		if (hasPbiProblems()) {
			Group pbiGroup = new Group(outerContainer, SWT.NONE);
			pbiGroup.setText(pbiTracker.getId() + ": " + pbiTracker.getTitle());
			GridLayout pbiLayout = new GridLayout();
			pbiLayout.numColumns = 1;
			pbiGroup.setLayout(pbiLayout);
			pbiGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));

			Label pbiMessageLabel = new Label(pbiGroup, SWT.NONE);
			pbiMessageLabel.setText("Check the problems that you wish to fix now:");
			
			pbiProblems = new ArrayList<TrackerProblem>();
			
			if (!benefitTypeCorrect) {
				String problemDescription;
				if (!benefitExists) {
					problemDescription = FIELD_DOES_NOT_EXIST;
				}
				else {
					problemDescription = getWrongTypeMessage(benefitField, TEXT);
				}
				TrackerProblem problem = new TrackerProblem(pbiTracker.getId(), "Benefit", problemDescription);
				pbiProblems.add(problem);
			}
			if (!penaltyTypeCorrect) {
				String problemDescription;
				if (!penaltyExists) {
					problemDescription = FIELD_DOES_NOT_EXIST;
				}
				else {
					problemDescription = getWrongTypeMessage(penaltyField, TEXT);
				}
				TrackerProblem problem = new TrackerProblem(pbiTracker.getId(), "Penalty", problemDescription);
				pbiProblems.add(problem);
			}
			if (!backlogEffortTypeCorrect) {
				String problemDescription;
				if (!backlogEffortExists) {
					problemDescription = FIELD_DOES_NOT_EXIST;
				}
				else {
					problemDescription = getWrongTypeMessage(backlogEffortField, TEXT);
				}
				TrackerProblem problem = new TrackerProblem(pbiTracker.getId(), "Backlog Effort", problemDescription);
				pbiProblems.add(problem);
			}
			if (!swpKeyTypeCorrect) {
				String problemDescription;
				if (!swpKeyExists) {
					problemDescription = FIELD_DOES_NOT_EXIST;
				}
				else {
					problemDescription = getWrongTypeMessage(swpKeyField, TEXT);
				}
				TrackerProblem problem = new TrackerProblem(pbiTracker.getId(), "SWP-Key", problemDescription);
				pbiProblems.add(problem);
			}
			if (!teamSprintTypeCorrect) {
				String problemDescription;
				if (!teamSprintExists) {
					problemDescription = FIELD_DOES_NOT_EXIST;
				}
				else {
					problemDescription = getWrongTypeMessage(teamSprintField, SINGLE_SELECT);
				}
				TrackerProblem problem = new TrackerProblem(pbiTracker.getId(), "Team/Sprint", problemDescription);
				pbiProblems.add(problem);
			}
			if (!sprintStartTypeCorrect) {
				String problemDescription;
				if (!sprintStartExists) {
					problemDescription = FIELD_DOES_NOT_EXIST;
				}
				else {
					problemDescription = getWrongTypeMessage(sprintStartField, DATE);
				}
				TrackerProblem problem = new TrackerProblem(pbiTracker.getId(), "Sprint Start", problemDescription);
				pbiProblems.add(problem);
			}
			if (!sprintEndTypeCorrect) {
				String problemDescription;
				if (!sprintEndExists) {
					problemDescription = FIELD_DOES_NOT_EXIST;
				}
				else {
					problemDescription = getWrongTypeMessage(sprintEndField, DATE);
				}
				TrackerProblem problem = new TrackerProblem(pbiTracker.getId(), "Sprint End", problemDescription);
				pbiProblems.add(problem);
			}
			if (!themeTypeCorrect) {
				String problemDescription;
				if (!themeExists) {
					problemDescription = FIELD_DOES_NOT_EXIST;
				}
				else {
					problemDescription = getWrongTypeMessage(themeField, MULTI_SELECT);
				}
				TrackerProblem problem = new TrackerProblem(pbiTracker.getId(), "Themes", problemDescription);
				pbiProblems.add(problem);
			}
			if (!pbiStatusValuesCorrect) {
				TrackerProblem problem = new TrackerProblem(pbiTracker.getId(), "Status", FIELD_VALUES_WRONG);
				pbiProblems.add(problem);
			}
			if (pbiGroupEnabled) {
				TrackerProblem problem = new TrackerProblem(pbiTracker.getId(), "Group", FIELD_NOT_DISABLED);
				pbiProblems.add(problem);
			}
			if (pbiCustomerEnabled) {
				TrackerProblem problem = new TrackerProblem(pbiTracker.getId(), "Customer", FIELD_NOT_DISABLED);
				pbiProblems.add(problem);
			}
			if (pbiReportedInReleaseEnabled) {
				TrackerProblem problem = new TrackerProblem(pbiTracker.getId(), "Reported in Release", FIELD_NOT_DISABLED);
				pbiProblems.add(problem);
			}
			if (pbiResolvedInReleaseEnabled) {
				TrackerProblem problem = new TrackerProblem(pbiTracker.getId(), "Fixed in Release", FIELD_NOT_DISABLED);
				pbiProblems.add(problem);
			}
			if (pbiEstimatedEffortEnabled) {
				TrackerProblem problem = new TrackerProblem(pbiTracker.getId(), "Estimated Effort", FIELD_NOT_DISABLED);
				pbiProblems.add(problem);
			}
			if (pbiActualEffortEnabled) {
				TrackerProblem problem = new TrackerProblem(pbiTracker.getId(), "Actual Effort", FIELD_NOT_DISABLED);
				pbiProblems.add(problem);
			}
			
			pbiTable = new Table(pbiGroup, SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
			pbiTable.setLinesVisible(true);
			GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
			gd.widthHint = 500;
			gd.heightHint = 200;
			pbiTable.setLayoutData(gd);
			TableLayout tableLayout = new TableLayout();
			pbiTable.setLayout(tableLayout);
			pbiTable.setHeaderVisible(true);
			pbiViewer = new TableViewer(pbiTable);
			pbiViewer.setContentProvider(new TrackerProblemContentProvider(pbiProblems));
			pbiViewer.setLabelProvider(new TrackerProblemLabelProvider());
			for (int i = 0; i < columnHeaders.length; i++) {
				tableLayout.addColumnData(columnLayouts[i]);
				TableColumn tc = new TableColumn(pbiTable, SWT.NONE,i);
				tc.setResizable(columnLayouts[i].resizable);
				tc.setText(columnHeaders[i]);
			}
			pbiViewer.setInput(this);	
			TableItem[] items = pbiTable.getItems();
			for (TableItem item : items) {
				item.setChecked(true);
			}
		}
		
		if (hasTaskProblems()) {
			Group taskGroup = new Group(outerContainer, SWT.NONE);
			taskGroup.setText(taskTracker.getId() + ": " + taskTracker.getTitle());
			GridLayout taskLayout = new GridLayout();
			taskLayout.numColumns = 1;
			taskGroup.setLayout(taskLayout);
			taskGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));

			Label taskMessageLabel = new Label(taskGroup, SWT.NONE);
			taskMessageLabel.setText("Check the problems that you wish to fix now:");
			
			taskProblems = new ArrayList<TrackerProblem>();
			
			if (!pointPersonTypeCorrect) {
				String problemDescription;
				if (!pointPersonExists) {
					problemDescription = FIELD_DOES_NOT_EXIST;
				}
				else {
					problemDescription = getWrongTypeMessage(pointPersonField, TEXT);
				}
				TrackerProblem problem = new TrackerProblem(taskTracker.getId(), "Point Person", problemDescription);
				taskProblems.add(problem);
			}
			if (!taskStatusValuesCorrect) {
				TrackerProblem problem = new TrackerProblem(taskTracker.getId(), "Status", FIELD_VALUES_WRONG);
				taskProblems.add(problem);
			}
			if (taskGroupEnabled) {
				TrackerProblem problem = new TrackerProblem(taskTracker.getId(), "Group", FIELD_NOT_DISABLED);
				taskProblems.add(problem);
			}
			if (taskCustomerEnabled) {
				TrackerProblem problem = new TrackerProblem(taskTracker.getId(), "Customer", FIELD_NOT_DISABLED);
				taskProblems.add(problem);
			}
			if (taskReportedInReleaseEnabled) {
				TrackerProblem problem = new TrackerProblem(taskTracker.getId(), "Reported in Release", FIELD_NOT_DISABLED);
				taskProblems.add(problem);
			}
			if (taskResolvedInReleaseEnabled) {
				TrackerProblem problem = new TrackerProblem(taskTracker.getId(), "Fixed in Release", FIELD_NOT_DISABLED);
				taskProblems.add(problem);
			}
			if (taskAutosummingEnabled) {
				TrackerProblem problem = new TrackerProblem(taskTracker.getId(), "Calculate Effort", FIELD_NOT_DISABLED);
				taskProblems.add(problem);
			}
			if (taskActualEffortEnabled) {
				TrackerProblem problem = new TrackerProblem(taskTracker.getId(), "Actual Effort", FIELD_NOT_DISABLED);
				taskProblems.add(problem);
			}
			taskTable = new Table(taskGroup, SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
			taskTable.setLinesVisible(true);
			GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
			gd.widthHint = 500;
			gd.heightHint = 200;
			taskTable.setLayoutData(gd);
			TableLayout tableLayout = new TableLayout();
			taskTable.setLayout(tableLayout);
			taskTable.setHeaderVisible(true);
			taskViewer = new TableViewer(taskTable);
			taskViewer.setContentProvider(new TrackerProblemContentProvider(taskProblems));
			taskViewer.setLabelProvider(new TrackerProblemLabelProvider());
			for (int i = 0; i < columnHeaders.length; i++) {
				tableLayout.addColumnData(columnLayouts[i]);
				TableColumn tc = new TableColumn(taskTable, SWT.NONE,i);
				tc.setResizable(columnLayouts[i].resizable);
				tc.setText(columnHeaders[i]);
			}
			taskViewer.setInput(this);
			TableItem[] items = taskTable.getItems();
			for (TableItem item : items) {
				item.setChecked(true);
			}
		}
		
		setControl(outerContainer);
	}
	
	private String getWrongTypeMessage(TrackerFieldDO field, String correctType) {
		return "Field type is " + field.getFieldType() + " when it should be " + correctType;
	}
	
	private boolean hasPbiProblems() {
		if (null == pbiTracker) {
			return false;
		}
		return pbiGroupEnabled ||
		pbiCustomerEnabled ||
		pbiReportedInReleaseEnabled ||
		pbiResolvedInReleaseEnabled ||
		pbiEstimatedEffortEnabled ||
		pbiActualEffortEnabled ||
		!benefitTypeCorrect ||
		!penaltyTypeCorrect ||
		!backlogEffortTypeCorrect ||
		!swpKeyTypeCorrect ||
		!teamSprintTypeCorrect ||
		!sprintStartTypeCorrect ||
		!sprintEndTypeCorrect ||
		!themeTypeCorrect ||
		!pbiStatusValuesCorrect;
	}
	
	private boolean hasTaskProblems() {
		if (null == taskTracker) {
			return false;
		}
		return taskGroupEnabled ||
		taskCustomerEnabled ||
		taskReportedInReleaseEnabled ||
		taskResolvedInReleaseEnabled ||
		taskAutosummingEnabled ||
		taskActualEffortEnabled ||
		!pointPersonTypeCorrect ||
		!taskStatusValuesCorrect;
	}
	
	private void initializeFlags() {
		pbiTracker = null;
		taskTracker = null;
		checkTrackersError = null;
		pbiGroupEnabled = false;
		pbiCustomerEnabled = false;
		pbiReportedInReleaseEnabled = false;
		pbiResolvedInReleaseEnabled = false;
		pbiEstimatedEffortEnabled = false;
		pbiActualEffortEnabled = false;
		taskGroupEnabled = false;
		taskCustomerEnabled = false;
		taskReportedInReleaseEnabled = false;
		taskResolvedInReleaseEnabled = false;
		taskAutosummingEnabled = false;
		taskActualEffortEnabled = false;
		benefitExists = false;
		benefitTypeCorrect = false;
		penaltyExists = false;
		penaltyTypeCorrect = false;
		backlogEffortExists = false;
		backlogEffortTypeCorrect = false;
		swpKeyExists = false;
		swpKeyTypeCorrect = false;
		teamSprintExists = false;
		teamSprintTypeCorrect = false;
		sprintStartExists = false;
		sprintStartTypeCorrect = false;
		sprintEndExists = false;
		sprintEndTypeCorrect = false;
		themeExists = false;
		themeTypeCorrect = false;
		pointPersonExists = false;
		pointPersonTypeCorrect = false;		
		pbiStatusValuesCorrect = false;
		taskStatusValuesCorrect = false;
	}
	
	public boolean fixProblems() {
		fixProblemsError = null;
		FixTrackersWizard wizard = (FixTrackersWizard)getWizard();
		final TFSoapClient soapClient = wizard.getSoapClient();
		pbiProblemsToFix = new ArrayList<TrackerProblem>();
		taskProblemsToFix = new ArrayList<TrackerProblem>();
		if (null != pbiTable) {
			TableItem[] items = pbiTable.getItems();
			for (TableItem item : items) {
				if (item.getChecked()) {
					TrackerProblem problem = (TrackerProblem)item.getData();
					pbiProblemsToFix.add(problem);
				}
			}
		}
		if (null != taskTable) {
			TableItem[] items = taskTable.getItems();
			for (TableItem item : items) {
				if (item.getChecked()) {
					TrackerProblem problem = (TrackerProblem)item.getData();
					taskProblemsToFix.add(problem);
				}
			}
		}
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				String taskName = "Fixing trackers";
				monitor.setTaskName(taskName);
				monitor.beginTask(taskName, pbiProblemsToFix.size() + taskProblemsToFix.size());		
				try {
					for (TrackerProblem problem : pbiProblemsToFix) {
						monitor.subTask("Field " + problem.getFieldName());
						if (problem.getFieldName().equals("Benefit")) {
							if (benefitExists) {
								soapClient.deleteField(pbiTracker.getId(), benefitField.getId());
							}
							soapClient.addTextField(pbiTracker.getId(), "Benefit", 5, 1, false, false, false, null);
						}
						if (problem.getFieldName().equals("Penalty")) {
							if (penaltyExists) {
								soapClient.deleteField(pbiTracker.getId(), penaltyField.getId());
							}
							soapClient.addTextField(pbiTracker.getId(), "Penalty", 5, 1, false, false, false, null);
						}
						if (problem.getFieldName().equals("Backlog Effort")) {
							if (backlogEffortExists) {
								soapClient.deleteField(pbiTracker.getId(), backlogEffortField.getId());
							}
							soapClient.addTextField(pbiTracker.getId(), "Backlog Effort", 5, 1, false, false, false, null);
						}
						if (problem.getFieldName().equals("SWP-Key")) {
							if (swpKeyExists) {
								soapClient.deleteField(pbiTracker.getId(), swpKeyField.getId());
							}
							soapClient.addTextField(pbiTracker.getId(), "SWP-Key", 30, 1, false, false, true, null);
						}
						if (problem.getFieldName().equals("Team/Sprint")) {
							if (teamSprintExists) {
								soapClient.deleteField(pbiTracker.getId(), teamSprintField.getId());
							}
							String[] teamSprintValues = getTeamSprintValues();
							soapClient.addSingleSelectField(pbiTracker.getId(), "Team/Sprint", false, false, true, teamSprintValues, null);
						}
						if (problem.getFieldName().equals("Sprint Start")) {
							if (sprintStartExists) {
								soapClient.deleteField(pbiTracker.getId(), sprintStartField.getId());
							}
							soapClient.addDateField(pbiTracker.getId(), "Sprint Start", false, false, true);
						}
						if (problem.getFieldName().equals("Sprint End")) {
							if (sprintEndExists) {
								soapClient.deleteField(pbiTracker.getId(), sprintEndField.getId());
							}
							soapClient.addDateField(pbiTracker.getId(), "Sprint End", false, false, true);
						}
						if (problem.getFieldName().equals("Themes")) {
							if (themeExists) {
								soapClient.deleteField(pbiTracker.getId(), themeField.getId());
							}
							String[] themeValues = getThemeValues();
							soapClient.addMultiSelectField(pbiTracker.getId(), "Themes", 10, false, false, false, themeValues, null);
						}
						if (problem.getFieldName().equals("Status")) {
							TrackerFieldValueDO[] oldValues = pbiStatusField.getFieldValues();
							TrackerFieldValueDO open = new TrackerFieldValueDO(soapClient.supports60(), soapClient.supports50());
							open.setIsDefault(true);
							open.setValue("Open");
							open.setValueClass("Open");
							open.setId(getFieldId("Open", oldValues));
							TrackerFieldValueDO done = new TrackerFieldValueDO(soapClient.supports60(), soapClient.supports50());
							done.setIsDefault(false);
							done.setValue("Done");
							done.setValueClass("Open");
							done.setId(getFieldId("Done", oldValues));
							TrackerFieldValueDO[] fieldValues = { open, done };
							pbiStatusField.setFieldValues(fieldValues);
							soapClient.setField(pbiTracker.getId(), pbiStatusField);
						}
						if (problem.getFieldName().equals("Group")) {
							pbiGroupField.setDisabled(true);
							soapClient.setField(pbiTracker.getId(), pbiGroupField);
						}
						if (problem.getFieldName().equals("Customer")) {
							pbiCustomerField.setDisabled(true);
							soapClient.setField(pbiTracker.getId(), pbiCustomerField);
						}
						if (problem.getFieldName().equals("Reported in Release")) {
							pbiReportedInReleaseField.setDisabled(true);
							soapClient.setField(pbiTracker.getId(), pbiReportedInReleaseField);
						}
						if (problem.getFieldName().equals("Fixed in Release")) {
							pbiResolvedInReleaseField.setDisabled(true);
							soapClient.setField(pbiTracker.getId(), pbiResolvedInReleaseField);
						}
						if (problem.getFieldName().equals("Estimated Effort")) {
							pbiEstimatedEffortField.setDisabled(true);
							soapClient.setField(pbiTracker.getId(), pbiEstimatedEffortField);
						}
						if (problem.getFieldName().equals("Actual Effort")) {
							pbiActualEffortField.setDisabled(true);
							soapClient.setField(pbiTracker.getId(), pbiActualEffortField);
						}
						monitor.worked(1);
					}
					for (TrackerProblem problem : taskProblemsToFix) {
						monitor.subTask("Field " + problem.getFieldName());
						if (problem.getFieldName().equals("Point Person")) {
							if (pointPersonExists) {
								soapClient.deleteField(taskTracker.getId(), pointPersonField.getId());
							}
							soapClient.addTextField(taskTracker.getId(), "Point Person", 30, 1, false, mapToAssignedToUser, false, null);							
						}
						if (problem.getFieldName().equals("Status")) {
							TrackerFieldValueDO[] oldValues = taskStatusField.getFieldValues();
							TrackerFieldValueDO notStarted = new TrackerFieldValueDO(soapClient.supports60(), soapClient.supports50());
							notStarted.setIsDefault(true);
							notStarted.setValue("Not Started");
							notStarted.setValueClass("Open");
							notStarted.setId(getFieldId("Not Started", oldValues));
							TrackerFieldValueDO impeded = new TrackerFieldValueDO(soapClient.supports60(), soapClient.supports50());
							impeded.setIsDefault(false);
							impeded.setValue("Impeded");
							impeded.setValueClass("Open");
							impeded.setId(getFieldId("Impeded", oldValues));
							TrackerFieldValueDO inProgress = new TrackerFieldValueDO(soapClient.supports60(), soapClient.supports50());
							inProgress.setIsDefault(false);
							inProgress.setValue("In Progress");
							inProgress.setValueClass("Open");
							inProgress.setId(getFieldId("In Progress", oldValues));
							TrackerFieldValueDO done = new TrackerFieldValueDO(soapClient.supports60(), soapClient.supports50());
							done.setIsDefault(false);
							done.setValue("Done");
							done.setValueClass("Close");
							done.setId(getFieldId("Done", oldValues));
							TrackerFieldValueDO[] fieldValues = { notStarted, impeded, inProgress, done };
							taskStatusField.setFieldValues(fieldValues);
							soapClient.setField(taskTracker.getId(), taskStatusField);
						}
						if (problem.getFieldName().equals("Group")) {
							taskGroupField.setDisabled(true);
							soapClient.setField(taskTracker.getId(), taskGroupField);
						}
						if (problem.getFieldName().equals("Customer")) {
							taskCustomerField.setDisabled(true);
							soapClient.setField(taskTracker.getId(), taskCustomerField);
						}
						if (problem.getFieldName().equals("Reported in Release")) {
							taskReportedInReleaseField.setDisabled(true);
							soapClient.setField(taskTracker.getId(), taskReportedInReleaseField);
						}
						if (problem.getFieldName().equals("Fixed in Release")) {
							taskResolvedInReleaseField.setDisabled(true);
							soapClient.setField(taskTracker.getId(), taskResolvedInReleaseField);
						}
						if (problem.getFieldName().equals("Calculate Effort")) {
							taskAutosummingField.setDisabled(true);
							soapClient.setField(taskTracker.getId(), taskAutosummingField);
						}
						if (problem.getFieldName().equals("Actual Effort")) {
							taskActualEffortField.setDisabled(true);
							soapClient.setField(taskTracker.getId(), taskActualEffortField);
						}							
						monitor.worked(1);
					}
				} catch (Exception e) {
					fixProblemsError = e;
					Activator.handleError(e);
					return;					
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, runnable);
		} catch (Exception e) {
			Activator.handleError(e);
			fixProblemsError = e;
		}
		if (null != fixProblemsError) {
			setErrorMessage("An unexpected error occurred while trying to fix TeamForge trackers.  See error log for details.");
		}
		return fixProblemsError == null;
	}

	private void checkTrackers() {
		initializeFlags();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				String taskName = "Checking trackers";
				monitor.setTaskName(taskName);
				monitor.beginTask(taskName, 3);			
				try {
					monitor.subTask("Retrieving trackers");
					FixTrackersWizard wizard = (FixTrackersWizard)getWizard();
					wizard.getProduct();
					SynchronizationStatus projectMapping = wizard.getProjectMapping();
					String swpTaskRepositoryId = wizard.getProduct() + "(" + wizard.getProductId() + ")-Task";
					String swpPbiRepositoryId = wizard.getProduct() + "(" + wizard.getProductId() + ")-PBI";
					ProjectMappings projectMappings = projectMapping.getProjectMappings();
					Landscape landscape = projectMappings.getLandscape();
					CcfDataProvider dataProvider = new CcfDataProvider();
					SynchronizationStatus[] mappings = dataProvider.getSynchronizationStatuses(landscape, null);
					String pbiTrackerId = null;
					String taskTrackerId = null;
					for (SynchronizationStatus mapping : mappings) {
						if (mapping.getSourceRepositoryId().equals(swpPbiRepositoryId)) {
							pbiTrackerId = mapping.getTargetRepositoryId();
						}
						else if (mapping.getTargetRepositoryId().equals(swpPbiRepositoryId)) {
							pbiTrackerId = mapping.getSourceRepositoryId();
						}
						else if (mapping.getSourceRepositoryId().equals(swpTaskRepositoryId)) {
							taskTrackerId = mapping.getTargetRepositoryId();
							mapToAssignedToUser = mapping.getSourceRepositoryKind().equals(ScrumWorksMappingSection.TEMPLATE_TASKS);
						}
						else if (mapping.getTargetRepositoryId().equals(swpTaskRepositoryId)) {
							taskTrackerId = mapping.getSourceRepositoryId();
							mapToAssignedToUser = mapping.getSourceRepositoryKind().equals(ScrumWorksMappingSection.TEMPLATE_TASKS);
						}
						if (null != pbiTrackerId && null != taskTrackerId) {
							break;
						}
					}
					if (null != pbiTrackerId) {
						pbiTracker = wizard.getSoapClient().getTrackerInformation(pbiTrackerId);
					}
					if (null != taskTrackerId) {
						taskTracker = wizard.getSoapClient().getTrackerInformation(taskTrackerId);
					}
					monitor.worked(1);
					if (null != pbiTrackerId) {
						monitor.subTask("Checking PBI tracker fields");
						TrackerFieldDO[] fields = wizard.getSoapClient().getFields(pbiTrackerId);
						for (TrackerFieldDO field : fields) {
							String fieldName = field.getName();
							String fieldType = field.getFieldType();
							if (fieldName.equals("status")) {
								boolean openCorrect = false;
								boolean doneCorrect = false;
								TrackerFieldValueDO[] values = field.getFieldValues();
								for (TrackerFieldValueDO value : values) {
									if (!value.getValueClass().equals("Open")) {
										break;
									}
									if (!value.getValue().equals("Open") &&
									    !value.getValue().equals("Done")) {
										break;
									}
									if (value.getValue().equals("Open")) {
										openCorrect = true;
									}
									if (value.getValue().equals("Done")) {
										doneCorrect = true;
									}
								}
								pbiStatusValuesCorrect = openCorrect && doneCorrect;
								if (!pbiStatusValuesCorrect) {
									pbiStatusField = field;
								}
							}
							if (fieldName.equals("group") ||
							    fieldName.equals("customer") ||
							    fieldName.equals("reportedInRelease") ||
							    fieldName.equals("resolvedInRelease") ||
							    fieldName.startsWith("estimated") ||
							    fieldName.startsWith("actual")) {
								if (!field.getDisabled()) {
									if (fieldName.equals("group")) {
										pbiGroupEnabled = true;
										pbiGroupField = field;
									}
									else if (fieldName.equals("customer")) {
										pbiCustomerEnabled = true;
										pbiCustomerField = field;
									}
									else if (fieldName.equals("reportedInRelease")) {
										pbiReportedInReleaseEnabled = true;
										pbiReportedInReleaseField = field;
									}
									else if (fieldName.equals("resolvedInRelease")) {
										pbiResolvedInReleaseEnabled = true;
										pbiResolvedInReleaseField = field;
									}
									else if (fieldName.startsWith("estimated")) {
										pbiEstimatedEffortEnabled = true;
										pbiEstimatedEffortField = field;
									}
									else if (fieldName.startsWith("actual")) {
										pbiActualEffortEnabled = true;
										pbiActualEffortField = field;
									}
								}
							}
							if (fieldName.equals("Benefit")) {
								benefitExists = true;
								benefitTypeCorrect = fieldType.equals(TEXT);
								benefitField = field;
							}
							else if (fieldName.equals("Penalty")) {
								penaltyExists = true;
								penaltyTypeCorrect = fieldType.equals(TEXT);
								penaltyField = field;
							}
							else if (fieldName.equals("Backlog Effort")) {
								backlogEffortExists = true;
								backlogEffortTypeCorrect = fieldType.equals(TEXT);
								backlogEffortField = field;
							}
							else if (fieldName.equals("SWP-Key")) {
								swpKeyExists = true;
								swpKeyTypeCorrect = fieldType.equals(TEXT);
								swpKeyField = field;
							}
							else if (fieldName.equals("Team/Sprint")) {
								teamSprintExists = true;
								teamSprintTypeCorrect = fieldType.equals(SINGLE_SELECT);
								teamSprintField = field;
							}
							else if (fieldName.equals("Sprint Start")) {
								sprintStartExists = true;
								sprintStartTypeCorrect = fieldType.equals(DATE);
								sprintStartField = field;
							}
							else if (fieldName.equals("Sprint End")) {
								sprintEndExists = true;
								sprintEndTypeCorrect = fieldType.equals(DATE);
								sprintEndField = field;
							}
							else if (fieldName.equals("Themes")) {
								themeExists = true;
								themeTypeCorrect = fieldType.equals(MULTI_SELECT);
								themeField = field;
							}
						}					
					}
					if (wizard.getSoapClient().supports54()) {
						backlogEffortTypeCorrect = true; // Backlog effort not used
						backlogEffortExists = true; // Story points used instead
					}
					monitor.worked(1);
					if (null != taskTrackerId) {
						monitor.subTask("Checking Task tracker fields");
						TrackerFieldDO[] fields = wizard.getSoapClient().getFields(taskTrackerId);
						for (TrackerFieldDO field : fields) {
							String fieldName = field.getName();
							String fieldType = field.getFieldType();
							if (fieldName.equals("status")) {
								boolean notStartedCorrect = false;
								boolean impededCorrect = false;
								boolean inProgressCorrect = false;
								boolean doneCorrect = false;
								TrackerFieldValueDO[] values = field.getFieldValues();
								for (TrackerFieldValueDO value : values) {
									if (!value.getValue().equals("Not Started") &&
										!value.getValue().equals("Impeded") &&
										!value.getValue().equals("In Progress") &&
										!value.getValue().equals("Done")) {
										break;
									}
									if (value.getValue().equals("Not Started")) {
										notStartedCorrect = value.getValueClass().equals("Open");
									}
									if (value.getValue().equals("Impeded")) {
										impededCorrect = value.getValueClass().equals("Open");
									}
									if (value.getValue().equals("In Progress")) {
										inProgressCorrect = value.getValueClass().equals("Open");
									}
									if (value.getValue().equals("Done")) {
										doneCorrect = value.getValueClass().equals("Close");
									}
								}
								taskStatusValuesCorrect = notStartedCorrect && impededCorrect && inProgressCorrect && doneCorrect;
								if (!taskStatusValuesCorrect) {
									taskStatusField = field;
								}
							}
							if (fieldName.equals("group") ||
								fieldName.equals("customer") ||
								fieldName.equals("reportedInRelease") ||
								fieldName.equals("resolvedInRelease") ||
								fieldName.startsWith("autosumming") ||
								fieldName.startsWith("actual")) {
								if (!field.getDisabled()) {
									if (fieldName.equals("group")) {
										taskGroupEnabled = true;
										taskGroupField = field;
									}
									else if (fieldName.equals("customer")) {
										taskCustomerEnabled = true;
										taskCustomerField = field;
									}
									else if (fieldName.equals("reportedInRelease")) {
										taskReportedInReleaseEnabled = true;
										taskReportedInReleaseField = field;
									}
									else if (fieldName.equals("resolvedInRelease")) {
										taskResolvedInReleaseEnabled = true;
										taskResolvedInReleaseField = field;
									}
									else if (fieldName.equals("autosumming")) {
										taskAutosummingEnabled = true;
										taskAutosummingField = field;
									}
									else if (fieldName.startsWith("actual")) {
										taskActualEffortEnabled = true;
										taskActualEffortField = field;
									}
								}
							}
							if (fieldName.equals("Point Person")) {
								pointPersonExists = true;
								pointPersonTypeCorrect = fieldType.equals(TEXT);
								pointPersonField = field;
							}
						}
					}
					monitor.worked(1);
				} catch (Exception e) {
					checkTrackersError = e;
					Activator.handleError(e);
					return;
				} finally {
					monitor.done();
				}
			}		
		};
		try {
			getContainer().run(true, false, runnable);
		} catch (Exception e) {
			Activator.handleError(e);
			checkTrackersError = e;
		}

		if (null != checkTrackersError) {
			setErrorMessage("An unexpected error occurred while checking TeamForge trackers.  See error log for details.");
		} else if (!hasPbiProblems() && !hasTaskProblems()) {
			setMessage("No problems found with TeamForge trackers.");
		} else {
			setMessage("Fix TeamForge trackers to make them compliant with ScrumWorks Pro integration requirements.");
		}
		setPageComplete(checkTrackersError == null);
	}

	public TrackerDO getPbiTracker() {
		return pbiTracker;
	}

	public TrackerDO getTaskTracker() {
		return taskTracker;
	}
	
	private String getFieldId(String value, TrackerFieldValueDO[] oldValues) {
		String id = null;
		for (TrackerFieldValueDO oldValue : oldValues) {
			if (oldValue.getValue().equals(value)) {
				id = oldValue.getId();
				break;
			}
		}
		return id;
	}
	
	private String[] getTeamSprintValues() throws MalformedURLException, ScrumWorksException {
		Map<Long, Team> teamMap = new HashMap<Long, Team>();
		List<String> teamSprintList = new ArrayList<String>();
		FixTrackersWizard wizard = (FixTrackersWizard)getWizard();
		Landscape landscape = wizard.getProjectMapping().getLandscape();
		String swpTimezone;
		if (landscape.getType1().equals("SWP")) {
			swpTimezone = landscape.getTimezone1();
		} else {
			swpTimezone = landscape.getTimezone2();
		}
		List<Sprint> sprints = getSprints(wizard.getProductId());
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
		String[] teamSprintValues = new String[teamSprintList.size()];
		teamSprintList.toArray(teamSprintValues);
		Arrays.sort(teamSprintValues);
		return teamSprintValues;
	}
	
	private List<Theme> getThemes(Long productId) throws MalformedURLException, ScrumWorksException {
		return getScrumWorksEndpoint().getThemesForProduct(productId);
	}
	
	private List<Sprint> getSprints(Long productId) throws MalformedURLException, ScrumWorksException {
		return getScrumWorksEndpoint().getSprintsForProduct(productId);
	}
	
	private String[] getThemeValues() throws MalformedURLException, ScrumWorksException {
		List<String> themeList = new ArrayList<String>();
		FixTrackersWizard wizard = (FixTrackersWizard)getWizard();
		List<Theme> themes = getThemes(wizard.getProductId());
		if (null != themes) {
			for (Theme theme : themes) {
				themeList.add(getValue(theme));
			}
		}
		String[] themeValues = new String[themeList.size()];
		themeList.toArray(themeValues);
		Arrays.sort(themeValues);
		return themeValues;
	}
	
	private String getValue(Theme theme) throws MalformedURLException, ScrumWorksException {
		if (null == programMap) {
			programMap = new HashMap<Long, Program>();
		}
		Program program = null;
		if (null != theme.getProgramId()) {
			program = programMap.get(theme.getProgramId());
			if (null == program) {
				program = getScrumWorksEndpoint().getProgramById(theme.getProgramId());
				programMap.put(theme.getProgramId(), program);
			}
		}
		if (null == program) {
			return theme.getName();
		} else {
			return theme.getName() + " (" + program.getName() + ")";
		}
	}
	
	private Team getTeam(Long teamId) throws MalformedURLException, ScrumWorksException {
		return getScrumWorksEndpoint().getTeamById(teamId);
	}
	
	private ScrumWorksAPIService getScrumWorksEndpoint() throws MalformedURLException {
		if (null == scrumWorksEndpoint) {
			FixTrackersWizard wizard = (FixTrackersWizard)getWizard();
			scrumWorksEndpoint = com.collabnet.ccf.sw.Activator.getScrumWorksEndpoint(wizard.getProjectMapping().getLandscape());
		}
		return scrumWorksEndpoint;
	}
	
	class TrackerProblem {
		private String trackerId;
		private String fieldName;
		private String problemDescription;
		
		public TrackerProblem(String trackerId, String fieldName, String problemDescription) {
			super();
			this.trackerId = trackerId;
			this.fieldName = fieldName;
			this.problemDescription = problemDescription;
		}

		public String getTrackerId() {
			return trackerId;
		}

		public String getFieldName() {
			return fieldName;
		}

		public String getProblemDescription() {
			return problemDescription;
		}		
		
	}
	
	class TrackerProblemContentProvider implements IStructuredContentProvider {
		private List<TrackerProblem> trackerProblems;
		public TrackerProblemContentProvider(List<TrackerProblem> trackerProblems) {
			super();
			this.trackerProblems = trackerProblems;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public Object[] getElements(Object obj) {
			if (null == trackerProblems) {
				return new TrackerProblem[0];
			}
			TrackerProblem[] problemArray = new TrackerProblem[trackerProblems.size()];
			trackerProblems.toArray(problemArray);
			return problemArray;
		}
	}	
	
	class TrackerProblemLabelProvider extends LabelProvider implements ITableLabelProvider {

		public String getColumnText(Object element, int columnIndex) {
			TrackerProblem trackerProblem = (TrackerProblem)element;
			switch (columnIndex) { 
				case 0: return trackerProblem.getFieldName();
				case 1: return trackerProblem.getProblemDescription();
			}
			return "";  //$NON-NLS-1$
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	
	}	

}
