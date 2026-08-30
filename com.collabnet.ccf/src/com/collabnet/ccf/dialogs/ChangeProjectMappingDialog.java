package com.collabnet.ccf.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.ICcfParticipant;
import com.collabnet.ccf.IMappingSection;
import com.collabnet.ccf.IPageCompleteListener;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.db.Filter;
import com.collabnet.ccf.db.Update;
import com.collabnet.ccf.model.Database;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.SynchronizationStatus;

public class ChangeProjectMappingDialog extends CcfDialog implements IPageCompleteListener {
	private SynchronizationStatus status;
	private ICcfParticipant ccfParticipant1;
	private ICcfParticipant ccfParticipant2;
	private IMappingSection mappingSection1;
	private IMappingSection mappingSection2;

	private String oldGroup;
	private String oldXslFileName;
	private boolean oldUsesGraphicalMapping;
	private boolean oldReverseUsesGraphicalMapping;
	private String newXslFileName;
	private String newGraphicalXslFileName;
	private String newSourceRepositorySchemaFileName;
	private String newTargetRepositorySchemaFileName;
	private String newGenericArtifactToSourceRepositorySchemaFileName;
	private String newGenericArtifactToTargetREpositorySchemaFileName;
	private String newSourceRepositorySchemaToGenericArtifactFileName;
	private String newTargetRepositorySchemaToGenericArtifactFileName;
	private String newMfdFileName;
	
	private Combo conflictResolutionCombo;
	private Text groupText;
	
	private Button okButton;
	
	private Database database;
	private boolean changeError;
	private boolean conflictResolutionChanged;
	private SynchronizationStatus reverseStatus;
	private boolean needsPause;
	private boolean reverseNeedsPause;

	public ChangeProjectMappingDialog(Shell shell, SynchronizationStatus status, SynchronizationStatus reverseStatus) {
		super(shell, "ChangeProjectMappingDialog.2." + status.getSourceSystemId() + "." + status.getTargetSystemId());
		this.status = status;
		this.reverseStatus = reverseStatus;
		if (!status.isPaused()) {
			needsPause = true;
		}
		oldXslFileName = status.getXslFileName();
		oldUsesGraphicalMapping = status.usesGraphicalMapping();
		if (null != reverseStatus) {
			oldReverseUsesGraphicalMapping = reverseStatus.usesGraphicalMapping();
			if (!reverseStatus.isPaused()) {
				reverseNeedsPause = true;
			}
		}
		oldGroup = status.getGroup();
		database = status.getLandscape().getDatabase();
		getCcfParticipants();
	}
	
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Change Project Mapping");
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		if (needsPause || reverseNeedsPause) {
			int delay = Activator.getDefault().getPreferenceStore().getInt(Activator.PREFERENCES_RESET_DELAY);
			Label pauseLabel = new Label(composite, SWT.WRAP);
			pauseLabel.setText("Synchronization will be paused for " + delay + " seconds before project mapping is changed, then resumed automatically.\n\n");		
		}
		
		if (null != ccfParticipant1) {
			mappingSection1 = ccfParticipant1.getMappingSection(1);
			if (null != mappingSection1) {
				mappingSection1.getComposite(composite, status.getLandscape());
				mappingSection1.initializeComposite(status, IMappingSection.TYPE_SOURCE);
				mappingSection1.setProjectPage(this);
			}
		}
		
		if (null != ccfParticipant2) {
			mappingSection2 = ccfParticipant2.getMappingSection(2);
			if (null != mappingSection2) {
				mappingSection2.getComposite(composite, status.getLandscape());
				mappingSection2.initializeComposite(status, IMappingSection.TYPE_TARGET);
				mappingSection2.setProjectPage(this);
			}
		}
		
		Group conflictGroup = new Group(composite, SWT.NULL);
		GridLayout conflictLayout = new GridLayout();
		conflictLayout.numColumns = 1;
		conflictGroup.setLayout(conflictLayout);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		conflictGroup.setLayoutData(gd);	
		conflictGroup.setText("Conflict resolution priority:");

