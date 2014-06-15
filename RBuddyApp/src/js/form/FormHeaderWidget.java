package js.form;

import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FormHeaderWidget extends FormWidget {

	public FormHeaderWidget(FormField owner) {
		super(owner);

		String headerText = owner.strArg("label", "");

		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		getWidgetContainer().addView(horizontalSpace());

		if (!headerText.isEmpty()) {
			TextView label = new TextView(context());
			label.setText(headerText);
			getWidgetContainer().addView(label, p);
		}

		getWidgetContainer().addView(horizontalSeparator());
	}
}
