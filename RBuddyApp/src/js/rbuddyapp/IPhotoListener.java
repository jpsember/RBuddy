package js.rbuddyapp;

import android.graphics.drawable.Drawable;

public interface IPhotoListener {

	/**
	 * Notify listener that drawable is available
	 * 
	 * @param d
	 *            drawable, or null if no such photo exists
	 * @param receiptId
	 *            id of receipt whose photo is being observed
	 * @param thumbnail
	 *            true if drawable represents a thumbnail (vs fullsize)
	 */
	public void drawableAvailable(Drawable d, int receiptId, boolean thumbnail);
}
