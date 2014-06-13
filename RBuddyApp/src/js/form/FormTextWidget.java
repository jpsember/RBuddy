package js.form;

import java.util.Map;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

public class FormTextWidget extends FormWidget {
	public FormTextWidget(FormItem item, Map arguments) {
		super(item);

		label = new TextView(context());
		label.setText(formItem.getName());
		label.setLayoutParams(FormWidget.LAYOUT_PARMS);

		constructInput(arguments);
		input.setLayoutParams(FormWidget.LAYOUT_PARMS);

		layout.addView(label);
		layout.addView(input);

	}

	protected void constructInput(Map arguments) {

		String autoCompletion = FormItem.strArg(arguments, "autocompletion",
				"none");
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
		input.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
	}

	@Override
	public void setHint(String h) {
		input.setHint(h);
	}

	@Override
	public void setMinLines(int minLines) {
		input.setMinLines(minLines);
	}

	protected TextView label;
	protected EditText input;
}
