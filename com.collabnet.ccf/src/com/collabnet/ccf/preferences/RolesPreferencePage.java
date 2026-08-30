package com.collabnet.ccf.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.dialogs.NewRoleDialog;
import com.collabnet.ccf.dialogs.RoleLoginDialog;
import com.collabnet.ccf.model.Role;

public class RolesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Button selectButton;
	private Button removeButton;
	private Button addButton;
	private TableViewer roleTableViewer;
	private Group optionGroup;
	private Group passwordGroup;
	private Text passwordText;
	
	private Button newLandscapeButton;
	private Button editLandscapeButton;
	private Button deleteLandscapeButton;
	private Button newProjectMappingButton;
	private Button changeProjectMappingButton;
	private Button deleteProjectMappingButton;
	private Button editFieldMappingsButton;
	private Button pauseSynchronizationButton;
	private Button resumeSynchronizationButton;
	private Button resetSynchronizationStatusButton;
	private Button deleteIdentityMappingsButton;
	
	private Button editQuarantinedArtifactButton;
	private Button markAsFixedButton;
	private Button reopenButton;
	private Button replayButton;
	private Button cancelReplayButton;
	private Button deleteHospitalEntryButton;
	
	private Button createReverseIdentityMappingButton;
	private Button deleteIdentityMappingButton;
	private Button editIdentityMappingButton;
	private Button consistencyCheckButton;
	
	private Button editLogSettingsButton;
	private Button maintainRolesButton;
	
	private Role[] roles;
	private Role activeRole;
	private String lastActiveRole;
	
	private Font italicFont;
	
	private boolean changes;
	private boolean changingRoles;

	public static final String ID = "com.collabnet.ccf.preferences.roles";
	
	public RolesPreferencePage() {
		super();
	}

	public RolesPreferencePage(String title) {
		super(title);
	}

	public RolesPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		
		lastActiveRole = Activator.getDefault().getPreferenceStore().getString(Activator.PREFERENCES_ACTIVE_ROLE);
		roles = getRoles();
		
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gd);
		
		Group rolesGroup = new Group(composite, SWT.NULL);
		GridLayout rolesLayout = new GridLayout();
		rolesLayout.numColumns = 2;
		rolesLayout.marginWidth = 0;
		rolesLayout.marginHeight = 0;
		rolesGroup.setLayout(rolesLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		rolesGroup.setLayoutData(gd);	
		rolesGroup.setText("Roles:");

		Table roleTable =	new Table(rolesGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		roleTable.setHeaderVisible(false);
		roleTable.setLinesVisible(false);
		
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		gd.heightHint = 75;
		roleTable.setLayoutData(gd);
		
		TableLayout roleTableLayout = new TableLayout();
		roleTable.setLayout(roleTableLayout);
		
		roleTableViewer = new TableViewer(roleTable);
		roleTableViewer.setContentProvider(new ArrayContentProvider());
		roleTableViewer.setLabelProvider(new RoleLabelProvider());
		roleTableViewer.setInput(roles);
		
		roleTableViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent oe) {
				setActiveRole();
			}			
		});
		
		TableColumn col = new TableColumn(roleTable, SWT.NONE);
		col.setResizable(true);
		roleTableLayout.addColumnData(new ColumnWeightData(800, true));
		
		Composite addRemoveGroup = new Composite(rolesGroup, SWT.NULL);
		GridLayout addRemoveLayout = new GridLayout();
		addRemoveLayout.numColumns = 1;
		addRemoveGroup.setLayout(addRemoveLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		addRemoveGroup.setLayoutData(gd);
		
		selectButton = new Button(addRemoveGroup, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		selectButton.setLayoutData(gd);
		selectButton.setText("Set Active");
		selectButton.setEnabled(false);
		selectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				setActiveRole();
			}			
		});
		
		addButton = new Button(addRemoveGroup, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		addButton.setLayoutData(gd);
		addButton.setText("Add...");
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				NewRoleDialog dialog = new NewRoleDialog(getShell());
				if (NewRoleDialog.OK == dialog.open()) {
					changes = true;
					final Role newRole = dialog.getRole();
					List<Role> roleList = new ArrayList<Role>();
					roleList.add(newRole);
					for (Role role : roles) {						
						roleList.add(role);
					}
					roles = new Role[roleList.size()];
					roleList.toArray(roles);
					Arrays.sort(roles);
					roleTableViewer.setInput(roles);
					roleTableViewer.setSelection(new IStructuredSelection() {
						public Object getFirstElement() {
							return newRole;
						}
						@SuppressWarnings("unchecked")
						public Iterator iterator() {
							return toList().iterator();
						}
						public int size() {
							return 1;
						}
						public Object[] toArray() {
							Role[] newRoleArray = { newRole };
							return newRoleArray;
						}
						@SuppressWarnings("unchecked")
						public List toList() {
							List<Role> roleList = new ArrayList<Role>();
							roleList.add(newRole);
							return roleList;
						}
						public boolean isEmpty() {
							return false;
						}
					});
				}
			}		
		});
		
		removeButton = new Button(addRemoveGroup, SWT.PUSH);
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		removeButton.setLayoutData(gd);
		removeButton.setText("Remove");
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			public void widgetSelected(SelectionEvent se) {
				changes = true;
				IStructuredSelection selection = (IStructuredSelection)roleTableViewer.getSelection();
				List<Role> deletedRoles = selection.toList();
				List<Role> roleList = new ArrayList<Role>();
				for (Role role : roles) {
					if (!deletedRoles.contains(role)) {
						roleList.add(role);
					}
				}
				roles = new Role[roleList.size()];
				roleList.toArray(roles);
				Arrays.sort(roles);
				roleTableViewer.setInput(roles);
			}			
		});
		
		passwordGroup = new Group(composite, SWT.NULL);
		GridLayout passwordLayout = new GridLayout();
		passwordLayout.numColumns = 1;
		passwordGroup.setLayout(passwordLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		passwordGroup.setLayoutData(gd);	
		passwordGroup.setText("Password:");
		passwordGroup.setEnabled(false);
		
		passwordText = new Text(passwordGroup, SWT.PASSWORD | SWT.BORDER);
		gd = new GridData();
		gd.widthHint = 300;
		passwordText.setLayoutData(gd);
		
		optionGroup = new Group(composite, SWT.NULL);
		GridLayout optionsLayout = new GridLayout();
		optionsLayout.numColumns = 2;
		optionGroup.setLayout(optionsLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		optionGroup.setLayoutData(gd);	
		optionGroup.setText("Option enablement:");
		optionGroup.setEnabled(false);
		
		Composite column1Group = new Composite(optionGroup, SWT.NULL);
		GridLayout column1Layout = new GridLayout();
		column1Layout.numColumns = 1;
		column1Group.setLayout(column1Layout);
		gd = new GridData(GridData.FILL_BOTH);
		column1Group.setLayoutData(gd);
		
		Group landscapeGroup = new Group(column1Group, SWT.NULL);
		GridLayout landscapeLayout = new GridLayout();
		landscapeLayout.numColumns = 1;
		landscapeGroup.setLayout(landscapeLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		landscapeGroup.setLayoutData(gd);	
		landscapeGroup.setText("Landscape options:");
		
		newLandscapeButton = new Button(landscapeGroup, SWT.CHECK);
		newLandscapeButton.setText("Add");
		editLandscapeButton = new Button(landscapeGroup, SWT.CHECK);
		editLandscapeButton.setText("Edit");
		deleteLandscapeButton = new Button(landscapeGroup, SWT.CHECK);
		deleteLandscapeButton.setText("Delete");
		
		Group projectMappingGroup = new Group(column1Group, SWT.NULL);
		GridLayout projectMappingLayout = new GridLayout();
		projectMappingLayout.numColumns = 1;
		projectMappingGroup.setLayout(projectMappingLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		projectMappingGroup.setLayoutData(gd);	
		projectMappingGroup.setText("Project mapping options:");
		
		newProjectMappingButton = new Button(projectMappingGroup, SWT.CHECK);
		newProjectMappingButton.setText("Add");
		changeProjectMappingButton = new Button(projectMappingGroup, SWT.CHECK);
		changeProjectMappingButton.setText("Change");
		deleteProjectMappingButton = new Button(projectMappingGroup, SWT.CHECK);
		deleteProjectMappingButton.setText("Delete");
		editFieldMappingsButton = new Button(projectMappingGroup, SWT.CHECK);
		editFieldMappingsButton.setText("Edit field mappings");
		pauseSynchronizationButton = new Button(projectMappingGroup, SWT.CHECK);
		pauseSynchronizationButton.setText("Pause synchronization");
		resumeSynchronizationButton = new Button(projectMappingGroup, SWT.CHECK);
		resumeSynchronizationButton.setText("Resume synchronization");	
		resetSynchronizationStatusButton = new Button(projectMappingGroup, SWT.CHECK);
		resetSynchronizationStatusButton.setText("Reset synchronization status");
		deleteIdentityMappingsButton = new Button(projectMappingGroup, SWT.CHECK);
		deleteIdentityMappingsButton.setText("Delete identity mappings");
		
		Composite column2Group = new Composite(optionGroup, SWT.NULL);
		GridLayout column2Layout = new GridLayout();
		column2Layout.numColumns = 1;
		column2Group.setLayout(column2Layout);
		gd = new GridData(GridData.FILL_BOTH);
		column2Group.setLayoutData(gd);
		
		Group hospitalGroup = new Group(column2Group, SWT.NULL);
		GridLayout hospitalLayout = new GridLayout();
		hospitalLayout.numColumns = 1;
		hospitalGroup.setLayout(hospitalLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		hospitalGroup.setLayoutData(gd);	
		hospitalGroup.setText("Hospital options:");
		
		editQuarantinedArtifactButton = new Button(hospitalGroup, SWT.CHECK);
		editQuarantinedArtifactButton.setText("Edit quarantined artifact");
		markAsFixedButton = new Button(hospitalGroup, SWT.CHECK);
		markAsFixedButton.setText("Mark as fixed");
		reopenButton = new Button(hospitalGroup, SWT.CHECK);
		reopenButton.setText("Reopen");
		replayButton = new Button(hospitalGroup, SWT.CHECK);
		replayButton.setText("Replay");
		cancelReplayButton = new Button(hospitalGroup, SWT.CHECK);
		cancelReplayButton.setText("Cancel replay");
		deleteHospitalEntryButton = new Button(hospitalGroup, SWT.CHECK);
		deleteHospitalEntryButton.setText("Delete");
		
		Group identityMappingGroup = new Group(column2Group, SWT.NULL);
		GridLayout identityMappingLayout = new GridLayout();
		identityMappingLayout.numColumns = 1;
		identityMappingGroup.setLayout(identityMappingLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		identityMappingGroup.setLayoutData(gd);	
		identityMappingGroup.setText("Identity mapping options:");
		
		editIdentityMappingButton = new Button(identityMappingGroup, SWT.CHECK);
		editIdentityMappingButton.setText("Edit");
		deleteIdentityMappingButton = new Button(identityMappingGroup, SWT.CHECK);
		deleteIdentityMappingButton.setText("Delete");
		createReverseIdentityMappingButton = new Button(identityMappingGroup, SWT.CHECK);
		createReverseIdentityMappingButton.setText("Create reverse identity mapping");
		consistencyCheckButton = new Button(identityMappingGroup, SWT.CHECK);
		consistencyCheckButton.setText("Consistency check");

		Group configGroup = new Group(column2Group, SWT.NULL);
		GridLayout configLayout = new GridLayout();
		configLayout.numColumns = 1;
		configGroup.setLayout(configLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		configGroup.setLayoutData(gd);	
		configGroup.setText("Configuration options:");
		
		editLogSettingsButton = new Button(configGroup, SWT.CHECK);
		editLogSettingsButton.setText("Edit CCF log settings");
		maintainRolesButton = new Button(configGroup, SWT.CHECK);
		maintainRolesButton.setText("Add/change/delete roles");
		
		roleTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent sce) {
				setEnablement();
			}			
		});
		
		SelectionListener optionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				changes = true;
				IStructuredSelection selection = (IStructuredSelection)roleTableViewer.getSelection();
				Role role = (Role)selection.getFirstElement();
				role.setAddLandscape(newLandscapeButton.getSelection());
				role.setEditLandscape(editLandscapeButton.getSelection());
				role.setDeleteLandscape(deleteLandscapeButton.getSelection());
				role.setAddProjectMapping(newProjectMappingButton.getSelection());
				role.setChangeProjectMapping(changeProjectMappingButton.getSelection());
				role.setDeleteProjectMapping(deleteProjectMappingButton.getSelection());
				role.setEditFieldMappings(editFieldMappingsButton.getSelection());
				role.setPauseSynchronization(pauseSynchronizationButton.getSelection());
				role.setResumeSynchronization(resumeSynchronizationButton.getSelection());
				role.setResetSynchronizationStatus(resetSynchronizationStatusButton.getSelection());
				role.setDeleteProjectMappingIdentityMappings(deleteIdentityMappingsButton.getSelection());
				role.setEditQuarantinedArtifact(editQuarantinedArtifactButton.getSelection());
				role.setMarkAsFixed(markAsFixedButton.getSelection());
				role.setReopen(reopenButton.getSelection());
				role.setReplay(replayButton.getSelection());
				role.setCancelReplay(cancelReplayButton.getSelection());
				role.setDeleteHospitalEntry(deleteHospitalEntryButton.getSelection());
				role.setEditIdentityMapping(editIdentityMappingButton.getSelection());
				role.setCreateReverseIdentityMapping(createReverseIdentityMappingButton.getSelection());
				role.setDeleteIdentityMapping(deleteIdentityMappingButton.getSelection());
				role.setEditLogSettings(editLogSettingsButton.getSelection());
				role.setConsistencyCheck(consistencyCheckButton.getSelection());
				role.setMaintainRoles(maintainRolesButton.getSelection());
			}			
		};
		
		newLandscapeButton.addSelectionListener(optionListener);
		editLandscapeButton.addSelectionListener(optionListener);
		deleteLandscapeButton.addSelectionListener(optionListener);
		newProjectMappingButton.addSelectionListener(optionListener);
		changeProjectMappingButton.addSelectionListener(optionListener);
		deleteProjectMappingButton.addSelectionListener(optionListener);
		editFieldMappingsButton.addSelectionListener(optionListener);
		pauseSynchronizationButton.addSelectionListener(optionListener);
		resumeSynchronizationButton.addSelectionListener(optionListener);
		resetSynchronizationStatusButton.addSelectionListener(optionListener);
		deleteIdentityMappingsButton.addSelectionListener(optionListener);
		editQuarantinedArtifactButton.addSelectionListener(optionListener);
		markAsFixedButton.addSelectionListener(optionListener);
		reopenButton.addSelectionListener(optionListener);
		replayButton.addSelectionListener(optionListener);
		cancelReplayButton.addSelectionListener(optionListener);
		deleteHospitalEntryButton.addSelectionListener(optionListener);
		editIdentityMappingButton.addSelectionListener(optionListener);
		createReverseIdentityMappingButton.addSelectionListener(optionListener);
		deleteIdentityMappingButton.addSelectionListener(optionListener);
		editLogSettingsButton.addSelectionListener(optionListener);	
		consistencyCheckButton.addSelectionListener(optionListener);
		maintainRolesButton.addSelectionListener(optionListener);
		
		if (null != activeRole) {
			roleTableViewer.setSelection(new IStructuredSelection() {
				public Object getFirstElement() {
					return activeRole;
				}
				@SuppressWarnings("unchecked")
				public Iterator iterator() {
					return toList().iterator();
				}
				public int size() {
					return 1;
				}
				public Object[] toArray() {
					Role[] newRoleArray = { activeRole };
					return newRoleArray;
				}
				@SuppressWarnings("unchecked")
				public List toList() {
					List<Role> roleList = new ArrayList<Role>();
					roleList.add(activeRole);
					return roleList;
				}
				public boolean isEmpty() {
					return false;
				}
			});
			setEnablement();
		}
		
		passwordText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent me) {
				IStructuredSelection selection = (IStructuredSelection)roleTableViewer.getSelection();
				Role role = (Role)selection.getFirstElement();
				role.setPassword(passwordText.getText().trim());
				if (!changingRoles) changes = true;
			}			
		});
	
		return composite;
	}

	@Override
	protected void performApply() {
		super.performApply();
		setEnablement();
	}

	@Override
	public boolean performOk() {
		Role[] currentRoles = Activator.getDefault().getRoles();
		for (Role role : currentRoles) {
			Activator.getDefault().deleteRole(role);
		}
		for (Role role : roles) {
			Activator.getDefault().storeRole(role);
		}
		if (null != activeRole) {
			Activator.getDefault().getPreferenceStore().setValue(Activator.PREFERENCES_ACTIVE_ROLE, activeRole.getName());
		}
		Activator.notifyRoleChanged(Activator.getDefault().getActiveRole());
		changes = false;
		return super.performOk();
	}

	@Override
	public boolean performCancel() {
		if (changes) {
			if (MessageDialog.openQuestion(getShell(), "Save Changes?", "Roles have been updated.  Do you want to save your changes?")) {
				performOk();
			}
		}
		return super.performCancel();
	}

