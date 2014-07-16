package com.js.rbuddyapp;

import java.util.HashSet;
import java.util.Set;

import com.js.android.App;

import android.content.Context;

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

	private Set<FormWidget.Factory> mAdditionalWidgetTypes;
}
