package js.basic;

import static js.basic.Tools.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.*;

public class IOSnapshot {

	public static void open() {
		singleton = new IOSnapshot();
		singleton.doOpen();
	}

	public static void close() {
		if (singleton != null) {
			singleton.doClose();
			singleton = null;
		}
	}

	private static IOSnapshot singleton;

	// Users can't construct objects of this class
	private IOSnapshot() {
	}

	private String constructDiff(String s1, String s2) {
		String diff = null;
		try {
			File t1 = File.createTempFile("temp_s1", ".txt");
			File t2 = File.createTempFile("temp_s2", ".txt");
			Files.writeTextFile(t1, s1);
			Files.writeTextFile(t2, s2);

			String[] output = systemCommand("diff " + t1.getPath() + " "
					+ t2.getPath());
			diff = output[0];
			t1.delete();
			t2.delete();
		} catch (Throwable e) {
			diff = "UNABLE TO CONSTRUCT DIFF: " + e;
		}
		return diff;
	}

	private void doClose() {
		System.setOut(originalStdOut);
		System.setErr(originalStdErr);
		Tools.sanitizeLineNumbers = false;
		String content = capturedStdOut.content();
		String content2 = capturedStdErr.content();
		if (content2.length() > 0)
			content = content + "\n*** System.err:\n"+content2;
		
		try {
			if (snapshotPath.exists()) {
				String previousContent = Files.readTextFile(snapshotPath);
				String diff = constructDiff(previousContent, content);
				if (diff != null) {
					fail("Output disagrees with snapshot (" + snapshotPath
							+ "):\n" + diff);
				}
			} else {
				System.out.println("...writing new snapshot: " + snapshotPath);
				Files.writeTextFile(snapshotPath, content);
			}
		} catch (IOException e) {
			fail(e);
		}
	}

	private void doOpen() {
		calculatePath();
		interceptOutput();
	}

	private void interceptOutput() {
		capturedStdOut = StringPrintStream.build();
		originalStdOut = System.out;
		System.setOut(capturedStdOut);
		
		capturedStdErr = StringPrintStream.build();
		originalStdErr = System.err;
		System.setErr(capturedStdErr);
		
		Tools.sanitizeLineNumbers = true;
	}

	private static File determineSnapshotDirectory() {
		if (snapshotDirectory == null) {
			String userDir = System.getProperty("user.dir");
			File d = new File(new File(userDir), "snapshots");
			if (!d.isDirectory())
				fail("cannot find directory: " + d);
			snapshotDirectory = d;
		}
		return snapshotDirectory;
	}

	private String determineTestName() {
		String st = stackTrace(2, 5);

		// Look for first occurrence of '.testXXX:'
		Pattern p = Pattern.compile("\\.test(\\w+):");
		Matcher m = p.matcher(st);
		if (!m.find())
			fail("no 'test' method name found in stack trace:\n" + st);
		String matchName = m.group(1);
		return matchName;
	}

	private void calculatePath() {
		File snapshotDir = determineSnapshotDirectory();
		String testName = determineTestName();
		this.snapshotPath = new File(snapshotDir, testName + ".txt");
	}

	private static File snapshotDirectory;
	private File snapshotPath;
	private StringPrintStream capturedStdOut, capturedStdErr;
	private PrintStream originalStdOut,originalStdErr;
}
