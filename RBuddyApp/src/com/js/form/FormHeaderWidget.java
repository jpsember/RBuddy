package com.js.form;

import java.util.Map;

import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FormHeaderWidget extends FormWidget {

	public static final Factory FACTORY = new FormWidget.Factory() {

		@Override
		public String getName() {
			return "header";
		}

		@Override
		public FormWidget constructInstance(Form owner, Map attributes) {
			return new FormHeaderWidget(owner, attributes);
		}
	};

	public FormHeaderWidget(Form owner, Map attributes) {
		super(owner, attributes);

		String headerText = strAttr("label", "");

		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		getWidgetContainer().addView(horizontalSpace(context()));

		if (!headerText.isEmpty()) {
			TextView label = new TextView(context());
			label.setText(headerText);
			getWidgetContainer().addView(label, p);
		}

		getWidgetContainer().addView(horizontalSeparator(context()));
	}

	protected String getId() {
		return "";
	}

}
