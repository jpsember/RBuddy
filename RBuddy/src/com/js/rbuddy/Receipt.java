package com.js.rbuddy;

import java.util.Comparator;

import com.js.basic.StringUtil;
import com.js.json.*;

public class Receipt implements IJSONEncoder {

	// Version number. If JSON format of Receipt changes, we increment this.
	// We incorporate the version number into the receipt filename (at least in
	// the 'simple' receipt file implementation) to ensure older, invalid
	// files are not used.
	public static final int VERSION = 4;

	/**
	 * Constructor
	 * 
	 * Sets date to current date, string fields to the empty string, tags empty
	 * set
	 * 
	 * @param identifier
	 *            unique, nonnegative identifier to uniquely identify this
	 *            receipt
	 * @throws IllegalArgumentException
	 *             if identifier <= 0
	 */
	public Receipt(int identifier) {
		if (identifier <= 0)
			throw new IllegalArgumentException();
		this.mId = identifier;
		setDate(JSDate.currentDate());
		this.mSummary = "";
		this.mTags = new TagSet();
		this.mCost = new Cost(0);
	}

	public int getId() {
		return mId;
	}

	/**
	 * Set the cost (or amount)
	 * 
	 * @param c
	 *            Cost
	 */
	public void setCost(Cost c) {
		this.mCost = c;
	}

	public Cost getCost() {
		return this.mCost;
	}

	/**
	 * Set the date
	 * 
	 * @param date
	 */
	public void setDate(JSDate date) {
		this.mDate = date;
	}

	public JSDate getDate() {
		return this.mDate;
	}

	/**
	 * Set the photo identifier, a string that can be used, for instance, to
	 * identify the filename or resource containing a photograph of this receipt
	 * 
	 * @param s
	 *            photo identifier, or null
	 */
	public void setPhotoId(String s) {
		this.mPhotoId = s;
	}

	public String getPhotoId() {
		return mPhotoId;
	}

	/**
	 * Set the summary, a possibly lengthy description of the receipt
	 * 
	 * @param s
	 */
	public void setSummary(String s) {

		s = s.trim();

		int state = 1;
		StringBuilder s_result = new StringBuilder();

		int i_pos;
		for (i_pos = 0; i_pos < s.length(); i_pos++) {

			char c_thischar = s.charAt(i_pos);

			boolean b_is_whitespace = (c_thischar <= ' ');

			switch (state) {

			// last character was not whitespace
			case 1:
				// but this one is...
				if (b_is_whitespace) {
					state = 2;
				} else {
					s_result.append(c_thischar);
					state = 1;
				}
				break;

			// last character was whitespace
			case 2:
				// and so is this one...
				if (b_is_whitespace) {
					state = 2;
				} else {
					s_result.append(' ');
					s_result.append(c_thischar);
					state = 1;
				}
				break;
			}

		}

		mSummary = s_result.toString();

	}

	public String getSummary() {
		return mSummary;
	}

	/**
	 * Set the tags, a set of keywords related to the receipt
	 * 
	 * @param set
	 */
	public void setTags(TagSet set) {
		this.mTags = set;
	}

	public TagSet getTags() {
		return mTags;
	}

	@Override
	public String toString() {
		JSONEncoder json = new JSONEncoder();
		json.encode(this);
		return "Receipt " + json;
	}

	/**
	 * For test purposes, build a random receipt
	 * 
	 * @return
	 */
	public static Receipt buildRandom(int id) {
		Receipt r = new Receipt(id);
		r.setDate(JSDate.buildRandom());
		r.setSummary(StringUtil.randomString(30));
		r.setCost(Cost.buildRandom());
		return r;
	}

	@Override
	public void encode(JSONEncoder j) {
		j.enterList();
		j.encode(getId());
		j.encode(mDate);
		j.encode(getSummary());
		j.encode(getCost().getValue());
		j.encode(getPhotoId());
		j.encode(getTags());
		j.exit();
	}

	public static Receipt parse(JSONParser json) {
		json.enterList();
		int id = json.nextInt();
		JSDate date = JSDate.parse(json);
		String summary = json.nextString();
		double costValue = json.nextDouble();
		String photoId = json.nextString();
		TagSet tags = TagSet.parse(json);

		json.exit();

		Receipt r = new Receipt(id);
		r.mSummary = summary;
		r.mDate = date;
		r.mTags = tags;
		r.mCost = new Cost(costValue);
		r.mPhotoId = photoId;

		return r;
	}

	/**
	 * Comparator for sorting receipts by date; ties are broken using ids
	 */
	public static final Comparator<Receipt> COMPARATOR_SORT_BY_DATE = new Comparator<Receipt>() {
		@Override
		public int compare(Receipt r0, Receipt r1) {
			int result = r0.getDate().toString()
					.compareTo(r1.getDate().toString());
			if (result == 0)
				result = r0.getId() - r1.getId();
			return result;
		}
	};

	private TagSet mTags;
	private JSDate mDate;
	private String mSummary;
	private String mPhotoId;
	private int mId;
	private Cost mCost;

}
