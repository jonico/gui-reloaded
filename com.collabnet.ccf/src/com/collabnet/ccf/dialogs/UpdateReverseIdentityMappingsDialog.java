package com.collabnet.ccf.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.db.Filter;
import com.collabnet.ccf.db.Update;
import com.collabnet.ccf.model.IdentityMapping;

public class UpdateReverseIdentityMappingsDialog extends CcfDialog {
	private IdentityMapping identityMapping;
	private IdentityMapping reverseIdentityMapping;
	private List<Update> updates;
	private List<Update> reverseUpdates;
	
	private IDialogSettings settings = Activator.getDefault().getDialogSettings();
	
	private TableViewer updatesTableViewer;
	private TableViewer reverseTableViewer;

	public UpdateReverseIdentityMappingsDialog(Shell shell, IdentityMapping identityMapping, IdentityMapping reverseIdentityMapping, List<Update> updates, List<Update> reverseUpdates) {
		super(shell, "UpdateReverseIdentityMappingsDialog");
		this.identityMapping = identityMapping;
		this.reverseIdentityMapping = reverseIdentityMapping;
		this.updates = updates;
		this.reverseUpdates = reverseUpdates;
	}
	
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Update Reverse Mapping");
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		if (0 < reverseUpdates.size()) {
			Group reverseUpdatesGroup = new Group(composite, SWT.NONE);
			GridLayout reverseUpdatesLayout = new GridLayout();
			reverseUpdatesLayout.numColumns = 1;
			reverseUpdatesGroup.setLayout(reverseUpdatesLayout);
			reverseUpdatesGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
			reverseUpdatesGroup.setText(reverseIdentityMapping.getEditableValue() + " updates:");
			
			Table reverseTable =	new Table(reverseUpdatesGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI | SWT.CHECK);
			reverseTable.setHeaderVisible(true);
			reverseTable.setLinesVisible(true);
			
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
			gridData.heightHint = 500;
			reverseTable.setLayoutData(gridData);
			TableLayout reverseTableLayout = new TableLayout();
			reverseTable.setLayout(reverseTableLayout);
	        
			reverseTableViewer = new TableViewer(reverseTable);
			createColumns(reverseTable, reverseTableLayout);
	
			reverseTableViewer.setContentProvider(new ArrayContentProvider());
			reverseTableViewer.setLabelProvider(new IdentityMappingLabelProvider());
			Update[] reverseUpdateArray = new Update[reverseUpdates.size()];
			reverseUpdates.toArray(reverseUpdateArray);
			reverseTableViewer.setInput(reverseUpdateArray);
			
			TableItem[] items = reverseTable.getItems();
			for (TableItem item : items) {
				item.setChecked(true);
			}
		}
		
		if (0 < updates.size()) {
			Group updatesGroup = new Group(composite, SWT.NONE);
			GridLayout updatesLayout = new GridLayout();
			updatesLayout.numColumns = 1;
			updatesGroup.setLayout(updatesLayout);
			updatesGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
			updatesGroup.setText(identityMapping.getEditableValue() + " updates:");
			
			Table updatesTable =	new Table(updatesGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI | SWT.CHECK);
			updatesTable.setHeaderVisible(true);
			updatesTable.setLinesVisible(true);
			
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
			gridData.heightHint = 500;
			updatesTable.setLayoutData(gridData);
			TableLayout updatesTableLayout = new TableLayout();
			updatesTable.setLayout(updatesTableLayout);
	        
			updatesTableViewer = new TableViewer(updatesTable);
			createColumns(updatesTable, updatesTableLayout);
	
			updatesTableViewer.setContentProvider(new ArrayContentProvider());
			updatesTableViewer.setLabelProvider(new IdentityMappingLabelProvider());
			Update[] updateArray = new Update[updates.size()];
			updates.toArray(updateArray);
			updatesTableViewer.setInput(updateArray);
			
			TableItem[] items = updatesTable.getItems();
			for (TableItem item : items) {
				item.setChecked(true);
			}
		}		
		
