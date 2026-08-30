package com.collabnet.ccf.wizards;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.dialogs.ExceptionDetailsErrorDialog;
import com.collabnet.ccf.model.SynchronizationStatus;

public class EditFieldMappingsWizardMainPage extends WizardPage {
	private SynchronizationStatus projectMapping;
	private File graphicalXslFile;
	private File xslFile;
	private File mapForceFile;
	
	private Button mapForceEditButton;
	private Button switchToGraphicalButton;
	private Button generateButton;
	private Label mapForcePathLabel;
	private Text mapForcePathText;
	private Button mapForceBrowseButton;
	private Button editButton;
	private Button switchToNonGraphicalButton;
	private Button copySampleButton;
	private Button switchOnlyButton;
	
	private Button restoreDefaultMappingButton;
	
	private final static String MAP_FORCE_URL = "http://www.altova.com/products/mapforce/data_mapping.html";

	public EditFieldMappingsWizardMainPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	public void createControl(Composite parent) {
		projectMapping = ((EditFieldMappingsWizard)getWizard()).getProjectMapping();
		
		Composite outerContainer = new Composite(parent,SWT.NONE);
		outerContainer.setLayout(new GridLayout());
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

		Composite buttonGroup = new Composite(outerContainer, SWT.NONE);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 2;
		buttonGroup.setLayout(buttonLayout);
		buttonGroup.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		graphicalXslFile = projectMapping.getMappingFile(projectMapping.getGraphicalXslFileName());	
		xslFile = projectMapping.getXslFile();
		mapForceFile = getMapForceFile();
		
		mapForceEditButton = new Button(buttonGroup, SWT.RADIO);
		mapForceEditButton.setText("Map fields graphically");
		GridData data = new GridData();
		if (null != mapForceFile && mapForceFile.exists()) {
			data.horizontalSpan = 2;
		} else {
			data.horizontalSpan = 1;
		}
		mapForceEditButton.setLayoutData(data);
		
		if (null == mapForceFile || !mapForceFile.exists()) {
			Composite mapForceGroup = new Composite(buttonGroup, SWT.NONE);
			GridLayout mapForceLayout = new GridLayout();
			mapForceLayout.numColumns = 3;
			mapForceLayout.horizontalSpacing = 0;
			mapForceGroup.setLayout(mapForceLayout);
			mapForceGroup.setLayoutData(
			new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
			
			Label requiresLabel1 = new Label(mapForceGroup, SWT.NONE);
			requiresLabel1.setText("(Requires ");
			
			FormToolkit toolkit = new FormToolkit(mapForceGroup.getDisplay());
			toolkit.setBackground(mapForceGroup.getBackground());
			Hyperlink mapForceLink = toolkit.createHyperlink(mapForceGroup, "Altova MapForce Standard Edition", SWT.NONE);
			mapForceLink.setHref(MAP_FORCE_URL);
			mapForceLink.setToolTipText(MAP_FORCE_URL);	
			
			IHyperlinkListener linkListener = new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent evt) {
					Hyperlink link = (Hyperlink)evt.getSource();
					try {
						URL url = new URL(link.getHref().toString());
						PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
					} catch (Exception e) {
						Activator.handleError("Edit Field Mappings", e);
						ExceptionDetailsErrorDialog.openError(getShell(), "Edit Field Mappings", e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
					}
				}
	        };
	        
	        mapForceLink.addHyperlinkListener(linkListener);
	        
			Label requiresLabel2 = new Label(mapForceGroup, SWT.NONE);
			requiresLabel2.setText(")");
		}
		
		if (!projectMapping.usesGraphicalMapping()) {
			switchToGraphicalButton = new Button(buttonGroup, SWT.CHECK);
			switchToGraphicalButton.setText("Switch to graphical mapping");
			data = new GridData();
			data.horizontalIndent = 10;
			data.horizontalSpan = 2;
			switchToGraphicalButton.setLayoutData(data);
		}
		
		generateButton = new Button(buttonGroup, SWT.CHECK);
		data = new GridData();
		data.horizontalIndent = 10;
		data.horizontalSpan = 2;
		generateButton.setLayoutData(data);
		
		Composite pathGroup = new Composite(buttonGroup, SWT.NONE);
		GridLayout pathLayout = new GridLayout();
		pathLayout.numColumns = 3;
		pathGroup.setLayout(pathLayout);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalIndent = 10;
		data.horizontalSpan = 2;
		pathGroup.setLayoutData(data);
		
		mapForcePathLabel = new Label(pathGroup, SWT.NONE);
		mapForcePathLabel.setText("MapForce path:");
		mapForcePathText = new Text(pathGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		mapForcePathText.setLayoutData(data);
		if (null != mapForceFile && mapForceFile.exists()) {
			mapForcePathText.setText(mapForceFile.getAbsolutePath());
		}
		mapForcePathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent me) {
				if (0 == mapForcePathText.getText().trim().length()) mapForceFile = null;
				else mapForceFile = new File(mapForcePathText.getText().trim());
				setMapForcePath();
				setPageComplete(canFinish());
			}			
		});
		mapForceBrowseButton = new Button(pathGroup, SWT.PUSH);
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
					mapForceFile = new File(file);
					IPath path = new Path(file);
					mapForcePathText.setText(path.toOSString());
					setMapForcePath();
					setPageComplete(canFinish());
				}		
			}		
		});
		
		editButton = new Button(buttonGroup, SWT.RADIO);
		editButton.setText("Edit using default editor");
		data = new GridData();
		data.horizontalSpan = 2;
		editButton.setLayoutData(data);
		
		if (projectMapping.usesGraphicalMapping()) {
			switchToNonGraphicalButton = new Button(buttonGroup, SWT.CHECK);
			switchToNonGraphicalButton.setText("Switch to non-graphical mapping");
			data = new GridData();
			data.horizontalIndent = 10;
			data.horizontalSpan = 2;
			switchToNonGraphicalButton.setLayoutData(data);
		}
		
		if (!xslFile.exists()) {
			copySampleButton = new Button(buttonGroup, SWT.CHECK);
			copySampleButton.setText("Create " + xslFile.getName() + " by copying sample.xsl");
			data = new GridData();
			data.horizontalIndent = 10;
			data.horizontalSpan = 2;
			copySampleButton.setLayoutData(data);
			copySampleButton.setSelection(true);
			copySampleButton.setEnabled(false);
		}

		if (graphicalXslFile.exists()) {
			generateButton.setText("Regenerate schema");
		} else {
			generateButton.setText("Generate schema");
			generateButton.setSelection(true);
			generateButton.setEnabled(false);
		}
		
		switchOnlyButton = new Button(buttonGroup, SWT.RADIO);
		if (projectMapping.usesGraphicalMapping()) {
			switchOnlyButton.setText("Switch to non-graphical mapping");
			switchOnlyButton.setEnabled(xslFile.exists());
		} else {
			switchOnlyButton.setText("Switch to graphical mapping");
			switchOnlyButton.setEnabled(graphicalXslFile.exists());
		}
		data = new GridData();
		data.horizontalSpan = 2;
		switchOnlyButton.setLayoutData(data);
		
		if (projectMapping.usesGraphicalMapping()) {
			mapForceEditButton.setSelection(true);
		} else {
			editButton.setSelection(true);
		}
		
		Composite restoreGroup = new Composite(outerContainer, SWT.NONE);
		GridLayout restoreLayout = new GridLayout();
		restoreLayout.numColumns = 1;
		restoreGroup.setLayout(restoreLayout);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		data.verticalIndent = 10;
		data.horizontalIndent = 0;
		restoreGroup.setLayoutData(data);
		
		restoreDefaultMappingButton = new Button(restoreGroup, SWT.CHECK);
		data = new GridData();
		data.horizontalIndent = 0;
		restoreDefaultMappingButton.setLayoutData(data);
		restoreDefaultMappingButton.setText("Restore default mapping");
		
		SelectionListener selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				setEnablement();
				setPageComplete(canFinish());
			}
		};
		
		mapForceEditButton.addSelectionListener(selectionListener);
		generateButton.addSelectionListener(selectionListener);
		editButton.addSelectionListener(selectionListener);
		switchOnlyButton.addSelectionListener(selectionListener);
		restoreDefaultMappingButton.addSelectionListener(selectionListener);
		if (null != copySampleButton) copySampleButton.addSelectionListener(selectionListener);
		if (null != switchToGraphicalButton) switchToGraphicalButton.addSelectionListener(selectionListener);
		if (null != switchToNonGraphicalButton) switchToNonGraphicalButton.addSelectionListener(selectionListener);
		
		setMessage("Specify your edit choices");
		
		setEnablement();
		setPageComplete(canFinish());

		setControl(outerContainer);
	}
	
	private void setEnablement() {
		generateButton.setEnabled(mapForceEditButton.getSelection() && graphicalXslFile.exists() && (restoreDefaultMappingButton == null || !restoreDefaultMappingButton.getSelection()));
		if (null != copySampleButton) {
			copySampleButton.setEnabled(editButton.getSelection());
		}
		mapForcePathLabel.setEnabled(mapForceEditButton.getSelection());
		mapForcePathText.setEnabled(mapForceEditButton.getSelection());
		mapForceBrowseButton.setEnabled(mapForceEditButton.getSelection());
		if (null != switchToGraphicalButton) {
			switchToGraphicalButton.setEnabled(mapForceEditButton.getSelection());
		}
		if (null != switchToNonGraphicalButton) {
			switchToNonGraphicalButton.setEnabled(editButton.getSelection());
		}
		if (null != restoreDefaultMappingButton) {
			restoreDefaultMappingButton.setEnabled(mapForceEditButton.getSelection() || editButton.getSelection());
			if (restoreDefaultMappingButton.getSelection() && mapForceEditButton.getSelection()) {
				generateButton.setSelection(true);
			}
		}
	}		

	public boolean isGenerate() {
		return generateButton.getSelection();
	}
	
	public boolean isMapForceEdit() {
		return mapForceEditButton.getSelection();
	}
	
	public boolean isEdit() {
		return editButton.getSelection();
	}
	
	public boolean isSwitchToGraphicalMapping() {
		return switchToGraphicalButton != null && switchToGraphicalButton.getSelection();
	}
	
	public boolean isSwitchToNonGraphicalMapping() {
		return switchToNonGraphicalButton != null && switchToNonGraphicalButton.getSelection();
	}
	
	public boolean isSwitchOnly() {
		return switchOnlyButton.getSelection();
	}
	
	public boolean isRestoreDefaultMapping() {
		return restoreDefaultMappingButton != null && restoreDefaultMappingButton.getSelection();
	}
	
	public String getMapForcePath() {
		return mapForcePathText.getText().trim();
	}
	
	private boolean canFinish() {
		setErrorMessage(null);
		if (mapForceEditButton.getSelection()) {
			if (!graphicalXslFile.exists() && !generateButton.getSelection()) {
				return false;
			}
			if (null == mapForceFile || !mapForceFile.exists()) {
				setErrorMessage("Altova MapForce not found");
				return false;
			}
		}
		if (editButton.getSelection() && !xslFile.exists() && !copySampleButton.getSelection())	{
			setErrorMessage(xslFile.getName() + " does not exist");
			return false;
		}
		return true;
	}
	
	private File getMapForceFile() {
		String mapForcePath = Activator.getDefault().getPreferenceStore().getString(Activator.PREFERENCES_MAPFORCE_PATH);
		if (null != mapForcePath && 0 < mapForcePath.trim().length()) {
			mapForceFile = new File(mapForcePath);
		}
		return mapForceFile;
	}
	
	private void setMapForcePath() {
		if (null != mapForceFile && mapForceFile.exists()) {
			Activator.getDefault().getPreferenceStore().setValue(Activator.PREFERENCES_MAPFORCE_PATH, mapForcePathText.getText().trim());
		}
	}

}
