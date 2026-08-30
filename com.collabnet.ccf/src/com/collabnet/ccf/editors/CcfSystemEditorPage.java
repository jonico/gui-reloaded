package com.collabnet.ccf.editors;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.TimeZone;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.ICcfParticipant;
import com.collabnet.ccf.IConnectionTester;

public class CcfSystemEditorPage extends CcfEditorPage {
	private ScrolledForm form;
	private FormToolkit toolkit;
	
	private Label errorImageLabel;
	private Label errorTextLabel;
	
	private int type;
	private Properties properties;
	private int systemNumber;
	
	private Text urlText;
	private Text userText;
	private Text displayNameText;
	private Text passwordText;
	private Text resyncUserText;
	private Text resyncDisplayNameText;
	private Text resyncPasswordText;
	private Text attachmentSizeText;
	private Combo timeZonesCombo;
	
	private String url;
	private String user;
	private String displayName;
	private String password;
	private String resyncUser;
	private String resyncDisplayName;
	private String resyncPassword;
	private String attachmentSize;
	private String timezone;
	
	private IConnectionTester connectionTester;
	
	public static final int QC = 0;
	public static final int TF = 1;
	public static final int PT = 2;
	public static final int SW = 3;
	
	public final static String SYSTEM_SECTION_STATE = "CcfSystemEditorPage.systemSectionExpanded";
	public final static String CREDENTIALS_SECTION_STATE = "CcfSystemEditorPage.credentialsSectionExpanded";
	public final static String RESYNC_SECTION_STATE = "CcfSystemEditorPage.resyncSectionExpanded";

	public CcfSystemEditorPage(FormEditor editor, String id, String title, int type) {
		super(editor, id, title);
		this.type = type;
	}

	@Override
	public boolean canLeaveThePage() {
		if (0 == urlText.getText().trim().length()) return false;
		if (0 < attachmentSizeText.getText().trim().length()) {
			int maxSize = 0;
			try {
				maxSize = Integer.parseInt(attachmentSizeText.getText().trim());
			} catch (Exception e) {}
			if (0 >= maxSize) return false;
		}
		return true;
	}

