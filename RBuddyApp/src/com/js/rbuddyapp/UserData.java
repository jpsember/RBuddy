package com.js.rbuddyapp;

import android.content.Context;

import com.js.android.AppPreferences;
import com.js.json.JSONParser;
import com.js.json.JSONTools;
import com.js.rbuddy.IReceiptFile;
import com.js.rbuddy.TagSetFile;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;

import static com.google.android.gms.drive.Drive.DriveApi;
import static com.js.android.Tools.*;

import com.js.android.DataStore;
import com.js.android.IPhotoStore;

public class UserData extends DataStore {

	/*
	 * TODO We may want to allow calls to read/write files from other threads by
	 * passing in a Handler object to execute the callback within, other than
	 * the default one which is tied to the UI thread.
	 */

	private static final String FILENAME_USER_ROOT_FOLDER = "RBuddy User Data";
	private static final String FILENAME_RECEIPTS = "Receipts.json";
	private static final String FILENAME_TAGS = "Tags.json";
	private static final String FILENAME_PHOTOS_FOLDER = "Photos";

	// This prefix is combined with the above filenames to produce corresponding
	// keys for the stored preferences
	private static final String PREFERENCE_KEY_PREFIX = "DriveId_";

	public UserData(Context context, GoogleApiClient client) {
		super(client);
		this.mContext = context;
	}

	public IReceiptFile getReceiptFile() {
		return mReceiptFile;
	}

	public TagSetFile getTagSetFile() {
		return mTagSetFile;
	}

	public IPhotoStore getPhotoStore() {
		return mPhotoStore;
	}

	public DriveFile getTagSetDriveFile() {
		return mTagSetDriveFile;
	}

	private void findUserDataFolder() {
		LocateResult r = locateFolder(PREFERENCE_KEY_PREFIX
				+ FILENAME_USER_ROOT_FOLDER,
				DriveApi.getRootFolder(mApiClient), FILENAME_USER_ROOT_FOLDER);
		mUserDataFolder = r.folder;
		if (r.wasCreated) {
			// We must remove any stored keys associated with files/folders
			// lying within the old user data folder, since we couldn't find it
			// and should abandon these other resources as well (otherwise it
			// may succeed at finding them, which is kind of messed up since
			// they will not lie within the new parent folder)
			AppPreferences.removeKey(PREFERENCE_KEY_PREFIX
					+ FILENAME_PHOTOS_FOLDER);
			AppPreferences.removeKey(PREFERENCE_KEY_PREFIX + FILENAME_TAGS);
			AppPreferences.removeKey(PREFERENCE_KEY_PREFIX + FILENAME_RECEIPTS);
		}
	}

	private void findReceiptFile() {
		LocateResult r = locateFile(PREFERENCE_KEY_PREFIX + FILENAME_RECEIPTS,
				mUserDataFolder, FILENAME_RECEIPTS, DriveReceiptFile.MIME_TYPE,
				DriveReceiptFile.INITIAL_CONTENTS.getBytes());
		String contents = blockingReadTextFile(r.file);
		this.mReceiptFile = new DriveReceiptFile(this, r.file, contents);
	}

	private void findTagsFile() {
		LocateResult r = locateFile(PREFERENCE_KEY_PREFIX + FILENAME_TAGS,
				mUserDataFolder, FILENAME_TAGS, JSONTools.JSON_MIME_TYPE,
				TagSetFile.INITIAL_JSON_CONTENTS.getBytes());
		this.mTagSetDriveFile = r.file;
		String contents = blockingReadTextFile(r.file);
		this.mTagSetFile = TagSetFile.parse(new JSONParser(contents));
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
		this.mBackgroundHandler.post(new Runnable() {
			public void run() {
				open_bgndThread(callback);
			}
		});
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
			pr("NOW calling the callback...");
		}
		mUiThreadHandler.post(callback);
	}

	private void findPhotosFolder() {
		LocateResult r = locateFolder(PREFERENCE_KEY_PREFIX
				+ FILENAME_PHOTOS_FOLDER, mUserDataFolder,
				FILENAME_PHOTOS_FOLDER);
		this.mPhotoStore = new DrivePhotoStore(mContext, this, r.folder);
	}

	private DriveFolder mUserDataFolder;
	private IReceiptFile mReceiptFile;
	private DriveFile mTagSetDriveFile;
	private TagSetFile mTagSetFile;
	private IPhotoStore mPhotoStore;
	private Context mContext;
}
