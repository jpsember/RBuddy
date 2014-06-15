package js.form;

import android.view.Gravity;
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
//		label.setLayoutParams(new LinearLayout.LayoutParams(
//				LayoutParams.WRAP_CONTENT, 150));
//		label.setLayoutParams(FormWidget.LAYOUT_PARMS);
		unimp("how do we set minimum height to something device-independent?");
		
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
//		p.weight = 1.0f;
//		p.gravity = Gravity.BOTTOM;
		getWidgetContainer().addView(horizontalSpace());
		getWidgetContainer().addView(label,p);
		getWidgetContainer().addView(horizontalSeparator());
	}
}
