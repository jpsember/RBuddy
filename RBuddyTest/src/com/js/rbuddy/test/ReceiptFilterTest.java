package com.js.rbuddy.test;

import com.js.rbuddy.ReceiptFilter;
import com.js.rbuddy.TagSet;
import com.js.testUtils.MyTest;

public class ReceiptFilterTest extends MyTest {

	/**
	 * Build filter, store in instance field 'f'; does nothing if already built
	 * 
	 * @return filter f
	 */
	private ReceiptFilter f() {
		if (f == null) {
			f = new ReceiptFilter();
		}
		return f;
	}

	public void testStoresTagSet() {
		TagSet s = TagSet.parse("alpha,bravo,charlie delta,epsilon");

		f();
		f.setInclusiveTags(s);
		assertEquals(s.size(), f.getInclusiveTags().size());
	}

	public void testThereAreSomeWhoKnowMeAsTim() {
		
		ReceiptFilter rf = new ReceiptFilter();
		
		rf.setMinDateActive(true);
		
		assertTrue(rf.isMinDateActive());
		assertFalse(rf.isMaxDateActive());
	}

	private ReceiptFilter f;
}
