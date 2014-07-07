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
import com.js.android.FileArguments;
import com.js.android.IPhotoStore;

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

		mApp = RBuddyApp.sharedInstance(getActivity());
		layoutElements();

		mActivityState = new ActivityState() //
				.add(mScrollView) //
				.restoreStateFrom(savedInstanceState);
		return mScrollView;
	}

	@Override
	public void onResume() {
		super.onResume();

		displayReceiptPhoto();

		// If no photo is defined for this receipt, act as if he has pressed the
		// camera button

		if (mReceipt.getPhotoId() == null) {
			startImageCaptureIntent();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		// Display nothing, so widget stops listening; else it will leak
		mImageWidget.displayPhoto(mApp.photoStore(), 0, null);
		mApp.receiptFile().flush();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE) {
			if (resultCode == Activity.RESULT_OK)
				processPhotoResult(data);
			// Whether or not the user selected a new photo, pop this fragment
			getActivity().getFragmentManager().popBackStack();
		}
	}

	private void layoutElements() {
		String jsonString = readTextFileResource(this.getActivity(),
				R.raw.form_photo_activity);

		this.mForm = mApp.parseForm(getActivity(), jsonString);
		mForm.getField("takephoto").setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startImageCaptureIntent();
			}
		});
		mImageWidget = (FormImageWidget) mForm.getField("photo");

		mScrollView = new ScrollView(getActivity());
		mScrollView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		mScrollView.addView(mForm.getView());
	}

	private void startImageCaptureIntent() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
			return;
		}

		File workFile = getWorkPhotoFile();
		File parent = workFile.getParentFile();

		// Create the directories leading up to this file if necessary
		if (!parent.exists()) {
			boolean r = parent.mkdirs();
			if (!r)
				die("failed to create directory " + parent);
		}

		Uri uri = Uri.fromFile(workFile);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	}

	private File getWorkPhotoFile() {
		return BitmapUtil.constructExternalImageFile("work");
	}

	private void processPhotoResult(Intent intent) {
		// TODO handle various problem situations in ways other than just 'die'

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
			args.setFileId(mReceipt.getPhotoId());

			final FileArguments arg = args;

			// We have to wait until the photo has been processed, and a photoId
			// assigned; then store this assignment in the receipt, and push new
			// version to any listeners
			args.setCallback(new Runnable() {
				public void run() {
					mReceipt.setPhotoId(arg.getFileIdString());
					mApp.photoStore().pushPhoto(mReceipt.getId(),
							mReceipt.getPhotoId());
				}
			});
			IPhotoStore ps = mApp.photoStore();
			ps.storePhoto(mReceipt.getId(), args);
		} catch (IOException e) {
			// TODO display popup message to user, and don't update receipt's
			// photo id
			die(e);
		}
	}

	/**
	 * Update the actual photo, if fragment is in an appropriate state
	 */
	private void displayReceiptPhoto() {
		if (!isResumed())
			return;
		mImageWidget.displayPhoto(mApp.photoStore(), mReceipt.getId(),
				mReceipt.getPhotoId());
	}

	// Photo interface (non-fragment methods)
	public void setReceipt(Receipt r) {
		this.mReceipt = r;
		displayReceiptPhoto();
	}

	private Receipt mReceipt;
	private RBuddyApp mApp;
	private Form mForm;
	private FormImageWidget mImageWidget;
	private ScrollView mScrollView;
}