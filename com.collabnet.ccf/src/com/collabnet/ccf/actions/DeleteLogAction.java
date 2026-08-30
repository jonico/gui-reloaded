package com.collabnet.ccf.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.model.Log;
import com.collabnet.ccf.model.Logs;
import com.collabnet.ccf.views.CcfExplorerView;

public class DeleteLogAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		List<Log> logList = new ArrayList<Log>();
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof Log) {
				logList.add((Log)object);
			}
		}
		String message = null;
		if (1 == logList.size()) {
			Log log = logList.get(0);
			message = "Delete " + log + "?";
		} else {
			message = "Delete the " + logList.size() + " selected logs?";
		}
		if (!MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Delete Log", message)) return;
		List<Logs> parents = new ArrayList<Logs>();
		iter = logList.iterator();
		while (iter.hasNext()) {
			Log log = (Log)iter.next();
			log.getLogFile().delete();
			if (!parents.contains(log.getLogs())) parents.add(log.getLogs());
		}
		if (null != CcfExplorerView.getView()) {
			for (Logs logs : parents) {
				CcfExplorerView.getView().refresh(logs);
			}
		}
	}
	
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fSelection= (IStructuredSelection) sel;
		}
	}	

}
