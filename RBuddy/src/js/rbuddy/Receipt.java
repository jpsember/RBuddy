package js.rbuddy;

import static js.basic.Tools.*;
import java.util.StringTokenizer;

import js.basic.StringUtil;

public class Receipt {

	/**
	 * Constructor
	 * 
	 * Sets date to current date, string fields to the empty string, and unique identifier to zero
	 */
	public Receipt() {
		setDate(JSDate.currentDate());
		summary = "";
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

	public static Receipt decode(String s) {
//		final boolean db = true;
		if (db) pr("Receipt.decode \""+s+"\"");
		
		StringTokenizer t = new StringTokenizer(s, "|");
		String id = t.nextToken();
		if (db) pr(" id="+id);
		String date = t.nextToken();
		if (db) pr(" date="+date);
		String summary = t.nextToken();
		if (db) pr(" summary="+summary);
		
		if (t.hasMoreTokens())
			throw new IllegalArgumentException("unable to decode " + s);
		Receipt r = new Receipt();
		r.uniqueIdentifier = Integer.parseInt(id);
		r.summary = StringUtil.decode(summary);
		r.date = JSDate.parse(date);
		return r;
	}

	public String encode() {
		StringBuilder sb = new StringBuilder();
		sb.append(getUniqueIdentifier());
		sb.append('|');
		sb.append(date.toString());
		sb.append('|');
		sb.append(StringUtil.encode(getSummary()));
		return sb.toString();
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

	/**
	 * A valid unique identifier must be a positive integer
	 * 
	 * @param uniqueIdentifier
	 */
	public void setUniqueIdentifier(int uniqueIdentifier) {
		if (uniqueIdentifier <= 0)
			throw new IllegalArgumentException();

		this.uniqueIdentifier = uniqueIdentifier;
	}

	@Override
	public String toString() {
		return "Receipt "+encode();
	}

	/**
	 * For test purposes, build a random receipt
	 * 
	 * @return
	 */
	public static Receipt buildRandom() {
		Receipt r = new Receipt();
		r.setDate(JSDate.buildRandom());
		r.setSummary(StringUtil.randomString(30));
		return r;
	}

	private JSDate date;
	private String summary;
	private int uniqueIdentifier;
}
