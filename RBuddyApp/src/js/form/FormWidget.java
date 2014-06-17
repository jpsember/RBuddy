package js.form;

import java.util.Map;

import js.rbuddyapp.RBuddyApp;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import static js.basic.Tools.*;

public abstract class FormWidget {

	// TODO see issue #31
	static final int AUX_WIDTH = 60;

	public static final LayoutParams LAYOUT_PARMS = new LayoutParams(
			LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

	private static final int AUXPANEL_NONE = 0;
	private static final int AUXPANEL_EMPTY = 1;
	private static final int AUXPANEL_CHECKBOX = 2;
	private static String[] auxPanelTypeStrings = { "none", "empty", "checkbox" };

	/**
	 * Factory constructor
	 * 
	 * @param owner
	 * @param attributes
	 * @return an appropriate FormWidget
	 */
	static FormWidget build(Form owner, Map attributes) {
		FormWidget widget;
		String type = (String) attributes.get("type");
		if (type == null)
			throw new IllegalArgumentException("no type found");
		if (type.equals("text")) {
			widget = new FormTextWidget(owner, attributes);
		} else if (type.equals("header")) {
			widget = new FormHeaderWidget(owner, attributes);
		} else if (type.equals("date")) {
			widget = new FormDateWidget(owner, attributes);
		} else if (type.equals("tagset")) {
			widget = new FormTagSetWidget(owner, attributes);
		} else if (type.equals("cost")) {
			widget = new FormCostWidget(owner, attributes);
		} else if (type.equals("button")) {
			widget = new FormButtonWidget(owner, attributes);
		} else if (type.equals("imageview")) {
			widget = new FormImageWidget(owner, attributes);
		} else if (type.equals("checkbox")) {
			widget = new FormCheckBoxWidget(owner, attributes);
		} else
			throw new IllegalArgumentException("unsupported field type " + type);
		return widget;
	}

	protected String getId() {
		return strAttr("id", "");
	}

	protected Form getForm() {
		return form;
	}

	protected double dblAttr(String key, Number defaultValue) {
		return ((Number) attr(key, defaultValue)).doubleValue();
	}

	protected int intAttr(String key, Number defaultValue) {
		return ((Number) attr(key, defaultValue)).intValue();
	}

	protected String strAttr(String key, String defaultValue) {
		return (String) attr(key, defaultValue);
	}

	private Object attr(String key, Object defaultValue) {
		Object value = attributes.get(key);
		if (value == null)
			value = defaultValue;
		if (value == null)
			throw new IllegalArgumentException("missing argument: " + key);
		return value;
	}

	/**
	 * Package visibility, provided only for debugging
	 * 
	 * @return
	 */
	Map getAttributes() {
		return attributes;
	}

	protected FormWidget(Form form, Map attributes) {
		this.form = form;
		this.attributes = attributes;
		this.enabledState = true;
		this.widgetValue = "";

		LinearLayout verticalPanel = new LinearLayout(context());
		verticalPanel.setLayoutParams(LAYOUT_PARMS);
		verticalPanel.setOrientation(LinearLayout.VERTICAL);

		this.outerContainerView = verticalPanel;
		this.widgetContainerView = verticalPanel;

		int auxType = indexOfString(auxPanelTypeStrings, strAttr("aux", "none"));
		if (auxType != AUXPANEL_NONE) {

			// Auxilliary checkbox enable logic
			// ---------------------------------
			// Construct a new ViewGroup that contains the vertical panel on the
			// right, and an auxilliary panel on the left that may contain a
			// checkbox that determines whether the main widgets are enabled.
			// Note: the main widgets aren't actually disabled; instead,
			// they will display blank values instead of the actual 'internal'
			// widget value.

			LinearLayout horizontalPanel = new LinearLayout(context());
			horizontalPanel.setLayoutParams(LAYOUT_PARMS);
			horizontalPanel.setOrientation(LinearLayout.HORIZONTAL);
			this.outerContainerView = horizontalPanel;

			if (auxType == AUXPANEL_EMPTY) {
				View v = new View(context());
				LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
						AUX_WIDTH, 10);
				p.weight = 0;
				horizontalPanel.addView(v, p);
			} else if (auxType == AUXPANEL_CHECKBOX) {
				CheckBox cb = new CheckBox(context());
				auxCheckBox = cb;
				cb.setChecked(true);

				cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						setEnabled(isChecked);
					}
				});
				LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
						AUX_WIDTH, LayoutParams.WRAP_CONTENT);
				p.weight = 0;
				p.gravity = Gravity.BOTTOM;
				horizontalPanel.addView(cb, p);
			}
			horizontalPanel.addView(verticalPanel,
					new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT, 1.0f));
		}
	}

	public void setEnabled(boolean enabled) {
		// If the current state already matches the requested state, we're done
		if (enabled == this.enabledState) {
			return;
		}

		this.enabledState = enabled;

		// If an auxilliary checkbox exists, update its 'checked' state to agree
		// with the requested enable state. This will (probably) induce a
		// recursive call to the current method, since we're registered as a
		// listener

		if (auxCheckBox != null && auxCheckBox.isChecked() != enabled) {
			auxCheckBox.setChecked(enabled);
		}

		String internalValueToDisplay;
		if (!enabled) {
			widgetValue = this.parseUserValue();
			internalValueToDisplay = "";
		} else {
			internalValueToDisplay = widgetValue;
		}

		this.updateUserValue(internalValueToDisplay);

		// If there's no actual checkbox determining the enable state,
		// then adjust the widget's actual disable states
		if (auxCheckBox == null)
			setChildWidgetsEnabled(enabled);
	}

	/**
	 * Override this method to call setEnabled() on any android.widget elements
	 * within this FormWidget
	 * 
	 * @param enabled
	 */
	protected void setChildWidgetsEnabled(boolean enabled) {
	}

	/**
	 * Convenience method to return the containing Form's context
	 * 
	 * @return
	 */
	public Context context() {
		return getForm().context();
	}

	/**
	 * Get the view containing this widget. It is most likely a view that
	 * contains a number of child views, including an optional label, and one or
	 * more android widgets.
	 * 
	 * @return
	 */
	public View getView() {
		return outerContainerView;
	}

	/**
	 * Get the main view container; the one containing the widget views; i.e.,
	 * excluding the auxilliary view (if one exists)
	 * 
	 * @return
	 */
	protected ViewGroup getWidgetContainer() {
		return widgetContainerView;
	}

	/**
	 * Get value of widget as string
	 */
	public final String getValue() {
		if (!enabledState)
			return widgetValue;
		return parseUserValue();
	}

	/**
	 * Set value as string; calls setDisplayedValue(...) which subclasses should
	 * override to convert 'internal' string representation to user-displayable
	 * value
	 * 
	 * @param internalValue
	 */
	public final void setValue(String internalValue) {
		if (internalValue == null)
			throw new IllegalArgumentException("value must not be null");
		widgetValue = internalValue;
		updateUserValue(enabledState ? internalValue : "");
	}

	/**
	 * Set displayed value; subclasses should perform whatever translation /
	 * parsing is appropriate to convert the internal value to something
	 * displayed in the widget.
	 * 
	 * @param internalValue
	 */
	public void updateUserValue(String internalValue) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get displayed value, and transform to 'internal' representation.
	 */
	public String parseUserValue() {
		throw new UnsupportedOperationException();
	}

	public String getLabel() {
		String labelValue = strAttr("label", getId());
		return RBuddyApp.sharedInstance().applyStringSubstitution(labelValue);
	}

	/**
	 * Construct a label for this widget, which appears above the actual
	 * widget(s)
	 */
	protected void constructLabel() {
		String labelText = getLabel();
		if (!labelText.isEmpty()) {
			TextView label = new TextView(context());
			label.setText(labelText);
			label.setLayoutParams(FormWidget.LAYOUT_PARMS);
			getWidgetContainer().addView(label);
		}
	}

	public void setOnClickListener(OnClickListener listener) {
		throw new UnsupportedOperationException();
	}

	public void setDrawableProvider(IDrawableProvider p) {
		throw new UnsupportedOperationException();
	}

	// For lack of a better place
	public static View horizontalSeparator() {
		View v = new View(RBuddyApp.sharedInstance().context());
		// TODO Avoid using literal constant
		v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 4));
		// TODO Setting color like this doesn't work:
		// v.setBackgroundColor(color.holo_orange_light);
		// TODO Maybe using constant DKGRAY is inappropriate
		v.setBackgroundColor(Color.DKGRAY);
		return v;
	}

	// For lack of a better place
	public static View horizontalSpace() {
		View v = new View(RBuddyApp.sharedInstance().context());
		// TODO Avoid using literal constant
		v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 12));
		return v;
	}

	public static void setDebugBgnd(View view, String colorToParse) {
		view.setBackgroundColor(Color.parseColor(colorToParse));
	}

	/**
	 * Utility method for diagnosing focus problems
	 */
	static String focusInfo(View v) {
		return v.getClass().getSimpleName()
				+ (v.isEnabled() ? "" : " DISABLED")
				+ (v.isFocusable() ? " FOCUSABLE" : "")
				+ (v.isFocusableInTouchMode() ? " FOCUSINTOUCH" : "")
				+ (v.hasFocus() ? " HASFOCUS" : "")
				+ (v.isClickable() ? " CLICKABLE" : "");
	}

	// The form that owns this widget
	private Form form;

	// Attributes defining widget type, options
	private Map attributes;

	// This is the value this widget represents, if any. It is an 'internal'
	// string representation of the value; for example, all dates are
	// stored using JSDate's internal representation yyyy-mm-dd, and are
	// transformed to an external representation based on the user's locale
	// when displayed in the TextView.
	private String widgetValue;

	private ViewGroup outerContainerView;
	private ViewGroup widgetContainerView;

	// If not null, this is user's control of the enable state; it sits in the
	// auxilliary panel
	private CheckBox auxCheckBox;

	// Indicates enabled state of this widget's controls; even if false, the
	// contained android.widgets may still be enabled, e.g. if auxCheckBox !=
	// null. This is all to allow a user to start editing
	// a 'disabled' element and automatically turn this checkbox on.
	private boolean enabledState;
}
