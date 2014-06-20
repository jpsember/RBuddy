package js.rbuddyapp;

//import static js.basic.Tools.*;

import java.util.ArrayList;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;

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
		args.mimeType = "image/jpeg";
		userData.writeBinaryFile(args);
	}

	@Override
	public void readPhoto(FileArguments args) {
		args.setParentPhoto(photosFolder); // though not used for reading
		userData.readBinaryFile(args);
	}

	@Override
	@Deprecated
	public void readPhoto(String photoId, Runnable callback,
			ArrayList returnValue) {
		DriveFile driveFile = userData.fileWithId(DriveId
				.decodeFromString(photoId));
		userData.readBinaryFile(driveFile, callback, returnValue);
	}

	@Override
	@Deprecated
	/**
	 * The photoId argument (and that returned) is an encoded DriveId 
	 * 
	 * @deprecated
	 */
	public void storePhoto(String photoId, byte[] jpeg, Runnable callback,
			ArrayList returnValue) {
		//
		// FileArguments args = new FileArguments();
		// args.parentFolder = photosFolder;
		// args.setDriveId(photoId);
		// args.mimeType = "image/jpeg";
		// args.callback = callback;
		// userData.writeBinaryFile(args);
		// Tools.die("not doing anything with return value");
		//
		// //
		// // userData.writeBinaryFile(photosFolder, driveId,
		// // "RBuddy receipt photo",
		// // jpeg, "image/jpeg", callback, returnValue);
	}

	private UserData userData;
	private DriveFolder photosFolder;
}
