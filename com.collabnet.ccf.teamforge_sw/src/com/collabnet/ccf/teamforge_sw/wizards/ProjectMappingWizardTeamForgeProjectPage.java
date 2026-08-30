package com.collabnet.ccf.teamforge_sw.wizards;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.collabnet.ccf.Activator;
import com.collabnet.teamforge.api.main.ProjectRow;

public class ProjectMappingWizardTeamForgeProjectPage extends WizardPage {
	private ProjectRow[] projects;
	private List<String> projectTitles;	
	private Button newProjectButton;
	private Text newProjectText;
	private Table table;
	private TableViewer viewer;
	private ProjectRow selectedProject;
	private boolean projectsRetrieved;
	private String newProjectTitle;
	private String selectedProduct;
	private Exception getProjectsError;
	
	private String[] columnHeaders = {"Project"};
	private ColumnLayoutData columnLayouts[] = {
		new ColumnWeightData(450, 450, true)};

	public ProjectMappingWizardTeamForgeProjectPage() {
		super("projectPage", "TeamForge Project", Activator.getDefault().getImageDescriptor(Activator.IMAGE_NEW_PROJECT_MAPPING_WIZBAN));
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		setMessage("Select the TeamForge project to be mapped.");
		Composite outerContainer = new Composite(parent,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		outerContainer.setLayout(layout);
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		newProjectButton = new Button(outerContainer, SWT.CHECK);
		newProjectButton.setText("Create new project:");
		newProjectButton.setSelection(true);
		newProjectButton.addSelectionListener(new SelectionAdapter() {			
			public void widgetSelected(SelectionEvent e) {
				if (newProjectButton.getSelection()) {
					viewer.getTable().deselectAll();
					if (0 == newProjectText.getText().trim().length()) {
						newProjectText.setFocus();
					}
				}
				setPageComplete(canFinish());
			}
		});
		newProjectText = new Text(outerContainer, SWT.BORDER);
		newProjectText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		newProjectText.addModifyListener(new ModifyListener() {			
			public void modifyText(ModifyEvent e) {
				newProjectTitle = newProjectText.getText().trim();
				setPageComplete(canFinish());
			}
		});
		
		table = new Table(outerContainer, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
		gd.widthHint = 500;
		gd.heightHint = 200;
		gd.horizontalSpan = 2;
		table.setLayoutData(gd);
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		viewer = new TableViewer(table);		
		viewer.setContentProvider(new ProjectsContentProvider());
		viewer.setLabelProvider(new ProjectsLabelProvider());
		for (int i = 0; i < columnHeaders.length; i++) {
			tableLayout.addColumnData(columnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE,i);
			tc.setResizable(columnLayouts[i].resizable);
			tc.setText(columnHeaders[i]);
		}
		viewer.setInput(this);	
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection projectSelection = (IStructuredSelection)viewer.getSelection();
				if (!projectSelection.isEmpty()) {
					newProjectButton.setSelection(false);
				}
				selectedProject = (ProjectRow)projectSelection.getFirstElement();
				setPageComplete(canFinish());
			}		
		});	

		setControl(outerContainer);
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible && null == projects && !projectsRetrieved) {
			projectsRetrieved = true;
			getProjects();
			viewer.refresh();
		}
		if (visible) {
			if (null == selectedProduct || !selectedProduct.equals(((ProjectMappingWizard)getWizard()).getSelectedProduct().getName())) {
				selectedProduct = ((ProjectMappingWizard)getWizard()).getSelectedProduct().getName();
				newProjectText.setText(selectedProduct);
				if (null != projects) {
					for (int i = 0; i < projects.length; i++) {
						if (projects[i].getTitle().equals(selectedProduct)) {
							table.select(i);
							newProjectButton.setSelection(false);
							newProjectText.setText("");
							newProjectText.setEnabled(false);
							viewer.reveal(projects[i]);
						}
					}
				}
				setPageComplete(canFinish());
			}
		}
		super.setVisible(visible);
	}
	
	public ProjectRow getSelectedProject() {
		return selectedProject;
	}

	public String getNewProjectTitle() {
		return newProjectTitle;
	}
	
	private boolean canFinish() {
		if (null == projectTitles) {
			return false;
		}
		setErrorMessage(null);
		boolean pageComplete;
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		selectedProject = (ProjectRow)selection.getFirstElement();
		if (!newProjectButton.getSelection() && selection.isEmpty()) {
			pageComplete = false;
		}
		else if (newProjectButton.getSelection() && 0 == newProjectText.getText().trim().length()) {
			pageComplete = false;
		} else {
			pageComplete = true;
		}
		if (pageComplete) {
			if (newProjectButton.getSelection() && projectTitles.contains(newProjectText.getText().trim())) {
				setErrorMessage("Project " + newProjectText.getText().trim() + " already exists.");
				pageComplete = false;
			}
		}
		newProjectText.setEnabled(newProjectButton.getSelection());
		return pageComplete;
	}

	private ProjectRow[] getProjects() {
		getProjectsError = null;
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				String taskName = "Retrieving TeamForge projects";
				monitor.setTaskName(taskName);
				monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
				monitor.subTask("");
				try {
					projects = ((ProjectMappingWizard)getWizard()).getSoapClient().getAllProjects();
					Arrays.sort(projects, new Comparator<ProjectRow>() {
						public int compare(ProjectRow p1, ProjectRow p2) {
							return p1.getTitle().toLowerCase().compareTo(p2.getTitle().toLowerCase());
						}
					});
					projectTitles = new ArrayList<String>();
					for (ProjectRow project : projects) {
						projectTitles.add(project.getTitle());
					}
				} catch (final RemoteException e) {
					Activator.handleError(e);
					getProjectsError = e;
				}
				monitor.done();
			}
		};
		
		try {
			getContainer().run(true, false, runnable);
		} catch (Exception e) {
			Activator.handleError(e);
			getProjectsError = e;
		}
		
		if (null != getProjectsError) {
			setErrorMessage("An unexpected error occurred while getting TeamForge projects.  See error log for details.");
		}

		return projects;
	}

	static class ProjectsLabelProvider extends LabelProvider implements ITableLabelProvider {

		public String getColumnText(Object element, int columnIndex) {
			ProjectRow project = (ProjectRow)element;
			switch (columnIndex) { 
				case 0: return project.getTitle();
			}
			return "";  //$NON-NLS-1$
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	
	}	
	
	class ProjectsContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public Object[] getElements(Object obj) {
			if (null == projects) {
				return new ProjectRow[0];
			}
			return projects;
		}
	}	

}
