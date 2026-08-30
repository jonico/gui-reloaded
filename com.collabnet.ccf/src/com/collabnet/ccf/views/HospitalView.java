package com.collabnet.ccf.views;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.actions.HospitalAction;
import com.collabnet.ccf.actions.HospitalEditAction;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.db.Filter;
import com.collabnet.ccf.dialogs.HospitalFilterDialog;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.Patient;
import com.collabnet.ccf.preferences.CcfPreferencePage;
import com.collabnet.ccf.preferences.HospitalPreferencePage;

public class HospitalView extends ViewPart {
	private Composite parentComposite;
	private TableViewer tableViewer;
	private IDialogSettings settings = Activator.getDefault().getDialogSettings();
	private CcfDataProvider dataProvider = new CcfDataProvider();
	private Patient[] patients;
	private boolean hospitalLoaded;
	
	private static HospitalView view;
	private static String contentDescription;
	private static Landscape landscape;
	private static Filter[][] filters;
	private static boolean filtering;
	private static boolean filtersActive = true;
	
	private static List<String> selectedColumns;
	private static List<String> allColumns;
	private static List<String> columnHeaderList;
	private static String[] columnHeaders = {
		"ID",
		"Timestamp",
		"Exception Class",
		"Exception Message",
		"Cause Exception Class",
		"Cause Exception Message",
		"Stack Trace",
		"Adaptor Name",
		"Originating Component",
		"Data Type",
		"Data",
		"Fixed",
		"Reprocessed",
		"Source System ID",
		"Source Repository ID",
		"Target System ID",
		"Target Repository ID",
		"Source System Kind",
		"Source Repository Kind",
		"Target System Kind",
		"Target Repository Kind",
		"Source Artifact ID",
		"Target Artifact ID",
		"Error Code",
		"Source Last Modified",
		"Target Last Modified",
		"Source Artifact Version",
		"Target Artifact Version",
		"Artifact Type",
		"Generic Artifact"
	};
	static {
		columnHeaderList = new ArrayList<String>();
		for (int i = 0; i < columnHeaders.length; i++) {
			columnHeaderList.add(columnHeaders[i]);
		}
	}
	private static int[] columnWidths = {
		50,
		200,
		100,
		300,
		200,
		300,
		200,
		200,
		300,
		200,
		200,
		50,
		50,
		200,
		100,
		200,
		100,
		100,
		100,
		100,
		100,
		200,
		200,
		200,
		100,
		100,
		100,
		100,
		100,
		300
	};
	
	public static final String ID = "com.collabnet.ccf.views.HospitalView";
	
	public HospitalView() {
		super();
		view = this;
	}

	@Override
	public void createPartControl(Composite parent) {
		parentComposite = parent;
		
		if (settings.getBoolean(Filter.HOSPITAL_FILTERS_SET)) {
			filtersActive = settings.getBoolean(Filter.HOSPITAL_FILTERS_ACTIVE);
			getPreviousFilters();
		}
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		parent.setLayout(layout);
		
		createControls();

		if (Activator.getDefault().getPreferenceStore().getBoolean(Activator.PREFERENCES_AUTOCONNECT)) {
			getPatients();
		} else {
			setContentDescription("Click Refresh to load Hospital");			
		}
	}
	
	private void createControls() {
		setSelectedColumns();
		
		// If tableViewer has already been created, we are refreshing because
		// column preferences have been changed.
		boolean recreatingTable = tableViewer != null;
		if (recreatingTable) {
			Control[] controls = parentComposite.getChildren();
			for (int i = 0; i < controls.length; i++) {
				if (!controls[i].isDisposed()) {
					controls[i].dispose();
				}
			}
		}
		
		tableViewer = createTable(parentComposite);
		
		createMenus(recreatingTable);
		if (!recreatingTable) createToolbar();
		
		getSite().setSelectionProvider(tableViewer);	
		
		if (recreatingTable && patients != null) {
			tableViewer.setInput(patients);
			parentComposite.layout();
			parentComposite.redraw();
		}
	}

	@Override
	public void setFocus() {
	}
	
	public void dispose() {
		view = null;
		super.dispose();
	}
	
	public void refresh() {
		getPatients();
	}
	
	public void refreshTableLayout() {
		createControls();
	}
	
	public boolean isHospitalLoaded() {
		return hospitalLoaded;
	}
	
	public static HospitalView getView() {
		return view;
	}
	
