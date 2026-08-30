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
package com.collabnet.ccf.migration.webclient;

import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.teamforge.api.Connection;

public class TeamForgeClient {

	private Connection connection;

	private static Map<String, TeamForgeClient> clients = new HashMap<String, TeamForgeClient>();
	
	public TeamForgeClient(String serverUrl, String userId, String password, Proxy proxy, String httpAuthUser, String httpAuthPass) {
		connection = Connection.builder(serverUrl)
		.userNamePassword(userId, password)
		.httpAuth(httpAuthUser, httpAuthPass)
		.proxy(proxy)
		.build();
		clients.put(serverUrl + userId, this);
	}
	
	public static TeamForgeClient getTeamForgeClient(Landscape landscape) {
		TeamForgeClient client = null;
		Properties properties = null;
		if (landscape.getType1().equals("TF")) {
			properties = landscape.getProperties1();
		} else if (landscape.getType2().equals("TF")){
			properties = landscape.getProperties2();
		}
		if (null != properties) {
			String serverUrl = properties.getProperty(Activator.PROPERTIES_SFEE_URL);
			String userId = properties.getProperty(Activator.PROPERTIES_SFEE_USER);
			String password = Activator.decodePassword(properties.getProperty(Activator.PROPERTIES_SFEE_PASSWORD));
			client = clients.get(serverUrl + userId);
			if (null == client || !client.getConnection().getPassword().equals(password)) {
				client = new TeamForgeClient(serverUrl, userId, password, null, null, null);
			}
		}	
		return client;
	}

	public Connection getConnection() {
		return connection;
	}

}
