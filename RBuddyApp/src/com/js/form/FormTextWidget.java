package com.js.form;

import java.util.Map;

import android.content.Context;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import static com.js.basic.Tools.*;

public class FormTextWidget extends FormWidget {

	// Here is where we describe how a particular subclass of FormTextWidget
	// deals with keyboard focus. The default is FOCUS_NORMAL.
	protected static final int FOCUS_NEVER = 0;
	protected static final int FOCUS_RESISTANT = 1;
	protected static final int FOCUS_NORMAL = 2;
	public static final Factory FACTORY = new FormWidget.Factory() {

		@Override
		public String getName() {
			return "text";
		}

		@Override
		public FormWidget constructInstance(Form owner, Map attributes) {
			return new FormTextWidget(owner, attributes);
		}
	};

	private static String[] focusTypeStrings = { "never", "resistant", "normal" };

	public FormTextWidget(Form owner, Map attributes) {
		super(owner, attributes);

		this.focusType = getFocusType();

		constructInput();

		input.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				processClick();
			}
		});

		if (focusType <= FOCUS_RESISTANT) {
			input.setFocusable(false);
			input.setClickable(true);

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

		input.setLayoutParams(FormWidget.LAYOUT_PARMS);

		constructLabel();
		getWidgetContainer().addView(input);
	}

	protected void processClick() {
		setEnabled(true);
		if (focusType == FOCUS_RESISTANT) {
			if (!input.isFocusableInTouchMode()) {
				input.setFocusableInTouchMode(true);
				input.requestFocus();
				input.setSelection(input.getText().length());

				// Keyboard is not appearing when input gets focus. This
				// solution is
				// from:
				// http://stackoverflow.com/questions/5105354/how-to-show-soft-keyboard-when-edittext-is-focused
				//
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
			}
		}
	}

	protected int getFocusType() {
		String ft = strAttr("focus", "normal");
		return indexOfString(focusTypeStrings, ft);
	}

	protected String getAutoCompletionType() {
		return strAttr("autocompletion", "none");
	}

	protected void constructInput() {

		String autoCompletion = getAutoCompletionType();
		if (autoCompletion.equals("none")) {
			input = new EditText(getActivity());
		} else if (autoCompletion.equals("single")) {
			input = new AutoCompleteTextView(getActivity());
		} else if (autoCompletion.equals("multiple")) {
			input = new MultiAutoCompleteTextView(getActivity());
		} else
			throw new IllegalArgumentException(
					"unsupported autocompletion type: " + autoCompletion);

		input.setImeOptions(EditorInfo.IME_ACTION_DONE);

		int inputType = InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
		if (intAttr("minlines", 1) > 1) {
			inputType |= InputType.TYPE_TEXT_FLAG_MULTI_LINE;
		}
		input.setInputType(inputType);
		String hint = strAttr("hint", "");
		if (!hint.isEmpty())
			input.setHint(hint);
		input.setMinLines(intAttr("minlines", 1));

		// When this view loses focus, immediately attempt to parse (and
		// possibly correct) the user's input
		input.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v0, boolean hasFocus) {
				if (!hasFocus) {
					if (db)
						pr("FormTextWidget, setting value for new text: "
								+ input.getText());
					setValue(input.getText().toString());
					if (focusType <= FOCUS_RESISTANT) {
						input.setFocusable(false);
					}
				}
			}
		});
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

	private int focusType;
	protected EditText input;
}
