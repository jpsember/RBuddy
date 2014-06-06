package js.rbuddy;

//import static js.basic.Tools.*;

import js.basic.StringUtil;

public class Receipt {

	/**
	 * Constructor
	 * 
	 * Sets date to current date, string fields to the empty string
	 * 
	 * @throws IllegalArgumentException
	 *             if identifier <= 0
	 */
	public Receipt(int identifier) {
		if (identifier <= 0)
			throw new IllegalArgumentException();
		this.uniqueIdentifier = identifier;
		setDate(JSDate.currentDate());
		this.summary = "";
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
		if (tokens.length != 3)
			throw new IllegalArgumentException("problem decoding " + s);

		int f = 0;
		Receipt r = new Receipt(Integer.parseInt(tokens[f++]));
		r.date = JSDate.parse(tokens[f++]);
		r.summary = StringUtil.decode(tokens[f++]).toString();
		return r;
	}

	public StringBuilder encode() {
		return encode(null);
	}
	
	public StringBuilder encode(StringBuilder sb) {
		if (sb==null)
			sb = new StringBuilder();
		sb.append(getUniqueIdentifier());
		sb.append('|');
		sb.append(date.toString());
		sb.append('|');
		StringUtil.encode(getSummary(), sb);
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

	public int getUniqueIdentifier() {
		return uniqueIdentifier;
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

	private JSDate date;
	private String summary;
	private int uniqueIdentifier;
}
