package js.rbuddy;

public interface JSDateFactory {
	JSDate currentDate();

	/**
	 * Parse a date from a string
	 * @param s
	 * @return JSDate
	 * @throws IllegalArgumentException if parsing failed
	 */
	JSDate parse(String s);
}
