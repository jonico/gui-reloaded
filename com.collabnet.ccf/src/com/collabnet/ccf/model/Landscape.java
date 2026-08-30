package com.collabnet.ccf.model;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.osgi.service.prefs.Preferences;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.ICcfParticipant;

public class Landscape implements IPropertySource {
	private String description;
	private int role;
	private String group;
	
	private String databaseUrl;
	private String databaseDriver;
	private String databaseUser;
	private String databasePassword;
	
	private String ccfHost1;
	private String ccfHost2;
	private String logsPath1;
	private String jmxPort1;
	private String jmxPort2;
	private String logsPath2;

	private String type1;
	private String type2;
	private String configurationFolder1;
	private String configurationFolder2;
	private String participantId1;
	private String participantId2;
	private Preferences node;

	private Properties ccfProperties1;
	private Properties ccfProperties2;
	private Properties properties1;
	private Properties properties2;
	
	private File configFile1;
	private File configFile2;
	private File logsFolder1;
	private File logsFolder2;
	private File xsltFolder1;
	private File xsltFolder2;
	private File libFolder;
	private File log4jFile;
	private File log4jRenameFile;
	
	public final static int ROLE_ADMINISTRATOR = 0;
	public final static int ROLE_OPERATOR = 1;
	
	public final static String TYPE_CCF = "CCF";
	
//	public final static String TYPE_QC = "QC";
//	public final static String TYPE_TF = "TF";
//	public final static String TYPE_PT = "PT";
	
	public static String P_ID_DESCRIPTION = "desc"; //$NON-NLS-1$
	public static String P_DESCRIPTION = "Description";
	public static String P_ID_ROLE = "role"; //$NON-NLS-1$
	public static String P_ROLE = "Role";
	public static String P_ID_GROUP = "group"; //$NON-NLS-1$
	public static String P_GROUP = "Group";
	public static String P_ID_TYPE1 = "type1"; //$NON-NLS-1$
	public static String P_TYPE1 = "System 1 type";
	public static String P_ID_TYPE2 = "type2"; //$NON-NLS-1$
	public static String P_TYPE2 = "System 2 type";
	public static String P_ID_FOLDER1 = "folder1"; //$NON-NLS-1$
	public static String P_FOLDER1 = "Configuration folder 1";
	public static String P_ID_FOLDER2 = "folder2"; //$NON-NLS-1$
	public static String P_FOLDER2 = "Configuration folder 2";
	
	public static List<PropertyDescriptor> descriptors;
	static
	{	
		descriptors = new ArrayList<PropertyDescriptor>();
		descriptors.add(new PropertyDescriptor(P_ID_DESCRIPTION, P_DESCRIPTION));
		descriptors.add(new PropertyDescriptor(P_ID_ROLE, P_ROLE));
		descriptors.add(new PropertyDescriptor(P_ID_GROUP, P_GROUP));
		descriptors.add(new PropertyDescriptor(P_ID_TYPE1, P_TYPE1));
		descriptors.add(new PropertyDescriptor(P_ID_TYPE2, P_TYPE2));
		descriptors.add(new PropertyDescriptor(P_ID_FOLDER1, P_FOLDER1));
		descriptors.add(new PropertyDescriptor(P_ID_FOLDER2, P_FOLDER2));
	}		

