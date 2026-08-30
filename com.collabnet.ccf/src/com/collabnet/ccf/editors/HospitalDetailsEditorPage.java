package com.collabnet.ccf.editors;

import java.sql.Timestamp;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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

public class HospitalDetailsEditorPage extends HospitalEditorPage {
	private ScrolledForm form;
	private FormToolkit toolkit;
	
	private Section sourceSection;
	private Composite sourceSectionClient;
	private Section targetSection;
	private Composite targetSectionClient;
	private Section detailsSection;
	private Composite detailsSectionClient;
	
	public final static String SOURCE_SECTION_STATE = "HospitalDetailsEditorPage.sourceSectionExpanded";
	public final static String TARGET_SECTION_STATE = "HospitalDetailsEditorPage.targetSectionExpanded";
	public final static String DETAILS_SECTION_STATE = "HospitalDetailsEditorPage.detailsSectionExpanded";

	public HospitalDetailsEditorPage(String id, String title) {
		super(id, title);
	}

	public HospitalDetailsEditorPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		form = managedForm.getForm();
        toolkit = getEditor().getToolkit();
        TableWrapLayout formLayout = new TableWrapLayout();
        formLayout.makeColumnsEqualWidth = true;
        formLayout.numColumns = 2;
        form.getBody().setLayout(formLayout);
		createControls(form.getBody());
	}
	
	private void createControls(Composite composite) {
		Label headerLabel = new Label(composite, SWT.NONE);
		headerLabel.setText("Hospital Details");
		headerLabel.setBackground(((FormEditor)getEditor()).getToolkit().getColors().getBackground());
		headerLabel.setFont(JFaceResources.getHeaderFont());
        TableWrapData td = new TableWrapData(TableWrapData.FILL);
        td.colspan = 2;
        headerLabel.setLayoutData(td);
        
        createSourceSection(composite);
        createTargetSection(composite);
        createDetailsSection(composite);
        
        toolkit.paintBordersFor(sourceSectionClient);
        String expansionState = getDialogSettings().get(SOURCE_SECTION_STATE);
        sourceSection.setExpanded(expansionState == null  || expansionState.equals(STATE_EXPANDED));
        
        toolkit.paintBordersFor(targetSectionClient);
        expansionState = getDialogSettings().get(TARGET_SECTION_STATE);
        targetSection.setExpanded(expansionState == null  || expansionState.equals(STATE_EXPANDED));
        
        toolkit.paintBordersFor(detailsSectionClient);
        expansionState = getDialogSettings().get(DETAILS_SECTION_STATE);
        detailsSection.setExpanded(expansionState == null  || expansionState.equals(STATE_EXPANDED));
	}
	
	private void createSourceSection(Composite composite) {
		TableWrapData td;
		sourceSection = toolkit.createSection(composite, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        sourceSection.setLayoutData(td);
        sourceSection.setText("Source");
        sourceSectionClient = toolkit.createComposite(sourceSection); 
        GridLayout sourceLayout = new GridLayout();
        sourceLayout.numColumns = 2;
        sourceLayout.verticalSpacing = 10;
        sourceSectionClient.setLayout(sourceLayout);
        sourceSection.setClient(sourceSectionClient);
        sourceSection.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
                if (e.getState()) getDialogSettings().put(SOURCE_SECTION_STATE, STATE_EXPANDED);
                else getDialogSettings().put(SOURCE_SECTION_STATE, STATE_CONTRACTED);
            }
        });
        
        toolkit.createLabel(sourceSectionClient, "Repository ID:");
        String repositoryId = getPatient().getSourceRepositoryId();
        if (null == repositoryId) repositoryId = "";
        Text repositoryIdText = toolkit.createText(sourceSectionClient, repositoryId, SWT.BORDER | SWT.READ_ONLY);
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        repositoryIdText.setLayoutData(gd);

        toolkit.createLabel(sourceSectionClient, "System kind:");
        String systemKind = getPatient().getSourceSystemKind();
        if (null == systemKind) systemKind = "";
        Text systemKindText = toolkit.createText(sourceSectionClient, systemKind, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        systemKindText.setLayoutData(gd);
        
        toolkit.createLabel(sourceSectionClient, "Repository kind:");
        String repositoryKind = getPatient().getSourceRepositoryKind();
        if (null == repositoryKind) repositoryKind = "";
        Text repositoryKindText = toolkit.createText(sourceSectionClient, repositoryKind, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        repositoryKindText.setLayoutData(gd);
        
        toolkit.createLabel(sourceSectionClient, "Artifact ID:");
        String artifactId = getPatient().getSourceArtifactId();
        if (null == artifactId) artifactId = "";
        Text artifactIdText = toolkit.createText(sourceSectionClient, artifactId, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        artifactIdText.setLayoutData(gd);
        
        toolkit.createLabel(sourceSectionClient, "Last modification:");
        Timestamp timestamp = getPatient().getSourceLastModificationTime();
        String lastModification;
        if (null == timestamp) lastModification = "";
        else lastModification = timestamp.toString();
        Text lastModificationText = toolkit.createText(sourceSectionClient, lastModification, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        lastModificationText.setLayoutData(gd);
        
        toolkit.createLabel(sourceSectionClient, "Artifact version:");
        String artifactVersion = getPatient().getSourceArtifactVersion();
        if (null == artifactVersion) artifactVersion = "";
        Text artifactVersionText = toolkit.createText(sourceSectionClient, artifactVersion, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        artifactVersionText.setLayoutData(gd);
        
	}
	
	private void createTargetSection(Composite composite) {
		TableWrapData td;
		targetSection = toolkit.createSection(composite, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        targetSection.setLayoutData(td);
        targetSection.setText("Target");
        targetSectionClient = toolkit.createComposite(targetSection); 
        GridLayout targetLayout = new GridLayout();
        targetLayout.numColumns = 2;
        targetLayout.verticalSpacing = 10;
        targetSectionClient.setLayout(targetLayout);
        targetSection.setClient(targetSectionClient);
        targetSection.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
                if (e.getState()) getDialogSettings().put(TARGET_SECTION_STATE, STATE_EXPANDED);
                else getDialogSettings().put(TARGET_SECTION_STATE, STATE_CONTRACTED);
            }
        });
        
        toolkit.createLabel(targetSectionClient, "Repository ID:");
        String repositoryId = getPatient().getTargetRepositoryId();
        if (null == repositoryId) repositoryId = "";
        Text repositoryIdText = toolkit.createText(targetSectionClient, repositoryId, SWT.BORDER | SWT.READ_ONLY);
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        repositoryIdText.setLayoutData(gd);
        
        toolkit.createLabel(targetSectionClient, "System kind:");
        String systemKind = getPatient().getTargetSystemKind();
        if (null == systemKind) systemKind = "";
        Text systemKindText = toolkit.createText(targetSectionClient, systemKind, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        systemKindText.setLayoutData(gd);
        
        toolkit.createLabel(targetSectionClient, "Repository kind:");
        String repositoryKind = getPatient().getTargetRepositoryKind();
        if (null == repositoryKind) repositoryKind = "";
        Text repositoryKindText = toolkit.createText(targetSectionClient, repositoryKind, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        repositoryKindText.setLayoutData(gd);
        
        toolkit.createLabel(targetSectionClient, "Artifact ID:");
        String artifactId = getPatient().getTargetArtifactId();
        if (null == artifactId) artifactId = "";
        Text artifactIdText = toolkit.createText(targetSectionClient, artifactId, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        artifactIdText.setLayoutData(gd);
        
        toolkit.createLabel(targetSectionClient, "Last modification:");
        Timestamp timestamp = getPatient().getTargetLastModificationTime();
        String lastModification;
        if (null == timestamp) lastModification = "";
        else lastModification = timestamp.toString();
        Text lastModificationText = toolkit.createText(targetSectionClient, lastModification, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        lastModificationText.setLayoutData(gd);
        
        toolkit.createLabel(targetSectionClient, "Artifact version:");
        String artifactVersion = getPatient().getTargetArtifactVersion();
        if (null == artifactVersion) artifactVersion = "";
        Text artifactVersionText = toolkit.createText(targetSectionClient, artifactVersion, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        artifactVersionText.setLayoutData(gd);

	}
	
	private void createDetailsSection(Composite composite) {
		TableWrapData td;
		detailsSection = toolkit.createSection(composite, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 2;
        detailsSection.setLayoutData(td);
        detailsSection.setText("Details");
        detailsSectionClient = toolkit.createComposite(detailsSection); 
        GridLayout detailsLayout = new GridLayout();
        detailsLayout.numColumns = 4;
        detailsLayout.verticalSpacing = 10;
        detailsSectionClient.setLayout(detailsLayout);
        detailsSection.setClient(detailsSectionClient);
        detailsSection.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
                if (e.getState()) getDialogSettings().put(DETAILS_SECTION_STATE, STATE_EXPANDED);
                else getDialogSettings().put(DETAILS_SECTION_STATE, STATE_CONTRACTED);
            }
        });
        
        toolkit.createLabel(detailsSectionClient, "Adaptor name:");
        String adaptorName = getPatient().getAdaptorName();
        if (null == adaptorName) adaptorName = "";
        Text adaptorNameText = toolkit.createText(detailsSectionClient, adaptorName, SWT.BORDER | SWT.READ_ONLY);
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        adaptorNameText.setLayoutData(gd);
        
        toolkit.createLabel(detailsSectionClient, "Artifact type:");
        String artifactType = getPatient().getArtifactType();
        if (null == artifactType) artifactType = "";
        Text artifactTypeText = toolkit.createText(detailsSectionClient, artifactType, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        artifactTypeText.setLayoutData(gd);    
        
        toolkit.createLabel(detailsSectionClient, "Data type:");
        String dataType = getPatient().getDataType();
        if (null == dataType) dataType = "";
        Text dataTypeText = toolkit.createText(detailsSectionClient, dataType, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        dataTypeText.setLayoutData(gd);
        
        toolkit.createLabel(detailsSectionClient, "Data:");
        String data = getPatient().getData();
        if (null == data) data = "";
        Text dataText = toolkit.createText(detailsSectionClient, data, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        dataText.setLayoutData(gd);
        
        toolkit.createLabel(detailsSectionClient, "Originating component:");
        String component = getPatient().getOriginatingComponent();
        if (null == component) component = "";
        Text componentText = toolkit.createText(detailsSectionClient, component, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        componentText.setLayoutData(gd);
        
        Composite fixedGroup = toolkit.createComposite(detailsSectionClient);
		GridLayout fixedLayout = new GridLayout();
		fixedLayout.numColumns = 2;
		fixedGroup.setLayout(fixedLayout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		fixedGroup.setLayoutData(gd);
		
		final Button reprocessedButton = toolkit.createButton(fixedGroup, "Reprocessed", SWT.CHECK);
		reprocessedButton.setSelection(getPatient().isReprocessed());
		
		final Button fixedButton = toolkit.createButton(fixedGroup, "Fixed", SWT.CHECK);
		fixedButton.setSelection(getPatient().isFixed());

		SelectionListener buttonListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				reprocessedButton.setSelection(getPatient().isReprocessed());
				fixedButton.setSelection(getPatient().isFixed());
			}			
		};
		reprocessedButton.addSelectionListener(buttonListener);
		fixedButton.addSelectionListener(buttonListener);
        
	}

}
