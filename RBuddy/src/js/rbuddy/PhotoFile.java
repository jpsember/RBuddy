package js.rbuddy;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class PhotoFile {

	public static final String PHOTO_EXTENSION = ".jpg";
	
	public PhotoFile(File directory) {
		mainDirectory = directory;
		thumbDirectory = new File(mainDirectory, "thumbnails");
	}

	private String photoBaseName(int photoId) {
		return photoId +PHOTO_EXTENSION;
	}

	public File getMainFileFor(int photoId) {
		String baseName = photoBaseName(photoId);
		return new File(mainDirectory, baseName);
	}

	public File getThumbFileFor(int photoId) {
		String baseName = photoBaseName(photoId);
		return new File(thumbDirectory, baseName);
	}

	/**
	 * Get a list of all Photos in the file
	 * @return
	 */
	public ArrayList<Integer> contents() {
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
		ArrayList<Integer> list = new ArrayList();
		for (int i = 0; i < filenames.length; i++) {
			String filename = filenames[i];
			String identifierString = filename.substring(0,filename.length() - PHOTO_EXTENSION.length());
			int identifier = Integer.parseInt(identifierString);
			list.add(identifier);
		}
		return list;
	}
	
	public void delete(int photoId) {
		File mainFile = getMainFileFor(photoId);
		if (mainFile.exists()) {
			mainFile.delete();
		}
		File thumbFile = getThumbFileFor(photoId);
		if (thumbFile.exists()) {
			thumbFile.delete();
		}
	}
	
	private File mainDirectory;
	private File thumbDirectory;
}
