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

public class IdentityMappingFilterDialog extends CcfDialog {
	private Filter[][] filters;
	private boolean filtersActive;
	
	private Button filtersActiveButton;

	private Text sourceSystemIdText;
	private Combo sourceSystemIdCombo;
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

	private Text targetSystemIdText;
	private Combo targetSystemIdCombo;
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
	
	private Text childSourceArtifactIdText;
	private Combo childSourceArtifactIdCombo;
	private Text childSourceRepositoryIdText;
	private Combo childSourceRepositoryIdCombo;
	private Text childSourceRepositoryKindText;
	private Combo childSourceRepositoryKindCombo;	
	private Text childTargetArtifactIdText;
	private Combo childTargetArtifactIdCombo;
	private Text childTargetRepositoryIdText;
	private Combo childTargetRepositoryIdCombo;
	private Text childTargetRepositoryKindText;
	private Combo childTargetRepositoryKindCombo;	
	private Text parentSourceArtifactIdText;
	private Combo parentSourceArtifactIdCombo;
	private Text parentSourceRepositoryIdText;
	private Combo parentSourceRepositoryIdCombo;
	private Text parentSourceRepositoryKindText;
	private Combo parentSourceRepositoryKindCombo;	
	private Text parentTargetArtifactIdText;
	private Combo parentTargetArtifactIdCombo;
	private Text parentTargetRepositoryIdText;
	private Combo parentTargetRepositoryIdCombo;
	private Text parentTargetRepositoryKindText;
	private Combo parentTargetRepositoryKindCombo;

