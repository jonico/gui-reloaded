package com.collabnet.ccf.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.collabnet.ccf.db.CcfDataProvider;

public class HospitalColumnSelectionDialog extends CcfDialog {
	private List columnList;
	private String column;
	
	private Button okButton;

	public HospitalColumnSelectionDialog(Shell shell) {
		super(shell, "HospitalColumnSelectionDialog");
	}
	
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Select Hospital Variable");
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		columnList = new List(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);													
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
		gd.heightHint = 100;
		columnList.setLayoutData(gd);
		
		columnList.add(CcfDataProvider.HOSPITAL_ID);
		columnList.add(CcfDataProvider.HOSPITAL_TIMESTAMP);
		columnList.add(CcfDataProvider.HOSPITAL_EXCEPTION_CLASS_NAME);
		columnList.add(CcfDataProvider.HOSPITAL_EXCEPTION_MESSAGE);
		columnList.add(CcfDataProvider.HOSPITAL_CAUSE_EXCEPTION_CLASS_NAME);
		columnList.add(CcfDataProvider.HOSPITAL_CAUSE_EXCEPTION_MESSAGE);
		columnList.add(CcfDataProvider.HOSPITAL_STACK_TRACE);
		columnList.add(CcfDataProvider.HOSPITAL_ADAPTOR_NAME);
		columnList.add(CcfDataProvider.HOSPITAL_ORIGINATING_COMPONENT);
		columnList.add(CcfDataProvider.HOSPITAL_DATA_TYPE);
		columnList.add(CcfDataProvider.HOSPITAL_EXCEPTION_CLASS_NAME);
		columnList.add(CcfDataProvider.HOSPITAL_EXCEPTION_MESSAGE);
		columnList.add(CcfDataProvider.HOSPITAL_FIXED);
		columnList.add(CcfDataProvider.HOSPITAL_REPROCESSED);
		columnList.add(CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_ID);
		columnList.add(CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_ID);
		columnList.add(CcfDataProvider.HOSPITAL_TARGET_SYSTEM_ID);
		columnList.add(CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_ID);
		columnList.add(CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_KIND);
		columnList.add(CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_KIND);
		columnList.add(CcfDataProvider.HOSPITAL_TARGET_SYSTEM_KIND);
		columnList.add(CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_KIND);		
		columnList.add(CcfDataProvider.HOSPITAL_SOURCE_ARTIFACT_ID);
		columnList.add(CcfDataProvider.HOSPITAL_TARGET_ARTIFACT_ID);
		columnList.add(CcfDataProvider.HOSPITAL_SOURCE_ARTIFACT_VERSION);
		columnList.add(CcfDataProvider.HOSPITAL_TARGET_ARTIFACT_VERSION);
		columnList.add(CcfDataProvider.HOSPITAL_ARTIFACT_TYPE);
		columnList.add(CcfDataProvider.HOSPITAL_GENERIC_ARTIFACT);
		
		MouseListener mouseListener = new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent me) {
				okPressed();
			}
		};
		
		columnList.addMouseListener(mouseListener);
		
		columnList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				okButton.setEnabled(columnList.getSelectionCount() > 0);
			}		
		});
		
		return composite;
	}
	
	public String getColumn() {
		return column;
	}
	
	public void okPressed() {
		column = "<" + columnList.getItem(columnList.getSelectionIndex()) + ">";
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

}
