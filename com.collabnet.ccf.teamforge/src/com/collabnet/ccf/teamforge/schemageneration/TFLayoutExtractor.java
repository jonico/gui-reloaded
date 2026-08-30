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

package com.collabnet.ccf.teamforge.schemageneration;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import com.collabnet.ccf.teamforge.schemageneration.TFArtifactMetaData.FIELD_TYPE;
import com.collabnet.ccf.teamforge.schemageneration.TFArtifactMetaData.SFEEFields;
import com.collabnet.teamforge.api.tracker.IFieldValueDO;
import com.collabnet.teamforge.api.tracker.TrackerFieldDO;
import com.collabnet.teamforge.api.tracker.TrackerFieldValueDO;

/**
 * This component generates generic artifacts that represent the layout (schema)
 * of a TF tracker
 * 
 * @author jnicolai
 * 
 */
public class TFLayoutExtractor implements RepositoryLayoutExtractor {
	private TFSoapClient soapClient;

	private TrackerFieldValueDO[] priorityFieldValues;

	private FieldNameAmbiguityDissolver fieldNameAmbiguityDissolver = new FieldNameAmbiguityDissolver();

	public TFLayoutExtractor(String serverUrl, String userId, String password) {
		soapClient = TFSoapClient.getSoapClient(serverUrl, userId, password);
	}

	private TrackerFieldValueDO[] getPriorityFieldValues() {
		if (null == priorityFieldValues) {
			// we manually have to create the field info for the priority field
			List<TrackerFieldValueDO> fieldValues = new ArrayList<TrackerFieldValueDO>();
			TrackerFieldValueDO fieldValue = new TrackerFieldValueDO(soapClient.supports60(), soapClient.supports50());
			fieldValue.setValue("0");
			fieldValues.add(fieldValue);
			fieldValue = new TrackerFieldValueDO(soapClient.supports60(), soapClient.supports50());
			fieldValue.setValue("1");
			fieldValues.add(fieldValue);
			fieldValue = new TrackerFieldValueDO(soapClient.supports60(), soapClient.supports50());
			fieldValue.setValue("2");
			fieldValues.add(fieldValue);
			fieldValue = new TrackerFieldValueDO(soapClient.supports60(), soapClient.supports50());
			fieldValue.setValue("3");
			fieldValues.add(fieldValue);
			fieldValue = new TrackerFieldValueDO(soapClient.supports60(), soapClient.supports50());
			fieldValue.setValue("4");
			fieldValues.add(fieldValue);
			fieldValue = new TrackerFieldValueDO(soapClient.supports60(), soapClient.supports50());
			fieldValue.setValue("5");
			fieldValues.add(fieldValue);
			priorityFieldValues = fieldValues
					.toArray(new TrackerFieldValueDO[] {});
		}
		return priorityFieldValues;
	}

	/**
	 * This method is used to add fields for system defined fields
	 * 
	 * @param sfField
	 * @param genericArtifact
	 * @return
	 */
	private GenericArtifactField createGenericArtifactField(
			TFArtifactMetaData.SFEEFields sfField,
			GenericArtifact genericArtifact, IFieldValueDO[] fieldInfo) {
		String fieldName = sfField.getFieldName();

		GenericArtifactField field = genericArtifact.addNewField(fieldName,
				GenericArtifactField.VALUE_FIELD_TYPE_MANDATORY_FIELD);
		GenericArtifactField.FieldValueTypeValue fieldValueType = TFArtifactMetaData
				.getFieldValueType(fieldName);
		field.setFieldAction(GenericArtifactField.FieldActionValue.REPLACE);
		field.setFieldValueType(fieldValueType);
		field.setFieldValueHasChanged(true);

		String alternativeFieldName = fieldNameAmbiguityDissolver
				.generateNewFieldName(fieldName, false);
		field.setAlternativeFieldName(alternativeFieldName);
		field.setNullValueSupported(sfField.isNullValueSupported().toString());
		field.setMinOccurs(0);
		field.setMaxOccurs(1);

		field.setFieldValue(generateFieldDocumentation(fieldName,
				alternativeFieldName, fieldValueType,
				GenericArtifactField.VALUE_FIELD_TYPE_MANDATORY_FIELD, sfField
						.isNullValueSupported().toString(), fieldInfo));
		return field;
	}

