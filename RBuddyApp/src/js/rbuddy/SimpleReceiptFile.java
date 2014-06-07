package js.rbuddy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import js.basic.Tools;
import static js.basic.Tools.*;

public class SimpleReceiptFile implements IReceiptFile {

	public SimpleReceiptFile() {
		this.map = new HashMap();
		readAllReceipts();
		unimp("some way of determining if receipt has really changed; note that iterator is returning values without going through getReceipt()");
	}

	private Receipt getReceiptFromMap(int identifier, boolean expectedToExist) {
		if (identifier <= 0) throw new IllegalArgumentException("bad id "+identifier);
		
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
	public Receipt getReceipt(int uniqueIdentifier) {
		return getReceiptFromMap(uniqueIdentifier,true);
	}

	@Override
	public void flush() {
		// final boolean db = true;
		if (db)
			pr("SimpleReceiptFile flush, changes " + changes + "; "
					+ Tools.stackTrace(1, 3));

		if (changes) {
			changes = false;

			StringBuilder sb = new StringBuilder();
			for (Iterator it = map.values().iterator(); it.hasNext();) {
				Receipt r = (Receipt) it.next();
				sb.append(r.encode());
				sb.append('\n');
			}
			try {
				FileOutputStream fs;
				fs = new FileOutputStream(getFile());
				if (db)
					pr(" writing:\n" + sb);
				fs.write(sb.toString().getBytes());
				fs.close();
			} catch (IOException e) {
				die(e);
			}
		}
	}

	@Override
	public void add(Receipt r) {
		getReceiptFromMap(r.getId(),false);
		map.put(r.getId(), r);
		setModified(r);
	}

	@Override
	public void delete(Receipt r) {
		getReceiptFromMap(r.getId(),true);
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

	private File getFile() {
		if (receiptFile == null) {
			receiptFile = new File(RBuddyApp.sharedInstance().activity()
					.getExternalFilesDir(null), RECEIPTS_FILENAME);
			if (!receiptFile.exists()) {
					warning("no receipt file found: "
							+ receiptFile.getAbsolutePath());
			}

		}
		return receiptFile;
	}

	private void readAllReceipts() {
		// final boolean db = true;
		if (db)
			pr("\n\nSimpleReceiptFile.readAllReceipts\n");
		ASSERT(map.isEmpty());

		if (!getFile().exists())
			return;

		try {
			FileInputStream fs = new FileInputStream(getFile());
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(fs));
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				line = line.trim();
				if (line.length() == 0)
					continue;
				if (db)
					pr(" read line " + line);
				Receipt r = Receipt.decode(line);
				if (db)
					pr(" decoded as " + r);
				map.put(r.getId(), r);
			}
			fs.close();
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

	private static final String RECEIPTS_FILENAME = "receipts.txt";

	private boolean changes;
	private Map map;
}
