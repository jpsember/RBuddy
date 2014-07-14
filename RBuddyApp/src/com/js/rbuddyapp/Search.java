package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import com.js.android.MyFragment;
import com.js.form.Form;
import com.js.rbuddy.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;

public class Search extends MyFragment implements IRBuddyActivityListener {

	public Search() {
		mApp = RBuddyApp.sharedInstance();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		String jsonString = readTextFileResource(getActivity(),
				R.raw.form_search);
		this.mForm = mApp.parseForm(getActivity(), jsonString);
		mForm.getField("search").setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getRBuddyActivity().performSearch();
			}
		});
		mScrollView = new ScrollView(getActivity());
		mScrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		mScrollView.addView(mForm.getView());
		getActivityState()//
				.add(mScrollView) //
				.restoreViewsFromSnapshot();
		return mScrollView;
	}

	@Override
	public void onResume() {
		super.onResume();
		getRBuddyActivity().addListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getRBuddyActivity().removeListener(this);
	}

	@Override
	public void onDestroyView() {
		mScrollView = null;
		mForm = null;
		super.onDestroyView();
	}

	private IRBuddyActivity getRBuddyActivity() {
		return (IRBuddyActivity) getActivity();
	}

	// IRBuddyActivityListener
	@Override
	public void activeReceiptChanged() {
	}

	@Override
	public void activeReceiptEdited() {
	}

	@Override
	public void receiptFileChanged() {
	}

	private RBuddyApp mApp;
	private Form mForm;
	private ScrollView mScrollView;

}
