package com.js.rbuddyapp;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;

import static com.js.basic.Tools.*;

/**
 * Class for organizing parameters for file operations (whether using Google
 * Drive API, or simple local file system)
 * 
 */
public class FileArguments {

	static {
		suppressWarning();
	}

	public FileArguments() {
		this.data = EMPTY_DATA;
		this.mimeType = "application/octet-stream";
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return this.data;
	}

	/**
	 * Set id of file, given DriveFile
	 * 
	 * @param file
	 *            DriveFile
	 */
	public void setFileId(DriveFile file) {
		this.file = file;
		this.fileIdString = null;
	}

	/**
	 * Set id of file, given string; if using Google API, this string can be
	 * converted to a DriveId from which a DriveFile can be derived; else,
	 * 
	 * @param fileIdString
	 */
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
			setFileId(Drive.DriveApi.getFile(apiClient, fileId));
		}
		return file;
	}

	/**
	 * Get id of file, as string
	 * 
	 * @return
	 */
	public String getFileIdString() {
		if (fileIdString == null && file != null)
			fileIdString = file.getDriveId().encodeToString();
		return fileIdString;
	}

	public void setFilename(String f) {
		this.filename = f;
	}

	public String getFilename() {
		return filename;
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

	public void setCallback(Runnable callback) {
		this.callback = callback;
	}

	public DriveFolder getParentFolder() {
		return this.parentFolder;
	}

	public void setParentFolder(DriveFolder f) {
		this.parentFolder = f;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("FileArguments[\n");
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

	private static final byte[] EMPTY_DATA = {};

	private DriveFile file;
	private String fileIdString;
	private DriveFolder parentFolder;
	private String filename;
	private byte[] data;
	private String mimeType;
	private Runnable callback;
}
