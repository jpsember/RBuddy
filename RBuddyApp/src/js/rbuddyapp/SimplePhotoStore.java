package js.rbuddyapp;

import static js.basic.Tools.*;

import java.io.File;
import java.io.IOException;
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
		ASSERT(!RBuddyApp.sharedInstance().useGoogleAPI());
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

	@Override
	public void readPhoto(FileArguments arg2) {
		final FileArguments args = arg2;
		backgroundHandler.post(new Runnable() {

			@Override
			public void run() {
				final boolean sleep = false;
				if (sleep)
					Tools.sleepFor(Tools.rnd.nextInt(400) + 120);

				String photoId = args.getFileIdString();
				if (photoId == null)
					throw new IllegalArgumentException(
							"expected photoId to be non-null");
				File f = getFileForPhotoId(photoId);
				try {
					args.setData(Files.readBinaryFile(f));
				} catch (IOException e) {
					die(e);
				}
				if (sleep) {
					Tools.sleepFor(Tools.rnd.nextInt(400) + 120);
				}

				if (args.getCallback() != null) {
					handler.post(args.getCallback());
				}
			}
		});

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

	private File getFileForPhotoId(String photoId) {
		return new File(RBuddyApp.sharedInstance().context()
				.getExternalFilesDir(null), "photo_" + photoId
				+ BitmapUtil.JPEG_EXTENSION);
	}

	private Handler handler;

	private Handler backgroundHandler;

}
