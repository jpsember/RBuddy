package com.js.android;

import static com.js.android.Tools.*;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

public abstract class PseudoFragment {

	public PseudoFragment(FragmentOrganizer f) {
		mFragments = f;
		if (db)
			setLogging(true);
		mActivityState = new ViewStates(this.getClass().getSimpleName());
	}

	protected void setLogging(boolean f) {
		mLogging = f;
	}

	protected void log(Object message) {
		if (mLogging) {
			StringBuilder sb = new StringBuilder("---> ");
			sb.append(nameOf(this));
			sb.append(" res=" + d(isResumed()));
			sb.append(" : ");
			tab(sb, 30);
			sb.append(message);
			pr(sb);
		}
	}

	public void onCreate() {
		log("onCreate");
	}

	public void onRestoreInstanceState(Bundle bundle) {
		log("onRestoreInstanceState bundle " + nameOf(bundle));
		mActivityState.retrieveSnapshotFrom(bundle);
		mFragments = mFragments.mostRecent();
	}

	public abstract View onCreateView(MyFragment fragment);

	/**
	 * Package visibility entry point for MyFragment, so our isResumed() returns
	 * true from start of onResume() through end of onPause()
	 */
	void onResumeAux() {
		mIsResumed = true;
		onResume();
	}

	public void onResume() {
		log("onResume");
	}

	void onPauseAux() {
		onPause();
		mIsResumed = false;
	}

	public void onPause() {
		log("onPause");
	}

	public void onSaveViews() {
		log("onSaveViews");
		getActivityState().recordSnapshot();
	}

	public void onSaveInstanceState(Bundle bundle) {
		log("onSaveInstanceState bundle " + nameOf(bundle));
		getActivityState().persistSnapshot(bundle);
	}

	public void onDestroyView() {
		log("onDestroyView");
		getActivityState().clearElementList();
	}

	public void onDestroy() {
		log("onDestroy");
	}

	protected ViewStates getActivityState() {
		return mActivityState;
	}

	protected Context getContext() {
		return mFragments.getActivity();
	}

	public boolean isResumed() {
		return mIsResumed;
	}

	public FragmentOrganizer getFragments() {
		return mFragments;
	}

	public void setFragments(FragmentOrganizer f) {
		mFragments = f;
	}


	private FragmentOrganizer mFragments;
	private ViewStates mActivityState;
	private boolean mLogging;
	private boolean mIsResumed;
}
