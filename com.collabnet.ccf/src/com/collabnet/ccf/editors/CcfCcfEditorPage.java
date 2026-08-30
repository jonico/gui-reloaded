package com.collabnet.ccf.editors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.DriverManager;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
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
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.dialogs.GroupSelectionDialog;
import com.collabnet.ccf.dialogs.HospitalColumnSelectionDialog;
import com.collabnet.ccf.model.Database;
import com.collabnet.ccf.model.Landscape;

public class CcfCcfEditorPage extends CcfEditorPage {
	private ScrolledForm form;
	private FormToolkit toolkit;
	
	private Label errorImageLabel;
	private Label errorTextLabel;
	
	private Text descriptionText;
	private Text groupText;
	
	private Text urlText;
	private Text driverText;
	private Text userText;
	private Text passwordText;
	
	private Text host1Text;
	private Text jmxPort1Text;
	private Text logs1Text;
	private Text host2Text;
	private Text jmxPort2Text;
	private Text logs2Text;
	private Button logs1BrowseButton;
	private Button logs2BrowseButton;
	
	private Text templateText;
	
	private String description;
	private String group;
	private String url;
	private String driver;
	private String user;
	private String password;
	
	private String ccfHost1;
	private String jmxPort1;
	private String logs1;
	private String ccfHost2;
	private String jmxPort2;
	private String logs2;
	
	private String template;
	
	private Button insertButton;
	
	private boolean ccfPropertiesUpdated;
	
	public final static String DATABASE_SECTION_STATE = "CcfCcfEditorPage.databaseSectionExpanded";
	public final static String JMX_SECTION_STATE = "CcfCcfEditorPage.jmxSectionExpanded";
	public final static String TEMPLATE_SECTION_STATE = "CcfCcfEditorPage.templateSectionExpanded";

	public CcfCcfEditorPage(String id, String title) {
		super(id, title);
	}

	public CcfCcfEditorPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
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
	
