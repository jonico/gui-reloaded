package com.collabnet.ccf.sw;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.FormEditor;

import com.collabnet.ccf.CcfParticipant;
import com.collabnet.ccf.IConnectionTester;
import com.collabnet.ccf.IMappingSection;
import com.collabnet.ccf.core.GenericArtifactParsingException;
import com.collabnet.ccf.editors.CcfEditorPage;
import com.collabnet.ccf.editors.CcfSystemEditorPage;
import com.collabnet.ccf.model.AdministratorMappingGroup;
import com.collabnet.ccf.model.AdministratorProjectMappings;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.MappingGroup;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.schemageneration.CCFSchemaAndXSLTFileGenerator;

public class ScrumWorksCcfParticipant extends CcfParticipant {
	public static final String DEFAULT_JMX_PORT = "8086";
	public static final String TYPE = "SWP";
	public static final String SWPREADER_METRICS = "openadaptor:id=SWPReader-metrics";
	public static final String SWPWRITER_METRICS = "openadaptor:id=SWPWriter-metrics";
	
	@Override
	public Image getImage() {
		return Activator.getImage(Activator.IMAGE_SW);
	}

	public void extract(SynchronizationStatus status,
			CCFSchemaAndXSLTFileGenerator xmlFileGenerator,
			IProgressMonitor monitor) throws GenericArtifactParsingException,
			IOException, TransformerException {

	}

	public String getUrl(Landscape landscape, int systemNumber) {
		Properties properties;
		if (1 == systemNumber) {
			properties = landscape.getProperties1();
		} else {
			properties = landscape.getProperties2();
		}
		return properties.getProperty(com.collabnet.ccf.Activator.PROPERTIES_SW_URL);
	}

	public CcfEditorPage getEditorPage1(FormEditor formEditor, String title) {
		CcfSystemEditorPage editorPage = new CcfSystemEditorPage(formEditor, "sw1", title, CcfSystemEditorPage.SW);
		editorPage.setSystemNumber(1);
		return editorPage;
	}

	public CcfEditorPage getEditorPage2(FormEditor formEditor, String title) {
		CcfSystemEditorPage editorPage = new CcfSystemEditorPage(formEditor, "sw2", title, CcfSystemEditorPage.SW);
		editorPage.setSystemNumber(2);
		return editorPage;
	}

	public String getInitialMDFFileNameSegment(String repositoryId,
			boolean isSource) {
		String fileNameSegment = isSource ? 
				(com.collabnet.ccf.Activator.CREATE_INITIAL_MFD_FILE_PREFIX + com.collabnet.ccf.Activator.CREATE_INITIAL_MFD_FILE_SEPARATOR)
				: "";
		fileNameSegment += StringUtils.substringAfterLast(repositoryId, "-");
		if (isSource) {
			fileNameSegment += com.collabnet.ccf.Activator.CREATE_INITIAL_MFD_FILE_SEPARATOR;
		}
		return fileNameSegment;
	}

	public String getDefaultJmxPort() {
		return DEFAULT_JMX_PORT;
	}

	public IMappingSection getMappingSection(int systemNumber) {
		IMappingSection mappingSection = new ScrumWorksMappingSection();
		mappingSection.setSystemNumber(systemNumber);
		return mappingSection;
	}

	public String getNewProjectMappingVersion() {
		return "0";
	}

	public String getResetProjectMappingVersion(Timestamp timestamp) {
		return "0";
	}
	
	public boolean showResetDate() {
		return false;
	}

	public boolean showResetVersion() {
		return true;
	}

	public String getReaderMetricsName() {
		return SWPREADER_METRICS;
	}

	public String getWriterMetricsName() {
		return SWPWRITER_METRICS;
	}

	public String getEntityType(String repositoryId) {
		return SWPMetaData.retrieveSWPTypeFromRepositoryId(repositoryId).toString();
	}

	@Override
	public int getSortPriority() {
		return 4;
	}

	@Override
	public boolean allowAsTargetRepository(String repositoryId) {
		if (repositoryId.endsWith("-MetaData")) {
			return false;
		}
		return super.allowAsTargetRepository(repositoryId);
	}

