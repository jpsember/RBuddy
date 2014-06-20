package js.rbuddyapp;

import java.util.ArrayList;

public interface IPhotoStore {

	/**
	 * Store a JPEG
	 * 
	 * @param photoId
	 *            id to store JPEG as; if null, allocates new one
	 * @param jpeg
	 *            JPEG
	 * @param callback
	 *            if not null, callback occurs when complete
	 * @param returnValue
	 *            the (String) id that the photo was stored as is returned here
	 *            as the first element
	 * @deprecated
	 */
	public void storePhoto(String photoId, byte[] jpeg, Runnable callback,
			ArrayList returnValue);

	/**
	 * Store a JPEG
	 * 
	 */
	public void storePhoto(FileArguments args);

	/**
	 * Read JPEG
	 * 
	 * @param photoId
	 * @param callback
	 *            if not null, callback occurs when complete
	 * @param returnValue
	 *            the JPEG byte array is returned as the first element
	 * @deprecated
	 */
	public void readPhoto(String photoId, Runnable callback,
			ArrayList returnValue);

	public void readPhoto(FileArguments args);
}
