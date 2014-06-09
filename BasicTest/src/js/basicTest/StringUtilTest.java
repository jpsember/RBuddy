package js.basicTest;

//import static org.junit.Assert.*;

import org.junit.*;

import js.basic.IOSnapshot;
import js.basic.StringUtil;

public class StringUtilTest extends js.testUtils.MyTest {

	@Test
	public void testRandomString() {
		IOSnapshot.open();
		for (int i = 0; i < 12; i++) {
			int len = i * i;
			System.out
					.println(len + ": '" + StringUtil.randomString(len) + "'");
		}
		IOSnapshot.close();
	}
}
