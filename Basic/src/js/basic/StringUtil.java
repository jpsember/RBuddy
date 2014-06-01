package js.basic;
import static js.basic.Tools.*;
import static js.basic.JSMath.*;

public class StringUtil {
	
	/**
	 * Generate a random string
	 * 
	 * @param maxLength
	 *            maximum length of string
	 * @return random string
	 */
	public static String randomString(int maxLength) {

		StringBuilder sb = new StringBuilder();
		if (maxLength > 0) {
			int ln = rnd.nextInt(maxLength);
			ln = clampInt(ln, 1, maxLength);
			for (int i = 0; i < ln; i++) {
				if (rnd.nextInt(6) == 0 && i > 0 && i < ln - 1
						&& sb.charAt(i - 1) != ' ')
					sb.append(' ');
				else
					sb.append((char) (rnd.nextInt(26) + 'a'));
			}
		}
		return sb.toString();
	}

}
