package js.rbuddy;

import java.io.File;
//import java.util.ArrayList;

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

	public static void prepare(Activity activity) {
		assertUIThread();
		if (sharedInstance == null) {
			sharedInstance = new RBuddyApp(activity);
		}
	}

	/**
	 * Get the singleton instance of the application
	 * 
	 * @return
	 */
	public static RBuddyApp sharedInstance() {
		if (sharedInstance == null)
			die("RBuddyApp must be prepared");

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

			// final boolean db = true;
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
			receiptFile = new SimpleReceiptFile();
		}
		return receiptFile;
	}
	
//	public ArrayList receiptList() {
//		if (receiptList == null) {
//			ArrayList list = new ArrayList();
//			int NUM_RECEIPTS = 50;
//			if (db)
//				timeStamp("building receipts");
//			for (int i = 0; i < NUM_RECEIPTS; i++) {
//				list.add(Receipt.buildRandom());
//			}
//			if (db)
//				timeStamp("done building");
//			receiptList = list;
//
//		}
//		return receiptList;
//	}

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

	public Activity activity() {
		return this.context;
	}
	
	private RBuddyApp(Activity activity) {
		this.context = activity;
		this.preferences = activity.getPreferences(Context.MODE_PRIVATE);
		startApp(); // does nothing if already started
		JSDate.setFactory(AndroidDate.androidDateFactory);
	}

	private SharedPreferences preferences;
	private static RBuddyApp sharedInstance;
	private Activity context;
	private PhotoFile photoFile;
//	private ArrayList receiptList;
	private IReceiptFile receiptFile;
}
