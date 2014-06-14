package js.rbuddy;

import static js.basic.Tools.*;

import java.io.File;
import java.io.IOException;

import js.basic.Files;
import js.form.Form;
import js.json.*;
import android.app.Activity;
import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.drawable.BitmapDrawable;
//import android.net.Uri;
import android.os.Bundle;
//import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//import android.view.View;
import android.view.ViewGroup.LayoutParams;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.View.OnClickListener;

public class EditReceiptActivity extends Activity {

	// Identifiers for the intents that we may spawn
	private static final int REQUEST_IMAGE_CAPTURE = 1;

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

		int receiptId = getIntent().getIntExtra(RBuddyApp.EXTRA_RECEIPT_ID, 0);
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
		String jsonString = null;
		try {
			jsonString = Files.readTextFile(getResources().openRawResource(
					R.raw.form_edit_receipt));
		} catch (IOException e) {
			die(e);
		}
		this.form = Form.parse(this, jsonString);
		this.form.getField("receipt").setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				processReceiptButtonPress();
			}
		});

		ScrollView scrollView = new ScrollView(this);
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		scrollView.addView(form.getView());

		unimp("add photo widget");

		setContentView(scrollView);
	}

	private void processReceiptButtonPress() {
		final boolean db = true;
		if (db)
			pr("Must now process Receipt Button Press");
	}

	// private View addPhotoWidget() {
	//
	// // Nest the image view within a horizontal layout, to add a 'camera'
	// // button to the bottom right
	// LinearLayout l2 = new LinearLayout(this);
	// l2.setOrientation(LinearLayout.HORIZONTAL);
	//
	// {
	// ImageView bitmapView = new ImageView(this);
	// this.photoView = bitmapView;
	//
	// // Give photo a fixed size that is small, but lots of weight to
	// // grow to take up what extra there is (horizontally)
	// LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(10,
	// LayoutParams.MATCH_PARENT, 1.0f);
	// l2.addView(bitmapView, p);
	// {
	// Button btn = new Button(this);
	// LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(
	// LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
	// 0.0f);
	// l2.addView(btn, p2);
	// btn.setCompoundDrawablesWithIntrinsicBounds(getResources()
	// .getDrawable(android.R.drawable.ic_menu_camera), null,
	// null, null);
	//
	// btn.setOnClickListener(new View.OnClickListener() {
	// public void onClick(View v) {
	// EditReceiptActivity a = (EditReceiptActivity) v
	// .getContext();
	// a.dispatchTakePictureIntent();
	// }
	// });
	// }
	// updatePhotoView();
	//
	// }
	// return l2;
	//
	// }

	// private void dispatchTakePictureIntent() {
	// if (db)
	// pr("dispatching an intent to take a picture\n");
	//
	// Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	//
	// if (intent.resolveActivity(getPackageManager()) == null) {
	// if (db)
	// pr(" could not resolve activity");
	// return;
	// }
	//
	// File workFile = getWorkPhotoFile();
	// workFile.delete();
	//
	// Uri uri = Uri.fromFile(workFile);
	// if (db)
	// pr("Uri.fromFile(workFile)=" + uri);
	// intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
	//
	// startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	// }

	private File getWorkPhotoFile() {
		return BitmapUtil.constructExternalImageFile("RBuddy_work");
	}

	private void processPhotoResult(Intent intent) {
		if (db)
			pr("\n\nprocessPhotoResult intent " + intent);

		File mainFile = null;
		unimp("handle various problem situations in ways other than just 'die'");
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

		mainFile = app.getPhotoFile().getMainFileFor(receipt.getId());
		if (db)
			pr("receipt id " + receipt.getId()
					+ "  copying scaled/rotated file " + workFile
					+ " to mainFile " + mainFile);

		try {
			Files.copy(workFile, mainFile);
		} catch (IOException e) {
			die(e);
		}

		if (db)
			pr("created main file " + mainFile);

		unimp("unpdate photo view");
		// if (db)
		// pr("updating photo view");
		// updatePhotoView();

	}

	// private void updatePhotoView() {
	// if (photoView == null)
	// return;
	// PhotoFile pf = app.getPhotoFile();
	//
	// // If no image exists, display placeholder instead
	// if (!pf.photoExists(receipt.getId())) {
	// photoView.setImageDrawable(getResources().getDrawable(
	// R.drawable.missingphoto));
	// } else {
	// File imageFile = pf.getMainFileFor(receipt.getId());
	// Bitmap bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
	// photoView.setImageDrawable(new BitmapDrawable(this.getResources(),
	// bmp));
	// }
	// }

	private void readWidgetValuesFromReceipt() {
		form.setValue("summary", receipt.getSummary());
		form.setValue("cost", receipt.getCost());
		form.setValue("date", receipt.getDate());
		form.setValue("tags", receipt.getTags());
	}

	private void updateReceiptWithWidgetValues() {
		if (db)
			pr("\nupdateReceiptWithWidgetValues\n");

		// To detect if changes have actually occurred, compare JSON
		// representations of the receipt before and after updating the fields.
		String origJSON = JSONEncoder.toJSON(receipt);

		receipt.setSummary(form.getValue("summary"));
		receipt.setCost(new Cost(form.getValue("cost"), true));
		JSDate date = JSDate.parse(form.getValue("date"), true);
		receipt.setDate(date);

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

	// private ImageView photoView;
}
