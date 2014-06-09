package js.rbuddy.test;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.junit.Test;

import static js.basic.Tools.pr;
import static js.basic.Tools.warning;
import static org.junit.Assert.*;
import js.rbuddy.Cost;

public class CostTest extends js.testUtils.MyTest {

	// the value of this object ALWAYS represents a positive number of pennies

	// if you don't send anything in, it is failure

	// if you call by integer, you mean pennies
	@Test
	public void testByInteger() {

		// see that we can set it by integer
		int ival = 456;
		Cost c = new Cost(ival);
		assertTrue(c.getValue() == ival);

	}

	// don't know how to test for failure, but if i did:
	//
	// calling by integer with zero should fail
	//
	// calling by integer with a negative number should fail
	//

	// now follows all the string tests...

	// all the correct ways i can think of to express
	// one hundred twenty three dollars and forty five cents...

	@Test
	public void testNormalAmount() {

		String s = "123.45";
		Cost c = new Cost(s);
		assertTrue(c.getValue() == 12345);
	}

	@Test
	public void testNormal2() {

		String s = "$123.45";
		Cost c = new Cost(s);
		assertTrue(c.getValue() == 12345);
	}

	@Test
	public void testNormal3() {
		Cost.setUserCurrencyFormat(NumberFormat
				.getCurrencyInstance(Locale.US));

		String s = "$  123.45";
		Cost c = new Cost(s);
		assertTrue(c.getValue() == 12345);

		Cost.setUserCurrencyFormat(NumberFormat
				.getCurrencyInstance(Locale.GERMANY));

		s = "\u20ac123.45";
		c = new Cost(s);
		assertTrue(c.getValue() == 12345);
	}

	// all the correct ways i can think of to express
	// sixty seven dollars...

	@Test
	public void testDollarAmount() {

		String s = "67";
		Cost c = new Cost(s);
		assertTrue(c.getValue() == 6700);
	}

	@Test
	public void testDollar2() {

		String s = "$67";
		Cost c = new Cost(s);
		assertTrue(c.getValue() == 6700);
	}

	@Test
	public void testDollar3() {

		String s = "$67.00";
		Cost c = new Cost(s);
		assertTrue(c.getValue() == 6700);
	}

	@Test
	public void testDollar4() {

		String s = "67.00";
		Cost c = new Cost(s);
		assertEqualsFloat(67.0, c.getValue());
	}

	@Test
	public void testDollar5() {

		String s = "$ 67";
		Cost c = new Cost(s);
		assertTrue(c.getValue() == 6700);
	}

	// and all the reasons for failure

	// no spaces in middle of digits

	// no more than two decimal places

	// either 0 decimal places or two

	@Test
	public void testCurrencyFormatterVariousCountries() {
		double value = 1323.526;

		NumberFormat[] fmts = { NumberFormat.getCurrencyInstance(Locale.US),
				NumberFormat.getCurrencyInstance(Locale.FRENCH),
				NumberFormat.getCurrencyInstance(Locale.JAPAN), };

		System.out.println("value=" + value);
		for (int i = 0; i < fmts.length; i++) {

			NumberFormat f = fmts[i];
			pr("\n" + f.getCurrency());

			String s = f.format(value);
			pr(" format()= '" + s + "'");
			Number n = null;
			try {
				n = f.parse(s);
				pr(" parse()= " + n);
			} catch (ParseException e) {
				pr(" parse failed: " + e);
			}
		}
	}

	@Test
	public void testCurrencyFormatterSloppyParseInput() {

		NumberFormat f = NumberFormat.getCurrencyInstance(Locale.US);

		String[] script = { "$123", "123", "123.30", "$123.3", "$123.",
				"$5000", "$5000.2", };

		for (int i = 0; i < script.length; i++) {
			String s = script[i];
			pr("Parsing '" + s + "'");
			Number n = null;
			try {
				n = f.parse(s);
				pr(" yields " + n);
			} catch (ParseException e) {
				pr(" parse failed: " + e);
				warning("If the user omits '$', parsing fails.  We could then tack on a '$' and try the parsing again, but this "
						+ "add-hoc approach has problems, since the user may be in another Locale (i.e. Euros).  One possible approach "
						+ "is to put a fixed amount like 1234.56 through a formatter for the user's Locale and see what it produces, and"
						+ " use the result to infer an appropriate preprocessing to apply to the user's string before attempting to parse it."
						+ "  There may exist some utilities that do this already... more research is required.");
			}
		}
	}

}
