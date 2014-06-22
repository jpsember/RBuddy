package js.rbuddyapp;


import js.rbuddy.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import static js.basic.Tools.*;

public class ExperimentalActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		layoutElements(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void layoutElements(Bundle savedInstanceState) {
		die("unimplemented");
	}

}
