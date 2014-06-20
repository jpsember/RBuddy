package js.rbuddyapp;

import static js.basic.Tools.*;

import java.io.File;
import java.io.IOException;

import js.basic.Files;
import js.form.Form;
import js.form.FormImageWidget;
import js.form.IDrawableProvider;
import js.rbuddy.R;
import js.rbuddy.Receipt;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.view.View.OnClickListener;

public class PhotoActivity extends Activity implements IDrawableProvider {

	// Identifiers for the intents that we may spawn
	private static final int REQUEST_IMAGE_CAPTURE = 1;

	@Override
	public void onPause() {
		super.onPause();
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
		if (receipt.getPhotoId() == null)
			startImageCaptureIntent();
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
		imageWidget.setDrawableProvider(this);

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
		workFile.delete();

		Uri uri = Uri.fromFile(workFile);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

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

		BitmapUtil.orientAndScaleBitmap(workFile, 800, true);

		try {
			FileArguments args = new FileArguments();
			args.setData(Files.readBinaryFile(workFile));
			args.setFilename(BitmapUtil.constructReceiptImageFilename(receipt
					.getId()));
			args.setFileId(receipt.getPhotoId());

			final FileArguments arg = args;
			final Receipt theReceipt = receipt;
			args.setCallback(new Runnable() {
				@Override
				public void run() {
					photoIdHasArrived(theReceipt, arg.getFileIdString(),
							arg.getData());
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

	private void photoIdHasArrived(Receipt receipt, String fileIdString,
			byte[] jpeg) {
		receipt.setPhotoId(fileIdString);
		app.receiptFile().setModified(receipt);
		processBitmapLoaded(jpeg);
	}

	@Override
	public Drawable getDrawable() {
		Drawable d = null;
		do {
			String photoId = receipt.getPhotoId();
			if (photoId == null)
				break;

			final FileArguments args = new FileArguments();
			args.setFileId(photoId);

			args.setCallback(new Runnable() {
				@Override
				public void run() {
					processBitmapLoaded(args.getData());
				}
			});
			app.photoStore().readPhoto(args);
		} while (false);
		return d;
	}

	private void processBitmapLoaded(byte[] jpeg) {
		Bitmap bmp = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
		Drawable d = new BitmapDrawable(this.getResources(), bmp);
		imageWidget.drawableArrived(d);
	}

	private Receipt receipt;
	private RBuddyApp app;
	private Form form;
	private FormImageWidget imageWidget;
}
