package js.rbuddyapp;

public interface IPhotoStore {

	// TODO Issue #31
	public static final int THUMBNAIL_HEIGHT = 150;
	public static final int FULLSIZE_HEIGHT = 800;

	public void readPhoto(int receiptId, String fileIdString, boolean thumbnail);

	/**
	 * Stored photos are assumed to be full size (not thumbnails)
	 * 
	 * @param args
	 */
	public void storePhoto(int receiptId, FileArguments args);

	public void deletePhoto(FileArguments args);

	/**
	 * Register a listener for a particular photo
	 * 
	 * @param receiptId
	 *            the receipt who owns the photo
	 * @param listener
	 *            the listener to notify when drawable is available
	 */
	public void addPhotoListener(int receiptId, IPhotoListener listener);

	/**
	 * Remove listener
	 * 
	 * @param receiptId
	 * @param listener
	 */
	public void removePhotoListener(int receiptId, IPhotoListener listener);

}
