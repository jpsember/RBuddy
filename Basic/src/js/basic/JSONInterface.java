package js.basic;

public interface JSONInterface {
	void encode(JSONEncoder encoder);
	Object decode(JSONInputStream stream);
}
