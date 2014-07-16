package com.js.rbuddy;

import static com.js.basic.Tools.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.js.json.*;

public class TagSet implements IJSONEncoder {
	public static final int MAX_SIZE = 5;

	public static TagSet parse(JSONParser json) {
		ArrayList<String> tags = new ArrayList();
		json.enterList();
		while (json.hasNext())
			tags.add(json.nextString());
		json.exit();
		return new TagSet(tags.iterator());
	}

	public TagSet() {
		construct();
	}

	public TagSet(Iterator<String> sequenceOfTags) {
		construct();
		Set<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		while (sequenceOfTags.hasNext()) {
			String tag = sequenceOfTags.next();
			if (set.add(tag)) {
				if (this.tags.size() == MAX_SIZE)
					break;
				this.tags.add(tag);
			}
		}
	}

	private void construct() {
		tags = new ArrayList<String>();
	}

	public Iterator<String> iterator() {
		return tags.iterator();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> iter = tags.iterator(); iter.hasNext();) {
			String name = iter.next();
			if (sb.length() != 0)
				sb.append(", ");
			sb.append(name);
		}
		return sb.toString();
	}

	/**
	 * Parse a string of comma- (or perhaps period-) delimited tags to a set
	 * 
	 * @param s
	 * @return
	 * @throws IllegalArgumentException
	 *             if parsing failed (but impossible for this to happen at
	 *             present)
	 */
	public static TagSet parse(String s) {
		return parse(s, null);
	}

	/**
	 * Parse a string of comma- (or perhaps period-) delimited tags to a set
	 * 
	 * @param s
	 * @param defaultValue
	 *            if not null, and parsing fails, returns this value
	 * @return
	 * @throws IllegalArgumentException
	 *             if parsing failed and no default value was given
	 */
	public static TagSet parse(String s, TagSet defaultValue) {
		ArrayList<String> tagNames = new ArrayList();
		int cursor = 0;
		int lastDelimeter = -1;

		// add a trailing delimeter to perform final tag 'flush'
		s = s + ",";
		while (cursor < s.length()) {
			char c = s.charAt(cursor);
			if (c == ',' || c == '.') {
				String tagName = s.substring(lastDelimeter + 1, cursor);
				tagName = tagName.trim();
				if (!tagName.isEmpty())
					tagNames.add(tagName);
				lastDelimeter = cursor;
			}
			cursor++;
		}
		return new TagSet(tagNames.iterator());
	}

	public int size() {
		return tags.size();
	}

	private List<String> tags;

	@Override
	public void encode(JSONEncoder encoder) {
		encoder.enterList();
		for (Iterator<String> s = tags.iterator(); s.hasNext();) {
			encoder.encode(s.next());
		}
		encoder.exit();
	}

	/**
	 * Move a receipt's tags (if any) to the front of a TagSetFile's queue.
	 * 
	 * @param tagSetFile
	 *            TagSetFile
	 */
	public void moveTagsToFrontOfQueue(TagSetFile tagSetFile) {
		if (db)
			pr("\n\nmoveTagsToFrontOfQueue for " + this);
		Iterator<String> iter = this.iterator();
		while (iter.hasNext()) {
			String tagName = iter.next();
			if (db)
				pr(" ...re-adding tag " + tagName);
			tagSetFile.addTag(tagName);
		}
	}

	/**
	 * @deprecated
	 * 
	 * @param ts
	 * @return
	 */
	public boolean isTagsetInTagsetInclusive(TagSet ts) {

		Iterator<String> iter = ts.iterator();
		while (iter.hasNext()) {
			String tagName = iter.next();
			if (this.isStringInTagset(tagName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @deprecated
	 * 
	 **/
	public boolean isTagsetInTagsetExclusive(TagSet ts) {

		Iterator<String> iter = ts.iterator();
		while (iter.hasNext()) {
			String tagName = iter.next();
			if (this.isStringInTagset(tagName) != true) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param ts
	 * @return true if tagset contains subset ts
	 */
	public boolean contains(TagSet ts) {

		Iterator<String> iter = ts.iterator();
		while (iter.hasNext()) {
			String tagName = iter.next();
			if (this.isStringInTagset(tagName) != true) {
				return false;
			}
		}
		
		return true;
		
	}

	public boolean isStringInTagset(String s) {

		Iterator<String> iter = this.iterator();
		while (iter.hasNext()) {
			String tagName = iter.next();
			if (db)
				pr("comparing " + tagName + " and " + s);
			if (tagName.equals(s)) {
				return true;
			}
		}
		return false;
	}

}
