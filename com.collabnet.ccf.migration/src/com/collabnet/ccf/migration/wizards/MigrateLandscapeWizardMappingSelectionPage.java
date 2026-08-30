/*******************************************************************************
 * Copyright (c) 2011 CollabNet.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     CollabNet - initial API and implementation
 ******************************************************************************/
package com.collabnet.ccf.migration.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.api.model.Directions;
import com.collabnet.ccf.api.model.ExternalApp;
import com.collabnet.ccf.api.model.RepositoryMapping;
import com.collabnet.ccf.api.model.RepositoryMappingDirection;
import com.collabnet.ccf.api.model.RepositoryMappingDirectionStatus;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.teamforge.api.main.ProjectDO;
import com.collabnet.teamforge.api.tracker.TrackerDO;

public class MigrateLandscapeWizardMappingSelectionPage extends WizardPage {
	private MappingsContentProvider mappingsContentProvider;
	private SynchronizationStatus[] projectMappings;
	private Map<SynchronizationStatus, String> projectMappingMap;
	private Map<String, ProjectDO> projectMap;
	private Map<String, TrackerDO> trackerMap;
	private Map<String, ExternalApp> externalAppMap;
	private Map<ExternalApp, List<RepositoryMapping>> repositoryMappingMap;
	private Map<RepositoryMapping, List<RepositoryMappingDirection>> repositoryMappingDirectionMap;
	private Map<RepositoryMappingDirection, SynchronizationStatus> synchronizationStatusMap;
	private ExternalApp[] externalApps;
	private Exception exception;
	private RepositoryMapping[] selectedRepositoryMappings;
	private SynchronizationStatus[] selectedProjectMappings;
	private List<String> selectedProjectIds;
	
	private CheckboxTreeViewer treeViewer;
	private Button selectAllButton;
	private Button deselectAllButton;
	private Button expandAllButton;
	private Button collapseAllButton;

	public MigrateLandscapeWizardMappingSelectionPage() {
		super("mappingPage", "Project Mappings", null);
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		setMessage("Select the project mappings to migrate.");		
		Composite outerContainer = new Composite(parent,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		outerContainer.setLayout(layout);
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		treeViewer = new CheckboxTreeViewer(outerContainer, SWT.MULTI | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		data.heightHint = 400;
		treeViewer.getTree().setLayoutData(data);
		mappingsContentProvider = new MappingsContentProvider();
		treeViewer.setContentProvider(mappingsContentProvider);
		treeViewer.setLabelProvider(new MappingsLabelProvider());
		treeViewer.setUseHashlookup(true);
		treeViewer.setSorter(new ViewerSorter());
		treeViewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
              handleCheckStateChange(event);
            }
          });
		
