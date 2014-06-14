package js.rbuddy;

import android.content.Context;
import android.text.InputType;
import android.text.method.TextKeyListener;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;

public class TagsEditText extends MultiAutoCompleteTextView {
	public TagsEditText(Context context) {
		super(context);

		setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		
		setTokenizer(new OurTokenizer());
		setKeyListener(TextKeyListener.getInstance(true,
				TextKeyListener.Capitalize.NONE));

		TagSetFile tf = RBuddyApp.sharedInstance().tagSetFile();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_dropdown_item_1line, tf.tagNamesList());
		setAdapter(adapter);

		setHint("Tags");
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
