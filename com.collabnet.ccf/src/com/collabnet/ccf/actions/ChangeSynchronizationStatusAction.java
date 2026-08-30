package com.collabnet.ccf.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.db.Filter;
import com.collabnet.ccf.db.Update;
import com.collabnet.ccf.dialogs.ChangeProjectMappingDialog;
import com.collabnet.ccf.dialogs.ProjectMappingRenameDialog;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;

public class ChangeSynchronizationStatusAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	
	private CcfDataProvider dataProvider = new CcfDataProvider();
	private SynchronizationStatus reverseStatus;

	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		final List<ProjectMappings> projectMappingsList = new ArrayList<ProjectMappings>();
		boolean mappingsChanged = false;
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof SynchronizationStatus) {
				final SynchronizationStatus status = (SynchronizationStatus)object;
				reverseStatus = null;
				try {
					reverseStatus = dataProvider.getReverseSynchronizationStatus(status);
				} catch (Exception e1) {}

				File xslFile = status.getXslFile();
				File graphicalXslFile = status.getGraphicalXslFile();
				File sourceRepositorySchemaFile = status.getSourceRepositorySchemaFile();
				File targetRepositorySchemaFile = status.getTargetRepositorySchemaFile();
				File genericArtifactToSourceRepositorySchemaFile = status.getGenericArtifactToSourceRepositorySchemaFile();
				File genericArtifactToTargetRepositorySchemaFile = status.getGenericArtifactToTargetRepositorySchemaFile();
				File sourceRepositorySchemaToGenericArtifactFile = status.getSourceRepositorySchemaToGenericArtifactFile();
				File targetRepositorySchemaToGenericArtifactFile = status.getTargetRepositorySchemaToGenericArtifactFile();
				File mfdFile = status.getMappingFile(status.getMFDFileName());
				
				File reverseXslFile = null;
				File reverseGraphicalXslFile = null;
				File reverseSourceRepositorySchemaFile = null;
				File reverseTargetRepositorySchemaFile = null;
				File reverseGenericArtifactToSourceRepositorySchemaFile = null;
				File reverseGenericArtifactToTargetRepositorySchemaFile = null;
				File reverseSourceRepositorySchemaToGenericArtifactFile = null;
				File reverseTargetRepositorySchemaToGenericArtifactFile = null;
				File reverseMfdFile = null;
				
				if (null != reverseStatus) {
					reverseXslFile = reverseStatus.getXslFile();
					reverseGraphicalXslFile = reverseStatus.getGraphicalXslFile();
					reverseSourceRepositorySchemaFile = reverseStatus.getSourceRepositorySchemaFile();
					reverseTargetRepositorySchemaFile = reverseStatus.getTargetRepositorySchemaFile();
					reverseGenericArtifactToSourceRepositorySchemaFile = reverseStatus.getGenericArtifactToSourceRepositorySchemaFile();
					reverseGenericArtifactToTargetRepositorySchemaFile = reverseStatus.getGenericArtifactToTargetRepositorySchemaFile();
					reverseSourceRepositorySchemaToGenericArtifactFile = reverseStatus.getSourceRepositorySchemaToGenericArtifactFile();
					reverseTargetRepositorySchemaToGenericArtifactFile = reverseStatus.getTargetRepositorySchemaToGenericArtifactFile();
					reverseMfdFile = reverseStatus.getMappingFile(reverseStatus.getMFDFileName());					
				}
				
				final String oldSourceRepositoryId = status.getSourceRepositoryId();
				final String oldTargetRepositoryId = status.getTargetRepositoryId();
				final ChangeProjectMappingDialog dialog = new ChangeProjectMappingDialog(Display.getDefault().getActiveShell(), status, reverseStatus);
				if (ChangeProjectMappingDialog.CANCEL == dialog.open()) return;
				if (!projectMappingsList.contains(status.getProjectMappings())) {
					projectMappingsList.add(status.getProjectMappings());
				}

				// TODO Someday this could benefit from some refactoring!
				if (dialog.isXslFileNameChanged()) {
					File newXslFile = new File(xslFile.getParentFile(), dialog.getNewXslFileName());
					File newGraphicalXslFile = new File(graphicalXslFile.getParentFile(), dialog.getNewGraphicalXslFileName());
					File newSourceRepositorySchemaFile = new File(sourceRepositorySchemaFile.getParentFile(), dialog.getNewSourceRepositorySchemaFileName());
					File newTargetRepositorySchemaFile = new File(targetRepositorySchemaFile.getParentFile(), dialog.getNewTargetRepositorySchemaFileName());
					File newGenericArtifactToSourceRepositorySchemaFile = new File(genericArtifactToSourceRepositorySchemaFile.getParentFile(), dialog.getNewGenericArtifactToSourceRepositorySchemaFileName());
					File newGenericArtifactToTargetRepositorySchemaFile = new File(genericArtifactToTargetRepositorySchemaFile.getParentFile(), dialog.getNewGenericArtifactToTargetRepositorySchemaFileName());
					File newSourceRepositorySchemaToGenericArtifactFile = new File(sourceRepositorySchemaToGenericArtifactFile.getParentFile(), dialog.getNewSourceRepositorySchemaToGenericArtifactFileName());
					File newTargetRepositorySchemaToGenericArtifactFile = new File(targetRepositorySchemaToGenericArtifactFile.getParentFile(), dialog.getNewTargetRepositorySchemaToGenericArtifactFileName());
					File newMfdFile = new File(mfdFile.getParentFile(), dialog.getNewMfdFileName());
					
					File newReverseXslFile = null;
					File newReverseGraphicalXslFile = null;
					File newReverseSourceRepositorySchemaFile = null;
					File newReverseTargetRepositorySchemaFile = null;
					File newReverseGenericArtifactToSourceRepositorySchemaFile = null;
					File newReverseGenericArtifactToTargetRepositorySchemaFile = null;
					File newReverseSourceRepositorySchemaToGenericArtifactFile = null;
					File newReverseTargetRepositorySchemaToGenericArtifactFile = null;
					File newReverseMfdFile = null;
					
					if (null != reverseStatus) {
						newReverseXslFile = new File(reverseXslFile.getParentFile(), reverseStatus.getXslFileName());
						newReverseGraphicalXslFile = new File(reverseGraphicalXslFile.getParentFile(), reverseStatus.getGraphicalXslFileName());
						newReverseSourceRepositorySchemaFile = new File(reverseSourceRepositorySchemaFile.getParentFile(), reverseStatus.getSourceRepositorySchemaFileName());
						newReverseTargetRepositorySchemaFile = new File(reverseTargetRepositorySchemaFile.getParentFile(), reverseStatus.getTargetRepositorySchemaFileName());
						newReverseGenericArtifactToSourceRepositorySchemaFile = new File(reverseGenericArtifactToSourceRepositorySchemaFile.getParentFile(), reverseStatus.getGenericArtifactToSourceRepositorySchemaFileName());
						newReverseGenericArtifactToTargetRepositorySchemaFile = new File(reverseGenericArtifactToTargetRepositorySchemaFile.getParentFile(), reverseStatus.getGenericArtifactToTargetRepositorySchemaFileName());
						newReverseSourceRepositorySchemaToGenericArtifactFile = new File(reverseSourceRepositorySchemaToGenericArtifactFile.getParentFile(), reverseStatus.getSourceRepositorySchemaToGenericArtifactFileName());
						newReverseTargetRepositorySchemaToGenericArtifactFile = new File(reverseTargetRepositorySchemaToGenericArtifactFile.getParentFile(), reverseStatus.getTargetRepositorySchemaToGenericArtifactFileName());
						newReverseMfdFile = new File(reverseMfdFile.getParentFile(), reverseStatus.getMFDFileName());						
					}
	
					if ((null != xslFile && xslFile.exists() && !newXslFile.exists()) ||
						(null != graphicalXslFile && graphicalXslFile.exists() && !newGraphicalXslFile.exists()) ||
						(null != mfdFile && mfdFile.exists() && !newMfdFile.exists()) ||
						(null != sourceRepositorySchemaFile && sourceRepositorySchemaFile.exists() && !newSourceRepositorySchemaFile.exists()) ||
						(null != targetRepositorySchemaFile && targetRepositorySchemaFile.exists() && !newTargetRepositorySchemaFile.exists()) ||
						(null != genericArtifactToSourceRepositorySchemaFile && genericArtifactToSourceRepositorySchemaFile.exists() && !newGenericArtifactToSourceRepositorySchemaFile.exists()) ||					
						(null != genericArtifactToTargetRepositorySchemaFile && genericArtifactToTargetRepositorySchemaFile.exists() && !newGenericArtifactToTargetRepositorySchemaFile.exists()) ||					
						(null != sourceRepositorySchemaToGenericArtifactFile && sourceRepositorySchemaToGenericArtifactFile.exists() && !newSourceRepositorySchemaToGenericArtifactFile.exists()) ||			
						(null != targetRepositorySchemaToGenericArtifactFile && targetRepositorySchemaToGenericArtifactFile.exists() && !newTargetRepositorySchemaToGenericArtifactFile.exists()) ||						
						(null != reverseXslFile && reverseXslFile.exists() && !newReverseXslFile.exists()) ||
						(null != reverseGraphicalXslFile && reverseGraphicalXslFile.exists() && !newReverseGraphicalXslFile.exists()) ||
						(null != reverseMfdFile && reverseMfdFile.exists() && !newReverseMfdFile.exists()) ||
						(null != reverseSourceRepositorySchemaFile && reverseSourceRepositorySchemaFile.exists() && !newReverseSourceRepositorySchemaFile.exists()) ||
						(null != reverseTargetRepositorySchemaFile && reverseTargetRepositorySchemaFile.exists() && !newReverseTargetRepositorySchemaFile.exists()) ||
						(null != reverseGenericArtifactToSourceRepositorySchemaFile && reverseGenericArtifactToSourceRepositorySchemaFile.exists() && !newReverseGenericArtifactToSourceRepositorySchemaFile.exists()) ||					
						(null != reverseGenericArtifactToTargetRepositorySchemaFile && reverseGenericArtifactToTargetRepositorySchemaFile.exists() && !newReverseGenericArtifactToTargetRepositorySchemaFile.exists()) ||					
						(null != reverseSourceRepositorySchemaToGenericArtifactFile && reverseSourceRepositorySchemaToGenericArtifactFile.exists() && !newReverseSourceRepositorySchemaToGenericArtifactFile.exists()) ||			
						(null != reverseTargetRepositorySchemaToGenericArtifactFile && reverseTargetRepositorySchemaToGenericArtifactFile.exists() && !newReverseTargetRepositorySchemaToGenericArtifactFile.exists())) {		
						ProjectMappingRenameDialog renameDialog = new ProjectMappingRenameDialog(Display.getDefault().getActiveShell(), true, true);
						renameDialog.open();
						if (renameDialog.isRenameFiles()) {
							if (null != xslFile && xslFile.exists() && !newXslFile.exists()) {
								xslFile.renameTo(newXslFile);
							}
							if (null != graphicalXslFile && graphicalXslFile.exists() && !newGraphicalXslFile.exists()) {
								graphicalXslFile.renameTo(newGraphicalXslFile);
							}
							if (null != mfdFile && mfdFile.exists() && !newMfdFile.exists()) {
								mfdFile.renameTo(newMfdFile);
							}
							if (null != sourceRepositorySchemaFile && sourceRepositorySchemaFile.exists() && !newSourceRepositorySchemaFile.exists()) {
								sourceRepositorySchemaFile.renameTo(newSourceRepositorySchemaFile);
							}
							if (null != targetRepositorySchemaFile && targetRepositorySchemaFile.exists() && !newTargetRepositorySchemaFile.exists()) {
								targetRepositorySchemaFile.renameTo(newTargetRepositorySchemaFile);
							}
							if (null != genericArtifactToSourceRepositorySchemaFile && genericArtifactToSourceRepositorySchemaFile.exists() && !newGenericArtifactToSourceRepositorySchemaFile.exists()) {
								genericArtifactToSourceRepositorySchemaFile.renameTo(newGenericArtifactToSourceRepositorySchemaFile);
							}	
							if (null != genericArtifactToTargetRepositorySchemaFile && genericArtifactToTargetRepositorySchemaFile.exists() && !newGenericArtifactToTargetRepositorySchemaFile.exists()) {
								genericArtifactToTargetRepositorySchemaFile.renameTo(newGenericArtifactToTargetRepositorySchemaFile);
							}	
							if (null != sourceRepositorySchemaToGenericArtifactFile && sourceRepositorySchemaToGenericArtifactFile.exists() && !newSourceRepositorySchemaToGenericArtifactFile.exists()) {
								sourceRepositorySchemaToGenericArtifactFile.renameTo(newSourceRepositorySchemaToGenericArtifactFile);
							}	
							if (null != targetRepositorySchemaToGenericArtifactFile && targetRepositorySchemaToGenericArtifactFile.exists() && !newTargetRepositorySchemaToGenericArtifactFile.exists()) {
								targetRepositorySchemaToGenericArtifactFile.renameTo(newTargetRepositorySchemaToGenericArtifactFile);
							}
							// Rename reverse files
							if (null != reverseXslFile && reverseXslFile.exists() && !newReverseXslFile.exists()) {
								reverseXslFile.renameTo(newReverseXslFile);
							}
							if (null != reverseGraphicalXslFile && reverseGraphicalXslFile.exists() && !newReverseGraphicalXslFile.exists()) {
								reverseGraphicalXslFile.renameTo(newReverseGraphicalXslFile);
							}
							if (null != reverseMfdFile && reverseMfdFile.exists() && !newReverseMfdFile.exists()) {
								reverseMfdFile.renameTo(newReverseMfdFile);
							}
							if (null != reverseSourceRepositorySchemaFile && reverseSourceRepositorySchemaFile.exists() && !newReverseSourceRepositorySchemaFile.exists()) {
								reverseSourceRepositorySchemaFile.renameTo(newReverseSourceRepositorySchemaFile);
							}
							if (null != reverseTargetRepositorySchemaFile && reverseTargetRepositorySchemaFile.exists() && !newReverseTargetRepositorySchemaFile.exists()) {
								reverseTargetRepositorySchemaFile.renameTo(newReverseTargetRepositorySchemaFile);
							}
							if (null != reverseGenericArtifactToSourceRepositorySchemaFile && reverseGenericArtifactToSourceRepositorySchemaFile.exists() && !newReverseGenericArtifactToSourceRepositorySchemaFile.exists()) {
								reverseGenericArtifactToSourceRepositorySchemaFile.renameTo(newReverseGenericArtifactToSourceRepositorySchemaFile);
							}	
							if (null != reverseGenericArtifactToTargetRepositorySchemaFile && reverseGenericArtifactToTargetRepositorySchemaFile.exists() && !newReverseGenericArtifactToTargetRepositorySchemaFile.exists()) {
								reverseGenericArtifactToTargetRepositorySchemaFile.renameTo(newReverseGenericArtifactToTargetRepositorySchemaFile);
							}	
							if (null != reverseSourceRepositorySchemaToGenericArtifactFile && reverseSourceRepositorySchemaToGenericArtifactFile.exists() && !newReverseSourceRepositorySchemaToGenericArtifactFile.exists()) {
								reverseSourceRepositorySchemaToGenericArtifactFile.renameTo(newReverseSourceRepositorySchemaToGenericArtifactFile);
							}	
							if (null != reverseTargetRepositorySchemaToGenericArtifactFile && reverseTargetRepositorySchemaToGenericArtifactFile.exists() && !newReverseTargetRepositorySchemaToGenericArtifactFile.exists()) {
								reverseTargetRepositorySchemaToGenericArtifactFile.renameTo(newReverseTargetRepositorySchemaToGenericArtifactFile);
							}							
						}	
						if (renameDialog.isUpdateDatabase()) {
							BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
								public void run() {
									try {										
										updateIdentityMappings(status,
												oldSourceRepositoryId,
												oldTargetRepositoryId,
												dataProvider);
									} catch (Exception e) {
										Activator.handleDatabaseError(e, false, true, "Update Identity Mappings");
									}
									try {										
										updateHospital(status,
												oldSourceRepositoryId,
												oldTargetRepositoryId,
												dataProvider);
									} catch (Exception e) {
										Activator.handleDatabaseError(e, false, true, "Update Hospital");
									}
								}								
							});
						}
					}
				}
				mappingsChanged = true;
				
				// After delay, resume mappings that were automatically paused.
				if (dialog.needsResume() || dialog.reverseNeedsResume()) {
					int delay = Activator.getDefault().getPreferenceStore().getInt(Activator.PREFERENCES_RESET_DELAY);
					dataProvider.runAfterDelay(new Runnable() {
						public void run() {
							if (dialog.needsResume()) {
								try {
									dataProvider.resumeSynchronization(status);
								} catch (Exception e) {
									Activator.handleError(e);
								}
							}
							if (dialog.reverseNeedsResume()) {
								try {
									dataProvider.resumeSynchronization(reverseStatus);
								} catch (Exception e) {
									Activator.handleError(e);
								}
							}
							
							// Refresh view automatically after resume, being sure to do it in UI thread.						
							for (final ProjectMappings projectMappings: projectMappingsList) {
								Display.getDefault().syncExec(new Runnable() {								
									public void run() {
										Activator.notifyChanged(projectMappings);
									}
								});								
							}
						}						
					}, delay);
				}
			}
		}
		if (mappingsChanged) {
			for (ProjectMappings projectMappings: projectMappingsList) {
				Activator.notifyChanged(projectMappings);
			}
		}
	}
	
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fSelection= (IStructuredSelection) sel;
		}

