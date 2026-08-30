/*
 * Copyright 2009 CollabNet, Inc. ("CollabNet")
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/

package com.collabnet.ccf.pt.schemageneration;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.collabnet.ccf.core.CCFRuntimeException;
import com.collabnet.core.ws.exception.WSException;
import com.collabnet.tracker.core.PTrackerWebServicesClient;
import com.collabnet.tracker.core.model.TrackerArtifactType;
import com.collabnet.tracker.ws.ArtifactTypeMetadata;


public class PTMetaDataHelper {
	private static final Log log = LogFactory.getLog(PTMetaDataHelper.class);
	private static PTMetaDataHelper instance = null;
	private HashMap<String, ArtifactTypeMetadata> artifactTypeMetadataHash =
		new HashMap<String, ArtifactTypeMetadata>();
	private PTMetaDataHelper(){
	}
	public static PTMetaDataHelper getInstance(){
		synchronized(PTMetaDataHelper.class){
			if(null == instance){
				instance = new PTMetaDataHelper();
			}
		}
		return instance;
	}
	public ArtifactTypeMetadata getArtifactTypeMetadata(String artifactTypeId){
		ArtifactTypeMetadata metadata = artifactTypeMetadataHash.get(artifactTypeId);
		return metadata;
	}
	public void putArtifactTypeMetadata(String artifactTypeId, ArtifactTypeMetadata metadata){
		artifactTypeMetadataHash.put(artifactTypeId, metadata);
	}
	public ArtifactTypeMetadata populateArtifactTypeMetadata(String repositoryKey, String artifactTypeNamespace,
			String artifactTypeTagName, PTrackerWebServicesClient twsclient){
		String artifactTypeFullyQualifiedName = "{"+artifactTypeNamespace+"}"+artifactTypeTagName;
		ArtifactTypeMetadata meta = null;
		try {
			meta = twsclient.getMetaDataForArtifactType(artifactTypeNamespace,
					artifactTypeTagName);
		} catch (WSException e) {
			String message = "Web services exception when trying to get the "+
			"artifact type metadata for "+artifactTypeFullyQualifiedName;
			log.error(message, e);
			throw new CCFRuntimeException(message, e);
		} catch (RemoteException e) {
			String message = "RemoteException when trying to get the "+
					"artifact type metadata for "+artifactTypeFullyQualifiedName;
			log.error(message, e);
			throw new CCFRuntimeException(message, e);
		} catch (ServiceException e) {
			String message = "ServiceException when trying to get the "+
					"artifact type metadata for "+artifactTypeFullyQualifiedName;
			log.error(message, e);
			throw new CCFRuntimeException(message, e);
		}
		getInstance().putArtifactTypeMetadata(repositoryKey, meta);
		return meta;
	}
	
	public ArtifactTypeMetadata getArtifactTypeMetadata(String repositoryKey, String artifactTypeNamespace,
			String artifactTypeTagName){
		ArtifactTypeMetadata meta = this.getArtifactTypeMetadata(repositoryKey);
		return meta;
	}
	
	public TrackerArtifactType getTrackerArtifactType(String repositoryKey, String artifactTypeNamespace,
					String artifactTypeTagName, PTrackerWebServicesClient twsclient){
		ArtifactTypeMetadata meta = getInstance().getArtifactTypeMetadata(repositoryKey);
		if(null == meta){
			meta = this.populateArtifactTypeMetadata(repositoryKey,
					artifactTypeNamespace, artifactTypeTagName, twsclient);
		}
		TrackerArtifactType newArtifactType =
			new TrackerArtifactType(null,artifactTypeTagName,artifactTypeNamespace);
		newArtifactType.populateAttributes(meta);
		return newArtifactType;
	}
	public TrackerArtifactType getTrackerArtifactType(String key) {
		ArtifactTypeMetadata meta = getInstance().getArtifactTypeMetadata(key);
		TrackerArtifactType trackerArtifactType = null;
		if(null != meta){
			TrackerArtifactType newArtifactType =
				new TrackerArtifactType(meta.getArtifactType().getDisplayName(),
						meta.getArtifactType().getTagName(),meta.getArtifactType().getNamespace());
			newArtifactType.populateAttributes(meta);
			trackerArtifactType = newArtifactType;
		}
		return trackerArtifactType;
	}
	
	public TrackerArtifactType getTrackerArtifactType(String repositoryKey, String artifactTypeDisplayName,
			PTrackerWebServicesClient twsclient) throws WSException, RemoteException, ServiceException{
		TrackerArtifactType artifactTypeForDisplayName = null;
		// Do not use caching to always get newest field changes
		/*artifactTypeForDisplayName = this.getTrackerArtifactType(repositoryKey);
		
		if(artifactTypeForDisplayName != null){
			return artifactTypeForDisplayName;
		}*/
		Collection<TrackerArtifactType> tAT = null;
		tAT = twsclient.getArtifactTypes();
		for (TrackerArtifactType type : tAT )
		{
			String namespace = type.getNamespace();
			String tagname = type.getTagName();
			if(artifactTypeDisplayName.equals(type.getDisplayName()))
			{
				ArtifactTypeMetadata meta = this.populateArtifactTypeMetadata(repositoryKey,
						namespace, tagname, twsclient);
				type.populateAttributes(meta);
				artifactTypeForDisplayName = type;
				break;
			}
		}
		return artifactTypeForDisplayName;
	}
}