	private static Object generateFieldDocumentation(String fieldName,
			String alternativeFieldName, FieldValueTypeValue fieldValueType,
			String fieldType, String isNullValueSupported,
			IFieldValueDO[] fieldValues) {
		StringBuffer documentation = new StringBuffer();
		documentation.append(fieldName + " (" + fieldType + " / "
				+ fieldValueType + ")\n");
		if (null != fieldValues && 0 != fieldValues.length) {
			documentation.append(" Values: [");
			Set<String> sortedValues = new TreeSet<String>();
			for (IFieldValueDO fieldValueSoapDO : fieldValues) {
				sortedValues.add(fieldValueSoapDO.getValue());
			}
			for (String fieldValueOption : sortedValues) {
				documentation.append(" '" + fieldValueOption + "',");
			}
			/*
			 * if (fieldValues.length == 0) { documentation.append("<empty>"); }
			 */
			documentation.deleteCharAt(documentation.length() - 1);
			documentation.append(" ]");
		}
		return documentation.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.collabnet.ccf.schemageneration.RepositoryLayoutExtractor#
	 * getRepositoryLayout(java.lang.String)
	 */
	public GenericArtifact getRepositoryLayout(String repositoryId) {
		if (isTrackerRepository(repositoryId)) {
			return getTrackerSchema(repositoryId);
		} else {
			return getPlanningFolderSchema(repositoryId);
		}
	}
	
	
	private GenericArtifact getPlanningFolderSchema(String repositoryId) {
		// since CTF planning folder do not support any custom fields (yet), we just output a constant schema
		GenericArtifact genericArtifact = new GenericArtifact();
		genericArtifact.setArtifactAction(ArtifactActionValue.CREATE);
		genericArtifact.setArtifactMode(ArtifactModeValue.COMPLETE);
		genericArtifact.setArtifactType(ArtifactTypeValue.PLAINARTIFACT);
		genericArtifact
				.setIncludesFieldMetaData(IncludesFieldMetaDataValue.TRUE);
		
		// set the fields of PlanningFolderSoapDO
		createGenericArtifactField(TFArtifactMetaData.SFEEFields.startDate,
				genericArtifact, null);
		createGenericArtifactField(TFArtifactMetaData.SFEEFields.endDate,
				genericArtifact, null);
		
		// set the fields of FolderSoapDO
		createGenericArtifactField(TFArtifactMetaData.SFEEFields.title,
				genericArtifact, null);
		createGenericArtifactField(
				TFArtifactMetaData.SFEEFields.description, genericArtifact,
				null);
		createGenericArtifactField(TFArtifactMetaData.SFEEFields.path,
				genericArtifact, null);
		createGenericArtifactField(TFArtifactMetaData.SFEEFields.projectId,
				genericArtifact, null);
		createGenericArtifactField(TFArtifactMetaData.SFEEFields.parentFolderId,
				genericArtifact, null);
		
		// set fields of ObjectSoapDO
		createGenericArtifactField(TFArtifactMetaData.SFEEFields.createdBy,
				genericArtifact, null);
		createGenericArtifactField(
				TFArtifactMetaData.SFEEFields.createdDate, genericArtifact,
				null);
		createGenericArtifactField(TFArtifactMetaData.SFEEFields.id,
				genericArtifact, null);
		createGenericArtifactField(
				TFArtifactMetaData.SFEEFields.lastModifiedBy,
				genericArtifact, null);
		createGenericArtifactField(
				TFArtifactMetaData.SFEEFields.lastModifiedDate,
				genericArtifact, null);
		createGenericArtifactField(TFArtifactMetaData.SFEEFields.version,
				genericArtifact, null);
		
		if (soapClient.supports54()) {
			IFieldValueDO[] fieldValues = null;
			try {
				fieldValues = soapClient.getPlanningClient().getPlanningStatusValues(extractProjectFromRepositoryId(repositoryId));
			} catch (RemoteException e) {
				// do nothing. 
				// If the exception occurs, the alternatives simply don't appear in the GUI.
			}
			createGenericArtifactField(TFArtifactMetaData.SFEEFields.status, genericArtifact, fieldValues);
			createGenericArtifactField(TFArtifactMetaData.SFEEFields.statusClass, genericArtifact, null);
			createGenericArtifactField(TFArtifactMetaData.SFEEFields.capacity, genericArtifact, null);
			createGenericArtifactField(TFArtifactMetaData.SFEEFields.releaseId, genericArtifact, null);
		}
		
		return genericArtifact;
	}
	
	/**
	 * If the planning folder repository id contains the project id this will be returned
	 * @param repositoryId
	 * @return project id
	 */
	public static String extractProjectFromRepositoryId(String repositoryId) {
		if(null != repositoryId){
			String[] splitRepo = repositoryId.split("-");
			if(null != splitRepo){
				if(2 != splitRepo.length){
					throw new IllegalArgumentException("Repository id is not valid.");
				}
				else {
					return splitRepo[0];
				}
			}
		}
		throw new IllegalArgumentException("Repository id is not valid.");
	}


	/**
	 * Returns whether this repository id belongs to a tracker
	 * If not, it belongs to a planning folder
	 * @param repositoryId repositoryId
	 * @return true if repository id belongs to a tracker
	 */
	public static boolean isTrackerRepository(String repositoryId) {
		return repositoryId.startsWith("tracker");
	}

	private GenericArtifact getTrackerSchema(String trackerId) {
		GenericArtifact genericArtifact = null;
		try {
			fieldNameAmbiguityDissolver.resetFieldNameMapping();
			genericArtifact = new GenericArtifact();
			genericArtifact.setArtifactAction(ArtifactActionValue.CREATE);
			genericArtifact.setArtifactMode(ArtifactModeValue.COMPLETE);
			genericArtifact.setArtifactType(ArtifactTypeValue.PLAINARTIFACT);
			genericArtifact
					.setIncludesFieldMetaData(IncludesFieldMetaDataValue.TRUE);

			TrackerFieldDO[] trackerFields = soapClient
					.getFlexFields(trackerId);

			// write schema for system defined and configurable fields
			createGenericArtifactField(
					TFArtifactMetaData.SFEEFields.actualHours,
					genericArtifact, null);
			createGenericArtifactField(
					TFArtifactMetaData.SFEEFields.estimatedHours,
					genericArtifact, null);
			if (soapClient.supports53()) {
				createGenericArtifactField(
						TFArtifactMetaData.SFEEFields.remainingEffort,
						genericArtifact, null);
				createGenericArtifactField(
						TFArtifactMetaData.SFEEFields.planningFolder,
						genericArtifact, null);
				createGenericArtifactField(
						TFArtifactMetaData.SFEEFields.autosumming,
						genericArtifact, getSupportedFieldValues(trackerFields,
								TFArtifactMetaData.SFEEFields.autosumming
										.getFieldName()));
			}
			
			if (soapClient.supports54()) {
				createGenericArtifactField(TFArtifactMetaData.SFEEFields.points,
						genericArtifact, null);
			}

			createGenericArtifactField(
					TFArtifactMetaData.SFEEFields.assignedTo, genericArtifact,
					null);
			createGenericArtifactField(TFArtifactMetaData.SFEEFields.category,
					genericArtifact, getSupportedFieldValues(trackerFields,
							TFArtifactMetaData.SFEEFields.category
									.getFieldName()));
			createGenericArtifactField(TFArtifactMetaData.SFEEFields.closeDate,
					genericArtifact, null);
			createGenericArtifactField(TFArtifactMetaData.SFEEFields.customer,
					genericArtifact, getSupportedFieldValues(trackerFields,
							TFArtifactMetaData.SFEEFields.customer
									.getFieldName()));
			createGenericArtifactField(
					TFArtifactMetaData.SFEEFields.description, genericArtifact,
					null);

			createGenericArtifactField(TFArtifactMetaData.SFEEFields.group,
					genericArtifact, getSupportedFieldValues(trackerFields,
							TFArtifactMetaData.SFEEFields.group.getFieldName()));

			createGenericArtifactField(TFArtifactMetaData.SFEEFields.priority,
					genericArtifact, getPriorityFieldValues());
			createGenericArtifactField(
					TFArtifactMetaData.SFEEFields.reportedReleaseId,
					genericArtifact, null);
			createGenericArtifactField(
					TFArtifactMetaData.SFEEFields.resolvedReleaseId,
					genericArtifact, null);
			createGenericArtifactField(TFArtifactMetaData.SFEEFields.status, genericArtifact, getSupportedFieldValues(
							trackerFields, TFArtifactMetaData.SFEEFields.status
									.getFieldName()));
			createGenericArtifactField(
					TFArtifactMetaData.SFEEFields.statusClass, genericArtifact,
					null);
			createGenericArtifactField(TFArtifactMetaData.SFEEFields.folderId,
					genericArtifact, null);
			createGenericArtifactField(TFArtifactMetaData.SFEEFields.path,
					genericArtifact, null);
			createGenericArtifactField(TFArtifactMetaData.SFEEFields.title,
					genericArtifact, null);
			createGenericArtifactField(TFArtifactMetaData.SFEEFields.createdBy,
					genericArtifact, null);
			createGenericArtifactField(
					TFArtifactMetaData.SFEEFields.createdDate, genericArtifact,
					null);
			createGenericArtifactField(TFArtifactMetaData.SFEEFields.id,
					genericArtifact, null);
			createGenericArtifactField(
					TFArtifactMetaData.SFEEFields.lastModifiedBy,
					genericArtifact, null);
			createGenericArtifactField(
					TFArtifactMetaData.SFEEFields.lastModifiedDate,
					genericArtifact, null);
			createGenericArtifactField(TFArtifactMetaData.SFEEFields.version,
					genericArtifact, null);

			// we manually have to add the comment text field (defined as flex
			// field)
			GenericArtifactField commentField = genericArtifact.addNewField(
					SFEEFields.commentText.getFieldName(),
					GenericArtifactField.VALUE_FIELD_TYPE_FLEX_FIELD);
			commentField
					.setFieldAction(GenericArtifactField.FieldActionValue.REPLACE);
			commentField.setFieldValueType(SFEEFields.commentText
					.getValueType());
			commentField.setAlternativeFieldName(fieldNameAmbiguityDissolver
					.generateNewFieldName(
							SFEEFields.commentText.getFieldName(), true));
			commentField.setMinOccurs(0);
			commentField.setMaxOccursValue(GenericArtifactField.UNBOUNDED);
			commentField.setNullValueSupported(SFEEFields.commentText
					.isNullValueSupported().toString());
			commentField
					.setFieldValue("This field contains the comments of TF.");

			for (TrackerFieldDO trackerFieldSoapDO : trackerFields) {
				String fieldName = trackerFieldSoapDO.getName();
				/**
				 * We have to find out whether this field is a custom field (and
				 * not a flex field) If so, we have already added it to the
				 * schema
				 */
				SFEEFields fieldConfig = null;
				try {
					fieldConfig = SFEEFields.valueOf(fieldName);
				} catch (IllegalArgumentException e) {
				}

				if (null != fieldConfig
						&& FIELD_TYPE.CONFIGURABLE == fieldConfig.getFieldType()
						&& !fieldName.equals(SFEEFields.reportedReleaseId
								.getFieldName())
						&& !fieldName.equals(SFEEFields.resolvedReleaseId
								.getFieldName())) {
					continue;
				}

				// we have to filter flex fields with the name of the comment
				// field and the alternate names of the release field
				// TODO Change comment field to mandatory field in the future
				if (fieldName.equals(SFEEFields.commentText.getFieldName())
						|| fieldName.equals(SFEEFields.reportedReleaseId
								.getAlternateName())
						|| fieldName.equals(SFEEFields.resolvedReleaseId
								.getAlternateName())) {
					continue;
				}

				TrackerFieldValueDO[] fieldValues = trackerFieldSoapDO
						.getFieldValues();

				GenericArtifactField.FieldValueTypeValue fieldValueType = TFArtifactMetaData
						.getFieldValueTypeForFieldType(trackerFieldSoapDO
								.getValueType());
				GenericArtifactField field = genericArtifact.addNewField(
						trackerFieldSoapDO.getName(),
						GenericArtifactField.VALUE_FIELD_TYPE_FLEX_FIELD);
				field
						.setFieldAction(GenericArtifactField.FieldActionValue.REPLACE);
				field.setFieldValueType(fieldValueType);
				String alternativeFieldName = fieldNameAmbiguityDissolver
						.generateNewFieldName(trackerFieldSoapDO.getName(),
								true);
				field.setAlternativeFieldName(alternativeFieldName);
				field.setMinOccurs(0);
				String fieldType = trackerFieldSoapDO.getFieldType();
				if (fieldType.equals(TrackerFieldDO.FIELD_TYPE_MULTISELECT)) {
					field.setMaxOccurs(fieldValues.length);
				} else if (fieldType
						.equals(TrackerFieldDO.FIELD_TYPE_MULTISELECT_USER)) {
					field.setMaxOccursValue(GenericArtifactField.UNBOUNDED);
				} else {
					field.setMaxOccurs(1);
				}
				if (fieldType.equals(TrackerFieldDO.FIELD_TYPE_DATE)) {
					field.setNullValueSupported("true");
				} else {
					field.setNullValueSupported("false");
				}

				field.setFieldValue(generateFieldDocumentation(fieldName,
						alternativeFieldName, fieldValueType,
						GenericArtifactField.VALUE_FIELD_TYPE_FLEX_FIELD,
						"false", fieldValues));
			}

			/*
			 * TrackerFieldValueSoapDO[] statusValues = getSupportedFieldValues(
			 * fieldMetaData, ArtifactSoapDO.COLUMN_STATUS); if (statusValues ==
			 * null) { throw new RemoteException(
			 * "Could not find status field for tracker " + tracker); }
			 * documentationBuffer
			 * .append("\n\nTracker Statuses (with status group in brackets): "
			 * );
			 * 
			 * for (int i = 0; i < statusValues.length; ++i) {
			 * documentationBuffer.append("\n	" + statusValues[i].getValue() +
			 * " (" + statusValues[i].getValueClass() + ")"); //
			 * statusValues[i].getIsDefault() }
			 * 
			 * documentationBuffer
			 * .append("\n\nTracker Fields (with value type in brackets): "); //
			 * run through all the fields to retrieve their names for (int i =
			 * 0; i < fieldMetaData.length; ++i) {
			 * documentationBuffer.append("\n	" + fieldMetaData[i].getName() +
			 * " (" + fieldMetaData[i].getValueType() + ")");
			 * 
			 * fieldMetaData[i].getDisabled() fieldMetaData[i].getFieldType()
			 * fieldMetaData[i].getId() fieldMetaData[i].getVersion()
			 * fieldMetaData[i].getRequired()
			 * 
			 * }
			 */

		} catch (IOException e) {
			throw new CCFRuntimeException(
					"An IOError occured while trying to connect to TF: "
							+ e.getMessage(), e);
		}
		return genericArtifact;
	}

	/**
	 * Searches for the specified field and its supported values
	 * 
	 * @param fieldName
	 *            field name in question
	 * @trackerFields field meta information
	 * @return possible field values or null if field does not exist
	 * @throws RemoteException
	 *             thrown if an errors occurs within SFEE
	 */
	private IFieldValueDO[] getSupportedFieldValues(
			TrackerFieldDO[] trackerFields, String fieldName) {
		// TODO: Decide between flex fields and mandatory fields with same name
		for (TrackerFieldDO trackerFieldSoapDO : trackerFields) {
			if (trackerFieldSoapDO.getName().equals(fieldName))
				return trackerFieldSoapDO.getFieldValues();
		}
		return null;
	}

}
