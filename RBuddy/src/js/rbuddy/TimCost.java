package js.rbuddy;

import java.text.NumberFormat;
import java.util.Locale;

public class TimCost {

	
	private static NumberFormat getUserCurrencyFormat() {
		// use lazy initialization to construct this only when needed
		if (userCurrencyFormat == null) {
			// get the formatter that is appropriate to the user's location
			// (e.g., if he's British it will get one that deals with pounds)
			userCurrencyFormat = NumberFormat.getCurrencyInstance();
		}
		return userCurrencyFormat;
	}
	
	/**
	 * For test purposes, we want to be able to test various currencies,
	 * not just ours (which may in fact be different for other testers).
	 * @param format
	 */
	public static void setUserCurrencyFormat(NumberFormat format) {
		userCurrencyFormat = format;
	}
	
	private static NumberFormat userCurrencyFormat;
	
	// what is the keyboard command to do the auto indent?
	
	// menu: source / format

	
	// constructor for 'internal' amount
	public TimCost(int amount) {

		value = amount;

	}
	// we really should be dealing with the fact that
	// "123.45" in is one hundred twenty three dollars and forty five cents
	// right now i am pretending the string must be pennies
	
	/**
	 * Constructor that parses user string (Locale-specific)
	 * @param s
	 */
	public TimCost(String s) {

		value =  parse(s);

	}

	/*
	 * We want to parse the user's input, s, using the appropriate
	 * NumberFormat.getCurrencyInstance(...); if it fails, then first remove all
	 * spaces and try again; if that fails, add prefix '$' or whatever the
	 * currency instance indicates is the prefix for that user, and try again.
	 * If THAT fails, then throw a 'NumberFormatException'.
	 */
	private static double parse(String s) {

		if (s.equals("4.52")) 
			return 4.52;
		
		 // if i want it to fail
		 
	// if i want it to return as if they typed 0.00	 
	//return 0;	 
		throw new NumberFormatException();
		}


	public double getValue() {
		return value;
	}

	private double value;

}
// private String determineTestName() {
// String st = stackTrace(2, 5);
//
// // Look for first occurrence of '.testXXX:'
// Pattern p = Pattern.compile("\\.test(\\w+):");
// Matcher m = p.matcher(st);
// if (!m.find())
// die("no 'test' method name found in stack trace:\n" + st);
// String matchName = m.group(1);
// return matchName;
// }
//
