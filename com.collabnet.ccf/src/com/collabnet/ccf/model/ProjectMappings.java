package com.collabnet.ccf.model;

public class ProjectMappings {
	private Landscape landscape;
	
	public final static String MAPPING_TYPE_PLANNING_FOLDERS = "planningFolders";
	public final static String MAPPING_TYPE_METADATA = "MetaData";

	public ProjectMappings(Landscape landscape) {
		this.landscape = landscape;
	}
	
	public String toString() {
		return "Project Mappings";
	}
	
	public Landscape getLandscape() {
		return landscape;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ProjectMappings compareTo) {
			return landscape.equals(compareTo.getLandscape());
		}
		return super.equals(obj);
	}

}
