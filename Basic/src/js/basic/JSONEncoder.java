package js.basic;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static js.basic.Tools.*;

public class JSONEncoder {

	private StringBuilder sb = new StringBuilder();

	public String toString() {
		return sb.toString();
	}

	public void encode(JSONInterface jsonInstance) {
		jsonInstance.encode(this);
	}

	public void encode(Object value) {
		if (value instanceof Number)
			encode(((Number) value).doubleValue());
		else if (value instanceof Boolean)
			encode(((Boolean) value).booleanValue());
		else if (value == null)
			encodeNull();
		else if (value instanceof Map)
			encode((Map) value);
		else if (value instanceof List)
			encode((List) value);
		else if (value instanceof Object[])
			encode((Object[]) value);
		else if (value instanceof int[])
			encode(cvtArray((int[]) value));
		else if (value instanceof double[])
			encode(cvtArray((double[]) value));
		else if (value instanceof String)
			encode((String) value);
		else
			throw new JSONException("unknown value type " + value + " : "
					+ value.getClass());
	}

	private static Object[] cvtArray(double[] value) {
		Object[] array = new Object[value.length];
		int i = 0;
		for (double x : value) {
			array[i++] = x;
		}
		return array;
	}

	private static Object[] cvtArray(int[] value) {
		Object[] array = new Object[value.length];
		int i = 0;
		for (int x : value) {
			array[i++] = x;
		}
		return array;
	}

	public void encode(Map map2) {
		// final boolean db = true;
		sb.append('{');
		boolean first = true;

		Map<String, Object> map = (Map<String, Object>) map2;

		for (Map.Entry<String, Object> entry : map.entrySet()) {

			if (!first) {
				sb.append(',');
			}
			first = false;
			encode(entry.getKey());
			sb.append(':');
			Object value = entry.getValue();
			encode(value);
		}
		sb.append('}');
	}

	public void encode(List list) {
		sb.append('[');
		boolean first = true;
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			if (!first) {
				sb.append(',');
			}
			first = false;
			encode(iter.next());
		}
		sb.append(']');
	}

	public void encode(Object[] array) {
		sb.append('[');
		boolean first = true;
		for (int i = 0; i < array.length; i++) {
			if (!first) {
				sb.append(',');
			}
			first = false;
			encode(array[i]);
		}
		sb.append(']');
	}

	public void encode(double d) {
		// final boolean db = true;
		long intValue = Math.round(d);
		if (d == intValue) {
			sb.append(intValue);
			if (db)
				pr(" encoding double " + d + " to int " + intValue);
		} else {
			if (db)
				pr(" encoding double " + d + " as double, since != intValue "
						+ intValue);
			sb.append(d);
		}
	}

	public void encode(String s) {
		sb.append('"');
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\n':
				sb.append("\\n");
				break;
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			default:
				if (c >= ' ' && c < 0x7f)
					sb.append(c);
				else {
					sb.append(String.format("\\u%04x", (int) c));
				}
				break;
			}
		}
		sb.append('"');
	}

	public void encode(boolean b) {
		sb.append(b ? "true" : "false");
	}

	public void encodeNull() {
		sb.append("null");
	}

	public void clear() {
		sb.setLength(0);
	}

}
