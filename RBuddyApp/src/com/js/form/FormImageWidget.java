package com.js.form;

import java.util.Map;

import com.js.android.IPhotoListener;
import com.js.android.IPhotoStore;

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

		imageView = new ImageView(context());
		imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		imageView.setAdjustViewBounds(true);

		constructLabel();
		getWidgetContainer().addView(imageView);
	}

	public void displayPhoto(IPhotoStore photoStore, int receiptId,
			String photoId) {
		if (receiptId == 0) {
			if (listeningForReceiptId != 0) {
				photoStore.removePhotoListener(listeningForReceiptId, false,
						this);
				listeningForReceiptId = 0;
			}
		} else {
			if (listeningForReceiptId != 0) {
				photoStore.removePhotoListener(listeningForReceiptId, false,
						this);
			}

			listeningForReceiptId = receiptId;
			photoStore.addPhotoListener(listeningForReceiptId, false, this);

			String fileIdString = photoId;
			if (fileIdString != null) {
				// Have the PhotoStore load the image, and it will notify any
				// listeners (including us) when it has arrived
				photoStore
						.readPhoto(listeningForReceiptId, fileIdString, false);
			}
		}
	}

	@Override
	public void drawableAvailable(Drawable d, int receiptId, boolean thumbnail) {
		// We're not interested in thumbnails
		if (thumbnail)
			return;

		if (d == null) {
			d = getForm().context().getResources()
					.getDrawable(android.R.drawable.ic_menu_gallery);
		}
		imageView.setImageDrawable(d);
	}

	private ImageView imageView;
	private int listeningForReceiptId;
}