	@Override
	public MappingGroup[] getMappingGroups(ProjectMappings projectMappingsParent, SynchronizationStatus[] projectMappings, SynchronizationStatus[] hiddenProjectMappings) {
		List<String> products = new ArrayList<String>();
		for (SynchronizationStatus projectMapping : projectMappings) {
			String product = getProduct(projectMapping);
			if (null != product && !products.contains(product)) {
				products.add(product);
			}
		}
		String[] productArray = new String[products.size()];
		products.toArray(productArray);
		Arrays.sort(productArray);
		List<MappingGroup> mappingGroups = new ArrayList<MappingGroup>();
		for (String product : productArray) {
			MappingGroup productsGroup;
			MappingGroup pbiGroup;
			MappingGroup taskGroup;
			MappingGroup productGroup;
			MappingGroup releaseGroup;
			MappingGroup metaDataGroup;
			if (projectMappingsParent instanceof AdministratorProjectMappings) {
				productsGroup = new AdministratorMappingGroup(this, projectMappingsParent, product, getProductName(product), Activator.getImage(Activator.IMAGE_SWP_PRODUCT));
				pbiGroup = new AdministratorMappingGroup(this, projectMappingsParent, product + "-" + SWPMetaData.PBI.toString(), SWPMetaData.PBI.toString(), Activator.getImage(Activator.IMAGE_PBI));
				taskGroup = new AdministratorMappingGroup(this, projectMappingsParent, product + "-" + SWPMetaData.TASK.toString(), SWPMetaData.TASK.toString(), Activator.getImage(Activator.IMAGE_TASK));
				productGroup = new AdministratorMappingGroup(this, projectMappingsParent, product + "-" + SWPMetaData.PRODUCT.toString(), SWPMetaData.PRODUCT.toString(), Activator.getImage(Activator.IMAGE_PRODUCT));
				releaseGroup = new AdministratorMappingGroup(this, projectMappingsParent, product + "-" + SWPMetaData.RELEASE.toString(), SWPMetaData.RELEASE.toString(), Activator.getImage(Activator.IMAGE_RELEASE));		
				metaDataGroup = new AdministratorMappingGroup(this, projectMappingsParent, product + "-" + "MetaData", "MetaData", Activator.getImage(Activator.IMAGE_METADATA));				
			} else {
				productsGroup = new MappingGroup(this, projectMappingsParent, product, getProductName(product), Activator.getImage(Activator.IMAGE_SWP_PRODUCT));
				pbiGroup = new MappingGroup(this, projectMappingsParent, product + "-" + SWPMetaData.PBI.toString(), SWPMetaData.PBI.toString(), Activator.getImage(Activator.IMAGE_PBI));
				taskGroup = new MappingGroup(this, projectMappingsParent, product + "-" + SWPMetaData.TASK.toString(), SWPMetaData.TASK.toString(), Activator.getImage(Activator.IMAGE_TASK));
				productGroup = new MappingGroup(this, projectMappingsParent, product + "-" + SWPMetaData.PRODUCT.toString(), SWPMetaData.PRODUCT.toString(), Activator.getImage(Activator.IMAGE_PRODUCT));
				releaseGroup = new MappingGroup(this, projectMappingsParent, product + "-" + SWPMetaData.RELEASE.toString(), SWPMetaData.RELEASE.toString(), Activator.getImage(Activator.IMAGE_RELEASE));
				metaDataGroup = new MappingGroup(this, projectMappingsParent, product + "-" + "MetaData", "MetaData", Activator.getImage(Activator.IMAGE_METADATA));
			}
			setChildMappings(product, pbiGroup, taskGroup, productGroup, releaseGroup, metaDataGroup, projectMappings, hiddenProjectMappings);
			MappingGroup[] subGroups = { pbiGroup, taskGroup, productGroup, releaseGroup, metaDataGroup };
			productsGroup.setChildGroups(subGroups);
			mappingGroups.add(productsGroup);
		}
		MappingGroup[] mappingGroupArray = new MappingGroup[mappingGroups.size()];
		mappingGroups.toArray(mappingGroupArray);
		return mappingGroupArray;
	}
	
	@Override
	public boolean enableFieldMappingEditing(String toType) {
		if (toType.equals("TF")) {
			return false;
		} else {
			return super.enableFieldMappingEditing(toType);
		}
	}

	@Override
	public IConnectionTester getConnectionTester() {
	// TODO If the ScrumWorksConnectionTester can be fixed to not prompt for credentials when
	//      invalid credentials are provided, the change this to return a ScrumWorksConnectionTester
	//      instance.  Until then, the Landscape editor will not show a Test Connection button
	//      on the ScrumWorks Pro properties page.
//		return new ScrumWorksConnectionTester();
		return null;
	}

