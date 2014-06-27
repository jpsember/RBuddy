package com.js.rbuddyapp;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;

import static com.js.basic.Tools.*;

public class FileArguments {

	public FileArguments(String filename) {
		ASSERT(filename != null);
		this.filename = filename;
		this.data = EMPTY_DATA;
		this.mimeType = "application/octet-stream";
	}

	/**
	 * Data to be written to file
	 * 
	 * @param data
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return this.data;
	}

	static final byte[] EMPTY_DATA = {};

	public void setFileId(String fileIdString) {
		this.fileIdString = fileIdString;
		this.file = null;
	}

	/**
	 * Get DriveFile, using file id if DriveFile not already known
	 * 
	 * @param apiClient
	 *            GoogleApiClient to use if conversion from file id is necessary
	 * @return
	 */
	public DriveFile getFile(GoogleApiClient apiClient) {
		if (file != null)
			return file;
		if (fileIdString != null) {
			DriveId fileId = DriveId.decodeFromString(fileIdString);
			setFile(Drive.DriveApi.getFile(apiClient, fileId));
		}
		return file;
	}

	public String getFileIdString() {
		if (fileIdString == null && file != null)
			fileIdString = file.getDriveId().encodeToString();
		return fileIdString;
	}

	public void setFile(DriveFile file) {
		this.file = file;
		this.fileIdString = null;
	}

	public String getFilename() {
		return filename;
	}

	/**
	 * When a new file is created, this is stored in its metadata
	 * 
	 * @param f
	 */
	public void setFilename(String f) {
		this.filename = f;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getMimeType() {
		return this.mimeType;
	}

	public Runnable getCallback() {
		return callback;
	}

	/**
	 * If not null, this callback's run() method will be executed, on the
	 * caller's thread, when the operation completes
	 */
	public void setCallback(Runnable callback) {
		this.callback = callback;
	}

	public DriveFolder getParentFolder() {
		return this.parentFolder;
	}

	/**
	 * For write operations only; set new file's containing folder
	 */
	public void setParentPhoto(DriveFolder f) {
		this.parentFolder = f;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Args[\n");
		String s = getFileIdString();
		if (s != null) {
			sb.append(" fileId:" + s + "\n");
		}
		if (filename != null)
			sb.append(" filename:" + filename + "\n");
		sb.append(" length:" + data.length);
		if (!mimeType.equals("application/octet-stream"))
			sb.append(" mimeType:" + mimeType + "\n");
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Id of file to be accessed (lazy-initialized as required to support simple
	 * & google drive versions)
	 */
	private String fileIdString;

	private DriveFile file;

	private DriveFolder parentFolder;

	private String filename;

	private byte[] data;
	private String mimeType;

	private Runnable callback;

}
