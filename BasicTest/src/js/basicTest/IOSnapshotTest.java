package js.basicTest;

import org.junit.*;
import js.basic.IOSnapshot;

public class IOSnapshotTest extends js.testUtils.MyTest {

	@Test
	public void testStdOut() {
		IOSnapshot.open();
		System.out.println("This is printed to System.out");
		IOSnapshot.close();
	}
	
	@Test
	public void testStdErr() {
		IOSnapshot.open();
		System.out.println("This is printed to System.out");
		System.err.println("This is printed to System.err");
		IOSnapshot.close();
	}

	@Test
	public void testStdErrOnly() {
		IOSnapshot.open();
		System.err.println("This is printed to System.err");
		IOSnapshot.close();
	}

}
