package js.rbuddyapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

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
	public void readPhoto(final int receiptId, final String fileIdString) {
		// We don't need the filename for this, just the file id
		unimp("allow passing null filename, null parent folder");
		final FileArguments args = new FileArguments("...unknown filename...");
		args.setParentPhoto(photosFolder); // though not used for reading
		ASSERT(fileIdString != null, "expected non-null fileIdString");
		args.setFileId(fileIdString);

		args.setCallback(new Runnable() {
			@Override
			public void run() {
				readPhotoCallback(receiptId, args);
			}
		});

		userData.readBinaryFile(args);
	}

	private void readPhotoCallback(int receiptId, FileArguments args) {
		warning("we should be doing this in a background thread");
		byte[] jpeg = args.getData();
		Bitmap bmp = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
		final Drawable d = new BitmapDrawable(RBuddyApp.sharedInstance()
				.context().getResources(), bmp);
		notifyListenersOfDrawable(receiptId, args.getFileIdString(), d);
	}

	private UserData userData;
	private DriveFolder photosFolder;
}
