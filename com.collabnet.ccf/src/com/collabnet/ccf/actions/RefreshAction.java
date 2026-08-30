package com.collabnet.ccf.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.views.CcfExplorerView;

public class RefreshAction extends ActionDelegate {

	@Override
	public void run(IAction action) {
		if (null != CcfExplorerView.getView()) {
			CcfExplorerView.getView().refreshViewerNode();
		}
	}

}
