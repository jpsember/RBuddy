package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.js.android.AppPreferences;
import com.js.android.BitmapUtil;
import com.js.basic.Files;
import com.js.android.IPhotoListener;
import com.js.android.FileArguments;
import com.js.android.IPhotoStore;

public class SimplePhotoStore implements IPhotoStore {

	private static final boolean TRACE_LISTENERS = false;

	public SimplePhotoStore(IRBuddyActivity activity) {
		this.mActivity = activity;
		for (Variant v : Variant.values()) {
			mListenerMaps.put(v, new HashMap());
		}

		// Construct ui and background task handlers
		this.mUiHandler = new Handler(Looper.getMainLooper());

		HandlerThread handlerThread = new HandlerThread(
				"PhotoStore bgndHandler");
		handlerThread.start();
		this.mBackgroundHandler = new Handler(handlerThread.getLooper());

		mCacheMap = new HashMap();

		if (!mActivity.usingGoogleAPI()) {
			// Use reduced cache capacities for test purposes
			mCacheMap.put(Variant.THUMBNAIL, new PhotoCache(200000, 5));
			mCacheMap.put(Variant.FULLSIZE, new PhotoCache(5000000, 5));
		} else {
			for (Variant v : Variant.values())
				mCacheMap.put(v, new PhotoCache());
		}
	}

	protected PhotoCache cacheFor(Variant variant) {
		return mCacheMap.get(variant);
	}

	/**
	 * Determine if the appropriate cache holds the photo, and notify its
	 * listeners if so
	 * 
	 * @param receiptId
	 *            id of photo
	 * @param fileIdString
	 * @param thumbnail
	 *            true if seeking thumbnail
	 * @return true if photo was found in cache
	 */
	protected boolean readPhotoWithinCache(final int receiptId,
			final String fileIdString, final Variant variant) {

		PhotoCache cache = cacheFor(variant);
		Drawable d = cache.readPhoto(receiptId);

		if (d != null) {
			if (db)
				pr("  found in cache: " + d);
			notifyListenersOfDrawable(receiptId, d, variant);
			return true;
		}

		// If we didn't find the photo in the cache, and we were looking for the
		// thumbnail, see if it exists in the fullsize cache, and if so,
		// construct a thumbnail from it

		if (variant == Variant.THUMBNAIL) {
			final Drawable dFull = cacheFor(Variant.FULLSIZE).readPhoto(
					receiptId);
			if (db)
				pr("looking in regular cache; got " + dFull);

			if (dFull != null) {
				if (db)
					pr("  found in regular : " + dFull);
				mBackgroundHandler.post(new Runnable() {
					public void run() {
						sleep();
						BitmapDrawable bd = (BitmapDrawable) dFull;
						Bitmap scaledBitmap = BitmapUtil.scaleBitmap(
								bd.getBitmap(), THUMBNAIL_HEIGHT, false);
						final BitmapDrawable dThumb = new BitmapDrawable(
								mActivity.getContext().getResources(),
								scaledBitmap);
						if (db)
							pr(" scaled to size "
									+ dThumb.getBitmap().getByteCount());

						mUiHandler.post(new Runnable() {
							public void run() {
								cacheFor(variant).storePhoto(receiptId, dThumb);
							}
						});
						notifyListenersOfDrawable(receiptId, dThumb, variant);
					}
				});
				return true;
			}
		}
		toast(mActivity.getContext(), "readPhoto " + receiptId + " (variant "
				+ variant
				+ ") wasn't in cache: " + cacheFor(variant));
		return false;
	}

	@Override
	public void readPhoto(final int receiptId, final String fileIdString,
			final Variant variant) {
		if (readPhotoWithinCache(receiptId, fileIdString, variant)) {
			return;
		}

		mBackgroundHandler.post(new Runnable() {
			public void run() {
				sleep();
				String photoId = fileIdString;
				if (photoId == null)
					throw new IllegalArgumentException(
							"expected photoId to be non-null");
				File f = getFileForPhotoId(photoId);
				byte[] jpeg = null;
				try {
					jpeg = Files.readBinaryFile(f);
				} catch (IOException e) {
					die(e);
				}
				convertJPEGAndCache(jpeg, receiptId, fileIdString, variant);
			}
		});
	}

	protected void convertJPEGAndCache(byte[] jpeg, final int receiptId,
			final String fileIdString, final Variant variant) {
		sleep();
		final BitmapDrawable d = convertJPEGToDrawable(jpeg);
		postAddFullSizeDrawableToCache(receiptId, d);

		// Request image again, now that it's guaranteed to be in the
		// cache
		mUiHandler.post(new Runnable() {
			public void run() {
				readPhoto(receiptId, fileIdString, variant);
			}
		});
	}

	/**
	 * Add (full size) drawable to cache
	 * 
	 * @param receiptId
	 * @param drawable
	 */
	protected void postAddFullSizeDrawableToCache(final int receiptId,
			final BitmapDrawable drawable) {
		mUiHandler.post(new Runnable() {
			public void run() {
				cacheFor(Variant.FULLSIZE).storePhoto(receiptId, drawable);
			}
		});
	}

	protected void removeCachedVersions(int receiptId) {
		for (Variant v : Variant.values())
			cacheFor(v).removePhoto(receiptId);
	}

