package js.rbuddy;

import android.content.Context;
import android.text.InputType;
import android.widget.EditText;

public class TagsEditText extends EditText {
	public TagsEditText(Context context) {
		super(context);

		setInputType(InputType.TYPE_CLASS_TEXT);
		setHint("Tags");
	}
}
