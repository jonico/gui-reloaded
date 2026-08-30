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
package com.collabnet.ccf.qc.schemageneration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.collabnet.ccf.core.CCFRuntimeException;
import com.collabnet.ccf.core.GenericArtifact;
import com.collabnet.ccf.core.GenericArtifactField;
import com.collabnet.ccf.core.GenericArtifact.ArtifactActionValue;
import com.collabnet.ccf.core.GenericArtifact.ArtifactModeValue;
import com.collabnet.ccf.core.GenericArtifact.ArtifactTypeValue;
import com.collabnet.ccf.core.GenericArtifact.IncludesFieldMetaDataValue;
import com.collabnet.ccf.core.GenericArtifactField.FieldValueTypeValue;
import com.collabnet.ccf.pi.qc.v90.api.ICommand;
import com.collabnet.ccf.pi.qc.v90.api.IConnection;
import com.collabnet.ccf.pi.qc.v90.api.IRecordSet;
import com.collabnet.ccf.pi.qc.v90.api.dcom.Connection;
import com.collabnet.ccf.schemageneration.RepositoryLayoutExtractor;

/**
 * The QCSchemaGenerator is responsible for generating a generic artifact that
 * fully describes the schema of a repository
 * 
 * @author jnicolai
 * 
 */
public class QCLayoutExtractor implements RepositoryLayoutExtractor {

	private static final String HUMAN_READABLE_SUFFIX = "_HUMAN_READABLE";
	static final String sfJoinTable = "SF_REFERENCE_TABLE";
	static final String sfJoinRowKey = "SF_REFERENCE_ID_COLUMN";
	static final String sfJoinRowDisplayName = "SF_REFERENCE_NAME_COLUMN";
	static final String sfJoinParentTable = "SF_TABLE_NAME";
	private static final int HIGHEST_KNOWN_QC_9_0_BUILD_NUMBER = 4434;

	/**
	 * Describe which columns in a child table are joined to which columns in a
	 * parent table in the internal QC DB. The join is always done from the
	 * parent to the child via an 'ID' column. But in the QC GUI (or in CCF) the
	 * user only deals with human-readable 'display' columns
	 */
	static class JoinedField {
		private String m_parentTable;
		private String m_parentCol;
		private String m_childTable;
		private String m_childIdCol;
		private String m_childDisplayCol;

		JoinedField(String parentTable, String parentCol, String childTable,
				String childIdCol, String childDisplayCol) {
			m_parentTable = parentTable;
			m_parentCol = parentCol;
			m_childTable = childTable;
			m_childIdCol = childIdCol;
			m_childDisplayCol = childDisplayCol;

			if (null == m_parentTable)
				throw new IllegalArgumentException(
						"Parent table name for joined field can't be null");
			if (null == m_parentCol)
				throw new IllegalArgumentException(
						"Parent column name for joined field can't be null");
			if (null == m_childTable)
				throw new IllegalArgumentException(
						"Child table name of joined field can't be null");
			if (null == m_childIdCol)
				throw new IllegalArgumentException(
						"ID column of joined field can't be null");
			if (null == m_childDisplayCol)
				throw new IllegalArgumentException(
						"Display column of joined field can't be null");
		}

		String parentTable() {
			return m_parentTable;
		}

		String parentCol() {
			return m_parentCol;
		}

		String childTable() {
			return m_childTable;
		}

		String childIdCol() {
			return m_childIdCol;
		}

		String childDisplayCol() {
			return m_childDisplayCol;
		}
	}

	/** Contains info on ALL joined columns in ALL tables. */
	private static ArrayList<JoinedField> s_JoinedFields;

