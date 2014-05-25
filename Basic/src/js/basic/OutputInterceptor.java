/**
 * Intercepts System.out or System.err, buffers to an internal string
 */
package js.basic;

//import static js.basic.Tools.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class OutputInterceptor extends PrintStream {

	private PrintStream ps;

	/** the origin output stream */
	private PrintStream orig;
	private ByteArrayOutputStream baos;

	/**
	 * Initializes a new instance of the class Interceptor.
	 * 
	 * @param out
	 *            the output stream to be assigned
	 */
	public OutputInterceptor(OutputStream out) {
		super(out, true);

		baos = new ByteArrayOutputStream();
		ps = new PrintStream(baos);
	}

	public String content() {
		String content;
		try {
			content = baos.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return content;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void print(String s) {
		ps.print(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void println(String s) {
		print(s);
		println();
	}
	
	@Override
	public void println() {
		print("\n");
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void println(Object obj) {
		println(obj.toString());
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public void println() {
//		print("ZZZZZZZZZZZZZZZZZ\n");
//	}

	/**
	 * Attaches System.out to interceptor.
	 */
	public void attachOut() {
		orig = System.out;
		System.setOut(this);
	}

	/**
	 * Attaches System.err to interceptor.
	 */
	public void attachErr() {
		orig = System.err;
		System.setErr(this);
	}

	/**
	 * Detaches System.out.
	 */
	public void detachOut() {
		if (null != orig) {
			System.setOut(orig);
		}
	}

	/**
	 * Detaches System.err.
	 */
	public void detachErr() {
		if (null != orig) {
			System.setErr(orig);
		}
	}
}
