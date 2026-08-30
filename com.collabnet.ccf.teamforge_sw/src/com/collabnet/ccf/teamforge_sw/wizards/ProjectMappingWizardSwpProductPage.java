package com.collabnet.ccf.teamforge_sw.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.danube.scrumworks.api2.client.Product;

public class ProjectMappingWizardSwpProductPage extends WizardPage {
	private Button mapUsersButton;
	private List<Product> products;
	private Table table;
	private TableViewer viewer;
	private Product selectedProduct;
	private boolean productsRetrieved;
	private Exception getProductsError;
	private boolean mapUsers = false;
	private boolean mapMultiple;
	
	private List<SynchronizationStatus> existingMappings;
	
	private String[] columnHeaders = {"Product"};
	private ColumnLayoutData columnLayouts[] = {
		new ColumnWeightData(450, 450, true)};

	public ProjectMappingWizardSwpProductPage() {
		super("productPage", "ScrumWorks Product", Activator.getDefault().getImageDescriptor(Activator.IMAGE_NEW_PROJECT_MAPPING_WIZBAN));
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		setMessage("Select the ScrumWorks product to be mapped.");
		Composite outerContainer = new Composite(parent,SWT.NONE);
		outerContainer.setLayout(new GridLayout());
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		mapUsersButton = new Button(outerContainer, SWT.CHECK);
		mapUsersButton.setText("Create ScrumWorks product users in TeamForge");
		mapUsersButton.addSelectionListener(new SelectionAdapter() {			
			public void widgetSelected(SelectionEvent e) {
				mapUsers = mapUsersButton.getSelection();
			}
		});

		table = new Table(outerContainer, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
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
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete(canFinish());
			}		
		});
		viewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof Product && e2 instanceof Product) {
					Product p1 = (Product)e1;
					Product p2 = (Product)e2;
					return p1.getName().toLowerCase().compareTo(p2.getName().toLowerCase());
				}
				return super.compare(viewer, e1, e2);
			}		
		});

		setControl(outerContainer);
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible && null == products && !productsRetrieved) {
			productsRetrieved = true;
			getProducts();
			viewer.refresh();
		}
	}
	
	public boolean isMapUsers() {
		return mapUsers;
	}

	public Product getSelectedProduct() {
		return selectedProduct;
	}
	
	private boolean canFinish() {
		setErrorMessage(null);
		IStructuredSelection productSelection = (IStructuredSelection)viewer.getSelection();
		selectedProduct = (Product)productSelection.getFirstElement();
		if (productSelection.isEmpty()) {
			return false;
		}
		if (!mapMultiple) {
			if (isProductAlreadyMapped(selectedProduct)) {
				setErrorMessage(selectedProduct.getName() + " is already mapped to TeamForge.");
				return false;				
			}
		}
		return true;
	}
	
	public boolean isProductAlreadyMapped(Product product) {
		if (null != existingMappings) {
			String productNameAndId = product.getName() + "(" + product.getId() + ")";
			for (SynchronizationStatus status : existingMappings) {
				if (status.getSourceRepositoryId().startsWith(productNameAndId + "-") || status.getTargetRepositoryId().startsWith(productNameAndId + "-")) {
					return true;
				}
			}
		}
		return false;
	}

	private List<Product> getProducts() {
		getProductsError = null;
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				String taskName = "Retrieving ScrumWorks products";
				monitor.setTaskName(taskName);
				monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
				monitor.subTask("");
				try {
					IPreferenceStore store = com.collabnet.ccf.teamforge_sw.Activator.getDefault().getPreferenceStore();
					mapMultiple = store.getBoolean(com.collabnet.ccf.teamforge_sw.Activator.PREFERENCES_MAP_MULTIPLE);
					if (!mapMultiple) {
						existingMappings = ((ProjectMappingWizard)getWizard()).getExistingMappings();
					}
					products = ((ProjectMappingWizard)getWizard()).getProducts();
				} catch (Exception e) {
					Activator.handleError(e);
					getProductsError = e;
				}				
				monitor.done();
			}		
		};
		
		try {
			getContainer().run(true, false, runnable);
		} catch (Exception e) {
			Activator.handleError(e);
			getProductsError = e;
		}
		
		if (null != getProductsError) {
			setErrorMessage("An unexpected error occurred while getting SWP products.  See error log for details.");
		}
		
		return products;
	}
	
	class ProductsLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {

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

		public Color getBackground(Object element, int columnIndex) {
			return null;
		}

		public Color getForeground(Object element, int columnIndex) {
			Product product = (Product)element;
			if (isProductAlreadyMapped(product)) {
				return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
			}
			return null;
		}
	
	}	
	
	class ProductsContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public Object[] getElements(Object obj) {
			if (null == products) {
				return new Product[0];
			}
			Product[] productArray = new Product[products.size()];
			products.toArray(productArray);
			return productArray;
		}
	}	

}
