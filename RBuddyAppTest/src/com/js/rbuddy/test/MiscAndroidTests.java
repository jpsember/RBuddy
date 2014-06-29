package com.js.rbuddy.test;

import java.io.File;
import java.io.IOException;
import com.js.basic.Files;

public class MiscAndroidTests extends JSAndroidTestCase {

	public void testDeletedFileIsDeletedImmediately() throws IOException {
		File directory = getContext().getExternalFilesDir(null);
		File sampleFile = new File(directory, "___xyz___.txt");
		Files.writeTextFile(sampleFile, "hello");

		assertTrue(sampleFile.exists());
		assertTrue(sampleFile.delete());
		assertFalse(sampleFile.exists());
	}
}
