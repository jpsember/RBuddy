package js.basic;
import static js.basic.Tools.*;

public class StringUtil {
	
	private static int clamp(int value, int min, int max) {
		if (value < min) value = min;
		else if (value > max) value = max;
		return value;
	}

	/**
	 * Generate a random string
	 * 
	 * @param maxLength
	 *            maximum length of string
	 * @return random string
	 */
	public static String randomString(int maxLength) {
		unimp("organize rnd, clamp into appropriate utility classes");
		
		StringBuilder sb = new StringBuilder();
		int ln = rnd.nextInt(maxLength / 2 + maxLength / 2);
		ln = clamp(ln, 1, maxLength);
		for (int i = 0; i < ln; i++) {
			if (rnd.nextInt(6) == 0 && i > 0 && i < ln - 1
					&& sb.charAt(i - 1) != ' ')
				sb.append(' ');
			else
				sb.append((char) (rnd.nextInt(26) + 'a'));
		}
		return sb.toString();
	}

}
