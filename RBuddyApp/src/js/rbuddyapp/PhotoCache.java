package js.rbuddyapp;

import static js.basic.Tools.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class PhotoCache {

	public PhotoCache() {
		this(100000000, 0);
	}

	public PhotoCache(long capacityBytesUsed, int capacityCardinality) {
		if (true) {
			warning("setting capacity low");
			capacityBytesUsed = 5000000;
			capacityCardinality = 5;
		}
		this.capacityBytesUsed = capacityBytesUsed;
		this.capacityCardinality = capacityCardinality;

		priorityQueue = new TreeSet<PhotoEntry>(new Comparator<PhotoEntry>() {
			public int compare(PhotoEntry arg0, PhotoEntry arg1) {
				return arg0.priority - arg1.priority;
			}
		});
		photoMap = new HashMap();

	}

	/**
	 * Read photo from cache if it exists. If it does, it gets moved to the
	 * front of the queue
	 * 
	 * @param receiptId
	 * @return photo if it was in the cache, else null
	 */
	public Drawable readPhoto(int receiptId) {
		if (db)
			pr("readPhoto id " + receiptId + " from: " + this);
		PhotoEntry entry = photoMap.get(receiptId);
		if (entry == null)
			return null;

		removeEntry(entry);
		storePhotoAux(entry.receiptId, entry.drawable);
		if (db)
			pr("updated position, now: " + this);
		return entry.drawable;
	}

	/**
	 * Store photo within cache; replaces any existing photo; moves to head of
	 * cache
	 * 
	 * @param receiptId
	 * @param photo
	 */
	public void storePhoto(int receiptId, BitmapDrawable photo) {
		if (db)
			pr("storePhoto id " + receiptId + " into: " + this);
		PhotoEntry entry = photoMap.get(receiptId);
		if (entry != null) {
			removeEntry(entry);
		}
		storePhotoAux(receiptId, photo);
		if (db)
			pr(" after storing: " + this);
	}

	private void storePhotoAux(int receiptId, BitmapDrawable photo) {
		PhotoEntry entry = new PhotoEntry();
		entry.priority = nextPriority;
		entry.receiptId = receiptId;
		entry.drawable = photo;
		nextPriority += 1;

		priorityQueue.add(entry);
		photoMap.put(receiptId, entry);
		adjustBytesUsed(photo, true);

		while (true) {
			boolean tooBig = false;
			int size = priorityQueue.size();

			// We MUST allow at least one item to be in the cache, or we may end
			// up in an infinite loop; the photo store expects an item
			// just-added to the cache to be there (though I suppose if
			// competing photo requests occur, throttling can result...)

			if (capacityCardinality > 0 && size > capacityCardinality) {
				if (db)
					pr("max cardinality " + capacityCardinality + " exceeded");
				tooBig = true;
			}
			if (capacityBytesUsed > 0 && bytesUsed > capacityBytesUsed
					&& size > 1) {
				if (db)
					pr("max bytes " + capacityBytesUsed
							+ " exceeded, bytesUsed currently " + bytesUsed);
				tooBig = true;
			}
			if (!tooBig)
				break;

			PhotoEntry entry2 = priorityQueue.first();
			if (db)
				pr("removing lowest-priority item " + entry2);
			removeEntry(entry2);
		}

	}

	private void removeEntry(PhotoEntry entry) {
		photoMap.remove(entry.receiptId);
		if (priorityQueue.remove(entry))
			adjustBytesUsed(entry.drawable, false);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(nameOf(this));
		sb.append("\n priorityQueue:");
		for (PhotoEntry ent : priorityQueue) {
			sb.append("\n  " + ent);
		}
		ASSERT(photoMap.size() == priorityQueue.size(), "photoMap size "
				+ photoMap.size() + " disagrees with priority queue "
				+ priorityQueue.size());
		sb.append("\n");
		return sb.toString();
	}

	private void adjustBytesUsed(BitmapDrawable drawable, boolean adding) {
		long nBytes = drawable.getBitmap().getByteCount();
		bytesUsed += nBytes * (adding ? 1 : -1);
		ASSERT(bytesUsed >= 0, "bytesUsed reached " + bytesUsed);
	}

	private static class PhotoEntry {
		int priority;
		int receiptId;
		BitmapDrawable drawable;

		@Override
		public String toString() {
			return "[Pri:" + priority + " Id:" + receiptId + " Bytes:"
					+ drawable.getBitmap().getByteCount() + "]";
		}
	}

	private int nextPriority;
	private TreeSet<PhotoEntry> priorityQueue;
	private Map<Integer, PhotoEntry> photoMap;
	private long bytesUsed;
	private int capacityCardinality;
	private long capacityBytesUsed;
}
