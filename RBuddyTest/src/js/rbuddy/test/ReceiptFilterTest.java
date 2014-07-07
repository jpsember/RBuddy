package js.rbuddy.test;

import js.json.JSONEncoder;
import js.json.JSONParser;
import js.rbuddy.JSDate;
import js.rbuddy.Cost;
import js.rbuddy.ReceiptFilter;
import js.rbuddy.TagSet;
import js.rbuddy.TagSetFile;
import js.testUtils.MyTest;
import static js.basic.Tools.*;

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
		
	
		JSDate d = JSDate.currentDate();		
		pr("got a current date of "+d);
		rf.setMinDate(d);
		
		rf.setMinCostActive(true);
		
		Cost c = new Cost("123.45");
		rf.setMinCost(c);
		
		TagSet ts = TagSet.parse("alpha,bravo,charlie delta,epsilon");
		rf.setInclusiveTags(ts);
		
		String s = JSONEncoder.toJSON(rf);

		pr("converted receiptFilter to JSON string:"+s);
		
		ReceiptFilter rf2 = (ReceiptFilter) JSONParser.parse(s,
				ReceiptFilter.JSON_PARSER);
		
		if (rf2.isMinDateActive())
			pr ("parsed receiptFilter thinks rf minDateActive is TRUE");
		else 
			pr ("parsed receiptFilter thinks rf minDateActive is FALSE");
		
		pr("Entire receiptFilter looks like");
		pr(rf2);
		
//		pr("min Date active: "+rf2.isMinDateActive());
//		pr(rf2.getMinDate());
//	
//		pr("max Date active: "+rf2.isMaxDateActive());
//		pr(rf2.getMaxDate());
//	
//		pr("min Cost active: "+rf2.isMinCostActive());
//		pr(rf2.getMinCost());
//	
//		pr("max Cost active: "+rf2.isMaxCostActive());
//		pr(rf2.getMaxCost());
//
//		pr("InclusiveTags active: "+rf2.isInclusiveTagsActive());
//		pr(rf2.getInclusiveTags());
//	
//		pr("ExclusiveTags active: "+rf2.isExclusiveTagsActive());
//		pr(rf2.getExclusiveTags());
		
//		TagSetFile ts2 = (TagSetFile) JSONParser.parse(s,
//				TagSetFile.JSON_PARSER);
//		assertStringsMatch(toString(ts.tags()), toString(ts2.tags()));
//		assertStringsMatch(s, JSONEncoder.toJSON(ts2));
		}



	private ReceiptFilter f;
}
