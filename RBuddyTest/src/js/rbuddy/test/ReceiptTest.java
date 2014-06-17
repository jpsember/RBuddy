package js.rbuddy.test;

import org.junit.*;

import js.json.JSONEncoder;
import js.json.JSONParser;
import js.rbuddy.JSDate;
import js.rbuddy.Receipt;
import js.rbuddy.Cost;

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
	public void testCostIsRecorded() {
		Receipt r = new Receipt(42);
		assertEqualsFloat(0, r.getCost().getValue());
		r.setCost(new Cost(123.45));
		assertEqualsFloat(123.45, r.getCost().getValue());
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
		Receipt r = new Receipt(72);
		r.setSummary("\n\nA long summary\n\n\n   \n\n with several linefeeds, \"quotes\", and | some other characters | ... \n\n");
		JSONEncoder enc = new JSONEncoder();
		r.encode(enc);
		String s = enc.toString();

		JSONParser js = new JSONParser(s);
		Receipt r2 = (Receipt) Receipt.JSON_PARSER.parse(js);

		JSONEncoder enc2 = new JSONEncoder();
		r2.encode(enc2);
		assertStringsMatch(s, enc2.toString());
	}

}
