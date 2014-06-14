package js.form;

import js.rbuddy.R;
import js.rbuddy.RBuddyApp;
import android.widget.ImageView;
import android.widget.TextView;
import static js.basic.Tools.*;

public class FormImageWidget extends FormWidget {

	public FormImageWidget(FormField owner) {
		super(owner);

		app = RBuddyApp.sharedInstance();

		unimp("need some way to specify which image is to be displayed");

		imageView = new ImageView(owner.getOwner().context());
		imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		imageView.setAdjustViewBounds(true);

		updatePhotoView();

		constructLabel();
		if (label != null)
			layout.addView(label);
		layout.addView(imageView); 
	}

	private void updatePhotoView() {
		if (imageView == null)
			return;
		imageView.setImageDrawable(app.context().getResources()
				.getDrawable(R.drawable.missingphoto_landscape));
	}

	private RBuddyApp app;
	private TextView label;
	private ImageView imageView;
}
