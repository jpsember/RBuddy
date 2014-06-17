package js.basicTest;

import js.basic.StringUtil;
import js.testUtils.*;

public class StringUtilTest extends MyTest {

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