	public IdentityMappingFilterDialog(Shell shell, Filter[][] filters, boolean filtersActive) {
		super(shell, "IdentityMappingFilterDialog");
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
		
		Label sourceSystemIdLabel = new Label(sourceGroup, SWT.NONE);
		sourceSystemIdLabel.setText("System ID:");
		sourceSystemIdText = new Text(sourceGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		sourceSystemIdText.setLayoutData(data);
		sourceSystemIdCombo = createCombo(sourceGroup);

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
		
		Label targetSystemIdLabel = new Label(targetGroup, SWT.NONE);
		targetSystemIdLabel.setText("System ID:");
		targetSystemIdText = new Text(targetGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		targetSystemIdText.setLayoutData(data);
		targetSystemIdCombo = createCombo(targetGroup);

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
		
		Group childGroup = new Group(composite, SWT.NULL);
		childGroup.setText("Child filters:");
		GridLayout childLayout = new GridLayout();
		childLayout.numColumns = 3;
		childGroup.setLayout(childLayout);
		data = new GridData(GridData.FILL_BOTH);
		childGroup.setLayoutData(data);

		Label childSourceArtifactIdLabel = new Label(childGroup, SWT.NONE);
		childSourceArtifactIdLabel.setText("Source artifact ID:");
		childSourceArtifactIdText = new Text(childGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		childSourceArtifactIdText.setLayoutData(data);
		childSourceArtifactIdCombo = createCombo(childGroup);
		
		Label childSourceRepositoryIdLabel = new Label(childGroup, SWT.NONE);
		childSourceRepositoryIdLabel.setText("Source repository ID:");
		childSourceRepositoryIdText = new Text(childGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		childSourceRepositoryIdText.setLayoutData(data);
		childSourceRepositoryIdCombo = createCombo(childGroup);
		
		Label childSourceRepositoryKindLabel = new Label(childGroup, SWT.NONE);
		childSourceRepositoryKindLabel.setText("Source repository kind:");
		childSourceRepositoryKindText = new Text(childGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		childSourceRepositoryKindText.setLayoutData(data);
		childSourceRepositoryKindCombo = createCombo(childGroup);
		
		Label childTargetArtifactIdLabel = new Label(childGroup, SWT.NONE);
		childTargetArtifactIdLabel.setText("Target artifact ID:");
		childTargetArtifactIdText = new Text(childGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		childTargetArtifactIdText.setLayoutData(data);
		childTargetArtifactIdCombo = createCombo(childGroup);
		
		Label childTargetRepositoryIdLabel = new Label(childGroup, SWT.NONE);
		childTargetRepositoryIdLabel.setText("Target repository ID:");
		childTargetRepositoryIdText = new Text(childGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		childTargetRepositoryIdText.setLayoutData(data);
		childTargetRepositoryIdCombo = createCombo(childGroup);
		
		Label childTargetRepositoryKindLabel = new Label(childGroup, SWT.NONE);
		childTargetRepositoryKindLabel.setText("Target repository kind:");
		childTargetRepositoryKindText = new Text(childGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		childTargetRepositoryKindText.setLayoutData(data);
		childTargetRepositoryKindCombo = createCombo(childGroup);
		
		Group parentGroup = new Group(composite, SWT.NULL);
		parentGroup.setText("Parent filters:");
		GridLayout parentLayout = new GridLayout();
		parentLayout.numColumns = 3;
		parentGroup.setLayout(parentLayout);
		data = new GridData(GridData.FILL_BOTH);
		parentGroup.setLayoutData(data);

		Label parentSourceArtifactIdLabel = new Label(parentGroup, SWT.NONE);
		parentSourceArtifactIdLabel.setText("Source artifact ID:");
		parentSourceArtifactIdText = new Text(parentGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		parentSourceArtifactIdText.setLayoutData(data);
		parentSourceArtifactIdCombo = createCombo(parentGroup);
		
		Label parentSourceRepositoryIdLabel = new Label(parentGroup, SWT.NONE);
		parentSourceRepositoryIdLabel.setText("Source repository ID:");
		parentSourceRepositoryIdText = new Text(parentGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		parentSourceRepositoryIdText.setLayoutData(data);
		parentSourceRepositoryIdCombo = createCombo(parentGroup);
		
		Label parentSourceRepositoryKindLabel = new Label(parentGroup, SWT.NONE);
		parentSourceRepositoryKindLabel.setText("Source repository kind:");
		parentSourceRepositoryKindText = new Text(parentGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		parentSourceRepositoryKindText.setLayoutData(data);
		parentSourceRepositoryKindCombo = createCombo(parentGroup);
		
		Label parentTargetArtifactIdLabel = new Label(parentGroup, SWT.NONE);
		parentTargetArtifactIdLabel.setText("Target artifact ID:");
		parentTargetArtifactIdText = new Text(parentGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		parentTargetArtifactIdText.setLayoutData(data);
		parentTargetArtifactIdCombo = createCombo(parentGroup);
		
		Label parentTargetRepositoryIdLabel = new Label(parentGroup, SWT.NONE);
		parentTargetRepositoryIdLabel.setText("Target repository ID:");
		parentTargetRepositoryIdText = new Text(parentGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		parentTargetRepositoryIdText.setLayoutData(data);
		parentTargetRepositoryIdCombo = createCombo(parentGroup);
		
		Label parentTargetRepositoryKindLabel = new Label(parentGroup, SWT.NONE);
		parentTargetRepositoryKindLabel.setText("Target repository kind:");
		parentTargetRepositoryKindText = new Text(parentGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		parentTargetRepositoryKindText.setLayoutData(data);
		parentTargetRepositoryKindCombo = createCombo(parentGroup);
		
		initializeValues();
		
		return composite;
	}
	
	@Override
	protected void okPressed() {
		IDialogSettings settings = getSettings();
		settings.put(Filter.IDENTITY_MAPPING_FILTERS_SET, true);
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_SOURCE_SYSTEM_ID, (String)null);
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_ID, (String)null);
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_TARGET_SYSTEM_ID, (String)null);
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID, (String)null);
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_SOURCE_SYSTEM_KIND, (String)null);
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_KIND, (String)null);
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_TARGET_SYSTEM_KIND, (String)null);
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_KIND, (String)null);	
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_SOURCE_ARTIFACT_ID, (String)null);		
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_TARGET_ARTIFACT_ID, (String)null);	
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_SOURCE_ARTIFACT_VERSION, (String)null);		
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_TARGET_ARTIFACT_VERSION, (String)null);
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_ARTIFACT_TYPE, (String)null);	
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_SOURCE_ARTIFACT_ID, (String)null);			
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_SOURCE_REPOSITORY_ID, (String)null);
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_SOURCE_REPOSITORY_KIND, (String)null);		
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_TARGET_ARTIFACT_ID, (String)null);			
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_TARGET_REPOSITORY_ID, (String)null);
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_TARGET_REPOSITORY_KIND, (String)null);			
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_SOURCE_ARTIFACT_ID, (String)null);			
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_SOURCE_REPOSITORY_ID, (String)null);
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_SOURCE_REPOSITORY_KIND, (String)null);		
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_TARGET_ARTIFACT_ID, (String)null);			
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_TARGET_REPOSITORY_ID, (String)null);
		settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_TARGET_REPOSITORY_KIND, (String)null);			
		filtersActive = filtersActiveButton.getSelection();
		settings.put(Filter.IDENTITY_MAPPING_FILTERS_ACTIVE, filtersActive);
		List<Filter> filterList = new ArrayList<Filter>();
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_SOURCE_SYSTEM_ID, true, sourceSystemIdText, sourceSystemIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_ID, true, sourceRepositoryIdText, sourceRepositoryIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_SOURCE_SYSTEM_KIND, true, sourceSystemKindText, sourceSystemKindCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_KIND, true, sourceRepositoryKindText, sourceRepositoryKindCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_SOURCE_ARTIFACT_ID, true, sourceArtifactIdText, sourceArtifactIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_SOURCE_ARTIFACT_VERSION, true, sourceArtifactVersionText, sourceArtifactVersionCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_TARGET_SYSTEM_ID, true, targetSystemIdText, targetSystemIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID, true, targetRepositoryIdText, targetRepositoryIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_TARGET_SYSTEM_KIND, true, targetSystemKindText, targetSystemKindCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_KIND, true, targetRepositoryKindText, targetRepositoryKindCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_TARGET_ARTIFACT_ID, true, targetArtifactIdText, targetArtifactIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_TARGET_ARTIFACT_VERSION, true, targetArtifactVersionText, targetArtifactVersionCombo, settings);		
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_SOURCE_ARTIFACT_ID, true, childSourceArtifactIdText, childSourceArtifactIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_SOURCE_REPOSITORY_ID, true, childSourceRepositoryIdText, childSourceRepositoryIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_SOURCE_REPOSITORY_KIND, true, childSourceRepositoryKindText, childSourceRepositoryKindCombo, settings);		
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_TARGET_ARTIFACT_ID, true, childTargetArtifactIdText, childTargetArtifactIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_TARGET_REPOSITORY_ID, true, childTargetRepositoryIdText, childTargetRepositoryIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_TARGET_REPOSITORY_KIND, true, childTargetRepositoryKindText, childTargetRepositoryKindCombo, settings);	
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_SOURCE_ARTIFACT_ID, true, childSourceArtifactIdText, childSourceArtifactIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_SOURCE_REPOSITORY_ID, true, childSourceRepositoryIdText, childSourceRepositoryIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_SOURCE_REPOSITORY_KIND, true, childSourceRepositoryKindText, childSourceRepositoryKindCombo, settings);		
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_TARGET_ARTIFACT_ID, true, childTargetArtifactIdText, childTargetArtifactIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_TARGET_REPOSITORY_ID, true, childTargetRepositoryIdText, childTargetRepositoryIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_TARGET_REPOSITORY_KIND, true, childTargetRepositoryKindText, childTargetRepositoryKindCombo, settings);		
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_SOURCE_ARTIFACT_ID, true, parentSourceArtifactIdText, parentSourceArtifactIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_SOURCE_REPOSITORY_ID, true, parentSourceRepositoryIdText, parentSourceRepositoryIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_SOURCE_REPOSITORY_KIND, true, parentSourceRepositoryKindText, parentSourceRepositoryKindCombo, settings);		
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_TARGET_ARTIFACT_ID, true, parentTargetArtifactIdText, parentTargetArtifactIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_TARGET_REPOSITORY_ID, true, parentTargetRepositoryIdText, parentTargetRepositoryIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_TARGET_REPOSITORY_KIND, true, parentTargetRepositoryKindText, parentTargetRepositoryKindCombo, settings);	
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_SOURCE_ARTIFACT_ID, true, parentSourceArtifactIdText, parentSourceArtifactIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_SOURCE_REPOSITORY_ID, true, parentSourceRepositoryIdText, parentSourceRepositoryIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_SOURCE_REPOSITORY_KIND, true, parentSourceRepositoryKindText, parentSourceRepositoryKindCombo, settings);		
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_TARGET_ARTIFACT_ID, true, parentTargetArtifactIdText, parentTargetArtifactIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_TARGET_REPOSITORY_ID, true, parentTargetRepositoryIdText, parentTargetRepositoryIdCombo, settings);
		updateFilterList(filterList, CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_TARGET_REPOSITORY_KIND, true, parentTargetRepositoryKindText, parentTargetRepositoryKindCombo, settings);	
		
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
			settings.put(Filter.IDENTITY_MAPPING_FILTER_VALUE + columnName, filter.getValue());
			settings.put(Filter.IDENTITY_MAPPING_FILTER_TYPE + columnName, filter.getFilterType());
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
				if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_SOURCE_SYSTEM_ID)) {
					sourceSystemIdText.setText(filters[i].getValue());
					setComboSelection(sourceSystemIdCombo, filters[i]);
				}
			    else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_ID)) {
					sourceRepositoryIdText.setText(filters[i].getValue());
					setComboSelection(sourceRepositoryIdCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_SOURCE_SYSTEM_KIND)) {
					sourceSystemKindText.setText(filters[i].getValue());
					setComboSelection(sourceSystemKindCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_KIND)) {
					sourceRepositoryKindText.setText(filters[i].getValue());
					setComboSelection(sourceRepositoryKindCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_SOURCE_ARTIFACT_ID)) {
					sourceArtifactIdText.setText(filters[i].getValue());
					setComboSelection(sourceArtifactIdCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_SOURCE_ARTIFACT_VERSION)) {
					sourceArtifactVersionText.setText(filters[i].getValue());
					setComboSelection(sourceArtifactVersionCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_TARGET_SYSTEM_ID)) {
					targetSystemIdText.setText(filters[i].getValue());
					setComboSelection(targetSystemIdCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID)) {
					targetRepositoryIdText.setText(filters[i].getValue());
					setComboSelection(targetRepositoryIdCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_TARGET_SYSTEM_KIND)) {
					targetSystemKindText.setText(filters[i].getValue());
					setComboSelection(targetSystemKindCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_KIND)) {
					targetRepositoryKindText.setText(filters[i].getValue());
					setComboSelection(targetRepositoryKindCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_TARGET_ARTIFACT_ID)) {
					targetArtifactIdText.setText(filters[i].getValue());
					setComboSelection(targetArtifactIdCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_TARGET_ARTIFACT_VERSION)) {
					targetArtifactVersionText.setText(filters[i].getValue());
					setComboSelection(targetArtifactVersionCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_SOURCE_ARTIFACT_ID)) {
					childSourceArtifactIdText.setText(filters[i].getValue());
					setComboSelection(childSourceArtifactIdCombo, filters[i]);
				}	
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_SOURCE_REPOSITORY_ID)) {
					childSourceRepositoryIdText.setText(filters[i].getValue());
					setComboSelection(childSourceRepositoryIdCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_SOURCE_REPOSITORY_KIND)) {
					childSourceRepositoryKindText.setText(filters[i].getValue());
					setComboSelection(childSourceRepositoryKindCombo, filters[i]);
				}				
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_TARGET_ARTIFACT_ID)) {
					childTargetArtifactIdText.setText(filters[i].getValue());
					setComboSelection(childTargetArtifactIdCombo, filters[i]);
				}	
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_TARGET_REPOSITORY_ID)) {
					childTargetRepositoryIdText.setText(filters[i].getValue());
					setComboSelection(childTargetRepositoryIdCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_DEP_CHILD_TARGET_REPOSITORY_KIND)) {
					childTargetRepositoryKindText.setText(filters[i].getValue());
					setComboSelection(childTargetRepositoryKindCombo, filters[i]);
				}			
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_SOURCE_ARTIFACT_ID)) {
					parentSourceArtifactIdText.setText(filters[i].getValue());
					setComboSelection(parentSourceArtifactIdCombo, filters[i]);
				}	
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_SOURCE_REPOSITORY_ID)) {
					parentSourceRepositoryIdText.setText(filters[i].getValue());
					setComboSelection(parentSourceRepositoryIdCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_SOURCE_REPOSITORY_KIND)) {
					parentSourceRepositoryKindText.setText(filters[i].getValue());
					setComboSelection(parentSourceRepositoryKindCombo, filters[i]);
				}				
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_TARGET_ARTIFACT_ID)) {
					parentTargetArtifactIdText.setText(filters[i].getValue());
					setComboSelection(parentTargetArtifactIdCombo, filters[i]);
				}	
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_TARGET_REPOSITORY_ID)) {
					parentTargetRepositoryIdText.setText(filters[i].getValue());
					setComboSelection(parentTargetRepositoryIdCombo, filters[i]);
				}
				else if (filters[i].getColumnName().equals(CcfDataProvider.IDENTITY_MAPPING_DEP_PARENT_TARGET_REPOSITORY_KIND)) {
					parentTargetRepositoryKindText.setText(filters[i].getValue());
					setComboSelection(parentTargetRepositoryKindCombo, filters[i]);
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
