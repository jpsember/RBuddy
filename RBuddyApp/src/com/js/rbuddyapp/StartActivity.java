package com.js.rbuddyapp;

import static com.js.basic.Tools.*;
import com.js.rbuddy.R;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.js.form.FormWidget;

public class StartActivity extends Activity implements ConnectionCallbacks,
		OnConnectionFailedListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = (RBuddyApp) RBuddyApp.sharedInstance(RBuddyApp.class, this);

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		layout.addView(constructStartView());
	}

	@Override
	public void onResume() {
		super.onResume();
		connectToGoogleDrive();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (userFilesPrepared)
			app.receiptFile().flush();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.start_activitiy_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			unimp("settings");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private View constructStartView() {
		View v = new View(this);
		FormWidget.setDebugBgnd(v, app.useGoogleAPI() ? "blue" : "green");
		return v;
	}

	private RBuddyApp app;

	private void connectToGoogleDrive() {
		if (!app.useGoogleAPI()) {
			processGoogleApiConnected();
		} else {
			if (db)
				pr(" attempting to connect to Google Drive API...");
			GoogleApiClient c = app.getGoogleApiClient();
			if (c == null) {
				if (db)
					pr(" building client");
				c = new GoogleApiClient.Builder(this).addApi(Drive.API)
						.addScope(Drive.SCOPE_FILE)
						.addConnectionCallbacks(this)
						.addOnConnectionFailedListener(this).build();
				app.setGoogleApiClient(c);
			}
			if (!(c.isConnected() || c.isConnecting())) {
				if (db)
					pr(" connecting to client");
				app.getGoogleApiClient().connect();
			}
			if (c.isConnected()) {
				if (db)
					pr(" already connected");
				processGoogleApiConnected();
			}
		}
	}

	private UserData userData;

	private void processGoogleApiConnected() {

		if (db)
			pr(hey() + "processGoogleAPIConnected");

		if (app.useGoogleAPI()) {
			if (db)
				pr("constructing UserData");
			userData = new UserData(this, app);
			if (db)
				pr("calling open() with null callback");
			userData.open(new Runnable() {
				@Override
				public void run() {
					processUserDataReady();
				}
			});
		} else {
			processUserDataReady();
		}

	}

	private boolean userFilesPrepared;

	private void processUserDataReady() {
		if (!userFilesPrepared) {
			if (app.useGoogleAPI()) {
				app.setUserData(userData.getReceiptFile(),
						userData.getTagSetFile(), userData.getPhotoStore());
			} else {
				SimpleReceiptFile s = new SimpleReceiptFile(this);
				IPhotoStore ps = new SimplePhotoStore(this);
				app.setUserData(s, s.readTagSetFile(), ps);
			}
			userFilesPrepared = true;
		}

		startActivity(RBuddyActivity.getStartIntent(this)//
				.addFlags(
						Intent.FLAG_ACTIVITY_CLEAR_TASK
								| Intent.FLAG_ACTIVITY_NEW_TASK));
	}

	private static final int REQUEST_CODE_RESOLUTION = 993; // Don't think
															// actual value
															// matters here

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (db)
			pr("\n\n" + stackTrace() + " result=" + result);
		// Called whenever the API client fails to connect.
		if (!result.hasResolution()) {
			// show the localized error dialog.
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
					0).show();
			return;
		}

		// If we've already attempted connection...

		// The failure has a resolution. Resolve it.
		// Called typically when the app is not yet authorized, and an
		// authorization
		// dialog is displayed to the user.
		try {
			result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
		} catch (SendIntentException e) {
			pr("Exception while starting resolution activity: " + e);
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (db)
			pr(hey() + " onConnected, connectionHint " + connectionHint);
		processGoogleApiConnected();
	}

	@Override
	public void onConnectionSuspended(int cause) {
		if (db)
			pr("\n\n" + stackTrace() + " cause " + cause);
	}
}
