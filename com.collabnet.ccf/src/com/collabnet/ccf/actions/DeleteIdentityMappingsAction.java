package com.collabnet.ccf.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.model.SynchronizationStatus;

public class DeleteIdentityMappingsAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		final List<SynchronizationStatus> statuses = new ArrayList<SynchronizationStatus>();
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof SynchronizationStatus) {
				statuses.add((SynchronizationStatus)object);
			}
		}

		String message;
		String warningMessage = "WARNING:  This operation cannot be undone!";
		if (1 == statuses.size()) {
			message = "Delete identity mappings for " + statuses.get(0) + " project mapping?\n\n" + warningMessage;
		} else {
			message = "Delete identity mappings for the " + statuses.size() + " selected project mappings?\n\n" + warningMessage;
		}
		
		if (!MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "Delete Identity Mappings", message)) {
			return;
		}
		
		final CcfDataProvider dataProvider = new CcfDataProvider();
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				Iterator<SynchronizationStatus> iter = statuses.iterator();
				while (iter.hasNext()) {
					SynchronizationStatus status = iter.next();
					try {
						dataProvider.deleteIdentityMappings(status);
					} catch (Exception e) {
						Activator.handleDatabaseError(e, false, true, "Delete Identity Mappings");
						break;
					}
				}
			}			
		});
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fSelection= (IStructuredSelection) sel;
		}
		if (null != action) {
			action.setEnabled(Activator.getDefault().getActiveRole().isDeleteProjectMappingIdentityMappings());
		}
	}	
}
