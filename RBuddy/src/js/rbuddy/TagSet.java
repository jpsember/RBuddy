package js.rbuddy;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import js.basic.IJSONEncoder;
import js.basic.JSONEncoder;
import static js.basic.Tools.*;

/**
 * To enforce queue limit, access tags by priority map: int => (name,int) To
 * display tags, access by name set: name
 * 
 * Maybe (name,int) isn't necessary
 * 
 * @author jeff
 * 
 */
public class TagSet implements IJSONEncoder {

	public TagSet(int maxTags) {
		this.maxTags = maxTags;
		tailEntry = new TagEntry(null);
		headEntry = tailEntry;

		tagMap = new TreeMap<String, TagEntry>(String.CASE_INSENSITIVE_ORDER);
	}

	private static final int MAX_TAGS = 200;

	public TagSet() {
		this(MAX_TAGS);
	}

	public int size() {
		return size;
	}

	public boolean addTag(String name) {
		TagEntry entry = tagMap.get(name);
		boolean tagExists = (entry != null);

		if (tagExists) {

			// If tag is already at front, do nothing

			if (entry != headEntry) {
				// Move tag to the front

				// Make this entry the container for its successor's value
				TagEntry next = entry.next;

				entry.name = next.name;
				entry.next = next.next;
				// Update this entry within the map, since its name has changed
				tagMap.put(entry.name, entry);

				// If the successor was the head entry, update it to this one
				// instead
				if (headEntry == next)
					headEntry = entry;

				entry = next;

				entry.name = name;
				entry.next = null;

				// Make the old head point to us
				headEntry.next = entry;
				headEntry = entry;

				// Update this entry within the map, since its name has changed
				tagMap.put(name, entry);
			}
		} else {
			if (size == maxTags) {
				entry = tailEntry.next;

				tagMap.remove(entry.name);
				tailEntry.next = entry.next;

				entry.next = null;
				entry.name = name;
			} else {
				size++;
				entry = new TagEntry(name);
			}
			headEntry.next = entry;
			headEntry = entry;
			// Update the entry associated with the name key, since the entry
			// has changed
			tagMap.put(name, entry);
		}

		if (db)
			pr("tagSet now, in reverse priority order:\n" + encode()
					+ "\n---------------------------");

		return tagExists;
	}

	public Set<String> tags() {
		return tagMap.keySet();
	}

	public String encode() {
		StringBuilder sb = new StringBuilder();
		TagEntry entry = tailEntry.next;
		while (entry != null) {
			sb.append(entry.name);
			sb.append('\n');
			entry = entry.next;
		}
		return sb.toString();
	}

	public static TagSet decode(String s) {
		// We could do this quicker by inserting objects ourselves,
		// but this is simpler and has an asymptotically equivalent runtime.
		String[] strs = s.split("\\n");
		TagSet set = new TagSet();
		for (int i = 0; i < strs.length; i++) {
			set.addTag(strs[i]);
		}
		return set;
	}

	private static class TagEntry {
		public TagEntry(String name) {
			this.name = name;
		}

		// Reenable these lines if debugging is required
		// @Override
		// public String toString() {
		// String s = "<" + name + " => ";
		// if (next != null)
		// s += next.name;
		// else
		// s += "null";
		// s += ">";
		// return s;
		// }

		private String name;
		private TagEntry next;
	}

	/**
	 * Provided for test purposes
	 */
	public void verifyInternalConsistency() {
		Iterator<String> it = tagMap.keySet().iterator();
		while (it.hasNext()) {
			String name = it.next();
			TagEntry entry = tagMap.get(name);
			if (!name.equals(entry.name))
				throw new IllegalStateException("map[" + name + "] points to "
						+ entry);
		}
	}

	/**
	 * Given a set of tag names, construct user-displayable string
	 * 
	 * @param tags
	 * @return
	 */
	public static String formatTagNameSet(Set<String> tags) {
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
	 * Construct an empty tag name set, one that will sort tags into
	 * alphabetical order
	 * 
	 * @return
	 */
	public static Set<String> constructTagNameSet() {
		return new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
	}

	private int size;
	private TagEntry headEntry, tailEntry;
	private int maxTags;
	private TreeMap<String, TagEntry> tagMap;

	@Override
	public void encode(JSONEncoder encoder) {
		// TODO Auto-generated method stub

	}

	/**
	 * Parse a string of comma- (or perhaps period-) delimited tags to a set
	 * 
	 * @param s
	 * @return
	 * @throws IllegalArgumentException
	 *             if parsing failed (but impossible for this to happen at present)
	 */
	public static Set<String> parseTagNameSet(String s) {
		Set<String> tagNameSet = constructTagNameSet();
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
					tagNameSet.add(tagName);
				lastDelimeter = cursor;
			}
			cursor++;
		}
		return tagNameSet;
	}
}
