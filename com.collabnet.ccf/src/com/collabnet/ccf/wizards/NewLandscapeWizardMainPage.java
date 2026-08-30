package com.collabnet.ccf.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.ICcfParticipant;
import com.collabnet.ccf.dialogs.GroupSelectionDialog;
import com.collabnet.ccf.model.Database;
import com.collabnet.ccf.model.Landscape;

public class NewLandscapeWizardMainPage extends WizardPage {
	private Composite outerContainer;
	private Text descriptionText;
	private Button administratorButton;
	private Button operatorButton;
	private Label groupLabel;
	private Text groupText;
	private Button groupBrowseButton;
	private Combo participant1Combo;
	private Combo participant2Combo;
	private Group descriptionGroup;
	private Composite imageGroup;
	private Label descriptionImage1;
	private Label arrowImage;
	private Label descriptionImage2;
	private Label descriptionLabel;

	private ICcfParticipant selectedParticipant1;
	private ICcfParticipant selectedParticipant2;
	
	private ICcfParticipant[] ccfParticipants;
	private IDialogSettings settings;
	
	private NewLandscapeWizardPropertiesFolderPage propertiesPage;

	private static final String LAST_ROLE = "NewLandscapeWizardMainPage.lastRole"; //$NON-NLS-1$
	private static final String LAST_CCF_PARTICIPANT_1 = "NewLandscapeWizardMainPage.lastCcfParticipant1"; //$NON-NLS-1$
	private static final String LAST_CCF_PARTICIPANT_2 = "NewLandscapeWizardMainPage.lastCcfParticipant2"; //$NON-NLS-1$

	public NewLandscapeWizardMainPage(String pageName, String title, ImageDescriptor titleImage, ICcfParticipant[] ccfParticipants) {
		super(pageName, title, titleImage);
		this.ccfParticipants = ccfParticipants;
		settings = Activator.getDefault().getDialogSettings();
	}

