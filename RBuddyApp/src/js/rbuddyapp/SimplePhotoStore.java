package js.rbuddyapp;

import static js.basic.Tools.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Handler.Callback;
import js.basic.Files;
import js.basic.Tools;

public class SimplePhotoStore implements IPhotoStore {

	private static final Random random = new Random(System.currentTimeMillis());

	public SimplePhotoStore() {
		ASSERT(!RBuddyApp.useGoogleAPI);
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
		ASSERT(args.filename != null,
				"filename must be defined for simple photo store");
		String photoId = args.filename;
		byte[] jpeg = args.data;
		if (jpeg == null)
			jpeg = new byte[0];
		if (db)
			pr("\n\n" + stackTrace() + "  storePhoto id=" + photoId
					+ " length=" + jpeg.length);
		if (photoId == null) {
			// This of course is not guaranteed to produce unique values, but
			// it's just for test purposes
			photoId = "RND_" + Math.abs(random.nextInt());
			if (db)
				pr(" chose new random id " + photoId);
		}

		try {
			File f = getFileForPhotoId(photoId);
			Files.writeBinaryFile(f, jpeg);
			if (db)
				pr(" wrote to file " + f);
		} catch (IOException e) {
			die(e);
		}
		args.filename = photoId;
		if (args.callback != null)
			args.callback.run();
	}

	@Override
	public void storePhoto(String photoId, byte[] jpeg, Runnable callback,
			ArrayList returnValue) {
		if (db)
			pr("\n\n" + stackTrace() + "  storePhoto id=" + photoId
					+ " length=" + jpeg.length);
		if (photoId == null) {
			// This of course is not guaranteed to produce unique values, but
			// it's just for test purposes
			photoId = "RND_" + Math.abs(random.nextInt());
			if (db)
				pr(" chose new random id " + photoId);
		}

		try {
			File f = getFileForPhotoId(photoId);
			Files.writeBinaryFile(f, jpeg);
			if (db)
				pr(" wrote to file " + f);
		} catch (IOException e) {
			die(e);
		}
		returnValue.clear();
		returnValue.add(photoId);
		if (callback != null)
			callback.run();
	}

	@Override
	public void readPhoto(FileArguments arg2) {
		final FileArguments args = arg2;
		backgroundHandler.post(new Runnable() {

			@Override
			public void run() {
				final boolean sleep = false;

				if (db)
					pr("Background handler, sleeping a bit...");
				if (sleep)
					Tools.sleepFor(Tools.rnd.nextInt(400) + 120);

				String photoId = args.filename;
				if (photoId == null)
					throw new IllegalArgumentException(
							"SimplePhotoStore requires args.filename to hold photo id");
				File f = getFileForPhotoId(photoId);
				try {
					args.data = Files.readBinaryFile(f);
				} catch (IOException e) {
					die(e);
				}
				if (db)
					pr(" read jpeg of length " + args.data.length);
				if (sleep) {
					if (db)
						pr("sleeping a bit...");
					Tools.sleepFor(Tools.rnd.nextInt(400) + 120);
				}

				if (args.callback != null) {
					handler.post(args.callback);
				}
			}
		});

	}

	@Override
	public void readPhoto(String photoId, Runnable callback,
			ArrayList returnValue) {
		if (db)
			pr("\n\n\nreadPhoto id=" + photoId);
		if (photoId == null)
			throw new IllegalArgumentException();
		File f = getFileForPhotoId(photoId);
		byte[] jpg = null;
		try {
			jpg = Files.readBinaryFile(f);
		} catch (IOException e) {
			die(e);
		}
		if (db)
			pr(" read jpeg of length " + jpg.length);
		returnValue.clear();
		returnValue.add(jpg);
		if (callback != null)
			callback.run();
	}

	private File getFileForPhotoId(String photoId) {
		return new File(RBuddyApp.sharedInstance().context()
				.getExternalFilesDir(null), "photo_" + photoId
				+ BitmapUtil.JPEG_EXTENSION);
	}

	private Handler handler;

	private Handler backgroundHandler;

}
