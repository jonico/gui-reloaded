package com.collabnet.ccf.teamforge_sw;

import java.rmi.RemoteException;
import java.util.Properties;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.IProjectMappingChangeListener;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.db.Filter;
import com.collabnet.ccf.db.Update;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.sw.ScrumWorksMappingSection;
import com.collabnet.ccf.teamforge.schemageneration.TFSoapClient;

public class TeamForgeSwpProjectMappingChangeListener implements IProjectMappingChangeListener {

	public void projectMappingChanged(final SynchronizationStatus projectMapping) {
		if (projectMapping.getSourceSystemId().equals(com.collabnet.ccf.sw.Activator.SYSTEM_ID) || projectMapping.getTargetSystemId().equals(com.collabnet.ccf.sw.Activator.SYSTEM_ID)) {
			if (projectMapping.getSourceRepositoryKind().startsWith("TemplateTasks")) {				
				BusyIndicator.showWhile(Display.getDefault(), new Runnable() {				
					public void run() {
						Landscape landscape = projectMapping.getLandscape();
						Properties properties = null;
						if (landscape.getType1().equals("TF")) {
							properties = landscape.getProperties1();
						}
						if (landscape.getType2().equals("TF")) {
							properties = landscape.getProperties2();
						}
						if (null != properties) {
							String serverUrl = properties.getProperty(com.collabnet.ccf.Activator.PROPERTIES_SFEE_URL);
							String userId = properties.getProperty(com.collabnet.ccf.Activator.PROPERTIES_SFEE_USER);
							String password = Activator.decodePassword(properties.getProperty(com.collabnet.ccf.Activator.PROPERTIES_SFEE_PASSWORD));
							TFSoapClient soapClient = TFSoapClient.getSoapClient(serverUrl, userId, password);
							boolean disabled = projectMapping.getSourceRepositoryKind().equals(ScrumWorksMappingSection.TEMPLATE_TASKS);
							String trackerId;
							if (projectMapping.getSourceRepositoryId().startsWith("tracker")) {
								trackerId = projectMapping.getSourceRepositoryId();
							} else {
								trackerId = projectMapping.getTargetRepositoryId();
							}
							try {
								soapClient.setFieldEnablement(trackerId, "Point Person", disabled);
							} catch (RemoteException e) {
								Activator.handleError(e);
							}
							// Update reverse mapping
							try {
								CcfDataProvider dataProvider = new CcfDataProvider();
								Filter sourceSystemFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_SYSTEM_ID, projectMapping.getTargetSystemId(), true);
								Filter sourceRepositoryFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_REPOSITORY_ID, projectMapping.getTargetRepositoryId(), true);
								Filter targetSystemFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_TARGET_SYSTEM_ID, projectMapping.getSourceSystemId(), true);
								Filter targetRepositoryFilter = new Filter(CcfDataProvider.SYNCHRONIZATION_STATUS_TARGET_REPOSITORY_ID, projectMapping.getSourceRepositoryId(), true);
								Filter[] filters = { sourceSystemFilter, sourceRepositoryFilter, targetSystemFilter, targetRepositoryFilter };
								Update repositoryKindUpdate = new Update(CcfDataProvider.SYNCHRONIZATION_STATUS_SOURCE_REPOSITORY_KIND, projectMapping.getSourceRepositoryKind());
								Update[] updates = { repositoryKindUpdate };						
								dataProvider.updateSynchronizationStatuses(landscape, updates, filters);
							} catch (Exception e) {
								Activator.handleError(e);
							}
						}
					}
				});				
			}
		}
	}

}
