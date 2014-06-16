package js.form;

import java.util.Map;

import js.rbuddy.Cost;
import android.text.InputType;

public class FormCostWidget extends FormTextWidget {
	public FormCostWidget(Form owner, Map attributes) {
		super(owner,attributes);

		input.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_NUMBER_FLAG_DECIMAL
				| InputType.TYPE_NUMBER_FLAG_SIGNED);

	}
	
	@Override
	protected int getFocusType() {
		return FOCUS_RESISTANT;
	}

	private String parseCost(String string, String defaultIfParsingFails) {
		try {
			defaultIfParsingFails = new Cost(string).toString();
		} catch (NumberFormatException e) {
		}
		return defaultIfParsingFails;
	}

	@Override
	public void updateUserValue(String internalValue) {
		setInputText(parseCost(internalValue, input.getText().toString()));
	}

	@Override
	public String parseUserValue() {
		String val = parseCost(input.getText().toString(), "");
		return val;
	}

}
