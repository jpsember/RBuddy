package js.rbuddy.test;

import static org.junit.Assert.*;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.junit.*;

import js.basic.IOSnapshot;
//import static org.junit.Assert.*;
import js.rbuddy.JSDate;
import js.rbuddy.Receipt;
import js.rbuddy.TagSet;
import static js.basic.Tools.*;

public class TagSetTest extends js.testUtils.MyTest {

	private static String[] script = { "zero", "alpha", "mary", "una",
			"jofree", };
	private static String[] script2 = { "q", "w", "e", "r", "t", "y", "u", "i",
			"o", "p", };

	private TagSet ts;

	private static final int OUR_MAX_SIZE = 5;
	
	private TagSet build() {
		if (ts == null)
			ts = new TagSet(OUR_MAX_SIZE);
		return ts;
	}

	private void addScript(String[] s) {
		for (int i = 0; i<s.length; i++)
			ts.addTag(s[i]);
	}
	
	private String dump() {
		return toString(build().tags());
	}
	
	private static String toString(Set<String> set) {
		StringBuilder sb = new StringBuilder();
		for (Iterator it = set.iterator(); it.hasNext();) {
			sb.append(it.next());
			sb.append(' ');
		}
		return sb.toString();
	}

	@Test
	public void testGetsTagsInAlphaOrder() {
		build();
		addScript(script);
		assertStringsMatch("alpha jofree mary una zero ",
				toString(ts.tags()));
	}

	@Test
	public void testBumpsLeastRecentlyUsed() {
		build();
		addScript(script2);
		addScript(script2);
		assertStringsMatch("p o i u y ",toString(ts.tags()));
		assertEquals(ts.size(), OUR_MAX_SIZE);
	}
}
