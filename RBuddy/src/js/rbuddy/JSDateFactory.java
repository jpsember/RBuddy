package js.rbuddy;

import java.util.Date;

public interface JSDateFactory {
	JSDate currentDate();

	/**
	 * Parse a date from a string
	 * @param s
	 * @return JSDate
	 * @throws IllegalArgumentException if parsing failed
	 */
	JSDate parse(String s);
	
	/**
	 * Convert JSDate to Java date
	 * @param d
	 * @return
	 */
	Date convertJSDateToJavaDate(JSDate d);
	
	/**
	 * Convert Java date to JSDate
	 */
	JSDate convertJavaDateToJSDate(Date d);
}