	/**
	 * Fill the s_JoinedFields list, which contains info on all joined (a.k.a
	 * 'referenced') fields in all tables. The info in s_JoinedFields describes
	 * how the join is performed (from which parent to which child cols) (e.g.
	 * BUG.BG_DETECTED_IN_REL) Assume this information can not change while HPQC
	 * is running; cache the result so we don't have to repeatedly to SQL
	 * queries
	 */
	private static synchronized ArrayList<JoinedField> queryAllJoinedFields(
			IConnection qcc) {
		final String sql = "SELECT "
			+ sfColumnName + ", "
			+ sfJoinParentTable + ", "
			+ sfJoinTable + ", "
			+ sfJoinRowKey + ", "
			+ sfJoinRowDisplayName
			+" FROM SYSTEM_FIELD WHERE (SF_TABLE_NAME='BUG' OR SF_TABLE_NAME='REQ') AND SF_REFERENCE_TABLE IS NOT NULL";

		ArrayList<JoinedField> joinedFields = s_JoinedFields;
		if (null == s_JoinedFields) {
			// we're changing a static member: make sure only 1 thread at a
			// time
			joinedFields = new ArrayList<JoinedField>();
			IRecordSet rs = null;
			try {
				rs = executeSQL(qcc, sql);
				int rc = rs.getRecordCount();
				for (int cnt = 0; cnt < rc; cnt++, rs.next()) {
					String colName = rs.getFieldValueAsString(sfColumnName);
					String tableName = rs.getFieldValueAsString(sfJoinParentTable);
					String joinTableName = rs.getFieldValueAsString(sfJoinTable);
					String joinRowKey = rs.getFieldValueAsString(sfJoinRowKey);
					String joinRowDisplayName = rs.getFieldValueAsString(sfJoinRowDisplayName);
					joinedFields.add(new JoinedField(tableName, colName,
							joinTableName, joinRowKey, joinRowDisplayName));
					if ("BG_TARGET_REL".equals(colName)
							|| "BG_TARGET_RCYC".equals(colName)) {
						// special case: add corresponding RQ joined fields,
						// because QC doesn't populate
						// the columns for RQ_TARGET_*.
						colName = colName.replaceFirst("BG", "RQ");
						joinedFields.add(new JoinedField("REQ", colName,
								joinTableName, joinRowKey, joinRowDisplayName));
					}
				}
				s_JoinedFields = joinedFields;
			} finally {
				if (null != rs) {
					rs.safeRelease();
				}
			}
		}
		return joinedFields;
	}

	static List<String> getJoinedFieldDisplayNames(IConnection qcc,boolean isDefect, String fieldName) {
		String parentTable = isDefect ? "BUG" : "REQ";
		List<String> result = new ArrayList<String>();
		IRecordSet rs = null;
		try {
			JoinedField f = getJoinedField(qcc, parentTable, fieldName);
			if (null == f) {
				throw new IllegalArgumentException("Not a joined field: \""
						+ String.valueOf(parentTable) + "\".\""
						+ String.valueOf(fieldName) + "\"");
			}

			String sql = "SELECT " + f.childDisplayCol() + " FROM "
					+ f.childTable();
			rs = executeSQL(qcc, sql);
			//String id = rs.getFieldValueAsString(f.childIdCol());
			if (null != rs)  {
				int count = rs.getRecordCount();
				for (int i = 0; i < count; ++i, rs.next()) {
					result.add(rs.getFieldValueAsString(f.childDisplayCol()));
				}
			}
		} finally {
			if (null != rs)
				rs.safeRelease();
		}
		return result;
	}
	
	/**
	 * Map the 'displayed value' of a joined column to the actual primary-key-id
	 * of the child table. In the QC gui, you always see a 'display value', i.e.
	 * something human-readable like a string. This is the same when using CCF -
	 * the foreign (exporting) system will not know the internal primary key
	 * values, but only the 'display value'. But the internal QC database record
	 * actually uses a join (FK) column containing the primary-key value of the
	 * child record. When writing a QC record (Defect, Requirement, etc.) you
	 * need this primary-key value.
	 * 
	 * @param qcc
	 *            QualityCenter OTA connection
	 * @param parentTableName
	 *            the table the joined column is in
	 * @param parentCol
	 *            the name of the column in the parent table
	 * @param parentDisplayColValue
	 *            the value that is 'displayed' in the QC gui
	 * @return the primary key value in the child table corresponding to
	 *         parentDisplayColValue
	 */
	static String mapJoinedFieldDisplayToId(IConnection qcc,
			boolean isDefect, String parentCol, String displayValue) {
		String parentTable = isDefect ? "BUG" : "REQ";
		String id = null;
		if (null != displayValue) {
			IRecordSet rs = null;
			try {
				JoinedField f = getJoinedField(qcc, parentTable, parentCol);
				if (null == f) {
					throw new IllegalArgumentException("Not a joined field: \""
							+ String.valueOf(parentTable) + "\".\""
							+ String.valueOf(parentCol) + "\"");
				}

				String sql = "SELECT " + f.childIdCol() + " FROM "
						+ f.childTable() + " WHERE " + f.childDisplayCol()
						+ " = '" + displayValue + "'";
				rs = executeSQL(qcc, sql);
				id = rs.getFieldValueAsString(f.childIdCol());
			} finally {
				if (null != rs)
					rs.safeRelease();
			}
		}

		if (null == id) {
			String msg = new StringBuilder(
					"Invalid value for Quality Center field ").append(
					parentTable).append(".").append(parentCol).append(": ")
					.append(displayValue).toString();
			//log.error(msg);
			throw new CCFRuntimeException(msg);
		}

		return id;
	}

	/**
	 * Get the JoinedField corresponding to the given (parent) table and column
	 * name
	 */
	static JoinedField getJoinedField(IConnection qcc, String parentTableName,
			String parentColName) {
		JoinedField jf = null;
		for (JoinedField f : queryAllJoinedFields(qcc)) {
			if (f.parentTable().equals(parentTableName)
					&& f.parentCol().equals(parentColName)) {
				jf = f;
				break;
			}
		}
		return jf;
	}

