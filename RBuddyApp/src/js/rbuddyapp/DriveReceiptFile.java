package js.rbuddyapp;

import static js.basic.Tools.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.android.gms.drive.DriveFile;

import js.json.JSONEncoder;
import js.json.JSONParser;
import js.rbuddy.IReceiptFile;
import js.rbuddy.Receipt;
import js.rbuddy.TagSetFile;

public class DriveReceiptFile implements IReceiptFile {

	public static String EMPTY_FILE_CONTENTS = "[]";
	public static final String RECEIPTFILE_NAME = "Receipts.json";
	public static final String TAGSFILE_NAME = "Tags.json";

	/**
	 * This constructor may be called from other than the UI thread!
	 * 
	 * @param driveFile
	 */
	public DriveReceiptFile(UserData userData, DriveFile driveFile,
			String contents) {
		if (db)
			pr("DriveReceiptFile constructor; " + UserData.dbPrefix(driveFile));
		this.userData = userData;
		this.driveFile = driveFile;
		this.map = new HashMap();

		{
			JSONParser json = new JSONParser(contents);
			json.enterList();
			while (json.hasNext()) {
				Receipt r = (Receipt) Receipt.JSON_PARSER.parse(json);
				map.put(r.getId(), r);
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
		if (db)
			pr("DriveReceiptFile.flush");
		if (changes) {
			if (db)
				pr(" flush; called from " + stackTrace(1, 1));
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
			if (db)
				pr(" writing text file " + text + "\n to "
						+ UserData.dbPrefix(driveFile));
			FileArguments args = new FileArguments(RECEIPTFILE_NAME);
			args.setFileId(driveFile.getDriveId());
			userData.writeTextFile(args, text);
		}
		flushTagSet();
	}

	private void flushTagSet() {
		if (db)
			pr("DriveReceiptFile.flushTagSet, called from " + stackTrace());
		TagSetFile tf = userData.getTagSetFile();
		if (db)
			pr(" file " + tf + ", isChanged=" + tf.isChanged());
		if (tf.isChanged()) {
			String json = JSONEncoder.toJSON(tf);
			FileArguments args = new FileArguments(TAGSFILE_NAME);

			args.setFileId(userData.getTagSetDriveFile().getDriveId());
			userData.writeTextFile(args, json);
			if (db)
				pr("  wrote json " + json);
			tf.setChanged(false);
		}
	}

	@Override
	public void add(Receipt r) {
		getReceiptFromMap(r.getId(), false);
		map.put(r.getId(), r);
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

	/**
	 * Remove all receipts from file
	 */
	public void clear() {
		if (!map.isEmpty()) {
			map.clear();
			setChanges();
		}
	}

	private void setChanges() {
		changes = true;
	}

	private boolean changes;
	private DriveFile driveFile;
	private UserData userData;
	private Map map;
}
