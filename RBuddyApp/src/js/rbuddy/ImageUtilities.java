package js.rbuddy;

import static js.basic.Tools.pr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import static js.basic.Tools.*;

public class ImageUtilities {
	
	public static final int JPEG_QUALITY_DEFAULT = 80;
	
	public static void writeJPEG(Bitmap bitmap, File destinationFile)
			throws IOException {
		writeJPEG(bitmap,destinationFile,JPEG_QUALITY_DEFAULT);
	}
	
	public static void writeJPEG(Bitmap bitmap, File destinationFile, int quality)
			throws IOException {
//		final boolean db = true;
		FileOutputStream fOut = new FileOutputStream(destinationFile);
		bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fOut);
		fOut.flush();
		fOut.close();
		
		if (db) pr("writeJPEG to "+destinationFile+", length "+destinationFile.length());
	}
	
	/**
	 * Construct a File for an xxxx.jpg image in the external storage directory;
	 * delete any existing file that is at that location
	 * 
	 * @param name
	 *            the 'xxxx'
	 * @return File
	 */
	public static File constructExternalImageFile(String name) {
		File storageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File imageFile = new File(storageDir, "JS_"+name + PhotoFile.PHOTO_EXTENSION);
		imageFile = imageFile.getAbsoluteFile();
		imageFile.delete();
		return imageFile;
	}

	/**
	 * Scale a jpeg so it fits within a rectangle
	 * 
	 * @param originalFile
	 *            file containing jpeg
	 * @param maxScaledDimension
	 *            the maximum number of pixels in either dimension
	 * @return file containing scaled version, or original if no scaling was
	 *         necessary. Caller should copy the file to a permanent location, since the
	 *         file may be deleted as its name is recycled in subsequent calls to this (or other) methods.
	 */
	public static File scalePhoto(File originalFile, int scaledWidth, int scaledHeight, boolean allowScalingUp)  
			throws IOException {
//		final boolean db = true;
		if (db)
			pr("scalePhoto " + originalFile);

		if (!originalFile.isFile())
			throw new FileNotFoundException("cannot scale missing photo: "
					+ originalFile);

		Bitmap myBitmap = BitmapFactory.decodeFile(originalFile
				.getAbsolutePath());
		double scaleFactor = Math.min(
				scaledWidth / (double) myBitmap.getWidth(),
				scaledHeight / (double) myBitmap.getHeight());
		if (db)
			pr(" original size " + myBitmap.getWidth() + " x "
					+ myBitmap.getHeight() + "  scale factor " + scaleFactor);

		if (!allowScalingUp)
			scaleFactor = Math.min(scaleFactor, 1.0);
		int actualScaledWidth = (int) Math.round(myBitmap.getWidth() * scaleFactor);
		int actualScaledHeight = (int) Math.round(myBitmap.getHeight() * scaleFactor);

		// Apparently no filtering is required if we're scaling down.
		boolean useFilter = scaleFactor > 0;
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(myBitmap, actualScaledWidth,
				actualScaledHeight, useFilter);
		File imageFile = ImageUtilities.constructExternalImageFile("RBuddy_scaled");
		ImageUtilities.writeJPEG(scaledBitmap, imageFile);
		return imageFile;
	}

}
