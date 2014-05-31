package js.rbuddy;
import java.util.regex.*;

import static js.basic.Tools.*;

public class JSDate {

	/**
	 * Constructor for subclasses only
	 */
	protected JSDate(int year, int month, int day) {
		this.year = year;this.month = month; this.day = day;
	}
	
	/**
	 * Get string representation of date, with these properties:
	 * 1) it can be parsed to reconstruct an exact copy of the date
	 * 2) its lexicographic order equals its chronological order
	 */
	public String toString() {
		if (str == null) {
			str = String.format("%04d-%02d-%02d",year,month,day);
		}
		return str;
	}

	public int year() {
		return year;
	}
	
	public int month() {
		return month;
	} 
	
	public int day() {
		return day;
	}
	
	private int year, month, day;
	private String str;
	
	/**
	 * Replace current factory with a new one.  Used, e.g., to allow an Android app to represent
	 * dates differently
	 * 
	 * @param f
	 */
	public static void setFactory(JSDateFactory f) {
		factory = f;
	}
	
	public static JSDate currentDate() {return factory.currentDate();}
	public static JSDate parse(String s) {return factory.parse(s);}
	
	private static Pattern dateRegExPattern = Pattern.compile("(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)");
	/**
	 * Provided to subclasses: parse YYYY-MM-DD into array of three integers
	 */
	protected static int[] parseStandardDateFromString(String s) {
		Matcher m = dateRegExPattern.matcher(s);
		if (!m.matches())
			die("can't parse date: " + s);

		int year = Integer.parseInt(m.group(1));
		int month = Integer.parseInt(m.group(2));
		int day = Integer.parseInt(m.group(3));
		int[] a = {year,month,day};
		return a;
	}

	private static JSDateFactory factory = new JSDateFactory() {
		@Override
		public JSDate currentDate() {
			// For this most basic factory, we just return a constant date
			return new JSDate(1965, 10, 31);
		}

		@Override
		public JSDate parse(String s) {
			int[] a = parseStandardDateFromString(s);
			return new JSDate(a[0], a[1], a[2]);
		}
	};
	
}
