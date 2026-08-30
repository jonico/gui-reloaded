package com.collabnet.ccf.model;

import org.eclipse.swt.graphics.Image;

import com.collabnet.ccf.ICcfParticipant;

public class MappingGroup {
	private ProjectMappings projectMappingsParent;
	private ICcfParticipant ccfParticipant;
	private String id;
	private String text;
	private Image image;
	private MappingGroup[] childGroups;
	private SynchronizationStatus[] childMappings;
	private SynchronizationStatus[] hiddenChildMappings;
	private int hospitalEntries;
	
	public MappingGroup(ICcfParticipant ccfParticipant, ProjectMappings projectMappingsParent, String id, String text, Image image) {
		super();
		this.ccfParticipant = ccfParticipant;
		this.projectMappingsParent = projectMappingsParent;
		this.id = id;
		this.text = text;
		this.image = image;
	}
	
	public SynchronizationStatus getFirstMapping() {
		if (childMappings != null && childMappings.length > 0) {
			return childMappings[0];
		}
		if (childGroups != null) {
			for (MappingGroup childGroup : childGroups) {
				SynchronizationStatus childGroupMapping = childGroup.getFirstMapping();
				if (childGroupMapping != null) {
					return childGroupMapping;
				}
			}
		}
		return null;
	}

	public MappingGroup[] getChildGroups() {
		return childGroups;
	}

	public void setChildGroups(MappingGroup[] childGroups) {
		this.childGroups = childGroups;
	}

	public SynchronizationStatus[] getChildMappings() {
		return childMappings;
	}

	public void setChildMappings(SynchronizationStatus[] childMappings) {
		this.childMappings = childMappings;
	}

	public SynchronizationStatus[] getHiddenChildMappings() {
		return hiddenChildMappings;
	}

	public void setHiddenChildMappings(SynchronizationStatus[] hiddenChildMappings) {
		this.hiddenChildMappings = hiddenChildMappings;
	}

	public String getText() {
		return text;
	}

	public Image getImage() {
		return image;
	}

	public String getId() {
		return id;
	}

	public ICcfParticipant getCcfParticipant() {
		return ccfParticipant;
	}

	public ProjectMappings getProjectMappingsParent() {
		return projectMappingsParent;
	}

	public String toString() {
		String returnString = null;
		if (text != null) {
			returnString = text;
		} else {
			return super.toString();
		}
		if (getHospitalEntries() > 0) {
			returnString = returnString + " (" + hospitalEntries + ")";
		}
		return returnString;
	}
	
	public int getHospitalEntries() {
		hospitalEntries = 0;
		return getHospitalEntries(this);
	}
	
	public int getHospitalEntries(MappingGroup mappingGroup) {
		SynchronizationStatus[] childMappings = mappingGroup.getChildMappings();
		if (childMappings != null) {
			for (SynchronizationStatus status : childMappings) {
				hospitalEntries += status.getHospitalEntries();
			}
		}
		MappingGroup[] childGroups = mappingGroup.getChildGroups();
		if (childGroups != null) {
			for (MappingGroup childGroup : childGroups) {
				getHospitalEntries(childGroup);
			}
		}		
		return hospitalEntries;
	}
	
	public Object[] getChildren() {
		if (childGroups == null) {
			return childMappings;
		} else {
			return childGroups;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MappingGroup compareTo) {
			return compareTo.getId().equals(id);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
