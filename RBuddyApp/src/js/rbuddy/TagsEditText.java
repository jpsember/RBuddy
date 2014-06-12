package js.rbuddy;

import android.content.Context;
import android.text.InputType;
import android.text.method.TextKeyListener;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;

public class TagsEditText extends MultiAutoCompleteTextView {
	public TagsEditText(Context context) {
		super(context);

		setInputType(InputType.TYPE_CLASS_TEXT);
		setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		setKeyListener(TextKeyListener.getInstance(true,
				TextKeyListener.Capitalize.NONE));

		TagSetFile tf = RBuddyApp.sharedInstance().tagSetFile();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_dropdown_item_1line, tf.tagNamesList());
		setAdapter(adapter);

		setHint("Tags");
	}

}
