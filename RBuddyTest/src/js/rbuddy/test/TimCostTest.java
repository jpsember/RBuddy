package js.rbuddy.test;

import org.junit.Test;

import static org.junit.Assert.*;
import js.rbuddy.TimCost;

public class TimCostTest extends js.testUtils.MyTest {

	// the value of this object ALWAYS represents a positive number of pennies

	// if you don't send anything in, it is failure
	
	// if you call by integer, you mean pennies
	@Test
	public void testByInteger() {

		// see that we can set it by integer
		int ival = 456;
		TimCost c = new TimCost(ival);
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
		TimCost c = new TimCost(s);
		assertTrue(c.getValue() == 12345);		
	}
	
	@Test
	public void testNormal2() {
		
		String s = "$123.45";
		TimCost c = new TimCost(s);
		assertTrue(c.getValue() == 12345);		
	}
	
	@Test
	public void testNormal3() {
		
		String s = "$  123.45";
		TimCost c = new TimCost(s);
		assertTrue(c.getValue() == 12345);		
	}
	
	
	// all the correct ways i can think of to express 
	// sixty seven dollars...
	
	@Test
	public void testDollarAmount() {
		
		String s = "67";
		TimCost c = new TimCost(s);
		assertTrue(c.getValue() == 6700);		
	}
	
	@Test
	public void testDollar2() {
		
		String s = "$67";
		TimCost c = new TimCost(s);
		assertTrue(c.getValue() == 6700);		
	}
	
	@Test
	public void testDollar3() {
		
		String s = "$67.00";
		TimCost c = new TimCost(s);
		assertTrue(c.getValue() == 6700);		
	}
	
	@Test
	public void testDollar4() {
		
		String s = "67.00";
		TimCost c = new TimCost(s);
		assertTrue(c.getValue() == 6700);		
	}
	
	@Test
	public void testDollar5() {
		
		String s = "$ 67";
		TimCost c = new TimCost(s);
		assertTrue(c.getValue() == 6700);		
	}
	
	// and all the reasons for failure
	
	// no chars other than '$','.',0-9
	@Test
	public void testBadCharacter() {
		
		String s = "!@#";
		TimCost c = new TimCost(s);
		// if that failed, it would have thrown an exception, and that is what
		// we want
		// but tim is super paranoid sometimes, and adding this test means
		// there are no compiler warnings
		assertTrue(c != null);
	}
	
	// no spaces in middle of digits
	
	// no more than two decimal places
	
	// either 0 decimal places or two
	
	
	
	
		
	
}
