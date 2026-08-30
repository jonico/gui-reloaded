package com.collabnet.ccf.schemageneration;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

public class Proxy {
	private String host;
	private int port;
	private boolean authRequired;
	private String username;
	private String password;

	/**
	 * Proxy configuration
	 * @param host - null if no proxy
	 * @param port
	 * @param authRequired
	 * @param username
	 * @param password
	 */
	public Proxy(String host, int port, boolean authRequired,
			String username, String password) {
		super();
		this.host = getDomain(host);
		this.port = port;
		this.authRequired = authRequired;
		this.username = username;
		this.password = password;
	}

	/**
	 * Sets up proxy information on given client
	 * @param client
	 */
	public void setProxy(HttpClient client) {
		if (null == host)
			return;
		client.getHostConfiguration().setProxy(host, port);
		if (authRequired) {
			client.getState().setProxyCredentials(new AuthScope(host, port),
					new UsernamePasswordCredentials(username, password));
		}
	}

	/**
	 * Converts a URL like http://host.domain/ into host.domain
	 * @param repositoryUrl
	 * @return
	 */
	public static String getDomain(String repositoryUrl) {
		String result = repositoryUrl;
		int colonSlashSlash = repositoryUrl.indexOf("://"); //$NON-NLS-1$

		if (0 <= colonSlashSlash) {
			result = repositoryUrl.substring(colonSlashSlash + 3);
		}

		int colonPort = result.indexOf(':');
		int requestPath = result.indexOf('/');

		int substringEnd;

		// minimum positive, or string length
		if (0 < colonPort && 0 < requestPath)
			substringEnd = Math.min(colonPort, requestPath);
		else if (0 < colonPort)
			substringEnd = colonPort;
		else if (0 < requestPath)
			substringEnd = requestPath;
		else
			substringEnd = result.length();

		return result.substring(0, substringEnd);
	}
    /**
     * For testing.
     * 
     * @param args
     */
    public static void main(String[] args) {
    	// Just want to test the getDomain method
   		System.out.println("http://host.domain: " + "host.domain".equals(getDomain("http://host.domain"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  		System.out.println("http://host.domain/: " + "host.domain".equals(getDomain("http://host.domain/"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  		System.out.println("http://host.domain:80: " + "host.domain".equals(getDomain("http://host.domain:80"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  		System.out.println("http://host.domain/blah: " + "host.domain".equals(getDomain("http://host.domain/blah"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  		System.out.println("http://host.domain:80/blah: " + "host.domain".equals(getDomain("http://host.domain:80/blah"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   		System.out.println("http://host.domain.net: " + "host.domain.net".equals(getDomain("http://host.domain.net"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  		System.out.println("http://host.domain.net/: " + "host.domain.net".equals(getDomain("http://host.domain.net/"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  		System.out.println("http://host.domain.net:80: " + "host.domain.net".equals(getDomain("http://host.domain.net:80"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  		System.out.println("http://host.domain.net/blah: " + "host.domain.net".equals(getDomain("http://host.domain.net/blah"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  		System.out.println("http://host.domain.net:80/blah: " + "host.domain.net".equals(getDomain("http://host.domain.net:80/blah"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  	
    }
}
