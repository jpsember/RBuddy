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
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;

public class EditReceiptFragment extends MyFragment {

	// public static final String TAG = "EditReceipt";
	public static final int MESSAGE_SET_RECEIPT = 1;

	public static final String TAG = "EditReceipt";
	public static Factory FACTORY = new Factory() {

		@Override
		public String tag() {
			return TAG;
		}

		@Override
		public MyFragment construct() {
			return new EditReceiptFragment();
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// final boolean db = true;
		if (db)
			pr(hey());
		app = RBuddyApp.sharedInstance();
		layoutElements();
		activityState = new ActivityState() //
				.add(scrollView) //
				.restoreStateFrom(savedInstanceState);
		if (db)
			pr(" returning scrollView " + scrollView);
		return scrollView;
	}

	@Override
	public void onResume() {
		if (db)
			pr(hey());
		super.onResume();
		readWidgetValuesFromReceipt();
	}

	@Override
	public void onPause() {
		// final boolean db = true;
		if (db)
			pr(hey());
		super.onPause();
		updateReceiptWithWidgetValues();
		app.receiptFile().flush();
		// Make widget display nothing, so it stops listening; otherwise
		// the widget will leak
		receiptWidget.displayPhoto(0, null);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		activityState.saveState(outState);
	}

	protected void processMessage(Message m) {
		switch (m.what) {
		case MESSAGE_SET_RECEIPT:
			setReceipt((Receipt) m.obj);
			break;
		default:
			warning("unhandled message " + m);
		}
	}

	public void setReceipt(Receipt receipt) {
		final boolean db = true;
		if (db)
			pr(hey() + "receipt=" + receipt);
		updateReceiptWithWidgetValues();
		this.receipt = receipt;
		readWidgetValuesFromReceipt();
	}

	private void layoutElements() {
		String jsonString = readTextFileResource(getActivity(),
				R.raw.form_edit_receipt);
		this.form = Form.parse(getActivity(), jsonString);
		receiptWidget = (FormButtonWidget) form.getField("receipt");
		receiptWidget.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				processPhotoButtonPress();
			}
		});

		scrollView = new ScrollView(getActivity());
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		scrollView.addView(form.getView());
	}

	private void processPhotoButtonPress() {
		startActivity(PhotoActivity.getStartIntent(getActivity(),
				receipt.getId()));
	}

	private void readWidgetValuesFromReceipt() {
		if (form == null || receipt == null)
			return;
		form.setValue("summary", receipt.getSummary());
		form.setValue("cost", receipt.getCost());
		form.setValue("date", receipt.getDate());
		form.setValue("tags", receipt.getTags());
		receiptWidget.displayPhoto(receipt.getId(), receipt.getPhotoId());
	}

	private void updateReceiptWithWidgetValues() {
		if (form == null || receipt == null)
			return;

		// To detect if changes have actually occurred, compare JSON
		// representations of the receipt before and after updating the fields.
		String origJSON = JSONEncoder.toJSON(receipt);

		receipt.setSummary(form.getValue("summary"));
		receipt.setCost(new Cost(form.getValue("cost"), true));
		receipt.setDate(JSDate.parse(form.getValue("date"), true));

		String origTagSetString = JSONEncoder.toJSON(receipt.getTags());

		receipt.setTags(TagSet.parse(form.getValue("tags"), new TagSet()));

		String newJSON = JSONEncoder.toJSON(receipt);
		if (db)
			pr("comparing old and new JSON:\n --> " + origJSON + "\n --> "
					+ newJSON);

		if (!origJSON.equals(newJSON)) {
			if (db)
				pr(" changed, marking receipt as modified");
			app.receiptFile().setModified(receipt);

			String newTagSetString = JSONEncoder.toJSON(receipt.getTags());
			if (db)
				pr(" orig tags: " + origTagSetString + "\n  new tags: "
						+ newTagSetString);

			if (!origTagSetString.equals(newTagSetString)) {
				if (db)
					pr("  moving tags to front of queue");
				receipt.getTags().moveTagsToFrontOfQueue(app.tagSetFile());
			}
		}
	}

	private RBuddyApp app;
	private Receipt receipt;
	private Form form;
	private FormButtonWidget receiptWidget;
	private ScrollView scrollView;
	private ActivityState activityState;
}