// Rather than disabling action for non-paused mapping, we will automatically pause
// before mapping is changed and resume after.
		
//		if (action != null) {
//			boolean paused = false;
//			if (fSelection != null && fSelection.getFirstElement() instanceof SynchronizationStatus) {
//				paused = ((SynchronizationStatus)fSelection.getFirstElement()).isPaused();
//			}
//			action.setEnabled(Activator.getDefault().getActiveRole().isChangeProjectMapping() && paused);
//		}
	}

	private void updateIdentityMappings(final SynchronizationStatus status,
			String oldSourceRepositoryId, final String oldTargetRepositoryId, CcfDataProvider dataProvider)
			throws Exception {
		if (!oldSourceRepositoryId.equals(status.getSourceRepositoryId())) {
			Filter sourceSystemFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_SYSTEM_ID, status.getSourceSystemId(), true);
			Filter sourceRepositoryFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_ID, oldSourceRepositoryId, true);
			Filter targetSystemFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_SYSTEM_ID, status.getTargetSystemId(), true);
			Filter targetRepositoryFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID, oldTargetRepositoryId, true);
			Filter[] filters = { sourceSystemFilter, sourceRepositoryFilter, targetSystemFilter, targetRepositoryFilter };										
			Update sourceRepositoryUpdate = new Update(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_ID, status.getSourceRepositoryId());
