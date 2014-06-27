package com.js.rbuddy.test;

import java.text.NumberFormat;
import java.util.Locale;

import com.js.rbuddy.Cost;
import com.js.testUtils.*;

public class CostTest extends MyTest {

	// Convenience methods to set specific locales
	private void setLocale(Locale locale) {
		Cost.setUserCurrencyFormat(NumberFormat.getCurrencyInstance(locale));
	}

	@Override
	protected void setUp() {
		super.setUp();
		setLocale(Locale.US);
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

	public void testNormalAmount() {

		String s = "123.45";
		Cost c = new Cost(s);
		assertEqualsFloat(123.45, c.getValue());
	}

	public void testNormal2() {

		String s = "$123.45";
		Cost c = new Cost(s);
		assertEqualsFloat(123.45, c.getValue());
	}

	public void testNormal3() {

		String s = "123.45";
		Cost c = new Cost(s);
		assertEqualsFloat(123.45, c.getValue());

		s = "$123.45";
		c = new Cost(s);
		assertEqualsFloat(123.45, c.getValue());

		s = "^123.45";
		c = new Cost(s);
		assertEqualsFloat(123.45, c.getValue());

		// Cost.setUserCurrencyFormat(NumberFormat
		// .getCurrencyInstance(Locale.GERMANY));
		//
		// s = "\u20ac123.45";
		// c = new Cost(s);
		// assertTrue(c.getValue() == 12345);
	}

	public void testFormattingViaToString() {
		Cost.setUserCurrencyFormat(NumberFormat.getCurrencyInstance(Locale.US));

		String s = "$1,123.00";
		Cost c = new Cost(s);
		assertStringsMatch("$1,123.00", c.toString());
	}

	// all the correct ways i can think of to express
	// sixty seven dollars...

	public void testDollarAmount() {
		Cost c = new Cost("67");
		assertEqualsFloat(67, c.getValue());
	}

	public void testDollar2() {
		Cost c = new Cost("$67");
		assertEqualsFloat(67, c.getValue());
	}

	public void testDollar3() {
		Cost c = new Cost("$67.00");
		assertEqualsFloat(67, c.getValue());
	}

	public void testDollar4() {
		Cost c = new Cost("67.00");
		assertEqualsFloat(67, c.getValue());
	}

	public void testDollar5() {

		String s = "$ 67";
		Cost c = new Cost(s);
		assertEqualsFloat(67, c.getValue());
	}

	public void testParseEmptyString() {
		Cost c = new Cost("");
		assertEqualsFloat(0, c.getValue());
	}

	public void testParsing() {
		// Verify that it can construct Cost objects by parsing various strings
		String[] script = { "67", "123.45", "67.00", "$ 67", "123.45" };
		for (int i = 0; i < script.length; i++)
			new Cost(script[i]);
	}

}
