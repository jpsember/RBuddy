package js.form;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import static js.basic.Tools.*;
import android.content.Context;
import android.widget.LinearLayout;
import js.json.*;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class Form implements IJSONEncoder {
	private Form(Context context) {
		this.context = context;
		unimp("rename FormItem -> FormField?");
	}

	private Context context;

	public Context context() {
		return context;
	}

	public static Form parse(Context context, String jsonString) {
		return parse(context, new JSONParser(jsonString));
	}

	public static Form parse(Context context, JSONParser json) {
		Form f = new Form(context);
		f.parse(json);
		return f;
	}

	private void parse(JSONParser json) {
		json.enterMap();

		while (json.hasNext()) {
			FormItem item = FormItem.parse(this, json);
			Object prev = itemsMap.put(item.getName(), item);
			if (prev != null)
				throw new IllegalArgumentException("form item named "
						+ item.getName() + " already exists");
		}
		json.exit();

	}

	@Override
	public void encode(JSONEncoder encoder) {
		throw new UnsupportedOperationException();
	}

	private static Set<FormItem> getOrderedFormItems(Map formItemsMap) {
		// Sort those items that have defined orders by those orders, and
		// add them;
		// then process the remaining
		Set<FormItem> orderedItems = new TreeSet(new Comparator() {
			@Override
			public int compare(Object arg0, Object arg1) {
				FormItem f0 = (FormItem) arg0;
				FormItem f1 = (FormItem) arg1;
				double diff = f0.getOrder() - f1.getOrder();
				if (diff != 0)
					return (int) Math.signum(diff);
				return f0.getName().compareTo(f1.getName());
			}
		});
		orderedItems.addAll(formItemsMap.values());
		return orderedItems;
	}

	public View getView() {
		if (layout == null) {
			LinearLayout layout = new LinearLayout(context);
			this.layout = layout;
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			for (FormItem fi : getOrderedFormItems(itemsMap)) {
				layout.addView(fi.getView());
			}
		}
		return layout;
	}

	/**
	 * Get value from a form field
	 */
	public String getValue(String fieldName) {
		return getField(fieldName).getWidget().getValue();
	}

	/**
	 * Write value to form field
	 */
	public void setValue(String fieldName, Object value) {
		getField(fieldName).getWidget().setValue(value.toString());
	}
	
	private FormItem getField(String fieldName) {
		FormItem field = itemsMap.get(fieldName);
		if (field == null)
			throw new IllegalArgumentException("no field found with name "
					+ fieldName);
		return field;
	}

	private View layout;
	private Map<String, FormItem> itemsMap = new HashMap();
}
