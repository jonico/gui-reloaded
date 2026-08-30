package com.collabnet.ccf.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.db.Filter;

public class HospitalFilterDialog extends CcfDialog {
	private Filter[][] filters;
	private boolean filtersActive;
	
	private Button filtersActiveButton;

	private Text sourceRepositoryIdText;
	private Combo sourceRepositoryIdCombo;
	private Text sourceSystemKindText;
	private Combo sourceSystemKindCombo;
	private Text sourceRepositoryKindText;
	private Combo sourceRepositoryKindCombo;
	private Text sourceArtifactIdText;
	private Combo sourceArtifactIdCombo;
	private Text sourceArtifactVersionText;
	private Combo sourceArtifactVersionCombo;

	private Text targetRepositoryIdText;
	private Combo targetRepositoryIdCombo;
	private Text targetSystemKindText;
	private Combo targetSystemKindCombo;
	private Text targetRepositoryKindText;
	private Combo targetRepositoryKindCombo;
	private Text targetArtifactIdText;
	private Combo targetArtifactIdCombo;
	private Text targetArtifactVersionText;
	private Combo targetArtifactVersionCombo;
	
	private Text adaptorNameText;
	private Combo adaptorNameCombo;
	private Text originatingComponentText;
	private Combo originatingComponentCombo;
	private Text dataText;
	private Combo dataCombo;
	private Text dataTypeText;
	private Combo dataTypeCombo;
	private Text artifactTypeText;
	private Combo artifactTypeCombo;
	private Text genericArtifactText;
	private Combo genericArtifactCombo;
	
	private Text errorCodeText;
	private Combo errorCodeCombo;
	private Text exceptionClassText;
	private Combo exceptionClassCombo;
	private Text exceptionMessageText;
	private Combo exceptionMessageCombo;
	private Text causeExceptionClassText;
	private Combo causeExceptionClassCombo;
	private Text causeExceptionMessageText;
	private Combo causeExceptionMessageCombo;
	private Text stackTraceText;
	private Combo stackTraceCombo;
	
	private Combo reprocessedCombo;
	private Combo fixedCombo;

	public HospitalFilterDialog(Shell shell, Filter[][] filters, boolean filtersActive) {
		super(shell, "HospitalFilterDialog");
		this.filters = filters;
		this.filtersActive = filtersActive;
	}
	
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Filter Hospital");
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		filtersActiveButton = new Button(composite, SWT.CHECK);
		filtersActiveButton.setText("Filters active");
		GridData data = new GridData();
		data.horizontalSpan = 2;
		filtersActiveButton.setLayoutData(data);
		
		Group sourceGroup = new Group(composite, SWT.NULL);
		sourceGroup.setText("Source filters:");
		GridLayout sourceLayout = new GridLayout();
		sourceLayout.numColumns = 3;
		sourceGroup.setLayout(sourceLayout);
		data = new GridData(GridData.FILL_BOTH);
		sourceGroup.setLayoutData(data);

