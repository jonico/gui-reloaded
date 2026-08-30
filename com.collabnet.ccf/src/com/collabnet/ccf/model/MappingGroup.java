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
		if (null != childMappings && 0 < childMappings.length) {
			return childMappings[0];
		}
		if (null != childGroups) {
			for (MappingGroup childGroup : childGroups) {
				SynchronizationStatus childGroupMapping = childGroup.getFirstMapping();
				if (null != childGroupMapping) {
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
		if (null != text) {
			returnString = text;
		} else {
			return super.toString();
		}
		if (0 < getHospitalEntries()) {
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
		if (null != childMappings) {
			for (SynchronizationStatus status : childMappings) {
				hospitalEntries += status.getHospitalEntries();
			}
		}
		MappingGroup[] childGroups = mappingGroup.getChildGroups();
		if (null != childGroups) {
			for (MappingGroup childGroup : childGroups) {
				getHospitalEntries(childGroup);
			}
		}		
		return hospitalEntries;
	}
	
	public Object[] getChildren() {
		if (null == childGroups) {
			return childMappings;
		} else {
			return childGroups;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MappingGroup) {
			MappingGroup compareTo = (MappingGroup)obj;
			return compareTo.getId().equals(id);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
