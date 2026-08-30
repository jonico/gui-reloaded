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
package com.collabnet.ccf.migration;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.collabnet.ccf.api.Proxy;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	@SuppressWarnings("rawtypes")
	private ServiceTracker proxyServiceTracker;

	// The plug-in ID
	public static final String PLUGIN_ID = "com.collabnet.ccf.migration"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	public final String ICON_PATH = "icons/"; //$NON-NLS-1$
	public static final String IMG_PROJECT = "project.gif"; //$NON-NLS-1$
	public static final String IMG_REPOSITORY_MAPPING = "repositoryMapping.gif"; //$NON-NLS-1$
	public static final String IMG_REPOSITORY_MAPPING_DIRECTION_RUNNING = "repositoryMappingDirection_running.gif"; //$NON-NLS-1$
	public static final String IMG_REPOSITORY_MAPPING_DIRECTION_PAUSED = "repositoryMappingDirection_paused.gif"; //$NON-NLS-1$
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		proxyServiceTracker = new ServiceTracker(getBundle().getBundleContext(), IProxyService.class.getName(), null);
		proxyServiceTracker.open();	
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		proxyServiceTracker.close();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public IProxyService getProxyService() {
		IProxyService proxyService = null;
		if (null != proxyServiceTracker) {
			proxyService = (IProxyService)proxyServiceTracker.getService();
		}
		return proxyService;
	}    	
	
	@SuppressWarnings("deprecation")
	public static Proxy getPlatformProxy(String url) {
		IProxyService service = getDefault().getProxyService();
		if (null != service && service.isProxiesEnabled()) {
			String host = Proxy.getDomain(url);
			IProxyData data = null;
			if (url.toLowerCase().startsWith("https://")) //$NON-NLS-1$
				data = service.getProxyDataForHost(host, IProxyData.HTTPS_PROXY_TYPE);
			else
				data = service.getProxyDataForHost(host, IProxyData.HTTP_PROXY_TYPE);
			if (null != data && null != data.getHost()) {
				return new Proxy(data.getHost(), data.getPort(), data.isRequiresAuthentication(),
						data.getUserId(), data.getPassword());
			}
		}
		return null;
	}	

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		reg.put(IMG_PROJECT, imageDescriptorFromPlugin(PLUGIN_ID, ICON_PATH + IMG_PROJECT));
		reg.put(IMG_REPOSITORY_MAPPING, imageDescriptorFromPlugin(PLUGIN_ID, ICON_PATH + IMG_REPOSITORY_MAPPING));
		reg.put(IMG_REPOSITORY_MAPPING_DIRECTION_RUNNING, imageDescriptorFromPlugin(PLUGIN_ID, ICON_PATH + IMG_REPOSITORY_MAPPING_DIRECTION_RUNNING));
		reg.put(IMG_REPOSITORY_MAPPING_DIRECTION_PAUSED, imageDescriptorFromPlugin(PLUGIN_ID, ICON_PATH + IMG_REPOSITORY_MAPPING_DIRECTION_PAUSED));
	}
}