	/**
	 * Certain fields are not directly contained in a given COM object/DB
	 * record; instead their values are obtained by JOINing a row from another
	 * table. These are also known as 'referenced' fields
	 * 
	 * @param tableName
	 *            name of the (parent) table containing joined fields
	 * @param fieldName
	 *            name of the column
	 * @return true if the field is not a simple scalar but instead a
	 *         'referenced' field
	 */
	static boolean isJoinedField(IConnection qcc, String tableName,
			String fieldName) {
		if ("9".equals(qcc.getMajorVersion()) && HIGHEST_KNOWN_QC_9_0_BUILD_NUMBER >= Integer.parseInt(qcc.getMinorVersion())) {
			return false;
		}
		boolean isRField = false;
		if (null != fieldName && null != tableName) {
			JoinedField jf = getJoinedField(qcc, tableName, fieldName);
			isRField = jf != null;
		}
		return isRField;
	}
	
	static boolean isJoinedField(IConnection qcc, boolean isDefect, String fieldName) {
		return isJoinedField(qcc, isDefect ? "BUG" : "REQ", fieldName);
	}
	
	private static final String RQ_DEV_COMMENTS = "RQ_DEV_COMMENTS";
	private static final String BG_DEV_COMMENTS = "BG_DEV_COMMENTS";
	static final String sfColumnName = "SF_COLUMN_NAME";
	static final String sfColumnType = "SF_COLUMN_TYPE";
	static final String sfUserLabel = "SF_USER_LABEL";
	static final String sfEditStyle = "SF_EDIT_STYLE";
	static final String sfIsMultiValue = "SF_IS_MULTIVALUE";

	static final String sfRootId = "SF_ROOT_ID";
	static final String alDescription = "AL_DESCRIPTION";
	static final String usUsername = "US_USERNAME";

	static final String sfListComboValue = "ListCombo";
	static final String sfUserComboValue = "UserCombo";

	static final String numberDataType = "number";
	static final String charDataType = "char";
	static final String memoDataType = "memo";
	static final String dateDataType = "DATE";

	static final String bgBugIdFieldName = "BG_BUG_ID";
	static final String auActionIdFieldName = "AU_ACTION_ID";
	static final String apFieldNameFieldName = "AP_FIELD_NAME";

	static final String apOldValueFieldName = "AP_OLD_VALUE";
	static final String apOldLongValueFieldName = "AP_OLD_LONG_VALUE";

	static final String CARDINALITY_GREATER_THAN_ONE = "GT1";
	
	static final String lastModifiedUserFieldName = "LAST_MODIFIED_USER";
	public static final String PARAM_DELIMITER = "-";

	private String serverUrl;
	private String userName;
	private String password;
	private boolean comInitialized = false;
	
	/**
	 * This data structure maps the repository id to the corresponding requirements type's technical id
	 */
	static Map <String, String> repositoryIdToTechnicalRequirementsTypeIdMap = new HashMap<String, String>();

	private void closeConnection(IConnection connection) {
		try {
			connection.logout();
			connection.disconnect();
		} catch (Exception e) {
			// log.warn(
			// "Could not close QC connection. So releasing the Connection COM dispatch"
			// , e);
		}
		connection.safeRelease();
		connection = null;
	}

	private void initCOM() {
		if (!comInitialized) {
			ComHandle.initCOM();
			comInitialized = true;
		}
	}

	private void tearDownCOM() {
		if (comInitialized) {
			ComHandle.tearDownCOM();
			comInitialized = false;
		}
	}

	private IConnection createConnection(String serverUrl, String repositoryId,
			String username, String password) {
		String domain = null;
		String project = null;
		if (null != repositoryId) {
			String[] splitRepoId = repositoryId.split(PARAM_DELIMITER);
			if (null != splitRepoId) {
				if (2 == splitRepoId.length || 3 == splitRepoId.length) {
					domain = splitRepoId[0];
					project = splitRepoId[1];
				} else {
					throw new IllegalArgumentException("Repository Id "
							+ repositoryId + " is invalid.");
				}
			}
		} else {
			throw new IllegalArgumentException("Repository Id cannot be null");
		}

		IConnection connection = new Connection(serverUrl, domain, project,
				username, password);
		return connection;
	}

	/**
	 * Returns the server URL of the source HP QC system that is configured in
	 * the wiring file.
	 * 
	 * @return
	 */
	public String getServerUrl() {
		return serverUrl;
	}

