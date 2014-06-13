package js.form;

import static js.basic.Tools.*;

import java.util.Map;

import js.rbuddy.Cost;

import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class FormCostWidget extends FormTextWidget {
	public FormCostWidget(FormItem item, Map arguments) {
		super(item, arguments);

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

		// When this view loses focus, immediately attempt to parse the user's
		// text;
		// if it fails, clear the field (and hence clear the cost to 0)

		// This may not work very well in practice; see, e.g.,
		// http://stackoverflow.com/questions/10627137/how-can-i-know-when-a-edittext-lost-focus
		input.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					parseUserInput();
				}
			}
		});

	}

	private void parseUserInput() {
		Cost c = new Cost(0);
		String s = input.getText().toString();
		try {
			c = new Cost(s);
		} catch (NumberFormatException e) {
			warning("Failed to parse " + s + "; clearing");
		}
		input.setText(c.toString());
	}

}
