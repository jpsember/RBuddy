package js.form;

import java.util.Map;

import js.rbuddyapp.IPhotoListener;
import js.rbuddyapp.IPhotoStore;
import js.rbuddyapp.RBuddyApp;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import static js.basic.Tools.*;

public class FormImageWidget extends FormWidget implements IPhotoListener {

	public FormImageWidget(Form owner, Map attributes) {
		super(owner, attributes);

		imageView = new ImageView(context());
		imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		imageView.setAdjustViewBounds(true);

		constructLabel();
		getWidgetContainer().addView(imageView);
	}

	public void displayPhoto(int receiptId, String fileIdString) {
		IPhotoStore photoStore = RBuddyApp.sharedInstance().photoStore();
		if (receiptId == 0) {
			if (listeningForReceiptId != 0) {
				photoStore.removePhotoListener(listeningForReceiptId, this);
				listeningForReceiptId = 0;
			}
		} else {
			if (listeningForReceiptId != 0) {
				photoStore.removePhotoListener(listeningForReceiptId, this);
			}

			listeningForReceiptId = receiptId;
			unimp("add parameter and support for thumbnails");
			photoStore.addPhotoListener(listeningForReceiptId, this);

			if (fileIdString != null) {
				// Have the PhotoStore load the image, and it will notify any
				// listeners (including us) when it has arrived
				photoStore
						.readPhoto(listeningForReceiptId, fileIdString, false);
			}
		}
	}

	@Override
	public void drawableAvailable(Drawable d, int receiptId, String fileIdString) {
		if (d == null) {
			RBuddyApp app = RBuddyApp.sharedInstance();
			d = app.context().getResources()
					.getDrawable(android.R.drawable.ic_menu_gallery);
		}
		imageView.setImageDrawable(d);
	}

	private ImageView imageView;
	private int listeningForReceiptId;
}
