package com.collabnet.ccf.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.collabnet.ccf.model.Role;

public class NewRoleDialog extends CcfDialog {
	private Text nameText;
	
	private Button okButton;
	
	private Role role;

	public NewRoleDialog(Shell shell) {
		super(shell, "NewRoleDialog");
	}
	
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Add Role");
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setText("Role name:");
		nameText = new Text(composite, SWT.BORDER);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		nameText.setLayoutData(gd);
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				okButton.setEnabled(canFinish());
			}			
		});
		
		return composite;
	}
	
	@Override
	protected void okPressed() {
		if (null == role) {
			role = new Role(nameText.getText().trim());
		} else {
			role.setName(nameText.getText().trim());
		}
		super.okPressed();
	}
	
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        Button button = super.createButton(parent, id, label, defaultButton);
		if (IDialogConstants.OK_ID == id) {
			okButton = button;
			okButton.setEnabled(false);
		}
        return button;
    }

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
	
	private boolean canFinish() {
		return nameText.getText().trim().length() > 0;
	}

}
