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

package com.collabnet.ccf.pi.qc.v90.api.dcom;


import java.util.ArrayList;
import java.util.List;

import com.collabnet.ccf.pi.qc.v90.api.ICommand;
import com.collabnet.ccf.pi.qc.v90.api.IConnection;
import com.collabnet.ccf.pi.qc.v90.api.IFactory;
import com.collabnet.ccf.pi.qc.v90.api.IHistory;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class Connection extends ActiveXComponent implements IConnection
{
	private IFactory factory = null;
	private ICommand command = null;
	
	
	private String majorVersion = null, minorVersion = null;
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public Connection(String server, String domain, String project, String user, String pass) {
        this(server, user, pass);
        connect(domain, project);
    }
	
	public Connection(String server, String user, String pass) {
        super("TDApiOle80.TDConnection");
        initConnectionEx(server);
        login(user, pass);
        populateVersions();
    }
	
	private void populateVersions() {
		Variant majorRef = new Variant("defaultMajor", true);
		Variant minorRef = new Variant("defaultMinor", true);
		try {
			Dispatch.callSub(this, "GetTDVersion", majorRef, minorRef);
			majorVersion = majorRef.getStringRef();
			minorVersion = minorRef.getStringRef();
		} finally {
			majorRef.safeRelease();
			minorRef.safeRelease();
		}
	}
	
	public List<String> getUserVisibleDomains() {
		List<String> result = new ArrayList<String>();
		if (loggedIn) {
	    	Variant res = Dispatch.call(this, "VisibleDomains");
	    	if (!res.isNull()) {
	    		assert(res.getvt() == Variant.VariantDispatch);
	    		Dispatch list = res.getDispatch();
	    		Variant listSize = Dispatch.call(list, "Count");
	    		assert(listSize.getvt() == Variant.VariantInt);
	    		int numItems = listSize.getInt();
	    		for(int i = 1; i <= numItems; i++) {
	    			Variant subFieldVal = Dispatch.call(list, "Item", i);
					assert(subFieldVal.getvt() == Variant.VariantString);
					if (!subFieldVal.isNull()) {
						result.add(subFieldVal.getString());
					}
	    		}
	    		res.safeRelease();
	    	}
		}
    	return result;
	}

	public List<String> getUserVisibleProjects(String domain) {
		List<String> result = new ArrayList<String>();
		if (loggedIn) {
	    	Variant res = Dispatch.call(this, "VisibleProjects", domain);
	    	if (!res.isNull()) {
	    		assert(res.getvt() == Variant.VariantDispatch);
	    		Dispatch list = res.getDispatch();
	    		Variant listSize = Dispatch.call(list, "Count");
	    		assert(listSize.getvt() == Variant.VariantInt);
	    		int numItems = listSize.getInt();
	    		for(int i = 1; i <= numItems; i++) {
	    			Variant subFieldVal = Dispatch.call(list, "Item", i);
					assert(subFieldVal.getvt() == Variant.VariantString);
					if (!subFieldVal.isNull()) {
						result.add(subFieldVal.getString());
					}
	    		}
	    		res.safeRelease();
	    	}
		}
    	return result;
	}
	
    boolean loggedIn = false;
    public void login(String user, String pass)
    {
        Dispatch.call(this, "Login", user, pass);
        loggedIn = true;
    }

    public void logout()
    {
    	if (loggedIn) {
    		Dispatch.call(this, "Logout");
    	}
        loggedIn = false;
    }

    public boolean isLoggedIn() {
		return loggedIn;
	}

    public void connect(String domain, String project)
    {
        Dispatch.call(this, "Connect", domain, project);
    }

    public void initConnectionEx(String serverName)
    {
        Dispatch.call(this, "InitConnectionEx", serverName);
    }

    public void connectProjectEx(String domain, String project, String user, String pass)
    {
        login(user, pass);
        connect(domain, project);
    }

    public void disconnectProject()
    {
    	loggedIn = false;
        Dispatch.call(this, "DisconnectProject");
    }

    public void releaseConnection()
    {
    	loggedIn = false;
        Dispatch.call(this, "ReleaseConnection");
    }

    public boolean connected()
    {
        return getPropertyAsBoolean("Connected");
    }

    public ICommand getCommand()
    {
    	if(null == command) {
    		command = new Command(getPropertyAsComponent("Command"));
    	}
        return command;
    }

    public void disconnect()
    {
    	if(null != factory) {
	    	factory.safeRelease();
	    	factory = null;
    	}
    	if(null != command) {
	    	command.safeRelease();
	    	command = null;
    	}
    	
    	if (loggedIn) {
    		Dispatch.call(this, "DisconnectProject");
    		logout();
    	}
        Dispatch.call(this, "ReleaseConnection");
    }

	public IFactory getBugFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	public IHistory getHistory() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getMajorVersion() {
		return majorVersion;
	}
	public String getMinorVersion() {
		return minorVersion;
	}

}
