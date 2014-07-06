package com.js.rbuddyapp;

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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivityState != null)
			mActivityState.saveState(outState);
	}

	protected ActivityState mActivityState;
}
