package js.rbuddy.test;

import org.junit.*;

//import static org.junit.Assert.*;
import js.rbuddy.JSDate;
import js.rbuddy.Receipt;

public class ReceiptTest extends js.testUtils.MyTest {

	private void verifySummary(String input, String expOutput) {
		Receipt r = new Receipt(42);
		r.setSummary(input);
		assertStringsMatch(r.getSummary(), expOutput);
	}

	@Test
	public void testSummaryLeadSpaces() {
		verifySummary("   leading spaces", "leading spaces");
	}

	@Test
	public void testSummaryTrailingSpaces() {
		verifySummary("trailing spaces   ", "trailing spaces");
	}

	@Test
	public void testSummaryReplaceLinefeedsWithSpaces() {
		verifySummary("linefeeds\n\n\nembedded", "linefeeds embedded");
		verifySummary("linefeeds    \t\n\n  embedded    aaa \n\n \n bbb ",
				"linefeeds embedded aaa bbb");
		verifySummary(" \n\n \n ", "");
	}

	@Test
	public void testConstructorStartsWithCurrentDate() {
		Receipt r = new Receipt(42);
		assertStringsMatch(JSDate.currentDate(), r.getDate());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAttemptToAssignIllegalUniqueIdentifier() {
		new Receipt(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAttemptToAssignIllegalUniqueIdentifier2() {
		new Receipt(-1);
	}

	@Test
	public void testEncode() {
		{
			Receipt r = new Receipt(72);
			r.setSummary("\n\nA long summary\n\n\n   \n\n with several linefeeds, \"quotes\", and | some other characters | ... \n\n");
			CharSequence s = r.encode();

			Receipt r2 = Receipt.decode(s);
			assertStringsMatch(s, r2.encode());
		}
		{
			Receipt r = new Receipt(72);
			CharSequence s = r.encode();
			Receipt r2 = Receipt.decode(s);
			assertStringsMatch(s, r2.encode());
		}
	}

}
