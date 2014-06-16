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
			String id = item.getId();
			if (!id.isEmpty()) {
				Object prev = itemsMap.put(item.getId(), item);
				if (prev != null)
					throw new IllegalArgumentException("form field id "
							+ item.getId() + " already exists");
			}
			fieldsList.add(item);
		}
		json.exit();
	}

	@Override
	public void encode(JSONEncoder encoder) {
		throw new UnsupportedOperationException();
	}

	public View getView() {
		if (layout == null) {
			LinearLayout layout = new LinearLayout(context);
			this.layout = layout;
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			for (FormField fi : fieldsList) {
				layout.addView(fi.getWidget().getOuterContainer());
			}
		}
		return layout;
	}

	/**
	 * Get value from a form field
	 */
	public String getValue(String fieldName) {
		return getField(fieldName).getValue();
	}

	/**
	 * Write value to form field
	 */
	public void setValue(String fieldName, Object value) {
		getField(fieldName).setValue(value.toString());
	}

	public FormWidget getField(String fieldName) {
		FormField field = itemsMap.get(fieldName);
		if (field == null)
			throw new IllegalArgumentException("no field found with name "
					+ fieldName);
		return field.getWidget();
	}

	private List<FormField> fieldsList = new ArrayList();
	private View layout;
	private Map<String, FormField> itemsMap = new HashMap();
}
