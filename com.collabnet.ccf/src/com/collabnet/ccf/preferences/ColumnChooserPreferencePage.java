package com.collabnet.ccf.preferences;

import java.util.ArrayList;
import java.util.Iterator;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.collabnet.ccf.Activator;

public abstract class ColumnChooserPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	protected IPreferenceStore store = Activator.getDefault().getPreferenceStore();
	
	protected List availableColumnsList;
	protected List selectedColumnsList;
	protected Button addButton;
	protected Button removeButton;
	protected Button upButton;
	protected Button downButton;
	
	protected boolean needsRefresh;
	
	protected java.util.List<String> availableColumns;
	protected java.util.List<String> selectedColumns;
	
	public ColumnChooserPreferencePage() {
		super();
	}

	public ColumnChooserPreferencePage(String title) {
		super(title);
	}

	public ColumnChooserPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gd);
		
		Group columnsGroup = new Group(composite, SWT.NULL);
		GridLayout columnsLayout = new GridLayout();
		columnsLayout.numColumns = 4;
		columnsGroup.setLayout(columnsLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		columnsGroup.setLayoutData(gd);	
		columnsGroup.setText("Columns:");
		
		Label availableLabel = new Label(columnsGroup, SWT.NONE);
		availableLabel.setText("Available:");
		
		new Label(columnsGroup, SWT.NONE);
		
		Label selectedLabel = new Label(columnsGroup, SWT.NONE);
		selectedLabel.setText("Selected:");

		new Label(columnsGroup, SWT.NONE);
		
		availableColumnsList = new List(columnsGroup, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData();
		gd.widthHint = 200;
		gd.heightHint = 300;
		availableColumnsList.setLayoutData(gd);
		
		availableColumnsList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				addButton.setEnabled(availableColumnsList.getSelectionCount() > 0);
			}			
		});
		
		Composite addRemoveGroup = new Composite(columnsGroup, SWT.NULL);
		GridLayout addRemoveLayout = new GridLayout();
		addRemoveLayout.numColumns = 1;
		addRemoveGroup.setLayout(addRemoveLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		addRemoveGroup.setLayoutData(gd);
		
		addButton = new Button(addRemoveGroup, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		addButton.setLayoutData(gd);
		addButton.setText("Add>>");
		addButton.setEnabled(false);
		
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				String[] selectedItems = availableColumnsList.getSelection();
				for (int i = 0; i < selectedItems.length; i++) {
					selectedColumns.add(selectedItems[i]);
					availableColumns.remove(selectedItems[i]);
				}
				String[] selectedColumnsArray = new String[selectedColumns.size()];
				selectedColumns.toArray(selectedColumnsArray);
				initializeValues(selectedColumnsArray);
				addButton.setEnabled(false);
				needsRefresh = true;
			}			
		});
		
		removeButton = new Button(addRemoveGroup, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		removeButton.setLayoutData(gd);
		removeButton.setText("<<Remove");
		removeButton.setEnabled(false);
		
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				String[] selectedItems = selectedColumnsList.getSelection();
				for (int i = 0; i < selectedItems.length; i++) {
					selectedColumns.remove(selectedItems[i]);
					availableColumns.add(selectedItems[i]);
				}
				String[] selectedColumnsArray = new String[selectedColumns.size()];
				selectedColumns.toArray(selectedColumnsArray);
				initializeValues(selectedColumnsArray);
				removeButton.setEnabled(false);
				needsRefresh = true;
			}			
		});
		
		selectedColumnsList = new List(columnsGroup, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData();
		gd.widthHint = 200;
		gd.heightHint = 300;
		selectedColumnsList.setLayoutData(gd);	

		selectedColumnsList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				removeButton.setEnabled(selectedColumnsList.getSelectionCount() > 0 && selectedColumnsList.getSelectionCount() < selectedColumnsList.getItemCount());
				upButton.setEnabled(isUpButtonEnabled());
				downButton.setEnabled(isDownButtonEnabled());
			}			
		});
		
		Composite upDownGroup = new Composite(columnsGroup, SWT.NULL);
		GridLayout upDownLayout = new GridLayout();
		upDownLayout.numColumns = 1;
		upDownGroup.setLayout(upDownLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		upDownGroup.setLayoutData(gd);
		
		upButton = new Button(upDownGroup, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		upButton.setLayoutData(gd);
		upButton.setText("Up");
		upButton.setEnabled(false);
		
		upButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				String[] allItems = selectedColumnsList.getItems();
				String[] selectedItems = selectedColumnsList.getSelection();
				int[] selectedIndices = selectedColumnsList.getSelectionIndices();
				selectedColumnsList.setSelection(selectedItems);
				
				for (int i = 0; i < selectedIndices.length; i++) {
					selectedColumnsList.remove(selectedIndices[i]);
					selectedColumnsList.add(allItems[selectedIndices[i]], selectedIndices[i] - 1);
				}			
				selectedColumnsList.setSelection(selectedItems);				
				needsRefresh = true;
				upButton.setEnabled(isUpButtonEnabled());
				downButton.setEnabled(isDownButtonEnabled());
				refreshSelectedColumns();
			}			
		});
		
		downButton = new Button(upDownGroup, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		downButton.setLayoutData(gd);
		downButton.setText("Down");
		downButton.setEnabled(false);
		
		downButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				String[] allItems = selectedColumnsList.getItems();
				String[] selectedItems = selectedColumnsList.getSelection();
				int[] selectedIndices = selectedColumnsList.getSelectionIndices();
				selectedColumnsList.setSelection(selectedItems);				
				for (int i = selectedIndices.length - 1; i >= 0; i--) {
					selectedColumnsList.remove(selectedIndices[i]);
					selectedColumnsList.add(allItems[selectedIndices[i]], selectedIndices[i] + 1);
				}			
				selectedColumnsList.setSelection(selectedItems);				
				needsRefresh = true;
				upButton.setEnabled(isUpButtonEnabled());
				downButton.setEnabled(isDownButtonEnabled());
				refreshSelectedColumns();
			}			
		});

		initializeCurrentValues();
		
		return composite;
	}
	
	public void init(IWorkbench workbench) {
	}

	public boolean performOk() {
		String[] selectedItems = selectedColumnsList.getItems();
		StringBuffer selectedColumns = new StringBuffer();
		for (int i = 0; i < selectedItems.length; i++) {
			if (0 < i) selectedColumns.append(",");
			selectedColumns.append(selectedItems[i]);
		}
		setValue(selectedColumns.toString());
		refresh();
		
		return super.performOk();
	}
	
	protected void performDefaults() {
		needsRefresh = true;
		selectedColumns = null;
		initializeDefaultValues();
		super.performDefaults();
	}
	
	public void initializeValues(String[] selectedColumnsArray) {
		selectedColumnsList.removeAll();
		availableColumnsList.removeAll();
		if (null == selectedColumns) {
			selectedColumns = new ArrayList<String>();
			availableColumns = new ArrayList<String>();		
			String[] allColumnsArray = getAllColumns();
			for (int i = 0; i < selectedColumnsArray.length; i++) {
				selectedColumns.add(selectedColumnsArray[i]);
			}
			for (int i = 0; i < allColumnsArray.length; i++) {
				if (!selectedColumns.contains(allColumnsArray[i])) availableColumns.add(allColumnsArray[i]);
			}
		}
		Iterator<String> iter = availableColumns.iterator();
		while (iter.hasNext()) {
			availableColumnsList.add(iter.next());
		}		
		iter = selectedColumns.iterator();
		while (iter.hasNext()) {
			selectedColumnsList.add(iter.next());
		}
	}
	
	private void refreshSelectedColumns() {
		selectedColumns = new ArrayList<String>();
		String[] selectedItems = selectedColumnsList.getItems();
		for (int i = 0; i < selectedItems.length; i++) {
			selectedColumns.add(selectedItems[i]);
		}
	}
	
	private boolean isUpButtonEnabled() {
		int[] selectedRows = selectedColumnsList.getSelectionIndices();
		if (0 == selectedRows.length || 0 == selectedRows[0]) return false;
		else return true;
	}
	
	private boolean isDownButtonEnabled() {
		int[] selectedRows = selectedColumnsList.getSelectionIndices();
		if (0 == selectedRows.length || selectedRows[selectedColumnsList.getSelectionCount() - 1] == selectedColumnsList.getItemCount() - 1)
			return false;
		else
			return true;
	}

	public abstract String[] getAllColumns();
	public abstract void initializeCurrentValues();
	public abstract void initializeDefaultValues();
	public abstract void setValue(String selectedColumns);
	public abstract void refresh();

}
