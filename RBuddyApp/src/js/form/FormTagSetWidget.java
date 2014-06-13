package js.form;

import static js.basic.Tools.*;
import js.rbuddy.RBuddyApp;
import js.rbuddy.TagSet;
import js.rbuddy.TagSetFile;
import android.text.InputType;
import android.text.method.TextKeyListener;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.MultiAutoCompleteTextView.Tokenizer;

public class FormTagSetWidget extends FormTextWidget {
	public FormTagSetWidget(FormItem item) {
		super(item);
	}

	protected void constructInput() {
		MultiAutoCompleteTextView textView = new MultiAutoCompleteTextView(
				context());
		input = textView;
		textView.setInputType(InputType.TYPE_CLASS_TEXT);
		textView.setTokenizer(new OurTokenizer());
		textView.setKeyListener(TextKeyListener.getInstance(true,
				TextKeyListener.Capitalize.NONE));

		TagSetFile tf = RBuddyApp.sharedInstance().tagSetFile();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context(),
				android.R.layout.simple_dropdown_item_1line, tf.tagNamesList());
		textView.setAdapter(adapter);

		// When this view loses focus, immediately attempt to parse the
		// user's tags
		textView.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v0, boolean hasFocus) {
				if (!hasFocus)
					parseTags();
			}
		});
	}

	private void parseTags() {
		TagSet tagNameSet = null;
		String s = input.getText().toString();
		if (db)
			pr("  attempting to parse TagSet from " + s);
		try {
			tagNameSet = TagSet.parse(s);
		} catch (IllegalArgumentException e) {
			if (db)
				pr("Failed to parse " + s + ": " + e);
		}

		// Update the view's contents with either a 'cleaned up'
		// version of what the user entered,
		// or (if it didn't parse correctly), the receipt's tag set.
		//
		// If the former occurs, note that we'll be parsing the
		// view's text into a TagSet and
		// storing it as the new receipt tag set when we call
		// updateReceiptWithWidgetValues() later.
		//
		if (tagNameSet != null) {
			setTagSet(tagNameSet);
		} else {
			// Set to existing set, to update the text field
			setTagSet(this.tagNameSet);
		}
	}

	@Override
	public TagSet getTagSet() {
		return tagNameSet;
	}

	@Override
	public void setTagSet(TagSet s) {
		tagNameSet = s;
		if (input != null)
			input.setText(tagNameSet.format());
	}

	private TagSet tagNameSet = new TagSet();

	/**
	 * Tokenizer that recognizes both periods and commas as delimeters
	 */
	private static class OurTokenizer implements Tokenizer {

		private static boolean isDelim(char c) {
			return c == ',' || c == '.';
		}

		private static boolean isWhitesp(char c) {
			return c <= ' ';
		}

		@Override
		public int findTokenStart(CharSequence text, int cursor) {
			int i = cursor;
			while (i > 0 && !isDelim(text.charAt(i - 1))) {
				i--;
			}
			while (i < cursor && isWhitesp(text.charAt(i)))
				i++;
			return i;
		}

		@Override
		public int findTokenEnd(CharSequence text, int cursor) {
			int i = cursor;
			int len = text.length();

			while (i < len && !isDelim(text.charAt(i))) {
				i++;
			}
			while (i > cursor && isWhitesp(text.charAt(i - 1)))
				i--;
			return i;
		}

		@Override
		public CharSequence terminateToken(CharSequence text) {
			int i = text.length();

			while (i > 0 && isWhitesp(text.charAt(i - 1))) {
				i--;
			}
			if (i > 0 && isDelim(text.charAt(i - 1))) {
				return text;
			}

			return text + ", ";
		}
	}
}
