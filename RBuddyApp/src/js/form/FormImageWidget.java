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

	public void displayPhoto(String fileIdString) {
		final boolean db = true;
		if (db)
			pr(this + ".displayPhoto fileId=" + fileIdString + ", previous "
					+ listeningForPhotoId);
		IPhotoStore photoStore = RBuddyApp.sharedInstance().photoStore();
		if (fileIdString == null) {
			if (listeningForPhotoId != null) {
				photoStore.removePhotoListener(listeningForPhotoId, this);
				listeningForPhotoId = null;
			}
		} else {
			if (listeningForPhotoId != null) {
				if (listeningForPhotoId.equals(fileIdString))
					return;
				photoStore.removePhotoListener(listeningForPhotoId, this);
			}

			listeningForPhotoId = fileIdString;
			unimp("add parameter and support for thumbnails");
			photoStore.addPhotoListener(listeningForPhotoId, this);

			// Have the PhotoStore load the image
			photoStore.readPhoto(listeningForPhotoId);
		}
	}

	@Override
	public void drawableAvailable(Drawable d, String fileIdString) {
		if (db)
			pr("FormImageWidget.drawableAvailable, id " + fileIdString
					+ " drawable " + d);
		if (d == null) {
			RBuddyApp app = RBuddyApp.sharedInstance();
			d = app.context().getResources()
					.getDrawable(android.R.drawable.ic_menu_gallery);
		}
		imageView.setImageDrawable(d);
	}

	private ImageView imageView;
	private String listeningForPhotoId;
}
