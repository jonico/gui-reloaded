package com.collabnet.ccf.teamforge_sw.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.model.SynchronizationStatus;

public class ProjectMappingWizardPreviewPage extends WizardPage {
	private String product;
	private String projectId;
	private String taskTracker;
	private String pbiTracker;
	private String trackerTaskMapping;
	private String trackerPbiMapping;
	private String planningFolderProductMapping;
	private String planningFolderProductReleaseMapping;
	private String taskTrackerMapping;
	private String pbiTrackerMapping;
	private String productPlanningFolderMapping;
	private String productReleasePlanningFolderMapping;
	private String metaDataMapping;
	private String newProjectDescription = DEFAULT_PROJECT_DESCRIPTION;

	private Group projectDescriptionGroup;
	private Text projectDescriptionText;
	
	private Combo trackerTaskCombo;
	private Combo trackerPbiCombo;
	private Combo planningFolderProductCombo;
	private Combo planningFolderProductReleaseCombo;
	private Combo taskTrackerCombo;
	private Combo pbiTrackerCombo;
	private Combo productPlanningFolderCombo;
	private Combo productReleasePlanningFolderCombo;
	
	private String comboText;
	
	private final static String DEFAULT_PROJECT_DESCRIPTION = "Project was automatically created for TeamForge-ScrumWorks integration.";

	private final static String SCRUMWORKS_TO_TEAMFORGE = "ScrumWorks => TeamForge:";
	private final static String TEAMFORGE_TO_SCRUMWORKS = "TeamForge => ScrumWorks:";
	
