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
import com.collabnet.ccf.model.IdentityMappingConsistencyCheck;
import com.collabnet.ccf.model.InconsistentIdentityMapping;
import com.collabnet.ccf.views.IdentityMappingConsistencyCheckView;

public class CreateReverseIdentityMappingAction extends ActionDelegate {
	private IStructuredSelection fSelection;

	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		final Iterator iter = fSelection.iterator();
		final CcfDataProvider dataProvider = new CcfDataProvider();
		final List<IdentityMappingConsistencyCheck> consistencyChecks = new ArrayList<IdentityMappingConsistencyCheck>();
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				while (iter.hasNext()) {
					Object object = iter.next();
					if (object instanceof InconsistentIdentityMapping) {
						InconsistentIdentityMapping identityMapping = (InconsistentIdentityMapping)object;
						try {
							dataProvider.createReverseIdentityMapping(identityMapping.getLandscape(), identityMapping);
							if (!consistencyChecks.contains(identityMapping.getConsistencyCheck())) {
								consistencyChecks.add(identityMapping.getConsistencyCheck());
							}
						} catch (Exception e) {
							Activator.handleError(e);
							break;
						}
					}
				}
				for (IdentityMappingConsistencyCheck consistencyCheck : consistencyChecks) {
					IdentityMappingConsistencyCheckView.getView().refresh(consistencyCheck);
				}
			}			
		});
	}
	
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fSelection= (IStructuredSelection) sel;
			if (null != action) action.setEnabled(isEnabledForSelection());
		}
	}	
	
	@SuppressWarnings("unchecked")
	private boolean isEnabledForSelection() {
		if (null == fSelection || !Activator.getDefault().getActiveRole().isCreateReverseIdentityMapping()) return false;
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof InconsistentIdentityMapping) {
				InconsistentIdentityMapping identityMapping = (InconsistentIdentityMapping)object;
				if (InconsistentIdentityMapping.ONE_WAY != identityMapping.getType()) return false;
			}
		}
		return true;
	}
}
