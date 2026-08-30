/*
 * Copyright 2009 CollabNet, Inc. ("CollabNet")
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/

package com.collabnet.ccf.schemageneration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.Document;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;

import com.collabnet.ccf.core.CCFRuntimeException;

/**
 * This component will apply XSLT scripts on generic artifact XML documents to
 * generate repository schemas and XSLT files to transform from/to the generic
 * artifact format to/from the repository specific layout
 * 
 * @author jnicolai
 * 
 */
public class CCFXSLTSchemaAndXSLTFileGenerator implements CCFSchemaAndXSLTFileGenerator {

	public final static String GENERIC_ARTIFACT_TO_REPOSITORY_SCHEMA_XSLT_FILE = "GenericArtifact2RepositorySchema.xsl";
	public final static String REPOSITORY_TO_GENERIC_ARTIFACT_XSLT_FILE = "Repository2GenericArtifact.xsl";
	public final static String GENERIC_ARTIFACT_TO_REPOSITORY_XSLT_FILE = "GenericArtifact2Repository.xsl";

	private Transformer genericArtifactToRepositoryTransformer = null;
	private Transformer genericArtifactToRepositorySchemaTransformer = null;
	private Transformer repositoryToGenericArtifactTransformer = null;

	public CCFXSLTSchemaAndXSLTFileGenerator(String xsltDirectory) {
		genericArtifactToRepositorySchemaTransformer = loadXSLT(new File(xsltDirectory+"/"+GENERIC_ARTIFACT_TO_REPOSITORY_SCHEMA_XSLT_FILE));
		genericArtifactToRepositoryTransformer = loadXSLT(new File(xsltDirectory+"/"+GENERIC_ARTIFACT_TO_REPOSITORY_XSLT_FILE));
		repositoryToGenericArtifactTransformer = loadXSLT(new File(xsltDirectory+"/"+REPOSITORY_TO_GENERIC_ARTIFACT_XSLT_FILE));
	}

	/**
	 * Applies the transform to the Dom4J document
	 * 
	 * @param transformer
	 * @param d
	 *            the document to transform
	 * 
	 * @return an array containing a single XML string representing the
	 *         transformed document
	 * @throws TransformerException
	 *             thrown if an XSLT runtime error happens during transformation
	 */
	private static Document transform(Transformer transformer, Document d)
			throws TransformerException {
		DocumentSource source = new DocumentSource(d);
		DocumentResult result = new DocumentResult();
		// TODO: Allow the user to specify stylesheet parameters?
		transformer.transform(source, result);
		return result.getDocument();
	}

	/* (non-Javadoc)
	 * @see com.collabnet.ccf.schemageneration.CCFSchemaAndXSLTFileGenerator#getRepositorySpecificLayout(org.dom4j.Document)
	 */
	public Document getRepositorySpecificLayout(Document ga)
			throws TransformerException {
		return transform(genericArtifactToRepositorySchemaTransformer, ga);
	}

	/* (non-Javadoc)
	 * @see com.collabnet.ccf.schemageneration.CCFSchemaAndXSLTFileGenerator#getGenericArtifactToRepositoryXSLTFile(org.dom4j.Document)
	 */
	public Document getGenericArtifactToRepositoryXSLTFile(Document ga)
			throws TransformerException {
		return transform(genericArtifactToRepositoryTransformer, ga);
	}

	/* (non-Javadoc)
	 * @see com.collabnet.ccf.schemageneration.CCFSchemaAndXSLTFileGenerator#getRepositoryToGenericArtifactXSLTFile(org.dom4j.Document)
	 */
	public Document getRepositoryToGenericArtifactXSLTFile(Document ga)
			throws TransformerException {
		return transform(repositoryToGenericArtifactTransformer, ga);
	}

	/**
	 * Loads XSLT files and builds transformer
	 * 
	 * @throws ValidationException
	 *             if the XSLT file is not defined in the properties, the file
	 *             cannot be found or there was an error parsing it
	 */
	private static Transformer loadXSLT(File xsltFile) {
		Transformer transform = null;
		InputStream inputStream = null;
		// load the transform
		try {		
			// inputStream = CCFXSLTSchemaAndXSLTFileGenerator.class.getResourceAsStream("/" + xsltFile);
			// StreamSource streamSource = new StreamSource(inputStream);
			StreamSource streamSource = new StreamSource(xsltFile);
			TransformerFactory factory = TransformerFactory.newInstance();
			transform = factory.newTransformer(streamSource);

			// log.debug("Loaded XSLT [" + xsltFile + "] successfully");
		} catch (TransformerConfigurationException e) {
			throw new CCFRuntimeException("Could not load XSLT file " + xsltFile + ": "
					+ e.getMessage(), e);
		} finally {
			if (null != inputStream) {
				try {
					inputStream.close();
				} catch (IOException e) {}
			}
		}
		return transform;
	}
}