	public ProjectMappingWizardPreviewPage() {
		super("previewPage", "Conflict Resolution", Activator.getDefault().getImageDescriptor(Activator.IMAGE_NEW_PROJECT_MAPPING_WIZBAN));
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		Composite outerContainer = new Composite(parent,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		outerContainer.setLayout(layout);
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		Group mappingGroup = new Group(outerContainer,SWT.NONE);
		mappingGroup.setText("Conflict Resolution:");
		GridLayout mappingLayout = new GridLayout();
		mappingGroup.setLayout(mappingLayout);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		mappingGroup.setLayoutData(data);
		
		Group pbiGroup = new Group(mappingGroup,SWT.NONE);
		pbiGroup.setText("PBI:");
		GridLayout pbiLayout = new GridLayout();
		pbiLayout.numColumns = 2;
		pbiGroup.setLayout(pbiLayout);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		pbiGroup.setLayoutData(data);

		Label pbiTrackerText = new Label(pbiGroup, SWT.NONE);
		pbiTrackerText.setText(SCRUMWORKS_TO_TEAMFORGE);
		pbiTrackerCombo = new Combo(pbiGroup, SWT.READ_ONLY);
		populateConflictResolutionCombo(pbiTrackerCombo);

		Label trackerPbiText = new Label(pbiGroup, SWT.NONE);
		trackerPbiText.setText(TEAMFORGE_TO_SCRUMWORKS);
		trackerPbiCombo = new Combo(pbiGroup, SWT.READ_ONLY);
		populateConflictResolutionCombo(trackerPbiCombo);
		
		Group taskGroup = new Group(mappingGroup,SWT.NONE);
		taskGroup.setText("Task:");
		GridLayout taskLayout = new GridLayout();
		taskLayout.numColumns = 2;
		taskGroup.setLayout(taskLayout);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		taskGroup.setLayoutData(data);

		Label taskTrackerText = new Label(taskGroup, SWT.NONE);
		taskTrackerText.setText(SCRUMWORKS_TO_TEAMFORGE);
		taskTrackerCombo = new Combo(taskGroup, SWT.READ_ONLY);
		populateConflictResolutionCombo(taskTrackerCombo);

		Label trackerTaskText = new Label(taskGroup, SWT.NONE);
		trackerTaskText.setText(TEAMFORGE_TO_SCRUMWORKS);
		trackerTaskCombo = new Combo(taskGroup, SWT.READ_ONLY);
		populateConflictResolutionCombo(trackerTaskCombo);	
		
		Group productGroup = new Group(mappingGroup,SWT.NONE);
		productGroup.setText("Product:");
		GridLayout productLayout = new GridLayout();
		productLayout.numColumns = 2;
		productGroup.setLayout(productLayout);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		productGroup.setLayoutData(data);

		Label productPlanningFolderText = new Label(productGroup, SWT.NONE);
		productPlanningFolderText.setText(SCRUMWORKS_TO_TEAMFORGE);
		productPlanningFolderCombo = new Combo(productGroup, SWT.READ_ONLY);
		populateConflictResolutionCombo(productPlanningFolderCombo);

		Label planningFolderProductText = new Label(productGroup, SWT.NONE);
		planningFolderProductText.setText(TEAMFORGE_TO_SCRUMWORKS);
		planningFolderProductCombo = new Combo(productGroup, SWT.READ_ONLY);
		populateConflictResolutionCombo(planningFolderProductCombo);
		
		Group releaseGroup = new Group(mappingGroup,SWT.NONE);
		releaseGroup.setText("Release:");
		GridLayout releaseLayout = new GridLayout();
		releaseLayout.numColumns = 2;
		releaseGroup.setLayout(releaseLayout);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		releaseGroup.setLayoutData(data);

		Label productReleasePlanningFolderText = new Label(releaseGroup, SWT.NONE);
		productReleasePlanningFolderText.setText(SCRUMWORKS_TO_TEAMFORGE);
		productReleasePlanningFolderCombo = new Combo(releaseGroup, SWT.READ_ONLY);
		populateConflictResolutionCombo(productReleasePlanningFolderCombo);

		Label planningFolderProductReleaseText = new Label(releaseGroup, SWT.NONE);
		planningFolderProductReleaseText.setText(TEAMFORGE_TO_SCRUMWORKS);
		planningFolderProductReleaseCombo = new Combo(releaseGroup, SWT.READ_ONLY);
		populateConflictResolutionCombo(planningFolderProductReleaseCombo);
		
//		Group metaDataGroup = new Group(mappingGroup,SWT.NONE);
//		metaDataGroup.setText("MetaData:");
//		GridLayout metaDataLayout = new GridLayout();
//		metaDataLayout.numColumns = 2;
//		metaDataGroup.setLayout(metaDataLayout);
//		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
//		metaDataGroup.setLayoutData(data);
//
//		Label metaDataText = new Label(metaDataGroup, SWT.NONE);
//		metaDataText.setText(SCRUMWORKS_TO_TEAMFORGE);
//		Combo metaDataCombo = new Combo(metaDataGroup, SWT.READ_ONLY);
//		populateConflictResolutionCombo(metaDataCombo);
//		metaDataCombo.setVisible(false);
		
		projectDescriptionGroup = new Group(outerContainer,SWT.NONE);
		projectDescriptionGroup.setText("Description for new TeamForge Project:");
		GridLayout descriptionLayout = new GridLayout();
		projectDescriptionGroup.setLayout(descriptionLayout);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		projectDescriptionGroup.setLayoutData(data);
		
		projectDescriptionText = new Text(projectDescriptionGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		data.heightHint = 75;
		projectDescriptionText.setLayoutData(data);
		projectDescriptionText.setText(newProjectDescription);
		projectDescriptionText.addModifyListener(new ModifyListener() {			
			public void modifyText(ModifyEvent e) {
				newProjectDescription = projectDescriptionText.getText();
			}
		});
		
		setMessage("Select the conflict resolution option to use for each mapping.");

		setControl(outerContainer);
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {		
			String selectedProjectId = null;
			if (null != ((ProjectMappingWizard)getWizard()).getSelectedProject()) {
				selectedProjectId = ((ProjectMappingWizard)getWizard()).getSelectedProject().getId();
				projectDescriptionGroup.setVisible(false);
				setMessage("Select the conflict resolution option to use for each mapping.");
			} else {
				projectDescriptionGroup.setVisible(true);
				setMessage("Select the conflict resolution option to use for each mapping and enter a description for the new TeamForge project.");
			}
			if (null == product || !product.equals(((ProjectMappingWizard)getWizard()).getSelectedProduct().getName()) ||
				null == projectId || !projectId.equals(selectedProjectId) ||
				null == taskTracker || !taskTracker.equals(((ProjectMappingWizard)getWizard()).getSelectedTaskTracker().getId()) ||
				null == pbiTracker || !pbiTracker.equals(((ProjectMappingWizard)getWizard()).getSelectedPbiTracker().getId())) {
				product = ((ProjectMappingWizard)getWizard()).getSelectedProduct().getName();
				if (null == ((ProjectMappingWizard)getWizard()).getSelectedProject()) {
					projectId = null;
				} else {
					projectId = ((ProjectMappingWizard)getWizard()).getSelectedProject().getId();
				}
				if (null == ((ProjectMappingWizard)getWizard()).getSelectedTaskTracker()) {
					taskTracker = null;
				} else {
					taskTracker = ((ProjectMappingWizard)getWizard()).getSelectedTaskTracker().getId();
				}
				if (null == ((ProjectMappingWizard)getWizard()).getSelectedPbiTracker()) {
					pbiTracker = null;
				} else {
					pbiTracker = ((ProjectMappingWizard)getWizard()).getSelectedPbiTracker().getId();
				}
				refreshMappings();
				setPageComplete(true);
			}
		}
	}
	
	public String getTrackerTaskMapping() {
		return trackerTaskMapping;
	}

	public String getTrackerPbiMapping() {
		return trackerPbiMapping;
	}

	public String getPlanningFolderProductMapping() {
		return planningFolderProductMapping;
	}
	
	public String getPlanningFolderProductReleaseMapping() {
		return planningFolderProductReleaseMapping;
	}

	public String getTaskTrackerMapping() {
		return taskTrackerMapping;
	}

	public String getPbiTrackerMapping() {
		return pbiTrackerMapping;
	}

	public String getProductPlanningFolderMapping() {
		return productPlanningFolderMapping;
	}
	
	public String getProductReleasePlanningFolderMapping() {
		return productReleasePlanningFolderMapping;
	}

	public String getMetaDataMapping() {
		return metaDataMapping;
	}

	public String getTrackerTaskConflictResolutionPriority() {
		return SynchronizationStatus.getConflictResolutionByDescription(getComboText(trackerTaskCombo));
	}
	
	public String getTrackerPbiConflictResolutionPriority() {
		return SynchronizationStatus.getConflictResolutionByDescription(getComboText(trackerPbiCombo));
	}
	
	public String getPlanningFolderProductConflictResolutionPriority() {
		return SynchronizationStatus.getConflictResolutionByDescription(getComboText(planningFolderProductCombo));
	}
	
	public String getPlanningFolderProductReleaseConflictResolutionPriority() {
		return SynchronizationStatus.getConflictResolutionByDescription(getComboText(planningFolderProductReleaseCombo));
	}
	
	public String getTaskTrackerConflictResolutionPriority() {
		return SynchronizationStatus.getConflictResolutionByDescription(getComboText(taskTrackerCombo));
	}
	
	public String getPbiTrackerConflictResolutionPriority() {
		return SynchronizationStatus.getConflictResolutionByDescription(getComboText(pbiTrackerCombo));
	}
	
	public String getProductPlanningFolderConflictResolutionPriority() {
		return SynchronizationStatus.getConflictResolutionByDescription(getComboText(productPlanningFolderCombo));
	}
	
	public String getProductReleasePlanningFolderConflictResolutionPriority() {
		return SynchronizationStatus.getConflictResolutionByDescription(getComboText(productReleasePlanningFolderCombo));
	}
	
	private String getComboText(final Combo combo) {
		Display.getDefault().syncExec(new Runnable() {		
			public void run() {
				comboText = combo.getText();
			}
		});
		return comboText;
	}
	
	public String getNewProjectDescription() {
		return newProjectDescription;
	}
	
	private void populateConflictResolutionCombo(Combo combo) {
		combo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_IGNORE);
		combo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE);
		combo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_QUARANTINE_ARTIFACT);
		combo.setText(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE);
	}
	
	private void refreshMappings() {
		ProjectMappingWizard wizard = (ProjectMappingWizard)getWizard();
		String pbiTracker;
		if (null == wizard.getSelectedPbiTracker()) {
			pbiTracker = "<new tracker id>";
		} else {
			pbiTracker = wizard.getSelectedPbiTracker().getId();
		}
		String taskTracker;
		if (null == wizard.getSelectedTaskTracker()) {
			taskTracker = "<new tracker id>";
		} else {
			taskTracker = wizard.getSelectedTaskTracker().getId();
		}
		String project;
		if (null == wizard.getSelectedProject()) {
			project = "<new project id>";
		} else {
			project = wizard.getSelectedProject().getId();
		}
		String productNameAndId = wizard.getSelectedProduct().getName() + "(" + wizard.getSelectedProduct().getId() + ")";
		trackerTaskMapping = taskTracker + " => " + productNameAndId + "-Task";
		trackerPbiMapping = pbiTracker + " => " + productNameAndId + "-PBI";
		planningFolderProductMapping = project + "-planningFolders => " + productNameAndId + "-Product";
		planningFolderProductReleaseMapping = project + "-planningFolders => " + productNameAndId + "-Release";
		taskTrackerMapping = productNameAndId + "-Task => " + taskTracker;
		pbiTrackerMapping = productNameAndId + "-PBI => " + pbiTracker;
		productPlanningFolderMapping = productNameAndId + "-Product => " + project + "-planningFolders";
		productReleasePlanningFolderMapping = productNameAndId + "-Release => " + project + "-planningFolders";
		metaDataMapping = productNameAndId + "-MetaData => " + pbiTracker + "-MetaData";
	}

}
