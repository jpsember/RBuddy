package js.rbuddy;

import static js.basic.Tools.pr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import js.json.IJSONEncoder;
import js.json.IJSONParser;
import js.json.JSONEncoder;
import js.json.JSONException;
import js.json.JSONParser;

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

	public static final IJSONParser JSON_PARSER = new IJSONParser() {

		@Override
		public Object parse(JSONParser json) {

			ReceiptFilter rf = new ReceiptFilter();

			String key;
			Object value;

			json.enterMap();
			while (json.hasNext()) {

				key = json.nextKey();
				pr("key = " + key);

				value = json.keyValue();
				pr("value = " + value);

				if (value == null)
					continue;

				// i forget if there is a switch statement
				// so i'll use a giant if-else...
				//
				if (key.equals("minDateActive")) {
					// We know that the value is a boolean, but the compiler
					// doesn't.

					// If it ISN'T a boolean, something is wrong and it will
					// throw a ClassCastException.
					// Boolean b = (Boolean)value;

					// read up on 'Java primitive wrapper'
					// and also 'autoboxing'

					rf.setMinDateActive((Boolean) value);

				} else if (key.equals("minDate")) {
					// We know the value is a String, a representation the
					// JSDate can parse
					JSDate date = JSDate.parse((String) value);

					rf.setMinDate(date);

				} else if (key.equals("maxDateActive")) {

					rf.setMaxDateActive((Boolean) value);

				} else if (key.equals("maxDate")) {
					JSDate date = JSDate.parse((String) value);

					rf.setMaxDate(date);

				} else if (key.equals("minCostActive")) {

					rf.setMinCostActive((Boolean) value);

				} else if (key.equals("minCost")) {

					// Cost does not implement JSON yet...
					// Cost cost = Cost.parse((String)value);
					
					Cost cost = new Cost((Double) value);
					
					rf.setMinCost(cost);


				} else if (key.equals("maxCostActive")) {

					rf.setMaxCostActive((Boolean) value);

				} else if (key.equals("maxCost")) {
					// Cost does not implement JSON yet...
					// Cost cost = Cost.parse((String)value);
					pr("Unimplemented key of " + key);

				} else if (key.equals("exclusiveTagsActive")) {

					rf.setExclusiveTagsActive((Boolean) value);

				} else if (key.equals("exclusiveTags")) {

					TagSet tag = TagSet.parse((String) value);

					rf.setExclusiveTags(tag);

				} else if (key.equals("inclusiveTagsActive")) {

					rf.setInclusiveTagsActive((Boolean) value);

				} else if (key.equals("inclusiveTags")) {

					// from above, minDate
					// JSDate date = JSDate.parse((String) value);

					// below dumps core because of exception can't cast
					// ArrayList to String
					// i don't know if this means Tagset has not implemented
					// JSON completely,
					// or if i haven't stumbled on to how to convert an
					// ArrayList to a String.
					// the debug of the ArrayList looks just as i expect.
					// and i don't understand the syntax:
					// ArrayList<String>
					// so i have no idea what to do, my hit-and-miss stabs at
					// getting syntax
					// and conversion to work have all failed...
					// TagSet tag = TagSet.parse( (String)value);
					// and now i see that JSDate which i have modelled from is
					// not really working
					// so i'm just gonna make something up, like JSDate...

					// We know that tagsets are encoded as lists of strings;
					// ideally classes such as TagSet that support JSON are able
					// to parse structured JSON data AFTER the JSON strings have
					// been
					// converted to Java objects (primitives, lists, or maps)

					List tagList = (List) value;
					TagSet ts = new TagSet(tagList.iterator());
					rf.setInclusiveTags(ts);
					pr("\n\n                ----------- just set inclusive tags to "
							+ ts);

				} else {
					throw new JSONException("unrecognized key:" + key);
				}
			}
			json.exit();

			return rf;

		}
	};

	// private List<String> tags;

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
		sb.append("\n minDateActive: " + minDateActive);
		sb.append("\n minDate:       " + minDate);
		sb.append("\n");
		return sb.toString();
	}
}
