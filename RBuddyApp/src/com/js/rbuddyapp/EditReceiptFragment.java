package com.js.rbuddyapp;

//import static com.js.android.Tools.*;
import com.js.android.FragmentOrganizer;
import com.js.android.MyFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EditReceiptFragment extends MyFragment {

	public EditReceiptFragment() {
		FragmentOrganizer f = RBuddyApp.sharedInstance().fragments();
		mEditor = (ReceiptEditor) f.getWrappedSingleton(ReceiptEditor.class);
	}

	public static final String TAG = "EditReceipt";
	public static Factory FACTORY = new Factory() {

		@Override
		public String name() {
			return TAG;
		}

		@Override
		public MyFragment construct() {
			return new EditReceiptFragment();
		}
	};

	/**
	 * Construct the singleton instance of this fragment, if it hasn't already
	 * been
	 * 
	 * @param organizer
	 * @return
	 */
	public static EditReceiptFragment construct(FragmentOrganizer organizer) {
		return (EditReceiptFragment) organizer.get(TAG, true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return mEditor.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		mEditor.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mEditor.onPause();
	}

	@Override
	public void onDestroyView() {
		mEditor.onDestroyView();
		super.onDestroyView();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mEditor.onSaveInstanceState(outState);
	}

	private ReceiptEditor mEditor;
}
