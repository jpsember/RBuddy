package js.basic.test;

import static org.junit.Assert.*;
import org.junit.*;
import static js.basic.Tools.*;

public class ToolsTest extends MyTest {

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

	@Test
	public void testfBits() {
		assertStringsMatch("010001", fBits(17, "6"));
		assertStringsMatch(".1...1", fBits(17, "6d"));
		assertStringsMatch(" 1...1", fBits(17, "6dz"));
		assertStringsMatch("                               .", fBits(0, "32dz"));
		assertStringsMatch("   1...1", fBits(17));
	}
	
	@Test
	public void testFormatHex() {
		assertStringsMatch("$       40",fh(64));
		assertStringsMatch("$        0",fh(0));
		assertStringsMatch("00000000",toHex(null,0,8,false,false) );
		assertStringsMatch("0000 0000",toHex(null,0,8,false,true));
	}
	
}
