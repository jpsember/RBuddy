package com.js.android;

import static com.js.android.Tools.*;

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.util.DisplayMetrics;

import com.js.rbuddy.JSDate;

/**
 * Utility methods associated with an application context
 * 
 */
public class App {

	public static final String PREFERENCE_KEY_SMALL_DEVICE_FLAG = "small_device";

	/**
	 * Get the singleton instance of the application, preparing it if necessary
	 * (must be called from UI thread in this case)
	 * 
	 * @param appSubClass
	 *            the class of the app to construct (if construction is
	 *            necessary)
	 * @param context
	 *            application's context (if construction is necessary)
	 * @return the singleton
	 */
	public static App sharedInstance(Class appSubClass, Context context) {
		if (sharedInstance != null)
			return sharedInstance;

		if (!testing())
			assertUIThread();

		try {
			Class[] paramTypes = { Context.class };
			Object[] paramValues = { context };

			Constructor c = appSubClass.getDeclaredConstructor(paramTypes);
			c.setAccessible(true);
			final App v = (App) c.newInstance(paramValues);

			sharedInstance = v;

		} catch (Throwable e) {
			die("failed to construct app instance", e);
		}
		return sharedInstance;
	}

	/**
	 * Get the singleton instance of the application, one that is known to have
	 * been already prepared
	 * 
	 * @return the singleton
	 */
	public static App sharedInstance() {
		ASSERT(sharedInstance != null);
		return sharedInstance;
	}

	private void prepareSystemOut() {
		AndroidSystemOutFilter.install();

		// Print message about app starting. Print a bunch of newlines
		// to simulate clearing the console, and for convenience,
		// print the time of day so we can figure out if the
		// output is current or not.

		String strTime = "";
		{
			Calendar cal = Calendar.getInstance();
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
					"h:mm:ss", Locale.CANADA);
			strTime = sdf.format(cal.getTime());
		}
		pr("\n\n\n\n\n\n\n\n\n\n\n\n\n--------------- Start of App ----- "
				+ strTime + " -------------\n\n\n");

	}

	protected App(Context context) {
		this.context = context;

		AppPreferences.prepare(this.context);

		if (!testing()) {
			prepareSystemOut();
			addResourceMappings();
		}
		JSDate.setFactory(AndroidDate.androidDateFactory(context));
		displayMetrics = context.getResources().getDisplayMetrics();
	}

	/**
	 * Store the resource id associated with the resource's name, so we can
	 * refer to them by name (for example, we want to be able to refer to them
	 * within JSON strings).
	 * 
	 * There are some facilities to do this mapping using reflection, but
	 * apparently it's really slow.
	 * 
	 * @param key
	 * @param resourceId
	 */
	public void addResource(String key, int resourceId) {
		resourceMap.put(key, resourceId);
	}

	/**
	 * Get the resource id associated with a resource name (added earlier).
	 * 
	 * @param key
	 * @return resource id
	 * @throws IllegalArgumentException
	 *             if no mapping exists
	 */
	public int getResource(String key) {
		Integer id = resourceMap.get(key);
		if (id == null)
			throw new IllegalArgumentException(
					"no resource id mapping found for " + key);
		return id.intValue();
	}

	private void addResourceMappings() {
		addResource("photo", android.R.drawable.ic_menu_gallery);
		addResource("camera", android.R.drawable.ic_menu_camera);
		addResource("search", android.R.drawable.ic_menu_search);
	}

	/**
	 * Convert density pixels to true pixels
	 * 
	 * @param densityPixels
	 * @return
	 */
	public static int truePixels(float densityPixels) {
		return (int) (densityPixels * displayMetrics.density);
	}

	// public void setFragments(FragmentOrganizer f) {
	// assertUIThread();
	// mFragments = f;
	// }
	//
	// public FragmentOrganizer fragments() {
	// assertUIThread();
	// ASSERT(mFragments != null, "no FragmentOrganizer defined");
	// return mFragments;
	// }

	protected static App sharedInstance;
	protected Context context;
	private static DisplayMetrics displayMetrics;
	private Map<String, Integer> resourceMap = new HashMap();
	// private FragmentOrganizer mFragments;
}
