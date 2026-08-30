package com.collabnet.ccf.teamforge_sw.wizards;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.sw.ScrumWorksCcfParticipant;
import com.collabnet.ccf.sw.ScrumWorksMappingSection;
import com.collabnet.ccf.teamforge.schemageneration.TFSoapClient;
import com.collabnet.teamforge.api.main.ProjectDO;
import com.collabnet.teamforge.api.main.ProjectMemberList;
import com.collabnet.teamforge.api.main.ProjectMemberRow;
import com.collabnet.teamforge.api.main.ProjectRow;
import com.collabnet.teamforge.api.main.UserDO;
import com.collabnet.teamforge.api.rbac.RbacClient;
import com.collabnet.teamforge.api.rbac.RoleDO;
import com.collabnet.teamforge.api.rbac.RoleList;
import com.collabnet.teamforge.api.rbac.RoleRow;
import com.collabnet.teamforge.api.tracker.TrackerDO;
import com.collabnet.teamforge.api.tracker.TrackerFieldDO;
import com.collabnet.teamforge.api.tracker.TrackerFieldValueDO;
import com.collabnet.teamforge.api.tracker.TrackerRow;
import com.danube.scrumworks.api2.client.Product;
import com.danube.scrumworks.api2.client.Program;
import com.danube.scrumworks.api2.client.ScrumWorksAPIService;
import com.danube.scrumworks.api2.client.ScrumWorksException;
import com.danube.scrumworks.api2.client.Sprint;
import com.danube.scrumworks.api2.client.Team;
import com.danube.scrumworks.api2.client.Theme;
import com.danube.scrumworks.api2.client.User;

public class ProjectMappingWizard extends Wizard {
	private Landscape landscape;
	private ProjectMappings projectMappings;
	private CcfDataProvider dataProvider = new CcfDataProvider();
	
	private ProjectMappingWizardSwpProductPage productPage;
	private ProjectMappingWizardTeamForgeProjectPage projectPage;
	private ProjectMappingWizardTeamForgeTrackerPage trackerPage;
	private ProjectMappingWizardPreviewPage previewPage;
	
	private String userId;
	private TFSoapClient soapClient;
	private List<SynchronizationStatus> existingMappings;
	private List<Exception> errors;
	private List<String> notCreated;
	private List<String> duplicateUsers;
	private boolean userMappingErrors;
	
	private Map<Long, Program> programMap;
	
	private ScrumWorksAPIService scrumWorksEndpoint;
	
	private final static String TRACKER_DESCRIPTION_PBIS = "SWP Product Backlog Items";
	private final static String TRACKER_DESCRIPTION_TASKS = "SWP Tasks";
	private final static String TRACKER_ICON_PBIS = "icon_41.png";
	private final static String TRACKER_ICON_TASKS = "icon_35.png";
	
	public final static String PRODUCT_DEVELOPER_ROLE_TITLE = "Product Developer";
	public final static String PRODUCT_DEVELOPER_ROLE_DESCRIPTION = "People who develop the software application, taking story input from the product managers, breaking them down into story tasks, estimating them, and implementing them when backlogged to a release or an iteration.";

	public ProjectMappingWizard(Landscape landscape, ProjectMappings projectMappings) {
		super();
		this.landscape = landscape;
		this.projectMappings = projectMappings;
	}
	
	@Override
	public void addPages() {
		super.addPages();
		setWindowTitle("Map TeamForge and ScrumWorks Projects");
		productPage = new ProjectMappingWizardSwpProductPage();
		addPage(productPage);
		projectPage = new ProjectMappingWizardTeamForgeProjectPage();
		addPage(projectPage);
		trackerPage = new ProjectMappingWizardTeamForgeTrackerPage();
		addPage(trackerPage);
		previewPage = new ProjectMappingWizardPreviewPage();
		addPage(previewPage);
	}

	@Override
	public boolean needsProgressMonitor() {
		return true;
	}

