package js.rbuddy.test;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.*;

import js.basic.JSONEncoder;
import js.basic.JSONParser;
//import static org.junit.Assert.*;
import js.rbuddy.JSDate;
import js.rbuddy.Receipt;
import js.testUtils.IOSnapshot;
import static js.basic.Tools.*;

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
		Receipt r = new Receipt(72);
		r.setSummary("\n\nA long summary\n\n\n   \n\n with several linefeeds, \"quotes\", and | some other characters | ... \n\n");
		r.setTags("alpha,beta,gamma");
		JSONEncoder enc = new JSONEncoder();
		r.encode(enc);
		String s = enc.toString();

		JSONParser js = new JSONParser(s);
		Receipt r2 = (Receipt) Receipt.JSON_PARSER.parse(js);

		JSONEncoder enc2 = new JSONEncoder();
		r2.encode(enc2);
		assertStringsMatch(s, enc2.toString());
	}

	@Test
	public void testTags() {
		Receipt r = new Receipt(72);
		r.setTags("alpha,beta,gamma");
		Set<String> tags = r.getTags();
		assertTrue(tags.size() == 3);
		assertTrue(tags.contains("alpha"));
		assertFalse(tags.contains("delta"));
	}

	@Test
	public void testTagParserTrimsWhitespace() {
		Receipt r = new Receipt(72);
		r.setTags("  , aaaa  ,  , ,   bbbb\nccc    cccc ");
		Set<String> tags = r.getTags();
		assertTrue(tags.size() == 3);
		assertTrue(tags.contains("aaaa"));
		assertTrue(tags.contains("bbbb"));
		assertTrue(tags.contains("ccc cccc"));
	}

	@Test
	public void testTagParserEmpty() {
		Receipt r = new Receipt(72);
		r.setTags("   ,   ,      ");
		Set<String> tags = r.getTags();
		assertTrue(tags.size() == 0);
	}

	@Test
	public void testTagParserMaxItemsRespected() {
		Receipt r = new Receipt(72);
		r.setTags("a,b, c, d, e, f, g, h, i, j, k");
		assertTrue(r.getTags().size() == Receipt.MAX_TAGS);
	}

	private static String[] scripts = {//
	"a,b, c, d, e, f, g, h, i, j, k",//
			"",//
			"aaa   aaaa",//
			" bb, aaa aaa,  aaa   aaa",//
	};

	@Test
	public void testTagsString() {
		IOSnapshot.open();
		Receipt r = new Receipt(72);
		for (int i = 0; i < scripts.length; i++) {
			r.setTags(scripts[i]);
			pr(r.getTagsString());
		}
		IOSnapshot.close();
	}

	@Test
	public void testTagsStringParseSymmetry() {
		Receipt r = new Receipt(72);
		for (int i = 0; i < scripts.length; i++) {
			r.setTags(scripts[i]);
			String s = r.getTagsString();
			Receipt r2 = new Receipt(73);
			r2.setTags(s);
			String s2 = r2.getTagsString();
			assertStringsMatch(s, s2);
		}
	}

}
