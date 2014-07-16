package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.js.android.MyFragment;
import com.js.form.FormWidget;

public class StartFragment extends MyFragment implements ConnectionCallbacks,
		OnConnectionFailedListener {

	private IRBuddyActivity getRBuddyActivity() {
		return (IRBuddyActivity) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		constructViews();
		restoreStateFrom(savedInstanceState);
		return mView;
	}

	@Override
	public void onResume() {
		super.onResume();
		connectToGoogleDrive();
	}

	private void connectToGoogleDrive() {
		IRBuddyActivity activity = getRBuddyActivity();
		if (!activity.usingGoogleAPI()) {
			getRBuddyActivity().connectedToServer(null);
		} else {
			if (db)
				pr(" attempting to connect to Google Drive API...");
			// TODO: Maybe construct our own client, then only store it in
			// activity if successful... this will eliminate an interface method
			// or two
			GoogleApiClient c = mApiClient;
			if (c == null) {
				if (db)
					pr(" building client");
				c = new GoogleApiClient.Builder(getActivity())
						.addApi(Drive.API).addScope(Drive.SCOPE_FILE)
						.addConnectionCallbacks(this)
						.addOnConnectionFailedListener(this).build();
				mApiClient = c;
			}
			if (!(c.isConnected() || c.isConnecting())) {
				if (db)
					pr(" connecting to client");
				c.connect();
			}
			if (c.isConnected()) {
				if (db)
					pr(" already connected");
				getRBuddyActivity().connectedToServer(c);
			}
		}
	}

	private static final int REQUEST_CODE_RESOLUTION = 993; // Don't think
															// actual value
															// matters here

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (db)
			pr(hey() + " result=" + result);
		// Called whenever the API client fails to connect.
		if (!result.hasResolution()) {
			// show the localized error dialog.
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
					getActivity(), 0).show();
			return;
		}

		// If we've already attempted connection...

		// The failure has a resolution. Resolve it.
		// Called typically when the app is not yet authorized, and an
		// authorization
		// dialog is displayed to the user.
		try {
			result.startResolutionForResult(getActivity(),
					REQUEST_CODE_RESOLUTION);
		} catch (SendIntentException e) {
			pr("Exception while starting resolution activity: " + e);
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (db)
			pr(hey() + " onConnected, connectionHint " + connectionHint);
		getRBuddyActivity().connectedToServer(mApiClient);
	}

	@Override
	public void onConnectionSuspended(int cause) {
		final boolean db = true;
		if (db)
			pr(hey() + " cause " + cause);
	}

	private void constructViews() {
		mView = new LinearLayout(getActivity());
		// Specify how this container is to be laid out in ITS container
		mView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mView.setOrientation(LinearLayout.VERTICAL);
		FormWidget.setDebugBgnd(mView,
				getRBuddyActivity().usingGoogleAPI() ? "blue" : "green");
	}

	private LinearLayout mView;
	private GoogleApiClient mApiClient;
}
