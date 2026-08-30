package com.collabnet.ccf.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;

public class NewProjectMappingWizardMainPage extends WizardPage {
	private ProjectMappings projectMappings;
	private int direction = -1;

	protected Button system1ToSystem2Button;
	protected Button system2ToSystem1Button;
	protected Button bothButton;

	private Label system1ToSystem2ConflictResolutionLabel;
	protected Combo system1ToSystem2ConflictResolutionCombo;
	private Label system2ToSystem1ConflictResolutionLabel;
	protected Combo system2ToSystem1ConflictResolutionCombo;
	
	private IDialogSettings settings = Activator.getDefault().getDialogSettings();
	private static final String PREVIOUS_DIRECTION = "NewProjectMapping.direction";
	
	public NewProjectMappingWizardMainPage(ProjectMappings projectMappings) {
		super("mainPage", "Mapping Type", Activator.getDefault().getImageDescriptor(Activator.IMAGE_NEW_PROJECT_MAPPING_WIZBAN));
		this.projectMappings = projectMappings;
		setPageComplete(true);
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	public void createControl(Composite parent) {
		Composite outerContainer = new Composite(parent,SWT.NONE);
		outerContainer.setLayout(new GridLayout());
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		Group directionGroup = new Group(outerContainer, SWT.NULL);
		GridLayout directionLayout = new GridLayout();
		directionLayout.numColumns = 1;
		directionGroup.setLayout(directionLayout);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		directionGroup.setLayoutData(gd);	
		directionGroup.setText("Direction:");
		
		system2ToSystem1Button = new Button(directionGroup, SWT.RADIO);
		system1ToSystem2Button = new Button(directionGroup, SWT.RADIO);

		if (projectMappings.getLandscape().getType1().equals(projectMappings.getLandscape().getType2())) {
			if (Landscape.ROLE_ADMINISTRATOR == projectMappings.getLandscape().getRole()) {
				String url1 = projectMappings.getLandscape().getUrl(1);
				String url2 = projectMappings.getLandscape().getUrl(2);
				system2ToSystem1Button.setText(Landscape.getTypeDescription(projectMappings.getLandscape().getType2()) + " (" + url2 + ") => " + Landscape.getTypeDescription(projectMappings.getLandscape().getType1()) + " (" + url1 + ")");			
				system1ToSystem2Button.setText(Landscape.getTypeDescription(projectMappings.getLandscape().getType1()) + " (" + url1 + ") => " + Landscape.getTypeDescription(projectMappings.getLandscape().getType2()) + " (" + url2 + ")");
			} else {
				system2ToSystem1Button.setText(Landscape.getTypeDescription(projectMappings.getLandscape().getType2()) + " (2) => " + Landscape.getTypeDescription(projectMappings.getLandscape().getType1()) + " (1)");			
				system1ToSystem2Button.setText(Landscape.getTypeDescription(projectMappings.getLandscape().getType1()) + " (1) => " + Landscape.getTypeDescription(projectMappings.getLandscape().getType2()) + " (2)");				
			}
		} else {
			system2ToSystem1Button.setText(Landscape.getTypeDescription(projectMappings.getLandscape().getType2()) + " => " + Landscape.getTypeDescription(projectMappings.getLandscape().getType1()));			
			system1ToSystem2Button.setText(Landscape.getTypeDescription(projectMappings.getLandscape().getType1()) + " => " + Landscape.getTypeDescription(projectMappings.getLandscape().getType2()));	
		}
				
		bothButton = new Button(directionGroup, SWT.RADIO);
		bothButton.setText("Create mappings for both directions");
		
		if (-1 == direction) {
			try {
				direction = settings.getInt(PREVIOUS_DIRECTION);
			} catch (Exception e) {
				direction = 0;
			}
		}
		switch (direction) {
		case 0:
			system2ToSystem1Button.setSelection(true);
			break;
		case 1:
			system1ToSystem2Button.setSelection(true);
			break;
		case 2:
			bothButton.setSelection(true);
			break;			
		default:
			system2ToSystem1Button.setSelection(true);
			break;
		}
		
		SelectionListener selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				if (system2ToSystem1Button.getSelection()) {
					settings.put(PREVIOUS_DIRECTION, 0);
				} else if (system1ToSystem2Button.getSelection()) {
					settings.put(PREVIOUS_DIRECTION, 1);
				} else if (bothButton.getSelection()) {
					settings.put(PREVIOUS_DIRECTION, 2);
				}
				setComboEnablement();
			}
	
		};
		
		system2ToSystem1Button.addSelectionListener(selectionListener);
		system1ToSystem2Button.addSelectionListener(selectionListener);
		bothButton.addSelectionListener(selectionListener);

		Group conflictGroup = new Group(outerContainer, SWT.NULL);
		GridLayout conflictLayout = new GridLayout();
		conflictLayout.numColumns = 2;
		conflictGroup.setLayout(conflictLayout);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		conflictGroup.setLayoutData(gd);	
		conflictGroup.setText("Conflict resolution priority:");
		
		system2ToSystem1ConflictResolutionLabel = new Label(conflictGroup, SWT.NONE);
		system2ToSystem1ConflictResolutionLabel.setText(system2ToSystem1Button.getText());
		system2ToSystem1ConflictResolutionCombo = new Combo(conflictGroup, SWT.READ_ONLY);
		system2ToSystem1ConflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_IGNORE);
		system2ToSystem1ConflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE);
		if (system2ToSystem1Button.getText().endsWith("Quality Center")) {
			system2ToSystem1ConflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE_AND_IGNORE_LOCKS);
		}
		system2ToSystem1ConflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_QUARANTINE_ARTIFACT);

		system2ToSystem1ConflictResolutionCombo.setText(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE);
		
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		system2ToSystem1ConflictResolutionCombo.setLayoutData(gd);
		
		system1ToSystem2ConflictResolutionLabel = new Label(conflictGroup, SWT.NONE);
		system1ToSystem2ConflictResolutionLabel.setText(system1ToSystem2Button.getText());
		system1ToSystem2ConflictResolutionCombo = new Combo(conflictGroup, SWT.READ_ONLY);
		system1ToSystem2ConflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_IGNORE);
		system1ToSystem2ConflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE);
		if (system1ToSystem2Button.getText().endsWith("Quality Center")) {
			system1ToSystem2ConflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE_AND_IGNORE_LOCKS);
		}
		system1ToSystem2ConflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_QUARANTINE_ARTIFACT);
		
		system1ToSystem2ConflictResolutionCombo.setText(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE);

		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		system1ToSystem2ConflictResolutionCombo.setLayoutData(gd);
		
		setComboEnablement();
		
		setMessage("Select the mapping direction and conflict resolution handling");

		setControl(outerContainer);
	}
	
	private void setComboEnablement() {
		system2ToSystem1ConflictResolutionLabel.setEnabled(system2ToSystem1Button.getSelection() || bothButton.getSelection());
		system2ToSystem1ConflictResolutionCombo.setEnabled(system2ToSystem1Button.getSelection() || bothButton.getSelection());
		system1ToSystem2ConflictResolutionLabel.setEnabled(system1ToSystem2Button.getSelection() || bothButton.getSelection());
		system1ToSystem2ConflictResolutionCombo.setEnabled(system1ToSystem2Button.getSelection() || bothButton.getSelection());
	}

}
