package js.jsonTest;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.*;

import js.json.IJSONEncoder;
import js.json.IJSONParser;
import js.json.JSONEncoder;
import js.json.JSONException;
import js.json.JSONParser;
import static js.basic.Tools.*;
import static js.json.JSONTools.*;

public class JSONTest extends js.testUtils.MyTest {

	private JSONParser json(String s) {
		json = new JSONParser(s);

		return json;
	}

	private JSONParser json;

	@Test
	public void testNumbers() {
		String script[] = { "0", "1", "-123.52e20", "-123.52e-20", "0.5" };
		for (int i = 0; i < script.length; i++) {
			String s = script[i];
			json(s);

			double d = json.nextDouble();
			assertFalse(json.hasNext());
			assertEquals(Double.parseDouble(s), d, 1e-10);
		}
	}

	@Test
	public void testBadNumbers() {
		// final boolean db = true;
		if (db)
			pr("\n\n\n\n----------------------------------------------------------\ntestBadNumbers\n");
		String script[] = { "-", "00", "12.", ".42", "123ee", "123e-", };
		for (int i = 0; i < script.length; i++) {
			String s = script[i];
			if (db)
				pr("\n-----------------\n constructing json for '" + s + "'");
			try {
				json(s);
				fail("expected exception with '" + s + "'");
			} catch (JSONException e) {
			}
		}
		if (db)
			pr("\n\n\n");
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
		json = new JSONParser(stream);
		Object a = json.next();
		assertTrue(a instanceof ArrayList);

		enc().encode(a);
		String s = enc.toString();
		assertStringsMatch(s, orig);
	}

	@Test
	public void testArray() {
		String orig = "[0,1,2,3,\"hello\"]";

		json(orig);
		json.enterList();
		for (int i = 0; i < 4; i++) {
			assertTrue(json.hasNext());
			assertEquals(i, json.nextInt());
		}
		assertTrue(json.hasNext());
		assertStringsMatch("hello", json.nextString());
		assertFalse(json.hasNext());
		json.exit();
	}

	@Test
	public void testReadMapAsSingleObject() {
		String s = "{'description':{'type':'text','hint':'enter something here'}}";
		s = swapQuotes(s);
		json(s);
		Map map = (Map) json.next();
		assertTrue(map.containsKey("description"));
	}

	@Test
	public void testMap() {
		String orig = "{\"u\":14,\"m\":false,\"w\":null,\"k\":true}";
		json(orig);

		json.enterMap();
		Map m = new HashMap();
		for (int i = 0; i < 4; i++) {
			String key = json.nextKey();
			Object value = json.keyValue();
			assertFalse(m.containsKey(key));
			m.put(key, value);

		}
		json.exit();

		assertStringsMatch(m.get("u"), "14.0");
		assertStringsMatch(m.get("m"), "false");
		assertTrue(m.get("w") == null);
		assertStringsMatch(m.get("k"), "true");
	}

	@Test
	public void testEncodeMap() {
		JSONEncoder enc = new JSONEncoder();
		enc.enterMap();
		enc.encode("a");

		enc.enterList();
		enc.encode(12);
		enc.encode(17);
		enc.exitList();

		enc.encode("b");
		enc.encode(true);
		enc.exitMap();
		String s = enc.toString();
		assertStringsMatch("{\"a\":[12,17],\"b\":true}", s);
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
		String decodedString = json.nextString();
		assertStringsMatch(decodedString, originalString);
	}

	@Test
	public void testSymmetry() {
		String script[] = {//
				"0",//
				"1",//
				"-1.2352E20",//
				"-1.2352E-20",//
				"0.5",//
				"{'hey':42}", //
				"[1,2,3,4]",//
				"{'hey':42,'you':43}",//
				"{'hey':{'you':17},'array':[1,2,3,4]}",//
				"{'trailing number':5}",//
				"{  'favorite song': { '_skip_order': 3, 'type': 'text','hint': 'name of song','zminlines': 5 },'wow':12 } ",//
		};

		for (int i = 0; i < script.length; i++) {
			String s = swapQuotes(script[i]);
			if (db)
				pr("\n testing '" + s + "'");

			json(s);
			Object obj = json.next();
			assertFalse(json.hasNext());
			if (db)
				pr("  parsed " + obj + " (type = " + obj.getClass() + ")");

			newEnc().encode(obj);
			String s2 = enc.toString();
			if (db)
				pr(" encoded is " + s2);

			Object obj2 = json(s2).next();
			if (db)
				pr("parsed object: " + obj2);

			newEnc().encode(obj2);
			String s3 = enc.toString();
			assertStringsMatch(s2, s3);
		}
	}

	@Test
	public void testComments() {

		String script[] = {//
				"{'hey':// this is a comment\n  // this is also a comment\n 42}",//
				"{'hey':42}",//
				"[42,15// start of comment\n//Another comment immediately\n    //Another comment after spaces\n,16]",//
				"[42,15, 16]",//
				"[42,15// zzz\n//zzz\n//zzz\n,16]",
				"[42,15,16]",//
		};

		for (int i = 0; i < script.length; i += 2) {
			String s = swapQuotes(script[i + 0]);
			Object obj = json(s).next();

			String s2 = swapQuotes(script[i + 1]);
			Object obj2 = json(s2).next();

			newEnc().encode(obj);
			String enc1 = enc.toString();

			newEnc().encode(obj2);
			String enc2 = enc.toString();

			assertStringsMatch(enc1, enc2);
		}
	}

	private static class OurClass implements IJSONEncoder {

		public static final IJSONParser parser = new IJSONParser() {

			@Override
			public Object parse(JSONParser json) {
				json.enterList();
				String message = json.nextString();
				int number = json.nextInt();
				json.exit();
				return new OurClass(message, number);
			}
		};

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
	}

	@Test
	public void testInterface() {
		OurClass c = new OurClass("hello", 42);
		enc().encode(c);
		String s = enc().toString();
		json(s);
		OurClass c2 = (OurClass) json.read(OurClass.parser);
		assertStringsMatch(c, c2);
	}
}
