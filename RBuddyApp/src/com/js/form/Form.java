package com.js.form;

import java.util.*;

import com.js.android.MyActivity;
import com.js.json.*;

import android.widget.LinearLayout;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class Form implements IJSONEncoder {

	private static FormWidget.Factory[] basicWidgets = {
			FormTextWidget.FACTORY, FormHeaderWidget.FACTORY,
			FormDateWidget.FACTORY, FormCostWidget.FACTORY,
			FormButtonWidget.FACTORY, FormImageWidget.FACTORY,
			FormCheckBoxWidget.FACTORY, };

	private Form(MyActivity activity) {
		this.mActivity = activity;
		for (int i = 0; i < basicWidgets.length; i++)
			registerWidget(basicWidgets[i]);
	}

	public void registerWidget(FormWidget.Factory factory) {
		mWidgetFactoryMap.put(factory.getName(), factory);
	}

	public MyActivity getActivity() {
		return mActivity;
	}

	public static Form parse(MyActivity activity, String jsonString,
			Set<FormWidget.Factory> widgetTypes) {
		return parse(activity, new JSONParser(jsonString), widgetTypes);
	}

	public static Form parse(MyActivity activity, JSONParser json,
			Set<FormWidget.Factory> widgetTypes) {
		Form f = new Form(activity);
		if (widgetTypes != null)
			for (FormWidget.Factory factory : widgetTypes) {
				f.registerWidget(factory);
			}
		f.parse(json);
		return f;
	}

	FormWidget.Factory getWidgetFactory(String name) {
		return mWidgetFactoryMap.get(name);
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
			LinearLayout layout = new LinearLayout(mActivity);
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

	private Map<String, FormWidget.Factory> mWidgetFactoryMap = new HashMap();
	private Set<Listener> mListeners = new HashSet();
	private List<FormWidget> fieldsList = new ArrayList();
	private View layout;
	private Map<String, FormWidget> itemsMap = new HashMap();
	private MyActivity mActivity;
}
