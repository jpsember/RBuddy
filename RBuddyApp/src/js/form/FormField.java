package js.form;

import java.util.Map;

import android.view.View.OnClickListener;

public class FormField {

	final static double ORDER_UNDEFINED = 1e20;

	public FormField(Form owner, Map attributes) {
		this.attributes = attributes;
		this.owner = owner;
	}

	public String getId() {
		return strArg("id", null);
	}

	public Form getOwner() {
		return owner;
	}

	public FormWidget getWidget() {
		if (widget == null) {
			String type = strArg("type", "***NONE SPECIFIED***");
			if (type.equals("text")) {
				widget = new FormTextWidget(this);
			} else if (type.equals("date")) {
				widget = new FormDateWidget(this);
			} else if (type.equals("tagset")) {
				widget = new FormTagSetWidget(this);
			} else if (type.equals("cost")) {
				widget = new FormCostWidget(this);
			} else if (type.equals("imagebutton")) {
				widget = new FormButtonWidget(this);
			} else
				throw new IllegalArgumentException("unsupported field type "
						+ type);
		}
		return widget;
	}

	private void dieIfNull(Object value, String key) {
		if (value == null)
			throw new IllegalArgumentException("missing argument: " + key);
	}

	protected double dblArg(String key, Number defaultValue) {
		Number num = (Number) attributes.get(key);
		if (num == null) {
			num = defaultValue;
		}
		dieIfNull(num, key);
		return num.doubleValue();
	}

	protected int intArg(String key, Number defaultValue) {
		Number num = (Number) attributes.get(key);
		if (num == null) {
			num = defaultValue;
		}
		dieIfNull(num, key);
		return num.intValue();
	}

	protected String strArg(String key, String defaultValue) {
		String val = (String) attributes.get(key);
		if (val == null)
			val = defaultValue;
		dieIfNull(val, key);
		return val;
	}

	public void setOnClickListener(OnClickListener onClickListener) {
		getWidget().setOnClickListener(onClickListener);
	}

	private Map attributes;
	private FormWidget widget;
	private Form owner;

}