//	@Override
//	protected void performDefaults() {
//		Role defaultRole = new Role("Default");
//		activeRole = defaultRole;
//		Role[] defaultRoles = { defaultRole };
//		roles = defaultRoles;
//		roleTableViewer.setInput(roles);
//		changes = true;
//		super.performDefaults();
//	}

	public void init(IWorkbench workbench) {
	}
	
	@Override
	public void dispose() {
		if (null != italicFont) {
			if (!italicFont.isDisposed()) italicFont.dispose();
			italicFont = null;
		}
		super.dispose();
	}

	private Role[] getRoles() {
		Role[] storedRoles = Activator.getDefault().getRoles();
		if (null != lastActiveRole) {
			for (Role role : storedRoles) {
				if (role.getName().equals(lastActiveRole)) {
					activeRole = role;
					break;
				}
			}
		}
		if (null == lastActiveRole && 0 < storedRoles.length) {
			lastActiveRole = storedRoles[0].getName();
		}
		return storedRoles;
	}
	
	private void setActiveRole() {		
		IStructuredSelection selection = (IStructuredSelection)roleTableViewer.getSelection();
		Role role = (Role)selection.getFirstElement();
		if (null == activeRole || !role.getName().equals(activeRole.getName())) {
			if (role.isPasswordRequired()) {
				RoleLoginDialog dialog = new RoleLoginDialog(getShell(), role);
				if (RoleLoginDialog.OK != dialog.open()) {
					return;
				}
			}
//			changes = true;
			changingRoles = true;
			activeRole = role;
			roleTableViewer.refresh();
			setEnablement();
			performApply();
			changingRoles = false;
		}
	}

	private void setEnablement() {
		boolean maintainRoles = Activator.getDefault().getActiveRole().isMaintainRoles();
		
		IStructuredSelection selection = (IStructuredSelection)roleTableViewer.getSelection();
		selectButton.setEnabled(selection.size() == 1 && selection.getFirstElement() != activeRole);
		removeButton.setEnabled(maintainRoles && selection.size() > 0 && !selection.toList().contains(activeRole));
		addButton.setEnabled(maintainRoles);
		passwordGroup.setEnabled(maintainRoles);
		passwordText.setEnabled(maintainRoles);
		
		optionGroup.setEnabled(selection.size() == 1 && maintainRoles);
		if (1 == selection.size()) {
			Role role = (Role)selection.getFirstElement();
			if (null == role.getPassword()) passwordText.setText("");
			else passwordText.setText(role.getPassword());
			newLandscapeButton.setSelection(role.isAddLandscape());
			editLandscapeButton.setSelection(role.isEditLandscape());
			deleteLandscapeButton.setSelection(role.isDeleteLandscape());
			newProjectMappingButton.setSelection(role.isAddProjectMapping());
			changeProjectMappingButton.setSelection(role.isChangeProjectMapping());
			deleteProjectMappingButton.setSelection(role.isDeleteProjectMapping());
			editFieldMappingsButton.setSelection(role.isEditFieldMappings());
			pauseSynchronizationButton.setSelection(role.isPauseSynchronization());
			resumeSynchronizationButton.setSelection(role.isResumeSynchronization());
			resetSynchronizationStatusButton.setSelection(role.isResetSynchronizationStatus());
			deleteIdentityMappingsButton.setSelection(role.isDeleteProjectMappingIdentityMappings());
			editQuarantinedArtifactButton.setSelection(role.isEditQuarantinedArtifact());
			markAsFixedButton.setSelection(role.isMarkAsFixed());
			reopenButton.setSelection(role.isReopen());
			replayButton.setSelection(role.isReplay());
			cancelReplayButton.setSelection(role.isCancelReplay());
			deleteHospitalEntryButton.setSelection(role.isDeleteHospitalEntry());
			editIdentityMappingButton.setSelection(role.isEditIdentityMapping());
			createReverseIdentityMappingButton.setSelection(role.isCreateReverseIdentityMapping());
			deleteIdentityMappingButton.setSelection(role.isDeleteIdentityMapping());
			editLogSettingsButton.setSelection(role.isEditLogSettings());
			consistencyCheckButton.setSelection(role.isConsistencyCheck());
			maintainRolesButton.setSelection(role.isMaintainRoles());
		}
	}

	class RoleLabelProvider implements ITableLabelProvider, IFontProvider {
		
		public RoleLabelProvider() {
			super();
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int index) {
			Role role = (Role)element;
			switch (index) {
			case 0:
				if (role == activeRole) return role.getName() + " (Active)";
				else return role.getName();	
			default:
				break;
			}
			return "";
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String columnIndex) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {		
		}

		public Font getFont(Object obj) {
			Role role = (Role)obj;
			if (role == activeRole) {
				if (null == italicFont) {
					Font defaultFont = JFaceResources.getDefaultFont();
			        FontData[] data = defaultFont.getFontData();
			        for (int i = 0; i < data.length; i++) {
			          data[i].setStyle(SWT.ITALIC | SWT.BOLD);
			        }
			        italicFont = new Font(roleTableViewer.getControl().getDisplay(), data);
				}
				return italicFont;
			}
			return null;
		}
		
	}
	
}