//			Update sourceRepositoryKindUpdate = new Update(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_KIND, status.getSourceRepositoryKind());
//			Update[] updates = { sourceRepositoryUpdate, sourceRepositoryKindUpdate };
			Update[] updates = { sourceRepositoryUpdate };
			dataProvider.updateIdentityMappings(status.getLandscape(), updates, filters);
			// Update reverse
			sourceSystemFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_SYSTEM_ID, status.getTargetSystemId(), true);
			sourceRepositoryFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_ID, oldTargetRepositoryId, true);
			targetSystemFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_SYSTEM_ID, status.getSourceSystemId(), true);
			targetRepositoryFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID, oldSourceRepositoryId, true);
			Filter[] reverseFilters = { sourceSystemFilter, sourceRepositoryFilter, targetSystemFilter, targetRepositoryFilter };										
			Update targetRepositoryUpdate = new Update(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID, status.getSourceRepositoryId());
//			Update targetRepositoryKindUpdate = new Update(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_KIND, status.getSourceRepositoryKind());
//			Update[] reverseUpdates = { targetRepositoryUpdate, targetRepositoryKindUpdate };
			Update[] reverseUpdates = { targetRepositoryUpdate };
			dataProvider.updateIdentityMappings(status.getLandscape(), reverseUpdates, reverseFilters);
			oldSourceRepositoryId = status.getSourceRepositoryId();
		}
		if (!oldTargetRepositoryId.equals(status.getTargetRepositoryId())) {
			Filter sourceSystemFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_SYSTEM_ID, status.getSourceSystemId(), true);
			Filter sourceRepositoryFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_ID, oldSourceRepositoryId, true);
			Filter targetSystemFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_SYSTEM_ID, status.getTargetSystemId(), true);
			Filter targetRepositoryFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID, oldTargetRepositoryId, true);
			Filter[] filters = { sourceSystemFilter, sourceRepositoryFilter, targetSystemFilter, targetRepositoryFilter };										
			Update targetRepositoryUpdate = new Update(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID, status.getTargetRepositoryId());
