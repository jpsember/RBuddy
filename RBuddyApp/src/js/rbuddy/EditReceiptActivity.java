package js.rbuddy;

import static js.basic.Tools.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import js.basic.Files;
import android.view.ViewGroup.LayoutParams;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Button;

public class EditReceiptActivity extends Activity {

	// Identifiers for the intents that we may spawn
	private static final int REQUEST_IMAGE_CAPTURE = 1;

	private void layoutElements() {

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		{
			Button btn = new Button(this);
			btn.setText("Teri");
			LayoutParams layoutParam = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layout.addView(btn, layoutParam);
		}

		{
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
				bitmapView.setImageDrawable(getResources().getDrawable(
						R.drawable.missingphoto));
				// Give photo a fixed size that is small, but lots of weight to
				// grow to take up what extra there is (horizontally)
				LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(10,
						LayoutParams.MATCH_PARENT, 1.0f);
				l2.addView(bitmapView, p);
				{
					Button btn = new Button(this);
					LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT, 0.0f);
					l2.addView(btn, p2);
					btn.setCompoundDrawablesWithIntrinsicBounds(getResources()
							.getDrawable(android.R.drawable.ic_menu_camera),
							null, null, null);

					btn.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							EditReceiptActivity a = (EditReceiptActivity) v
									.getContext();
							a.dispatchTakePictureIntent();
						}
					});
				}

			}
		}
		{
			Button btn = new Button(this);
			btn.setText("Hatcher");
			LayoutParams layoutParam = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layout.addView(btn, layoutParam);
		}

		{
			Button btn = new Button(this);
			btn.setText("Yum");
			LayoutParams layoutParam = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layout.addView(btn, layoutParam);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle s) {
		// The OS may be shutting down our activity to service some other
		// (possibly memory-intensive) task;
		// so save our state
		s.putString("pathOfTakenPhoto", pathOfTakenPhoto);
		super.onSaveInstanceState(s);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = RBuddyApp.sharedInstance();

		if (savedInstanceState != null) {
			// Restore our activity's previous state
			pathOfTakenPhoto = savedInstanceState.getString("pathOfTakenPhoto");
		}
		// preparePhotoFile();

		{
			Intent i = getIntent();
			int receiptId = i.getIntExtra(RBuddyApp.EXTRA_RECEIPT_ID, 0);
			if (receiptId != 0) {
				unimp("have data structure for receipt list, that can return receipt by id");
				this.receipt = (Receipt) app.receiptList().get(0);
			} else {
				this.receipt = new Receipt();
			}
		}
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

	@SuppressLint("SimpleDateFormat")
	private void processPhotoResult(Intent intent) {
		File mainFile = null;
		{
			unimp("handle various problem situations in ways other than just 'die'");
			// final boolean db = true;
			if (db)
				pr("processPhotoResult intent=" + intent);

			if (intent != null) {
				warning("did not expect intent to be non-null: " + intent);
			}

			File workFile = new File(pathOfTakenPhoto);
			if (!workFile.isFile()) {
				die("no work file found: " + pathOfTakenPhoto);
			}

			File scaledFile = null;
			try {
				scaledFile = ImageUtilities
						.scalePhoto(workFile, 800, 800, true);
			} catch (IOException e1) {
				die(e1);
			}

			// Create a new photo to store this work file
			// Create an image file name
			String photoIdentifier = new SimpleDateFormat("yyyyMMdd_HHmmss")
					.format(new Date());
			Photo photo = new Photo(photoIdentifier);
			mainFile = app.getPhotoFile().getMainFileFor(photo);
			if (mainFile.exists())
				die("main file already exists:" + mainFile);

			try {
				Files.copy(scaledFile, mainFile);
			} catch (IOException e) {
				die(e);
			}

			unimp("construct thumbnail");
			if (db)
				pr("created main file " + mainFile);
		}
		if (mainFile != null) {
			replaceReceiptPhoto(receipt,mainFile);
		}
	}

	private void replaceReceiptPhoto(Receipt receipt, File photoFile) {
		// If there's an existing photo id for this receipt, use it; otherwise, allocate a new one
		
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

		File workFile = ImageUtilities
				.constructExternalImageFile("RBuddy_work");

		// save work file in instance field, so we can refer to it later
		pathOfTakenPhoto = workFile.getPath();

		Uri uri = Uri.fromFile(workFile);
		if (db)
			pr("Uri.fromFile(workFile)=" + uri);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	}

	// private void preparePhotoFile() {
	// if (photoFile != null)
	// return;
	//
	// // final boolean db = true;
	// if (db)
	// pr("preparePhotoFile; " + stackTrace(1, 1));
	//
	// File d = new File(getExternalFilesDir(null), "photos");
	// if (!d.exists()) {
	// d.mkdir();
	// }
	// if (!d.isDirectory())
	// die("failed to create directory " + d);
	// photoFile = new PhotoFile(d);
	// if (db)
	// pr("preparePhotoFile, created " + photoFile + ";\ncontents=\n"
	// + photoFile.contents());
	// }

	// private PhotoFile photoFile;
	private String pathOfTakenPhoto;
	private RBuddyApp app;
	private Receipt receipt;
}
