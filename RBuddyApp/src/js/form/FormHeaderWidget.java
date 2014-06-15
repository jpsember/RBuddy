package js.form;

import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import static js.basic.Tools.*;

public class FormHeaderWidget extends FormWidget {

	public FormHeaderWidget(FormField owner) {
		super(owner);

		String headerText = owner.strArg("label", "--------------");

		TextView label = new TextView(context());
		label.setText(headerText);
		unimp("how do we set minimum height to something device-independent?");

		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		getWidgetContainer().addView(horizontalSpace());
		getWidgetContainer().addView(label, p);
		getWidgetContainer().addView(horizontalSeparator());
	}
}