//			Update targetRepositoryKindUpdate = new Update(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_KIND, status.getTargetRepositoryKind());
//			Update[] updates = { targetRepositoryUpdate, targetRepositoryKindUpdate };
			Update[] updates = { targetRepositoryUpdate };			
			dataProvider.updateIdentityMappings(status.getLandscape(), updates, filters);
			// Update reverse
			sourceSystemFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_SYSTEM_ID, status.getTargetSystemId(), true);
			sourceRepositoryFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_ID, oldTargetRepositoryId, true);
			targetSystemFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_SYSTEM_ID, status.getSourceSystemId(), true);
			targetRepositoryFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID, oldSourceRepositoryId, true);
			Filter[] reverseFilters = { sourceSystemFilter, sourceRepositoryFilter, targetSystemFilter, targetRepositoryFilter };										
			Update sourceRepositoryUpdate = new Update(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_ID, status.getTargetRepositoryId());
//			Update sourceRepositoryKindUpdate = new Update(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_KIND, status.getTargetRepositoryKind());
//			Update[] reverseUpdates = { sourceRepositoryUpdate, sourceRepositoryKindUpdate };
			Update[] reverseUpdates = { sourceRepositoryUpdate };			
			dataProvider.updateIdentityMappings(status.getLandscape(), reverseUpdates, reverseFilters);
		}
	}
	
	private void updateHospital(final SynchronizationStatus status,
			String oldSourceRepositoryId, final String oldTargetRepositoryId, CcfDataProvider dataProvider)
			throws Exception {
		if (!oldSourceRepositoryId.equals(status.getSourceRepositoryId())) {
			Filter sourceSystemFilter = new Filter(CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_ID, status.getSourceSystemId(), true);
			Filter sourceRepositoryFilter = new Filter(CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_ID, oldSourceRepositoryId, true);
			Filter targetSystemFilter = new Filter(CcfDataProvider.HOSPITAL_TARGET_SYSTEM_ID, status.getTargetSystemId(), true);
			Filter targetRepositoryFilter = new Filter(CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_ID, oldTargetRepositoryId, true);
			Filter[] filters = { sourceSystemFilter, sourceRepositoryFilter, targetSystemFilter, targetRepositoryFilter };										
			Update sourceRepositoryUpdate = new Update(CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_ID, status.getSourceRepositoryId());
//			Update sourceRepositoryKindUpdate = new Update(CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_KIND, status.getSourceRepositoryKind());
//			Update[] updates = { sourceRepositoryUpdate, sourceRepositoryKindUpdate };
			Update[] updates = { sourceRepositoryUpdate };
			dataProvider.updatePatients(status.getLandscape(), updates, filters);
			// Update reverse
			sourceSystemFilter = new Filter(CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_ID, status.getTargetSystemId(), true);
			sourceRepositoryFilter = new Filter(CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_ID, oldTargetRepositoryId, true);
			targetSystemFilter = new Filter(CcfDataProvider.HOSPITAL_TARGET_SYSTEM_ID, status.getSourceSystemId(), true);
			targetRepositoryFilter = new Filter(CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_ID, oldSourceRepositoryId, true);
			Filter[] reverseFilters = { sourceSystemFilter, sourceRepositoryFilter, targetSystemFilter, targetRepositoryFilter };										
			Update targetRepositoryUpdate = new Update(CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_ID, status.getSourceRepositoryId());
//			Update targetRepositoryKindUpdate = new Update(CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_KIND, status.getSourceRepositoryKind());
//			Update[] reverseUpdates = { targetRepositoryUpdate, targetRepositoryKindUpdate };
			Update[] reverseUpdates = { targetRepositoryUpdate };			
			dataProvider.updatePatients(status.getLandscape(), reverseUpdates, reverseFilters);
			oldSourceRepositoryId = status.getSourceRepositoryId();
		}
		if (!oldTargetRepositoryId.equals(status.getTargetRepositoryId())) {
			Filter sourceSystemFilter = new Filter(CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_ID, status.getSourceSystemId(), true);
			Filter sourceRepositoryFilter = new Filter(CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_ID, oldSourceRepositoryId, true);
			Filter targetSystemFilter = new Filter(CcfDataProvider.HOSPITAL_TARGET_SYSTEM_ID, status.getTargetSystemId(), true);
			Filter targetRepositoryFilter = new Filter(CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_ID, oldTargetRepositoryId, true);
			Filter[] filters = { sourceSystemFilter, sourceRepositoryFilter, targetSystemFilter, targetRepositoryFilter };										
			Update targetRepositoryUpdate = new Update(CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_ID, status.getTargetRepositoryId());
//			Update targetRepositoryKindUpdate = new Update(CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_KIND, status.getTargetRepositoryKind());
//			Update[] updates = { targetRepositoryUpdate, targetRepositoryKindUpdate };
			Update[] updates = { targetRepositoryUpdate };
			dataProvider.updatePatients(status.getLandscape(), updates, filters);
			// Update reverse
			sourceSystemFilter = new Filter(CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_ID, status.getTargetSystemId(), true);
			sourceRepositoryFilter = new Filter(CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_ID, oldTargetRepositoryId, true);
			targetSystemFilter = new Filter(CcfDataProvider.HOSPITAL_TARGET_SYSTEM_ID, status.getSourceSystemId(), true);
			targetRepositoryFilter = new Filter(CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_ID, oldSourceRepositoryId, true);
			Filter[] reverseFilters = { sourceSystemFilter, sourceRepositoryFilter, targetSystemFilter, targetRepositoryFilter };										
//			Update sourceRepositoryUpdate = new Update(CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_ID, status.getTargetRepositoryId());
//			Update sourceRepositoryKindUpdate = new Update(CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_KIND, status.getTargetRepositoryKind());
			Update sourceRepositoryUpdate = new Update(CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_ID, status.getTargetRepositoryId());
			Update[] reverseUpdates = { sourceRepositoryUpdate };
			dataProvider.updatePatients(status.getLandscape(), reverseUpdates, reverseFilters);
		}
	}	
	
}
