package com.collabnet.ccf.preferences;

import java.sql.DriverManager;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.views.CcfExplorerView;
import com.collabnet.ccf.views.HospitalView;

public class CcfPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Combo descriptionCombo;
	private Text urlText;
	private Text driverText;
	private Text userText;
	private Text passwordText;
	private Button autoConnectButton;
	private Button hospitalCountButton;
	
	private Text resetDelayText;
	
	private Button encryptIfPreviouslyEncryptedButton;
	private Button encryptAlwaysButton;
	private Button encryptNeverButton;
	
	private IPreferenceStore store = Activator.getDefault().getPreferenceStore();
	
	public static final String ID = "com.collabnet.ccf.preferences";
	
	public CcfPreferencePage() {
		super();
	}

	public CcfPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	public CcfPreferencePage(String title) {
		super(title);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gd);
		
		Group databaseGroup = new Group(composite, SWT.NULL);
		GridLayout clientLayout = new GridLayout();
		clientLayout.numColumns = 2;
		databaseGroup.setLayout(clientLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		databaseGroup.setLayoutData(gd);	
		databaseGroup.setText("Database:");
		
		Label descriptionLabel = new Label(databaseGroup, SWT.NONE);
		descriptionLabel.setText("Description:");
		descriptionCombo = new Combo(databaseGroup, SWT.BORDER);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.widthHint = 300;
		descriptionCombo.setLayoutData(gd);
		
		Label urlLabel = new Label(databaseGroup, SWT.NONE);
		urlLabel.setText("URL:");
		urlText = new Text(databaseGroup, SWT.BORDER);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.widthHint = 300;
		urlText.setLayoutData(gd);
		
		Label driverLabel = new Label(databaseGroup, SWT.NONE);
		driverLabel.setText("Driver:");
		driverText = new Text(databaseGroup, SWT.BORDER);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.widthHint = 300;
		driverText.setLayoutData(gd);
		
		Label userLabel = new Label(databaseGroup, SWT.NONE);
		userLabel.setText("User:");
		userText = new Text(databaseGroup, SWT.BORDER);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.widthHint = 300;
		userText.setLayoutData(gd);
		
		Label passwordLabel = new Label(databaseGroup, SWT.NONE);
		passwordLabel.setText("Password:");
		passwordText = new Text(databaseGroup, SWT.BORDER);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.widthHint = 300;
		passwordText.setLayoutData(gd);
		passwordText.setEchoChar('*');
		
		autoConnectButton = new Button(databaseGroup, SWT.CHECK);
		autoConnectButton.setText("Connect on startup");
		gd = new GridData();
		gd.horizontalSpan = 2;
		autoConnectButton.setLayoutData(gd);
		
		Button testButton = new Button(databaseGroup, SWT.PUSH);
		testButton.setText("Test Connection");
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_CENTER;
		testButton.setLayoutData(gd);
		testButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				try {
					Class.forName(driverText.getText().trim());
					DriverManager.getConnection(urlText.getText().trim(), userText.getText().trim(), passwordText.getText().trim());	
					MessageDialog.openInformation(getShell(), "Test Connection", "Connection successful!");
				} catch (Exception e) {
					MessageDialog.openError(getShell(), "Test Connection", "Connection failed:\n\n" + e.getLocalizedMessage());
				}
			}			
		});
		
		Group encryptGroup = new Group(composite, SWT.NULL);
		GridLayout encryptLayout = new GridLayout();
		encryptLayout.numColumns = 1;
		encryptGroup.setLayout(encryptLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		encryptGroup.setLayoutData(gd);	
		encryptGroup.setText("Obfuscate passwords:");
		
		encryptIfPreviouslyEncryptedButton = new Button(encryptGroup, SWT.RADIO);
		encryptIfPreviouslyEncryptedButton.setText("If previously obfuscated");
		encryptAlwaysButton = new Button(encryptGroup, SWT.RADIO);
		encryptAlwaysButton.setText("Always");	
		encryptNeverButton = new Button(encryptGroup, SWT.RADIO);
		encryptNeverButton.setText("Never");
		
		Label resetDelayLabel = new Label(composite, SWT.NONE);
		resetDelayLabel.setText("Auto-resume delay (seconds):");
		resetDelayText = new Text(composite, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = 75;
		resetDelayText.setLayoutData(gd);
		
		hospitalCountButton = new Button(composite, SWT.CHECK);
		hospitalCountButton.setText("Show hospital entry count for project mappings in CCF Explorer");
		gd = new GridData();
		gd.horizontalSpan = 2;
		hospitalCountButton.setLayoutData(gd);
		
		initializeValues();
		
		return composite;
	}
	
	public boolean performOk() {
		boolean needsRefresh =
			!(urlText.getText().trim().equals(store.getString(Activator.PREFERENCES_DATABASE_URL))) ||
			!(driverText.getText().trim().equals(store.getString(Activator.PREFERENCES_DATABASE_DRIVER))) ||
			!(userText.getText().trim().equals(store.getString(Activator.PREFERENCES_DATABASE_USER))) ||
			!(passwordText.getText().trim().equals(Activator.decodePassword(store.getString(Activator.PREFERENCES_DATABASE_PASSWORD))));			
		boolean showHospitalChanged = store.getBoolean(Activator.PREFERENCES_SHOW_HOSPITAL_COUNT) != hospitalCountButton.getSelection();
		String previousPassword = store.getString(Activator.PREFERENCES_DATABASE_PASSWORD);
		boolean passwordPreviouslyEncoded = previousPassword != null && previousPassword.startsWith(Activator.OBFUSCATED_PASSWORD_PREFIX);
		store.setValue(Activator.PREFERENCES_DATABASE_DESCRIPTION, descriptionCombo.getText().trim());
		store.setValue(Activator.PREFERENCES_DATABASE_URL, urlText.getText().trim());
		store.setValue(Activator.PREFERENCES_DATABASE_DRIVER, driverText.getText().trim());
		store.setValue(Activator.PREFERENCES_DATABASE_USER, userText.getText().trim());
		store.setValue(Activator.PREFERENCES_DATABASE_PASSWORD, Activator.encodePassword(passwordText.getText().trim(), passwordPreviouslyEncoded));
		store.setValue(Activator.PREFERENCES_AUTOCONNECT, autoConnectButton.getSelection());
		int resetDelay = Activator.DEFAULT_RESET_DELAY;
		try {
			resetDelay = Integer.parseInt(resetDelayText.getText().trim());
		} catch (Exception e) {}
		store.setValue(Activator.PREFERENCES_RESET_DELAY, resetDelay);
		if (needsRefresh && null != HospitalView.getView() && HospitalView.getView().isHospitalLoaded()) {
			HospitalView.getView().refresh();
		}
		store.setValue(Activator.PREFERENCES_SHOW_HOSPITAL_COUNT, hospitalCountButton.getSelection());
		if (encryptAlwaysButton.getSelection()) {
			store.setValue(Activator.PREFERENCES_ENCRYPT_PASSWORDS, Activator.ENCRYPT_PASSWORDS_ALWAYS);
		}
		else if (encryptNeverButton.getSelection()) {
			store.setValue(Activator.PREFERENCES_ENCRYPT_PASSWORDS, Activator.ENCRYPT_PASSWORDS_NEVER);
		}
		else {
			store.setValue(Activator.PREFERENCES_ENCRYPT_PASSWORDS, Activator.ENCRYPT_PASSWORDS_IF_PREVIOUSLY_ENCRYPTED);
		}
		if (showHospitalChanged && null != CcfExplorerView.getView()) {
			CcfExplorerView.getView().refreshProjectMappings();
		}
		return super.performOk();
	}
	
	protected void performDefaults() {
		descriptionCombo.setText(Activator.DATABASE_DEFAULT_DESCRIPTION);
		urlText.setText(Activator.DATABASE_DEFAULT_URL);
		driverText.setText(Activator.DATABASE_DEFAULT_DRIVER);
		userText.setText(Activator.DATABASE_DEFAULT_USER);
		passwordText.setText(Activator.DATABASE_DEFAULT_PASSWORD);
		autoConnectButton.setSelection(Activator.DEFAULT_AUTOCONNECT);
		resetDelayText.setText(Integer.toString(Activator.DEFAULT_RESET_DELAY));
		hospitalCountButton.setSelection(Activator.DEFAULT_SHOW_HOSPITAL_COUNT);
		encryptIfPreviouslyEncryptedButton.setSelection(true);
		encryptAlwaysButton.setSelection(false);
		encryptNeverButton.setSelection(false);
		super.performDefaults();
	}

	public void init(IWorkbench workbench) {
	}
	
	private void initializeValues() {
		descriptionCombo.add(store.getString(Activator.PREFERENCES_DATABASE_DESCRIPTION));
		descriptionCombo.setText(store.getString(Activator.PREFERENCES_DATABASE_DESCRIPTION));
		urlText.setText(store.getString(Activator.PREFERENCES_DATABASE_URL));
		driverText.setText(store.getString(Activator.PREFERENCES_DATABASE_DRIVER));
		userText.setText(store.getString(Activator.PREFERENCES_DATABASE_USER));
		passwordText.setText(Activator.decodePassword(store.getString(Activator.PREFERENCES_DATABASE_PASSWORD)));
		autoConnectButton.setSelection(store.getBoolean(Activator.PREFERENCES_AUTOCONNECT));
		resetDelayText.setText(Integer.toString(store.getInt(Activator.PREFERENCES_RESET_DELAY)));
		hospitalCountButton.setSelection(store.getBoolean(Activator.PREFERENCES_SHOW_HOSPITAL_COUNT));
		int encrypt = store.getInt(Activator.PREFERENCES_ENCRYPT_PASSWORDS);
		switch (encrypt) {
		case Activator.ENCRYPT_PASSWORDS_ALWAYS:
			encryptAlwaysButton.setSelection(true);
			break;
		case Activator.ENCRYPT_PASSWORDS_NEVER:
			encryptNeverButton.setSelection(true);
			break;			
		default:
			encryptIfPreviouslyEncryptedButton.setSelection(true);
			break;
		}
	}

}