	public void createControl(Composite parent) {
		outerContainer = new Composite(parent,SWT.NONE);
		outerContainer.setLayout(new GridLayout());
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		Group descriptionGroup = new Group(outerContainer, SWT.NONE);
		descriptionGroup.setText("Description:");
		GridLayout descriptionLayout = new GridLayout();
		descriptionLayout.numColumns = 1;
		descriptionGroup.setLayout(descriptionLayout);
		descriptionGroup.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		descriptionText = new Text(descriptionGroup, SWT.BORDER);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		descriptionText.setLayoutData(gd);
		
		descriptionText.addModifyListener(new ModifyListener() {			
			public void modifyText(ModifyEvent e) {
				setPageComplete(canFinish());
			}
		});
		
		Group roleGroup = new Group(outerContainer, SWT.NONE);
		roleGroup.setText("Role:");
		GridLayout roleLayout = new GridLayout();
		roleLayout.numColumns = 4;
		roleGroup.setLayout(roleLayout);
		roleGroup.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		administratorButton = new Button(roleGroup, SWT.RADIO);
		administratorButton.setText("Administrator");
		gd = new GridData();
		gd.horizontalSpan = 4;
		administratorButton.setLayoutData(gd);
		operatorButton = new Button(roleGroup, SWT.RADIO);
		operatorButton.setText("Operator");
		groupLabel = new Label(roleGroup, SWT.NONE);
		groupLabel.setText("Group:");
		groupText = new Text(roleGroup, SWT.BORDER);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
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
		groupBrowseButton = new Button(roleGroup, SWT.PUSH);
		groupBrowseButton.setText("Browse...");
		groupBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				NewLandscapeWizard wizard = (NewLandscapeWizard)getWizard();
				Database database = new Database();
				database.setDriver(wizard.getDatabaseDriver());
				database.setPassword(wizard.getDatabasePassword());
				database.setUrl(wizard.getDatabaseUrl());
				database.setPassword(wizard.getDatabasePassword());			
				GroupSelectionDialog dialog = new GroupSelectionDialog(getShell(), database);
				if (GroupSelectionDialog.OK == dialog.open()) {
					groupText.setText(dialog.getSelectedGroup());
				}
			}			
		});
		
		int lastRole = Landscape.ROLE_ADMINISTRATOR;
		try {
			lastRole = settings.getInt(LAST_ROLE);
		} catch (Exception e) {}
		if (Landscape.ROLE_OPERATOR == lastRole) operatorButton.setSelection(true);
		else administratorButton.setSelection(true);
		
		if (!operatorButton.getSelection()) {
			groupLabel.setEnabled(false);
			groupText.setEnabled(false);
			groupBrowseButton.setEnabled(false);
		}
		
		SelectionListener roleListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				if (operatorButton.getSelection()) settings.put(LAST_ROLE, Landscape.ROLE_OPERATOR);
				else settings.put(LAST_ROLE, Landscape.ROLE_ADMINISTRATOR);
				groupLabel.setEnabled(operatorButton.getSelection());
				groupText.setEnabled(operatorButton.getSelection());
				groupBrowseButton.setEnabled(operatorButton.getSelection());
				if (se.getSource() == operatorButton) {
					groupText.setFocus();
					groupText.selectAll();
				}
			}		
		};
		administratorButton.addSelectionListener(roleListener);
		operatorButton.addSelectionListener(roleListener);
		
		Group landscapeGroup = new Group(outerContainer, SWT.NONE);
		landscapeGroup.setText("Synchronization participants:");
		GridLayout landscapeLayout = new GridLayout();
		landscapeLayout.numColumns = 1;
		landscapeGroup.setLayout(landscapeLayout);
		landscapeGroup.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

		participant1Combo = new Combo(landscapeGroup, SWT.READ_ONLY);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		participant1Combo.setLayoutData(gd);
		participant2Combo = new Combo(landscapeGroup, SWT.READ_ONLY);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		participant2Combo.setLayoutData(gd);
		
		getCcfParticipants();
		
		createDescriptionArea();
		
		setPageComplete(canFinish());
		
		setMessage("Specify the type and description of landscape");

		setControl(outerContainer);
	}
	
	private void getCcfParticipants() {
		String lastParticipant1Selection = settings.get(LAST_CCF_PARTICIPANT_1);
		String lastParticipant2Selection = settings.get(LAST_CCF_PARTICIPANT_2);
		if (null != lastParticipant2Selection && lastParticipant2Selection.equals("ScrumWorksPro")) {
			lastParticipant2Selection = "ScrumWorks Pro";
		}
		for (ICcfParticipant ccfParticipant : ccfParticipants) {
			participant1Combo.add(ccfParticipant.getName());
			participant2Combo.add(ccfParticipant.getName());
		}
		if (null != lastParticipant1Selection && -1 != participant1Combo.indexOf(lastParticipant1Selection)) {
			participant1Combo.setText(lastParticipant1Selection);
			selectedParticipant1 = ccfParticipants[participant1Combo.indexOf(lastParticipant1Selection)];
		} else {
			participant1Combo.setText(ccfParticipants[0].getName());
			selectedParticipant1 = ccfParticipants[0];
		}
		if (null != lastParticipant2Selection && -1 != participant2Combo.indexOf(lastParticipant2Selection)) {
			participant2Combo.setText(lastParticipant2Selection);
			selectedParticipant2 = ccfParticipants[participant2Combo.indexOf(lastParticipant2Selection)];
		} else {
			if (1 < ccfParticipants.length) {
				participant2Combo.setText(ccfParticipants[1].getName());
				selectedParticipant2 = ccfParticipants[1];
			} else {
				participant2Combo.setText(ccfParticipants[0].getName());
				selectedParticipant2 = ccfParticipants[0];				
			}
		}
		propertiesPage.setCcfParticipant1(selectedParticipant1);
		propertiesPage.setCcfParticipant2(selectedParticipant2);
		SelectionListener selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = -1;
				if (e.getSource() == participant1Combo) {
					index = participant1Combo.getSelectionIndex();
				}
				if (e.getSource() == participant2Combo) {
					index = participant2Combo.getSelectionIndex();
				}
				ICcfParticipant selectedParticipant = null;
				if (-1 != index) {
					selectedParticipant = ccfParticipants[index];
				}
				if (e.getSource() == participant1Combo) {
					selectedParticipant1 = selectedParticipant;
					settings.put(LAST_CCF_PARTICIPANT_1, selectedParticipant1.getName());
					propertiesPage.setCcfParticipant1(selectedParticipant1);
				}
				if (e.getSource() == participant2Combo) {
					selectedParticipant2 = selectedParticipant;
					settings.put(LAST_CCF_PARTICIPANT_2, selectedParticipant2.getName());
					propertiesPage.setCcfParticipant2(selectedParticipant2);
				}
				createDescriptionArea();
				setPageComplete(canFinish());
			}
		};
		participant1Combo.addSelectionListener(selectionListener);
		participant2Combo.addSelectionListener(selectionListener);
	}

	private void createDescriptionArea() {
		boolean needsRedraw = descriptionGroup != null;
		if (null == descriptionGroup) {
			descriptionGroup = new Group(outerContainer, SWT.NONE);
			GridLayout descriptionLayout = new GridLayout();
			descriptionLayout.numColumns = 1;
			descriptionGroup.setLayout(descriptionLayout);
			descriptionGroup.setLayoutData(
			new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL));			
		}
		descriptionGroup.setText(selectedParticipant1.getName() + "/" + selectedParticipant2.getName());
		if (null != descriptionLabel && !descriptionLabel.isDisposed()) descriptionLabel.dispose();
		if (null != descriptionImage1 && !descriptionImage1.isDisposed()) descriptionImage1.dispose();
		if (null != arrowImage && !arrowImage.isDisposed()) arrowImage.dispose();
		if (null != descriptionImage2 && !descriptionImage2.isDisposed()) descriptionImage2.dispose();

		if (null == imageGroup) {
			imageGroup = new Composite(descriptionGroup, SWT.NONE);
			GridLayout imageLayout = new GridLayout();
			imageLayout.numColumns = 3;
			imageGroup.setLayout(imageLayout);
			GridData data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
			imageGroup.setLayoutData(data);
		}
		
		if (null != selectedParticipant1.getImage()) {
			descriptionImage1 = new Label(imageGroup, SWT.NONE);
			descriptionImage1.setImage(selectedParticipant1.getImage());			
		}
		arrowImage = new Label(imageGroup, SWT.NONE);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		arrowImage.setLayoutData(data);
		arrowImage.setImage(Activator.getImage(Activator.IMAGE_ARROWS));	
		if (null != selectedParticipant2.getImage()) {
			descriptionImage2 = new Label(imageGroup, SWT.NONE);
			descriptionImage2.setImage(selectedParticipant2.getImage());			
		}
		
		descriptionLabel = new Label(descriptionGroup, SWT.WRAP);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
		data.widthHint = 500;
		data.horizontalSpan = 3;
		descriptionLabel.setLayoutData(data);
		StringBuffer description = new StringBuffer("Bidirectional " + selectedParticipant1.getName());
		if (selectedParticipant1 == selectedParticipant2) {
			description.append(" to ");
		} else {
			description.append(" and ");
		}
		description.append(selectedParticipant2.getName() + " synchronizations");
		descriptionLabel.setText(description.toString());
		if (needsRedraw) {
			descriptionGroup.layout(true);
			descriptionGroup.redraw();
		}
	}

	public String getDescription() {
		if (0 == descriptionText.getText().trim().length())
			return selectedParticipant1.getName() + "/" + selectedParticipant2.getName();
		else
			return descriptionText.getText().trim();
	}
	
	public int getRole() {
		if (operatorButton.getSelection()) return Landscape.ROLE_OPERATOR;
		else return Landscape.ROLE_ADMINISTRATOR;
	}
	
	public String getGroup() {
		if (administratorButton.getSelection() || 0 == groupText.getText().trim().length()) return null;
		else return groupText.getText().trim();
	}

	public void setPropertiesPage(
			NewLandscapeWizardPropertiesFolderPage propertiesPage) {
		this.propertiesPage = propertiesPage;
	}

	public ICcfParticipant getSelectedParticipant1() {
		return selectedParticipant1;
	}

	public ICcfParticipant getSelectedParticipant2() {
		return selectedParticipant2;
	}
	
	private boolean canFinish() {
		boolean uniqueDescription =  ((NewLandscapeWizard)getWizard()).isUnique(getDescription());
		if (uniqueDescription) {
			setErrorMessage(null);
		} else {
			setErrorMessage("Please enter a unique Landscape description.");
		}
		return uniqueDescription;
	}

}
