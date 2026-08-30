package com.collabnet.ccf.editors;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.CCFJMXMonitorBean;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.model.SynchronizationStatus;

public class JmxConsoleProjectMappingsEditorPage extends JmxConsoleEditorPage {
	private ScrolledForm form;
	private FormToolkit toolkit;
	
	private TableViewer tableViewer;
	
	private IDialogSettings settings = Activator.getDefault().getDialogSettings();
	private CcfDataProvider dataProvider = new CcfDataProvider();
	
	private SynchronizationStatus[] projectMappings;

	public JmxConsoleProjectMappingsEditorPage(String id, String title) {
		super(id, title);
	}

	public JmxConsoleProjectMappingsEditorPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		form = managedForm.getForm();
        toolkit = getEditor().getToolkit();
        TableWrapLayout formLayout = new TableWrapLayout();
        formLayout.numColumns = 1;
        form.getBody().setLayout(formLayout);
		createControls(form.getBody());
	}
	
	private void createControls(Composite composite) {
		Section projectMappingsSection = toolkit.createSection(composite, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 1;
        projectMappingsSection.setLayoutData(td);
        
        projectMappingsSection.setText("Project Mappings");
        Composite projectMappingsSectionClient = toolkit.createComposite(projectMappingsSection); 
        GridLayout projectMappingsLayout = new GridLayout();
        projectMappingsLayout.numColumns = 1;
        projectMappingsLayout.verticalSpacing = 10;
        projectMappingsSectionClient.setLayout(projectMappingsLayout);
        projectMappingsSection.setClient(projectMappingsSectionClient);
        projectMappingsSection.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
            }
        });
        
        Table table = toolkit.createTable(projectMappingsSectionClient, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
        table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		GridData gridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		gridData.heightHint = 250;
		table.setLayoutData(gridData);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		
		tableViewer = new TableViewer(table);
		createColumns(tableViewer, table, layout);
		
		Button refreshButton = toolkit.createButton(projectMappingsSectionClient, "Refresh", SWT.PUSH);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		refreshButton.setLayoutData(gridData);
		refreshButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				refresh();
			}		
		});
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new ProjectMappingsLabelProvider());
		
		boolean sortReversed = false;
		int sortIndex = 0;
		try {
			sortIndex = settings.getInt("consoleProjectMappings.sortColumn");
			sortReversed = settings.getBoolean("consoleProjectMappings.sortReversed");					
		} catch (Exception e) {}
		
		ProjectMappingsSorter sorter = new ProjectMappingsSorter(sortIndex);
		sorter.setReversed(sortReversed);
		tableViewer.setSorter(sorter);
		if (sortReversed) table.setSortDirection(SWT.DOWN);
		else table.setSortDirection(SWT.UP);
		table.setSortColumn(table.getColumn(sortIndex));
		
		getMappings();
		
		toolkit.paintBordersFor(projectMappingsSectionClient);
		
		tableViewer.setInput(projectMappings);
		tableViewer.refresh();
	}
	
	private void createColumns(TableViewer tableViewer, final Table table, TableLayout layout) {
		DisposeListener disposeListener = new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				TableColumn col = (TableColumn)e.getSource();
				if (0 < col.getWidth()) settings.put("consoleProjectMappingTable." + col.getText(), col.getWidth()); //$NON-NLS-1$
			}			
		};
		
		SelectionListener headerListener = getColumnListener(tableViewer);
		
		TableColumn col;

		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText("Source Repository ID");
		col.addSelectionListener(headerListener);
		setColumnWidth(table, layout, disposeListener, col, 200);
		
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText("Target Repository ID");
		col.addSelectionListener(headerListener);
		setColumnWidth(table, layout, disposeListener, col, 200);
		
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText("Hospital Entries");
		col.addSelectionListener(headerListener);
		setColumnWidth(table, layout, disposeListener, col, 100);
		
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText("Queued Artifacts");
		col.addSelectionListener(headerListener);
		setColumnWidth(table, layout, disposeListener, col, 100);
	}
	
	private void setColumnWidth(Table table, TableLayout layout,
			DisposeListener disposeListener, TableColumn col, int defaultWidth) {
		String columnWidth = settings.get("consoleProjectMappingTable." + col.getText()); //$NON-NLS-1$
		if (null == columnWidth || columnWidth.equals("0")) layout.addColumnData(new ColumnWeightData(defaultWidth, defaultWidth, true)); //$NON-NLS-1$
		else layout.addColumnData(new ColumnPixelData(Integer.parseInt(columnWidth), true));
		col.addDisposeListener(disposeListener);
	}
	
	private SelectionListener getColumnListener(final TableViewer tableViewer) {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int column = tableViewer.getTable().indexOf((TableColumn) e.widget);
				setSortColumn(tableViewer, column);
			}
		};
	}
	
	public void setSortColumn(final TableViewer tableViewer, int column) {
		ProjectMappingsSorter oldSorter = (ProjectMappingsSorter)tableViewer.getSorter();
		if (null != oldSorter && column == oldSorter.getColumnNumber()) {
			oldSorter.setReversed(!oldSorter.isReversed());
			if (oldSorter.isReversed()) tableViewer.getTable().setSortDirection(SWT.DOWN);
			else tableViewer.getTable().setSortDirection(SWT.UP);	
			tableViewer.refresh();
		} else {
			ProjectMappingsSorter newSorter = new ProjectMappingsSorter(column);
			tableViewer.setSorter(newSorter);
			tableViewer.getTable().setSortDirection(SWT.UP);
		}
		tableViewer.getTable().setSortColumn(tableViewer.getTable().getColumn(column));
		ProjectMappingsSorter sorter = (ProjectMappingsSorter)tableViewer.getSorter();
		settings.put("projectMappings." + tableViewer.getTable().getData() + ".sortColumn", column);
		settings.put("projectMappings." + tableViewer.getTable().getData() + ".sortReversed", sorter.isReversed());
	}
	
	private void refresh() {
		getMappings();
		tableViewer.setInput(projectMappings);
		tableViewer.refresh();
	}
	
	private void getMappings() {
		final CCFJMXMonitorBean monitor1 = ((JmxConsoleEditor)getEditor()).getMonitor1();
		final CCFJMXMonitorBean monitor2 = ((JmxConsoleEditor)getEditor()).getMonitor2();
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				try {
					projectMappings = dataProvider.getSynchronizationStatuses(getLandscape(), null);
					for (SynchronizationStatus projectMapping : projectMappings) {
						String queueCount = null;
//						if (projectMapping.getTargetSystemKind().equals(Landscape.TYPE_QC)) {
//							queueCount = monitor1.getNumberOfWaitingArtifacts(projectMapping.getSourceSystemId(), projectMapping.getSourceRepositoryId(), projectMapping.getTargetSystemId(), projectMapping.getTargetRepositoryId());
//						} else {
//							queueCount = monitor2.getNumberOfWaitingArtifacts(projectMapping.getSourceSystemId(), projectMapping.getSourceRepositoryId(), projectMapping.getTargetSystemId(), projectMapping.getTargetRepositoryId());
//						}
						
						if (projectMapping.getSourceSystemKind().equals(getLandscape().getType1())) {
							queueCount = monitor1.getNumberOfWaitingArtifacts(projectMapping.getSourceSystemId(), projectMapping.getSourceRepositoryId(), projectMapping.getTargetSystemId(), projectMapping.getTargetRepositoryId());
						} else {
							queueCount = monitor2.getNumberOfWaitingArtifacts(projectMapping.getSourceSystemId(), projectMapping.getSourceRepositoryId(), projectMapping.getTargetSystemId(), projectMapping.getTargetRepositoryId());
						}						
						
						if (null != queueCount) {
							int count = 0;
							try {
								count = Integer.parseInt(queueCount);
							} catch (Exception e) {}
							projectMapping.setQueuedArtifacts(count);
						}
					}
				} catch (Exception e) {
					Activator.handleError(e);
				};
			}
		});
	}
	
	class ProjectMappingsLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			if (0 == columnIndex) {
				SynchronizationStatus status = (SynchronizationStatus)element;
				if (status.isPaused())
					return Activator.getImage(Activator.IMAGE_SYNC_STATUS_ENTRY_PAUSED);
				else
					return Activator.getImage(Activator.IMAGE_SYNC_STATUS_ENTRY);
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			SynchronizationStatus status = (SynchronizationStatus)element;
			String value = null;
			switch (columnIndex) {
			case 0:
				value = status.getSourceRepositoryId();
				break;
			case 1:
				value = status.getTargetRepositoryId();
				break;
			case 2:
				value = Integer.toString(status.getHospitalEntries());
				break;
			case 3:
				value = Integer.toString(status.getQueuedArtifacts());
				break;
			default:
				break;
			}
			if (null == value) value = "";
			return value;
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
		
	};	
	
	class ProjectMappingsSorter extends ViewerSorter {
		private boolean reversed = false;
		private int columnNumber;
		
		private int[][] SORT_ORDERS_BY_COLUMN = {
				{ 0, 1, 2, 3 },
				{ 1, 0, 2, 3 },
				{ 2, 0, 1, 3 },
				{ 3, 0, 1, 2 }
			};
		
		public ProjectMappingsSorter(int columnNumber) {
			this.columnNumber = columnNumber;
		}
		
		public int compare(Viewer viewer, Object o1, Object o2) {
            SynchronizationStatus s1 = (SynchronizationStatus)o1;
            SynchronizationStatus s2 = (SynchronizationStatus)o2;
			int result = 0;
			if (null == s1 || null == s2) {
				result = super.compare(viewer, o1, o2);
			} else {
				int[] columnSortOrder = SORT_ORDERS_BY_COLUMN[columnNumber];;
				for (int i = 0; i < columnSortOrder.length; ++i) {
					result = compareColumnValue(columnSortOrder[i], s1, s2);
					if (0 != result)
						break;
				}
			}
			if (reversed)
				result = -result;
			return result;
		}
		
		int compareColumnValue(int columnNumber, SynchronizationStatus s1, SynchronizationStatus s2) {
			String value1 = null;
			String value2 = null;
			switch (columnNumber) {
				case 0:
                    if (null == s1.getSourceRepositoryId()) value1 = ""; //$NON-NLS-1$
                    else value1 = s1.getSourceRepositoryId();
                    if (null == s2.getSourceRepositoryId()) value2 = ""; //$NON-NLS-1$
                    else value2 = s2.getSourceRepositoryId();
                    break;
				case 1:
                    if (null == s1.getTargetRepositoryId()) value1 = ""; //$NON-NLS-1$
                    else value1 = s1.getTargetRepositoryId();
                    if (null == s2.getTargetRepositoryId()) value2 = ""; //$NON-NLS-1$
                    else value2 = s2.getTargetRepositoryId();
                    break;
				case 2:
                    return Integer.valueOf(s1.getHospitalEntries()).compareTo(Integer.valueOf(s2.getHospitalEntries()));
				case 3:
                    return Integer.valueOf(s1.getQueuedArtifacts()).compareTo(Integer.valueOf(s2.getQueuedArtifacts()));
				default:
					value1 = ""; //$NON-NLS-1$
					value2 = ""; //$NON-NLS-1$
			}
			return value1.compareTo(value2);
		}		
		
		public int getColumnNumber() {
			return columnNumber;
		}

		public boolean isReversed() {
			return reversed;
		}

		public void setReversed(boolean newReversed) {
			reversed = newReversed;
		}		
	}

}
