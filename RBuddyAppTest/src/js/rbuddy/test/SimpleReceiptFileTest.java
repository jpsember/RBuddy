package js.rbuddy.test;

import java.io.File;

import js.rbuddy.Receipt;
import js.rbuddy.SimpleReceiptFile;

public class SimpleReceiptFileTest extends JSAndroidTestCase {

	private static final String BASENAME = "__test__receipts_json.txt";

	private SimpleReceiptFile constructFile() {
		if (rf == null) {
			rf = new SimpleReceiptFile(BASENAME);
		}
		return rf;
	}

	private void close() {
		if (rf != null) {
			rf.flush();
			rf = null;
		}
	}

	public void testContructsEmptyFileIfDoesntExist() {
		constructFile();
		assertFalse(rf.iterator().hasNext());
		File f = rf.getFile();
		close();
		assertTrue(f.isFile());
	}

	private void generate(int startId, int count) {
		constructFile();
		for (int i = 0; i < count; i++) {
			Receipt r = new Receipt(startId + i);
			r.setSummary("This is receipt #" + r.getId());
			rf.add(r);
		}
	}

	public void testSavesReceipts() {
		generate(80, 4);
		close();

		constructFile();
		for (int i = 0; i < 4; i++) {
			rf.getReceipt(80 + i);
		}
	}

	public void testDeletesReceipts() {
		generate(80, 50);

		for (int id = 80; id < 80 + 50; id += 3) {
			rf.delete(rf.getReceipt(id));
		}
		close();

		constructFile();
		for (int id = 80; id < 80 + 50; id++) {
			boolean expected = ((id - 80) % 3 != 0);
			assertEquals(expected, rf.exists(id));
		}
		close();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		File f = SimpleReceiptFile.fileForBaseName(BASENAME);
		if (f.isFile())
			f.delete();
	}

	@Override
	protected void tearDown() throws Exception {
		if (rf != null) {
			File f = rf.getFile();
			if (f != null)
				f.delete();
		}
		super.tearDown();
	}

	private SimpleReceiptFile rf;
}
