package js.rbuddy;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static js.basic.Tools.*;

/**
 * To enforce queue limit, access tags by priority map: int => (name,int)
 * To display tags, access by name set: name
 * 
 * Maybe (name,int) isn't necessary
 * 
 * @author jeff
 *
 */
public class TagSet {

	public TagSet(int maxTags) {
		this.maxTags = maxTags;
		tagMap = new TreeMap<String, TagEntry>();
		tagSet = new TreeSet<String>();
		unimp("sort set by priority, and map by name");
		unimp("rename tag -> name to disambiguate");
		unimp("figure out MapEntry");
	}

	private static final int MAX_TAGS = 200;

	public TagSet() {
		this(MAX_TAGS);
	}

	public int size() {
		return tagSet.size();
	}

	public boolean addTag(String tag) {
		final boolean db = true;
		if (db)
			pr("addTag " + tag);
		TagEntry entry = tagMap.remove(tag);
		if (db)
			pr(" removed tag, got entry " + entry);
		boolean tagExists = (entry != null);
		if (!tagExists) {
			entry = new TagEntry(tag, 0);
		}
		if (entry.priority <= this.priority) {
			entry.priority = incrementPriority();
		}
		tagMap.put(tag, entry);
		tagSet.add(tag);
		if (db) pr("tagSet now:\n"+encode());

		trimSize();
		return tagExists;
	}

	public Set<String> tags() {
		return tagSet;
	}

	private void trimSize() {
		final boolean db = true;
		if (db) pr("trimSize, currently "+tagMap.size()+"/"+tagSet.size()+" (max "+maxTags+")");
		while (tagMap.size() > maxTags) {
			String tag = tagMap.lastKey();
			if (db) pr(" attempting to remove "+tag);
			tagMap.remove(tag);
			tagSet.remove(tag);
		}
	}

	private int incrementPriority() {
		priority++;
		return priority;
	}

	private static class TagEntry {
		public TagEntry(String tag, int priority) {
			this.tag = tag;
			this.priority = priority;
		}

		@Override
		public String toString() {
			return "<" + tag + " #" + priority + ">";
		}

		private String tag;
		private int priority;
	}

	public String encode() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<TagEntry> it = tagMap.values().iterator(); it.hasNext();) {
			TagEntry tagEntry = it.next();
			sb.append(tagEntry.tag);
			sb.append('\n');
		}
		return sb.toString();
	}

	public static TagSet decode(String s) {
		String[] strs = s.split("\\n");
		TagSet set = new TagSet();
		for (int i = strs.length - 1; i >= 0; i--)
			set.addTag(strs[i]);
		return set;
	}

	private int maxTags;
	private int priority;
	private TreeMap<String, TagEntry> tagMap;
	private TreeSet<String> tagSet;
}
