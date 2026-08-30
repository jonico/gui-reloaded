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

package com.collabnet.ccf;

import java.io.IOException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * This class gives us access to the JMX properties of a running CCF instance via RMI
 * @author jnicolai
 *
 */
public class CCFJMXMonitorBean {
	public static final String PROCESS_TIME = "ProcessTime";
	public static final String OUTPUT_MSGS = "OutputMsgs";
	public static final String INPUT_MSGS = "InputMsgs";
	public static final String OPENADAPTOR_ID_EXCEPTION_CONVERTOR_METRICS = "openadaptor:id=ExceptionConvertor-metrics";
	public static final String UPTIME = "Uptime";
	public static final String JAVA_LANG_TYPE_RUNTIME = "java.lang:type=Runtime";
	public static final String EXIT = "exit";
	public static final String MEMORY = "Memory";
	public static final String OPENADAPTOR_ID_SYSTEM_UTIL = "openadaptor:id=SystemUtil";
	public static final String TFREADER_METRICS = "openadaptor:id=TFReader-metrics";
	public static final String TFWRITER_METRICS = "openadaptor:id=TFWriter-metrics";
	public static final String QCREADER_METRICS = "openadaptor:id=QCReader-metrics";
	public static final String QCWRITER_METRICS = "openadaptor:id=QCWriter-metrics";
	public static final String PTREADER_METRICS = "openadaptor:id=PTReader-metrics";
	public static final String PTWRITER_METRICS = "openadaptor:id=PTWriter-metrics";
	
	public static final String DEFAULT_HOSTNAME = "localhost";
	public static final int QC2PT_PORT = 9999;
	public static final int PT2QC_PORT = 10000;
	public static final int TF2QC_PORT = 10001;
	public static final int QC2TF_PORT = 10002; 
	public static final int TF2SWP_PORT = 10010;
	public static final int SWP2TF_PORT = 10011;
	
	private String hostName=DEFAULT_HOSTNAME;
	private int rmiPort=QC2TF_PORT;
	private JMXConnector connector = null;
	private MBeanServerConnection connection = null;
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	public String getHostName() {
		return hostName;
	}
	
	public void setRmiPort(String fromType, String toType) {
		if (fromType.equals("TF") && toType.equals("QC")) {
			rmiPort = TF2QC_PORT;
		}
		else if (fromType.equals("QC") && toType.equals("TF")) {
			rmiPort = QC2TF_PORT;
		}
		else if (fromType.equals("TF") && toType.equals("SWP")) {
			rmiPort = TF2SWP_PORT;
		}
		else if (fromType.equals("SWP") && toType.equals("TF")) {
			rmiPort = SWP2TF_PORT;
		}
		else if (fromType.equals("QC") && toType.equals("PT")) {
			rmiPort = QC2PT_PORT;
		}
		else if (fromType.equals("PT") && toType.equals("QC")) {
			rmiPort = PT2QC_PORT;
		}
	}
	
	public void setRmiPort(int rmiPort) {
		this.rmiPort = rmiPort;
	}
	
	public int getRmiPort() {
		return rmiPort;
	}
	
