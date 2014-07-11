package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import com.js.android.App;
import com.js.android.AppPreferences;
import com.js.android.FragmentOrganizer;
import com.js.android.MyActivity;
import com.js.rbuddy.R;
import com.js.rbuddy.Receipt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import com.js.android.IPhotoStore;

public class RBuddyActivity extends MyActivity implements //
		ReceiptList.Listener //
		, ReceiptEditor.Listener //
		, Photo.Listener //
{
	private static final boolean OMIT_MOST_FRAGMENTS = false;

	private static final int REQUEST_IMAGE_CAPTURE = 990;

	public RBuddyActivity() {

		super(false); // log lifecycle events?
	}

	public static Intent getStartIntent(Context context) {
		return startIntentFor(context, RBuddyActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = RBuddyApp.sharedInstance(this);

		createFragments(savedInstanceState);

		if (savedInstanceState != null) {
			int rid = savedInstanceState.getInt("XXX", 0);
			mEditReceipt = null;
			if (rid > 0)
				// TODO: have it optionally return null if no such id rather
				// than dying
				mEditReceipt = app.receiptFile().getReceipt(rid);
		}
	}

	private void createFragments(Bundle savedInstanceState) {
		if (db)
			pr(hey(this));
		if (db)
			pr(" creating fragments");
		fragments = new FragmentOrganizer(this);

		if (db)
			pr(" creating ReceiptList");
		mReceiptList = fragments.register(new ReceiptList(fragments));
		if (!OMIT_MOST_FRAGMENTS) {
			mReceiptEditor = fragments.register(new ReceiptEditor(fragments));
			mSearch = fragments.register(new Search(fragments));
			mPhoto = fragments.register(new Photo(fragments));
		}
		fragments.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			// No previous state (including, presumably, the fragments) was
			// defined, so set initial fragments
			// TODO do this if no fragment exists in the slot, in case no state
			// was saved for some (unusual) reason
			fragments.plot("ReceiptList", true, false);
			if (!OMIT_MOST_FRAGMENTS) {
				if (fragments.supportDualFragments()) {
					fragments.plot("ReceiptEditor", false, false);
				}
			}
		}
	}

	private void setEditReceipt(Receipt r) {
		if (OMIT_MOST_FRAGMENTS)
			return;
		mEditReceipt = r;
		mReceiptEditor.setReceipt(mEditReceipt);
	}

	@Override
	public void onResume() {
		super.onResume();
		View contentView = fragments.getView();
		contentView = wrapView(contentView, nameOf(this));
		setContentView(contentView, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		fragments.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		fragments.onSaveInstanceState(outState);
		// TODO: give this a proper key
		outState.putInt("XXX", mEditReceipt == null ? 0 : mEditReceipt.getId());
	}

	@Override
	protected void onPause() {
		super.onPause();
		fragments.onPause();
		app.receiptFile().flush();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.receiptlist_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			unimp("settings");
			return true;
		case R.id.action_add:
			processAddReceipt();
			return true;
		case R.id.action_testonly_generate:
			processGenerate();
			return true;
		case R.id.action_testonly_zap:
			processZap();
			return true;
		case R.id.action_testonly_toggle_photo_delay:
			AppPreferences.toggle(IPhotoStore.PREFERENCE_KEY_PHOTO_DELAY);
			return true;
		case R.id.action_testonly_toggle_google_drive:
			AppPreferences
					.toggle(RBuddyApp.PREFERENCE_KEY_USE_GOOGLE_DRIVE_API);
			showGoogleDriveState();
			return true;
		case R.id.action_testonly_toggle_small_device:
			AppPreferences.toggle(App.PREFERENCE_KEY_SMALL_DEVICE_FLAG);
			return true;
		case R.id.action_search:
			fragments.plot("Search", false, true);
			return true;
		case R.id.action_testonly_exit:
			android.os.Process.killProcess(android.os.Process.myPid());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showGoogleDriveState() {
		toast(this,
				"Google Drive is "
						+ (app.useGoogleAPI() ? "active" : "inactive")
						+ ", and will be "
						+ (AppPreferences.getBoolean(
								RBuddyApp.PREFERENCE_KEY_USE_GOOGLE_DRIVE_API,
								true) ? "active" : "inactive")
						+ " when app restarts.");
	}

	public static void setMenuLabel(Menu menu, int menuItemId, String label) {
		MenuItem item = menu.findItem(menuItemId);
		if (item != null) {
			item.setTitle(label);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {

		// There's a bug in the Android API which causes some items to
		// disappear; specifically, the 'exit' item (the last one) is not
		// appearing. Workaround seems to be to delay this code until after the
		// onPrepareCall() completes...

		runOnUiThread(new Runnable() {
			public void run() {
				setMenuLabel(
						menu,
						R.id.action_testonly_toggle_google_drive,
						(AppPreferences.getBoolean(
								RBuddyApp.PREFERENCE_KEY_USE_GOOGLE_DRIVE_API,
								true) ? "Disable" : "Enable")
								+ " Google Drive");
				setMenuLabel(
						menu,
						R.id.action_testonly_toggle_photo_delay,
						(AppPreferences.getBoolean(
								IPhotoStore.PREFERENCE_KEY_PHOTO_DELAY, false) ? "Remove"
								: "Add")
								+ " simulated photo delay");
				setMenuLabel(
						menu,
						R.id.action_testonly_toggle_small_device,
						(AppPreferences.getBoolean(
								App.PREFERENCE_KEY_SMALL_DEVICE_FLAG, false) ? "Disable"
								: "Enable")
								+ " small device flag");
			}
		});
		return super.onPrepareOptionsMenu(menu);
	}

	private void processGenerate() {
		confirmOperation(this, "Generate some random receipts?",
				new Runnable() {
					@Override
					public void run() {
						seedRandom(0);

						for (int i = 0; i < 30; i++) {
							int id = app.receiptFile().allocateUniqueId();
							Receipt r = Receipt.buildRandom(id);
							app.receiptFile().add(r);
						}
						app.receiptFile().flush();
						mReceiptList.refreshList();
					}
				});
	}

	private void processZap() {
		confirmOperation(this, "Delete all receipts?", new Runnable() {
			@Override
			public void run() {
				// stop editing existing receipt (if any)
				setEditReceipt(null);
				app.receiptFile().clear();
				app.receiptFile().flush();
				mReceiptList.refreshList();
			}
		});
	}

	private void processAddReceipt() {
		Receipt r = new Receipt(app.receiptFile().allocateUniqueId());
		app.receiptFile().add(r);
		mReceiptList.refreshList();
		receiptSelected(r);
	}

	// ReceiptList.Listener
	@Override
	public void receiptSelected(Receipt r) {
		focusOn("ReceiptEditor");
		setEditReceipt(r);
	}

	// ReceiptEditor.Listener
	public Receipt getReceipt() {
		return mEditReceipt;
	}

	@Override
	public void receiptEdited(Receipt r) {
		mReceiptList.refreshReceipt(r);
	}

	@Override
	public void editPhoto(Receipt r) {
		mPhoto.setReceipt(r);
		focusOn("Photo");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE) {
			mPhoto.processImageCaptureResult(resultCode, data);
		}
	}

	private void focusOn(String fragmentName) {
		fragments.plot(fragmentName, false, true);
	}

	// Photo.Listener
	@Override
	public void processCapturePhotoIntent(Intent intent) {
		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	}

	private FragmentOrganizer fragments;
	private ReceiptList mReceiptList;
	private ReceiptEditor mReceiptEditor;
	private RBuddyApp app;
	/* private */Search mSearch;
	private Photo mPhoto;
	private Receipt mEditReceipt;
}
