package com.js.form;

import java.util.*;

import com.js.json.*;

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
			FormWidget item = FormWidget.build(this, attributes);
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

			for (FormWidget w : fieldsList) {
				layout.addView(w.getView());
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
	public void setValue(String fieldName, Object value, boolean notifyListeners) {
		getField(fieldName).setValue(value.toString(), notifyListeners);
	}

	public FormWidget getField(String widgetName) {
		FormWidget field = itemsMap.get(widgetName);
		if (field == null)
			throw new IllegalArgumentException("no widget found with name "
					+ widgetName);
		return field;
	}

	/**
	 * Notify listeners that form values have changed
	 */
	public void valuesChanged() {
		for (Listener listener : mListeners) {
			listener.valuesChanged(this);
		}
	}

	public static interface Listener {
		void valuesChanged(Form form);
	}

	public void addListener(Listener listener) {
		mListeners.add(listener);
	}

	public void removeListener(Listener listener) {
		mListeners.remove(listener);
	}

	private Set<Listener> mListeners = new HashSet();
	private List<FormWidget> fieldsList = new ArrayList();
	private View layout;
	private Map<String, FormWidget> itemsMap = new HashMap();
}
