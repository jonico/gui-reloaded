package com.collabnet.ccf.model;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.ICcfParticipant;
import com.collabnet.ccf.IProjectMappingVisibilityChecker;

@SuppressWarnings("unchecked")
public class SynchronizationStatus implements IPropertySource, Comparable {
	private String sourceSystemId;
	private String sourceRepositoryId;
	private String targetSystemId;
	private String targetRepositoryId;
	private String sourceSystemKind;
	private String sourceRepositoryKind;
	private String targetSystemKind;
	private String targetRepositoryKind;
	private Timestamp sourceLastModificationTime;
	private String sourceLastArtifactVersion;
	private String sourceLastArtifactId;
	private String conflictResolutionPriority;
	private String sourceSystemTimezone;
	private String targetSystemTimezone;
	private String group;
	private String targetSystemEncoding;
	private Landscape landscape;
	private ProjectMappings projectMappings;
	private File xslFile;
	private File graphicalXslFile;
	private File sourceRepositorySchemaFile;
	private File targetRepositorySchemaFile;
	private File genericArtifactToSourceRepositorySchemaFile;
	private File genericArtifactToTargetRepositorySchemaFile;
	private File sourceRepositorySchemaToGenericArtifactFile;
	private File targetRepositorySchemaToGenericArtifactFile;
	private File sampleXslFile;
	private File createInitialMFDFile;
	private int hospitalEntries;
	private int queuedArtifacts;

	public static final String CONFLICT_RESOLUTION_ALWAYS_IGNORE = "alwaysIgnore"; //$NON-NLS-1$
	public static final String CONFLICT_RESOLUTION_ALWAYS_OVERRIDE = "alwaysOverride"; //$NON-NLS-1$
	public static final String CONFLICT_RESOLUTION_ALWAYS_OVERRIDE_AND_IGNORE_LOCKS = "alwaysOverrideAndIgnoreLocks"; //$NON-NLS-1$
	public static final String CONFLICT_RESOLUTION_QUARANTINE_ARTIFACT = "quarantineArtifact"; //$NON-NLS-1$

	public static final String CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_IGNORE = "Do not update target artifact"; //$NON-NLS-1$
	public static final String CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE = "Overwrite target artifact"; //$NON-NLS-1$
	public static final String CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE_AND_IGNORE_LOCKS = "Overwrite target artifact and ignore locks"; //$NON-NLS-1$
	public static final String CONFLICT_RESOLUTION_DESCRIPTION_QUARANTINE_ARTIFACT = "Store artifact in hospital"; //$NON-NLS-1$	

	public static final String[] CONFLICT_RESOLUTIONS = {
			CONFLICT_RESOLUTION_ALWAYS_IGNORE,
			CONFLICT_RESOLUTION_ALWAYS_OVERRIDE,
			CONFLICT_RESOLUTION_QUARANTINE_ARTIFACT };
	public static final String[] CONFLICT_RESOLUTION_DESCRIPTIONS = {
			CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_IGNORE,
			CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE,
			CONFLICT_RESOLUTION_DESCRIPTION_QUARANTINE_ARTIFACT };

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
	public static String P_ID_SOURCE_LAST_MODIFICATION_TIME = "srcLastMod"; //$NON-NLS-1$
	public static String P_SOURCE_LAST_MODIFICATION_TIME = "Source last modification time";
	public static String P_ID_SOURCE_LAST_ARTIFACT_VERSION = "srcLastArtVer"; //$NON-NLS-1$
	public static String P_SOURCE_LAST_ARTIFACT_VERSION = "Source last artifact version";
	public static String P_ID_SOURCE_LAST_ARTIFACT_ID = "srcLastArtId"; //$NON-NLS-1$
	public static String P_SOURCE_LAST_ARTIFACT_ID = "Source last artifact ID";
	public static String P_ID_CONFLICT_RESOLUTION_PRIORITY = "confResPty"; //$NON-NLS-1$
	public static String P_CONFLICT_RESOLUTION_PRIORITY = "Conflict resolution priority";
	public static String P_ID_SOURCE_SYSTEM_TIMEZONE = "srcTimezone"; //$NON-NLS-1$
	public static String P_SOURCE_SYSTEM_TIMEZONE = "Source system timezone";
	public static String P_ID_TARGET_SYSTEM_TIMEZONE = "tgtTimezone"; //$NON-NLS-1$
	public static String P_TARGET_SYSTEM_TIMEZONE = "Target system timezone";
	public static String P_ID_SOURCE_SYSTEM_ENCODING = "srcEncoding"; //$NON-NLS-1$
	public static String P_SOURCE_SYSTEM_ENCODING = "Group";
	public static String P_ID_TARGET_SYSTEM_ENCODING = "tgtEncoding"; //$NON-NLS-1$
	public static String P_TARGET_SYSTEM_ENCODING = "Target system encoding";

