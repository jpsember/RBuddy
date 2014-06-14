package js.rbuddy;

import java.io.File;
//import java.util.ArrayList;

import java.util.Calendar;
import java.util.Locale;

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
		}
		JSDate.setFactory(AndroidDate.androidDateFactory);
	}

	private SharedPreferences preferences;
	private static RBuddyApp sharedInstance;
	private Context context;
	private PhotoFile photoFile;
	private IReceiptFile receiptFile;
	private TagSetFile tagSetFile;
}
