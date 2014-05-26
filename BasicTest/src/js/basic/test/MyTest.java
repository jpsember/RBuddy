package js.basic.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import static js.basic.Tools.*;

public class MyTest {
	public static void assertStringsMatch(Object s1, Object s2) {
		if (s1 == null)
			s1 = "<null>";
		if (s2 == null)
			s2 = "<null>";
		assertEquals(s1.toString(), s2.toString());
	}

	/**
	 * Create a temporary directory
	 * Note: the web says this code may include race conditions and security problems;
	 * but if we're only using it for testing, I'm not concerned.
	 * 
	 * @return
	 */
	public File tempDirectory() {
		File t = null;
		try {
			t = File.createTempFile("_test_temp_",
					Long.toString(System.nanoTime()));
		} catch (IOException e) {
			die(e);
		}
		if (!(t.delete()))
			die("could not delete temp dir prior to creation");
		if (!t.mkdir())
			die("could not create temp dir");
		return t;
	}
}
