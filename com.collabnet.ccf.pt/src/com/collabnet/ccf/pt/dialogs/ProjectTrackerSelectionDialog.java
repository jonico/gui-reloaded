package com.collabnet.ccf.pt.dialogs;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.MessageContext;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.CommonsHTTPSender;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import com.collabnet.ccf.pt.schemageneration.PTClient;
import com.collabnet.ccf.schemageneration.Proxy;
import com.collabnet.core.ws.exception.WSException;
import com.collabnet.teamforge.api.Connection;
import com.collabnet.teamforge.api.main.ProjectList;
import com.collabnet.teamforge.api.main.ProjectRow;
import com.collabnet.teamforge.api.main.TeamForgeClient;

public class ProjectTrackerSelectionDialog extends CcfDialog {
	private Properties properties;
	private int type;
	private TreeViewer treeViewer;
	
	private String title;

	private PTClient ptClient;
	private TeamForgeClient tfClient;
	private Project[] projects;
	private ArtifactType[] artifactTypes;
	private String projectName;
	private String artifactType;
	
	public static final int BROWSER_TYPE_PROJECT = 0;
	public static final int BROWSER_TYPE_ARTIFACT_TYPE = 1;
	
	public static final String HTTP = "http://";
	public static final String HTTPS = "https://";
	public static final String WWW = "www.";
	
	private Button okButton;

	public ProjectTrackerSelectionDialog(Shell shell, Landscape landscape, int type, int systemNumber) {
		super(shell, "ProjectTrackerSelectionDialog");
		this.type = type;
		switch (systemNumber) {
		case 1:
			if (!landscape.getType1().equals("PT") && landscape.getType2().equals("PT")) {
				properties = landscape.getProperties2();
			} else {
				properties = landscape.getProperties1();
			}
			break;
		case 2:
			if (!landscape.getType2().equals("PT") && landscape.getType1().equals("PT")) {
				properties = landscape.getProperties1();
			} else {
				properties = landscape.getProperties2();
			}
			break;
		default:
			break;
		}
	}
	
