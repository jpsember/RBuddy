package js.basic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static js.basic.Tools.*;

public class JSONParser {

	/**
	 * Utility method that constructs a parser, and given a string and an
	 * IJSONParser, parses and returns an object
	 * 
	 * @param jsonString
	 * @param objectParser
	 * @return
	 */
	public static Object parse(String jsonString, IJSONParser objectParser) {
		JSONParser parser = new JSONParser(jsonString);
		return parser.read(objectParser);
	}

	public JSONParser(String string) {
		try {
			InputStream stream = new ByteArrayInputStream(
					string.getBytes("UTF-8"));
			readValueFromStream(stream);
		} catch (UnsupportedEncodingException e) {
			throw new JSONException(e);
		}
	}

	public Object next() {
		Object value = this.iterator.next();
		
		if (currentMap != null) {
			this.valueForLastKey = currentMap.get(value);
		}
		return value;
	}

	public String nextKey() {
		if (currentMap == null)
			throw new IllegalStateException("not iterating within map");
		return (String) next();
	}

	public void setTrace(boolean t) {
		trace = t;
		if (trace) warning("enabling trace, called from "+stackTrace(1,1));
	}

	/**
	 * Get the value for the last key read; assumes iterating within map
	 * 
	 * @return
	 */
	public Object keyValue() {
		if (currentMap == null)
			throw new IllegalStateException("not iterating within map");
		return this.valueForLastKey;
	}

	public int nextInt() {
		return ((Double) next()).intValue();
	}

	public double nextDouble() {
		return ((Double) next()).doubleValue();
	}

	public String nextString() {
		String s = (String) next();
		return s;
	}

	/**
	 * Starts iterating through the current object, which is assumed to be a
	 * list
	 */
	public void enterList() {
		ArrayList list = (ArrayList) next();
		pushParseLocation();
		startProcessing(list);
	}

	/**
	 * Starts iterating through the current object, which is assumed to be a map
	 */
	public void enterMap() {
		Map map = (Map) next();
		pushParseLocation();
		startProcessing(map);
	}

	/**
	 * Stop iterating through the current object, and resume the previous
	 * object; throws exception if the current object's iteration is not
	 * complete
	 */
	public void exit() {
		exit(true);
	}

	/**
	 * Stop iterating through the current object, and resume the previous object
	 */
	public void exit(boolean verifyNoItemsRemain) {
		if (verifyNoItemsRemain) {
			if (iterator.hasNext())
				throw new IllegalStateException("incomplete iteration");
		}
		if (parseStack.isEmpty())
			throw new IllegalStateException("cannot exit from top level");
		this.iterator = (Iterator) pop(parseStack);
		this.currentContainer = pop(parseStack);
		if (this.currentContainer instanceof Map) {
			this.currentMap = (Map) this.currentContainer;
		} else {
			this.currentMap = null;
		}
		this.valueForLastKey = null;
	}

	private void pushParseLocation() {
		parseStack.add(this.currentContainer);
		parseStack.add(this.iterator);
	}

	public JSONParser(InputStream stream) {
		readValueFromStream(stream);
	}

	private void readValueFromStream(InputStream s) {
		this.stream = s;
		Object topLevelValue = readValue();
		verifyDone();
		// Construct a list that contains this single object
		ArrayList topLevelList = new ArrayList();
		topLevelList.add(topLevelValue);
		startProcessing(topLevelList);
	}

	private void startProcessing(ArrayList list) {
		this.currentContainer = list;
		this.currentMap = null;
		this.iterator = list.iterator();
	}

	private void startProcessing(Map map) {
		this.currentContainer = map;
		this.currentMap = map;
		this.iterator = map.keySet().iterator();
	}

	public boolean hasNext() {
		return this.iterator.hasNext();
	}

	private Object readValue() {
		int c = peek(true);
		switch (c) {
		case '"':
			return readString();
		case '{':
			return readObject();
		case '[':
			return readArray();
		case 't':
		case 'f':
			return readBoolean();
		case 'n':
			readNull();
			return null;
		default:
			return readNumber();
		}
	}

	private String readString() {
		sb.setLength(0);
		read('"', true);
		while (true) {
			int c = read2(false);
			switch (c) {
			case '"':
				return sb.toString();
			case '\\': {
				c = read2(false);
				switch (c) {
				case '"':
				case '/':
				case '\\':
					sb.append((char) c);
					break;
				case 'b':
					sb.append('\b');
					break;
				case 'f':
					sb.append('\f');
					break;
				case 'n':
					sb.append('\n');
					break;
				case 'r':
					sb.append('\r');
					break;
				case 't':
					sb.append('\t');
					break;
				case 'u':
					sb.append(parseHex());
					break;
				}
			}
				break;
			default:
				sb.append((char) c);
				break;
			}
		}
	}

	private int parseDigit() {
		char c = (char) read2(false);
		if (c >= 'A' && c <= 'F') {
			return c - 'A' + 10;
		}
		if (c >= 'A' && c <= 'F') {
			return c - 'A' + 10;
		}
		if (c >= 'a' && c <= 'f') {
			return c - 'a' + 10;
		}
		if (c >= '0' && c <= '9') {
			return c - '0';
		}
		throw new JSONException("unexpected input");
	}

