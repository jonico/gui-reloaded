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
import com.collabnet.ccf.model.Patient;
import com.collabnet.ccf.views.HospitalView;

public class DeletePatientAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	private boolean patientsDeleted;
	
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		if (!MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Delete Hospital Item", "Delete the selected hospital items?")) return;
		patientsDeleted = false;
		final CcfDataProvider dataProvider = new CcfDataProvider();
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				Iterator iter = fSelection.iterator();
				while (iter.hasNext()) {
					Object object = iter.next();
					if (object instanceof Patient patient) {
						Filter filter = new Filter(CcfDataProvider.HOSPITAL_ID, Integer.toString(patient.getId()), false);
						Filter[] filters = { filter };
						try {
							dataProvider.deletePatients(patient.getLandscape(), filters);
							patientsDeleted = true;
						} catch (Exception e) {
							Activator.handleError(e);
							break;
						}
					}
				}
			}			
		});
		if (patientsDeleted && HospitalView.getView() != null) {
			HospitalView.getView().refresh();
		}
	}	
	
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection selection) {
			fSelection= selection;
		}
		if (action != null) {
			action.setEnabled(Activator.getDefault().getActiveRole().isDeleteHospitalEntry());
		}
	}	

}
