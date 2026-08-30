package com.collabnet.ccf.teamforge_sw.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.collabnet.ccf.Activator;
import com.collabnet.teamforge.api.main.ProjectMemberList;
import com.collabnet.teamforge.api.main.ProjectMemberRow;
import com.collabnet.teamforge.api.main.UserDO;
import com.collabnet.teamforge.api.tracker.TrackerDO;
import com.danube.scrumworks.api2.client.Product;
import com.danube.scrumworks.api2.client.Sprint;
import com.danube.scrumworks.api2.client.User;

public class MapUsersWizardPage extends WizardPage {
	private List<String> productUserList;
	private List<User> createUserList;
	private List<UserDO> activateUserList;
	private List<User> addProjectMemberList;
	private Exception getUsersError;
	private String projectId;
	private org.eclipse.swt.widgets.List createList;
	private org.eclipse.swt.widgets.List activateList;
	private org.eclipse.swt.widgets.List addList;

	public MapUsersWizardPage() {
		super("mainPage", "Map Users", Activator.getDefault().getImageDescriptor(Activator.IMAGE_NEW_PROJECT_MAPPING_WIZBAN));
	}

	public void createControl(Composite parent) {
		Composite outerContainer = new Composite(parent,SWT.NONE);
		outerContainer.setLayout(new GridLayout());
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		getUsers();
		
		if (0 < createUserList.size()) {
			Group createGroup = new Group(outerContainer, SWT.NONE);
			createGroup.setText("Create TeamForge users:");
			createGroup.setLayout(new GridLayout());
			createGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
			createList = new org.eclipse.swt.widgets.List(createGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
			data.heightHint = 200;			
			createList.setLayoutData(data);
		}
		
		if (0 < activateUserList.size()) {
			Group activateGroup = new Group(outerContainer, SWT.NONE);
			activateGroup.setText("Activate TeamForge users:");
			activateGroup.setLayout(new GridLayout());
			activateGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
			activateList = new org.eclipse.swt.widgets.List(activateGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
			data.heightHint = 200;				
			activateList.setLayoutData(data);
		}
		
		if (0 < addProjectMemberList.size()) {
			Group addGroup = new Group(outerContainer, SWT.NONE);
			addGroup.setText("Add project members:");
			addGroup.setLayout(new GridLayout());
			addGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
			addList = new org.eclipse.swt.widgets.List(addGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
			data.heightHint = 200;	
			addList.setLayoutData(data);
		}
		
		if (0 < createUserList.size() || 0 < activateUserList.size() || 0 < addProjectMemberList.size()) {
			refresh(false);
		}

		setControl(outerContainer);
	}
	
	public String getProjectId() {
		return projectId;
	}

	public List<User> getCreateUserList() {
		return createUserList;
	}

	public List<UserDO> getActivateUserList() {
		return activateUserList;
	}

	public List<User> getAddProjectMemberList() {
		return addProjectMemberList;
	}
	
	public void refresh(boolean getUsers) {
		setErrorMessage(null);
		List<String> duplicateUsers = ((MapUsersWizard)getWizard()).getDuplicateUsers();
		if (null != duplicateUsers && 0 < duplicateUsers.size()) {
			setErrorMessage("One or more user could not be created because a similar user name (different case) already exists.");
		}
		if (getUsers) {
			getUsers();
		}
		if (null != createList) {
			createList.removeAll();
			for (User user : createUserList) {
				String userText;
				if (null != duplicateUsers && duplicateUsers.contains(user.getUserName())) {
					userText = user.getDisplayName() + " - User name already exists with different case";
				} else {
					userText = user.getDisplayName();
				}
				createList.add(userText);
			}
		}
		if (null != activateList) {
			activateList.removeAll();
			for (UserDO user : activateUserList) {
				activateList.add(user.getFullName());
			}
		}
		if (null != addList) {
			addList.removeAll();
			for (User user : addProjectMemberList) {
				addList.add(user.getDisplayName());
			}
		}
	}

	private void getUsers() {
		productUserList = new ArrayList<String>();
		createUserList = new ArrayList<User>();
		activateUserList = new ArrayList<UserDO>();
		addProjectMemberList = new ArrayList<User>();
		getUsersError = null;
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				String taskName = "Retrieving users";
				monitor.setTaskName(taskName);
				monitor.beginTask(taskName, 6);				
				try {
					monitor.subTask("TeamForge project member list");
					projectId = ((AbstractMappingWizard)getWizard()).getProject();
					if (null == projectId) {
						String trackerId = ((AbstractMappingWizard)getWizard()).getTracker();
						TrackerDO trackerDO = ((AbstractMappingWizard)getWizard()).getSoapClient().getTrackerInformation(trackerId);
						projectId = trackerDO.getProjectId();
					}
					monitor.worked(1);
					ProjectMemberList memberList = ((AbstractMappingWizard)getWizard()).getSoapClient().getProjectMemberList(projectId);
					ProjectMemberRow[] memberRows = memberList.getDataRows();
					List<String> projectMemberList = new ArrayList<String>();
					for (ProjectMemberRow memberRow : memberRows) {
						projectMemberList.add(memberRow.getUserName());
					}					
					monitor.worked(1);
					monitor.subTask("SWP product users");
					Product product = null;
					product =  ((MapUsersWizard)getWizard()).getScrumWorksEndpoint().getProductById(getProductId());
					if (null == product) {
						product =  ((MapUsersWizard)getWizard()).getScrumWorksEndpoint().getProductByName(getProduct());
					}
					monitor.worked(1);
					List<Sprint> sprints = ((AbstractMappingWizard)getWizard()).getScrumWorksEndpoint().getSprintsForProduct(product.getId());
					monitor.worked(1);
					for (Sprint sprint : sprints) {
						List<String> sprintUsers = ((AbstractMappingWizard)getWizard()).getScrumWorksEndpoint().getUsersForSprint(sprint.getId());
						if (null != sprintUsers) {
							for (String sprintUser : sprintUsers) {
								if (!productUserList.contains(sprintUser)) {
									productUserList.add(sprintUser);
								}
							}
						}
					}
					monitor.worked(1);
					List<User> swpUsers = ((AbstractMappingWizard)getWizard()).getScrumWorksEndpoint().getUsers();
					monitor.worked(1);
					if (null != swpUsers) {
						for (User swpUser : swpUsers) {
							if (productUserList.contains(swpUser.getDisplayName())) {
								UserDO userDO = null;
								try {
									userDO = ((AbstractMappingWizard)getWizard()).getSoapClient().getUserData(swpUser.getUserName());
								} catch (Exception e) {}
								if (null == userDO) {
									createUserList.add(swpUser);
								} else {
									if (!userDO.getStatus().equals("Active")) {
										activateUserList.add(userDO);
									}
								}
								if (!projectMemberList.contains(swpUser.getUserName())) {
									addProjectMemberList.add(swpUser);
								}
							}
						}
					}
				} catch (Exception e) {
					getUsersError = e;
					Activator.handleError(e);
					return;
				}
			}
		};
		
		try {
			getContainer().run(true, false, runnable);
		} catch (Exception e) {
			Activator.handleError(e);
			getUsersError = e;
		}
		if (null != getUsersError) {
			setErrorMessage("An unexpected error occurred while retrieving users.  See error log for details.");
		} else if (0 == createUserList.size() && 0 == activateUserList.size() && 0 == addProjectMemberList.size()) {
			setMessage("All " + getProduct() + " users are already members of TeamForge project.");
		} else {
			setMessage("Map " + getProduct() + " users to TeamForge.");
		}
		setPageComplete(getUsersError == null);
	}
	
	private String getProduct() {
		return ((AbstractMappingWizard)getWizard()).getProduct();
	}
	
	private Long getProductId() {
		return ((AbstractMappingWizard)getWizard()).getProductId();
	}

}
