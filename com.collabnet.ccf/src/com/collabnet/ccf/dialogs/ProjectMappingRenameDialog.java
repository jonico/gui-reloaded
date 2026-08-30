package com.collabnet.ccf.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class ProjectMappingRenameDialog extends CcfDialog {
	private Button renameFilesButton;
	private Button updateDatabaseButton;
	private boolean renameFiles;
	private boolean updateDatabase;

	public ProjectMappingRenameDialog(Shell shell, boolean renameFiles, boolean updateIdentityMappings) {
		super(shell, "renameProjectMappingDialog");
		this.renameFiles = renameFiles;
		this.updateDatabase = updateIdentityMappings;
	}
	
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Project Mapping Changed");
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		if (renameFiles) {
			renameFilesButton = new Button(composite, SWT.CHECK);
			renameFilesButton.setText("Rename field mapping files");
			renameFilesButton.setSelection(true);
		}
		
		if (updateDatabase) {
			updateDatabaseButton = new Button(composite, SWT.CHECK);
			updateDatabaseButton.setText("Update identity mappings and hospital");
			updateDatabaseButton.setSelection(true);
		}
		
		return composite;
	}

	@Override
	protected void okPressed() {
		if (null != renameFilesButton) {
			renameFiles = renameFilesButton.getSelection();
		}
		if (null != updateDatabaseButton) {
			updateDatabase = updateDatabaseButton.getSelection();
		}
		super.okPressed();
	}

	public boolean isRenameFiles() {
		return renameFiles;
	}

	public boolean isUpdateDatabase() {
		return updateDatabase;
	}

}
