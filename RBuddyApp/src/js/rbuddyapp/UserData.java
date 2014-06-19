package js.rbuddyapp;

import java.util.Iterator;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import static js.basic.Tools.*;

public class UserData {

	private static final String PREFERENCES_NAME = "RBuddy";
	private static final String PREFERENCE_KEY_ROOTFOLDER = "UserData root folder";
	private static final String USER_ROOTFOLDER_NAME = "RBuddy User Data";

	/**
	 * Constructor
	 * 
	 * @param app
	 */
	public UserData(RBuddyApp app) {
		RBuddyApp.assertUIThread();
		this.app = app;
		this.driveClient = app.getGoogleApiClient();
		this.handler = new Handler(new Callback() {
			@Override
			public boolean handleMessage(Message m) {
				warning("not handling message: " + m);
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

	/**
	 * Prepare user data for use. Looks for user's data in his Google Drive; if
	 * successful, executes callback. Must only be called from UI thread.
	 * 
	 * @param callback
	 * @throws RuntimeException
	 *             if unable to create user data folder
	 */
	public void open(Runnable callback) {
		RBuddyApp.assertUIThread();
		if (db)
			pr(hey() + "UserData.open");

		final Runnable theCallback = callback;

		// Fire up a thread to do this potentially time consuming stuff in the
		// background.
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (db)
					pr(hey() + "UserData.open, performing run() method");

				String storedFolderIdString = getPreferenceString(
						PREFERENCE_KEY_ROOTFOLDER, "");
				String rootFolderIdString = storedFolderIdString;

				if (rootFolderIdString.isEmpty()) {
					rootFolderIdString = lookForUserRootFolder();
				}

				if (rootFolderIdString == null) {
					if (db)
						pr(" no stored default exists; creating user root folder");
					rootFolderIdString = createUserRootFolder();
				} else {
					if (db)
						pr(" stored default = "
								+ rootFolderIdString
								+ ";\n  verifying that this folder exists and is good to go");

					// Make sure the root folder exists; if not, clear id
					DriveId folderId = DriveId
							.decodeFromString(rootFolderIdString);
					DriveFolder folder = Drive.DriveApi.getFolder(driveClient,
							folderId);
					DriveResource.MetadataResult result = folder.getMetadata(
							driveClient).await();

					boolean found = false;
					do {
						if (!success(result))
							break;
						Metadata metadata = result.getMetadata();
						if (!doesMetadataMatchHealthyUserRoot(metadata))
							break;
						found = true;
					} while (false);
					if (!found)
						rootFolderIdString = createUserRootFolder();
				}

				if (rootFolderIdString == null) {
					throw new RuntimeException(
							"unable to create Google Drive RBuddy root folder");
				}

				if (!storedFolderIdString.equals(rootFolderIdString)) {
					storedFolderIdString = rootFolderIdString;
					setPreferenceString(PREFERENCE_KEY_ROOTFOLDER,
							storedFolderIdString);
				}

				if (db) {
					pr("delaying a bit before calling the callback...");
					sleepFor(1500);
					if (db)
						pr("NOW calling the callback...");
				}
				handler.post(theCallback);
			}
		}).start();
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

		DriveFolder rootFolder = Drive.DriveApi.getRootFolder(driveClient);
		MetadataBufferResult result = rootFolder.listChildren(driveClient)
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

		DriveFolder rootFolder = Drive.DriveApi.getRootFolder(driveClient);
		MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(
				USER_ROOTFOLDER_NAME).build();
		DriveFolderResult result = rootFolder.createFolder(driveClient,
				changeSet).await();
		if (!success(result))
			return null;

		DriveFolder folder = result.getDriveFolder();
		if (db)
			pr(" getDriveFolder returned " + folder);
		return folder.getDriveId().encodeToString();
	}

	private RBuddyApp app;
	private GoogleApiClient driveClient;
	private Handler handler;
}
