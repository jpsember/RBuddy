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

import android.content.Context;
import static js.basic.Tools.*;

public class SimpleReceiptFile implements IReceiptFile {

	public SimpleReceiptFile() {
		this.map = new HashMap();
		readAllReceipts();
	}

	@Override
	public Receipt getReceipt(int uniqueIdentifier) {
		Receipt r = (Receipt) map.get(uniqueIdentifier);
		ASSERT(r != null);
		return r;
	}

	@Override
	public void flush() {
		final boolean db = true;
		if (db)
			pr("SimpleReceiptFile flush, changes " + changes);

		if (changes) {
			changes = false;

			StringBuilder sb = new StringBuilder();
			for (Iterator it = map.values().iterator(); it.hasNext();) {
				Receipt r = (Receipt) it.next();
				sb.append(r.encode());
				sb.append('\n');
			}
			FileOutputStream fs;
			try {
				fs = RBuddyApp
						.sharedInstance()
						.activity()
						.openFileOutput(RECEIPTS_FILENAME, Context.MODE_PRIVATE);
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
		ASSERT(r.getUniqueIdentifier() != 0);
		ASSERT(!map.containsKey(r.getUniqueIdentifier()));
		map.put(r.getUniqueIdentifier(), r);
		setModified(r);
	}

	@Override
	public void delete(Receipt r) {
		ASSERT(map.containsKey(r.getUniqueIdentifier()));
		map.remove(r.getUniqueIdentifier());
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

	private void readAllReceipts() {
		final boolean db = true;
		if (db)
			pr("readAllReceipts\n");
		ASSERT(map.isEmpty());

		File f = new File(RECEIPTS_FILENAME);
		if (!f.exists()) {
			warning("no receipt file found: " + f);
			return;
		}

		try {
			FileInputStream fs;
			fs = RBuddyApp.sharedInstance().activity()
					.openFileInput(RECEIPTS_FILENAME);
			if (db)
				pr(" opened input stream\n");
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
				map.put(r.getUniqueIdentifier(), r);
			}
			fs.close();
		} catch (IOException e) {
			die(e);
		}
	}

	private void setChanges() {
		final boolean db = true;
		if (db)
			pr("setChanges\n");
		if (!changes) {
			if (db)
				pr("  ....... changes now true\n");
			changes = true;
		}
	}

	private static final String RECEIPTS_FILENAME = "receipts2.txt";

	private boolean changes;
	private Map map;
}
