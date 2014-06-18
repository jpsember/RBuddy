package js.rbuddy;

//import java.io.File;
//import java.io.FilenameFilter;
//import java.util.ArrayList;

public interface IFileCollection {

	// public static final String PHOTO_EXTENSION = ".jpg";

	// /**
	// * Constructor
	// *
	// * @param parentFolderId
	// * string from which root folder or directory containing the
	// * photos can be derived; for instance, for the Drive API, a
	// * DriveId can be decoded from this string, which can then be
	// * passed to getFolder(GoogleApiClient, DriveId)
	// */
	// public FileCollection(String parentFolderId) {
	// this.parentFolderId = parentFolderId;
	// }

	/**
	 * Create a new file in the set
	 * 
	 * @return identifier of new file
	 */
	public abstract String allocateNew();

	//
	// public String getPhotoFile(String driveFileIdString) {
	// String baseName = photoBaseName(photoId);
	// return new File(mainDirectory, baseName);
	// }

	// public boolean photoExists(String photoIdString) {
	// // File f = getMainFileFor(photoId);
	// // return f.isFile();
	// }

	// /**
	// * Get a list of all Photos in the file
	// *
	// * @return
	// */
	// public ArrayList<Integer> contents() {
	// String[] filenames = mainDirectory.list(new FilenameFilter() {
	// public boolean accept(File f, String s) {
	// boolean accept = false;
	// do {
	// if (!s.endsWith(PHOTO_EXTENSION))
	// break;
	// // Note: we'll assume that any file ending with .jpg is NOT
	// // a directory;
	// // presumably we created it ourselves, though I suppose some
	// // malevolent(sp?) app could
	// // have manipulated this directory
	// accept = true;
	// } while (false);
	// return accept;
	// }
	// });
	// ArrayList<Integer> list = new ArrayList();
	// for (int i = 0; i < filenames.length; i++) {
	// String filename = filenames[i];
	// String identifierString = filename.substring(0, filename.length()
	// - PHOTO_EXTENSION.length());
	// int identifier = Integer.parseInt(identifierString);
	// list.add(identifier);
	// }
	// return list;
	// }

	// public void delete(String photoId) {
	// throw new UnsupportedOperationException();
	// // File mainFile = getMainFileFor(photoId);
	// // if (mainFile.exists()) {
	// // mainFile.delete();
	// // }
	// }

	// private String photoBaseName(int photoId) {
	// return photoId + PHOTO_EXTENSION;
	// }

	// private String parentFolderId;
}
