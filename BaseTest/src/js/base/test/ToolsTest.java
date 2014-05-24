package js.base.test;

import android.test.InstrumentationTestCase;
import static js.base.Tools.*;

public class ToolsTest extends InstrumentationTestCase {

	public void testASSERT() {
		String msg = "";
		ASSERT(true);

		try {
			ASSERT(false);

		} catch (RuntimeException e) {
			msg = e.getMessage();
		}
		assertTrue(msg.contains("ASSERTION FAILED"));
	}

	public void testfBits() {
		assertStringsMatch("010001", fBits(17, "6"));
		assertStringsMatch(".1...1", fBits(17, "6d"));
		assertStringsMatch(" 1...1", fBits(17, "6dz"));
		assertStringsMatch("                               .", fBits(0, "32dz"));
		assertStringsMatch("   1...1", fBits(17));
	}
	
	public void testFormatHex() {
		assertStringsMatch("$       40",fh(64));
		assertStringsMatch("$        0",fh(0));
		assertStringsMatch("00000000",toHex(null,0,8,false,false) );
		assertStringsMatch("0000 0000",toHex(null,0,8,false,true));
	}
	
	public static void assertStringsMatch(Object s1, Object s2) {
		unimp("move this to a test utility class");
		if (s1==null) s1 = "<null>";
		if (s2==null) s2 = "<null>";
		assertEquals(s1.toString(),s2.toString());
	}
	
}