		Composite buttonGroup = new Composite(outerContainer,SWT.NONE);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.makeColumnsEqualWidth = true;
		buttonLayout.numColumns = 4;
		buttonGroup.setLayout(buttonLayout);
		buttonGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		
		selectAllButton = new Button(buttonGroup, SWT.PUSH);
		selectAllButton.setText("Select All");
		selectAllButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		deselectAllButton = new Button(buttonGroup, SWT.PUSH);
		deselectAllButton.setText("Deselect All");
		deselectAllButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		expandAllButton = new Button(buttonGroup, SWT.PUSH);
		expandAllButton.setText("Expand All");
		expandAllButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		collapseAllButton = new Button(buttonGroup, SWT.PUSH);
		collapseAllButton.setText("Collapse All");
		collapseAllButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		SelectionListener buttonListener = new SelectionAdapter() {			
			@SuppressWarnings("deprecation")
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource() == selectAllButton) {
					treeViewer.setAllChecked(true);
					updateSelectedProjectMappings();
				}
				else if (e.getSource() == deselectAllButton) {
					treeViewer.setAllChecked(false);
					updateSelectedProjectMappings();
				}
				else if (e.getSource() == expandAllButton) {
					treeViewer.expandAll();
				}
				else if (e.getSource() == collapseAllButton) {
					treeViewer.collapseAll();
				}
			}
		};
		
		selectAllButton.addSelectionListener(buttonListener);
		deselectAllButton.addSelectionListener(buttonListener);
		expandAllButton.addSelectionListener(buttonListener);
		collapseAllButton.addSelectionListener(buttonListener);

		setControl(outerContainer);
	}
	
	private void handleCheckStateChange(CheckStateChangedEvent event) {
		((CheckboxTreeViewer)treeViewer).setGrayed(event.getElement(), false);
		((CheckboxTreeViewer)treeViewer).setSubtreeChecked(event.getElement(), event.getChecked());	
		updateParentState(event.getElement(), event.getChecked());
		updateSelectedProjectMappings();
	}
	
	private void updateParentState(Object child, boolean baseChildState) {
		if (null == child) return;
		Object parent = mappingsContentProvider.getParent(child);
		if (null == parent) return;
		boolean allSameState = true;
		Object[] children = null;
		children = mappingsContentProvider.getChildren(parent);
		for (int i = children.length - 1; i >= 0; i--) {
			if (treeViewer.getChecked(children[i]) != baseChildState || treeViewer.getGrayed(children[i])) {
			   allSameState = false;
		       break;
			}
		}
		treeViewer.setGrayed(parent, !allSameState);
		treeViewer.setChecked(parent, !allSameState || baseChildState);
		updateParentState(parent, baseChildState);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setVisible(boolean visible) {
		setErrorMessage(null);
		exception = null;
		if (visible && null == projectMappings) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {				
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						projectMappings = ((MigrateLandscapeWizard)getWizard()).getProjectMappings(monitor);
						projectMappingMap = ((MigrateLandscapeWizard)getWizard()).getProjectMappingMap(monitor);
						projectMap = ((MigrateLandscapeWizard)getWizard()).getProjectMap();
						trackerMap = ((MigrateLandscapeWizard)getWizard()).getTrackerMap();
					} catch (Exception e) {
						exception = e;
					}
				}
			};
			try {
				getContainer().run(true, false, runnable);
			} catch (Exception e) {
				exception = e;
			}
			if (null == exception) {
				treeViewer.setInput(MigrateLandscapeWizardMappingSelectionPage.this);
				treeViewer.expandAll();
				treeViewer.setAllChecked(true);
				updateSelectedProjectMappings();
				if (null == projectMappings || 0 == projectMappings.length) {
					setErrorMessage("There are no unmigrated project mappings to migrate.");
				}
				else {
					setPageComplete(true);
				}
			}
		};
		if (null != exception) {
			Activator.handleError(exception);
			setErrorMessage(exception.getMessage());
		}
		super.setVisible(visible);
	}

	public RepositoryMapping[] getSelectedRepositoryMappings() {
		return selectedRepositoryMappings;
	}

	public SynchronizationStatus[] getSelectedProjectMappings() {
		return selectedProjectMappings;
	}
	
	public List<String> getSelectedProjectIds() {
		return selectedProjectIds;
	}
	
	private void updateSelectedProjectMappings() {
		List<RepositoryMapping> selectedRepositoryMappingsList = new ArrayList<RepositoryMapping>();
		List<SynchronizationStatus> selectedProjectMappingsList = new ArrayList<SynchronizationStatus>();
		selectedProjectIds = new ArrayList<String>();
		Object[] checkedItems = treeViewer.getCheckedElements();
		for (Object checkedItem : checkedItems) {
			if (checkedItem instanceof RepositoryMappingDirection) {
				RepositoryMappingDirection repositoryMappingDirection = (RepositoryMappingDirection)checkedItem;
				SynchronizationStatus projectMapping = synchronizationStatusMap.get(repositoryMappingDirection);
				selectedProjectMappingsList.add(projectMapping);
				String projectId = repositoryMappingDirection.getRepositoryMapping().getExternalApp().getProjectPath();
				if (!selectedProjectIds.contains(projectId)) {
					selectedProjectIds.add(projectId);
				}
				if (!selectedRepositoryMappingsList.contains(repositoryMappingDirection.getRepositoryMapping())) {
					selectedRepositoryMappingsList.add(repositoryMappingDirection.getRepositoryMapping());
				}
			}
		}
		selectedProjectMappings = new SynchronizationStatus[selectedProjectMappingsList.size()];
		selectedProjectMappingsList.toArray(selectedProjectMappings);
		selectedRepositoryMappings = new RepositoryMapping[selectedRepositoryMappingsList.size()];
		selectedRepositoryMappingsList.toArray(selectedRepositoryMappings);
		setPageComplete(selectedProjectMappings.length > 0);
	}

	private ExternalApp[] getExternalApps() {
		if (null == externalApps) {
			externalAppMap = new HashMap<String, ExternalApp>();
			List<ExternalApp> externalAppList = new ArrayList<ExternalApp>();
			Collection<ProjectDO> projects = projectMap.values();
			long id = 1;
			for (ProjectDO project : projects) {
				ExternalApp externalApp = new ExternalApp();
				externalApp.setProjectTitle(project.getTitle());
				externalApp.setProjectPath(project.getId());
				externalApp.setId(id++);
				externalAppList.add(externalApp);
				externalAppMap.put(project.getId(), externalApp);
			}
			externalApps = new ExternalApp[externalAppList.size()];
			externalAppList.toArray(externalApps);
		}
		return externalApps;
	}
	
	private RepositoryMapping[] getRepositoryMappings(ExternalApp externalApp) {
		if (null == repositoryMappingMap) {
			synchronizationStatusMap = new HashMap<RepositoryMappingDirection, SynchronizationStatus>();
			repositoryMappingMap = new HashMap<ExternalApp, List<RepositoryMapping>>();
			repositoryMappingDirectionMap = new HashMap<RepositoryMapping, List<RepositoryMappingDirection>>();
			List<String> rmList = new ArrayList<String>();
			Map<String, RepositoryMapping> rmMap = new HashMap<String, RepositoryMapping>();
			long id = 1;
			long rmdId = 1;
			for (SynchronizationStatus projectMapping : projectMappings) {
				RepositoryMappingDirection repositoryMappingDirection = new RepositoryMappingDirection();
				if (projectMapping.isPaused()) {
					repositoryMappingDirection.setStatus(RepositoryMappingDirectionStatus.PAUSED);
				}
				else {
					repositoryMappingDirection.setStatus(RepositoryMappingDirectionStatus.RUNNING);
				}
				repositoryMappingDirection.setId(rmdId);
				String teamForgeRepositoryId = null;
				String participantRepositoryId = null;
				if (projectMapping.getSourceSystemKind().startsWith("TF")) {
					teamForgeRepositoryId = projectMapping.getSourceRepositoryId();
					participantRepositoryId = projectMapping.getTargetRepositoryId();
					repositoryMappingDirection.setDirection(Directions.FORWARD);
				}
				else if (projectMapping.getTargetSystemKind().startsWith("TF")) {
					teamForgeRepositoryId = projectMapping.getTargetRepositoryId();
					participantRepositoryId = projectMapping.getSourceRepositoryId();
					repositoryMappingDirection.setDirection(Directions.REVERSE);
				}
				
				if (rmList.contains(teamForgeRepositoryId + participantRepositoryId)) {
					RepositoryMapping repositoryMapping = rmMap.get(teamForgeRepositoryId + participantRepositoryId);
					repositoryMappingDirection.setRepositoryMapping(repositoryMapping);
					repositoryMappingDirectionMap.get(repositoryMapping).add(repositoryMappingDirection);
				}
				else {		
					rmList.add(teamForgeRepositoryId + participantRepositoryId);
					ExternalApp projectMappingExternalApp = externalAppMap.get(projectMappingMap.get(projectMapping));
					List<RepositoryMapping> repositoryMappingList = repositoryMappingMap.get(projectMappingExternalApp);
					if (null == repositoryMappingList) {
						repositoryMappingList = new ArrayList<RepositoryMapping>();
						repositoryMappingMap.put(projectMappingExternalApp, repositoryMappingList);
					}
					RepositoryMapping repositoryMapping = new RepositoryMapping();
					repositoryMapping.setExternalApp(projectMappingExternalApp);
					repositoryMapping.setParticipantRepositoryId(participantRepositoryId);
					repositoryMapping.setTeamForgeRepositoryId(teamForgeRepositoryId);
					repositoryMapping.setId(id++);
					repositoryMappingList.add(repositoryMapping);
					
					repositoryMappingDirection.setRepositoryMapping(repositoryMapping);
					List<RepositoryMappingDirection> repositoryMappingDirectionList = new ArrayList<RepositoryMappingDirection>();
					repositoryMappingDirectionList.add(repositoryMappingDirection);
					repositoryMappingDirectionMap.put(repositoryMapping, repositoryMappingDirectionList);
					
					rmMap.put(teamForgeRepositoryId + participantRepositoryId, repositoryMapping);
				}
				synchronizationStatusMap.put(repositoryMappingDirection, projectMapping);
			}
		}
	
		List<RepositoryMapping> repositoryMappingList = repositoryMappingMap.get(externalApp);
		if (null != repositoryMappingList) {
			RepositoryMapping[] repositoryMappings = new RepositoryMapping[repositoryMappingList.size()];
			repositoryMappingList.toArray(repositoryMappings);
			return repositoryMappings;
		}
		return null;
	}
	
	private RepositoryMappingDirection[] getRepositoryMappingDirections(RepositoryMapping repositoryMapping) {
		List<RepositoryMappingDirection> repositoryMappingDirectionList = repositoryMappingDirectionMap.get(repositoryMapping);
		if (null != repositoryMappingDirectionList) {
			RepositoryMappingDirection[] repositoryMappingDirections = new RepositoryMappingDirection[repositoryMappingDirectionList.size()];
			repositoryMappingDirectionList.toArray(repositoryMappingDirections);
			return repositoryMappingDirections;
		}
		return null;
	}
	
	private String getTeamForgeFriendlyName(String repositoryId) {
		if (repositoryId.startsWith("tracker")) { //$NON-NLS-1$
			String trackerId = getProjectOrTrackerId(repositoryId);
			String friendlyName = null;
			TrackerDO trackerDO = trackerMap.get(trackerId);
			if (null != trackerDO) {
				friendlyName = trackerDO.getTitle();
				if (repositoryId.endsWith("MetaData")) { //$NON-NLS-1$
					friendlyName = friendlyName + " " + "MetaData"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (null != friendlyName) {
				return friendlyName;
			}
		}
		else {
			return "Planning Folders"; //$NON-NLS-1$
		}
		return repositoryId;
	}
	
	private String getScrumWorksFriendlyName(String repositoryId) {
		int index = repositoryId.indexOf("("); //$NON-NLS-1$
		if (-1 != index) {
			StringBuffer stringBuffer = new StringBuffer(repositoryId.substring(0, index));
			index = repositoryId.indexOf(")-"); //$NON-NLS-1$
			if (-1 != index) {
				stringBuffer.append(" " + repositoryId.substring(index+2)); //$NON-NLS-1$
				if (!repositoryId.endsWith("MetaData")) { //$NON-NLS-1$
					stringBuffer.append("s"); //$NON-NLS-1$
				}
			}
			return stringBuffer.toString();
		}	
		return repositoryId;
	}
	
	private static String getProjectOrTrackerId(String repositoryId) {
		int index = repositoryId.indexOf("-"); //$NON-NLS-1$
		if (-1 != index) {
			return repositoryId.substring(0, index);
		}
		return repositoryId;
	}
	
	private class MappingsLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			if (element instanceof ExternalApp) {
				return com.collabnet.ccf.migration.Activator.getDefault().getImageRegistry().get(com.collabnet.ccf.migration.Activator.IMG_PROJECT);
			}
			else if (element instanceof RepositoryMapping) {
				return com.collabnet.ccf.migration.Activator.getDefault().getImageRegistry().get(com.collabnet.ccf.migration.Activator.IMG_REPOSITORY_MAPPING);
			}
			else if (element instanceof RepositoryMappingDirection) {
				RepositoryMappingDirection repositoryMappingDirection = (RepositoryMappingDirection)element;
				if (repositoryMappingDirection.getStatus().equals(RepositoryMappingDirectionStatus.RUNNING)) {
					return com.collabnet.ccf.migration.Activator.getDefault().getImageRegistry().get(com.collabnet.ccf.migration.Activator.IMG_REPOSITORY_MAPPING_DIRECTION_RUNNING);
				}
				else {
					return com.collabnet.ccf.migration.Activator.getDefault().getImageRegistry().get(com.collabnet.ccf.migration.Activator.IMG_REPOSITORY_MAPPING_DIRECTION_PAUSED);
				}
			}
			return null;
		}
		@Override
		public String getText(Object element) {
			if (element instanceof ExternalApp) {
				return ((ExternalApp)element).getProjectTitle();
			}
			else if (element instanceof RepositoryMapping) {
				RepositoryMapping repositoryMapping = (RepositoryMapping)element;
				String participantRepository;
				Landscape landscape = ((MigrateLandscapeWizard)getWizard()).getLandscape();
				if (landscape.getType1().equals("SWP") || landscape.getType2().equals("SWP")) {
					participantRepository = getScrumWorksFriendlyName(repositoryMapping.getParticipantRepositoryId());
				}
				else {
					participantRepository = repositoryMapping.getParticipantRepositoryId();
				}
				return getTeamForgeFriendlyName(repositoryMapping.getTeamForgeRepositoryId()) + " " + "\u21D4" + " " + participantRepository;
			}
			else if (element instanceof RepositoryMappingDirection) {
				RepositoryMappingDirection repositoryMappingDirection = (RepositoryMappingDirection)element;
				String teamForgeRepository = getTeamForgeFriendlyName(repositoryMappingDirection.getRepositoryMapping().getTeamForgeRepositoryId());
				String participantRepository;
				Landscape landscape = ((MigrateLandscapeWizard)getWizard()).getLandscape();
				if (landscape.getType1().equals("SWP") || landscape.getType2().equals("SWP")) {
					participantRepository = getScrumWorksFriendlyName(repositoryMappingDirection.getRepositoryMapping().getParticipantRepositoryId());
				}
				else {
					participantRepository = repositoryMappingDirection.getRepositoryMapping().getParticipantRepositoryId();
				}
				if (repositoryMappingDirection.getDirection().equals(Directions.REVERSE)) {
					return participantRepository + " " + "\u21D2" + " " + teamForgeRepository;
				}
				else {
					return teamForgeRepository + " " + "\u21D2" + " " + participantRepository;
				}
			}
			return null;
		}		
	}
	
	private class MappingsContentProvider implements ITreeContentProvider {
		public void dispose() {}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement == MigrateLandscapeWizardMappingSelectionPage.this) {
				return getExternalApps();
			}
			else if (parentElement instanceof ExternalApp) {
				return getRepositoryMappings((ExternalApp)parentElement);
			}
			else if (parentElement instanceof RepositoryMapping) {
				return getRepositoryMappingDirections((RepositoryMapping)parentElement);
			}
			return null;
		}

		public Object getParent(Object element) {
			if (element instanceof RepositoryMappingDirection) {
				return ((RepositoryMappingDirection)element).getRepositoryMapping();
			}
			else if (element instanceof RepositoryMapping) {
				return ((RepositoryMapping)element).getExternalApp();
			}
			else {
				return null;
			}
		}

		public boolean hasChildren(Object element) {
			if (element instanceof RepositoryMappingDirection) {
				return false;
			}
			else {
				return true;
			}
		}
		
	}

}
