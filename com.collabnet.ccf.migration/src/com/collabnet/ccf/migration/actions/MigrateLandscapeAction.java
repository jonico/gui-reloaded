/*******************************************************************************
 * Copyright (c) 2011 CollabNet.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     CollabNet - initial API and implementation
 ******************************************************************************/
package com.collabnet.ccf.migration.actions;

import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.migration.wizards.MigrateLandscapeWizard;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.wizards.CustomWizardDialog;

public class MigrateLandscapeAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	
	@Override
	public void run(IAction action) {
		@SuppressWarnings("rawtypes")
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof Landscape) {
				Landscape landscape = (Landscape)object;
				MigrateLandscapeWizard wizard = new MigrateLandscapeWizard(landscape);
				WizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), wizard);
				dialog.open();
				break;
			}
		}
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fSelection= (IStructuredSelection) sel;
		}
		if (null != action) {
			boolean enabled = false;
			if (null != fSelection && fSelection.getFirstElement() instanceof Landscape) {
				Landscape landscape = (Landscape)fSelection.getFirstElement();
				enabled = landscape.getType1().equals("TF") || landscape.getType2().equals("TF");
			}
			action.setEnabled(enabled);
		}
	}	
}
