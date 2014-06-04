package js.rbuddy;

import static js.basic.Tools.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import js.basic.Files;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

public class MainActivity extends Activity {

	private static final int REQUEST_IMAGE_CAPTURE = 1;

	@Override
	public void onSaveInstanceState(Bundle s) {
		// The OS may be shutting down our activity to service some other
		// (possibly memory-intensive) task;
		// so save our state
		s.putString("pathOfTakenPhoto", pathOfTakenPhoto);
		super.onSaveInstanceState(s);
	}

	private void constructView() {

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams linLayoutParam = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		// set this layout as the root element of the activity
		setContentView(layout, linLayoutParam);

		{
			LayoutParams layoutParam = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

			{
				TextView tv = new TextView(this);
				this.textView = tv;
				tv.setLayoutParams(layoutParam);
				layout.addView(tv);
				textView.setText(getDrinkOrderString());
			}

			{
				Button btn = new Button(this);

				btn.setText("Change Drink Order");
				layout.addView(btn, layoutParam);
				btn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						MainActivity a = (MainActivity) v.getContext();
						a.updateDrinkOrder();
					}
				});
			}
			{
				Button btn = new Button(this);
				btn.setText("Show List Activity");
				layout.addView(btn, layoutParam);
				btn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						displayListActivity();
					}
				});
			}

			{
				Button btn = new Button(this);
				btn.setText("Take Photo");
				layout.addView(btn, layoutParam);
				btn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						dispatchTakePictureIntent();
					}
				});
			}
		}
	}

	private void displayListActivity() {
		// Start the receipt list activity, and pass a message to it
		Intent intent = new Intent(getApplicationContext(),
				ReceiptListActivity.class);
		intent.putExtra("message", "Drink Menu (" + textView.getText() + ")");
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_search:
			displayListActivity();
			return true;
		case R.id.action_settings:
			unimp("settings");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		getMenuInflater().inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		startApp(); // does nothing if already started
		JSDate.setFactory(AndroidDate.androidDateFactory);
		if (savedInstanceState != null) {
			// Restore our activity's previous state
			pathOfTakenPhoto = savedInstanceState.getString("pathOfTakenPhoto");
			// this is maybe a good place for an 'ActivityState' object
			// that handles this persistence automatically, maybe as a key/value
			// store;
			// or maybe we just store such values in a Java Map, and persist
			// that map using the existing API
		}
		preparePhotoFile();
		constructView();
	}

	private static final String[] drinks = {//
	"---no drink selected---", //
			"Double short Americano", //
			"Frappucino", //
			"Drip Coffee", //
	};

	private String getDrinkOrderString() {
		return drinks[drinkNumber];
	}

	private void updateDrinkOrder() {
		drinkNumber = (drinkNumber + 1) % drinks.length;
		textView.setText(getDrinkOrderString());
	}

	private int drinkNumber;
	private TextView textView;

	// ------------- photo stuff

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

		File workFile = ImageUtilities.constructExternalImageFile("RBuddy_work");

		// save work file in instance field, so we can refer to it later
		pathOfTakenPhoto = workFile.getPath();

		Uri uri = Uri.fromFile(workFile);
		if (db)
			pr("Uri.fromFile(workFile)=" + uri);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	}

	@SuppressLint("SimpleDateFormat")
	private void processPhotoResult(Intent intent) {
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
			scaledFile = ImageUtilities.scalePhoto(workFile, 800,800,true);
		} catch (IOException e1) {
			die(e1);
		}

		// Create a new photo to store this work file
		// Create an image file name
		String photoIdentifier = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		Photo photo = new Photo(photoIdentifier);
		File mainFile = photoFile.getMainFileFor(photo);
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

	private void preparePhotoFile() {
		// final boolean db = true;
		if (db)
			pr("preparePhotoFile; " + stackTrace(1, 1));

		File d = new File(getExternalFilesDir(null), "photos");
		if (!d.exists()) {
			d.mkdir();
		}
		if (!d.isDirectory())
			die("failed to create directory " + d);
		photoFile = new PhotoFile(d);
		if (db)
			pr("preparePhotoFile, created " + photoFile + ";\ncontents=\n"
					+ photoFile.contents());
	}

	private PhotoFile photoFile;
	private String pathOfTakenPhoto;
}
