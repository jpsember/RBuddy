package js.rbuddyapp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import js.basic.Files;
//import js.basic.Tools;
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
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.common.api.Status;

import static com.google.android.gms.drive.Drive.DriveApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import static js.basic.Tools.*;

public class UserData {

	private static final String PREFERENCES_NAME = "RBuddy";
	private static final String PREFERENCE_KEY_ROOTFOLDER = "UserData root folder";
	private static final String USER_ROOTFOLDER_NAME = "RBuddy User Data";
	private static final String RECEIPTFILE_NAME = "Receipts.json";
	private static final String TAGSFILE_NAME = "Tags.json";
	private static final String PHOTOSFOLDER_NAME = "Photos";

	/**
	 * Constructor
	 * 
	 * @param app
	 */
	public UserData(RBuddyApp app) {
		RBuddyApp.assertUIThread();
		this.app = app;
		this.apiClient = app.getGoogleApiClient();

		HandlerThread ht = new HandlerThread("BgndHandler");
		ht.start();
		this.backgroundHandler = new Handler(ht.getLooper());

		this.handler = new Handler(new Callback() {
			@Override
			public boolean handleMessage(Message m) {
				warning("UserData handler not handling message: " + m);
				return false;
			}
		});

	}

	/**
	 * Read string from app preferences (we'll probably move this to some other
	 * class later)
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	private String getPreferenceString(String key, String defaultValue) {
		SharedPreferences preferences = app.context().getSharedPreferences(
				PREFERENCES_NAME, Context.MODE_PRIVATE);
		return preferences.getString(key, defaultValue);
	}

	/**
	 * Store string in app preferences (we'll probably move this to some other
	 * class later)
	 * 
	 * @param key
	 * @param value
	 */
	private void setPreferenceString(String key, String value) {
		SharedPreferences preferences = app.context().getSharedPreferences(
				PREFERENCES_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();
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

	private MetadataResult inspectFolder(String folderIdString) {
		DriveId folderId = DriveId.decodeFromString(folderIdString);
		DriveFolder folder = DriveApi.getFolder(apiClient, folderId);
		return folder.getMetadata(apiClient).await();
	}

	// Package visibility to get rid of unused warning
	MetadataResult inspectFile(String fileIdString) {
		DriveId fileId = DriveId.decodeFromString(fileIdString);
		DriveFile file = fileWithId(fileId);
		return file.getMetadata(apiClient).await();
	}

	private void findUserDataFolder() {
		if (db)
			pr("UserData.findUserDataFolder");
		RBuddyApp.assertNotUIThread();
		String storedFolderIdString = getPreferenceString(
				PREFERENCE_KEY_ROOTFOLDER, "");
		String rootFolderIdString = storedFolderIdString;
		if (db)
			pr(" id of user folder, read from preferences: "
					+ rootFolderIdString);

		if (rootFolderIdString.isEmpty()) {
			if (db)
				pr(" looking for user root folder");
			rootFolderIdString = lookForUserRootFolder();
			if (db)
				pr(" looking for user root folder produced: "
						+ rootFolderIdString);
		}

		if (rootFolderIdString != null) {
			if (db)
				pr(" verifying that root folder id is valid");
			MetadataResult result = inspectFolder(rootFolderIdString);
			if (!success(result)
					|| !doesMetadataMatchHealthyUserRoot(result.getMetadata()))
				rootFolderIdString = null;
		}

		if (rootFolderIdString == null) {
			if (db)
				pr(" folder id is null, creating one");
			rootFolderIdString = createUserRootFolder();
		}

		if (rootFolderIdString == null) {
			throw new RuntimeException(
					"unable to create Google Drive RBuddy root folder");
		}

		userDataFolder = DriveApi.getFolder(apiClient,
				DriveId.decodeFromString(rootFolderIdString));

		if (db)
			pr(" userDataFolder=" + dbPrefix(userDataFolder));

		if (!storedFolderIdString.equals(rootFolderIdString)) {
			storedFolderIdString = rootFolderIdString;
			setPreferenceString(PREFERENCE_KEY_ROOTFOLDER, storedFolderIdString);
			if (db)
				pr(" wrote new folder id string " + storedFolderIdString);
		}
	}

	private void findReceiptFile() {
		DriveId receiptFileId = lookForDriveResource(userDataFolder,
				RECEIPTFILE_NAME, false);

		if (receiptFileId == null) {
			DriveFile receiptFile = createTextFile(userDataFolder,
					RECEIPTFILE_NAME, DriveReceiptFile.EMPTY_FILE_CONTENTS);
			receiptFileId = receiptFile.getDriveId();
		}

		DriveFile rf = fileWithId(receiptFileId);
		this.receiptFile = new DriveReceiptFile(this, rf);
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
		handler.post(callback);
	}

	private void findTagsFile() {
		DriveId tagsFileId = lookForDriveResource(userDataFolder,
				TAGSFILE_NAME, false);

		if (tagsFileId == null) {
			TagSetFile tf = new TagSetFile();
			String initialTextContents = JSONEncoder.toJSON(tf);
			DriveFile tagsFile = createTextFile(userDataFolder, TAGSFILE_NAME,
					initialTextContents);
			tagsFileId = tagsFile.getDriveId();
		}

		this.tagSetDriveFile = fileWithId(tagsFileId);

		String content = blockingReadTextFile(tagSetDriveFile);
		TagSetFile tfFile = (TagSetFile) JSONParser.parse(content,
				TagSetFile.JSON_PARSER);
		this.tagSetFile = tfFile;

	}

	private void findPhotosFolder() {
		unimp("maybe store drive ids in preferences so we don't need to look");
		DriveId photosFolderId = lookForDriveResource(userDataFolder,
				PHOTOSFOLDER_NAME, true);
		if (photosFolderId == null) {
			// DriveFolder rootFolder = DriveApi.getRootFolder(apiClient);
			MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
					.setTitle(PHOTOSFOLDER_NAME).build();
			DriveFolderResult result = userDataFolder.createFolder(apiClient,
					changeSet).await();
			if (!success(result))
				die("failed to create photos folder");

			photosFolderId = result.getDriveFolder().getDriveId();
		}
		this.photoStore = new DrivePhotoStore(this, DriveApi.getFolder(
				apiClient, photosFolderId));
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
	 * In the case where no user root folder was stored in the preferences, look
	 * in the drive to find one
	 * 
	 * @return encoded DriveId if found, else null
	 */
	private String lookForUserRootFolder() {
		if (db)
			pr(hey() + "looking for user root folder");

		DriveFolder rootFolder = DriveApi.getRootFolder(apiClient);
		MetadataBufferResult result = rootFolder.listChildren(apiClient)
				.await();
		if (!success(result)) {
			return null;
		}

		MetadataBuffer buffer = result.getMetadataBuffer();
		Iterator<Metadata> iter = buffer.iterator();
		String foundId = null;
		while (iter.hasNext()) {
			Metadata m = iter.next();
			if (db)
				pr(" iterating; title=" + m.getTitle() + " mimetype="
						+ m.getMimeType() + " trashed=" + m.isTrashed()
						+ " folder=" + m.isFolder());
			if (!doesMetadataMatchHealthyUserRoot(m))
				continue;
			if (db)
				pr(" ................. we found it!");
			foundId = m.getDriveId().encodeToString();
		}
		buffer.close();
		return foundId;
	}

	/**
	 * In the case where no user root folder was stored in the preferences, look
	 * in the drive to find one
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

	/**
	 * See if the file or folder described by metadata matches a 'healthy' user
	 * data folder
	 * 
	 * @param m
	 * @return true if so
	 */
	private boolean doesMetadataMatchHealthyUserRoot(Metadata m) {
		if (!m.isFolder())
			return false;
		if (m.isTrashed())
			return false;
		if (!(m.getTitle().equals(USER_ROOTFOLDER_NAME)))
			return false;
		return true;
	}

	/**
	 * Create a user data folder
	 * 
	 * @return encoded DriveId of the folder, if successful; else null
	 */
	private String createUserRootFolder() {
		if (db)
			pr(hey() + "attempting to createUserRootFolder");

		DriveFolder rootFolder = DriveApi.getRootFolder(apiClient);
		MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(
				USER_ROOTFOLDER_NAME).build();
		DriveFolderResult result = rootFolder
				.createFolder(apiClient, changeSet).await();
		if (!success(result))
			return null;

		DriveFolder folder = result.getDriveFolder();
		if (db)
			pr(" getDriveFolder returned " + folder);
		return folder.getDriveId().encodeToString();
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
		//
		// DriveFile ret = null;
		//
		// MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
		// .setTitle(filename).setMimeType("text/plain").build();
		// Contents c = buildContents();
		//
		// try {
		// OutputStream s = c.getOutputStream();
		// s.write(contents.getBytes());
		// s.close();
		// DriveFolder.DriveFileResult result = parentFolder.createFile(
		// apiClient, changeSet, c).await();
		//
		// if (success(result))
		// ret = result.getDriveFile();
		// } catch (IOException e) {
		// throw new RuntimeException(e);
		// }
		//
		// return ret;
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

				// DriveId resultDriveId = arg.driveId;
				DriveFile driveFile = null;
				if (arg.getFileId() != null) {
					driveFile = fileWithId(arg.getFileId());
					// TODO what if file has been deleted?
				}

				if (driveFile == null) {
					arg.setFileId(createBinaryFile(arg.getParentFolder(),
							arg.getFilename(), arg.getMimeType(), arg.getData())
							.getDriveId());
				} else {
					driveFile = fileWithId(arg.getFileId());
					if (driveFile == null)
						die("could not find DriveFile " + arg.getFileId());

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
					handler.post(arg.getCallback());
			}
		});

	}

	/**
	 * @param driveFile
	 * @param text
	 * @param callback
	 *            if not null, calls this when task complete
	 */
	public void writeTextFile(final DriveFile driveFile, final String text,
			final Runnable callback) {

		unimp("we can just call WriteBinaryFile with appropriate parameters; also use new FileArguments object instead");

		RBuddyApp.assertUIThread();
		this.backgroundHandler.post(new Runnable() {
			public void run() {
				if (db)
					pr("UserData.writeTextFile " + dbPrefix(driveFile));
				ContentsResult cr = driveFile.openContents(apiClient,
						DriveFile.MODE_WRITE_ONLY, null).await();
				if (!success(cr))
					die("failed to get contents");
				Contents c = cr.getContents();
				try {
					OutputStream s = c.getOutputStream();
					s.write(text.getBytes());
					s.close();
					if (db)
						pr(" wrote text " + text);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				Status r = driveFile.commitAndCloseContents(apiClient, c)
						.await();
				if (!success(r))
					die("problem committing and closing");
				if (callback != null)
					handler.post(callback);
			}
		});
	}

	/**
	 * Read text file asynchronously
	 * 
	 * @param driveFile
	 *            file containing text file
	 * @param callback
	 *            run() method called when read is complete
	 * @param output
	 *            contents of text file stored here
	 */
	public void readTextFile(final DriveFile driveFile,
			final Runnable callback, final ArrayList output) {
		warning("use new FileArguments object instead");
		RBuddyApp.assertUIThread();
		output.clear();
		this.backgroundHandler.post(new Runnable() {
			public void run() {
				String text = blockingReadTextFile(driveFile);
				output.add(text);
				handler.post(callback);
			}
		});
	}

	public void readBinaryFile(FileArguments args) {
		RBuddyApp.assertUIThread();
		// args.returnValue = null;
		final FileArguments arg = args;
		this.backgroundHandler.post(new Runnable() {
			public void run() {
				arg.setData(blockingReadBinaryFile(fileWithId(arg.getFileId())));
				if (arg.getCallback() != null)
					handler.post(arg.getCallback());
			}
		});
	}


	public byte[] blockingReadBinaryFile(DriveFile driveFile) {
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

	public String blockingReadTextFile(DriveFile driveFile) {
		if (db)
			pr("\n\nUserData.blockingReadTextFile " + dbPrefix(driveFile));

		DriveApi.ContentsResult cr = driveFile.openContents(apiClient,
				DriveFile.MODE_READ_ONLY, null).await();
		if (!success(cr))
			die("can't get results");
		Contents c = cr.getContents();

		String text = null;
		try {
			text = Files.readTextFile(c.getInputStream());
		} catch (IOException e) {
			die("Failed to read text file", e);
		}
		driveFile.discardContents(apiClient, c);
		if (db)
			pr(" returning file contents: " + text);
		return text;
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

	public DriveFile fileWithId(DriveId id) {
		return DriveApi.getFile(apiClient, id);
	}

	private RBuddyApp app;
	private GoogleApiClient apiClient;
	private Handler handler;
	private Handler backgroundHandler;
	private DriveFolder userDataFolder;
	private IReceiptFile receiptFile;
	private DriveFile tagSetDriveFile;
	private TagSetFile tagSetFile;
	private IPhotoStore photoStore;
}
