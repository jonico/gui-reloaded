package com.collabnet.ccf.teamforge.schemageneration;

import java.io.Serial;
import java.net.URL;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.CommonsHTTPSender;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.params.HttpClientParams;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.schemageneration.Proxy;

public class TeamForgeHTTPSender extends CommonsHTTPSender {

	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	protected HostConfiguration getHostConfiguration(HttpClient client, MessageContext context, URL url) {
		Proxy proxy = Activator.getPlatformProxy(url.toString());
		if (proxy != null) {
			proxy.setProxy(client);
		}

		// This needs to be set to 1.0 otherwise errors
		client.getHostConfiguration().getParams().setParameter(HttpClientParams.PROTOCOL_VERSION, HttpVersion.HTTP_1_0);		
		return client.getHostConfiguration();
	}
	
}