	public String getDescription() {
		if (null == description) {
			String t1 = null;
			String t2 = null;
			try {
				ICcfParticipant p1 = Activator.getCcfParticipantForType(type1);
				if (null != p1) {
					t1 = p1.getName();
				}
				ICcfParticipant p2 = Activator.getCcfParticipantForType(type2);
				if (null != p2) {
					t2 = p2.getName();
				}
			} catch (Exception e) {}
			if (null == t1) {
				t1 = type1;
			}
			if (null == t2) {
				t2 = type2;
			}		
			return t1 + "/" + t2;
		}
		else return description.replaceAll("%slash%", "/");
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getRole() {
		return role;
	}
	public void setRole(int role) {
		this.role = role;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getType1() {
		return type1;
	}
	public void setType1(String type1) {
		this.type1 = type1;
	}
	public String getType2() {
		return type2;
	}
	public void setType2(String type2) {
		this.type2 = type2;
	}
	public String getConfigurationFolder() {
		if (null == configurationFolder1) return configurationFolder2;
		else return configurationFolder1;
	}
	public String getConfigurationFolder1() {
		return configurationFolder1;
	}
	public void setConfigurationFolder1(String configurationFolder1) {
		this.configurationFolder1 = configurationFolder1;
	}
	public String getConfigurationFolder2() {
		return configurationFolder2;
	}
	public void setConfigurationFolder2(String configurationFolder2) {
		this.configurationFolder2 = configurationFolder2;
	}	
	public Preferences getNode() {
		return node;
	}
	
	public void setNode(Preferences node) {
		this.node = node;
	}

	public void setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
	}
	
	public String getParticipantId1() {
		return participantId1;
	}
	public void setParticipantId1(String participantId1) {
		this.participantId1 = participantId1;
	}
	public String getParticipantId2() {
		return participantId2;
	}
	public void setParticipantId2(String participantId2) {
		this.participantId2 = participantId2;
	}
	
	public void setDatabaseDriver(String databaseDriver) {
		this.databaseDriver = databaseDriver;
	}
	
	public void setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
	}
	
	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}
	
	public Database getDatabase() {
		Database database = new Database();
		database.setDriver(getDatabaseDriver());
		database.setPassword(getDatabasePassword());
		database.setUrl(getDatabaseUrl());
		database.setPassword(getDatabasePassword());
		return database;
	}
	
	public String getDatabaseUrl() {
		if (null != databaseUrl) return databaseUrl;
		String url = null;
		ccfProperties1 = getCcfProperties1();
		if (null != ccfProperties1) {
			url = ccfProperties1.getProperty(Activator.PROPERTIES_CCF_URL);
		}
		return url;
	}
	
	public String getDatabaseDriver() {
		if (null != databaseDriver) return databaseDriver;
		String driver = null;
		ccfProperties1 = getCcfProperties1();
		if (null != ccfProperties1) {
			driver = ccfProperties1.getProperty(Activator.PROPERTIES_CCF_DRIVER);
		}
		return driver;
	}
	
	public String getDatabaseUser() {
		if (null != databaseUser) return databaseUser;
		String user = null;
		ccfProperties1 = getCcfProperties1();
		if (null != ccfProperties1) {
			user = ccfProperties1.getProperty(Activator.PROPERTIES_CCF_USER);
		}
		return user;
	}
	
	public String getDatabasePassword() {
		if (null != databasePassword) return databasePassword;
		String password = null;
		ccfProperties1 = getCcfProperties1();
		if (null != ccfProperties1) {
			password = Activator.decodePassword(ccfProperties1.getProperty(Activator.PROPERTIES_CCF_PASSWORD));
		}
		return password;
	}
	
	public void setCcfHost1(String ccfHost1) {
		this.ccfHost1 = ccfHost1;
	}
	
	public void setCcfHost2(String ccfHost2) {
		this.ccfHost2 = ccfHost2;
	}
	
	public void setJmxPort1(String jmxPort1) {
		this.jmxPort1 = jmxPort1;
	}
	
	public void setJmxPort2(String jmxPort2) {
		this.jmxPort2 = jmxPort2;
	}
	
	public void setLogsPath1(String logsPath1) {
		this.logsPath1 = logsPath1;
	}
	
	public void setLogsPath2(String logsPath2) {
		this.logsPath2 = logsPath2;
	}
	
	public String getHostName1() {
		String hostName = getCcfHost1();
		int index = hostName.indexOf("//");
		if (-1 != index) {
			hostName = hostName.substring(index + 2);
		}
		return hostName;
	}
	
	public String getHostName2() {
		String hostName = getCcfHost2();
		int index = hostName.indexOf("//");
		if (-1 != index) {
			hostName = hostName.substring(index + 2);
		}
		return hostName;
	}
	
	public String getCcfHost1() {
		if (null != ccfHost1) return ccfHost1;
		String hostName = null;
		ccfProperties1 = getCcfProperties1();
		if (null != ccfProperties1) {
			hostName = ccfProperties1.getProperty(Activator.PROPERTIES_CCF_HOST_NAME, "http://localhost");
		}
		return hostName;
	}
	
	public String getCcfHost2() {
		if (null != ccfHost2) return ccfHost2;
		String hostName = null;
		ccfProperties2 = getCcfProperties2();
		if (null != ccfProperties2) {
			hostName = ccfProperties2.getProperty(Activator.PROPERTIES_CCF_HOST_NAME, "http://localhost");
		}
		return hostName;
	}		
	
	public String getJmxPort1() {
		if (null != jmxPort1) return jmxPort1;
		String port = null;
		ccfProperties1 = getCcfProperties1();
		if (null != ccfProperties1) {
			port = ccfProperties1.getProperty(Activator.PROPERTIES_CCF_JMX_PORT);
		}
		return port;
	}
	
	public String getJmxUrl1() {
		StringBuffer url = new StringBuffer(getCcfHost1());
		String port = getJmxPort1();
		if (null != port && 0 < port.length()) {
			url.append(":" + port);
		}
		return url.toString();
	}
	
	public String getLogsPath1() {
		return logsPath1;
	}
	
	public String getLogsPath2() {
		return logsPath2;
	}
	
	public String getJmxUrl2() {
		StringBuffer url = new StringBuffer(getCcfHost2());
		String port = getJmxPort2();
		if (null != port && 0 < port.length()) {
			url.append(":" + port);
		}
		return url.toString();
	}
	
	public String getJmxPort2() {
		if (null != jmxPort2) return jmxPort2;
		String port = null;
		ccfProperties2 = getCcfProperties2();
		if (null != ccfProperties2) {
			port = ccfProperties2.getProperty(Activator.PROPERTIES_CCF_JMX_PORT);
		}
		return port;
	}
	
	public String getLogMessageTemplate1() {
		String template = null;
		ccfProperties1 = getCcfProperties1();
		if (null != ccfProperties1) {
			template = ccfProperties1.getProperty(Activator.PROPERTIES_CCF_LOG_MESSAGE_TEMPLATE);
		}
		return template;
	}
	
	public String getLogMessageTemplate2() {
		String template = null;
		ccfProperties2 = getCcfProperties2();
		if (null != ccfProperties2) {
			template = ccfProperties2.getProperty(Activator.PROPERTIES_CCF_LOG_MESSAGE_TEMPLATE);
		}
		return template;
	}
	
	public String getId1() {
		return getTypeDescription(type1);
	}
	
	public String getId2() {
		return getTypeDescription(type2);
	}
	
	public String getTimezone1() {
		String timezone = null;
		properties1 = getProperties1();
		if (null != properties1) {
			timezone = properties1.getProperty(Activator.PROPERTIES_SYSTEM_TIMEZONE, TimeZone.getDefault().getID());
		}
		return timezone;
	}
	
	public String getTimezone2() {
		String timezone = null;
		properties2 = getProperties2();
		if (null != properties2) {
			timezone = properties2.getProperty(Activator.PROPERTIES_SYSTEM_TIMEZONE, TimeZone.getDefault().getID());
		}
		return timezone;
	}
	
	public String getEncoding1() {
		String encoding = null;
		properties1 = getProperties1();
		if (null != properties1) {
			encoding = properties1.getProperty(Activator.PROPERTIES_SYSTEM_ENCODING);
		}
		return encoding;
	}
	
	public String getEncoding2() {
		String encoding = null;
		properties2 = getProperties2();
		if (null != properties2) {
			encoding = properties2.getProperty(Activator.PROPERTIES_SYSTEM_ENCODING);
		}
		return encoding;
	}
	
	public Properties getCcfProperties1() {
		if (null == ccfProperties1) {
			ccfProperties1 = getProperties(configurationFolder1, TYPE_CCF);
		}
		return ccfProperties1;
	}
	
	public Properties getCcfProperties2() {
		if (null == ccfProperties2) {
			ccfProperties2 = getProperties(configurationFolder2, TYPE_CCF);
		}
		return ccfProperties2;
	}

	public Properties getProperties1() {
		if (null == properties1) {
			properties1 = getProperties(configurationFolder1, type1);
		}
		return properties1;
	}
	
	public Properties getProperties2() {
		if (null == properties2) {
			properties2 = getProperties(configurationFolder2, type2);
		}
		return properties2;
	}
	
	public File getConfigurationFile1() {
		if (null == configFile1) {
			configFile1 = new File(getConfigurationFolder1(), "config.xml");
		}
		return configFile1;
	}
	
	public File getConfigurationFile2() {
		if (null == configFile2) {
			configFile2 = new File(getConfigurationFolder2(), "config.xml");
		}
		return configFile2;
	}
	
	public File getLogsFolder1() {
		String logsPath = getLogsPath1();
		if (null != logsPath) {
			File logsFolder = new File(logsPath);
			if (logsFolder.exists()) return logsFolder;
			else return null;
		}
		if (null == logsFolder1) {
			logsFolder1 = getLogsFolder(configurationFolder2);
		}
		if (logsFolder1.exists()) return logsFolder1;
		else return null;
	}
	
	public File getLogsFolder2() {
		String logsPath = getLogsPath2();
		if (null != logsPath) {
			File logsFolder = new File(logsPath);
			if (logsFolder.exists()) return logsFolder;
			else return null;
		}
		if (null == logsFolder2) {
			logsFolder2 = getLogsFolder(configurationFolder1);
		}
		if (logsFolder2.exists()) return logsFolder2;
		else return null;
	}
	
	private File getLogsFolder(String config) {
		File configurationFolder = new File(config);
		File logsFolder = new File(configurationFolder.getParent(), "logs");
		return logsFolder;
	}
	
	public File getXsltFolder1() {
		if (null == xsltFolder1) {
			xsltFolder1 = getXsltFolder(configurationFolder1);
		}
		if (xsltFolder1.exists()) return xsltFolder1;
		else return null;
	}
	
	public File getXsltFolder2() {
		if (null == xsltFolder2) {
			xsltFolder2 = getXsltFolder(configurationFolder2);
		}
		if (xsltFolder2.exists()) return xsltFolder2;
		else return null;
	}
	
	private File getXsltFolder(String config) {
		File configurationFolder = new File(config);
		File xsltFolder = new File(configurationFolder.getParent(), "xslt");
		return xsltFolder;
	}
	
	public File getLog4jFile() {
		if (null == log4jFile) {
			libFolder = getLibFolder();
			if (null != libFolder) {
				log4jFile = new File(libFolder, "log4j.xml");
			}
		}
		return log4jFile;
	}
	
	public File getLog4jRenameFile() {
		if (null == log4jRenameFile) {
			libFolder = getLibFolder();
			if (null != libFolder) {
				log4jRenameFile = new File(libFolder, "log4j.xml.rename_me");
			}
		}
		return log4jRenameFile;
	}
	
	public boolean enableEditFieldMapping() {
		try {
			ICcfParticipant participant1 = Activator.getCcfParticipantForType(type1);
			if (null != participant1 && !participant1.enableFieldMappingEditing(type2)) {
				return false;
			}
			ICcfParticipant participant2 = Activator.getCcfParticipantForType(type2);
			if (null != participant2 && !participant2.enableFieldMappingEditing(type1)) {
				return false;
			}
		} catch (Exception e) {}
		return true;
	}
	
	private File getLibFolder() {
		if (null == libFolder) {
			File configurationFolder = new File(getConfigurationFolder());
			File parent = configurationFolder.getParentFile();
			if (null != parent) {
				parent = parent.getParentFile();
				if (null != parent) {
					parent = parent.getParentFile();
					if (null != parent) {
						parent = parent.getParentFile();
						if (null != parent) {
							libFolder = new File(parent, "lib");
						}
					}
				}
			}			
		}
		return libFolder;
	}
	
	public Log[] getLogs1(Logs logs) {
		List<Log> logList = new ArrayList<Log>();
		File[] logFiles = getLogFiles(getLogsFolder1());
		if (null != logFiles) {
			for (File logFile : logFiles) {
				Log log = new Log(logs, logFile);
				logList.add(log);
			}
		}
		Log[] logArray = new Log[logList.size()];
		logList.toArray(logArray);
		return logArray;
	}
	
	public Log[] getLogs2(Logs logs) {
		List<Log> logList = new ArrayList<Log>();
		File[] logFiles = getLogFiles(getLogsFolder2());
		if (null != logFiles) {
			for (File logFile : logFiles) {
				Log log = new Log(logs, logFile);
				logList.add(log);
			}
		}
		Log[] logArray = new Log[logList.size()];
		logList.toArray(logArray);
		return logArray;
	}
	
	public Log[] getLogs(Logs logs) {
		if (Logs.TYPE_1_2 == logs.getType()) return getLogs2(logs);
		else return getLogs1(logs);
	}
	
	public String getUrl(int systemNumber) {
		String url = null;
		try {
		ICcfParticipant ccfParticipant;
			if (1 == systemNumber ) {
				ccfParticipant = Activator.getCcfParticipantForType(getType1());
			} else {
				ccfParticipant = Activator.getCcfParticipantForType(getType2());
			}
			url = ccfParticipant.getUrl(this, systemNumber);
		} catch (Exception e) {
			Activator.handleError(e);
		}
		return url;
	}
	
	public String getDirectionButtonText(boolean reversed) {
		String t1;
		String t2;
		if (reversed) {
			t1 = type2;
			t2 = type1;
		} else {
			t1 = type1;
			t2 = type2;			
		}
		StringBuffer d1 = new StringBuffer(getTypeDescription(t1));
		StringBuffer d2 = new StringBuffer(getTypeDescription(t2));
		if (t1.equals(t2)) {
			if (reversed) {
				d1.append("(2)");
				d2.append("(1)");
			} else {
				d1.append("(1)");
				d2.append("(2)");				
			}
		}
		return d1 + " => " + d2;
	}
	
	public static String getTypeDescription(String type) {
		String description = null;
		try {
			ICcfParticipant ccfParticipant = Activator.getCcfParticipantForType(type);
			if (null != ccfParticipant) {
				return ccfParticipant.getName();
			}
		} catch (Exception e) {
			Activator.handleError(e);
		}
		return description;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Landscape) {
			Landscape compareTo = (Landscape)obj;
			return getDescription().equals(compareTo.getDescription()) && getRole() == compareTo.getRole();
		}
		return super.equals(obj);
	}
	
	private File[] getLogFiles(File logsFolder) {
		List<File> logFiles = new ArrayList<File>();
		File[] logs = null;
		if (null != logsFolder) {
			logs = logsFolder.listFiles();
			if (null != logs) {
				for (File logFile : logs) {
					if (!logFile.isDirectory()) {
						logFiles.add(logFile);
					}
				}
			}
		}
		File[] logFileArray = new File[logFiles.size()];
		logFiles.toArray(logFileArray);
		return logFileArray;		
	}
	
	private Properties getProperties(String configurationFolder, String type) {
		if (null == configurationFolder) return null;
		Properties properties = null;
		File folder = new File(configurationFolder);
		String propertyFile = null;
		if (type.equals(TYPE_CCF)) {
			propertyFile = "ccf.properties";
		}
		else {
			try {
				propertyFile = Activator.getCcfParticipantForType(type).getPropertiesFileName();
			} catch (Exception e) {
				Activator.handleError(e);
			}
		}
		if (null != propertyFile) {
			File propertiesFile = new File(folder, propertyFile);
			if (propertiesFile.exists()) {
				try {
					FileInputStream inputStream = new FileInputStream(propertiesFile);
					properties = new Properties();
					properties.load(inputStream);
					inputStream.close();
				} catch (Exception e) { 
					Activator.handleError(e);
				}
			}
		}	
		return properties;
	}
	
	public Object getEditableValue() {
		return description;
	}
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return (IPropertyDescriptor[])getDescriptors().toArray(new IPropertyDescriptor[getDescriptors().size()]);
	}
	
	private static List<PropertyDescriptor> getDescriptors() {
		return descriptors;
	}	
	
	public Object getPropertyValue(Object id) {
		if (P_ID_DESCRIPTION.equals(id)) return getDescription();
		if (P_ID_ROLE.equals(id)) {
			if (ROLE_OPERATOR == role) return "Operator";
			else return "Administrator";
		}
		if (P_ID_GROUP.equals(id)) return group;
		if (P_ID_TYPE1.equals(id)) return type1;
		if (P_ID_TYPE2.equals(id)) return type2;
		if (P_ID_FOLDER1.equals(id)) return configurationFolder1;
		if (P_ID_FOLDER2.equals(id)) return configurationFolder2;
		return null;
	}
	
	public boolean isPropertySet(Object id) {
		return false;
	}
	
	public void resetPropertyValue(Object id) {
	}
	
	public void setPropertyValue(Object id, Object value) {
	}
}