	/**
	 * Sets the source HP QC system's server URL.
	 * 
	 * @param serverUrl
	 *            - the URL of the source HP QC system.
	 */
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
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
	 * Sets the mandatory username
	 * 
	 * The user name is used to login into the HP QC instance whenever an
	 * artifact should be updated or extracted. This user has to differ from the
	 * resync user in order to force initial resyncs with the source system once
	 * a new artifact has been created.
	 * 
	 * @param userName
	 *            the user name to set
	 */
	public void setUserName(String username) {
		this.userName = username;
	}

	public GenericArtifact getProjectSchema(String repositoryId) {
		IConnection qcConnection = null;
		try {
			initCOM();
			qcConnection = createConnection(serverUrl, repositoryId, userName,
					password);
			if (isDefectRepository(repositoryId)) {
				return getSchemaFieldsForDefect(qcConnection);
			} else {
				String technicalReleaseId = extractTechnicalRequirementsType(repositoryId, qcConnection);
				return getSchemaFieldsForRequirement(qcConnection, technicalReleaseId);
			}
		} finally {
			if (null != qcConnection) {
				closeConnection(qcConnection);
			}
			tearDownCOM();
		}
	}
	
	/**
	 * Validates, whether given QC domain and project (will throw an exception otherwise) 
	 * @param domain
	 * @param project
	 */
	public void validateQCDomainAndProject(String domain, String project) {
		IConnection qcConnection = null;
		try {
			initCOM();
			qcConnection = createConnection(serverUrl, domain + PARAM_DELIMITER + project, userName,
					password);
		} finally {
			if (null != qcConnection) {
				closeConnection(qcConnection);
			}
		}
	}
	
	/**
	 * Get the available requirement types
	 * @param domain
	 * @param project
	 * @return list of available requirement types
	 */
	public List<String> getRequirementTypes(String domain, String project) {
		List<String> requirementTypes = new ArrayList<String>();
		IConnection qcConnection = null;
		try {
			initCOM();
			qcConnection = createConnection(serverUrl, domain + PARAM_DELIMITER + project, userName,
					password);
			String sql = "SELECT TPR_NAME FROM REQ_TYPE";
			IRecordSet rs = null;
			try {
				rs = executeSQL(qcConnection, sql);
				int rc = rs.getRecordCount();
				for (int cnt = 0; cnt < rc; cnt++, rs.next()) {
					String reqType = rs.getFieldValueAsString("TPR_NAME");
					requirementTypes.add(reqType);
				}
			} finally {
				if (null != rs) {
					rs.safeRelease();
					rs = null;
				}
			}
		} finally {
			if (null != qcConnection) {
				closeConnection(qcConnection);
			}		
			// tearDownCOM();
		}
		return requirementTypes;
	}
	
