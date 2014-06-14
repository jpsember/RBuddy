package js.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//import static js.basic.Tools.*;
import js.json.*;
import android.content.Context;
import android.widget.LinearLayout;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class Form implements IJSONEncoder {
	private Form(Context context) {
		this.context = context;
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
		json.enterList();
		while (json.hasNext()) {
			Map attributes = (Map) json.next();
			FormField item = new FormField(this, attributes);
			Object prev = itemsMap.put(item.getName(), item);
			if (prev != null)
				throw new IllegalArgumentException("form item named "
						+ item.getName() + " already exists");
			fieldsList.add(item);
			}
		json.exit();
	}

	@Override
	public void encode(JSONEncoder encoder) {
		throw new UnsupportedOperationException();
	}

//	private static Set<FormField> getOrderedFormItems(Map formItemsMap) {
//		// Sort those items that have defined orders by those orders, and
//		// add them;
//		// then process the remaining
//		Set<FormField> orderedItems = new TreeSet(new Comparator() {
//			@Override
//			public int compare(Object arg0, Object arg1) {
//				FormField f0 = (FormField) arg0;
//				FormField f1 = (FormField) arg1;
//				double diff = f0.getOrder() - f1.getOrder();
//				if (diff != 0)
//					return (int) Math.signum(diff);
//				return f0.getName().compareTo(f1.getName());
//			}
//		});
//		orderedItems.addAll(formItemsMap.values());
//		return orderedItems;
//	}

	public View getView() {
		if (layout == null) {
			LinearLayout layout = new LinearLayout(context);
			this.layout = layout;
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			for (FormField fi : fieldsList) {
				layout.addView(fi.getWidget().getView());
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

	private FormField getField(String fieldName) {
		FormField field = itemsMap.get(fieldName);
		if (field == null)
			throw new IllegalArgumentException("no field found with name "
					+ fieldName);
		return field;
	}

	private List<FormField> fieldsList = new ArrayList();
	private View layout;
	private Map<String, FormField> itemsMap = new HashMap();
}
