package com.js.rbuddyapp;

import android.content.Context;

import com.google.android.gms.drive.DriveFolder;
import com.js.android.BitmapUtil;
import com.js.android.FileArguments;

import static com.js.basic.Tools.*;

public class DrivePhotoStore extends SimplePhotoStore {

	/**
	 * This constructor may be called from other than the UI thread!
	 * 
	 * @param driveFile
	 */
	public DrivePhotoStore(Context context, UserData userData,
			DriveFolder photosFolder) {
		super(context);
		// UserData and DrivePhotoStore are mutually coupled
		this.mUserData = userData;
		this.mPhotosFolder = photosFolder;
	}

	@Override
	public void storePhoto(int receiptId, FileArguments args) {
		removeCachedVersions(receiptId);
		// Fill in additional fields, in case we're creating a new file
		args.setParentFolder(mPhotosFolder);
		args.setMimeType("image/jpeg");
		args.setFilename(BitmapUtil.constructReceiptImageFilename(receiptId));
		mUserData.writeBinaryFile(args);
	}

	@Override
	public void deletePhoto(int receiptId, FileArguments args) {
		warning("Google Drive api doesn't support delete yet (I think)");
	}

	@Override
	public void readPhoto(final int receiptId, final String fileIdString,
			final Variant variant) {
		if (readPhotoWithinCache(receiptId, fileIdString, variant))
			return;

		// We don't need the filename for this, just the file id
		final FileArguments args = new FileArguments();
		ASSERT(fileIdString != null, "expected non-null fileIdString");
		args.setFileId(fileIdString);

		// Specify code to run on UI thread when Drive API finishes reading:
		args.setCallback(new Runnable() {
			public void run() {
				// Run some code in the background thread to convert the JPEG to
				// a Drawable
				mBackgroundHandler.post(new Runnable() {
					@Override
					public void run() {
						convertJPEGAndCache(args.getData(), receiptId,
								fileIdString, variant);
					}
				});
			}
		});
		mUserData.readBinaryFile(args);
	}

	private UserData mUserData;
	private DriveFolder mPhotosFolder;
}
