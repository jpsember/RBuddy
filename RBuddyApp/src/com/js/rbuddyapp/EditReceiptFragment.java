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
		app = RBuddyApp.sharedInstance();
		layoutElements();
		mActivityState = new ActivityState() //
				.add(scrollView) //
				.restoreStateFrom(savedInstanceState);
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
		super.onPause();
		updateReceiptWithWidgetValues();
		app.receiptFile().flush();
		// Make widget display nothing, so it stops listening; otherwise
		// the widget will leak
		receiptWidget.displayPhoto(0, null);
	}

	public void setReceipt(Receipt receipt) {
		if (db)
			pr(hey() + "receipt=" + receipt);
		// In case there's an existing receipt, flush its changes
		updateReceiptWithWidgetValues();
		this.receipt = receipt;
		readWidgetValuesFromReceipt();
	}

	private void layoutElements() {
		String jsonString = readTextFileResource(getActivity(),
				R.raw.form_edit_receipt);
		this.form = Form.parse(getActivity(), jsonString);
		form.addListener(new Form.Listener() {
			@Override
			public void valuesChanged(Form form) {
				updateReceiptWithWidgetValues();
			}
		});

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
		if (receipt == null)
			return;
		listener().editPhoto(receipt);
	}

	private void readWidgetValuesFromReceipt() {
		if (form == null || receipt == null)
			return;
		form.setValue("summary", receipt.getSummary(), false);
		form.setValue("cost", receipt.getCost(), false);
		form.setValue("date", receipt.getDate(), false);
		form.setValue("tags", receipt.getTags(), false);
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

		if (!origJSON.equals(newJSON)) {
			app.receiptFile().setModified(receipt);

			String newTagSetString = JSONEncoder.toJSON(receipt.getTags());
			if (!origTagSetString.equals(newTagSetString)) {
				receipt.getTags().moveTagsToFrontOfQueue(app.tagSetFile());
			}

			listener().receiptEdited(receipt);
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

	private RBuddyApp app;
	private Receipt receipt;
	private Form form;
	private FormButtonWidget receiptWidget;
	private ScrollView scrollView;

}
