/*******************************************************************************
 * Copyright (c) 2011 CollabNet.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     CollabNet - initial API and implementation
 ******************************************************************************/
package com.collabnet.ccf.migration.wizards;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.api.CcfMasterClient;
import com.collabnet.ccf.api.model.ConflictResolutionPolicy;
import com.collabnet.ccf.api.model.Direction;
import com.collabnet.ccf.api.model.DirectionConfig;
import com.collabnet.ccf.api.model.Directions;
import com.collabnet.ccf.api.model.ExternalApp;
import com.collabnet.ccf.api.model.FieldMapping;
import com.collabnet.ccf.api.model.FieldMappingKind;
import com.collabnet.ccf.api.model.FieldMappingRule;
import com.collabnet.ccf.api.model.FieldMappingRuleType;
import com.collabnet.ccf.api.model.FieldMappingScope;
import com.collabnet.ccf.api.model.LandscapeConfig;
import com.collabnet.ccf.api.model.Participant;
import com.collabnet.ccf.api.model.ParticipantConfig;
import com.collabnet.ccf.api.model.RepositoryMapping;
import com.collabnet.ccf.api.model.RepositoryMappingDirection;
import com.collabnet.ccf.api.model.RepositoryMappingDirectionStatus;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.db.Filter;
import com.collabnet.ccf.dialogs.ExceptionDetailsErrorDialog;
import com.collabnet.ccf.migration.MigrationResult;
import com.collabnet.ccf.migration.dialogs.MigrateLandscapeErrorDialog;
import com.collabnet.ccf.migration.dialogs.MigrateLandscapeResultsDialog;
import com.collabnet.ccf.migration.webclient.TeamForgeClient;
import com.collabnet.ccf.model.IdentityMapping;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.schemageneration.XSLTInitialMFDGeneratorScriptGenerator;
import com.collabnet.teamforge.api.main.ProjectDO;
import com.collabnet.teamforge.api.main.UserDO;
import com.collabnet.teamforge.api.tracker.TrackerDO;

public class MigrateLandscapeWizard extends Wizard {
	private CcfDataProvider ccfDataProvider;
	private SynchronizationStatus[] projectMappings;
	private Map<SynchronizationStatus, String> projectMappingMap; // Maps project mapping to project ID
	private Map<String, ProjectDO> projectMap; // Maps project ID to project
	private Map<String, TrackerDO> trackerMap; // Maps tracker ID to tracker
	private List<String> projectIds;
	
	private Landscape landscape;
	private MigrateLandscapeWizardCcfMasterPage ccfMasterPage;
	private MigrateLandscapeWizardMappingSelectionPage mappingPage;
	private TeamForgeClient teamForgeClient;
	private int createdCount;
	private int notCreatedCount;
	private int alreadyExistedCount;
	
	private List<MigrationResult> migrationResults;
	private Exception exception;
	private boolean canceled;
	private boolean showMigrationResults;
	
	private ParticipantConfig[] participantConfigs;
	private LandscapeConfig[] landscapeConfigs;
	private DirectionConfig[] directionConfigs;
	
	private IDialogSettings settings = com.collabnet.ccf.migration.Activator.getDefault().getDialogSettings();
	
	public MigrateLandscapeWizard(Landscape landscape) {
		super();
		this.landscape = landscape;		
		teamForgeClient = TeamForgeClient.getTeamForgeClient(landscape);
	}
	
	@Override
	public void addPages() {
		super.addPages();
		setWindowTitle("Migrate Landscape to CCF 2.x");
		ccfMasterPage = new MigrateLandscapeWizardCcfMasterPage();
		addPage(ccfMasterPage);
		mappingPage = new MigrateLandscapeWizardMappingSelectionPage();
		addPage(mappingPage);
	}	

