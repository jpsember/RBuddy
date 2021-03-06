package com.js.rbuddyapp;

import static com.js.android.Tools.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;

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
public class ReceiptEditor extends MyFragment implements
		IRBuddyActivityListener {

	public ReceiptEditor() {
		// final boolean db = true;
		if (db)
			setLogging(true);
		log("constructing");
		ASSERT(!isAdded());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		log("onCreateView");

		constructViews();
		defineState(mScrollView);
		restoreStateFrom(savedInstanceState);
		return mScrollViewContainer;
	}

	private void constructViews() {
		mScrollView = new ScrollView(getActivity());
		mScrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		String message = nameOf(this);
		mScrollViewContainer = wrapView(mScrollView, message);
		constructForm();
	}

	@Override
	public void onResume() {
		super.onResume();
		getRBuddyActivity().addListener(this);
		setReceipt(getRBuddyActivity().getActiveReceipt());
	}

	@Override
	public void onPause() {
		super.onPause();
		writeReceiptFromWidgets();
		// TODO: this is not necessarily a good place to do this
		getRBuddyActivity().receiptFile().flush();
		getRBuddyActivity().removeListener(this);
	}

	@Override
	public void onDestroyView() {
		disposeForm();
		mScrollView = null;
		mScrollViewContainer = null;
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void setReceipt(Receipt receipt) {
		// In case there's an existing receipt, flush its changes
		writeReceiptFromWidgets();
		disposeForm();
		this.mReceipt = receipt;
		constructForm();
	}

	private void disposeForm() {
		if (mForm != null) {
			stopFormListeners();
			mScrollView.removeView(mForm.getView());
			mForm = null;
		}
	}

	private void stopFormListeners() {
		if (mReceiptWidget == null)
			return;
		// Make widget display nothing, so it stops listening; otherwise
		// the widget will leak
		mReceiptWidget.displayPhoto(getRBuddyActivity().photoStore(), 0, null);
	}

	/**
	 * If receipt exists, construct form if none already exists; otherwise,
	 * dispose of any existing form
	 */
	private void constructForm() {
		if (mReceipt == null) {
			disposeForm();
			return;
		}

		// If we already have a form, done
		if (mForm != null)
			return;

		String jsonString = readTextFileResource(getActivity(),
				R.raw.form_edit_receipt);

		mForm = getRBuddyActivity().parseForm(jsonString);

		mForm.addListener(new Form.Listener() {
			@Override
			public void valuesChanged(Form form) {
				writeReceiptFromWidgets();
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

		readReceiptToWidgets();
		if (db)
			pr(" read cost " + mForm.getValue("cost"));
	}

	private void readReceiptToWidgets() {
		mForm.setValue("summary", mReceipt.getSummary(), false);
		mForm.setValue("cost", mReceipt.getCost(), false);
		mForm.setValue("date", mReceipt.getDate(), false);
		mForm.setValue("tags", mReceipt.getTags(), false);
		mReceiptWidget.displayPhoto(getRBuddyActivity().photoStore(),
				mReceipt.getId(),
				mReceipt.getPhotoId());
	}

	private void processPhotoButtonPress() {
		if (mReceipt == null)
			return;
		getRBuddyActivity().editActiveReceiptPhoto();
	}

	/**
	 * If form exists, write its values to the receipt
	 */
	private void writeReceiptFromWidgets() {
		if (mForm == null)
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
			getRBuddyActivity().receiptFile().setModified(mReceipt);

			String newTagSetString = JSONEncoder.toJSON(mReceipt.getTags());
			if (!origTagSetString.equals(newTagSetString)) {
				mReceipt.getTags().moveTagsToFrontOfQueue(
						getRBuddyActivity().tagSetFile());
			}

			getRBuddyActivity().activeReceiptEdited();
		}
	}

	private IRBuddyActivity getRBuddyActivity() {
		return (IRBuddyActivity) getActivity();
	}

	// IRBuddyActivityListener
	@Override
	public void activeReceiptChanged() {
		setReceipt(getRBuddyActivity().getActiveReceipt());
	}

	@Override
	public void activeReceiptEdited() {
		// Ignore these events, since we generated them
	}

	@Override
	public void receiptFileChanged() {
	}

	private Receipt mReceipt;
	private Form mForm;
	private FormButtonWidget mReceiptWidget;
	private ScrollView mScrollView;
	private View mScrollViewContainer;

}
