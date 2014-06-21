package js.rbuddyapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import js.basic.Files;
import js.json.*;
import js.rbuddy.IReceiptFile;
import js.rbuddy.Receipt;
import js.rbuddy.TagSetFile;
import static js.basic.Tools.*;

public class SimpleReceiptFile implements IReceiptFile {

	public static final boolean SHOW_FILE_ACTIVITY = TagSetFile.SHOW_FILE_ACTIVITY;

	public SimpleReceiptFile() {
		this(null, null);
	}

	public SimpleReceiptFile(String receiptsBaseName, String tagsBaseName) {
		ASSERT(!RBuddyApp.useGoogleAPI);
		if (receiptsBaseName == null)
			receiptsBaseName = "receipts_json_v" + Receipt.VERSION + ".txt";
		this.receiptsBaseName = receiptsBaseName;

		if (tagsBaseName == null)
			tagsBaseName = "tags_json_v" + Receipt.VERSION + ".txt";
		this.tagSetFileBaseName = tagsBaseName;

		this.map = new HashMap();
		readAllReceipts();
	}

	public TagSetFile readTagSetFile() {
		if (tagSetFile == null) {
			tagSetStorageFile = fileForBaseName(tagSetFileBaseName);
			TagSetFile tf = null;
			if (tagSetStorageFile.isFile()) {
				try {
					tf = (TagSetFile) JSONParser.parse(
							Files.readTextFile(tagSetStorageFile),
							TagSetFile.JSON_PARSER);
				} catch (IOException e) {
					warning("caught " + e + ", starting with empty tag file");
				}
			}
			if (tf == null) {
				tf = new TagSetFile();
			}
			tagSetFile = tf;
		}
		return tagSetFile;
	}

	private void flushTagSetFile() {
		final boolean db = SHOW_FILE_ACTIVITY;
		if (tagSetFile != null) {
			File file = fileForBaseName(tagSetFileBaseName);
			try {
				if (db)
					pr("\nflushTagSetFile, writing " + file + ": " + tagSetFile);
				String json = JSONEncoder.toJSON(tagSetFile);
				Files.writeTextFile(file, json);
			} catch (IOException e) {
				warning("caught " + e + ", unable to write tag file");
			}
		}
	}

	public static File fileForBaseName(String baseName) {
		return new File(RBuddyApp.sharedInstance().context()
				.getExternalFilesDir(null), baseName);
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
			final boolean db = SHOW_FILE_ACTIVITY;
			if (db)
				pr("SimpleReceiptFile flush; called from " + stackTrace(1, 1));
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

			try {
				FileOutputStream fs;
				fs = new FileOutputStream(getFile());
				if (db)
					pr(" writing:\n" + text);
				fs.write(text.getBytes());
				fs.close();
			} catch (IOException e) {
				die(e);
			}

			flushTagSetFile();
		}
	}

	@Override
	public void add(Receipt r) {
		getReceiptFromMap(r.getId(), false);
		map.put(r.getId(), r);
		setModified(r);
		updateUniqueIdentifier(r.getId());

	}

	private void updateUniqueIdentifier(int receiptId) {
		if (highestId < receiptId)
			highestId = receiptId;
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

	@Override
	public int allocateUniqueId() {
		int id = 1 + highestId;
		highestId = id;
		return id;
	}

	@Override
	public void clear() {
		if (!map.isEmpty()) {
			map.clear();
			setChanges();
			this.highestId = 0;
		}
	}

	public File getFile() {
		if (receiptFile == null) {
			receiptFile = fileForBaseName(receiptsBaseName);
		}
		return receiptFile;
	}

	private void readAllReceipts() {
		if (db)
			pr("\n\nSimpleReceiptFile.readAllReceipts\n");
		ASSERT(map.isEmpty());

		if (!getFile().exists()) {
			setChanges();
			return;
		}

		if (db) {
			try {
				pr("receipt file:\n" + Files.readTextFile(getFile()));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		try {
			FileInputStream fs = new FileInputStream(getFile());
			JSONParser json = new JSONParser(fs);
			fs.close();

			json.enterList();
			while (json.hasNext()) {
				Receipt r = (Receipt) Receipt.JSON_PARSER.parse(json);
				map.put(r.getId(), r);
				updateUniqueIdentifier(r.getId());
			}
			json.exit();
		} catch (IOException e) {
			die(e);
		}
	}

	private void setChanges() {
		changes = true;
	}

	private File receiptFile;
	private File tagSetStorageFile;
	private TagSetFile tagSetFile;
	private String receiptsBaseName;
	private String tagSetFileBaseName;
	private boolean changes;
	private Map map;
	private int highestId;
}
