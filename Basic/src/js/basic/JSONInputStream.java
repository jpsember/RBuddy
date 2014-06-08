package js.basic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static js.basic.Tools.*;

public class JSONInputStream extends InputStream {

	public JSONInputStream(String string) {
		try {
			stream = new ByteArrayInputStream(string.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new JSONException(e);
		}
	}

	public JSONInputStream(InputStream stream) {
		this.stream = stream;
	}

	public Object readValue() {
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

	public String readString() {
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

	public double readNumber() {
		sb.setLength(0);
		int state = 0;
		boolean done = false;
		while (true) {
			int c = peek(false);
			boolean isDigit = (c >= '0' && c <= '9');

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

			if (state < 0)
				throw new JSONException("unexpected input");
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

	public ArrayList readArray() {
		// final boolean db = true;
		if (db)
			pr("readArray");
		ArrayList a = new ArrayList();
		read('[', true);
		if (peek(true) != ']') {
			while (true) {
				if (db)
					pr(" peek = " + (char) peek(false));
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

	public void readNull() {
		readExpString("null");
	}

	public boolean readBoolean() {
		if (peek(false) == 't') {
			readExpString("true");
			return true;
		} else {
			readExpString("false");
			return false;
		}
	}

	private int peek(boolean ignoreWhitespace) {
		// final boolean db = true;
		if (db)
			pr("peek(ignore=" + ignoreWhitespace + ", peek=" + peek + ")");
		if (peek < 0) {
			try {
				while (true) {
					peek = stream.read();
					if (db)
						pr(" stream.read() returned " + peek);
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
		return p;
	}

	@Override
	public int read() {
		int p = peek(false);
		peek = -1;
		return p;
	}

	private void read(int expectedChar, boolean ignoreWhitespace) {
		int c = read2(ignoreWhitespace);
		if (c != expectedChar)
			throw new JSONException("unexpected input");
	}

	public void verifyDone() {
		if (peek(true) >= 0)
			throw new JSONException("extra input");
	}

	private StringBuilder sb = new StringBuilder();
	private int peek = -1;
	private InputStream stream;
}
