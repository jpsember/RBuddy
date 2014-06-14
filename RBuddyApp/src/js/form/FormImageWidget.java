package js.form;

import js.rbuddy.RBuddyApp;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
//import static js.basic.Tools.*;

public class FormImageWidget extends FormWidget {

	public FormImageWidget(FormField owner) {
		super(owner);

		app = RBuddyApp.sharedInstance();

		imageView = new ImageView(owner.getOwner().context());
		imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		imageView.setAdjustViewBounds(true);

		updatePhotoView();

		constructLabel();
		getWidgetContainer().addView(imageView);
	}

	private void updatePhotoView() {
		Drawable d = null;
		if (drawableProvider != null)
			d = drawableProvider.getDrawable();
		if (d == null) {
			d = app.context().getResources()
					.getDrawable(android.R.drawable.ic_menu_gallery);
		}
		imageView.setImageDrawable(d);
	}

	@Override
	public void setDrawableProvider(FormDrawableProvider p) {
		if (drawableProvider != p) {
			this.drawableProvider = p;
			updatePhotoView();
		}
	}

	private FormDrawableProvider drawableProvider;
	private RBuddyApp app;
	private ImageView imageView;
}
