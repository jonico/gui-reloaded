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

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.model.Role;

public class RoleLoginDialog extends CcfDialog {
	private Role role;
	private Text passwordText;
	private Label errorLabel;
	private Label errorText;
	
	private Button okButton;

	public RoleLoginDialog(Shell shell, Role role) {
		super(shell, "RoleLoginDialog");
		this.role = role;
	}
	
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Role Login: " + role.getName());
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite errorGroup = new Composite(parent, SWT.NULL);
		GridLayout errorLayout = new GridLayout();
		errorLayout.numColumns = 2;
		errorGroup.setLayout(errorLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		errorGroup.setLayoutData(gd);
		
		errorLabel = new Label(errorGroup, SWT.NONE);
		errorLabel.setImage(Activator.getImage(Activator.IMAGE_ERROR));
		errorText = new Label(errorGroup, SWT.NONE);
		errorText.setText("Invalid password entered.");
		errorLabel.setVisible(false);
		errorText.setVisible(false);
		
		Label passwordLabel = new Label(composite, SWT.NONE);
		passwordLabel.setText("Password:");
		
		passwordText = new Text(composite, SWT.PASSWORD | SWT.BORDER);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		passwordText.setLayoutData(gd);
		
		passwordText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent me) {
				okButton.setEnabled(passwordText.getText().trim().length() > 0);
			}			
		});
		
		return composite;
	}
	
	@Override
	protected void okPressed() {
		if (!passwordText.getText().trim().equals(role.getPassword())) {
			errorLabel.setVisible(true);
			errorText.setVisible(true);
			return;
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

}
