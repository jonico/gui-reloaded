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

package com.collabnet.ccf.pt.schemageneration;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.rpc.ServiceException;

import org.apache.commons.lang.StringUtils;

import com.collabnet.ccf.core.CCFRuntimeException;
import com.collabnet.ccf.core.GenericArtifact;
import com.collabnet.ccf.core.GenericArtifactField;
import com.collabnet.ccf.core.GenericArtifact.ArtifactActionValue;
import com.collabnet.ccf.core.GenericArtifact.ArtifactModeValue;
import com.collabnet.ccf.core.GenericArtifact.ArtifactTypeValue;
import com.collabnet.ccf.core.GenericArtifact.IncludesFieldMetaDataValue;
import com.collabnet.ccf.core.GenericArtifactField.FieldValueTypeValue;
import com.collabnet.ccf.schemageneration.FieldNameAmbiguityDissolver;
import com.collabnet.ccf.schemageneration.RepositoryLayoutExtractor;
import com.collabnet.core.ws.exception.WSException;
import com.collabnet.tracker.core.PTrackerWebServicesClient;
import com.collabnet.tracker.core.TrackerClientManager;
import com.collabnet.tracker.core.model.TrackerArtifactType;
import com.collabnet.tracker.core.model.TrackerAttribute;
import com.collabnet.tracker.ws.ArtifactTypeMetadata;
import com.collabnet.tracker.ws.Attribute;
import com.collabnet.tracker.ws.Option;

public class PTLayoutExtractor implements RepositoryLayoutExtractor {

	private FieldNameAmbiguityDissolver fieldNameAmbiguityDissolver = new FieldNameAmbiguityDissolver();

	public static final String PARAM_DELIMITER = ":";

	private String serverUrl = null;
	private String password = null;
	private String username = null;

	public static final String ATTACHMENT_TAG_NAME = "attachment";
	public static final String ATTACHMENT_ADDED_HISTORY_ACTIVITY_TYPE = "FileAddedDesc";
	public static final String ATTACHMENT_DELETED_HISTORY_ACTIVITY_TYPE = "FileDeletedDesc";
	public static final String URL_ADDED_HISTORY_ACTIVITY_TYPE = "UrlDescAdded";
	public static final String URL_DELETED_HISTORY_ACTIVITY_TYPE = "UrlDescDeleted";
	public static final String TRACKER_NAMESPACE = "urn:ws.tracker.collabnet.com";
	public static final String CREATED_ON_FIELD = "createdOn";
	public static final String MODIFIED_ON_FIELD = "modifiedOn";
	public static final String REASON_FIELD = "reason";
	public static final String ID_FIELD = "id";
	public static final String CREATED_BY_FIELD = "createdBy";
	public static final String MODIFIED_BY_FIELD = "modifiedBy";
	public static final String PROJECT_FIELD = "project";
	public static final String COMMENT_FIELD = "comment";
	public static final String CREATED_ON_FIELD_NAME = "{" + TRACKER_NAMESPACE
			+ "}" + CREATED_ON_FIELD;
	public static final String MODIFIED_ON_FIELD_NAME = "{" + TRACKER_NAMESPACE
			+ "}" + MODIFIED_ON_FIELD;
	public static final String REASON_FIELD_NAME = "{" + TRACKER_NAMESPACE
			+ "}" + REASON_FIELD;
	public static final String ID_FIELD_NAME = "{" + TRACKER_NAMESPACE + "}"
			+ ID_FIELD;
	public static final String CREATED_BY_FIELD_NAME = "{" + TRACKER_NAMESPACE
			+ "}" + CREATED_BY_FIELD;
	public static final String MODIFIED_BY_FIELD_NAME = "{" + TRACKER_NAMESPACE
			+ "}" + MODIFIED_BY_FIELD;
	public static final String PROJECT_FIELD_NAME = "{" + TRACKER_NAMESPACE
	+ "}" + PROJECT_FIELD;
	public static final String COMMENT_FIELD_NAME = "{" + TRACKER_NAMESPACE
			+ "}" + COMMENT_FIELD;
	private PTMetaDataHelper metadataHelper = PTMetaDataHelper.getInstance();

	ProjectTrackerHelper ptHelper = ProjectTrackerHelper.getInstance();

