package js.rbuddy;


public class TimCost {

		// what is the keyboard command to do the auto indent?
	
		public TimCost(int amount) {
		
			value = amount;
			
		}

		// we really should be dealing with the fact that 
		// "123.45" in is one hundred twenty three dollars and forty five cents
		// right now i am pretending the string must be pennies
		public TimCost(String s){
			
			value = Integer.parseInt(s);
				
		}
		
		public int getValue() {
			return value;
		}	

		private int value;
	

}
//private String determineTestName() {
//	String st = stackTrace(2, 5);
//
//	// Look for first occurrence of '.testXXX:'
//	Pattern p = Pattern.compile("\\.test(\\w+):");
//	Matcher m = p.matcher(st);
//	if (!m.find())
//		die("no 'test' method name found in stack trace:\n" + st);
//	String matchName = m.group(1);
//	return matchName;
//}
//
