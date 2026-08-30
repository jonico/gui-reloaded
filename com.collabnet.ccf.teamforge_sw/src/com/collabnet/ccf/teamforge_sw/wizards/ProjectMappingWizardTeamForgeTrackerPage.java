package com.collabnet.ccf.teamforge_sw.wizards;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.sw.ScrumWorksMappingSection;
import com.collabnet.teamforge.api.main.ProjectRow;
import com.collabnet.teamforge.api.tracker.TrackerRow;

public class ProjectMappingWizardTeamForgeTrackerPage extends WizardPage {
	private Map<String, TrackerRow[]> trackerMap = new HashMap<String, TrackerRow[]>();
	private TrackerRow[] trackers;
	private List<String> trackerTitles;	
	private Button newPbiTrackerButton;
	private Text newPbiTrackerText;
	private Table pbiTable;
	private TableViewer pbiViewer;
	private Button newTaskTrackerButton;
	private Text newTaskTrackerText;
	private Table taskTable;
	private TableViewer taskViewer;
	private Button mapToAssignedToUserButton;
	private TrackerRow selectedPbiTracker;
	private TrackerRow selectedTaskTracker;
	private String projectId;
	private String newPbiTrackerTitle;
	private String newTaskTrackerTitle;
	private boolean mapToAssignedToUser = false;
	private Exception getTrackersError;
	
	private String[] columnHeaders = {"Existing Trackers in the selected TeamForge project"};
	private ColumnLayoutData columnLayouts[] = {
		new ColumnWeightData(450, 450, true)};

	public ProjectMappingWizardTeamForgeTrackerPage() {
		super("trackerPage", "TeamForge Trackers", Activator.getDefault().getImageDescriptor(Activator.IMAGE_NEW_PROJECT_MAPPING_WIZBAN));
		setPageComplete(true);
	}