	private char parseHex() {
		return (char) ((parseDigit() << 12) | (parseDigit() << 8)
				| (parseDigit() << 4) | parseDigit());
	}

	// private int readInt() {
	// double d = readNumber();
	// int i = (int) Math.round(d);
	// if (i != d)
	// throw new JSONException("not an integer");
	// return i;
	// }

	private double readNumber() {
		// final boolean db = true;
		if (db)
			pr("\n\nreadNumber");
		sb.setLength(0);
		int state = 0;
		boolean done = false;
		while (true) {
			int c = peek(false);
			boolean isDigit = (c >= '0' && c <= '9');
			if (db)
				pr(" state=" + state + " c=" + (char) c + " buffer:" + sb);
			int oldState = state;
			state = -1;
			switch (oldState) {
			case 0:
				if (c == '-')
					state = 1;
				else if (isDigit) {
					state = (c == '0') ? 2 : 3;
				}
				break;
			case 1:
				if (isDigit) {
					state = (c == '0') ? 2 : 3;
				}
				break;
			case 2:
				if (c == '.')
					state = 4;
				else
					done = true;
				break;
			case 3:
				if (c == '.')
					state = 4;
				else if (isDigit)
					state = 3;
				else if (c == 'e' || c == 'E')
					state = 6;
				else
					done = true;
				break;
			case 4:
				if (isDigit)
					state = 5;
				break;
			case 5:
				if (isDigit)
					state = 5;
				else if (c == 'e' || c == 'E')
					state = 6;
				else
					done = true;
				break;
			case 6:
				if (c == '+' || c == '-')
					state = 7;
				else if (isDigit)
					state = 8;
				break;
			case 7:
				if (isDigit)
					state = 8;
				break;
			case 8:
				if (isDigit)
					state = 8;
				else
					done = true;
				break;
			}
			if (done)
				break;

			if (state < 0) {
				if (db)
					pr("   ...throwing exception");
				throw new JSONException("unexpected input");
			}
			sb.append((char) c);
			read2(false);
		}
		double value;
		try {
			value = Double.parseDouble(sb.toString());
		} catch (NumberFormatException e) {
			throw new JSONException(e);
		}
		return value;
	}

	public Object read(IJSONParser parser) {
		return parser.parse(this);
	}

	public Map readObject() {
		Map m = new HashMap();
		read('{', true);
		if (peek(true) != '}') {
			while (true) {
				String key = readString();
				read(':', true);
				Object value = readValue();
				m.put(key, value);
				if (peek(true) != ',')
					break;
				read2(false);
			}
		}
		read('}', true);
		return m;
	}

	private ArrayList readArray() {
		// final boolean db = true;
		if (db)
			pr("\n\nreadArray");
		ArrayList a = new ArrayList();
		read('[', true);
		if (db)
			pr(" seeing if immediately ends... peek=" + (char) peek(true));
		if (peek(true) != ']') {
			while (true) {
				if (db)
					pr("  reading next value, peek = " + (char) peek(false));
				Object value = readValue();
				a.add(value);

				if (peek(true) != ',')
					break;
				read2(false);
			}
		}
		read(']', true);
		return a;
	}

	private void readExpString(String s) {
		for (int i = 0; i < s.length(); i++)
			read(s.charAt(i), false);
	}

	private void readNull() {
		readExpString("null");
	}

	private Boolean readBoolean() {
		if (peek(false) == 't') {
			readExpString("true");
			return Boolean.TRUE;
		} else {
			readExpString("false");
			return Boolean.FALSE;
		}
	}

	private int peek(boolean ignoreWhitespace) {
		final boolean db = false;
		if (db)
			pr("peek(ignore=" + ignoreWhitespace + ", peek=" + peek + ")");
		if (peek < 0) {
			try {
				while (true) {
					peek = stream.read();
					if (db)
						pr(" stream.read() returned " + peek);
					if (trace) {
						String s = (peek < 0) ? "EOF" : Character
								.toString((char) peek);
						if (s == "\n")
							s = "\\n";
						System.out.println("JSON > " + s);
					}

					if (!ignoreWhitespace || peek > ' ' || peek < 0)
						break;
				}
			} catch (IOException e) {
				throw new JSONException(e);
			}
		}
		return peek;
	}

	private int read2(boolean ignoreWhitespace) {
		int p = peek(ignoreWhitespace);
		if (p < 0)
			throw new JSONException("end of input");
		peek = -1;
		if (db)
			pr("Read char: " + (char) p);
		return p;
	}

	private void read(int expectedChar, boolean ignoreWhitespace) {
		int c = read2(ignoreWhitespace);
		if (c != expectedChar)
			throw new JSONException("unexpected input");
	}

	private void verifyDone() {
		if (peek(true) >= 0)
			throw new JSONException("extra input");
	}

	private StringBuilder sb = new StringBuilder();
	private int peek = -1;
	private InputStream stream;
	private boolean trace;

	private Object currentContainer;
	private Map currentMap; // null if current container is not a map
	private Object valueForLastKey;
	// iterator into current object (map or list)
	private Iterator iterator;
	private ArrayList parseStack = new ArrayList();
}
