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

import java.util.HashMap;

import com.collabnet.ccf.core.GenericArtifactField;
import com.collabnet.ccf.core.GenericArtifactField.FieldValueTypeValue;
import com.collabnet.teamforge.api.tracker.TrackerFieldDO;

public class TFArtifactMetaData {

	private static final HashMap<String, GenericArtifactField.FieldValueTypeValue>
			fieldValueTypeGAFieldTypeMap = new HashMap<String, GenericArtifactField.FieldValueTypeValue>();
	private static final HashMap<GenericArtifactField.FieldValueTypeValue, String>
			fieldGAValueTypeFieldTypeMap = new HashMap<GenericArtifactField.FieldValueTypeValue, String>();
	static {
		fieldValueTypeGAFieldTypeMap.put(TrackerFieldDO.FIELD_VALUE_TYPE_DATE,
				GenericArtifactField.FieldValueTypeValue.DATE);
		fieldValueTypeGAFieldTypeMap.put(TrackerFieldDO.FIELD_VALUE_TYPE_INTEGER,
				GenericArtifactField.FieldValueTypeValue.INTEGER);
		fieldValueTypeGAFieldTypeMap.put(TrackerFieldDO.FIELD_VALUE_TYPE_STRING,
				GenericArtifactField.FieldValueTypeValue.STRING);
		fieldValueTypeGAFieldTypeMap.put(TrackerFieldDO.FIELD_VALUE_TYPE_USER,
				GenericArtifactField.FieldValueTypeValue.USER);
		fieldValueTypeGAFieldTypeMap.put("SfUser",
				GenericArtifactField.FieldValueTypeValue.USER);
		fieldValueTypeGAFieldTypeMap.put("Boolean",
				GenericArtifactField.FieldValueTypeValue.BOOLEAN);
		fieldValueTypeGAFieldTypeMap.put("FolderPath",
				GenericArtifactField.FieldValueTypeValue.STRING);
	}
	static {
		fieldGAValueTypeFieldTypeMap.put(GenericArtifactField.FieldValueTypeValue.DATE,
				TrackerFieldDO.FIELD_VALUE_TYPE_DATE);
		fieldGAValueTypeFieldTypeMap.put(GenericArtifactField.FieldValueTypeValue.DATETIME,
				TrackerFieldDO.FIELD_VALUE_TYPE_DATE);
		fieldGAValueTypeFieldTypeMap.put(GenericArtifactField.FieldValueTypeValue.INTEGER,
				TrackerFieldDO.FIELD_VALUE_TYPE_INTEGER);
		fieldGAValueTypeFieldTypeMap.put(GenericArtifactField.FieldValueTypeValue.STRING,
				TrackerFieldDO.FIELD_VALUE_TYPE_STRING);
		fieldGAValueTypeFieldTypeMap.put(GenericArtifactField.FieldValueTypeValue.DOUBLE,
				TrackerFieldDO.FIELD_VALUE_TYPE_STRING);
		fieldGAValueTypeFieldTypeMap.put(GenericArtifactField.FieldValueTypeValue.USER,
				TrackerFieldDO.FIELD_VALUE_TYPE_USER);
		fieldGAValueTypeFieldTypeMap.put(GenericArtifactField.FieldValueTypeValue.HTMLSTRING,
				TrackerFieldDO.FIELD_VALUE_TYPE_STRING);
		fieldGAValueTypeFieldTypeMap.put(GenericArtifactField.FieldValueTypeValue.BASE64STRING,
				TrackerFieldDO.FIELD_VALUE_TYPE_STRING);
		fieldGAValueTypeFieldTypeMap.put(GenericArtifactField.FieldValueTypeValue.BOOLEAN,
				TrackerFieldDO.FIELD_VALUE_TYPE_STRING);
	}

	public enum FIELD_TYPE {
		SYSTEM_DEFINED,
		CONFIGURABLE,
		USER_DEFINED /* Flex fields */
	}

