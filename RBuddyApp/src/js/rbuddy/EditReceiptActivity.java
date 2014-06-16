package js.rbuddy;

import static js.basic.Tools.*;
import js.form.Form;
import js.json.*;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.view.View.OnClickListener;

public class EditReceiptActivity extends Activity {

	@Override
	public void onResume() {
		super.onResume();
		readWidgetValuesFromReceipt();
	}

	@Override
	public void onPause() {
		super.onPause();
		updateReceiptWithWidgetValues();
		app.receiptFile().flush();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = RBuddyApp.sharedInstance();

		if (db) {
			app.dumpIntent(this);
		}
		int receiptId = this.getIntent().getIntExtra(RBuddyApp.EXTRA_RECEIPT_ID, 0);
		ASSERT(receiptId > 0);
		this.receipt = app.receiptFile().getReceipt(receiptId);

		layoutElements();
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
		case android.R.id.home:
			Intent intent = new Intent(this, ReceiptListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			warning("does putting receipt id here help?");
			intent.putExtra(RBuddyApp.EXTRA_RECEIPT_ID, receipt.getId());
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void layoutElements() {
		String jsonString = RBuddyApp.sharedInstance().readTextFileResource(
				R.raw.form_edit_receipt);
		this.form = Form.parse(this, jsonString);
		this.form.getField("receipt").setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				processPhotoButtonPress();
			}
		});

		ScrollView scrollView = new ScrollView(this);
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		scrollView.addView(form.getView());

		setContentView(scrollView);
	}

	private void processPhotoButtonPress() {
		// Start the edit receipt activity
		Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);
		intent.putExtra(RBuddyApp.EXTRA_RECEIPT_ID, receipt.getId());
		startActivity(intent);
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

		String newTagSetString = form.getValue("tags");
		receipt.setTags(TagSet.parse(form.getValue("tags"), new TagSet()));

		String newJSON = JSONEncoder.toJSON(receipt);
		if (db)
			pr("comparing old and new JSON:\n --> " + origJSON + "\n --> "
					+ newJSON);

		if (!origJSON.equals(newJSON)) {
			if (db)
				pr(" changed, marking receipt as modified");
			app.receiptFile().setModified(receipt);
			if (!newTagSetString.equals(receipt.getTags().toString())) {
				if (db)
					pr("  moving tags to front of queue");
				receipt.getTags().moveTagsToFrontOfQueue(app.tagSetFile());
			}
		}
	}

	private RBuddyApp app;
	private Receipt receipt;
	private Form form;
}
