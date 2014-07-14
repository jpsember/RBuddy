package com.js.rbuddy;

import static com.js.basic.Tools.*;

import com.js.json.*;

public class ReceiptFilter implements IJSONEncoder {

	private boolean minDateActive;
	private JSDate minDate;

	private boolean maxDateActive;
	private JSDate maxDate;

	private boolean minCostActive;
	private Cost minCost;

	private boolean maxCostActive;
	private Cost maxCost;

	private boolean inclusiveTagsActive;
	private TagSet inclusiveTags;

	private boolean exclusiveTagsActive;
	private TagSet exclusiveTags;

	public boolean isMinDateActive() {
		return minDateActive;
	}

	public void setMinDateActive(boolean minDateActive) {
		this.minDateActive = minDateActive;
	}

	public JSDate getMinDate() {
		return minDate;
	}

	public void setMinDate(JSDate minDate) {
		this.minDate = minDate;
	}

	public boolean isMaxDateActive() {
		return maxDateActive;
	}

	public void setMaxDateActive(boolean maxDateActive) {
		this.maxDateActive = maxDateActive;
	}

	public JSDate getMaxDate() {
		return maxDate;
	}

	public void setMaxDate(JSDate maxDate) {
		this.maxDate = maxDate;
	}

	public boolean isMinCostActive() {
		return minCostActive;
	}

	public void setMinCostActive(boolean minCostActive) {
		this.minCostActive = minCostActive;
	}

	public Cost getMinCost() {
		return minCost;
	}

	public void setMinCost(Cost minCost) {
		this.minCost = minCost;
	}

	public boolean isMaxCostActive() {
		return maxCostActive;
	}

	public void setMaxCostActive(boolean maxCostActive) {
		this.maxCostActive = maxCostActive;
	}

	public Cost getMaxCost() {
		return maxCost;
	}

	public void setMaxCost(Cost maxCost) {
		this.maxCost = maxCost;
	}

	public boolean isInclusiveTagsActive() {
		return inclusiveTagsActive;
	}

	public void setInclusiveTagsActive(boolean inclusiveTagsActive) {
		this.inclusiveTagsActive = inclusiveTagsActive;
	}

	public TagSet getInclusiveTags() {
		return inclusiveTags;
	}

	public void setInclusiveTags(TagSet inclusiveTags) {
		this.inclusiveTags = inclusiveTags;
	}

	public boolean isExclusiveTagsActive() {
		return exclusiveTagsActive;
	}

	public void setExclusiveTagsActive(boolean exclusiveTagsActive) {
		this.exclusiveTagsActive = exclusiveTagsActive;
	}

	public TagSet getExclusiveTags() {
		return exclusiveTags;
	}

	public void setExclusiveTags(TagSet exclusiveTags) {
		this.exclusiveTags = exclusiveTags;
	}

	public static ReceiptFilter parse(JSONParser json) {

		ReceiptFilter rf = new ReceiptFilter();

		json.enterMap();
		while (json.hasNext()) {
			String key = json.nextKey();

			// If values are null, the pair can be safely ignored
			if (json.nextIfNull())
				continue;

			if (key.equals("minDateActive")) {
				rf.setMinDateActive(json.nextBoolean());
			} else if (key.equals("minDate")) {
				rf.setMinDate(JSDate.parse(json));
			} else if (key.equals("maxDateActive")) {
				rf.setMaxDateActive(json.nextBoolean());
			} else if (key.equals("maxDate")) {
				rf.setMaxDate(JSDate.parse(json));
			} else if (key.equals("minCostActive")) {
				rf.setMinCostActive(json.nextBoolean());
			} else if (key.equals("minCost")) {
				rf.setMinCost(Cost.parse(json));
			} else if (key.equals("maxCostActive")) {
				rf.setMaxCostActive(json.nextBoolean());
			} else if (key.equals("maxCost")) {
				rf.setMaxCost(Cost.parse(json));
			} else if (key.equals("exclusiveTagsActive")) {
				rf.setExclusiveTagsActive(json.nextBoolean());
			} else if (key.equals("exclusiveTags")) {
				rf.setExclusiveTags(TagSet.parse(json));
			} else if (key.equals("inclusiveTagsActive")) {
				rf.setInclusiveTagsActive(json.nextBoolean());
			} else if (key.equals("inclusiveTags")) {
				rf.setInclusiveTags(TagSet.parse(json));
			} else {
				throw new JSONException("unrecognized key:" + key);
			}
		}
		json.exit();

		return rf;
	}

	@Override
	public void encode(JSONEncoder encoder) {

		encoder.enterMap();
		encoder.encodePair("minDate", getMinDate());
		encoder.encodePair("minDateActive", isMinDateActive());
		encoder.encodePair("maxDate", getMaxDate());
		encoder.encodePair("maxDateActive", isMaxDateActive());
		if (getMinCost() != null)
			encoder.encodePair("minCost", getMinCost().getValue());
		encoder.encodePair("minCostActive", isMinCostActive());
		if (getMaxCost() != null)
			encoder.encodePair("maxCost", getMaxCost().getValue());
		encoder.encodePair("maxCostActive", isMaxCostActive());
		encoder.encodePair("inclusiveTags", getInclusiveTags());
		encoder.encodePair("inclusiveTagsActive", isInclusiveTagsActive());
		encoder.encodePair("exclusiveTags", getExclusiveTags());
		encoder.encodePair("exclusiveTagsActive", isExclusiveTagsActive());
		encoder.exit();

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ReceiptFilter:");
		sb.append("\n minDateActive      : " + minDateActive);
		sb.append("\n minDate            : " + minDate);
		sb.append("\n maxDateActive      : " + maxDateActive);
		sb.append("\n maxDate            : " + maxDate);
		sb.append("\n minCostActive      : " + minCostActive);
		sb.append("\n minCost            : " + minCost);
		sb.append("\n maxCostActive      : " + maxCostActive);
		sb.append("\n maxCost            : " + maxCost);
		sb.append("\n inclusiveTagsActive: " + inclusiveTagsActive);
		sb.append("\n inclusiveTags      : " + inclusiveTags);
		sb.append("\n exclusiveTagsActive: " + exclusiveTagsActive);
		sb.append("\n exclusiveTags      : " + exclusiveTags);
		sb.append("\n");
		return sb.toString();
	}

