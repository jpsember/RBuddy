package com.js.rbuddyapp;

import static com.js.android.Tools.*;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;

import com.js.android.ActivityState;
import com.js.android.FragmentWrapper;
import com.js.android.MyFragment;
import com.js.form.Form;
import com.js.form.FormButtonWidget;
import com.js.json.JSONEncoder;
import com.js.rbuddy.Cost;
import com.js.rbuddy.JSDate;
import com.js.rbuddy.R;
import com.js.rbuddy.Receipt;
import com.js.rbuddy.TagSet;

/**
 * Singleton receipt editor
 * 
 */
public class ReceiptEditor extends MyFragment {

	public static class Wrapper extends FragmentWrapper {
		public Wrapper() {
		}

		@Override
		public Class getFragmentClass() {
			return ReceiptEditor.class;
		}
	}

	public ReceiptEditor() {
		// super(true); // enable to print log messages

		// Register the wrapper class
		new Wrapper();

		// Perform class-specific initialization
		mApp = RBuddyApp.sharedInstance();
	}

	@Override
	public void onRestoreInstanceState(Bundle bundle) {
		super.onRestoreInstanceState(bundle);
		if (bundle != null) {
			int receiptId = bundle.getInt("XXX", 0);
			Receipt r = null;
			if (receiptId != 0)
				r = mApp.receiptFile().getReceipt(receiptId);
			setReceipt(r);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		log("onCreateView, mReceipt=" + mReceipt);
		layoutElements();
		mActivityState = new ActivityState() //
				.add(mScrollView) //
				.restoreStateFrom(savedInstanceState);
		log(" returning scrollView " + mScrollView);
		return mScrollView;
	}

	@Override
	public void onResume() {
		super.onResume();
		constructFormIfNecessary();
		readWidgetValuesFromReceipt();
	}

	@Override
	public void onPause() {
		super.onPause();
		updateReceiptWithWidgetValues();
		mApp.receiptFile().flush();
	}

	@Override
	public void onDestroyView() {
		disposeForm();
		mScrollView = null;
		super.onDestroyView();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivityState != null)
			mActivityState.saveState(outState);
		outState.putInt("XXX", mReceipt == null ? 0 : mReceipt.getId());
	}

	public void setReceipt(Receipt receipt) {
		final boolean db = true;
		if (db)
			pr(hey() + "receipt=" + receipt);
		// In case there's an existing receipt, flush its changes
		updateReceiptWithWidgetValues();
		this.mReceipt = receipt;
		if (receipt == null)
			disposeForm();
		else
			constructFormIfNecessary();
		readWidgetValuesFromReceipt();
	}

	private void disposeForm() {
		if (mForm != null) {
			stopFormListeners();
			mForm = null;
		}
		if (mScrollView != null)
			mScrollView.removeAllViews();
	}

	private void stopFormListeners() {
		if (mReceiptWidget == null)
			return;
		// Make widget display nothing, so it stops listening; otherwise
		// the widget will leak
		mReceiptWidget.displayPhoto(mApp.photoStore(), 0, null);
	}

	/**
	 * Construct the form if it doesn't exist, receipt exists, and its container
	 * exists
	 */
	private void constructFormIfNecessary() {
		if (mReceipt == null)
			return;
		if (mForm != null)
			return;
		if (mScrollView == null)
			return;

		String jsonString = readTextFileResource(getContext(),
				R.raw.form_edit_receipt);

		mForm = mApp.parseForm(getContext(), jsonString);

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
		mScrollView.addView(mForm.getView(), new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}

	private void layoutElements() {
		ASSERT(mScrollView == null);
		mScrollView = new ScrollView(getContext());
		debugChangeBgndColor(mScrollView);
		mScrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		log("layoutElements, scrollView " + mScrollView);

		constructFormIfNecessary();
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
		mReceiptWidget.displayPhoto(mApp.photoStore(), mReceipt.getId(),
				mReceipt.getPhotoId());
	}

	private void updateReceiptWithWidgetValues() {
		if (mForm == null || mReceipt == null)
			return;

		// To detect if changes have actually occurred, compare JSON
		// representations of the receipt before and after updating the fields.
		String origJSON = JSONEncoder.toJSON(mReceipt);
		String origTagSetString = JSONEncoder.toJSON(mReceipt.getTags());

		mReceipt.setSummary(mForm.getValue("summary"));
		mReceipt.setCost(new Cost(mForm.getValue("cost"), true));
		mReceipt.setDate(JSDate.parse(mForm.getValue("date"), true));
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
		return (Listener) getContext();
	}

	public static interface Listener {
		void receiptEdited(Receipt r);

		void editPhoto(Receipt r);
	}

	private Context getContext() {
		return mApp.fragments().getActivity();
	}

	private RBuddyApp mApp;
	private Receipt mReceipt;
	private Form mForm;
	private FormButtonWidget mReceiptWidget;
	private ScrollView mScrollView;
	protected ActivityState mActivityState;
}
