package com.js.rbuddyapp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.android.gms.drive.DriveFile;

import com.js.json.JSONEncoder;
import com.js.json.JSONParser;
import com.js.rbuddy.IReceiptFile;
import com.js.rbuddy.Receipt;
import com.js.rbuddy.TagSetFile;

public class DriveReceiptFile implements IReceiptFile {

	public static final String MIME_TYPE = "application/json";
	public static final String INITIAL_CONTENTS = "[]";

	/**
	 * This constructor may be called from other than the UI thread!
	 * 
	 * @param driveFile
	 */
	public DriveReceiptFile(UserData userData, DriveFile driveFile,
			String contents) {
		this.userData = userData;
		this.driveFile = driveFile;
		this.map = new HashMap();

		{
			JSONParser json = new JSONParser(contents);
			json.enterList();
			while (json.hasNext()) {
				Receipt r = (Receipt) Receipt.JSON_PARSER.parse(json);
				map.put(r.getId(), r);
				updateUniqueIdentifier(r.getId());
			}
			json.exit();
		}
	}

	@Override
	public boolean exists(int id) {
		return map.containsKey(id);
	}

	@Override
	public Receipt getReceipt(int uniqueIdentifier) {
		return getReceiptFromMap(uniqueIdentifier, true);
	}

	@Override
	public void flush() {
		if (changes) {
			changes = false;
			String text;
			{
				JSONEncoder json = new JSONEncoder();
				json.enterList();
				for (Iterator it = map.values().iterator(); it.hasNext();) {
					Receipt r = (Receipt) it.next();
					r.encode(json);
				}
				json.exit();
				text = json.toString();
			}
			FileArguments args = new FileArguments();
			args.setFileId(driveFile);
			userData.writeTextFile(args, text);
		}
		flushTagSet();
	}

	private void flushTagSet() {
		TagSetFile tf = userData.getTagSetFile();
		if (tf.isChanged()) {
			String json = JSONEncoder.toJSON(tf);
			FileArguments args = new FileArguments();
			args.setFileId(userData.getTagSetDriveFile());
			userData.writeTextFile(args, json);
			tf.setChanged(false);
		}
	}

	@Override
	public void add(Receipt r) {
		getReceiptFromMap(r.getId(), false);
		map.put(r.getId(), r);
		updateUniqueIdentifier(r.getId());
		setModified(r);
	}

	@Override
	public void delete(Receipt r) {
		getReceiptFromMap(r.getId(), true);
		map.remove(r.getId());
		setChanges();
	}

	@Override
	public void setModified(Receipt r) {
		setChanges();
	}

	@Override
	public Iterator iterator() {
		return map.values().iterator();
	}

	private Receipt getReceiptFromMap(int identifier, boolean expectedToExist) {
		if (identifier <= 0)
			throw new IllegalArgumentException("bad id " + identifier);

		Receipt r = (Receipt) map.get(identifier);
		if (r == null) {
			if (expectedToExist)
				throw new IllegalArgumentException("no receipt found with id "
						+ identifier);
		} else {
			if (!expectedToExist)
				throw new IllegalArgumentException("receipt with id "
						+ identifier + " already exists");
		}
		return r;
	}

	@Override
	public void clear() {
		if (!map.isEmpty()) {
			map.clear();
			highestId = 0;
			setChanges();
		}
	}

	@Override
	public int allocateUniqueId() {
		int id = 1 + highestId;
		highestId = id;
		return id;
	}

	private void setChanges() {
		changes = true;
	}

	private void updateUniqueIdentifier(int receiptId) {
		if (highestId < receiptId)
			highestId = receiptId;
	}

	private boolean changes;
	private DriveFile driveFile;
	private UserData userData;
	private Map map;
	private int highestId;
}
