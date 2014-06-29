package com.js.rbuddyapp;

import static com.js.basic.Tools.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.js.basic.Files;
import com.js.rbuddy.IReceiptFile;
import com.js.rbuddy.JSDate;
import com.js.rbuddy.TagSetFile;

import android.content.Context;
import android.util.DisplayMetrics;

import com.google.android.gms.common.api.GoogleApiClient;
import static com.js.android.Tools.*;

/**
 * Maintains data structures and whatnot that are global to the RBuddy app, and
 * used by the various activities
 */
public class RBuddyApp {

	public static final String PREFERENCE_KEY_USE_GOOGLE_DRIVE_API = "use_google_drive_api";

	public static void prepare(Context context) {
		if (sharedInstance == null) {
			if (!testing())
				assertUIThread();
			sharedInstance = new RBuddyApp(context);
		}
	}

	/**
	 * Get the singleton instance of the application
	 * 
	 * @return
	 */
	public static RBuddyApp sharedInstance() {
		if (sharedInstance == null) {
			if (testing()) {
				sharedInstance = new RBuddyApp(null);
			} else {
				die("RBuddyApp must be prepared");
			}
		}

		return sharedInstance;
	}

	public void setUserData(IReceiptFile receiptFile, TagSetFile tagSetFile,
			IPhotoStore photoStore) {
		this.receiptFile = receiptFile;
		this.tagSetFile = tagSetFile;
		this.photoStore = photoStore;
	}

	public IReceiptFile receiptFile() {
		ASSERT(receiptFile != null);
		return receiptFile;
	}

	public TagSetFile tagSetFile() {
		ASSERT(tagSetFile != null);
		return tagSetFile;
	}

	public IPhotoStore photoStore() {
		ASSERT(photoStore != null);
		return photoStore;
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

	private RBuddyApp(Context context) {
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
		useGoogleAPI();

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

	public String applyStringSubstitution(String s) {
		if (s.startsWith("@")) {
			s = getStringResource(s.substring(1));
		}
		return s;
	}

	public GoogleApiClient getGoogleApiClient() {
		ASSERT(useGoogleAPI());
		// TODO Is GoogleApiClient thread-safe? This code isn't.
		return mGoogleApiClient;
	}

	public void setGoogleApiClient(GoogleApiClient c) {
		ASSERT(useGoogleAPI());
		ASSERT(mGoogleApiClient == null);
		mGoogleApiClient = c;
	}

	public boolean useGoogleAPI() {
		if (useGoogleAPIFlag == null) {
			if (testing())
				useGoogleAPIFlag = false;
			else {
				useGoogleAPIFlag = AppPreferences.getBoolean(
						PREFERENCE_KEY_USE_GOOGLE_DRIVE_API, true);
			}
		}
		return useGoogleAPIFlag.booleanValue();
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

	private GoogleApiClient mGoogleApiClient;
	private Map<String, Integer> resourceMap = new HashMap();
	private static RBuddyApp sharedInstance;
	private Boolean useGoogleAPIFlag;
	private static DisplayMetrics displayMetrics;
	private Context context;
	private IPhotoStore photoStore;
	private IReceiptFile receiptFile;
	private TagSetFile tagSetFile;
}
