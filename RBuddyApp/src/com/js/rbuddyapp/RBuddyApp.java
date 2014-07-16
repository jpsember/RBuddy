package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import java.util.HashSet;
import java.util.Set;

import com.js.android.App;
import com.js.rbuddy.IReceiptFile;
import com.js.rbuddy.TagSetFile;

import android.content.Context;

import com.js.android.IPhotoStore;
import com.js.form.Form;
import com.js.form.FormWidget;

/**
 * Maintains data structures and whatnot that are global to the RBuddy app, and
 * used by the various activities
 */
public class RBuddyApp extends App {

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
	}

	public Form parseForm(Context context, String json) {
		if (mAdditionalWidgetTypes == null) {
			mAdditionalWidgetTypes = new HashSet();
			mAdditionalWidgetTypes.add(FormTagSetWidget.FACTORY);
		}
		return Form.parse(context, json, mAdditionalWidgetTypes);
	}

	private IPhotoStore mPhotoStore;
	private IReceiptFile mReceiptFile;
	private TagSetFile mTagSetFile;
	private Set<FormWidget.Factory> mAdditionalWidgetTypes;
}