	private static HashMap<String, GenericArtifactField.FieldValueTypeValue> ptGATypesMap = new HashMap<String, GenericArtifactField.FieldValueTypeValue>();
	static {
		ptGATypesMap.put("SHORT_TEXT",
				GenericArtifactField.FieldValueTypeValue.STRING);
		ptGATypesMap.put("_SHORT_TEXT",
				GenericArtifactField.FieldValueTypeValue.STRING);
		ptGATypesMap.put("NUMBER",
				GenericArtifactField.FieldValueTypeValue.INTEGER);
		ptGATypesMap.put("MULTI_SELECT",
				GenericArtifactField.FieldValueTypeValue.STRING);
		ptGATypesMap.put("DATE",
				GenericArtifactField.FieldValueTypeValue.DATETIME);
		ptGATypesMap.put("STATE",
				GenericArtifactField.FieldValueTypeValue.STRING);
		ptGATypesMap.put("EMAIL",
				GenericArtifactField.FieldValueTypeValue.STRING);
		ptGATypesMap.put("LONG_TEXT",
				GenericArtifactField.FieldValueTypeValue.STRING);
		ptGATypesMap.put("SINGLE_SELECT",
				GenericArtifactField.FieldValueTypeValue.STRING);
		ptGATypesMap.put("USER", GenericArtifactField.FieldValueTypeValue.USER);
	}

	private Object generateFieldDocumentation(String displayName, String fieldName,
			String alternativeFieldName, FieldValueTypeValue fieldValueType,
			String fieldType, String isNullValueSupported, Option[] fieldValues) {
		StringBuffer documentation = new StringBuffer();
		documentation.append(displayName + " (" + fieldValueType+ ")\n");
	
		if (null != fieldValues) {
			documentation.append(" Values: [");
			Set<String> sortedValues = new TreeSet<String>();
			for (Option fieldValue : fieldValues) {
				String fieldDisplayName = fieldValue.getDisplayName();
				if (StringUtils.isEmpty(fieldDisplayName)) {
					sortedValues.add(fieldValue.getTagName());
				} else {
					sortedValues.add(fieldDisplayName);
				}
			}
			for (String fieldValueOption : sortedValues) {
				documentation.append(" '"+fieldValueOption+ "',");	
			}
			if (0 == fieldValues.length) {
				documentation.append("<empty>,");
			}
			documentation.deleteCharAt(documentation.length()-1);
			documentation.append(" ]");
			
		}
		return documentation.toString();
	}

	private Option[] getFieldOptions(String attributeNamespace,
			String attributeTagName, ArtifactTypeMetadata metadata) {
		for (Attribute att : metadata.getAttribute()) {
			String namespace = att.getNamespace();
			String tagName = att.getTagName();
			if (namespace.equals(attributeNamespace)
					&& tagName.equals(attributeTagName)) {
				return att.getOptions();
			}
		}
		return null;
	}

