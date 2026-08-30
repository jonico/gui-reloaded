package com.collabnet.ccf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.transform.TransformerException;

import org.dom4j.Document;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;

import com.collabnet.ccf.core.GenericArtifact;
import com.collabnet.ccf.core.GenericArtifactHelper;
import com.collabnet.ccf.core.GenericArtifactParsingException;
import com.collabnet.ccf.model.MappingGroup;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.schemageneration.CCFSchemaAndXSLTFileGenerator;
import com.collabnet.ccf.schemageneration.RepositoryLayoutExtractor;

public abstract class CcfParticipant implements ICcfParticipant {
	private String id;
	private String name;
	private String type;
	private String repositoryKind;
	private String description;
	private int sequence;
	private Image image;
	private String propertiesFileName;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}	
	public String getPropertiesFileName() {
		return propertiesFileName;
	}
	public void setPropertiesFileName(String propertiesFileName) {
		this.propertiesFileName = propertiesFileName;
	}
	public String getRepositoryKind() {
		return repositoryKind;
	}
	public void setRepositoryKind(String repositoryKind) {
		this.repositoryKind = repositoryKind;
	}	
	public MappingGroup[] getMappingGroups(ProjectMappings projectMappingsParent, SynchronizationStatus[] projectMappings, SynchronizationStatus[] hiddenProjectMappings) {
		return null;
	}
	public int getSortPriority() {
		return 0;
	}
	
	public boolean allowAsSourceRepository(String repositoryId) {
		return true;
	}
	
	public boolean allowAsTargetRepository(String repositoryId) {
		return true;
	}
	
	public boolean enableFieldMappingEditing(String toType) {
		return true;
	}
	
	public IConnectionTester getConnectionTester() {
		return null;
	}
	
	public int compareTo(Object compareToObject) {
		if (!(compareToObject instanceof ICcfParticipant)) return 0;
		ICcfParticipant compareToCcfParticipant = (ICcfParticipant)compareToObject;
		if (getSequence() > compareToCcfParticipant.getSequence()) return 1;
		else if (compareToCcfParticipant.getSequence() > getSequence()) return -1;
		return getName().compareTo(compareToCcfParticipant.getName());
	}
	
	public static void outputSchemaAndXSLTFiles(
			RepositoryLayoutExtractor extractor, String repositoryId,
			CCFSchemaAndXSLTFileGenerator generator, File artifactToSchemaFile,
			File schemaToArtifactFile, File repositorySchemaFile,
			boolean isSourceSystem, IProgressMonitor monitor)
			throws GenericArtifactParsingException, IOException,
			TransformerException {

		monitor.subTask("Creating generic artifact XML document for "
				+ repositorySchemaFile.getName());

		GenericArtifact genericArtifact = extractor
				.getRepositoryLayout(repositoryId);
		Document genericArtifactDocument = GenericArtifactHelper
				.createGenericArtifactXMLDocument(genericArtifact);

		monitor.worked(1);
		if (monitor.isCanceled()) 
			return;

		monitor.subTask("Generating " + repositorySchemaFile.getName());

		writeFile(repositorySchemaFile, generator
				.getRepositorySpecificLayout(genericArtifactDocument));

		monitor.worked(1);
		if (monitor.isCanceled())
			return;

		if (isSourceSystem) {
			monitor.subTask("Generating " + artifactToSchemaFile.getName());

			writeFile(
					artifactToSchemaFile,
					generator
							.getGenericArtifactToRepositoryXSLTFile(genericArtifactDocument));
			monitor.worked(1);
		}

		if (monitor.isCanceled())
			return;

		if (!isSourceSystem) {
			monitor.subTask("Generating " + schemaToArtifactFile.getName());

			writeFile(
					schemaToArtifactFile,
					generator
							.getRepositoryToGenericArtifactXSLTFile(genericArtifactDocument));
			monitor.worked(1);
		}

		if (monitor.isCanceled())
			return;
	}
	
	private static void writeFile(File file, Document content) throws IOException {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8"));
			out.write(content.asXML());
		} catch (IOException e) {
			throw e;
		} finally {
			if (null != out) {
				try {
					out.close();
				} catch (Exception e) {
			}
		}
	}
}	

}
