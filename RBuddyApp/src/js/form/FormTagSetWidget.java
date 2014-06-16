package js.form;

//import static js.basic.Tools.*;

import js.rbuddy.RBuddyApp;
import js.rbuddy.TagSet;
import js.rbuddy.TagSetFile;
import android.text.method.TextKeyListener;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.MultiAutoCompleteTextView.Tokenizer;

public class FormTagSetWidget extends FormTextWidget {
	
	public FormTagSetWidget(FormField owner) {
		super(owner);
	}

	protected String getAutoCompletionType() {
		return "multiple";
	}

	protected void constructInput() {
		super.constructInput();
		
		// The input view is always MultiAutoCompleteTextView, because we overrode
		// getAutoCompletionType() to return "multiple" 
		MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView) input;

		input.setHint(getOwner().strArg("hint", "tags"));
		
		textView.setTokenizer(new OurTokenizer());
		textView.setKeyListener(TextKeyListener.getInstance(true,
				TextKeyListener.Capitalize.NONE));

		TagSetFile tf = RBuddyApp.sharedInstance().tagSetFile();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context(),
				android.R.layout.simple_dropdown_item_1line, tf.tagNamesList());
		textView.setAdapter(adapter);
	}

	@Override
	protected int getFocusType() {
		return FOCUS_RESISTANT;
	}

	@Override
	public void updateUserValue(String internalValue) {
		TagSet ts = TagSet.parse(internalValue,new TagSet());
		setInputText(ts.toString());
	}

	@Override
	public String parseUserValue() {
		TagSet ts = TagSet.parse(input.getText().toString(),new TagSet());
		return ts.toString();
	}
	
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
