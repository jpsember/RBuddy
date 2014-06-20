package js.form;

import java.util.Map;

import static js.basic.Tools.*;
import js.rbuddyapp.RBuddyApp;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class FormImageWidget extends FormWidget implements IDrawableListener {

	public FormImageWidget(Form owner, Map attributes) {
		super(owner, attributes);

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
		drawableArrived(d);
	}

	@Override
	public void setDrawableProvider(IDrawableProvider p) {
		if (drawableProvider != p) {
			this.drawableProvider = p;
			updatePhotoView();
		}
	}

	@Override
	public void drawableArrived(Drawable d) {
		if (db)
			pr("FormImageWidget.drawableArrived: " + d + "\n  currently: "
					+ imageView.getDrawable() + "\n" + stackTrace(0, 5));

		if (d == null) {
			RBuddyApp app = RBuddyApp.sharedInstance();
			d = app.context().getResources()
					.getDrawable(android.R.drawable.ic_menu_gallery);
		}
		imageView.setImageDrawable(d);

	}

	private IDrawableProvider drawableProvider;
	private ImageView imageView;
}