	private void createControls(Composite composite) {
		description = getLandscape().getDescription();
		if (null == description) description = "";
		group = getLandscape().getGroup();
		if (null == group) group = "";
		url = getLandscape().getDatabaseUrl();
		if (null == url) url = "";
		driver = getLandscape().getDatabaseDriver();
		if (null == driver) driver = "";
		user = getLandscape().getDatabaseUser();
		if (null == user) user = "";
		password = getLandscape().getDatabasePassword();
		if (null == password) password = "";
		template = getLandscape().getLogMessageTemplate1();
		if (null == template) template = "";
		jmxPort1 = getLandscape().getJmxPort1();
		if (null == jmxPort1) jmxPort1 = "";
		jmxPort2 = getLandscape().getJmxPort2();
		if (null == jmxPort2) jmxPort2 = "";
		ccfHost1 = getLandscape().getCcfHost1();
		ccfHost2 = getLandscape().getCcfHost2();
		logs1 = getLandscape().getLogsPath1();
		if (null == logs1) logs1 = "";
		logs2 = getLandscape().getLogsPath2();
		if (null == logs2) logs1 = "";
		
		Label headerImageLabel = new Label(composite, SWT.NONE);
		headerImageLabel.setImage(Activator.getImage(getLandscape()));
		headerImageLabel.setBackground(((FormEditor)getEditor()).getToolkit().getColors().getBackground());
		
		Label headerLabel = new Label(composite, SWT.NONE);
		headerLabel.setText("CCF Properties");
		headerLabel.setBackground(((FormEditor)getEditor()).getToolkit().getColors().getBackground());
		headerLabel.setFont(JFaceResources.getHeaderFont());
        TableWrapData td = new TableWrapData(TableWrapData.FILL);
        headerLabel.setLayoutData(td);
        
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
		
	    Composite propertiesGroup = toolkit.createComposite(composite);
	    GridLayout propertiesLayout = new GridLayout();
	    propertiesLayout.numColumns = 3;
	    propertiesGroup.setLayout(propertiesLayout);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 4;
        propertiesGroup.setLayoutData(td);

        toolkit.createLabel(propertiesGroup, "Description:");
        descriptionText = toolkit.createText(propertiesGroup, getLandscape().getDescription());
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		descriptionText.setLayoutData(gd);
		
		if (Landscape.ROLE_OPERATOR == getLandscape().getRole()) {
			Label groupLabel = new Label(propertiesGroup, SWT.NONE);
			groupLabel.setText("Group:");
			groupText = toolkit.createText(propertiesGroup, group);
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			groupText.setLayoutData(gd);
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
			Button groupBrowseButton = new Button(propertiesGroup, SWT.PUSH);
			groupBrowseButton.setText("Browse:");
			groupBrowseButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent se) {
					Database database = getLandscape().getDatabase();	
					GroupSelectionDialog dialog = new GroupSelectionDialog(Display.getDefault().getActiveShell(), database);
					if (GroupSelectionDialog.OK == dialog.open()) {
						groupText.setText(dialog.getSelectedGroup());
					}
				}			
			});
		}
		
		Section databaseSection = toolkit.createSection(composite, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 4;
        databaseSection.setLayoutData(td);
        databaseSection.setText("Database");
        Composite databaseSectionClient = toolkit.createComposite(databaseSection); 
        GridLayout databaseLayout = new GridLayout();
        databaseLayout.numColumns = 2;
        databaseLayout.verticalSpacing = 10;
        databaseSectionClient.setLayout(databaseLayout);
        databaseSection.setClient(databaseSectionClient);
        databaseSection.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
                if (e.getState()) getDialogSettings().put(DATABASE_SECTION_STATE, STATE_EXPANDED);
                else getDialogSettings().put(DATABASE_SECTION_STATE, STATE_CONTRACTED);
            }
        });
        
        toolkit.createLabel(databaseSectionClient, "URL:");
        urlText = toolkit.createText(databaseSectionClient, getLandscape().getDatabaseUrl());
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		urlText.setLayoutData(gd);
		
        toolkit.createLabel(databaseSectionClient, "Driver:");
        driverText = toolkit.createText(databaseSectionClient, getLandscape().getDatabaseDriver());
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		driverText.setLayoutData(gd);
		
        toolkit.createLabel(databaseSectionClient, "User:");
        userText = toolkit.createText(databaseSectionClient, getLandscape().getDatabaseUser());
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		userText.setLayoutData(gd);
		
        toolkit.createLabel(databaseSectionClient, "Password:");
        passwordText = toolkit.createText(databaseSectionClient, getLandscape().getDatabasePassword());
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		passwordText.setLayoutData(gd);
		passwordText.setEchoChar('*');
		
		Button testButton = toolkit.createButton(databaseSectionClient, "Test Connection", SWT.PUSH);
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_CENTER;
		testButton.setLayoutData(gd);
		testButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				testConnection(false);
			}			
		});
		Composite hostSectionClient = null;
		Composite templateSectionClient = null;
		Section hostSection = null;
		Section templateSection = null;
		
		hostSection = toolkit.createSection(composite, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 4;
        hostSection.setLayoutData(td);
        hostSection.setText("CCF Hosts");
        hostSectionClient = toolkit.createComposite(hostSection); 
        GridLayout jmxLayout = new GridLayout();
        jmxLayout.numColumns = 2;
        jmxLayout.verticalSpacing = 10;
        hostSectionClient.setLayout(jmxLayout);
        hostSection.setClient(hostSectionClient);
        hostSection.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
                if (e.getState()) getDialogSettings().put(JMX_SECTION_STATE, STATE_EXPANDED);
                else getDialogSettings().put(JMX_SECTION_STATE, STATE_CONTRACTED);
            }
        });	
        
		toolkit.createLabel(hostSectionClient, getLandscape().getType1() + " => " + getLandscape().getType2() + " host name:");
		host1Text = toolkit.createText(hostSectionClient, ccfHost1);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		host1Text.setLayoutData(gd);
        
		toolkit.createLabel(hostSectionClient, getLandscape().getType1() + " => " + getLandscape().getType2() + " JMX port:");
		jmxPort1Text = toolkit.createText(hostSectionClient, jmxPort1);
		gd = new GridData();
		gd.widthHint = 100;
		jmxPort1Text.setLayoutData(gd);
		
		if (Landscape.ROLE_OPERATOR == getLandscape().getRole()) {		
			toolkit.createLabel(hostSectionClient, getLandscape().getType2() + " => " + getLandscape().getType1() + " logs folder:");
			String logsPath = getLandscape().getLogsPath1();
			if (null == logsPath) logsPath = "";			
		    Composite logsGroup1 = toolkit.createComposite(hostSectionClient);
		    GridLayout logsLayout1 = new GridLayout();
		    logsLayout1.numColumns = 2;
		    logsLayout1.marginWidth = 0;
		    logsLayout1.marginHeight = 0;
		    logsGroup1.setLayout(logsLayout1);
		    gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		    logsGroup1.setLayoutData(gd);
			logs1Text = toolkit.createText(logsGroup1, logsPath);
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			logs1Text.setLayoutData(gd);
			logs1BrowseButton = toolkit.createButton(logsGroup1, "Browse...", SWT.PUSH);
			logs1BrowseButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent se) {
					DirectoryDialog dialog = new DirectoryDialog(Display.getDefault().getActiveShell(), SWT.PRIMARY_MODAL | SWT.OPEN);
					dialog.setText(getLandscape().getType2() + " => " + getLandscape().getType1() + " Logs Folder");
					String folder = dialog.open();
					if (null != folder) {
						IPath path = new Path(folder);
						logs1Text.setText(path.toOSString());
					}
				}				
			});
		}
		
		toolkit.createLabel(hostSectionClient, getLandscape().getType2() + " => " + getLandscape().getType1() + " host name:");
		host2Text = toolkit.createText(hostSectionClient, ccfHost2);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		host2Text.setLayoutData(gd);
		
		toolkit.createLabel(hostSectionClient, getLandscape().getType2() + " => " + getLandscape().getType1() + " JMX port:");
		jmxPort2Text = toolkit.createText(hostSectionClient, jmxPort2);
		gd = new GridData();
		gd.widthHint = 100;
		jmxPort2Text.setLayoutData(gd);
		
		if (Landscape.ROLE_OPERATOR == getLandscape().getRole()) {
			toolkit.createLabel(hostSectionClient, getLandscape().getType1() + " => " + getLandscape().getType2() + " logs folder:");
			String logsPath = getLandscape().getLogsPath2();
			if (null == logsPath) logsPath = "";			
		    Composite logsGroup2 = toolkit.createComposite(hostSectionClient);
		    GridLayout logsLayout2 = new GridLayout();
		    logsLayout2.numColumns = 2;
		    logsLayout2.marginWidth = 0;
		    logsLayout2.marginHeight = 0;
		    logsGroup2.setLayout(logsLayout2);
		    gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		    logsGroup2.setLayoutData(gd);
			logs2Text = toolkit.createText(logsGroup2, logsPath);
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			logs2Text.setLayoutData(gd);
			logs2BrowseButton = toolkit.createButton(logsGroup2, "Browse...", SWT.PUSH);
			logs2BrowseButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent se) {
					DirectoryDialog dialog = new DirectoryDialog(Display.getDefault().getActiveShell(), SWT.PRIMARY_MODAL | SWT.OPEN);
					dialog.setText(getLandscape().getType1() + " => " + getLandscape().getType2() + " Logs Folder");
					String folder = dialog.open();
					if (null != folder) {
						IPath path = new Path(folder);
						logs2Text.setText(path.toOSString());
					}
				}				
			});		
		}
		
		if (Landscape.ROLE_ADMINISTRATOR == getLandscape().getRole()) {			
			templateSection = toolkit.createSection(composite, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
	        td = new TableWrapData(TableWrapData.FILL_GRAB);
	        td.colspan = 4;
	        templateSection.setLayoutData(td);
	        templateSection.setText("Log Message Template");
	        templateSectionClient = toolkit.createComposite(templateSection); 
	        GridLayout templateLayout = new GridLayout();
	        templateLayout.numColumns = 1;
	        templateLayout.verticalSpacing = 10;
	        templateSectionClient.setLayout(templateLayout);
	        templateSection.setClient(templateSectionClient);
	        templateSection.addExpansionListener(new ExpansionAdapter() {
	            public void expansionStateChanged(ExpansionEvent e) {
	                form.reflow(true);
	                if (e.getState()) getDialogSettings().put(TEMPLATE_SECTION_STATE, STATE_EXPANDED);
	                else getDialogSettings().put(TEMPLATE_SECTION_STATE, STATE_CONTRACTED);
	            }
	        });
	        
	        templateText = toolkit.createText(templateSectionClient, getLandscape().getLogMessageTemplate1(), SWT.BORDER | SWT.MULTI | SWT.WRAP);
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			gd.heightHint = 175;
			templateText.setLayoutData(gd);
			
			Composite buttonGroup = toolkit.createComposite(templateSectionClient);
			GridLayout buttonLayout = new GridLayout();
			buttonLayout.numColumns = 2;
			buttonGroup.setLayout(buttonLayout);
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			buttonGroup.setLayoutData(gd);	
			
			Button resetButton = toolkit.createButton(buttonGroup, "Restore Default Template", SWT.PUSH);
			gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
			resetButton.setLayoutData(gd);
			resetButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent se) {
					templateText.setText(Activator.DEFAULT_LOG_MESSAGE_TEMPLATE);
				}			
			});
			
			insertButton = toolkit.createButton(buttonGroup, "Insert Hospital Column...", SWT.PUSH);
			insertButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent se) {
					insertHospitalColumn();
				}			
			});
			insertButton.setEnabled(true);
		}
		
        toolkit.paintBordersFor(databaseSectionClient);
        if (null != hostSectionClient) toolkit.paintBordersFor(hostSectionClient);
        if (null != templateSectionClient) toolkit.paintBordersFor(templateSectionClient);
        
        String expansionState = getDialogSettings().get(DATABASE_SECTION_STATE);
        databaseSection.setExpanded(expansionState == null  || expansionState.equals(STATE_EXPANDED));
        
        if (null != hostSection) {
        	expansionState = getDialogSettings().get(JMX_SECTION_STATE);
        	hostSection.setExpanded(expansionState == null  || expansionState.equals(STATE_EXPANDED));
        }
        
        if (null != templateSection) {
        	expansionState = getDialogSettings().get(TEMPLATE_SECTION_STATE);
        	templateSection.setExpanded(expansionState == null  || expansionState.equals(STATE_EXPANDED));
        }
        
        ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent me) {
				errorImageLabel.setVisible(!canLeaveThePage());
				errorTextLabel.setVisible(!canLeaveThePage());
				((CcfEditor)getEditor()).setDirty();
				if (me.getSource() != descriptionText && me.getSource() != groupText) {
					ccfPropertiesUpdated = true;
				}
			}			
		};
		
		descriptionText.addModifyListener(modifyListener);
		if (null != groupText) {
			groupText.addModifyListener(modifyListener);
		}
		urlText.addModifyListener(modifyListener);
		driverText.addModifyListener(modifyListener);
		userText.addModifyListener(modifyListener);
		passwordText.addModifyListener(modifyListener);
		if (null != jmxPort1Text) jmxPort1Text.addModifyListener(modifyListener);
		if (null != host1Text) host1Text.addModifyListener(modifyListener);
		if (null != logs1Text) logs1Text.addModifyListener(modifyListener);
		if (null != jmxPort2Text) jmxPort2Text.addModifyListener(modifyListener);
		if (null != host2Text) host2Text.addModifyListener(modifyListener);
		if (null != logs2Text) logs2Text.addModifyListener(modifyListener);
		if (null != templateText) templateText.addModifyListener(modifyListener);
		
		FocusListener focusListener = new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				((Text)e.getSource()).selectAll();
			}
			public void focusLost(FocusEvent e) {
				((Text)e.getSource()).setText(((Text)e.getSource()).getText());
			}					
		};
		
		descriptionText.addFocusListener(focusListener);
		if (null != groupText) {
			groupText.addFocusListener(focusListener);
		}
		urlText.addFocusListener(focusListener);
		driverText.addFocusListener(focusListener);
		userText.addFocusListener(focusListener);
		passwordText.addFocusListener(focusListener);
		if (null != jmxPort1Text) jmxPort1Text.addFocusListener(focusListener);
		if (null != host1Text) host1Text.addFocusListener(focusListener);
		if (null != logs1Text) logs1Text.addFocusListener(focusListener);
		if (null != jmxPort2Text) jmxPort2Text.addFocusListener(focusListener);
		if (null != host2Text) host2Text.addFocusListener(focusListener);
		if (null != logs2Text) logs2Text.addFocusListener(focusListener);
		// if (templateText != null) templateText.addFocusListener(focusListener);
	}
	
	@Override
	public boolean canLeaveThePage() {
		if (0 == descriptionText.getText().trim().length() ||
		0 == urlText.getText().trim().length() ||
		0 == driverText.getText().trim().length()) return false;
		if (null != jmxPort1Text) {
			if (0 < jmxPort1Text.getText().trim().length()) {
				int port = 0;
				try {
					port = Integer.parseInt(jmxPort1Text.getText().trim());
				} catch (Exception e) {}
				if (0 >= port) return false;			
			}
		}
		if (null != jmxPort2Text) {
			if (0 < jmxPort2Text.getText().trim().length()) {
				int port = 0;
				try {
					port = Integer.parseInt(jmxPort2Text.getText().trim());
				} catch (Exception e) {}
				if (0 >= port) return false;			
			}		
		}
		return true;
	}

	private void insertHospitalColumn() {
		HospitalColumnSelectionDialog dialog = new HospitalColumnSelectionDialog(Display.getDefault().getActiveShell());
		if (HospitalColumnSelectionDialog.OK == dialog.open()) {
			String column = dialog.getColumn();
			if (null != column) {
				templateText.insert(column);
			}
		}
		templateText.setFocus();		
	}

	public boolean isDirty() {
		if (null == urlText) return false;
		return !descriptionText.getText().trim().equals(description) ||
		(groupText != null && !groupText.getText().trim().equals(group)) ||
		!urlText.getText().trim().equals(url) ||
		!driverText.getText().trim().equals(driver) ||
		!userText.getText().trim().equals(user) ||
		!passwordText.getText().trim().equals(password) ||
		(jmxPort1Text != null && !jmxPort1Text.getText().trim().equals(jmxPort1)) ||
		(host1Text != null && !host1Text.getText().trim().equals(ccfHost1)) ||
		(logs1Text != null && !logs1Text.getText().trim().equals(logs1)) ||
		(jmxPort2Text != null && !jmxPort2Text.getText().trim().equals(jmxPort2)) ||
		(host2Text != null && !host2Text.getText().trim().equals(ccfHost2)) ||
		(logs2Text != null && !logs2Text.getText().trim().equals(logs2)) ||
		(templateText != null && !templateText.getText().trim().equals(template));
	}
	
	@Override
	public void setSystemNumber(int systemNumber) {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (null == descriptionText) return;
		if (ccfPropertiesUpdated()) {
			saveCcfProperties();			
		}
		boolean descriptionChanged = !descriptionText.getText().trim().equals(getLandscape().getDescription());
		if (descriptionChanged || Landscape.ROLE_OPERATOR == getLandscape().getRole()) {
			if (null != groupText) {
				getLandscape().setGroup(groupText.getText().trim());
				if (0 < groupText.getText().trim().length() && !groupText.getText().trim().equals(group)) {
					CcfDataProvider dataProvider = new CcfDataProvider();
					try {
						if (!dataProvider.groupExists(groupText.getText().trim(), getLandscape().getDatabase())) {
							dataProvider.addGroup(groupText.getText().trim(), getLandscape().getDatabase());
						}
					} catch (Exception e) {
						Activator.handleError(e);
					}
				}
			}
			getLandscape().setDescription(descriptionText.getText().trim().replaceAll("/", "%slash%"));
			getLandscape().setDatabaseUrl(urlText.getText().trim());
			getLandscape().setDatabaseDriver(driverText.getText().trim());
			getLandscape().setDatabaseUser(userText.getText().trim());
			getLandscape().setDatabasePassword(passwordText.getText().trim());
			getLandscape().setCcfHost1(host1Text.getText().trim());
			getLandscape().setJmxPort1(jmxPort1Text.getText().trim());
			if (null != logs1Text) getLandscape().setLogsPath1(logs1Text.getText().trim());
			getLandscape().setCcfHost2(host2Text.getText().trim());
			getLandscape().setJmxPort2(jmxPort2Text.getText().trim());
			if (null != logs2Text) getLandscape().setLogsPath2(logs2Text.getText().trim());
			if (Activator.getDefault().storeLandscape(getLandscape())) {
				// Delete old node.
				if (descriptionChanged) Activator.getDefault().deleteLandscape(getLandscape());
			}
			Landscape landscape = Activator.getDefault().getLandscape(getLandscape().getDescription());
			if (null != landscape) setLandscape(landscape);
		}
		description = descriptionText.getText().trim();
		if (null != groupText) {
			group = groupText.getText().trim();
		}
		url = urlText.getText().trim();
		driver = driverText.getText().trim();
		user = userText.getText().trim();
		password = passwordText.getText().trim();
		if (null != jmxPort1Text) jmxPort1 = jmxPort1Text.getText().trim();
		if (null != host1Text) ccfHost1 = host1Text.getText().trim();
		if (null != logs1Text) logs1 = logs1Text.getText().trim();
		if (null != jmxPort2Text) jmxPort2 = jmxPort2Text.getText().trim();
		if (null != host2Text) ccfHost2 = host2Text.getText().trim();
		if (null != logs2Text) logs2 = logs2Text.getText().trim();
		if (null != templateText) template = templateText.getText().trim();
	}
	
	private void saveCcfProperties() {
		try {
			Properties properties = new Properties();
			File folder = new File(getLandscape().getConfigurationFolder());
			if (null != folder && 0 < folder.length()) {
				File propertiesFile = new File(folder, "ccf.properties");
				FileInputStream inputStream = new FileInputStream(propertiesFile);
				properties.load(inputStream);
				inputStream.close();
			}
			
			File propertiesFile1 = null;
			File propertiesFile2 = null;
			
			if (null != getLandscape().getConfigurationFolder1()) {
				folder = new File(getLandscape().getConfigurationFolder1());
				propertiesFile1 = new File(folder, "ccf.properties");
			}
			
			if (null != getLandscape().getConfigurationFolder2()) {
				folder = new File(getLandscape().getConfigurationFolder2());
				propertiesFile2 = new File(folder, "ccf.properties");
			}
			
			properties.setProperty(Activator.PROPERTIES_CCF_URL, getUrl());
			properties.setProperty(Activator.PROPERTIES_CCF_DRIVER, getDriver());
			properties.setProperty(Activator.PROPERTIES_CCF_USER, getUser());
			String previousPassword = properties.getProperty(Activator.PROPERTIES_CCF_PASSWORD);
			boolean passwordPreviouslyEncoded = previousPassword != null && previousPassword.startsWith(Activator.OBFUSCATED_PASSWORD_PREFIX);
			properties.setProperty(Activator.PROPERTIES_CCF_PASSWORD, Activator.encodePassword(getPassword(), passwordPreviouslyEncoded));
			if (Landscape.ROLE_ADMINISTRATOR == getLandscape().getRole()) {
				properties.setProperty(Activator.PROPERTIES_CCF_LOG_MESSAGE_TEMPLATE, getLogMessageTemplate());
				if (null != propertiesFile1) {
					properties.setProperty(Activator.PROPERTIES_CCF_JMX_PORT, getJmxPort1());
					properties.setProperty(Activator.PROPERTIES_CCF_HOST_NAME, getHostName1());
					FileOutputStream outputStream = new FileOutputStream(propertiesFile1);
					properties.store(outputStream, null);
					outputStream.close();
				}
				if (null != propertiesFile2) {
					properties.setProperty(Activator.PROPERTIES_CCF_JMX_PORT, getJmxPort2());
					properties.setProperty(Activator.PROPERTIES_CCF_HOST_NAME, getHostName2());
					FileOutputStream outputStream = new FileOutputStream(propertiesFile2);
					properties.store(outputStream, null);
					outputStream.close();
				}	
			}
		} catch (Exception e) {
			Activator.handleError(e);
		}
	}

	public String getDescription() {
		return descriptionText.getText().trim();
	}
	
	public String getGroup() {
		if (null == groupText) {
			return "";
		} else {
			return groupText.getText().trim();
		}
	}
	
	public String getUrl() {
		return urlText.getText().trim();
	}
	
	public String getDriver() {
		return driverText.getText().trim();
	}
	
	public String getUser() {
		return userText.getText().trim();
	}
	
	public String getPassword() {
		return passwordText.getText().trim();
	}
	
	public String getJmxPort1() {
		return jmxPort1Text.getText().trim();
	}
	
	public String getJmxPort2() {
		return jmxPort2Text.getText().trim();
	}
	
	public String getHostName1() {
		return host1Text.getText().trim();
	}
	
	public String getHostName2() {
		return host2Text.getText().trim();
	}
	
	public String getLogMessageTemplate() {
		return templateText.getText().trim();
	}
	
	public boolean ccfPropertiesUpdated() {
		return ccfPropertiesUpdated;
	}
	
	public boolean testConnection(boolean saving) {
		if (saving && null == urlText) {
			return true;
		}
		if (saving && getLandscape().getDatabaseUrl().equals(urlText.getText().trim()) &&
				getLandscape().getDatabaseDriver().equals(driverText.getText().trim()) &&
				getLandscape().getDatabaseUser().equals(userText.getText().trim()) &&
				getLandscape().getDatabasePassword().equals(passwordText.getText().trim())) {
			return true;
		}
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
