package com.collabnet.ccf.qc;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.FormEditor;

import com.collabnet.ccf.CcfParticipant;
import com.collabnet.ccf.IConnectionTester;
import com.collabnet.ccf.IMappingSection;
import com.collabnet.ccf.core.GenericArtifactParsingException;
import com.collabnet.ccf.editors.CcfEditorPage;
import com.collabnet.ccf.editors.CcfSystemEditorPage;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.qc.schemageneration.QCLayoutExtractor;
import com.collabnet.ccf.schemageneration.CCFSchemaAndXSLTFileGenerator;

public class QualityCenterCcfParticipant extends CcfParticipant {
	public static final String CREATE_INITIAL_MFD_FILE_QC_DEFECT = "QCDefect"; //$NON-NLS-1$
	public static final String CREATE_INITIAL_MFD_FILE_QC_REQUIREMENT = "QCRequirement"; //$NON-NLS-1$

	public static final String PROPERTIES_QC_URL = "qc.system.1.url"; //$NON-NLS-1$
	public static final String PROPERTIES_QC_USER = "qc.system.1.username"; //$NON-NLS-1$
	public static final String PROPERTIES_QC_PASSWORD = "qc.system.1.password"; //$NON-NLS-1$
	public static final String PROPERTIES_QC_RESYNC_USER = "qc.system.1.resync.username"; //$NON-NLS-1$
	public static final String PROPERTIES_QC_RESYNC_PASSWORD = "qc.system.1.resync.password"; //$NON-NLS-1$
	public static final String PROPERTIES_QC_ATTACHMENT_SIZE = "qc.max.attachmentsize.per.artifact"; //$NON-NLS-1$
	
	public static final String QCREADER_METRICS = "openadaptor:id=QCReader-metrics";
	public static final String QCWRITER_METRICS = "openadaptor:id=QCWriter-metrics";
	
	public static final String DEFAULT_JMX_PORT = "8085";
	
	public IMappingSection getMappingSection(int systemNumber) {
		IMappingSection mappingSection = new QualityCenterMappingSection();
		mappingSection.setSystemNumber(systemNumber);
		return mappingSection;
	}

	public CcfEditorPage getEditorPage1(FormEditor formEditor, String title) {
		CcfSystemEditorPage editorPage = new CcfSystemEditorPage(formEditor, "qc1", title, CcfSystemEditorPage.QC);
		editorPage.setSystemNumber(1);
		return editorPage;
	}
	
	public CcfEditorPage getEditorPage2(FormEditor formEditor, String title) {
		CcfSystemEditorPage editorPage = new CcfSystemEditorPage(formEditor, "qc2", title, CcfSystemEditorPage.QC);
		editorPage.setSystemNumber(2);
		return editorPage;
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

	@Override
	public Image getImage() {
		return Activator.getImage(Activator.IMAGE_QC);
	}

	public String getDefaultJmxPort() {
		return DEFAULT_JMX_PORT;
	}

	public String getReaderMetricsName() {
		return QCREADER_METRICS;
	}

	public String getWriterMetricsName() {
		return QCWRITER_METRICS;
	}

	public String getInitialMDFFileNameSegment(String repositoryId, boolean isSource) {
		String fileNameSegment = isSource ?
				(com.collabnet.ccf.Activator.CREATE_INITIAL_MFD_FILE_PREFIX + com.collabnet.ccf.Activator.CREATE_INITIAL_MFD_FILE_SEPARATOR) 
				: "";
		if (QCLayoutExtractor.isDefectRepository(repositoryId)) {
			fileNameSegment += CREATE_INITIAL_MFD_FILE_QC_DEFECT;
		} else {
			fileNameSegment += CREATE_INITIAL_MFD_FILE_QC_REQUIREMENT;
		}
		if (isSource) {
			fileNameSegment = fileNameSegment + com.collabnet.ccf.Activator.CREATE_INITIAL_MFD_FILE_SEPARATOR;
		}
		return fileNameSegment;
	}
	
	public String getUrl(Landscape landscape, int systemNumber) {
		Properties properties;
		if (1 == systemNumber) {
			properties = landscape.getProperties1();
		} else {
			properties = landscape.getProperties2();
		}
		return properties.getProperty(PROPERTIES_QC_URL);
	}
	
	public void extract(SynchronizationStatus status,
			CCFSchemaAndXSLTFileGenerator xmlFileGenerator,
			IProgressMonitor monitor) throws GenericArtifactParsingException,
			IOException, TransformerException {
		QCLayoutExtractor qcLayoutExtractor = new QCLayoutExtractor();
		Properties properties;
		if (status.getLandscape().getType2().equals(getType())) {
			properties = status.getLandscape().getProperties2();
		} else {
			properties = status.getLandscape().getProperties1();
		}
		String url = properties.getProperty(PROPERTIES_QC_URL, "");
		String user = properties.getProperty(PROPERTIES_QC_USER, "");
		String password = com.collabnet.ccf.Activator.decodePassword(properties.getProperty(
				PROPERTIES_QC_PASSWORD, ""));
		qcLayoutExtractor.setServerUrl(url);
		qcLayoutExtractor.setUserName(user);
		qcLayoutExtractor.setPassword(password);
		String repositoryId = null;

		File artifactToSchemaFile = null;
		File schemaToArtifactFile = null;
		File repositorySchemaFile = null;

		boolean isSourceSystem = false;
		if (status.getSourceSystemKind().startsWith(getType())) {
			isSourceSystem = true;
			repositoryId = status.getSourceRepositoryId();
			artifactToSchemaFile = status.getMappingFile(status
					.getGenericArtifactToSourceRepositorySchemaFileName());
			schemaToArtifactFile = status.getMappingFile(status
					.getSourceRepositorySchemaToGenericArtifactFileName());
			repositorySchemaFile = status.getMappingFile(status
					.getSourceRepositorySchemaFileName());
		} else {
			repositoryId = status.getTargetRepositoryId();
			artifactToSchemaFile = status.getMappingFile(status
					.getGenericArtifactToTargetRepositorySchemaFileName());
			schemaToArtifactFile = status.getMappingFile(status
					.getTargetRepositorySchemaToGenericArtifactFileName());
			repositorySchemaFile = status.getMappingFile(status
					.getTargetRepositorySchemaFileName());
		}

		outputSchemaAndXSLTFiles(qcLayoutExtractor, repositoryId,
				xmlFileGenerator, artifactToSchemaFile, schemaToArtifactFile,
				repositorySchemaFile, isSourceSystem, monitor);
	}

	public String getEntityType(String repositoryId) {
		int index = repositoryId.indexOf("-");
		if (-1 != index) {
			String project = repositoryId.substring(index + 1);
			index = project.indexOf("-");
			if (-1 != index) {
				return project.substring(index + 1);
			}
		}
		return null;
	}
	
	@Override
	public IConnectionTester getConnectionTester() {
		// TODO Once QualityCenterConnectionTester implements the actual connection test,
		//      return a QualityCenterConnectionTester instance instead of null.
//		return new QualityCenterConnectionTester();
		return null;
	}
	
}
