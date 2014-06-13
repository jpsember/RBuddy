package js.form;

import java.util.Map;

import android.view.View;
import js.basic.JSONParser;

public class FormItem {

	final static double ORDER_UNDEFINED = 1e20;

	private FormItem(Form owner, String name) {
		this.owner = owner;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static FormItem parse(Form owner, JSONParser json) {
		FormItem f = new FormItem(owner, json.nextKey());
		f.parseJSON(json);
		return f;
	}

	private void parseJSON(JSONParser json) {
		Map map = (Map) json.keyValue();

		// Process type first, since this constructs the widget
		{
			String type = str(map, "type", null);
			if (type == null)
				throw new IllegalArgumentException(
						"no type specified for form item");
			setType(type);
		}
		setOrder(dbl(map, "order", ORDER_UNDEFINED));

		widget.setHint(str(map, "hint", null));
	}

	private static String str(Map map, String key, String defaultValue) {
		String val = (String) map.get(key);
		if (val == null)
			val = defaultValue;
		return val;
	}

	private static double dbl(Map map, String key, double defaultValue) {
		Number num = (Number) map.get(key);
		if (num == null)
			return defaultValue;
		return num.doubleValue();
	}

	private void setType(String type) {
		if (type.equals("text")) {
			widget = new FormTextWidget(this);
		} else if (type.equals("date")) {
			widget = new FormDateWidget(this);
		} else if (type.equals("tagset")) {
			widget = new FormTagSetWidget(this);
		} else
			throw new IllegalArgumentException("unknown form item type: "
					+ type);
	}

	public View getView() {
		return widget.getView();
	}

	public Form owner() {
		return owner;
	}

	public void setOrder(double order) {
		this.order = order;
	}

	public double getOrder() {
		return order;
	}

	private String name;
	private FormWidget widget;
	private Form owner;
	private double order = ORDER_UNDEFINED;
}
