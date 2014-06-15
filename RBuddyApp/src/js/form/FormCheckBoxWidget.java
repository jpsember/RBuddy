package js.form;

import static js.basic.Tools.*;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FormCheckBoxWidget extends FormWidget {

	public FormCheckBoxWidget(FormField owner) {
		super(owner);

		checkBox = new CheckBox(context());

		// Construct a new ViewGroup that contains the checkbox on the left and
		// a label on the right

		LinearLayout horizontalPanel = new LinearLayout(context());
		ViewGroup ourContainer = horizontalPanel;
		horizontalPanel.setLayoutParams(LAYOUT_PARMS);
		horizontalPanel.setOrientation(LinearLayout.HORIZONTAL);

		final int AUX_WIDTH = 60;
		unimp("figure out device-independent way of choosing aux panel width");
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(AUX_WIDTH,
				LayoutParams.WRAP_CONTENT);
		p.weight = 0;
		p.gravity = Gravity.BOTTOM;
		horizontalPanel.addView(checkBox, p);

		String label = owner.strArg("label", owner.getId());
		buildLabel(label, ourContainer);

		getWidgetContainer().addView(ourContainer);
	}

	private void buildLabel(String labelText, ViewGroup container) {
		TextView label = new TextView(context());
		label.setText(labelText);
		label.setLayoutParams(FormWidget.LAYOUT_PARMS);

		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		p.weight = 1.0f;
		p.gravity = Gravity.BOTTOM;

		container.addView(label, p);
	}

	private CheckBox checkBox;
}
