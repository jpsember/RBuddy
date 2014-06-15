package js.form;

import js.rbuddy.RBuddyApp;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import static js.basic.Tools.*;

public abstract class FormWidget {

	public static final LayoutParams LAYOUT_PARMS = new LayoutParams(
			LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

	public FormWidget(FormField owner) {
		this.owner = owner;

		LinearLayout verticalPanel = new LinearLayout(context());
		verticalPanel.setLayoutParams(LAYOUT_PARMS);
		verticalPanel.setOrientation(LinearLayout.VERTICAL);

		this.outerContainerView = verticalPanel;
		this.widgetContainerView = verticalPanel;

		String auxPanelType = owner.strArg("aux", "");

		if (!auxPanelType.isEmpty()) {
			// Construct a new ViewGroup that contains the vertical panel on the
			// left, and an auxilliary panel on the right

			LinearLayout horizontalPanel = new LinearLayout(context());
			horizontalPanel.setLayoutParams(LAYOUT_PARMS);
			horizontalPanel.setOrientation(LinearLayout.HORIZONTAL);
			this.outerContainerView = horizontalPanel;


			final int AUX_WIDTH = 60;
			unimp("figure out device-independent way of choosing aux panel width");
			if (auxPanelType.equals("checkbox")) {
				CheckBox cb = new CheckBox(context());
				auxCheckBox = cb;
				LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
						AUX_WIDTH, LayoutParams.WRAP_CONTENT);
				p.weight = 0;
				p.gravity = Gravity.BOTTOM;
				horizontalPanel.addView(cb, p);
			} else if (auxPanelType.equals("empty")) {
				View v = new View(context());
				LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
						AUX_WIDTH, 10);
				p.weight = 0;
				horizontalPanel.addView(v, p);
			} else {
				throw new IllegalArgumentException("unknown aux panel type: "
						+ auxPanelType);
			}
			
			// WRAP_CONTENT should be thought of as the normal layout parameter;
			// use the weight to distribute
			// any extra.
			horizontalPanel.addView(verticalPanel,
					new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT, 1.0f));

		}

	}

	public Context context() {
		return getOwner().getOwner().context();
	}

	/**
	 * Get the view that contains this field's widget plus any auxilliary views
	 * 
	 * @return
	 */
	public View getOuterContainer() {
		return outerContainerView;
	}

	/**
	 * Get the field's main view container; the one containing the widget views
	 * 
	 * @return
	 */
	public ViewGroup getWidgetContainer() {
		return widgetContainerView;
	}

	/**
	 * Get value of widget as string; should be overridden
	 */
	public String getValue() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Set value as string (parse as appropriate); should be overridden
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		throw new UnsupportedOperationException();
	}

	protected FormField getOwner() {
		return owner;
	}

	public String getLabel() {
		String labelValue = owner.strArg("label", owner.getId());
		return RBuddyApp.sharedInstance().applyStringSubstitution(labelValue);
	}

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

	public void setDrawableProvider(FormDrawableProvider p) {
		throw new UnsupportedOperationException();
	}

	public CheckBox getAuxCheckBox() {return auxCheckBox;}
	
	
	// For lack of a better place
	public static View horizontalSeparator() {
		View v = new View(RBuddyApp.sharedInstance().context());
		unimp("how do we set 1dp here, instead of '4'?");
		v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,4));
//		v.setBackgroundColor(color.holo_orange_light);
		unimp("color isn't showing up");
		v.setBackgroundColor(Color.DKGRAY);
		return v;
	}
	
	// For lack of a better place
	public static View horizontalSpace() {
		View v = new View(RBuddyApp.sharedInstance().context());
		v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,12));
		return v;
	}

	public static void setDebugBgnd(View view, String colorToParse) {
		view.setBackgroundColor(Color.parseColor(colorToParse));
	}
	
	
	private FormField owner;
	private ViewGroup outerContainerView;
	private ViewGroup widgetContainerView;
	private CheckBox auxCheckBox;
}
