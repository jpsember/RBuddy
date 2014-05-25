package js.basic.test;

//import static org.junit.Assert.*;
//
//import org.junit.*;

//import static js.basic.Tools.*;
import js.basic.OutputInterceptor;

public class OutputInterceptorTest extends MyTest {

//	@Test
	public void _SKIP_testInterceptor() {

		System.out.println("testInterceptor");
		OutputInterceptor icept = new OutputInterceptor(System.out);
		System.out.println("Built JSOutputInterceptor");
		icept.attachOut();

		printStuff();
		
		icept.detachOut();

		System.out.println("detached output");
		String content = icept.content();
		System.out.println("content:\n" + content
				+ "\n-------------------------------");
	}

	private void printStuff() {
		for (int i = 0; i < 20; i++)
			System.out.println("i=" + i);
	}

}
