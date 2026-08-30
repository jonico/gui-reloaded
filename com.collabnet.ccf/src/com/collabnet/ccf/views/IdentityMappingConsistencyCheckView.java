package com.collabnet.ccf.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.part.ViewPart;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.IProjectMappingsChangeListener;
import com.collabnet.ccf.actions.ChangeSynchronizationStatusAction;
import com.collabnet.ccf.actions.IdentityMappingEditAction;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.model.IdentityMapping;
import com.collabnet.ccf.model.IdentityMappingConsistencyCheck;
import com.collabnet.ccf.model.InconsistentIdentityMapping;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;

public class IdentityMappingConsistencyCheckView extends ViewPart implements IProjectMappingsChangeListener {
	private static Landscape landscape;
	private static SynchronizationStatus synchronizationStatus;
	
	private TreeViewer treeViewer;
	private CcfDataProvider dataProvider;
	
	private SynchronizationStatus[] projectMappings;
	private IdentityMappingConsistencyCheck[] consistencyChecks;
	private IdentityMapping[] inconsistentIdentityMappings;
	
	private static IdentityMappingConsistencyCheckView view;
	public static final String ID = "com.collabnet.ccf.views.IdentityMappingConsistencyCheckView";
	
	public IdentityMappingConsistencyCheckView() {
		super();
		view = this;
		Activator.addChangeListener(this);
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

		treeViewer.setLabelProvider(new ConsistencyLabelProvider());
		treeViewer.setContentProvider(new ConsistencyContentProvider());
		
		treeViewer.setUseHashlookup(true);
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.FILL;
		treeViewer.getControl().setLayoutData(layoutData);
		if (null != landscape) getInconsistencies();
		treeViewer.setInput(this);
		treeViewer.setAutoExpandLevel(2);
		
		treeViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent se) {
				IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
				if (null != selection && 1 == selection.size()) {
					ActionDelegate action = null;
					if (selection.getFirstElement() instanceof SynchronizationStatus && Activator.getDefault().getActiveRole().isChangeProjectMapping()) {
						action = new ChangeSynchronizationStatusAction();
					}
					else if (selection.getFirstElement() instanceof IdentityMapping && Activator.getDefault().getActiveRole().isEditIdentityMapping()) {
						action = new IdentityMappingEditAction();
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
						Activator.handleError(exception);
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
		
		getSite().setSelectionProvider(treeViewer);
	}
	
	private void createMenus() {
		MenuManager menuMgr = new MenuManager("#ConsistencyCheckPopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
	}
	
	private void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void createToolbar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();		
		toolbarManager.add(new RefreshAction());
		toolbarManager.add(new Separator());
	}
	
	private void getInconsistencies() {
		if (null == landscape) return;
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				CcfDataProvider dataProvider = getDataProvider();
				List<SynchronizationStatus> problemProjectMappings = new ArrayList<SynchronizationStatus>();
				List<IdentityMappingConsistencyCheck> problemChecks = new ArrayList<IdentityMappingConsistencyCheck>();
				List<IdentityMapping> problemIdentityMappings = new ArrayList<IdentityMapping>();
				try {
					SynchronizationStatus[] allMappings = dataProvider.getSynchronizationStatuses(landscape, null);
					for (SynchronizationStatus status : allMappings) {
						if (null == synchronizationStatus || status.equals(synchronizationStatus)) {
							IdentityMappingConsistencyCheck multipleSourceCheck = new IdentityMappingConsistencyCheck(status, IdentityMappingConsistencyCheck.MULTIPLE_SOURCE_TO_ONE_TARGET);
							IdentityMapping[] inconsistentMappings = dataProvider.getIdentityMappingConsistencyCheckViolations(multipleSourceCheck);
							if (0 < inconsistentMappings.length) {
								if (!problemProjectMappings.contains(status)) problemProjectMappings.add(status);
								problemChecks.add(multipleSourceCheck);
								for (IdentityMapping mapping : inconsistentMappings) {
									problemIdentityMappings.add(mapping);
								}
							}
							IdentityMappingConsistencyCheck multipleTargetCheck = new IdentityMappingConsistencyCheck(status, IdentityMappingConsistencyCheck.MULTIPLE_TARGET_TO_ONE_SOURCE);
							inconsistentMappings = dataProvider.getIdentityMappingConsistencyCheckViolations(multipleTargetCheck);
							if (0 < inconsistentMappings.length) {
								if (!problemProjectMappings.contains(status)) problemProjectMappings.add(status);
								problemChecks.add(multipleTargetCheck);
								for (IdentityMapping mapping : inconsistentMappings) {
									problemIdentityMappings.add(mapping);
								}
							}
							IdentityMappingConsistencyCheck oneWayCheck = new IdentityMappingConsistencyCheck(status, IdentityMappingConsistencyCheck.ONE_WAY);					
							inconsistentMappings = dataProvider.getIdentityMappingConsistencyCheckViolations(oneWayCheck);
							if (0 < inconsistentMappings.length) {
								if (!problemProjectMappings.contains(status)) problemProjectMappings.add(status);
								problemChecks.add(oneWayCheck);
								for (IdentityMapping mapping : inconsistentMappings) {
									problemIdentityMappings.add(mapping);
								}
							}	
						}
					}
					projectMappings = new SynchronizationStatus[problemProjectMappings.size()];
					problemProjectMappings.toArray(projectMappings);
					consistencyChecks = new IdentityMappingConsistencyCheck[problemChecks.size()];
					problemChecks.toArray(consistencyChecks);
					inconsistentIdentityMappings = new IdentityMapping[problemIdentityMappings.size()];
					problemIdentityMappings.toArray(inconsistentIdentityMappings);
					if (null == synchronizationStatus) {
						if (null != landscape) {
							setContentDescription(landscape.getDescription());
						}
					} else {
						setContentDescription(synchronizationStatus.toString());
					}
				} catch (Exception e) {
					setContentDescription("Could not connect to database.  See error log.");
					inconsistentIdentityMappings = new IdentityMapping[0];
					Activator.handleDatabaseError(e, false, true, "Identity Mapping Consistency Check View");
				}
			}			
		});
	}

	@Override
	public void setFocus() {

	}
	
	@Override
	public void dispose() {
		Activator.removeChangeListener(this);
		view = null;
		super.dispose();
	}
	
	public void refresh() {
		getInconsistencies();	
		treeViewer.refresh();
		TreeItem[] items = treeViewer.getTree().getItems();
		for (TreeItem item : items) {
			treeViewer.setExpandedState(item.getData(), true);
		}
		treeViewer.expandAll();
	}

	public static IdentityMappingConsistencyCheckView getView() {
		return view;
	}
	
	public static void setLandscape(Landscape landscape) {
		IdentityMappingConsistencyCheckView.landscape = landscape;
	}
	
	public static void setSynchronizationStatus(SynchronizationStatus synchronizationStatus) {
		IdentityMappingConsistencyCheckView.synchronizationStatus = synchronizationStatus;
	}
	
	public CcfDataProvider getDataProvider() {
		if (null == dataProvider) dataProvider = new CcfDataProvider();
		return dataProvider;
	}
	
	public void refresh(Object object) {
		getInconsistencies();
		Object[] expandedElements = treeViewer.getExpandedElements();
		for (Object obj : expandedElements) {
			if (obj.equals(object)) {
				treeViewer.refresh(obj);
			}
		}
	}
	
	public void changed(ProjectMappings projectMappings) {
		refresh();
	}
	
	class ConsistencyLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			if (element instanceof SynchronizationStatus) {
				if (((SynchronizationStatus)element).isPaused())
					return Activator.getImage(Activator.IMAGE_SYNC_STATUS_ENTRY_PAUSED);
				else
					return Activator.getImage(Activator.IMAGE_SYNC_STATUS_ENTRY);
			}
			else if (element instanceof Exception) return Activator.getImage(Activator.IMAGE_ERROR);
			else if (element instanceof IdentityMappingConsistencyCheck) {
				IdentityMappingConsistencyCheck consistencyCheck = (IdentityMappingConsistencyCheck)element;
				switch (consistencyCheck.getType()) {
				case IdentityMappingConsistencyCheck.MULTIPLE_SOURCE_TO_ONE_TARGET:
					return Activator.getImage(Activator.IMAGE_MULTIPLE_SOURCE);
				case IdentityMappingConsistencyCheck.MULTIPLE_TARGET_TO_ONE_SOURCE:
					return Activator.getImage(Activator.IMAGE_MULTIPLE_TARGET);
				case IdentityMappingConsistencyCheck.ONE_WAY:
					return Activator.getImage(Activator.IMAGE_ONE_WAY);
				default:
					return null;
				}
			}
			else if (element instanceof IdentityMapping) {
				return Activator.getImage(Activator.IMAGE_IDENTITY_MAPPING);
			}
			else if (element instanceof String) {
				return Activator.getImage(Activator.IMAGE_NO_INCONSISTENCIES);
			}			
			else return super.getImage(element);
		}
		
		public String getText(Object element) {
			if (element instanceof Exception) {
				if (null == ((Exception)element).getMessage()) return super.getText(element);
				else return ((Exception)element).getMessage();
			}
			else if (element instanceof IdentityMapping) {
				IdentityMapping identityMapping = (IdentityMapping)element;
				return identityMapping.getEditableValue().toString();
			}
			else return super.getText(element);
		}
	}	
	
	class ConsistencyContentProvider implements ITreeContentProvider {
		public Object getParent(Object element) {
			if (element instanceof SynchronizationStatus) {
				return this;
			}
			if (element instanceof IdentityMappingConsistencyCheck) {
				IdentityMappingConsistencyCheck consistencyCheck = (IdentityMappingConsistencyCheck)element;
				return consistencyCheck.getSynchronizationStatus();
			}
			return null;
		}
		
		public boolean hasChildren(Object element) {
			return !(element instanceof Exception) && !(element instanceof IdentityMapping) && !(element instanceof String);
		}
		
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof IdentityMappingConsistencyCheckView && null != landscape) {
				if (null == projectMappings || 0 == projectMappings.length) {
					String[] noInconsistencies = { "No inconsistencies found" };
					return noInconsistencies;
				} else {
					return projectMappings;
				}
			}
			else if (parentElement instanceof SynchronizationStatus) {
				List<IdentityMappingConsistencyCheck> mappingChecks = new ArrayList<IdentityMappingConsistencyCheck>();
				for (IdentityMappingConsistencyCheck check : consistencyChecks) {
					if (check.getSynchronizationStatus() == parentElement) {
						mappingChecks.add(check);
					}
				}
				IdentityMappingConsistencyCheck[] checkArray = new IdentityMappingConsistencyCheck[mappingChecks.size()];
				mappingChecks.toArray(checkArray);
				return checkArray;
			}
			else if (parentElement instanceof IdentityMappingConsistencyCheck) {
				List<IdentityMapping> checkViolations = new ArrayList<IdentityMapping>();
				for (IdentityMapping mapping : inconsistentIdentityMappings) {
					if (((InconsistentIdentityMapping)mapping).getConsistencyCheck() == parentElement) {
						checkViolations.add(mapping);
					}
				}
				IdentityMapping[] mappingArray = new IdentityMapping[checkViolations.size()];
				checkViolations.toArray(mappingArray);
				return mappingArray;			
			}
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			treeViewer = (TreeViewer)viewer;
		}
	}
	
	class RefreshAction extends Action {
		public RefreshAction() {
			super();
			setImageDescriptor(Activator.getDefault().getImageDescriptor(Activator.IMAGE_REFRESH));
			setToolTipText("Refresh View");
		}
		public void run() {
			refresh();
		}
	}

}
