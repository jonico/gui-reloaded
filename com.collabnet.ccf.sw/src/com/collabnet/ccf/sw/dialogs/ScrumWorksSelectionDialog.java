package com.collabnet.ccf.sw.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.collabnet.ccf.dialogs.CcfDialog;
import com.danube.scrumworks.api2.client.Product;

public class ScrumWorksSelectionDialog extends CcfDialog {
	private int type;
	
	private Table table;
	private TableViewer viewer;

	private Product[] products;
	
	private Button okButton;
	
	private String selection;
	
	private String[] columnHeaders = {"Product"};
	private ColumnLayoutData columnLayouts[] = {
		new ColumnWeightData(450, 450, true)};
	
	public static final int BROWSER_TYPE_PRODUCT = 0;

	public ScrumWorksSelectionDialog(Shell parentShell, int type) {
		super(parentShell, "ScrumWorksSelectionDialog");
		this.type = type;
	}
	
	protected Control createDialogArea(Composite parent) {
		String title;
		switch (type) {
		case BROWSER_TYPE_PRODUCT:
			title = "Select Product";
			break;
		default:
			title = "Select Product";
			break;
		}
		
		getShell().setText(title);
		
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));	
		
		table = new Table(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
		gd.widthHint = 500;
		gd.heightHint = 200;
		table.setLayoutData(gd);
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		viewer = new TableViewer(table);		
		viewer.setContentProvider(new ProductsContentProvider());
		viewer.setLabelProvider(new ProductsLabelProvider());
		for (int i = 0; i < columnHeaders.length; i++) {
			tableLayout.addColumnData(columnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE,i);
			tc.setResizable(columnLayouts[i].resizable);
			tc.setText(columnHeaders[i]);
		}
		viewer.setInput(this);
		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				if (okButton.isEnabled()) okPressed();
			}			
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (null != okButton) {
					IStructuredSelection productSelection = (IStructuredSelection)viewer.getSelection();
					okButton.setEnabled(!productSelection.isEmpty());
				}
			}		
		});
		
		return composite;
	}

	public void setProducts(Product[] products) {
		this.products = products;
	}

	protected void okPressed() {
		IStructuredSelection productSelection = (IStructuredSelection)viewer.getSelection();
		Product product = (Product)productSelection.getFirstElement();
		selection = product.getName() + "(" + product.getId() + ")";
		super.okPressed();
	}

	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        Button button = super.createButton(parent, id, label, defaultButton);
		if (IDialogConstants.OK_ID == id) {
			okButton = button;
			okButton.setEnabled(false);
		}
        return button;
    }

	public String getSelection() {
		return selection;
	}
	
	static class ProductsLabelProvider extends LabelProvider implements ITableLabelProvider {

		public String getColumnText(Object element, int columnIndex) {
			Product product = (Product)element;
			switch (columnIndex) { 
				case 0: return product.getName();
			}
			return "";  //$NON-NLS-1$
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	
	}	
	
	class ProductsContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public Object[] getElements(Object obj) {
			return products;
		}
	}	

}
