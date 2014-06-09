package js.basicTest;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.*;

import js.basic.JSONInterface;
import js.basic.JSONEncoder;
import js.basic.JSONException;
import js.basic.JSONInputStream;
import static js.basic.Tools.*;

public class JSONTest extends js.testUtils.MyTest {

	private JSONInputStream json(String s) {
		json = new JSONInputStream(s);
		return json;
	}

	private JSONInputStream json;

	@Test
	public void testNumbers() {
		String script[] = { "0", "1", "-123.52e20", "-123.52e-20", "0.5" };
		for (int i = 0; i < script.length; i++) {
			String s = script[i];
			json(s);
			double d = json.readNumber();
			json.verifyDone();
			assertEquals(Double.parseDouble(s), d, 1e-10);
		}
	}

	@Test
	public void testBadNumbers() {
		String script[] = { "-", "00", "12.", ".42", "123ee", "123e-", };
		for (int i = 0; i < script.length; i++) {
			String s = script[i];
			try {
				json(s);
				json.readNumber();
				json.verifyDone();
				fail("expected exception with '" + s + "'");
			} catch (JSONException e) {
			}
		}
	}

	private JSONEncoder newEnc() {
		enc = null;
		return enc();
	}

	private JSONEncoder enc() {
		if (enc == null)
			enc = new JSONEncoder();
		return enc;
	}

	private JSONEncoder enc;

	@Test
	public void testStreamConstructor() throws UnsupportedEncodingException {
		String orig = "[0,1,2,3,\"hello\"]";
		InputStream stream = new ByteArrayInputStream(orig.getBytes("UTF-8"));
		json = new JSONInputStream(stream);
		Object a = json.readArray();
		json.verifyDone();
		assertTrue(a instanceof ArrayList);

		enc().encode(a);
		String s = enc.toString();
		assertStringsMatch(s, orig);
	}

	@Test
	public void testArray() {
		String orig = "[0,1,2,3,\"hello\"]";

		json(orig);
		Object a = json.readArray();
		json.verifyDone();
		assertTrue(a instanceof ArrayList);

		enc().encode(a);
		String s = enc.toString();
		assertStringsMatch(s, orig);
	}

	@Test
	public void testArrays() {
		int[] intArray = { 1, 2, 3, 4 };
		enc().encode(intArray);
		String s = enc.toString();
		assertStringsMatch(s, "[1,2,3,4]");
	}

	@Test
	public void testString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			sb.append((char) i);
		}
		String originalString = sb.toString();
		enc().encode(originalString);
		String jsonString = enc.toString();
		json(jsonString);
		String decodedString = json.readString();
		assertStringsMatch(decodedString, originalString);
	}

	@Test
	public void testSymmetry() {
		String script[] = { "0",//
				"1",//
				"-1.2352E20",//
				"-1.2352E-20",//
				"0.5",//
				"{\"hey\":42}", //
				"[1,2,3,4]",//
				"{\"hey\":42,\"you\":43}",//
				"{\"hey\":{\"you\":17},\"array\":[1,2,3,4]}",//
		};
		// final boolean db = true;

		for (int i = 0; i < script.length; i++) {
			String s = script[i];
			if (db)
				pr("\n testing '" + s + "'");

			json(s);
			Object obj = json.readValue();
			json.verifyDone();
			if (db)
				pr("  parsed " + obj + " (type = " + obj.getClass() + ")");

			newEnc().encode(obj);
			String s2 = enc.toString();
			if (db)
				pr(" encoded is " + s2);
			assertStringsMatch(s, s2);
		}
	}

	private static class OurClass implements JSONInterface {

		public static final JSONInterface factory = new OurClass("", 0);

		public OurClass(String message, int number) {
			map.put("message", message);
			map.put("number", number);
			for (int i = 0; i < array.length; i++)
				array[i] = (number + i + 1) * (number + i + 1);
		}

		private int[] array = new int[3];
		private Map map = new HashMap();

		@Override
		public String toString() {
			return map.get("message") + "/" + map.get("number") + "/"
					+ Arrays.toString(array);
		}

		@Override
		public void encode(JSONEncoder encoder) {
			Object[] items = { map.get("message"), map.get("number") };
			encoder.encode(items);
		}

		@Override
		public Object decode(JSONInputStream stream) {
			ArrayList array = stream.readArray();
			return new OurClass(//
					(String) array.get(0), //
					((Double) array.get(1)).intValue()//
			);
		}
	}

	@Test
	public void testInterface() {
		OurClass c = new OurClass("hello", 42);
		enc().encode(c);
		String s = enc().toString();
		json(s);
		OurClass c2 = (OurClass) json.read(OurClass.factory);
		assertStringsMatch(c, c2);
	}
}
