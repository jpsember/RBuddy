package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import com.js.android.ActivityState;
import com.js.form.Form;
import com.js.form.FormButtonWidget;
import com.js.json.JSONEncoder;
import com.js.rbuddy.Cost;
import com.js.rbuddy.JSDate;
import com.js.rbuddy.R;
import com.js.rbuddy.Receipt;
import com.js.rbuddy.TagSet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;

public class EditReceiptFragment extends MyFragment {

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
		readWidgetValuesFromReceipt();
	}

	@Override
	public void onPause() {
		super.onPause();
		updateReceiptWithWidgetValues();
		mApp.receiptFile().flush();
		// Make widget display nothing, so it stops listening; otherwise
		// the widget will leak
		mReceiptWidget.displayPhoto(mApp.photoStore(), 0, null);
	}

	public void setReceipt(Receipt receipt) {
		// In case there's an existing receipt, flush its changes
		updateReceiptWithWidgetValues();
		this.mReceipt = receipt;
		readWidgetValuesFromReceipt();
	}

	private void layoutElements() {
		String jsonString = readTextFileResource(getActivity(),
				R.raw.form_edit_receipt);
		this.mForm = mApp.parseForm(getActivity(), jsonString);
		mForm.addListener(new Form.Listener() {
			@Override
			public void valuesChanged(Form form) {
				updateReceiptWithWidgetValues();
			}
		});

		mReceiptWidget = (FormButtonWidget) mForm.getField("receipt");
		mReceiptWidget.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				processPhotoButtonPress();
			}
		});

		mScrollView = new ScrollView(getActivity());
		mScrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		mScrollView.addView(mForm.getView());
	}

	private void processPhotoButtonPress() {
		if (mReceipt == null)
			return;
		listener().editPhoto(mReceipt);
	}

	private void readWidgetValuesFromReceipt() {
		if (mForm == null || mReceipt == null)
			return;
		mForm.setValue("summary", mReceipt.getSummary(), false);
		mForm.setValue("cost", mReceipt.getCost(), false);
		mForm.setValue("date", mReceipt.getDate(), false);
		mForm.setValue("tags", mReceipt.getTags(), false);
		final boolean db = true;
		if (db)
			pr(hey() + " rid " + mReceipt.getId() + " photoId "
					+ mReceipt.getPhotoId());
		mReceiptWidget.displayPhoto(mApp.photoStore(), mReceipt.getId(),
				mReceipt.getPhotoId());
	}

	private void updateReceiptWithWidgetValues() {
		if (mForm == null || mReceipt == null)
			return;

		// To detect if changes have actually occurred, compare JSON
		// representations of the receipt before and after updating the fields.
		String origJSON = JSONEncoder.toJSON(mReceipt);

		mReceipt.setSummary(mForm.getValue("summary"));
		mReceipt.setCost(new Cost(mForm.getValue("cost"), true));
		mReceipt.setDate(JSDate.parse(mForm.getValue("date"), true));

		String origTagSetString = JSONEncoder.toJSON(mReceipt.getTags());

		mReceipt.setTags(TagSet.parse(mForm.getValue("tags"), new TagSet()));

		String newJSON = JSONEncoder.toJSON(mReceipt);

		if (!origJSON.equals(newJSON)) {
			mApp.receiptFile().setModified(mReceipt);

			String newTagSetString = JSONEncoder.toJSON(mReceipt.getTags());
			if (!origTagSetString.equals(newTagSetString)) {
				mReceipt.getTags().moveTagsToFrontOfQueue(mApp.tagSetFile());
			}

			listener().receiptEdited(mReceipt);
		}
	}

	/**
	 * Get listener by casting parent activity
	 * 
	 * @return
	 */
	private Listener listener() {
		return (Listener) getActivity();
	}

	public static interface Listener {
		void receiptEdited(Receipt r);

		void editPhoto(Receipt r);
	}

	private RBuddyApp mApp;
	private Receipt mReceipt;
	private Form mForm;
	private FormButtonWidget mReceiptWidget;
	private ScrollView mScrollView;

}