	/**
	 * Determine if a receipt passes the filter
	 * 
	 * @param r
	 * @return true if receipt satisfies the filter
	 */
	public boolean apply(Receipt r) {

		boolean success = false;
		do {
			if (isMinCostActive() && r.getCost().compare(getMinCost()) < 0)
				break;

			if (isMinDateActive() && r.getDate().compare(getMinDate()) < 0)
				break;

			if (isMaxCostActive() && r.getCost().compare(getMaxCost()) > 0)
				break;

			// passed all the conditions
			success = true;
		} while (false);
		return success;
	}

	// TJS 7 July
	// don't know if i like the name, and place this function is
	// but so what for now...

	public boolean applyFilter(Cost c, JSDate d, TagSet ts) {

		if (db) {
			pr("applying receipt filter");
			pr("Inputs are:");
			pr("Cost");
			pr(c);
			pr("Date");
			pr(d);
			pr("TagSet");
			pr(ts);

			pr(this);
		}
		// cost filtering

		if (c != null) {

			Cost filter_min_cost = this.getMinCost();
			if (filter_min_cost != null) {
				if (c.getValue() < filter_min_cost.getValue()) {
					if (db)
						pr("input cost is less than the filter minimum cost...");
					return false;
				}
			}

			Cost filter_max_cost = this.getMaxCost();
			if (filter_max_cost != null) {
				if (c.getValue() > filter_max_cost.getValue()) {
					if (db)
						pr("input cost is more than the filter maximum cost...");
					return false;
				}
			}
		}

		// date filtering
		if (d != null) {
			JSDate filter_min_date = this.getMinDate();
			JSDate filter_max_date = this.getMaxDate();

			if (filter_min_date != null) {
				if (d.year() < filter_min_date.year()) {
					if (db)
						pr("input date year is less than the filter date year...");
					return false;
				} else if (d.year() == filter_min_date.year()) {
					if (d.month() < filter_min_date.month()) {
						if (db)
							pr("input date month is less than the filter date month...");
						return false;
					} else if (d.month() == filter_min_date.month()) {
						if (d.day() < filter_min_date.day()) {
							if (db)
								pr("input date day is less than filter date day...");
							return false;
						}
					}
				}
			}

			if (filter_max_date != null) {
				if (d.year() > filter_max_date.year()) {
					if (db)
						pr("input date year is greater than the filter date year...");
					return false;
				} else if (d.year() == filter_max_date.year()) {
					if (d.month() > filter_max_date.month()) {
						if (db)
							pr("input date month is greater than the filter date month...");
						return false;
					} else if (d.month() == filter_max_date.month()) {
						if (d.day() > filter_max_date.day()) {
							if (db)
								pr("input date day is greater than filter date day...");
							return false;
						}
					}
				}
			}
		}

		if (ts != null) {
			// TJS 10 July
			// filtering tests
			// i have thought up reasonable definitions that may or may not
			// last...
			// and am not sure what to do about null filter definitions, but
			// have done something...
			//
			// and i'm starting to think there is something obvious that exists
			// or should be implemented in TagSets so that i can get each
			// element
			// one at a time and examine them, and that is gonna have to wait
			// until
			// i have more than 5 minutes to discuss and solve...

			TagSet filter_inclusive_tags = this.getInclusiveTags();
			// TJS 10 July
			// at this point, if the inclusive tag filter exists,
			// each element of the rf.tagset should be "in" the inclusive filter
			// -go thru rf.tagset, and check that each element matches something
			// in the inclusive filter
			if (filter_inclusive_tags != null) {

				if (db) {
					pr("checking inclusive tags...");
					pr("tag set = " + ts);
					pr("inclusive tags = " + filter_inclusive_tags);
				}
				if (ts.isTagsetInTagsetInclusive(filter_inclusive_tags) == false) {
					if (db)
						pr("tag set is not 'inclusively in' the inclusive set, failed on inclusive test");
					return false;
				}
			}

			TagSet filter_exclusive_tags = this.getExclusiveTags();
			// TJS 10 July
			// at this point, if the exclusive tag filter exists,
			// each element of the exclusive tag filter must be "in" the
			// rf.tagset
			// -go thru exclusive tag filter, and check each element matches
			// something in rf.tagset
			if (filter_exclusive_tags != null) {
				if (db) {
					pr("checking exclusive tags...");
					pr("tag set = " + ts);
					pr("exclusive tags = " + filter_exclusive_tags);
				}
				if (ts.isTagsetInTagsetExclusive(filter_exclusive_tags) == false) {
					if (db)
						pr("tag set is not 'exclusively in' the exclusive set, failed on exclusive test");
					return false;
				}
			}
		}

		return true;
	}
}
