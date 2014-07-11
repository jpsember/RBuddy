package com.js.form;

import java.util.Map;

import com.js.android.IPhotoListener;
import com.js.android.IPhotoStore;
import com.js.android.IPhotoStore.Variant;

import static com.js.android.Tools.*;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class FormImageWidget extends FormWidget implements IPhotoListener {
	public static final Factory FACTORY = new FormWidget.Factory() {

		@Override
		public String getName() {
			return "imageview";
		}

		@Override
		public FormWidget constructInstance(Form owner, Map attributes) {
			return new FormImageWidget(owner, attributes);
		}
	};

	public FormImageWidget(Form owner, Map attributes) {
		super(owner, attributes);

		mImageVariant = Variant.FULLSIZE;
		mImageView = new ImageView(context());
		mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		mImageView.setAdjustViewBounds(true);

		constructLabel();
		getWidgetContainer().addView(mImageView);
	}

	/**
	 * Display a particular photo in the image view
	 * 
	 * @param photoStore
	 *            source of photos
	 * @param ownerId
	 *            owner id, or zero if none assigned
	 * @param photoId
	 *            id of photo for owner, or null if none; ignored if owner id is
	 *            zero
	 */
	public void displayPhoto(IPhotoStore photoStore, int ownerId, String photoId) {
		if (db)
			pr(hey() + "ownerId " + ownerId + " photoId " + photoId);
		if (ownerId == 0)
			photoId = null;

		// If owner id is changing, replace old listener with new
		if (mOwnerIdBeingListenedTo != ownerId) {
			photoStore.removePhotoListener(mOwnerIdBeingListenedTo,
					IPhotoStore.Variant.FULLSIZE, this);
			mOwnerIdBeingListenedTo = ownerId;
			if (ownerId != 0)
				photoStore.addPhotoListener(ownerId,
						IPhotoStore.Variant.FULLSIZE, this);
		}

		if (photoId == null) {
			clearPhoto();
		} else {
			photoStore.readPhoto(ownerId, photoId, mImageVariant);
		}
	}

	private void clearPhoto() {
		setDrawable(null);
	}

	private void setDrawable(Drawable d) {
		if (d == null) {
			d = getForm().context().getResources()
					.getDrawable(android.R.drawable.ic_menu_gallery);
		}
		mImageView.setImageDrawable(d);
	}

	@Override
	public void drawableAvailable(Drawable d, int receiptId,
			IPhotoStore.Variant variant) {
		ASSERT(variant == Variant.FULLSIZE);
		setDrawable(d);
	}

	private ImageView mImageView;
	private int mOwnerIdBeingListenedTo;
	private Variant mImageVariant;
}
