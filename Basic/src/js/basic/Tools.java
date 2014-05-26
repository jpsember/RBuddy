package js.basic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static js.basic.MyMath.*;

public final class Tools {

	// This global flag determines whether the line numbers that appear in
	// warnings (and 'unimp' messages)
	// are to be replaced with constant placeholders ('XXX') so that old
	// snapshots remain valid even if
	// a line number associated with a warning has changed.
	static boolean sanitizeLineNumbers;

//	private static String stackTrace() {
//		return stackTraceFmt(1);
//	}

	public static String stackTrace(Throwable t) {
		return stackTrace(1, 10, t);
	}

	// public static String stackTrace(int max) {
	// StringBuilder sb = new StringBuilder();
	// sb.append(stackTrace(1, max));
	// sb.append(" : ");
	// tab(sb, 24);
	// return sb.toString();
	// }

//	private static String stackTraceFmt(int skip) {
//		StringBuilder sb = new StringBuilder();
//		sb.append(stackTrace(1 + skip, 1));
//		sb.append(" : ");
//		tab(sb, 24);
//		return sb.toString();
//	}

	public static void sleepFor(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			report(e, "sleep interrupted");
		}
	}

	/**
	 * Construct a string describing a stack trace
	 * 
	 * @param skipCount
	 *            # stack frames to skip (actually skips 1 + skipCount, to skip
	 *            the call to this method)
	 * @param displayCount
	 *            maximum # stack frames to display
	 * @return String; iff displayCount > 1, cr's inserted after every item
	 */
	public static String stackTrace(int skipCount, int displayCount) {
		// skip 1 for call to this method...
		return stackTrace(1 + skipCount, displayCount, new Throwable());
	}

	/**
	 * Construct string describing stack trace
	 * 
	 * @param skipCount
	 *            # stack frames to skip (actually skips 1 + skipCount, to skip
	 *            the call to this method)
	 * @param displayCount
	 *            maximum # stack frames to display
	 * @param tThrowable
	 *            containing stack trace
	 * @return String; iff displayCount > 1, cr's inserted after every item
	 */
	private static String stackTrace(int skipCount, int displayCount,
			Throwable t) {
		StringBuilder sb = new StringBuilder();

		StackTraceElement[] elist = t.getStackTrace();

		int s0 = skipCount;
		int s1 = s0 + displayCount;

		for (int i = s0; i < s1; i++) {
			if (i >= elist.length) {
				break;
			}
			StackTraceElement e = elist[i];
			String cn = e.getClassName();
			cn = cn.substring(cn.lastIndexOf('.') + 1);
			sb.append(cn);
			sb.append(".");
			sb.append(e.getMethodName());
			sb.append(":");
			sb.append(e.getLineNumber());
			if (displayCount > 1) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Simple assertion mechanism, throws RuntimeException if flag is false
	 * 
	 * @param flag
	 *            : flag to test
	 * @param message
	 *            : if flag is false, throws RuntimeException including this
	 *            message
	 */
	public static void ASSERT(boolean flag, String message) {
		if (!flag) {
			die("ASSERTION FAILED (" + message + ")");

		}
	}

	public static void die() {
		die("(no reason given)");
	}

	public static void die(String message) {
		throw new RuntimeException("Failing; " + message);
	}

	public static void die(Throwable t) {
		die("(Throwable:" + t.getMessage() + ")");
	}

//	private static void toss(String msg) {
//		RuntimeException e = new RuntimeException(msg + " " + stackTrace());
//		// pr("Throwing: " + e+"\n"+stackTrace(8));
//		throw e;
//	}

	/**
	 * Simple assertion mechanism, throws RuntimeException if flag is false
	 * 
	 * @param flag
	 *            flag to test
	 */
	public static void ASSERT(boolean flag) {
		if (!flag) {
			die("ASSERTION FAILED");
		}
	}

	public static void unimp() {
		warning("TODO", null, 1);
	}

	public static void unimp(String msg) {
		warning("TODO", msg, 1);
	}

	private static Pattern lineNumbersPattern;

	/**
	 * Replace all line numbers within a stack trace with 'XXX' so they are
	 * ignored within snapshots; has no effect if sanitize is not active
	 * 
	 * @param s
	 *            string containing stack trace
	 * @return possibly modified stack trace
	 */
	private static String sanitizeStackTrace(String s) {
		if (sanitizeLineNumbers) {
			if (lineNumbersPattern == null)
				lineNumbersPattern = Pattern.compile(":(\\d+)($|\\n)");
			Matcher m = lineNumbersPattern.matcher(s);
			s = m.replaceAll("_XXX");
		}
		return s;
	}

	private static void warning(String type, String s, int skipCount) {
		String st = stackTrace(1 + skipCount, 1);
		st = sanitizeStackTrace(st);
		StringBuilder sb = new StringBuilder();
		sb.append("*** ");
		if (type == null) {
			type = "WARNING";
		}
		sb.append(type);
		if (s != null && s.length() > 0) {
			sb.append(": ");
			sb.append(s);
		}
		sb.append(" (");
		sb.append(st);
		sb.append(")");
		String keyString = sb.toString();

		{
			Object wr = warningStrings.get(keyString);
			if (wr == null) {
				warningStrings.put(keyString, Boolean.TRUE);
				pr(keyString);
			}
		}
	}

	public static void warning(String s) {
		warning(s,1);
	}
	public static void warning(String s, int skipCount) {
		warning(null, s, 1 + skipCount);
	}
	public static String f(boolean b) {
		return b ? " T" : " F";
	}

	/**
	 * Convert (unsigned) integer to string representing its binary equivalent.
	 * Uses default format: 8 bits, omit leading zeros, display zeros as dots
	 * 
	 * @param word
	 *            integer to display
	 * @return
	 */
	public static String fBits(int word) {
		return fBits(word, "8zd");
	}

	private static Object[] parseNumberOfDigitsPrefix(String s) {
		Object[] output = { null, null };

		int cursor = 0;
		while (cursor < s.length()) {
			char c = s.charAt(cursor);
			if (c < '0' || c > '9')
				break;
			cursor++;
		}
		ASSERT(cursor > 0 && cursor <= 2);
		String digitsString = s.substring(0, cursor);
		int nDigits = Integer.parseInt(digitsString);
		ASSERT(nDigits > 0 && nDigits <= 32);

		output[0] = nDigits;
		output[1] = s.substring(cursor);
		return output;
	}

	/**
	 * Convert (unsigned) integer to string representing its binary equivalent
	 * 
	 * @param word
	 * @param format
	 *            "D*[zd]*" where D is decimal digit (number of significant
	 *            bits) z : skip leading zero bits d : display dots (.) for
	 *            zeros instead of 0
	 * @return String
	 */
	public static String fBits(int word, String format) { // int nBits, boolean
															// ignoreLeadingZeros)
															// {
		StringBuilder sb = new StringBuilder();

		Object[] parts = parseNumberOfDigitsPrefix(format);
		int nBits = (Integer) parts[0];
		format = (String) parts[1];
		boolean filteringLeadingZeros = format.contains("z");
		boolean useDots = format.contains("d");
		char zeroChar = useDots ? '.' : '0';

		for (int j = nBits - 1; j >= 0; j--) {
			boolean nonzero = ((word & (1 << j)) != 0);
			if (j == 0 || nonzero)
				filteringLeadingZeros = false;

			if (nonzero) {
				sb.append('1');
			} else {
				sb.append(filteringLeadingZeros ? ' ' : zeroChar);
			}
		}
		return sb.toString();
	}

	/**
	 * Format a string to be at least a certain size
	 * 
	 * @param s
	 *            string to format
	 * @param length
	 *            minimum size to pad to; negative to insert leading spaces
	 * @return blank-padded string
	 */
	public static String f(String s, int length) {
		return f(s, length, null).toString();
	}

	public static StringBuilder f(String s, int length, StringBuilder sb) {
		if (sb == null)
			sb = new StringBuilder();
		int origLen = sb.length();
		if (length >= 0) {
			sb.append(s);
			if (length > s.length())
				tab(sb, length + origLen);
		} else {
			length = -length;
			if (s.length() < length)
				tab(sb, length - s.length());
			sb.append(s);
		}
		return sb;
	}

	/**
	 * Format a string for debug purposes
	 * 
	 * @param s
	 *            String, may be null
	 * @return String
	 */
	public static String d(CharSequence s) {
		return d(s, 80, false);
	}

	public static String d(Throwable t) {
		return t.getMessage() + "\n" + stackTrace(0, 15, t);
	}

	public static String d(Object obj) {
		String s = null;
		if (obj != null)
			s = obj.toString();
		return d(s);
	}

	public static String describe(Object obj) {
		if (obj == null)
			return "<null>";
		return "<type=" + obj.getClass().getName() + " value="
				+ d(obj.toString()) + ">";
	}

	public static String d(Map m) {
		if (m == null)
			return "null";
		StringBuilder sb = new StringBuilder();
		Iterator it = m.keySet().iterator();
		while (it.hasNext()) {
			Object k = it.next();
			sb.append(f(k.toString(), 50));
			sb.append(" -> ");
			Object v = m.get(k);
			String s = "";
			if (v != null)
				s = chomp(v.toString());

			sb.append(d(s));
			sb.append("\n");
		}
		return sb.toString();
	}

	public static String d(Collection c) {
		StringBuilder sb = new StringBuilder();
		sb.append("[\n");
		Iterator it = c.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			// sb.append(' ');
			sb.append(chomp(obj.toString()));
			sb.append('\n');
		}
		sb.append("]\n");
		return sb.toString();
	}

	public static String d(char c) {
		StringBuilder sb = new StringBuilder();
		sb.append('\'');
		convert(c, sb);
		sb.append('\'');
		return sb.toString();
	}

	/**
	 * Convert string to debug display
	 * 
	 * @param orig
	 *            String
	 * @param maxLen
	 *            : maximum length of resulting string
	 * @param pad
	 *            : if true, pads with spaces after conversion
	 * @return String in form [xxxxxx...xxx], with nonprintables converted to
	 *         unicode or escape sequences, and ... inserted if length is
	 *         greater than about the width of a line
	 */
	public static String d(CharSequence orig, int maxLen, boolean pad) {
		if (maxLen < 8) {
			maxLen = 8;
		}

		StringBuilder sb = new StringBuilder();
		if (orig == null) {
			sb.append("<null>");
		} else {
			sb.append("[");
			convert(orig, sb);
			sb.append("]");
			if (sb.length() > maxLen) {
				sb.replace(maxLen - 7, sb.length() - 4, "...");
			}
		}
		if (pad) {
			tab(sb, maxLen);
		}

		return sb.toString();
	}

	private static void convert(char c, StringBuilder dest) {
		switch (c) {
		case '\n':
			dest.append("\\n");
			break;
		default:
			if (c >= ' ' && c < (char) 0x80) {
				dest.append(c);
			} else {
				dest.append("\\#");
				dest.append((int) c);
			}
			break;
		}
	}

	private static void convert(CharSequence orig, StringBuilder sb) {
		for (int i = 0; i < orig.length(); i++) {
			convert(orig.charAt(i), sb);
		}
	}

	private static final String SPACES = "                             ";

	/**
	 * Get a string consisting of n spaces
	 */
	public static String sp(int n) {
		n = Math.max(n, 0);
		if (n < SPACES.length())
			return SPACES.substring(0, n);
		StringBuilder sb = new StringBuilder(n);
		while (sb.length() < n) {
			int chunk = Math.min(n - sb.length(),SPACES.length());
			sb.append(SPACES.substring(0,chunk));
		}
		return sb.toString();
	}

	/**
	 * Add a space to a StringBuilder if it doesn't already end with whitespace
	 * 
	 * @param sb
	 *            StringBuilder
	 * @return sb the StringBuilder
	 */
	public static StringBuilder addSpace(StringBuilder sb) {
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) > ' ')
			sb.append(' ');
		return sb;
	}

	/**
	 * Add a newline to a StringBuilder if it doesn't already end with one
	 * 
	 * @param sb
	 *            StringBuilder
	 * @return sb the StringBuilder
	 */
	public static StringBuilder addNewline(StringBuilder sb) {
		if (sb.length() == 0 || sb.charAt(sb.length() - 1) != '\n')
			sb.append('\n');
		return sb;
	}

	/**
	 * Format an int into a string
	 * 
	 * @param v
	 *            value
	 * @param width
	 *            max number of digits to display
	 * @param spaceLeadZeros
	 *            if true, right-justifies string
	 * @return String, with format siiii where s = sign (' ' or '-'), if
	 *         overflow, returns s********* of same size
	 */
	public static String f(int v, int width, boolean spaceLeadZeros) {

		// get string representation of absolute value
		String s = Integer.toString(Math.abs(v));

		// get number of spaces to pad
		int pad = width - s.length();

		StringBuilder sb = new StringBuilder();

		// if it won't fit, print stars
		if (pad < 0) {
			sb.append(v < 0 ? '-' : ' ');
			while (sb.length() < width + 1)
				sb.append('*');
		} else {
			// print padding spaces before or after number
			if (spaceLeadZeros) {
				while (pad-- > 0)
					sb.append(' ');
			}
			sb.append(v < 0 ? '-' : ' ');
			sb.append(s);
			// print trailing padding, if any required
			while (pad-- > 0)
				sb.append(' ');
		}
		return sb.toString();
	}

	/**
	 * Format a double into a string, without scientific notation
	 * 
	 * @param v
	 *            : value
	 * @param iDig
	 *            : number of integer digits to display
	 * @param fDig
	 *            : number of fractional digits to display
	 * @return String, with format siiii.fff where s = sign (' ' or '-'), . is
	 *         present only if fDig > 0 if overflow, returns s********* of same
	 *         size
	 */
	public static String f(double v, int iDig, int fDig) {

		StringBuilder sb = new StringBuilder();

		boolean neg = false;
		if (v < 0) {
			neg = true;
			v = -v;
		}

		int[] dig = new int[iDig + fDig];

		boolean overflow = false;

		// Determine which digits will be displayed.
		// Round last digit and propagate leftward.
		{
			double n = Math.pow(10, iDig);
			if (v >= n) {
				overflow = true;
			} else {
				double v2 = v;
				for (int i = 0; i < iDig + fDig; i++) {
					n /= 10.0;
					double d =  Math.floor(v2 / n);
					dig[i] = (int) d;
					v2 -= d * n;
				}
				double d2 = Math.floor(v2 * 10 / n);
				if (d2 >= 5) {
					for (int k = dig.length - 1;; k--) {
						if (k < 0) {
							overflow = true;
							break;
						}
						if (++dig[k] == 10) {
							dig[k] = 0;
						} else
							break;
					}
				}
			}
		}

		if (overflow) {
			int nDig = iDig + fDig + 1;
			if (fDig != 0)
				nDig++;
			for (int k = 0; k < nDig; k++)
				sb.append("*");
		} else {

			sb.append(' ');
			int signPos = 0;
			boolean leadZero = false;
			for (int i = 0; i < iDig + fDig; i++) {
				int digit = dig[i]; // (int) d;
				if (!leadZero) {
					if (digit != 0 || i == iDig || (i == iDig - 1 && fDig == 0)) {
						leadZero = true;
						signPos = sb.length() - 1;
					}
				}
				if (i == iDig) {
					sb.append('.');
				}

				if (digit == 0 && !leadZero) {
					sb.append(' ');
				} else {
					sb.append((char) ('0' + digit));
				}
			}
			if (neg)
				sb.setCharAt(signPos, '-');
		}
		return sb.toString();
	}

	public static String f(double f) {
		return f(f, 5, 3);
	}

	public static String fa(double radians) {
		return f(radians * RADTODEG, 3, 2);
	}

	public static String f(int f) {
		return f(f, 6, true);
	}

	public static String f(int[] ia) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = 0; i < ia.length; i++) {
			if (i > 0)
				sb.append(' ');
			sb.append(ia[i]);
		}
		sb.append("]");
		return sb.toString();
	}

	public static String f(int val, int width) {
		return f(val, width, true);
	}

	/**
	 * Add spaces to a StringBuilder until its length is at some value. Sort of
	 * a 'tab' feature, useful for aligning output.
	 * 
	 * @param sb
	 *            : StringBuilder to pad out
	 * @param len
	 *            : desired length of StringBuilder; if it is already past this
	 *            point, nothing is added to it
	 */
	public static StringBuilder tab(StringBuilder sb, int len) {
		sb.append(sp(len - sb.length()));
		return sb;
	}

	private static HashMap warningStrings = new HashMap();

	public static CharSequence fh(int n) {
		return fh(n, "8zg");
	}

	/**
	 * Convert an unsigned integer to its hex string representation
	 * 
	 * @param n
	 * @param format
	 *            "D+[F]*" where D is decimal digit, representing number of hex
	 *            digits to display; F is z : skip lead zeros g : insert
	 *            underscores to display digits in (g)roups of four
	 * @return string
	 */
	public static CharSequence fh(int n, String format) {
		Object[] fmt = parseNumberOfDigitsPrefix(format);
		int nDig = (Integer) fmt[0];
		String flags = (String) fmt[1];
		return toHex(new StringBuilder("$"), n, nDig, flags.contains("z"),
				flags.contains("g"));
	}

	public static Random rseed(int seed) {
		rnd = null;
		if (seed == 0)
			rnd = new Random();
		else
			rnd = new Random(seed);
		return rnd;
	}

	private static Random random() {
		if (rnd == null)
			rseed(0);
		return rnd;
	}

	public static int rnd(int i) {
		return random().nextInt(i);
	}

	private static Random rnd;

	public static String fa2(double ang) {
		return fa(angle2(ang));
	}

	public static void pr(Object obj) {
		System.out.println(obj);
	}

	/**
	 * Trim trailing linefeeds from string
	 * 
	 * @param s
	 *            input
	 * @return trimmed string
	 */
	public static String chomp(String s) {
		while (s.endsWith("\n")) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	/**
	 * Convert value to hex, store in StringBuilder
	 * 
	 * @param sb
	 *            where to store result, or null
	 * @param value0
	 *            value to convert
	 * @param digits
	 *            number of hex digits to output
	 * @return result
	 */
	public static StringBuilder toHex(StringBuilder sb, int value0, int digits,
			boolean stripLeadingZeros, boolean groupsOfFour) {
		if (sb == null)
			sb = new StringBuilder();

		boolean nonZeroSeen = !stripLeadingZeros;

		long value = value0;

		int shift = (digits - 1) << 2;
		while (digits-- > 0) {
			shift = digits << 2;
			int v = (int) ((value >> shift)) & 0xf;
			if (v != 0 || digits == 0)
				nonZeroSeen = true;

			char c;
			if (!nonZeroSeen) {
				c = ' ';

			} else {
				if (v < 10) {
					c = (char) ('0' + v);
				} else {
					c = (char) ('a' + (v - 10));
				}
			}
			sb.append(c);
			if (groupsOfFour && (digits & 3) == 0 && digits != 0) {
				if (!nonZeroSeen)
					sb.append(' ');
				else
					sb.append('_');
			}
		}
		return sb;
	}

	public static void report(Throwable t, String msg) {
		if (t != null) {
			pr("*** Problem (" + msg + ")");
			t.printStackTrace();
		}
	}

	public static String[] systemCommand(String command) {
		String[] ret = null;
		try {
			ret = systemCommand(command, true);
		} catch (IOException e) {
			die(e);
		}
		return ret;
	}

	public static String[] systemCommand(String command, boolean failIfError)
			throws IOException {
		String[] out = { null, null };
		Process p = Runtime.getRuntime().exec(command);
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));
		StringBuilder sb = new StringBuilder();
		String s;
		while ((s = stdInput.readLine()) != null) {
			sb.append(s);
			sb.append("\n");
		}
		out[0] = sb.toString();
		sb = new StringBuilder();
		while ((s = stdError.readLine()) != null) {
			sb.append(s);
			sb.append("\n");
		}
		out[1] = sb.toString();
		for (int i = 0; i < out.length; i++) {
			if (out[i].length() == 0)
				out[i] = null;
		}
		if (failIfError && out[1] != null)
			die("Failed executing system command '" + command + "';\nstdout:\n"
					+ out[0] + "\nstderr:\n" + out[1]);

		return out;
	}

	public static String hexDump(byte[] byteArray) {
		return hexDump(byteArray, 0, byteArray.length);
	}

	public static String hexDump(byte[] byteArray, int offset, int length) {
		return hexDump(byteArray, offset, length, "16gza");
	}

	public static String hexDump(byte[] byteArray, int offset, int length,
			String options) {
		int groupSize = (1 << 2); // Must be power of 2
		int rowSize = 16;
		Pattern p = Pattern.compile("^(\\d+)(.)*");
		Matcher m = p.matcher(options);
		if (m.find()) {
			rowSize = Integer.parseInt(m.group(1));
		}
		boolean hideZeroes = options.contains("z");
		boolean groups = options.contains("g");
		boolean absoluteIndex = options.contains("A");
		boolean withASCII = options.contains("a");

		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < length) {
			int rSize = rowSize;
			if (rSize + i > length)
				rSize = length - i;
			int address = absoluteIndex ? i + offset : i;
			toHex(sb, address, 4, true, false);
			sb.append(": ");
			if (groups)
				sb.append("| ");
			for (int j = 0; j < rowSize; j++) {
				if (j < rSize) {
					byte val = byteArray[offset + i + j];
					if (hideZeroes && val == 0) {
						sb.append("  ");
					} else {
						toHex(sb, val, 2, false, false);
					}
				} else {
					sb.append("  ");
				}
				sb.append(' ');
				if (groups) {
					if ((j & (groupSize - 1)) == groupSize - 1)
						sb.append("| ");
					// sb.append("  ");
				}
			}
			if (withASCII) {
				sb.append(' ');
				for (int j = 0; j < rSize; j++) {
					byte v = byteArray[offset + i + j];
					// if (false && hideZeroes && v == 0x00)
					// v = ' ';
					// else
					if (v < 0x20 || v >= 0x80)
						v = '.';
					sb.append((char) v);
					if (groups && ((j & (groupSize - 1)) == groupSize - 1)) {
						sb.append(' ');
					}
				}
			}
			sb.append('\n');
			i += rSize;
		}
		return sb.toString();
	}

}