	public enum SFEEFields {
		id("id", "ID", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.STRING, true,"",false),
		actualHours("actualHours", "Actual hours", FIELD_TYPE.CONFIGURABLE,GenericArtifactField.FieldValueTypeValue.INTEGER, false,"",false),
		actualEffort("actualEffort", "Actual Effort", FIELD_TYPE.CONFIGURABLE,GenericArtifactField.FieldValueTypeValue.INTEGER, false,"",false),
		estimatedEffort("estimatedEffort", "Estimated Effort", FIELD_TYPE.CONFIGURABLE,GenericArtifactField.FieldValueTypeValue.INTEGER, false,"",false),
		remainingEffort("remainingEffort", "Remaining Effort", FIELD_TYPE.CONFIGURABLE,GenericArtifactField.FieldValueTypeValue.INTEGER, false,"",false),
		autosumming("autosumming", "Calculate Effort", FIELD_TYPE.CONFIGURABLE,GenericArtifactField.FieldValueTypeValue.BOOLEAN, false,"",false),
		planningFolder("planningFolder", "Planning Folder", FIELD_TYPE.CONFIGURABLE,GenericArtifactField.FieldValueTypeValue.STRING, false,"",true),
		assignedTo("assignedTo", "Assigned to",FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.USER, false,"",true),
		lastModifiedBy("lastModifiedBy", "Last modified by", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.USER, false,"",false),
		createdBy("createdBy", "Created by", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.USER, false,"",false),
		folderId("folderId", "Folder id",FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.STRING, false,"",false),
		projectId("projectId", "Project id",FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.STRING, false,"",false),
		parentFolderId("parentFolderId", "Parent folder id",FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.STRING, false,"",true),
		version("version", "Version", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.STRING, false,"",false),
		title("title", "Title", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.STRING, true,"",false),
		path("path", "Path", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.STRING, false,"",false),
		category("category", "Category", FIELD_TYPE.CONFIGURABLE, GenericArtifactField.FieldValueTypeValue.STRING, false,"",true),
		/* This field is not set by the user. But TF automatically sets it when the state
		 * changes to CLOSED
		 * */
		closeDate("closeDate", "Close date", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.DATETIME, false,"",true),
		createdDate("createdDate", "Created date", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.DATETIME, false,"",false),
		startDate("startDate", "Start date", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.DATE, false,"",true),
		endDate("endDate", "End date", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.DATE, false,"",true),
		lastModifiedDate("lastModifiedDate", "Last modified date", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.DATETIME, false,"",false),
		customer("customer", "Customer", FIELD_TYPE.CONFIGURABLE, GenericArtifactField.FieldValueTypeValue.STRING, false,"", false),
		description("description", "Description", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.STRING, true,"",false),
		estimatedHours("estimatedHours", "Estimated hours", FIELD_TYPE.CONFIGURABLE, GenericArtifactField.FieldValueTypeValue.INTEGER, false,"",false),
		//flexFields(),
		group("group", "Group", FIELD_TYPE.CONFIGURABLE, GenericArtifactField.FieldValueTypeValue.STRING, false,"",false),
		priority("priority", "Priority", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.INTEGER, false,"",false),
		reportedReleaseId("reportedReleaseId", "Reported in release", FIELD_TYPE.CONFIGURABLE, GenericArtifactField.FieldValueTypeValue.STRING, false,"reportedInRelease",true),
		resolvedReleaseId("resolvedReleaseId", "Resolved in release", FIELD_TYPE.CONFIGURABLE, GenericArtifactField.FieldValueTypeValue.STRING, false,"resolvedInRelease",true),
		status("status", "Status", FIELD_TYPE.CONFIGURABLE, GenericArtifactField.FieldValueTypeValue.STRING, true,"",false),
		statusClass("statusClass", "Status class", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.STRING, false,"",false),
		commentText("Comment Text", "Comment Text", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.STRING, false,"",false),
		//FIXME: this is just guessing.
		points("points", "Story Points", FIELD_TYPE.CONFIGURABLE, GenericArtifactField.FieldValueTypeValue.INTEGER, false,"", true),
		capacity("capacity", "Capacity", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.INTEGER, false,"", true),
		releaseId("releaseId", "Release ID", FIELD_TYPE.SYSTEM_DEFINED, GenericArtifactField.FieldValueTypeValue.STRING, false, "", true),
		;

