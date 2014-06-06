package js.rbuddy;

import static js.basic.Tools.*;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import js.basic.Files;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.method.TextKeyListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.view.KeyEvent;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

public class EditReceiptActivity extends Activity {

	// Identifiers for the intents that we may spawn
	private static final int REQUEST_IMAGE_CAPTURE = 1;

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

		int receiptId = getIntent().getIntExtra(RBuddyApp.EXTRA_RECEIPT_ID, 0);
		ASSERT(receiptId > 0);
		this.receipt = app.receiptFile().getReceipt(receiptId);

		layoutElements();
	}

	@Override
	public void onResume() {
		// final boolean db = true;
		if (db)
			pr("\n\nEditReceiptActivity.resume");
		super.onResume();
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

	/**
	 * Parse JSDate from date widget, if possible
	 * 
	 * @return JSDate, or null if parsing failed
	 */
	private JSDate readDateFromDateWidget() {
		String content = dateView.getText().toString();
		JSDate ret = receipt.getDate();
		try {
			ret = AndroidDate.parseJSDateFromUserString(content);
		} catch (ParseException e) {
			warning("problem parsing " + e);
		}
		return ret;
	}

	private void addDateWidget(ViewGroup layout) {

		EditText tf = new EditText(this);
		dateView = tf;

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
						dateView.setText(AndroidDate
								.formatUserDateFromJSDate(date));
					}
				};
				int[] ymd = AndroidDate
						.getJavaYearMonthDay(readDateFromDateWidget());
				new DatePickerDialog(EditReceiptActivity.this, dateListener,
						ymd[0], ymd[1], ymd[2]).show();
			}
		});
	}

	private void addSummaryWidget(ViewGroup layout) {
		MultiAutoCompleteTextView tf = new MultiAutoCompleteTextView(this);

		tf.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

		tf.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

		tf.setKeyListener(TextKeyListener.getInstance(true,
				TextKeyListener.Capitalize.NONE));

		// This makes pressing the 'done' keyboard key close the keyboard
		tf.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				return false;
			}
		});

		// May or may not be useful; let's see after adding other components
		// tf.clearFocus();

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
		return BitmapUtil.constructExternalImageFile("RBuddy_work");
	}

	private void processPhotoResult(Intent intent) {
		// final boolean db = true;
		if (db)
			pr("\n\nprocessPhotoResult intent " + intent);

		File mainFile = null;
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

		BitmapUtil.orientAndScaleBitmap(workFile, 800, true);

		mainFile = app.getPhotoFile().getMainFileFor(
				receipt.getUniqueIdentifier());
		if (db)
			pr("receipt id " + receipt.getUniqueIdentifier()
					+ "  copying scaled/rotated file " + workFile
					+ " to mainFile " + mainFile);

		try {
			Files.copy(workFile, mainFile);
		} catch (IOException e) {
			die(e);
		}

		if (db)
			pr("created main file " + mainFile);

		if (db)
			pr("updating photo view");
		updatePhotoView();

	}

	private void updatePhotoView() {
		if (photoView == null)
			return;
		PhotoFile pf = app.getPhotoFile();

		// If no image exists, display placeholder instead
		if (!pf.photoExists(receipt.getUniqueIdentifier())) {
			photoView.setImageDrawable(getResources().getDrawable(
					R.drawable.missingphoto));
		} else {
			File imageFile = pf.getMainFileFor(receipt.getUniqueIdentifier());
			Bitmap bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
			photoView.setImageDrawable(new BitmapDrawable(this.getResources(),
					bmp));
		}
	}

	private void readWidgetValuesFromReceipt() {
		summaryView.setText(receipt.getSummary());
		summaryView.setSelection(summaryView.getText().length());
		dateView.setText(AndroidDate.formatUserDateFromJSDate(receipt.getDate()));
	}

	private void updateReceiptWithWidgetValues() {
		receipt.setDate(readDateFromDateWidget());
		receipt.setSummary(summaryView.getText().toString());
		app.receiptFile().setModified(receipt);
	}

	private RBuddyApp app;
	private Receipt receipt;
	private ImageView photoView;
	private EditText summaryView;
	private TextView dateView;
}