	/**
	 * Returns a generic artifact describing the repository layout
	 * 
	 * @param systemId
	 *            PT system id (used for caching purposes only)
	 * @param repositoryId
	 *            repository id of the PT project/artifact type
	 * @return
	 */
	public GenericArtifact getTrackerSchema(String systemId, String repositoryId) {
		fieldNameAmbiguityDissolver.resetFieldNameMapping();
		PTrackerWebServicesClient twsclient = null;
		GenericArtifact ga = null;
		try {
			twsclient = connect(repositoryId);
			String artifactTypeDisplayName = repositoryId
					.substring(repositoryId.lastIndexOf(":") + 1);

			String repositoryKey = systemId + ":" + repositoryId;
			// Do not use caching
			// TrackerArtifactType trackerArtifactType = metadataHelper
			// .getTrackerArtifactType(repositoryKey);

			TrackerArtifactType trackerArtifactType = null;
			try {
				trackerArtifactType = metadataHelper.getTrackerArtifactType(
						repositoryKey, artifactTypeDisplayName, twsclient);

				if (null == trackerArtifactType) {
					throw new CCFRuntimeException(
							"Artifact type for repository "
									+ repositoryKey
									+ " unknown, cannot synchronize repository.");
				}

				ArtifactTypeMetadata metaData = metadataHelper
						.getArtifactTypeMetadata(repositoryKey);

				ga = new GenericArtifact();
				ga.setArtifactAction(ArtifactActionValue.CREATE);
				ga.setArtifactMode(ArtifactModeValue.COMPLETE);
				ga.setArtifactType(ArtifactTypeValue.PLAINARTIFACT);
				ga.setIncludesFieldMetaData(IncludesFieldMetaDataValue.TRUE);

				Map<String, TrackerAttribute> attributeMap = trackerArtifactType
						.getAttributes();
				Set<String> attributeNames = attributeMap.keySet();

				for (String attributeName : attributeNames) {
					TrackerAttribute trackerAttribute = attributeMap
							.get(attributeName);
					if (null == trackerAttribute)
						continue;

					String ptAttributeType = trackerAttribute
							.getAttributeType();
					String attributeDisplayName = trackerAttribute
							.getDisplayName();
					String attributeNamespace = trackerAttribute.getNamespace();
					String attributeTagName = trackerAttribute.getTagName();
					String attributeNamespaceDisplayName = null;
					String alternativeName = null;
					GenericArtifactField field = null;
					if (attributeName.equals(CREATED_ON_FIELD_NAME)
							|| attributeName.equals(MODIFIED_ON_FIELD_NAME)
							|| attributeName.equals(ID_FIELD_NAME)
							|| attributeName.equals(CREATED_BY_FIELD_NAME)
							|| attributeName.equals(MODIFIED_BY_FIELD_NAME)
							|| attributeName.equals(PROJECT_FIELD_NAME)) 
					{
						attributeNamespaceDisplayName = attributeName;
						alternativeName = attributeTagName;

					} else {
						attributeNamespaceDisplayName = "{"
								+ attributeNamespace + "}"
								+ attributeDisplayName;
						alternativeName = attributeDisplayName;
					}
					GenericArtifactField.FieldValueTypeValue gaFieldType = null;
					gaFieldType = ptGATypesMap.get(ptAttributeType);

					field = ga.addNewField(attributeNamespaceDisplayName,
							GenericArtifactField.VALUE_FIELD_TYPE_FLEX_FIELD);
					field
							.setFieldAction(GenericArtifactField.FieldActionValue.REPLACE);
					field.setFieldValueType(gaFieldType);
					field.setFieldValueHasChanged(true);
					field.setMinOccurs(0);
					
					field.setNullValueSupported("true");
					

					if (attributeName.equals(CREATED_ON_FIELD_NAME)
							|| attributeName.equals(MODIFIED_ON_FIELD_NAME)) {
						field
								.setFieldValueType(GenericArtifactField.FieldValueTypeValue.DATETIME);
					}

					field.setAlternativeFieldName(fieldNameAmbiguityDissolver
							.generateNewFieldName(alternativeName, true));
					if (trackerAttribute.getAttributeType().equals(
							"MULTI_SELECT")) {
						field.setMaxOccursValue(GenericArtifactField.UNBOUNDED);
					} else {
						field.setMaxOccurs(1);
					}
					Option[] fieldValues = getFieldOptions(attributeNamespace,
							attributeTagName, metaData);
					field.setFieldValue(generateFieldDocumentation(attributeDisplayName, field
								.getFieldName(), field
								.getAlternativeFieldName(), field
								.getFieldValueType(), field.getFieldType(),
								field.getNullValueSupported(), fieldValues));
				}

				// add special field for comments
				GenericArtifactField commentField = ga.addNewField(
						COMMENT_FIELD_NAME,
						GenericArtifactField.VALUE_FIELD_TYPE_FLEX_FIELD);
				commentField
						.setFieldAction(GenericArtifactField.FieldActionValue.REPLACE);
				commentField
						.setFieldValueType(GenericArtifactField.FieldValueTypeValue.STRING);
				commentField.setFieldValueHasChanged(true);
				commentField
						.setAlternativeFieldName(fieldNameAmbiguityDissolver
								.generateNewFieldName(COMMENT_FIELD, true));
				commentField.setMinOccurs(0);
				commentField.setMaxOccursValue(GenericArtifactField.UNBOUNDED);
				commentField.setNullValueSupported("false");
				commentField.setFieldValue("This field contains the comments of PT.");

				// add reason field
				GenericArtifactField reasonField = ga.addNewField(
						REASON_FIELD_NAME,
						GenericArtifactField.VALUE_FIELD_TYPE_FLEX_FIELD);
				reasonField
						.setFieldAction(GenericArtifactField.FieldActionValue.REPLACE);
				reasonField
						.setFieldValueType(GenericArtifactField.FieldValueTypeValue.STRING);
				reasonField.setFieldValueHasChanged(true);
				reasonField.setAlternativeFieldName(fieldNameAmbiguityDissolver
						.generateNewFieldName(REASON_FIELD, true));
				reasonField.setMinOccurs(0);
				reasonField.setMaxOccursValue(GenericArtifactField.UNBOUNDED);
				reasonField.setNullValueSupported("false");
				reasonField.setFieldValue("This field contains the reason of a change in PT.");

			} catch (WSException e1) {
				String message = "Web Service Exception while retrieving Artifact Type meta data";
				// log.error(message, e1);
				throw new CCFRuntimeException(message, e1);
			} catch (RemoteException e1) {
				String message = "Remote Exception while retrieving Artifact Type meta data";
				// log.error(message, e1);
				throw new CCFRuntimeException(message, e1);
			} catch (ServiceException e1) {
				String message = "Service Exception while retrieving Artifact Type meta data";
				// log.error(message, e1);
				throw new CCFRuntimeException(message, e1);
			} catch (Exception e) {
				String message = "Exception while getting the changed artifacts";
				// log.error(message, e);
				throw new CCFRuntimeException(message, e);
			}
		} finally {
			if (null != twsclient) {
				disconnect(twsclient);
			}
		}
		return ga;
	}