		Label sourceRepositoryIdLabel = new Label(sourceGroup, SWT.NONE);
		sourceRepositoryIdLabel.setText("Repository ID:");
		sourceRepositoryIdText = new Text(sourceGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		sourceRepositoryIdText.setLayoutData(data);
		sourceRepositoryIdCombo = createCombo(sourceGroup);
		
		Label sourceSystemKindLabel = new Label(sourceGroup, SWT.NONE);
		sourceSystemKindLabel.setText("System kind:");
		sourceSystemKindText = new Text(sourceGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		sourceSystemKindText.setLayoutData(data);
		sourceSystemKindCombo = createCombo(sourceGroup);
		
		Label sourceRepositoryKindLabel = new Label(sourceGroup, SWT.NONE);
		sourceRepositoryKindLabel.setText("Repository kind:");
		sourceRepositoryKindText = new Text(sourceGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		sourceRepositoryKindText.setLayoutData(data);
		sourceRepositoryKindCombo = createCombo(sourceGroup);	
		
		Label sourceArtifactIdLabel = new Label(sourceGroup, SWT.NONE);
		sourceArtifactIdLabel.setText("Artifact ID:");
		sourceArtifactIdText = new Text(sourceGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		sourceArtifactIdText.setLayoutData(data);
		sourceArtifactIdCombo = createCombo(sourceGroup);
		
		Label sourceArtifactVersionLabel = new Label(sourceGroup, SWT.NONE);
		sourceArtifactVersionLabel.setText("Artifact version:");
		sourceArtifactVersionText = new Text(sourceGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		sourceArtifactVersionText.setLayoutData(data);
		sourceArtifactVersionCombo = createCombo(sourceGroup);
		
		Group targetGroup = new Group(composite, SWT.NULL);
		targetGroup.setText("Target filters:");
		GridLayout targetLayout = new GridLayout();
		targetLayout.numColumns = 3;
		targetGroup.setLayout(targetLayout);
		data = new GridData(GridData.FILL_BOTH);
		targetGroup.setLayoutData(data);

		Label targetRepositoryIdLabel = new Label(targetGroup, SWT.NONE);
		targetRepositoryIdLabel.setText("Repository ID:");
		targetRepositoryIdText = new Text(targetGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		targetRepositoryIdText.setLayoutData(data);
		targetRepositoryIdCombo = createCombo(targetGroup);
		
		Label targetSystemKindLabel = new Label(targetGroup, SWT.NONE);
		targetSystemKindLabel.setText("System kind:");
		targetSystemKindText = new Text(targetGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		targetSystemKindText.setLayoutData(data);
		targetSystemKindCombo = createCombo(targetGroup);
		
		Label targetRepositoryKindLabel = new Label(targetGroup, SWT.NONE);
		targetRepositoryKindLabel.setText("Repository kind:");
		targetRepositoryKindText = new Text(targetGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		targetRepositoryKindText.setLayoutData(data);
		targetRepositoryKindCombo = createCombo(targetGroup);	
		
		Label targetArtifactIdLabel = new Label(targetGroup, SWT.NONE);
		targetArtifactIdLabel.setText("Artifact ID:");
		targetArtifactIdText = new Text(targetGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		targetArtifactIdText.setLayoutData(data);
		targetArtifactIdCombo = createCombo(targetGroup);
		
		Label targetArtifactVersionLabel = new Label(targetGroup, SWT.NONE);
		targetArtifactVersionLabel.setText("Artifact version:");
		targetArtifactVersionText = new Text(targetGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		targetArtifactVersionText.setLayoutData(data);
		targetArtifactVersionCombo = createCombo(targetGroup);
		
		Group exceptionGroup = new Group(composite, SWT.NULL);
		exceptionGroup.setText("Exception filters:");
		GridLayout exceptionLayout = new GridLayout();
		exceptionLayout.numColumns = 3;
		exceptionGroup.setLayout(exceptionLayout);
		data = new GridData(GridData.FILL_BOTH);
		exceptionGroup.setLayoutData(data);
		
		Label errorCodeLabel = new Label(exceptionGroup, SWT.NONE);
		errorCodeLabel.setText("Error code:");
		errorCodeText = new Text(exceptionGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		errorCodeText.setLayoutData(data);
		errorCodeCombo = createCombo(exceptionGroup);
		
		Label exceptionClassLabel = new Label(exceptionGroup, SWT.NONE);
		exceptionClassLabel.setText("Class:");
		exceptionClassText = new Text(exceptionGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		exceptionClassText.setLayoutData(data);
		exceptionClassCombo = createCombo(exceptionGroup);
		
		Label exceptionMessageLabel = new Label(exceptionGroup, SWT.NONE);
		exceptionMessageLabel.setText("Message:");
		exceptionMessageText = new Text(exceptionGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		exceptionMessageText.setLayoutData(data);
		exceptionMessageCombo = createCombo(exceptionGroup);
		
		Label causeExceptionClassLabel = new Label(exceptionGroup, SWT.NONE);
		causeExceptionClassLabel.setText("Cause class:");
		causeExceptionClassText = new Text(exceptionGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		causeExceptionClassText.setLayoutData(data);
		causeExceptionClassCombo = createCombo(exceptionGroup);
		
		Label causeExceptionMessageLabel = new Label(exceptionGroup, SWT.NONE);
		causeExceptionMessageLabel.setText("Cause message:");
		causeExceptionMessageText = new Text(exceptionGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		causeExceptionMessageText.setLayoutData(data);
		causeExceptionMessageCombo = createCombo(exceptionGroup);
		
		Label stackTraceLabel = new Label(exceptionGroup, SWT.NONE);
		stackTraceLabel.setText("Stack trace:");
		stackTraceText = new Text(exceptionGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		stackTraceText.setLayoutData(data);
		stackTraceCombo = createCombo(exceptionGroup);
		
		Group otherGroup = new Group(composite, SWT.NULL);
		otherGroup.setText("Other filters:");
		GridLayout otherLayout = new GridLayout();
		otherLayout.numColumns = 3;
		otherGroup.setLayout(otherLayout);
		data = new GridData(GridData.FILL_BOTH);
		otherGroup.setLayoutData(data);
		
		Label adaptorNameLabel = new Label(otherGroup, SWT.NONE);
		adaptorNameLabel.setText("Adaptor name:");
		adaptorNameText = new Text(otherGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		adaptorNameText.setLayoutData(data);
		adaptorNameCombo = createCombo(otherGroup);
		
		Label originatingComponentLabel = new Label(otherGroup, SWT.NONE);
		originatingComponentLabel.setText("Originating component:");
		originatingComponentText = new Text(otherGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		originatingComponentText.setLayoutData(data);
		originatingComponentCombo = createCombo(otherGroup);
		
		Label dataTypeLabel = new Label(otherGroup, SWT.NONE);
		dataTypeLabel.setText("Data type:");
		dataTypeText = new Text(otherGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		dataTypeText.setLayoutData(data);
		dataTypeCombo = createCombo(otherGroup);
		
		Label dataLabel = new Label(otherGroup, SWT.NONE);
		dataLabel.setText("Data:");
		dataText = new Text(otherGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		dataText.setLayoutData(data);
		dataCombo = createCombo(otherGroup);
		
		Label artifactTypeLabel = new Label(otherGroup, SWT.NONE);
		artifactTypeLabel.setText("Artifact type:");
		artifactTypeText = new Text(otherGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		artifactTypeText.setLayoutData(data);
		artifactTypeCombo = createCombo(otherGroup);
		
		Label genericArtifactLabel = new Label(otherGroup, SWT.NONE);
		genericArtifactLabel.setText("Generic artifact:");
		genericArtifactText = new Text(otherGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		genericArtifactText.setLayoutData(data);
		genericArtifactCombo = createCombo(otherGroup);
		
		Group statusGroup = new Group(composite, SWT.NULL);
		statusGroup.setText("Status:");
		GridLayout statusLayout = new GridLayout();
		statusLayout.numColumns = 4;
		statusGroup.setLayout(statusLayout);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		statusGroup.setLayoutData(data);
		
		Label reprocessedLabel = new Label(statusGroup, SWT.NONE);
		reprocessedLabel.setText("Reprocessed:");
		reprocessedCombo = new Combo(statusGroup, SWT.READ_ONLY);
		reprocessedCombo.add("");
		reprocessedCombo.add("true");
		reprocessedCombo.add("false");
		
		Label fixedLabel = new Label(statusGroup, SWT.NONE);
		fixedLabel.setText("Fixed:");
		fixedCombo = new Combo(statusGroup, SWT.READ_ONLY);
		fixedCombo.add("");
		fixedCombo.add("true");
		fixedCombo.add("false");
		
		initializeValues();
		
		return composite;
	}
	
	@Override
	protected void okPressed() {
		IDialogSettings settings = getSettings();
		settings.put(Filter.HOSPITAL_FILTERS_SET, true);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_TIMESTAMP, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_EXCEPTION_CLASS_NAME, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_EXCEPTION_MESSAGE, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_CAUSE_EXCEPTION_CLASS_NAME, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_CAUSE_EXCEPTION_MESSAGE, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_STACK_TRACE, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_ADAPTOR_NAME, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_ORIGINATING_COMPONENT, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_DATA_TYPE, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_DATA, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_FIXED, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_REPROCESSED, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_ID, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_ID, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_TARGET_SYSTEM_ID, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_ID, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_KIND, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_KIND, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_TARGET_SYSTEM_KIND, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_KIND, (String)null);	
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_SOURCE_ARTIFACT_ID, (String)null);		
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_TARGET_ARTIFACT_ID, (String)null);	
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_ERROR_CODE, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_SOURCE_ARTIFACT_VERSION, (String)null);		
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_TARGET_ARTIFACT_VERSION, (String)null);
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_ARTIFACT_TYPE, (String)null);		
		settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_GENERIC_ARTIFACT, (String)null);	
		filtersActive = filtersActiveButton.getSelection();
		settings.put(Filter.HOSPITAL_FILTERS_ACTIVE, filtersActive);
		List<Filter> filterList = new ArrayList<Filter>();
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_EXCEPTION_CLASS_NAME, true, exceptionClassText, exceptionClassCombo, settings);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_EXCEPTION_MESSAGE, true, exceptionMessageText, exceptionMessageCombo, settings);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_CAUSE_EXCEPTION_CLASS_NAME, true, causeExceptionClassText, causeExceptionClassCombo, settings);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_CAUSE_EXCEPTION_MESSAGE, true, causeExceptionMessageText, causeExceptionMessageCombo, settings);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_STACK_TRACE, true, stackTraceText, stackTraceCombo, settings);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_ERROR_CODE, true, errorCodeText, errorCodeCombo, settings);		
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_ID, true, sourceRepositoryIdText, sourceRepositoryIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_KIND, true, sourceSystemKindText, sourceSystemKindCombo, settings);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_KIND, true, sourceRepositoryKindText, sourceRepositoryKindCombo, settings);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_SOURCE_ARTIFACT_ID, true, sourceArtifactIdText, sourceArtifactIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_SOURCE_ARTIFACT_VERSION, true, sourceArtifactVersionText, sourceArtifactVersionCombo, settings);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_ID, true, targetRepositoryIdText, targetRepositoryIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_TARGET_SYSTEM_KIND, true, targetSystemKindText, targetSystemKindCombo, settings);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_KIND, true, targetRepositoryKindText, targetRepositoryKindCombo, settings);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_TARGET_ARTIFACT_ID, true, targetArtifactIdText, targetArtifactIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_TARGET_ARTIFACT_VERSION, true, targetArtifactVersionText, targetArtifactVersionCombo, settings);	
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_ADAPTOR_NAME, true, adaptorNameText, adaptorNameCombo, settings);	
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_ORIGINATING_COMPONENT, true, originatingComponentText, originatingComponentCombo, settings);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_DATA, true, dataText, dataCombo, settings);	
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_DATA_TYPE, true, dataTypeText, dataTypeCombo, settings);	
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_ARTIFACT_TYPE, true, artifactTypeText, artifactTypeCombo, settings);	
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_GENERIC_ARTIFACT, true, genericArtifactText, genericArtifactCombo, settings);			
		if (0 < reprocessedCombo.getText().length()) {
			Filter filter = new Filter(CcfDataProvider.HOSPITAL_REPROCESSED, reprocessedCombo.getText(), false, Filter.FILTER_TYPE_EQUAL);
			filterList.add(filter);		
			settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_REPROCESSED, filter.getValue());
			settings.put(Filter.HOSPITAL_FILTER_TYPE + CcfDataProvider.HOSPITAL_REPROCESSED, filter.getFilterType());
		}
		if (0 < fixedCombo.getText().length()) {
			Filter filter = new Filter(CcfDataProvider.HOSPITAL_FIXED, fixedCombo.getText(), false, Filter.FILTER_TYPE_EQUAL);
			filterList.add(filter);			
			settings.put(Filter.HOSPITAL_FILTER_VALUE + CcfDataProvider.HOSPITAL_FIXED, filter.getValue());
			settings.put(Filter.HOSPITAL_FILTER_TYPE + CcfDataProvider.HOSPITAL_FIXED, filter.getFilterType());
		}