	/**
	 * Connects to the JMX port
	 * If this operation fails, CCFInstance is not up and running
	 * @throws IOException 
	 */
	private void connect() throws IOException {
		close();
		JMXServiceURL url=new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+hostName+":"+rmiPort+"/jmxrmi");
		connector=JMXConnectorFactory.connect(url);
		connection = connector.getMBeanServerConnection();
	}
	
	/**
	 * Finds out whether CCFInstance is alive
	 * @return true if CCF is alive, false if not
	 */
	public boolean isAlive() {
		if (null == connection) {
			try {
				connect();
			} catch (IOException e) {
				return false;
			}
		}
		else {
			try {
				connection.getMBeanCount();
			} catch (IOException e) {
				try {
					connect();
				} catch (IOException e1) {
					return false;
				}
			}
		}
		return true;
	}
	
	private void close() {
		if (null != connector) {
			try {
				connection=null;
				connector.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}
	
	/**
	 * Executes JMX method
	 * @param objectName
	 * @param operationName
	 * @param params
	 * @param signature
	 * @return result or null if operation failed
	 */
	public Object invokeJMXOperation (String objectName, String operationName, Object[] params, String [] signature) {
		if (!isAlive()) {
			return null;
		}
		try {
			Object result= connection.invoke(new ObjectName(objectName), operationName, params, signature);
			return result;
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
			return null;
		} catch (MBeanException e) {
			e.printStackTrace();
			return null;
		} catch (ReflectionException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void restartCCFInstance() {
		invokeJMXOperation(OPENADAPTOR_ID_SYSTEM_UTIL, EXIT, new Object[]{}, new String[] {});
	}
	
	/**
	 * Returns JMX attribute value
	 * @param objectName
	 * @param attributeName
	 * @return value or null if an error occured
	 */
	public Object getJMXAttribute (String objectName, String attributeName) {
		if (!isAlive()) {
			return null;
		}
		try {
			return connection.getAttribute(new ObjectName(objectName), attributeName);
		} catch (AttributeNotFoundException e) {
			return null;
		} catch (InstanceNotFoundException e) {
			return null;
		} catch (MalformedObjectNameException e) {
			return null;
		} catch (MBeanException e) {
			return null;
		} catch (ReflectionException e) {
			return null;
		} catch (NullPointerException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Returns CCF memory consumption
	 * @return memory consumption or null if info is not available 
	 */
	public String getCCFMemoryConsumption() {
		return (String) getJMXAttribute(OPENADAPTOR_ID_SYSTEM_UTIL, MEMORY);
	}
	
	/**
	 * Returns number of exception received by the CCF Exception handler
	 * @return number of exception received by the CCF Exception handler
	 */
	public String getNumberOfCCFExceptionsCaught() {
		return (String) getJMXAttribute(OPENADAPTOR_ID_EXCEPTION_CONVERTOR_METRICS, INPUT_MSGS);
	}
	
	/**
	 * Returns number of artifacts quarantined by the CCF Exception handler
	 * @return number of artifacts quarantined by the CCF Exception handler
	 */
	public String getNumberOfArtifactsQuarantined() {
		return (String) getJMXAttribute(OPENADAPTOR_ID_EXCEPTION_CONVERTOR_METRICS, OUTPUT_MSGS);
	}
	
	/**
	 * Returns number of artifacts shipped
	 * @param readerMetricsName name of JMX OA reader metrics name 
	 * @return
	 */
	public String getNumberOfArtifactsShipped(String readerMetricsName) {
		return (String) getJMXAttribute(readerMetricsName, OUTPUT_MSGS);
	}
	
	/**
	 * Returns the number of artifacts waiting to be synchronized per
	 * synchronization status record
	 * 
	 * @param sourceSystemId     source system id
	 * @param sourceRepositoryId source repository id
	 * @param targetSystemId     target system id
	 * @param targetRepositoryId target repository id
	 * @return number of artifacts waiting for specific synchronization status record
	 */
	public String getNumberOfWaitingArtifacts(String sourceSystemId, String sourceRepositoryId, String targetSystemId, String targetRepositoryId) {
		return (String) invokeJMXOperation("CCF:name=AbstractReader", "getNumberOfWaitingArtifacts",
				new String[] { sourceSystemId, sourceRepositoryId, targetSystemId, targetRepositoryId },
				new String[] { "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String"});
	}	
	
	/**
	 * Returns stats about processing time needed to extract artifacts
	 * @param readerMetricsName name of JMX OA reader metrics name 
	 * @return
	 */
	public String getArtifactExtractionProcessingTime(String readerMetricsName) {
		return (String) getJMXAttribute(readerMetricsName, PROCESS_TIME);
	}
	
	/**
	 * Returns stats about
	 * @param writerMetricsName name of JMX OA reader metrics name 
	 * @return
	 */
	public String getArtifactUpdateProcessingTime(String writerMetricsName) {
		return (String) getJMXAttribute(writerMetricsName, PROCESS_TIME);
	}
	
	/**
	 * Returns CCF uptime in milliseconds
	 * @return uptime in milliseconds or null if info is not available 
	 */
	public Long getCCFUptime() {
		return (Long) getJMXAttribute(JAVA_LANG_TYPE_RUNTIME, UPTIME);
	}
	
	
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
	
}
