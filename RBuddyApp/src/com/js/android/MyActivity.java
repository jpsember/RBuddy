package com.js.android;

import static com.js.android.Tools.*;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;

public abstract class MyActivity extends Activity {

	public MyActivity() {
		mFragmentMap = new HashMap();
		mReferenceMap = new HashMap();
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
	 */
	void fragmentCreated(MyFragment f) {
		String name = f.getName();
		log("fragmentCreated " + nameOf(f) + "(name " + name + ")");
		if (name == null)
			throw new IllegalStateException("fragment has no name:" + nameOf(f));

		{
			FragmentReference reference = mReferenceMap.get(name);
			if (reference != null) {
				reference.setFragment(f);
			}
		}

		mFragmentMap.put(name, f);
	}

	<T extends MyFragment> MyFragment getFragment(String name) {
		return mFragmentMap.get(name);
	}

	void addReference(FragmentReference reference) {
		mReferenceMap.put(reference.getName(), reference);
		reference.refresh();
	}

	public <T extends MyFragment> FragmentReference<T> buildFragment(
			Class fragmentClass) {
		FragmentReference<T> ref = new FragmentReference<T>(this, fragmentClass);
		return ref;
	}

	private Map<String, FragmentReference> mReferenceMap;
	private Map<String, MyFragment> mFragmentMap;
	private boolean mLogging;
}
