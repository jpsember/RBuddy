package js.rbuddy;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import js.basic.Files;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import static js.basic.Tools.*;

/**
 * Maintains data structures and whatnot that are global to the RBuddy app, and
 * used by the various activities
 */
public class RBuddyApp {

	public static final String EXTRA_RECEIPT_ID = "receipt_id";
	private static final String KEY_UNIQUE_IDENTIFIER = "unique_id";

	public static void prepare(Context context) {
		if (sharedInstance == null) {
			if (!testing())
				assertUIThread();
			sharedInstance = new RBuddyApp(context);
			if (db)
				pr("RBuddyApp.prepare, prepared sharedInstance "
						+ sharedInstance);
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

	public static void assertUIThread() {
		if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
			die("not running within UI thread");
		}
	}

	public PhotoFile getPhotoFile() {
		if (photoFile == null) {
			assertUIThread();

			if (db)
				pr("preparePhotoFile; " + stackTrace(1, 1));

			File d = new File(context.getExternalFilesDir(null), "photos");
			if (!d.exists()) {
				d.mkdir();
			}
			if (!d.isDirectory())
				die("failed to create directory " + d);
			photoFile = new PhotoFile(d);
			if (db)
				pr("preparePhotoFile, created " + photoFile + ";\ncontents=\n"
						+ photoFile.contents());
		}
		return photoFile;
	}

	public IReceiptFile receiptFile() {
		if (receiptFile == null) {
			SimpleReceiptFile s = new SimpleReceiptFile();
			receiptFile = s;
			tagSetFile = s.readTagSetFile();
		}
		return receiptFile;
	}

	public TagSetFile tagSetFile() {
		receiptFile();
		return tagSetFile;
	}

	public int getUniqueIdentifier() {
		int value;
		synchronized (this) {
			value = preferences.getInt(KEY_UNIQUE_IDENTIFIER, 1000);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putInt(KEY_UNIQUE_IDENTIFIER, 1 + value);
			editor.commit();
		}
		return value;
	}

	public SharedPreferences getPreferences() {
		return preferences;
	}

	public Context context() {
		return this.context;
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
		if (context instanceof Activity) {
			this.preferences = ((Activity) context)
					.getPreferences(Context.MODE_PRIVATE);
		} else {
			this.preferences = context.getSharedPreferences(
					"__RBuddyApp_test_", Context.MODE_PRIVATE);
		}
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
		JSDate.setFactory(AndroidDate.androidDateFactory);
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

	private Map<String, Integer> resourceMap = new HashMap();
	private SharedPreferences preferences;
	private static RBuddyApp sharedInstance;
	private Context context;
	private PhotoFile photoFile;
	private IReceiptFile receiptFile;
	private TagSetFile tagSetFile;
}