	public static GenericArtifact getSchemaFieldsForRequirement(IConnection qcc,
			String technicalReleaseTypeId) {

		// Get all the fields in the project represented
		// by qcc
		String sql = "SELECT * FROM REQ_TYPE_FIELD rf, system_field sf where sf.sf_user_label is not null AND rf.rtf_type_id = '"
				+ technicalReleaseTypeId
				+ "' and rf.rtf_sf_column_name = sf.sf_column_name";
		GenericArtifact genericArtifact = null;
		IRecordSet rs = null;
		try {
			rs = executeSQL(qcc, sql);
			int rc = rs.getRecordCount();
			genericArtifact = new GenericArtifact();
			genericArtifact.setArtifactAction(ArtifactActionValue.CREATE);
			genericArtifact.setArtifactMode(ArtifactModeValue.COMPLETE);
			genericArtifact.setArtifactType(ArtifactTypeValue.PLAINARTIFACT);
			genericArtifact
					.setIncludesFieldMetaData(IncludesFieldMetaDataValue.TRUE);
			
			for (int cnt = 0; cnt < rc; cnt++, rs.next()) {
				String columnName = rs.getFieldValueAsString(sfColumnName);
				
				String columnType = rs.getFieldValueAsString(sfColumnType);

				String fieldDisplayName = rs.getFieldValueAsString(sfUserLabel);
				if (null == fieldDisplayName) {
					continue;
				}

				List<String> fieldValueOptions = new ArrayList<String>();
				
				String editStyle = rs.getFieldValueAsString(sfEditStyle);
				// String isMultiValue =
				// rs.getFieldValueAsString(sfIsMultiValue);
				GenericArtifactField field;
				
				boolean joinedField = isJoinedField(qcc, false, columnName);

				// obtain the GenericArtifactField datatype from the columnType
				// and editStyle
				GenericArtifactField.FieldValueTypeValue fieldValueType = convertQCDataTypeToGADatatype(
						columnType, editStyle, columnName);

				boolean isMultiSelectField = false;
				if (fieldValueType
						.equals(GenericArtifactField.FieldValueTypeValue.STRING)) {
					String isMultiValue = rs
							.getFieldValueAsString(sfIsMultiValue);
					
					String rootId = rs.getFieldValueAsString(sfRootId);
					addFieldOptionValue(qcc, rootId, fieldValueOptions);

					if (columnType.equals("char") && null != editStyle
							&& "Y".equals(isMultiValue)) {
						if (editStyle.equals("ListCombo")
								|| editStyle.equals("TreeCombo")
								|| editStyle.equals("ReqTreeCombo")) {
							isMultiSelectField = true;
						}
					}
				} else if(fieldValueType.equals(GenericArtifactField.FieldValueTypeValue.INTEGER)) {
					// these edit styles are used for RQ_TARGET_* fields and allow multi-select,
					// even though the column sfIsMultiValue is set to "N".
					isMultiSelectField = "ReleaseCycleMultiTreeCombo".equals(editStyle) ||
										 "ReleaseMultiTreeCombo".equals(editStyle);
				}

				field = genericArtifact.addNewField(columnName,
						GenericArtifactField.VALUE_FIELD_TYPE_FLEX_FIELD);
				field.setFieldValueType(fieldValueType);
				field
						.setMaxOccursValue(isMultiSelectField ? GenericArtifactField.UNBOUNDED
								: "1");
				field.setMinOccurs(0);
				field.setNullValueSupported("true");
				field.setAlternativeFieldName(columnName);
				
				// special treatment for some fields (have to find out whether this is only due to the agile accelarator)
				if (columnName.equals("RQ_TYPE_ID")) {
					fieldValueType = FieldValueTypeValue.STRING;
					field.setFieldValueType(FieldValueTypeValue.STRING);
					fieldValueOptions = getJoinedFieldDisplayNames(qcc, false, columnName);
				}
				else if (columnName.equals("RQ_REQ_TIME")) {
					fieldValueType = FieldValueTypeValue.STRING;
					field.setFieldValueType(FieldValueTypeValue.STRING);
				}
				else if (columnName.equals("RQ_VC_CHECKOUT_TIME")) {
					fieldValueType = FieldValueTypeValue.STRING;
					field.setFieldValueType(FieldValueTypeValue.STRING);
				}
				if (columnName.equals(RQ_DEV_COMMENTS)) {
					field
						.setFieldAction(GenericArtifactField.FieldActionValue.APPEND);
				}
				else {
					field
						.setFieldAction(GenericArtifactField.FieldActionValue.REPLACE);
				}
				field.setFieldValue(generateFieldDocumentation(
						fieldDisplayName, fieldValueType, fieldValueOptions));
				
				if (joinedField && !"RQ_TYPE_ID".equals(columnName)) {
					// RQ_TYPE_ID field identifies itself as a joined field, but it really is a regular string.
					// generate a synthetic, "human readable" field for convenience.
					// NOTE: mapping this field may introduce ambiguities, when i.e. cycle names occur
					// multiple times.
					List<String> possibleFieldValues = getJoinedFieldDisplayNames(qcc, false, columnName);
					String humanReadableColumnName = columnName + HUMAN_READABLE_SUFFIX;
					fieldValueType = FieldValueTypeValue.STRING;
					GenericArtifactField humanReadableField = genericArtifact.addNewField(humanReadableColumnName, GenericArtifactField.VALUE_FIELD_TYPE_FLEX_FIELD);
					humanReadableField.setFieldValueType(fieldValueType);
					humanReadableField.setAlternativeFieldName(humanReadableColumnName);
					humanReadableField.setMaxOccursValue(field.getMaxOccursValue());
					humanReadableField.setMinOccurs(field.getMinOccurs());
					humanReadableField.setNullValueSupported(field.getNullValueSupported());
					humanReadableField.setFieldAction(GenericArtifactField.FieldActionValue.REPLACE);
					humanReadableField.setFieldValue(generateFieldDocumentation(
							fieldDisplayName + " [READ-ONLY]", fieldValueType, possibleFieldValues));

				}

			}
		} finally {
			if (null != rs) {
				rs.safeRelease();
				rs = null;
			}
		}

		// add last modified user as a mappable field
		GenericArtifactField lastModifiedByField = genericArtifact.addNewField(lastModifiedUserFieldName,
				GenericArtifactField.VALUE_FIELD_TYPE_FLEX_FIELD);
		lastModifiedByField.setFieldValueType(GenericArtifactField.FieldValueTypeValue.STRING);
		lastModifiedByField.setMaxOccursValue("1");
		lastModifiedByField.setMinOccurs(1);
		lastModifiedByField.setNullValueSupported("false");
		lastModifiedByField.setAlternativeFieldName(lastModifiedUserFieldName);
		lastModifiedByField.setFieldValue("user that performed the last modification");

		return genericArtifact;
	}
	
