package com.collabnet.ccf.wizards;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.ICcfParticipant;
import com.collabnet.ccf.IMappingSection;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.MappingGroup;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;

public class NewProjectMappingWizard extends Wizard {
	private ProjectMappings projectMappings;
	private MappingGroup mappingGroup;
	private int direction = -1;
	
	private NewProjectMappingWizardMainPage mainPage;
	private NewProjectMappingWizardProjectPage projectPage;
	
	private boolean addError;
	private boolean conflictResolutionChanged;

	public NewProjectMappingWizard(ProjectMappings projectMappings) {
		super();
		this.projectMappings = projectMappings;
	}
	
	public NewProjectMappingWizard(MappingGroup mappingGroup) {
		super();
		this.mappingGroup = mappingGroup;
		this.projectMappings = mappingGroup.getProjectMappingsParent();
	}
	
	public void setDirection(int direction) {
		this.direction = direction;
	}

	@Override
	public void addPages() {
		super.addPages();
		setWindowTitle("New Project Mapping");
		mainPage = new NewProjectMappingWizardMainPage(projectMappings);
		mainPage.setDirection(direction);
		addPage(mainPage);
		projectPage = new NewProjectMappingWizardProjectPage(projectMappings);
		addPage(projectPage);
	}

	@Override
	public boolean performFinish() {
		if (!validate()) {
			return false;
		}
		addError = false;
		conflictResolutionChanged = false;
		final SynchronizationStatus status = new SynchronizationStatus();	
		createProjectMapping(status);
		if (addError) return false;	
		if (conflictResolutionChanged) {
			MessageDialog.openWarning(getShell(), "New Project Mapping", "Conflict resolution 'Overwrite target artifact and ignore locks' is only valid for Defects.  Conflict resolution was changed to 'Overwrite target artifact'.");
		}
		return true;
	}

	private void createProjectMapping(final SynchronizationStatus status) {
		if (mainPage.system1ToSystem2Button.getSelection() || mainPage.bothButton.getSelection()) {
			status.setConflictResolutionPriority(SynchronizationStatus.getConflictResolutionByDescription(mainPage.system1ToSystem2ConflictResolutionCombo.getText()));
			status.setSourceSystemId(projectMappings.getLandscape().getId1());			
			status.setTargetSystemId(projectMappings.getLandscape().getId2());			
			status.setSourceSystemKind(projectMappings.getLandscape().getType1());			
			status.setTargetSystemKind(projectMappings.getLandscape().getType2());
			status.setSourceSystemTimezone(projectMappings.getLandscape().getTimezone1());
			status.setTargetSystemTimezone(projectMappings.getLandscape().getTimezone2());
//			if (projectMappings.getLandscape().getEncoding1() != null && projectMappings.getLandscape().getEncoding1().trim().length() > 0) {
//				status.setSourceSystemEncoding(projectMappings.getLandscape().getEncoding1());
//			}
			status.setGroup(projectMappings.getLandscape().getGroup());
			if (projectMappings.getLandscape().getEncoding2() != null && projectMappings.getLandscape().getEncoding2().trim().length() > 0) {
				status.setTargetSystemEncoding(projectMappings.getLandscape().getEncoding2());
			}
			projectPage.getMappingSection1().updateSourceFields(status);
			projectPage.getMappingSection2().updateTargetFields(status);
				
			createMapping(status);
			if (projectMappings.getLandscape().getRole() == Landscape.ROLE_ADMINISTRATOR) {
				createFieldMappingFile(status);
			}
			if (addError) return;
		}
		if (mainPage.system2ToSystem1Button.getSelection() || mainPage.bothButton.getSelection()) {
			status.setConflictResolutionPriority(SynchronizationStatus.getConflictResolutionByDescription(mainPage.system2ToSystem1ConflictResolutionCombo.getText()));
			status.setSourceSystemId(projectMappings.getLandscape().getId2());
			status.setTargetSystemId(projectMappings.getLandscape().getId1());
			status.setSourceSystemKind(projectMappings.getLandscape().getType2());
			status.setTargetSystemKind(projectMappings.getLandscape().getType1());
			status.setSourceSystemTimezone(projectMappings.getLandscape().getTimezone2());
			status.setTargetSystemTimezone(projectMappings.getLandscape().getTimezone1());
//			if (projectMappings.getLandscape().getEncoding2() != null && projectMappings.getLandscape().getEncoding2().trim().length() > 0) {
//				status.setSourceSystemEncoding(projectMappings.getLandscape().getEncoding2());
//			}
			status.setGroup(projectMappings.getLandscape().getGroup());
			if (projectMappings.getLandscape().getEncoding1() != null && projectMappings.getLandscape().getEncoding1().trim().length() > 0) {
				status.setTargetSystemEncoding(projectMappings.getLandscape().getEncoding1());
			}
			projectPage.getMappingSection2().updateSourceFields(status);
			projectPage.getMappingSection1().updateTargetFields(status);
			
			createMapping(status);
			if (projectMappings.getLandscape().getRole() == Landscape.ROLE_ADMINISTRATOR) {
				createFieldMappingFile(status);
			}
		}
	}
	
