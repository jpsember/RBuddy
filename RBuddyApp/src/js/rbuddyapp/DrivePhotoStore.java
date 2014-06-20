package js.rbuddyapp;

import com.google.android.gms.drive.DriveFolder;

public class DrivePhotoStore implements IPhotoStore {

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
	public void readPhoto(FileArguments args) {
		args.setParentPhoto(photosFolder); // though not used for reading
		userData.readBinaryFile(args);
	}

	private UserData userData;
	private DriveFolder photosFolder;
}
