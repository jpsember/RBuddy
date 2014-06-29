package com.js.android;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;

public class Tools extends com.js.basic.Tools {

	public static void assertUIThread() {
		if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
			die("not running within UI thread");
		}
	}

	public static void assertNotUIThread() {
		if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
			die("unexpectedly running within UI thread");
		}
	}

	/**
	 * Construct an Intent for starting an activity
	 * 
	 * @param context
	 *            current activity's context
	 * @param theClass
	 *            the activity's class
	 * @return intent
	 */
	public static Intent startIntentFor(Context context, Class theClass) {
		return new Intent(context, theClass);
	}

}