	protected Control createDialogArea(Composite parent) {
		switch (type) {
		case BROWSER_TYPE_PROJECT:
			title = "Select Project";
			break;
		default:
			title = "Select Artifact Type";
			break;
		}
		
		getShell().setText(title);
		
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));	
		
		treeViewer = new TreeViewer(composite);
		treeViewer.setLabelProvider(new ProjectTrackerSelectionLabelProvider());
		treeViewer.setContentProvider(new ProjectTrackerSelectionContentProvider());
		treeViewer.setUseHashlookup(true);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL);
		layoutData.heightHint = 300;
		layoutData.widthHint = 300;
		treeViewer.getControl().setLayoutData(layoutData);
		treeViewer.setInput(this);
		
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (okButton != null) {
					okButton.setEnabled(canFinish());
				}
			}		
		});
		
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent e) {
                if (okButton.isEnabled()) okPressed();
            }
        });  
		
		return composite;
	}
	
	@Override
	protected void okPressed() {
		IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
		Object firstSelection = selection.getFirstElement();
		if (firstSelection instanceof Project project) {
			if (project.getInternalName() == null) {
				projectName = project.getName();
			} else {
				projectName = project.getInternalName();
			}
		}
		if (firstSelection instanceof ArtifactType artifactType1) {
			projectName = artifactType1.getProjectName();
			artifactType = artifactType1.getName();
		}
		super.okPressed();
	}
	
	public String getProjectName() {
		return projectName;
	}

	public String getArtifactType() {
		return artifactType;
	}

	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        Button button = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID) {
			okButton = button;
			okButton.setEnabled(false);
		}
        return button;
    }
	
	private boolean canFinish() {
		IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
		if (selection.isEmpty()) return false;
		Object firstSelection = selection.getFirstElement();
		if (type == BROWSER_TYPE_PROJECT) return (firstSelection instanceof Project);
		else return (!(firstSelection instanceof Project));
	}

	// TODO  Filter list to only include Project Tracker projects.  This fix can be done once the
	//       5.4 SOAP API changes have been merged into the 6.x SOAP API.
	//       At that time we will use IRbacAppSoap..getProjectsByIntegratedAppPermission to get a
	//       list of all PT project IDs.  The list returned by ICollabNetSoap.getProjectList will
	//       then be filtered to include only projects whose ID is included in the list from the
	//       first call.
	private Project[] getTeamForgeProjects() {
		try {
			ProjectList projectList = getTeamForgeClient().getProjectList();
			ProjectRow[] projectRows = projectList.getDataRows();
			projects = new Project[projectRows.length];
			for (int i = 0; i < projectRows.length; i++) {
				String internalName;
				if (projectRows[i].getPath() == null || !projectRows[i].getPath().startsWith("projects.")) {
					internalName = projectRows[i].getTitle();
				} else {
					internalName = projectRows[i].getPath().substring(9);
				}
				projects[i] = new Project(projectRows[i].getId(), internalName, projectRows[i].getTitle(), getProjectUrl(internalName));
			}
		} catch (Exception e) {
			Activator.handleError(e);
			ExceptionDetailsErrorDialog.openError(getShell(), title, e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
		}
		if (projects == null) return new Project[0];
		else return projects;
	}
	
	private Project[] getProjects() {
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				projects = null;
				try {
					List<String> projectList = getClient().getProjects(PTClient.PROJECT_TYPE_PT);
					projects = new Project[projectList.size()];
					int i = 0;
					for (String projectName : projectList) {
						projects[i++] = new Project(null, null, projectName, getProjectUrl(projectName));
					}
				} catch (Exception e) {
					if (e.getMessage() != null && e.getMessage().indexOf("could not find a target service") != -1) {
						projects = getTeamForgeProjects();
					} else {
						Activator.handleError(e);
						ExceptionDetailsErrorDialog.openError(getShell(), title, e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));						
					}
				}
			}
		});
		if (projects == null) return new Project[0];
		else return projects;
	}

	private ArtifactType[] getArtifactTypes(final Project project) {
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				artifactTypes = null;
				try {
					List<String> artifactTypeList = getClient().getArtifactTypes(project.getUrl());
					artifactTypes = new ArtifactType[artifactTypeList.size()];
					int i = 0;
					for (String artifactTypeName : artifactTypeList) {
						String internalName;
						if (project.getInternalName() == null) {
							internalName = project.getName();
						} else {
							internalName = project.getInternalName();
						}
						artifactTypes[i++] = new ArtifactType(artifactTypeName, internalName);
					}
				} catch (Exception e) {
					// For now we are just catching the error when a non-PT project is expanded.
					// Eventually, these projects will not be included in the pick list.
					if (e.getMessage() != null && e.getMessage().contains("while initializing the call context")) {
						return;
					}
					if (e instanceof WSException exception && e.getMessage() != null && e.getMessage().contains("View artifact")) {
						if (exception.getCauseExceptionType() != null && exception.getCauseExceptionType().contains("AccessControlException")) {
							MessageDialog.openError(getShell(), title, "You do not have permission to view artifact types for project " + project.getName() + ".");
							return;
						}
					}
					Activator.handleError(e);
					ExceptionDetailsErrorDialog.openError(getShell(), title, e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
				}
			}
		});
		if (artifactTypes == null) return new ArtifactType[0];
		else return artifactTypes;		
	}
	
	private String getProjectUrl(String projectName) {
		String baseurl = properties.getProperty(Activator.PROPERTIES_CEE_URL);
		if (baseurl != null) {
			if (baseurl.toLowerCase().startsWith(HTTP)) {
				if (!baseurl.toLowerCase().startsWith(HTTP + projectName + ".")) { //$NON-NLS-1$
					return baseurl.replaceAll(HTTP, HTTP + projectName + "."); //$NON-NLS-1$
				}
			}
			if (baseurl.toLowerCase().startsWith(HTTPS)) {
				if (!baseurl.toLowerCase().startsWith(HTTPS + projectName + ".")) { //$NON-NLS-1$
					return baseurl.replaceAll(HTTPS, HTTPS + projectName + "."); //$NON-NLS-1$
				}		
			}						
		}
		return baseurl;
	}
	
	private PTClient getClient() {
		if (ptClient == null) {
			String serverUrl = getPickerUrl(properties.getProperty(Activator.PROPERTIES_CEE_URL));
			String userId = properties.getProperty(Activator.PROPERTIES_CEE_USER);
			String password = Activator.decodePassword(properties.getProperty(Activator.PROPERTIES_CEE_PASSWORD));
			ptClient = PTClient.getClient(serverUrl, userId, password);
		}
		return ptClient;
	}
	
	private TeamForgeClient getTeamForgeClient() throws RemoteException {
		if (tfClient == null) {
			String serverUrl = getPickerUrl(properties.getProperty(Activator.PROPERTIES_CEE_URL));
			String userId = properties.getProperty(Activator.PROPERTIES_CEE_USER);
			String password = Activator.decodePassword(properties.getProperty(Activator.PROPERTIES_CEE_PASSWORD));
			Connection connection = Connection.builder(serverUrl)
			.userNamePassword(userId, password)
			.httpAuth(null, null)
			.proxy(null)
			.build();
			Connection.setEngineConfiguration(getEngineConfiguration());	
			tfClient = connection.getTeamForgeClient();
		}
		return tfClient;
	}
	
	private static String getPickerUrl(String serverUrl) {
		if (serverUrl != null) {
			if (serverUrl.toLowerCase().startsWith(HTTP)) {
				if (!serverUrl.toLowerCase().startsWith(HTTP + WWW)) {
					return serverUrl.replaceAll(HTTP, HTTP + WWW);
				}
			}
			if (serverUrl.toLowerCase().startsWith(HTTPS)) {
				if (!serverUrl.toLowerCase().startsWith(HTTPS + WWW)) {
					return serverUrl.replaceAll(HTTPS, HTTPS + WWW);
				}		
			}			
		}
		return serverUrl;
	}
	
	public static EngineConfiguration getEngineConfiguration() {
		SimpleProvider config = new SimpleProvider();
		config.deployTransport("http", new SimpleTargetedChain(new TeamForgeHTTPSender())); //$NON-NLS-1$
		config.deployTransport("https", new SimpleTargetedChain(new TeamForgeHTTPSender())); //$NON-NLS-1$
		return config;
	}		
	
	class ProjectTrackerSelectionLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			return null;
		}
		public String getText(Object element) {
			if (element instanceof Project project) {
				return project.getName();
			}
			if (element instanceof ArtifactType artifactType1) {
				return artifactType1.getName();
			}
			return super.getText(element);
		}
	}
	
	class ProjectTrackerSelectionContentProvider extends WorkbenchContentProvider {
		public Object[] getChildren(Object element) {
			if (element instanceof ProjectTrackerSelectionDialog) {
				return getProjects();
			}
			if (element instanceof Project project) {
				return getArtifactTypes(project);
			}
			return new Object[0];
		}
		public Object[] getElements(Object element) {
			return getChildren(element);
		}
		public boolean hasChildren(Object element) {
			if (element instanceof Project) {
				return type == BROWSER_TYPE_ARTIFACT_TYPE;
			}
			return false;
		}
	}
	
	class Project {
		private String name;
		private String url;
		private String id;
		private String internalName;
		
		public Project(String id, String internalName, String name, String url) {
			this.id = id;
			this.internalName = internalName;
			this.name = name;
			this.url = url;
		}
		
		public String getId() {
			return id;
		}

		public String getInternalName() {
			return internalName;
		}

		public String getName() {
			return name;
		}

		public String getUrl() {
			return url;
		}
		
	}
	
	class ArtifactType {
		private String name;
		private String projectName;
		
		public ArtifactType(String name, String projectName) {
			super();
			this.name = name;
			this.projectName = projectName;
		}

		public String getName() {
			return name;
		}

		public String getProjectName() {
			return projectName;
		}
		
	}
	
	static class TeamForgeHTTPSender extends CommonsHTTPSender {

		private static final long serialVersionUID = 1L;

		@Override
		protected HostConfiguration getHostConfiguration(HttpClient client, MessageContext context, URL url) {
			Proxy proxy = Activator.getPlatformProxy(url.toString());
			if (proxy != null) {
				proxy.setProxy(client);
			}

			// This needs to be set to 1.0 otherwise errors
			client.getHostConfiguration().getParams().setParameter(HttpClientParams.PROTOCOL_VERSION, HttpVersion.HTTP_1_0);		
			return client.getHostConfiguration();
		}
		
	}

}
