package com.collabnet.ccf.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import com.collabnet.ccf.Activator;

public class CcfDialog extends TrayDialog {
	private String id;
	private IDialogSettings settings = Activator.getDefault().getDialogSettings();

	public CcfDialog(Shell shell, String id) {
		super(shell);
		this.id = id;
		int shellStyle = getShellStyle();
		setShellStyle(shellStyle | SWT.RESIZE);
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

	public IDialogSettings getSettings() {
		return settings;
	}
	
	protected Point getInitialLocation(Point initialSize) {
		if (null != id) {
		    try {
		        int x = settings.getInt(id + ".location.x"); //$NON-NLS-1$
		        int y = settings.getInt(id + ".location.y"); //$NON-NLS-1$
		        return new Point(x, y);
		    } catch (NumberFormatException e) {}
		}
        return super.getInitialLocation(initialSize);
    }
    
    protected Point getInitialSize() {
    	if (null != id) {
		    try {
		        int x = settings.getInt(id + ".size.x"); //$NON-NLS-1$
		        int y = settings.getInt(id + ".size.y"); //$NON-NLS-1$
		        return new Point(x, y);
		    } catch (NumberFormatException e) {}
    	}
        return super.getInitialSize();
    }	

	protected void saveLocation() {
    	if (null != id) {
	        int x = getShell().getLocation().x;
	        int y = getShell().getLocation().y;
	        settings.put(id + ".location.x", x); //$NON-NLS-1$
	        settings.put(id + ".location.y", y); //$NON-NLS-1$
	        x = getShell().getSize().x;
	        y = getShell().getSize().y;
	        settings.put(id + ".size.x", x); //$NON-NLS-1$
	        settings.put(id + ".size.y", y); //$NON-NLS-1$ 
    	}
    }

}
