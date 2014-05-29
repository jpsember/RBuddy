package js.rbuddy.test;

import org.junit.*;

import static org.junit.Assert.*;
import js.rbuddy.Receipt;
//import js.basic.Tools;


public class ReceiptTest extends js.testUtils.MyTest {

	private void verifySummary(String input, String expOutput) {
	Receipt r = new Receipt();
	r.setSummary(input);
	assertStringsMatch(r.getSummary(),expOutput);
	}
	
	@Test
	public void testSummaryLeadSpaces() {
		verifySummary("   leading spaces","leading spaces");
	}
	
	@Test
	public void testSummaryTrailingSpaces() {
		verifySummary("trailing spaces   ","trailing spaces");
	}

	@Test
	public void testSummaryReplaceLinefeedsWithSpaces() {
		verifySummary("linefeeds\n\n\nembedded","linefeeds embedded");
	}
	
	@Test
	public void testConstructorStartsWithCurrentDate() {
		
		Receipt r = new Receipt();
		assertTrue(r.getDate() != null);
	}
	
}

