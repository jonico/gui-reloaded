package com.collabnet.ccf.preferences;

import org.eclipse.jface.resource.ImageDescriptor;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.views.IdentityMappingView;

public class IdentityMappingPreferencePage extends ColumnChooserPreferencePage {

	public static final String ID = "com.collabnet.ccf.preferences.identityMapping";
	
	public IdentityMappingPreferencePage() {
		super();
	}

	public IdentityMappingPreferencePage(String title) {
		super(title);
	}

	public IdentityMappingPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public String[] getAllColumns() {
		return CcfDataProvider.IDENTITY_MAPPING_COLUMNS.split("\\,");
	}

	@Override
	public void initializeCurrentValues() {
		initializeValues(store.getString(Activator.PREFERENCES_IDENTITY_MAPPING_COLUMNS).split("\\,"));
	}

	@Override
	public void initializeDefaultValues() {
		initializeValues(CcfDataProvider.DEFAULT_IDENTITY_MAPPING_COLUMNS.split("\\,"));
	}

	@Override
	public void refresh() {
		if (needsRefresh && null != IdentityMappingView.getView()) {
			IdentityMappingView.getView().refreshTableLayout();
		}
	}

	@Override
	public void setValue(String selectedColumns) {
		store.setValue(Activator.PREFERENCES_IDENTITY_MAPPING_COLUMNS, selectedColumns);
	}

}
