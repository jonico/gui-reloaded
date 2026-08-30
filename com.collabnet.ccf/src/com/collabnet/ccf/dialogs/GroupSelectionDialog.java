package com.collabnet.ccf.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.model.Database;

public class GroupSelectionDialog extends CcfDialog {
	private Database database;
	private CcfDataProvider dataProvider;
	
	private List groupList;
	private Text groupText;
	private Button okButton;
	
	private String[] groups;
	private String selectedGroup;

	public GroupSelectionDialog(Shell shell, Database database) {
		super(shell, "GroupSelectionDialog");
		this.database = database;
		dataProvider = new CcfDataProvider();
		getGroups();
	}
	
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Select Group");
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		if (0 == groups.length) {
			Label noGroupsLabel = new Label(composite, SWT.WRAP);
			noGroupsLabel.setText("No groups have yet been defined.  Please enter the name of a new group to assign to this Landscape.");
			GridData gd = new GridData();
			gd.widthHint = 400;
			gd.horizontalSpan = 2;
			noGroupsLabel.setLayoutData(gd);
			Label spacer = new Label(composite, SWT.NONE);
			gd = new GridData();
			gd.horizontalSpan = 2;
			spacer.setLayoutData(gd);
			Label groupLabel = new Label(composite, SWT.NONE);
			groupLabel.setText("Group:");
			groupText = new Text(composite, SWT.BORDER);
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
			gd.widthHint = 400;
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
			ModifyListener modifyListener = new ModifyListener() {
				public void modifyText(ModifyEvent me) {
					okButton.setEnabled(canFinish());
				}				
			};
			groupText.addModifyListener(modifyListener);
		} else {
			Group groupGroup = new Group(composite, SWT.NONE);
			GridLayout groupLayout = new GridLayout();
			groupLayout.numColumns = 1;
			groupGroup.setLayout(groupLayout);
			groupGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
			groupGroup.setText("Groups:");
			groupList = new List(groupGroup, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);													
			GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
			gd.heightHint = 100;
			groupList.setLayoutData(gd);
			for (String group : groups) {
				groupList.add(group);
			}
			groupList.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent se) {
					okButton.setEnabled(canFinish());
				}		
			});
			MouseListener mouseListener = new MouseAdapter() {
				public void mouseDoubleClick(MouseEvent me) {
					okPressed();
				}
			};
			groupList.addMouseListener(mouseListener);
		}
		
		return composite;
	}

	@Override
	protected void okPressed() {
		if (null != groupText) {
			selectedGroup = groupText.getText().trim();
			addGroup();
		}
		if (null != groupList) {
			String[] selection = groupList.getSelection();
			selectedGroup = selection[0];
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
	
	private boolean canFinish() {
		if (null != groupList && 0 < groupList.getSelectionCount()) {
			return true;
		}
		if (null != groupText && 0 < groupText.getText().trim().length()) {
			return true;
		}
		return false;
	}

	public String getSelectedGroup() {
		return selectedGroup;
	}
	
	private void getGroups() {
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				try {
					groups = dataProvider.getGroups(database);
				} catch (Exception e) {
					Activator.handleError(e);
				}
			}			
		});
	}
	
	private void addGroup() {
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				try {
					dataProvider.addGroup(groupText.getText().trim(), database);
				} catch (Exception e) {
					Activator.handleError(e);
				}
			}			
		});		
	}

}
