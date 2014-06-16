package js.form;

import java.util.Map;

import js.rbuddy.RBuddyApp;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class FormImageWidget extends FormWidget {

	public FormImageWidget(Form owner, Map attributes) {
		super(owner,attributes);

		imageView = new ImageView(context());
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
			RBuddyApp app = RBuddyApp.sharedInstance();
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
	private ImageView imageView;
}
