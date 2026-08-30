package com.collabnet.ccf.editors;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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

public class HospitalExceptionEditorPage extends HospitalEditorPage {
	private ScrolledForm form;
	private FormToolkit toolkit;
	
	private Text errorCodeText;
	private Text classText;
	private Text messageText;
	private Text causeClassText;
	private Text causeMessageText;

	private Section messageSection;
	private Composite messageSectionClient;
	private Section causeMessageSection;
	private Composite causeMessageSectionClient;
	private Section stackTraceSection;
	private Composite stackTraceSectionClient;
	
	public final static String STACKTRACE_SECTION_STATE = "HospitalExceptionEditorPage.stackTraceSectionExpanded";
	public final static String MESSAGE_SECTION_STATE = "HospitalExceptionEditorPage.messageSectionExpanded";
	public final static String CAUSE_MESSAGE_SECTION_STATE = "HospitalExceptionEditorPage.causeMessageSectionExpanded";
	
	public HospitalExceptionEditorPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}
	
	public HospitalExceptionEditorPage(String id, String title) {
		super(id, title);
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
		Label headerLabel = new Label(composite, SWT.NONE);
		headerLabel.setText("Exception Details");
		headerLabel.setBackground(((FormEditor)getEditor()).getToolkit().getColors().getBackground());
		headerLabel.setFont(JFaceResources.getHeaderFont());
        TableWrapData td = new TableWrapData(TableWrapData.FILL);
        td.colspan = 2;
        headerLabel.setLayoutData(td);
 
        toolkit.createLabel(composite, "Error code:");
        String errorCode = getPatient().getErrorCode();
        if (null == errorCode) errorCode = "";
        errorCodeText = toolkit.createText(composite, errorCode, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        errorCodeText.setLayoutData(td);
		
        toolkit.createLabel(composite, "Class:");
        String className = getPatient().getExceptionClassName();
        if (null == className) className = "";
        classText = toolkit.createText(composite, className, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        classText.setLayoutData(td);
		
		createMessageSection(composite);
		
        toolkit.createLabel(composite, "Cause class:");
        className = getPatient().getCauseExceptionClassName();
        if (null == className) className = "";
        causeClassText = toolkit.createText(composite, className, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        causeClassText.setLayoutData(td);
		
		createCauseMessageSection(composite);
        
        if (null != getPatient().getStackTrace() && 0 < getPatient().getStackTrace().trim().length()) {
        	createStackTraceSection(composite);
        }
        
        toolkit.paintBordersFor(messageSectionClient);
        String expansionState = getDialogSettings().get(MESSAGE_SECTION_STATE);
        messageSection.setExpanded(expansionState == null  || expansionState.equals(STATE_EXPANDED));
        
        toolkit.paintBordersFor(causeMessageSectionClient);
        expansionState = getDialogSettings().get(CAUSE_MESSAGE_SECTION_STATE);
        causeMessageSection.setExpanded(expansionState == null  || expansionState.equals(STATE_EXPANDED));
        
        if (null != stackTraceSectionClient) { 
        	toolkit.paintBordersFor(stackTraceSectionClient);
            expansionState = getDialogSettings().get(STACKTRACE_SECTION_STATE);
            stackTraceSection.setExpanded(expansionState == null  || expansionState.equals(STATE_EXPANDED));
        }
        
	}

	private void createMessageSection(Composite composite) {
		TableWrapData td;
		messageSection = toolkit.createSection(composite, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 2;
        messageSection.setLayoutData(td);
        messageSection.setText("Message");
        messageSectionClient = toolkit.createComposite(messageSection); 
        GridLayout messageLayout = new GridLayout();
        messageLayout.numColumns = 1;
        messageLayout.verticalSpacing = 10;
        messageSectionClient.setLayout(messageLayout);
        messageSection.setClient(messageSectionClient);
        messageSection.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
                if (e.getState()) getDialogSettings().put(MESSAGE_SECTION_STATE, STATE_EXPANDED);
                else getDialogSettings().put(MESSAGE_SECTION_STATE, STATE_CONTRACTED);
            }
        });
        
        String message = getPatient().getExceptionMessage();
        if (null == message) message = "";
        messageText = toolkit.createText(messageSectionClient, message, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        gd.heightHint = 75;
        messageText.setLayoutData(gd);
	}
	
	private void createCauseMessageSection(Composite composite) {
		TableWrapData td;
		causeMessageSection = toolkit.createSection(composite, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 2;
        causeMessageSection.setLayoutData(td);
        causeMessageSection.setText("Cause Message");
        causeMessageSectionClient = toolkit.createComposite(causeMessageSection); 
        GridLayout causeMessageLayout = new GridLayout();
        causeMessageLayout.numColumns = 1;
        causeMessageLayout.verticalSpacing = 10;
        causeMessageSectionClient.setLayout(causeMessageLayout);
        causeMessageSection.setClient(causeMessageSectionClient);
        causeMessageSection.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
                if (e.getState()) getDialogSettings().put(CAUSE_MESSAGE_SECTION_STATE, STATE_EXPANDED);
                else getDialogSettings().put(CAUSE_MESSAGE_SECTION_STATE, STATE_CONTRACTED);
            }
        });
        
        String message = getPatient().getCauseExceptionMessage();
        if (null == message) message = "";
        causeMessageText = toolkit.createText(causeMessageSectionClient, message, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        gd.heightHint = 75;
        causeMessageText.setLayoutData(gd);
	}	
	
	private void createStackTraceSection(Composite composite) {
		TableWrapData td;
		stackTraceSection = toolkit.createSection(composite, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 2;
        stackTraceSection.setLayoutData(td);
        stackTraceSection.setText("Stack Trace");
        stackTraceSectionClient = toolkit.createComposite(stackTraceSection); 
        GridLayout stackTraceLayout = new GridLayout();
        stackTraceLayout.numColumns = 1;
        stackTraceLayout.verticalSpacing = 10;
        stackTraceSectionClient.setLayout(stackTraceLayout);
        stackTraceSection.setClient(stackTraceSectionClient);
        stackTraceSection.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
                if (e.getState()) getDialogSettings().put(STACKTRACE_SECTION_STATE, STATE_EXPANDED);
                else getDialogSettings().put(STACKTRACE_SECTION_STATE, STATE_CONTRACTED);
            }
        });
        
        toolkit.createText(stackTraceSectionClient, getPatient().getStackTrace(), SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
	}	

}
