package com.js.android;

import static com.js.android.Tools.*;

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.util.DisplayMetrics;

import com.js.basic.Files;
import com.js.rbuddy.JSDate;

/**
 * Utility methods associated with an application context
 * 
 */
public class App {

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
		if (db)
			pr("\n  returning sharedInstance " + describe(sharedInstance)
					+ "\n\n\n");

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

	protected App(Context context) {
		this.context = context;

		AppPreferences.prepare(this.context);

		if (!testing()) {
			AndroidSystemOutFilter.install();

			// Print message about app starting. Print a bunch of newlines
			// to
			// simulate
			// clearing the console, and for convenience, print the time of
			// day
			// so we can figure out if the
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

	public String getStringResource(String stringName) {
		String packageName = context.getPackageName();
		int resId = context.getResources().getIdentifier(stringName, "string",
				packageName);
		if (db)
			pr("getIdentifier string='" + stringName + "' package='"
					+ packageName + "' yields resId " + resId);
		String str = null;
		if (resId != 0)
			str = context.getString(resId);
		if (db)
			pr(" string for id " + resId + " = " + str);
		if (str == null)
			throw new IllegalArgumentException("string name " + stringName
					+ "  has resource id " + resId + ", no string found");
		return str;
	}

	public String readTextFileResource(int resourceId) {
		String str = null;
		try {
			str = Files.readTextFile(context.getResources().openRawResource(
					resourceId));
		} catch (Throwable e) {
			die("problem reading resource #" + resourceId, e);
		}
		return str;
	}

	public String applyStringSubstitution(String s) {
		if (s.startsWith("@")) {
			s = getStringResource(s.substring(1));
		}
		return s;
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

	protected static App sharedInstance;
	protected Context context;
	private static DisplayMetrics displayMetrics;
	private Map<String, Integer> resourceMap = new HashMap();
}