	@Override
	public boolean performFinish() {		
		errors = new ArrayList<Exception>();
		userMappingErrors = false;
		notCreated = new ArrayList<String>();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				boolean newProject = false;
				
				int totalWork = 12;
				
				// Need to create project.
				if (null == getSelectedProject()) {
					totalWork++;
					newProject = true;
				}
				
				// Need to map users.
				if (productPage.isMapUsers()) {
					totalWork = totalWork + 3;
					if (!newProject) {
						totalWork++;
					}
				}
				
				String taskName = "Creating project mappings";
				monitor.setTaskName(taskName);
				monitor.beginTask(taskName, totalWork);
				
				monitor.subTask("");
				getExistingMappings();
				monitor.worked(1);
				
				String projectId = null;
				String pbiTrackerId = null;
				String taskTrackerId = null;
				
				try {			
					if (null == getSelectedProject()) {
						monitor.subTask("Creating project " + projectPage.getNewProjectTitle());
						ProjectDO projectDO = getSoapClient().createProject(null, projectPage.getNewProjectTitle(), previewPage.getNewProjectDescription());
						projectId = projectDO.getId();
						monitor.worked(1);
					} else {
						projectId = getSelectedProject().getId();
					}
					
					if (null == getSelectedPbiTracker()) {
						monitor.subTask("Creating tracker " + trackerPage.getNewPbiTrackerTitle());
						TrackerDO trackerDO = getSoapClient().createTracker(projectId, null, trackerPage.getNewPbiTrackerTitle(), TRACKER_DESCRIPTION_PBIS, TRACKER_ICON_PBIS);
						pbiTrackerId = trackerDO.getId();
					} else {
						monitor.subTask("Checking PBI tracker fields");
						pbiTrackerId = getSelectedPbiTracker().getId();
					}
					setPbiTrackerFields(pbiTrackerId);
					monitor.worked(1);
					
					if (null == getSelectedTaskTracker()) {
						monitor.subTask("Creating tracker " + trackerPage.getNewTaskTrackerTitle());
						TrackerDO trackerDO = getSoapClient().createTracker(projectId, null, trackerPage.getNewTaskTrackerTitle(), TRACKER_DESCRIPTION_TASKS, TRACKER_ICON_TASKS);
						taskTrackerId = trackerDO.getId();
					} else {
						monitor.subTask("Checking Task tracker fields");
						taskTrackerId = getSelectedTaskTracker().getId();
					}
					setTaskTrackerFields(taskTrackerId);
					monitor.worked(1);
					if (productPage.isMapUsers()) {
						userMappingErrors = !mapUsers(getSelectedProduct(), projectId, newProject, monitor);
					}
				} catch (Exception e) {
					Activator.handleError(e);
					errors.add(e);
					monitor.done();
					return;
				}
				
				monitor.subTask(previewPage.getTrackerTaskMapping());
				SynchronizationStatus projectMapping = new SynchronizationStatus();
				projectMapping.setGroup(landscape.getGroup());
				projectMapping.setTargetRepositoryKind("TRACKER");
				if (null != landscape.getEncoding2() && 0 < landscape.getEncoding2().trim().length()) {
					projectMapping.setTargetSystemEncoding(landscape.getEncoding2());
				}				
				if (landscape.getType1().equals("TF")) {
					projectMapping.setSourceSystemId(landscape.getId1());	
					projectMapping.setTargetSystemId(landscape.getId2());			
					projectMapping.setSourceSystemKind(landscape.getType1());			
					projectMapping.setTargetSystemKind(landscape.getType2());
					projectMapping.setSourceSystemTimezone(landscape.getTimezone1());
					projectMapping.setTargetSystemTimezone(landscape.getTimezone2());
				} else {
					projectMapping.setSourceSystemId(landscape.getId2());	
					projectMapping.setTargetSystemId(landscape.getId1());			
					projectMapping.setSourceSystemKind(landscape.getType2());			
					projectMapping.setTargetSystemKind(landscape.getType1());
					projectMapping.setSourceSystemTimezone(landscape.getTimezone2());
					projectMapping.setTargetSystemTimezone(landscape.getTimezone1());					
				}
				String productNameAndId = getSelectedProduct().getName() + "(" + getSelectedProduct().getId() + ")";
				projectMapping.setSourceRepositoryId(taskTrackerId);
				projectMapping.setTargetRepositoryId(productNameAndId + "-Task");
				projectMapping.setConflictResolutionPriority(previewPage.getTrackerTaskConflictResolutionPriority());
				if (trackerPage.isMapToAssignedToUser()) {
					projectMapping.setSourceRepositoryKind(ScrumWorksMappingSection.TEMPLATE_TASKS);
				} else {
					projectMapping.setSourceRepositoryKind(ScrumWorksMappingSection.TEMPLATE_TASKS_FLEX_FIELD);
				}
				createMapping(projectMapping, dataProvider);
				monitor.worked(1);
				
				monitor.subTask(previewPage.getTrackerPbiMapping());
				projectMapping.setSourceRepositoryId(pbiTrackerId);
				projectMapping.setTargetRepositoryId(productNameAndId + "-PBI");
				projectMapping.setConflictResolutionPriority(previewPage.getTrackerPbiConflictResolutionPriority());
				projectMapping.setSourceRepositoryKind("TemplatePBIs.xsl");
				createMapping(projectMapping, dataProvider);
				monitor.worked(1);
				
				monitor.subTask(previewPage.getPlanningFolderProductMapping());
				projectMapping.setSourceRepositoryId(projectId + "-planningFolders");
				projectMapping.setTargetRepositoryId(productNameAndId + "-Product");
				projectMapping.setConflictResolutionPriority(previewPage.getPlanningFolderProductConflictResolutionPriority());
				projectMapping.setSourceRepositoryKind("TemplateProducts.xsl");
				createMapping(projectMapping, dataProvider);
				monitor.worked(1);
				
				monitor.subTask(previewPage.getPlanningFolderProductReleaseMapping());
				projectMapping.setTargetRepositoryId(productNameAndId + "-Release");
				projectMapping.setConflictResolutionPriority(previewPage.getPlanningFolderProductReleaseConflictResolutionPriority());
				projectMapping.setSourceRepositoryKind("TemplateReleases.xsl");
				createMapping(projectMapping, dataProvider);
				monitor.worked(1);
				
				monitor.subTask(previewPage.getTaskTrackerMapping());
				if (landscape.getType1().equals(ScrumWorksCcfParticipant.TYPE)) {
					projectMapping.setSourceSystemId(landscape.getId1());	
					projectMapping.setTargetSystemId(landscape.getId2());			
					projectMapping.setSourceSystemKind(landscape.getType1());			
					projectMapping.setTargetSystemKind(landscape.getType2());
					projectMapping.setSourceSystemTimezone(landscape.getTimezone1());
					projectMapping.setTargetSystemTimezone(landscape.getTimezone2());
				} else {
					projectMapping.setSourceSystemId(landscape.getId2());	
					projectMapping.setTargetSystemId(landscape.getId1());			
					projectMapping.setSourceSystemKind(landscape.getType2());			
					projectMapping.setTargetSystemKind(landscape.getType1());
					projectMapping.setSourceSystemTimezone(landscape.getTimezone2());
					projectMapping.setTargetSystemTimezone(landscape.getTimezone1());					
				}
				projectMapping.setSourceRepositoryId(productNameAndId + "-Task");
				if (trackerPage.isMapToAssignedToUser()) {
					projectMapping.setSourceRepositoryKind(ScrumWorksMappingSection.TEMPLATE_TASKS);
				} else {
					projectMapping.setSourceRepositoryKind(ScrumWorksMappingSection.TEMPLATE_TASKS_FLEX_FIELD);
				}
				projectMapping.setTargetRepositoryId(taskTrackerId);
				projectMapping.setConflictResolutionPriority(previewPage.getTaskTrackerConflictResolutionPriority());
				createMapping(projectMapping, dataProvider);
				monitor.worked(1);
				
				monitor.subTask(previewPage.getPbiTrackerMapping());
				projectMapping.setSourceRepositoryId(productNameAndId + "-PBI");
				projectMapping.setSourceRepositoryKind("TemplatePBIs.xsl");
				projectMapping.setTargetRepositoryId(pbiTrackerId);
				projectMapping.setConflictResolutionPriority(previewPage.getPbiTrackerConflictResolutionPriority());
				createMapping(projectMapping, dataProvider);
				monitor.worked(1);
				
				monitor.subTask(previewPage.getProductPlanningFolderMapping());
				projectMapping.setSourceRepositoryId(productNameAndId + "-Product");
				projectMapping.setSourceRepositoryKind("TemplateProducts.xsl");
				projectMapping.setTargetRepositoryId(projectId + "-planningFolders");
				projectMapping.setConflictResolutionPriority(previewPage.getProductPlanningFolderConflictResolutionPriority());
				createMapping(projectMapping, dataProvider);
				monitor.worked(1);
				
				monitor.subTask(previewPage.getProductReleasePlanningFolderMapping());
				projectMapping.setSourceRepositoryId(productNameAndId + "-Release");
				projectMapping.setSourceRepositoryKind("TemplateReleases.xsl");
				projectMapping.setConflictResolutionPriority(previewPage.getProductReleasePlanningFolderConflictResolutionPriority());
				createMapping(projectMapping, dataProvider);
				monitor.worked(1);
				
				monitor.subTask(previewPage.getMetaDataMapping());
				projectMapping.setSourceRepositoryId(productNameAndId + "-MetaData");
				projectMapping.setTargetRepositoryId(pbiTrackerId + "-MetaData");
				projectMapping.setSourceRepositoryKind("TemplateMetaData.xsl");
				projectMapping.setConflictResolutionPriority(previewPage.getProductReleasePlanningFolderConflictResolutionPriority());
				createMapping(projectMapping, dataProvider);
				monitor.worked(1);
				
				monitor.done();
			}
		};
		try {
			getContainer().run(true, false, runnable);
		} catch (Exception e) {
			Activator.handleError(e);
			MessageDialog.openError(getShell(), "Create Project Mappings", e.getMessage());
			return false;
		}
		if (0 < errors.size()) {
			StringBuffer errorMessage = new StringBuffer();
			for (Exception error : errors) {
				if (0 < errorMessage.length()) {
					errorMessage.append("\n\n");
				}
				errorMessage.append(error.getMessage());
			}
			MessageDialog.openError(getShell(), "Create Project Mappings", errorMessage.toString());
			return false;
		}
		if (userMappingErrors) {
			MessageDialog.openError(getShell(), "Map Users", "Errors occurred mapping ScrumWorks users to TeamForge.  See error log for details.");
		}
		if (null != duplicateUsers && 0 < duplicateUsers.size()) {
			DuplicateUserDialog dialog = new DuplicateUserDialog(getShell(), duplicateUsers);
			dialog.open();
		}
		if (0 < notCreated.size()) {
			StringBuffer notCreatedMessage = new StringBuffer("The following mappings already existed and were not created:\n");
			for (String mapping : notCreated) {
				notCreatedMessage.append("\n" + mapping);
			}
			MessageDialog.openInformation(getShell(), "Create Project Mappings", notCreatedMessage.toString());
		}
		return true;
	}
	
	private String getFieldId(String value, TrackerFieldValueDO[] oldValues) {
		String id = null;
		for (TrackerFieldValueDO oldValue : oldValues) {
			if (oldValue.getValue().equals(value)) {
				id = oldValue.getId();
				break;
			}
		}
		return id;
	}
	
	public Product getSelectedProduct() {
		return productPage.getSelectedProduct();
	}

	public ProjectRow getSelectedProject() {
		return projectPage.getSelectedProject();
	}
	
	public TrackerRow getSelectedPbiTracker() {
		return trackerPage.getSelectedPbiTracker();
	}
	
	public TrackerRow getSelectedTaskTracker() {
		return trackerPage.getSelectedTaskTracker();
	}
	
	public ProjectMappings getProjectMappings() {
		return projectMappings;
	}
	
	public TFSoapClient getSoapClient() {
		if (null == soapClient) {
			Properties properties = null;
			if (landscape.getType1().equals("TF")) {
				properties = landscape.getProperties1();
			} else {
				properties = landscape.getProperties2();
			}
			if (null != properties) {
				String serverUrl = properties.getProperty(Activator.PROPERTIES_SFEE_URL);
				userId = properties.getProperty(Activator.PROPERTIES_SFEE_USER);
				String password = Activator.decodePassword(properties.getProperty(Activator.PROPERTIES_SFEE_PASSWORD));
				soapClient = TFSoapClient.getSoapClient(serverUrl, userId, password);
			}
		}
		return soapClient;
	}
	
	public List<Product> getProducts() throws MalformedURLException, ScrumWorksException {
		return getScrumWorksEndpoint().getProducts();
	}
	
	private boolean mapUsers(Product product, String projectId, boolean newProject, IProgressMonitor monitor) {
		duplicateUsers = new ArrayList<String>();
		boolean errors = false;
		monitor.subTask("Getting product " + product.getName() + " users");
		List<String> productUserList = new ArrayList<String>();
		try {
			List<Sprint> sprints = getScrumWorksEndpoint().getSprintsForProduct(product.getId());
			for (Sprint sprint : sprints) {
				List<String> sprintUsers = getScrumWorksEndpoint().getUsersForSprint(sprint.getId());
				if (null != sprintUsers) {
					for (String sprintUser : sprintUsers) {
						if (!productUserList.contains(sprintUser)) {
							productUserList.add(sprintUser);
						}
					}
				}
			}
		} catch (Exception e) {
			Activator.handleError(e);
			errors = true;
		}
		monitor.worked(1);
		if (errors) {
			monitor.worked(2);
		} else {			
			List<String> projectMemberList = new ArrayList<String>();
			if (!newProject) {
				monitor.subTask("Getting project members");	
				try {
					ProjectMemberList memberList = getSoapClient().getProjectMemberList(projectId);
					ProjectMemberRow[] memberRows = memberList.getDataRows();
					for (ProjectMemberRow memberRow : memberRows) {
						projectMemberList.add(memberRow.getUserName());
					}
				} catch (Exception e) {
					Activator.handleError(e);
					errors = true;
				}
				monitor.worked(1);
			}
			if (!errors) {
				monitor.subTask("Creating TeamForge users");
				try {
					List<String> newUsers = new ArrayList<String>();
					List<User> swpUsers = getScrumWorksEndpoint().getUsers();
					if (null != swpUsers) {
						for (User swpUser : swpUsers) {
							if (productUserList.contains(swpUser.getDisplayName())) {
								boolean createUserError = false;
								UserDO userDO = null;
								try {
									userDO = getSoapClient().getUserData(swpUser.getUserName());
								} catch (Exception e) {}
								if (null == userDO) {
									String email = swpUser.getEmail();
									if (null == email) {
										email = swpUser.getUserName() + "@default.com";
									}
									String locale = "en";
									String timeZone = getScrumWorksEndpoint().getTimezone();
									String password = swpUser.getUserName() + "_defaultPassword";
									try {
										getSoapClient().createUser(swpUser.getUserName(), email, swpUser.getName(), locale, timeZone, false, false, password);
									} catch (Exception e) {
										createUserError = true;
										if (e.getMessage().startsWith("Username already exists")) {
											duplicateUsers.add(swpUser.getUserName());
										} else {
											Activator.handleError(e);
											errors = true;
										}
									}
								} else {
									// If user already exists but is not active (i.e., deleted status), activate user.
									if (!userDO.getStatus().equals("Active")) {
										userDO.setStatus("Active");
										try {
											getSoapClient().setUserData(userDO);
										} catch (Exception e) {
											Activator.handleError(e);
											errors = true;
										}
									}
								}
								if (!createUserError && !projectMemberList.contains(swpUser.getUserName())) {
									try {
										getSoapClient().addProjectMember(projectId, swpUser.getUserName());
										newUsers.add(swpUser.getUserName());
									} catch (Exception e) {
										Activator.handleError(e);
										errors = true;
									}
								}
							}
						}
					}
					if (null != newUsers && 0 < newUsers.size()) {
						createRole(projectId, newUsers);
					}
				} catch (Exception e) {
					Activator.handleError(e);
					errors = true;
				}
			}
			monitor.worked(1);
		}
		return !errors;
	}
	
	private void createRole(String projectId, List<String> users) throws RemoteException {
		String roleId = null;
		RoleList roleList = getSoapClient().getRoleList(projectId);
		RoleRow[] roleRows = roleList.getDataRows();
		for (RoleRow roleRow : roleRows) {
			if (roleRow.getTitle().equals(ProjectMappingWizard.PRODUCT_DEVELOPER_ROLE_TITLE)) {
				roleId = roleRow.getId();
				break;
			}
		}		
		if (null == roleId) {
			RoleDO roleDO = getSoapClient().createRole(projectId, PRODUCT_DEVELOPER_ROLE_TITLE, PRODUCT_DEVELOPER_ROLE_DESCRIPTION);
			roleId = roleDO.getId();
			getSoapClient().addCluster(roleId, RbacClient.TRACKER_CREATE, "");
			getSoapClient().addCluster(roleId, RbacClient.TRACKER_EDIT, "");
			getSoapClient().addCluster(roleId, RbacClient.PAGE_VIEW, "");
			getSoapClient().addCluster(roleId, RbacClient.DOCMAN_CREATE, "");
			getSoapClient().addCluster(roleId, RbacClient.DOCMAN_EDIT, "");
			getSoapClient().addCluster(roleId, RbacClient.SCM_COMMIT, "");
			getSoapClient().addCluster(roleId, RbacClient.DISCUSSION_PARTICIPATE, "");
		}
		for (String username : users) {
			getSoapClient().addUser(roleId, username);
		}
	}
	
	private List<Theme> getThemes(Product product) throws MalformedURLException, ScrumWorksException {
		return getScrumWorksEndpoint().getThemesForProduct(product.getId());
	}
	
	private List<Sprint> getSprints(Product product) throws MalformedURLException, ScrumWorksException {
		return getScrumWorksEndpoint().getSprintsForProduct(product.getId());
	}
	
	private Team getTeam(Long teamId) throws MalformedURLException, ScrumWorksException {
		return getScrumWorksEndpoint().getTeamById(teamId);
	}
	
	private String[] getTeamSprintValues() throws MalformedURLException, ScrumWorksException {
		String swpTimezone;
		if (landscape.getType1().equals("SWP")) {
			swpTimezone = landscape.getTimezone1();
		} else {
			swpTimezone = landscape.getTimezone2();
		}
		Map<Long, Team> teamMap = new HashMap<Long, Team>();
		List<String> teamSprintList = new ArrayList<String>();
		List<Sprint> sprints = getSprints(getSelectedProduct());
		if (null != sprints) {
			for (Sprint sprint : sprints) {
				Team team = teamMap.get(sprint.getTeamId());
				if (null == team) {
					team = getTeam(sprint.getTeamId());
					if (null != team) {
						teamMap.put(sprint.getTeamId(), team);
					}
				}
				if (null != team) {
					teamSprintList.add(com.collabnet.ccf.teamforge_sw.Activator.getTeamSprintStringRepresentation(sprint, team, swpTimezone));
				}
			}
		}
		String[] teamSprintValues = new String[teamSprintList.size()];
		teamSprintList.toArray(teamSprintValues);
		Arrays.sort(teamSprintValues);
		return teamSprintValues;
	}
	
	private String[] getThemeValues() throws MalformedURLException, ScrumWorksException {
		List<String> themeList = new ArrayList<String>();
		List<Theme> themes = getThemes(getSelectedProduct());
		if (null != themes) {
			for (Theme theme : themes) {
				themeList.add(getValue(theme));
			}
		}
		String[] themeValues = new String[themeList.size()];
		themeList.toArray(themeValues);
		Arrays.sort(themeValues);
		return themeValues;
	}
	
	private ScrumWorksAPIService getScrumWorksEndpoint() throws MalformedURLException {
		if (null == scrumWorksEndpoint) {
			scrumWorksEndpoint = com.collabnet.ccf.sw.Activator.getScrumWorksEndpoint(landscape);
		}
		return scrumWorksEndpoint;
	}

	private void createMapping(SynchronizationStatus status, CcfDataProvider dataProvider) {
		if (existingMappings.contains(status)) {
			notCreated.add(status.toString());
			return;
		}
		if (!status.getSourceSystemKind().endsWith("_paused")) {
			status.setSourceSystemKind(status.getSourceSystemKind() + "_paused");
		}
		try {
			dataProvider.addSynchronizationStatus(landscape, status);
		} catch (Exception e) {
			Activator.handleError(e);
			errors.add(e);
			return;
		}
		if (Landscape.ROLE_ADMINISTRATOR == landscape.getRole()) {
			createFieldMappingFile(status);
		}
	}
	
	private void createFieldMappingFile(SynchronizationStatus status) {
		status.setLandscape(landscape);
		status.clearXslInfo();
		if (landscape.enableEditFieldMapping()) {
			File xslFile = status.getXslFile();
			if (!xslFile.exists()) {
				try {
					xslFile.createNewFile();
					File sampleFile = status.getSampleXslFile();
					if (null != sampleFile && sampleFile.exists()) {
						CcfDataProvider.copyFile(sampleFile, xslFile);
					}
				} catch (IOException e) {
					Activator.handleError(e);
					errors.add(e);
				}
			}
		}
	}
	
	public List<SynchronizationStatus> getExistingMappings() {
		if (null == existingMappings) {
			existingMappings = new ArrayList<SynchronizationStatus>();
			try {
				SynchronizationStatus[] existingMappingsArray = dataProvider.getSynchronizationStatuses(landscape, projectMappings);
				for (SynchronizationStatus mapping : existingMappingsArray) {
					existingMappings.add(mapping);
				}
			} catch (Exception e) {
				Activator.handleError(e);
			}			
		}
		return existingMappings;
	}
	
	private void setPbiTrackerFields(String pbiTrackerId) throws RemoteException, MalformedURLException, ScrumWorksException {
		TrackerFieldDO[] fields = getSoapClient().getFields(pbiTrackerId);
		boolean addBenefit = true;
		boolean addPenalty = true;
		boolean addBacklogEffort = !getSoapClient().supports54();
		boolean addSwpKey = true;
		boolean addTeamSprint = true;
		boolean addSprintStart = true;
		boolean addSprintEnd = true;
		boolean addTheme = true;
		for (TrackerFieldDO field : fields) {
			String fieldName = field.getName();
			if (fieldName.equals("group") ||
			    fieldName.equals("customer") ||
			    fieldName.equals("reportedInRelease") ||
			    fieldName.equals("resolvedInRelease") ||
			    fieldName.startsWith("estimated") ||
			    fieldName.startsWith("actual")) {
				field.setDisabled(true);
				getSoapClient().setField(pbiTrackerId, field);
			}
			if (fieldName.equals("Benefit")) addBenefit = false;
			if (fieldName.equals("Penalty")) addPenalty = false;
			if (fieldName.equals("Backlog Effort")) addBacklogEffort = false;
			if (fieldName.equals("SWP-Key")) addSwpKey = false;
			if (fieldName.equals("Team/Sprint")) addTeamSprint = false;
			if (fieldName.equals("Sprint Start")) addSprintStart = false;
			if (fieldName.equals("Sprint End")) addSprintEnd = false;
			if (fieldName.equals("Themes")) addTheme = false;
		}
		if (addBenefit) getSoapClient().addTextField(pbiTrackerId, "Benefit", 5, 1, false, false, false, null);
		if (addPenalty) getSoapClient().addTextField(pbiTrackerId, "Penalty", 5, 1, false, false, false, null);
		if (addBacklogEffort) getSoapClient().addTextField(pbiTrackerId, "Backlog Effort", 5, 1, false, false, false, null);
		if (addSwpKey) getSoapClient().addTextField(pbiTrackerId, "SWP-Key", 30, 1, false, false, true, null);
		if (addTeamSprint) {
			String[] teamSprintValues = getTeamSprintValues();
			getSoapClient().addSingleSelectField(pbiTrackerId, "Team/Sprint", false, false, true, teamSprintValues, null);
		}
		if (addSprintStart) getSoapClient().addDateField(pbiTrackerId, "Sprint Start", false, false, true);
		if (addSprintEnd) getSoapClient().addDateField(pbiTrackerId, "Sprint End", false, false, true);
		
		if (addTheme) {
			String[] themeValues = getThemeValues();
			getSoapClient().addMultiSelectField(pbiTrackerId, "Themes", 10, false, false, false, themeValues, null);
		}
		
		for (TrackerFieldDO field : fields) {
			if (field.getName().equals("status")) {
				TrackerFieldValueDO[] oldValues = field.getFieldValues();
				TrackerFieldValueDO open = new TrackerFieldValueDO(getSoapClient().supports60(), getSoapClient().supports50());
				open.setIsDefault(true);
				open.setValue("Open");
				open.setValueClass("Open");
				open.setId(getFieldId("Open", oldValues));
				TrackerFieldValueDO done = new TrackerFieldValueDO(getSoapClient().supports60(), getSoapClient().supports50());
				done.setIsDefault(false);
				done.setValue("Done");
				done.setValueClass("Open");
				done.setId(getFieldId("Done", oldValues));
				TrackerFieldValueDO[] fieldValues = { open, done };
				field.setFieldValues(fieldValues);
				getSoapClient().setField(pbiTrackerId, field);
				break;
			}
		}		
	}
	
	private void setTaskTrackerFields(String taskTrackerId) throws RemoteException {
		TrackerFieldDO[] fields = getSoapClient().getFields(taskTrackerId);
		boolean addPointPerson = true;
		for (TrackerFieldDO field : fields) {
			String fieldName = field.getName();
			if (fieldName.equals("group") ||
			    fieldName.equals("customer") ||
			    fieldName.equals("reportedInRelease") ||
			    fieldName.equals("resolvedInRelease") ||
			    fieldName.startsWith("autosumming") ||
			    fieldName.startsWith("actual") ||
			    (fieldName.equals("points") && getSoapClient().supports54())) {
				field.setDisabled(true);
				getSoapClient().setField(taskTrackerId, field);
			}
			if (fieldName.equals("Point Person")) {
				addPointPerson = false;
			}
		}
		if (addPointPerson) {
			getSoapClient().addTextField(taskTrackerId, "Point Person", 30, 1, false, trackerPage.isMapToAssignedToUser(), false, null);
		} else {
			getSoapClient().setFieldEnablement(taskTrackerId, "Point Person", trackerPage.isMapToAssignedToUser());
		}
		for (TrackerFieldDO field : fields) {
			if (field.getName().equals("status")) {
				TrackerFieldValueDO[] oldValues = field.getFieldValues();
				TrackerFieldValueDO notStarted = new TrackerFieldValueDO(getSoapClient().supports60(), getSoapClient().supports50());
				notStarted.setIsDefault(true);
				notStarted.setValue("Not Started");
				notStarted.setValueClass("Open");
				notStarted.setId(getFieldId("Not Started", oldValues));
				TrackerFieldValueDO impeded = new TrackerFieldValueDO(getSoapClient().supports60(), getSoapClient().supports50());
				impeded.setIsDefault(false);
				impeded.setValue("Impeded");
				impeded.setValueClass("Open");
				impeded.setId(getFieldId("Impeded", oldValues));
				TrackerFieldValueDO inProgress = new TrackerFieldValueDO(getSoapClient().supports60(), getSoapClient().supports50());
				inProgress.setIsDefault(false);
				inProgress.setValue("In Progress");
				inProgress.setValueClass("Open");
				inProgress.setId(getFieldId("In Progress", oldValues));
				TrackerFieldValueDO done = new TrackerFieldValueDO(getSoapClient().supports60(), getSoapClient().supports50());
				done.setIsDefault(false);
				done.setValue("Done");
				done.setValueClass("Close");
				done.setId(getFieldId("Done", oldValues));
				TrackerFieldValueDO[] fieldValues = { notStarted, impeded, inProgress, done };
				field.setFieldValues(fieldValues);
				getSoapClient().setField(taskTrackerId, field);
				break;
			}
		}		
	}
	
	private String getValue(Theme theme) throws MalformedURLException, ScrumWorksException {
		if (null == programMap) {
			programMap = new HashMap<Long, Program>();
		}
		Program program = null;
		if (null != theme.getProgramId()) {
			program = programMap.get(theme.getProgramId());
			if (null == program) {
				program = getScrumWorksEndpoint().getProgramById(theme.getProgramId());
				programMap.put(theme.getProgramId(), program);
			}
		}
		if (null == program) {
			return theme.getName();
		} else {
			return theme.getName() + " (" + program.getName() + ")";
		}
	}

}
