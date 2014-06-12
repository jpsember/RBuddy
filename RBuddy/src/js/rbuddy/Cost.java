package js.rbuddy;

import static js.basic.Tools.pr;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;

import static js.basic.Tools.*;

public class Cost {

	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean zeroAsEmptyString) {
		if (zeroAsEmptyString && value == 0)
			return "";
		NumberFormat f = getUserCurrencyFormat();
		return f.format(value);
	}

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
	 * For test purposes, we want to be able to test various currencies, not
	 * just ours (which may in fact be different for other testers).
	 * 
	 * @param format
	 */
	public static void setUserCurrencyFormat(NumberFormat format) {
		userCurrencyFormat = format;
	}

	private static NumberFormat userCurrencyFormat;

	// what is the keyboard command to do the auto indent?

	// menu: source / format

	// constructor for 'internal' amount
	public Cost(double amount) {
		value = amount;
	}

	// we really should be dealing with the fact that
	// "123.45" in is one hundred twenty three dollars and forty five cents
	// right now i am pretending the string must be pennies

	/**
	 * Constructor that parses user string (Locale-specific)
	 * 
	 * @param s
	 */
	public Cost(String s) {

		value = parse(s);

	}

	/*
	 * We want to parse the user's input, s, using the appropriate
	 * NumberFormat.getCurrencyInstance(...); if it fails, then first remove all
	 * spaces and try again; if that fails, add prefix '$' or whatever the
	 * currency instance indicates is the prefix for that user, and try again.
	 * If THAT fails, then throw a 'NumberFormatException'.
	 */
	private static double parse(String s) {
		// An empty string is always interpreted as zero.
		if (s.isEmpty())
			return 0;
		
		/*
		 * NumberFormat[] fmts = { NumberFormat.getCurrencyInstance(Locale.US),
		 * NumberFormat.getCurrencyInstance(Locale.FRENCH),
		 * NumberFormat.getCurrencyInstance(Locale.JAPAN), };
		 */
		/*
		 * NumberFormat f = fmts[i]; pr("\n" + f.getCurrency());
		 * 
		 * String s = f.format(value); pr(" format()= '" + s + "'"); Number n =
		 * null; try { n = f.parse(s); pr(" parse()= " + n); } catch
		 * (ParseException e) { pr(" parse failed: " + e); }
		 */

		// we are not gonna use currency stuff anymore
		// and are just trying to get a number to parse...
		//NumberFormat f = getUserCurrencyFormat();
		// Currency c = f.getCurrency();
		// pr("\n" + "tim was here " + c.getSymbol());

		
		s = s.replaceAll("[^\\d.]", "");

		
			Double d = Double.parseDouble(s);
			return d;
			
		
			
			//pr(" first parse failed: " + e);
			//unimp("not done... try other heuristics before giving up");
		}
		
//		try {
//			// try putting the dollar sign at the front...
//			
//			pr(" so we try to add the correct prefix...");
//			Currency c = f.getCurrency();
//			String firstfix = c.getSymbol() + s;
//			n = f.parse(firstfix);
//			pr(" which worked!");
//			return n.doubleValue();
//		} catch (ParseException e) {
//			pr(" second parse failed: " + e);
//		}
		// maybe we should try something else like removing spaces between $ and number...
		
//		pr("TJS parse of the input fell all the way through...");
//		throw new NumberFormatException();
	
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
