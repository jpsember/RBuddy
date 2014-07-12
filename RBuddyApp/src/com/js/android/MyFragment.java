package com.js.android;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import static com.js.android.Tools.*;

public class MyFragment extends Fragment {

	public MyFragment() {
		String s = getClass().getSimpleName();
		if (s.endsWith("Fragment"))
			s = s.substring(0, s.length() - "Fragment".length());
		setName(s);
	}

	@Override
	public void onAttach(Activity activity) {
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
		getActivityState().persistSnapshot(outState);
	}

	/**
	 * Name this fragment
	 * 
	 * @param name
	 *            name to distinguish it from other fragments
	 */
	public void setName(String name) {
		pr("setName of " + nameOf(this) + " to " + name);
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
		return mActivityState;
	}

	protected void setLogging(boolean logging) {
		mLogging = logging;
	}

	public <T extends MyFragment> T register(MyActivity activity) {
		activity.fragmentCreated(this);
		return (T) this;
	}

	private ViewStates mActivityState = new ViewStates(this.getClass()
			.getSimpleName());;
	private boolean mLogging;
	private String mName;
}
