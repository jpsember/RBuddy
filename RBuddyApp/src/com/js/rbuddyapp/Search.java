package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import com.js.android.FragmentOrganizer;
import com.js.android.MyFragment;
import com.js.android.PseudoFragment;
import com.js.form.Form;
import com.js.rbuddy.R;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;

public class Search extends PseudoFragment {

	public static class Wrapper extends MyFragment {
		public Wrapper() {
		}

		@Override
		public Class getFragmentClass() {
			return Search.class;
		}
	}

	public Search(FragmentOrganizer fragments) {
		super(fragments);
		new Wrapper().register(fragments);
		mApp = RBuddyApp.sharedInstance();
	}
	@Override
	public View onCreateView() {

		String jsonString = readTextFileResource(getContext(),
				R.raw.form_search);
		this.mForm = mApp.parseForm(getContext(), jsonString);
		mForm.getField("search").setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				performSearch();
			}
		});
		mScrollView = new ScrollView(getContext());
		mScrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		mScrollView.addView(mForm.getView());
		getActivityState()//
				.add(mScrollView) //
				.restoreViewsFromSnapshot();
		return mScrollView;
	}

	@Override
	public void onDestroyView() {
		mScrollView = null;
		mForm = null;
		super.onDestroyView();
	}

	// Methods the Search pseudofragment provides

	private void performSearch() {
		toast(getContext(), "Search isn't yet implemented.");
	}

	/* private */Listener listener() {
		return (Listener) getContext();
	}

	public static interface Listener {
		// no methods yet
	}

	private RBuddyApp mApp;
	private Form mForm;
	private ScrollView mScrollView;
}
