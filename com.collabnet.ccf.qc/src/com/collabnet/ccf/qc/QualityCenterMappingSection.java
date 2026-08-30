package com.collabnet.ccf.qc;

import java.util.Properties;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.IMappingSection;
import com.collabnet.ccf.MappingSection;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.qc.dialogs.DomainProjectSelectionDialog;
import com.collabnet.ccf.qc.dialogs.RequirementTypeSelectionDialog;
import com.collabnet.ccf.qc.schemageneration.QCLayoutExtractor;

public class QualityCenterMappingSection extends MappingSection {
	private Text domainText;
	private Text projectText;
	private Button projectBrowseButton;
	private Label requirementTypeLabel;
	private Text requirementTypeText;
	private Button requirementTypeBrowseButton;
	private Button defectsButton;
	private Button requirementsButton;
	private Label requirementsErrorLabel;
	private Label requirementsErrorMessageLabel;
	private boolean advancedProjectMappingOptionsEnabled;
	
	private IDialogSettings settings = Activator.getDefault().getDialogSettings();
	private static final String PREVIOUS_TYPE = "QualityCenterMappingSection.type";
	public static final String PREVIOUS_DOMAIN = "QualityCenterMappingSection.previousDomain.";
	public static final String PREVIOUS_DOMAIN_COUNT = "QualityCenterMappingSection.previousDomainCount";
	private static final int TYPE_DEFECTS = 0;
	private static final int TYPE_REQUIREMENTS = 1;
	
	public void updateSourceFields(SynchronizationStatus projectMapping) {
		projectMapping.setSourceRepositoryId(getRepositoryId());
		projectMapping.setSourceRepositoryKind("DEFECT");
	}
	
	public void updateTargetFields(SynchronizationStatus projectMapping) {
		projectMapping.setTargetRepositoryId(getRepositoryId());
		projectMapping.setTargetRepositoryKind("DEFECT");
	}
	
	private String getRepositoryId() {
		StringBuffer repositoryId = new StringBuffer(domainText.getText().trim() + "-" + projectText.getText().trim());
		if (requirementsButton.getSelection()) {
			repositoryId.append("-" + requirementTypeText.getText().trim());
		}
		return repositoryId.toString();
	}
	
