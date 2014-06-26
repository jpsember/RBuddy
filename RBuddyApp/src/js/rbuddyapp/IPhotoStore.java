package js.rbuddyapp;

public interface IPhotoStore {

	public void readPhoto(String fileIdString);

	public void storePhoto(FileArguments args);

	public void deletePhoto(FileArguments args);

	/**
	 * Register a listener for a particular photo
	 * 
	 * @param fileIdString
	 *            the fileIdString of the photo being listened for
	 * @param listener
	 *            the listener to notify when drawable is available
	 */
	public void addPhotoListener(String fileIdString, IPhotoListener listener);

	/**
	 * Remove listener
	 * 
	 * @param fileIdString
	 * @param listener
	 */
	public void removePhotoListener(String fileIdString, IPhotoListener listener);

}
