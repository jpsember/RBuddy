package js.form;

import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import static js.basic.Tools.*;

public class FormButtonWidget extends FormWidget {

	public FormButtonWidget(FormField owner) {
		super(owner);

		label = new TextView(context());
		label.setText(getLabel());
		label.setLayoutParams(FormWidget.LAYOUT_PARMS);

		button = new ImageButton(context());
		button.setLayoutParams(FormWidget.LAYOUT_PARMS);

		unimp("need some way to refer to drawables within JSON string");
		unimp("add attributes to have variants: Button, ImageButton, Button with android:drawableLeft attribute");
		
		button.setImageResource(android.R.drawable.ic_menu_gallery);

		layout.addView(label);
		layout.addView(button);
	}

	@Override
	public void setOnClickListener(OnClickListener listener) {
		button.setOnClickListener(listener);
	}
	

	private TextView label;
	private ImageButton button;
}
