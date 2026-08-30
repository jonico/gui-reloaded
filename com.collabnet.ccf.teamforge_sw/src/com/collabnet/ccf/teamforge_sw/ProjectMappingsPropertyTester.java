package com.collabnet.ccf.teamforge_sw;

import org.eclipse.core.expressions.PropertyTester;

import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.MappingGroup;
import com.collabnet.ccf.model.ProjectMappings;
import com.collabnet.ccf.model.SynchronizationStatus;

public class ProjectMappingsPropertyTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof ProjectMappings && property.equals("isTeamForgeSwpLandscape")) {
			ProjectMappings projectMappings = (ProjectMappings)receiver;
			return isTeamForgeSwpLandscape(projectMappings);
		}
		if (receiver instanceof Landscape && property.equals("isTeamForgeSwpLandscape")) {
			Landscape landscape = (Landscape)receiver;
			return isTeamForgeSwpLandscape(landscape);
		}
		if (receiver instanceof SynchronizationStatus && property.equals("isTrackerPbisMapping")) {
			SynchronizationStatus projectMapping = (SynchronizationStatus)receiver;
			if (isTeamForgeSwpLandscape(projectMapping.getProjectMappings())) {
				return projectMapping.toString().indexOf("-PBI") != -1;
			}
		}
		if (receiver instanceof MappingGroup && property.equals("isTrackerPbisMapping")) {
			SynchronizationStatus projectMapping = ((MappingGroup)receiver).getFirstMapping();
			if (null != projectMapping && isTeamForgeSwpLandscape(projectMapping.getProjectMappings())) {
				return projectMapping.toString().indexOf("-PBI") != -1;
			}
		}
		if (receiver instanceof SynchronizationStatus && property.equals("isTeamForgeScrumWorksMapping")) {
			SynchronizationStatus projectMapping = (SynchronizationStatus)receiver;
			return isTeamForgeSwpLandscape(projectMapping.getProjectMappings());
		}
		if (receiver instanceof MappingGroup && property.equals("isTeamForgeScrumWorksMappingGroup")) {
			SynchronizationStatus projectMapping = ((MappingGroup)receiver).getFirstMapping();
			return projectMapping != null && isTeamForgeSwpLandscape(projectMapping.getProjectMappings());
		}		
		return false;
	}
	
	private boolean isTeamForgeSwpLandscape(ProjectMappings projectMappings) {
		if (null != projectMappings) {
			return isTeamForgeSwpLandscape(projectMappings.getLandscape());
		}
		return false;
	}
	
	private boolean isTeamForgeSwpLandscape(Landscape landscape) {
		if (null != landscape) {
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
