package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import com.js.android.AndroidDate;
import com.js.android.App;
import com.js.android.AppPreferences;
import com.js.rbuddy.IReceiptFile;
import com.js.rbuddy.JSDate;
import com.js.rbuddy.TagSetFile;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Maintains data structures and whatnot that are global to the RBuddy app, and
 * used by the various activities
 */
public class RBuddyApp extends App {

	public static final String PREFERENCE_KEY_USE_GOOGLE_DRIVE_API = "use_google_drive_api";

	/**
	 * Convenience method to get the singleton app, assumed to already exist,
	 * cast to an RBuddyApp
	 * 
	 * @return RBuddyApp
	 */
	public static RBuddyApp sharedInstance() {
		return (RBuddyApp) App.sharedInstance();
	}

	/**
	 * Convenience method to get the singleton RBuddyApp object, and construct
	 * it if necessary
	 * 
	 * @return RBuddyApp
	 */
	public static RBuddyApp sharedInstance(Context context) {
		return (RBuddyApp) App.sharedInstance(RBuddyApp.class, context);
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

	protected RBuddyApp(Context context) {
		super(context);

		// Determine whether Google Drive API is to be used, by reading
		// from preferences
		useGoogleAPI();
	}

	public GoogleApiClient getGoogleApiClient() {
		ASSERT(useGoogleAPI());
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

	private GoogleApiClient mGoogleApiClient;
	private Boolean useGoogleAPIFlag;
	private IPhotoStore photoStore;
	private IReceiptFile receiptFile;
	private TagSetFile tagSetFile;
}
