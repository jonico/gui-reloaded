package com.collabnet.ccf.qc.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchContentProvider;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.dialogs.CcfDialog;
import com.collabnet.ccf.dialogs.ExceptionDetailsErrorDialog;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.qc.schemageneration.QCLayoutExtractor;

public class DomainProjectSelectionDialog extends CcfDialog {
	private Landscape landscape;
	private int type;
	private String title;
	private QCLayoutExtractor qcLayoutExtractor;
	private List<Domain> domains = new ArrayList<DomainProjectSelectionDialog.Domain>();
	private List<Project> domainProjects;
	private Map<String, List<Project>> projectMap = new HashMap<String, List<Project>>();
	
	private TreeViewer treeViewer;
	private Button okButton;
	
	private String domain;
	private String project;
	
	private Domain previouslySelectedDomain;
	private Project previouslySelectedProject;
	
	public static final int BROWSER_TYPE_DOMAIN = 0;
	public static final int BROWSER_TYPE_PROJECT = 1;

	public DomainProjectSelectionDialog(Shell shell, Landscape landscape, String domain, String project, int type) {
		super(shell, "DomainProjectSelectionDialog");
		this.landscape = landscape;
		this.domain = domain;
		this.project = project;
		this.type = type;
	}
	
	protected Control createDialogArea(Composite parent) {
		getDomains();
		
		switch (type) {
		case BROWSER_TYPE_DOMAIN:
			title = "Select Domain";
			break;
		default:
			title = "Select Project";
			break;
		}		
		getShell().setText(title);
		
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		treeViewer = new TreeViewer(composite);
		treeViewer.setLabelProvider(new DomainProjectLabelProvider());
		treeViewer.setContentProvider(new DomainProjectSelectionContentProvider());
		treeViewer.setUseHashlookup(true);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL);
		layoutData.heightHint = 300;
		layoutData.widthHint = 300;
		treeViewer.getControl().setLayoutData(layoutData);
		treeViewer.setInput(this);
		
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (null != okButton) {
					okButton.setEnabled(canFinish());
				}
			}		
		});
		
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent e) {
                if (okButton.isEnabled()) okPressed();
            }
        });  
        
        if (null != previouslySelectedDomain) {
        	if (BROWSER_TYPE_PROJECT == type) {
        		treeViewer.expandToLevel(previouslySelectedDomain, TreeViewer.ALL_LEVELS);
        		treeViewer.reveal(previouslySelectedDomain);
        	} else {
        		treeViewer.setSelection(getSelection(previouslySelectedDomain));
        	}
        }
        
        if (BROWSER_TYPE_PROJECT == type && null != previouslySelectedProject) {
        	treeViewer.setSelection(getSelection(previouslySelectedProject));
        }
 
		return composite;
	}

	@SuppressWarnings("rawtypes")
	private ISelection getSelection(final Object object) {
		ISelection selection = new IStructuredSelection() {			
			public boolean isEmpty() {
				return false;
			}			
			@SuppressWarnings("unchecked")
			public List toList() {
				List list = new ArrayList();
				list.add(object);
				return list;
			}
			
			public Object[] toArray() {
				return toList().toArray();
			}			
			public int size() {
				return 1;
			}			
			public Iterator iterator() {
				return toList().iterator();
			}			
			public Object getFirstElement() {
				return object;
			}
		};
		return selection;
	}
	
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        Button button = super.createButton(parent, id, label, defaultButton);
		if (IDialogConstants.OK_ID == id) {
			okButton = button;
			if (BROWSER_TYPE_DOMAIN == type && null == previouslySelectedDomain) {
				okButton.setEnabled(false);
			}
			if (BROWSER_TYPE_PROJECT == type && null == previouslySelectedProject) {
				okButton.setEnabled(false);
			}
		}
        return button;
    }
	
	protected void okPressed() {
		IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
		Object firstSelection = selection.getFirstElement();
		if (firstSelection instanceof Domain) {
			Domain selectedDomain = (Domain)firstSelection;
			domain = selectedDomain.toString();
		}
		if (firstSelection instanceof Project) {
			Project selectedProject = (Project)firstSelection;
			domain = selectedProject.getDomain();
			project = selectedProject.toString();
		}
		super.okPressed();
	}
	
	private boolean canFinish() {
		IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
		if (selection.isEmpty()) return false;
		Object firstSelection = selection.getFirstElement();
		if (BROWSER_TYPE_PROJECT == type) return (firstSelection instanceof Project);
		else return (!(firstSelection instanceof Project));
	}
	
	public String getDomain() {
		return domain;
	}

	public String getProject() {
		return project;
	}

	private void getDomains() {
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				try {
					qcLayoutExtractor = new QCLayoutExtractor();
					Properties properties;
					if (landscape.getType2().equals("QT")) {
						properties = landscape.getProperties2();
					} else {
						properties = landscape.getProperties1();
					}
					String url = properties.getProperty(Activator.PROPERTIES_QC_URL, "");
					String user = properties.getProperty(Activator.PROPERTIES_QC_USER, "");
					String password = Activator.decodePassword(properties.getProperty(
							Activator.PROPERTIES_QC_PASSWORD, ""));
					qcLayoutExtractor.setServerUrl(url);
					qcLayoutExtractor.setUserName(user);
					qcLayoutExtractor.setPassword(password);
					List<String> domainList = qcLayoutExtractor.getDomains();
					domains = new ArrayList<DomainProjectSelectionDialog.Domain>();
					for (String qcDomain : domainList) {
						Domain addDomain = new Domain(qcDomain);
						if (null != domain && domain.equals(qcDomain)) {
							previouslySelectedDomain = addDomain;
						}
						domains.add(addDomain);
					}
				} catch (Exception e) {
					Activator.handleError(e);
					ExceptionDetailsErrorDialog.openError(getShell(), "Select Project", e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
				}
			}			
		});		
	}
	
	private List<Project> getProjects(final String domain) {
		domainProjects = projectMap.get(domain);
		if (null == domainProjects) {
			BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
				public void run() {
					try {
						List<String> projectList = qcLayoutExtractor.getProjects(domain);
						if (null != projectList) {
							domainProjects = new ArrayList<DomainProjectSelectionDialog.Project>();
							for (String qcProject : projectList) {
								Project addProject = new Project(domain, qcProject);
								if (null != previouslySelectedDomain && domain.equals(previouslySelectedDomain.toString()) && null != project && project.equals(qcProject)) {
									previouslySelectedProject = addProject;
								}
								domainProjects.add(addProject);
							}
							projectMap.put(domain, domainProjects);
						}
					} catch (Exception e) {
						Activator.handleError(e);
						ExceptionDetailsErrorDialog.openError(getShell(), "Select Project", e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
					}
				}			
			});		
		}
		return domainProjects;
	}
	
	class DomainProjectSelectionLabelProvider extends LabelProvider {
		// TODO icons?
		public Image getImage(Object element) {
			return null;
		}
		public String getText(Object element) {
			return super.getText(element);
		}
	}	
	
	class DomainProjectSelectionContentProvider extends WorkbenchContentProvider {
		public Object[] getChildren(Object element) {
			if (element instanceof DomainProjectSelectionDialog) {
				return domains.toArray();
			}
			if (element instanceof Domain) {
				Domain domain = (Domain)element;
				domainProjects = getProjects(domain.toString());
				if (null != domainProjects) {
					return domainProjects.toArray();
				}
			}
			return new Object[0];
		}
		public Object[] getElements(Object element) {
			return getChildren(element);
		}
		public boolean hasChildren(Object element) {
			if (element instanceof Domain) {
				return type == BROWSER_TYPE_PROJECT;
			}
			return false;
		}
	}
	
	class Domain {
		private String name;
		
		public Domain(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
		
	}
	
	class Project {
		private String domain;
		private String name;
		
		public Project(String domain, String name) {
			this.domain = domain;
			this.name = name;
		}

		public String getDomain() {
			return domain;
		}

		public String toString() {
			return name;
		}
		
	}
	
	class DomainProjectLabelProvider extends LabelProvider {
		
	}

}
