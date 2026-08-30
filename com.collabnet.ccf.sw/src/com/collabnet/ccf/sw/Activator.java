package com.collabnet.ccf.sw;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.collabnet.ccf.model.Landscape;
import com.danube.scrumworks.api2.client.ScrumWorksAPIService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	public static final String SYSTEM_ID = "ScrumWorks Pro"; //$NON-NLS-1$
	
	public static final String IMAGE_SW = "SWLogo.png"; //$NON-NLS-1$
	public static final String IMAGE_SWP_PRODUCT = "swp_product.gif"; //$NON-NLS-1$
	public static final String IMAGE_PBI = "pbi.png"; //$NON-NLS-1$
	public static final String IMAGE_TASK = "task.png"; //$NON-NLS-1$
	public static final String IMAGE_PRODUCT = "product.gif"; //$NON-NLS-1$
	public static final String IMAGE_RELEASE = "release.gif"; //$NON-NLS-1$
	public static final String IMAGE_THEME = "theme.gif"; //$NON-NLS-1$
	public static final String IMAGE_METADATA = "metadata.gif"; //$NON-NLS-1$
	
	// ScrumWorks Properties
	public static final String PROPERTIES_SW_URL = "swp.server.url"; //$NON-NLS-1$
	public static final String PROPERTIES_SW_USER = "swp.server.username"; //$NON-NLS-1$
	public static final String PROPERTIES_SW_PASSWORD = "swp.server.password"; //$NON-NLS-1$
	public static final String PROPERTIES_SW_RESYNC_USER = "swp.server.resync.username"; //$NON-NLS-1$
	public static final String PROPERTIES_SW_RESYNC_PASSWORD = "swp.server.resync.password"; //$NON-NLS-1$
	public static final String PROPERTIES_SW_ATTACHMENT_SIZE = "swp.max.attachmentsize.per.artifact"; //$NON-NLS-1$
	
	// The plug-in ID
	public static final String PLUGIN_ID = "com.collabnet.ccf.sw";

	// The shared instance
	private static Activator plugin;
	
	private Hashtable<String, ImageDescriptor> imageDescriptors;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
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
	
	   /**
     * Returns the image descriptor for the given image ID.
     * Returns null if there is no such image.
     */
    public ImageDescriptor getImageDescriptor(String id) {
    	if (null == imageDescriptors);
    		this.initializeImages();
		return (ImageDescriptor) imageDescriptors.get(id);
    }


	/**
	 * Creates an image and places it in the image registry.
	 */
	private void createImageDescriptor(String id) {
		imageDescriptors.put(id, imageDescriptorFromPlugin(PLUGIN_ID, "icons/" + id)); //$NON-NLS-1$
	}
	
	public static Image getImage(String key) {
		return getDefault().getImageRegistry().get(key);
	}
	
	private void initializeImages() {
		imageDescriptors = new Hashtable<String, ImageDescriptor>(40);
		createImageDescriptor(IMAGE_SW);
		createImageDescriptor(IMAGE_SWP_PRODUCT);
		createImageDescriptor(IMAGE_PBI);
		createImageDescriptor(IMAGE_TASK);
		createImageDescriptor(IMAGE_PRODUCT);
		createImageDescriptor(IMAGE_RELEASE);
		createImageDescriptor(IMAGE_THEME);
		createImageDescriptor(IMAGE_METADATA);
	}
	
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		reg.put(IMAGE_SW, getImageDescriptor(IMAGE_SW));
		reg.put(IMAGE_SWP_PRODUCT, getImageDescriptor(IMAGE_SWP_PRODUCT));
		reg.put(IMAGE_PBI, getImageDescriptor(IMAGE_PBI));
		reg.put(IMAGE_TASK, getImageDescriptor(IMAGE_TASK));
		reg.put(IMAGE_PRODUCT, getImageDescriptor(IMAGE_PRODUCT));
		reg.put(IMAGE_RELEASE, getImageDescriptor(IMAGE_RELEASE));
		reg.put(IMAGE_THEME, getImageDescriptor(IMAGE_THEME));
		reg.put(IMAGE_METADATA, getImageDescriptor(IMAGE_METADATA));
	}
	
	public static ScrumWorksAPIService getScrumWorksEndpoint(Landscape landscape) throws MalformedURLException {
		Properties properties = null;
		if (landscape.getType1().equals(ScrumWorksCcfParticipant.TYPE)) {
			properties = landscape.getProperties1();
		} else {
			properties = landscape.getProperties2();
		}
		
		String url = properties.get(PROPERTIES_SW_URL).toString();
		String user = properties.get(PROPERTIES_SW_USER).toString();
		String password = com.collabnet.ccf.Activator.decodePassword(properties.get(PROPERTIES_SW_PASSWORD).toString());	
		
		return getScrumWorksEndpoint(url, user, password);
	}
	
	public static ScrumWorksAPIService getScrumWorksEndpoint(String url, String user, String password) throws MalformedURLException {
		ScrumWorksAPIService endpoint = null;
		Service service = Service.create(new URL(url), new QName(
				"http://api2.scrumworks.danube.com/",
				"ScrumWorksAPIBeanService"));
		
		endpoint = service.getPort(ScrumWorksAPIService.class);
		((BindingProvider) endpoint).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, user);
		((BindingProvider) endpoint).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
		
		return endpoint;
	}

}
