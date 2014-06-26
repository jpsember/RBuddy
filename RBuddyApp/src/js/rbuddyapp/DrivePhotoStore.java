package js.rbuddyapp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.google.android.gms.drive.DriveFolder;

import static js.basic.Tools.*;

public class DrivePhotoStore implements IPhotoStore {

	/**
	 * This constructor may be called from other than the UI thread!
	 * 
	 * @param driveFile
	 */
	public DrivePhotoStore(UserData userData, DriveFolder photosFolder) {
		this.userData = userData;
		this.photosFolder = photosFolder;
		this.listenersMap = new HashMap();
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
	public void addPhotoListener(String fileIdString, IPhotoListener listener) {
		final boolean db = true;

		Set<IPhotoListener> listeners = listenersMap.get(fileIdString);
		if (listeners == null) {
			listeners = new HashSet<IPhotoListener>();
			listenersMap.put(fileIdString, listeners);
		}
		listeners.add(listener);
		if (db)
			pr(hey() + listener + "\n"
					+ SimplePhotoStore.dumpListeners(listenersMap));
	}

	@Override
	public void removePhotoListener(String fileIdString, IPhotoListener listener) {
		Set<IPhotoListener> listeners = listenersMap.get(fileIdString);
		if (listeners == null)
			return;
		listenersMap.remove(fileIdString);
		if (db)
			pr(hey() + listener + "\n"
					+ SimplePhotoStore.dumpListeners(listenersMap));
	}

	@Override
	public void readPhoto(final String fileIdString) {
		// We don't need the filename for this, just the file id
		unimp("allow passing null filename, null parent folder");
		final FileArguments args = new FileArguments("...unknown filename...");
		args.setParentPhoto(photosFolder); // though not used for reading
		ASSERT(fileIdString != null, "expected non-null fileIdString");
		args.setFileId(fileIdString);

		args.setCallback(new Runnable() {
			@Override
			public void run() {
				readPhotoCallback(args);
			}
		});

		userData.readBinaryFile(args);
	}

	private void readPhotoCallback(FileArguments args) {
		final boolean db = true;
		warning("we should be doing this in a background thread");
		byte[] jpeg = args.getData();
		if (db)
			pr(hey() + " jpeg=" + jpeg);
		Bitmap bmp = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
		final Drawable d = new BitmapDrawable(RBuddyApp.sharedInstance()
				.context().getResources(), bmp);
		if (db)
			pr(" drawable=" + d);
		notifyListenersOfDrawable(args.getFileIdString(), d);
	}

	private void notifyListenersOfDrawable(String photoIdString, Drawable d) {
		if (db)
			pr("DrivePhotoStore.notifyListenersOfDrawable, photoId "
					+ photoIdString);
		Set<IPhotoListener> listeners = listenersMap.get(photoIdString);
		if (listeners == null)
			return;
		for (IPhotoListener listener : listeners) {
			if (db)
				pr(" notifying listener " + listener);
			listener.drawableAvailable(d, photoIdString);
		}
	}

	private Map<String, Set<IPhotoListener>> listenersMap;

	private UserData userData;
	private DriveFolder photosFolder;
}
