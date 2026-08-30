package com.collabnet.ccf.pt;

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
import com.collabnet.ccf.pt.schemageneration.PTLayoutExtractor;
import com.collabnet.ccf.schemageneration.CCFSchemaAndXSLTFileGenerator;

public class ProjectTrackerCcfParticipant extends CcfParticipant {
	public static final String ENTITY_TRACKER = "Tracker";
	
	public static final String CREATE_INITIAL_MFD_FILE_PT_ISSUE = "PTIssue"; //$NON-NLS-1$
	
	public static final String PROPERTIES_CEE_URL = "cee.server.1.url"; //$NON-NLS-1$
	public static final String PROPERTIES_CEE_USER = "cee.server.1.username"; //$NON-NLS-1$
	public static final String PROPERTIES_CEE_DISPLAY_NAME = "cee.server.1.connector.user.displayName"; //$NON-NLS-1$
	public static final String PROPERTIES_CEE_PASSWORD = "cee.server.1.password"; //$NON-NLS-1$
	public static final String PROPERTIES_CEE_RESYNC_USER = "cee.server.1.resync.username"; //$NON-NLS-1$
	public static final String PROPERTIES_CEE_RESYNC_DISPLAY_NAME = "cee.server.1.resync.user.displayName"; //$NON-NLS-1$
	public static final String PROPERTIES_CEE_RESYNC_PASSWORD = "cee.server.1.resync.password"; //$NON-NLS-1$
	public static final String PROPERTIES_CEE_ATTACHMENT_SIZE = "cee.max.attachmentsize.per.artifact";	 //$NON-NLS-1$

	public static final String PTREADER_METRICS = "openadaptor:id=PTReader-metrics";
	public static final String PTWRITER_METRICS = "openadaptor:id=PTWriter-metrics";

	public static final String DEFAULT_JMX_PORT = "8083";
	
	public IMappingSection getMappingSection(int systemNumber) {
		IMappingSection mappingSection = new ProjectTrackerMappingSection();
		mappingSection.setSystemNumber(systemNumber);
		return mappingSection;
	}

	public CcfEditorPage getEditorPage1(FormEditor formEditor, String title) {
		CcfSystemEditorPage editorPage = new CcfSystemEditorPage(formEditor, "cee1", title, CcfSystemEditorPage.PT);
		editorPage.setSystemNumber(1);
		return editorPage;
	}
	
	public CcfEditorPage getEditorPage2(FormEditor formEditor, String title) {
		CcfSystemEditorPage editorPage = new CcfSystemEditorPage(formEditor, "cee2", title, CcfSystemEditorPage.PT);
		editorPage.setSystemNumber(2);
		return editorPage;
	}

	public String getNewProjectMappingVersion() {
		return Long.toString(Timestamp.valueOf("1999-01-01 00:00:00.0").getTime());
	}

	public String getResetProjectMappingVersion(Timestamp timestamp) {
		return Long.toString(timestamp.getTime());
	}
	
	public boolean showResetDate() {
		return true;
	}

	public boolean showResetVersion() {
		return false;
	}

	@Override
	public Image getImage() {
		return Activator.getImage(Activator.IMAGE_PROJECT_TRACKER);
	}

	public String getDefaultJmxPort() {
		return DEFAULT_JMX_PORT;
	}

	public String getReaderMetricsName() {
		return PTREADER_METRICS;
	}

	public String getWriterMetricsName() {
		return PTWRITER_METRICS;
	}

	public String getInitialMDFFileNameSegment(String repositoryId, boolean isSource) {
		String fileNameSegment = isSource ? 
				(com.collabnet.ccf.Activator.CREATE_INITIAL_MFD_FILE_PREFIX + com.collabnet.ccf.Activator.CREATE_INITIAL_MFD_FILE_SEPARATOR)
				: "";
		fileNameSegment += CREATE_INITIAL_MFD_FILE_PT_ISSUE;
		if (isSource) {
			fileNameSegment = fileNameSegment + com.collabnet.ccf.Activator.CREATE_INITIAL_MFD_FILE_SEPARATOR;
		}
		return fileNameSegment;
	}
	
	public void extract(SynchronizationStatus status,
			CCFSchemaAndXSLTFileGenerator xmlFileGenerator,
			IProgressMonitor monitor) throws GenericArtifactParsingException,
			IOException, TransformerException {
		PTLayoutExtractor ptLayoutExtractor = new PTLayoutExtractor();
		Properties properties;
		if (status.getLandscape().getType2().equals(getType())) {
			properties = status.getLandscape().getProperties2();
		} else {
			properties = status.getLandscape().getProperties1();
		}
		String url = properties.getProperty(PROPERTIES_CEE_URL, "");
		String user = properties.getProperty(PROPERTIES_CEE_USER, "");
		String password = com.collabnet.ccf.Activator.decodePassword(properties.getProperty(
				PROPERTIES_CEE_PASSWORD, ""));
		ptLayoutExtractor.setServerUrl(url);
		ptLayoutExtractor.setUsername(user);
		ptLayoutExtractor.setPassword(password);
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
			// schemaToArtifactFile = status.getMappingFile(status.
			// getSourceRepositorySchemaToGenericArtifactFileName());
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
		outputSchemaAndXSLTFiles(ptLayoutExtractor, repositoryId,
				xmlFileGenerator, artifactToSchemaFile, schemaToArtifactFile,
				repositorySchemaFile, isSourceSystem, monitor);
	}

	public String getUrl(Landscape landscape, int systemNumber) {
		Properties properties;
		if (1 == systemNumber) {
			properties = landscape.getProperties1();
		} else {
			properties = landscape.getProperties2();
		}
		return properties.getProperty(PROPERTIES_CEE_URL);
	}

	public String getEntityType(String repositoryId) {
		return ENTITY_TRACKER;
	}

	@Override
	public IConnectionTester getConnectionTester() {
		// TODO Once ProjectTrackerConnectionTester implements the actual connection test,
		//      return a ProjectTrackerConnectionTester instance instead of null.
//		return new ProjectTrackerConnectionTester();
		return null;
	}

}
