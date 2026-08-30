package com.collabnet.ccf.pt.schemageneration;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.axis.EngineConfiguration;

import com.collabnet.core.ws.exception.WSException;
import com.collabnet.helm.ws.PaginationType;
import com.collabnet.helm.ws.SimpleProjectType;
import com.collabnet.helm.ws.domain.Domain;
import com.collabnet.helm.ws.domain.DomainService;
import com.collabnet.helm.ws.domain.DomainServiceLocator;
import com.collabnet.helm.ws.domain.ProjectsForUserContainer;
import com.collabnet.helm.ws.project.Project;
import com.collabnet.helm.ws.project.ProjectServiceLocator;
import com.collabnet.tracker.common.WebServiceClient;
import com.collabnet.tracker.core.TrackerClientManager;
import com.collabnet.tracker.ws.ArtifactType;
import com.collabnet.tracker.ws.Metadata;
import com.collabnet.tracker.ws.MetadataServiceLocator;

public class PTClient {
	private String serverUrl;
	private String userId;
	private String password;
	private WebServiceClient mClient;
	private Domain domainService;

	private static Map<String, PTClient> clients = new HashMap<String, PTClient>();
	
	public static final String PROJECT_TYPE_PT = "Project Tracker";

	public PTClient(String serverUrl, String userId, String password) {
		this.serverUrl = serverUrl;
		this.userId = userId;
		this.password = password;
		clients.put(serverUrl + userId + password, this);
		try {
			TrackerClientManager.getInstance().createClient(serverUrl, userId, password, null, null);
		} catch (MalformedURLException e) {}
	}
	
	public static PTClient getClient(String serverUrl, String userId, String password) {
		PTClient client = clients.get(serverUrl + userId + password);
		if (null == client) {
			client = new PTClient(serverUrl, userId, password);
		}
		return client;
	}
	
	public List<String> getProjects(String projectType) throws MalformedURLException, ServiceException, WSException, RemoteException {
		List<SimpleProjectType> projects = new ArrayList<SimpleProjectType>();
		Domain domainService  = getService();
		ProjectsForUserContainer userProjects;
		userProjects = domainService.getProjectsForUser(
				userId,	new PaginationType(100,0));
		for (SimpleProjectType project : userProjects.getProjects()) {
			projects.add(project);
		}
		
		int totalPagesCount = userProjects.getPaginationResult().getTotalNumberOfPages();
		if (1 < totalPagesCount) {
			for (int i = 2; i <= totalPagesCount; i++) {
				userProjects = domainService.getProjectsForUser(
						userId,	new PaginationType(100,i));
				for (SimpleProjectType project : userProjects.getProjects()) {
					projects.add(project);
				}
			}
		}
		List<String> projectNames = new ArrayList<String>();
		for (SimpleProjectType project : projects) {
			boolean includeProject = false;
			if (null == projectType) includeProject = true;
			else {
				try {
					Project projectService = getProjectService(getProjectUrl(project));
					includeProject = projectService.getProjectInfo().getTrackingSystem().getName().equals(projectType);
				} catch (Exception e) {}
			}

			if (includeProject) projectNames.add(project.getName());
		}
		return projectNames;
	}
	
	public List<String> getArtifactTypes(String projectUrl) throws MalformedURLException, ServiceException, WSException, RemoteException {
		List<String> artifactTypeList = new ArrayList<String>();
		Metadata metadata = getMetadataService(projectUrl);
		ArtifactType[] artifactTypes = metadata.getArtifactTypes();
		for (ArtifactType artifactType : artifactTypes) {
			artifactTypeList.add(artifactType.getDisplayName());
		}
		return artifactTypeList;
	}
	
	private String getProjectUrl(SimpleProjectType project) {
		String baseurl = serverUrl;
		int prefixIndex = baseurl.indexOf("//"); //$NON-NLS-1$
		int postfixIndex = baseurl.indexOf("."); //$NON-NLS-1$
		
		return baseurl.substring(0, prefixIndex+2) 
		 	+ project.getName()
		 	+ baseurl.substring(postfixIndex);
	}
	
	public Domain getService() throws ServiceException, MalformedURLException {
		if (null == domainService) {
			if (null == mClient) {
				mClient = new WebServiceClient();
				mClient.init(userId, password, serverUrl);
			}
			EngineConfiguration config = mClient.getEngineConfiguration();
	        DomainService service = new DomainServiceLocator(config);
	        URL portAddress = this.mClient.constructServiceURL("/helm/Domain"); //$NON-NLS-1$
	        this.domainService = service.getDomain(portAddress);
		}
		return domainService;
	}
	
	public Project getProjectService(String projectUrl) throws ServiceException, MalformedURLException {
		WebServiceClient wsClient = new WebServiceClient();
		wsClient.init(userId, password, projectUrl);
		EngineConfiguration config = wsClient.getEngineConfiguration();
        ProjectServiceLocator service = new ProjectServiceLocator(config);
        URL portAddress = wsClient.constructServiceURL("/helm/Project"); //$NON-NLS-1$
        
        if (null == TrackerClientManager.getInstance().getClient(projectUrl)) {
    		try {
    			TrackerClientManager.getInstance().createClient(projectUrl, userId, password, null, null);
    		} catch (MalformedURLException e) {}        	
        }
        
        return service.getProject(portAddress);
    }
	
	public Metadata getMetadataService(String projectUrl) throws ServiceException, MalformedURLException {
		WebServiceClient wsClient = new WebServiceClient();
		wsClient.init(userId, password, projectUrl);
		EngineConfiguration config = wsClient.getEngineConfiguration();
        MetadataServiceLocator service = new MetadataServiceLocator(config);
        URL portAddress = wsClient.constructServiceURL("/tracker/Metadata"); //$NON-NLS-1$
        
        if (null == TrackerClientManager.getInstance().getClient(projectUrl)) {
    		try {
    			TrackerClientManager.getInstance().createClient(projectUrl, userId, password, null, null);
    		} catch (MalformedURLException e) {}        	
        }
        
        return service.getMetadataService(portAddress);
    }
}
