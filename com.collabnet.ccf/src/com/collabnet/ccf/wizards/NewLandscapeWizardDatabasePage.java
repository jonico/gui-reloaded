package com.collabnet.ccf.wizards;

import java.sql.DriverManager;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.collabnet.ccf.Activator;

public class NewLandscapeWizardDatabasePage extends WizardPage {
	private Text urlText;
	private Text driverText;
	private Text userText;
	private Text passwordText;
	private Button testButton;

	public NewLandscapeWizardDatabasePage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	public void createControl(Composite parent) {
		Composite outerContainer = new Composite(parent,SWT.NONE);
		outerContainer.setLayout(new GridLayout());
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

		Group databaseGroup = new Group(outerContainer, SWT.NONE);
		databaseGroup.setText("Database:");
		GridLayout databaseLayout = new GridLayout();
		databaseLayout.numColumns = 2;
		databaseGroup.setLayout(databaseLayout);
		databaseGroup.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		Label urlLabel = new Label(databaseGroup, SWT.NONE);
		urlLabel.setText("URL:");
		urlText = new Text(databaseGroup, SWT.BORDER);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		urlText.setLayoutData(data);
		urlText.setText(Activator.DATABASE_DEFAULT_URL);
		
		Label driverLabel = new Label(databaseGroup, SWT.NONE);
		driverLabel.setText("Driver:");
		driverText = new Text(databaseGroup, SWT.BORDER);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		driverText.setLayoutData(data);
		driverText.setText(Activator.DATABASE_DEFAULT_DRIVER);
		
		Label userLabel = new Label(databaseGroup, SWT.NONE);
		userLabel.setText("User:");
		userText = new Text(databaseGroup, SWT.BORDER);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		userText.setLayoutData(data);
		userText.setText(Activator.DATABASE_DEFAULT_USER);
		
		Label passwordLabel = new Label(databaseGroup, SWT.NONE);
		passwordLabel.setText("Password:");
		passwordText = new Text(databaseGroup, SWT.BORDER);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		passwordText.setLayoutData(data);
		passwordText.setEchoChar('*');
		passwordText.setText(Activator.DATABASE_DEFAULT_PASSWORD);
		
		testButton = new Button(databaseGroup, SWT.PUSH);
		testButton.setText("Test Connection");
		data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		data.horizontalSpan = 2;
		testButton.setLayoutData(data);
		testButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				testConnection(false);
			}			
		});
		
		ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent me) {
				setPageComplete(canFinish());
				testButton.setEnabled(canFinish());
			}		
		};
		urlText.addModifyListener(modifyListener);
		driverText.addModifyListener(modifyListener);
		
		setMessage("Enter database information");

		setControl(outerContainer);
	}

	private boolean canFinish() {
		return urlText.getText().trim().length() > 0 &&
		driverText.getText().trim().length() > 0;
	}
	
	public String getUrl() {
		if (null == urlText) {
			return Activator.DATABASE_DEFAULT_URL;
		}
		return urlText.getText().trim();
	}
	
	public String getDriver() {
		if (null == driverText) {
			return Activator.DATABASE_DEFAULT_DRIVER;
		}
		return driverText.getText().trim();
	}
	
	public String getUser() {
		if (null == userText) {
			return Activator.DATABASE_DEFAULT_USER;
		}
		return userText.getText().trim();
	}
	
	public String getPassword() {
		if (null == passwordText) {
			return Activator.DATABASE_DEFAULT_PASSWORD;
		}
		return passwordText.getText().trim();
	}
	
	public boolean testConnection(boolean saving) {
		try {
			Class.forName(driverText.getText().trim());
			DriverManager.getConnection(urlText.getText().trim(), userText.getText().trim(), passwordText.getText().trim());	
			if (!saving) MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Test Connection", "Connection successful!");
		} catch (Exception e) {
			if (saving) {
				return MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "Landscape","Could not connect to database:\n\n" + e.getLocalizedMessage() + "\n\nSave anyway?");
			}
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Test Connection", "Connection failed:\n\n" + e.getLocalizedMessage());
		}
		return true;
	}
}
