package com.js.rbuddyapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.js.basic.Files;
import com.js.json.*;
import com.js.rbuddy.IReceiptFile;
import com.js.rbuddy.Receipt;
import com.js.rbuddy.TagSetFile;
import static com.js.basic.Tools.*;

public class SimpleReceiptFile implements IReceiptFile {

	public static final boolean SHOW_FILE_ACTIVITY = TagSetFile.SHOW_FILE_ACTIVITY;

	public SimpleReceiptFile(IRBuddyActivity activity) {
		this(activity, null, null);
	}

	public SimpleReceiptFile(IRBuddyActivity activity, String receiptsBaseName,
			String tagsBaseName) {
		ASSERT(!activity.usingGoogleAPI());
		this.mActivity = activity;
		if (receiptsBaseName == null)
			receiptsBaseName = "receipts_json_v" + Receipt.VERSION + ".txt";
		this.mReceiptsBaseName = receiptsBaseName;

		if (tagsBaseName == null)
			tagsBaseName = "tags_json_v" + Receipt.VERSION + ".txt";
		this.mTagSetFileBaseName = tagsBaseName;

		this.mMap = new HashMap();
		readAllReceipts();
	}

	public TagSetFile readTagSetFile() {
		if (mTagSetFile == null) {
			mTagSetStorageFile = fileForBaseName(mTagSetFileBaseName);
			TagSetFile tf = null;
			if (mTagSetStorageFile.isFile()) {
				try {
					tf = TagSetFile.parse(new JSONParser(Files
							.readTextFile(mTagSetStorageFile)));
				} catch (IOException e) {
					warning("caught " + e + ", starting with empty tag file");
				}
			}
			if (tf == null) {
				tf = new TagSetFile();
			}
			mTagSetFile = tf;
		}
		return mTagSetFile;
	}

	private void flushTagSetFile() {
		final boolean db = SHOW_FILE_ACTIVITY;
		if (mTagSetFile != null) {
			File file = fileForBaseName(mTagSetFileBaseName);
			try {
				if (db)
					pr("\nflushTagSetFile, writing " + file + ": "
							+ mTagSetFile);
				String json = JSONEncoder.toJSON(mTagSetFile);
				Files.writeTextFile(file, json);
			} catch (IOException e) {
				warning("caught " + e + ", unable to write tag file");
			}
		}
	}

	public File fileForBaseName(String baseName) {
		return new File(mActivity.getContext().getExternalFilesDir(null),
				baseName);
	}

	private Receipt getReceiptFromMap(int identifier, boolean expectedToExist) {
		if (identifier <= 0)
			throw new IllegalArgumentException("bad id " + identifier);

		Receipt r = (Receipt) mMap.get(identifier);
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
		return mMap.containsKey(id);
	}

	@Override
	public Receipt getReceipt(int uniqueIdentifier) {
		return getReceiptFromMap(uniqueIdentifier, true);
	}

	@Override
	public void flush() {
		if (mChanges) {
			final boolean db = SHOW_FILE_ACTIVITY;
			if (db)
				pr("SimpleReceiptFile flush; called from " + stackTrace(1, 1));
			mChanges = false;
			String text;
			{
				JSONEncoder json = new JSONEncoder();
				json.enterList();
				for (Iterator it = mMap.values().iterator(); it.hasNext();) {
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
		mMap.put(r.getId(), r);
		setModified(r);
		updateUniqueIdentifier(r.getId());

	}

	private void updateUniqueIdentifier(int receiptId) {
		if (mHighestId < receiptId)
			mHighestId = receiptId;
	}

	@Override
	public void delete(Receipt r) {
		getReceiptFromMap(r.getId(), true);
		mMap.remove(r.getId());
		setChanges();
	}

	@Override
	public void setModified(Receipt r) {
		setChanges();
	}

	@Override
	public Iterator iterator() {
		return mMap.values().iterator();
	}

	@Override
	public int allocateUniqueId() {
		int id = 1 + mHighestId;
		mHighestId = id;
		return id;
	}

	@Override
	public void clear() {
		if (!mMap.isEmpty()) {
			mMap.clear();
			setChanges();
			this.mHighestId = 0;
		}
	}

	public File getFile() {
		if (mReceiptFile == null) {
			mReceiptFile = fileForBaseName(mReceiptsBaseName);
		}
		return mReceiptFile;
	}

	private void readAllReceipts() {
		if (db)
			pr("\n\nSimpleReceiptFile.readAllReceipts\n");
		ASSERT(mMap.isEmpty());

		if (!getFile().exists()) {
			setChanges();
			return;
		}

		if (db) {
			try {
				pr("receipt file:\n" + Files.readTextFile(getFile()));
			} catch (IOException e1) {
			}
		}

		try {
			FileInputStream fs = new FileInputStream(getFile());
			JSONParser json = new JSONParser(fs);
			fs.close();

			json.enterList();
			while (json.hasNext()) {
				Receipt r = Receipt.parse(json);
				mMap.put(r.getId(), r);
				updateUniqueIdentifier(r.getId());
			}
			json.exit();
		} catch (IOException e) {
			die(e);
		}
	}

	private void setChanges() {
		mChanges = true;
	}

	private File mReceiptFile;
	private File mTagSetStorageFile;
	private TagSetFile mTagSetFile;
	private String mReceiptsBaseName;
	private String mTagSetFileBaseName;
	private boolean mChanges;
	private Map mMap;
	private int mHighestId;
	private IRBuddyActivity mActivity;
}
