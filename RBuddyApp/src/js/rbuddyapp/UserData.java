package js.rbuddyapp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import js.basic.Files;
import js.json.JSONEncoder;
import js.json.JSONParser;
import js.rbuddy.IReceiptFile;
import js.rbuddy.TagSetFile;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.common.api.Status;

import static com.google.android.gms.drive.Drive.DriveApi;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import static js.basic.Tools.*;

public class UserData {

	/*
	 * TODO We may want to allow calls to read/write files from other threads by
	 * passing in a Handler object to execute the callback within, other than
	 * the default one which is tied to the UI thread.
	 */

	private static final String PREFERENCE_KEY_ROOTFOLDER = "UserData root folder";
	private static final String PREFERENCE_KEY_PHOTOFOLDER = "UserData photos folder";

	private static final String USER_ROOTFOLDER_NAME = "RBuddy User Data";
	private static final String PHOTOSFOLDER_NAME = "Photos";

	/**
	 * Constructor
	 * 
	 * @param app
	 */
	public UserData(RBuddyApp app) {
		RBuddyApp.assertUIThread();
		this.apiClient = app.getGoogleApiClient();

		HandlerThread ht = new HandlerThread("BgndHandler");
		ht.start();
		this.backgroundHandler = new Handler(ht.getLooper());
		this.uiThreadHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message m) {
				warning("ignoring message: " + m);
			}
		};
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

	// // Package visibility to get rid of unused warning
	// MetadataResult inspectFile(String fileIdString) {
	// DriveId fileId = DriveId.decodeFromString(fileIdString);
	// DriveFile file = fileWithId(fileId);
	// return file.getMetadata(apiClient).await();
	// }

	private void findUserDataFolder() {
		final boolean db = true;

		if (db)
			pr("UserData.findUserDataFolder");

		LocateFolderResult r = locateFolder(PREFERENCE_KEY_ROOTFOLDER,
				DriveApi.getRootFolder(apiClient), USER_ROOTFOLDER_NAME);

		userDataFolder = r.folder;
		if (r.wasCreated) {
			// We must remove any stored
			AppPreferences.removeKey(PREFERENCE_KEY_PHOTOFOLDER);
			unimp("remove other file's keys as well");
		}

	}

	private void findReceiptFile() {
		unimp("use similar utility method to look for receipt file, etc");

		DriveId receiptFileId = lookForDriveResource(userDataFolder,
				DriveReceiptFile.RECEIPTFILE_NAME, false);
		DriveFile rf = null;
		if (receiptFileId != null)
			rf = fileWithId(receiptFileId);
		else {
			rf = createTextFile(userDataFolder,
					DriveReceiptFile.RECEIPTFILE_NAME,
					DriveReceiptFile.EMPTY_FILE_CONTENTS);
		}
		String contents = blockingReadTextFile(rf);

		this.receiptFile = new DriveReceiptFile(this, rf, contents);
	}

	/**
	 * Work associated with open() that runs in separate background thread
	 * 
	 * @param callback
	 */
	private void open_bgndThread(Runnable callback) {
		findUserDataFolder();
		findReceiptFile();
		findTagsFile();
		findPhotosFolder();

		if (db) {
			pr("delaying a bit before calling the callback...");
			sleepFor(1500);
			if (db)
				pr("NOW calling the callback...");
		}
		uiThreadHandler.post(callback);
	}

	private void findTagsFile() {
		DriveId tagsFileId = lookForDriveResource(userDataFolder,
				DriveReceiptFile.TAGSFILE_NAME, false);
		DriveFile tagsDriveFile = null;
		if (tagsFileId != null)
			tagsDriveFile = fileWithId(tagsFileId);
		else {
			TagSetFile tf = new TagSetFile();
			String initialTextContents = JSONEncoder.toJSON(tf);
			tagsDriveFile = createTextFile(userDataFolder,
					DriveReceiptFile.TAGSFILE_NAME, initialTextContents);
		}
		this.tagSetDriveFile = tagsDriveFile;

		String content = blockingReadTextFile(tagSetDriveFile);
		this.tagSetFile = (TagSetFile) JSONParser.parse(content,
				TagSetFile.JSON_PARSER);
	}

	// States for getFolder()
	private enum State {
		CHECK_PREFERENCES, //
		SEARCH_PARENT_FOLDER, //
		VERIFY_FOLDER_EXISTS, //
		FOUND_FOLDER, //
		CREATE_NEW_FOLDER, //
		STORE_FOLDER_IN_PREFERENCES, //
	};

	public static class LocateFolderResult {
		public DriveFolder folder;
		public boolean wasCreated;
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
	private LocateFolderResult locateFolder(String preferencesKey,
			DriveFolder parent, String folderName) {

		LocateFolderResult ret = new LocateFolderResult();
		String folderIdString = null;

		State state = State.CHECK_PREFERENCES;
		final boolean db = true;
		if (db)
			pr("\nUserData.getFolder name=" + folderName + " key="
					+ preferencesKey);

		while (true) {
			if (db)
				pr("getFolder, state=" + state);
			State nextState = null;
			switch (state) {
			case CHECK_PREFERENCES: {
				folderIdString = AppPreferences.getString(preferencesKey, null);
				if (folderIdString == null)
					nextState = State.SEARCH_PARENT_FOLDER;
				else
					nextState = State.VERIFY_FOLDER_EXISTS;
			}
				break;

			case VERIFY_FOLDER_EXISTS: {
				try {
					DriveId folderId = DriveId.decodeFromString(folderIdString);
					ret.folder = DriveApi.getFolder(apiClient, folderId);
					nextState = State.FOUND_FOLDER;
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
					nextState = State.CREATE_NEW_FOLDER;
				} else {
					ret.folder = DriveApi.getFolder(apiClient, driveFolderId);
					nextState = State.STORE_FOLDER_IN_PREFERENCES;
				}
			}
				break;

			case CREATE_NEW_FOLDER: {
				MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
						.setTitle(folderName).build();
				DriveFolderResult result = parent.createFolder(
						apiClient, changeSet).await();
				if (!success(result))
					die("failed to create folder: " + folderName);
				ret.folder = result.getDriveFolder();
				ret.wasCreated = true;
				nextState = State.STORE_FOLDER_IN_PREFERENCES;
			}
				break;

			case STORE_FOLDER_IN_PREFERENCES: {
				AppPreferences.putString(preferencesKey, ret.folder
						.getDriveId().encodeToString());
				nextState = State.FOUND_FOLDER;
			}
				break;

			case FOUND_FOLDER:
				return ret;
			}
			ASSERT(nextState != null);
			state = nextState;
		}
	}

	private void findPhotosFolder() {
		LocateFolderResult r = locateFolder(PREFERENCE_KEY_PHOTOFOLDER,
				userDataFolder, PHOTOSFOLDER_NAME);
		this.photoStore = new DrivePhotoStore(this, r.folder);
	}

	/**
	 * Prepare user data for use. Looks for user's data in his Google Drive,
	 * executes callback when done
	 * 
	 * @param callback
	 * @throws RuntimeException
	 *             if unable to create user data folder
	 */
	public void open(final Runnable callback) {
		this.backgroundHandler.post(new Runnable() {
			public void run() {
				open_bgndThread(callback);
			}
		});
	}

	/**
	 * Look for a particular file or folder within a parent folder
	 * 
	 * @return DriveId if found, else null
	 */
	private DriveId lookForDriveResource(DriveFolder parentFolder,
			String resourceName, boolean resourceIsFolder) {

		MetadataBufferResult result = parentFolder.listChildren(apiClient)
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

	private Contents buildContents() {
		DriveApi.ContentsResult r = DriveApi.newContents(apiClient).await();
		if (!success(r))
			die("can't get results");
		return r.getContents();
	}

	/**
	 * @param parentFolder
	 * @param filename
	 * @param contents
	 * @return
	 * @throws RuntimeException
	 *             if failed to create text file
	 */
	private DriveFile createTextFile(DriveFolder parentFolder, String filename,
			String contents) {
		return createBinaryFile(parentFolder, filename, "text/plain",
				contents.getBytes());
	}

	private DriveFile createBinaryFile(DriveFolder parentFolder,
			String filename, String mimeType, byte[] contents) {
		MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
				.setTitle(filename).setMimeType(mimeType).build();
		Contents c = buildContents();

		DriveFile ret = null;
		try {
			OutputStream s = c.getOutputStream();
			s.write(contents);
			s.close();
			DriveFolder.DriveFileResult result = parentFolder.createFile(
					apiClient, changeSet, c).await();
			if (success(result))
				ret = result.getDriveFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	public static String dbPrefix(DriveId d) {
		if (d == null)
			return "<null>";
		String str = d.encodeToString();
		String prefix = str.substring(0, 16);
		return prefix;
	}

	public static String dbPrefix(DriveResource d) {
		if (d == null)
			return "<null>";
		return dbPrefix(d.getDriveId());
	}

	public void writeBinaryFile(FileArguments args) {

		final FileArguments arg = args;
		ASSERT(args.getFilename() != null);

		RBuddyApp.assertUIThread();

		this.backgroundHandler.post(new Runnable() {
			public void run() {
				if (db)
					pr("UserData.writeBinaryFile " + arg);

				DriveFile driveFile = arg.getFile(apiClient);

				// TODO what if file has been deleted?
				if (driveFile == null) {
					arg.setFile(createBinaryFile(arg.getParentFolder(),
							arg.getFilename(), arg.getMimeType(), arg.getData()));
				} else {
					driveFile = arg.getFile(apiClient);
					if (driveFile == null)
						die("could not find DriveFile " + arg);

					ContentsResult cr = driveFile.openContents(apiClient,
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
					Status r = driveFile.commitAndCloseContents(apiClient, c)
							.await();
					if (!success(r))
						die("problem committing and closing");
				}
				if (arg.getCallback() != null)
					uiThreadHandler.post(arg.getCallback());
			}
		});

	}

	public void writeTextFile(FileArguments args, String text) {
		args.setData(text.getBytes());
		writeBinaryFile(args);
	}

	public void readBinaryFile(FileArguments args) {

		RBuddyApp.assertUIThread();
		final FileArguments arg = args;
		this.backgroundHandler.post(new Runnable() {
			public void run() {
				arg.setData(blockingReadBinaryFile(arg.getFile(apiClient)));
				if (arg.getCallback() != null)
					uiThreadHandler.post(arg.getCallback());
			}
		});
	}

	private byte[] blockingReadBinaryFile(DriveFile driveFile) {
		if (db)
			pr("\n\nUserData.blockingReadTextFile " + dbPrefix(driveFile));

		DriveApi.ContentsResult cr = driveFile.openContents(apiClient,
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
		driveFile.discardContents(apiClient, c);
		if (db)
			pr(" returning " + bytes.length + " bytes");
		return bytes;
	}

	private String blockingReadTextFile(DriveFile driveFile) {
		return new String(blockingReadBinaryFile(driveFile));
	}

	public IReceiptFile getReceiptFile() {
		return receiptFile;
	}

	public TagSetFile getTagSetFile() {
		return tagSetFile;
	}

	public DriveFile getTagSetDriveFile() {
		return tagSetDriveFile;
	}

	public IPhotoStore getPhotoStore() {
		return photoStore;
	}

	private DriveFile fileWithId(DriveId id) {
		return DriveApi.getFile(apiClient, id);
	}

	private GoogleApiClient apiClient;
	private Handler uiThreadHandler;
	private Handler backgroundHandler;
	private DriveFolder userDataFolder;
	private IReceiptFile receiptFile;
	private DriveFile tagSetDriveFile;
	private TagSetFile tagSetFile;
	private IPhotoStore photoStore;
}
