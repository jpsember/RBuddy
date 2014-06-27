package com.js.rbuddyapp;

import static com.js.basic.Tools.*;

import java.io.File;
import java.io.IOException;

import com.js.form.Form;
import com.js.form.FormImageWidget;

import com.js.basic.Files;
import com.js.rbuddy.R;
import com.js.rbuddy.Receipt;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.view.View.OnClickListener;

public class PhotoActivity extends Activity {

	// Identifiers for the intents that we may spawn
	private static final int REQUEST_IMAGE_CAPTURE = 1;

	@Override
	protected void onResume() {
		super.onResume();
		imageWidget.displayPhoto(receipt.getId(), receipt.getPhotoId());
	}

	@Override
	public void onPause() {
		super.onPause();
		unimp("we should probably pass a receipt here instead");
		imageWidget.displayPhoto(0, null);
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

		// If no photo is defined for this receipt, jump right into the take
		// photo intent
		if (receipt.getPhotoId() == null) {
			startImageCaptureIntent();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		getMenuInflater().inflate(R.menu.photo_activity_actions, menu);
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
			Intent intent = new Intent(this, EditReceiptActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(RBuddyApp.EXTRA_RECEIPT_ID, receipt.getId());
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			processPhotoResult(data);
			// Now that user has presumably selected a photo, exit out to the
			// EditReceipt activity.
			finish();
		}
	}

	private void layoutElements() {
		String jsonString = app.readTextFileResource(R.raw.form_photo_activity);

		this.form = Form.parse(this, jsonString);
		form.getField("takephoto").setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startImageCaptureIntent();
			}
		});
		imageWidget = (FormImageWidget) form.getField("photo");

		ScrollView scrollView = new ScrollView(this);
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		scrollView.addView(form.getView());

		setContentView(scrollView);
	}

	private void startImageCaptureIntent() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (intent.resolveActivity(getPackageManager()) == null) {
			return;
		}

		File workFile = getWorkPhotoFile();
		Uri uri = Uri.fromFile(workFile);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		if (db)
			pr(" starting activity REQUEST_IMAGE_CAPTURE");
		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	}

	private File getWorkPhotoFile() {
		return BitmapUtil.constructExternalImageFile("RBuddy_work");
	}

	private void processPhotoResult(Intent intent) {
		unimp("handle various problem situations in ways other than just 'die'");

		File workFile = getWorkPhotoFile();
		if (!workFile.isFile()) {
			die("no work file found: " + workFile);
		}

		BitmapUtil.orientAndScaleBitmap(workFile, IPhotoStore.FULLSIZE_HEIGHT,
				true);

		try {
			FileArguments args = new FileArguments(
					BitmapUtil.constructReceiptImageFilename(receipt.getId()));

			args.setData(Files.readBinaryFile(workFile));
			args.setFileId(receipt.getPhotoId());

			final FileArguments arg = args;

			// We have to wait until the photo has been processed, and a photoId
			// assigned; then store this assignment in the receipt, and update
			// the photo's widget
			args.setCallback(new Runnable() {
				public void run() {
					receipt.setPhotoId(arg.getFileIdString());
					imageWidget.displayPhoto(receipt.getId(),
							receipt.getPhotoId());
				}
			});
			IPhotoStore ps = app.photoStore();
			ps.storePhoto(receipt.getId(), args);
		} catch (IOException e) {
			// TODO display popup message to user, and don't update receipt's
			// photo id
			die(e);
		}
	}

	private Receipt receipt;
	private RBuddyApp app;
	private Form form;
	private FormImageWidget imageWidget;
}
