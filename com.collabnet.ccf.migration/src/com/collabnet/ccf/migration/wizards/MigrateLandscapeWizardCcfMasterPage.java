/*******************************************************************************
 * Copyright (c) 2011 CollabNet.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     CollabNet - initial API and implementation
 ******************************************************************************/
package com.collabnet.ccf.migration.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.collabnet.ccf.migration.Activator;

public class MigrateLandscapeWizardCcfMasterPage extends WizardPage {
	private Combo ccfMasterUrlCombo;
	private Text ccfMasterUserText;
	private Text ccfMasterPasswordText;
	
	private String url;
	private String user;
	private String password;
	
	private IDialogSettings settings = Activator.getDefault().getDialogSettings();

	public MigrateLandscapeWizardCcfMasterPage() {
		super("ccfMasterPage", "CCF Master", null);
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		setMessage("Select target CCF Master for migration.");		
		Composite outerContainer = new Composite(parent,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		outerContainer.setLayout(layout);
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		Label ccfMasterUrlLabel = new Label(outerContainer, SWT.NONE);
		ccfMasterUrlLabel.setText("CCF Master URL:");
		
		ccfMasterUrlCombo = new Combo(outerContainer, SWT.BORDER);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		gd.widthHint = 300;
		ccfMasterUrlCombo.setLayoutData(gd);
		
	    for (int i = 0; i < 5; i++) {
	        String url = settings.get("CCFMaster.url." + i); //$NON-NLS-1$ //$NON-NLS-2$
	        if (null == url)
	          break;
	        ccfMasterUrlCombo.add(url);
	    }
	    
	    ccfMasterUrlCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String user = settings.get("CCFMaster.user." + ccfMasterUrlCombo.getText());
				if (null != user) {
					ccfMasterUserText.setText(user);
					ccfMasterPasswordText.setFocus();
					ccfMasterPasswordText.selectAll();
				}
			}
		});
		
		Label ccfMasterUserLabel = new Label(outerContainer, SWT.NONE);
		ccfMasterUserLabel.setText("User name:");
		
		ccfMasterUserText = new Text(outerContainer, SWT.BORDER);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		gd.widthHint = 300;
		ccfMasterUserText.setLayoutData(gd);
		
		Label ccfMasterPasswordLabel = new Label(outerContainer, SWT.NONE);
		ccfMasterPasswordLabel.setText("Password:");
		
		ccfMasterPasswordText = new Text(outerContainer, SWT.BORDER);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		gd.widthHint = 300;
		ccfMasterPasswordText.setLayoutData(gd);
		ccfMasterPasswordText.setEchoChar('*');
		
		ModifyListener modifyListener = new ModifyListener() {			
			public void modifyText(ModifyEvent e) {
				if (e.getSource() == ccfMasterUrlCombo) {
					url = ccfMasterUrlCombo.getText().trim();
				}
				else if (e.getSource() == ccfMasterUserText) {
					user = ccfMasterUserText.getText().trim();
				}
				else if (e.getSource() == ccfMasterPasswordText) {
					password = ccfMasterPasswordText.getText().trim();
				}
				setPageComplete(canFinish());
			}
		};
		
		ccfMasterUrlCombo.addModifyListener(modifyListener);
		ccfMasterUserText.addModifyListener(modifyListener);
		ccfMasterPasswordText.addModifyListener(modifyListener);

		setControl(outerContainer);
	}
	
	public String getCcfMasterUrl() {
		if (!url.endsWith("/")) {
			url = url + "/";
		}
		return url;
	}
	
	public String getCcfMasterUser() {
		return user;
	}
	
	public String getCcfMasterPassword() {
		return password;
	}
	
	private boolean canFinish() {
		String url = ccfMasterUrlCombo.getText().trim();
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			return false;
		}
		if (url.startsWith("http://") && 8 > url.length()) {
			return false;
		}
		else if (url.startsWith("https://") && 9 > url.length()) {
			return false;
		}
		if (0 == ccfMasterUserText.getText().trim().length() || 0 == ccfMasterPasswordText.getText().trim().length()) {
			return false;
		}
		return true;
	}

}
