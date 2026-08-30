package com.collabnet.ccf.teamforge_sw;

import org.eclipse.core.expressions.PropertyTester;

import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.MappingGroup;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;

public class ProjectMappingsPropertyTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof ProjectMappings projectMappings && property.equals("isTeamForgeSwpLandscape")) {
			return isTeamForgeSwpLandscape(projectMappings);
		}
		if (receiver instanceof Landscape landscape && property.equals("isTeamForgeSwpLandscape")) {
			return isTeamForgeSwpLandscape(landscape);
		}
		if (receiver instanceof SynchronizationStatus projectMapping && property.equals("isTrackerPbisMapping")) {
			if (isTeamForgeSwpLandscape(projectMapping.getProjectMappings())) {
				return projectMapping.toString().indexOf("-PBI") != -1;
			}
		}
		if (receiver instanceof MappingGroup group && property.equals("isTrackerPbisMapping")) {
			SynchronizationStatus projectMapping = group.getFirstMapping();
			if (projectMapping != null && isTeamForgeSwpLandscape(projectMapping.getProjectMappings())) {
				return projectMapping.toString().indexOf("-PBI") != -1;
			}
		}
		if (receiver instanceof SynchronizationStatus projectMapping && property.equals("isTeamForgeScrumWorksMapping")) {
			return isTeamForgeSwpLandscape(projectMapping.getProjectMappings());
		}
		if (receiver instanceof MappingGroup group1 && property.equals("isTeamForgeScrumWorksMappingGroup")) {
			SynchronizationStatus projectMapping = group1.getFirstMapping();
			return projectMapping != null && isTeamForgeSwpLandscape(projectMapping.getProjectMappings());
		}		
		return false;
	}
	
	private boolean isTeamForgeSwpLandscape(ProjectMappings projectMappings) {
		if (projectMappings != null) {
			return isTeamForgeSwpLandscape(projectMappings.getLandscape());
		}
		return false;
	}
	
	private boolean isTeamForgeSwpLandscape(Landscape landscape) {
		if (landscape != null) {
			if (!landscape.getType1().equals(landscape.getType2())) {
				if (landscape.getType1().equals("TF") || landscape.getType1().equals("SWP")) {
					if (landscape.getType2().equals("TF") || landscape.getType2().equals("SWP")) {
						return true;
					}
				}
			}			
		}
		return false;
	}

}
