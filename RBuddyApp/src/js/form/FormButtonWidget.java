package js.form;

//import static js.basic.Tools.*;
import java.util.Map;

import js.rbuddy.RBuddyApp;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class FormButtonWidget extends FormWidget {

	public FormButtonWidget(Form owner, Map attributes) {
		super(owner,attributes);
		String button_icon = strAttr("icon", "");
		String button_label = strAttr("label", "");
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

		getWidgetContainer().addView(button);
	}

	@Override
	protected void setChildWidgetsEnabled(boolean enabled) {
		button.setEnabled(enabled);
	}

	@Override
	public void setOnClickListener(OnClickListener listener) {
		button.setOnClickListener(listener);
	}

	// Protected to avoid warnings for the moment
	protected boolean hasIcon;
	protected boolean hasLabel;

	private View button;
}
