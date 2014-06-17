package js.rbuddy.test;

import js.rbuddyapp.RBuddyApp;
import android.test.AndroidTestCase;

public class JSAndroidTestCase extends AndroidTestCase {

	 @Override
	protected void setUp() throws Exception {
		super.setUp();
	 	RBuddyApp.prepare(this.getContext());
	 }

}
