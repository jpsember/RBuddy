package js.rbuddy.test;

import org.junit.*;

//import static org.junit.Assert.*;
import js.rbuddy.JSDate;
//import static js.basic.Tools.*;
import js.rbuddy.Receipt;


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
		verifySummary("linefeeds\n\n\nembedded", "linefeeds embedded");
		verifySummary("linefeeds    \t\n\n  embedded    aaa \n\n \n bbb ",
				"linefeeds embedded aaa bbb");
		verifySummary(" \n\n \n ",
				"");
	}
	
	@Test
	public void testConstructorStartsWithCurrentDate() {
		Receipt r = new Receipt();
		assertStringsMatch(JSDate.currentDate(), r.getDate());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAttemptToAssignIllegalUniqueIdentifier() {
		new Receipt().setUniqueIdentifier(0);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAttemptToAssignIllegalUniqueIdentifier2() {
		new Receipt().setUniqueIdentifier(-1);
	}
	
}

