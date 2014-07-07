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
		setLogging(withLogging);
	}

	protected void setLogging(boolean f) {
		mLogging = f;
	}

	protected void log(Object message) {
		if (mLogging) {
			StringBuilder sb = new StringBuilder("---> ");
			sb.append(nameOf(this));
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
		log("onSaveInstanceState; outState " + outState);
		super.onSaveInstanceState(outState);
		if (mActivityState != null)
			mActivityState.saveState(outState);
	}

	public void onRestoreInstanceState(Bundle bundle) {
		log("onRestoreInstanceState; bundle " + bundle);
	}

	protected ActivityState mActivityState;
	private boolean mLogging;
}
