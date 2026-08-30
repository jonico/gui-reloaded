package com.collabnet.ccf.actions;

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
import com.collabnet.ccf.dialogs.DeleteIdentityMappingDialog;
import com.collabnet.ccf.model.IdentityMapping;
import com.collabnet.ccf.model.IdentityMappingConsistencyCheck;
import com.collabnet.ccf.model.InconsistentIdentityMapping;
import com.collabnet.ccf.views.IdentityMappingConsistencyCheckView;
import com.collabnet.ccf.views.IdentityMappingView;

public class DeleteIdentityMappingAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	private boolean identityMappingsDeleted;
	
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		final List<IdentityMappingConsistencyCheck> consistencyChecks = new ArrayList<IdentityMappingConsistencyCheck>();
		DeleteIdentityMappingDialog dialog = new DeleteIdentityMappingDialog(Display.getDefault().getActiveShell());
		if (dialog.open() == DeleteIdentityMappingDialog.CANCEL) return;
		final boolean deleteReverse = dialog.isDeleteReverse();
		identityMappingsDeleted = false;
		final CcfDataProvider dataProvider = new CcfDataProvider();
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				Iterator iter = fSelection.iterator();
				while (iter.hasNext()) {
					Object object = iter.next();
					if (object instanceof IdentityMapping identityMapping) {
						Filter sourceRepositoryIdFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_ID, identityMapping.getSourceRepositoryId(), true);
						Filter targetRepositoryIdFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID, identityMapping.getTargetRepositoryId(), true);
						Filter sourceArtifactIdFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_ARTIFACT_ID, identityMapping.getSourceArtifactId(), true);
						Filter artifactTypeFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_ARTIFACT_TYPE, identityMapping.getArtifactType(), true);
						Filter[] filters = { sourceRepositoryIdFilter, targetRepositoryIdFilter, sourceArtifactIdFilter, artifactTypeFilter };
						Filter[] reverseFilters = null;
						if (deleteReverse) {
							Filter reverseSourceRepositoryIdFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID, identityMapping.getSourceRepositoryId(), true);
							Filter reverseTargetRepositoryIdFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_ID, identityMapping.getTargetRepositoryId(), true);
							Filter reverseSourceArtifactIdFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_ARTIFACT_ID, identityMapping.getSourceArtifactId(), true);
							Filter[] reverse = { reverseSourceRepositoryIdFilter, reverseTargetRepositoryIdFilter, reverseSourceArtifactIdFilter, artifactTypeFilter };							
							reverseFilters = reverse;
						}
						try {
							dataProvider.deleteIdentityMappings(identityMapping.getLandscape(), filters);
							if (identityMapping instanceof InconsistentIdentityMapping mapping) {
								if (!consistencyChecks.contains(mapping.getConsistencyCheck())) {
									consistencyChecks.add(mapping.getConsistencyCheck());
								}
							}
							if (reverseFilters != null) {
								dataProvider.deleteIdentityMappings(identityMapping.getLandscape(), reverseFilters);
							}
							identityMappingsDeleted = true;
						} catch (Exception e) {
							Activator.handleError(e);
							break;
						}
					}
				}
			}			
		});
		if (identityMappingsDeleted && IdentityMappingView.getView() != null) {
			IdentityMappingView.getView().refresh();
		}
		if (IdentityMappingConsistencyCheckView.getView() != null) {
			for (IdentityMappingConsistencyCheck consistencyCheck : consistencyChecks) {
				IdentityMappingConsistencyCheckView.getView().refresh(consistencyCheck);
			}
		}
	}	
	
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection selection) {
			fSelection= selection;
		}
		if (action != null) {
			action.setEnabled(Activator.getDefault().getActiveRole().isDeleteIdentityMapping());
		}
	}	

}
