package com.collabnet.ccf.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.views.CcfExplorerView;

public class ProjectMappingFilterDialog extends CcfDialog {
	private boolean filtersActive;
	
	private Button filtersActiveButton;
	private Button hospitalEntriesButton;

	private Text sourceRepositoryIdText;
	private Combo sourceRepositoryIdCombo;
	private Text targetRepositoryIdText;
	private Combo targetRepositoryIdCombo;
	
	private IDialogSettings settings = Activator.getDefault().getDialogSettings();

	public ProjectMappingFilterDialog(Shell shell) {
		super(shell, "ProjectMappingFilterDialog");
	}

	protected Control createDialogArea(Composite parent) {
		getShell().setText("Filter Project Mappings");
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		filtersActiveButton = new Button(composite, SWT.CHECK);
		filtersActiveButton.setText("Filters active");
		GridData data = new GridData();
		data.horizontalSpan = 3;
		filtersActiveButton.setLayoutData(data);
		
		if (Activator.getDefault().getPreferenceStore().getBoolean(Activator.PREFERENCES_SHOW_HOSPITAL_COUNT)) {		
			hospitalEntriesButton = new Button(composite, SWT.CHECK);
			hospitalEntriesButton.setText("Show only project mappings with hospital entries");
			data = new GridData();
			data.horizontalSpan = 3;
			hospitalEntriesButton.setLayoutData(data);		
		}
		
		Label sourceRepositoryIdLabel = new Label(composite, SWT.NONE);
		sourceRepositoryIdLabel.setText("Source repository ID:");
		sourceRepositoryIdText = new Text(composite, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		sourceRepositoryIdText.setLayoutData(data);
		sourceRepositoryIdCombo = createCombo(composite);
		
		Label targetRepositoryIdLabel = new Label(composite, SWT.NONE);
		targetRepositoryIdLabel.setText("Target repository ID:");
		targetRepositoryIdText = new Text(composite, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		targetRepositoryIdText.setLayoutData(data);
		targetRepositoryIdCombo = createCombo(composite);
		
		initializeValues();
		
		return composite;
	}
	
	@Override
	protected void okPressed() {
		settings.put("ProjectMappingFilter.filtersActive", filtersActiveButton.getSelection());
		if (null == hospitalEntriesButton) settings.put("ProjectMappingFilter.hospitalOnly", false);
		else settings.put("ProjectMappingFilter.hospitalOnly", hospitalEntriesButton.getSelection());
		settings.put("ProjectMappingFilter.sourceRepository", sourceRepositoryIdText.getText().trim());
		settings.put("ProjectMappingFilter.sourceRepositoryCompare", sourceRepositoryIdCombo.getText());
		settings.put("ProjectMappingFilter.targetRepository", targetRepositoryIdText.getText().trim());
		settings.put("ProjectMappingFilter.targetRepositoryCompare", targetRepositoryIdCombo.getText());
		settings.put("ProjectMappingFilter.filtersSet", true);
		CcfExplorerView.getView().refreshProjectMappings();
		super.okPressed();
	}

	private Combo createCombo(Composite composite) {
		Combo combo = new Combo(composite, SWT.READ_ONLY);
		combo.add("equals");
		combo.add("contains");
		combo.select(0);
		return combo;
	}
	
	private void initializeValues() {
		boolean hospitalEntriesOnly = false;
		String sourceRepository = null;
		String sourceRepositoryCompare = null;
		String targetRepository = null;
		String targetRepositoryCompare = null;
		if (settings.getBoolean("ProjectMappingFilter.filtersSet")) {
			filtersActive = settings.getBoolean("ProjectMappingFilter.filtersActive");
			hospitalEntriesOnly = settings.getBoolean("ProjectMappingFilter.hospitalOnly");
			sourceRepository = settings.get("ProjectMappingFilter.sourceRepository");
			sourceRepositoryCompare = settings.get("ProjectMappingFilter.sourceRepositoryCompare");
			targetRepository = settings.get("ProjectMappingFilter.targetRepository");
			targetRepositoryCompare = settings.get("ProjectMappingFilter.targetRepositoryCompare");
		} else {
			filtersActive = true;
		}
		filtersActiveButton.setSelection(filtersActive);
		if (null != hospitalEntriesButton) hospitalEntriesButton.setSelection(hospitalEntriesOnly);
		if (null != sourceRepository) sourceRepositoryIdText.setText(sourceRepository);
		if (null != sourceRepositoryCompare) sourceRepositoryIdCombo.setText(sourceRepositoryCompare);
		else sourceRepositoryIdCombo.setText("equals");
		if (null != targetRepository) targetRepositoryIdText.setText(targetRepository);
		if (null != targetRepositoryCompare) targetRepositoryIdCombo.setText(targetRepositoryCompare);
		else targetRepositoryIdCombo.setText("equals");
	}
	
}
