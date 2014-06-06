package js.rbuddy;

import static js.basic.Tools.*;

import java.io.File;
import java.io.IOException;

import js.basic.Files;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EditReceiptActivity extends Activity {

	// Identifiers for the intents that we may spawn
	private static final int REQUEST_IMAGE_CAPTURE = 1;

	@Override
	public void onPause() {
		final boolean db = true;
		if (db)
			pr("\nEditReceiptActivity.onPause");
		super.onPause(); // Always call the superclass method first
		updateReceiptWithWidgetValues();
		app.receiptFile().flush();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (db)
			pr("\n\nEditReceiptActivity onCreate\n");
		app = RBuddyApp.sharedInstance();

		{
			Intent i = getIntent();
			int receiptId = i.getIntExtra(RBuddyApp.EXTRA_RECEIPT_ID, 0);
			ASSERT(receiptId > 0);
			this.receipt = app.receiptFile().getReceipt(receiptId);
		}
		layoutElements();
	}

	@Override
	public void onResume() {
		final boolean db = true;
		if (db)
			pr("\n\nEditReceiptActivity.resume");
		super.onResume(); // Always call the superclass method first
		readWidgetValuesFromReceipt();
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// final boolean db = true;
		if (db)
			pr("onActivityResult\n requestCode=" + requestCode
					+ "\n resultCode=" + resultCode + "\n intent=" + data);

		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			processPhotoResult(data);
		}
	}

	private void layoutElements() {

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		addPhotoWidget(layout);
		addDateWidget(layout);
		addSummaryWidget(layout);
	}

	private void addPhotoWidget(ViewGroup layout) {
		unimp("if no photo exists, use default; currently nothing is showing up");

		// Nest the image view within a horizontal layout, to add a 'camera'
		// button to the bottom right
		LinearLayout l2 = new LinearLayout(this);
		l2.setOrientation(LinearLayout.HORIZONTAL);
		{
			// Give this layout a fixed size that is small, but lots of
			// weight to grow to take up what extra there is.
			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 10, 1.0f);
			layout.addView(l2, p);
		}

		{
			ImageView bitmapView = new ImageView(this);
			this.photoView = bitmapView;

			// Give photo a fixed size that is small, but lots of weight to
			// grow to take up what extra there is (horizontally)
			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(10,
					LayoutParams.MATCH_PARENT, 1.0f);
			l2.addView(bitmapView, p);
			{
				Button btn = new Button(this);
				LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
						0.0f);
				l2.addView(btn, p2);
				btn.setCompoundDrawablesWithIntrinsicBounds(getResources()
						.getDrawable(android.R.drawable.ic_menu_camera), null,
						null, null);

				btn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						EditReceiptActivity a = (EditReceiptActivity) v
								.getContext();
						a.dispatchTakePictureIntent();
					}
				});
			}
			updatePhotoView();

		}
	}

	// private static final Calendar myCalendar = Calendar.getInstance();

	/**
	 * Parse JSDate from date widget, if possible
	 * 
	 * @return JSDate, or null if parsing failed
	 */
	private JSDate readDateFromDateWidget() {
		String content = dateView.getText().toString();
		JSDate ret = receipt.getDate();
		try {
			ret = JSDate.parse(content);
		} catch (IllegalArgumentException e) {
			warning("failed to parse " + content);
		}
		return ret;
	}

	private void addDateWidget(ViewGroup layout) {
		EditText tf = new EditText(this);
		dateView = tf;
		dateView.setText(receipt.getDate().toString());

		tf.setFocusable(false);
		tf.setMinHeight(50);
		LayoutParams layoutParam = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		layout.addView(tf, layoutParam);

		tf.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						JSDate date = JSDate.buildFromValues(year, monthOfYear,
								dayOfMonth);
						dateView.setText(date.toString());
						// updateReceiptWithWidgetValues();
					}
				};
				JSDate d = receipt.getDate();
				// final boolean db = true;
				if (db)
					pr("initializing DatePickerDialog with date from receipt: "
							+ d);
				new DatePickerDialog(EditReceiptActivity.this, dateListener, d
						.year(), d.month(), d.day()).show();
			}
		});
	}

	private void addSummaryWidget(ViewGroup layout) {
		AutoCompleteTextView tf = new AutoCompleteTextView(this);
		tf.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
				| InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
		summaryView = tf;
		tf.setHint("Summary");
		tf.setMinHeight(50);
		LayoutParams layoutParam = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		layout.addView(tf, layoutParam);
	}

	private void dispatchTakePictureIntent() {
		// final boolean db = true;
		if (db)
			pr("dispatching an intent to take a picture\n");

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		if (intent.resolveActivity(getPackageManager()) == null) {
			if (db)
				pr(" could not resolve activity");
			return;
		}

		File workFile = getWorkPhotoFile();
		workFile.delete();

		Uri uri = Uri.fromFile(workFile);
		if (db)
			pr("Uri.fromFile(workFile)=" + uri);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	}

	private File getWorkPhotoFile() {
		return ImageUtilities.constructExternalImageFile("RBuddy_work");
	}

	private void processPhotoResult(Intent intent) {
		// final boolean db = true;
		if (db)
			pr("\n\nprocessPhotoResult intent " + intent);

		File mainFile = null;
		{
			unimp("handle various problem situations in ways other than just 'die'");
			// final boolean db = true;
			if (db)
				pr("processPhotoResult intent=" + intent);

			if (intent != null) {
				warning("did not expect intent to be non-null: " + intent);
			}

			File workFile = getWorkPhotoFile();
			if (db)
				pr(" pathOfTakenPhoto " + workFile);
			if (!workFile.isFile()) {
				die("no work file found: " + workFile);
			}

			ImageUtilities.orientAndScaleBitmap(workFile, 800, true);
			int photoIdentifier = receipt.getUniqueIdentifier();
			if (photoIdentifier == 0) {
				photoIdentifier = app.getUniqueIdentifier();
				receipt.setUniqueIdentifier(photoIdentifier);
				// Note: we don't mark it as modified here; we'll assume this
				// occurs when activity pauses
			}

			mainFile = app.getPhotoFile().getMainFileFor(photoIdentifier);
			if (db)
				pr("photoIdentifier " + photoIdentifier
						+ "  copying scaled/rotated file " + workFile
						+ " to mainFile " + mainFile);

			try {
				Files.copy(workFile, mainFile);
			} catch (IOException e) {
				die(e);
			}

			unimp("construct thumbnail");
			if (db)
				pr("created main file " + mainFile);

			if (db)
				pr("updating photo view");
			updatePhotoView();
		}
	}

	private void updatePhotoView() {
		// final boolean db = true;
		if (db)
			pr("updatePhotoView " + photoView);

		if (photoView == null)
			return;
		int requestedPhotoId = 0;
		if (receipt != null)
			requestedPhotoId = receipt.getUniqueIdentifier();
		if (db)
			pr(" receipt " + receipt + "  requested id " + requestedPhotoId);
		if (requestedPhotoId == 0) {
			photoView.setImageDrawable(getResources().getDrawable(
					R.drawable.missingphoto));
		} else {
			File imageFile = app.getPhotoFile()
					.getMainFileFor(requestedPhotoId);
			if (db)
				pr(" reading bitmap from file " + imageFile);
			Bitmap bmp = ImageUtilities.readImage(imageFile);

			photoView.setImageDrawable(new BitmapDrawable(this.getResources(),
					bmp));
		}
	}

	private void readWidgetValuesFromReceipt() {
		unimp("save and restore cursor position as well as text?");
		summaryView.setText(receipt.getSummary());
		dateView.setText(receipt.getDate().toString());
	}

	private void updateReceiptWithWidgetValues() {
		if (db)
			pr("\n\nupdateReceiptWithWidgetValues");
		receipt.setDate(readDateFromDateWidget());
		receipt.setSummary(summaryView.getText().toString());
		if (db)
			pr(" receipt now: " + receipt);
		app.receiptFile().setModified(receipt);
	}

	private RBuddyApp app;
	private Receipt receipt;
	private ImageView photoView;
	private TextView summaryView;
	private TextView dateView;
}
