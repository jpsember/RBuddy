package com.js.android;

//import com.js.android.IPhotoListener;

public interface IPhotoStore {

	public enum Variant {
		FULLSIZE, THUMBNAIL,
	}

	// TODO Issue #31
	public static final int THUMBNAIL_HEIGHT = 150;
	public static final int FULLSIZE_HEIGHT = 800;
	public static final String PREFERENCE_KEY_PHOTO_DELAY = "photo_delays";

	public void readPhoto(int ownerId, String resourceName, Variant variant);

	/**
	 * Stored photos are assumed to be full size (not thumbnails)
	 * 
	 * @param args
	 */
	public void storePhoto(int receiptId, FileArguments args);

	/**
	 * @param receiptId
	 * @param args
	 */
	public void deletePhoto(int receiptId, FileArguments args);

	/**
	 * Prepare drawables and send to listeners for a particular id
	 */
	public void pushPhoto(int ownerId, String photoId);

	/**
	 * Register a listener for a particular photo
	 * 
	 * @param receiptId
	 *            the receipt who owns the photo
	 * @param listener
	 *            the listener to notify when drawable is available
	 */
	public void addPhotoListener(int receiptId, Variant variant,
			IPhotoListener listener);

	/**
	 * Remove listener
	 * 
	 * @param receiptId
	 * @param listener
	 */
	public void removePhotoListener(int receiptId, Variant variant,
			IPhotoListener listener);

}
