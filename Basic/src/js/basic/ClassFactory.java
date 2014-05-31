package js.basic;

import java.util.*;

import static js.basic.Tools.*;

/**
 * I'm currently not using this class; I was trying to figure out a good way to 
 * support different implementations of classes e.g. when running as an Android app vs a plain old Java project.
 *
 */
public class ClassFactory {

	private static Map instanceMap = new HashMap();
	
	/**
	 * Given a fully qualified class name "xxx.yyy.base", if and object with key "base"
	 * already exists, returns it; otherwise, constructs an instance of xxx.yyy.base and stores it in the map.
	 * 
	 * @param fullClassName
	 * @return
	 */
	public static Object constructInstanceOf(String fullClassName) {
		int cursor = fullClassName.lastIndexOf('.');
		String baseName = 
				fullClassName.substring(1+cursor);
		
		Object obj = instanceMap.get(baseName);
		if (obj == null) {
			Class c = null;
			try {
				 c = Class.forName(fullClassName);
			} catch (ClassNotFoundException e) {
				die(e);
			}
			try {
				obj = c.newInstance();
			} catch (Throwable e) {
				die(e);
			}
			instanceMap.put(baseName, obj);
		}
		return obj;
	}
}
