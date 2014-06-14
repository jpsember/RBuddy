package js.form;

import js.rbuddy.RBuddyApp;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
//import static js.basic.Tools.*;

public class FormButtonWidget extends FormWidget {

	public FormButtonWidget(FormField owner) {
		super(owner);

		String button_icon = owner.strArg("button_icon", "");
		String button_label = owner.strArg("button_label", "");

		if (button_icon.isEmpty()) {
			if (button_label.isEmpty()) {
				throw new IllegalArgumentException(
						"no button label or icon defined; args "
								+ owner.getAttributes());
			}
			hasLabel = true;
			Button b = new Button(context());
			b.setText(button_label);
			button = b;
		} else {
			hasIcon = true;
			int resourceId = RBuddyApp.sharedInstance().getResource(button_icon);
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

		button.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		constructLabel();
		if (label != null)
			layout.addView(label);

		layout.addView(button);
	}

	@Override
	public void setOnClickListener(OnClickListener listener) {
		button.setOnClickListener(listener);
	}

	// Protected to avoid warnings for the moment
	protected boolean hasIcon;
	protected boolean hasLabel;

	private TextView label;
	private View button;
}
