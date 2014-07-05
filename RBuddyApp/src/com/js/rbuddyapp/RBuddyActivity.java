package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import java.util.*;

import com.js.android.AppPreferences;
import com.js.rbuddy.R;
import com.js.rbuddy.Receipt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;

public class RBuddyActivity extends MyActivity implements
		ReceiptListFragment.Listener, //
		EditReceiptFragment.Listener {

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

		fragments = new FragmentOrganizer(this);
		fragments.register(ReceiptListFragment.FACTORY).//
				register(EditReceiptFragment.FACTORY).//
				register(SearchFragment.FACTORY);
		fragments.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			// No previous state (including, presumably, the fragments) was
			// defined, so set initial fragments
			// TODO do this if no fragment exists in the slot, in case no state
			// was saved for some (unusual) reason
			fragments.plot(ReceiptListFragment.TAG, true, false);
			if (fragments.supportDualFragments()) {
				fragments.plot(EditReceiptFragment.TAG, false, false);
			}
		}
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
			AppPreferences.toggle(RBuddyApp.PREFERENCE_KEY_SMALL_DEVICE_FLAG);
			return true;
		case R.id.action_search:
			doSearchActivity();
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
								RBuddyApp.PREFERENCE_KEY_SMALL_DEVICE_FLAG,
								false) ? "Disable" : "Enable")
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
						unimp();
						if (false) {
							rebuildReceiptList(receiptList);
							receiptListAdapter.notifyDataSetChanged();
						}
						app.receiptFile().flush();
					}
				});
	}

	private void processZap() {
		confirmOperation(this, "Delete all receipts?", new Runnable() {
			@Override
			public void run() {
				app.receiptFile().clear();
				unimp();
				if (false) {
					rebuildReceiptList(receiptList);
				}
			}
		});
	}

	private void invalidateReceiptList() {
	}

	private void rebuildReceiptList(List list) {
		if (list == null) {
			unimp();
			return;
		}
		list.clear();
		for (Iterator it = app.receiptFile().iterator(); it.hasNext();)
			list.add(it.next());
		Collections.sort(list, Receipt.COMPARATOR_SORT_BY_DATE);

		if (receiptListAdapter != null)
			receiptListAdapter.notifyDataSetChanged();
	}

	private void processAddReceipt() {
		Receipt r = new Receipt(app.receiptFile().allocateUniqueId());
		app.receiptFile().add(r);
		invalidateReceiptList();
	}

	private void editReceipt(Receipt receipt) {
		EditReceiptFragment f = (EditReceiptFragment) fragments.plot(
				EditReceiptFragment.TAG, false, true);
		f.setReceipt(receipt);
	}

	private void doSearchActivity() {
		fragments.plot(SearchFragment.TAG, false, true);
	}

	private ReceiptListFragment receiptListFragment() {
		return (ReceiptListFragment) fragments.get(ReceiptListFragment.TAG);
	}

	private RBuddyApp app;
	private FragmentOrganizer fragments;
	private ArrayAdapter<Receipt> receiptListAdapter;
	private List receiptList;

	// ReceiptListFragment.Listener
	@Override
	public void receiptSelected(Receipt r) {
		editReceipt(r);
	}

	// EditReceiptFragment.Listener
	@Override
	public void receiptEdited(Receipt r) {
		final boolean db = true;
		ReceiptListFragment f = receiptListFragment();
		if (db)
			pr("EditReceiptFragment.Listener; RBuddyActivity telling ReceiptListFragment "
					+ f + " to refresh " + r);

		// If we're shutting down the app, the fragment may no longer exist
		if (f != null)
			f.refreshReceipt(r);
	}
}