	public PTrackerWebServicesClient connect(String repositoryId) {
		if (StringUtils.isEmpty(repositoryId)) {
			throw new IllegalArgumentException("Repository Id cannot be null");
		}

		String username = getUsername();
		String password = getPassword();

		String projectName = null;
		if (null != repositoryId) {
			String[] splitProjectName = repositoryId.split(":");
			if (null != splitProjectName) {
				if (1 <= splitProjectName.length) {
					projectName = splitProjectName[0];
				} else {
					throw new IllegalArgumentException(
							"Repository id is not valid."
									+ " Could not extract project name from repository id");
				}
			}
		}
		String connectionInfo = getServerUrl();

		String url = connectionInfo.substring(0,
				connectionInfo.indexOf("://") + 3)
				+ projectName
				+ "."
				+ connectionInfo.substring(connectionInfo.indexOf("://") + 3);
		PTrackerWebServicesClient twsclient = null;
		try {
			twsclient = TrackerClientManager.getInstance().createClient(url,
					username, password, null, null);
		} catch (MalformedURLException e) {
			String message = "Exception when trying to get the Web Services client: "
					+ e.getMessage();
			// log.error(message, e);
			throw new CCFRuntimeException(message, e);
		}
		return twsclient;
	}

	/**
	 * Releases the connection to the ConnectionManager.
	 * 
	 * @param connection
	 *            - The connection to be released to the ConnectionManager
	 */
	public void disconnect(PTrackerWebServicesClient connection) {
		// do nothing at the moment
	}

	/**
	 * Returns the server URL of the CEE system that is configured in the wiring
	 * file.
	 * 
	 * @return
	 */
	public String getServerUrl() {
		return serverUrl;
	}

	/**
	 * Sets the source CEE system's SOAP server URL.
	 * 
	 * @param serverUrl
	 *            - the URL of the source CEE system.
	 */
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	/**
	 * Gets the mandatory password that belongs to the username
	 * 
	 * @return the password
	 */
	private String getPassword() {
		return password;
	}

	/**
	 * Sets the password that belongs to the username
	 * 
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the mandatory user name The user name is used to login into the CEE
	 * instance whenever an artifact should be updated or extracted. This user
	 * has to differ from the resync user in order to force initial resyncs with
	 * the source system once a new artifact has been created.
	 * 
	 * @return the userName
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the mandatory username
	 * 
	 * The user name is used to login into the CEE instance whenever an artifact
	 * should be updated or extracted. This user has to differ from the resync
	 * user in order to force initial resyncs with the source system once a new
	 * artifact has been created.
	 * 
	 * @param usser
	 *            name the user name to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	public GenericArtifact getRepositoryLayout(String repositoryId) {
		return getTrackerSchema("CEESYSTEM", repositoryId);
	}

}
