package com.collabnet.ccf.teamforge_sw.wizards;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.model.MappingGroup;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.teamforge.api.main.UserDO;
import com.collabnet.teamforge.api.rbac.RbacClient;
import com.collabnet.teamforge.api.rbac.RoleDO;
import com.collabnet.teamforge.api.rbac.RoleList;
import com.collabnet.teamforge.api.rbac.RoleRow;
import com.danube.scrumworks.api2.client.User;

public class MapUsersWizard extends AbstractMappingWizard {
	private MapUsersWizardPage wizardPage;
	private boolean errors;
	private List<String> duplicateUsers;

	public MapUsersWizard(SynchronizationStatus projectMapping) {
		super(projectMapping);
	}
	
	public MapUsersWizard(MappingGroup mappingGroup) {
		super(mappingGroup);
	}

	@Override
	public void addPages() {
		super.addPages();
		setWindowTitle("Map Users");
		wizardPage = new MapUsersWizardPage();
		addPage(wizardPage);
	}

	@Override
	public boolean performFinish() {
		final List<UserDO> activateUserList = wizardPage.getActivateUserList();
		final List<User> createUserList = wizardPage.getCreateUserList();
		final List<User> addProjectMemberList = wizardPage.getAddProjectMemberList();
		if (0 == activateUserList.size() && 0 == createUserList.size() && 0 == addProjectMemberList.size()) {
			return true;
		}
		errors = false;
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					int totalWork = createUserList.size() + activateUserList.size() + (addProjectMemberList.size() * 2);
					String taskName = "Mapping ScrumWorks users to TeamForge";
					monitor.setTaskName(taskName);
					monitor.beginTask(taskName, totalWork);
					List<String> notCreatedUsers = new ArrayList<String>();
					duplicateUsers = new ArrayList<String>();
					for (User swpUser : createUserList) {
						monitor.subTask("Creating " + swpUser.getUserName());
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
							notCreatedUsers.add(swpUser.getUserName());
							if (e.getMessage().startsWith("Username already exists")) {
								duplicateUsers.add(swpUser.getUserName());
							} else {
								Activator.handleError(e);
								errors = true;
							}
						}
						monitor.worked(1);
					}
					for (UserDO userDO : activateUserList) {
						try {
							monitor.subTask("Activating " + userDO.getUsername());
							userDO.setStatus("Active");
							getSoapClient().setUserData(userDO);
						} catch (Exception e) {
							Activator.handleError(e);
							errors = true;
						}
						monitor.worked(1);
					}
					List<String> newUsers = new ArrayList<String>();
					for (User swpUser : addProjectMemberList) {
						try {
							if (!notCreatedUsers.contains(swpUser.getUserName())) {
								monitor.subTask("Adding " + swpUser.getUserName() + " to member list");
								getSoapClient().addProjectMember(wizardPage.getProjectId(), swpUser.getUserName());
								newUsers.add(swpUser.getUserName());
							}
						} catch (Exception e) {
							Activator.handleError(e);
							errors = true;
						}
						monitor.worked(1);
					}
					if (null != newUsers && 0 < newUsers.size()) {
						createRole(wizardPage.getProjectId(), newUsers, monitor);
					}
				} catch (Exception e) {
					Activator.handleError(e);
					errors = true;
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, runnable);
		} catch (Exception e) {
			Activator.handleError(e);
			MessageDialog.openError(getShell(), "Map Users", e.getMessage());
			return false;
		}
		if (errors || 0 < duplicateUsers.size()) {
			wizardPage.refresh(true);
			if (errors) {
				MessageDialog.openWarning(getShell(), "Map Users", "One or more error occurred mapping ScrumWorks users to TeamForge.  See error log for details.");
			}
			return false;
		}
		return true;
	}
	
	public List<String> getDuplicateUsers() {
		return duplicateUsers;
	}

	private void createRole(String projectId, List<String> users, IProgressMonitor monitor) throws RemoteException {
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
			RoleDO roleDO = getSoapClient().createRole(projectId, ProjectMappingWizard.PRODUCT_DEVELOPER_ROLE_TITLE, ProjectMappingWizard.PRODUCT_DEVELOPER_ROLE_DESCRIPTION);
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
			monitor.subTask("Setting permissions for " + username);
			getSoapClient().addUser(roleId, username);
			monitor.worked(1);
		}
	}

}