	public static List<PropertyDescriptor> descriptors;
	static {
		descriptors = new ArrayList<PropertyDescriptor>();
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_SYSTEM_ID,
				P_SOURCE_SYSTEM_ID));
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_REPOSITORY_ID,
				P_SOURCE_REPOSITORY_ID));
		descriptors.add(new PropertyDescriptor(P_ID_TARGET_SYSTEM_ID,
				P_TARGET_SYSTEM_ID));
		descriptors.add(new PropertyDescriptor(P_ID_TARGET_REPOSITORY_ID,
				P_TARGET_REPOSITORY_ID));
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_SYSTEM_KIND,
				P_SOURCE_SYSTEM_KIND));
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_REPOSITORY_KIND,
				P_SOURCE_REPOSITORY_KIND));
		descriptors.add(new PropertyDescriptor(P_ID_TARGET_SYSTEM_KIND,
				P_TARGET_SYSTEM_KIND));
		descriptors.add(new PropertyDescriptor(P_ID_TARGET_REPOSITORY_KIND,
				P_TARGET_REPOSITORY_KIND));
		descriptors.add(new PropertyDescriptor(
				P_ID_SOURCE_LAST_MODIFICATION_TIME,
				P_SOURCE_LAST_MODIFICATION_TIME));
		descriptors.add(new PropertyDescriptor(
				P_ID_SOURCE_LAST_ARTIFACT_VERSION,
				P_SOURCE_LAST_ARTIFACT_VERSION));
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_LAST_ARTIFACT_ID,
				P_SOURCE_LAST_ARTIFACT_ID));
		descriptors.add(new PropertyDescriptor(
				P_ID_CONFLICT_RESOLUTION_PRIORITY,
				P_CONFLICT_RESOLUTION_PRIORITY));
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_SYSTEM_TIMEZONE,
				P_SOURCE_SYSTEM_TIMEZONE));
		descriptors.add(new PropertyDescriptor(P_ID_TARGET_SYSTEM_TIMEZONE,
				P_TARGET_SYSTEM_TIMEZONE));
		descriptors.add(new PropertyDescriptor(P_ID_SOURCE_SYSTEM_ENCODING,
				P_SOURCE_SYSTEM_ENCODING));
		descriptors.add(new PropertyDescriptor(P_ID_TARGET_SYSTEM_ENCODING,
				P_TARGET_SYSTEM_ENCODING));
	}
	
	public static String getConflictResolutionByDescription(String description) {
		if (description.equals(CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_IGNORE)) {
			return CONFLICT_RESOLUTION_ALWAYS_IGNORE;
		}
		if (description.equals(CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE)) {
			return CONFLICT_RESOLUTION_ALWAYS_OVERRIDE;
		}
		if (description.equals(CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE_AND_IGNORE_LOCKS)) {
			return CONFLICT_RESOLUTION_ALWAYS_OVERRIDE_AND_IGNORE_LOCKS;
		}
		if (description.equals(CONFLICT_RESOLUTION_DESCRIPTION_QUARANTINE_ARTIFACT)) {
			return CONFLICT_RESOLUTION_QUARANTINE_ARTIFACT;
		}
		return null;
	}
	
	public static boolean isAlwaysOverrideAndIgnoreLocksValid(SynchronizationStatus status) {
		return status.getTargetSystemKind() != null && status.getTargetSystemKind().equals("QC");
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

	public Timestamp getSourceLastModificationTime() {
		return sourceLastModificationTime;
	}

	public void setSourceLastModificationTime(
			Timestamp sourceLastModificationTime) {
		this.sourceLastModificationTime = sourceLastModificationTime;
	}

	public String getSourceLastArtifactVersion() {
		return sourceLastArtifactVersion;
	}

	public void setSourceLastArtifactVersion(String sourceLastArtifactVersion) {
		this.sourceLastArtifactVersion = sourceLastArtifactVersion;
	}

	public String getSourceLastArtifactId() {
		return sourceLastArtifactId;
	}

	public void setSourceLastArtifactId(String sourceLastArtifactId) {
		this.sourceLastArtifactId = sourceLastArtifactId;
	}

	public String getConflictResolutionPriority() {
		return conflictResolutionPriority;
	}

	public void setConflictResolutionPriority(String conflictResolutionPriority) {
		this.conflictResolutionPriority = conflictResolutionPriority;
	}

	public String getSourceSystemTimezone() {
		return sourceSystemTimezone;
	}

	public void setSourceSystemTimezone(String sourceSystemTimezone) {
		this.sourceSystemTimezone = sourceSystemTimezone;
	}

	public String getTargetSystemTimezone() {
		return targetSystemTimezone;
	}

	public void setTargetSystemTimezone(String targetSystemTimezone) {
		this.targetSystemTimezone = targetSystemTimezone;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getTargetSystemEncoding() {
		return targetSystemEncoding;
	}

	public void setTargetSystemEncoding(String targetSystemEncoding) {
		this.targetSystemEncoding = targetSystemEncoding;
	}

	public int getHospitalEntries() {
		return hospitalEntries;
	}

	public void setHospitalEntries(int hospitalEntries) {
		this.hospitalEntries = hospitalEntries;
	}

	public int getQueuedArtifacts() {
		return queuedArtifacts;
	}

	public void setQueuedArtifacts(int queuedArtifacts) {
		this.queuedArtifacts = queuedArtifacts;
	}
	
	public static SynchronizationStatus getProjectMapping(Patient patient) {
		SynchronizationStatus projectMapping = new SynchronizationStatus();
		projectMapping.setSourceSystemId(patient.getSourceSystemId());
		projectMapping.setSourceRepositoryId(patient.getSourceRepositoryId());
		projectMapping.setTargetSystemId(patient.getTargetSystemId());
		projectMapping.setTargetRepositoryId(patient.getTargetRepositoryId());
		return projectMapping;
	}
	
	public static SynchronizationStatus getProjectMapping(IdentityMapping identityMapping) {
		SynchronizationStatus projectMapping = new SynchronizationStatus();
		projectMapping.setSourceSystemId(identityMapping.getSourceSystemId());
		projectMapping.setSourceRepositoryId(identityMapping.getSourceRepositoryId());
		projectMapping.setTargetSystemId(identityMapping.getTargetSystemId());
		projectMapping.setTargetRepositoryId(identityMapping.getTargetRepositoryId());
		return projectMapping;		
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SynchronizationStatus) {
			SynchronizationStatus compareTo = (SynchronizationStatus) obj;
			return sourceSystemId.equals(compareTo.getSourceSystemId())
					&& sourceRepositoryId.equals(compareTo
							.getSourceRepositoryId())
					&& targetSystemId.equals(compareTo.getTargetSystemId())
					&& targetRepositoryId.equals(compareTo
							.getTargetRepositoryId());
		}
		return super.equals(obj);
	}

	public String getMFDFileName() {
		return (sourceSystemId + "+" + sourceRepositoryId + "+"
				+ targetSystemId + "+" + targetRepositoryId + ".mfd")
				.replaceAll(":", "-");
	}

	public void switchToNonGraphicalMapping() {
		setSourceRepositoryKind("DEFECT");
		setTargetRepositoryKind("DEFECT");
	}

	public void switchToGraphicalMapping() {
		setSourceRepositoryKind(getGenericArtifactToSourceRepositorySchemaFileName());
		setTargetRepositoryKind(getTargetRepositorySchemaToGenericArtifactFileName());
	}

	public String getGenericArtifactToSourceRepositorySchemaFileName() {
		return ("GenericArtifactFormatTo" + sourceSystemId + "+"
				+ sourceRepositoryId + "+" + targetSystemId + "+"
				+ targetRepositoryId + ".xsl").replaceAll(":", "-");
	}

	public String getGenericArtifactToTargetRepositorySchemaFileName() {
		return ("GenericArtifactFormatTo" + targetSystemId + "+"
				+ targetRepositoryId + "+" + sourceSystemId + "+"
				+ sourceRepositoryId + ".xsl").replaceAll(":", "-");
	}

	public String getSourceRepositorySchemaToGenericArtifactFileName() {
		return (sourceSystemId + "+" + sourceRepositoryId + "+"
				+ targetSystemId + "+" + targetRepositoryId + "ToGenericArtifactFormat.xsl")
				.replaceAll(":", "-");
	}

	public String getTargetRepositorySchemaToGenericArtifactFileName() {
		return (targetSystemId + "+" + targetRepositoryId + "+"
				+ sourceSystemId + "+" + sourceRepositoryId + "ToGenericArtifactFormat.xsl")
				.replaceAll(":", "-");
	}

	public String getSourceRepositorySchemaFileName() {
		return (sourceSystemId + "+" + sourceRepositoryId + "+"
				+ targetSystemId + "+" + targetRepositoryId + ".xsd")
				.replaceAll(":", "-");
	}

	public String getTargetRepositorySchemaFileName() {
		return (targetSystemId + "+" + targetRepositoryId + "+"
				+ sourceSystemId + "+" + sourceRepositoryId + ".xsd")
				.replaceAll(":", "-");
	}

	public File getMappingFile(String fileName) {
		File file = null;
		File xsltFolder = getXSLTFolder();
		if (null != xsltFolder) {
			file = new File(xsltFolder, fileName);
		}
		return file;
	}

	public File getXSLTFolder() {
		File xsltFolder = null;
		if (sourceSystemKind.startsWith(landscape.getType1())) {
			xsltFolder = landscape.getXsltFolder1();
		} else if (sourceSystemKind.startsWith(landscape.getType2())) {
			xsltFolder = landscape.getXsltFolder2();
		}
		return xsltFolder;
	}

	public boolean mappingFileExists(String fileName) {
		File file = null;
		File xsltFolder = getXSLTFolder();
		if (null != xsltFolder) {
			file = new File(xsltFolder, fileName);
			return file.exists();
		}
		return false;
	}

	public String getXslFileName() {
		return (sourceSystemId + "+" + sourceRepositoryId + "+"
				+ targetSystemId + "+" + targetRepositoryId + ".xsl")
				.replaceAll(":", "-");
	}

	public String getGraphicalXslFileName() {
		return ("Graphical+" + sourceSystemId + "+" + sourceRepositoryId + "+"
				+ targetSystemId + "+" + targetRepositoryId + ".xsl")
				.replaceAll(":", "-");
	}

	public String getMFXslFilename() {
		return ("MappingMapTo" + targetSystemId + "+" + targetRepositoryId
				+ "+" + sourceSystemId + "+" + sourceRepositoryId).replaceAll(
				":", "-").replaceAll("[\\ +\\.]", "_").replaceAll("\\[", "_").replaceAll("\\]", "_")
				+ ".xslt";
	}

	public File getSampleXslFile() {
		if (null == sampleXslFile) {
			File xsltFolder = getXSLTFolder();
			if (null != xsltFolder) {
				ICcfParticipant sourceParticipant = null;
				ICcfParticipant targetParticipant = null;
				try {
					sourceParticipant = Activator.getCcfParticipantForType(getSourceSystemKind());
					targetParticipant = Activator.getCcfParticipantForType(getTargetSystemKind());
				} catch (Exception e) {
					Activator.handleError(e);
				}
				if (null != sourceParticipant && null != targetParticipant) {
					String sourceEntityType = sourceParticipant.getEntityType(getSourceRepositoryId());
					String targetEntityType = targetParticipant.getEntityType(getTargetRepositoryId());
					if (null != sourceEntityType && null != targetEntityType) {
						String sampleFileName = "sample-" + sourceParticipant.getType() + sourceEntityType + "-" + targetParticipant.getType() + targetEntityType + ".xsl";
						sampleXslFile = new File(xsltFolder, sampleFileName);
						if (!sampleXslFile.exists()) {
							sampleXslFile = null;
						}
					}
				}
				if (null == sampleXslFile) {
					sampleXslFile = new File(xsltFolder,
							Activator.SAMPLE_XSL_FILE_NAME);
				}
			}
		}

		return sampleXslFile;
	}
	
	public File getFallbackCreateInitialMFDFile() {
		File xsltFolder = getXSLTFolder();
		if (null != xsltFolder) {
			return new File(xsltFolder, Activator.CREATE_INITIAL_MFD_FILE_NAME);
		} else {
			return null;
		}
	}

	public File getCreateInitialMFDFile() {
		if (null == createInitialMFDFile) {
			File xsltFolder = getXSLTFolder();
			if (null != xsltFolder) {
				ICcfParticipant ccfParticipant = null;
				String createInitialMFDFileName = Activator.CREATE_INITIAL_MFD_FILE_NAME;
				try {
					ccfParticipant = Activator.getCcfParticipantForType(getSourceSystemKind());
				} catch (Exception e) {
					Activator.handleError(e);
				}
				if (null == ccfParticipant) {
					createInitialMFDFileName = Activator.CREATE_INITIAL_MFD_FILE_PREFIX
					+ Activator.CREATE_INITIAL_MFD_FILE_SEPARATOR
					+ Activator.CREATE_INITIAL_MFD_FILE_UNKNOWN_ENTITY
					+ Activator.CREATE_INITIAL_MFD_FILE_SEPARATOR;					
				} else {
					createInitialMFDFileName = ccfParticipant.getInitialMDFFileNameSegment(getSourceRepositoryId(), true);
				}
				try {
					ccfParticipant = Activator.getCcfParticipantForType(getTargetSystemKind());
				} catch (Exception e) {
					Activator.handleError(e);
					ccfParticipant = null;
				}
				if (null == ccfParticipant) {
					createInitialMFDFileName = createInitialMFDFileName
					+ Activator.CREATE_INITIAL_MFD_FILE_UNKNOWN_ENTITY
					+ Activator.CREATE_INITIAL_MFD_FILE_SUFFIX;					
				} else {
					createInitialMFDFileName = createInitialMFDFileName
					+ ccfParticipant.getInitialMDFFileNameSegment(getTargetRepositoryId(), false)
					+ Activator.CREATE_INITIAL_MFD_FILE_SUFFIX;
				}
				createInitialMFDFile = new File(xsltFolder,
						createInitialMFDFileName);
			}
		}
		return createInitialMFDFile;
	}

	public File getXslFile() {
		if (null == xslFile) {
			File xsltFolder = getXSLTFolder();
			if (null != xsltFolder) {
				xslFile = new File(xsltFolder, getXslFileName());
			}
		}
		return xslFile;
	}

	public File getGraphicalXslFile() {
		if (null == graphicalXslFile) {
			File xsltFolder = getXSLTFolder();
			if (null != xsltFolder) {
				graphicalXslFile = new File(xsltFolder,
						getGraphicalXslFileName());
			}
		}
		return graphicalXslFile;
	}

	public File getSourceRepositorySchemaFile() {
		if (null == sourceRepositorySchemaFile) {
			File xsltFolder = getXSLTFolder();
			if (null != xsltFolder) {
				sourceRepositorySchemaFile = new File(xsltFolder,
						getSourceRepositorySchemaFileName());
			}
		}
		return sourceRepositorySchemaFile;
	}

	public File getTargetRepositorySchemaFile() {
		if (null == targetRepositorySchemaFile) {
			File xsltFolder = getXSLTFolder();
			if (null != xsltFolder) {
				targetRepositorySchemaFile = new File(xsltFolder,
						getTargetRepositorySchemaFileName());
			}
		}
		return targetRepositorySchemaFile;
	}

	public File getGenericArtifactToSourceRepositorySchemaFile() {
		if (null == genericArtifactToSourceRepositorySchemaFile) {
			File xsltFolder = getXSLTFolder();
			if (null != xsltFolder) {
				genericArtifactToSourceRepositorySchemaFile = new File(
						xsltFolder,
						getGenericArtifactToSourceRepositorySchemaFileName());
			}
		}
		return genericArtifactToSourceRepositorySchemaFile;
	}

	public File getGenericArtifactToTargetRepositorySchemaFile() {
		if (null == genericArtifactToTargetRepositorySchemaFile) {
			File xsltFolder = getXSLTFolder();
			if (null != xsltFolder) {
				genericArtifactToTargetRepositorySchemaFile = new File(
						xsltFolder,
						getGenericArtifactToTargetRepositorySchemaFileName());
			}
		}
		return genericArtifactToTargetRepositorySchemaFile;
	}

	public File getSourceRepositorySchemaToGenericArtifactFile() {
		if (null == sourceRepositorySchemaToGenericArtifactFile) {
			File xsltFolder = getXSLTFolder();
			if (null != xsltFolder) {
				sourceRepositorySchemaToGenericArtifactFile = new File(
						xsltFolder,
						getSourceRepositorySchemaToGenericArtifactFileName());
			}
		}
		return sourceRepositorySchemaToGenericArtifactFile;
	}

	public File getTargetRepositorySchemaToGenericArtifactFile() {
		if (null == targetRepositorySchemaToGenericArtifactFile) {
			File xsltFolder = getXSLTFolder();
			if (null != xsltFolder) {
				targetRepositorySchemaToGenericArtifactFile = new File(
						xsltFolder,
						getTargetRepositorySchemaToGenericArtifactFileName());
			}
		}
		return targetRepositorySchemaToGenericArtifactFile;
	}

	public void clearXslInfo() {
		sampleXslFile = null;
		xslFile = null;
		graphicalXslFile = null;
		sourceRepositorySchemaFile = null;
		targetRepositorySchemaFile = null;
		genericArtifactToSourceRepositorySchemaFile = null;
		genericArtifactToTargetRepositorySchemaFile = null;
		sourceRepositorySchemaToGenericArtifactFile = null;
		targetRepositorySchemaToGenericArtifactFile = null;
	}

	public Landscape getLandscape() {
		return landscape;
	}

	public void setLandscape(Landscape landscape) {
		this.landscape = landscape;
	}

	public ProjectMappings getProjectMappings() {
		if (null == projectMappings) {
			if (null != landscape
					&& Landscape.ROLE_OPERATOR == landscape.getRole()) {
				projectMappings = new ProjectMappings(landscape);
			} else {
				projectMappings = new AdministratorProjectMappings(landscape);
			}
		}
		return projectMappings;
	}

	public void setProjectMappings(ProjectMappings projectMappings) {
		this.projectMappings = projectMappings;
	}

	public boolean isPaused() {
		return sourceSystemKind.endsWith("_paused");
	}

	public String toString() {
		if (0 < hospitalEntries)
			return sourceRepositoryId + " => " + targetRepositoryId + " ("
					+ hospitalEntries + ")";
		else
			return sourceRepositoryId + " => " + targetRepositoryId;
	}

	public static String getConflictResolutionDescription(String code) {
		if (code.equals(CONFLICT_RESOLUTION_ALWAYS_IGNORE)) {
			return CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_IGNORE;
		}
		if (code.equals(CONFLICT_RESOLUTION_ALWAYS_OVERRIDE)) {
			return CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE;
		}
		if (code.equals(CONFLICT_RESOLUTION_ALWAYS_OVERRIDE_AND_IGNORE_LOCKS)) {
			return CONFLICT_RESOLUTION_DESCRIPTION_ALWAYS_OVERRIDE_AND_IGNORE_LOCKS;
		}
		if (code.equals(CONFLICT_RESOLUTION_QUARANTINE_ARTIFACT)) {
			return CONFLICT_RESOLUTION_DESCRIPTION_QUARANTINE_ARTIFACT;
		}
		return null;
	}

	public boolean usesGraphicalMapping() {
		return targetRepositoryKind != null
				&& targetRepositoryKind.endsWith(".xsl");
	}
	
	public boolean isHiddenMapping() {
		try {
			IProjectMappingVisibilityChecker[] visibilityCheckers = Activator.getVisibilityCheckers();
			if (null != visibilityCheckers && 0 < visibilityCheckers.length) {
				for (IProjectMappingVisibilityChecker checker : visibilityCheckers) {
					if (!checker.isProjectMappingVisible(this)) {
						return true;
					}
				}
			}
		} catch (Exception e) {}
		return false;
	}

	public int compareTo(Object compareToObject) {
		if (!(compareToObject instanceof SynchronizationStatus))
			return 0;
		SynchronizationStatus compareToStatus = (SynchronizationStatus) compareToObject;
		return toString().compareTo(compareToStatus.toString());
	}

	public Object getEditableValue() {
		return toString();
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return (IPropertyDescriptor[]) getDescriptors().toArray(
				new IPropertyDescriptor[getDescriptors().size()]);
	}

	private static List<PropertyDescriptor> getDescriptors() {
		return descriptors;
	}

	public Object getPropertyValue(Object id) {
		if (P_ID_SOURCE_SYSTEM_ID.equals(id))
			return sourceSystemId;
		if (P_ID_SOURCE_REPOSITORY_ID.equals(id))
			return sourceRepositoryId;
		if (P_ID_TARGET_SYSTEM_ID.equals(id))
			return targetSystemId;
		if (P_ID_TARGET_REPOSITORY_ID.equals(id))
			return targetRepositoryId;
		if (P_ID_SOURCE_SYSTEM_KIND.equals(id))
			return sourceSystemKind;
		if (P_ID_SOURCE_REPOSITORY_KIND.equals(id))
			return sourceRepositoryKind;
		if (P_ID_TARGET_SYSTEM_KIND.equals(id))
			return targetSystemKind;
		if (P_ID_TARGET_REPOSITORY_KIND.equals(id))
			return targetRepositoryKind;
		if (P_ID_SOURCE_LAST_MODIFICATION_TIME.equals(id)) {
			if (null != sourceLastModificationTime)
				return sourceLastModificationTime.toString();
		}
		if (P_ID_SOURCE_LAST_ARTIFACT_VERSION.equals(id))
			return sourceLastArtifactVersion;
		if (P_ID_SOURCE_LAST_ARTIFACT_ID.equals(id))
			return sourceLastArtifactId;
		if (P_ID_CONFLICT_RESOLUTION_PRIORITY.equals(id))
			return conflictResolutionPriority;
		if (P_ID_SOURCE_SYSTEM_TIMEZONE.equals(id))
			return sourceSystemTimezone;
		if (P_ID_SOURCE_SYSTEM_ENCODING.equals(id))
			return group;
		if (P_ID_TARGET_SYSTEM_TIMEZONE.equals(id))
			return targetSystemTimezone;
		if (P_ID_TARGET_SYSTEM_ENCODING.equals(id))
			return targetSystemEncoding;
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
