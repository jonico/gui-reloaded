package com.collabnet.ccf.wizards;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.collabnet.ccf.ICcfParticipant;

public class NewLandscapeWizardPropertiesFolderPage extends WizardPage {
	private ICcfParticipant ccfParticipant1;
	private ICcfParticipant ccfParticipant2;

	private Group fileGroup1;
	private Group fileGroup2;
	private Text configFileText1;
	private Text configFileText2;
	private File parent1;
	private File parent2;

	public NewLandscapeWizardPropertiesFolderPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		Composite outerContainer = new Composite(parent,SWT.NONE);
		outerContainer.setLayout(new GridLayout());
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		fileGroup1 = new Group(outerContainer, SWT.NULL);
		GridLayout fileLayout1 = new GridLayout();
		fileLayout1.numColumns = 2;
		fileGroup1.setLayout(fileLayout1);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		fileGroup1.setLayoutData(data);
		
		if (null != ccfParticipant1 && null != ccfParticipant2) {
			fileGroup1.setText(getGroup1Text());
		}

		configFileText1 = new Text(fileGroup1, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		data.widthHint = 350;
		configFileText1.setLayoutData(data);
		configFileText1.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent me) {
				setPageComplete(canFinish());
			}			
		});

		Button browseButton1 = new Button(fileGroup1, SWT.NULL);
		browseButton1.setText("Browse...");
		
		browseButton1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog d = new FileDialog(getShell(), SWT.PRIMARY_MODAL | SWT.OPEN);				
				StringBuffer browseTitle = new StringBuffer("Select " + ccfParticipant1.getName());
				if (ccfParticipant1 == ccfParticipant2) {
					browseTitle.append("(1)");
				}
				browseTitle.append(" => " + ccfParticipant2.getName());
				if (ccfParticipant1 == ccfParticipant2) {
					browseTitle.append("(2)");
				}
				browseTitle.append(" config.xml file");
				d.setText(browseTitle.toString());
				
				d.setFileName("config.xml"); //$NON-NLS-1$
				String[] filterExtensions = { "*.xml" }; //$NON-NLS-1$
				d.setFilterExtensions(filterExtensions);
				String file = d.open();
				if(null!=file) {
					IPath path = new Path(file);
					configFileText1.setText(path.toOSString());
					if (null != configFileText2 && 0 == configFileText2.getText().trim().length()) {
						File parent = path.toFile().getParentFile();
						if (null != parent && parent.getName().equals("config")) {
							parent = parent.getParentFile();
							if (null != parent) {
								String folderName = parent.getName();
								int twoIndex = folderName.indexOf("2");
								if (-1 != twoIndex) {
									String reverseFolderName = folderName.substring(twoIndex + 1) + "2" + folderName.substring(0, twoIndex);
									configFileText2.setText(path.toOSString().replaceAll(folderName, reverseFolderName));
								}
							}
						}
					}
					setPageComplete(canFinish());
				}							
			}
		});
		
		fileGroup2 = new Group(outerContainer, SWT.NULL);
		GridLayout fileLayout2 = new GridLayout();
		fileLayout2.numColumns = 2;
		fileGroup2.setLayout(fileLayout2);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		fileGroup2.setLayoutData(data);
		if (null != ccfParticipant1 && null != ccfParticipant2) {
			fileGroup2.setText(getGroup2Text());
		}
		
		configFileText2 = new Text(fileGroup2, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		data.widthHint = 350;
		configFileText2.setLayoutData(data);
		configFileText2.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent me) {
				setPageComplete(canFinish());
			}			
		});

		Button browseButton2 = new Button(fileGroup2, SWT.NULL);
		browseButton2.setText("Browse...");
		
		browseButton2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog d = new FileDialog(getShell(), SWT.PRIMARY_MODAL | SWT.OPEN);				
				StringBuffer browseTitle = new StringBuffer("Select " + ccfParticipant2.getName());
				if (ccfParticipant1 == ccfParticipant2) {
					browseTitle.append("(2)");
				}
				browseTitle.append(" => " + ccfParticipant1.getName());
				if (ccfParticipant1 == ccfParticipant2) {
					browseTitle.append("(1)");
				}
				browseTitle.append(" config.xml file");
				d.setText(browseTitle.toString());
				
				d.setFileName("config.xml"); //$NON-NLS-1$
				String[] filterExtensions = { "*.xml" }; //$NON-NLS-1$
				d.setFilterExtensions(filterExtensions);
				String file = d.open();
				if(null!=file) {
					IPath path = new Path(file);
					configFileText2.setText(path.toOSString());
					if (null != configFileText1 && 0 == configFileText1.getText().trim().length()) {
						File parent = path.toFile().getParentFile();
						if (null != parent && parent.getName().equals("config")) {
							parent = parent.getParentFile();
							if (null != parent) {
								String folderName = parent.getName();
								int twoIndex = folderName.indexOf("2");
								if (-1 != twoIndex) {
									String reverseFolderName = folderName.substring(twoIndex + 1) + "2" + folderName.substring(0, twoIndex);
									configFileText1.setText(path.toOSString().replaceAll(folderName, reverseFolderName));
								}
							}
						}
					}
					setPageComplete(canFinish());
				}							
			}
		});

		setMessage("Select the config.xml files");

		setControl(outerContainer);
	}
	
	public String getConfigurationFolder1() {
		if (null != parent1) {
			return parent1.getAbsolutePath();
		}
		return null;
	}
	
	public String getConfigurationFolder2() {
		if (0 < configFileText2.getText().trim().length() && null != parent2) {
			return parent2.getAbsolutePath();
		}
		return null;
	}
	
	private boolean canFinish() {
		String propFileName1 = ccfParticipant1.getPropertiesFileName();
		String propFileName2 = ccfParticipant2.getPropertiesFileName();
		setErrorMessage(null);
		if (0 == configFileText1.getText().trim().length() && 0 == configFileText2.getText().trim().length()) {
			setErrorMessage("At least one config.xml file must be selected");
			return false;			
		}
		if (0 < configFileText1.getText().trim().length()) {
			if (!configFileText1.getText().endsWith("config.xml")) return false;
			File file = new File(configFileText1.getText());
			if (!file.exists()) {				
				StringBuffer errorText = new StringBuffer(ccfParticipant1.getName());
				if (ccfParticipant1 == ccfParticipant2) {
					errorText.append("(1)");
				}
				errorText.append(" => " + ccfParticipant2.getName());
				if (ccfParticipant1 == ccfParticipant2) {
					errorText.append("(2)");
				}
				errorText.append(" config.xml does not exist");
				
				setErrorMessage(errorText.toString());
				return false;
			}
			parent1 = file.getParentFile();
			File ccfFile = new File(parent1, "ccf.properties");
			if (!ccfFile.exists()) {
				setErrorMessage("File ccf.properties must exist in same folder as config.xml");
				return false;
			}
			File propFile1 = new File(parent1, propFileName1);
			if (!propFile1.exists()) {
				setErrorMessage("File " + propFileName1 + " must exist in same folder as config.xml");
				return false;
			}
			
			File propFile2 = new File(parent1, propFileName2);
			if (!propFile2.exists()) {
				setErrorMessage("File " + propFileName2 + " must exist in same folder as config.xml");
				return false;
			}
		}
		
		if (0 < configFileText2.getText().trim().length()) {
			if (!configFileText2.getText().endsWith("config.xml")) return false;
			File file = new File(configFileText2.getText());
			if (!file.exists()) {				
				StringBuffer errorText = new StringBuffer(ccfParticipant2.getName());
				if (ccfParticipant1 == ccfParticipant2) {
					errorText.append("(2)");
				}
				errorText.append(" => " + ccfParticipant1.getName());
				if (ccfParticipant1 == ccfParticipant2) {
					errorText.append("(1)");
				}
				errorText.append(" config.xml does not exist");
				
				setErrorMessage(errorText.toString());
				
				return false;
			}
			parent2 = file.getParentFile();
			File ccfFile = new File(parent2, "ccf.properties");
			if (!ccfFile.exists()) {
				setErrorMessage("File ccf.properties must exist in same folder as config.xml");
				return false;
			}
			File propFile1 = new File(parent2, propFileName2);
			if (!propFile1.exists()) {
				setErrorMessage("File " + propFileName2 + " must exist in same folder as config.xml");
				return false;
			}
			File propFile2 = new File(parent2, propFileName1);
			if (!propFile2.exists()) {
				setErrorMessage("File " + propFileName1 + " must exist in same folder as config.xml");
				return false;
			}
		}
		
		if (0 < configFileText1.getText().trim().length() && 0 < configFileText2.getText().trim().length() && configFileText1.getText().trim().equals(configFileText2.getText().trim())) {
			setErrorMessage("Same config.xml file cannot be used for both directions");
			return false;					
		}
		
		return true;
	}
	
	private String getGroup1Text() {
		StringBuffer groupLabel = new StringBuffer(ccfParticipant1.getName());
		if (ccfParticipant1 == ccfParticipant2) {
			groupLabel.append("(1)");
		}
		groupLabel.append(" => " + ccfParticipant2.getName());
		if (ccfParticipant1 == ccfParticipant2) {
			groupLabel.append("(2)");
		}
		groupLabel.append(" config.xml:");
		return groupLabel.toString();
	}
	
	public String getGroup2Text() {
		StringBuffer groupLabel = new StringBuffer(ccfParticipant2.getName());
		if (ccfParticipant1 == ccfParticipant2) {
			groupLabel.append("(2)");
		}
		groupLabel.append(" => " + ccfParticipant1.getName());
		if (ccfParticipant1 == ccfParticipant2) {
			groupLabel.append("(1)");
		}
		groupLabel.append(" config.xml:");
		return groupLabel.toString();
	}

	public void setCcfParticipant1(ICcfParticipant ccfParticipant1) {
		this.ccfParticipant1 = ccfParticipant1;
		if (null != fileGroup1) fileGroup1.setText(getGroup1Text());
		if (null != fileGroup2) fileGroup2.setText(getGroup2Text());
	}

	public void setCcfParticipant2(ICcfParticipant ccfParticipant2) {
		this.ccfParticipant2 = ccfParticipant2;
		if (null != fileGroup1) fileGroup1.setText(getGroup1Text());
		if (null != fileGroup2) fileGroup2.setText(getGroup2Text());
	}

}
