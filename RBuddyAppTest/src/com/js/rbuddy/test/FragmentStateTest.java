package com.js.rbuddy.test;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.test.AndroidTestCase;

import com.js.android.PersistentFragmentState;

public class FragmentStateTest extends AndroidTestCase {

	private PersistentFragmentState state;
	private Bundle bIn;
	private Bundle bOut;

	@Override
	protected void setUp() throws Exception {
		state = new PersistentFragmentState();
		bIn = new Bundle();
		bOut = new Bundle();
	}

	public void testMap() {
		Map<String, Object> m = new HashMap();

		state.add(m);
		state.restoreFrom(bIn);

		assertTrue(m.isEmpty());

		m.put("a", 72);
		m.put("b", "beta");
		m.put("c", null);

		state.saveTo(bOut);
		state.restoreFrom(bOut);
		assertEquals(72, ((Number) m.get("a")).intValue());
		assertEquals("beta", m.get("b"));
		Boolean b = (Boolean) m.get("c");
		assertTrue(b == null || !b.booleanValue());
	}

}
