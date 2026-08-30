package com.collabnet.ccf.dialogs;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.collabnet.ccf.Activator;

public class ResetProjectMappingDialog extends CcfDialog {	
	private boolean needsPause;
	private boolean showDateSelection;
	private boolean showVersionSelection;
	
	private Button resetTo1999Button;
	private Button resetToCurrentButton;
	private Button resetToSelectedButton;
	private Label dateLabel;
	private DateTime date;
	private Label timeLabel;
	private DateTime time;
	
	private Text resetVersionText;
	
	private Timestamp resetDate;
	
	private String resetVersion;

	public ResetProjectMappingDialog(Shell shell, boolean needsPause, boolean showDateSelection, boolean showVersionSelection) {
		super(shell, "ResetProjectMappingDialog." + needsPause + showDateSelection + showVersionSelection);
		this.needsPause = needsPause;
		this.showDateSelection = showDateSelection;
		this.showVersionSelection = showVersionSelection;
	}
	
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Reset Synchronization Status");
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		if (needsPause) {
			int delay = Activator.getDefault().getPreferenceStore().getInt(Activator.PREFERENCES_RESET_DELAY);
			if (0 < delay) {
				Label pauseLabel = new Label(composite, SWT.WRAP);
				pauseLabel.setText("Synchronization will be paused for " + delay + " seconds before resetting, then resumed automatically.\n\n");
			}
		}
		
		if (!showDateSelection && !showVersionSelection) {
			Label confirmLabel = new Label(composite, SWT.NONE);
			confirmLabel.setText("Are you sure you want to reset synchronization status?");
		}
		
		if (showDateSelection) {	
			Group dateGroup = new Group(composite, SWT.NONE);
			dateGroup.setText("Last artifact modification date:");
			GridLayout groupLayout = new GridLayout();
			groupLayout.numColumns = 5;
			dateGroup.setLayout(groupLayout);
			dateGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			resetTo1999Button = new Button(dateGroup, SWT.RADIO);
			resetTo1999Button.setText("Reset to 1999");
			GridData gd = new GridData();
			gd.horizontalSpan = 5;
			resetTo1999Button.setLayoutData(gd);
			
			resetToCurrentButton = new Button(dateGroup, SWT.RADIO);
			resetToCurrentButton.setText("Reset to current date/time");
			gd = new GridData();
			gd.horizontalSpan = 5;
			resetToCurrentButton.setLayoutData(gd);
			
			resetToSelectedButton = new Button(dateGroup, SWT.RADIO);
			resetToSelectedButton.setText("Reset to selected date/time");
			
			dateLabel = new Label(dateGroup, SWT.NONE);
			dateLabel.setText("Date:");
			date = new DateTime(dateGroup, SWT.DATE | SWT.MEDIUM);
			timeLabel = new Label(dateGroup, SWT.NONE);
			timeLabel.setText("Time:");
			time = new DateTime(dateGroup, SWT.TIME | SWT.MEDIUM);
			
			resetTo1999Button.setSelection(true);
			
			setDateTimeEnablement();
			
			SelectionListener selectionListener = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent se) {
					setDateTimeEnablement();
				}			
			};
			
			resetTo1999Button.addSelectionListener(selectionListener);
			resetToCurrentButton.addSelectionListener(selectionListener);
			resetToSelectedButton.addSelectionListener(selectionListener);		
		}
		
		if (showVersionSelection) {
			Group versionGroup = new Group(composite, SWT.NONE);
			versionGroup.setText("Last artifact version:");
			GridLayout versionLayout = new GridLayout();
			versionLayout.numColumns = 2;
			versionGroup.setLayout(versionLayout);
			versionGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			Label versionLabel = new Label(versionGroup, SWT.NONE);
			versionLabel.setText("Reset to:");
			resetVersionText = new Text(versionGroup, SWT.BORDER);
			GridData data = new GridData();
			data.widthHint = 100;
			resetVersionText.setLayoutData(data);
			resetVersionText.setText("0");
			VerifyListener verifyListener = new VerifyListener() {
			    public void verifyText(VerifyEvent e) {
			    	String text = e.text;
			    	for (int i = 0; i < text.length(); i++) {
			    		if (-1 == "0123456789".indexOf(text.substring(i, i+1))) {
			    			e.doit = false;
			    			break;
			    		}
			    	}
			    }				
			};
			resetVersionText.addVerifyListener(verifyListener);
		}
		
		return composite;
	}
	
	@Override
	protected void okPressed() {
		if (!showDateSelection || resetTo1999Button.getSelection()) {
			resetDate = Timestamp.valueOf("1999-01-01 00:00:00.0");
		}
		else if (resetToCurrentButton.getSelection()) {
			Date date = new Date();
			resetDate = new Timestamp(date.getTime());
		}
		else if (resetToSelectedButton.getSelection()) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(date.getYear(), date.getMonth(), date.getDay());
			calendar.set(Calendar.HOUR_OF_DAY, time.getHours());
			calendar.set(Calendar.MINUTE, time.getMinutes());
			calendar.set(Calendar.SECOND, time.getSeconds());
			resetDate = new Timestamp(calendar.getTimeInMillis());
		}
		if (showVersionSelection) {
			if (0 == resetVersionText.getText().trim().length()) {
				resetVersion = "0";
			} else {
				resetVersion = resetVersionText.getText();
			}
		}
		super.okPressed();
	}

	private void setDateTimeEnablement() {
		dateLabel.setEnabled(resetToSelectedButton.getSelection());
		date.setEnabled(resetToSelectedButton.getSelection());
		timeLabel.setEnabled(resetToSelectedButton.getSelection());
		time.setEnabled(resetToSelectedButton.getSelection());
	}
	
	public Timestamp getResetDate() {
		return resetDate;
	}

	public String getResetVersion() {
		return resetVersion;
	}

}
