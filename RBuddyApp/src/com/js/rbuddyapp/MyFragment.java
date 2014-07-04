package com.js.rbuddyapp;

import android.app.Fragment;

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
}
