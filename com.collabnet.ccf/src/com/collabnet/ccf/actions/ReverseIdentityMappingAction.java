package com.collabnet.ccf.actions;

import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.db.Filter;
import com.collabnet.ccf.model.IdentityMapping;
import com.collabnet.ccf.views.IdentityMappingView;

public class ReverseIdentityMappingAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof IdentityMapping identityMapping) {
				try {
					IdentityMappingView.setLandscape(identityMapping.getLandscape());
					Filter sourceRepositoryFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID, identityMapping.getSourceRepositoryId(), true);
					Filter sourceSystemFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_SYSTEM_ID, identityMapping.getSourceSystemId(), true);					
					Filter sourceArtifactFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_ARTIFACT_ID, identityMapping.getSourceArtifactId(), true);
					Filter[] filters = { sourceArtifactFilter, sourceRepositoryFilter, sourceSystemFilter };					
					Filter[][] filterGroups = { filters };
					String description = "Target = " + identityMapping.getSourceArtifactId() + " (" + identityMapping.getSourceRepositoryId() + ")";
					IdentityMappingView.setFilters(filterGroups, true, description);
					IdentityMappingView identityMappingView = (IdentityMappingView)Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(IdentityMappingView.ID);
					identityMappingView.refresh();
				} catch (PartInitException e) {
					Activator.handleError(e);
				}
				break;
			}
		}
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection selection) {
			fSelection= selection;
		}
	}	
}
