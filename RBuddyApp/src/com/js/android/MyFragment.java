package com.js.android;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import static com.js.android.Tools.*;

public class MyFragment extends Fragment {

	static String deriveFragmentName(Class c) {
		String s = c.getSimpleName();
		if (s.endsWith("Fragment"))
			s = s.substring(0, s.length() - "Fragment".length());
		return s;
	}

	public MyFragment() {
		setName(deriveFragmentName(this.getClass()));
	}

	@Override
	public void onAttach(Activity activity) {
		if (db)
			pr(hey());
		log("onAttach");
		super.onAttach(activity);
		((MyActivity) getActivity()).fragmentCreated(this);
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
		log("onDestroyView done");
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		log("onDestroy done");
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		log("onSaveInstanceState");
		super.onSaveInstanceState(outState);
		log(" persisting activity state " + nameOf(getActivityState()));
		getActivityState().persistSnapshot(outState);
	}

	/**
	 * Name this fragment
	 * 
	 * @param name
	 *            name to distinguish it from other fragments
	 */
	public void setName(String name) {
		mName = name;
	}

	public String getName() {
		return mName;
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

	protected final ViewStates getActivityState() {
		if (db) {
			warning("enabling ActivityState logging");
			mActivityState.setLogging(true);
		}
		return mActivityState;
	}

	protected void setLogging(boolean logging) {
		mLogging = logging;
	}

	private ViewStates mActivityState = new ViewStates(this.getClass()
			.getSimpleName());
	private boolean mLogging;
	private String mName;
}
