package js.form;

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

		// Construct a new container that contains the checkbox on the left and
		// a label on the right

		LinearLayout horizontalPanel = new LinearLayout(context());
		ViewGroup ourContainer = horizontalPanel;
		horizontalPanel.setLayoutParams(LAYOUT_PARMS);
		horizontalPanel.setOrientation(LinearLayout.HORIZONTAL);

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

	/**
	 * Set displayed value; subclasses should perform whatever translation / parsing is
	 * appropriate to convert the internal value to something displayed in the widget.
	 * 
	 * @param internalValue
	 */
	public void updateUserValue(String internalValue) {
		boolean checked = internalValue.equals("true");
		checkBox.setChecked(checked); 
	}

	/**
	 * Get displayed value, and transform to 'internal' representation.
	 */
	public String parseUserValue() {
		return checkBox.isChecked() ? "true" : "false";
	}
	
	@Override
	protected void setChildWidgetsEnabled(boolean enabled) {
		checkBox.setEnabled(enabled);
	}

	private CheckBox checkBox;
}