	private void setChildMappings(String product, MappingGroup pbiGroup, MappingGroup taskGroup, MappingGroup productGroup, MappingGroup releaseGroup, MappingGroup metaDataGroup, SynchronizationStatus[] projectMappings, SynchronizationStatus[] hiddenProjectMappings) {
		if (null != hiddenProjectMappings) {
			for (SynchronizationStatus projectMapping : hiddenProjectMappings) {
				String mappingProduct = getProduct(projectMapping);
				if (null != mappingProduct && mappingProduct.equals(product)) {
					if (projectMapping.getTargetRepositoryId().endsWith("-Product")) {
						SynchronizationStatus[] hiddenProductMappings = { projectMapping };
						productGroup.setHiddenChildMappings(hiddenProductMappings);
					}
					if (projectMapping.getTargetRepositoryId().endsWith("-Release")) {
						SynchronizationStatus[] hiddenReleaseMappings = { projectMapping };
						releaseGroup.setHiddenChildMappings(hiddenReleaseMappings);
					}
				}
			}
		}
		List<SynchronizationStatus> pbiMappings = new ArrayList<SynchronizationStatus>();
		List<SynchronizationStatus> taskMappings = new ArrayList<SynchronizationStatus>();
		List<SynchronizationStatus> productMappings = new ArrayList<SynchronizationStatus>();
		List<SynchronizationStatus> releaseMappings = new ArrayList<SynchronizationStatus>();
		List<SynchronizationStatus> metaDataMappings = new ArrayList<SynchronizationStatus>();
		for (SynchronizationStatus projectMapping : projectMappings) {
			String mappingProduct = getProduct(projectMapping);
			if (null != mappingProduct && mappingProduct.equals(product)) {
				if (projectMapping.getSourceRepositoryId().endsWith("-PBI") || projectMapping.getTargetRepositoryId().endsWith("-PBI")) {
					pbiMappings.add(projectMapping);
				}
				else if (projectMapping.getSourceRepositoryId().endsWith("-Task") || projectMapping.getTargetRepositoryId().endsWith("-Task")) {
					taskMappings.add(projectMapping);
				}	
				else if (projectMapping.getSourceRepositoryId().endsWith("-Product") || projectMapping.getTargetRepositoryId().endsWith("-Product")) {
					productMappings.add(projectMapping);
				}
				else if (projectMapping.getSourceRepositoryId().endsWith("-Release") || projectMapping.getTargetRepositoryId().endsWith("-Release")) {
					releaseMappings.add(projectMapping);
				}
				else if (projectMapping.getSourceRepositoryId().endsWith("-MetaData") || projectMapping.getTargetRepositoryId().endsWith("-MetaData")) {
					metaDataMappings.add(projectMapping);
				}	
			}
		}
		SynchronizationStatus[] pbiMappingArray = new SynchronizationStatus[pbiMappings.size()];
		pbiMappings.toArray(pbiMappingArray);
		pbiGroup.setChildMappings(pbiMappingArray);
		SynchronizationStatus[] taskMappingArray = new SynchronizationStatus[taskMappings.size()];
		taskMappings.toArray(taskMappingArray);
		taskGroup.setChildMappings(taskMappingArray);
		SynchronizationStatus[] productMappingArray = new SynchronizationStatus[productMappings.size()];
		productMappings.toArray(productMappingArray);
		productGroup.setChildMappings(productMappingArray);
		SynchronizationStatus[] releaseMappingArray = new SynchronizationStatus[releaseMappings.size()];
		releaseMappings.toArray(releaseMappingArray);
		releaseGroup.setChildMappings(releaseMappingArray);
		SynchronizationStatus[] metaDataMappingArray = new SynchronizationStatus[metaDataMappings.size()];
		metaDataMappings.toArray(metaDataMappingArray);
		metaDataGroup.setChildMappings(metaDataMappingArray);
	}
	
	public static String getProduct(SynchronizationStatus projectMapping) {
		String repositoryId = null;
		if (projectMapping.getSourceRepositoryId().endsWith("-PBI") ||
		    projectMapping.getSourceRepositoryId().endsWith("-Task") ||
	        projectMapping.getSourceRepositoryId().endsWith("-Product") ||
	        projectMapping.getSourceRepositoryId().endsWith("-MetaData") ||
	        projectMapping.getSourceRepositoryId().endsWith("-Release")) {
			repositoryId = projectMapping.getSourceRepositoryId();
		}
		else if (projectMapping.getTargetRepositoryId().endsWith("-PBI") ||
			    projectMapping.getTargetRepositoryId().endsWith("-Task") ||
		        projectMapping.getTargetRepositoryId().endsWith("-Product") ||
		        projectMapping.getTargetRepositoryId().endsWith("-MetaData") ||
		        projectMapping.getTargetRepositoryId().endsWith("-Release")) {
				repositoryId = projectMapping.getTargetRepositoryId();
		}
		if (null != repositoryId) {
			String product = repositoryId.substring(0, repositoryId.lastIndexOf("-"));
			return product;
		}
		return null;
	}
	
	private String getProductName(String product) {
		String productName = null;
		if (null != product) {
			int index = product.lastIndexOf("(");
			if (-1 != index) {
				productName = product.substring(0, index);
			}
		}
		if (null == productName) {
			productName = product;
		}
		return productName;
	}

}
