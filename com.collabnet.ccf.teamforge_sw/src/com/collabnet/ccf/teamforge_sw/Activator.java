package com.collabnet.ccf.teamforge_sw;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.danube.scrumworks.api2.client.Sprint;
import com.danube.scrumworks.api2.client.Team;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	// Preferences
	public static final String PREFERENCES_MAP_MULTIPLE = "pref_map_multiple"; //$NON-NLS-1$
	public static final boolean DEFAULT_MAP_MULTIPLE = false;
	
	// The plug-in ID
	public static final String PLUGIN_ID = "com.collabnet.ccf.teamforge_sw";

	// The shared instance
	private static Activator plugin;
	
	public static final String GMT_TIME_ZONE_STRING = "GMT";
	
	public final static SimpleDateFormat sprintDateFormat = new SimpleDateFormat(
	"yyyy-MM-dd");
	
	static {
		sprintDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
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
	
	public static String getTeamSprintStringRepresentation(Sprint sprint,
			Team team, String sourceTimezone) {
		Date startDate = sprint.getStartDate();
		startDate = convertToGMTAbsoluteDate(startDate, sourceTimezone);
		Date endDate = sprint.getEndDate();
		endDate = convertToGMTAbsoluteDate(endDate, sourceTimezone);
		StringBuffer value = new StringBuffer(team.getName() + " "
				+ sprintDateFormat.format(startDate) + " - "
				+ sprintDateFormat.format(endDate));
		if (null != sprint.getName() && 0 < sprint.getName().trim().length()) {
			value.append(" -- " + sprint.getName());
		}
		return value.toString();
	}
	
	public static Date convertToGMTAbsoluteDate(Date dateValue,
			String sourceSystemTimezone) {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone(sourceSystemTimezone));
		cal.setLenient(false);
		cal.setTime(dateValue);
		Calendar newCal = new GregorianCalendar(TimeZone.getTimeZone(GMT_TIME_ZONE_STRING));
		newCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),0,0,0);
		newCal.set(Calendar.MILLISECOND, 0);
		Date returnDate = newCal.getTime();
		return returnDate;
	}

}
