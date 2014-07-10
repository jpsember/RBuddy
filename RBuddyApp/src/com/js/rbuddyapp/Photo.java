package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import java.io.File;
import java.io.IOException;

import com.js.form.Form;
import com.js.form.FormImageWidget;
import com.js.android.FragmentOrganizer;
import com.js.android.MyFragment;
import com.js.android.PseudoFragment;
import com.js.android.BitmapUtil;
import com.js.basic.Files;
import com.js.rbuddy.R;
import com.js.rbuddy.Receipt;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.view.View.OnClickListener;
import com.js.android.FileArguments;
import com.js.android.IPhotoStore;

public class Photo extends PseudoFragment {

	public static class Wrapper extends MyFragment {
		public Wrapper() {
		}

		@Override
		public Class getFragmentClass() {
			return Photo.class;
		}
	}

	public Photo(FragmentOrganizer fragments) {
		super(fragments);
		mFragments = fragments;
		new Wrapper().register(fragments);
		mApp = RBuddyApp.sharedInstance();
	}

	@Override
	public void onRestoreInstanceState(Bundle bundle) {
		super.onRestoreInstanceState(bundle);
		if (bundle != null) {
			int receiptId = bundle.getInt("PhotoReceiptNumber", 0);
			Receipt r = null;
			if (receiptId != 0)
				r = mApp.receiptFile().getReceipt(receiptId);
			setReceipt(r);
		}
	}

	@Override
	public View onCreateView() {
		log("onCreateView");

		layoutElements();

		getActivityState() //
				.add(mScrollView) //
				.restoreViewsFromSnapshot();
		return mScrollView;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mPopFlag) {
			mPopFlag = false;
			mFragments.pop();
			return;
		}

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
		if (mForm != null) {
			// Display nothing, so widget stops listening; else it will leak
			imageWidget().displayPhoto(mApp.photoStore(), 0, null);
		}
		mApp.receiptFile().flush();
	}

	private void layoutElements() {
		String jsonString = readTextFileResource(getContext(),
				R.raw.form_photo_activity);

		this.mForm = mApp.parseForm(getContext(), jsonString);
		mForm.getField("takephoto").setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startImageCaptureIntent();
			}
		});

		mScrollView = new ScrollView(getContext());
		mScrollView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		mScrollView.addView(mForm.getView());
	}

	private void startImageCaptureIntent() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		if (intent.resolveActivity(getContext().getPackageManager()) == null) {
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
		listener().processCapturePhotoIntent(intent);
	}

	private File getWorkPhotoFile() {
		return BitmapUtil.constructExternalImageFile("work");
	}

	/**
	 * Update the actual photo, if fragment is in an appropriate state
	 */
	private void displayReceiptPhoto() {
		if (mForm == null)
			return;
		imageWidget().displayPhoto(mApp.photoStore(), mReceipt.getId(),
				mReceipt.getPhotoId());
	}

	// Photo interface (non-fragment methods)
	public void setReceipt(Receipt r) {
		this.mReceipt = r;
		displayReceiptPhoto();
	}

	private FormImageWidget imageWidget() {
		return (FormImageWidget) mForm.getField("photo");
	}

	public void processImageCaptureResult(int resultCode, Intent intent) {
		if (resultCode == Activity.RESULT_OK) {
			// TODO handle various problem situations in ways other than just
			// 'die'

			File workFile = getWorkPhotoFile();
			if (!workFile.isFile()) {
				Uri uri = Uri.fromFile(workFile);
				die("no work file found: " + workFile + ", uri=" + uri);
			}

			BitmapUtil.orientAndScaleBitmap(workFile,
					IPhotoStore.FULLSIZE_HEIGHT, true);

			try {
				FileArguments args = new FileArguments();
				args.setData(Files.readBinaryFile(workFile));
				args.setFileId(mReceipt.getPhotoId());

				final FileArguments arg = args;

				// We have to wait until the photo has been processed, and a
				// photoId
				// assigned; then store this assignment in the receipt, and push
				// new
				// version to any listeners
				args.setCallback(new Runnable() {
					public void run() {
						mReceipt.setPhotoId(arg.getFileIdString());
						mApp.receiptFile().setModified(mReceipt);
						// TODO: need better pattern to keep track of changes to
						// receipts, maybe do it silently
						mApp.photoStore().pushPhoto(mReceipt.getId(),
								mReceipt.getPhotoId());
					}
				});
				IPhotoStore ps = mApp.photoStore();
				ps.storePhoto(mReceipt.getId(), args);
			} catch (IOException e) {
				// TODO display popup message to user, and don't update
				// receipt's
				// photo id
				die(e);
			}
		}
		// Whether or not the user selected a new photo, pop this fragment at
		// the next opportunity (we must wait until onResume() is called again;
		// see issue #70)
		mPopFlag = true;
	}

	/**
	 * Get listener by casting parent activity
	 * 
	 * @return
	 */
	private Listener listener() {
		return (Listener) getContext();
	}

	public static interface Listener {
		void processCapturePhotoIntent(Intent intent);
	}

	private Receipt mReceipt;
	private RBuddyApp mApp;
	private Form mForm;
	private ScrollView mScrollView;
	private boolean mPopFlag;
	private FragmentOrganizer mFragments;
}
