package com.js.rbuddyapp;

import static com.js.basic.Tools.*;

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
import com.js.basic.Files;

public class SimplePhotoStore implements IPhotoStore {

	public SimplePhotoStore() {
		listenersMap = new HashMap();

		this.app = RBuddyApp.sharedInstance();

		// Construct ui and background task handlers
		this.uiHandler = new Handler(Looper.getMainLooper());

		HandlerThread handlerThread = new HandlerThread(
				"PhotoStore bgndHandler");
		handlerThread.start();
		this.backgroundHandler = new Handler(handlerThread.getLooper());

		if (!app.useGoogleAPI()) {
			warning("constructing PhotoCaches with reduced capacity");
			this.thumbnailCache = new PhotoCache(200000, 5);
			this.regularCache = new PhotoCache(5000000, 5);
		} else {
			this.thumbnailCache = new PhotoCache();
			this.regularCache = new PhotoCache();
		}
	}

	protected PhotoCache cacheFor(boolean thumbnail) {
		return thumbnail ? thumbnailCache : regularCache;
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
			final String fileIdString, boolean thumbnail) {
		if (db)
			pr(hey() + " receipt " + receiptId + " thumb " + thumbnail);

		PhotoCache cache = cacheFor(thumbnail);
		Drawable d = cache.readPhoto(receiptId);

		if (d != null) {
			if (db)
				pr("  found in cache: " + d);
			notifyListenersOfDrawable(receiptId, d, thumbnail);
			return true;
		}

		// If we didn't find the photo in the cache, and we were looking for the
		// thumbnail, see if it exists in the fullsize cache, and if so,
		// construct a thumbnail from it

		if (thumbnail) {
			final Drawable dFull = regularCache.readPhoto(receiptId);
			if (db)
				pr("looking in regular cache; got " + dFull);

			if (dFull != null) {
				if (db)
					pr("  found in regular : " + dFull);
				backgroundHandler.post(new Runnable() {
					public void run() {
						sleep();
						BitmapDrawable bd = (BitmapDrawable) dFull;
						Bitmap scaledBitmap = BitmapUtil.scaleBitmap(
								bd.getBitmap(), THUMBNAIL_HEIGHT, false);
						final BitmapDrawable dThumb = new BitmapDrawable(app
								.context().getResources(), scaledBitmap);
						if (db)
							pr(" scaled to size "
									+ dThumb.getBitmap().getByteCount());

						uiHandler.post(new Runnable() {
							public void run() {
								thumbnailCache.storePhoto(receiptId, dThumb);
							}
						});
						notifyListenersOfDrawable(receiptId, dThumb, true);
					}
				});
				return true;
			}
		}
		app.toast("readPhoto " + receiptId + " (thumb " + thumbnail
				+ ") wasn't in cache: " + cacheFor(thumbnail));
		return false;
	}

	@Override
	public void readPhoto(final int receiptId, final String fileIdString,
			final boolean thumbnail) {
		if (readPhotoWithinCache(receiptId, fileIdString, thumbnail)) {
			return;
		}

		backgroundHandler.post(new Runnable() {
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
				convertJPEGAndCache(jpeg, receiptId, fileIdString, thumbnail);
			}
		});
	}

	protected void convertJPEGAndCache(byte[] jpeg, final int receiptId,
			final String fileIdString, final boolean thumbnail) {
		sleep();
		final BitmapDrawable d = convertJPEGToDrawable(jpeg);
		postAddFullSizeDrawableToCache(receiptId, d);

		// Request image again, now that it's guaranteed to be in the
		// cache
		uiHandler.post(new Runnable() {
			public void run() {
				readPhoto(receiptId, fileIdString, thumbnail);
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
		uiHandler.post(new Runnable() {
			public void run() {
				regularCache.storePhoto(receiptId, drawable);
			}
		});
	}

	protected void removeCachedVersions(int receiptId) {
		regularCache.removePhoto(receiptId);
		thumbnailCache.removePhoto(receiptId);
	}

	@Override
	public void storePhoto(int receiptId, final FileArguments args) {
		removeCachedVersions(receiptId);
		backgroundHandler.post(new Runnable() {
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
		uiHandler.post(r);
	}

	protected void sleep() {
		if (AppPreferences.getBoolean(PREFERENCE_KEY_PHOTO_DELAY, false)) {
			sleepFor((rnd.nextInt(800) + 350));
		}
	}

	@Override
	public void deletePhoto(int receiptId, final FileArguments args) {
		backgroundHandler.post(new Runnable() {
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

	@Override
	public void addPhotoListener(int receiptId, IPhotoListener listener) {
		if (db)
			pr("addPhotoListener receiptId " + receiptId + ", listener "
					+ listener);
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
	public void removePhotoListener(int receiptId, IPhotoListener listener) {
		Set<IPhotoListener> listeners = listenersMap.get(receiptId);
		if (listeners == null)
			return;
		listeners.remove(listener);
		if (listeners.isEmpty())
			listenersMap.remove(receiptId);
		if (db)
			pr(dumpListeners());
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
			final Drawable d, final boolean thumbnail) {
		runOnUIThread(new Runnable() {
			public void run() {
				Set<IPhotoListener> listeners = listenersMap.get(receiptId);
				if (listeners == null)
					return;
				for (IPhotoListener listener : listeners) {
					sleep();
					listener.drawableAvailable(d, receiptId, thumbnail);
				}
			}
		});
	}

	private File getFileForPhotoId(String photoId) {
		return new File(app.context().getExternalFilesDir(null), "photo_"
				+ photoId + BitmapUtil.JPEG_EXTENSION);
	}

	public String dumpListeners() {
		StringBuilder sb = new StringBuilder();
		sb.append("------\n " + nameOf(this) + " listeners:\n");
		for (int receiptId : listenersMap.keySet()) {
			Set<IPhotoListener> set = listenersMap.get(receiptId);
			sb.append("  id " + receiptId + " : ");
			for (IPhotoListener x : set) {
				sb.append(x + " ");
			}
			sb.append("\n");
		}
		sb.append("------\n\n");
		return sb.toString();
	}

	protected BitmapDrawable convertJPEGToDrawable(byte[] jpeg) {
		Bitmap bmp = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
		return new BitmapDrawable(app.context().getResources(), bmp);
	}

	private RBuddyApp app;

	private Map<Integer, Set<IPhotoListener>> listenersMap;

	// Handler for executing tasks serially on the UI thread
	protected Handler uiHandler;

	// Handler for executing tasks in the background
	protected Handler backgroundHandler;

	private PhotoCache regularCache;
	private PhotoCache thumbnailCache;
}
