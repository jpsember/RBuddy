package js.form;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
//import static js.basic.Tools.*;

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

//	/**
//	 * sets the hint for the widget, method should be overriden in sub-class
//	 */
//	public void setHint(String hint) {
//	}
//
//	/**
//	 * sets the minimum number of lines (i.e. for TextView)
//	 */
//	public void setMinLines(int minLines) {
//	}

	protected FormField getOwner() {
		return owner;
	}
	
	public String getLabel() {
		return owner.strArg("label", owner.getName());
	}
	
//	private String widgetLabel;
//	public String getLabel() {
//		return widgetLabel;
//	}
	
	private FormField owner;
	protected View view;
	protected String _property;
//	protected String displayText;
	protected LinearLayout layout;
}
