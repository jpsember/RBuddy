package js.basic;

import java.io.*;

public class Files {

	private static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

	/**
	 * Read a file into a string
	 * 
	 * @param path
	 *            file to read
	 * @return String
	 */
	public static String readTextFile(File file) throws IOException {
		StringBuilder sb = new StringBuilder();

		BufferedReader input = new BufferedReader(new FileReader(file));
		try {
			String line = null;
			/*
			 * Readline strips newlines, and returns null only for the end of
			 * the stream.
			 */
			while ((line = input.readLine()) != null) {
				sb.append(line);
				sb.append(LINE_SEPARATOR);
			}
		} finally {
			input.close();
		}
		return sb.toString();
	}

	public static void writeTextFile(File file, String content,
			boolean onlyIfChanged) throws IOException {
		if (onlyIfChanged) {
			if (file.isFile()) {
				String currentContents = readTextFile(file);
				if (currentContents.equals(content))
					return;
			}
		}
		BufferedWriter w = new BufferedWriter(new FileWriter(file));
		w.write(content);
		w.close();
	}

	public static void writeTextFile(File file, String content)
			throws IOException {
		writeTextFile(file, content, false);
	}

}