	/**
	 * Returns whether this repository id belongs to a defect repository
	 * If not, it belongs to a requirements type
	 * @param repositoryId repositoryId
	 * @return true if repository id belongs to a defect repository
	 */
	public static boolean isDefectRepository(String repositoryId) {
		String[] splitRepoId = repositoryId.split(PARAM_DELIMITER);
		if(null != splitRepoId){
			// we now also accept a double hyphen to synchronize requirement types as well
			if(2 == splitRepoId.length){
				return true;
			}
			else if (3 == splitRepoId.length) {
				return false;
			}
			else {
				throw new IllegalArgumentException("Repository Id "+repositoryId+" is invalid.");
			}
		}
		throw new IllegalArgumentException("Repository Id "+repositoryId+" is invalid.");
	}

	private static GenericArtifactField.FieldValueTypeValue convertQCDataTypeToGADatatype(String dataType, String editStyle, String columnName) {

		// TODO: Convert the datatype, editStyle pair to a valid GA type
		if(dataType.equals("char") && (null==editStyle || ( null!=editStyle && editStyle.equals(""))) )
			return GenericArtifactField.FieldValueTypeValue.STRING;
		if(dataType.equals("char") && ( null!=editStyle && editStyle.equals("UserCombo")) )
			return GenericArtifactField.FieldValueTypeValue.USER;
		if(dataType.equals("char") && ( null!=editStyle && editStyle.equals("DateCombo")) )
			return GenericArtifactField.FieldValueTypeValue.DATE;
		if(dataType.equals("char") && ( null!=editStyle && editStyle.equals("ListCombo")) ) {
			//if(isMultiValue.equals("N"))
				return GenericArtifactField.FieldValueTypeValue.STRING;
			//if(isMultiValue.equals("Y")) // MULTI_SELECT_LIST
			//	return GenericArtifactField.FieldValueTypeValue.STRING;
		}
		if(dataType.equals("memo"))
			return GenericArtifactField.FieldValueTypeValue.HTMLSTRING;
		if(dataType.equals("char") && ( null!=editStyle && editStyle.equals("TreeCombo")) ) {
			//if(isMultiValue.equals("N"))
				return GenericArtifactField.FieldValueTypeValue.STRING;
			//if(isMultiValue.equals("Y")) // MULTI_SELECT_LIST
			//	return GenericArtifactField.FieldValueTypeValue.STRING;
		}
		if(dataType.equals("char") && ( null!=editStyle && editStyle.equals("ReqTreeCombo")) ) {
			//if(isMultiValue.equals("N"))
				return GenericArtifactField.FieldValueTypeValue.STRING;
			//if(isMultiValue.equals("Y")) // MULTI_SELECT_LIST
			//	return GenericArtifactField.FieldValueTypeValue.STRING;
		}

		if(dataType.equals("number"))
			return GenericArtifactField.FieldValueTypeValue.INTEGER;
		if(dataType.equals("DATE") && (null!=editStyle && editStyle.equals("DateCombo")) )
			return GenericArtifactField.FieldValueTypeValue.DATE;
		if(dataType.equals("DATE") && null==editStyle)
			return GenericArtifactField.FieldValueTypeValue.DATE;
		if(dataType.equals("time"))
			return GenericArtifactField.FieldValueTypeValue.DATETIME;

		return GenericArtifactField.FieldValueTypeValue.STRING;
	}

	
	private static Object generateFieldDocumentation(String fieldDisplayName,
			FieldValueTypeValue fieldValueType, List<String> fieldValues) {
		StringBuffer documentation = new StringBuffer();
		documentation.append(fieldDisplayName + " (" + fieldValueType + ")\n");
		if (null != fieldValues && 0 != fieldValues.size()) {
			Collections.sort(fieldValues);
			documentation.append(" Values: [");
			for (String fieldValue : fieldValues) {
				documentation.append(" '" + fieldValue + "',");
			}
			/*
			 * if (fieldValues.length == 0) { documentation.append("<empty>"); }
			 */
			documentation.deleteCharAt(documentation.length() - 1);
			documentation.append(" ]");
		}
		return documentation.toString();
	}

