package js.rbuddyapp;

import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import static js.basic.Tools.*;

public class FileArguments {

	public String toString() {
		StringBuilder sb = new StringBuilder("Args[\n");
		if (fileIdString != null) {
			sb.append(" fileId:" + fileIdString + "\n");
		}

		if (filename != null)
			sb.append(" filename:" + filename + "\n");
		sb.append(" length:" + data.length);
		if (!mimeType.equals("application/octet-stream"))
			sb.append(" mimeType:" + mimeType + "\n");
		// if (returnValue != null)
		// sb.append(" returnValue:" + describe(returnValue) + "\n");
		sb.append("]");
		return sb.toString();
	}

	public void setFileId(String fileIdString) {
		this.fileIdString = fileIdString;
		// fileId = null;
		// if (fileIdString != null)
		// fileId = DriveId.decodeFromString(fileIdString);
	}

	public DriveId getFileId() {
		return DriveId.decodeFromString(this.fileIdString);
	}

	public void setFileId(DriveId fileId) {
		this.fileIdString = (fileId == null) ? null : fileId.encodeToString();
		// this.fileId = fileId;
	}

	/**
	 * Id of file to be accessed (if null, a new file is created)
	 */
	private String fileIdString; // DriveId fileId;

	/**
	 * For write operations only; this is its containing folder, and is used to
	 * generate new one
	 */
	private DriveFolder parentFolder;

	/**
	 * If not null, and a new file is created, this is stored in its metadata
	 */
	public String filename;

	/**
	 * This is the data to be written to the file; if null, writes zero bytes
	 */
	public byte[] data;

	/**
	 * Type of data
	 */
	public String mimeType = "application/octet-stream";

	/**
	 * If not null, this callback's run() method will be executed, on the
	 * caller's thread, when the operation completes
	 */
	public Runnable callback;

	public String getFileIdAsString() {
		return this.fileIdString;
	}

	public DriveFolder getParentFolder() {
		ASSERT(RBuddyApp.useGoogleAPI);
		return this.parentFolder;
	}

	public void setParentPhoto(DriveFolder f) {
		ASSERT(RBuddyApp.useGoogleAPI);
		this.parentFolder = f;
	}

	// /**
	// * This is where the return value, if any, will be found
	// */
	// public Object returnValue2;
	//
	// /**
	// * For client use
	// */
	// public Object user;

	// public byte[] waitForData() {
	// warning("this is a hack");
	// while (data == null) {
	// if (startSleepTime == 0)
	// startSleepTime = System.currentTimeMillis();
	// else {
	// long elapsed = System.currentTimeMillis() - startSleepTime;
	// if (elapsed > 5000)
	// die("data never arrived!");
	// Tools.sleepFor(200);
	// }
	// }
	// return data;
	// }

	// public Object waitForUser() {
	// warning("this is a hack");
	// while (user == null) {
	// if (startSleepTime == 0)
	// startSleepTime = System.currentTimeMillis();
	// else {
	// long elapsed = System.currentTimeMillis() - startSleepTime;
	// if (elapsed > 5000)
	// die("user data never arrived!");
	// Tools.sleepFor(200);
	// }
	// }
	// return user;
	// }

	// private long startSleepTime;
}