	public void createControl(Composite parent) {
		Composite outerContainer = new Composite(parent,SWT.NONE);
		outerContainer.setLayout(new GridLayout());
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		Group pbiGroup = new Group(outerContainer, SWT.NONE);
		GridLayout pbiLayout = new GridLayout();
		pbiLayout.numColumns = 2;
		pbiGroup.setLayout(pbiLayout);
		pbiGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		pbiGroup.setText("Tracker to be mapped to ScrumWorks PBI:");
		
		newPbiTrackerButton = new Button(pbiGroup, SWT.CHECK);
		newPbiTrackerButton.setText("Create new tracker:");
		newPbiTrackerButton.setSelection(true);
		newPbiTrackerText = new Text(pbiGroup, SWT.BORDER);
		newPbiTrackerText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		newPbiTrackerTitle = "PBIs";
		newPbiTrackerText.setText(newPbiTrackerTitle);
		
		pbiTable = createTable(pbiGroup);	
		pbiViewer = createViewer(pbiTable);
		
		Group taskGroup = new Group(outerContainer, SWT.NONE);
		GridLayout taskLayout = new GridLayout();
		taskLayout.numColumns = 2;
		taskGroup.setLayout(taskLayout);
		taskGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		taskGroup.setText("Tracker to be mapped to ScrumWorks Task:");
		
		newTaskTrackerButton = new Button(taskGroup, SWT.CHECK);
		newTaskTrackerButton.setText("Create new tracker:");
		newTaskTrackerButton.setSelection(true);
		newTaskTrackerText = new Text(taskGroup, SWT.BORDER);
		newTaskTrackerText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		newTaskTrackerTitle = "Tasks";
		newTaskTrackerText.setText(newTaskTrackerTitle);
		
		mapToAssignedToUserButton = new Button(taskGroup, SWT.CHECK);
		mapToAssignedToUserButton.setText(ScrumWorksMappingSection.MAP_POINT_PERSON_TO_ASSIGNED_TO);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		mapToAssignedToUserButton.setLayoutData(data);
		mapToAssignedToUserButton.setSelection(mapToAssignedToUser);
		
		taskTable = createTable(taskGroup);	
		taskViewer = createViewer(taskTable);
		
		SelectionListener selectionListener = new SelectionAdapter() {			
			public void widgetSelected(SelectionEvent event) {
				if (event.getSource() == mapToAssignedToUserButton) {
					mapToAssignedToUser = mapToAssignedToUserButton.getSelection();
				} else {
					if (event.getSource() == newPbiTrackerButton) {
						if (newPbiTrackerButton.getSelection()) {
							pbiViewer.getTable().deselectAll();
							if (0 == newPbiTrackerText.getText().trim().length()) {
								newPbiTrackerText.setFocus();
							}
						}
					}
					if (event.getSource() == newTaskTrackerButton) {
						if (newTaskTrackerButton.getSelection()) {
							taskViewer.getTable().deselectAll();
							if (0 == newTaskTrackerText.getText().trim().length()) {
								newTaskTrackerText.setFocus();
							}
						}
					}
					setPageComplete(canFinish());
				}
			}
		};
		
		newPbiTrackerButton.addSelectionListener(selectionListener);
		newTaskTrackerButton.addSelectionListener(selectionListener);
		mapToAssignedToUserButton.addSelectionListener(selectionListener);
		
		ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSource() == pbiViewer) {
					IStructuredSelection pbiSelection = (IStructuredSelection)pbiViewer.getSelection();
					if (!pbiSelection.isEmpty()) {
						newPbiTrackerButton.setSelection(false);
					}
				}
				if (event.getSource() == taskViewer) {
					IStructuredSelection taskSelection = (IStructuredSelection)taskViewer.getSelection();
					if (!taskSelection.isEmpty()) {
						newTaskTrackerButton.setSelection(false);
					}
				}
				setPageComplete(canFinish());
			}		
		};
		
		pbiViewer.addSelectionChangedListener(selectionChangedListener);
		taskViewer.addSelectionChangedListener(selectionChangedListener);
		
		ModifyListener modifyListener = new ModifyListener() {	
			public void modifyText(ModifyEvent event) {
				newPbiTrackerTitle = newPbiTrackerText.getText().trim();
				newTaskTrackerTitle = newTaskTrackerText.getText().trim();
				setPageComplete(canFinish());
			}
		};
		
		newPbiTrackerText.addModifyListener(modifyListener);
		newTaskTrackerText.addModifyListener(modifyListener);

		setMessage("Select the TeamForge trackers to be mapped to ScrumWorks PBIs and Tasks.");

		setControl(outerContainer);
	}
	
	private boolean canFinish() {
		setErrorMessage(null);
		IStructuredSelection pbiSelection = (IStructuredSelection)pbiViewer.getSelection();
		IStructuredSelection taskSelection = (IStructuredSelection)taskViewer.getSelection();
		selectedPbiTracker = (TrackerRow)pbiSelection.getFirstElement();
		selectedTaskTracker = (TrackerRow)taskSelection.getFirstElement();
		boolean pageComplete;
		if (!newPbiTrackerButton.getSelection() && pbiSelection.isEmpty()) {
			pageComplete = false;
		}
		else if (newPbiTrackerButton.getSelection() && 0 == newPbiTrackerText.getText().trim().length()) {
			pageComplete = false;
		}
		else if (!newTaskTrackerButton.getSelection() && taskSelection.isEmpty()) {
			pageComplete = false;
		}
		else if (newTaskTrackerButton.getSelection() && 0 == newTaskTrackerText.getText().trim().length()) {
			pageComplete = false;
		}
		else if (!newPbiTrackerButton.getSelection() && !newTaskTrackerButton.getSelection() && selectedPbiTracker.getId().equals(selectedTaskTracker.getId())) {
			pageComplete = false;
			setErrorMessage("PBI and Task trackers cannot be the same.");
		}
		else {
			pageComplete = true;
		}
		if (pageComplete && null != ((ProjectMappingWizard)getWizard()).getSelectedProject()) {
			StringBuffer errorMessage = null;
			if (newPbiTrackerButton.getSelection() && trackerTitles.contains(newPbiTrackerText.getText().trim())) {
				errorMessage = new StringBuffer("Tracker " + newPbiTrackerText.getText().trim() + " already exists.");
			}
			if (newTaskTrackerButton.getSelection() && trackerTitles.contains(newTaskTrackerText.getText().trim())) {
				if (null == errorMessage) {
					errorMessage = new StringBuffer();
				} else {
					errorMessage.append("\n");
				}
				errorMessage.append("Tracker " + newTaskTrackerText.getText().trim() + " already exists.");
			}
			if (null != errorMessage) {
				setErrorMessage(errorMessage.toString());
				pageComplete = false;
			}
		}
		newPbiTrackerText.setEnabled(newPbiTrackerButton.getSelection());
		newTaskTrackerText.setEnabled(newTaskTrackerButton.getSelection());
		return pageComplete;
	}

	private Table createTable(Group pbiGroup) {
		Table table = new Table(pbiGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
		gd.widthHint = 500;
		gd.heightHint = 200;
		gd.horizontalSpan = 2;
		table.setLayoutData(gd);
		TableLayout tableLayout = new TableLayout();
		for (int i = 0; i < columnHeaders.length; i++) {
			tableLayout.addColumnData(columnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE,i);
			tc.setResizable(columnLayouts[i].resizable);
			tc.setText(columnHeaders[i]);
		}
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		return table;
	}
	
	private TableViewer createViewer(Table table) {
		TableViewer viewer = new TableViewer(table);		
		viewer.setContentProvider(new TrackersContentProvider());
		viewer.setLabelProvider(new TrackersLabelProvider());
		viewer.setInput(this);
		return viewer;
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {	
			String selectedProjectId = null;
			if (null != ((ProjectMappingWizard)getWizard()).getSelectedProject()) {
				selectedProjectId = ((ProjectMappingWizard)getWizard()).getSelectedProject().getId();
			}
			if (null == projectId || !projectId.equals(selectedProjectId)) {
				ProjectRow selectedProject = ((ProjectMappingWizard)getWizard()).getSelectedProject();
				if (null == selectedProject) {
					projectId = null;
					trackers = new TrackerRow[0];
					pbiViewer.refresh();
					taskViewer.refresh();
				} else {
					projectId = ((ProjectMappingWizard)getWizard()).getSelectedProject().getId();
					trackers = getTrackers(((ProjectMappingWizard)getWizard()).getSelectedProject().getId());
					trackerTitles = new ArrayList<String>();
					pbiViewer.refresh();
					taskViewer.refresh();
					for (int i = 0; i < trackers.length; i++) {
						trackerTitles.add(trackers[i].getTitle());
						if (trackers[i].getTitle().equals("PBIs")) {
							pbiViewer.getTable().select(i);
							selectedPbiTracker = trackers[i];
							newPbiTrackerButton.setSelection(false);
							newPbiTrackerText.setText("");
							newPbiTrackerText.setEnabled(false);
						}
						if (trackers[i].getTitle().equals("Tasks")) {
							taskViewer.getTable().select(i);
							selectedTaskTracker = trackers[i];
							newTaskTrackerButton.setSelection(false);
							newTaskTrackerText.setText("");
							newTaskTrackerText.setEnabled(false);
						}
					}
				}
				setPageComplete(canFinish());
			}
		}
	}

	public TrackerRow getSelectedPbiTracker() {
		return selectedPbiTracker;
	}
	
	public TrackerRow getSelectedTaskTracker() {
		return selectedTaskTracker;
	}

	public String getNewPbiTrackerTitle() {
		return newPbiTrackerTitle;
	}

	public String getNewTaskTrackerTitle() {
		return newTaskTrackerTitle;
	}

	public boolean isMapToAssignedToUser() {
		return mapToAssignedToUser;
	}

	private TrackerRow[] getTrackers(final String projectId) {
		getTrackersError = null;
		trackers = trackerMap.get(projectId);
		if (null == trackers) {
			
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					String taskName = "Retrieving TeamForge trackers";
					monitor.setTaskName(taskName);
					monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
					monitor.subTask("");
					try {
						trackers = ((ProjectMappingWizard)getWizard()).getSoapClient().getAllTrackersOfProject(projectId);
						if (null != trackers) {
							trackerMap.put(projectId, trackers);
						}
					} catch (RemoteException e) {
						Activator.handleError(e);
						getTrackersError = e;
					}
					monitor.done();
				}
			};
			
			try {
				getContainer().run(true, false, runnable);
			} catch (Exception e) {
				Activator.handleError(e);
				getTrackersError = e;
			}
			if (null != getTrackersError) {
				setErrorMessage("An unexpected error occurred while getting TeamForge trackers.  See error log for details.");
			}

		}
		return trackers;
	}
	
	static class TrackersLabelProvider extends LabelProvider implements ITableLabelProvider {

		public String getColumnText(Object element, int columnIndex) {
			TrackerRow tracker = (TrackerRow)element;
			switch (columnIndex) { 
				case 0: return tracker.getTitle();
			}
			return "";  //$NON-NLS-1$
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	
	}	
	
	class TrackersContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public Object[] getElements(Object obj) {
			if (null == trackers) {
				return new TrackerRow[0];
			}
			return trackers;
		}
	}	

}
