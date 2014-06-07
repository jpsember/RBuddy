package js.rbuddy;

import static js.basic.Tools.*;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import js.basic.StringUtil;

public class Receipt {

	public static final int MAX_TAGS = 5;

	/**
	 * Constructor
	 * 
	 * Sets date to current date, string fields to the empty string, tags empty
	 * set
	 * 
	 * @throws IllegalArgumentException
	 *             if identifier <= 0
	 */
	public Receipt(int identifier) {
		if (identifier <= 0)
			throw new IllegalArgumentException();
		this.id = identifier;
		setDate(JSDate.currentDate());
		this.summary = "";
		this.tags = new TreeSet<String>();
	}

	public JSDate getDate() {
		return this.date;
	}

	public void setDate(JSDate date) {
		this.date = date;
	}

	public String getSummary() {
		return summary;
	}

	public static Receipt decode(CharSequence s) {
		String[] tokens = StringUtil.tokenize(s);
		boolean problem = true;
		Receipt r = null;
		do {
			if (tokens.length < 4)
				break;

			int f = 0;
			r = new Receipt(Integer.parseInt(tokens[f++]));
			r.date = JSDate.parse(tokens[f++]);
			r.summary = StringUtil.decode(tokens[f++]).toString();

			int nTags = Integer.parseInt(tokens[f++]);
			if (nTags < 0)
				break;

			if (f + nTags > tokens.length)
				break;

			Set<String> set = new TreeSet<String>();
			for (int i = 0; i < nTags; i++) {
				set.add(tokens[f++]);
			}
			r.setTags(set);

			if (f != tokens.length)
				break;
			problem = false;
		} while (false);
		if (problem)
			throw new IllegalArgumentException("problem decoding " + s);

		return r;
	}

	public StringBuilder encode() {
		return encode(null);
	}

	public StringBuilder encode(StringBuilder sb) {
		if (sb == null)
			sb = new StringBuilder();
		sb.append(getId());
		sb.append('|');
		sb.append(date.toString());
		sb.append('|');
		StringUtil.encode(getSummary(), sb);

		// Encode tags by preceding with number of tags
		sb.append('|');
		sb.append(tags.size());
		for (Iterator<String> it = tags.iterator(); it.hasNext();) {
			sb.append('|');
			StringUtil.encode(it.next(), sb);
		}
		return sb;
	}

	public void setSummary(String s) {

		s = s.trim();

		int state = 1;
		StringBuilder s_result = new StringBuilder();

		int i_pos;
		for (i_pos = 0; i_pos < s.length(); i_pos++) {

			char c_thischar = s.charAt(i_pos);

			boolean b_is_whitespace = (c_thischar <= ' ');

			switch (state) {

			// last character was not whitespace
			case 1:
				// but this one is...
				if (b_is_whitespace) {
					state = 2;
				} else {
					s_result.append(c_thischar);
					state = 1;
				}
				break;

			// last character was whitespace
			case 2:
				// and so is this one...
				if (b_is_whitespace) {
					state = 2;
				} else {
					s_result.append(' ');
					s_result.append(c_thischar);
					state = 1;
				}
				break;
			}

		}

		summary = s_result.toString();

	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Receipt " + encode();
	}

	/**
	 * For test purposes, build a random receipt
	 * 
	 * @return
	 */
	public static Receipt buildRandom(int id) {
		Receipt r = new Receipt(id);
		r.setDate(JSDate.buildRandom());
		r.setSummary(StringUtil.randomString(30));
		return r;
	}

	/**
	 * Get the set of tags
	 * 
	 * @return set of strings
	 */
	public Set<String> getTags() {
		return tags;
	}

	/**
	 * Used in setTags(); flushes tag being built, adds to set if nonempty;
	 * clears buffer
	 * 
	 * @param str
	 * @param set
	 */
	private static void flush(StringBuilder str, Set<String> set) {
		String s = str.toString();
		ASSERT(s.length() > 0);
		if (set.size() < MAX_TAGS) {
			set.add(s);
		}
		str.setLength(0);
	}

	/**
	 * Set tags by parsing a script of comma(or linefeed)-separated tags
	 * 
	 * @param s
	 */
	public void setTags(CharSequence s) {
		Set<String> set = new TreeSet<String>();
		int state = 0;
		StringBuilder buffer = new StringBuilder();
		int cursor = 0;
		while (cursor < s.length()) {
			char c = s.charAt(cursor);
			cursor++;

			// linefeeds are treated identically as commas
			if (c == ',')
				c = '\n';
			if (c < ' ' && c != '\n')
				c = ' ';

			switch (state) {
			case 0:
				if (c > ' ') {
					buffer.append(c);
					state = 1;
				}
				break;
			case 1:
				if (c == '\n') {
					flush(buffer, set);
					state = 0;
				} else if (c == ' ') {
					state = 2;
				} else {
					buffer.append(c);
				}
				break;
			case 2:
				if (c == '\n') {
					flush(buffer, set);
					state = 0;
				} else if (c > ' ') {
					buffer.append(' ');
					buffer.append(c);
					state = 1;
				}
				break;
			}
		}
		if (buffer.length() != 0)
			flush(buffer, set);
		setTags(set);
	}

	/**
	 * Set tags
	 * 
	 * @param set
	 *            set of zero to MAX_TAGS tags
	 */
	public void setTags(Set<String> set) {
		if (set.size() > MAX_TAGS)
			throw new IllegalArgumentException("too many tags");
		this.tags = set;
	}

	/**
	 * Get receipt's tags as a user-displayable string
	 * 
	 * @return tags separated by commas
	 */
	public String getTagsString() {
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = tags.iterator();
		while (it.hasNext()) {
			String s = it.next();
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(s);
		}
		return sb.toString();
	}

	private Set<String> tags;
	private JSDate date;
	private String summary;
	private int id;
}
