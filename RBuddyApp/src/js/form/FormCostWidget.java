package js.form;

import js.rbuddy.Cost;
import android.content.Context;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import static js.basic.Tools.*;
import android.widget.EditText;
import android.widget.TextView;

public class FormCostWidget extends FormTextWidget {
	public FormCostWidget(FormField owner) {
		super(owner);

		input.setFocusable(false);
		input.setClickable(true);
		input.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				processClick();
			}
		});

		input.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_NUMBER_FLAG_DECIMAL
				| InputType.TYPE_NUMBER_FLAG_SIGNED);

		// This makes pressing the 'done' keyboard key close the keyboard
		input.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				input.setFocusable(false);
				return false;
			}
		});
	}

	protected void losingFocus() {
		super.losingFocus();
		input.setFocusable(false);
		final boolean db = true;
		if (db)
			pr("\n\n"+this.getClass().getSimpleName()+" lost focus;\n"+focusInfo(input)+"\n\n");
	}

	private void processClick() {
		setEnabled(true);
		if (!input.isFocusableInTouchMode()) {
			input.setFocusableInTouchMode(true);
			input.requestFocus();
			input.setSelection(input.getText().length());

			// Keyboard is not appearing when input gets focus. This solution is
			// from:
			// http://stackoverflow.com/questions/5105354/how-to-show-soft-keyboard-when-edittext-is-focused
			//
			InputMethodManager imm = (InputMethodManager) context()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
		}
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
