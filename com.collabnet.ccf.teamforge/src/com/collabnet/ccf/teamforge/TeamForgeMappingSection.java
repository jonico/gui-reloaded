package com.collabnet.ccf.teamforge;

import java.util.Properties;

import org.eclipse.jface.dialogs.IDialogSettings;
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
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.teamforge.dialogs.TeamForgeSelectionDialog;
import com.collabnet.ccf.teamforge.schemageneration.TFSoapClient;

public class TeamForgeMappingSection extends MappingSection {
	private Label teamForgeLabel;
	protected Text teamForgeText;
	private Button teamForgeBrowseButton;
	private Button trackerButton;
	private Button planningFoldersButton;
	private Button metaDataButton;
	
	private IDialogSettings settings = Activator.getDefault().getDialogSettings();
	
	private static final int TYPE_TRACKER = 0;
	private static final int TYPE_PLANNING_FOLDERS = 1;
	private static final int TYPE_METADATA = 2;
	private static final String PREVIOUS_TYPE = "TeamForgeMappingSection.type";

	public void updateSourceFields(SynchronizationStatus projectMapping) {
		projectMapping.setSourceRepositoryId(getRepositoryId());
		projectMapping.setSourceRepositoryKind("TRACKER");
	}
	
	public void updateTargetFields(SynchronizationStatus projectMapping) {
		projectMapping.setTargetRepositoryId(getRepositoryId());
		projectMapping.setTargetRepositoryKind("TRACKER");
	}
	
	private String getRepositoryId() {
		String repositoryId;
		if (metaDataButton.getSelection()) {
			repositoryId = teamForgeText.getText().trim() + "-" + ProjectMappings.MAPPING_TYPE_METADATA;
		} else if (planningFoldersButton.getSelection()) {
			repositoryId = teamForgeText.getText().trim() + "-" + ProjectMappings.MAPPING_TYPE_PLANNING_FOLDERS;
		} else {
			repositoryId = teamForgeText.getText().trim();
		}
		return repositoryId;
	}

