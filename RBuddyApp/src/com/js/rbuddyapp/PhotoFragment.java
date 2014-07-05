package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import java.io.File;
import java.io.IOException;

import com.js.form.Form;
import com.js.form.FormImageWidget;
import com.js.android.ActivityState;
import com.js.android.BitmapUtil;
import com.js.basic.Files;
import com.js.rbuddy.R;
import com.js.rbuddy.Receipt;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.view.View.OnClickListener;

public class PhotoFragment extends MyFragment {

	public static final String TAG = "Photo";
	public static Factory FACTORY = new Factory() {

		@Override
		public String name() {
			return TAG;
		}

		@Override
		public MyFragment construct() {
			return new PhotoFragment();
		}
	};

	/**
	 * Construct the singleton instance of this fragment, if it hasn't already
	 * been
	 * 
	 * @param organizer
	 * @return
	 */
	public static PhotoFragment construct(FragmentOrganizer organizer) {
		organizer.register(FACTORY);
		return (PhotoFragment) organizer.get(TAG, true);
	}

	// Identifiers for the intents that we may spawn
	private static final int REQUEST_IMAGE_CAPTURE = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		app = RBuddyApp.sharedInstance(getActivity());
		layoutElements();

		// layoutElements();
		activityState = new ActivityState() //
				.add(scrollView) //
				.restoreStateFrom(savedInstanceState);
		return scrollView;
	}

	@Override
	public void onResume() {
		super.onResume();
		imageWidget.displayPhoto(receipt.getId(), receipt.getPhotoId());
	}

	@Override
	public void onPause() {
		super.onPause();
		// Display nothing, so widget stops listening; else it will leak
		unimp("we should probably pass a receipt here instead");
		imageWidget.displayPhoto(0, null);
		app.receiptFile().flush();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		activityState.saveState(outState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		final boolean db = true;
		if (db)
			pr(hey() + "requestCode " + requestCode + " result " + resultCode
					+ " data " + data);
		if (requestCode == REQUEST_IMAGE_CAPTURE) {
			if (resultCode == Activity.RESULT_OK)
				processPhotoResult(data);
			// Whether or not the user selected a new photo, exit to the
			// EditReceipt activity.
			unimp("pop stack to previous activity?");
			// finish();
		}
	}

	private void layoutElements() {
		String jsonString = readTextFileResource(this.getActivity(),
				R.raw.form_photo_activity);

		this.form = Form.parse(this.getActivity(), jsonString);
		form.getField("takephoto").setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startImageCaptureIntent();
			}
		});
		imageWidget = (FormImageWidget) form.getField("photo");

		scrollView = new ScrollView(getActivity());
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		scrollView.addView(form.getView());

		// setContentView(scrollView);
	}

	private void startImageCaptureIntent() {
		final boolean db = true;
		if (db)
			pr(hey());
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
			return;
		}

		File workFile = getWorkPhotoFile();
		// Create the directories leading up to this file if necessary
		if (!workFile.exists()) {
			workFile.mkdirs();
		}

		if (db)
			pr(" workFile: " + workFile);
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
			pr(hey() + "intent " + intent);
		unimp("handle various problem situations in ways other than just 'die'");

		File workFile = getWorkPhotoFile();
		if (!workFile.isFile()) {
			Uri uri = Uri.fromFile(workFile);

			die("no work file found: " + workFile + ", uri=" + uri);
		}

		BitmapUtil.orientAndScaleBitmap(workFile, IPhotoStore.FULLSIZE_HEIGHT,
				true);

		try {
			FileArguments args = new FileArguments();
			args.setData(Files.readBinaryFile(workFile));
			args.setFileId(receipt.getPhotoId());

			final FileArguments arg = args;

			// We have to wait until the photo has been processed, and a photoId
			// assigned; then store this assignment in the receipt, and push new
			// version to any listeners
			args.setCallback(new Runnable() {
				public void run() {
					receipt.setPhotoId(arg.getFileIdString());
					app.photoStore().pushPhoto(receipt);
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

	// Photo interface (non-fragment methods)
	public void setReceipt(Receipt r) {
		this.receipt = r;
	}

	public void plot(FragmentOrganizer fragments, Receipt r) {
		setReceipt(r);
		fragments.plot(TAG, false, true);

		// // If no photo is defined for this receipt, jump right into the take
		// // photo intent
		// if (receipt.getPhotoId() == null) {
		// startImageCaptureIntent();
		// }
		unimp("not yet jumping into take photo intent");

	}

	private Receipt receipt;
	private RBuddyApp app;
	private Form form;
	private FormImageWidget imageWidget;
	private ScrollView scrollView;
	private ActivityState activityState;
}
