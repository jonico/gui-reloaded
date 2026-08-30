package com.collabnet.ccf.views;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.part.ViewPart;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.ICcfParticipant;
import com.collabnet.ccf.IProjectMappingVisibilityChecker;
import com.collabnet.ccf.IProjectMappingsChangeListener;
import com.collabnet.ccf.IRoleChangedListener;
import com.collabnet.ccf.actions.ChangeSynchronizationStatusAction;
import com.collabnet.ccf.actions.EditLandscapeAction;
import com.collabnet.ccf.actions.EditLogAction;
import com.collabnet.ccf.actions.JmxBrowserAction;
import com.collabnet.ccf.actions.JmxConsoleAction;
import com.collabnet.ccf.actions.NewLandscapeAction;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.dialogs.ProjectMappingFilterDialog;
import com.collabnet.ccf.dialogs.RoleLoginDialog;
import com.collabnet.ccf.model.AdministratorProjectMappings;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.Log;
import com.collabnet.ccf.model.Logs;
import com.collabnet.ccf.model.MappingGroup;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.Role;
import com.collabnet.ccf.model.SynchronizationStatus;

public class CcfExplorerView extends ViewPart implements IProjectMappingsChangeListener, IRoleChangedListener {
	private static CcfExplorerView view;
	private TreeViewer treeViewer;
	private CcfDataProvider dataProvider;
	private CcfComparator ccfComparator = new CcfComparator();
	private IDialogSettings settings = Activator.getDefault().getDialogSettings();
	private Font italicFont;
	private Role activeRole = Activator.getDefault().getActiveRole();
	private NewLandscapeAction toolbarNewLandscapeAction = new NewLandscapeAction("New CCF Landscape...");
	private boolean showHidden;
	public static final String PROJECT_MAPPING_SORT_ORDER = "CcfExplorerView.projectMappingSort";
	public static final String PROJECT_MAPPING_SORT_ORDER_REPOSITORY_TYPE = "CcfExplorerView.projectMappingSortRepositoryType";
	public static final int SORT_BY_SOURCE_REPOSITORY = 0;
	public static final int SORT_BY_TARGET_REPOSITORY = 1;	
	public static final int SORT_BY_GROUP = 3;
	public static final int SORT_BY_REPOSITORY_TYPE = 4;
	
	public static final String ID = "com.collabnet.ccf.views.CcfExplorerView";

