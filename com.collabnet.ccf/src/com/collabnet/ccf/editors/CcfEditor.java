package com.collabnet.ccf.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.EditorPart;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.CCFJMXMonitorBean;
import com.collabnet.ccf.ICcfParticipant;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.views.CcfExplorerView;

public class CcfEditor extends FormEditor implements ISaveablePart2 {
	private Landscape landscape;
	
	private CCFJMXMonitorBean monitor1;
	private CCFJMXMonitorBean monitor2;
	
	private CcfCcfEditorPage ccfPage;
	private CcfEditorPage page1;
	private CcfEditorPage page2;
	private CcfProjectMappingsEditorPage mappingsPage;

	private IDialogSettings settings = Activator.getDefault().getDialogSettings();
	
	public final static String ID = "com.collabnet.ccf.editors.CcfEditor";

	public CcfEditor() {
		super();	
	}
	
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        setSite(site);
        setInput(input);
        setPartName(input.getName());
		CcfEditorInput ccfEditorInput = (CcfEditorInput)getEditorInput();
		landscape = ccfEditorInput.getLandscape();	
		if (null != landscape && Landscape.ROLE_ADMINISTRATOR == landscape.getRole()) {
			getMonitors();
		}
        setTitleImage(Activator.getImage(landscape));
    }

	private void getMonitors() {
		monitor1 = new CCFJMXMonitorBean();
		monitor1.setHostName(landscape.getHostName1());
		monitor1.setRmiPort(landscape.getType1(), landscape.getType2());
		
		monitor2 = new CCFJMXMonitorBean();
		monitor2.setHostName(landscape.getHostName2());
		monitor2.setRmiPort(landscape.getType2(), landscape.getType1());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addPages() {
		createCcfPage();
		if (Landscape.ROLE_ADMINISTRATOR == landscape.getRole()) {
			try {
				ICcfParticipant ccfParticipant1 = Activator.getCcfParticipantForType(landscape.getType1());
				page1 = ccfParticipant1.getEditorPage1(this, getEditorInput().getName());
				if (null != page1) {
			        int index1 = addPage(page1);
			        setPageText(index1, ccfParticipant1.getName() + " Properties");
			        pages.add(page1);
				}
				ICcfParticipant ccfParticipant2 = Activator.getCcfParticipantForType(landscape.getType2());
				page2 = ccfParticipant2.getEditorPage2(this, getEditorInput().getName());
				if (null != page2) {
			        int index2 = addPage(page2);
			        setPageText(index2, ccfParticipant2.getName() + " Properties");
			        pages.add(page2);
				}				
			} catch (Exception e) {
				Activator.handleError(e);
			}
		}
		createMappingsPage();
		try {
			String activePage = settings.get(CcfEditorPage.ACTIVE_PAGE);
			if (null != activePage) {
				setActivePage(activePage);	
			}
		} catch (Exception e) {}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (!ccfPage.testConnection(true)) {
			return;
		}
		
		ccfPage.doSave(monitor);
		
		String oldTimezone1 = landscape.getTimezone1();
		String oldTimezone2 = landscape.getTimezone2();
		
		if (null != page1) {
			page1.doSave(monitor);
		}
		
		if (null != page2) {
			page2.doSave(monitor);
		}
		
		boolean timezoneChanged = 
			(oldTimezone1 == null && !(landscape.getTimezone1() == null)) ||
			(oldTimezone2 == null && !(landscape.getTimezone2() == null)) ||
			(oldTimezone1 != null && !oldTimezone1.equals(landscape.getTimezone1())) ||
			(oldTimezone2 != null && !oldTimezone2.equals(landscape.getTimezone2()));

		if (timezoneChanged) {
			CcfDataProvider dataProvider = new CcfDataProvider();
			try {
				SynchronizationStatus[] projectMappings = dataProvider.getSynchronizationStatuses(landscape, null);
				for (SynchronizationStatus projectMapping : projectMappings) {
					if (projectMapping.getSourceSystemKind().startsWith(landscape.getType1())) {
						projectMapping.setSourceSystemTimezone(landscape.getTimezone1());
						projectMapping.setTargetSystemTimezone(landscape.getTimezone2());
					} else {
						projectMapping.setSourceSystemTimezone(landscape.getTimezone2());
						projectMapping.setTargetSystemTimezone(landscape.getTimezone1());
					}
					dataProvider.updateTimezones(projectMapping);
				}
			} catch (Exception e) {
				Activator.handleError(e);
			}
		}
		
		setDirty();
		
		if (null != CcfExplorerView.getView()) {
			CcfExplorerView.getView().refresh();
		}
		
		if ((null != monitor1 && monitor1.isAlive()) || (null != monitor2 && monitor2.isAlive())) {
			if (MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "Landscape","Changes will take effect when CCF is restarted.\n\nDo you wish to restart now?")) {
				if (null != monitor1 && monitor1.isAlive()) {
					monitor1.restartCCFInstance();
				}
				if (null != monitor2 && monitor2.isAlive()) {
					monitor2.restartCCFInstance();
				}
			}
		}
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
    @SuppressWarnings("unchecked")
	private void createCcfPage() {
        try {
        	ccfPage = new CcfCcfEditorPage(this, "ccf", getEditorInput().getName());
	        int ccfIndex = addPage(ccfPage);
	        setPageText(ccfIndex, "CCF Properties");
	        pages.add(ccfPage);
        } catch (Exception e) { 
        	Activator.handleError(e);
        }
    }

    @SuppressWarnings("unchecked")
	private void createMappingsPage() {
        try {
        	mappingsPage = new CcfProjectMappingsEditorPage(this, "projectMappings", getEditorInput().getName());
	        int mappingsIndex = addPage(mappingsPage);
	        setPageText(mappingsIndex, "Project Mappings");
	        pages.add(mappingsPage);
        } catch (Exception e) { 
        	Activator.handleError(e);
        }
    }
 
    public void setDirty() {
    	firePropertyChange(EditorPart.PROP_DIRTY); 
    }

	public int promptToSaveOnClose() {
		String[] buttons = { "&No", "&Cancel", "&Yes" };
		MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), "Save Landscape", null, "'" + landscape.getDescription() + "' has been modified.  Save changes?", MessageDialog.QUESTION, buttons, 2);
		switch (dialog.open()) {
		case 0:	
			return ISaveablePart2.NO;
		case 1:	
			return ISaveablePart2.CANCEL;
		case 2:	
			return ISaveablePart2.YES;			
		default:
			return ISaveablePart2.DEFAULT;
		}
	}
	
	public IDialogSettings getDialogSettings() {
		return settings;
	}

}
