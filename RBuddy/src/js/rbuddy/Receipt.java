package js.rbuddy;

import java.util.Comparator;
import java.util.Iterator;

import js.json.*;
import js.basic.StringUtil;

public class Receipt implements IJSONEncoder {

	// Version number. If JSON format of Receipt changes, we increment this.
	// We incorporate the version number into the receipt filename (at least in
	// the 'simple' receipt file implementation) to ensure older, invalid
	// files are not used.
	public static final int VERSION = 3;

	/**
	 * Constructor
	 * 
	 * Sets date to current date, string fields to the empty string, tags empty
	 * set
	 * 
	 * @throws IllegalArgumentException
	 *             if identifier <= 0
	 */
	public Receipt(int identifier) {
		if (identifier <= 0)
			throw new IllegalArgumentException();
		this.id = identifier;
		setDate(JSDate.currentDate());
		this.summary = "";
		this.tags = new TagSet();
		this.cost = new Cost(0);
	}

	public Cost getCost() {
		return this.cost;
	}

	public void setCost(Cost c) {
		this.cost = c;
	}

	public JSDate getDate() {
		return this.date;
	}

	public void setDate(JSDate date) {
		this.date = date;
	}

	public String getSummary() {
		return summary;
	}

	public String getPhotoId() {
		return photoId;
	}

	public void setPhotoId(String s) {
		this.photoId = s;
	}

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

		summary = s_result.toString();

	}

	public int getId() {
		return id;
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

	/**
	 * Get the set of tags
	 * 
	 * @return set of strings
	 */
	public TagSet getTags() {
		return tags;
	}

	/**
	 * Set tags
	 * 
	 * @param set
	 *            set of zero to MAX_TAGS tags
	 */
	public void setTags(TagSet set) {
		this.tags = set;
	}

	/**
	 * Get receipt's tags as a user-displayable string
	 * 
	 * @return tags separated by commas
	 */
	public String getTagsString() {
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = tags.iterator();
		while (it.hasNext()) {
			String s = it.next();
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(s);
		}
		return sb.toString();
	}

	@Override
	public void encode(JSONEncoder j) {
		j.enterList();
		j.encode(getId());
		j.encode(date);
		j.encode(getSummary());
		j.encode(getCost().getValue());
		j.encode(getPhotoId());
		j.encode(getTags());
		j.exit();
	}

	public static final IJSONParser JSON_PARSER = new IJSONParser() {
		@Override
		public Object parse(JSONParser json) {
			json.enterList();
			int id = json.nextInt();
			JSDate date = (JSDate) json.read(JSDate.JSON_PARSER);
			String summary = json.nextString();
			double costValue = json.nextDouble();
			String photoId = json.nextString();
			TagSet tags = (TagSet) json.read(TagSet.JSON_PARSER);

			json.exit();

			Receipt r = new Receipt(id);
			r.summary = summary;
			r.date = date;
			r.tags = tags;
			r.cost = new Cost(costValue);
			r.photoId = photoId;

			return r;

		}
	};

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

	private TagSet tags;
	private JSDate date;
	private String summary;
	private String photoId;
	private int id;
	private Cost cost;

}
