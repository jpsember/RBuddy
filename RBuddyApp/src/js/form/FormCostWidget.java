package js.form;

import static js.basic.Tools.*;

import js.rbuddy.Cost;
import android.text.InputType;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

public class FormCostWidget extends FormTextWidget {
	public FormCostWidget(FormField owner) {
		super(owner);

		input.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_NUMBER_FLAG_DECIMAL
				| InputType.TYPE_NUMBER_FLAG_SIGNED);

		// This makes pressing the 'done' keyboard key close the keyboard
		input.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				return false;
			}
		});
	}

	private String parseCost(String string, String defaultIfParsingFails) {
		try {
			defaultIfParsingFails = new Cost(string).toString();
		} catch (NumberFormatException e) {
			warning("Failed to parse " + string);
		}
		return defaultIfParsingFails;
	}

	@Override
	public void setValue(String value) {
		input.setText(parseCost(value,input.getText().toString()));
	}
}
