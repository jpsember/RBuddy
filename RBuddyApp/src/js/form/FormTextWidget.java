package js.form;

import android.text.InputType;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
//import static js.basic.Tools.*;

public class FormTextWidget extends FormWidget {

	public FormTextWidget(FormField owner) {
		super(owner);

		label = new TextView(context());
		label.setText(getLabel());
		label.setLayoutParams(FormWidget.LAYOUT_PARMS);

		constructInput();
		input.setLayoutParams(FormWidget.LAYOUT_PARMS);

		layout.addView(label);
		layout.addView(input);

	}

	protected String getAutoCompletionType() {
		return getOwner().strArg("autocompletion", "none");
	}

	protected void constructInput() {

		String autoCompletion = getAutoCompletionType();
		if (autoCompletion.equals("none")) {
			input = new EditText(context());
		} else if (autoCompletion.equals("single")) {
			input = new AutoCompleteTextView(context());
		} else if (autoCompletion.equals("multiple")) {
			input = new MultiAutoCompleteTextView(context());
		} else
			throw new IllegalArgumentException(
					"unsupported autocompletion type: " + autoCompletion);

		input.setImeOptions(EditorInfo.IME_ACTION_DONE);

		int inputType = InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
		if (getOwner().intArg("minlines", 1) > 1) {
			inputType |= InputType.TYPE_TEXT_FLAG_MULTI_LINE;
		}
		input.setInputType(inputType);
		String hint = getOwner().strArg("hint", "");
		if (!hint.isEmpty())
			input.setHint(hint);
		input.setMinLines(getOwner().intArg("minlines", 1));
		
		// When this view loses focus, immediately attempt to parse (and
		// possibly correct) the user's input
		input.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v0, boolean hasFocus) {
				if (!hasFocus)
					setValue(input.getText().toString());
			}
		});

	}

	@Override
	public void setValue(String value) {
		input.setText(value);

		// Reposition the cursor to the end of the text, in case we've just
		// rotated the device
		input.setSelection(input.getText().length());
	}

	@Override
	public String getValue() {
		return input.getText().toString();
	}

	private TextView label;
	protected EditText input;
}
