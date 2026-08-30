package com.collabnet.ccf.model;

import org.osgi.service.prefs.Preferences;

@SuppressWarnings("unchecked")
public class Role implements Comparable {
	private String name;
	private Preferences node;
	
	private String password;

	private boolean addLandscape = true;
	private boolean editLandscape = true;
	private boolean deleteLandscape = true;
	private boolean addProjectMapping = true;
	private boolean changeProjectMapping = true;
	private boolean deleteProjectMapping = true;
	private boolean editFieldMappings = true;
	private boolean pauseSynchronization = true;
	private boolean resumeSynchronization = true;
	private boolean resetSynchronizationStatus = true;
	private boolean deleteProjectMappingIdentityMappings = true;
	private boolean editQuarantinedArtifact = true;
	private boolean markAsFixed = true;
	private boolean reopen = true;
	private boolean replay = true;
	private boolean cancelReplay = true;
	private boolean deleteHospitalEntry = true;
	private boolean createReverseIdentityMapping = true;
	private boolean deleteIdentityMapping = true;
	private boolean editIdentityMapping = true;
	private boolean editLogSettings = true;
	private boolean consistencyCheck = true;
	private boolean maintainRoles = true;

	public Role(String name) {
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isPasswordRequired() {
		return password != null && password.trim().length() > 0;
	}

	public boolean isAddLandscape() {
		return addLandscape;
	}

	public void setAddLandscape(boolean addLandscape) {
		this.addLandscape = addLandscape;
	}

	public boolean isEditLandscape() {
		return editLandscape;
	}

	public void setEditLandscape(boolean editLandscape) {
		this.editLandscape = editLandscape;
	}

	public boolean isDeleteLandscape() {
		return deleteLandscape;
	}

	public void setDeleteLandscape(boolean deleteLandscape) {
		this.deleteLandscape = deleteLandscape;
	}

	public boolean isAddProjectMapping() {
		return addProjectMapping;
	}

	public void setAddProjectMapping(boolean addProjectMapping) {
		this.addProjectMapping = addProjectMapping;
	}

	public boolean isChangeProjectMapping() {
		return changeProjectMapping;
	}

	public void setChangeProjectMapping(boolean changeProjectMapping) {
		this.changeProjectMapping = changeProjectMapping;
	}

	public boolean isDeleteProjectMapping() {
		return deleteProjectMapping;
	}

	public void setDeleteProjectMapping(boolean deleteProjectMapping) {
		this.deleteProjectMapping = deleteProjectMapping;
	}

	public boolean isEditFieldMappings() {
		return editFieldMappings;
	}

	public void setEditFieldMappings(boolean editFieldMappings) {
		this.editFieldMappings = editFieldMappings;
	}

	public boolean isPauseSynchronization() {
		return pauseSynchronization;
	}

	public void setPauseSynchronization(boolean pauseSynchronization) {
		this.pauseSynchronization = pauseSynchronization;
	}

	public boolean isResumeSynchronization() {
		return resumeSynchronization;
	}

	public void setResumeSynchronization(boolean resumeSynchronization) {
		this.resumeSynchronization = resumeSynchronization;
	}

	public boolean isResetSynchronizationStatus() {
		return resetSynchronizationStatus;
	}

	public void setResetSynchronizationStatus(boolean resetSynchronizationStatus) {
		this.resetSynchronizationStatus = resetSynchronizationStatus;
	}

	public boolean isDeleteProjectMappingIdentityMappings() {
		return deleteProjectMappingIdentityMappings;
	}

	public void setDeleteProjectMappingIdentityMappings(
			boolean deleteProjectMappingIdentityMappings) {
		this.deleteProjectMappingIdentityMappings = deleteProjectMappingIdentityMappings;
	}

	public boolean isEditQuarantinedArtifact() {
		return editQuarantinedArtifact;
	}

	public void setEditQuarantinedArtifact(boolean editQuarantinedArtifact) {
		this.editQuarantinedArtifact = editQuarantinedArtifact;
	}

	public boolean isMarkAsFixed() {
		return markAsFixed;
	}

	public void setMarkAsFixed(boolean markAsFixed) {
		this.markAsFixed = markAsFixed;
	}

	public boolean isReopen() {
		return reopen;
	}

	public void setReopen(boolean reopen) {
		this.reopen = reopen;
	}

	public boolean isReplay() {
		return replay;
	}

	public void setReplay(boolean replay) {
		this.replay = replay;
	}

	public boolean isCancelReplay() {
		return cancelReplay;
	}

	public void setCancelReplay(boolean cancelReplay) {
		this.cancelReplay = cancelReplay;
	}

	public boolean isDeleteHospitalEntry() {
		return deleteHospitalEntry;
	}

	public void setDeleteHospitalEntry(boolean deleteHospitalEntry) {
		this.deleteHospitalEntry = deleteHospitalEntry;
	}

	public boolean isCreateReverseIdentityMapping() {
		return createReverseIdentityMapping;
	}

	public void setCreateReverseIdentityMapping(boolean createReverseIdentityMapping) {
		this.createReverseIdentityMapping = createReverseIdentityMapping;
	}

	public boolean isDeleteIdentityMapping() {
		return deleteIdentityMapping;
	}

	public void setDeleteIdentityMapping(boolean deleteIdentityMapping) {
		this.deleteIdentityMapping = deleteIdentityMapping;
	}

	public boolean isEditIdentityMapping() {
		return editIdentityMapping;
	}

	public void setEditIdentityMapping(boolean editIdentityMapping) {
		this.editIdentityMapping = editIdentityMapping;
	}

	public boolean isEditLogSettings() {
		return editLogSettings;
	}

	public void setEditLogSettings(boolean editLogSettings) {
		this.editLogSettings = editLogSettings;
	}
	
	public boolean isConsistencyCheck() {
		return consistencyCheck;
	}

	public void setConsistencyCheck(boolean consistencyCheck) {
		this.consistencyCheck = consistencyCheck;
	}
	
	public boolean isMaintainRoles() {
		return maintainRoles;
	}

	public void setMaintainRoles(boolean maintainRoles) {
		this.maintainRoles = maintainRoles;
	}
	
	public Preferences getNode() {
		return node;
	}

	public void setNode(Preferences node) {
		this.node = node;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Role compareTo) {
			return compareTo.getName().equals(name);
		}
		return super.equals(obj);
	}

	public int compareTo(Object obj) {
		if (obj instanceof Role compareTo) {
			return name.compareTo(compareTo.getName());
		}
		return 0;
	}

}
