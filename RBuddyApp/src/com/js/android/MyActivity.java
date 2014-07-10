package com.js.android;

import static com.js.android.Tools.*;

import com.js.android.FragmentOrganizer;

import android.app.Activity;
import android.os.Bundle;

public class MyActivity extends Activity {
	public MyActivity() {
		this(false);
	}

	public MyActivity(boolean withLogging) {
		mLogging = withLogging;
	}

	private void log(Object message) {
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
		if (mFragments != null)
		mFragments.kill();
		super.onDestroy();
	}

	public FragmentOrganizer getFragments() {
		return mFragments;
	}

	private FragmentOrganizer mFragments;
	private boolean mLogging;
}
