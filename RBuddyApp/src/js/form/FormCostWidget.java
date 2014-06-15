package js.form;

import js.rbuddy.Cost;
import android.text.InputType;
import android.view.KeyEvent;
//import static js.basic.Tools.*;
//import android.view.View;
//import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class FormCostWidget extends FormTextWidget {
	public FormCostWidget(FormField owner) {
		super(owner);

//		input.setFocusable(false);
//		input.setClickable(true);
		
		input.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_NUMBER_FLAG_DECIMAL
				| InputType.TYPE_NUMBER_FLAG_SIGNED);

//		input.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				final boolean db = true;
//				if (db)
//					pr(describe(v) + " onClick, setting focusable; currently "+input.isFocusable());
////				if (!input.isFocusable()) 
//				{
//					input.setFocusable(true);
//					if (db) pr(" requesting focus");
//					input.requestFocus();
//					input.setSelection(input.getText().length());
//				}
//			}
//		});

		
		// This makes pressing the 'done' keyboard key close the keyboard
		input.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
//				input.setFocusable(false);
				return false;
			}
		});

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
