package js.rbuddy;

import java.util.Date;
import java.util.Locale;
import java.util.regex.*;

import js.json.*;
import static js.basic.Tools.*;

public class JSDate implements IJSONEncoder {

	public static final int MONTH_BASE = 0;
	public static final int DAY_BASE = 1;
	
	/**
	 * Constructor for subclasses only
	 */
	protected JSDate(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}

	/**
	 * Get string representation of date, with these properties: 1) it can be
	 * parsed to reconstruct an exact copy of the date 2) its lexicographic
	 * order equals its chronological order
	 */
	public String toString() {
		if (str == null) {
			str = buildString(year, month, day);
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

	public static JSDateFactory factory() {return factory;}
	
	private int year, month, day;
	private String str;

	/**
	 * Replace current factory with a new one. Used, e.g., to allow an Android
	 * app to represent dates differently
	 * 
	 * @param f
	 */
	public static void setFactory(JSDateFactory f) {
		factory = f;
	}

	public static JSDate currentDate() {
		return factory.currentDate();
	}

	public static JSDate parse(String s) {
		return factory.parse(s);
	}

	private static Pattern dateRegExPattern = Pattern
			.compile("(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)");

	/**
	 * Provided to subclasses: parse YYYY-MM-DD into array of three integers
	 * 
	 * @throws IllegalArgumentException
	 */
	protected static int[] parseStandardDateFromString(String s) {
		Matcher m = dateRegExPattern.matcher(s);
		int[] a = null;
		do {
			if (!m.matches())
				break;

			int year = Integer.parseInt(m.group(1));
			int month = Integer.parseInt(m.group(2)) + (MONTH_BASE - 1);
			int day = Integer.parseInt(m.group(3)) + (DAY_BASE-1);
			if (month < MONTH_BASE || day < DAY_BASE || month >= 12+MONTH_BASE || day >= 31 + DAY_BASE)
				break;

			int[] b = { year, month, day };
			a = b;
		} while (false);
		
		if (a == null)
			throw new IllegalArgumentException("failed parsing date: " + s);

		return a;
	}

	private static JSDateFactory factory = new JSDateFactory() {
		
		@Override
		public JSDate currentDate() {
			// For this most basic factory, we just return a constant date
			return new JSDate(1965, 10 + MONTH_BASE, 31);
		}

		@Override
		public JSDate parse(String s) {
			int[] a = parseStandardDateFromString(s);
			return new JSDate(a[0], a[1], a[2]);
		}

		@Override
		public Date convertJSDateToJavaDate(JSDate d) {
			throw new UnsupportedOperationException();
		}

		@Override
		public JSDate convertJavaDateToJSDate(Date d) {
			throw new UnsupportedOperationException();
		}
		
	};

	private static String buildString(int year, int month, int day) {
		return String.format(Locale.US, "%04d-%02d-%02d", year, month + (1 - MONTH_BASE),
				day + (1 - DAY_BASE));
	}

	public static JSDate buildFromValues(int year, int month, int day) {
		return JSDate.parse(buildString(year, month, day));
	}

	public static JSDate buildRandom() {
		int year = rnd.nextInt(4) + 2010;
		int month = MONTH_BASE + rnd.nextInt(12);
		int day = DAY_BASE +rnd.nextInt(28);
		return buildFromValues(year, month, day);
	}

	@Override
	public void encode(JSONEncoder encoder) {
		encoder.encode(this.toString());
	}
	
	public static final IJSONParser JSON_PARSER = new IJSONParser() {
		@Override
		public Object parse(JSONParser json) {
			return JSDate.parse(json.nextString());
		}
	};
	
}