	@Override
	public boolean performFinish() {
		
		saveSelections();
		
		participantConfigs = null;
		landscapeConfigs = null;
		directionConfigs = null;
		migrationResults = new ArrayList<MigrationResult>();
		exception = null;
		canceled = false;
		showMigrationResults = true;
		
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				String taskName = "Migrating landscape";
				monitor.setTaskName(taskName);
				int taskCount = 25 + (mappingPage.getSelectedProjectMappings().length * 2) + mappingPage.getSelectedRepositoryMappings().length;
				monitor.beginTask(taskName, taskCount);	
				try {	
					projectMappings = mappingPage.getSelectedProjectMappings();
					if (null == projectMappings) {
						projectMappings = getProjectMappings(monitor);
					}
					else {
						projectIds = mappingPage.getSelectedProjectIds();
					}
					boolean resumedMappingSelected = false;
					for (SynchronizationStatus projectMapping : projectMappings) {
						if (!projectMapping.isPaused()) {
							resumedMappingSelected = true;
							break;
						}
					}
					if (resumedMappingSelected) {
						Display.getDefault().syncExec(new Runnable() {						
							public void run() {
								if (!MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Migrate Landscape to CCF 2.x", "Before migration starts, all selected CCF 1.x project mappings will be paused.  This is to prevent situation where CCF 1.x and CCF 2.x are trying to synchronize the same entities which might result in conflicts and duplicate artifacts. After the migration has been finished, please resume the repository mapping directions in CCF 2.x again and make sure that they are not resumed again in CCF 1.x. The reason this migrator does not automatically delete CCF 1.x project mappings after successful migration is that you should still have the chance to fall back to CCF 1.x if you are not satisfied with the migration result.\n\nPause selected CCF 1.x project mappings and continue with migration?")) {
									canceled = true;
									showMigrationResults = false;
								}
							}
						});
						if (true == canceled) {
							return;
						}
						monitor.subTask("Pausing CCF 1.x project mappings");
						for (SynchronizationStatus projectMapping : projectMappings) {
							if (!projectMapping.isPaused()) {
								getCcfDataProvider().pauseSynchronization(projectMapping);
							}
						}
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					
					if (null == projectMappingMap) {						
						projectMappingMap = getProjectMappingMap(monitor);
					}

					teamForgeClient.getConnection().login();
					if (!teamForgeClient.getConnection().supports54()) {
						exception = new Exception("TeamForge 5.4 or higher is required to migrate landscape.");
						return;
					}
					
					UserDO userDO = teamForgeClient.getConnection().getTeamForgeClient().getUserData(ccfMasterPage.getCcfMasterUser());
					if (!userDO.getSuperUser()) {
						exception = new Exception("TeamForge site admin privileges are required to migrate landscape.");
						return;						
					}
					
					monitor.worked(1);
					monitor.subTask("Checking for existing CCF Master participants");
					String otherType;
					if (landscape.getType1().equals("TF")) {
						otherType = landscape.getType2();
					} else {
						otherType = landscape.getType1();
					}	

					Participant[] participants = getCcfMasterClient().getParticipants();
					Participant teamForgeParticipant = null;
					Participant otherParticipant = null;
					for (Participant participant : participants) {
						if (participant.getSystemKind().equals("TF")) {
							teamForgeParticipant = participant;
							migrationResults.add(new MigrationResult("TeamForge participant already exists in CCF Master."));
						}
						else {
							otherParticipant = participant;
							migrationResults.add(new MigrationResult(getParticipantDescription(otherType) + " participant already exists in CCF Master."));
						}
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					
					boolean teamForgeParticipantAlreadyExists = teamForgeParticipant != null;
					if (null == teamForgeParticipant) {
						monitor.subTask("Creating CCF Master TeamForge participant");						
						teamForgeParticipant = new Participant();
						teamForgeParticipant.setSystemId("TeamForge");
						teamForgeParticipant.setDescription("TeamForge");
						teamForgeParticipant.setSystemKind("TF");
						if (landscape.getType2().equals("TF")) {
							teamForgeParticipant.setTimezone(landscape.getTimezone2());
						} else {
							teamForgeParticipant.setTimezone(landscape.getTimezone1());
						}
						teamForgeParticipant = getCcfMasterClient().createParticipant(teamForgeParticipant);
						migrationResults.add(new MigrationResult("TeamForge participant created in CCF Master."));
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}

					ParticipantConfig teamForgeParticipantConfig = new ParticipantConfig();
					teamForgeParticipantConfig.setName(ParticipantConfig.TF_URL);
					if (landscape.getType2().equals("TF")) {
						teamForgeParticipantConfig.setVal(landscape.getUrl(2));
					} else {
						teamForgeParticipantConfig.setVal(landscape.getUrl(1));
					}
					teamForgeParticipantConfig.setParticipant(teamForgeParticipant);
					if (createOrUpdateParticipantConfig(getCcfMasterClient(),
							teamForgeParticipant,
							teamForgeParticipantAlreadyExists,
							teamForgeParticipantConfig)) {
						migrationResults.add(new MigrationResult("TeamForge participant properties set in CCF Master."));
					}
					else {
						String message = "CCF Master landscape already exists and refers to a different TeamForge site.";
						migrationResults.add(new MigrationResult(message, MigrationResult.ERROR, new Exception(message)));
						return;
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					
					boolean otherParticipantAlreadyExists = otherParticipant != null;
					if (null == otherParticipant) {
						String otherDescription = getParticipantDescription(otherType);			
						monitor.subTask("Creating CCF Master " + otherDescription + " participant");
						otherParticipant = new Participant();
						if (otherType.equals("QC")) {
							otherParticipant.setSystemId("Quality Center");
						}
						else {
							otherParticipant.setSystemId(otherType);
						}
						otherParticipant.setDescription(otherDescription);	
						otherParticipant.setSystemKind(otherType);
						if (landscape.getType2().equals("TF")) {
							otherParticipant.setTimezone(landscape.getTimezone1());
						} else {
							otherParticipant.setTimezone(landscape.getTimezone2());
						}
						otherParticipant = getCcfMasterClient().createParticipant(otherParticipant);
						migrationResults.add(new MigrationResult(getParticipantDescription(otherType) + " participant created in CCF Master."));
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					
					ParticipantConfig otherParticipantConfig = new ParticipantConfig();
					if (otherType.equals("SWP")) {
						otherParticipantConfig.setName(ParticipantConfig.SWP_URL);
					}
					else {
						otherParticipantConfig.setName(ParticipantConfig.QC_URL);
					}		
					if (landscape.getType2().equals("TF")) {
						otherParticipantConfig.setVal(landscape.getUrl(1));
					} else {
						otherParticipantConfig.setVal(landscape.getUrl(2));
					}		
					otherParticipantConfig.setParticipant(otherParticipant);
					if (createOrUpdateParticipantConfig(getCcfMasterClient(),
							otherParticipant,
							otherParticipantAlreadyExists,
							otherParticipantConfig)) {
						migrationResults.add(new MigrationResult(getParticipantDescription(otherType) + " participant properties set in CCF Master."));
					}
					else {
						String participantDescription;
						if (otherType.equals("SWP")) {
							participantDescription = "ScrumWorks Pro";
						}
						else {
							participantDescription = "Quality Center";
						}
						String message = "CCF Master landscape already exists and refers to a different " + participantDescription + " site.";
						migrationResults.add(new MigrationResult(message, MigrationResult.ERROR, new Exception(message)));
						return;
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}										
					monitor.subTask("Checking for existing CCF Master landscape");
					com.collabnet.ccf.api.model.Landscape ccfMasterLandscape = null;
					com.collabnet.ccf.api.model.Landscape[] landscapes = getCcfMasterClient().getLandscapes(true);
					for (com.collabnet.ccf.api.model.Landscape landscape : landscapes) {
						ccfMasterLandscape = landscape;
						migrationResults.add(new MigrationResult("Landscape " + ccfMasterLandscape.getName() + " already exists in CCF Master."));
						break;
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					
					boolean landscapeAlreadyExists = ccfMasterLandscape != null;
					
					if (null == ccfMasterLandscape) {
						monitor.subTask("Creating CCF Master landscape");
						ccfMasterLandscape = new com.collabnet.ccf.api.model.Landscape();
						ccfMasterLandscape.setName(landscape.getDescription());
						ccfMasterLandscape.setParticipant(otherParticipant);
						ccfMasterLandscape.setTeamForge(teamForgeParticipant);
						ccfMasterLandscape = getCcfMasterClient().createLandscape(ccfMasterLandscape);												
						migrationResults.add(new MigrationResult("Landscape " + ccfMasterLandscape.getName() + " created in CCF Master."));
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					
					String teamForgeUsername = null;
					String teamForgePassword = null;
					String otherUsername = null;
					String otherPassword = null;
					String swpResyncUsername = null;
					String swpResyncPassword = null;
					if (landscape.getType2().equals("TF")) {
						teamForgeUsername = landscape.getProperties2().getProperty(Activator.PROPERTIES_SFEE_USER, "");
						teamForgePassword = landscape.getProperties2().getProperty(Activator.PROPERTIES_SFEE_PASSWORD, "");
						if (otherType.equals("QC")) {
							otherUsername = landscape.getProperties1().getProperty(Activator.PROPERTIES_QC_USER, "");
							otherPassword = landscape.getProperties1().getProperty(Activator.PROPERTIES_QC_PASSWORD, "");								
						}
						else if (otherType.equals("SWP")) {
							otherUsername = landscape.getProperties1().getProperty(Activator.PROPERTIES_SW_USER, "");
							otherPassword = landscape.getProperties1().getProperty(Activator.PROPERTIES_SW_PASSWORD, "");
							swpResyncUsername = landscape.getProperties1().getProperty(Activator.PROPERTIES_SW_RESYNC_USER, "");
							swpResyncPassword = landscape.getProperties1().getProperty(Activator.PROPERTIES_SW_RESYNC_PASSWORD, "");		
						}
					} else {
						teamForgeUsername = landscape.getProperties1().getProperty(Activator.PROPERTIES_SFEE_USER, "");
						teamForgePassword = landscape.getProperties1().getProperty(Activator.PROPERTIES_SFEE_PASSWORD, "");
						if (otherType.equals("QC")) {
							otherUsername = landscape.getProperties2().getProperty(Activator.PROPERTIES_QC_USER, "");
							otherPassword = landscape.getProperties2().getProperty(Activator.PROPERTIES_QC_PASSWORD, "");								
						}
						else if (otherType.equals("SWP")) {
							otherUsername = landscape.getProperties2().getProperty(Activator.PROPERTIES_SW_USER, "");
							otherPassword = landscape.getProperties2().getProperty(Activator.PROPERTIES_SW_PASSWORD, "");
							swpResyncUsername = landscape.getProperties2().getProperty(Activator.PROPERTIES_SW_RESYNC_USER, "");
							swpResyncPassword = landscape.getProperties2().getProperty(Activator.PROPERTIES_SW_RESYNC_PASSWORD, "");		
						}
					}
					
					LandscapeConfig landscapeConfig = new LandscapeConfig();
					landscapeConfig.setLandscape(ccfMasterLandscape);
					landscapeConfig.setName(LandscapeConfig.TF_USERNAME);
					landscapeConfig.setVal(teamForgeUsername);
					createOrUpdateLandscapeConfig(getCcfMasterClient(), ccfMasterLandscape, landscapeAlreadyExists, landscapeConfig);
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					landscapeConfig.setName(LandscapeConfig.TF_PASSWORD);
					landscapeConfig.setVal(obfuscatePassword(teamForgePassword));
					createOrUpdateLandscapeConfig(getCcfMasterClient(), ccfMasterLandscape, landscapeAlreadyExists, landscapeConfig);
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					if (null != otherUsername) {
						if (otherType.equals("SWP")) {
							landscapeConfig.setName(LandscapeConfig.SWP_USERNAME);
						}
						else {
							landscapeConfig.setName(LandscapeConfig.QC_USERNAME);
						}
						landscapeConfig.setVal(otherUsername);
						createOrUpdateLandscapeConfig(getCcfMasterClient(), ccfMasterLandscape, landscapeAlreadyExists, landscapeConfig);
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					if (null != otherPassword) {
						if (otherType.equals("SWP")) {
							landscapeConfig.setName(LandscapeConfig.SWP_PASSWORD);
						}
						else {
							landscapeConfig.setName(LandscapeConfig.QC_PASSWORD);
						}
						landscapeConfig.setVal(obfuscatePassword(otherPassword));
						createOrUpdateLandscapeConfig(getCcfMasterClient(), ccfMasterLandscape, landscapeAlreadyExists, landscapeConfig);
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					if (null != swpResyncUsername) {
						landscapeConfig.setName(LandscapeConfig.SWP_RESYNC_USERNAME);
						landscapeConfig.setVal(swpResyncUsername);
						createOrUpdateLandscapeConfig(getCcfMasterClient(), ccfMasterLandscape, landscapeAlreadyExists, landscapeConfig);
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					if (null != swpResyncPassword) {
						landscapeConfig.setName(LandscapeConfig.SWP_RESYNC_PASSWORD);
						landscapeConfig.setVal(obfuscatePassword(swpResyncPassword));
						createOrUpdateLandscapeConfig(getCcfMasterClient(), ccfMasterLandscape, landscapeAlreadyExists, landscapeConfig);
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					migrationResults.add(new MigrationResult("Landscape " + ccfMasterLandscape.getName() + " properties set in CCF Master."));
					
					Direction forward = null;
					Direction reverse = null;
					if (landscapeAlreadyExists) {
						monitor.subTask("Checking for existing CCF Master directions");
						Direction[] directions = getCcfMasterClient().getDirections(ccfMasterLandscape, null, true);
						for (Direction direction : directions) {
							if (direction.getLandscape().getId() == ccfMasterLandscape.getId()) {
								if (direction.getDirections().equals(Directions.FORWARD)) {
									forward = direction;
									migrationResults.add(new MigrationResult("Direction " + forward.getDescription() + " (FORWARD) already exists in CCF Master."));
								}
								else if (direction.getDirections().equals(Directions.REVERSE)) {
									reverse = direction;
									migrationResults.add(new MigrationResult("Direction " + reverse.getDescription() + " (REVERSE) already exists in CCF Master."));
								}
								if (null != forward && null != reverse) {
									break;
								}
							}
						}
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					
					if (null == forward || null == reverse) {
						monitor.subTask("Creating CCF Master directions");
					}
					
					if (null == forward) {						
						forward = new Direction();
						forward.setLandscape(ccfMasterLandscape);
						forward.setDirections(Directions.FORWARD);
						if (landscape.getType1().equals("TF")) {
							forward.setDescription(landscape.getType1() + landscape.getType2());
						} else {
							forward.setDescription(landscape.getType2() + landscape.getType1());
						}
						forward.setShouldStartAutomatically(Boolean.valueOf(false));
						forward = getCcfMasterClient().createDirection(forward);
						migrationResults.add(new MigrationResult("Direction " + forward.getDescription() + " (FORWARD) created in CCF Master."));
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					
					DirectionConfig forwardConfig = new DirectionConfig();
					String teamForgeMaxAttachmentSize = null;
					String otherMaxAttachmentSize = null;
					if (landscape.getType1().equals("TF")) {
						forwardConfig.setVal(landscape.getLogMessageTemplate1());
						teamForgeMaxAttachmentSize = landscape.getProperties1().getProperty(Activator.PROPERTIES_SFEE_ATTACHMENT_SIZE, "10485760");
						if (otherType.equals("SWP")) {
							otherMaxAttachmentSize = landscape.getProperties2().getProperty(Activator.PROPERTIES_SW_ATTACHMENT_SIZE, "10485760");
						}
						else {
							otherMaxAttachmentSize = landscape.getProperties2().getProperty(Activator.PROPERTIES_QC_ATTACHMENT_SIZE, "10485760");
						}
					} else {
						forwardConfig.setVal(landscape.getLogMessageTemplate2());
						teamForgeMaxAttachmentSize = landscape.getProperties2().getProperty(Activator.PROPERTIES_SFEE_ATTACHMENT_SIZE, "10485760");	
						if (otherType.equals("SWP")) {
							otherMaxAttachmentSize = landscape.getProperties1().getProperty(Activator.PROPERTIES_SW_ATTACHMENT_SIZE, "10485760");
						}
						else {
							otherMaxAttachmentSize = landscape.getProperties1().getProperty(Activator.PROPERTIES_QC_ATTACHMENT_SIZE, "10485760");
						}				
					}	
					forwardConfig.setDirection(forward);
					forwardConfig.setName(DirectionConfig.LOG_MESSAGE_TEMPLATE);
					createOrUpdateDirectionConfig(getCcfMasterClient(), forward, forwardConfig);
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					if (null != teamForgeMaxAttachmentSize) {
						forwardConfig.setName(DirectionConfig.TF_MAX_ATTACHMENT_SIZE);
						forwardConfig.setVal(teamForgeMaxAttachmentSize);
						createOrUpdateDirectionConfig(getCcfMasterClient(), forward, forwardConfig);
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					migrationResults.add(new MigrationResult("Direction " + forward.getDescription() + " (FORWARD) properties set in CCF Master."));
					
					if (null == reverse) {
						reverse = new Direction();
						reverse.setLandscape(ccfMasterLandscape);
						reverse.setDirections(Directions.REVERSE);
						if (landscape.getType1().equals("TF")) {
							reverse.setDescription(landscape.getType2() + landscape.getType1());
						} else {
							reverse.setDescription(landscape.getType1() + landscape.getType2());
						}
						reverse.setShouldStartAutomatically(Boolean.valueOf(false));
						reverse = getCcfMasterClient().createDirection(reverse);
						migrationResults.add(new MigrationResult("Direction " + reverse.getDescription() + " (REVERSE) created in CCF Master."));
						
						// Null out existing direction configs because next time we check them we want them to be retrieved again since creating
						// the reverse direction will have resulted in more configs being created automatically.
						directionConfigs = null;
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					
					DirectionConfig reverseConfig = new DirectionConfig();
					teamForgeMaxAttachmentSize = null;
					otherMaxAttachmentSize = null;
					if (landscape.getType1().equals("TF")) {
						reverseConfig.setVal(landscape.getLogMessageTemplate2());
						teamForgeMaxAttachmentSize = landscape.getProperties1().getProperty(Activator.PROPERTIES_SFEE_ATTACHMENT_SIZE, "10485760");
						if (otherType.equals("SWP")) {
							otherMaxAttachmentSize = landscape.getProperties2().getProperty(Activator.PROPERTIES_SW_ATTACHMENT_SIZE, "10485760");
						}
						else {
							otherMaxAttachmentSize = landscape.getProperties2().getProperty(Activator.PROPERTIES_QC_ATTACHMENT_SIZE, "10485760");
						}
					} else {
						reverseConfig.setVal(landscape.getLogMessageTemplate1());
						teamForgeMaxAttachmentSize = landscape.getProperties2().getProperty(Activator.PROPERTIES_SFEE_ATTACHMENT_SIZE, "10485760");
						if (otherType.equals("SWP")) {
							otherMaxAttachmentSize = landscape.getProperties1().getProperty(Activator.PROPERTIES_SW_ATTACHMENT_SIZE, "10485760");
						}
						else {
							otherMaxAttachmentSize = landscape.getProperties1().getProperty(Activator.PROPERTIES_QC_ATTACHMENT_SIZE, "10485760");
						}
					}
					reverseConfig.setDirection(reverse);
					reverseConfig.setName(DirectionConfig.LOG_MESSAGE_TEMPLATE);
					createOrUpdateDirectionConfig(getCcfMasterClient(), reverse, reverseConfig);
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					if (null != otherMaxAttachmentSize) {
						if (otherType.equals("SWP")) {
							reverseConfig.setName(DirectionConfig.SWP_MAX_ATTACHMENT_SIZE);
						}
						else {
							reverseConfig.setName(DirectionConfig.QC_MAX_ATTACHMENT_SIZE);
						}
						reverseConfig.setVal(otherMaxAttachmentSize);
						createOrUpdateDirectionConfig(getCcfMasterClient(), reverse, reverseConfig);
					}	
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					migrationResults.add(new MigrationResult("Direction " + reverse.getDescription() + " (REVERSE) properties set in CCF Master."));

					Map<String, ExternalApp> externalAppMap = new HashMap<String, ExternalApp>();
					monitor.subTask("Creating CCF Master external applications:");
					ExternalApp[] externalApps = getCcfMasterClient().getExternalApps(ccfMasterLandscape, true);
					for (String project : projectIds) {
						ProjectDO projectDO = projectMap.get(project);
						ExternalApp externalApp = new ExternalApp();
						externalApp.setProjectPath(projectDO.getPath());
						externalApp.setProjectTitle(projectDO.getTitle());
						externalApp.setLandscape(ccfMasterLandscape);
						ExternalApp existingApp = getExternalApp(externalApp, externalApps);
						if (null != existingApp) {
							externalApp = existingApp;
							migrationResults.add(new MigrationResult("External application " + externalApp.getLinkId() + " (" + project + ") already exists in CCF Master."));
						} else {
							monitor.subTask("Creating CCF Master external applications: " + externalApp.getProjectTitle());
							externalApp = getCcfMasterClient().createExternalApp(externalApp);
							migrationResults.add(new MigrationResult("External application " + externalApp.getLinkId() + " (" + project + ") created in CCF Master."));
						}
						externalAppMap.put(project, externalApp);
						if (monitor.isCanceled()) {
							canceled = true;
							return;
						}
					}
					monitor.worked(1);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}

					monitor.subTask("Creating CCF Master repository mappings:");
					List<String> repositoryMappingList = new ArrayList<String>();
					RepositoryMapping[] repositoryMappings = getCcfMasterClient().getRepositoryMappings(true);
					for (SynchronizationStatus projectMapping : projectMappings) {
						String projectId = projectMappingMap.get(projectMapping);
						if (null != projectId) {
							ExternalApp externalApp = externalAppMap.get(projectId);
							if (null != externalApp) {
								String teamForgeRepositoryId = null;
								String participantRepositoryId = null;
								if (projectMapping.getSourceSystemKind().startsWith("TF")) {
									teamForgeRepositoryId = projectMapping.getSourceRepositoryId();
									participantRepositoryId = projectMapping.getTargetRepositoryId();
								}
								else if (projectMapping.getTargetSystemKind().startsWith("TF")) {
									teamForgeRepositoryId = projectMapping.getTargetRepositoryId();
									participantRepositoryId = projectMapping.getSourceRepositoryId();
								}
								if (null != teamForgeRepositoryId) {
									if (!repositoryMappingList.contains(projectId + teamForgeRepositoryId + participantRepositoryId)) {										
										RepositoryMapping repositoryMapping = new RepositoryMapping();
										String description = null;
										if (otherType.equals("SWP")) {
											int index = participantRepositoryId.lastIndexOf("-");
											if (-1 != index) {
												description = participantRepositoryId.substring(index + 1);
											}
										}
										if (null == description) {
											description = teamForgeRepositoryId + " " + "\u21D4" + " " + participantRepositoryId;
										}
										repositoryMapping.setDescription(description);
										repositoryMapping.setExternalApp(externalApp);
										repositoryMapping.setParticipantRepositoryId(participantRepositoryId);
										repositoryMapping.setTeamForgeRepositoryId(teamForgeRepositoryId);
										RepositoryMapping checkMapping = getRepositoryMapping(repositoryMapping, repositoryMappings);
										if (null == checkMapping) {
											monitor.subTask("Creating CCF Master repository mappings: " + repositoryMapping.getDescription());
											repositoryMapping = getCcfMasterClient().createRepositoryMapping(repositoryMapping);
											migrationResults.add(new MigrationResult("Repository mapping " + repositoryMapping.getDescription() + " created in CCF Master."));
										} else {
											repositoryMapping = checkMapping;
											migrationResults.add(new MigrationResult("Repository mapping " + repositoryMapping.getDescription() + " already exists in CCF Master."));										
										}
										repositoryMappingList.add(projectId + teamForgeRepositoryId + participantRepositoryId);
									}
								}
							}
							if (monitor.isCanceled()) {
								canceled = true;
								return;
							}
						}
						monitor.worked(1);
						if (monitor.isCanceled()) {
							canceled = true;
							return;
						}
					}
					
					monitor.subTask("Creating CCF Master repository mapping directions:");
					repositoryMappings = getCcfMasterClient().getRepositoryMappings(ccfMasterLandscape, true);
					RepositoryMappingDirection[] repositoryMappingDirections = getCcfMasterClient().getRepositoryMappingDirections(ccfMasterLandscape, true);
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
					for (SynchronizationStatus projectMapping : projectMappings) {
						if (projectMapping.getSourceSystemKind().startsWith("TF") || projectMapping.getTargetSystemKind().startsWith("TF")) {
							RepositoryMappingDirection repositoryMappingDirection = new RepositoryMappingDirection();
							if (projectMapping.getTargetSystemKind().startsWith("TF")) {
								repositoryMappingDirection.setDirection(Directions.REVERSE);
							} else {
								repositoryMappingDirection.setDirection(Directions.FORWARD);
							}
							repositoryMappingDirection.setRepositoryMapping(getRepositoryMapping(projectMapping, repositoryMappings));
							if (projectMapping.isPaused()) {
								repositoryMappingDirection.setStatus(RepositoryMappingDirectionStatus.PAUSED);
							} else {
								repositoryMappingDirection.setStatus(RepositoryMappingDirectionStatus.RUNNING);
							}
							repositoryMappingDirection.setLastSourceArtifactModificationDate(projectMapping.getSourceLastModificationTime());
							repositoryMappingDirection.setLastSourceArtifactVersion(projectMapping.getSourceLastArtifactVersion());
							repositoryMappingDirection.setLastSourceArtifactId(projectMapping.getSourceLastArtifactId());
							String conflictResolutionPolicy = projectMapping.getConflictResolutionPriority();
							if (conflictResolutionPolicy.equals(ConflictResolutionPolicy.alwaysIgnore.toString())) {
								repositoryMappingDirection.setConflictResolutionPolicy(ConflictResolutionPolicy.alwaysIgnore);
							}
							else if (conflictResolutionPolicy.equals(ConflictResolutionPolicy.quarantineArtifact.toString())) {
								repositoryMappingDirection.setConflictResolutionPolicy(ConflictResolutionPolicy.quarantineArtifact);
							}	
							else if (conflictResolutionPolicy.equals(ConflictResolutionPolicy.alwaysOverrideAndIgnoreLocks.toString())) {
								repositoryMappingDirection.setConflictResolutionPolicy(ConflictResolutionPolicy.alwaysOverrideAndIgnoreLocks);
							}
							else {
								repositoryMappingDirection.setConflictResolutionPolicy(ConflictResolutionPolicy.alwaysOverride);
							}
							RepositoryMappingDirection checkRepositoryMappingDirection = getRepositoryMappingDirection(repositoryMappingDirection, repositoryMappingDirections);
							if (null == checkRepositoryMappingDirection) {
								monitor.subTask("Creating CCF Master repository mapping directions: " + repositoryMappingDirection.getRepositoryMapping().getDescription() + " (" + repositoryMappingDirection.getDirection() + ")");
								repositoryMappingDirection = getCcfMasterClient().createRepositoryMappingDirection(repositoryMappingDirection);
								migrationResults.add(new MigrationResult("Repository mapping direction " + repositoryMappingDirection.getRepositoryMapping().getDescription() + " (" + repositoryMappingDirection.getDirection() + ") created in CCF Master."));
								if (projectMapping.getSourceRepositoryKind().contains("Template")) {
									FieldMapping fieldMapping = new FieldMapping();
									fieldMapping.setParent(repositoryMappingDirection);
									fieldMapping.setScope(FieldMappingScope.CCF_CORE);
									String fieldMappingName;
									int index = projectMapping.getSourceRepositoryKind().indexOf(".xsl");
									if (-1 == index) {
										fieldMappingName = projectMapping.getSourceRepositoryKind();
									}
									else {
										fieldMappingName = projectMapping.getSourceRepositoryKind().substring(0, index);
									}
									fieldMapping.setName(fieldMappingName);
									fieldMapping.setKind(FieldMappingKind.CUSTOM_XSLT);
									fieldMapping = getCcfMasterClient().createFieldMapping(fieldMapping);
									repositoryMappingDirection.setActiveFieldMapping(fieldMapping);
									repositoryMappingDirection = getCcfMasterClient().updateRepositoryMappingDirection(repositoryMappingDirection);
								}
								else if (projectMapping.getSourceRepositoryKind().contains(".xsl")) {
									List<FieldMappingRule> fieldMappingRules = new ArrayList<FieldMappingRule>();
									File preFile = projectMapping.getGenericArtifactToSourceRepositorySchemaFile();
									File postFile = projectMapping.getTargetRepositorySchemaToGenericArtifactFile();
									File mfdFile = projectMapping.getMappingFile(projectMapping.getMFDFileName());
									File mainFile = projectMapping.getGraphicalXslFile();
									File sourceRepositorySchemaFile = projectMapping.getSourceRepositorySchemaFile();
									File targetRepositorySchemaFile = projectMapping.getTargetRepositorySchemaFile();
									FieldMapping fieldMapping = new FieldMapping();
									fieldMapping.setParent(repositoryMappingDirection);
									fieldMapping.setScope(FieldMappingScope.REPOSITORY_MAPPING_DIRECTION);
									fieldMapping.setKind(FieldMappingKind.MAPFORCE);	
									fieldMapping.setName(FieldMappingKind.MAPFORCE.toString());								
									if (sourceRepositorySchemaFile.exists()) {
										fieldMappingRules.add(getFieldMappingRule(FieldMappingRuleType.SOURCE_REPOSITORY_LAYOUT, sourceRepositorySchemaFile));
									}
									if (targetRepositorySchemaFile.exists()) {
										fieldMappingRules.add(getFieldMappingRule(FieldMappingRuleType.TARGET_REPOSITORY_LAYOUT, targetRepositorySchemaFile));
									}
									if (preFile.exists()) {
										fieldMappingRules.add(getFieldMappingRule(FieldMappingRuleType.MAPFORCE_PRE, preFile));
									}
									if (mainFile.exists()) {
										fieldMappingRules.add(getFieldMappingRule(FieldMappingRuleType.MAPFORCE_MAIN, mainFile));
									}
									if (postFile.exists()) {
										fieldMappingRules.add(getFieldMappingRule(FieldMappingRuleType.MAPFORCE_POST, postFile));
									}
									if (mfdFile.exists()) {
										XSLTInitialMFDGeneratorScriptGenerator generator = new XSLTInitialMFDGeneratorScriptGenerator();
										Document mfdDocument = generator.generateCreateInitialMFDScript(mfdFile.getAbsolutePath(), projectMapping.getSourceRepositorySchemaFileName(), projectMapping.getTargetRepositorySchemaFileName()); //$NON-NLS-1$ //$NON-NLS-2$
										FieldMappingRule mfdFieldMappingRule = getFieldMappingRule(FieldMappingRuleType.MAPFORCE_MFD, mfdFile);
										mfdFieldMappingRule.setXmlContent(mfdDocument.asXML());
										fieldMappingRules.add(mfdFieldMappingRule);
									}
									fieldMapping.setRules(fieldMappingRules);
									fieldMapping = getCcfMasterClient().createFieldMapping(fieldMapping);
									repositoryMappingDirection.setActiveFieldMapping(fieldMapping);
									repositoryMappingDirection = getCcfMasterClient().updateRepositoryMappingDirection(repositoryMappingDirection);
								}
								else {
									FieldMapping fieldMapping = new FieldMapping();
									fieldMapping.setParent(repositoryMappingDirection);
									fieldMapping.setScope(FieldMappingScope.REPOSITORY_MAPPING_DIRECTION);
									fieldMapping.setKind(FieldMappingKind.CUSTOM_XSLT);
									fieldMapping.setName(FieldMappingKind.CUSTOM_XSLT.toString());
									FieldMappingRule fieldMappingRule = new FieldMappingRule();
									fieldMappingRule.setType(FieldMappingRuleType.CUSTOM_XSLT_DOCUMENT);
									fieldMappingRule.setSource("source");
									fieldMappingRule.setSourceIsTopLevelAttribute(Boolean.valueOf(false));
									fieldMappingRule.setTarget("target");
									fieldMappingRule.setTargetIsTopLevelAttribute(Boolean.valueOf(false));
									File xslFile = projectMapping.getXslFile();
									if (!xslFile.exists()) {
										xslFile = projectMapping.getSampleXslFile();
									}								
									fieldMappingRule.setXmlContent(CcfMasterClient.readFile(xslFile));
									List<FieldMappingRule> fieldMappingRules = new ArrayList<FieldMappingRule>();
									fieldMappingRules.add(fieldMappingRule);
									fieldMapping.setRules(fieldMappingRules);
									fieldMapping = getCcfMasterClient().createFieldMapping(fieldMapping);
									repositoryMappingDirection.setActiveFieldMapping(fieldMapping);
									repositoryMappingDirection = getCcfMasterClient().updateRepositoryMappingDirection(repositoryMappingDirection);
								}
								
							} else {
								repositoryMappingDirection = checkRepositoryMappingDirection;
								migrationResults.add(new MigrationResult("Repository mapping direction " + repositoryMappingDirection.getRepositoryMapping().getDescription() + " (" + repositoryMappingDirection.getDirection() + ") already exists in CCF Master."));
							}
							if (monitor.isCanceled()) {
								canceled = true;
								return;
							}
						}
						monitor.worked(1);
						if (monitor.isCanceled()) {
							canceled = true;
							return;
						}						
					}
					repositoryMappingDirections = getCcfMasterClient().getRepositoryMappingDirections(ccfMasterLandscape, true);
				
					monitor.subTask("Creating CCF Master identity mappings:");
					
					createdCount = 0;
					notCreatedCount = 0;
					alreadyExistedCount = 0;
					
					for (RepositoryMapping repositoryMapping : mappingPage.getSelectedRepositoryMappings()) {
					
						List<String> identityMappingList = new ArrayList<String>();

						Filter forwardSourceRepositoryFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_ID, repositoryMapping.getTeamForgeRepositoryId(), true);
						Filter forwardTargetRepositoryFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID, repositoryMapping.getParticipantRepositoryId(), true);
						Filter[] forwardFilter = { forwardSourceRepositoryFilter, forwardTargetRepositoryFilter };
						Filter reverseSourceRepositoryFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_SOURCE_REPOSITORY_ID, repositoryMapping.getParticipantRepositoryId(), true);
						Filter reverseTargetRepositoryFilter = new Filter(CcfDataProvider.IDENTITY_MAPPING_TARGET_REPOSITORY_ID, repositoryMapping.getTeamForgeRepositoryId(), true);
						Filter[] reverseFilter = { reverseSourceRepositoryFilter, reverseTargetRepositoryFilter };						
						Filter[][] filter = { forwardFilter, reverseFilter };
						
						IdentityMapping[] identityMappings = getCcfDataProvider().getIdentityMappings(landscape, filter);
						
						// First select source = TF, version != -1
						for (IdentityMapping mapping : identityMappings) {
							if (mapping.getSourceSystemKind().startsWith("TF")
									&& !mapping.getArtifactType().equals("attachment")
									&& !"-1".equals(mapping.getSourceArtifactVersion())
									&& !"-1".equals(mapping.getTargetArtifactVersion())) {
								com.collabnet.ccf.api.model.IdentityMapping identityMapping = getIdentityMapping(mapping);
								if (!identityMappingList.contains(identityMapping.getSourceArtifactId() + identityMapping.getArtifactType())) {
									identityMapping.setRepositoryMapping(getRepositoryMapping(mapping, repositoryMappings));
									try {
										getCcfMasterClient().createIdentityMapping(identityMapping);
										createdCount++;
									} catch (Exception e) {
										handleIdentityMappingMigrationError(mapping, e);
									}
									identityMappingList.add(identityMapping.getSourceArtifactId() + identityMapping.getArtifactType());
									monitor.subTask("Creating CCF Master identity mappings: " + getStatusMessage(""));
								}
								if (monitor.isCanceled()) {
									if (0 < createdCount || 0 < notCreatedCount || 0 < alreadyExistedCount) {
										migrationResults.add(new MigrationResult(getStatusMessage("identity mappings")));
									}
									canceled = true;
									return;
								}
							}
						}
	
						// Next, target = TF, version != -1 (and attachments)
						for (IdentityMapping mapping : identityMappings) {
							if (mapping.getTargetSystemKind().startsWith("TF")
									&& (mapping.getArtifactType().equals("attachment") ||
									(!"-1".equals(mapping.getSourceArtifactVersion())
									&& !"-1".equals(mapping.getTargetArtifactVersion())))) {
								com.collabnet.ccf.api.model.IdentityMapping identityMapping = getIdentityMapping(mapping);
								if (!identityMappingList.contains(identityMapping.getSourceArtifactId() + identityMapping.getArtifactType())) {
									identityMapping.setRepositoryMapping(getRepositoryMapping(mapping, repositoryMappings));
									try {
										getCcfMasterClient().createIdentityMapping(identityMapping);
										createdCount++;
									} catch (Exception e) {
										handleIdentityMappingMigrationError(mapping, e);
									}
									identityMappingList.add(identityMapping.getSourceArtifactId() + identityMapping.getArtifactType());
									monitor.subTask("Creating CCF Master identity mappings: " + getStatusMessage(""));
								}
								if (monitor.isCanceled()) {
									if (0 < createdCount || 0 < notCreatedCount || 0 < alreadyExistedCount) {
										migrationResults.add(new MigrationResult(getStatusMessage("identity mappings")));
									}
									canceled = true;
									return;
								}
							}
						}
	
						// Next source = TF, version = -1
						for (IdentityMapping mapping : identityMappings) {
							if (mapping.getSourceSystemKind().startsWith("TF")
									&& !mapping.getArtifactType().equals("attachment")
									&& ("-1".equals(mapping.getSourceArtifactVersion())
									|| "-1".equals(mapping.getTargetArtifactVersion()))) {
								com.collabnet.ccf.api.model.IdentityMapping identityMapping = getIdentityMapping(mapping);
								if (!identityMappingList.contains(identityMapping.getSourceArtifactId() + identityMapping.getArtifactType())) {
									identityMapping.setRepositoryMapping(getRepositoryMapping(mapping, repositoryMappings));
									try {
										getCcfMasterClient().createIdentityMapping(identityMapping);
										createdCount++;
									} catch (Exception e) {
										handleIdentityMappingMigrationError(mapping, e);
									}
									identityMappingList.add(identityMapping.getSourceArtifactId() + identityMapping.getArtifactType());
									monitor.subTask("Creating CCF Master identity mappings: " + getStatusMessage(""));
								}
								if (monitor.isCanceled()) {
									if (0 < createdCount || 0 < notCreatedCount || 0 < alreadyExistedCount) {
										migrationResults.add(new MigrationResult(getStatusMessage("identity mappings")));
									}
									canceled = true;
									return;
								}
							}
						}
	
						// Finally target = TF, version = -1
						for (IdentityMapping mapping : identityMappings) {
							if (mapping.getTargetSystemKind().startsWith("TF")
									&& !mapping.getArtifactType().equals("attachment")
									&& ("-1".equals(mapping.getSourceArtifactVersion())
									|| "-1".equals(mapping.getTargetArtifactVersion()))) {
								com.collabnet.ccf.api.model.IdentityMapping identityMapping = getIdentityMapping(mapping);
								if (!identityMappingList.contains(identityMapping.getSourceArtifactId() + identityMapping.getArtifactType())) {
									identityMapping.setRepositoryMapping(getRepositoryMapping(mapping, repositoryMappings));
									try {
										getCcfMasterClient().createIdentityMapping(identityMapping);
										createdCount++;
									} catch (Exception e) {
										handleIdentityMappingMigrationError(mapping, e);
									}
									identityMappingList.add(identityMapping.getSourceArtifactId() + identityMapping.getArtifactType());
									monitor.subTask("Creating CCF Master identity mappings: " + getStatusMessage(""));
								}
								if (monitor.isCanceled()) {
									if (0 < createdCount || 0 < notCreatedCount || 0 < alreadyExistedCount) {
										migrationResults.add(new MigrationResult(getStatusMessage("identity mappings")));
									}
									canceled = true;
									return;
								}
							}
						}
						monitor.worked(1);
					}

					if (0 < createdCount || 0 < notCreatedCount || 0 < alreadyExistedCount) {
						migrationResults.add(new MigrationResult(getStatusMessage("identity mappings")));
					}
					if (monitor.isCanceled()) {
						canceled = true;
						return;
					}
				} catch (Exception e) {
					exception = e;
					return;					
				} finally {
					monitor.done();
				}
			}			
		};
		
		try {
			getContainer().run(true, true, runnable);
		} catch (Exception e) {
			Activator.handleError(e);
			if (null != e.getMessage() && e.getMessage().contains("<html>")) {
				MigrateLandscapeErrorDialog dialog = new MigrateLandscapeErrorDialog(getShell(), e);
				dialog.open();				
			} else {
				ExceptionDetailsErrorDialog.openError(getShell(), "Migrate Landscape to CCF 2.x", e.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
			}
			migrationResults.add(new MigrationResult(e));
			return false;
		}
		
		if (null != exception) {
			Activator.handleError(exception);
			if (null != exception.getMessage() && exception.getMessage().contains("<html>")) {
				MigrateLandscapeErrorDialog dialog = new MigrateLandscapeErrorDialog(getShell(), exception);
				dialog.open();					
			} else {
				ExceptionDetailsErrorDialog.openError(getShell(), "Migrate Landscape to CCF 2.x", exception.getMessage(), new Status(IStatus.ERROR, Activator.PLUGIN_ID, exception.getLocalizedMessage(), exception));
			}
			migrationResults.add(new MigrationResult(exception));
		}
		
		if (showMigrationResults) {
			if (canceled) {
				migrationResults.add(new MigrationResult(new Exception("Migration canceled by user.")));
			}
			
			MigrationResult[] migrationResultArray = new MigrationResult[migrationResults.size()];
			migrationResults.toArray(migrationResultArray);
			MigrateLandscapeResultsDialog dialog = new MigrateLandscapeResultsDialog(getShell(), migrationResultArray);
			if (MigrateLandscapeResultsDialog.CANCEL == dialog.open()) {
				return false;
			}
		}
		
		return exception == null && !canceled;
	}
	
	public List<String> getProjectIds() {
		return projectIds;
	}

	protected Map<SynchronizationStatus, String> getProjectMappingMap(IProgressMonitor monitor) throws RemoteException {
		monitor.subTask("Compiling TeamForge project list");
		projectMappingMap = new HashMap<SynchronizationStatus, String>();
		projectIds = new ArrayList<String>();
		projectMap = new HashMap<String, ProjectDO>();
		trackerMap = new HashMap<String, TrackerDO>();
		for (SynchronizationStatus projectMapping : projectMappings) {
			String repositoryId = null;
			if (projectMapping.getSourceSystemKind().startsWith("TF")) {
				repositoryId = projectMapping.getSourceRepositoryId();
			}
			else if (projectMapping.getTargetSystemKind().startsWith("TF")) {
				repositoryId = projectMapping.getTargetRepositoryId();
			}
			if (null != repositoryId) {
				String projectId = null;
				if (repositoryId.startsWith("proj")) {
					int index = repositoryId.indexOf("-");
					if (-1 == index) {
						projectId = repositoryId;
					} else {
						projectId = repositoryId.substring(0, index);
					}
				}
				else if (repositoryId.startsWith("tracker")) {
					String trackerId = null;
					int index = repositoryId.indexOf("-");
					if (-1 == index) {
						trackerId = repositoryId;
					} else {
						trackerId = repositoryId.substring(0, index);
					}
					if (null != trackerId) {
						TrackerDO tracker = teamForgeClient.getConnection().getTrackerClient().getTrackerData(trackerId);
						if (null != tracker) {
							projectId = tracker.getProjectId();
							trackerMap.put(tracker.getId(), tracker);
						}
					}
				}
				if (null != projectId) {
					projectMappingMap.put(projectMapping, projectId);
				}
				if (null != projectId && !projectIds.contains(projectId)) {
					ProjectDO projectDO = teamForgeClient.getConnection().getTeamForgeClient().getProjectData(projectId);
					if (null != projectDO) {
						projectIds.add(projectId);
						projectMap.put(projectId, projectDO);
					}
				}
			}
		}
		return projectMappingMap;
	}

	private com.collabnet.ccf.api.model.IdentityMapping getIdentityMapping(IdentityMapping mapping) {
		String childSourceArtifactId;
		String childSourceRepositoryId;
		String childTargetArtifactId;
		String childTargetRepositoryId;
		String parentSourceArtifactId;
		String parentSourceRepositoryId;
		String parentTargetArtifactId;
		String parentTargetRepositoryId;
		String sourceArtifactId;
		String sourceArtifactVersion;
		Timestamp sourceLastModificationTime;
		String targetArtifactId;
		String targetArtifactVersion;
		Timestamp targetLastModificationTime;
		if (mapping.getSourceSystemKind().startsWith("TF")) {
			childSourceArtifactId = mapping.getChildSourceArtifactId();
			childSourceRepositoryId = mapping.getChildSourceRepositoryId();
			childTargetArtifactId = mapping.getChildTargetRepositoryId();
			childTargetRepositoryId = mapping.getChildTargetRepositoryId();
			parentSourceArtifactId = mapping.getParentSourceArtifactId();
			parentSourceRepositoryId = mapping.getParentSourceRepositoryId();
			parentTargetArtifactId = mapping.getParentTargetArtifactId();
			parentTargetRepositoryId = mapping.getParentTargetRepositoryId();
			sourceArtifactId = mapping.getSourceArtifactId();
			sourceArtifactVersion = mapping.getSourceArtifactVersion();
			sourceLastModificationTime = mapping.getSourceLastModificationTime();
			targetArtifactId = mapping.getTargetArtifactId();
			targetArtifactVersion = mapping.getTargetArtifactVersion();
			targetLastModificationTime = mapping.getTargetLastModificationTime();
		}
		else {
			childSourceArtifactId = mapping.getChildTargetArtifactId();
			childSourceRepositoryId = mapping.getChildTargetRepositoryId();
			childTargetArtifactId = mapping.getChildSourceRepositoryId();
			childTargetRepositoryId = mapping.getChildSourceRepositoryId();
			parentSourceArtifactId = mapping.getParentTargetArtifactId();
			parentSourceRepositoryId = mapping.getParentTargetRepositoryId();
			parentTargetArtifactId = mapping.getParentSourceArtifactId();
			parentTargetRepositoryId = mapping.getParentSourceRepositoryId();
			sourceArtifactId = mapping.getTargetArtifactId();
			sourceArtifactVersion = mapping.getTargetArtifactVersion();
			sourceLastModificationTime = mapping.getTargetLastModificationTime();
			targetArtifactId = mapping.getSourceArtifactId();
			targetArtifactVersion = mapping.getSourceArtifactVersion();
			targetLastModificationTime = mapping.getSourceLastModificationTime();								
		}	
		com.collabnet.ccf.api.model.IdentityMapping identityMapping = new com.collabnet.ccf.api.model.IdentityMapping();
		identityMapping.setArtifactType(mapping.getArtifactType());
		identityMapping.setDepChildSourceArtifactId(childSourceArtifactId);
		identityMapping.setDepChildSourceRepositoryId(childSourceRepositoryId);
		identityMapping.setDepChildTargetArtifactId(childTargetArtifactId);
		identityMapping.setDepChildTargetRepositoryId(childTargetRepositoryId);
		identityMapping.setDepParentSourceArtifactId(parentSourceArtifactId);
		identityMapping.setDepParentSourceRepositoryId(parentSourceRepositoryId);
		identityMapping.setDepParentTargetArtifactId(parentTargetArtifactId);
		identityMapping.setDepParentTargetRepositoryId(parentTargetRepositoryId);
		identityMapping.setDescription("This identity mapping has been added by CCF GUI during migration");
		identityMapping.setSourceArtifactId(sourceArtifactId);
		identityMapping.setSourceArtifactVersion(sourceArtifactVersion);
		identityMapping.setSourceLastModificationTime(sourceLastModificationTime);
		identityMapping.setTargetArtifactId(targetArtifactId);
		identityMapping.setTargetArtifactVersion(targetArtifactVersion);
		identityMapping.setTargetLastModificationTime(targetLastModificationTime);
		return identityMapping;
	}
	
	private FieldMappingRule getFieldMappingRule(FieldMappingRuleType fieldMappingRuleType, File file) throws IOException {
		FieldMappingRule fieldMappingRule = new FieldMappingRule();
		fieldMappingRule.setType(fieldMappingRuleType);
		fieldMappingRule.setSource("source");
		fieldMappingRule.setSourceIsTopLevelAttribute(Boolean.valueOf(false));
		fieldMappingRule.setTarget("target");
		fieldMappingRule.setTargetIsTopLevelAttribute(Boolean.valueOf(false));
		if (!fieldMappingRuleType.equals(FieldMappingRuleType.MAPFORCE_MFD)) {
			fieldMappingRule.setXmlContent(CcfMasterClient.readFile(file));
		}
		return fieldMappingRule;
	}
	
	@Override
	public boolean needsProgressMonitor() {
		return true;
	}

	public Landscape getLandscape() {
		return landscape;
	}
	
	public SynchronizationStatus[] getProjectMappings(IProgressMonitor monitor) throws Exception {
		if (null == projectMappings) {
			monitor.subTask("Retrieving CCF 1.x project mappings");							
			projectMappings = getUnmigratedProjectMappings(getCcfDataProvider().getSynchronizationStatuses(landscape, null));
		}
		return projectMappings;
	}
	
	private SynchronizationStatus[] getUnmigratedProjectMappings(SynchronizationStatus[] projectMappings) {
		RepositoryMappingDirection[] existingMappings = null;
		try {
			existingMappings = getCcfMasterClient().getRepositoryMappingDirections();
		} catch (Exception e) {}
		if (null == existingMappings || 0 == existingMappings.length) {
			return projectMappings;
		}
		List<SynchronizationStatus> unmigratedMappingList = new ArrayList<SynchronizationStatus>();
		for (SynchronizationStatus projectMapping : projectMappings) {
			if (!mappingExists(projectMapping, existingMappings)) {
				unmigratedMappingList.add(projectMapping);
			}
		}
		SynchronizationStatus[] unmigratedMappings = new SynchronizationStatus[unmigratedMappingList.size()];
		unmigratedMappingList.toArray(unmigratedMappings);
		return unmigratedMappings;
	}
	
	private boolean mappingExists(SynchronizationStatus mapping, RepositoryMappingDirection[] existingMappings) {
		for (RepositoryMappingDirection existingMapping : existingMappings) {
			if (existingMapping.getDirection().equals(Directions.FORWARD) && mapping.getSourceRepositoryId().equals(existingMapping.getRepositoryMapping().getTeamForgeRepositoryId()) && mapping.getTargetRepositoryId().equals(existingMapping.getRepositoryMapping().getParticipantRepositoryId())) {
				return true;
			}
			if (existingMapping.getDirection().equals(Directions.REVERSE) && mapping.getTargetRepositoryId().equals(existingMapping.getRepositoryMapping().getTeamForgeRepositoryId()) && mapping.getSourceRepositoryId().equals(existingMapping.getRepositoryMapping().getParticipantRepositoryId())) {
				return true;
			}
		}
		return false;
	}

	public CcfDataProvider getCcfDataProvider() {
		if (null == ccfDataProvider) {
			ccfDataProvider = new CcfDataProvider();
		}
		return ccfDataProvider;
	}
	
	public CcfMasterClient getCcfMasterClient() {
		return CcfMasterClient.getClient(com.collabnet.ccf.migration.Activator.getPlatformProxy(ccfMasterPage.getCcfMasterUrl()), ccfMasterPage.getCcfMasterUrl(), null, ccfMasterPage.getCcfMasterUser(), ccfMasterPage.getCcfMasterPassword(), true);
	}

	public Map<String, ProjectDO> getProjectMap() {
		return projectMap;
	}

	public Map<String, TrackerDO> getTrackerMap() {
		return trackerMap;
	}

	private String getParticipantDescription(String type) {
		String description;
		if (type.equals("QC")) {
			description = "Quality Center";
		}
		else if (type.equals("PT")) {
			description = "Project Tracker";
		}
		else if (type.equals("SWP")) {
			description = "ScrumWorks Pro";
		}	
		else {
			description = "Unknown";
		}
		return description;
	}
	
	private ExternalApp getExternalApp(ExternalApp checkApp, ExternalApp[] existingApps) {
		if (null != checkApp.getLandscape()) {
			for (ExternalApp externalApp : existingApps) {
				if (null != externalApp.getLandscape() && externalApp.getLandscape().getId() == checkApp.getLandscape().getId() &&
						externalApp.getProjectPath().equals(checkApp.getProjectPath())) {
					return externalApp;
				}
			}
		}
		return null;
	}
	
	private RepositoryMapping getRepositoryMapping(RepositoryMapping checkMapping, RepositoryMapping[] existingMappings) {
		if (null != checkMapping.getExternalApp()) {
			for (RepositoryMapping repositoryMapping: existingMappings) {
				if (null != repositoryMapping.getExternalApp() && repositoryMapping.getExternalApp().getId() == checkMapping.getExternalApp().getId() &&
						repositoryMapping.getParticipantRepositoryId().equals(checkMapping.getParticipantRepositoryId()) &&
						repositoryMapping.getTeamForgeRepositoryId().equals(checkMapping.getTeamForgeRepositoryId())) {
					return repositoryMapping;		
				}
			}
		}
		return null;
	}
	
	private RepositoryMapping getRepositoryMapping(SynchronizationStatus projectMapping, RepositoryMapping[] repositoryMappings) {
		for (RepositoryMapping repositoryMapping : repositoryMappings) {
			if (projectMapping.getSourceRepositoryId().equals(repositoryMapping.getTeamForgeRepositoryId()) || projectMapping.getSourceRepositoryId().equals(repositoryMapping.getParticipantRepositoryId())) {
				if (projectMapping.getTargetRepositoryId().equals(repositoryMapping.getTeamForgeRepositoryId()) || projectMapping.getTargetRepositoryId().equals(repositoryMapping.getParticipantRepositoryId())) {
					return repositoryMapping;
				}
			}
		}
		return null;
	}
	
	private RepositoryMapping getRepositoryMapping(IdentityMapping identityMapping, RepositoryMapping[] repositoryMappings) {
		String teamForgeRepositoryId;
		String participantRepositoryId;
		if (identityMapping.getSourceSystemKind().startsWith("TF")) {
			teamForgeRepositoryId = identityMapping.getSourceRepositoryId();
			participantRepositoryId = identityMapping.getTargetRepositoryId();
		}
		else {
			teamForgeRepositoryId = identityMapping.getTargetRepositoryId();
			participantRepositoryId = identityMapping.getSourceRepositoryId();			
		}
		for (RepositoryMapping repositoryMapping : repositoryMappings) {			
			if (repositoryMapping.getTeamForgeRepositoryId().equals(teamForgeRepositoryId) && repositoryMapping.getParticipantRepositoryId().equals(participantRepositoryId)) {
				return repositoryMapping;
			}
		}
		return null;
	}

	private RepositoryMappingDirection getRepositoryMappingDirection(RepositoryMappingDirection checkMappingDirection, RepositoryMappingDirection[] existingDirections) {
		if (null != checkMappingDirection.getRepositoryMapping()) {
			for (RepositoryMappingDirection repositoryMappingDirection: existingDirections) {
				if (null != repositoryMappingDirection.getRepositoryMapping() && repositoryMappingDirection.getRepositoryMapping().getId() == checkMappingDirection.getRepositoryMapping().getId() &&
						repositoryMappingDirection.getDirection().toString().equals(checkMappingDirection.getDirection().toString())) {
					return repositoryMappingDirection;		
				}
			}
		}
		return null;
	}
	
	private String obfuscatePassword(String password) {
		String obfuscatedPassword;
		if (null != password && !password.startsWith(Activator.OBFUSCATED_PASSWORD_PREFIX)) {
			obfuscatedPassword = Activator.OBFUSCATED_PASSWORD_PREFIX + Activator.encode(password);
		}
		else {
			obfuscatedPassword = password;
		}
		return obfuscatedPassword;
	}
	
	private void saveSelections() {
	    List<String> urls = new ArrayList<String>();
	    urls.add(ccfMasterPage.getCcfMasterUrl());
	    for (int i = 0; i < 5; i++) {
	      String url = settings.get("CCFMaster.url." + i);
	      if (null == url)
	        break;
	      if (!urls.contains(url))
	        urls.add(url);
	    }	
	    int i = 0;
	    for (String url : urls) {
	        settings.put("CCFMaster.url." + i++, url); //$NON-NLS-1$ //$NON-NLS-2$
	        if (5 == i)
	          break;	    	
	    }
	    settings.put("CCFMaster.user." + ccfMasterPage.getCcfMasterUrl(), ccfMasterPage.getCcfMasterUser());
	}

	private void createOrUpdateLandscapeConfig(CcfMasterClient ccfMasterClient, com.collabnet.ccf.api.model.Landscape landscape, boolean landscapeAlreadyExists, LandscapeConfig landscapeConfig) throws Exception {
		if (landscapeAlreadyExists) {
			LandscapeConfig updateConfig = getLandscapeConfig(landscape, landscapeConfig.getName());
			if (null == updateConfig) {
				ccfMasterClient.createLandscapeConfig(landscapeConfig);
			} else {
				updateConfig.setVal(landscapeConfig.getVal());
				ccfMasterClient.updateLandscapeConfig(updateConfig);
			}
		} else {
			ccfMasterClient.createLandscapeConfig(landscapeConfig);
		}
	}

	private boolean createOrUpdateParticipantConfig(CcfMasterClient ccfMasterClient, Participant participant, boolean participantAlreadyExists, ParticipantConfig participantConfig) throws Exception {
		if (participantAlreadyExists) {
			ParticipantConfig updateConfig = getParticipantConfig(participant, participantConfig.getName());
			if (null == updateConfig) {
				ccfMasterClient.createParticipantConfig(participantConfig);
			} else {
				URL currentUrl = new URL(updateConfig.getVal());
				URL newUrl = new URL(participantConfig.getVal());
				if (!currentUrl.getHost().equals(newUrl.getHost())) {
					return false;
				}
				updateConfig.setVal(participantConfig.getVal());
				ccfMasterClient.updateParticipantConfig(updateConfig);
			}
		} else {
			ccfMasterClient.createParticipantConfig(participantConfig);
		}
		return true;
	}

	private void createOrUpdateDirectionConfig(CcfMasterClient ccfMasterClient, Direction direction, DirectionConfig directionConfig) throws Exception {
		DirectionConfig updateConfig = getDirectionConfig(direction, directionConfig.getName());
		if (null == updateConfig) {
			ccfMasterClient.createDirectionConfig(directionConfig);
		} else {
			updateConfig.setVal(directionConfig.getVal());
			ccfMasterClient.updateDirectionConfig(updateConfig);
		}
	}
	
	private ParticipantConfig[] getParticipantConfigs() throws Exception {
		if (null == participantConfigs) {
			participantConfigs = getCcfMasterClient().getParticipantConfigs(true);
		}
		return participantConfigs;
	}
	
	private LandscapeConfig[] getLandscapeConfigs() throws Exception {
		if (null == landscapeConfigs) {
			landscapeConfigs = getCcfMasterClient().getLandscapeConfigs(true);
		}
		return landscapeConfigs;
	}	
	
	private DirectionConfig[] getDirectionConfigs() throws Exception {
		if (null == directionConfigs) {
			directionConfigs = getCcfMasterClient().getDirectionConfigs(true);
		}
		return directionConfigs;
	}
	
	private ParticipantConfig getParticipantConfig(Participant participant, String name) throws Exception {
		ParticipantConfig participantConfig = null;
		ParticipantConfig[] participantConfigs = getParticipantConfigs();
		for (ParticipantConfig checkConfig : participantConfigs) {
			if (checkConfig.getName().equals(name) && checkConfig.getParticipant().getId() == participant.getId()) {
				participantConfig = checkConfig;
				break;
			}
		}
		return participantConfig;
	}
	
	private LandscapeConfig getLandscapeConfig(com.collabnet.ccf.api.model.Landscape landscape, String name) throws Exception {
		LandscapeConfig landscapeConfig = null;
		LandscapeConfig[] landscapeConfigs = getLandscapeConfigs();
		for (LandscapeConfig checkConfig : landscapeConfigs) {
			if (checkConfig.getName().equals(name) && checkConfig.getLandscape().getId() == landscape.getId()) {
				landscapeConfig = checkConfig;
				break;
			}
		}
		return landscapeConfig;
	}
	
	private DirectionConfig getDirectionConfig(Direction direction, String name) throws Exception {
		DirectionConfig directionConfig = null;
		DirectionConfig[] directionConfigs = getDirectionConfigs();
		for (DirectionConfig checkConfig : directionConfigs) {
			if (checkConfig.getName().equals(name) && checkConfig.getDirection().getId() == direction.getId()) {
				directionConfig = checkConfig;
				break;
			}
		}
		return directionConfig;
	}

	private String getStatusMessage(String entityType) {
		StringBuffer statusMessage = new StringBuffer();
		if (0 < createdCount) {
			statusMessage.append(createdCount + " " + entityType + " created");
		}
		if (0 < notCreatedCount) {
			if (0 < createdCount) {
				statusMessage.append(", ");
			}
			statusMessage.append(notCreatedCount + " " + entityType + " not created");
		}
		if (0 < alreadyExistedCount) {
			if (0 < createdCount || 0 < notCreatedCount) {
				statusMessage.append(", ");
			}
			statusMessage.append(alreadyExistedCount + " " + entityType + " already existed");
		}
		if (0 < entityType.length()) {
			statusMessage.append(" in CCF Master");
		}
		return statusMessage.toString();
	}

	private void handleIdentityMappingMigrationError(IdentityMapping mapping, Exception e) {
		if (null != e.getMessage() && e.getMessage().contains("ConstraintViolationException")) {
			alreadyExistedCount++;
		}
		else {
			migrationResults.add(new MigrationResult("Identity mapping " + mapping.getSourceArtifactId() + "\u21D4" + " " +  mapping.getTargetArtifactId() + " not migrated.", MigrationResult.ERROR, e));
			notCreatedCount++;
		}
	}
}
