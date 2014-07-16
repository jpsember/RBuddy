package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import java.util.HashSet;
import java.util.Set;

import com.js.android.App;
import com.js.android.AppPreferences;
import com.js.rbuddy.IReceiptFile;
import com.js.rbuddy.TagSetFile;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.js.android.IPhotoStore;
import com.js.form.Form;
import com.js.form.FormWidget;

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
		this.mReceiptFile = receiptFile;
		this.mTagSetFile = tagSetFile;
		this.mPhotoStore = photoStore;
	}

	public IReceiptFile receiptFile() {
		ASSERT(mReceiptFile != null);
		return mReceiptFile;
	}

	public TagSetFile tagSetFile() {
		ASSERT(mTagSetFile != null);
		return mTagSetFile;
	}

	public IPhotoStore photoStore() {
		ASSERT(mPhotoStore != null);
		return mPhotoStore;
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
		if (mUseGoogleAPIFlag == null) {
			if (testing())
				mUseGoogleAPIFlag = false;
			else {
				mUseGoogleAPIFlag = AppPreferences.getBoolean(
						PREFERENCE_KEY_USE_GOOGLE_DRIVE_API, true);
			}
		}
		return mUseGoogleAPIFlag.booleanValue();
	}

	public Form parseForm(Context context, String json) {
		if (mAdditionalWidgetTypes == null) {
			mAdditionalWidgetTypes = new HashSet();
			mAdditionalWidgetTypes.add(FormTagSetWidget.FACTORY);
		}
		return Form.parse(context, json, mAdditionalWidgetTypes);
	}

	private GoogleApiClient mGoogleApiClient;
	private Boolean mUseGoogleAPIFlag;
	private IPhotoStore mPhotoStore;
	private IReceiptFile mReceiptFile;
	private TagSetFile mTagSetFile;
	private Set<FormWidget.Factory> mAdditionalWidgetTypes;
}
