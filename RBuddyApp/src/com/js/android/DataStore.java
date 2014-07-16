package com.js.android;

import static com.google.android.gms.drive.Drive.DriveApi;
import static com.js.android.Tools.assertUIThread;
import static com.js.basic.Tools.ASSERT;
import static com.js.basic.Tools.db;
import static com.js.basic.Tools.die;
import static com.js.basic.Tools.pr;
import static com.js.basic.Tools.stackTrace;
import static com.js.basic.Tools.warning;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.js.basic.Files;

public class DataStore {

	/**
	 * Constructor
	 * 
	 * @param client
	 *            GoogleApiClient connection to data store
	 */
	public DataStore(GoogleApiClient client) {
		assertUIThread();
		this.mApiClient = client;

		HandlerThread ht = new HandlerThread("BgndHandler");
		ht.start();
		this.mBackgroundHandler = new Handler(ht.getLooper());
		this.mUiThreadHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message m) {
				warning("ignoring message: " + m);
			}
		};
	}

	/**
	 * Write bytes to file. If file doesn't exist yet, both parent folder and
	 * filename must be specified
	 * 
	 * @param args
	 */
	public void writeBinaryFile(final FileArguments arg) {

		assertUIThread();

		this.mBackgroundHandler.post(new Runnable() {
			public void run() {
				if (db)
					pr("UserData.writeBinaryFile " + arg);

				DriveFile driveFile = arg.getFile(mApiClient);

				// TODO what if file has been deleted?
				if (driveFile == null) {
					if (arg.getFilename() == null
							|| arg.getParentFolder() == null)
						throw new IllegalArgumentException(
								"must not be null; filename="
										+ arg.getFilename() + ", parentFolder:"
										+ arg.getParentFolder());
					arg.setFileId(createBinaryFile(arg.getParentFolder(),
							arg.getFilename(), arg.getMimeType(), arg.getData()));
				} else {
					driveFile = arg.getFile(mApiClient);
					if (driveFile == null)
						die("could not find DriveFile " + arg);

					ContentsResult cr = driveFile.openContents(mApiClient,
							DriveFile.MODE_WRITE_ONLY, null).await();
					if (!success(cr))
						die("failed to get contents");
					Contents c = cr.getContents();
					try {
						OutputStream s = c.getOutputStream();
						byte[] data = arg.getData();
						s.write(data);
						s.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					Status r = driveFile.commitAndCloseContents(mApiClient, c)
							.await();
					if (!success(r))
						die("problem committing and closing");
				}
				if (arg.getCallback() != null)
					mUiThreadHandler.post(arg.getCallback());
			}
		});

	}

	public void readBinaryFile(final FileArguments arg) {
		assertUIThread();
		this.mBackgroundHandler.post(new Runnable() {
			public void run() {
				arg.setData(blockingReadBinaryFile(arg.getFile(mApiClient)));
				if (arg.getCallback() != null)
					mUiThreadHandler.post(arg.getCallback());
			}
		});
	}

	public void writeTextFile(FileArguments args, String text) {
		args.setData(text.getBytes());
		writeBinaryFile(args);
	}

	/**
	 * Locate DriveFile, by looking in preferences. If not found, 1) look for it
	 * in the filesystem and create it if necessary; and 2) store its new id
	 * within the preferences
	 * 
	 * @param preferencesKey
	 *            key where this file's DriveId is stored in the app preferences
	 * @param parent
	 *            the parent folder, if it needs to be constructed
	 * @param filename
	 *            the name of to give the new file, if it needs to be
	 *            constructed
	 * @param contents
	 *            initial contents of file, if it needs to be created; if null,
	 *            leaves it empty
	 * @return LocateResult
	 */
	protected LocateResult locateFile(String preferencesKey,
			DriveFolder parent, String filename, String mimeType,
			byte[] contents) {
		LocateResult ret = new LocateResult();
		String fileIdString = null;

		State state = State.CHECK_PREFERENCES;
		if (db)
			pr("\nUserData.locateFile name=" + filename + " key="
					+ preferencesKey);

		while (true) {
			if (db)
				pr("locateFile, state=" + state);
			State nextState = null;
			switch (state) {
			case CHECK_PREFERENCES: {
				fileIdString = AppPreferences.getString(preferencesKey, null);
				if (fileIdString == null)
					nextState = State.SEARCH_PARENT_FOLDER;
				else
					nextState = State.VERIFY_EXISTS;
			}
				break;

			case VERIFY_EXISTS: {
				try {
					DriveId fileId = DriveId.decodeFromString(fileIdString);
					ret.file = DriveApi.getFile(mApiClient, fileId);
					nextState = State.FOUND;
				} catch (Throwable e) {
					warning("problem decoding/locating file: " + e);
					AppPreferences.removeKey(preferencesKey);
					nextState = State.SEARCH_PARENT_FOLDER;
				}
			}
				break;

			case SEARCH_PARENT_FOLDER: {
				DriveId driveFileId = lookForDriveResource(parent, filename,
						false);
				if (driveFileId == null) {
					nextState = State.CREATE_NEW;
				} else {
					ret.file = DriveApi.getFile(mApiClient, driveFileId);
					nextState = State.STORE_ID_IN_PREFERENCES;
				}
			}
				break;

			case CREATE_NEW: {
				if (contents == null)
					contents = new byte[0];

				ret.file = createBinaryFile(parent, filename, mimeType,
						contents);
				ret.wasCreated = true;
				nextState = State.STORE_ID_IN_PREFERENCES;
			}
				break;

			case STORE_ID_IN_PREFERENCES: {
				AppPreferences.putString(preferencesKey, ret.file.getDriveId()
						.encodeToString());
				nextState = State.FOUND;
			}
				break;

			case FOUND:
				return ret;
			}
			ASSERT(nextState != null);
			state = nextState;
		}
	}

	/**
	 * Determine drive folder, by looking in preferences. If not found, 1) look
	 * for it in the filesystem and create it if necessary; and 2) store its new
	 * id within the preferences
	 * 
	 * @param preferencesKey
	 *            key where this folder's DriveId is stored in the app
	 *            preferences
	 * @param parent
	 *            the parent folder, if it needs to be constructed
	 * @param folderName
	 *            the name of the folder, if it needs to be constructed
	 * @return folder
	 */
	protected LocateResult locateFolder(String preferencesKey,
			DriveFolder parent, String folderName) {

		LocateResult ret = new LocateResult();
		String folderIdString = null;

		State state = State.CHECK_PREFERENCES;
		if (db)
			pr("\nUserData.locateFolder name=" + folderName + " key="
					+ preferencesKey);

		while (true) {
			if (db)
				pr("locateFolder, state=" + state);
			State nextState = null;
			switch (state) {
			case CHECK_PREFERENCES: {
				folderIdString = AppPreferences.getString(preferencesKey, null);
				if (folderIdString == null)
					nextState = State.SEARCH_PARENT_FOLDER;
				else
					nextState = State.VERIFY_EXISTS;
			}
				break;

			case VERIFY_EXISTS: {
				try {
					DriveId folderId = DriveId.decodeFromString(folderIdString);
					ret.folder = DriveApi.getFolder(mApiClient, folderId);
					nextState = State.FOUND;
				} catch (Throwable e) {
					warning("problem decoding/locating folder: " + e);
					AppPreferences.removeKey(preferencesKey);
					nextState = State.SEARCH_PARENT_FOLDER;
				}
			}
				break;

			case SEARCH_PARENT_FOLDER: {
				DriveId driveFolderId = lookForDriveResource(parent,
						folderName, true);
				if (driveFolderId == null) {
					nextState = State.CREATE_NEW;
				} else {
					ret.folder = DriveApi.getFolder(mApiClient, driveFolderId);
					nextState = State.STORE_ID_IN_PREFERENCES;
				}
			}
				break;

			case CREATE_NEW: {
				MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
						.setTitle(folderName).build();
				DriveFolderResult result = parent.createFolder(mApiClient,
						changeSet).await();
				if (!success(result))
					die("failed to create folder: " + folderName);
				ret.folder = result.getDriveFolder();
				ret.wasCreated = true;
				nextState = State.STORE_ID_IN_PREFERENCES;
			}
				break;

			case STORE_ID_IN_PREFERENCES: {
				AppPreferences.putString(preferencesKey, ret.folder
						.getDriveId().encodeToString());
				nextState = State.FOUND;
			}
				break;

			case FOUND:
				return ret;
			}
			ASSERT(nextState != null);
			state = nextState;
		}
	}

	protected static class LocateResult {
		public DriveFolder folder;
		public DriveFile file;
		public boolean wasCreated;
	}

	protected DriveFile createBinaryFile(DriveFolder parentFolder,
			String filename, String mimeType, byte[] contents) {
		MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
				.setTitle(filename).setMimeType(mimeType).build();

		DriveFile ret = null;
		try {
			DriveApi.ContentsResult r = DriveApi.newContents(mApiClient)
					.await();
			if (!success(r))
				die("can't get results");
			Contents c = r.getContents();
			OutputStream s = c.getOutputStream();
			s.write(contents);
			s.close();
			DriveFolder.DriveFileResult result = parentFolder.createFile(
					mApiClient, changeSet, c).await();
			if (success(result))
				ret = result.getDriveFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	/**
	 * Look for a particular file or folder within a parent folder
	 * 
	 * @return DriveId if found, else null
	 */
	private DriveId lookForDriveResource(DriveFolder parentFolder,
			String resourceName, boolean resourceIsFolder) {

		MetadataBufferResult result = parentFolder.listChildren(mApiClient)
				.await();
		if (!success(result))
			return null;
		MetadataBuffer buffer = result.getMetadataBuffer();
		Iterator<Metadata> iter = buffer.iterator();
		DriveId foundId = null;
		while (iter.hasNext()) {
			Metadata m = iter.next();
			if (m.isTrashed())
				continue;
			if (m.isFolder() != resourceIsFolder)
				continue;
			if (!(m.getTitle().equals(resourceName)))
				continue;
			foundId = m.getDriveId();
			break;
		}
		buffer.close();
		return foundId;
	}

	/**
	 * Convenience method that verifies a Drive API call was successful
	 * 
	 * @param result
	 * @return true if successful
	 */
	private static boolean success(Result result) {
		if (result.getStatus().isSuccess())
			return true;
		pr("Drive API call failed; " + stackTrace(1, 1) + "; "
				+ result.toString() + " status=" + result.getStatus());
		return false;
	}

	private static String dbPrefix(DriveId d) {
		if (d == null)
			return "<null>";
		String str = d.encodeToString();
		String prefix = str.substring(0, 16);
		return prefix;
	}

	private static String dbPrefix(DriveResource d) {
		if (d == null)
			return "<null>";
		return dbPrefix(d.getDriveId());
	}

	private byte[] blockingReadBinaryFile(DriveFile driveFile) {
		if (db)
			pr("\n\nUserData.blockingReadTextFile " + dbPrefix(driveFile));

		DriveApi.ContentsResult cr = driveFile.openContents(mApiClient,
				DriveFile.MODE_READ_ONLY, null).await();
		if (!success(cr))
			die("can't get results");
		Contents c = cr.getContents();

		byte[] bytes = null;
		try {
			bytes = Files.readBytes(c.getInputStream());
		} catch (IOException e) {
			die("Failed to read binary file", e);
		}
		driveFile.discardContents(mApiClient, c);
		if (db)
			pr(" returning " + bytes.length + " bytes");
		return bytes;
	}

	protected String blockingReadTextFile(DriveFile driveFile) {
		return new String(blockingReadBinaryFile(driveFile));
	}

	// States for locateFile/Folder
	private enum State {
		CHECK_PREFERENCES, //
		SEARCH_PARENT_FOLDER, //
		VERIFY_EXISTS, //
		CREATE_NEW, //
		STORE_ID_IN_PREFERENCES, //
		FOUND, //
	};

	protected GoogleApiClient mApiClient;
	protected Handler mUiThreadHandler;
	protected Handler mBackgroundHandler;
}
