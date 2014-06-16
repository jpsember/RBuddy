package js.rbuddy;

//import static js.basic.Tools.*;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//import java.util.TreeSet;
//
//import js.json.IJSONEncoder;
//import js.json.IJSONParser;
//import js.json.JSONEncoder;
//import js.json.JSONParser;

public class ReceiptFilter {

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
	
}
