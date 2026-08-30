package com.collabnet.ccf.dialogs;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.ICcfParticipant;
import com.collabnet.ccf.IMappingSection;
import com.collabnet.ccf.IPageCompleteListener;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;

public class ReverseProjectMappingDialog extends CcfDialog implements IPageCompleteListener {
	private ProjectMappings projectMappings;
	
	private SynchronizationStatus reverseStatus;
	private ICcfParticipant ccfParticipant1;
	private ICcfParticipant ccfParticipant2;
	private IMappingSection mappingSection1;
	private IMappingSection mappingSection2;

	private Combo conflictResolutionCombo;
	
	private Button okButton;
	
	private boolean addError;
	
	public ReverseProjectMappingDialog(Shell shell, ProjectMappings projectMappings) {
		super(shell, "ReverseProjectMappingDialog");
		this.projectMappings = projectMappings;
	}
	
	public ReverseProjectMappingDialog(Shell shell, ProjectMappings projectMappings, SynchronizationStatus reverseStatus) {
		this(shell, projectMappings);
		this.reverseStatus = reverseStatus;
		getCcfParticipants();
	}
	
	protected Control createDialogArea(Composite parent) {
		getShell().setText("New Project Mapping");
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		if (null != ccfParticipant1) {
			mappingSection1 = ccfParticipant1.getMappingSection(1);
			if (null != mappingSection1) {
				mappingSection1.getComposite(composite, reverseStatus.getLandscape());
				mappingSection1.initializeComposite(reverseStatus, IMappingSection.TYPE_TARGET);
				mappingSection1.setProjectPage(this);
			}
		}
		
		if (null != ccfParticipant2) {
			mappingSection2 = ccfParticipant2.getMappingSection(2);
			if (null != mappingSection2) {
				mappingSection2.getComposite(composite, reverseStatus.getLandscape());
				mappingSection2.initializeComposite(reverseStatus, IMappingSection.TYPE_SOURCE);
				mappingSection2.setProjectPage(this);
			}
		}		

		Label conflictResolutionPriorityLabel = new Label(composite, SWT.NONE);
		conflictResolutionPriorityLabel.setText("Conflict resolution priority:");
		
		conflictResolutionCombo = new Combo(composite, SWT.READ_ONLY);
		conflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_IGNORE);
		conflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE);
		SynchronizationStatus testStatus = new SynchronizationStatus();
		testStatus.setTargetSystemId(reverseStatus.getSourceSystemId());
		testStatus.setTargetRepositoryId(reverseStatus.getSourceRepositoryId());
		if (SynchronizationStatus.isAlwaysOverrideAndIgnoreLocksValid(testStatus)) {
			conflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE_AND_IGNORE_LOCKS);
		} else {
			if (reverseStatus.getConflictResolutionPriority().equals(SynchronizationStatus.CONFLICT_RESOLUTION_ALWAYS_OVERRIDE_AND_IGNORE_LOCKS)) {
				reverseStatus.setConflictResolutionPriority(SynchronizationStatus.CONFLICT_RESOLUTION_ALWAYS_OVERRIDE);
			}
		}
		conflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_QUARANTINE_ARTIFACT);

		conflictResolutionCombo.setText(SynchronizationStatus.getConflictResolutionDescription(reverseStatus.getConflictResolutionPriority()));
		conflictResolutionCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				okButton.setEnabled(canFinish());
			}			
		});

		return composite;
	}

	@Override
	protected void okPressed() {
		boolean conflictResolutionChanged = false;
		addError = false;
		final SynchronizationStatus status = new SynchronizationStatus();
		status.setConflictResolutionPriority(SynchronizationStatus.getConflictResolutionByDescription(conflictResolutionCombo.getText()));
		status.setSourceSystemId(reverseStatus.getTargetSystemId());			
		status.setTargetSystemId(reverseStatus.getSourceSystemId());			
		status.setSourceSystemKind(reverseStatus.getTargetSystemKind());
		String sourceSystemKind = reverseStatus.getSourceSystemKind();
		int index = sourceSystemKind.indexOf("_paused");
		if (-1 != index) {
			sourceSystemKind = sourceSystemKind.substring(0, index);
		}
		status.setTargetSystemKind(sourceSystemKind);
		status.setSourceSystemTimezone(reverseStatus.getTargetSystemTimezone());
		status.setTargetSystemTimezone(reverseStatus.getSourceSystemTimezone());
//		status.setSourceSystemEncoding(reverseStatus.getTargetSystemEncoding());
		status.setGroup(reverseStatus.getGroup());
		status.setTargetSystemEncoding(reverseStatus.getGroup());
		mappingSection1.updateSourceFields(status);
		mappingSection2.updateTargetFields(status);	
		if (status.getConflictResolutionPriority().equals(SynchronizationStatus.CONFLICT_RESOLUTION_ALWAYS_OVERRIDE_AND_IGNORE_LOCKS)) {
			if (!SynchronizationStatus.isAlwaysOverrideAndIgnoreLocksValid(status)) {
				status.setConflictResolutionPriority(SynchronizationStatus.CONFLICT_RESOLUTION_ALWAYS_OVERRIDE);
				conflictResolutionChanged = true;
			}
		}
	
		createMapping(status);
		createFieldMappingFile(status);
		if (addError) return;
		if (conflictResolutionChanged) {
			MessageDialog.openWarning(getShell(), "New Project Mapping", "Conflict resolution 'Overwrite target artifact and ignore locks' is only valid for Defects.  Conflict resolution was changed to 'Overwrite target artifact'.");
		}
		super.okPressed();
	}

	private void createMapping(final SynchronizationStatus status) {
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
	
	private void createFieldMappingFile(final SynchronizationStatus status) {
		status.setLandscape(projectMappings.getLandscape());
		status.clearXslInfo();
		if (projectMappings.getLandscape().enableEditFieldMapping()) {
			File xslFile = status.getXslFile();
			if (!xslFile.exists()) {
				try {
					xslFile.createNewFile();
					File sampleFile = status.getSampleXslFile();
					if (null != sampleFile && sampleFile.exists()) {
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

	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        Button button = super.createButton(parent, id, label, defaultButton);
		if (IDialogConstants.OK_ID == id) {
			okButton = button;
			if (null == reverseStatus) okButton.setEnabled(false);
		}
        return button;
    }
	
	private boolean canFinish() {
		if (null != mappingSection1 && !mappingSection1.isPageComplete()) {
			return false;
		}
		if (null != mappingSection2 && !mappingSection2.isPageComplete()) {
			return false;
		}
		return true;
	}
	
	private void getCcfParticipants() {
		try {
			ccfParticipant1 = Activator.getCcfParticipantForType(reverseStatus.getTargetSystemKind());
			ccfParticipant2 = Activator.getCcfParticipantForType(reverseStatus.getSourceSystemKind());
		} catch (Exception e) {
			Activator.handleError(e);
		}
	}
	
	public void setPageComplete() {
		okButton.setEnabled(canFinish());
	}

}
