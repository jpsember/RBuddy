package js.testUtils;

import static org.junit.Assert.*;
import org.junit.*;

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

	@Before
	public void setUp() {
		tempDirectory = null;
	}

	@After
	public void tearDown() {
		// Remove our reference to the temporary directory, so a new one is
		// created for the next test.
		// In order to delete it, if it wasn't empty, we'd have to recursively
		// delete all of its contents
		// and subdirectories; let's instead let the OS take care of this in its
		// own time, since it was created
		// in the OS's temporary directory folder.
		tempDirectory = null;
	}

	/**
	 * Create a temporary directory Note: the web says this code may include
	 * race conditions and security problems; but if we're only using it for
	 * testing, I'm not concerned.
	 * 
	 * @return
	 */
	public File tempDirectory() {
		if (tempDirectory == null) {
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
			tempDirectory = t;
		}
		return tempDirectory;
	}

	private File tempDirectory;
}
