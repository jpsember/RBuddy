package js.rbuddyapp;

import android.graphics.drawable.Drawable;

public interface IPhotoListener {

	/**
	 * Notify listener that drawable is available
	 * 
	 * @param d
	 *            drawable, or null if no such photo exists
	 * @param fileIdString
	 *            id of photo being listened to
	 */
	public void drawableAvailable(Drawable d, String fileIdString);
}
