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
		this.mData = EMPTY_DATA;
		this.mMimeType = "application/octet-stream";
	}

	public void setData(byte[] data) {
		this.mData = data;
	}

	public byte[] getData() {
		return this.mData;
	}

	/**
	 * Set id of file, given DriveFile
	 * 
	 * @param file
	 *            DriveFile
	 */
	public void setFileId(DriveFile file) {
		this.mFile = file;
		this.mFileIdString = null;
	}

	/**
	 * Set id of file, given string; if using Google API, this string can be
	 * converted to a DriveId from which a DriveFile can be derived; else,
	 * 
	 * @param fileIdString
	 */
	public void setFileId(String fileIdString) {
		this.mFileIdString = fileIdString;
		this.mFile = null;
	}

	/**
	 * Get DriveFile, using file id if DriveFile not already known
	 * 
	 * @param apiClient
	 *            GoogleApiClient to use if conversion from file id is necessary
	 * @return
	 */
	public DriveFile getFile(GoogleApiClient apiClient) {
		if (mFile != null)
			return mFile;
		if (mFileIdString != null) {
			DriveId fileId = DriveId.decodeFromString(mFileIdString);
			setFileId(Drive.DriveApi.getFile(apiClient, fileId));
		}
		return mFile;
	}

	/**
	 * Get id of file, as string
	 * 
	 * @return
	 */
	public String getFileIdString() {
		if (mFileIdString == null && mFile != null)
			mFileIdString = mFile.getDriveId().encodeToString();
		return mFileIdString;
	}

	public void setFilename(String f) {
		this.mFilename = f;
	}

	public String getFilename() {
		return mFilename;
	}

	public void setMimeType(String mimeType) {
		this.mMimeType = mimeType;
	}

	public String getMimeType() {
		return this.mMimeType;
	}

	public Runnable getCallback() {
		return mCallback;
	}

	public void setCallback(Runnable callback) {
		this.mCallback = callback;
	}

	public DriveFolder getParentFolder() {
		return this.mParentFolder;
	}

	public void setParentFolder(DriveFolder f) {
		this.mParentFolder = f;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("FileArguments[\n");
		String s = getFileIdString();
		if (s != null) {
			sb.append(" fileId:" + s + "\n");
		}
		if (mFilename != null)
			sb.append(" filename:" + mFilename + "\n");
		sb.append(" length:" + mData.length);
		if (!mMimeType.equals("application/octet-stream"))
			sb.append(" mimeType:" + mMimeType + "\n");
		sb.append("]");
		return sb.toString();
	}

	private static final byte[] EMPTY_DATA = {};

	private DriveFile mFile;
	private String mFileIdString;
	private DriveFolder mParentFolder;
	private String mFilename;
	private byte[] mData;
	private String mMimeType;
	private Runnable mCallback;
}
