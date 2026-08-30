package com.collabnet.ccf.actions;

import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.db.Filter;
import com.collabnet.ccf.db.Update;
import com.collabnet.ccf.model.Patient;
import com.collabnet.ccf.views.CcfExplorerView;
import com.collabnet.ccf.views.HospitalView;

public class MarkFixedAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	private boolean patientsUpdated;
	
	private boolean undo = false;

	public void setUndo(boolean undo) {
		this.undo = undo;
	}

	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		String title;
		String updateValue;
		String message;
		if (undo) {
			title = "Reopen Hospital Items";
			message = "Reopen the selected hospital items?";
			updateValue = "false";
		} else {
			title = "Mark Hospital Items Fixed";
			message = "Mark the selected hospital items as fixed?";
			updateValue = "true";
		}
		if (!MessageDialog.openConfirm(Display.getDefault().getActiveShell(), title, message)) return;
		patientsUpdated = false;
		final CcfDataProvider dataProvider = new CcfDataProvider();
		Update update = new Update(CcfDataProvider.HOSPITAL_FIXED, updateValue, false);
		final Update[] updates = { update };
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				Iterator iter = fSelection.iterator();
				while (iter.hasNext()) {
					Object object = iter.next();
					if (object instanceof Patient patient) {
						Filter filter = new Filter(CcfDataProvider.HOSPITAL_ID, Integer.toString(patient.getId()), false);
						Filter[] filters = { filter };
						try {
							dataProvider.updatePatients(patient.getLandscape(), updates, filters);
							patientsUpdated = true;
						} catch (Exception e) {
							Activator.handleError(e);
							break;
						}
					}
				}
			}			
		});
		if (patientsUpdated && HospitalView.getView() != null) {
			HospitalView.getView().refresh();
		}
		if (patientsUpdated && CcfExplorerView.getView() != null && Activator.getDefault().getPreferenceStore().getBoolean(Activator.PREFERENCES_SHOW_HOSPITAL_COUNT)) {
			CcfExplorerView.getView().refreshProjectMappings();
		}
	}	
	
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection selection) {
			fSelection= selection;
			if (action != null) action.setEnabled(isEnabledForSelection());
		}
	}	
	
	@SuppressWarnings("unchecked")
	private boolean isEnabledForSelection() {
		if (fSelection == null) return false;
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof Patient patient) {
				if (patient.isFixed() != undo) return false;
				if (undo && !Activator.getDefault().getActiveRole().isReopen()) return false;
				if (!undo && !Activator.getDefault().getActiveRole().isMarkAsFixed()) return false;
			}
		}
		return true;
	}	
	
}
