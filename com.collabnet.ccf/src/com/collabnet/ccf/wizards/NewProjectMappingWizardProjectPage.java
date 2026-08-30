package com.collabnet.ccf.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.ICcfParticipant;
import com.collabnet.ccf.IMappingSection;
import com.collabnet.ccf.IPageCompleteListener;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.MappingGroup;
import com.collabnet.ccf.model.ProjectMappings;

public class NewProjectMappingWizardProjectPage extends WizardPage implements IPageCompleteListener {
	private ProjectMappings projectMappings;
	
	private ICcfParticipant ccfParticipant1;
	private ICcfParticipant ccfParticipant2;
	private IMappingSection mappingSection1;
	private IMappingSection mappingSection2;

	public static final String PREVIOUS_QC_DOMAIN = "NewProjectMapping.previousDomain.";
	public static final String PREVIOUS_QC_DOMAIN_COUNT = "NewProjectMapping.previousDomainCount";
	
	public NewProjectMappingWizardProjectPage(ProjectMappings projectMappings) {
		super("projectPage", "Mapping Details", Activator.getDefault().getImageDescriptor(Activator.IMAGE_NEW_PROJECT_MAPPING_WIZBAN));
		this.projectMappings = projectMappings;
		getCcfParticipants();
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		Composite outerContainer = new Composite(parent,SWT.NONE);
		outerContainer.setLayout(new GridLayout());
		outerContainer.setLayoutData(
		new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		MappingGroup mappingGroup = ((NewProjectMappingWizard)getWizard()).getMappingGroup();
		
		if (null != ccfParticipant1) {
			mappingSection1 = ccfParticipant1.getMappingSection(1);
			if (null != mappingSection1) {
				mappingSection1.getComposite(outerContainer, projectMappings.getLandscape());
				mappingSection1.setProjectPage(this);
				if (null != mappingGroup) {
					mappingSection1.initializeComposite(mappingGroup);
				}
			}
		}
		
		if (null != ccfParticipant2) {
			mappingSection2 = ccfParticipant2.getMappingSection(2);
			if (null != mappingSection2) {
				mappingSection2.getComposite(outerContainer, projectMappings.getLandscape());
				mappingSection2.setProjectPage(this);
				if (null != mappingGroup) {
					mappingSection2.initializeComposite(mappingGroup);
				}
			}
		}
					
		setMessage("Enter project mapping details");

		setControl(outerContainer);
	}

	public void setPageComplete() {
		setPageComplete(canFinish());
	}
	
	private boolean canFinish() {
		if (null != mappingSection1 && !mappingSection1.isPageComplete()) {
			return false;
		}
		if (null != mappingSection2 && !mappingSection2.isPageComplete()) {
			return false;
		}
		return true;
	}
	
	private void getCcfParticipants() {
		Landscape landscape = projectMappings.getLandscape();
		try {
			ccfParticipant1 = Activator.getCcfParticipantForId(landscape.getParticipantId1());
			if (null == ccfParticipant1) {
				ccfParticipant1 = Activator.getCcfParticipantForType(landscape.getType1());
			}
			ccfParticipant2 = Activator.getCcfParticipantForId(landscape.getParticipantId2());
			if (null == ccfParticipant2) {
				ccfParticipant2 = Activator.getCcfParticipantForType(landscape.getType2());
			}
		} catch (Exception e) {
			Activator.handleError(e);
		}
	}

	public IMappingSection getMappingSection1() {
		return mappingSection1;
	}

	public IMappingSection getMappingSection2() {
		return mappingSection2;
	}

}
