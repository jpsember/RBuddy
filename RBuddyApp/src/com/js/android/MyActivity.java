package com.js.android;

import static com.js.android.Tools.*;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

public abstract class MyActivity extends Activity {

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
	 * Store fragment within FragmentReference, if one has been registered with
	 * this activity
	 * 
	 * @param f
	 *            fragment
	 */
	void registerFragment(MyFragment f) {
		log("registerFragment " + nameOf(f) + "(name " + f.getName() + ")");
		FragmentReference reference = mReferenceMap.get(f.getName());
		if (reference != null) {
			reference.setFragment(f);
		}
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

	/**
	 * Pop a fragment, and anything above it, from the backstack
	 * 
	 * @param fragmentName
	 *            name of fragment to pop
	 */
	public void popFragment(String fragmentName) {
		assertUIThread();
		FragmentManager manager = getFragmentManager();
		boolean popped = false;
		while (true) {
			if (manager.findFragmentByTag(fragmentName) == null)
				break;
			manager.popBackStack();
			manager.executePendingTransactions();
			popped = true;
		}
		if (!popped) {
			warning("couldn't find fragment " + fragmentName + " on back stack");
		}
	}

	public void buildFragmentOrganizer() {
		if (mFragmentOrganizer != null)
			return;
		mFragmentOrganizer = new FragmentOrganizer(this);
	}

	public FragmentOrganizer getFragmentOrganizer() {
		return mFragmentOrganizer;
	}

	private Map<String, FragmentReference> mReferenceMap = new HashMap();
	private boolean mLogging;
	private FragmentOrganizer mFragmentOrganizer;
}
