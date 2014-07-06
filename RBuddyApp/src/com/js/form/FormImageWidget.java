package com.js.form;

import java.util.Map;

import com.js.rbuddy.Receipt;
import com.js.rbuddyapp.IPhotoListener;
import com.js.rbuddyapp.IPhotoStore;
import com.js.rbuddyapp.RBuddyApp;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class FormImageWidget extends FormWidget implements IPhotoListener {

	public FormImageWidget(Form owner, Map attributes) {
		super(owner, attributes);

		imageView = new ImageView(context());
		imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		imageView.setAdjustViewBounds(true);

		constructLabel();
		getWidgetContainer().addView(imageView);
	}

	public void displayPhoto(Receipt receipt) {
		// TODO: decouple Form widgets (such as this one) from RBuddyApp (e.g.
		// Receipt class, PhotoStore)
		IPhotoStore photoStore = RBuddyApp.sharedInstance().photoStore();
		if (receipt == null) {
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

			listeningForReceiptId = receipt.getId();
			photoStore.addPhotoListener(listeningForReceiptId, false, this);

			String fileIdString = receipt.getPhotoId();
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
