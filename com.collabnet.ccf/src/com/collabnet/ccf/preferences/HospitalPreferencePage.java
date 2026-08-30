package com.collabnet.ccf.preferences;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.views.HospitalView;

public class HospitalPreferencePage extends ColumnChooserPreferencePage {
	
	public static final String ID = "com.collabnet.ccf.preferences.hospital";
	
	private Button flagOutdatedButton;
	
	public HospitalPreferencePage() {
		super();
	}

	public HospitalPreferencePage(String title) {
		super(title);
	}

	public HospitalPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public boolean performOk() {
		store.setValue(Activator.PREFERENCES_HOSPITAL_FLAG_OUTDATED, flagOutdatedButton.getSelection());
		return super.performOk();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = (Composite)super.createContents(parent);
		flagOutdatedButton = new Button(composite, SWT.CHECK);
		flagOutdatedButton.setText("Flag outdated");
		flagOutdatedButton.setSelection(store.getBoolean(Activator.PREFERENCES_HOSPITAL_FLAG_OUTDATED));
		return composite;
	}

	@Override
	public String[] getAllColumns() {
		return CcfDataProvider.HOSPITAL_COLUMNS.split("\\,");
	}

	@Override
	public void initializeCurrentValues() {
		initializeValues(store.getString(Activator.PREFERENCES_HOSPITAL_COLUMNS).split("\\,"));
	}

	@Override
	public void initializeDefaultValues() {
		initializeValues(CcfDataProvider.DEFAULT_HOSPITAL_COLUMNS.split("\\,"));
		flagOutdatedButton.setSelection(Activator.DEFAULT_HOSPITAL_FLAG_OUTDATED);
	}

	@Override
	public void refresh() {
		if (needsRefresh && null != HospitalView.getView()) {
			HospitalView.getView().refreshTableLayout();
		}
	}

	@Override
	public void setValue(String selectedColumns) {
		store.setValue(Activator.PREFERENCES_HOSPITAL_COLUMNS, selectedColumns);
	}

}
