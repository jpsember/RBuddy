package js.basic.test;

import static org.junit.Assert.*;

public class MyTest   {
	public static void assertStringsMatch(Object s1, Object s2) {
		if (s1==null) s1 = "<null>";
		if (s2==null) s2 = "<null>";
		assertEquals(s1.toString(),s2.toString());
	}
}
