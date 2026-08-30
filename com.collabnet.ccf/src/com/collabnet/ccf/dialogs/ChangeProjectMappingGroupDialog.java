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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.db.Filter;
import com.collabnet.ccf.db.Update;
import com.collabnet.ccf.model.Database;
import com.collabnet.ccf.model.SynchronizationStatus;

public class ChangeProjectMappingGroupDialog extends CcfDialog {
	private SynchronizationStatus[] projectMappings;
	private Text groupText;	
	private Button okButton;	
	private Database database;
	private String currentGroup;
	private boolean changeError;
	private boolean multipleGroupsSelected;

	public ChangeProjectMappingGroupDialog(Shell shell, SynchronizationStatus[] projectMappings) {
		super(shell, "ChangeProjectMappingGroupDialog");
		this.projectMappings = projectMappings;
		database = projectMappings[0].getLandscape().getDatabase();
		currentGroup = getCurrentGroup();
	}
	
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Change Project Mapping Group");
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label currentLabel = new Label(composite, SWT.NONE);
		currentLabel.setText("Current group:");
		final Text currentText = new Text(composite, SWT.READ_ONLY | SWT.BORDER);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		gd.widthHint = 300;
		currentText.setLayoutData(gd);
		currentText.setText(currentGroup);
		
		new Label(composite, SWT.NONE);
		
		Label newLabel = new Label(composite, SWT.NONE);
		newLabel.setText("New group:");
		groupText = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		gd.widthHint = 300;
		groupText.setLayoutData(gd);
		if (!multipleGroupsSelected) {
			groupText.setText(currentText.getText());
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
				okButton.setEnabled(!groupText.getText().trim().equals(currentText.getText().trim()));
			}			
		});
		Button groupBrowseButton = new Button(composite, SWT.PUSH);
		groupBrowseButton.setText("Browse...");
		groupBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {		
				GroupSelectionDialog dialog = new GroupSelectionDialog(getShell(), database);
				if (GroupSelectionDialog.OK == dialog.open()) {
					groupText.setText(dialog.getSelectedGroup());
				}
			}			
		});
		
		groupText.setFocus();
		
		return composite;		
	}
	
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        Button button = super.createButton(parent, id, label, defaultButton);
		if (IDialogConstants.OK_ID == id) {
			okButton = button;
			if (!multipleGroupsSelected) {
				okButton.setEnabled(false);
			}
		}
        return button;
    }
	
	@Override
	protected void okPressed() {
		if (multipleGroupsSelected && 0 == groupText.getText().trim().length()) {
			if (!MessageDialog.openQuestion(getShell(), "Change Project Mapping Group", "Are you sure you want to change all the selected project mappings to have no group?")) {
				return;
			}
		}
		changeError = false;
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				try {
					CcfDataProvider dataProvider = new CcfDataProvider();
					for (SynchronizationStatus status : projectMappings) {
						if (!groupText.getText().trim().equals(status.getGroup())) {
							Filter sourceSystemFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_SYSTEM_ID, status.getSourceSystemId(), true);
							Filter sourceRepositoryFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_REPOSITORY_ID, status.getSourceRepositoryId(), true);
							Filter targetSystemFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_TARGET_SYSTEM_ID, status.getTargetSystemId(), true);
							Filter targetRepositoryFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_TARGET_REPOSITORY_ID, status.getTargetRepositoryId(), true);
							Filter[] filters = { sourceSystemFilter, sourceRepositoryFilter, targetSystemFilter, targetRepositoryFilter };
							Update groupUpdate = new Update(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_SYSTEM_ENCODING, groupText.getText().trim());
							Update[] updates = { groupUpdate };						
							dataProvider.updateSynchronizationStatuses(status.getLandscape(), updates, filters);
						}
					}
					if (0 < groupText.getText().trim().length() && !groupText.getText().trim().equals(currentGroup)) {
						if (!dataProvider.groupExists(groupText.getText().trim(), database)) {
							dataProvider.addGroup(groupText.getText().trim(), database);
						}
					}
				} catch (Exception e) {
					Activator.handleDatabaseError(e, false, true, "Change Project Mapping Group");
					changeError = true;
				}
			}			
		});
		if (changeError) return;
		super.okPressed();
	}
	
	private String getCurrentGroup() {
		String currentGroup = null;
		for (SynchronizationStatus projectMapping : projectMappings) {
			if (null != currentGroup && (null == projectMapping.getGroup() || !projectMapping.getGroup().equals(currentGroup))) {
				currentGroup = "(Multiple groups selected)";
				multipleGroupsSelected = true;
				break;
			}
			if (null == projectMapping.getGroup()) {
				currentGroup = "";
			} else {
				currentGroup = projectMapping.getGroup();
			}
		}
		return currentGroup;
	}

}
