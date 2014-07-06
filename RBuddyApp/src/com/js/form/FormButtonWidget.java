package com.js.form;

import java.util.Map;

import com.js.android.App;
import com.js.android.IPhotoListener;
import com.js.android.IPhotoStore;

import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import static com.js.android.Tools.*;

public class FormButtonWidget extends FormWidget implements IPhotoListener {

	public static final Factory FACTORY = new FormWidget.Factory() {

		@Override
		public String getName() {
			return "button";
		}

		@Override
		public FormWidget constructInstance(Form owner, Map attributes) {
			return new FormButtonWidget(owner, attributes);
		}
	};

	public FormButtonWidget(Form owner, Map attributes) {
		super(owner, attributes);
		String button_icon = strAttr("icon", "");
		String button_label = strAttr("label", "");
		this.mWithImage = boolAttr("hasimage", false);
		if (button_icon.isEmpty()) {
			if (button_label.isEmpty()) {
				// Use id as fallback
				button_label = getId();
			}
			Button b = new Button(context());
			b.setText(button_label);
			mButton = b;
		} else {
			int resourceId = App.sharedInstance().getResource(button_icon);
			Drawable img = context().getResources().getDrawable(resourceId);

			if (button_label.isEmpty()) {
				ImageButton ib = new ImageButton(context());
				ib.setImageDrawable(img);
				mButton = ib;
			} else {
				Button b = new Button(context());
				b.setText(button_label);
				b.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
				mButton = b;
			}
		}

		if (mWithImage) {
			mImageVariant = IPhotoStore.Variant.THUMBNAIL;
			LinearLayout parentView = new LinearLayout(context());

			// What does 'match parent' mean for layout parameters?
			parentView.setLayoutParams(LAYOUT_PARMS);

			parentView.setOrientation(LinearLayout.HORIZONTAL);
			getWidgetContainer().addView(parentView);

			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f);
			p.gravity = Gravity.BOTTOM;
			parentView.addView(mButton, p);

			mImageView = new ImageView(context());
			mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			mImageView.setAdjustViewBounds(true);

			int THUMBNAIL_WIDTH = App
					.truePixels(IPhotoStore.THUMBNAIL_HEIGHT * 1.0f);

			p = new LinearLayout.LayoutParams(THUMBNAIL_WIDTH,
					App.truePixels(IPhotoStore.THUMBNAIL_HEIGHT), 0.2f);
			parentView.addView(mImageView, p);
		} else {
			getWidgetContainer().addView(mButton);
		}

	}

	@Override
	protected void setChildWidgetsEnabled(boolean enabled) {
		mButton.setEnabled(enabled);
	}

	@Override
	public void setOnClickListener(OnClickListener listener) {
		mButton.setOnClickListener(listener);
	}

	/**
	 * Display a particular photo in the image view
	 * 
	 * @param photoStore
	 *            source of photos
	 * @param ownerId
	 *            owner id, or zero if none assigned
	 * @param photoId
	 *            id of photo for owner, or null if none; ignored if owner id is
	 *            zero
	 */
	public void displayPhoto(IPhotoStore photoStore, int ownerId, String photoId) {
		if (ownerId == 0)
			photoId = null;

		// If owner id is changing, replace old listener with new
		if (mOwnerIdBeingListenedTo != ownerId) {
			photoStore.removePhotoListener(mOwnerIdBeingListenedTo,
					mImageVariant, this);
			mOwnerIdBeingListenedTo = ownerId;
			if (ownerId != 0)
				photoStore.addPhotoListener(ownerId, mImageVariant, this);
		}

		if (photoId == null) {
			clearPhoto();
		} else {
			photoStore.readPhoto(ownerId, photoId, mImageVariant);
		}
	}

	private void clearPhoto() {
		setDrawable(null);
	}

	private void setDrawable(Drawable d) {
		if (d == null) {
			d = getForm().context().getResources()
					.getDrawable(android.R.drawable.ic_menu_gallery);
		}
		mImageView.setImageDrawable(d);
	}

	// IPhotoListener interface
	//
	@Override
	public void drawableAvailable(Drawable d, int receiptId,
			IPhotoStore.Variant variant) {
		ASSERT(variant == mImageVariant);
		setDrawable(d);
	}

	private int mOwnerIdBeingListenedTo;
	private boolean mWithImage;
	private View mButton;
	private ImageView mImageView;
	private IPhotoStore.Variant mImageVariant;
}
