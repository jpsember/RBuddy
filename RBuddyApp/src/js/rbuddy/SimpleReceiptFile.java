package js.rbuddy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import js.basic.Files;
import js.basic.JSONEncoder;
import js.basic.JSONParser;
import js.basic.Tools;
import static js.basic.Tools.*;

public class SimpleReceiptFile implements IReceiptFile {

	public SimpleReceiptFile() {
		this(null);
	}

	public SimpleReceiptFile(String baseName) {
		if (baseName == null)
			baseName = "receipts_json.txt";
		this.baseName = baseName;
		this.map = new HashMap();
		readAllReceipts();
		unimp("some way of determining if receipt has really changed; note that iterator is returning values without going through getReceipt()");
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
		// final boolean db = true;
		if (db)
			pr("SimpleReceiptFile flush, changes " + changes + "; "
					+ Tools.stackTrace(1, 3));

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
				json.exitList();
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

	/**
	 * Remove all receipts from file
	 */
	public void clear() {
		if (!map.isEmpty()) {
			map.clear();
			setChanges();
		}
	}

	private File receiptFile;

	public File getFile() {
		if (receiptFile == null) {
			receiptFile = fileForBaseName(baseName);
		}
		return receiptFile;
	}

	private void readAllReceipts() {
		// final boolean db = true;
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
				if (db)
					pr(" read receipt: " + r);
			}
			json.exit();
		} catch (IOException e) {
			die(e);
		}
	}

	private void setChanges() {
		// final boolean db = true;
		if (db)
			pr("setChanges\n");
		if (!changes) {
			if (db)
				pr("  ....... changes now true\n");
			changes = true;
		}
	}

	private String baseName;
	private boolean changes;
	private Map map;
}
