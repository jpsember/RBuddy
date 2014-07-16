package com.js.rbuddyapp;

import com.js.android.App;

import android.content.Context;

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

}
