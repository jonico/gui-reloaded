package com.collabnet.ccf.preferences;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.collabnet.ccf.Activator;

public class FieldMappingsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Button graphicalMappingButton;
	private Text mapForcePathText;
	
	private IPreferenceStore store = Activator.getDefault().getPreferenceStore();
	
	public FieldMappingsPreferencePage() {
		super();
	}

	public FieldMappingsPreferencePage(String title) {
		super(title);
	}

	public FieldMappingsPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);

		graphicalMappingButton = new Button(composite, SWT.CHECK);
		graphicalMappingButton.setText("Graphical field mapping available");
		data = new GridData();
		data.horizontalSpan = 3;
		graphicalMappingButton.setLayoutData(data);
		
		Label mapForcePathLabel = new Label(composite, SWT.NONE);
		mapForcePathLabel.setText("MapForce path:");
		mapForcePathText = new Text(composite, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		mapForcePathText.setLayoutData(data);
		
		Button mapForceBrowseButton = new Button(composite, SWT.PUSH);
		mapForceBrowseButton.setText("Browse...");
		mapForceBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				FileDialog d = new FileDialog(getShell(), SWT.PRIMARY_MODAL | SWT.OPEN);
				d.setText("Select MapForce.exe file");
				d.setFileName("MapForce.exe");
				String[] filterExtensions = { "*.exe" };
				d.setFilterExtensions(filterExtensions);
				String file = d.open();
				if(null!=file) {
					IPath path = new Path(file);
					mapForcePathText.setText(path.toOSString());
				}		
			}		
		});
		
		initializeValues();
		
		return composite;
	}
	
	public boolean performOk() {
		store.setValue(Activator.PREFERENCES_GRAPHICAL_MAPPING_AVAILABLE, graphicalMappingButton.getSelection());
		store.setValue(Activator.PREFERENCES_MAPFORCE_PATH, mapForcePathText.getText().trim());
		return super.performOk();
	}
	
	protected void performDefaults() {
		graphicalMappingButton.setSelection(Activator.DEFAULT_GRAPHICAL_MAPPING_AVAILABLE);
		mapForcePathText.setText(Activator.DEFAULT_MAPFORCE_PATH);
		super.performDefaults();
	}

	public void init(IWorkbench workbench) {
	}
	
	private void initializeValues() {
		graphicalMappingButton.setSelection(store.getBoolean(Activator.PREFERENCES_GRAPHICAL_MAPPING_AVAILABLE));
		mapForcePathText.setText(store.getString(Activator.PREFERENCES_MAPFORCE_PATH));
	}

}
