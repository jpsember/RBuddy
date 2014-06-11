package js.rbuddy;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import js.basic.IJSONEncoder;
import js.basic.IJSONParser;
import js.basic.JSONEncoder;
import js.basic.JSONParser;
import static js.basic.Tools.*;

public class TagSetFile implements IJSONEncoder {

	public TagSetFile(int maxTags) {
		this.maxTags = maxTags;
		tailEntry = new TagEntry(null);
		headEntry = tailEntry;

		tagMap = new TreeMap<String, TagEntry>(String.CASE_INSENSITIVE_ORDER);
	}

	private static final int MAX_TAGS = 200;

	public TagSetFile() {
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
			pr("tagSet now, in reverse priority order:\n" + JSONEncoder.toJSON(this)  
					+ "\n---------------------------");

		return tagExists;
	}

	public Set<String> tags() {
		return tagMap.keySet();
	}

	private static class TagEntry {
		public TagEntry(String name) {
			this.name = name;
		}

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

	private int size;
	private TagEntry headEntry, tailEntry;
	private int maxTags;
	private TreeMap<String, TagEntry> tagMap;

	@Override
	public void encode(JSONEncoder json) {
		// We'll encode the tag names in order from last to first,
		// since this is the only way we can iterate over them in order of
		// recency of use, and also it's natural to add them in this order to
		// end up with the original sequence.
		json.enterList();
		TagEntry entry = tailEntry.next;
		while (entry != null) {
			json.encode(entry.name);
			entry = entry.next;
		}
		json.exitList();
	}

	public static final IJSONParser JSON_PARSER = new IJSONParser() {
		@Override
		public Object parse(JSONParser json) {
			// The tag names appear in order of least-recently-used first. By
			// adding them in this order, the first one processed will end up at
			// the end of the list, as we would like.
			TagSetFile tf = new TagSetFile();
			json.enterList();
			while (json.hasNext())
				tf.addTag(json.nextString());
			json.exit();
			return tf;
		}
	};

}
