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

	private void applyTestFilter(ReceiptFilter rf) {
		List<Receipt> receipts = generateReceipts();
		IOSnapshot.open();
		pr(JSONEncoder.toJSON(rf));
		for (Receipt r : receipts) {
			if (rf.apply(r)) {
				pr(r);
			}
		}
		IOSnapshot.close();
	}

	public void testMinCostFilter() {

		ReceiptFilter rf = new ReceiptFilter();
		rf.setMinCostActive(true);
		rf.setMinCost(new Cost(10.0));

		applyTestFilter(rf);
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

		pr("converted receiptFilter to JSON string:"+s);
		
		ReceiptFilter rf2 = ReceiptFilter.parse(new JSONParser(s));
		
		pr(rf2);
		

		}

	public void filteringTest() {

		ReceiptFilter rf = new ReceiptFilter();
		
		rf.setMinDateActive(true);
		JSDate d = JSDate.currentDate();		
		rf.setMinDate(d);
		
		rf.setMinCostActive(true);	
		Cost c = new Cost("123.45");
		rf.setMinCost(c);
		
		rf.setInclusiveTagsActive(true);
		TagSet ts = TagSet.parse("Florida,Georgia");
		rf.setInclusiveTags(ts);
		
		
		JSDate test_date = JSDate.currentDate();
		Cost test_cost = new Cost("678.90");
		TagSet test_ts = TagSet.parse("Florida");
		
		if (rf.applyFilter(test_cost,test_date,test_ts) == true) 
			pr("Test receipt passed thru the filter...");
		else
			pr("Filter failed the test receipt...");
		


	}
	

	private ReceiptFilter f;
}