//		filters = new Filter[filterList.size()][];
//		filterList.toArray(filters);
		
		Filter[] filterArray = new Filter[filterList.size()];
		filterList.toArray(filterArray);
		filters = new Filter[][] { filterArray };
		
		super.okPressed();
	}
	
	private void updateFilterList(List<Filter> filterList, String columnName, boolean stringValue, Text text, Combo combo, IDialogSettings settings) {
		if (0 < text.getText().trim().length()) {
			int filterType = getFilterType(combo);
			Filter filter = new Filter(columnName, text.getText().trim(), stringValue, filterType);
			filterList.add(filter);						
			settings.put(Filter.HOSPITAL_FILTER_VALUE + columnName, filter.getValue());
			settings.put(Filter.HOSPITAL_FILTER_TYPE + columnName, filter.getFilterType());
		}
	}

	public Filter[][] getFilters() {
		return filters;
	}
	
	public boolean isFiltering() {
		return filtersActive && filters != null && filters.length > 0;
	}
	
	public boolean filtersActive() {
		return filtersActive;
	}
	
	private void initializeValues() {
		filtersActiveButton.setSelection(filtersActive);
		
		Filter[] filters;
		if (null == this.filters) filters = null;
		else filters = this.filters[0];
		
		if (null != filters) {
			for (int i = 0; i < filters.length; i++) {
				if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_ERROR_CODE)) {
					errorCodeText.setText(filters[i].getValue());
					setComboSelection(errorCodeCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_EXCEPTION_CLASS_NAME)) {
					exceptionClassText.setText(filters[i].getValue());
					setComboSelection(exceptionClassCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_EXCEPTION_MESSAGE)) {
					exceptionMessageText.setText(filters[i].getValue());
					setComboSelection(exceptionMessageCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_CAUSE_EXCEPTION_CLASS_NAME)) {
					causeExceptionClassText.setText(filters[i].getValue());
					setComboSelection(causeExceptionClassCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_CAUSE_EXCEPTION_MESSAGE)) {
					causeExceptionMessageText.setText(filters[i].getValue());
					setComboSelection(causeExceptionMessageCombo, filters[i]);
				}	
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_STACK_TRACE)) {
					stackTraceText.setText(filters[i].getValue());
					setComboSelection(stackTraceCombo, filters[i]);
				}			
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_ID)) {
					sourceRepositoryIdText.setText(filters[i].getValue());
					setComboSelection(sourceRepositoryIdCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_KIND)) {
					sourceSystemKindText.setText(filters[i].getValue());
					setComboSelection(sourceSystemKindCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_KIND)) {
					sourceRepositoryKindText.setText(filters[i].getValue());
					setComboSelection(sourceRepositoryKindCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_SOURCE_ARTIFACT_ID)) {
					sourceArtifactIdText.setText(filters[i].getValue());
					setComboSelection(sourceArtifactIdCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_SOURCE_ARTIFACT_VERSION)) {
					sourceArtifactVersionText.setText(filters[i].getValue());
					setComboSelection(sourceArtifactVersionCombo, filters[i]);
				}		
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_ID)) {
					targetRepositoryIdText.setText(filters[i].getValue());
					setComboSelection(targetRepositoryIdCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_TARGET_SYSTEM_KIND)) {
					targetSystemKindText.setText(filters[i].getValue());
					setComboSelection(targetSystemKindCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_KIND)) {
					targetRepositoryKindText.setText(filters[i].getValue());
					setComboSelection(targetRepositoryKindCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_TARGET_ARTIFACT_ID)) {
					targetArtifactIdText.setText(filters[i].getValue());
					setComboSelection(targetArtifactIdCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_TARGET_ARTIFACT_VERSION)) {
					targetArtifactVersionText.setText(filters[i].getValue());
					setComboSelection(targetArtifactVersionCombo, filters[i]);
				}				
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_ADAPTOR_NAME)) {
					adaptorNameText.setText(filters[i].getValue());
					setComboSelection(adaptorNameCombo, filters[i]);
				}	
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_ORIGINATING_COMPONENT)) {
					originatingComponentText.setText(filters[i].getValue());
					setComboSelection(originatingComponentCombo, filters[i]);
				}	
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_DATA)) {
					dataText.setText(filters[i].getValue());
					setComboSelection(dataCombo, filters[i]);
				}	
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_DATA_TYPE)) {
					dataTypeText.setText(filters[i].getValue());
					setComboSelection(dataTypeCombo, filters[i]);
				}	
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_ARTIFACT_TYPE)) {
					artifactTypeText.setText(filters[i].getValue());
					setComboSelection(artifactTypeCombo, filters[i]);
				}	
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_GENERIC_ARTIFACT)) {
					genericArtifactText.setText(filters[i].getValue());
					setComboSelection(genericArtifactCombo, filters[i]);
				}				
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_REPROCESSED)) {
					reprocessedCombo.setText(filters[i].getValue());
				}	
				else if (filters[i].getColumnName().equals(CcfDataProvider.HOSPITAL_FIXED)) {
					fixedCombo.setText(filters[i].getValue());
				}	
			}
		}
	}
	
	private Combo createCombo(Composite composite) {
		Combo combo = new Combo(composite, SWT.READ_ONLY);
		combo.add("equals");
		combo.add("contains");
		combo.select(0);
		return combo;
	}
	
	private int getFilterType(Combo combo) {
		switch (combo.getSelectionIndex()) {
		case 0:
			return Filter.FILTER_TYPE_EQUAL;
		case 1:
			return Filter.FILTER_TYPE_LIKE;
		default:
			return Filter.FILTER_TYPE_EQUAL;
		}
	}
	
	private void setComboSelection(Combo combo, Filter filter) {
		if (Filter.FILTER_TYPE_LIKE == filter.getFilterType()) combo.select(1);
		else combo.select(0);
	}

}
