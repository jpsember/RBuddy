package js.rbuddyapp;

import static js.basic.Tools.*;

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
import js.basic.Files;

public class SimplePhotoStore implements IPhotoStore {

	public SimplePhotoStore() {
		listenersMap = new HashMap();

		// Construct ui and background task handlers
		this.uiHandler = new Handler(Looper.getMainLooper());

		HandlerThread handlerThread = new HandlerThread(
				"PhotoStore bgndHandler");
		handlerThread.start();
		this.backgroundHandler = new Handler(handlerThread.getLooper());

		this.simulateDelay = true;
	}

	@Override
	public void readPhoto(final int receiptId, final String fileIdString) {
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
				sleep();
				final Drawable d = convertJPEGToDrawable(jpeg);
				notifyListenersOfDrawable(receiptId, fileIdString, d);
			}
		});
	}

	@Override
	public void storePhoto(final FileArguments args) {
		ASSERT(args.getFilename() != null);
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
				runOnUIThread(args.getCallback());
			}
		});
	}

	protected void runOnUIThread(Runnable r) {
		if (r == null)
			return;
		uiHandler.post(r);
	}

	protected void sleep() {
		if (simulateDelay) {
			warning("adding simulated delays");
			sleepFor((rnd.nextInt(800) + 350));
		}
	}

	@Override
	public void deletePhoto(final FileArguments args) {
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
	protected void notifyListenersOfDrawable(final int receiptId,
			final String photoId, final Drawable d) {
		runOnUIThread(new Runnable() {
			public void run() {
				Set<IPhotoListener> listeners = listenersMap.get(receiptId);
				if (listeners == null)
					return;
				for (IPhotoListener listener : listeners) {
					sleep();
					listener.drawableAvailable(d, receiptId, photoId);
				}
			}
		});
	}

	private File getFileForPhotoId(String photoId) {
		return new File(RBuddyApp.sharedInstance().context()
				.getExternalFilesDir(null), "photo_" + photoId
				+ BitmapUtil.JPEG_EXTENSION);
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

	public static Drawable convertJPEGToDrawable(byte[] jpeg) {
		Bitmap bmp = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
		return new BitmapDrawable(RBuddyApp.sharedInstance().context()
				.getResources(), bmp);
	}

	private Map<Integer, Set<IPhotoListener>> listenersMap;

	// Handler for executing tasks serially on the UI thread
	private Handler uiHandler;

	// Handler for executing tasks in the background
	protected Handler backgroundHandler;

	protected boolean simulateDelay;
}