	public Composite getComposite(Composite parent, final Landscape landscape) {		
		Group qcGroup = new Group(parent, SWT.NULL);
		GridLayout qcLayout = new GridLayout();
		qcLayout.numColumns = 3;
		qcGroup.setLayout(qcLayout);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		qcGroup.setLayoutData(gd);	
		
		if (landscape.getType1().equals("QC") && landscape.getType2().equals("QC")) {
			if (Landscape.ROLE_ADMINISTRATOR == landscape.getRole()) {
				String url;
				if (1 == getSystemNumber()) {
					url = landscape.getProperties1().getProperty(QualityCenterCcfParticipant.PROPERTIES_QC_URL);
				} else {
					url = landscape.getProperties2().getProperty(QualityCenterCcfParticipant.PROPERTIES_QC_URL);
				}
				qcGroup.setText("Quality Center (" + url + "):");
			} else {
				qcGroup.setText("Quality Center " + getSystemNumber());
			}
		} else {
			qcGroup.setText("Quality Center:");
		}		
		
		Label domainLabel = new Label(qcGroup, SWT.NONE);
		domainLabel.setText("Domain:");

		domainText = new Text(qcGroup, SWT.BORDER);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		domainText.setLayoutData(gd);	
		
		new Label(qcGroup, SWT.NONE);

		Label projectLabel = new Label(qcGroup, SWT.NONE);
		projectLabel.setText("Project:");
	
		projectText = new Text(qcGroup, SWT.BORDER);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		projectText.setLayoutData(gd);	
		
		projectBrowseButton = new Button(qcGroup, SWT.PUSH);
		projectBrowseButton.setText("Browse...");
		projectBrowseButton.setVisible("win32".equals(SWT.getPlatform()));
		projectBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				DomainProjectSelectionDialog dialog = new DomainProjectSelectionDialog(Display.getDefault().getActiveShell(), landscape, domainText.getText(), projectText.getText(), DomainProjectSelectionDialog.BROWSER_TYPE_PROJECT);
				if (DomainProjectSelectionDialog.OK == dialog.open()) {
					domainText.setText(dialog.getDomain());
					projectText.setText(dialog.getProject());
					if (null != getProjectPage()) {
						getProjectPage().setPageComplete();
					}
				}
			}			
		});

		defectsButton = new Button(qcGroup, SWT.RADIO);
		defectsButton.setText("Defects");
		gd = new GridData();
		gd.horizontalSpan = 3;
		defectsButton.setLayoutData(gd);
		requirementsButton = new Button(qcGroup, SWT.RADIO);
		requirementsButton.setText("Requirements");
		gd = new GridData();
		gd.horizontalSpan = 3;
		requirementsButton.setLayoutData(gd);
	
		int qcType;
		try {
			qcType = settings.getInt(PREVIOUS_TYPE);
		} catch (Exception e) { qcType = TYPE_DEFECTS; }
		switch (qcType) {
		case TYPE_REQUIREMENTS:
			requirementsButton.setSelection(true);
			break;
		default:
			defectsButton.setSelection(true);
			break;
		}		
	
		requirementTypeLabel = new Label(qcGroup, SWT.NONE);
		requirementTypeLabel.setText("Requirement type:");
		requirementTypeLabel.setVisible(requirementsButton.getSelection());
		requirementTypeText = new Text(qcGroup, SWT.BORDER);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		requirementTypeText.setLayoutData(gd);	
		requirementTypeText.setVisible(requirementsButton.getSelection());
		requirementTypeBrowseButton = new Button(qcGroup, SWT.PUSH);
		requirementTypeBrowseButton.setText("Browse...");
		requirementTypeBrowseButton.setVisible("win32".equals(SWT.getPlatform()) && requirementsButton.getSelection());
		requirementTypeBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if (!validate(landscape)) {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Select Requirement Type", "Invalid Quality Center Domain/Project entered.");
					return;
				}
				RequirementTypeSelectionDialog dialog = new RequirementTypeSelectionDialog(Display.getDefault().getActiveShell(), landscape, domainText.getText().trim(), projectText.getText().trim());
				if (RequirementTypeSelectionDialog.OK == dialog.open()) {
					requirementTypeText.setText(dialog.getType());
					if (null != getProjectPage()) {
						getProjectPage().setPageComplete();
					}
				}
			}			
		});
		
		Composite warningGroup = new Composite(qcGroup, SWT.NULL);
		GridLayout warningLayout = new GridLayout();
		warningLayout.numColumns = 2;
		warningGroup.setLayout(warningLayout);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 3;
		warningGroup.setLayoutData(gd);	
		
		requirementsErrorLabel = new Label(warningGroup, SWT.NONE);
		requirementsErrorLabel.setImage(Activator.getImage(Activator.IMAGE_ERROR));
		requirementsErrorLabel.setVisible(false);
		requirementsErrorMessageLabel = new Label(warningGroup, SWT.NONE);
		requirementsErrorMessageLabel.setText("CCF cannot map requirement types containing \"-\" character");
		requirementsErrorMessageLabel.setVisible(false);
		
		if (Landscape.ROLE_OPERATOR == landscape.getRole()) {
			projectBrowseButton.setEnabled(false);
			requirementTypeBrowseButton.setEnabled(false);
		}
	
		ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent me) {
				if (null != getProjectPage()) {
					getProjectPage().setPageComplete();
				}
			}			
		};
		
		projectText.addModifyListener(modifyListener);
		domainText.addModifyListener(modifyListener);
		requirementTypeText.addModifyListener(modifyListener);
		
		domainText.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				if (null != getProjectPage()) {
					getProjectPage().setPageComplete();
				}
			}			
		});
		
		SelectionListener typeListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				if (requirementsButton.getSelection()) {
					settings.put(PREVIOUS_TYPE, TYPE_REQUIREMENTS);
					requirementTypeText.setFocus();
				} else {
					settings.put(PREVIOUS_TYPE, TYPE_DEFECTS);
				}
				requirementTypeLabel.setVisible(requirementsButton.getSelection());
				requirementTypeText.setVisible(requirementsButton.getSelection());
				requirementTypeBrowseButton.setVisible("win32".equals(SWT.getPlatform()) && requirementsButton.getSelection() && advancedProjectMappingOptionsEnabled);
				if (null != getProjectPage()) {
					getProjectPage().setPageComplete();
				}
			}			
		};
		
		defectsButton.addSelectionListener(typeListener);
		requirementsButton.addSelectionListener(typeListener);	
		
		advancedProjectMappingOptionsEnabled = com.collabnet.ccf.qc.Activator.getDefault().getPreferenceStore().getBoolean(com.collabnet.ccf.qc.Activator.PREFERENCES_ADVANCED_PROJECT_MAPPING);
		if (!advancedProjectMappingOptionsEnabled) {
			projectBrowseButton.setVisible(false);
			requirementTypeBrowseButton.setVisible(false);
		}
		
		return qcGroup;
	}

	public void initializeComposite(SynchronizationStatus projectMapping, int type) {
		domainText.setText(getDomain(projectMapping, type));
		projectText.setText(getProject(projectMapping, type));
		String requirementType = getRequirementType(projectMapping, type);
		if (null != requirementType) {
			requirementTypeLabel.setVisible(true);
			requirementTypeText.setVisible(true);
			requirementTypeBrowseButton.setVisible(true);
			requirementTypeText.setText(requirementType);
			requirementsButton.setSelection(true);
			defectsButton.setSelection(false);
		} else {
			requirementTypeLabel.setVisible(false);
			requirementTypeText.setVisible(false);
			requirementTypeBrowseButton.setVisible(false);
			requirementsButton.setSelection(false);
			defectsButton.setSelection(true);
		}
	}

	public boolean isPageComplete() {
		if (requirementTypeText.isVisible() && requirementTypeText.getText().contains("-")) {
			requirementsErrorLabel.setVisible(true);
			requirementsErrorMessageLabel.setVisible(true);
		}
		else {
			requirementsErrorLabel.setVisible(false);
			requirementsErrorMessageLabel.setVisible(false);
		}
		if (null == domainText) {
			return false;
		}
		if (0 == domainText.getText().trim().length() ||
			0 == projectText.getText().trim().length()) {
			return false;
		}
		if (requirementTypeText.isVisible()) {
			if (0 == requirementTypeText.getText().trim().length()) {
				return false;
			}
			if (requirementTypeText.getText().contains("-")) {
				return false;
			}
		}
		return true;
	}

	public boolean validate(Landscape landscape) {
		// Only validate on 32-bit windows, because the COM driver doesn't work on 64-bit windows.
		if (Landscape.ROLE_OPERATOR == landscape.getRole() || !"win32".equals(SWT.getPlatform())) return true;
		
		// Only validate if "Enable advanced project mapping wizard options" preference is selected.
		if (!advancedProjectMappingOptionsEnabled) {
			return true;
		}
		
		QCLayoutExtractor qcLayoutExtractor = new QCLayoutExtractor();
		Properties properties;
		if (landscape.getType2().equals("QT")) {
			properties = landscape.getProperties2();
		} else {
			properties = landscape.getProperties1();
		}
		String url = properties.getProperty(Activator.PROPERTIES_QC_URL, "");
		String user = properties.getProperty(Activator.PROPERTIES_QC_USER, "");
		String password = Activator.decodePassword(properties.getProperty(
				Activator.PROPERTIES_QC_PASSWORD, ""));
		qcLayoutExtractor.setServerUrl(url);
		qcLayoutExtractor.setUserName(user);
		qcLayoutExtractor.setPassword(password);
		
		boolean validDomainAndProject;
		try {
			qcLayoutExtractor.validateQCDomainAndProject(domainText.getText().trim(), projectText.getText().trim());
			validDomainAndProject = true;
		} catch (Exception e) {
			validDomainAndProject = false;
		}
		if (!validDomainAndProject) {
			if (!showValidationQuestionDialog("Could not validate the supplied Quality Center Domain/Project.  Continue anyway?")) {
				return false;
			}
		}
		return true;
	}
	
	private String getDomain(SynchronizationStatus projectMapping, int type) {
		String repositoryId;
		if (IMappingSection.TYPE_SOURCE == type) {
			repositoryId = projectMapping.getSourceRepositoryId();
		} else {
			repositoryId = projectMapping.getTargetRepositoryId();
		}
		int index = repositoryId.indexOf("-");
		if (-1 == index) return "";
		else return repositoryId.substring(0, index);
	}
	
	private String getProject(SynchronizationStatus projectMapping, int type) {
		String repositoryId;
		if (IMappingSection.TYPE_SOURCE == type) {
			repositoryId = projectMapping.getSourceRepositoryId();
		} else {
			repositoryId = projectMapping.getTargetRepositoryId();
		}
		int index = repositoryId.indexOf("-");
		if (-1 == index) return "";
		else {
			String project = repositoryId = repositoryId.substring(index + 1);
			index = project.indexOf("-");
			if (-1 != index) {
				project = project.substring(0, index);
			}
			return project;
		}
	}
	
	private String getRequirementType(SynchronizationStatus projectMapping, int type) {
		String repositoryId;
		if (IMappingSection.TYPE_SOURCE == type) {
			repositoryId = projectMapping.getSourceRepositoryId();
		} else {
			repositoryId = projectMapping.getTargetRepositoryId();
		}
		int index = repositoryId.indexOf("-");
		if (-1 != index) {
			String project = repositoryId.substring(index + 1);
			index = project.indexOf("-");
			if (-1 != index) {
				return project.substring(index + 1);
			}
		}
		return null;
	}	

}
