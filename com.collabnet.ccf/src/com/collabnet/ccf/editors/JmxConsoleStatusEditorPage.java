package com.collabnet.ccf.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
import com.collabnet.ccf.CCFJMXMonitorBean;
import com.collabnet.ccf.ICcfParticipant;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.model.Landscape;

public class JmxConsoleStatusEditorPage extends JmxConsoleEditorPage {
	private Landscape landscape;
	private CCFJMXMonitorBean monitor1;
	private CCFJMXMonitorBean monitor2;
	private CcfDataProvider dataProvider;
	
	private ScrolledForm form;
	private FormToolkit toolkit;
	
	private Button aliveButton1;
	private Text uptimeText1;
	private Text extractionTimeText1;
	private Text updateTimeText1;
	private Text memoryConsumptionText1;
	private Text artifactsShippedText1;
	private Text exceptionsCaughtText1;
	private Text artifactsQuarantinedText1;
	private Text hospitalCountText1;
	private Button restartButton1;
	private Button refreshButton1;
	
	private Button aliveButton2;
	private Text uptimeText2;
	private Text extractionTimeText2;
	private Text updateTimeText2;
	private Text memoryConsumptionText2;
	private Text artifactsShippedText2;
	private Text exceptionsCaughtText2;
	private Text artifactsQuarantinedText2;
	private Text hospitalCountText2;
	private Button restartButton2;
	private Button refreshButton2;
	
	private boolean running1;
	private boolean running2;

	public JmxConsoleStatusEditorPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		form = managedForm.getForm();
        toolkit = getEditor().getToolkit();
        TableWrapLayout formLayout = new TableWrapLayout();
        formLayout.numColumns = 2;
        form.getBody().setLayout(formLayout);
		createControls(form.getBody());
	}
	
	private void createControls(Composite composite) {
		JmxConsoleEditor editor = (JmxConsoleEditor)getEditor();
		landscape = editor.getLandscape();
		monitor1 = editor.getMonitor1();
		monitor2 = editor.getMonitor2();
		dataProvider = editor.getDataProvider();

		int port1 = monitor1.getRmiPort();
		int port2 = monitor2.getRmiPort();
		
		Section direction1Section = toolkit.createSection(composite, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 1;
        direction1Section.setLayoutData(td);
        direction1Section.setText(Landscape.getTypeDescription(landscape.getType1()) + " => " + Landscape.getTypeDescription(landscape.getType2()));
        Composite direction1SectionClient = toolkit.createComposite(direction1Section); 
        GridLayout direction1Layout = new GridLayout();
        direction1Layout.numColumns = 2;
        direction1Layout.verticalSpacing = 10;
        direction1SectionClient.setLayout(direction1Layout);
        direction1Section.setClient(direction1SectionClient);
        direction1Section.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
            }
        });
        
        aliveButton1 = toolkit.createButton(direction1SectionClient, "Running", SWT.CHECK);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		aliveButton1.setLayoutData(data);
		
		toolkit.createLabel(direction1SectionClient, "Host:");
		Text host1Text = toolkit.createText(direction1SectionClient, landscape.getHostName1() + ":" + port1, SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		host1Text.setLayoutData(data);
		
		toolkit.createLabel(direction1SectionClient, "Uptime:");
		uptimeText1 = toolkit.createText(direction1SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		uptimeText1.setLayoutData(data);
		
		toolkit.createLabel(direction1SectionClient, "Artifact extraction time:");
		extractionTimeText1 = toolkit.createText(direction1SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		extractionTimeText1.setLayoutData(data);
		
		toolkit.createLabel(direction1SectionClient, "Artifact update time:");
		updateTimeText1 = toolkit.createText(direction1SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		updateTimeText1.setLayoutData(data);
		
		toolkit.createLabel(direction1SectionClient, "Memory consumption:");
		memoryConsumptionText1 = toolkit.createText(direction1SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		memoryConsumptionText1.setLayoutData(data);
		
		toolkit.createLabel(direction1SectionClient, "Artifacts shipped:");
		artifactsShippedText1 = toolkit.createText(direction1SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		artifactsShippedText1.setLayoutData(data);
		
		toolkit.createLabel(direction1SectionClient, "Exceptions caught:");
		exceptionsCaughtText1 = toolkit.createText(direction1SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		exceptionsCaughtText1.setLayoutData(data);
		
		toolkit.createLabel(direction1SectionClient, "Artifacts quarantined:");
		artifactsQuarantinedText1 = toolkit.createText(direction1SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		artifactsQuarantinedText1.setLayoutData(data);
		
		toolkit.createLabel(direction1SectionClient, "Hospital entries:");
		hospitalCountText1 = toolkit.createText(direction1SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		hospitalCountText1.setLayoutData(data);		
		
		Composite refreshGroup1 = toolkit.createComposite(direction1SectionClient, SWT.NULL);
		GridLayout refresh1Layout = new GridLayout();
		refresh1Layout.numColumns = 2;
		refreshGroup1.setLayout(refresh1Layout);
		data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		data.horizontalSpan = 2;
		refreshGroup1.setLayoutData(data);
		
		restartButton1 = toolkit.createButton(refreshGroup1, "Restart", SWT.PUSH);
		data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		restartButton1.setLayoutData(data);
		
		refreshButton1 = toolkit.createButton(refreshGroup1, "Refresh", SWT.PUSH);
		data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		refreshButton1.setLayoutData(data);
		
		Section direction2Section = toolkit.createSection(composite, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 1;
        direction2Section.setLayoutData(td);
        direction2Section.setText(Landscape.getTypeDescription(landscape.getType2()) + " => " + Landscape.getTypeDescription(landscape.getType1()));
        Composite direction2SectionClient = toolkit.createComposite(direction2Section); 
        GridLayout direction2Layout = new GridLayout();
        direction2Layout.numColumns = 2;
        direction2Layout.verticalSpacing = 10;
        direction2SectionClient.setLayout(direction2Layout);
        direction2Section.setClient(direction2SectionClient);
        direction2Section.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
            }
        });
        
        aliveButton2 = toolkit.createButton(direction2SectionClient, "Running", SWT.CHECK);
		data = new GridData();
		data.horizontalSpan = 2;
		aliveButton2.setLayoutData(data);
		
		toolkit.createLabel(direction2SectionClient, "Host:");
		Text host2Text = toolkit.createText(direction2SectionClient, landscape.getHostName2() + ":" + port2, SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		host2Text.setLayoutData(data);
		
		toolkit.createLabel(direction2SectionClient, "Uptime:");
		uptimeText2 = toolkit.createText(direction2SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		uptimeText2.setLayoutData(data);
		
		toolkit.createLabel(direction2SectionClient, "Artifact extraction time:");
		extractionTimeText2 = toolkit.createText(direction2SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		extractionTimeText2.setLayoutData(data);
		
		toolkit.createLabel(direction2SectionClient, "Artifact update time:");
		updateTimeText2 = toolkit.createText(direction2SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		updateTimeText2.setLayoutData(data);
		
		toolkit.createLabel(direction2SectionClient, "Memory consumption:");
		memoryConsumptionText2 = toolkit.createText(direction2SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		memoryConsumptionText2.setLayoutData(data);
		
		toolkit.createLabel(direction2SectionClient, "Artifacts shipped:");
		artifactsShippedText2 = toolkit.createText(direction2SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		artifactsShippedText2.setLayoutData(data);
		
		toolkit.createLabel(direction2SectionClient, "Exceptions caught:");
		exceptionsCaughtText2 = toolkit.createText(direction2SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		exceptionsCaughtText2.setLayoutData(data);
		
		toolkit.createLabel(direction2SectionClient, "Artifacts quarantined:");
		artifactsQuarantinedText2 = toolkit.createText(direction2SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		artifactsQuarantinedText2.setLayoutData(data);
		
		toolkit.createLabel(direction2SectionClient, "Hospital entries:");
		hospitalCountText2 = toolkit.createText(direction2SectionClient, "", SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		hospitalCountText2.setLayoutData(data);		
		
		Composite refreshGroup2 = toolkit.createComposite(direction2SectionClient, SWT.NULL);
		GridLayout refresh2Layout = new GridLayout();
		refresh2Layout.numColumns = 2;
		refreshGroup2.setLayout(refresh2Layout);
		data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		data.horizontalSpan = 2;
		refreshGroup2.setLayoutData(data);
		
		restartButton2 = toolkit.createButton(refreshGroup2, "Restart", SWT.PUSH);
		data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		restartButton2.setLayoutData(data);
		
		refreshButton2 = toolkit.createButton(refreshGroup2, "Refresh", SWT.PUSH);
		data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		refreshButton2.setLayoutData(data);
		
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				getMonitor1Info();
				getMonitor2Info();
			}			
		});
		
		SelectionListener selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				if (se.getSource() == aliveButton1) {
					aliveButton1.setSelection(running1);
				}
				if (se.getSource() == aliveButton2) {
					aliveButton2.setSelection(running2);
				}
				if (se.getSource() == refreshButton1) {
					BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
						public void run() {
							getMonitor1Info();
						}			
					});					
				}
				if (se.getSource() == refreshButton2) {
					BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
						public void run() {
							getMonitor2Info();
						}			
					});					
				}	
				if (se.getSource() == restartButton1) {
					restart(monitor1);
				}
				if (se.getSource() == restartButton2) {
					restart(monitor2);
				}
			}			
		};
		
		aliveButton1.addSelectionListener(selectionListener);
		aliveButton2.addSelectionListener(selectionListener);
		refreshButton1.addSelectionListener(selectionListener);
		refreshButton2.addSelectionListener(selectionListener);
		restartButton1.addSelectionListener(selectionListener);
		restartButton2.addSelectionListener(selectionListener);
        
        toolkit.paintBordersFor(direction1SectionClient);
        toolkit.paintBordersFor(direction2SectionClient);
	}
	
	private void getMonitor1Info() {
		running1 = monitor1.isAlive();
		aliveButton1.setSelection(running1);
		Long uptime = monitor1.getCCFUptime();
		uptimeText1.setText(getTime(uptime));
		String readerMetricsName = null;
		String writerMetricsName = null;
		String extractionTime = null;
		String updateTime = null;
		try {
			ICcfParticipant ccfParticipant = Activator.getCcfParticipantForType(landscape.getType1());
			readerMetricsName = ccfParticipant.getReaderMetricsName();
			ccfParticipant = Activator.getCcfParticipantForType(landscape.getType2());
			writerMetricsName = ccfParticipant.getWriterMetricsName();
		} catch (Exception e) {
			Activator.handleError(e);
		}
		extractionTime = monitor1.getArtifactExtractionProcessingTime(readerMetricsName);
		updateTime = monitor1.getArtifactUpdateProcessingTime(writerMetricsName);
		if (null == extractionTime) extractionTimeText1.setText("");
		else extractionTimeText1.setText(extractionTime);
		if (null == updateTime) updateTimeText1.setText("");
		else updateTimeText1.setText(updateTime);
		String memoryConsumption = monitor1.getCCFMemoryConsumption();
		if (null == memoryConsumption) memoryConsumptionText1.setText("");
		else memoryConsumptionText1.setText(memoryConsumption);
		restartButton1.setEnabled(running1);
		String artifactsShipped = monitor1.getNumberOfArtifactsShipped(readerMetricsName);
		if (null == artifactsShipped) artifactsShippedText1.setText("");
		else artifactsShippedText1.setText(artifactsShipped);
		String exceptionsCaught = monitor1.getNumberOfCCFExceptionsCaught();
		if (null == exceptionsCaught) exceptionsCaughtText1.setText("");
		else exceptionsCaughtText1.setText(exceptionsCaught);
		String artifactsQuarantined = monitor1.getNumberOfArtifactsQuarantined();
		if (null == artifactsQuarantined) artifactsQuarantinedText1.setText("");
		else artifactsQuarantinedText1.setText(artifactsQuarantined);
		
		int hospitalEntries = getHospitalCount(landscape.getType2());
		if (0 == hospitalEntries) hospitalCountText1.setText("");
		else hospitalCountText1.setText(Integer.toString(hospitalEntries));
	}
	
	private void getMonitor2Info() {
		running2 = monitor2.isAlive();
		aliveButton2.setSelection(running2);
		Long uptime = monitor2.getCCFUptime();
		uptimeText2.setText(getTime(uptime));
		String readerMetricsName = null;
		String writerMetricsName = null;
		String extractionTime = null;
		String updateTime = null;
		try {
			ICcfParticipant ccfParticipant = Activator.getCcfParticipantForType(landscape.getType2());
			readerMetricsName = ccfParticipant.getReaderMetricsName();
			ccfParticipant = Activator.getCcfParticipantForType(landscape.getType1());
			writerMetricsName = ccfParticipant.getWriterMetricsName();
		} catch (Exception e) {
			Activator.handleError(e);
		}
		extractionTime = monitor2.getArtifactExtractionProcessingTime(readerMetricsName);
		updateTime = monitor2.getArtifactUpdateProcessingTime(writerMetricsName);
		if (null == extractionTime) extractionTimeText2.setText("");
		else extractionTimeText2.setText(extractionTime);
		if (null == updateTime) updateTimeText2.setText("");
		else updateTimeText2.setText(updateTime);
		String memoryConsumption = monitor2.getCCFMemoryConsumption();
		if (null == memoryConsumption) memoryConsumptionText2.setText("");
		else memoryConsumptionText2.setText(memoryConsumption);
		restartButton2.setEnabled(running2);
		String artifactsShipped = monitor2.getNumberOfArtifactsShipped(readerMetricsName);
		if (null == artifactsShipped) artifactsShippedText2.setText("");
		else artifactsShippedText2.setText(artifactsShipped);
		String exceptionsCaught = monitor2.getNumberOfCCFExceptionsCaught();
		if (null == exceptionsCaught) exceptionsCaughtText2.setText("");
		else exceptionsCaughtText2.setText(exceptionsCaught);
		String artifactsQuarantined = monitor2.getNumberOfArtifactsQuarantined();
		if (null == artifactsQuarantined) artifactsQuarantinedText2.setText("");
		else artifactsQuarantinedText2.setText(artifactsQuarantined);
		
		int hospitalEntries = getHospitalCount(landscape.getType1());
		if (0 == hospitalEntries) hospitalCountText2.setText("");
		else hospitalCountText2.setText(Integer.toString(hospitalEntries));
	}
	
	private void restart(CCFJMXMonitorBean monitor) {
		monitor.restartCCFInstance();
		if (monitor == monitor1) getMonitor1Info();
		if (monitor == monitor2) getMonitor2Info();
	}
	
	private String getTime(Long milliseconds) {
		if (null != milliseconds) {
			long timeMilliseconds = milliseconds.longValue();
			long time = timeMilliseconds/1000;
			String seconds = Integer.toString((int)(time % 60));  
			String minutes = Integer.toString((int)((time % 3600) / 60));  
			String hours = Integer.toString((int)(time / 3600));  
			for (int i = 0; i < 2; i++) {  
				if (2 > seconds.length()) {  
					seconds = "0" + seconds;  
				}  
				if (2 > minutes.length()) {  
					minutes = "0" + minutes;  
				}  
				if (2 > hours.length()) {  
					hours = "0" + hours;  
				}  
			}
			return hours + ":" + minutes + ":" + seconds;
		}
		return "";
	}
	
	private int getHospitalCount(String target) {
		String targetType = null;
		String sourceType = null;

		if (target.equals(landscape.getType1())) {
			targetType = landscape.getType1();
			sourceType = landscape.getType2();
		} else {
			targetType = landscape.getType2();
			sourceType = landscape.getType1();
		}		
		
		int hospitalCount = 0;
		try {
			hospitalCount = dataProvider.getHospitalCount(landscape, targetType, sourceType);
		} catch (Exception e) {
			Activator.handleError(e);
		}	
		return hospitalCount;
	}

}
