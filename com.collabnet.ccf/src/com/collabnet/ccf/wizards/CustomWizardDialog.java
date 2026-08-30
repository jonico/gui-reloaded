package com.collabnet.ccf.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import com.collabnet.ccf.Activator;

public class CustomWizardDialog extends WizardDialog {
	private IDialogSettings settings;
	private String dialogId;
	
	public CustomWizardDialog(Shell parentShell, IWizard wizard) {
		super(parentShell, wizard);
		settings = Activator.getDefault().getDialogSettings();	
	}
	
	public CustomWizardDialog(Shell parentShell, IWizard wizard, String dialogId) {
		this(parentShell, wizard);
		this.dialogId = dialogId;
	}
	
    protected void cancelPressed() {
        saveLocation();
        super.cancelPressed();
    }

    protected void okPressed() {
        saveLocation();
        super.okPressed();
    }

	@Override
	public boolean isHelpAvailable() {
		return false;
	}

	protected Point getInitialLocation(Point initialSize) {
	    try {
	        int x = settings.getInt(getDialogKey() + ".location.x"); //$NON-NLS-1$
	        int y = settings.getInt(getDialogKey() + ".location.y"); //$NON-NLS-1$
	        return new Point(x, y);
	    } catch (NumberFormatException e) {}
	    return super.getInitialLocation(initialSize);
	}
	
	protected Point getInitialSize() {
	    try {
	        int x = settings.getInt(getDialogKey() + ".size.x"); //$NON-NLS-1$
	        int y = settings.getInt(getDialogKey() + ".size.y"); //$NON-NLS-1$
	        return new Point(x, y);
	    } catch (NumberFormatException e) {}		
		 return super.getInitialSize();
	}
	
	protected void saveLocation() {
        int x = getShell().getLocation().x;
        int y = getShell().getLocation().y;
        settings.put(getDialogKey() + ".location.x", x); //$NON-NLS-1$
        settings.put(getDialogKey() + ".location.y", y); //$NON-NLS-1$  
        x = getShell().getSize().x;
        y = getShell().getSize().y; 
        settings.put(getDialogKey() + ".size.x", x); //$NON-NLS-1$
        settings.put(getDialogKey() + ".size.y", y); //$NON-NLS-1$    
	}
	
	private String getDialogKey() {
		if (null == dialogId) {
			return getWizard().getClass().getName();
		} else {
			return getWizard().getClass().getName() + "." + dialogId;
		}
	}
}
