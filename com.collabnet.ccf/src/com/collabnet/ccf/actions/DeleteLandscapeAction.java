package com.collabnet.ccf.actions;

import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionDelegate;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.views.CcfExplorerView;

public class DeleteLandscapeAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	private boolean landscapeDeleted;
	
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		if (!MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Delete Landscape", "Delete the selected landscapes?")) return;
		landscapeDeleted = false;
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof Landscape) {
				Landscape landscape = (Landscape)object;
				Preferences node = landscape.getNode();
				if (null != node) {
					try {
						node.removeNode();
						landscapeDeleted = true;
					} catch (BackingStoreException e) {
						Activator.handleError(e);
					}
				}
			}
		}
		if (landscapeDeleted && null != CcfExplorerView.getView()) {
			CcfExplorerView.getView().refresh();
		}
	}
	
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fSelection= (IStructuredSelection) sel;
		}
		if (null != action) {
			action.setEnabled(Activator.getDefault().getActiveRole().isDeleteLandscape());
		}
	}	

}