	private void createControls(Composite composite) {		
		Label headerImageLabel = new Label(composite, SWT.NONE);
		headerImageLabel.setImage(Activator.getImage(getLandscape()));
		headerImageLabel.setBackground(((FormEditor)getEditor()).getToolkit().getColors().getBackground());
		
		Label headerLabel = new Label(composite, SWT.NONE);
		headerLabel.setBackground(((FormEditor)getEditor()).getToolkit().getColors().getBackground());
		headerLabel.setFont(JFaceResources.getHeaderFont());
        TableWrapData td = new TableWrapData(TableWrapData.FILL);
        headerLabel.setLayoutData(td);
        
        switch (systemNumber) {
		case 1:
			properties = getLandscape().getProperties1();
			break;
		case 2:
			properties = getLandscape().getProperties2();
			break;
		default:
			break;
		}
        ICcfParticipant ccfParticipant = null;
		switch (type) {
		case QC:
			headerLabel.setText("QC Properties");
			initializeQcValues();
			break;
		case TF:
			headerLabel.setText("TeamForge Properties");
			try {
				ccfParticipant = Activator.getCcfParticipantForType("TF");
			} catch (Exception e1) {}
			initializeTeamForgeValues();
			break;
		case PT:
			headerLabel.setText("Project Tracker Properties");
			initializeCeeValues();
			break;
		case SW:
			headerLabel.setText("ScrumWorks Properties");
			try {
				ccfParticipant = Activator.getCcfParticipantForType("SWP");
			} catch (Exception e1) {}
			initializeSwValues();
			break;					
		default:
			break;
		}
		
		if (null != ccfParticipant) {
			connectionTester = ccfParticipant.getConnectionTester();
		}
		
		timezone = properties.getProperty(Activator.PROPERTIES_SYSTEM_TIMEZONE, TimeZone.getDefault().getID()); //$NON-NLS-1$	
        
		errorImageLabel = new Label(composite, SWT.NONE);
		errorImageLabel.setImage(Activator.getImage(Activator.IMAGE_ERROR));
		errorImageLabel.setBackground(((FormEditor)getEditor()).getToolkit().getColors().getBackground());
		errorImageLabel.setVisible(false);
		
		errorTextLabel = new Label(composite, SWT.NONE);
		errorTextLabel.setText("One or more required field is empty or invalid.");
		errorTextLabel.setBackground(((FormEditor)getEditor()).getToolkit().getColors().getBackground());
		errorTextLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		errorTextLabel.setVisible(false);
		
		td = new TableWrapData(TableWrapData.FILL);
		td.align = TableWrapData.BOTTOM;
		errorTextLabel.setLayoutData(td);
		
		Section systemSection = toolkit.createSection(composite, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 4;
        systemSection.setLayoutData(td);
        systemSection.setText("System");
        Composite systemSectionClient = toolkit.createComposite(systemSection); 
        GridLayout systemLayout = new GridLayout();
        systemLayout.numColumns = 2;
        systemLayout.verticalSpacing = 10;
        systemSectionClient.setLayout(systemLayout);
        systemSection.setClient(systemSectionClient);
        systemSection.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
                if (e.getState()) getDialogSettings().put(SYSTEM_SECTION_STATE, STATE_EXPANDED);
                else getDialogSettings().put(SYSTEM_SECTION_STATE, STATE_CONTRACTED);
            }
        });
        
        toolkit.createLabel(systemSectionClient, "URL:");
        urlText = toolkit.createText(systemSectionClient, url);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		urlText.setLayoutData(gd);

		toolkit.createLabel(systemSectionClient, "Time zone:");
		timeZonesCombo = new Combo(systemSectionClient, SWT.READ_ONLY);
		String[] timeZoneIds = TimeZone.getAvailableIDs();
		Arrays.sort(timeZoneIds);
		for (String zone : timeZoneIds) {
			timeZonesCombo.add(zone);
		}	
		if (null != timezone) {
			timeZonesCombo.select(timeZonesCombo.indexOf(timezone));
		}
		
		Composite sizeGroup = toolkit.createComposite(systemSectionClient);
		GridLayout sizeLayout = new GridLayout();
		sizeLayout.numColumns = 2;
		sizeLayout.marginWidth = 0;
		sizeLayout.marginHeight = 0;
		sizeGroup.setLayout(sizeLayout);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		sizeGroup.setLayoutData(gd);	
		
		toolkit.createLabel(sizeGroup, "Maximum attachment size per artifact (bytes):");
		attachmentSizeText = toolkit.createText(sizeGroup, attachmentSize);
		gd = new GridData();
		gd.widthHint = 100;
		attachmentSizeText.setLayoutData(gd);
		
		Section credentialsSection = toolkit.createSection(composite, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 4;
        credentialsSection.setLayoutData(td);
        credentialsSection.setText("Credentials");
        Composite credentialsSectionClient = toolkit.createComposite(credentialsSection); 
        GridLayout credentialsLayout = new GridLayout();
        credentialsLayout.numColumns = 2;
        credentialsLayout.verticalSpacing = 10;
        credentialsSectionClient.setLayout(credentialsLayout);
        credentialsSection.setClient(credentialsSectionClient);
        credentialsSection.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
                if (e.getState()) getDialogSettings().put(CREDENTIALS_SECTION_STATE, STATE_EXPANDED);
                else getDialogSettings().put(CREDENTIALS_SECTION_STATE, STATE_CONTRACTED);
            }
        });
        
        toolkit.createLabel(credentialsSectionClient, "User:");
        userText = toolkit.createText(credentialsSectionClient, user);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		userText.setLayoutData(gd);
		if (PT == type) {
	        toolkit.createLabel(credentialsSectionClient, "Display name:");
	        displayNameText = toolkit.createText(credentialsSectionClient, displayName);
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			displayNameText.setLayoutData(gd);			
		}
        toolkit.createLabel(credentialsSectionClient, "Password:");
        passwordText = toolkit.createText(credentialsSectionClient, password);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		passwordText.setLayoutData(gd);
		passwordText.setEchoChar('*');
		
		if (null != connectionTester) {
			Button testButton = toolkit.createButton(credentialsSectionClient, "Test Connection", SWT.PUSH);
			gd = new GridData();
			gd.horizontalSpan = 2;
			gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_CENTER;
			testButton.setLayoutData(gd);
			testButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent se) {
					testConnection(urlText.getText().trim(), userText.getText().trim(), passwordText.getText().trim());
				}			
			});			
		}
        
		Section resyncSection = null;
		Composite resyncSectionClient = null;
		if (SW == type) {
			resyncSection = toolkit.createSection(composite, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
	        td = new TableWrapData(TableWrapData.FILL_GRAB);
	        td.colspan = 4;
	        resyncSection.setLayoutData(td);
	        resyncSection.setText("Resync user");
	        resyncSectionClient = toolkit.createComposite(resyncSection); 
	        GridLayout resyncLayout = new GridLayout();
	        resyncLayout.numColumns = 2;
	        resyncLayout.verticalSpacing = 10;
	        resyncSectionClient.setLayout(resyncLayout);
	        resyncSection.setClient(resyncSectionClient);
	        resyncSection.addExpansionListener(new ExpansionAdapter() {
	            public void expansionStateChanged(ExpansionEvent e) {
	                form.reflow(true);
	                if (e.getState()) getDialogSettings().put(RESYNC_SECTION_STATE, STATE_EXPANDED);
	                else getDialogSettings().put(RESYNC_SECTION_STATE, STATE_CONTRACTED);
	            }
	        }); 
	        
	        toolkit.createLabel(resyncSectionClient, "User:");
	        resyncUserText = toolkit.createText(resyncSectionClient, resyncUser);
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			resyncUserText.setLayoutData(gd);
			if (PT == type) {
		        toolkit.createLabel(resyncSectionClient, "Display name:");
		        resyncDisplayNameText = toolkit.createText(resyncSectionClient, resyncDisplayName);
				gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
				resyncDisplayNameText.setLayoutData(gd);			
			}
	        toolkit.createLabel(resyncSectionClient, "Password:");
	        resyncPasswordText = toolkit.createText(resyncSectionClient, resyncPassword);
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			resyncPasswordText.setLayoutData(gd);
			resyncPasswordText.setEchoChar('*'); 
			
			if (null != connectionTester) {
				Button testButton = toolkit.createButton(resyncSectionClient, "Test Connection", SWT.PUSH);
				gd = new GridData();
				gd.horizontalSpan = 2;
				gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_CENTER;
				testButton.setLayoutData(gd);
				testButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent se) {
						testConnection(urlText.getText().trim(), resyncUserText.getText().trim(), resyncPasswordText.getText().trim());
					}			
				});			
			}
		}
		
        toolkit.paintBordersFor(systemSectionClient);
        toolkit.paintBordersFor(credentialsSectionClient);
        if (null != resyncSectionClient) {
        	toolkit.paintBordersFor(resyncSectionClient);
        }
        
        String expansionState = getDialogSettings().get(SYSTEM_SECTION_STATE);
        systemSection.setExpanded(expansionState == null  || expansionState.equals(STATE_EXPANDED));
        expansionState = getDialogSettings().get(CREDENTIALS_SECTION_STATE);
        credentialsSection.setExpanded(expansionState == null  || expansionState.equals(STATE_EXPANDED));
        if (null != resyncSection) {
	        expansionState = getDialogSettings().get(RESYNC_SECTION_STATE);
	        resyncSection.setExpanded(expansionState == null  || expansionState.equals(STATE_EXPANDED));
        }
        
        ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent me) {
				errorImageLabel.setVisible(!canLeaveThePage());
				errorTextLabel.setVisible(!canLeaveThePage());
				((CcfEditor)getEditor()).setDirty();
			}			
		};
		
		urlText.addModifyListener(modifyListener);
		attachmentSizeText.addModifyListener(modifyListener);
		userText.addModifyListener(modifyListener);
		if (null != displayNameText) displayNameText.addModifyListener(modifyListener);
		passwordText.addModifyListener(modifyListener);
		
		if (null != resyncSection) {
			resyncUserText.addModifyListener(modifyListener);
			if (null != resyncDisplayNameText) resyncDisplayNameText.addModifyListener(modifyListener);
			resyncPasswordText.addModifyListener(modifyListener);
		}
		
		SelectionListener selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				((CcfEditor)getEditor()).setDirty();
			}			
		};
		
		timeZonesCombo.addSelectionListener(selectionListener);
		
		FocusListener focusListener = new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				((Text)e.getSource()).selectAll();
			}
			public void focusLost(FocusEvent e) {
				((Text)e.getSource()).setText(((Text)e.getSource()).getText());
			}					
		};
		
		urlText.addFocusListener(focusListener);
		attachmentSizeText.addFocusListener(focusListener);
		userText.addFocusListener(focusListener);
		if (null != displayNameText) displayNameText.addFocusListener(focusListener);
		passwordText.addFocusListener(focusListener);
		if (null != resyncSection) {
			resyncUserText.addFocusListener(focusListener);
			if (null != resyncDisplayNameText) resyncDisplayNameText.addFocusListener(focusListener);
			resyncPasswordText.addFocusListener(focusListener);
		}
		
		urlText.setFocus();
	}
	
	private void testConnection(String url, String user, String password) {
		Exception connectionError = connectionTester.testConnection(url, user, password);
		if (null == connectionError) {
			MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Test Connection", "Connection successful!");
		} else {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Test Connection", "Connection failed:\n\n" + connectionError.getLocalizedMessage());
		}
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		form = managedForm.getForm();
        toolkit = getEditor().getToolkit();
        TableWrapLayout formLayout = new TableWrapLayout();
        formLayout.numColumns = 4;
        form.getBody().setLayout(formLayout);
		createControls(form.getBody());
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		if (null == urlText) return;
		try {
			properties.setProperty(Activator.PROPERTIES_SYSTEM_TIMEZONE, timeZonesCombo.getText());
			
			File folder = new File(getLandscape().getConfigurationFolder());
			File propertiesFile1 = null;
			File propertiesFile2 = null;
			
			switch (type) {
			case QC:
				setQcProperties();
				if (null != getLandscape().getConfigurationFolder1()) {
					folder = new File(getLandscape().getConfigurationFolder1());
					propertiesFile1 = new File(folder, "qc.properties");
				}			
				if (null != getLandscape().getConfigurationFolder2()) {
					folder = new File(getLandscape().getConfigurationFolder2());
					propertiesFile2 = new File(folder, "qc.properties");
				}
				initializeQcValues();
				break;
			case TF:
				setTeamForgeProperties();
				if (null != getLandscape().getConfigurationFolder1()) {
					folder = new File(getLandscape().getConfigurationFolder1());
					propertiesFile1 = new File(folder, "sfee.properties");
				}			
				if (null != getLandscape().getConfigurationFolder2()) {
					folder = new File(getLandscape().getConfigurationFolder2());
					propertiesFile2 = new File(folder, "sfee.properties");
				}	
				initializeTeamForgeValues();
				break;
			case PT:
				setCeeProperties();
				if (null != getLandscape().getConfigurationFolder1()) {
					folder = new File(getLandscape().getConfigurationFolder1());
					propertiesFile1 = new File(folder, "cee.properties");
				}			
				if (null != getLandscape().getConfigurationFolder2()) {
					folder = new File(getLandscape().getConfigurationFolder2());
					propertiesFile2 = new File(folder, "cee.properties");
				}
				initializeCeeValues();
				break;
			case SW:
				setSwProperties();
				if (null != getLandscape().getConfigurationFolder1()) {
					folder = new File(getLandscape().getConfigurationFolder1());
					propertiesFile1 = new File(folder, "swp.properties");
				}			
				if (null != getLandscape().getConfigurationFolder2()) {
					folder = new File(getLandscape().getConfigurationFolder2());
					propertiesFile2 = new File(folder, "swp.properties");
				}
				initializeSwValues();
				break;							
			default:
				break;
			}
			if (null != propertiesFile1) {
				FileOutputStream outputStream = new FileOutputStream(propertiesFile1);
				properties.store(outputStream, null);
				outputStream.close();
			}
			if (null != propertiesFile2) {
				FileOutputStream outputStream = new FileOutputStream(propertiesFile2);
				properties.store(outputStream, null);
				outputStream.close();
			}
			timezone = timeZonesCombo.getText();
		} catch (Exception e) {
			Activator.handleError(e);
		}	
	}

	private void initializeCeeValues() {
		url = properties.getProperty(Activator.PROPERTIES_CEE_URL, "");
		user = properties.getProperty(Activator.PROPERTIES_CEE_USER, "");
		displayName = properties.getProperty(Activator.PROPERTIES_CEE_DISPLAY_NAME, "");
		password = Activator.decodePassword(properties.getProperty(Activator.PROPERTIES_CEE_PASSWORD, ""));	
		attachmentSize = properties.getProperty(Activator.PROPERTIES_CEE_ATTACHMENT_SIZE, "10485760");			
	}
	
	private void initializeQcValues() {
		url = properties.getProperty(Activator.PROPERTIES_QC_URL, "");
		user = properties.getProperty(Activator.PROPERTIES_QC_USER, "");
		password = Activator.decodePassword(properties.getProperty(Activator.PROPERTIES_QC_PASSWORD, ""));
		attachmentSize = properties.getProperty(Activator.PROPERTIES_QC_ATTACHMENT_SIZE, "10485760");
	}
	
	private void initializeTeamForgeValues() {
		url = properties.getProperty(Activator.PROPERTIES_SFEE_URL, "");
		user = properties.getProperty(Activator.PROPERTIES_SFEE_USER, "");
		password = Activator.decodePassword(properties.getProperty(Activator.PROPERTIES_SFEE_PASSWORD, ""));		
		attachmentSize = properties.getProperty(Activator.PROPERTIES_SFEE_ATTACHMENT_SIZE, "10485760");	
	}
	
	private void initializeSwValues() {
		url = properties.getProperty(Activator.PROPERTIES_SW_URL, "");
		user = properties.getProperty(Activator.PROPERTIES_SW_USER, "");
		password = Activator.decodePassword(properties.getProperty(Activator.PROPERTIES_SW_PASSWORD, ""));	
		resyncUser = properties.getProperty(Activator.PROPERTIES_SW_RESYNC_USER, "");
		resyncPassword = Activator.decodePassword(properties.getProperty(Activator.PROPERTIES_SW_RESYNC_PASSWORD, ""));	
		attachmentSize = properties.getProperty(Activator.PROPERTIES_SW_ATTACHMENT_SIZE, "10485760");	
	}
	
	public boolean isDirty() {
		if (null == urlText) return false;
		return !urlText.getText().trim().equals(url) ||
		!attachmentSizeText.getText().trim().equals(attachmentSize) ||
		!timeZonesCombo.getText().equals(timezone) ||
		!userText.getText().trim().equals(user) ||
		(displayNameText != null && !displayNameText.getText().trim().equals(displayName)) ||
		!passwordText.getText().trim().equals(password) ||
		(resyncUserText != null && !resyncUserText.getText().trim().equals(resyncUser)) ||
		(resyncDisplayNameText != null && !resyncDisplayNameText.getText().trim().equals(resyncDisplayName)) ||
		(resyncPasswordText != null && !resyncPasswordText.getText().trim().equals(resyncPassword));
	}
	
	private void setCeeProperties() throws Exception {
		String password = properties.getProperty(Activator.PROPERTIES_CEE_PASSWORD);
		boolean passwordPreviouslyEncoded = password != null && password.startsWith(Activator.OBFUSCATED_PASSWORD_PREFIX);
		password = properties.getProperty(Activator.PROPERTIES_CEE_RESYNC_PASSWORD);
		
		properties.setProperty(Activator.PROPERTIES_CEE_URL, urlText.getText().trim());
		properties.setProperty(Activator.PROPERTIES_CEE_USER, userText.getText().trim());
		properties.setProperty(Activator.PROPERTIES_CEE_DISPLAY_NAME, displayNameText.getText().trim());
		properties.setProperty(Activator.PROPERTIES_CEE_PASSWORD, Activator.encodePassword(passwordText.getText().trim(), passwordPreviouslyEncoded));		
		properties.setProperty(Activator.PROPERTIES_CEE_ATTACHMENT_SIZE, attachmentSizeText.getText().trim());		
	}
	
	private void setQcProperties() throws Exception {
		String password = properties.getProperty(Activator.PROPERTIES_QC_PASSWORD);
		boolean passwordPreviouslyEncoded = password != null && password.startsWith(Activator.OBFUSCATED_PASSWORD_PREFIX);
		password = properties.getProperty(Activator.PROPERTIES_QC_RESYNC_PASSWORD);
		
		properties.setProperty(Activator.PROPERTIES_QC_URL, urlText.getText().trim());
		properties.setProperty(Activator.PROPERTIES_QC_USER, userText.getText().trim());
		properties.setProperty(Activator.PROPERTIES_QC_PASSWORD, Activator.encodePassword(passwordText.getText().trim(), passwordPreviouslyEncoded));		
		properties.setProperty(Activator.PROPERTIES_QC_ATTACHMENT_SIZE, attachmentSizeText.getText().trim());
	}
	
	@Override
	public void setSystemNumber(int systemNumber) {
		this.systemNumber = systemNumber;
	}
	
	private void setTeamForgeProperties() throws Exception {
		String password = properties.getProperty(Activator.PROPERTIES_SFEE_PASSWORD);
		boolean passwordPreviouslyEncoded = password != null && password.startsWith(Activator.OBFUSCATED_PASSWORD_PREFIX);
		password = properties.getProperty(Activator.PROPERTIES_SFEE_RESYNC_PASSWORD);

		properties.setProperty(Activator.PROPERTIES_SFEE_URL, urlText.getText().trim());
		properties.setProperty(Activator.PROPERTIES_SFEE_USER, userText.getText().trim());
		properties.setProperty(Activator.PROPERTIES_SFEE_PASSWORD, Activator.encodePassword(passwordText.getText().trim(), passwordPreviouslyEncoded));		
		properties.setProperty(Activator.PROPERTIES_SFEE_ATTACHMENT_SIZE, attachmentSizeText.getText().trim());
	}
	
	private void setSwProperties() throws Exception {
		String password = properties.getProperty(Activator.PROPERTIES_SW_PASSWORD);
		boolean passwordPreviouslyEncoded = password != null && password.startsWith(Activator.OBFUSCATED_PASSWORD_PREFIX);
		password = properties.getProperty(Activator.PROPERTIES_SW_RESYNC_PASSWORD);
		boolean resyncPasswordPreviouslyEncoded = password != null && password.startsWith(Activator.OBFUSCATED_PASSWORD_PREFIX);
		
		properties.setProperty(Activator.PROPERTIES_SW_URL, urlText.getText().trim());
		properties.setProperty(Activator.PROPERTIES_SW_USER, userText.getText().trim());
		properties.setProperty(Activator.PROPERTIES_SW_PASSWORD, Activator.encodePassword(passwordText.getText().trim(), passwordPreviouslyEncoded));		
		properties.setProperty(Activator.PROPERTIES_SW_RESYNC_USER, resyncUserText.getText().trim());
		properties.setProperty(Activator.PROPERTIES_SW_RESYNC_PASSWORD, Activator.encodePassword(resyncPasswordText.getText().trim(), resyncPasswordPreviouslyEncoded));
		properties.setProperty(Activator.PROPERTIES_SW_ATTACHMENT_SIZE, attachmentSizeText.getText().trim());
	}

}
