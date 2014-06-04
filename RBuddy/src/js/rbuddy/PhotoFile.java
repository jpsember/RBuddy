package js.rbuddy;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class PhotoFile {

	public static final String PHOTO_EXTENSION = ".jpg";
	
	public PhotoFile(File directory) {
		mainDirectory = directory;
		thumbDirectory = new File(mainDirectory, "thumbnails");
		workDirectory = new File(mainDirectory,"work");
	}

	private String photoBaseName(Photo photo) {
		return photo.identifier() +PHOTO_EXTENSION;
	}

	public File getMainFileFor(Photo photo) {
		String baseName = photoBaseName(photo);
		return new File(mainDirectory, baseName);
	}

	public File getThumbFileFor(Photo photo) {
		String baseName = photoBaseName(photo);
		return new File(thumbDirectory, baseName);
	}

	/**
	 * Get the singleton 'work' file (not currently used; in Android, such a file
	 * would be stored elsewhere to service an Intent, probably due to permissions)
	 * 
	 * @return
	 */
	public File getWorkFile() {
		return new File(workDirectory,"_work_"+PHOTO_EXTENSION);
	}
	
	/**
	 * Delete the work file, if it exists
	 */
	public void deleteWorkFile() {
		File f = getWorkFile();
		if (f.exists())
			f.delete();
	}

	/**
	 * Get a list of all Photos in the file
	 * @return
	 */
	public ArrayList<Photo> contents() {
		String[] filenames = mainDirectory.list(new FilenameFilter(){
			public boolean accept(File f, String s) {
				boolean accept = false;
				do {
				if (!s.endsWith(PHOTO_EXTENSION)) 
					break;
				// Note: we'll assume that any file ending with .jpg is NOT a directory;
				// presumably we created it ourselves, though I suppose some malevolent(sp?) app could
				// have manipulated this directory 
				accept = true;
				} while (false);
				return accept;
			}});
		ArrayList<Photo> list = new ArrayList();
		for (int i = 0; i < filenames.length; i++) {
			String filename = filenames[i];
			String identifier = filename.substring(0,filename.length() - PHOTO_EXTENSION.length());
			list.add(new Photo(identifier));
		}
		return list;
	}
	
	public void delete(Photo p) {
		File mainFile = getMainFileFor(p);
		if (mainFile.exists()) {
			mainFile.delete();
		}
		File thumbFile = getThumbFileFor(p);
		if (thumbFile.exists()) {
			thumbFile.delete();
		}
	}
	
	private File mainDirectory;
	private File thumbDirectory;
	private File workDirectory;
}
