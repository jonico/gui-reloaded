package com.collabnet.ccf.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.CCFJMXMonitorBean;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.model.Landscape;

public class JmxConsoleEditor extends FormEditor {
	private Landscape landscape;
	
	private CCFJMXMonitorBean monitor1;
	private CCFJMXMonitorBean monitor2;
	
	private CcfDataProvider dataProvider;

	private JmxConsoleStatusEditorPage statusPage;
	private JmxConsoleProjectMappingsEditorPage mappingsPage;
	
	private IDialogSettings settings = Activator.getDefault().getDialogSettings();
	
	public final static String ID = "com.collabnet.ccf.editors.JmxConsoleEditor";

	public JmxConsoleEditor() {
		super();
	}
	
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        setSite(site);
        setInput(input);
        setPartName(input.getName());
		CcfEditorInput ccfEditorInput = (CcfEditorInput)getEditorInput();
		landscape = ccfEditorInput.getLandscape();	
		if (null != landscape) {
			getMonitors();
		}
    }
    
	private void getMonitors() {
		monitor1 = new CCFJMXMonitorBean();
		monitor1.setHostName(landscape.getHostName1());
		monitor1.setRmiPort(landscape.getType1(), landscape.getType2());
		
		monitor2 = new CCFJMXMonitorBean();
		monitor2.setHostName(landscape.getHostName2());
		monitor2.setRmiPort(landscape.getType2(), landscape.getType1());
	}

	@Override
	protected void addPages() {
		createStatusPage();
		createMappingsPage();
		try {
			String activePage = settings.get(JmxConsoleEditorPage.ACTIVE_PAGE);
			if (null != activePage) {
				setActivePage(activePage);	
			}
		} catch (Exception e) {}
	}
	
    @SuppressWarnings("unchecked")
	private void createStatusPage() {
        try {
        	statusPage = new JmxConsoleStatusEditorPage(this, "status", getEditorInput().getName());
	        int statusIndex = addPage(statusPage);
	        setPageText(statusIndex, "Status");
	        pages.add(statusPage);
        } catch (Exception e) { 
        	Activator.handleError(e);
        }
    }
    
    @SuppressWarnings("unchecked")
	private void createMappingsPage() {
        try {
        	mappingsPage = new JmxConsoleProjectMappingsEditorPage(this, "mappings", getEditorInput().getName());
	        int mappingsIndex = addPage(mappingsPage);
	        setPageText(mappingsIndex, "Project Mappings");
	        pages.add(mappingsPage);
        } catch (Exception e) { 
        	Activator.handleError(e);
        }
    }

	@Override
	public void doSave(IProgressMonitor arg0) {
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
	
	public CCFJMXMonitorBean getMonitor1() {
		return monitor1;
	}

	public CCFJMXMonitorBean getMonitor2() {
		return monitor2;
	}
	
	public Landscape getLandscape() {
		return landscape;
	}
	
	public CcfDataProvider getDataProvider() {
		if (null == dataProvider) {
			dataProvider = new CcfDataProvider();
		}
		return dataProvider;
	}

}
