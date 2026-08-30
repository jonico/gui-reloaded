/*******************************************************************************
 * Copyright (c) 2011 CollabNet.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     CollabNet - initial API and implementation
 ******************************************************************************/
package com.collabnet.ccf.migration.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.dialogs.CcfDialog;
import com.collabnet.ccf.dialogs.ExceptionDetailsErrorDialog;
import com.collabnet.ccf.migration.MigrationResult;

public class MigrateLandscapeResultsDialog extends CcfDialog {
	private MigrationResult[] migrationResults;
	
	private Table table;
	private TableViewer tableViewer;
	
	private String[] columnHeaders = {"Migration Result"};
	private ColumnLayoutData columnLayouts[] = {
		new ColumnWeightData(100, 100, true)};

	public MigrateLandscapeResultsDialog(Shell shell, MigrationResult[] migrationResults) {
		super(shell, "MigrateLandscapeResultsDialog");
		this.migrationResults = migrationResults;
	}
	
	protected Control createDialogArea(Composite parent) {
		getShell().setText("CCF 2.x Migration Results");
		
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));	
		
		table = new Table(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLinesVisible(false);
		table.setHeaderVisible(true);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
		gd.widthHint = 500;
		gd.heightHint = 500;
		table.setLayoutData(gd);
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new MigrationResultsContentProvider(migrationResults));
		tableViewer.setLabelProvider(new MigrationResultsLabelProvider());
		for (int i = 0; i < columnHeaders.length; i++) {
			tableLayout.addColumnData(columnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE,i);
			tc.setResizable(columnLayouts[i].resizable);
			tc.setText(columnHeaders[i]);
		}
		tableViewer.setInput(this);
		
		tableViewer.addOpenListener(new IOpenListener() {			
			public void open(OpenEvent event) {
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				if (null != selection && selection.getFirstElement() instanceof MigrationResult) {
					MigrationResult migrationResult = (MigrationResult)selection.getFirstElement();
					if (MigrationResult.ERROR == migrationResult.getResultType()) {
						if (migrationResult.getException().getMessage().contains("<html>")) {
							MigrateLandscapeErrorDialog dialog = new MigrateLandscapeErrorDialog(getShell(), migrationResult.getException());
							dialog.open();
						} else {
							ExceptionDetailsErrorDialog.openError(getShell(), "Migrate Landscape to CCF 2.x", migrationResult.getException().getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, migrationResult.getException().getLocalizedMessage(), migrationResult.getException()));
						}
					}
				}
			}
		});
		
		for (MigrationResult migrationResult : migrationResults) {
			if (MigrationResult.ERROR == migrationResult.getResultType()) {
				new Label(composite, SWT.NONE);
				Label errorLabel = new Label(composite, SWT.NONE);
				errorLabel.setText("Double click error message for details.");
				break;
			}
		}
		
		return composite;
	}
	
	class MigrationResultsContentProvider implements IStructuredContentProvider {
		private MigrationResult[] migrationResults;
		public MigrationResultsContentProvider(MigrationResult[] migrationResults) {
			super();
			this.migrationResults = migrationResults;
		}
		public void dispose() {}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		public Object[] getElements(Object inputElement) {
			if (null == migrationResults) {
				return new MigrationResult[0];
			}
			return migrationResults;
		}	
	}
	
	class MigrationResultsLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			if (0 == columnIndex) {
				if (MigrationResult.ERROR == ((MigrationResult)element).getResultType()) {
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
				}
			}
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			return ((MigrationResult)element).getResultMessage();
		}		
	}

}
