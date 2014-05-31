package js.rbuddy.test;

import js.rbuddy.JSDate;

import org.junit.*;

//import static org.junit.Assert.*;

public class JSDateTest extends js.testUtils.MyTest {

	@Test
	public void testCurrentDate() {
		JSDate d = JSDate.currentDate();

		String s = d.toString();
		JSDate d2 = JSDate.parse(s);
		assertStringsMatch(s, d2.toString());
	}

}