		conflictResolutionCombo = new Combo(conflictGroup, SWT.READ_ONLY);
		conflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_IGNORE);
		conflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE);
		if (SynchronizationStatus.isAlwaysOverrideAndIgnoreLocksValid(status)) {
			conflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE_AND_IGNORE_LOCKS);
		}
		conflictResolutionCombo.add(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_QUARANTINE_ARTIFACT);

		conflictResolutionCombo.setText(SynchronizationStatus.getConflictResolutionDescription(status.getConflictResolutionPriority()));
		
		conflictResolutionCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				okButton.setEnabled(canFinish());
			}			
		});
		
		Composite groupGroup = new Group(composite, SWT.NULL);
		GridLayout groupLayout = new GridLayout();
		groupLayout.numColumns = 3;
		groupGroup.setLayout(groupLayout);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		groupGroup.setLayoutData(gd);	
		
		Label groupLabel = new Label(groupGroup, SWT.NONE);
		groupLabel.setText("Group:");
		groupText = new Text(groupGroup, SWT.BORDER);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		groupText.setLayoutData(gd);
		if (null != status.getGroup()) {
			groupText.setText(status.getGroup());
		}
		groupText.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
		    	String text = e.text;
		    	for (int i = 0; i < text.length(); i++) {
		    		if (0 < text.substring(i, i+1).trim().length() && !text.substring(i, i+1).matches("\\p{Alnum}+")) {
		    			e.doit = false;
		    			break;
		    		}
		    	}
			}			
		});
		groupText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				setPageComplete();
			}			
		});
		Button groupBrowseButton = new Button(groupGroup, SWT.PUSH);
		groupBrowseButton.setText("Browse...");
		groupBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {		
				GroupSelectionDialog dialog = new GroupSelectionDialog(getShell(), database);
				if (GroupSelectionDialog.OK == dialog.open()) {
					groupText.setText(dialog.getSelectedGroup());
				}
			}			
		});
		
		return composite;
	}
	
	@Override
	protected void okPressed() {
		if (!validate()) {
			return;
		}
		changeError = false;
		conflictResolutionChanged = false;
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				try {
					Landscape landscape = status.getLandscape();
					CcfDataProvider dataProvider = new CcfDataProvider();
					
					if (needsPause) {
						dataProvider.pauseSynchronization(status);
					}
					if (reverseNeedsPause) {
						dataProvider.pauseSynchronization(reverseStatus);
					}

					Filter sourceSystemFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_SYSTEM_ID, status.getSourceSystemId(), true);
					Filter sourceRepositoryFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_REPOSITORY_ID, status.getSourceRepositoryId(), true);
					Filter targetSystemFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_TARGET_SYSTEM_ID, status.getTargetSystemId(), true);
					Filter targetRepositoryFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_TARGET_REPOSITORY_ID, status.getTargetRepositoryId(), true);
					Filter[] filters = { sourceSystemFilter, sourceRepositoryFilter, targetSystemFilter, targetRepositoryFilter };

					mappingSection1.updateSourceFields(status);
					mappingSection2.updateTargetFields(status);
					
					String sourceRepository = status.getSourceRepositoryId();
					String targetRepository = status.getTargetRepositoryId();

					if (conflictResolutionCombo.getText().equals(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE_AND_IGNORE_LOCKS)) {
						if (!SynchronizationStatus.isAlwaysOverrideAndIgnoreLocksValid(status)) {
							conflictResolutionCombo.setText(SynchronizationStatus.CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE);
							conflictResolutionChanged = true;
						}
					}
					
					Update sourceRepositoryUpdate = new Update(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_REPOSITORY_ID, sourceRepository);
					Update targetRepositoryUpdate = new Update(CcfDataProvider.SYNCHRONIZATION_STATUS_TARGET_REPOSITORY_ID, targetRepository);
					Update conflictResolutionPriorityUpdate = new Update(CcfDataProvider.SYNCHRONIZATION_STATUS_CONFLICT_RESOLUTION_PRIORITY, SynchronizationStatus.getConflictResolutionByDescription(conflictResolutionCombo.getText()));
					Update groupUpdate = new Update(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_SYSTEM_ENCODING, groupText.getText().trim());
					Update[] updates = { sourceRepositoryUpdate, targetRepositoryUpdate, conflictResolutionPriorityUpdate, groupUpdate };						
					dataProvider.updateSynchronizationStatuses(landscape, updates, filters);

					status.setSourceRepositoryId(sourceRepository);
					status.setTargetRepositoryId(targetRepository);	
					newXslFileName = status.getXslFileName();
					newGraphicalXslFileName = status.getGraphicalXslFileName();
					newSourceRepositorySchemaFileName = status.getSourceRepositorySchemaFileName();
					newTargetRepositorySchemaFileName = status.getTargetRepositorySchemaFileName();
					newGenericArtifactToSourceRepositorySchemaFileName = status.getGenericArtifactToSourceRepositorySchemaFileName();
					newGenericArtifactToTargetREpositorySchemaFileName = status.getGenericArtifactToTargetRepositorySchemaFileName();
					newSourceRepositorySchemaToGenericArtifactFileName = status.getSourceRepositorySchemaToGenericArtifactFileName();
					newTargetRepositorySchemaToGenericArtifactFileName = status.getTargetRepositorySchemaToGenericArtifactFileName();
					newMfdFileName = status.getMFDFileName();
					
					if (null != reverseStatus && !newXslFileName.equals(oldXslFileName)) {
						Filter reverseSourceSystemFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_SYSTEM_ID, reverseStatus.getSourceSystemId(), true);
						Filter reverseSourceRepositoryFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_REPOSITORY_ID, reverseStatus.getSourceRepositoryId(), true);
						Filter reverseTargetSystemFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_TARGET_SYSTEM_ID, reverseStatus.getTargetSystemId(), true);
						Filter reverseTargetRepositoryFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_TARGET_REPOSITORY_ID, reverseStatus.getTargetRepositoryId(), true);
						Filter[] reverseFilters = { reverseSourceSystemFilter, reverseSourceRepositoryFilter, reverseTargetSystemFilter, reverseTargetRepositoryFilter };		
						
						Update reverseSourceRepositoryUpdate = new Update(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_REPOSITORY_ID, targetRepository);
						Update reverseTargetRepositoryUpdate = new Update(CcfDataProvider.SYNCHRONIZATION_STATUS_TARGET_REPOSITORY_ID, sourceRepository);
						Update[] reverseUpdates = { reverseSourceRepositoryUpdate, reverseTargetRepositoryUpdate };	
						dataProvider.updateSynchronizationStatuses(landscape, reverseUpdates, reverseFilters);
						reverseStatus.setSourceRepositoryId(targetRepository);
						reverseStatus.setTargetRepositoryId(sourceRepository);
					}
					
					if (oldUsesGraphicalMapping) {
						status.switchToGraphicalMapping();
					}
					if (null != reverseStatus && oldReverseUsesGraphicalMapping) {
						reverseStatus.switchToGraphicalMapping();
					}
					
					dataProvider.setFieldMappingMode(status);
					if (null != reverseStatus) {
						dataProvider.setFieldMappingMode(reverseStatus);
					}
					
					if (0 < groupText.getText().trim().length() && !groupText.getText().trim().equals(oldGroup)) {
						if (!dataProvider.groupExists(groupText.getText().trim(), database)) {
							dataProvider.addGroup(groupText.getText().trim(), database);
						}
					}
					Activator.notifyProjectMappingChangeListeners(status);
					if (null != reverseStatus) {
						Activator.notifyProjectMappingChangeListeners(reverseStatus);
					}
				} catch (Exception e) {
					Activator.handleDatabaseError(e, false, true, "Change Project Mapping");
					changeError = true;
				}
			}			
		});
		if (changeError) return;
		if (conflictResolutionChanged) {
			MessageDialog.openWarning(getShell(), "Change Project Mapping", "Conflict resolution 'Overwrite target artifact and ignore locks' is only valid for Defects.  Conflict resolution was changed to 'Overwrite target artifact'.");
		}
		super.okPressed();
	}
	
	public boolean isXslFileNameChanged() {
		return !newXslFileName.equals(oldXslFileName);
	}
	
	public String getOldXslFileName() {
		return oldXslFileName;
	}
	
	public String getNewXslFileName() {
		return newXslFileName;
	}
	
	public String getNewGraphicalXslFileName() {
		return newGraphicalXslFileName;
	}

	public String getNewSourceRepositorySchemaFileName() {
		return newSourceRepositorySchemaFileName;
	}

	public String getNewTargetRepositorySchemaFileName() {
		return newTargetRepositorySchemaFileName;
	}

	public String getNewGenericArtifactToSourceRepositorySchemaFileName() {
		return newGenericArtifactToSourceRepositorySchemaFileName;
	}

	public String getNewGenericArtifactToTargetRepositorySchemaFileName() {
		return newGenericArtifactToTargetREpositorySchemaFileName;
	}

	public String getNewSourceRepositorySchemaToGenericArtifactFileName() {
		return newSourceRepositorySchemaToGenericArtifactFileName;
	}

	public String getNewTargetRepositorySchemaToGenericArtifactFileName() {
		return newTargetRepositorySchemaToGenericArtifactFileName;
	}

	public void setNewXslFileName(String newXslFileName) {
		this.newXslFileName = newXslFileName;
	}
	
	public String getNewMfdFileName() {
		return newMfdFileName;
	}
	
	public boolean needsResume() {
		// If it needed to be paused, it will need to be resumed.
		return needsPause;
	}
	
	public boolean reverseNeedsResume() {
		// If it needed to be paused, it will need to be resumed.
		return reverseNeedsPause;
	}

	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        Button button = super.createButton(parent, id, label, defaultButton);
		if (IDialogConstants.OK_ID == id) {
			okButton = button;
			okButton.setEnabled(false);
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
	
	private boolean validate() {
		if (null != mappingSection1 && !mappingSection1.validate(status.getLandscape())) {
			return false;
		}
		if (null != mappingSection2 && !mappingSection2.validate(status.getLandscape())) {
			return false;
		}		
		return true;
	}
	
	private void getCcfParticipants() {
		try {
			ccfParticipant1 = Activator.getCcfParticipantForType(status.getSourceSystemKind());
			ccfParticipant2 = Activator.getCcfParticipantForType(status.getTargetSystemKind());
		} catch (Exception e) {
			Activator.handleError(e);
		}
	}
	
	public void setPageComplete() {
		okButton.setEnabled(canFinish());
	}

}
