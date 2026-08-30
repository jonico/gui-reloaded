package com.collabnet.ccf.dialogs;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ExceptionDetailsErrorDialog extends IconAndMessageDialog {
    public static boolean AUTOMATED_MODE = false;
    private static final String NESTING_INDENT = "  "; //$NON-NLS-1$
    private Button detailsButton;
    private String title;
    private Text text;
    private boolean listCreated = false;
    private int displayMask = 0xFFFF;
    private IStatus status;
    private Clipboard clipboard;

	private boolean shouldIncludeTopLevelErrorInDetails = false;

    public ExceptionDetailsErrorDialog(Shell parentShell, String dialogTitle, String message, IStatus status, int displayMask) {
        super(parentShell);
        this.title = dialogTitle == null ? JFaceResources
                .getString("Problem_Occurred") : //$NON-NLS-1$
                dialogTitle;
        this.message = message == null ? status.getMessage()
                : JFaceResources
                        .format(
                                "Reason", new Object[] { message, status.getMessage() }); //$NON-NLS-1$
        this.status = status;
        this.displayMask = displayMask;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    protected void buttonPressed(int id) {
        if (id == IDialogConstants.DETAILS_ID) {
            toggleDetailsArea();
        } else {
            super.buttonPressed(id);
        }
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
    }

    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createDetailsButton(parent);
    }

	protected void createDetailsButton(Composite parent) {
		if (shouldShowDetailsButton()) {
            detailsButton = createButton(parent, IDialogConstants.DETAILS_ID,
                    IDialogConstants.SHOW_DETAILS_LABEL, false);
        }
	}

    protected Control createDialogArea(Composite parent) {
        createMessageArea(parent);
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        layout.numColumns = 2;
        composite.setLayout(layout);
        GridData childData = new GridData(GridData.FILL_BOTH);
        childData.horizontalSpan = 2;
        composite.setLayoutData(childData);
        composite.setFont(parent.getFont());
        return composite;
    }

    protected void createDialogAndButtonArea(Composite parent) {
        super.createDialogAndButtonArea(parent);
        if (this.dialogArea instanceof Composite) {
            Composite dialogComposite = (Composite) dialogArea;
            if (dialogComposite.getChildren().length == 0) {
				new Label(dialogComposite, SWT.NULL);
			}
        }
    }

    protected Image getImage() {
        if (status != null) {
            if (status.getSeverity() == IStatus.WARNING) {
				return getWarningImage();
			}
            if (status.getSeverity() == IStatus.INFO) {
				return getInfoImage();
			}
        }
        return getErrorImage();
    }

    protected Text createDropDownList(Composite parent) {
        text = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
        populateList(text);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL
                | GridData.GRAB_VERTICAL);
        data.horizontalSpan = 2;
        data.heightHint = 300;
        text.setLayoutData(data);
        text.setFont(parent.getFont());
        Menu copyMenu = new Menu(text);
        MenuItem copyItem = new MenuItem(copyMenu, SWT.NONE);
        copyItem.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                copyToClipboard();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                copyToClipboard();
            }
        });
        copyItem.setText(JFaceResources.getString("copy")); //$NON-NLS-1$
        text.setMenu(copyMenu);
        listCreated = true;
        return text;
    }

    public int open() {
        if (!AUTOMATED_MODE && shouldDisplay(status, displayMask)) {
            return super.open();
        }
        setReturnCode(OK);
        return OK;
    }

    public static int openError(Shell parent, String dialogTitle,
            String message, IStatus status) {
        return openError(parent, dialogTitle, message, status, IStatus.OK
                | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
    }

    public static int openError(Shell parentShell, String title,
            String message, IStatus status, int displayMask) {
        ExceptionDetailsErrorDialog dialog = new ExceptionDetailsErrorDialog(parentShell, title, message,
                status, displayMask);
        return dialog.open();
    }

    private void populateList(Text listToPopulate) {
        populateList(listToPopulate, status, 0, shouldIncludeTopLevelErrorInDetails);
    }

    private void populateList(Text listToPopulate, IStatus buildingStatus,
            int nesting, boolean includeStatus) {
        
        if (!buildingStatus.matches(displayMask)) {
            return;
        }

        Throwable t = buildingStatus.getException();
        boolean isCoreException= t instanceof CoreException;
        boolean incrementNesting= false;
        
       	if (includeStatus) {
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < nesting; i++) {
	            sb.append(NESTING_INDENT);
	        }
	        String message = buildingStatus.getMessage();
            sb.append(message);
	        listToPopulate.append(sb.toString());
	        incrementNesting= true;
       	}
        	
        if (!isCoreException && t != null) {
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < nesting; i++) {
	            sb.append(NESTING_INDENT);
	        }
	        
	        StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw );
			
	        String message = sw.getBuffer().toString();
	        if (message == null) {
				message = t.toString();
			}
	        	
	        sb.append(message);
	        listToPopulate.append(sb.toString());
	        incrementNesting= true;
        }
        
        if (incrementNesting) {
			nesting++;
		}
 
        if (isCoreException) {
            CoreException ce = (CoreException)t;
            IStatus eStatus = ce.getStatus();
            if (message == null || message.indexOf(eStatus.getMessage()) == -1) {
                populateList(listToPopulate, eStatus, nesting, true);
            }
        }

        IStatus[] children = buildingStatus.getChildren();
        for (int i = 0; i < children.length; i++) {
            populateList(listToPopulate, children[i], nesting, true);
        }
    }

    protected static boolean shouldDisplay(IStatus status, int mask) {
        IStatus[] children = status.getChildren();
        if (children == null || children.length == 0) {
            return status.matches(mask);
        }
        for (int i = 0; i < children.length; i++) {
            if (children[i].matches(mask)) {
				return true;
			}
        }
        return false;
    }

    private void toggleDetailsArea() {
        Point windowSize = getShell().getSize();
        Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (listCreated) {
            text.dispose();
            listCreated = false;
            detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
        } else {
            text = createDropDownList((Composite) getContents());
            detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
        }
        Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        getShell()
                .setSize(
                        new Point(windowSize.x, windowSize.y
                                + (newSize.y - oldSize.y)));
    }

    private void populateCopyBuffer(IStatus buildingStatus,
            StringBuffer buffer, int nesting) {
        if (!buildingStatus.matches(displayMask)) {
            return;
        }
        for (int i = 0; i < nesting; i++) {
            buffer.append(NESTING_INDENT);
        }
        buffer.append(buildingStatus.getMessage());
        buffer.append("\n"); //$NON-NLS-1$
        
        Throwable t = buildingStatus.getException();
        if (t instanceof CoreException ce) {
            populateCopyBuffer(ce.getStatus(), buffer, nesting + 1);
        }
        
        IStatus[] children = buildingStatus.getChildren();
        for (int i = 0; i < children.length; i++) {
            populateCopyBuffer(children[i], buffer, nesting + 1);
        }
    }

    private void copyToClipboard() {
        if (clipboard != null) {
			clipboard.dispose();
		}
        StringBuffer statusBuffer = new StringBuffer();
        populateCopyBuffer(status, statusBuffer, 0);
        clipboard = new Clipboard(text.getDisplay());
        clipboard.setContents(new Object[] { statusBuffer.toString() },
                new TextTransfer[] { TextTransfer.getInstance() });
    }

    public boolean close() {
        if (clipboard != null) {
			clipboard.dispose();
		}
        return super.close();
    }
 
    protected final void showDetailsArea() {
        if (!listCreated) {
            Control control = getContents();
            if (control != null && ! control.isDisposed()) {
				toggleDetailsArea();
			}
        }
    }
 
    protected boolean shouldShowDetailsButton() {
        return status.isMultiStatus() || status.getException() != null;
    }
 
    protected final void setStatus(IStatus status) {
        if (this.status != status) {
	        this.status = status;
        }
        shouldIncludeTopLevelErrorInDetails = true;
        if (listCreated) {
            repopulateList();
        }
    }
 
    private void repopulateList() {
        if (text != null && !text.isDisposed()) {
	        text.setText("");
	        populateList(text);
        }
    }
}
