package com.collabnet.ccf.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

import com.collabnet.ccf.Activator;

public class HospitalEditor extends FormEditor {
	
	private HospitalExceptionEditorPage exceptionPage;
	private HospitalDetailsEditorPage detailsPage;
	
	private IDialogSettings settings = Activator.getDefault().getDialogSettings();
	
	public final static String ID = "com.collabnet.ccf.editors.HospitalEditor";
	
	public HospitalEditor() {
		super();
	}
	
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        setSite(site);
        setInput(input);
        setPartName(input.getName());
        setTitleImage(Activator.getImage(Activator.IMAGE_HOSPITAL_ENTRY));
    }

	@Override
	protected void addPages() {
		createExceptionPage();
		createDetailsPage();
		try {
			String activePage = settings.get(HospitalEditorPage.ACTIVE_PAGE);
			if (null != activePage) {
				setActivePage(activePage);	
			}
		} catch (Exception e) {
			Activator.handleError(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void createExceptionPage() {
        try {
        	exceptionPage = new HospitalExceptionEditorPage(this, "exception", getEditorInput().getName());
	        int index = addPage(exceptionPage);
	        setPageText(index, "Exception Details");
	        pages.add(exceptionPage);
        } catch (Exception e) { 
        	Activator.handleError(e);
        }		
	}
	
	@SuppressWarnings("unchecked")
	private void createDetailsPage() {
        try {
        	detailsPage = new HospitalDetailsEditorPage(this, "details", getEditorInput().getName());
	        int index = addPage(detailsPage);
	        setPageText(index, "Hospital Details");
	        pages.add(detailsPage);
        } catch (Exception e) { 
        	Activator.handleError(e);
        }		
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {
		
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	public IDialogSettings getDialogSettings() {
		return settings;
	}

}