		private String fieldName;
		private String displayName;
		private FIELD_TYPE fieldType;
		private GenericArtifactField.FieldValueTypeValue valueType;
		private boolean required;
		private String alternateName;
		private Boolean nullValueSupported;
		private SFEEFields(String fieldName,
				String displayName,
				FIELD_TYPE fieldType,
				GenericArtifactField.FieldValueTypeValue valueType,
				boolean required, String alternateName, Boolean nullValueSupported){
			this.fieldName = fieldName;
			this.displayName = displayName;
			this.fieldType = fieldType;
			this.valueType = valueType;
			this.required = required;
			this.alternateName = alternateName;
			this.nullValueSupported=nullValueSupported;
		}
		
		public Boolean isNullValueSupported() {
			return nullValueSupported;
		}
		
		public String getFieldName() {
			return fieldName;
		}
		public String getDisplayName(){
			return displayName;
		}
		public FIELD_TYPE getFieldType() {
			return fieldType;
		}
		public GenericArtifactField.FieldValueTypeValue getValueType() {
			return valueType;
		}
		public boolean isRequired() {
			return required;
		}
		public String getAlternateName() {
			return alternateName;
		}
	}

	public static FieldValueTypeValue getFieldValueType(String fieldName) {
		SFEEFields field = null;
		try{
			field = SFEEFields.valueOf(fieldName);
		}
		catch(IllegalArgumentException e){
			// log.warn("Field "+fieldName+" is not found in ArtifactMetaData");
		}
		if(fieldName.equals(SFEEFields.commentText.getFieldName())){
			field = SFEEFields.commentText;
		}
		if(null != field){
			return field.getValueType();
		}
		return null;
	}

	public static FieldValueTypeValue getFieldValueTypeForFieldType(String fieldType) {
		FieldValueTypeValue fieldValueType = fieldValueTypeGAFieldTypeMap.get(fieldType);
		if(null != fieldValueType){
			return fieldValueType;
		}
		else {
			// log.error("No type found for " + fieldType);
		}
		return null;
	}

	public static String getSFEEFieldValueTypeForGAFieldType(FieldValueTypeValue fieldType) {
		String fieldValueType = fieldGAValueTypeFieldTypeMap.get(fieldType);
		if(null != fieldValueType){
			return fieldValueType;
		}
		else {
			// log.error("No TF type found for GA type " + fieldType);
		}
		return null;
	}

	/*public static void setDateFieldValue(String fieldName, Object value,
			String sourceSystemTimezone, GenericArtifactField field){
		Date dateValue = null;
		if(value instanceof GregorianCalendar){
			dateValue = ((GregorianCalendar)value).getTime();
		}
		else if(value instanceof Date){
			dateValue = (Date) value;
		}
		if(value instanceof String){
			long dataValue = Long.parseLong((String) value)*1000;
			Date returnDate = new Date(dataValue);
			dateValue = returnDate;
		}
		if(DateUtil.isAbsoluteDateInTimezone(dateValue, sourceSystemTimezone)){
			dateValue = DateUtil.convertToGMTAbsoluteDate(dateValue, sourceSystemTimezone);
			field.setFieldValue(dateValue);
			field.setFieldValueType(GenericArtifactField.FieldValueTypeValue.DATE);
		}
		else {
			field.setFieldValueType(GenericArtifactField.FieldValueTypeValue.DATETIME);
			field.setFieldValue(dateValue);
		}
	}*/
}
