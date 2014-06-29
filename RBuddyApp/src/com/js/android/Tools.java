package com.js.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

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

	/**
	 * Display a yes/no dialog box to confirm an operation. For test purposes
	 * only (uses fixed English yes/no strings)
	 * 
	 * @param activity
	 * @param warningMessage
	 *            message to display
	 * @param operation
	 *            operation to perform if user selects 'yes'
	 */
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

	/**
	 * Debug purposes only; get description of an activity's intent
	 * 
	 * @param activity
	 * @return
	 */
	public static String dumpIntent(Activity activity) {
		StringBuilder sb = new StringBuilder(nameOf(activity) + " Intent:");

		Intent intent = activity.getIntent();

		Bundle bundle = intent.getExtras();
		for (String key : bundle.keySet()) {
			Object value = bundle.get(key);
			sb.append("  " + key + " : " + describe(value));
		}
		return sb.toString();
	}

	/**
	 * Display a toast message of short duration
	 * 
	 * @param context
	 * @param message
	 */
	public static void toast(Context context, String message) {
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, message, duration);
		toast.show();
	}

}
