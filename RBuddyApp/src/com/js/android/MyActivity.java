package com.js.android;

import static com.js.android.Tools.*;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;

public abstract class MyActivity extends Activity {
	public MyActivity() {
		mFragmentMap = new HashMap();
	}

	public void setLogging(boolean f) {
		mLogging = f;
	}

	protected void log(Object message) {
		if (mLogging) {
			StringBuilder sb = new StringBuilder("===> ");
			sb.append(nameOf(this));
			sb.append(" : ");
			tab(sb, 30);
			sb.append(message);
			pr(sb);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log("onCreate savedInstanceState=" + nameOf(savedInstanceState));
		super.onCreate(savedInstanceState);
		refreshFragmentsAux();
	}

	@Override
	protected void onResume() {
		log("onResume");
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		log("onSaveInstanceState outState=" + nameOf(outState));
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		log("onPause");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		log("onDestroy");
		super.onDestroy();
	}

	/**
	 * Called when a fragment has been created, so we can store it in our map of
	 * current fragments; any older version will be bumped out
	 * 
	 * @param f
	 * @return true if fragment is different than previous one in the map
	 */
	void fragmentCreated(MyFragment f) {
		String name = f.getName();
		log("fragmentCreated " + nameOf(f) + "(name " + name + ")");
		if (name == null)
			throw new IllegalStateException("fragment has no name:" + nameOf(f));
		MyFragment fPrevious = mFragmentMap.put(name, f);
		if (fPrevious != null) {
			// Make sure old fragment is not 'active'; we don't want to be
			// confused about which one should be used
			if (fPrevious.isResumed())
				die("old fragment " + nameOf(fPrevious) + " is still active!");
		}
		if (fPrevious != f) {
			log(" (previous fragment=" + nameOf(fPrevious) + ")");
			refreshFragmentsAux();
		}
	}

	private void refreshFragmentsAux() {
		// Avoid recursive rentry
		if (mRefreshingFragments)
			return;
		mRefreshingFragments = true;
		refreshFragments();
		mRefreshingFragments = false;
	}

	public abstract void refreshFragments();

	<T extends MyFragment> MyFragment getFragment(String name) {
		return mFragmentMap.get(name);
	}

	private Map<String, MyFragment> mFragmentMap;
	private boolean mLogging;
	private boolean mRefreshingFragments;
}
