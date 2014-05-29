package js.basic.test;

import java.io.PrintStream;
import org.junit.*;

import static js.basic.Tools.*;
import js.basic.StringPrintStream;

public class StringPrintStreamTest extends js.testUtils.MyTest {

	@Test
	public void testInterceptor() {

		StringPrintStream icept = StringPrintStream.build();
		PrintStream originalOut = System.out;
		System.setOut(icept);

		pr("aaa");
		System.out.print(15);
		pr("bbb");
		System.setOut(originalOut);

		String content = icept.content();
		assertStringsMatch("aaa\n15bbb\n",content);
	}

}
