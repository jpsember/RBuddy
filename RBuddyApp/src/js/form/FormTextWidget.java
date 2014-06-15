package js.form;

import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;

public class FormTextWidget extends FormWidget {

	public FormTextWidget(FormField owner) {
		super(owner);

		constructInput();

		// Enable all the widgets in this form; this is basically the auxilliary
		// checkbox enable logic.
		input.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setEnabled(true);
			}
		});

		input.setLayoutParams(FormWidget.LAYOUT_PARMS);

		constructLabel();
		getWidgetContainer().addView(input);
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
				if (!hasFocus) {
					losingFocus();
				}
			}
		});

	}

	protected void losingFocus() {
		setValue(input.getText().toString());
	}

	protected void setChildWidgetsEnabled(boolean enabled) {
		input.setEnabled(enabled);
	}

	@Override
	public void updateUserValue(String value) {
		setInputText(value);
	}

	@Override
	public String parseUserValue() {
		return input.getText().toString();
	}

	protected void setInputText(String s) {
		input.setText(s);
		// Reposition the cursor to the end of the text, in case we've just
		// rotated the device
		input.setSelection(input.getText().length());
	}

	protected EditText input;
}