	private static GenericArtifact getSchemaFieldsForDefect(IConnection qcc) {

		// Get all the fields in the project represented
		// by qcc
		String sql = "SELECT * FROM SYSTEM_FIELD WHERE SF_TABLE_NAME='BUG' AND SF_USER_LABEL IS NOT NULL";
		GenericArtifact genericArtifact = null;
		IRecordSet rs = null;
		try {
			rs = executeSQL(qcc, sql);
			int rc = rs.getRecordCount();
			genericArtifact = new GenericArtifact();
			genericArtifact.setArtifactAction(ArtifactActionValue.CREATE);
			genericArtifact.setArtifactMode(ArtifactModeValue.COMPLETE);
			genericArtifact.setArtifactType(ArtifactTypeValue.PLAINARTIFACT);
			genericArtifact
					.setIncludesFieldMetaData(IncludesFieldMetaDataValue.TRUE);
			for (int cnt = 0; cnt < rc; cnt++, rs.next()) {
				String columnName = rs.getFieldValueAsString(sfColumnName);

				String columnType = rs.getFieldValueAsString(sfColumnType);
				String fieldDisplayName = rs.getFieldValueAsString(sfUserLabel);
				if (null == fieldDisplayName) {
					continue;
				}
				List<String> fieldValueOptions = new ArrayList<String>();

				String editStyle = rs.getFieldValueAsString(sfEditStyle);
				// String isMultiValue =
				// rs.getFieldValueAsString(sfIsMultiValue);
				GenericArtifactField field;

				boolean joinedField = isJoinedField(qcc, true, columnName);
				
					
				// obtain the GenericArtifactField datatype from the columnType
				// and editStyle
				GenericArtifactField.FieldValueTypeValue fieldValueType = convertQCDataTypeToGADatatype(columnType, editStyle, columnName);

				
				boolean isMultiSelectField = false;
				if (fieldValueType
						.equals(GenericArtifactField.FieldValueTypeValue.STRING)) {

					String rootId = rs.getFieldValueAsString(sfRootId);
					addFieldOptionValue(qcc, rootId, fieldValueOptions);
					String isMultiValue = rs
							.getFieldValueAsString(sfIsMultiValue);

					if (columnType.equals("char") && null != editStyle
							&& null != isMultiValue
							&& 0 < isMultiValue.trim().length()
//							&& !StringUtils.isEmpty(isMultiValue)
							&& isMultiValue.equals("Y")) {
						if (editStyle.equals("ListCombo")
								|| editStyle.equals("TreeCombo")) {
							isMultiSelectField = true;
						}
					}
				}

				field = genericArtifact.addNewField(columnName,
						GenericArtifactField.VALUE_FIELD_TYPE_FLEX_FIELD);
				field.setFieldValueType(fieldValueType);
				field
						.setMaxOccursValue(isMultiSelectField ? GenericArtifactField.UNBOUNDED
								: "1");
				field.setMinOccurs(0);
				field.setNullValueSupported("true");
				field.setAlternativeFieldName(columnName);

				// Only for the Comments field, the action value of the
				// GenericArtifactField is set to APPEND. Later, this feature
				// can be upgraded.
				if (null != columnName && columnName.equals(BG_DEV_COMMENTS))
					field
							.setFieldAction(GenericArtifactField.FieldActionValue.APPEND);
				if (null != columnName
						&& !(columnName.equals(BG_DEV_COMMENTS)))
					field
							.setFieldAction(GenericArtifactField.FieldActionValue.REPLACE);
				
				if (columnName.equals("BG_VC_CHECKOUT_TIME")) {
					fieldValueType = FieldValueTypeValue.STRING;
					field.setFieldValueType(FieldValueTypeValue.STRING);
				}
				
				field.setFieldValue(generateFieldDocumentation(
						fieldDisplayName, fieldValueType, fieldValueOptions));
				
				if (joinedField) {
					List<String> possibleFieldValues = getJoinedFieldDisplayNames(qcc, true, columnName);
					// generate a synthetic, "human readable" field for convenience.
					// NOTE: mapping this field may introduce ambiguities, when i.e. cycle names occur
					// multiple times.
					field = genericArtifact.addNewField(columnName + HUMAN_READABLE_SUFFIX, GenericArtifactField.VALUE_FIELD_TYPE_FLEX_FIELD);
					fieldValueType = FieldValueTypeValue.STRING;
					field.setFieldValueType(fieldValueType);
					field.setMaxOccurs(1); // we assume multi-select isn't possible for joined fields.
					field.setMinOccurs(0);
					field.setNullValueSupported("true");
					field.setAlternativeFieldName(columnName + HUMAN_READABLE_SUFFIX);
					field.setFieldAction(GenericArtifactField.FieldActionValue.REPLACE);
					field.setFieldValue(generateFieldDocumentation(
							fieldDisplayName + " [READ-ONLY]", fieldValueType, possibleFieldValues));

				}
			}
		} finally {
			if (null != rs) {
				rs.safeRelease();
				rs = null;
			}
		}

		// add last modified user as a mappable field
		GenericArtifactField lastModifiedByField = genericArtifact.addNewField(lastModifiedUserFieldName,
				GenericArtifactField.VALUE_FIELD_TYPE_FLEX_FIELD);
		lastModifiedByField.setFieldValueType(GenericArtifactField.FieldValueTypeValue.STRING);
		lastModifiedByField.setMaxOccursValue("1");
		lastModifiedByField.setMinOccurs(1);
		lastModifiedByField.setNullValueSupported("false");
		lastModifiedByField.setAlternativeFieldName(lastModifiedUserFieldName);
		lastModifiedByField.setFieldValue("user that performed the last modification");

		return genericArtifact;
	}

