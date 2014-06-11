package js.rbuddy.test;

import static js.basic.Tools.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.*;

import js.rbuddy.TagSet;
import js.testUtils.IOSnapshot;

public class TagSetTest extends js.testUtils.MyTest {

	private TagSet ts;

	private static final int OUR_MAX_SIZE = 5;

	private TagSet build() {
		if (ts == null)
			ts = new TagSet(OUR_MAX_SIZE);
		return ts;
	}

	private void addScript(String[] s) {
		for (int i = 0; i < s.length; i++) {
			ts.addTag(s[i]);
			ts.verifyInternalConsistency();
		}
	}

	private void addScript(String s) {
		// For some reason, splitting the empty string produces one (empty)
		// string instead of zero strings
		String[] script = {};
		if (s.length() > 0)
			script = s.split("(?!^)");
		assertEquals(script.length, s.length());
		addScript(script);
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
	/**
	 * Add random tags to a set with a large capacity, and simulate it using a simple
	 * but slower data structure; verify that the contents match afterwards.
	 */
	public void testEncodeDecode() {
		build();
		String[] script = {"alpha","bravo charlie","delta epsilon gamma","whisky","foxtrot","echo","zulu","november"};
		for (int i = 0; i < 20; i++) {
			ts.addTag(script[random().nextInt(script.length)]);
		}
		String s = ts.encode();
		TagSet ts2 = TagSet.decode(s);
		assertStringsMatch(toString(ts.tags()),toString(ts2.tags()));
		assertStringsMatch(s,ts2.encode());
	}
	

	@Test
	public void testEmptySet() {
		testScr("", "");
	}

	private void testScr(String s, String exp) {
		build();
		addScript(s);
		String q = toString(ts.tags());
		q = q.replaceAll("\\s+", "");
		assertStringsMatch(exp, q);
	}

	@Test
	public void testMoveToFrontNotNecessaryWithSize1() {
		testScr("aaaaa", "a");
	}

	@Test
	public void testMoveToFrontNotNecessaryWithSize2() {
		testScr("abbb", "ab");
	}

	@Test
	public void testMoveToFrontNotNecessaryWithSize3() {
		testScr("abcccbbbaaabbbaaa", "abc");
	}

	@Test
	public void testAdd2() {
		testScr("baaaaa", "ab");
	}

	@Test
	public void testAdd3() {
		testScr("abcde", "abcde");
	}

	@Test
	public void testAdd4() {
		testScr("abcdef", "bcdef");
	}

	@Test
	public void testAdd5() {
		testScr("fedcba", "abcde");
	}

	@Test
	public void testAlphaOrder() {
		testScr("mzajhq", "ahjqz");
	}

	@Test
	public void testAdd6() {
		testScr("abcdeeeeeeeee", "abcde");
	}

	@Test
	public void testAdd7() {
		testScr("abcdeaaaaaaa", "abcde");
	}

	@Test
	public void testMovingDifferentPositionsToFront() {
		String s = "abcde";
		for (int i = 0; i < s.length(); i++) {
			int c = s.length() - 1 - i;
			String s2 = s + s.substring(c, c + 1);
			testScr(s2, s);
		}
	}

	@Test
	/**
	 * Add random tags to a set with a large capacity, and simulate it using a simple
	 * but slower data structure; verify that the contents match afterwards.
	 */
	public void testLargeTagSet() {

		int maxTagSetSize = 5000;
		TagSet set = new TagSet(maxTagSetSize);

		// Construct a map of elements we expect to see in the tag set, mapped
		// to the step number when they were 'born'
		Map<String, Integer> nameToBirthdayMap = new HashMap();

		// Construct a sorted set of tags, sorted by birthdate
		Map<Integer, String> birthdayToNameMap = new HashMap();

		// Keep track of the minimum birthday in our simulated data structure;
		// we will look for this birthday in our data structure to find the one
		// to toss out,
		// and increment it until we do find one
		int purgeBirthday = 0;

		for (int stepNumber = 0; stepNumber < maxTagSetSize * 5; stepNumber++) {

			// Choose a random name that will generate some collisions
			// (simulating 'move to front')
			String tagName = "__"
					+ (random().nextInt(maxTagSetSize * 5) + 1000);

			Integer prevBirthdayForThisName = nameToBirthdayMap.put(tagName,
					stepNumber);
			if (prevBirthdayForThisName != null) {
				birthdayToNameMap.remove(prevBirthdayForThisName);
			}
			birthdayToNameMap.put(stepNumber, tagName);

			set.addTag(tagName);

			while (nameToBirthdayMap.size() > maxTagSetSize) {
				while (true) {
					assertTrue(purgeBirthday < stepNumber);
					String removeName = birthdayToNameMap.get(purgeBirthday);

					if (removeName == null) {
						purgeBirthday++;
						continue;
					}

					Object val = nameToBirthdayMap.remove(removeName);
					assertTrue(val != null);
					birthdayToNameMap.remove(purgeBirthday);
					purgeBirthday++;
					break;
				}
			}
		}
		
		// We've added so many, and don't expect a ridiculous number of collisions; hence the
		// tag set ought to be full
		assertEquals(set.size(), maxTagSetSize); 
		
		// Verify that the contents are equal
		for (Iterator<String> iter = nameToBirthdayMap.keySet().iterator(); iter
				.hasNext();) {
			String name = iter.next();
			assertTrue(set.tags().contains(name));
		}
	}
	
	@Test
	public void testFormatEmptyTagNameSet() {
		Set<String> tagSet = TagSet.constructTagNameSet();
		assertStringsMatch("",TagSet.formatTagNameSet(tagSet));
	}
	
	@Test
	public void testTagNameSetIsCaseInsensitive() {
		Set<String> tagSet = TagSet.constructTagNameSet();
		tagSet.add("hello");
		tagSet.add("Hello");
		tagSet.add("hEllO");
		assertTrue(tagSet.size() == 1);
	}
	
	@Test
	public void testTagNameSetFormat() {
		IOSnapshot.open();
		
		Set<String> tagSet = TagSet.constructTagNameSet();
		tagSet.add("medium");
		tagSet.add("large box");
		tagSet.add("tiny");
		tagSet.add("big");
		tagSet.add("small");
		
		pr(TagSet.formatTagNameSet(tagSet));
		
		IOSnapshot.close();
	}
	
	@Test
	public void testTagNameSetParse() {
		
		String[] script = {
				"","",//
				"   ","",//
				"alpha","alpha",//
				"nospace,aftercomma","aftercomma, nospace",//
				"two words","two words",//
				"two words,then three words","then three words, two words", //
				"trailingcomma,","trailingcomma",//
				"boo,trailingcommaandwhitespace   ,","boo, trailingcommaandwhitespace",//
				"boo.trailingcommaandwhitespace   ...  . . ","boo, trailingcommaandwhitespace",//
		};
		
		for (int i = 0; i<script.length; i+=2) {
			String s = script[i];
			String sExp = script[i+1];
			Set<String> tagNameSet = TagSet.parseTagNameSet(s);
			String s2 = TagSet.formatTagNameSet(tagNameSet);
			assertStringsMatch(sExp,s2);
		}
	}
	
}
