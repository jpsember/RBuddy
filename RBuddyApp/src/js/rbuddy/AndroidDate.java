package js.rbuddy;

import java.util.Calendar;

public class AndroidDate extends JSDate {

	/**
	 * Constructor for subclasses only
	 */
	private AndroidDate(int year, int month, int day) {
		super(year, month, day);
	}

	public static final JSDateFactory androidDateFactory = new JSDateFactory() {
		@Override
		public JSDate currentDate() {
			Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = 1+c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			return new AndroidDate(year, month, day);
		}

		@Override
		public JSDate parse(String s) {
			int[] a = parseStandardDateFromString(s);
			return new AndroidDate(a[0], a[1], a[2]);
		}
	};
}
