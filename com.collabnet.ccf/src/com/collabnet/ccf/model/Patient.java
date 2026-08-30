package com.collabnet.ccf.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class Patient implements IPropertySource {
	private int id;
	private String timeStamp;
	private String exceptionClassName;
	private String exceptionMessage;
	private String causeExceptionClassName;
	private String causeExceptionMessage;
	private String stackTrace;
	private String adaptorName;
	private String originatingComponent;
	private String dataType;
	private String data;
	private boolean fixed;
	private boolean reprocessed;
	private String sourceSystemId;
	private String sourceRepositoryId;
	private String targetSystemId;
	private String targetRepositoryId;
	private String sourceSystemKind;
	private String sourceRepositoryKind;
	private String targetSystemKind;
	private String targetRepositoryKind;
	private String sourceArtifactId;
	private String targetArtifactId;
	private String errorCode;
	private Timestamp sourceLastModificationTime;
	private Timestamp targetLastModificationTime;
	private String sourceArtifactVersion;
	private String targetArtifactVersion;
	private String artifactType;
	private String genericArtifact;
	private Landscape landscape;
	private boolean outdated;
	
	public static String P_ID_ID = "id"; //$NON-NLS-1$
	public static String P_ID = "ID";
	public static String P_ID_TIMESTAMP = "timestamp"; //$NON-NLS-1$
	public static String P_TIMESTAMP = "Timestamp";
	public static String P_ID_EXCEPTION_CLASS = "excClass"; //$NON-NLS-1$
	public static String P_EXCEPTION_CLASS = "Exception class";
	public static String P_ID_EXCEPTION_MESSAGE = "excMsg"; //$NON-NLS-1$
	public static String P_EXCEPTION_MESSAGE = "Exception message";
	public static String P_ID_CAUSE_EXCEPTION_CLASS = "causeExcClass"; //$NON-NLS-1$
	public static String P_CAUSE_EXCEPTION_CLASS = "Cause exception class";
	public static String P_ID_CAUSE_EXCEPTION_MESSAGE = "causeExcMsg"; //$NON-NLS-1$
	public static String P_CAUSE_EXCEPTION_MESSAGE = "Cause exception message";
	public static String P_ID_STACK_TRACE = "stackTrace"; //$NON-NLS-1$
	public static String P_STACK_TRACE = "Stack trace";		
	public static String P_ID_ADAPTOR_NAME = "adaptor"; //$NON-NLS-1$
	public static String P_ADAPTOR_NAME = "Adaptor name";
	public static String P_ID_ORIGINATING_COMPONENT = "component"; //$NON-NLS-1$
	public static String P_ORIGINATING_COMPONENT = "Originating component";	
	public static String P_ID_DATA_TYPE = "dataType"; //$NON-NLS-1$
	public static String P_DATA_TYPE = "Data type";
	public static String P_ID_DATA = "data"; //$NON-NLS-1$
	public static String P_DATA = "Data";	
	public static String P_ID_FIXED = "fixed"; //$NON-NLS-1$
	public static String P_FIXED = "Fixed";	
	public static String P_ID_REPROCESSED = "reprocessed"; //$NON-NLS-1$
	public static String P_REPROCESSED = "Reprocessed";	
	public static String P_ID_SOURCE_SYSTEM_ID = "srcSysId"; //$NON-NLS-1$
	public static String P_SOURCE_SYSTEM_ID = "Source system ID";
	public static String P_ID_SOURCE_REPOSITORY_ID = "srcRepoId"; //$NON-NLS-1$
	public static String P_SOURCE_REPOSITORY_ID = "Source repository ID";	
	public static String P_ID_TARGET_SYSTEM_ID = "tgtSysId"; //$NON-NLS-1$
	public static String P_TARGET_SYSTEM_ID = "Target system ID";
	public static String P_ID_TARGET_REPOSITORY_ID = "tgtRepoId"; //$NON-NLS-1$
	public static String P_TARGET_REPOSITORY_ID = "Target repository ID";	
	public static String P_ID_SOURCE_SYSTEM_KIND = "srcSysKind"; //$NON-NLS-1$
	public static String P_SOURCE_SYSTEM_KIND = "Source system kind";
	public static String P_ID_SOURCE_REPOSITORY_KIND = "srcRepoKind"; //$NON-NLS-1$
	public static String P_SOURCE_REPOSITORY_KIND = "Source repository kind";
	public static String P_ID_TARGET_SYSTEM_KIND = "tgtSysKind"; //$NON-NLS-1$
	public static String P_TARGET_SYSTEM_KIND = "Target system kind";
	public static String P_ID_TARGET_REPOSITORY_KIND = "tgtRepoKind"; //$NON-NLS-1$
	public static String P_TARGET_REPOSITORY_KIND = "Target repository kind";
	public static String P_ID_SOURCE_ARTIFACT_ID = "srcArtfId"; //$NON-NLS-1$
	public static String P_SOURCE_ARTIFACT_ID = "Source artifact ID";
	public static String P_ID_TARGET_ARTIFACT_ID = "tgtArtfId"; //$NON-NLS-1$
	public static String P_TARGET_ARTIFACT_ID = "Target artifact ID";	
	public static String P_ID_ERROR_CODE = "errCode"; //$NON-NLS-1$
	public static String P_ERROR_CODE = "Error code";	
	public static String P_ID_SOURCE_LAST_MODIFIED = "srcLastMod"; //$NON-NLS-1$
	public static String P_SOURCE_LAST_MODIFIED = "Source last modified";	
	public static String P_ID_TARGET_LAST_MODIFIED = "tgtLastMod"; //$NON-NLS-1$
	public static String P_TARGET_LAST_MODIFIED = "Target last modified";	
	public static String P_ID_SOURCE_ARTIFACT_VERSION = "srcArtfVer"; //$NON-NLS-1$
	public static String P_SOURCE_ARTIFACT_VERSION = "Source artifact version";
	public static String P_ID_TARGET_ARTIFACT_VERSION = "tgtArtfVer"; //$NON-NLS-1$
	public static String P_TARGET_ARTIFACT_VERSION = "Target artifact version";
	public static String P_ID_ARTIFACT_TYPE = "artfType"; //$NON-NLS-1$
	public static String P_ARTIFACT_TYPE = "Artifact type";	
	public static String P_ID_GENERIC_ARTIFACT = "genArtf"; //$NON-NLS-1$
	public static String P_GENERIC_ARTIFACT = "Generic artifact";	
	public static List<PropertyDescriptor> descriptors;
	static
	{	
		descriptors = new ArrayList<PropertyDescriptor>();
		descriptors.add(new PropertyDescriptor(P_ID_ID, P_ID));
		descriptors.add(new PropertyDescriptor(P_ID_TIMESTAMP, P_TIMESTAMP));
		descriptors.add(new PropertyDescriptor(P_ID_EXCEPTION_CLASS, P_EXCEPTION_CLASS));
		descriptors.add(new PropertyDescriptor(P_ID_EXCEPTION_MESSAGE, P_EXCEPTION_MESSAGE));
		descriptors.add(new PropertyDescriptor(P_ID_CAUSE_EXCEPTION_CLASS, P_CAUSE_EXCEPTION_CLASS));
		descriptors.add(new PropertyDescriptor(P_ID_CAUSE_EXCEPTION_MESSAGE, P_CAUSE_EXCEPTION_MESSAGE));
		descriptors.add(new PropertyDescriptor(P_ID_STACK_TRACE, P_STACK_TRACE));
		descriptors.add(new PropertyDescriptor(P_ID_ADAPTOR_NAME, P_ADAPTOR_NAME));	
		descriptors.add(new PropertyDescriptor(P_ID_ORIGINATING_COMPONENT, P_ORIGINATING_COMPONENT));
		descriptors.add(new PropertyDescriptor(P_ID_DATA_TYPE, P_DATA_TYPE));
		descriptors.add(new PropertyDescriptor(P_ID_DATA, P_DATA));
		descriptors.add(new PropertyDescriptor(P_ID_FIXED, P_FIXED));	
		descriptors.add(new PropertyDescriptor(P_ID_REPROCESSED, P_REPROCESSED));
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_SYSTEM_ID, P_SOURCE_SYSTEM_ID));
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_REPOSITORY_ID, P_SOURCE_REPOSITORY_ID));
		descriptors.add(new PropertyDescriptor(P_ID_TARGET_SYSTEM_ID, P_TARGET_SYSTEM_ID));
		descriptors.add(new PropertyDescriptor(P_ID_TARGET_REPOSITORY_ID, P_TARGET_REPOSITORY_ID));
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_SYSTEM_KIND, P_SOURCE_SYSTEM_KIND));
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_REPOSITORY_KIND, P_SOURCE_REPOSITORY_KIND));
		descriptors.add(new PropertyDescriptor(P_ID_TARGET_SYSTEM_KIND, P_TARGET_SYSTEM_KIND));
		descriptors.add(new PropertyDescriptor(P_ID_TARGET_REPOSITORY_KIND, P_TARGET_REPOSITORY_KIND));
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_ARTIFACT_ID, P_SOURCE_ARTIFACT_ID));
		descriptors.add(new PropertyDescriptor(P_ID_TARGET_ARTIFACT_ID, P_TARGET_ARTIFACT_ID));
		descriptors.add(new PropertyDescriptor(P_ID_ERROR_CODE, P_ERROR_CODE));
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_LAST_MODIFIED, P_SOURCE_LAST_MODIFIED));
		descriptors.add(new PropertyDescriptor(P_ID_TARGET_LAST_MODIFIED, P_TARGET_LAST_MODIFIED));
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_ARTIFACT_VERSION, P_SOURCE_ARTIFACT_VERSION));
		descriptors.add(new PropertyDescriptor(P_ID_TARGET_ARTIFACT_VERSION, P_TARGET_ARTIFACT_VERSION));
		descriptors.add(new PropertyDescriptor(P_ID_ARTIFACT_TYPE, P_ARTIFACT_TYPE));
		descriptors.add(new PropertyDescriptor(P_ID_GENERIC_ARTIFACT, P_GENERIC_ARTIFACT));
	}		
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getExceptionClassName() {
		return exceptionClassName;
	}
	public void setExceptionClassName(String exceptionClassName) {
		this.exceptionClassName = exceptionClassName;
	}
	public String getExceptionMessage() {
		return exceptionMessage;
	}
	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}
	public String getCauseExceptionClassName() {
		return causeExceptionClassName;
	}
	public void setCauseExceptionClassName(String causeExceptionClassName) {
		this.causeExceptionClassName = causeExceptionClassName;
	}
	public String getCauseExceptionMessage() {
		return causeExceptionMessage;
	}
	public void setCauseExceptionMessage(String causeExceptionMessage) {
		this.causeExceptionMessage = causeExceptionMessage;
	}
	public String getStackTrace() {
		return stackTrace;
	}
	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
	public String getAdaptorName() {
		return adaptorName;
	}
	public void setAdaptorName(String adaptorName) {
		this.adaptorName = adaptorName;
	}
	public String getOriginatingComponent() {
		return originatingComponent;
	}
	public void setOriginatingComponent(String originatingComponent) {
		this.originatingComponent = originatingComponent;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public boolean isFixed() {
		return fixed;
	}
	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}
	public boolean isReprocessed() {
		return reprocessed;
	}
	public void setReprocessed(boolean reprocessed) {
		this.reprocessed = reprocessed;
	}
	public String getSourceSystemId() {
		return sourceSystemId;
	}
	public void setSourceSystemId(String sourceSystemId) {
		this.sourceSystemId = sourceSystemId;
	}
	public String getSourceRepositoryId() {
		return sourceRepositoryId;
	}
	public void setSourceRepositoryId(String sourceRepositoryId) {
		this.sourceRepositoryId = sourceRepositoryId;
	}
	public String getTargetSystemId() {
		return targetSystemId;
	}
	public void setTargetSystemId(String targetSystemId) {
		this.targetSystemId = targetSystemId;
	}
	public String getTargetRepositoryId() {
		return targetRepositoryId;
	}
	public void setTargetRepositoryId(String targetRepositoryId) {
		this.targetRepositoryId = targetRepositoryId;
	}
	public String getSourceSystemKind() {
		return sourceSystemKind;
	}
	public void setSourceSystemKind(String sourceSystemKind) {
		this.sourceSystemKind = sourceSystemKind;
	}
	public String getSourceRepositoryKind() {
		return sourceRepositoryKind;
	}
	public void setSourceRepositoryKind(String sourceRepositoryKind) {
		this.sourceRepositoryKind = sourceRepositoryKind;
	}
	public String getTargetSystemKind() {
		return targetSystemKind;
	}
	public void setTargetSystemKind(String targetSystemKind) {
		this.targetSystemKind = targetSystemKind;
	}
	public String getTargetRepositoryKind() {
		return targetRepositoryKind;
	}
	public void setTargetRepositoryKind(String targetRepositoryKind) {
		this.targetRepositoryKind = targetRepositoryKind;
	}
	public String getSourceArtifactId() {
		return sourceArtifactId;
	}
	public void setSourceArtifactId(String sourceArtifactId) {
		this.sourceArtifactId = sourceArtifactId;
	}
	public String getTargetArtifactId() {
		return targetArtifactId;
	}
	public void setTargetArtifactId(String targetArtifactId) {
		this.targetArtifactId = targetArtifactId;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public Timestamp getSourceLastModificationTime() {
		return sourceLastModificationTime;
	}
	public void setSourceLastModificationTime(Timestamp sourceLastModificationTime) {
		this.sourceLastModificationTime = sourceLastModificationTime;
	}
	public Timestamp getTargetLastModificationTime() {
		return targetLastModificationTime;
	}
	public void setTargetLastModificationTime(Timestamp targetLastModificationTime) {
		this.targetLastModificationTime = targetLastModificationTime;
	}
	public String getSourceArtifactVersion() {
		return sourceArtifactVersion;
	}
	public void setSourceArtifactVersion(String sourceArtifactVersion) {
		this.sourceArtifactVersion = sourceArtifactVersion;
	}
	public String getTargetArtifactVersion() {
		return targetArtifactVersion;
	}
	public void setTargetArtifactVersion(String targetArtifactVersion) {
		this.targetArtifactVersion = targetArtifactVersion;
	}
	public String getArtifactType() {
		return artifactType;
	}
	public void setArtifactType(String artifactType) {
		this.artifactType = artifactType;
	}
	public String getGenericArtifact() {
		return genericArtifact;
	}
	public void setGenericArtifact(String genericArtifact) {
		this.genericArtifact = genericArtifact;
	}
	
	public Landscape getLandscape() {
		return landscape;
	}
	public void setLandscape(Landscape landscape) {
		this.landscape = landscape;
	}
	
	public boolean isOutdated() {
		return outdated;
	}
	
	public void setOutdated(boolean outdated) {
		this.outdated = outdated;
	}
	public String toClipboard() {
		StringBuffer clipboard = new StringBuffer();
		// avoid NPE by adding at least one constant header
		clipboard.append("Content of CCF hospital table\n");
		if (null != exceptionMessage) {
			clipboard.append("Exception Message:\n\n");
			clipboard.append(exceptionMessage + "\n\n");
		}
		if (null != causeExceptionMessage) {
			clipboard.append("Cause Exception Message:\n\n");
			clipboard.append(causeExceptionMessage + "\n\n");
		}
		if (null != stackTrace) {
			clipboard.append("Stack Trace:\n\n");
			clipboard.append(stackTrace + "\n\n");
		}
		if (null != genericArtifact) {
			clipboard.append("Payload:\n\n");
			clipboard.append(genericArtifact);
		}
		return clipboard.toString();
	}
	
	public Object getEditableValue() {
		return id + ": " + timeStamp;
	}
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return (IPropertyDescriptor[])getDescriptors().toArray(new IPropertyDescriptor[getDescriptors().size()]);
	}
	
	private static List<PropertyDescriptor> getDescriptors() {
		return descriptors;
	}	
	
	public Object getPropertyValue(Object id) {
		if (P_ID_ID.equals(id)) return Integer.toString(getId());
		if (P_ID_TIMESTAMP.equals(id)) return timeStamp;
		if (P_ID_EXCEPTION_CLASS.equals(id)) return exceptionClassName;
		if (P_ID_EXCEPTION_MESSAGE.equals(id)) return exceptionMessage;
		if (P_ID_CAUSE_EXCEPTION_CLASS.equals(id)) return causeExceptionClassName;
		if (P_ID_CAUSE_EXCEPTION_MESSAGE.equals(id)) return causeExceptionMessage;
		if (P_ID_STACK_TRACE.equals(id)) return stackTrace;
		if (P_ID_ADAPTOR_NAME.equals(id)) return adaptorName;
		if (P_ID_ORIGINATING_COMPONENT.equals(id)) return originatingComponent;
		if (P_ID_DATA_TYPE.equals(id)) return dataType;
		if (P_ID_DATA.equals(id)) return data;
		if (P_ID_FIXED.equals(id)) {
			if (fixed) return "true";
			else return "false";
		}
		if (P_ID_REPROCESSED.equals(id)) {
			if (reprocessed) return "true";
			else return "false";
		}
		if (P_ID_SOURCE_SYSTEM_ID.equals(id)) return sourceSystemId;
		if (P_ID_SOURCE_REPOSITORY_ID.equals(id)) return sourceRepositoryId;
		if (P_ID_TARGET_SYSTEM_ID.equals(id)) return targetSystemId;
		if (P_ID_TARGET_REPOSITORY_ID.equals(id)) return targetRepositoryId;		
		if (P_ID_SOURCE_SYSTEM_KIND.equals(id)) return sourceSystemKind;
		if (P_ID_SOURCE_REPOSITORY_KIND.equals(id)) return sourceRepositoryKind;
		if (P_ID_TARGET_SYSTEM_KIND.equals(id)) return targetSystemKind;
		if (P_ID_TARGET_REPOSITORY_KIND.equals(id)) return targetRepositoryKind;
		if (P_ID_SOURCE_ARTIFACT_ID.equals(id)) return sourceArtifactId;
		if (P_ID_TARGET_ARTIFACT_ID.equals(id)) return targetArtifactId;
		if (P_ID_ERROR_CODE.equals(id)) return errorCode;
		if (P_ID_SOURCE_LAST_MODIFIED.equals(id)) {
			if (null != sourceLastModificationTime) return sourceLastModificationTime.toString();
		}
		if (P_ID_TARGET_LAST_MODIFIED.equals(id)) {
			if (null != targetLastModificationTime) return targetLastModificationTime.toString();
		}
		if (P_ID_SOURCE_ARTIFACT_VERSION.equals(id)) return sourceArtifactVersion;
		if (P_ID_TARGET_ARTIFACT_VERSION.equals(id)) return targetArtifactVersion;
		if (P_ID_ARTIFACT_TYPE.equals(id)) return artifactType;
		if (P_ID_GENERIC_ARTIFACT.equals(id)) return genericArtifact;
		return null;
	}
	
	public boolean isPropertySet(Object id) {
		return false;
	}
	
	public void resetPropertyValue(Object id) {
	}
	
	public void setPropertyValue(Object id, Object value) {
	}

}
