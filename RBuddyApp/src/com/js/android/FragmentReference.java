package com.js.android;

import static com.js.basic.Tools.*;

import android.app.FragmentManager;

public class FragmentReference<T extends MyFragment> {

	public FragmentReference(MyActivity activity, Class theClass) {
		mActivity = activity;
		mClass = theClass;
		mName = MyFragment.deriveFragmentName(theClass);
	}

	public void refresh() {
		if (db)
			pr("refresh " + this);
		T fragment = mFragment;

		// Replace with the instance that the activity is storing, if such an
		// instance exists
		{
			T fragmentFromActivityMap = (T) mActivity.getFragment(mName);
			if (fragmentFromActivityMap != null) {
				fragment = fragmentFromActivityMap;
			}
		}

		// If still null, see if there's one in the FragmentManager (which may
		// include items in the back stack)
		if (fragment == null) {
			FragmentManager manager = mActivity.getFragmentManager();
			fragment = (T) manager.findFragmentByTag(mName);
		}

		// If it's still null, construct a new instance and register it with the
		// activity
		if (fragment == null) {
			try {
				fragment = (T) mClass.newInstance();
			} catch (Throwable e) {
				die("failed to build instance of " + mName, e);
			}
			mActivity.fragmentCreated(fragment);
		}
		mFragment = fragment;
	}

	public T f() {
		ASSERT(mFragment != null);
		return mFragment;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(nameOf(this));
		sb.append(" mName:" + mName);
		sb.append(" mFragment:" + nameOf(mFragment));

		return sb.toString();
	}

	// Maybe add this support later
	// /**
	// * Subclasses can override this to restore state from JSON string
	// *
	// * @param jsonString
	// * previous state, or null if none
	// */
	// public void restoreState(String jsonString) {
	// }
	//
	// /**
	// * Subclasses can override this to save state to JSON string
	// *
	// * @param json
	// */
	// public void saveState(JSONEncoder json) {
	// }

	private MyActivity mActivity;
	private T mFragment;
	private String mName;
	private Class mClass;
}
