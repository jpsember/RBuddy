package js.rbuddyapp;

import com.google.android.gms.drive.DriveFolder;

import static js.basic.Tools.*;

public class DrivePhotoStore extends SimplePhotoStore {

	/**
	 * This constructor may be called from other than the UI thread!
	 * 
	 * @param driveFile
	 */
	public DrivePhotoStore(UserData userData, DriveFolder photosFolder) {
		this.userData = userData;
		this.photosFolder = photosFolder;
	}

	@Override
	public void storePhoto(FileArguments args) {
		args.setParentPhoto(photosFolder);
		args.setMimeType("image/jpeg");
		userData.writeBinaryFile(args);
	}

	@Override
	public void deletePhoto(FileArguments args) {
		warning("Google Drive api doesn't support delete yet (I think)");
	}

	@Override
	public void readPhoto(final int receiptId, final String fileIdString,
			final boolean thumbnail) {
		if (readPhotoWithinCache(receiptId, fileIdString, thumbnail))
			return;

		// We don't need the filename for this, just the file id
		unimp("allow passing null filename, null parent folder");
		final FileArguments args = new FileArguments("...unknown filename...");
		args.setParentPhoto(photosFolder); // though not used for reading
		ASSERT(fileIdString != null, "expected non-null fileIdString");
		args.setFileId(fileIdString);

		// Specify code to run on UI thread when Drive API finishes reading:
		args.setCallback(new Runnable() {
			public void run() {
				// Run some code in the background thread to convert the JPEG to
				// a Drawable
				backgroundHandler.post(new Runnable() {
					@Override
					public void run() {
						convertJPEGAndCache(args.getData(), receiptId,
								fileIdString, thumbnail);
					}
				});
			}
		});
		userData.readBinaryFile(args);
	}

	private UserData userData;
	private DriveFolder photosFolder;
}
