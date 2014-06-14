package js.form;

import js.rbuddy.R;
import js.rbuddy.RBuddyApp;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import static js.basic.Tools.*;

public class FormImageWidget extends FormWidget {

	public FormImageWidget(FormField owner) {
		super(owner);

		app = RBuddyApp.sharedInstance();
//		String labelText = getLabel();
//		if (!labelText.isEmpty()) {
//		label = new TextView(context());
//		label.setText(labelText);
//		label.setLayoutParams(FormWidget.LAYOUT_PARMS);
//		}
		
		unimp("need some way to specify which image is to be displayed");
		// constructInput();
		// input.setLayoutParams(FormWidget.LAYOUT_PARMS);

		imageView = new ImageView(owner.getOwner().context());
		LayoutParams lp;
		if (true) {
			lp = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			warning("still not sure what the deal is here");
			imageView.setLayoutParams(FormWidget.LAYOUT_PARMS);
		} else {
			warning("trying adaptive layout size");
			// Give photo a fixed size that is small, but lots of weight to
			// grow to take up what extra there is (horizontally)
			lp = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 120, 1.0f);
		}
unimp("Wow do we make photo expand to fill parent? Weight seems to be ignored.");

		updatePhotoView();

		constructLabel();
		if (label != null)
			layout.addView(label);
		layout.addView(imageView, lp);
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
