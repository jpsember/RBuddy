package com.js.rbuddy.test;

import java.util.ArrayList;
import java.util.List;

import com.js.basic.Tools;
import com.js.json.JSONEncoder;
import com.js.json.JSONParser;
import com.js.rbuddy.JSDate;
import com.js.rbuddy.Cost;
import com.js.rbuddy.Receipt;
import com.js.rbuddy.ReceiptFilter;
import com.js.rbuddy.TagSet;
import com.js.testUtils.IOSnapshot;
import com.js.testUtils.MyTest;
import static com.js.basic.Tools.*;

public class ReceiptFilterTest extends MyTest {

	private List<Receipt> generateReceipts() {
		Tools.seedRandom(1965); // ensure consistent random numbers
		ArrayList list = new ArrayList();
		for (int i = 0; i < 50; i++) {
			list.add(Receipt.buildRandom(1 + i));
		}
		return list;
	}

	private void applyTestFilter(ReceiptFilter rf, boolean replaceExistingSnapshot) {
		List<Receipt> receipts = generateReceipts();
		IOSnapshot.open(replaceExistingSnapshot);
		pr(JSONEncoder.toJSON(rf));
		for (Receipt r : receipts) {
			boolean pass = rf.apply(r);
			pr((pass ? "YES      " : "         ") + r);
		}
		IOSnapshot.close();
	}

	public void testMinCostFilter() {
		ReceiptFilter rf = new ReceiptFilter();
		rf.setMinCostActive(true);
		rf.setMinCost(new Cost(10.0));
		applyTestFilter(rf,false);
	}

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
		JSDate d = JSDate.currentDate();
		rf.setMinDate(d);

		rf.setMinCostActive(true);
		Cost c = new Cost("123.45");
		rf.setMinCost(c);

		rf.setInclusiveTagsActive(true);
		TagSet ts = TagSet.parse("alpha,bravo,charlie delta,epsilon");
		rf.setInclusiveTags(ts);

		String s = JSONEncoder.toJSON(rf);

		pr("converted receiptFilter to JSON string:" + s);

		ReceiptFilter rf2 = ReceiptFilter.parse(new JSONParser(s));

		pr(rf2);

	}

	public void testFiltering() {

		ReceiptFilter rf = new ReceiptFilter();

		JSDate min_date = JSDate.buildFromValues(2014, 4, 4);
		rf.setMinDate(min_date);
		rf.setMinDateActive(true);

		JSDate max_date = JSDate.buildFromValues(2014, 7, 7);
		rf.setMaxDate(max_date);
		rf.setMaxDateActive(true);

		Cost min_cost = new Cost("123.45");
		rf.setMinCost(min_cost);
		rf.setMinCostActive(true);

		Cost max_cost = new Cost("678.90");
		rf.setMaxCost(max_cost);
		rf.setMaxCostActive(true);

		TagSet inc_ts = TagSet.parse("Florida,Georgia,Alabama");
		rf.setInclusiveTags(inc_ts);
		rf.setInclusiveTagsActive(true);

		TagSet exc_ts = TagSet.parse("Florida");
		rf.setExclusiveTags(exc_ts);
		rf.setExclusiveTagsActive(true);

		JSDate test_date = JSDate.buildFromValues(2014, 5, 12);
		Cost test_cost = new Cost("500");
		TagSet test_ts = TagSet.parse("Florida");

		if (rf.applyFilter(test_cost, test_date, test_ts) == true)
			pr("Test receipt passed thru the filter...");
		else
			pr("Filter failed the test receipt...");

	}

	private ReceiptFilter f;
}
