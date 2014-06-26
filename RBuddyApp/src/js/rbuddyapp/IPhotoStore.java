package js.rbuddyapp;

public interface IPhotoStore {

	public void readPhoto(int receiptId, String fileIdString);

	public void storePhoto(FileArguments args);

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
