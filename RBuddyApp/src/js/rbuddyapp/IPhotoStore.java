package js.rbuddyapp;

import java.io.IOException;

public interface IPhotoStore {

	/**
	 * Store a JPEG
	 * 
	 * @param photoId
	 *            id to store JPEG as; if null, allocates new one
	 * @param jpeg
	 *            JPEG
	 * @return the id it was stored as
	 * @throws IOException
	 */
	public String storePhoto(String photoId, byte[] jpeg) throws IOException;

	/**
	 * Read JPEG
	 * 
	 * @param photoId
	 * @return JPEG byte array
	 * @throws IOException
	 */
	public byte[] readPhoto(String photoId) throws IOException;

}
