package com.js.rbuddyapp;

import com.js.rbuddy.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import static com.js.basic.Tools.*;

public class ExperimentalActivity extends Activity {

	public static Intent getStartIntent(Context context) {
		Intent intent = RBuddyApp.startIntentFor(context,
				ExperimentalActivity.class);
		return intent;
	}

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
