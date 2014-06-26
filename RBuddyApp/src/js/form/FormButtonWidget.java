package js.form;

import static js.basic.Tools.*;

import java.util.Map;

import js.rbuddyapp.IPhotoListener;
import js.rbuddyapp.IPhotoStore;
import js.rbuddyapp.RBuddyApp;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class FormButtonWidget extends FormWidget implements IPhotoListener {

	public FormButtonWidget(Form owner, Map attributes) {
		super(owner, attributes);
		String button_icon = strAttr("icon", "");
		String button_label = strAttr("label", "");
		this.withImage = boolAttr("hasimage", false);
		if (button_icon.isEmpty()) {
			if (button_label.isEmpty()) {
				// Use id as fallback
				button_label = getId();
			}
			hasLabel = true;
			Button b = new Button(context());
			b.setText(button_label);
			button = b;
		} else {
			hasIcon = true;
			int resourceId = RBuddyApp.sharedInstance()
					.getResource(button_icon);
			Drawable img = context().getResources().getDrawable(resourceId);

			if (button_label.isEmpty()) {
				ImageButton ib = new ImageButton(context());
				ib.setImageDrawable(img);
				button = ib;
			} else {
				Button b = new Button(context());
				b.setText(button_label);
				b.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);

				button = b;
				hasLabel = true;
			}
		}

		if (withImage) {
			LinearLayout parentView = new LinearLayout(context());

			// What does 'match parent' mean for layout parameters?
			parentView.setLayoutParams(LAYOUT_PARMS);

			parentView.setOrientation(LinearLayout.HORIZONTAL);
			getWidgetContainer().addView(parentView);

			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f);
			p.gravity = Gravity.BOTTOM;
			parentView.addView(button, p);
			// setDebugBgnd(parentView, "#220000");

			imageView = new ImageView(context());
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			imageView.setAdjustViewBounds(true);

			// TODO Issue #31
			int THUMBNAIL_HEIGHT = 150;
			int THUMBNAIL_WIDTH = (int) (THUMBNAIL_HEIGHT * 1.0);

			p = new LinearLayout.LayoutParams(THUMBNAIL_WIDTH,
					THUMBNAIL_HEIGHT, 0.2f);
			parentView.addView(imageView, p);
			// setDebugBgnd(imageView, "#004400");

		} else {
			getWidgetContainer().addView(button);
		}

	}

	@Override
	protected void setChildWidgetsEnabled(boolean enabled) {
		button.setEnabled(enabled);
	}

	@Override
	public void setOnClickListener(OnClickListener listener) {
		button.setOnClickListener(listener);
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
			if (listeningForPhotoId != null
					&& !fileIdString.equals(listeningForPhotoId)) {
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

	private String listeningForPhotoId;

	// private IDrawableProvider drawableProvider;

	// Protected to avoid warnings for the moment
	protected boolean hasIcon;
	protected boolean hasLabel;
	private boolean withImage;
	private View button;
	private ImageView imageView;
}
