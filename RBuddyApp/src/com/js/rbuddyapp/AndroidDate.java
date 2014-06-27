package com.js.rbuddyapp;

import static com.js.basic.Tools.*;
import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.js.rbuddy.JSDate;
import com.js.rbuddy.JSDateFactory;

public class AndroidDate extends JSDate {

	/**
	 * Constructor for subclasses only
	 */
	private AndroidDate(int year, int month, int day) {
		super(year, month, day);
	}

	@SuppressLint("SimpleDateFormat")
	public static final JSDateFactory androidDateFactory = new JSDateFactory() {

		private SimpleDateFormat jsDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd");

		@Override
		public Date convertJSDateToJavaDate(JSDate d) {
			Date date = null;
			try {
				date = jsDateFormat.parse(d.toString());
			} catch (ParseException e) {
				die(e);
			}
			return date;
		}

		@Override
		public JSDate convertJavaDateToJSDate(Date d) {
			String jsString = jsDateFormat.format(d);
			return parse(jsString);
		}

		@Override
		public JSDate currentDate() {
			return convertJavaDateToJSDate(new Date());
		}

		@Override
		public JSDate parse(String s) {
			int[] a = parseStandardDateFromString(s);
			return new AndroidDate(a[0], a[1], a[2]);
		}

	};

	private static Calendar calendar;

	public static int[] getJavaYearMonthDay(JSDate jsDate) {
		Date date = androidDateFactory.convertJSDateToJavaDate(jsDate);
		if (calendar == null)
			calendar = Calendar.getInstance();
		calendar.setTime(date);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int[] ret = { year, month, day };
		return ret;
	}

	private static java.text.DateFormat userDateFormat;

	private static java.text.DateFormat userDateFormat() {
		if (userDateFormat == null) {
			userDateFormat = android.text.format.DateFormat
					.getDateFormat(RBuddyApp.sharedInstance().context());
		}
		return userDateFormat;
	}

	public static JSDate parseJSDateFromUserString(String userString)
			throws ParseException {
		Date date = userDateFormat().parse(userString);
		return JSDate.factory().convertJavaDateToJSDate(date);

	}

	public static String formatUserDateFromJSDate(JSDate date) {
		return userDateFormat().format(factory().convertJSDateToJavaDate(date));
	}

}
