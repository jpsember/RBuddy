package com.js.android;

import static com.js.basic.Tools.*;

import android.app.FragmentManager;

public class FragmentReference<T extends MyFragment> {

	public FragmentReference(MyActivity activity, Class theClass) {
		mActivity = activity;
		mClass = theClass;
		mName = MyFragment.deriveFragmentName(theClass);
		if (db)
			pr("constructed FragmentReference name:" + mName + " activity:"
					+ nameOf(mActivity));
	}

	public void refresh() {
		if (db)
			pr("refresh " + this);
		T fragment = mFragment;
		T fragment2 = (T) mActivity.getFragment(mName);
		if (db)
			pr(" activity map yields " + nameOf(fragment2));

		if (fragment2 != null && fragment2 != fragment) {
			fragment = fragment2;
		}

		// TODO:not sure this code is required

		if (false && fragment == null) {
			// See if there's one in the FragmentManager
			FragmentManager m = mActivity.getFragmentManager();
			fragment = (T) m.findFragmentByTag(mName);
			if (fragment != null)
				pr(hey()
						+ "======================================= found in frag manager: "
						+ fragment);
		}

		if (fragment == null) {
			try {
				fragment = (T) mClass.newInstance();
				if (db)
					pr(" constructed new instance " + nameOf(fragment));
			} catch (Throwable e) {
				die("failed to build instance of " + mName, e);
			}
			if (db)
				pr(" registering " + nameOf(fragment) + " with "
						+ nameOf(mActivity));
			fragment.register(mActivity);
		}
		mFragment = fragment;
		if (db)
			pr(" done refresh: " + this);
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
