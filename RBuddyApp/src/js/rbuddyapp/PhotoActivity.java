package js.rbuddyapp;

import static js.basic.Tools.*;

import java.io.File;
import java.io.IOException;

import js.basic.Files;
import js.form.Form;
import js.form.FormImageWidget;
import js.rbuddy.R;
import js.rbuddy.Receipt;
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
		final boolean db = true;
		if (db) pr(hey()+" telling imageWidget to display photo "+receipt.getPhotoId());
		imageWidget.displayPhoto(this.receipt.getPhotoId());
	}

	@Override
	public void onPause() {
		super.onPause();
		final boolean db = true;
		if (db) pr(hey()+" telling imageWidget to display null");
	imageWidget.displayPhoto(null);
		app.receiptFile().flush();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		final boolean db = true;
		if (db) pr(hey()+" onCreate");
	super.onCreate(savedInstanceState);
		app = RBuddyApp.sharedInstance();
		int receiptId = getIntent().getIntExtra(RBuddyApp.EXTRA_RECEIPT_ID, 0);
		ASSERT(receiptId > 0);
		this.receipt = app.receiptFile().getReceipt(receiptId);
		layoutElements();

		// If no photo is defined for this receipt, jump right into the take
		// photo intent
		if (receipt.getPhotoId() == null) {
			if (db) pr("  jumping right into image capture intent");
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
		final boolean db = true;
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			if (db) pr(hey()+" processing photo result");
		
			processPhotoResult(data);
			// Now that user has presumably selected a photo, exit out to the
			// EditReceipt activity.
			if (db) pr(" calling finish()\n");
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
		final boolean db = true;
		if (db)
			pr(hey());
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (intent.resolveActivity(getPackageManager()) == null) {
			return;
		}

		File workFile = getWorkPhotoFile();
		// Issue #46: is deletion required? If not, omit
		if (db)
			pr(" workFile=" + workFile + ", deleting");
		workFile.delete();

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
		final boolean db = true;
		if (db)
			pr("\n\n\n" + hey());
		unimp("handle various problem situations in ways other than just 'die'");

		File workFile = getWorkPhotoFile();
		if (db)
			pr(" workFile=" + workFile + ", isFile=" + workFile.isFile());
		if (!workFile.isFile()) {
			if (db) pr(" no work file found! "+workFile);
			die("no work file found: " + workFile);
		}

		BitmapUtil.orientAndScaleBitmap(workFile, 800, true);

		try {
			FileArguments args = new FileArguments(
					BitmapUtil.constructReceiptImageFilename(receipt.getId()));

			args.setData(Files.readBinaryFile(workFile));
			args.setFileId(receipt.getPhotoId());

			final FileArguments arg = args;

			// We have to wait until the photo has been processed, and a photoId
			// assigned; then store this assignment in the receipt, and update
			// the
			// photo's widget
			args.setCallback(new Runnable() {
				@Override
				public void run() {
					if (db) pr("storePhoto.callback, setting photo id to "+arg.getFileIdString());
					receipt.setPhotoId(arg.getFileIdString());
					if (db) pr(" displaying photo");
					imageWidget.displayPhoto(receipt.getPhotoId());
				}
			});
			IPhotoStore ps = app.photoStore();
			ps.storePhoto(args);
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
