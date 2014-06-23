package js.rbuddyapp;

import static js.basic.Tools.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import js.basic.Files;
import js.rbuddy.IReceiptFile;
import js.rbuddy.JSDate;
import js.rbuddy.TagSetFile;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Maintains data structures and whatnot that are global to the RBuddy app, and
 * used by the various activities
 */
public class RBuddyApp {

	public static final boolean useGoogleAPI = false;

	public static final String EXTRA_RECEIPT_ID = "receipt_id";

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
		final boolean db = true;
		if (db)
 {
			Log.e("Fuck", "assertUIThread, testing=" + testing());

			pr("RBuddyApp.assertUIThread");
		}
		if (db)
			pr(" testing=" + testing());

		if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
			die("not running within UI thread");
		}
	}

	public static void assertNotUIThread() {
		if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
			die("unexpectedly running within UI thread");
		}
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

	public void dumpIntent(Activity activity) {
		Intent intent = activity.getIntent();
		Bundle bundle = intent.getExtras();
		pr(activity.getClass().getSimpleName() + " Intent:");
		for (String key : bundle.keySet()) {
			Object value = bundle.get(key);
			pr("  " + key + " : " + describe(value));
		}
	}

	public GoogleApiClient getGoogleApiClient() {
		ASSERT(useGoogleAPI);
		// TODO Is GoogleApiClient thread-safe? This code isn't.
		return mGoogleApiClient;
	}

	public void setGoogleApiClient(GoogleApiClient c) {
		ASSERT(useGoogleAPI);
		ASSERT(mGoogleApiClient == null);
		mGoogleApiClient = c;
	}

	public static void confirmOperation(Activity activity,
			String warningMessage, final Runnable operation) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					operation.run();
					break;
				}
			}
		};
		Context c = activity.getWindow().getDecorView().getRootView()
				.getContext();
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setMessage(warningMessage)
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();
	}

	private GoogleApiClient mGoogleApiClient;
	private Map<String, Integer> resourceMap = new HashMap();
	private static RBuddyApp sharedInstance;

	// This is a pretty grim way of achieving this...

	public static boolean isReceiptListValid() {
		return receiptListValid;
	}

	public static void setReceiptListValid(boolean f) {
		receiptListValid = f;
	}

	private static boolean receiptListValid;

	private Context context;
	private IPhotoStore photoStore;
	private IReceiptFile receiptFile;
	private TagSetFile tagSetFile;
}