	public CcfExplorerView() {
		super();
		view = this;
		Activator.addChangeListener(this);
		Activator.addRoleChangedListener(this);
		showHidden = settings.getBoolean("CcfExplorerView.showHidden");
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 2;
		parent.setLayout(layout);
		
		treeViewer = new TreeViewer(parent);

		treeViewer.setLabelProvider(new LandscapeLabelProvider());
		treeViewer.setContentProvider(new LandscapeContentProvider());
		
		int sortOrder = -1;
		String sortRepositoryType = null;
		try {
			sortOrder = settings.getInt(PROJECT_MAPPING_SORT_ORDER);
			sortRepositoryType = settings.get(PROJECT_MAPPING_SORT_ORDER_REPOSITORY_TYPE);
		} catch (Exception e) {}
		if (-1 == sortOrder) {
			sortRepositoryType = Activator.getDefault().getDefaultSortType();
			if (null == sortRepositoryType) {
				sortOrder = 0;
			} else {
				sortOrder = 4;
			}
			settings.put(PROJECT_MAPPING_SORT_ORDER, sortOrder);
			settings.put(PROJECT_MAPPING_SORT_ORDER_REPOSITORY_TYPE, sortRepositoryType);
		}
		ccfComparator.setSortOrder(sortOrder);	
		ccfComparator.setRepositoryType(sortRepositoryType);
		treeViewer.setComparator(ccfComparator);
		
		treeViewer.setUseHashlookup(true);
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.FILL;
		treeViewer.getControl().setLayoutData(layoutData);
		treeViewer.setInput(this);
		
		treeViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent se) {
				IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
				if (null != selection && 1 == selection.size()) {
					ActionDelegate action = null;
					if (selection.getFirstElement() instanceof Landscape && activeRole.isEditLandscape()) {
						action = new EditLandscapeAction();
					}
					else if (selection.getFirstElement() instanceof Log) {
						action = new EditLogAction();
					}
					else if (selection.getFirstElement() instanceof SynchronizationStatus) {
						SynchronizationStatus status = (SynchronizationStatus)selection.getFirstElement();
						if (Landscape.ROLE_ADMINISTRATOR == status.getLandscape().getRole() && activeRole.isChangeProjectMapping()) {
							action = new ChangeSynchronizationStatusAction();
						}
					}
					else if (selection.getFirstElement() instanceof Exception) {
						Exception exception = (Exception)selection.getFirstElement();
						StringBuffer errorMessage = new StringBuffer("An unexpected error occurred.  Review error log for more details.");
						if (null != exception.getLocalizedMessage()) {
							errorMessage.append("\n\n" + exception.getLocalizedMessage());
						}
						if (null != exception.getCause() && null != exception.getCause().getLocalizedMessage()) {
							errorMessage.append("\n\nCause:\n\n" + exception.getCause().getLocalizedMessage());
						}
						MessageDialog.openError(Display.getCurrent().getActiveShell(), "Exception", errorMessage.toString());					
					}
					if (null != action) {
						action.selectionChanged(null, selection);
						action.run(null);						
					}
				}
			}			
		});
		
		createMenus();
		createToolbar();
		
		Transfer[] dragTypes = new Transfer[] { LocalSelectionTransfer.getTransfer() };
		
		DragSourceListener dragSourceListener = new ActiveViewSelectionDragAdapter(treeViewer) {
			@Override
			protected boolean isDragable(ISelection selection) {
				if (null == selection || selection.isEmpty()) return false;
				IStructuredSelection structuredSelection = (IStructuredSelection)selection;
				if (1 < structuredSelection.size()) return false;
				Object selectedObject = structuredSelection.getFirstElement();
				return (selectedObject instanceof Landscape || selectedObject instanceof SynchronizationStatus);
			}			
		};
		treeViewer.addDragSupport(DND.DROP_COPY | DND.DROP_DEFAULT, dragTypes, dragSourceListener);
		
		getSite().setSelectionProvider(treeViewer);
	}
	
	private void createMenus() {
		MenuManager menuMgr = new MenuManager("#CcfExplorerPopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
		
		final IMenuManager barMenuManager = getViewSite().getActionBars().getMenuManager();
		barMenuManager.setRemoveAllWhenShown(true);		
		barMenuManager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager menu) {
        		NewLandscapeAction newLandscapeAction = new NewLandscapeAction("New CCF Landscape...");
        		newLandscapeAction.setEnabled(activeRole.isAddLandscape());
        		barMenuManager.add(newLandscapeAction);  
        		barMenuManager.add(new Separator());
        		MenuManager sortMenu = new MenuManager("Sort project mappings by");
        		SortAction bySourceAction = new SortAction("Source repository", SORT_BY_SOURCE_REPOSITORY, null);
        		SortAction byTargetAction = new SortAction("Target repository", SORT_BY_TARGET_REPOSITORY, null);
        		SortAction byGroupAction = new SortAction("Group", SORT_BY_GROUP, null);
        		sortMenu.add(bySourceAction);
        		sortMenu.add(byTargetAction);
        		sortMenu.add(byGroupAction);
        		List<SortAction> sortByRepositoryTypeActions = new ArrayList<SortAction>();
        		try {
					ICcfParticipant[] ccfParticipants = Activator.getCcfParticipants();
					for (ICcfParticipant ccfParticipant : ccfParticipants) {
						SortAction byRepositoryTypeAction = new SortAction(ccfParticipant.getType() + " repository", SORT_BY_REPOSITORY_TYPE, ccfParticipant.getType());
						sortMenu.add(byRepositoryTypeAction);
						sortByRepositoryTypeActions.add(byRepositoryTypeAction);
					}
				} catch (Exception e) {
					Activator.handleError(e);
				}
        		
        		switch (ccfComparator.getSortOrder()) {
				case SORT_BY_SOURCE_REPOSITORY:
					bySourceAction.setChecked(true);
					break;
				case SORT_BY_TARGET_REPOSITORY:
					byTargetAction.setChecked(true);
					break;
				case SORT_BY_GROUP:
					byGroupAction.setChecked(true);
					break;
				case SORT_BY_REPOSITORY_TYPE:
					boolean actionChecked = false;
					for (SortAction sortAction : sortByRepositoryTypeActions) {
						if (sortAction.getRepositoryType().equals(ccfComparator.getRepositoryType())) {
							sortAction.setChecked(true);
							actionChecked = true;
							break;
						}
					}
					if (actionChecked) {
						break;
					}
				default:
					bySourceAction.setChecked(true);
					break;
				}
        		barMenuManager.add(sortMenu);
        		Action showHiddenAction = new Action("Show hidden project mappings", Action.AS_CHECK_BOX) {
        			public void run() {
        				showHidden = !settings.getBoolean("CcfExplorerView.showHidden");
        				settings.put("CcfExplorerView.showHidden", showHidden);
        				refreshProjectMappings();
        			}
        		};
        		showHiddenAction.setChecked(showHidden);
        		barMenuManager.add(showHiddenAction);
        		barMenuManager.add(new Separator());
        		MenuManager roleMenu = new MenuManager("Active role");
        		Role[] roles = Activator.getDefault().getRoles();
        		for (Role role : roles) {
        			RoleAction roleAction = new RoleAction(role, activeRole != null && role.getName().equals(activeRole.getName()));
        			if (null != activeRole && role.getName().equals(activeRole.getName())) {
        				roleAction.setChecked(true);
        			}
        			roleMenu.add(roleAction);
        		}
        		barMenuManager.add(roleMenu);
            }
		});
   		NewLandscapeAction newLandscapeAction = new NewLandscapeAction("New CCF Landscape...");
		barMenuManager.add(newLandscapeAction);  	
	}
	
	private void fillContextMenu(IMenuManager manager) {
		MenuManager sub = new MenuManager("New", IWorkbenchActionConstants.GROUP_ADD); //$NON-NLS-1$
		NewLandscapeAction newLandscapeAction = new NewLandscapeAction("CCF Landscape");
		newLandscapeAction.setEnabled(activeRole.isAddLandscape());
		sub.add(newLandscapeAction);
		sub.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(sub);	
		
		manager.add(new Separator());
		
		IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
		if (1 == selection.size() && selection.getFirstElement() instanceof Landscape) {

			manager.add(new JmxConsoleAction((Landscape)selection.getFirstElement()));
			
			MenuManager jmxMenu = new MenuManager("Open JMX console in browser", IWorkbenchActionConstants.GROUP_ADD); //$NON-NLS-1$
			JmxBrowserAction jmxToQcAction = new JmxBrowserAction((Landscape)selection.getFirstElement(), JmxBrowserAction.SYSTEM1_SYSTEM2);
			jmxMenu.add(jmxToQcAction);
			JmxBrowserAction jmxFromQcAction = new JmxBrowserAction((Landscape)selection.getFirstElement(), JmxBrowserAction.SYSTEM2_SYSTEM1);
			jmxMenu.add(jmxFromQcAction);			
			manager.add(jmxMenu);
		}
	}
	
	private void createToolbar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();		
		toolbarManager.add(new RefreshAction());
		toolbarManager.add(new FilterAction());
		toolbarManager.add(new Separator());
		toolbarNewLandscapeAction.setEnabled(activeRole.isAddLandscape());
		toolbarManager.add(toolbarNewLandscapeAction);
	}

	@Override
	public void setFocus() {
	}
	
	public void refresh() {
		treeViewer.refresh();
	}
	
	public void refresh(Object object) {
		Object[] expandedElements = treeViewer.getExpandedElements();
		
		if (null == object) {
			TreeItem[] items = treeViewer.getTree().getItems();
			for (TreeItem item : items) {
				if (item.getData() instanceof Landscape) {
					TreeItem[] landscapeItems = item.getItems();
					for (TreeItem landscapeItem : landscapeItems) {
						if (landscapeItem.getData() instanceof ProjectMappings) {
							treeViewer.refresh(landscapeItem.getData());
						}
					}
				}
			}		
		} else {
			for (Object obj : expandedElements) {
				if (obj.equals(object)) {
					treeViewer.refresh(obj);
				}
				if (obj instanceof ProjectMappings && null == object) {
					treeViewer.refresh(obj);
				}
			}
		}
		
		treeViewer.setExpandedElements(expandedElements);
	}
	
	public void refreshProjectMappings() {
		Object[] expandedElements = treeViewer.getExpandedElements();
		for (Object obj : expandedElements) {
			if (obj instanceof ProjectMappings) {
				treeViewer.refresh(obj);
			}
		}
		
		treeViewer.setExpandedElements(expandedElements);
	}
	
	@SuppressWarnings("unchecked")
	public void refreshViewerNode() {
    	IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
        Iterator iter = selection.iterator();
        while (iter.hasNext()) {
        	refresh(iter.next());
        }
	}
	
	public void dispose() {
		Activator.removeChangeListener(this);
		view = null;
		if (null != italicFont && !italicFont.isDisposed()) italicFont.dispose();
		super.dispose();
	}
	
	public CcfDataProvider getDataProvider() {
		if (null == dataProvider) dataProvider = new CcfDataProvider();
		return dataProvider;
	}
	
	public void roleChanged(Role activeRole) {
		this.activeRole = activeRole;
		toolbarNewLandscapeAction.setEnabled(activeRole.isAddLandscape());
	}

	public static CcfExplorerView getView() {
		return view;
	}
	
	private Font getItalicFont() {
		if (null == italicFont) {
			Font defaultFont = JFaceResources.getDefaultFont();
	        FontData[] data = defaultFont.getFontData();
	        for (int i = 0; i < data.length; i++) {
	          data[i].setStyle(SWT.ITALIC | SWT.BOLD);
	        }
	        italicFont = new Font(treeViewer.getControl().getDisplay(), data);
		}
        return italicFont;		
	}
	
	class LandscapeLabelProvider extends LabelProvider implements IFontProvider {
		public Image getImage(Object element) {
			if (element instanceof Landscape) return Activator.getImage((Landscape)element);
			else if (element instanceof ProjectMappings) return Activator.getImage(Activator.IMAGE_PROJECT_MAPPINGS);
			else if (element instanceof Logs) return Activator.getImage(Activator.IMAGE_LOGS);
			else if (element instanceof Log) return Activator.getImage(Activator.IMAGE_LOG);
			else if (element instanceof MappingGroup) {
				return ((MappingGroup)element).getImage();
			}
			else if (element instanceof SynchronizationStatus) {
				if (((SynchronizationStatus)element).isPaused())
					return Activator.getImage(Activator.IMAGE_SYNC_STATUS_ENTRY_PAUSED);
				else
					return Activator.getImage(Activator.IMAGE_SYNC_STATUS_ENTRY);
			}
			else if (element instanceof Exception) return Activator.getImage(Activator.IMAGE_ERROR);
			else return super.getImage(element);
		}
		
		public String getText(Object element) {
			if (element instanceof Landscape) return ((Landscape) element).getDescription();
			else if (element instanceof Exception) {
				if (null == ((Exception)element).getMessage()) return super.getText(element);
				else return ((Exception)element).getMessage();
			}
			else return super.getText(element);
		}

		public Font getFont(Object obj) {
			if (obj instanceof SynchronizationStatus) {
				SynchronizationStatus synchronizationStatus = (SynchronizationStatus)obj;
				if (0 < synchronizationStatus.getHospitalEntries()) {
					return getItalicFont();
				}
			}
			if (obj instanceof MappingGroup) {
				MappingGroup mappingGroup = (MappingGroup)obj;
				if (0 < mappingGroup.getHospitalEntries()) {
					return getItalicFont();
				}
			}
			return null;
		}
	}
	
	class LandscapeContentProvider extends WorkbenchContentProvider {
		private Object[] synchronizationStatuses;
		private SynchronizationStatus[] hiddenSynchronizationStatuses;
		
		public Object getParent(Object element) {
			return null;
		}
		
		public boolean hasChildren(Object element) {
			if (element instanceof SynchronizationStatus || element instanceof Log || element instanceof Exception) return false;
			return true;
		}
		
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof CcfExplorerView) {
				return Activator.getDefault().getLandscapes();
			}
			else if (parentElement instanceof Landscape) {
				ProjectMappings projectMappings = null;
				if (Landscape.ROLE_ADMINISTRATOR == ((Landscape)parentElement).getRole()) {
					projectMappings = new AdministratorProjectMappings((Landscape)parentElement);
				} else {
					projectMappings = new ProjectMappings((Landscape)parentElement);
				}
				Logs logs = new Logs((Landscape)parentElement);
				Object[] children = { projectMappings, logs };
				return children;
			}
			else if (parentElement instanceof MappingGroup) {
				return ((MappingGroup)parentElement).getChildren();
			}
			else if (parentElement instanceof ProjectMappings) {
				final ProjectMappings projectMappings = (ProjectMappings)parentElement;
				BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
					public void run() {
						try {
							synchronizationStatuses = getFilteredSynchronizationStatuses(getDataProvider().getSynchronizationStatuses(projectMappings.getLandscape(), projectMappings));
							IProjectMappingVisibilityChecker[] visibilityCheckers = Activator.getVisibilityCheckers();
							if (null != visibilityCheckers && 0 < visibilityCheckers.length) {
								List<Object> visibleMappingList = new ArrayList<Object>();
								List<SynchronizationStatus> hiddenMappingList = new ArrayList<SynchronizationStatus>();
								for (Object object : synchronizationStatuses) {
									if (object instanceof SynchronizationStatus) {
										SynchronizationStatus projectMapping = (SynchronizationStatus)object;
										boolean visible = true;
										if (!showHidden) {
											for (IProjectMappingVisibilityChecker checker : visibilityCheckers) {
												visible = checker.isProjectMappingVisible(projectMapping);
												if (!visible) {
													break;
												}
											}
										}
										if (visible) {
											visibleMappingList.add(projectMapping);
										} else {
											hiddenMappingList.add(projectMapping);
										}
									} else {
										visibleMappingList.add(object);
									}
								}
								synchronizationStatuses = new Object[visibleMappingList.size()];
								visibleMappingList.toArray(synchronizationStatuses);
								hiddenSynchronizationStatuses = new SynchronizationStatus[hiddenMappingList.size()];
								hiddenMappingList.toArray(hiddenSynchronizationStatuses);
							}
						} catch (Exception e) {
							synchronizationStatuses = new Object[1];
							if (e instanceof SQLException) {
								synchronizationStatuses[0] = new Exception("Could not connect to database.  Please make sure database is started.  See error log for more details.", e);
							} else {
								synchronizationStatuses[0] = e;
								Activator.handleError(e);
							}												
						}
					}					
				});
				
				if (0 == synchronizationStatuses.length || synchronizationStatuses[0] instanceof SynchronizationStatus) {
					if (null != ccfComparator && SORT_BY_REPOSITORY_TYPE == ccfComparator.getSortOrder()) {
						String repositoryType = ccfComparator.getRepositoryType();
						if (null != repositoryType) {
							if (projectMappings.getLandscape().getType1().equals(repositoryType) || projectMappings.getLandscape().getType2().equals(repositoryType)) {
								ICcfParticipant participant = null;
								try {
									participant = Activator.getCcfParticipantForType(repositoryType);
								} catch (Exception e) {}
								if (null != participant) {
									SynchronizationStatus[] projectMappingArray = new SynchronizationStatus[synchronizationStatuses.length];
									for (int i = 0; i < synchronizationStatuses.length; i++) {
										projectMappingArray[i] = (SynchronizationStatus)synchronizationStatuses[i];
									}
									MappingGroup[] mappingGroups = participant.getMappingGroups(projectMappings, projectMappingArray, hiddenSynchronizationStatuses);
									if (null != mappingGroups) {
										synchronizationStatuses = mappingGroups;
									}
								}
							}
						}
					}
				}
				
				return synchronizationStatuses;
			}
			else if (parentElement instanceof Logs) {
				Logs logs = (Logs)parentElement;
				switch (logs.getType()) {
				case Logs.TYPE_FOLDER:
					Logs logs1 = new Logs(logs.getLandscape(), Logs.TYPE_1_2);
					Logs logs2 = new Logs(logs.getLandscape(), Logs.TYPE_2_1);
					Logs[] logsArray = { logs1, logs2 };
					return logsArray;
				default:
					return logs.getLandscape().getLogs(logs);
				}
			}
			return new Object[0];
		}
		
		private SynchronizationStatus[] getFilteredSynchronizationStatuses(SynchronizationStatus[] statuses) {
			setContentDescription("");
			if (!settings.getBoolean("ProjectMappingFilter.filtersSet") || !settings.getBoolean("ProjectMappingFilter.filtersActive")) return statuses;
			boolean hospitalEntriesOnly = false;
			try {
				hospitalEntriesOnly = settings.getBoolean("ProjectMappingFilter.hospitalOnly");
			} catch (Exception e) {}
			String sourceRepository = settings.get("ProjectMappingFilter.sourceRepository");
			String sourceRepositoryCompare = settings.get("ProjectMappingFilter.sourceRepositoryCompare");
			String targetRepository = settings.get("ProjectMappingFilter.targetRepository");
			String targetRepositoryCompare = settings.get("ProjectMappingFilter.targetRepositoryCompare");		
			if (!hospitalEntriesOnly && isEmpty(sourceRepository) && isEmpty(targetRepository)) return statuses;

			setContentDescription("(Filters Active)");
			List<SynchronizationStatus> filteredStatuses = new ArrayList<SynchronizationStatus>();			
			boolean showHospitalCount = Activator.getDefault().getPreferenceStore().getBoolean(Activator.PREFERENCES_SHOW_HOSPITAL_COUNT);
			for (SynchronizationStatus status : statuses) {
				if (showHospitalCount && hospitalEntriesOnly) {
					if (0 == status.getHospitalEntries()) continue;
				}
				if (!isEmpty(sourceRepository)) {
					if (sourceRepositoryCompare.equals("contains")) {
						if (-1 == status.getSourceRepositoryId().indexOf(sourceRepository)) continue;
					} else {
						if (!status.getSourceRepositoryId().equals(sourceRepository)) continue;
					}
				}
				if (!isEmpty(targetRepository)) {
					if (targetRepositoryCompare.equals("contains")) {
						if (-1 == status.getTargetRepositoryId().indexOf(targetRepository)) continue;
					} else {
						if (!status.getTargetRepositoryId().equals(targetRepository)) continue;
					}
				}				
				filteredStatuses.add(status);
			}
			
			SynchronizationStatus[] filteredStatusArray = new SynchronizationStatus[filteredStatuses.size()];
			filteredStatuses.toArray(filteredStatusArray);
			return filteredStatusArray;
		}
		
		private boolean isEmpty(String string) {
			return string == null || string.trim().length() == 0;
		}
	}
	
	class RefreshAction extends Action {
		public RefreshAction() {
			super();
			setImageDescriptor(Activator.getDefault().getImageDescriptor(Activator.IMAGE_REFRESH));
			setToolTipText("Refresh View");
		}
		public void run() {
			treeViewer.refresh();
		}
	}

	public void changed(ProjectMappings projectMappings) {
		refresh(projectMappings);
	}
	
	class CcfComparator extends ViewerComparator {
		private int sortOrder = SORT_BY_SOURCE_REPOSITORY;
		private String repositoryType;

		public void setSortOrder(int sortOrder) {
			this.sortOrder = sortOrder;
		}
		
		public int getSortOrder() {
			return sortOrder;
		}

		public void setRepositoryType(String repositoryType) {
			this.repositoryType = repositoryType;
		}

		public String getRepositoryType() {
			return repositoryType;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof SynchronizationStatus && e2 instanceof SynchronizationStatus) {
				SynchronizationStatus s1 = (SynchronizationStatus)e1;
				SynchronizationStatus s2 = (SynchronizationStatus)e2;
				String cmp1;
				String cmp2;
				
				switch (sortOrder) {
				case SORT_BY_SOURCE_REPOSITORY:
					cmp1 = s1.toString();
					cmp2 = s2.toString();					
					break;
				case SORT_BY_TARGET_REPOSITORY:
					cmp1 = s1.getTargetRepositoryId() + s1.getSourceRepositoryId();
					cmp2 = s2.getTargetRepositoryId() + s2.getSourceRepositoryId();	
					break;
				case SORT_BY_GROUP:
					if (null == s1.getGroup()) {
						cmp1 = "";
					} else {
						cmp1 = s1.getGroup();
					}
					if (null == s2.getGroup()) {
						cmp2 = "";
					} else {
						cmp2 = s2.getGroup();
					}					
					break;					
				case SORT_BY_REPOSITORY_TYPE:
					if (null != repositoryType) {
						if (s1.getSourceSystemKind().startsWith(repositoryType)) {
							cmp1 = s1.getSourceRepositoryId() + s1.getTargetRepositoryId();
						} else {
							cmp1 = s1.getTargetRepositoryId() + s1.getSourceRepositoryId();
						}
						if (s2.getSourceSystemKind().startsWith(repositoryType)) {
							cmp2 = s2.getSourceRepositoryId() + s2.getTargetRepositoryId();
						} else {
							cmp2 = s2.getTargetRepositoryId() + s2.getSourceRepositoryId();
						}
						break;		
					}								
				default:
					cmp1 = s1.toString();
					cmp2 = s2.toString();		
					break;
				}
				return cmp1.compareTo(cmp2);				
			}
			return 0;
		}
	
	}
	
	class SortAction extends Action {
		private int order;
		private String repositoryType;

		public SortAction(String text, int order, String repositoryType) {
			super(text, Action.AS_CHECK_BOX);
			this.order = order;
			this.repositoryType = repositoryType;
		}
		
		public String getRepositoryType() {
			return repositoryType;
		}

		public void run() {
			settings.put(PROJECT_MAPPING_SORT_ORDER, order);
			settings.put(PROJECT_MAPPING_SORT_ORDER_REPOSITORY_TYPE, repositoryType);
			ccfComparator.setSortOrder(order);
			ccfComparator.setRepositoryType(repositoryType);
			refreshProjectMappings();
		}
		
	}
	
	class RoleAction extends Action {
		private Role role;
		private boolean isActiveRole;

		public RoleAction(Role role, boolean isActiveRole) {
			super(role.getName(), Action.AS_CHECK_BOX);
			this.role = role;
			this.isActiveRole = isActiveRole;
		}
		
		@Override
		public String getText() {
			if (!isActiveRole && role.isPasswordRequired()) {
				return role.getName() + "...";
			} else {
				return role.getName();
			}
		}

		public void run() {
			if (isActiveRole) return;
			if (role.isPasswordRequired()) {
				RoleLoginDialog dialog = new RoleLoginDialog(Display.getDefault().getActiveShell(), role);
				if (RoleLoginDialog.OK != dialog.open()) return;
			}
			Activator.getDefault().getPreferenceStore().putValue(Activator.PREFERENCES_ACTIVE_ROLE, role.getName());
			roleChanged(role);
		}
		
	}
	
	class FilterAction extends Action {
		public FilterAction() {
			super();
			setImageDescriptor(Activator.getDefault().getImageDescriptor(Activator.IMAGE_FILTERS));
			setToolTipText("Filters...");
		}
		public void run() {
			ProjectMappingFilterDialog dialog = new ProjectMappingFilterDialog(Display.getDefault().getActiveShell());
			dialog.open();
		}
	}

}