	public static void setFilters(Filter[][] filters, boolean filtering, String description) {
		HospitalView.filters = filters;
		HospitalView.filtering = filtering;
		if (filtering) {
			if (description == null) {
				contentDescription = "(Filters Active)";
			} else {
				contentDescription = description;
			}
		} else {
			contentDescription = "";
		}
	}
	
	public static void setLandscape(Landscape landscape) {
		HospitalView.landscape = landscape;
	}
	
	private TableViewer createTable(Composite parent) {
		Table table =	new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gridData);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
        
		tableViewer = new TableViewer(table);
		createColumns(table, layout);

		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new HospitalLabelProvider());
		
		int sortIndex = 0;
		boolean sortReversed = false;
		String sortColumn = settings.get("HospitalView.sortColumn");
		if (sortColumn != null) {
			int index = columnHeaderList.indexOf(sortColumn);
			if (index != -1) {
				String columnName = allColumns.get(index);
				int nameIndex = selectedColumns.indexOf(columnName);
				if (nameIndex != -1) {
					sortIndex = nameIndex;
					sortReversed = settings.getBoolean("HospitalView.sortReversed");					
				}
			}
		}
		
		HospitalSorter sorter = new HospitalSorter(sortIndex);
		sorter.setReversed(sortReversed);
		tableViewer.setSorter(sorter);
		if (sortReversed) table.setSortDirection(SWT.DOWN);
		else table.setSortDirection(SWT.UP);
		table.setSortColumn(table.getColumn(sortIndex));
		
		tableViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent evt) {
				HospitalEditAction action = new HospitalEditAction();
				action.selectionChanged(null, tableViewer.getSelection());
				action.run(null);
			}			
		});
		
		Transfer[] dropTypes = { LocalSelectionTransfer.getTransfer() };
		tableViewer.addDropSupport(DND.DROP_COPY | DND.DROP_DEFAULT, dropTypes,
				new HospitalDropAdapter(tableViewer));
		
		return tableViewer;
	}
	
	private void createColumns(Table table, TableLayout layout) {
		DisposeListener disposeListener = new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				TableColumn col = (TableColumn)e.getSource();
				if (col.getWidth() > 0) settings.put("HospitalView." + col.getText(), col.getWidth()); //$NON-NLS-1$
			}			
		};
		
		SelectionListener headerListener = getColumnListener();

		Iterator<String> iter = selectedColumns.iterator();
		while (iter.hasNext()) {
			String columnName = iter.next();
			createColumn(columnName, table, headerListener, disposeListener, layout);
		}
	}
	
	private void createColumn(String columnName, Table table, SelectionListener headerListener, DisposeListener disposeListener, TableLayout layout) {
		int index = allColumns.indexOf(columnName);
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(columnHeaders[index]);
		col.addSelectionListener(headerListener);
		setColumnWidth(layout, disposeListener, col, columnWidths[index]);
		
		if (columnName.equals("ID")) table.setSortColumn(col);
	}

	private SelectionListener getColumnListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int column = tableViewer.getTable().indexOf((TableColumn) e.widget);
				setSortColumn(tableViewer, column);
			}
		};
	}
	
	private void setSortColumn(TableViewer tableViewer, int column) {
		HospitalSorter oldSorter = (HospitalSorter)tableViewer.getSorter();
		if (oldSorter != null && column == oldSorter.getColumnNumber()) {
			oldSorter.setReversed(!oldSorter.isReversed());
			if (oldSorter.isReversed()) tableViewer.getTable().setSortDirection(SWT.DOWN);
			else tableViewer.getTable().setSortDirection(SWT.UP);	
			tableViewer.refresh();
		} else {
			HospitalSorter newSorter = new HospitalSorter(column);
			tableViewer.setSorter(newSorter);
			tableViewer.getTable().setSortDirection(SWT.UP);
		}
		tableViewer.getTable().setSortColumn(tableViewer.getTable().getColumn(column));
		String columnName = tableViewer.getTable().getColumn(column).getText();
		HospitalSorter sorter = (HospitalSorter)tableViewer.getSorter();
		settings.put("HospitalView.sortColumn", columnName);
		settings.put("HospitalView.sortReversed", sorter.isReversed());
	}
	
	private void setColumnWidth(TableLayout layout,
			DisposeListener disposeListener, TableColumn col, int defaultWidth) {
		String columnWidth = settings.get("HospitalView." + col.getText()); //$NON-NLS-1$
		if (columnWidth == null || columnWidth.equals("0")) layout.addColumnData(new ColumnWeightData(defaultWidth, true)); //$NON-NLS-1$
		else layout.addColumnData(new ColumnPixelData(Integer.parseInt(columnWidth), true));
		col.addDisposeListener(disposeListener);
	}
	
	private void createToolbar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		toolbarManager.add(new RefreshAction());
		toolbarManager.add(new FilterAction());
		toolbarManager.add(new ConnectionAction());
	}
	
	private void createMenus(boolean recreatingTable) {
		MenuManager menuMgr = new MenuManager("#HospitalViewPopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				HospitalView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(tableViewer.getControl());
		tableViewer.getControl().setMenu(menu);		
		getSite().registerContextMenu(menuMgr, tableViewer);
		
		if (!recreatingTable) {
			IActionBars actionBars = getViewSite().getActionBars();
			IMenuManager actionBarsMenu = actionBars.getMenuManager();
			actionBarsMenu.add(new ArrangeColumnsAction());
		}
	}
	
	private void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));		
	}
	
	private void getPatients() {
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				if (landscape == null || landscape.getDescription() == null) {
					landscape = new Landscape();
					landscape.setDatabaseUrl(Activator.getDefault().getPreferenceStore().getString(Activator.PREFERENCES_DATABASE_URL));
					landscape.setDatabaseDriver(Activator.getDefault().getPreferenceStore().getString(Activator.PREFERENCES_DATABASE_DRIVER));
					landscape.setDatabaseUser(Activator.getDefault().getPreferenceStore().getString(Activator.PREFERENCES_DATABASE_USER));
					landscape.setDatabasePassword(Activator.decodePassword(Activator.getDefault().getPreferenceStore().getString(Activator.PREFERENCES_DATABASE_PASSWORD)));
				}
				if (contentDescription == null) setContentDescription("");
				else setContentDescription(contentDescription);
				try {
					if (filtering) patients = dataProvider.getPatients(landscape, filters);
					else patients = dataProvider.getPatients(landscape, (Filter[])null);
					hospitalLoaded = true;
				} catch (Exception e) {
					setContentDescription("Could not connect to database.  See error log.");
					patients = new Patient[0];
					Activator.handleDatabaseError(e, false, true, "Hospital View");
				}
				tableViewer.setInput(patients);
				tableViewer.refresh();
			}			
		});
	}
	
	private void getPreviousFilters() {
		List<Filter> filterList = new ArrayList<Filter>();
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_TIMESTAMP, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_EXCEPTION_CLASS_NAME, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_EXCEPTION_MESSAGE, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_CAUSE_EXCEPTION_CLASS_NAME, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_CAUSE_EXCEPTION_MESSAGE, true);		
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_STACK_TRACE, true);	
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_ADAPTOR_NAME, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_ORIGINATING_COMPONENT, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_DATA_TYPE, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_DATA, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_FIXED, false);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_REPROCESSED, false);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_ID, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_ID, true);	
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_KIND, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_KIND, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_TARGET_SYSTEM_KIND, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_KIND, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_SOURCE_ARTIFACT_ID, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_TARGET_ARTIFACT_ID, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_ERROR_CODE, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_SOURCE_ARTIFACT_VERSION, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_TARGET_ARTIFACT_VERSION, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_ARTIFACT_TYPE, true);
		updateFilterList(filterList, CcfDataProvider.HOSPITAL_GENERIC_ARTIFACT, true);
		if (filterList.size() > 0) {
			
			Filter[] previousFilters = new Filter[filterList.size()];
			filterList.toArray(previousFilters);
			Filter[][] filterGroups = { previousFilters };
			
//			filters = new Filter[filterList.size()];
//			filterList.toArray(filters);
			filtering = filtersActive;
//			setFilters(filters, filtering);
			setFilters(filterGroups, filtering, null);
		}
	}
	
	private void updateFilterList(List<Filter> filterList, String columnName, boolean stringValue) {
		String filterValue = settings.get(Filter.HOSPITAL_FILTER_VALUE + columnName);
		if (filterValue != null && filterValue.length() > 0) {
			Filter filter = new Filter(columnName, filterValue, stringValue, settings.getInt(Filter.HOSPITAL_FILTER_TYPE + columnName));
			filterList.add(filter);			
		}
	}
	
	public static void setSelectedColumns() {
		selectedColumns = new ArrayList<String>();
		String columns = Activator.getDefault().getPreferenceStore().getString(Activator.PREFERENCES_HOSPITAL_COLUMNS);
		String[] columnArray = columns.split("\\,");
		for (int i = 0; i < columnArray.length; i++) {
			selectedColumns.add(columnArray[i]);
		}
		allColumns = new ArrayList<String>();
		String[] allColumnArray = CcfDataProvider.HOSPITAL_COLUMNS.split("\\,");
		for (int i = 0; i < allColumnArray.length; i++) {
			allColumns.add(allColumnArray[i]);
		}
	}
	
	class HospitalLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				Patient patient = (Patient)element;
				
				if (patient.isOutdated()) {
					return Activator.getImage(Activator.IMAGE_HOSPITAL_ENTRY_OUTDATED);
				}
				
				if (patient.isReprocessed() && !patient.isFixed()) {
					return Activator.getImage(Activator.IMAGE_HOSPITAL_ENTRY_REPLAY_FAILED);
				}
				
				String errorCode = patient.getErrorCode();
				if (patient.isFixed()) return Activator.getImage(Activator.IMAGE_HOSPITAL_ENTRY_FIXED);
				else if (errorCode != null && errorCode.equals("replay")) return Activator.getImage(Activator.IMAGE_HOSPITAL_ENTRY_REPLAY);
				else return Activator.getImage(Activator.IMAGE_HOSPITAL_ENTRY);
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			String columnName = selectedColumns.get(columnIndex);
			int index = allColumns.indexOf(columnName);			
			Patient patient = (Patient)element;
			switch (index) {
			case 0:
				if (patient.getId() == 0) return "";
				else return Integer.toString(patient.getId());
			case 1:
				if (patient.getTimeStamp() == null) return "";
				else return patient.getTimeStamp();		
			case 2:
				if (patient.getExceptionClassName() == null) return "";
				else return patient.getExceptionClassName();
			case 3:
				if (patient.getExceptionMessage() == null) return "";
				else return patient.getExceptionMessage();	
			case 4:
				if (patient.getCauseExceptionClassName() == null) return "";
				else return patient.getCauseExceptionClassName();
			case 5:
				if (patient.getCauseExceptionMessage() == null) return "";
				else return patient.getCauseExceptionMessage();
			case 6:
				if (patient.getStackTrace() == null) return "";
				else return patient.getStackTrace();	
			case 7:
				if (patient.getAdaptorName() == null) return "";
				else return patient.getAdaptorName();	
			case 8:
				if (patient.getOriginatingComponent() == null) return "";
				else return patient.getOriginatingComponent();	
			case 9:
				if (patient.getDataType() == null) return "";
				else return patient.getDataType();	
			case 10:
				if (patient.getData() == null) return "";
				else return patient.getData();	
			case 11:
				if (patient.isFixed()) return "true";
				else return "";	
			case 12:
				if (patient.isReprocessed()) return "true";
				else return "";	
			case 13:
				if (patient.getSourceSystemId() == null) return "";
				else return patient.getSourceSystemId();	
			case 14:
				if (patient.getSourceRepositoryId() == null) return "";
				else return patient.getSourceRepositoryId();
			case 15:
				if (patient.getTargetSystemId() == null) return "";
				else return patient.getTargetSystemId();	
			case 16:
				if (patient.getTargetRepositoryId() == null) return "";
				else return patient.getTargetRepositoryId();	
			case 17:
				if (patient.getSourceSystemKind() == null) return "";
				else return patient.getSourceSystemKind();
			case 18:
				if (patient.getSourceRepositoryKind() == null) return "";
				else return patient.getSourceRepositoryKind();	
			case 19:
				if (patient.getTargetSystemKind() == null) return "";
				else return patient.getTargetSystemKind();
			case 20:
				if (patient.getTargetRepositoryKind() == null) return "";
				else return patient.getTargetRepositoryKind();	
			case 21:
				if (patient.getSourceArtifactId() == null) return "";
				else return patient.getSourceArtifactId();	
			case 22:
				if (patient.getTargetArtifactId() == null) return "";
				else return patient.getTargetArtifactId();	
			case 23:
				if (patient.getErrorCode() == null) return "";
				else return patient.getErrorCode();
			case 24:
				if (patient.getSourceLastModificationTime() == null) return "";
				else return patient.getSourceLastModificationTime().toString();
			case 25:
				if (patient.getTargetLastModificationTime() == null) return "";
				else return patient.getTargetLastModificationTime().toString();
			case 26:
				if (patient.getSourceArtifactVersion() == null) return "";
				else return patient.getSourceArtifactVersion();
			case 27:
				if (patient.getTargetArtifactVersion() == null) return "";
				else return patient.getTargetArtifactVersion();	
			case 28:
				if (patient.getArtifactType() == null) return "";
				else return patient.getArtifactType();	
			case 29:
				if (patient.getGenericArtifact() == null) return "";
				else return patient.getGenericArtifact();					
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
	
	class HospitalSorter extends ViewerSorter {
		private boolean reversed = false;
		private int columnNumber;
		private int index;
		
		public HospitalSorter(int columnNumber) {
			this.columnNumber = columnNumber;
			String columnName = selectedColumns.get(columnNumber);
			index = allColumns.indexOf(columnName);			
		}
		
		public void setReversed(boolean reversed) {
			this.reversed = reversed;
		}
		
		public int getColumnNumber() {
			return columnNumber;
		}
		
		public boolean isReversed() {
			return reversed;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 == null || e2 == null) return super.compare(viewer, e1, e2);
			
			Patient p1 = (Patient)e1;
			Patient p2 = (Patient)e2;
			int result = 0;
			
			result = compareColumnValue(p1, p2);
			if (result == 0) {
				if (p1.getId() > p2.getId()) result = 1;
				else if (p2.getId() > p1.getId()) result = -1;
			}
			
			if (reversed)
				result = -result;
			return result;
		}
		
		private int compareColumnValue(Patient p1, Patient p2) {
			String value1 = null;
			String value2 = null;
			switch (index) {
			case 0:
				if (p1.getId() > p2.getId()) return 1;
				else if (p2.getId() > p1.getId()) return -1;
				else return 0;
			case 1:
				if (p1.getId() > p2.getId()) return 1;
				else if (p2.getId() > p1.getId()) return -1;
				else return 0;
			case 2:
				value1 = p1.getExceptionClassName();
				value2 = p2.getExceptionClassName();
				break;
			case 3:
				value1 = p1.getExceptionMessage();
				value2 = p2.getExceptionMessage();
				break;			
			case 4:
				value1 = p1.getCauseExceptionClassName();
				value2 = p2.getCauseExceptionClassName();
				break;		
			case 5:
				value1 = p1.getCauseExceptionMessage();
				value2 = p2.getCauseExceptionMessage();
				break;
			case 6:
				value1 = p1.getStackTrace();
				value2 = p2.getStackTrace();
				break;
			case 7:
				value1 = p1.getAdaptorName();
				value2 = p2.getAdaptorName();
				break;
			case 8:
				value1 = p1.getOriginatingComponent();
				value2 = p2.getOriginatingComponent();
				break;
			case 9:
				value1 = p1.getDataType();
				value2 = p2.getDataType();
				break;
			case 10:
				value1 = p1.getData();
				value2 = p2.getData();
				break;
			case 11:
				if (p1.isFixed()) value1 = "true";
				else value1 = "";
				if (p2.isFixed()) value2 = "true";
				else value2 = "";
				break;
			case 12:
				if (p1.isReprocessed()) value1 = "true";
				else value1 = "";
				if (p2.isReprocessed()) value2 = "true";
				else value2 = "";
				break;
			case 13:
				value1 = p1.getSourceSystemId();
				value2 = p2.getSourceSystemId();
				break;
			case 14:
				value1 = p1.getSourceRepositoryId();
				value2 = p2.getSourceRepositoryId();
				break;
			case 15:
				value1 = p1.getTargetSystemId();
				value2 = p2.getTargetSystemId();
				break;
			case 16:
				value1 = p1.getTargetRepositoryId();
				value2 = p2.getTargetRepositoryId();
				break;
			case 17:
				value1 = p1.getSourceSystemKind();
				value2 = p2.getSourceSystemKind();
				break;
			case 18:
				value1 = p1.getSourceRepositoryKind();
				value2 = p2.getSourceRepositoryKind();
				break;
			case 19:
				value1 = p1.getTargetSystemKind();
				value2 = p2.getTargetSystemKind();
				break;
			case 20:
				value1 = p1.getTargetRepositoryKind();
				value2 = p2.getTargetRepositoryKind();
				break;	
			case 21:
				value1 = p1.getSourceArtifactId();
				value2 = p2.getSourceArtifactId();
				break;
			case 22:
				value1 = p1.getTargetArtifactId();
				value2 = p2.getTargetArtifactId();
				break;
			case 23:
				value1 = p1.getErrorCode();
				value2 = p2.getErrorCode();
				break;
			case 24:
				Timestamp ts1 = p1.getSourceLastModificationTime();
				Timestamp ts2 = p2.getSourceLastModificationTime();
				if (ts1 == null && ts2 == null) return 0;
				else if (ts1 == null) return -1;
				else if (ts2 == null) return 1;
				else return ts1.compareTo(ts2);
			case 25:
				ts1 = p1.getTargetLastModificationTime();
				ts2 = p2.getTargetLastModificationTime();
				if (ts1 == null && ts2 == null) return 0;
				else if (ts1 == null) return -1;
				else if (ts2 == null) return 1;
				else return ts1.compareTo(ts2);
			case 26:
				value1 = p1.getSourceArtifactVersion();
				value2 = p2.getSourceArtifactVersion();
				break;
			case 27:
				value1 = p1.getTargetArtifactVersion();
				value2 = p2.getTargetArtifactVersion();
				break;
			case 28:
				value1 = p1.getArtifactType();
				value2 = p2.getArtifactType();
				break;
			case 29:
				value1 = p1.getGenericArtifact();
				value2 = p2.getGenericArtifact();
				break;
			default:
				break;
			}
			if (value1 == null) value1 = "";
			if (value2 == null) value2 = "";
			return value1.compareTo(value2);
		}
		
	}
	
	class RefreshAction extends Action {
		public RefreshAction() {
			super();
			setImageDescriptor(Activator.getDefault().getImageDescriptor(Activator.IMAGE_REFRESH));
			setToolTipText("Refresh View");
		}
		public void run() {
			getPatients();
		}
	}
	
	class FilterAction extends Action {
		public FilterAction() {
			super();
			setImageDescriptor(Activator.getDefault().getImageDescriptor(Activator.IMAGE_FILTERS));
			setToolTipText("Filters...");
		}
		public void run() {
			HospitalFilterDialog dialog = new HospitalFilterDialog(Display.getDefault().getActiveShell(), filters, filtersActive);
			if (dialog.open() == HospitalFilterDialog.OK) {
				filtering = dialog.isFiltering();
				filtersActive = dialog.filtersActive();
				
//				Filter[][] filterGroups = { dialog.getFilters() };
//				
//				setFilters(filterGroups, filtering);
				
				setFilters(dialog.getFilters(), filtering, null);
				getPatients();
			}
		}
	}
	
	class ArrangeColumnsAction extends Action {
		public ArrangeColumnsAction() {
			super();
			setText("Columns...");
		}
		public void run() {
			PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(), HospitalPreferencePage.ID, null, null);
			if (pref != null) pref.open();		
		}
	}
	
	class ConnectionAction extends Action {
		public ConnectionAction() {
			super();
			setImageDescriptor(Activator.getDefault().getImageDescriptor(Activator.IMAGE_DATABASE_CONNECTION));
			setToolTipText("Database connection...");
		}
		public void run() {
			PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(), CcfPreferencePage.ID, null, null);
			if (pref != null) pref.open();			
		}
	}
	
	class HospitalDropAdapter extends ViewerDropAdapter {
		
		protected HospitalDropAdapter(Viewer viewer) {
			super(viewer);
			setFeedbackEnabled(true);
		}

		@Override
		public boolean performDrop(Object arg0) {
			return true;
		}

		@Override
		public boolean validateDrop(Object arg0, int arg1, TransferData arg2) {
			return true;
		}
		
		@Override
		public void drop(DropTargetEvent event) {
			if (event.data instanceof IStructuredSelection selection) {
				HospitalAction action = new HospitalAction();
				action.selectionChanged(null, selection);
				action.run(null);		
			}
		}
		
		public void dragEnter(DropTargetEvent event) {
			if (event.detail == DND.DROP_DEFAULT) {
				if ((event.operations & DND.DROP_COPY) != 0) {
					event.detail = DND.DROP_COPY;
				} else {
					event.detail = DND.DROP_NONE;
				}
			}
		}
		
		public void dragOver(DropTargetEvent event) {
			event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
		}
		
		public void dragOperationChanged(DropTargetEvent event) {
			if ((event.detail == DND.DROP_DEFAULT) || (event.operations & DND.DROP_COPY) != 0) {

				event.detail = DND.DROP_COPY;
			} else {
				event.detail = DND.DROP_NONE;
			}
		}
		
	}

}
