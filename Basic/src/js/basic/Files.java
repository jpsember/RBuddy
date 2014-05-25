package js.basic;

import java.io.*;

public class Files {
	
	  /**
	   * Get a buffered InputStream for reading from a file.
	   * @param path  path of file
	   * @return OutputStream
	   * @throws IOException
	   */
	  public static InputStream inputStream(String path) throws IOException {
	    InputStream ret;
	    ret = new BufferedInputStream(new FileInputStream(path));
	    return ret;
	  }

	  /**
	   * Get a buffered Reader for a file.
	   * @param path path of file
	   * @return Reader
	   */
	  public static Reader reader(String path) throws IOException {
	    return new InputStreamReader(inputStream(path));
	  }

	  /**
	   * Read a file into a string
	   * @param file File to read 
	   * @return String
	   */
	  public static String readTextFile(String path) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    Reader r = reader(path);
	    while (true) {
	      int c = r.read();
	      if (c < 0) {
	        break;
	      }
	      sb.append((char) c);
	    }
	    r.close();
	    return sb.toString();
	  }
	  
	public static void writeTextFile(File file, String content)
			throws IOException {

		BufferedWriter w = new BufferedWriter(new FileWriter(file));
		w.write(content);
		w.close();
	}

}