	public ProjectMappings getProjectMappings() {
		return projectMappings;
	}

	private void createMapping(final SynchronizationStatus status) {
		if (status.getConflictResolutionPriority().equals(SynchronizationStatus.CONFLICT_RESOLUTION_ALWAYS_OVERRIDE_AND_IGNORE_LOCKS)) {
			if (!SynchronizationStatus.isAlwaysOverrideAndIgnoreLocksValid(status)) {
				status.setConflictResolutionPriority(SynchronizationStatus.CONFLICT_RESOLUTION_ALWAYS_OVERRIDE);
				conflictResolutionChanged = true;
			}
		}
		try {
			ICcfParticipant p1 = Activator.getCcfParticipantForType(status.getSourceSystemKind());
			if (!p1.allowAsSourceRepository(status.getSourceRepositoryId())) {
				showReverseNotAllowedDialog(status);
				addError = true;
				return;
			}
			ICcfParticipant p2 = Activator.getCcfParticipantForType(status.getTargetSystemKind());
			if (!p2.allowAsTargetRepository(status.getTargetRepositoryId())) {
				showReverseNotAllowedDialog(status);
				addError = true;
				return;
			}
		} catch (Exception e) {}
		
		status.setSourceSystemKind(status.getSourceSystemKind() + "_paused");
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				try {
					CcfDataProvider dataProvider = new CcfDataProvider();
					dataProvider.addSynchronizationStatus(projectMappings, status);
				} catch (Exception e) {
					Activator.handleDatabaseError(e, false, true, "New Project Mapping");
					addError = true;
				}
			}			
		});
	}
	
	private void showReverseNotAllowedDialog(SynchronizationStatus status) {
		MessageDialog.openError(Display.getDefault().getActiveShell(), "New Project Mapping", status.getSourceRepositoryId() + "=>" + status.getTargetRepositoryId()	+ " is not a supported project mapping.");
	}
	
	private void createFieldMappingFile(final SynchronizationStatus status) {
		status.setLandscape(projectMappings.getLandscape());
		status.clearXslInfo();
		if (projectMappings.getLandscape().enableEditFieldMapping()) {
			File xslFile = status.getXslFile();
			if (!xslFile.exists()) {
				try {
					File sampleFile = status.getSampleXslFile();
					if (sampleFile != null && sampleFile.exists()) {
						xslFile.createNewFile();
						CcfDataProvider.copyFile(sampleFile, xslFile);
					}
				} catch (IOException e) {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "New Project Mapping", "Unable to create field mapping file " + xslFile.getName() + ":\n\n" + e.getMessage());
					Activator.handleError(e);
					return;
				}
			}
		}
	}
	
	public boolean validate() {
		IMappingSection mappingSection1 = projectPage.getMappingSection1();
		if (mappingSection1 != null && !mappingSection1.validate(projectMappings.getLandscape())) {
			return false;
		}
		
		IMappingSection mappingSection2 = projectPage.getMappingSection2();
		if (mappingSection2 != null && !mappingSection2.validate(projectMappings.getLandscape())) {
			return false;
		}		
		return true;
	}

	public MappingGroup getMappingGroup() {
		return mappingGroup;
	}

}
