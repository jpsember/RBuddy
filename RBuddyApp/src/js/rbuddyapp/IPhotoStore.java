package js.rbuddyapp;


public interface IPhotoStore {
	public void storePhoto(FileArguments args);
	public void readPhoto(FileArguments args);
	public void deletePhoto(FileArguments args);
}