		return composite;
	}
	
	@Override
	protected void okPressed() {
		CcfDataProvider dataProvider = new CcfDataProvider();
		
		if (null != reverseTableViewer) {
			reverseUpdates = new ArrayList<Update>();
			TableItem[] items = reverseTableViewer.getTable().getItems();
			for (TableItem item : items) {
				if (item.getChecked()) {
					reverseUpdates.add((Update)item.getData());
				}
			}
			if (0 < reverseUpdates.size()) {			
				Filter sourceRepositoryIdFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_ID, reverseIdentityMapping.getSourceRepositoryId(), true);
				Filter targetRepositoryIdFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID, reverseIdentityMapping.getTargetRepositoryId(), true);
				Filter sourceArtifactIdFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_ARTIFACT_ID, reverseIdentityMapping.getSourceArtifactId(), true);
				Filter artifactTypeFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_ARTIFACT_TYPE, reverseIdentityMapping.getArtifactType(), true);
				Filter[] filters = { sourceRepositoryIdFilter, targetRepositoryIdFilter, sourceArtifactIdFilter, artifactTypeFilter };
				
				Update[] updates = new Update[reverseUpdates.size()];
				reverseUpdates.toArray(updates);
				
				try {
					dataProvider.updateIdentityMappings(reverseIdentityMapping.getLandscape(), updates, filters);
				} catch (Exception e) {
					Activator.handleError(e);
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Update Reverse Mapping", e.getMessage());
					return;
				}				
			}		
		}
		
		if (null != updatesTableViewer) {
			updates = new ArrayList<Update>();
			TableItem[] items = updatesTableViewer.getTable().getItems();
			for (TableItem item : items) {
				if (item.getChecked()) {
					updates.add((Update)item.getData());
				}
			}
			if (0 < updates.size()) {			
				Filter sourceRepositoryIdFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_ID, identityMapping.getSourceRepositoryId(), true);
				Filter targetRepositoryIdFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID, identityMapping.getTargetRepositoryId(), true);
				Filter sourceArtifactIdFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_ARTIFACT_ID, identityMapping.getSourceArtifactId(), true);
				Filter artifactTypeFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_ARTIFACT_TYPE, identityMapping.getArtifactType(), true);
				Filter[] filters = { sourceRepositoryIdFilter, targetRepositoryIdFilter, sourceArtifactIdFilter, artifactTypeFilter };
				
				Update[] updateArray = new Update[updates.size()];
				updates.toArray(updateArray);
				
				try {
					dataProvider.updateIdentityMappings(identityMapping.getLandscape(), updateArray, filters);
				} catch (Exception e) {
					Activator.handleError(e);
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Update Reverse Mapping", e.getMessage());
					return;
				}				
			}		
		}
		
		super.okPressed();
	}

	private void createColumns(Table table, TableLayout layout) {
		DisposeListener disposeListener = new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				TableColumn col = (TableColumn)e.getSource();
				if (0 < col.getWidth()) settings.put("UpdateReverseIdentityMappingDialog." + col.getText(), col.getWidth()); //$NON-NLS-1$
			}			
		};
	
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText("Update");
		String columnWidth = settings.get("UpdateReverseIdentityMapping." + col.getText()); //$NON-NLS-1$
		if (null == columnWidth || columnWidth.equals("0")) layout.addColumnData(new ColumnWeightData(800, true)); //$NON-NLS-1$
		else layout.addColumnData(new ColumnPixelData(Integer.parseInt(columnWidth), true));
		col.addDisposeListener(disposeListener);
	}
	
	class IdentityMappingLabelProvider implements ITableLabelProvider {
		
		public IdentityMappingLabelProvider() {
			super();
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int index) {
			Update update = (Update)element;
			switch (index) {
			case 0:
				return update.toString();		
			default:
				break;
			}
			return "";
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String columnIndex) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {		
		}
		
	}

}
