package js.form;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

abstract class FormWidget {

	public static final LayoutParams LAYOUT_PARMS = new LayoutParams(
			LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

	public FormWidget(FormItem item) {
		this.formItem = item;
		this.layout = new LinearLayout(context());
		layout.setLayoutParams(LAYOUT_PARMS);
		layout.setOrientation(LinearLayout.VERTICAL);

		displayText = item.getName();
	}

	public Context context() {
		return formItem.owner().context();
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

	/**
	 * sets the hint for the widget, method should be overriden in sub-class
	 */
	public void setHint(String hint) {
	}

	/**
	 * sets the minimum number of lines (i.e. for TextView)
	 */
	public void setMinLines(int minLines) {
	}

//	/**
//	 * Parse JSDate
//	 * 
//	 * @return JSDate, or null if parsing failed
//	 */
//	public JSDate getDate() {
//		throw new UnsupportedOperationException();
//	}

//	public TagSet getTagSet() {
//		throw new UnsupportedOperationException();
//	}
//
//	public void setTagSet(TagSet s) {
//		throw new UnsupportedOperationException();
//	}

	protected FormItem formItem;
	protected View view;
	protected String _property;
	protected String displayText;
	protected LinearLayout layout;
}