	/**
	 * Populates the available field values into the passed list
	 * This function has to be recursive because field values can result from a hierarchical field value tree hierarchy
	 * 
	 * @param qcc TDConnection objects
	 * @param rootId field value node id
	 * @param fieldValueOptions list that will be filled with the available field values
	 * @return true if rootId field value node had children
	 */
	private static boolean addFieldOptionValue(IConnection qcc, String rootId,
			List<String> fieldValueOptions) {
		if ("0".equals(rootId)) {
			return false;
		}
		boolean isQC11 = "11".equals(qcc.getMajorVersion());
		IRecordSet rsDetails = null;
		try {
			String detailSQL = "SELECT al_description as VALUE, al_item_id AS ROOT_ID FROM ALL_LISTS where al_father_id = "
					+ rootId;
			if (isQC11) {
				detailSQL = "SELECT ls_name as VALUE, ls_id AS ROOT_ID FROM LISTS where ls_father_id = "
					+ rootId;
			}
			rsDetails = executeSQL(qcc, detailSQL);
			int detailRowCount = rsDetails.getRecordCount();
			if (0 == detailRowCount) {
				// No records retrieved, return false
				return false;
			}
			for (int i = 0; i < detailRowCount; ++i) {
				if (!addFieldOptionValue(qcc, rsDetails
						.getFieldValueAsString("ROOT_ID"),
						fieldValueOptions)) {
					fieldValueOptions.add(rsDetails.getFieldValueAsString("VALUE"));
				}
				rsDetails.next();
			}
			// records retrieved, return true
			return true;
		} finally {
			if (null != rsDetails) {
				rsDetails.safeRelease();
				rsDetails = null;
			}
		}
	}
	
	public static IRecordSet executeSQL(IConnection qcc, String sql) {
		ICommand command = null;
		try {
			command = qcc.getCommand();
			command.setCommandText(sql);
			IRecordSet rs = command.execute();
			return rs;
		} finally {
			command = null;
		}
	}

	public GenericArtifact getRepositoryLayout(String repositoryId) {
		return getProjectSchema(repositoryId);
	}
	
	/**
	 * Extracts the technical requirements type from the repository id (does a lookup and retrieve technical id for it)
	 * @param repositoryId repository id
	 * @param qcc HP QC connection
	 * @return technical requirements type
	 */
	public static String extractTechnicalRequirementsType (String repositoryId, IConnection qcc) {
		// first lookup the map
		String requirementsType = repositoryIdToTechnicalRequirementsTypeIdMap.get(repositoryId);
		if (null == requirementsType) {
			// we have to extract the requirements type now
			String[] splitRepoId = repositoryId.split(PARAM_DELIMITER);
			if(null != splitRepoId){
				// we now also accept a double hyphen to synchronize requirement types as well
				if(3 == splitRepoId.length){
					requirementsType = splitRepoId[2];
					// now we have to retrieve the technical id for the requirements type
					String technicalId = getRequirementTypeTechnicalId(qcc, requirementsType);
					repositoryIdToTechnicalRequirementsTypeIdMap.put(repositoryId, technicalId);
					return technicalId;
				}
				else {
					throw new IllegalArgumentException("Repository Id "+repositoryId+" is invalid.");
				}
			} else {
				throw new IllegalArgumentException("Repository Id "+repositoryId+" is invalid.");
			}
		} else {
			return requirementsType;
		}
	}
	
	public static String getRequirementTypeTechnicalId(IConnection qcc,
			String requirementTypeName) {
		IRecordSet rs = null;
		try {
			rs = executeSQL(qcc,
					"SELECT TPR_TYPE_ID FROM REQ_TYPE WHERE TPR_NAME = '"
							+ requirementTypeName + "'");
			if (1 != rs.getRecordCount()) {
				throw new CCFRuntimeException(
						"Could not retrieve technical id for requirements type "
								+ requirementTypeName);
			} else {
				return rs.getFieldValueAsString("TPR_TYPE_ID");
			}
		} finally {
			if (null != rs) {
				rs.safeRelease();
				rs = null;
			}
		}
	}
	
	public List<String> getDomains () {
		IConnection qcConnection = null;
		try {
			initCOM();
			qcConnection = new Connection(getServerUrl(), userName, password);
			return qcConnection.getUserVisibleDomains();
		} finally {
			if (null != qcConnection) {
				closeConnection(qcConnection);
			}		
		}
	}
	
	public List<String> getProjects (String domain) {
		IConnection qcConnection = null;
		try {
			initCOM();
			qcConnection = new Connection(getServerUrl(), userName, password);
			return qcConnection.getUserVisibleProjects(domain);
		} finally {
			if (null != qcConnection) {
				closeConnection(qcConnection);
			}		
		}
	}

}