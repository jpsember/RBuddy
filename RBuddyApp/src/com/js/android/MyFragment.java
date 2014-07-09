package com.js.android;

import static com.js.android.Tools.*;

import android.app.Activity;
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

		// mActivityState = new ActivityState(withLogging);
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
	public void onAttach(Activity activity) {
		log("onAttach " + nameOf(activity));
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		log("onCreate " + nameOf(savedInstanceState));
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		log("onActivityCreated " + nameOf(savedInstanceState));
		super.onActivityCreated(savedInstanceState);
	}

	public void onRestoreInstanceState(Bundle bundle) {
		log("onRestoreInstanceState; " + nameOf(bundle) + "\n\n");
	}

	@Override
	public void onStart() {
		log("onStart");
		super.onStart();
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
	public void onSaveInstanceState(Bundle outState) {
		log("onSaveInstanceState; " + nameOf(outState) + "\n\n");
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStop() {
		log("onStop");
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		log("onDestroyView begins");
		super.onDestroyView();
		log("onDestroyView ends");
	}

	@Override
	public void onDestroy() {
		log("onDestroy");
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		log("onDetach");
		super.onDetach();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(nameOf(this));
		sb.append(" #" + mUniqueIdentifier);
		return sb.toString();
	}

	// protected ActivityState getActivityState() {
	// return mActivityState;
	// }

	private static int sNextUniqueIdentifier;

	private int mUniqueIdentifier;
	// private ActivityState mActivityState;
	private boolean mLogging;
}
