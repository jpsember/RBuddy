package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import com.js.android.ActivityState;
import com.js.form.Form;
import com.js.form.FormButtonWidget;
import com.js.json.*;
import com.js.rbuddy.Cost;
import com.js.rbuddy.JSDate;
import com.js.rbuddy.R;
import com.js.rbuddy.Receipt;
import com.js.rbuddy.TagSet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.view.View.OnClickListener;

public class EditReceiptActivity extends Activity {

	private static String EXTRA_RECEIPT_ID = "receipt_id";

	/**
	 * Construct intent for starting this activity
	 * 
	 * @param receiptId
	 *            id of receipt to be edited
	 * @return
	 */
	public static Intent getStartIntent(Context context, int receiptId) {
		return startIntentFor(context, EditReceiptActivity.class) //
				.putExtra(EXTRA_RECEIPT_ID, receiptId);
	}

	@Override
	public void onResume() {
		super.onResume();
		readWidgetValuesFromReceipt();
		receiptWidget.displayPhoto(this.receipt.getId(),
				this.receipt.getPhotoId());
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = RBuddyApp.sharedInstance(this);
		int receiptId = this.getIntent().getIntExtra(EXTRA_RECEIPT_ID, 0);
		this.receipt = app.receiptFile().getReceipt(receiptId);

		layoutElements();
		activityState = new ActivityState() //
				.add(scrollView) //
				.restoreStateFrom(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		activityState.saveState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		getMenuInflater().inflate(R.menu.editreceipt_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_settings:
			unimp("settings");
			return true;
		case R.id.action_delete_receipt:
			confirmOperation(this, "Delete Receipt?", new Runnable() {
				@Override
				public void run() {
					deleteReceipt();
				}
			});
			return true;
		case android.R.id.home:
			Intent intent = RBuddyActivity.getStartIntent(this).addFlags(
					Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void deleteReceipt() {
		String photoIdString = receipt.getPhotoId();
		if (photoIdString != null) {
			final FileArguments args = new FileArguments();
			args.setFileId(photoIdString);
			app.photoStore().deletePhoto(receipt.getId(), args);
		}
		app.receiptFile().delete(this.receipt);
		finish();
	}

	private void layoutElements() {
		String jsonString = readTextFileResource(this, R.raw.form_edit_receipt);
		this.form = Form.parse(this, jsonString);
		receiptWidget = (FormButtonWidget) form.getField("receipt");
		receiptWidget.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				processPhotoButtonPress();
			}
		});

		scrollView = new ScrollView(this);
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		scrollView.addView(form.getView());

		setContentView(scrollView);
	}

	private void processPhotoButtonPress() {
		startActivity(PhotoActivity.getStartIntent(this, receipt.getId()));
	}

	private void readWidgetValuesFromReceipt() {
		form.setValue("summary", receipt.getSummary());
		form.setValue("cost", receipt.getCost());
		form.setValue("date", receipt.getDate());
		form.setValue("tags", receipt.getTags());
	}

	private void updateReceiptWithWidgetValues() {

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
