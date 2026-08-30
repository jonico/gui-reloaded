package com.collabnet.ccf.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class IdentityMapping implements IPropertySource {
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
	private Timestamp sourceLastModificationTime;
	private Timestamp targetLastModificationTime;
	private String sourceArtifactVersion;
	private String targetArtifactVersion;
	private String artifactType;
	private String childSourceArtifactId;
	private String childSourceRepositoryId;
	private String childSourceRepositoryKind;
	private String childTargetArtifactId;
	private String childTargetRepositoryId;
	private String childTargetRepositoryKind;	
	private String parentSourceArtifactId;
	private String parentSourceRepositoryId;
	private String parentSourceRepositoryKind;
	private String parentTargetArtifactId;
	private String parentTargetRepositoryId;
	private String parentTargetRepositoryKind;
	private Landscape landscape;
	
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
	public static String P_ID_CHILD_SOURCE_ARTIFACT_ID = "childSrcArtfId"; //$NON-NLS-1$
	public static String P_CHILD_SOURCE_ARTIFACT_ID = "Child source artifact ID";	
	public static String P_ID_CHILD_SOURCE_REPOSITORY_ID = "childSrcRepoId"; //$NON-NLS-1$
	public static String P_CHILD_SOURCE_REPOSITORY_ID = "Child source repository ID";
	public static String P_ID_CHILD_SOURCE_REPOSITORY_KIND = "childSrcRepoKind"; //$NON-NLS-1$
	public static String P_CHILD_SOURCE_REPOSITORY_KIND = "Child source repository kind";	
	public static String P_ID_CHILD_TARGET_ARTIFACT_ID = "childTgtArtfId"; //$NON-NLS-1$
	public static String P_CHILD_TARGET_ARTIFACT_ID = "Child target artifact ID";	
	public static String P_ID_CHILD_TARGET_REPOSITORY_ID = "childTgtRepoId"; //$NON-NLS-1$
	public static String P_CHILD_TARGET_REPOSITORY_ID = "Child target repository ID";
	public static String P_ID_CHILD_TARGET_REPOSITORY_KIND = "childTgtRepoKind"; //$NON-NLS-1$
	public static String P_CHILD_TARGET_REPOSITORY_KIND = "Child target repository kind";	
	public static String P_ID_PARENT_SOURCE_ARTIFACT_ID = "parentSrcArtfId"; //$NON-NLS-1$
	public static String P_PARENT_SOURCE_ARTIFACT_ID = "Parent source artifact ID";	
	public static String P_ID_PARENT_SOURCE_REPOSITORY_ID = "parentSrcRepoId"; //$NON-NLS-1$
	public static String P_PARENT_SOURCE_REPOSITORY_ID = "Parent source repository ID";
	public static String P_ID_PARENT_SOURCE_REPOSITORY_KIND = "parentSrcRepoKind"; //$NON-NLS-1$
	public static String P_PARENT_SOURCE_REPOSITORY_KIND = "Parent source repository kind";	
	public static String P_ID_PARENT_TARGET_ARTIFACT_ID = "parentTgtArtfId"; //$NON-NLS-1$
	public static String P_PARENT_TARGET_ARTIFACT_ID = "Parent target artifact ID";	
	public static String P_ID_PARENT_TARGET_REPOSITORY_ID = "parentTgtRepoId"; //$NON-NLS-1$
	public static String P_PARENT_TARGET_REPOSITORY_ID = "Parent target repository ID";
	public static String P_ID_PARENT_TARGET_REPOSITORY_KIND = "parentTgtRepoKind"; //$NON-NLS-1$
	public static String P_PARENT_TARGET_REPOSITORY_KIND = "Parent target repository kind";		
	public static List<PropertyDescriptor> descriptors;
	static
	{	
		descriptors = new ArrayList<PropertyDescriptor>();
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
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_LAST_MODIFIED, P_SOURCE_LAST_MODIFIED));
		descriptors.add(new PropertyDescriptor(P_ID_TARGET_LAST_MODIFIED, P_TARGET_LAST_MODIFIED));
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_ARTIFACT_VERSION, P_SOURCE_ARTIFACT_VERSION));
		descriptors.add(new PropertyDescriptor(P_ID_TARGET_ARTIFACT_VERSION, P_TARGET_ARTIFACT_VERSION));
		descriptors.add(new PropertyDescriptor(P_ID_ARTIFACT_TYPE, P_ARTIFACT_TYPE));
		descriptors.add(new PropertyDescriptor(P_ID_CHILD_SOURCE_ARTIFACT_ID, P_CHILD_SOURCE_ARTIFACT_ID));
		descriptors.add(new PropertyDescriptor(P_ID_CHILD_SOURCE_REPOSITORY_ID, P_CHILD_SOURCE_REPOSITORY_ID));
		descriptors.add(new PropertyDescriptor(P_ID_CHILD_SOURCE_REPOSITORY_KIND, P_CHILD_SOURCE_REPOSITORY_KIND));
		descriptors.add(new PropertyDescriptor(P_ID_CHILD_TARGET_ARTIFACT_ID, P_CHILD_TARGET_ARTIFACT_ID));
		descriptors.add(new PropertyDescriptor(P_ID_CHILD_TARGET_REPOSITORY_ID, P_CHILD_TARGET_REPOSITORY_ID));
		descriptors.add(new PropertyDescriptor(P_ID_CHILD_TARGET_REPOSITORY_KIND, P_CHILD_TARGET_REPOSITORY_KIND));	
		descriptors.add(new PropertyDescriptor(P_ID_PARENT_SOURCE_ARTIFACT_ID, P_PARENT_SOURCE_ARTIFACT_ID));
		descriptors.add(new PropertyDescriptor(P_ID_PARENT_SOURCE_REPOSITORY_ID, P_PARENT_SOURCE_REPOSITORY_ID));
		descriptors.add(new PropertyDescriptor(P_ID_PARENT_SOURCE_REPOSITORY_KIND, P_PARENT_SOURCE_REPOSITORY_KIND));
		descriptors.add(new PropertyDescriptor(P_ID_PARENT_TARGET_ARTIFACT_ID, P_PARENT_TARGET_ARTIFACT_ID));
		descriptors.add(new PropertyDescriptor(P_ID_PARENT_TARGET_REPOSITORY_ID, P_PARENT_TARGET_REPOSITORY_ID));
		descriptors.add(new PropertyDescriptor(P_ID_PARENT_TARGET_REPOSITORY_KIND, P_PARENT_TARGET_REPOSITORY_KIND));
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

	public String getChildSourceArtifactId() {
		return childSourceArtifactId;
	}

	public void setChildSourceArtifactId(String childSourceArtifactId) {
		this.childSourceArtifactId = childSourceArtifactId;
	}

	public String getChildSourceRepositoryId() {
		return childSourceRepositoryId;
	}

	public void setChildSourceRepositoryId(String childSourceRepositoryId) {
		this.childSourceRepositoryId = childSourceRepositoryId;
	}

	public String getChildSourceRepositoryKind() {
		return childSourceRepositoryKind;
	}

	public void setChildSourceRepositoryKind(String childSourceRepositoryKind) {
		this.childSourceRepositoryKind = childSourceRepositoryKind;
	}

	public String getChildTargetArtifactId() {
		return childTargetArtifactId;
	}

	public void setChildTargetArtifactId(String childTargetArtifactId) {
		this.childTargetArtifactId = childTargetArtifactId;
	}

	public String getChildTargetRepositoryId() {
		return childTargetRepositoryId;
	}

	public void setChildTargetRepositoryId(String childTargetRepositoryId) {
		this.childTargetRepositoryId = childTargetRepositoryId;
	}

	public String getChildTargetRepositoryKind() {
		return childTargetRepositoryKind;
	}

	public void setChildTargetRepositoryKind(String childTargetRepositoryKind) {
		this.childTargetRepositoryKind = childTargetRepositoryKind;
	}

	public String getParentSourceArtifactId() {
		return parentSourceArtifactId;
	}

	public void setParentSourceArtifactId(String parentSourceArtifactId) {
		this.parentSourceArtifactId = parentSourceArtifactId;
	}

	public String getParentSourceRepositoryId() {
		return parentSourceRepositoryId;
	}

	public void setParentSourceRepositoryId(String parentSourceRepositoryId) {
		this.parentSourceRepositoryId = parentSourceRepositoryId;
	}

	public String getParentSourceRepositoryKind() {
		return parentSourceRepositoryKind;
	}

	public void setParentSourceRepositoryKind(String parentSourceRepositoryKind) {
		this.parentSourceRepositoryKind = parentSourceRepositoryKind;
	}

	public String getParentTargetArtifactId() {
		return parentTargetArtifactId;
	}

	public void setParentTargetArtifactId(String parentTargetArtifactId) {
		this.parentTargetArtifactId = parentTargetArtifactId;
	}

	public String getParentTargetRepositoryId() {
		return parentTargetRepositoryId;
	}

	public void setParentTargetRepositoryId(String parentTargetRepositoryId) {
		this.parentTargetRepositoryId = parentTargetRepositoryId;
	}

	public String getParentTargetRepositoryKind() {
		return parentTargetRepositoryKind;
	}

	public void setParentTargetRepositoryKind(String parentTargetRepositoryKind) {
		this.parentTargetRepositoryKind = parentTargetRepositoryKind;
	}
	
	public Landscape getLandscape() {
		return landscape;
	}

	public void setLandscape(Landscape landscape) {
		this.landscape = landscape;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IdentityMapping compareTo) {
			return compareTo.getSourceSystemId().equals(sourceSystemId) &&
			compareTo.getSourceRepositoryId().equals(sourceRepositoryId) &&
			compareTo.getSourceArtifactId().equals(sourceArtifactId) &&
			compareTo.getTargetSystemId().equals(targetSystemId) &&
			compareTo.getTargetRepositoryId().equals(targetRepositoryId) &&
			compareTo.getTargetArtifactId().equals(targetArtifactId) &&
			compareTo.getArtifactType().equals(artifactType);
		}
		return super.equals(obj);
	}

	public Object getEditableValue() {
		return sourceArtifactId + " => " + targetArtifactId;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return (IPropertyDescriptor[])getDescriptors().toArray(new IPropertyDescriptor[getDescriptors().size()]);
	}
	
	private static List<PropertyDescriptor> getDescriptors() {
		return descriptors;
	}	

	public Object getPropertyValue(Object id) {
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
		if (P_ID_SOURCE_LAST_MODIFIED.equals(id)) {
			if (sourceLastModificationTime != null) return sourceLastModificationTime.toString();
		}
		if (P_ID_TARGET_LAST_MODIFIED.equals(id)) {
			if (targetLastModificationTime != null) return targetLastModificationTime.toString();
		}
		if (P_ID_SOURCE_ARTIFACT_VERSION.equals(id)) return sourceArtifactVersion;
		if (P_ID_TARGET_ARTIFACT_VERSION.equals(id)) return targetArtifactVersion;
		if (P_ID_ARTIFACT_TYPE.equals(id)) return artifactType;
		if (P_ID_CHILD_SOURCE_ARTIFACT_ID.equals(id)) return childSourceArtifactId;
		if (P_ID_CHILD_SOURCE_REPOSITORY_ID.equals(id)) return childSourceRepositoryId;
		if (P_ID_CHILD_SOURCE_REPOSITORY_KIND.equals(id)) return childSourceRepositoryKind;
		if (P_ID_CHILD_TARGET_ARTIFACT_ID.equals(id)) return childTargetArtifactId;
		if (P_ID_CHILD_TARGET_REPOSITORY_ID.equals(id)) return childTargetRepositoryId;
		if (P_ID_CHILD_TARGET_REPOSITORY_KIND.equals(id)) return childTargetRepositoryKind;	
		if (P_ID_PARENT_SOURCE_ARTIFACT_ID.equals(id)) return parentSourceArtifactId;
		if (P_ID_PARENT_SOURCE_REPOSITORY_ID.equals(id)) return parentSourceRepositoryId;
		if (P_ID_PARENT_SOURCE_REPOSITORY_KIND.equals(id)) return parentSourceRepositoryKind;
		if (P_ID_PARENT_TARGET_ARTIFACT_ID.equals(id)) return parentTargetArtifactId;
		if (P_ID_PARENT_TARGET_REPOSITORY_ID.equals(id)) return parentTargetRepositoryId;
		if (P_ID_PARENT_TARGET_REPOSITORY_KIND.equals(id)) return parentTargetRepositoryKind;
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
