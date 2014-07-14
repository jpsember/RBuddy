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
		if (db)
			pr(hey() + nameOf(f) + " (name=" + f.getName() + ")");

		String name = f.getName();
		log("fragmentCreated " + nameOf(f) + "(name " + name + ")");
		if (name == null)
			throw new IllegalStateException("fragment has no name:" + nameOf(f));

		if (db)
			pr(" mFragmentMap:\n" + d(mFragmentMap));

		MyFragment fPrevious = mFragmentMap.put(name, f);
		if (db)
			pr("  stored new " + nameOf(f) + "; previous=" + nameOf(fPrevious));

		if (fPrevious != null) {
			// Make sure old fragment is not 'active'; we don't want to be
			// confused about which one should be used
			ASSERT(!fPrevious.isResumed(), "old fragment " + nameOf(fPrevious)
					+ " is still active");
		}

		boolean differs = (fPrevious != f);
		if (differs) {
			log(" (previous fragment=" + nameOf(fPrevious) + ")");
			if (db)
				pr("  calling refreshFragmentsAux");
			refreshFragmentsAux();
		}
	}

	private void refreshFragmentsAux() {
		if (db)
			pr("refreshFragmentsAux  mRef=" + d(mRefreshingFragments)
					+ " mFragmentsMap=" + d(mFragmentMap));
		if (mRefreshingFragments)
			return;
		mRefreshingFragments = true;
		refreshFragments();
		mRefreshingFragments = false;
		if (db)
			pr(" done refreshFragmentsAux");
	}

	public abstract void refreshFragments();

	// TODO: make this package visibility again once done experiment
	public <T extends MyFragment> MyFragment getFragment(String name) {
		return mFragmentMap.get(name);
	}

	/**
	 * @deprecated make this package visibility
	 * @param f
	 * @return
	 */
	public <T extends MyFragment> T getFragment(T f) {
		if (f == null)
			return null;
		return (T) mFragmentMap.get(f.getName());
	}

	private Map<String, MyFragment> mFragmentMap;
	private boolean mLogging;
	private boolean mRefreshingFragments;
}