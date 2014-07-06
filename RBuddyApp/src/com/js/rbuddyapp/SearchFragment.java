package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import com.js.android.ActivityState;
import com.js.form.Form;
import com.js.rbuddy.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;

public class SearchFragment extends MyFragment {

	public static final String TAG = "Search";
	public static Factory FACTORY = new Factory() {

		@Override
		public String name() {
			return TAG;
		}

		@Override
		public MyFragment construct() {
			return new SearchFragment();
		}
	};

	/**
	 * Construct the singleton instance of this fragment, if it hasn't already
	 * been
	 * 
	 * @param organizer
	 * @return
	 */
	public static SearchFragment construct(FragmentOrganizer organizer) {
		return (SearchFragment) organizer.get(TAG, true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mApp = RBuddyApp.sharedInstance();
		layoutElements();
		mActivityState = new ActivityState() //
				.add(mScrollView) //
				.restoreStateFrom(savedInstanceState);
		return mScrollView;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void layoutElements() {

		String jsonString = readTextFileResource(getActivity(),
				R.raw.form_search);
		this.mForm = mApp.parseForm(getActivity(), jsonString);
		mForm.getField("search").setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				performSearch();
			}
		});
		mScrollView = new ScrollView(getActivity());
		mScrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		mScrollView.addView(mForm.getView());
	}

	private void performSearch() {
		toast(getActivity(), "Search isn't yet implemented.");
	}

	private RBuddyApp mApp;
	private Form mForm;
	private ScrollView mScrollView;
}
