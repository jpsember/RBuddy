package js.form;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class FormTextWidget extends FormWidget {
	public FormTextWidget(FormItem item) {
		super(item);

		label = new TextView(context());
		label.setText(formItem.getName());
		label.setLayoutParams(FormWidget.LAYOUT_PARMS);

		constructInput();
		
		layout.addView(label);
		layout.addView(input);

	}

	protected void constructInput() {
		input = new EditText(context());
		input.setLayoutParams(FormWidget.LAYOUT_PARMS);
		input.setImeOptions(EditorInfo.IME_ACTION_DONE);
		input.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		input.setMinLines(1);
	}
	
	@Override
	public void setHint(String h) {
		input.setHint(h);
	}

	protected TextView label;
	protected EditText input;
}
