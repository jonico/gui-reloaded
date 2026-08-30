package com.collabnet.ccf.teamforge_sw.wizards;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.teamforge.schemageneration.TFSoapClient;
import com.collabnet.teamforge.api.FieldValues;
import com.collabnet.teamforge.api.tracker.ArtifactDO;
import com.collabnet.teamforge.api.tracker.ArtifactList;
import com.collabnet.teamforge.api.tracker.ArtifactRow;
import com.collabnet.teamforge.api.tracker.TrackerClient;
import com.collabnet.teamforge.api.tracker.TrackerFieldDO;

public class UpgradeTo54WizardPage extends WizardPage {
	private Landscape landscape;
	private TFSoapClient soapClient;
	private Exception checkTrackersError;
	private Exception upgradeTrackersError;
	private List<String> pbiTrackers;
	private List<String> taskTrackers;
	private Map<String, String> backLogEffortFieldsToRemove;
	private Map<String, String> storyPointFieldsToHide;

	public UpgradeTo54WizardPage(Landscape landscape) {
		super("mainPage", "Upgrade Trackers", Activator.getDefault().getImageDescriptor(Activator.IMAGE_NEW_PROJECT_MAPPING_WIZBAN));
		this.landscape = landscape;
	}
	
	public void createControl(Composite parent) {
		Composite outerContainer = new Composite(parent,SWT.NONE);
		outerContainer.setLayout(new GridLayout());
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		try {
			getSoapClient().login();
		} catch (RemoteException e) {
			Activator.handleError(e);
			setErrorMessage(e.getMessage());
			setControl(outerContainer);
			return;
		}
		
		if (!getSoapClient().supports54()) {
			setErrorMessage("Server does not support TeamForge 5.4");
			setPageComplete(false);
			setControl(outerContainer);
			return;
		}
		
		checkTrackers();
		
		if (0 < backLogEffortFieldsToRemove.size() || 0 < storyPointFieldsToHide.size()) {
			Group upgradeGroup = new Group(outerContainer, SWT.NONE);
			upgradeGroup.setText("The following changes will be made:");
			GridLayout upgradeLayout = new GridLayout();
			upgradeLayout.numColumns = 1;
			upgradeGroup.setLayout(upgradeLayout);
			upgradeGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
			
			org.eclipse.swt.widgets.List upgradeList = new org.eclipse.swt.widgets.List(upgradeGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			upgradeList.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
			for (String pbiTracker : pbiTrackers) {
				String backlogEffortField = backLogEffortFieldsToRemove.get(pbiTracker);
				if (null != backlogEffortField) {
					upgradeList.add("Copy Backlog Effort to Story Points for all artifacts in PBIs tracker (" + pbiTracker + ")");
					upgradeList.add("Remove Backlog Effort field from PBIs tracker (" + pbiTracker + ")");
				}
			}
			for (String taskTracker : taskTrackers) {
				String storyPointsField = storyPointFieldsToHide.get(taskTracker);
				if (null != storyPointsField) {
					upgradeList.add("Disable Story Points field in Tasks tracker (" + taskTracker + ")");
				}
			}
		}
		
		setControl(outerContainer);
	}

	public boolean upgradeTo54() {
		upgradeTrackersError = null;
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				String taskName = "Upgrading trackers";
				monitor.setTaskName(taskName);
				monitor.beginTask(taskName, (backLogEffortFieldsToRemove.size() * 4) + storyPointFieldsToHide.size());
				try {
					for (String pbiTracker: pbiTrackers) {
						String backLogEffortField = backLogEffortFieldsToRemove.get(pbiTracker);
						if (null != backLogEffortField) {
							monitor.subTask("Retrieving artifacts from PBIs tracker " + pbiTracker);
							TrackerClient trackerClient = soapClient.getConnection().getTrackerClient();
							ArtifactList artifactList = trackerClient.getArtifactList(pbiTracker, null);
							ArtifactRow[] artifactRows = artifactList.getDataRows();
							monitor.worked(1);
							for (ArtifactRow artifactRow : artifactRows) {
								if (0 >= artifactRow.getPoints()) {
									monitor.subTask("Copying Backlog Effort to Story Points for PBI " + artifactRow.getId());
									ArtifactDO artifactDO = trackerClient.getArtifactData(artifactRow.getId());
									FieldValues fieldValues = artifactDO.getFlexFields();
									String[] fieldNames = fieldValues.getNames();
							fields:		
									for (int i = 0; i < fieldNames.length; i++) {
										if (fieldNames[i].equals("Backlog Effort")) {
											Object[] values = fieldValues.getValues();
											Object value = values[i];
											if (null != value) {
												String effort = value.toString();
												int effortInt = 0;
												try {
													effortInt = Integer.parseInt(effort);
												} catch (Exception e) {}
												if (0 < effortInt) {
													artifactDO.setPoints(effortInt);
													trackerClient.setArtifactData(artifactDO, null, null, null, null);
												}
											}
											break fields;
										}
									}
								}
							}
							monitor.worked(2);
							monitor.subTask("Removing Backlog Effort field from tracker " + pbiTracker);
							getSoapClient().deleteField(pbiTracker, backLogEffortField);
							monitor.worked(1);
						}
					}
					for (String taskTracker: taskTrackers) {
						String storyPointsField = storyPointFieldsToHide.get(taskTracker);
						if (null != storyPointsField) {
							monitor.subTask("Disabling Story Points field in tracker " + taskTracker);
							getSoapClient().setFieldEnablement(taskTracker, "points", true); // true = disabled
							monitor.worked(1);
						}
					}
				} catch (Exception e) {
					upgradeTrackersError = e;
					Activator.handleError(e);
					return;					
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, runnable);
		} catch (Exception e) {
			Activator.handleError(e);
			upgradeTrackersError = e;
		}
		if (null != upgradeTrackersError) {
			setErrorMessage("An unexpected error occurred while trying to upgrade TeamForge trackers.  See error log for details.");
		}
		return upgradeTrackersError == null;
	}
	
