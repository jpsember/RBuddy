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

	// @Override
	// public void encode(JSONEncoder encoder) {
	// throw new UnsupportedOperationException("unimplemented");
	// }

	public static ReceiptFilter parse(JSONParser json) {

			ReceiptFilter rf = new ReceiptFilter();

			json.enterMap();
			while (json.hasNext()) {

				String key = json.nextKey();
				pr("key = " + key);

				if (json.nextIfNull())
					continue;

				// i forget if there is a switch statement
				// so i'll use a giant if-else...
				//
				// ...There is a switch statement, but it doesn't work for
				// strings.
				//
				if (key.equals("minDateActive")) {
					// We know that the value is a boolean, but the compiler
					// doesn't.

					// If it ISN'T a boolean, something is wrong and it will
					// throw a ClassCastException.

					rf.setMinDateActive(json.nextBoolean());

				} else if (key.equals("minDate")) {
					JSDate date = JSDate.parse(json);
					rf.setMinDate(date);

				} else if (key.equals("maxDateActive")) {

					rf.setMaxDateActive(json.nextBoolean());

				} else if (key.equals("maxDate")) {
					JSDate date = JSDate.parse(json);

					rf.setMaxDate(date);

				} else if (key.equals("minCostActive")) {

					rf.setMinCostActive(json.nextBoolean());

				} else if (key.equals("minCost")) {

					// Cost does not implement JSON yet...
					// Cost cost = Cost.parse((String)value);
					
					Cost cost = Cost.parse(json);
					
					rf.setMinCost(cost);

				} else if (key.equals("maxCostActive")) {

					rf.setMaxCostActive(json.nextBoolean());

				} else if (key.equals("maxCost")) {
					rf.setMaxCost(Cost.parse(json));

				} else if (key.equals("exclusiveTagsActive")) {

					rf.setExclusiveTagsActive(json.nextBoolean());

				} else if (key.equals("exclusiveTags")) {

					TagSet tag = TagSet.parse(json);

					rf.setExclusiveTags(tag);

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

	// TJS 7 July
	// don't know if i like the name, and place this function is
	// but so what for now...
	
	public boolean applyFilter(Cost c, JSDate d, TagSet ts) {

		pr("applying receipt filter");
		pr("Inputs are:");
		pr ("Cost");
		pr (c);
		pr ("Date");
		pr (d);
		pr ("TagSet");
		pr (ts);
		
		pr (this);
		
//		if (this.isMinCostActive()) {
//		}
		
		return true;
	}

}
