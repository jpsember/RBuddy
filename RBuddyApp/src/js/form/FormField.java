package js.form;

import java.util.Map;

// TODO The FormField could be joined with FormWidget.
class FormField {

	public FormField(Form owner, Map attributes) {
		this.attributes = attributes;
		this.owner = owner;
	}

	public String getId() {
		String id = strArg("id", "");
		if (id.isEmpty() && !(strArg("type", null).equals("header")))
			throw new IllegalArgumentException("no id for field");
		return id;
	}

	public Form getOwner() {
		return owner;
	}

	public FormWidget getWidget() {
		if (widget == null) {
			String type = strArg("type", "***NONE SPECIFIED***");
			if (type.equals("text")) {
				widget = new FormTextWidget(this);
			} else if (type.equals("header")) {
				widget = new FormHeaderWidget(this);
			} else if (type.equals("date")) {
				widget = new FormDateWidget(this);
			} else if (type.equals("tagset")) {
				widget = new FormTagSetWidget(this);
			} else if (type.equals("cost")) {
				widget = new FormCostWidget(this);
			} else if (type.equals("button")) {
				widget = new FormButtonWidget(this);
			} else if (type.equals("imageview")) {
				widget = new FormImageWidget(this);
			} else if (type.equals("checkbox")) {
				widget = new FormCheckBoxWidget(this);
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

	/**
	 * Package visibility, provided only for debugging
	 * 
	 * @return
	 */
	Map getAttributes() {
		return attributes;
	}

	private Map attributes;
	private FormWidget widget;
	private Form owner;
}
