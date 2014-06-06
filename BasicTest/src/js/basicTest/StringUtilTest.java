package js.basicTest;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.StringTokenizer;

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

	@Test
	public void testStringEncode() {
		String special = "\"\'|\\{}\u0000";
		StringBuilder sb = new StringBuilder();
		int k = 0;
		for (int i = 0; i < 1000; i++) {
			int c;
			int j = random().nextInt(4);
			switch (j) {
			default:
				c = k;
				k += 1;
				break;
			case 1:
				c = random().nextInt(1024);
				break;
			case 2:
				c = special.charAt(random().nextInt(special.length()));
				break;
			case 3:
				c = random().nextInt(127 - ' ') + ' ';
				break;
			}
			sb.append((char) c);
		}
		String source = sb.toString();
		CharSequence encoded = StringUtil.encode(source);
		CharSequence decoded = StringUtil.decode(encoded);
		assertStringsMatch(source, decoded);
	}

	@Test
	public void testStringTokenizer() {
		String s = "aaa|bbb|ccc";
		StringTokenizer t = new StringTokenizer(s, "|");
		assertStringsMatch("aaa", t.nextToken());
		assertStringsMatch("bbb", t.nextToken());
		assertStringsMatch("ccc", t.nextToken());
		assertFalse(t.hasMoreTokens());
	}

	// This test confirms that StringTokenizer doesn't recognize zero-length
	// tokens.
	@Test
	public void testStringTokenizer2() {
		String s = "aaa|bbb|ccc||eee|";
		String[] sp = s.split("\\|");
		System.out.println("s.split=" + Arrays.toString(sp));

		StringTokenizer t = new StringTokenizer(s, "|");
		assertStringsMatch("aaa", t.nextToken());
		assertStringsMatch("bbb", t.nextToken());
		assertStringsMatch("ccc", t.nextToken());
		// It's skipping the blank token altogether
		// assertStringsMatch("", t.nextToken());
		assertStringsMatch("eee", t.nextToken());
		// It also omits the last token
		assertFalse(t.hasMoreTokens());
	}

	@Test
	public void testStringSplit() {
		IOSnapshot.open();
		String[] script = { "aaa|bbb|ccc||eee|", "|||", "", "abc|", "|abc" };
		for (int i = 0; i < script.length; i++) {
			String s = script[i];
			String[] sp = s.split("\\|");
			System.out.println("split " + s + ":\n --> " + Arrays.toString(sp));
		}
		IOSnapshot.close();
	}

	@Test
	public void testMyTokenize() {
		IOSnapshot.open();
		String[] script = { "aaa|bbb|ccc||eee|", "|||", "", "abc|", "|abc" };
		for (int i = 0; i < script.length; i++) {
			String s = script[i];
			String[] sp = StringUtil.tokenize(s);
			System.out.println("split " + s + ":\n --> " + Arrays.toString(sp));
		}
		IOSnapshot.close();
	}


}
