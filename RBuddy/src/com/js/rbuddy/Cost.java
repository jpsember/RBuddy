package com.js.rbuddy;

import static com.js.basic.Tools.rnd;

import java.text.NumberFormat;

import com.js.json.IJSONEncoder;
import com.js.json.JSONEncoder;
import com.js.json.JSONParser;

public class Cost implements IJSONEncoder {

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
		this(s, false);
	}

	/**
	 * Constructor that parses user string (Locale-specific)
	 * 
	 * @param s
	 */
	public Cost(String s, boolean useZeroIfParseFails) {
		try {
			value = parse(s);
		} catch (NumberFormatException e) {
			if (!useZeroIfParseFails)
				throw e;
		}
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
		// NumberFormat f = getUserCurrencyFormat();
		// Currency c = f.getCurrency();
		// pr("\n" + "tim was here " + c.getSymbol());

		s = s.replaceAll("[^\\d.]", "");

		Double d = Double.parseDouble(s);
		return d;

		// pr(" first parse failed: " + e);
		// unimp("not done... try other heuristics before giving up");
	}

	// try {
	// // try putting the dollar sign at the front...
	//
	// pr(" so we try to add the correct prefix...");
	// Currency c = f.getCurrency();
	// String firstfix = c.getSymbol() + s;
	// n = f.parse(firstfix);
	// pr(" which worked!");
	// return n.doubleValue();
	// } catch (ParseException e) {
	// pr(" second parse failed: " + e);
	// }
	// maybe we should try something else like removing spaces between $ and
	// number...

	// pr("TJS parse of the input fell all the way through...");
	// throw new NumberFormatException();

	public double getValue() {
		return value;
	}

	/**
	 * Generate a random cost for test purposes
	 * 
	 * @return
	 */
	public static Cost buildRandom() {

		return new Cost(((int) (rnd.nextDouble() * rnd.nextDouble()
				* rnd.nextDouble() * 100000)) / 100.0);
	}

	@Override
	public void encode(JSONEncoder encoder) {
		encoder.encode(value);
	}

	public static Cost parse(JSONParser json) {
		return new Cost(json.nextDouble());
	}

	/**
	 * Compare two costs
	 * 
	 * @param other
	 *            other cost
	 * @return integer indicating whether this cost is greater than (>0), equal
	 *         to (=0), or less than (<0) the other one
	 * 
	 */
	public int compare(Cost other) {
		return Double.compare(value, other.value);
	}

	private double value;

}