	public Composite getComposite(Composite parent, final Landscape landscape) {
		Group tfGroup = new Group(parent, SWT.NULL);
		GridLayout tfLayout = new GridLayout();
		tfLayout.numColumns = 3;
		tfGroup.setLayout(tfLayout);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		tfGroup.setLayoutData(gd);	
		if (landscape.getType1().equals("TF") && landscape.getType2().equals("TF")) {
			if (Landscape.ROLE_ADMINISTRATOR == landscape.getRole()) {
				String url;
				if (1 == getSystemNumber()) {
					url = landscape.getProperties1().getProperty(TeamForgeCcfParticipant.PROPERTIES_SFEE_URL);
				} else {
					url = landscape.getProperties2().getProperty(TeamForgeCcfParticipant.PROPERTIES_SFEE_URL);
				}
				tfGroup.setText("TeamForge (" + url + "):");
			} else {
				tfGroup.setText("TeamForge " + getSystemNumber());
			}
		} else {
			tfGroup.setText("TeamForge:");
		}
		
		trackerButton = new Button(tfGroup, SWT.RADIO);
		trackerButton.setText("Tracker");
		gd = new GridData();
		gd.horizontalSpan = 3;
		trackerButton.setLayoutData(gd);
		planningFoldersButton = new Button(tfGroup, SWT.RADIO);
		planningFoldersButton.setText("Planning folders (requires TeamForge 5.3 or later)");
		gd = new GridData();
		gd.horizontalSpan = 3;
		planningFoldersButton.setLayoutData(gd);
		metaDataButton = new Button(tfGroup, SWT.RADIO);
		metaDataButton.setText("MetaData");
		gd = new GridData();
		gd.horizontalSpan = 3;
		metaDataButton.setLayoutData(gd);
		int tfType;
		try {
			tfType = settings.getInt(PREVIOUS_TYPE);
		} catch (Exception e) { tfType = TYPE_TRACKER; }
		switch (tfType) {
		case TYPE_PLANNING_FOLDERS:
			planningFoldersButton.setSelection(true);
			break;
		case TYPE_METADATA:
			metaDataButton.setSelection(true);
			break;			
		default:
			trackerButton.setSelection(true);
			break;
		}
		
		teamForgeLabel = new Label(tfGroup, SWT.NONE);
		if (planningFoldersButton.getSelection()) {
			teamForgeLabel.setText("Project ID: ");
		} else {
			teamForgeLabel.setText("Tracker ID:");
		}	
		teamForgeText = new Text(tfGroup, SWT.BORDER);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		teamForgeText.setLayoutData(gd);
		teamForgeBrowseButton = new Button(tfGroup, SWT.PUSH);
		teamForgeBrowseButton.setText("Browse...");
		teamForgeBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				int type;
				if (planningFoldersButton.getSelection()) type = TeamForgeSelectionDialog.BROWSER_TYPE_PROJECT;
				else type = TeamForgeSelectionDialog.BROWSER_TYPE_TRACKER;

				TeamForgeSelectionDialog dialog = new TeamForgeSelectionDialog(Display.getDefault().getActiveShell(), landscape, type, getSystemNumber());
				if (TeamForgeSelectionDialog.OK == dialog.open()) {
					teamForgeText.setText(dialog.getSelectedId());
				}
			}			
		});
		if (Landscape.ROLE_OPERATOR == landscape.getRole()) {
			teamForgeBrowseButton.setVisible(false);
		}
		ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent me) {
				if (null != getProjectPage()) {
					getProjectPage().setPageComplete();
				}
			}			
		};
		teamForgeText.addModifyListener(modifyListener);	
		
		SelectionListener typeListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				setPlanningFoldersSelected(planningFoldersButton.getSelection());
				if (null != getProjectPage()) {
					getProjectPage().setPageComplete();
				}
				teamForgeText.setFocus();
				teamForgeText.selectAll();
			}			
		};

		trackerButton.addSelectionListener(typeListener);
		planningFoldersButton.addSelectionListener(typeListener);	
		metaDataButton.addSelectionListener(typeListener);
		
		return tfGroup;
	}

	public void initializeComposite(SynchronizationStatus projectMapping, int type) {
		teamForgeText.setText(getTrackerId(projectMapping, type));
		if (teamForgeText.getText().startsWith("proj")) {
			planningFoldersButton.setSelection(true);
			trackerButton.setSelection(false);
			metaDataButton.setSelection(false);
			teamForgeLabel.setText("Project ID: ");
		} else {
			planningFoldersButton.setSelection(false);
			teamForgeLabel.setText("Tracker ID: ");
			if (projectMapping.getSourceRepositoryId().endsWith("-MetaData") || projectMapping.getTargetRepositoryId().endsWith("-MetaData")) {
				trackerButton.setSelection(false);
				metaDataButton.setSelection(true);				
			} else {
				trackerButton.setSelection(true);
				metaDataButton.setSelection(false);
			}
		}
		metaDataButton.setVisible(type == TYPE_TARGET);
	}

	public boolean isPageComplete() {
		if (null == teamForgeText) {
			return false;
		}
		if (planningFoldersButton.getSelection()) {
			if (!teamForgeText.getText().trim().startsWith("proj")) return false;
			if (5 > teamForgeText.getText().trim().length()) return false;
		} else {
			if (!teamForgeText.getText().trim().startsWith("tracker")) return false;
			if (8 > teamForgeText.getText().trim().length()) return false;
		}
		return true;
	}

	public boolean validate(Landscape landscape) {
		if (Landscape.ROLE_ADMINISTRATOR == landscape.getRole()) {
			return true;
		}
		if (null != planningFoldersButton && planningFoldersButton.getSelection()) {
			Properties properties = null;
			switch (getSystemNumber()) {
			case 1:
				properties = landscape.getProperties1();
				break;
			case 2:
				properties = landscape.getProperties2();
				break;
			default:
				break;
			}
			if (null != properties) {
				String serverUrl = properties.getProperty(Activator.PROPERTIES_SFEE_URL);
				String userId = properties.getProperty(Activator.PROPERTIES_SFEE_USER);
				String password = Activator.decodePassword(properties.getProperty(Activator.PROPERTIES_SFEE_PASSWORD));
				TFSoapClient soapClient = TFSoapClient.getSoapClient(serverUrl, userId, password);
				if (!soapClient.supports53()) {
					showValidationErrorDialog("Server does not support planning folders.");
					return false;
				}
			}
	 	}		
		return true;
	}
	
	private void setPlanningFoldersSelected(boolean planningFoldersSelected) {
		if (null != teamForgeLabel) {
			if (planningFoldersSelected) {
				teamForgeLabel.setText("Project ID: ");
			} else {
				teamForgeLabel.setText("Tracker ID:");
			}
		}
		if (planningFoldersSelected) {
			settings.put(PREVIOUS_TYPE, TYPE_PLANNING_FOLDERS);
		} else {
			settings.put(PREVIOUS_TYPE, TYPE_TRACKER);
		}	
	}
	
	private String getTrackerId(SynchronizationStatus projectMapping, int type) {
		String trackerId;
		if (IMappingSection.TYPE_SOURCE == type) {
			trackerId = projectMapping.getSourceRepositoryId();
		} else {
			trackerId = projectMapping.getTargetRepositoryId();
		}
		int index = trackerId.indexOf("-");
		if (-1 != index) {
			trackerId = trackerId.substring(0, index);
		}
		return trackerId;
	}

}
