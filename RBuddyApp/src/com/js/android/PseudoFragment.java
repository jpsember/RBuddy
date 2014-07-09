package com.js.android;

import static com.js.android.Tools.*;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

public abstract class PseudoFragment {

	public PseudoFragment() {
		mActivityState = new ActivityState(this.getClass().getSimpleName());
	}

	protected void setLogging(boolean f) {
		mLogging = f;
	}

	protected void log(Object message) {
		if (mLogging) {
			StringBuilder sb = new StringBuilder("---> ");
			sb.append(this);
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
	}

	public abstract View onCreateView();

	public void onResume() {
		log("onResume");
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

	protected ActivityState getActivityState() {
		return mActivityState;
	}

	protected Context getContext() {
		// TODO: this seems very involved
		return App.sharedInstance().fragments().getActivity();
	}

	private ActivityState mActivityState;
	private boolean mLogging;
}