	@Override
	public void storePhoto(int receiptId, final FileArguments args) {
		removeCachedVersions(receiptId);
		mBackgroundHandler.post(new Runnable() {
			public void run() {
				String photoId = args.getFileIdString();
				byte[] jpeg = args.getData();
				if (photoId == null) {
					// This of course is not guaranteed to produce unique
					// values, but it's good enough for test purposes
					photoId = "RND_" + Math.abs(rnd.nextInt());
				}

				try {
					File f = getFileForPhotoId(photoId);
					Files.writeBinaryFile(f, jpeg);
				} catch (IOException e) {
					die(e);
				}
				args.setFileId(photoId);

				runOnUIThread(new Runnable() {
					public void run() {
						if (args.getCallback() != null)
							args.getCallback().run();
					}
				});
			}
		});
	}

	protected void runOnUIThread(Runnable r) {
		if (r == null)
			return;
		mUiHandler.post(r);
	}

	protected void sleep() {
		if (AppPreferences.getBoolean(PREFERENCE_KEY_PHOTO_DELAY, false)) {
			sleepFor((rnd.nextInt(800) + 350));
		}
	}

	@Override
	public void deletePhoto(int receiptId, final FileArguments args) {
		mBackgroundHandler.post(new Runnable() {
			public void run() {
				String photoId = args.getFileIdString();
				if (photoId == null)
					throw new IllegalArgumentException(
							"expected photoId to be non-null");
				File f = getFileForPhotoId(photoId);
				boolean deleted = f.delete();
				if (!deleted)
					warning("failed to delete file: " + f);
				runOnUIThread(args.getCallback());
			}
		});
	}

	private Map<Integer, Set<IPhotoListener>> getListenerMap(Variant variant) {
		return mListenerMaps.get(variant);
	}

	@Override
	public void addPhotoListener(int receiptId, Variant variant,
			IPhotoListener listener) {
		final boolean db = TRACE_LISTENERS;
		if (db)
			pr(hey() + "receiptId " + receiptId + " variant " + variant
					+ " listener " + listener);

		Map<Integer, Set<IPhotoListener>> listenersMap = getListenerMap(variant);
		Set<IPhotoListener> listeners = listenersMap.get(receiptId);
		if (listeners == null) {
			listeners = new HashSet<IPhotoListener>();
			listenersMap.put(receiptId, listeners);
		}
		listeners.add(listener);
		if (db)
			pr(dumpListeners());
	}

	@Override
	public void removePhotoListener(int receiptId, Variant variant,
			IPhotoListener listener) {
		final boolean db = TRACE_LISTENERS;
		if (db)
			pr(hey() + "receiptId " + receiptId + " variant " + variant
					+ " listener " + listener);

		Map<Integer, Set<IPhotoListener>> listenersMap = getListenerMap(variant);
		Set<IPhotoListener> listeners = listenersMap.get(receiptId);
		if (listeners == null)
			return;
		listeners.remove(listener);
		if (listeners.isEmpty())
			listenersMap.remove(receiptId);
		if (db)
			pr(dumpListeners());
	}

	@Override
	public void pushPhoto(int ownerId, String photoId) {
		if (photoId == null)
			return;

		// Read both full-size and thumbnail versions of the photo;
		// the cache logic will notify any listeners
		readPhoto(ownerId, photoId, Variant.FULLSIZE);
		readPhoto(ownerId, photoId, Variant.THUMBNAIL);
	}

	/**
	 * This can be called from any thread. It notifies listeners, on the UI
	 * thread, that a drawable is available
	 * 
	 * @param receiptId
	 * @param photoId
	 * @param d
	 */
	private void notifyListenersOfDrawable(final int receiptId,
			final Drawable d, final Variant variant) {
		final boolean db = TRACE_LISTENERS;
		if (db)
			pr(hey() + "receiptId " + receiptId + " drawable " + d
					+ " variant " + variant);

		runOnUIThread(new Runnable() {
			public void run() {
				Map<Integer, Set<IPhotoListener>> listenersMap = getListenerMap(variant);
				Set<IPhotoListener> listeners = listenersMap.get(receiptId);
				if (listeners == null)
					return;
				for (IPhotoListener listener : listeners) {
					if (db)
						pr(" drawableAvailable sending to listener " + listener);
					listener.drawableAvailable(d, receiptId, variant);
				}
			}
		});
	}

	private File getFileForPhotoId(String photoId) {
		return new File(mActivity.getContext().getExternalFilesDir(null),
				"photo_" + photoId
				+ BitmapUtil.JPEG_EXTENSION);
	}

	public String dumpListeners() {
		StringBuilder sb = new StringBuilder();
		sb.append("------\n " + nameOf(this) + " listeners:\n");
		for (Variant v : Variant.values()) {
			sb.append(v.name() + "\n");
			Map<Integer, Set<IPhotoListener>> listenersMap = getListenerMap(v);
			for (int receiptId : listenersMap.keySet()) {
				Set<IPhotoListener> set = listenersMap.get(receiptId);
				sb.append("  id " + receiptId + " : ");
				for (IPhotoListener x : set) {
					sb.append(x + " ");
				}
				sb.append("\n");
			}
		}
		sb.append("------\n\n");
		return sb.toString();
	}

	protected BitmapDrawable convertJPEGToDrawable(byte[] jpeg) {
		Bitmap bmp = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
		return new BitmapDrawable(mActivity.getContext().getResources(), bmp);
	}

  // Maps of photo listeners, keyed by variant
	private Map<IPhotoStore.Variant, Map> mListenerMaps = new HashMap();

	// Handler for executing tasks serially on the UI thread
	protected Handler mUiHandler;

	// Handler for executing tasks in the background
	protected Handler mBackgroundHandler;

	private Map<Variant, PhotoCache> mCacheMap;
	private IRBuddyActivity mActivity;
}
