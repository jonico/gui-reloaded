package com.collabnet.ccf.teamforge.dialogs;

import java.rmi.RemoteException;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
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
import com.collabnet.ccf.teamforge.schemageneration.TFSoapClient;
import com.collabnet.teamforge.api.main.ProjectRow;
import com.collabnet.teamforge.api.tracker.TrackerRow;

public class TeamForgeSelectionDialog extends CcfDialog {
	private Landscape landscape;
	private int type;
	@SuppressWarnings("unused")
	private int systemNumber;
	private TreeViewer treeViewer;
	private String projectId;
	private String trackerId;
	private TFSoapClient soapClient;
	private ProjectRow[] projects;
	private TrackerRow[] trackers;
	private String title;
	
	public static final int BROWSER_TYPE_TRACKER = 0;
	public static final int BROWSER_TYPE_PROJECT = 1;
	
	private Button okButton;

	public TeamForgeSelectionDialog(Shell parentShell, Landscape landscape, int type, int systemNumber) {
		super(parentShell, "TeamForgeSelectionDialog");
		this.landscape = landscape;
		this.type = type;
		this.systemNumber = systemNumber;
	}
	
	protected Control createDialogArea(Composite parent) {
		switch (type) {
		case BROWSER_TYPE_PROJECT:
			title = "Select Project";
			break;
		default:
			title = "Select Tracker";
			break;
		}
		
		getShell().setText(title);
		
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));	
		
		treeViewer = new TreeViewer(composite);
		treeViewer.setLabelProvider(new TeamForgeSelectionLabelProvider());
		treeViewer.setContentProvider(new TeamForgeSelectionContentProvider());
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
        
        treeViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof ProjectRow && e2 instanceof ProjectRow) {
					ProjectRow p1 = (ProjectRow)e1;
					ProjectRow p2 = (ProjectRow)e2;
					return p1.getTitle().compareTo(p2.getTitle());
				}
				return super.compare(viewer, e1, e2);
			}
        });
		
		return composite;
	}
	
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        Button button = super.createButton(parent, id, label, defaultButton);
		if (IDialogConstants.OK_ID == id) {
			okButton = button;
			okButton.setEnabled(false);
		}
        return button;
    }
	
	@Override
	protected void okPressed() {
		IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
		Object firstSelection = selection.getFirstElement();
		if (firstSelection instanceof ProjectRow) {
			projectId = ((ProjectRow)firstSelection).getId();
		}
		if (firstSelection instanceof TrackerRow) {
			projectId = ((TrackerRow)firstSelection).getProjectId();
			trackerId = ((TrackerRow)firstSelection).getId();
		}
		super.okPressed();
	}

	public String getProjectId() {
		return projectId;
	}

	public String getTrackerId() {
		return trackerId;
	}
	
	public String getSelectedId() {
		if (BROWSER_TYPE_PROJECT == type) return getProjectId();
		else return getTrackerId();
	}
	
	private TFSoapClient getSoapClient() {
		if (null == soapClient) {
			Properties properties = null;
			if (landscape.getType1().equals("TF")) {
				properties = landscape.getProperties1();
			} else {
				properties = landscape.getProperties2();
			}
			if (null != properties) {
				String serverUrl = properties.getProperty(Activator.PROPERTIES_SFEE_URL);
				String userId = properties.getProperty(Activator.PROPERTIES_SFEE_USER);
				String password = Activator.decodePassword(properties.getProperty(Activator.PROPERTIES_SFEE_PASSWORD));
				soapClient = TFSoapClient.getSoapClient(serverUrl, userId, password);
			}
		}
		return soapClient;
	}

	private boolean canFinish() {
		IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
		if (selection.isEmpty()) return false;
		Object firstSelection = selection.getFirstElement();
		if (BROWSER_TYPE_PROJECT == type) return (firstSelection instanceof ProjectRow);
		else return (firstSelection instanceof TrackerRow);
	}
	
	private ProjectRow[] getProjects() {
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				projects = null;
				try {
					projects = getSoapClient().getAllProjects();
				} catch (RemoteException e) {
					Activator.handleError(e);
					ExceptionDetailsErrorDialog.openError(getShell(), title, e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
				}
			}
		});
		if (null == projects) return new ProjectRow[0];
		else return projects;
	}
	
	private TrackerRow[] getTrackers(final String projectId) {
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				trackers = null;
				try {
					trackers = getSoapClient().getAllTrackersOfProject(projectId);
				} catch (RemoteException e) {
					Activator.handleError(e);
					ExceptionDetailsErrorDialog.openError(getShell(), title, e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
				}
			}
		});
		if (null == trackers) return new TrackerRow[0];
		else return trackers;
	}
	
	class TeamForgeSelectionLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			return null;
		}
		public String getText(Object element) {
			if (element instanceof ProjectRow) {
				return ((ProjectRow)element).getTitle();
			}
			if (element instanceof TrackerRow) {
				return ((TrackerRow)element).getTitle();
			}
			return super.getText(element);
		}
	}
	
	class TeamForgeSelectionContentProvider extends WorkbenchContentProvider {
		public Object[] getChildren(Object element) {
			if (element instanceof TeamForgeSelectionDialog) {
				return getProjects();
			}
			if (element instanceof ProjectRow) {
				return getTrackers(((ProjectRow)element).getId());
			}
			return new Object[0];
		}
		public Object[] getElements(Object element) {
			return getChildren(element);
		}
		public boolean hasChildren(Object element) {
			if (element instanceof TrackerRow) return false;
			if (element instanceof ProjectRow) {
				return type == BROWSER_TYPE_TRACKER;
			}
			return true;
		}
	}

}