	private void checkTrackers() {
		initializeFlags();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				String taskName = "Checking trackers";
				monitor.setTaskName(taskName);
				monitor.beginTask(taskName, 3);		
				try {
					monitor.subTask("Retrieving trackers");
					CcfDataProvider dataProvider = new CcfDataProvider();
					SynchronizationStatus[] mappings = dataProvider.getSynchronizationStatuses(landscape, null);
					for (SynchronizationStatus mapping : mappings) {
						String pbiTrackerId = null;
						String taskTrackerId = null;
						if (mapping.getSourceRepositoryId().endsWith("-PBI")) {
							pbiTrackerId = mapping.getTargetRepositoryId();
						}
						else if (mapping.getTargetRepositoryId().endsWith("-PBI")) {
							pbiTrackerId = mapping.getSourceRepositoryId();
						}
						if (mapping.getSourceRepositoryId().endsWith("-Task")) {
							taskTrackerId = mapping.getTargetRepositoryId();
						}
						else if (mapping.getTargetRepositoryId().endsWith("-Task")) {
							taskTrackerId = mapping.getSourceRepositoryId();
						}
						if (null != pbiTrackerId && !pbiTrackers.contains(pbiTrackerId)) {
							pbiTrackers.add(pbiTrackerId);
						}
						else if (null != taskTrackerId && !taskTrackers.contains(taskTrackerId)) {
							taskTrackers.add(taskTrackerId);
						}
					}
					monitor.worked(1);				
					for (String pbiTracker : pbiTrackers) {
						monitor.subTask("Checking fields for PBIs tracker " + pbiTracker);
						TrackerFieldDO[] fields = getSoapClient().getFields(pbiTracker);
						for (TrackerFieldDO field : fields) {
							if (field.getName().equals("Backlog Effort")) {
								backLogEffortFieldsToRemove.put(pbiTracker, field.getId());
							}
						}
					}
					monitor.worked(1);
					for (String taskTracker : taskTrackers) {
						monitor.subTask("Checking fields for Tasks tracker " + taskTracker);
						TrackerFieldDO[] fields = getSoapClient().getFields(taskTracker);
						for (TrackerFieldDO field : fields) {
							if (field.getName().equals("points") && !field.getDisabled()) {
								storyPointFieldsToHide.put(taskTracker, field.getId());
							}
						}
					}
					monitor.worked(1);
				} catch (Exception e) {
					checkTrackersError = e;
					Activator.handleError(e);
					return;
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, runnable);
		} catch (Exception e) {
			Activator.handleError(e);
			checkTrackersError = e;
		}
		if (null != checkTrackersError) {
			setErrorMessage("An unexpected error occurred while checking TeamForge trackers.  See error log for details.");
		} if (0 == backLogEffortFieldsToRemove.size() && 0 == storyPointFieldsToHide.size()) {
			setMessage("Tracker layouts are already correct for 5.4.  No upgrade required.");
		} else {
			setMessage("Upgrade trackers to 5.4 layout.");
		}
		setPageComplete(checkTrackersError == null);
	}
	
	private void initializeFlags() {
		checkTrackersError = null;
		pbiTrackers = new ArrayList<String>();
		taskTrackers = new ArrayList<String>();
		backLogEffortFieldsToRemove = new HashMap<String, String>();
		storyPointFieldsToHide = new HashMap<String, String>();
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

}
