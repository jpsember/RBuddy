package js.rbuddyapp;

import static js.basic.Tools.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Handler.Callback;
import js.basic.Files;
import js.basic.Tools;

public class SimplePhotoStore implements IPhotoStore {

	// Simulate a network or other delay?
	private static final boolean SIMULATE_DELAY = true;

	private static final Random random = new Random(System.currentTimeMillis());

	public SimplePhotoStore() {
		ASSERT(!RBuddyApp.sharedInstance().useGoogleAPI());

		listenersMap = new HashMap();

		HandlerThread ht = new HandlerThread("SimplePhotoStore BgndHandler");
		ht.start();
		this.backgroundHandler = new Handler(ht.getLooper());

		this.handler = new Handler(new Callback() {
			@Override
			public boolean handleMessage(Message m) {
				warning("SimplePhotoStore handler not handling message: " + m);
				return false;
			}
		});
	}

	@Override
	public void storePhoto(FileArguments args) {
		ASSERT(args.getFilename() != null);
		String photoId = args.getFileIdString();

		byte[] jpeg = args.getData();

		if (photoId == null) {
			// This of course is not guaranteed to produce unique values, but
			// it's good enough for test purposes
			photoId = "RND_" + Math.abs(random.nextInt());
		}

		try {
			File f = getFileForPhotoId(photoId);
			Files.writeBinaryFile(f, jpeg);
			if (db)
				pr(" wrote to file " + f);
		} catch (IOException e) {
			die(e);
		}
		args.setFileId(photoId);
		if (args.getCallback() != null)
			args.getCallback().run();
	}

	private static void sleep() {
		// final boolean db = true;
		if (SIMULATE_DELAY) {
			if (db)
				pr("...simulating delay...");
			Tools.sleepFor((Tools.rnd.nextInt(800) + 350) / 2);
			if (db)
				pr("...done delay");
		}
	}

	@Override
	public void deletePhoto(FileArguments arg2) {
		final FileArguments args = arg2;
		backgroundHandler.post(new Runnable() {

			@Override
			public void run() {
				String photoId = args.getFileIdString();
				if (photoId == null)
					throw new IllegalArgumentException(
							"expected photoId to be non-null");
				File f = getFileForPhotoId(photoId);
				boolean deleted = f.delete();
				if (!deleted)
					warning("failed to delete file: " + f);
				if (args.getCallback() != null) {
					handler.post(args.getCallback());
				}
			}
		});
	}

	@Override
	public void addPhotoListener(int receiptId, IPhotoListener listener) {
		final boolean db = true;
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
			pr(dumpListeners(listenersMap));
	}

	@Override
	public void removePhotoListener(int receiptId, IPhotoListener listener) {
		final boolean db = true;
		if (db)
			pr("\nremovePhotoListener receiptId " + receiptId + ", listener "
					+ listener);
		Set<IPhotoListener> listeners = listenersMap.get(receiptId);
		if (listeners == null)
			return;
		listenersMap.remove(receiptId);
		if (db)
			pr(dumpListeners(listenersMap));
	}

	@Override
	public void readPhoto(final int receiptId, final String fileIdString) {
		backgroundHandler.post(new Runnable() {
			@Override
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
				Bitmap bmp = BitmapFactory
						.decodeByteArray(jpeg, 0, jpeg.length);
				final Drawable d = new BitmapDrawable(RBuddyApp
						.sharedInstance().context().getResources(), bmp);
				handler.post(new Runnable() {
					@Override
					public void run() {
						notifyListenersOfDrawable(receiptId, fileIdString, d);
					}
				});

			}
		});
	}

	private void notifyListenersOfDrawable(int receiptId, String photoId,
			Drawable d) {
		Set<IPhotoListener> listeners = listenersMap.get(receiptId);
		if (listeners == null)
			return;
		for (IPhotoListener listener : listeners) {
			sleep();
			listener.drawableAvailable(d, receiptId, photoId);
		}
	}

	private File getFileForPhotoId(String photoId) {
		return new File(RBuddyApp.sharedInstance().context()
				.getExternalFilesDir(null), "photo_" + photoId
				+ BitmapUtil.JPEG_EXTENSION);
	}

	public static String dumpListeners(
			Map<Integer, Set<IPhotoListener>> listenersMap) {
		StringBuilder sb = new StringBuilder();
		sb.append("------\n SimplePhotoStore listeners:\n");
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

	private Map<Integer, Set<IPhotoListener>> listenersMap;

	private Handler handler;

	private Handler backgroundHandler;

}
