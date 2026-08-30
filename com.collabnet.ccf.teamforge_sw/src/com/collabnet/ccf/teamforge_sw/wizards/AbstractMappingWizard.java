package com.collabnet.ccf.teamforge_sw.wizards;

import java.net.MalformedURLException;
import java.util.Properties;

import org.eclipse.jface.wizard.Wizard;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.MappingGroup;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.sw.SWPMetaData;
import com.collabnet.ccf.teamforge.schemageneration.TFSoapClient;
import com.danube.scrumworks.api2.client.ScrumWorksAPIService;

public abstract class AbstractMappingWizard extends Wizard {
	private TFSoapClient soapClient;
	private SynchronizationStatus projectMapping;
	private ScrumWorksAPIService scrumWorksEndpoint;

	public AbstractMappingWizard(SynchronizationStatus projectMapping) {
		super();
		this.projectMapping = projectMapping;
	}
	
	public AbstractMappingWizard(MappingGroup mappingGroup) {
		super();
		projectMapping = mappingGroup.getFirstMapping();
	}
	
	@Override
	public boolean needsProgressMonitor() {
		return true;
	}

	public SynchronizationStatus getProjectMapping() {
		return projectMapping;
	}

	public TFSoapClient getSoapClient() {
		if (null == soapClient) {
			Landscape landscape = projectMapping.getProjectMappings().getLandscape();
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
	
//	public ScrumWorksEndpoint getScrumWorksEndpoint() throws ServiceException {
//		if (scrumWorksEndpoint == null) {
//			Landscape landscape = projectMapping.getProjectMappings().getLandscape();
//			scrumWorksEndpoint = com.collabnet.ccf.sw.Activator.getScrumWorksEndpoint(landscape);
//		}
//		return scrumWorksEndpoint;
//	}
	
	public ScrumWorksAPIService getScrumWorksEndpoint() throws MalformedURLException {
		if (null == scrumWorksEndpoint) {
			Landscape landscape = projectMapping.getProjectMappings().getLandscape();
			scrumWorksEndpoint = com.collabnet.ccf.sw.Activator.getScrumWorksEndpoint(landscape);
		}
		return scrumWorksEndpoint;
	}
	
	public String getProduct() {
		String repositoryId = null;
		if (projectMapping.getSourceRepositoryId().endsWith("-PBI") ||
		    projectMapping.getSourceRepositoryId().endsWith("-Task") ||
		    projectMapping.getSourceRepositoryId().endsWith("-MetaData") ||
	        projectMapping.getSourceRepositoryId().endsWith("-Product") ||
	        projectMapping.getSourceRepositoryId().endsWith("-Release")) {
			repositoryId = projectMapping.getSourceRepositoryId();
		}
		else if (projectMapping.getTargetRepositoryId().endsWith("-PBI") ||
			    projectMapping.getTargetRepositoryId().endsWith("-Task") ||
			    projectMapping.getTargetRepositoryId().endsWith("-MetaData") ||
		        projectMapping.getTargetRepositoryId().endsWith("-Product") ||
		        projectMapping.getTargetRepositoryId().endsWith("-Release")) {
				repositoryId = projectMapping.getTargetRepositoryId();
		}
		if (null != repositoryId) {
			int index = repositoryId.lastIndexOf("(");
			if (-1 == index) {
				index = repositoryId.lastIndexOf("-");
			}
			String product = repositoryId.substring(0, index);
			return product;
		}
		return null;
	}
	
	public Long getProductId() {
		String repositoryId = null;
		if (projectMapping.getSourceRepositoryId().endsWith("-PBI") ||
		    projectMapping.getSourceRepositoryId().endsWith("-Task") ||
		    projectMapping.getSourceRepositoryId().endsWith("-MetaData") ||
	        projectMapping.getSourceRepositoryId().endsWith("-Product") ||
	        projectMapping.getSourceRepositoryId().endsWith("-Release")) {
			
			repositoryId = projectMapping.getSourceRepositoryId();
		}
		else if (projectMapping.getTargetRepositoryId().endsWith("-PBI") ||
			    projectMapping.getTargetRepositoryId().endsWith("-Task") ||
			    projectMapping.getTargetRepositoryId().endsWith("-MetaData") ||
		        projectMapping.getTargetRepositoryId().endsWith("-Product") ||
		        projectMapping.getTargetRepositoryId().endsWith("-Release")) {
				repositoryId = projectMapping.getTargetRepositoryId();
		}
		return SWPMetaData.retrieveProductIdFromRepositoryId(repositoryId);
	}
	
	public String getTracker() {
		String tracker = null;
		if (projectMapping.getSourceRepositoryId().startsWith("tracker")) {
			tracker = projectMapping.getSourceRepositoryId();
		}
		else if (projectMapping.getTargetRepositoryId().startsWith("tracker")) {
			tracker = projectMapping.getTargetRepositoryId();
		}
		return tracker;		
	}
	
	public String getProject() {
		String project = null;
		if (projectMapping.getSourceRepositoryId().endsWith("-planningFolders")) {
			project = projectMapping.getSourceRepositoryId().substring(0, projectMapping.getSourceRepositoryId().indexOf("-"));
		}
		else if (projectMapping.getTargetRepositoryId().endsWith("-planningFolders")) {
			project = projectMapping.getTargetRepositoryId().substring(0, projectMapping.getTargetRepositoryId().indexOf("-"));
		}
		return project;
	}

}
