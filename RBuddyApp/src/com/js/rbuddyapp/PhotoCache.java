package com.js.rbuddyapp;

import static com.js.basic.Tools.*;

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
		this.mCapacityBytesUsed = capacityBytesUsed;
		this.mCapacityCardinality = capacityCardinality;

		mPriorityQueue = new TreeSet<PhotoEntry>(new Comparator<PhotoEntry>() {
			public int compare(PhotoEntry arg0, PhotoEntry arg1) {
				return arg0.priority - arg1.priority;
			}
		});
		mPhotoMap = new HashMap();

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
		PhotoEntry entry = mPhotoMap.get(receiptId);
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
		PhotoEntry entry = mPhotoMap.get(receiptId);
		if (entry != null) {
			removeEntry(entry);
		}
		storePhotoAux(receiptId, photo);
		if (db)
			pr(" after storing: " + this);
	}

	private void storePhotoAux(int receiptId, BitmapDrawable photo) {
		PhotoEntry entry = new PhotoEntry();
		entry.priority = mNextPriority;
		entry.receiptId = receiptId;
		entry.drawable = photo;
		mNextPriority += 1;

		mPriorityQueue.add(entry);
		mPhotoMap.put(receiptId, entry);
		adjustBytesUsed(photo, true);

		while (true) {
			boolean tooBig = false;
			int size = mPriorityQueue.size();

			// We MUST allow at least one item to be in the cache, or we may end
			// up in an infinite loop; the photo store expects an item
			// just-added to the cache to be there (though I suppose if
			// competing photo requests occur, throttling can result...)

			if (mCapacityCardinality > 0 && size > mCapacityCardinality) {
				if (db)
					pr("max cardinality " + mCapacityCardinality + " exceeded");
				tooBig = true;
			}
			if (mCapacityBytesUsed > 0 && mBytesUsed > mCapacityBytesUsed
					&& size > 1) {
				if (db)
					pr("max bytes " + mCapacityBytesUsed
							+ " exceeded, bytesUsed currently " + mBytesUsed);
				tooBig = true;
			}
			if (!tooBig)
				break;

			PhotoEntry entry2 = mPriorityQueue.first();
			if (db)
				pr("removing lowest-priority item " + entry2);
			removeEntry(entry2);
		}

	}

	public void removePhoto(int receiptId) {
		PhotoEntry entry = mPhotoMap.get(receiptId);
		if (entry != null)
			removeEntry(entry);
	}

	private void removeEntry(PhotoEntry entry) {
		mPhotoMap.remove(entry.receiptId);
		if (mPriorityQueue.remove(entry))
			adjustBytesUsed(entry.drawable, false);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(nameOf(this));
		sb.append("\n priorityQueue:");
		for (PhotoEntry ent : mPriorityQueue) {
			sb.append("\n  " + ent);
		}
		ASSERT(mPhotoMap.size() == mPriorityQueue.size(), "photoMap size "
				+ mPhotoMap.size() + " disagrees with priority queue "
				+ mPriorityQueue.size());
		sb.append("\n");
		return sb.toString();
	}

	private void adjustBytesUsed(BitmapDrawable drawable, boolean adding) {
		long nBytes = drawable.getBitmap().getByteCount();
		mBytesUsed += nBytes * (adding ? 1 : -1);
		ASSERT(mBytesUsed >= 0, "bytesUsed reached " + mBytesUsed);
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

	private int mNextPriority;
	private TreeSet<PhotoEntry> mPriorityQueue;
	private Map<Integer, PhotoEntry> mPhotoMap;
	private long mBytesUsed;
	private int mCapacityCardinality;
	private long mCapacityBytesUsed;
}
