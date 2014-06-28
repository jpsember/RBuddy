package com.js.rbuddyapp;

import static com.js.basic.Tools.*;

import com.js.form.Form;
import com.js.rbuddy.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;

public class SearchActivity extends Activity {

	public static Intent getStartIntent() {
		return RBuddyApp.startIntentFor(SearchActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = RBuddyApp.sharedInstance();

		layoutElements();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		getMenuInflater().inflate(R.menu.search_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_settings:
			unimp("settings");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}

	private void layoutElements() {
		String jsonString = RBuddyApp.sharedInstance().readTextFileResource(
				R.raw.form_search);
		this.form = Form.parse(this, jsonString);
		form.getField("search").setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				performSearch();
			}
		});
		ScrollView scrollView = new ScrollView(this);
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		scrollView.addView(form.getView());

		setContentView(scrollView);
	}

	private void performSearch() {
		app.toast("Search isn't yet implemented.");
	}

	private Form form;
	private RBuddyApp app;
}
