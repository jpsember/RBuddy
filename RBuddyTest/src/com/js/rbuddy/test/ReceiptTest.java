package com.js.rbuddy.test;

import com.js.json.JSONEncoder;
import com.js.json.JSONParser;
import com.js.rbuddy.Cost;
import com.js.rbuddy.JSDate;
import com.js.rbuddy.Receipt;
import com.js.testUtils.*;

public class ReceiptTest extends MyTest {

	private void verifySummary(String input, String expOutput) {
		Receipt r = new Receipt(42);
		r.setSummary(input);
		assertStringsMatch(r.getSummary(), expOutput);
	}

	public void testSummaryLeadSpaces() {
		verifySummary("   leading spaces", "leading spaces");
	}

	public void testSummaryTrailingSpaces() {
		verifySummary("trailing spaces   ", "trailing spaces");
	}

	public void testCostIsRecorded() {
		Receipt r = new Receipt(42);
		assertEqualsFloat(0, r.getCost().getValue());
		r.setCost(new Cost(123.45));
		assertEqualsFloat(123.45, r.getCost().getValue());
	}

	public void testSummaryReplaceLinefeedsWithSpaces() {
		verifySummary("linefeeds\n\n\nembedded", "linefeeds embedded");
		verifySummary("linefeeds    \t\n\n  embedded    aaa \n\n \n bbb ",
				"linefeeds embedded aaa bbb");
		verifySummary(" \n\n \n ", "");
	}

	public void testConstructorStartsWithCurrentDate() {
		Receipt r = new Receipt(42);
		assertStringsMatch(JSDate.currentDate(), r.getDate());
	}

	public void testAttemptToAssignIllegalUniqueIdentifier() {
		try {
			new Receipt(0);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testAttemptToAssignIllegalUniqueIdentifier2() {
		try {
			new Receipt(-1);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

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
