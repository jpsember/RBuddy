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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		layoutElements();
		activityState = new ActivityState() //
				.add(scrollView) //
				.restoreStateFrom(savedInstanceState);
		return scrollView;
	}

	@Override
	public void onResume() {
		super.onResume();
		// readWidgetValuesFromReceipt();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		activityState.saveState(outState);
	}

	private void layoutElements() {

		String jsonString = readTextFileResource(getActivity(),
				R.raw.form_search);
		this.form = Form.parse(getActivity(), jsonString);
		form.getField("search").setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				performSearch();
			}
		});
		scrollView = new ScrollView(getActivity());
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		scrollView.addView(form.getView());
	}

	private void performSearch() {
		toast(getActivity(), "Search isn't yet implemented.");
	}

	private Form form;
	private ScrollView scrollView;
	private ActivityState activityState;
}
