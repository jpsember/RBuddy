package js.rbuddy;


public class Photo {

	public Photo(String identifier) {
		this.identifier = identifier;
	}
	
	@Override
	public String toString() {
		return "Photo("+identifier()+")";
	}
	
	public String identifier() {
		return identifier;
	}
	
	
	// unique identifier that distinguishes this photo from all others in the same file
	private String identifier;
	
}
