package js.rbuddyapp;

import static js.basic.Tools.*;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import js.basic.Files;

public class PhotoStore implements IPhotoStore {

	private static final Random random = new Random(System.currentTimeMillis());

	public PhotoStore() {
	}

	@Override
	public String storePhoto(String photoId, byte[] jpeg) throws IOException {
		// final boolean db = true;
		if (db)
			pr("\n\n" + stackTrace() + "  storePhoto id=" + photoId
					+ " length=" + jpeg.length);
		if (photoId == null) {
			// This of course is not guaranteed to produce unique values, but
			// it's just for test purposes
			photoId = "RND_" + Math.abs(random.nextInt());
			if (db)
				pr(" chose new random id " + photoId);
		}

		File f = getFileForPhotoId(photoId);
		Files.writeBinaryFile(f, jpeg);
		if (db)
			pr(" wrote to file " + f);
		return photoId;
	}

	@Override
	public byte[] readPhoto(String photoId) throws IOException {
		// final boolean db = true;
		if (db)
			pr("\n\n\nreadPhoto id=" + photoId);
		if (photoId == null)
			throw new IllegalArgumentException();
		File f = getFileForPhotoId(photoId);
		byte[] jpg = Files.readBinaryFile(f);
		if (db)
			pr(" read jpeg of length " + jpg.length);
		return jpg;
	}

	private File getFileForPhotoId(String photoId) {
		return new File(RBuddyApp.sharedInstance().context()
				.getExternalFilesDir(null), "photo_" + photoId
				+ BitmapUtil.JPEG_EXTENSION);
	}

}
