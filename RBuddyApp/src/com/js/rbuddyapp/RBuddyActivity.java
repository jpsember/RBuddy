package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import com.js.android.App;
import com.js.android.AppPreferences;
import com.js.android.FragmentOrganizer;
import com.js.rbuddy.R;
import com.js.rbuddy.Receipt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import com.js.android.IPhotoStore;

public class RBuddyActivity extends MyActivity implements
		ReceiptListFragment.Listener //
		, ReceiptEditor.Listener //
{

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

		createFragments();

		fragments.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			// No previous state (including, presumably, the fragments) was
			// defined, so set initial fragments
			// TODO do this if no fragment exists in the slot, in case no state
			// was saved for some (unusual) reason
			fragments.plot(ReceiptListFragment.TAG, true, false);

			if (fragments.supportDualFragments()) {
				fragments.plot("ReceiptEditor", false, false);
			}
		}
	}

	private void createFragments() {
		fragments = new FragmentOrganizer(this);
		app.setFragments(fragments);

		fragments.register(ReceiptListFragment.FACTORY);
		fragments.register(SearchFragment.FACTORY);

		// Construct instances of the fragments we need. They will be stored
		// within the fragment organizer, and will be the same instances
		// manipulated by the FragmentManager when added to the activity's views

		mReceiptEditor = new ReceiptEditor();
		fragments.setWrappedSingleton(mReceiptEditor);

		mReceiptListFragment = ReceiptListFragment.construct(fragments);
		// mEditReceiptFragment = EditReceiptFragment.construct(fragments);
		mSearchFragment = SearchFragment.construct(fragments);
		mPhotoFragment = PhotoFragment.construct(fragments);
	}

	@Override
	public void onResume() {
		super.onResume();

		setContentView(fragments.getView(), new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		fragments.onResume();

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		fragments.onSaveInstanceState(outState);
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
			fragments.plot(SearchFragment.TAG, false, true);
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
						mReceiptListFragment.refreshList();
					}
				});
	}

	private void processZap() {
		confirmOperation(this, "Delete all receipts?", new Runnable() {
			@Override
			public void run() {
				// stop editing existing receipt (if any)
				mReceiptEditor.setReceipt(null);
				app.receiptFile().clear();
				app.receiptFile().flush();
				mReceiptListFragment.refreshList();
			}
		});
	}

	private void processAddReceipt() {
		Receipt r = new Receipt(app.receiptFile().allocateUniqueId());
		app.receiptFile().add(r);
		mReceiptListFragment.refreshList();
		receiptSelected(r);
	}

	// ReceiptListFragment.Listener
	@Override
	public void receiptSelected(Receipt r) {
		focusOn("ReceiptEditor");
		mReceiptEditor.setReceipt(r);
	}

	// EditReceiptFragment.Listener
	@Override
	public void receiptEdited(Receipt r) {
		mReceiptListFragment.refreshReceipt(r);
	}

	@Override
	public void editPhoto(Receipt r) {
		mPhotoFragment.setReceipt(r);
		focusOn(PhotoFragment.TAG);
	}

	private void focusOn(String fragmentName) {
		fragments.plot(fragmentName, false, true);
	}

	private ReceiptEditor mReceiptEditor;
	private RBuddyApp app;
	private FragmentOrganizer fragments;
	private ReceiptListFragment mReceiptListFragment;
	/* private */SearchFragment mSearchFragment;
	private PhotoFragment mPhotoFragment;
}
