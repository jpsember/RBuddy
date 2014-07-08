package com.js.android;

import static com.js.android.Tools.*;

import com.js.android.ActivityState;

import android.app.Fragment;
import android.os.Bundle;

public abstract class MyFragment extends Fragment {

	public static interface Factory {
		/**
		 * Get name of fragment. This is what is used as its tag when it is
		 * added to an activity
		 * 
		 * @return
		 */
		public String name();

		public MyFragment construct();
	}

	public MyFragment() {
		this(false);
	}

	public MyFragment(boolean withLogging) {
		assertUIThread();
		mUniqueIdentifier = ++sNextUniqueIdentifier;
		setLogging(withLogging);
	}

	protected void setLogging(boolean f) {
		mLogging = f;
	}

	protected void log(Object message) {
		if (mLogging) {
			StringBuilder sb = new StringBuilder("---> ");
			sb.append(this); // nameOf(this));
			sb.append(" : ");
			tab(sb, 30);
			sb.append(message);
			pr(sb);
		}
	}

	@Override
	public void onResume() {
		log("onResume");
		super.onResume();
	}

	@Override
	public void onPause() {
		log("onPause");
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		log("onDestroyView finished");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		log("onSaveInstanceState; " + nameOf(outState) + "\n\n");
		super.onSaveInstanceState(outState);
		if (mActivityState != null) {
			log("saving ActivityState");
			mActivityState.saveState(outState);
		}
	}

	public void onRestoreInstanceState(Bundle bundle) {
		log("onRestoreInstanceState; " + nameOf(bundle) + "\n\n");
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(nameOf(this));
		sb.append(" #" + mUniqueIdentifier);
		return sb.toString();
	}

	private static int sNextUniqueIdentifier;

	private int mUniqueIdentifier;
	protected ActivityState mActivityState;
	private boolean mLogging;
}
