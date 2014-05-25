package js.basic.test;

import static org.junit.Assert.*;
import js.basic.IOSnapshot;

import org.junit.*;

import static js.basic.Tools.*;

public class ToolsTest extends MyTest {

	@Test
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
		assertStringsMatch("0000_0000",toHex(null,0,8,false,true));
	}
	
	@Test
	public void testFormatHexWithSnapshots() {
		IOSnapshot.open();
		for (int i = 0; i < 32; i++) {
			int v = 1 << i;
			System.out.println(fh(v));
			pr(fh(v,"8zg"));
			pr(fh(v,"8g"));
			pr(fh(v,"8z"));
			pr(fh(v,"8"));
		v = ~v;
		pr(fh(v,"8zg"));
		pr(fh(v,"8g"));
		pr(fh(v,"8z"));
		pr(fh(v,"8"));
		pr("");
		}
		IOSnapshot.close();
	}
	
}
