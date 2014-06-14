package js.form;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import static js.basic.Tools.*;

abstract class FormWidget {

	public static final LayoutParams LAYOUT_PARMS = new LayoutParams(
			LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

	public FormWidget(FormField owner) {
		this.owner = owner;
		this.layout = new LinearLayout(context());
		layout.setLayoutParams(LAYOUT_PARMS);
		layout.setOrientation(LinearLayout.VERTICAL);
	}

	public Context context() {
		return getOwner().getOwner().context();
	}

	/**
	 * return LinearLayout containing this widget's view elements
	 */
	public View getView() {
		return layout;
	}

	/**
	 * Get value of widget as string; should be overridden
	 */
	public String getValue() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Set value as string (parse as appropriate); should be overridden
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		throw new UnsupportedOperationException();
	}

	protected FormField getOwner() {
		return owner;
	}

	public String getLabel() {
		unimp("support 'name' attribute, with link to resource strings file");
		return owner.strArg("label", owner.getId());
	}

	protected void constructLabel() {
		String labelText = getLabel();
		if (!labelText.isEmpty()) {
		label = new TextView(context());
		label.setText(labelText);
		label.setLayoutParams(FormWidget.LAYOUT_PARMS);
		}
	}
	
	public void setOnClickListener(OnClickListener listener) {
		throw new UnsupportedOperationException();
	}
	
	private FormField owner;
	protected View view;
	protected LinearLayout layout;
	protected TextView label;
}
