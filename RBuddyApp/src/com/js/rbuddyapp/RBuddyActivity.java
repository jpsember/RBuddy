package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.js.android.App;
import com.js.android.AppPreferences;
import com.js.android.FragmentOrganizer;
import com.js.android.FragmentReference;
import com.js.android.MyActivity;
import com.js.rbuddy.R;
import com.js.rbuddy.Receipt;
import com.js.rbuddy.ReceiptFilter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;

import com.js.android.IPhotoStore;

public class RBuddyActivity extends MyActivity implements //
		IRBuddyActivity //
{
	private static final int REQUEST_IMAGE_CAPTURE = 990;

	private static final String PERSIST_KEY_ACTIVE_RECEIPT_ID = "activeReceiptId";
	private static final String PERSIST_KEY_SEARCH_RESULTS = "searchResults";

	public RBuddyActivity() {
		if (db)
			setLogging(true);
	}

	public static Intent getStartIntent(Context context) {
		return startIntentFor(context, RBuddyActivity.class);
	}

	private static boolean DEBUG_ORIENTATION = true;

	private static int sOrientation = -1;

	private void initOrientation() {
		if (sOrientation >= 0)
			return;
		sOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
		// TODO: make this a preferences flag
		setRequestedOrientation(sOrientation);
	}

	private void toggleOrientation() {
		sOrientation ^= (ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE ^ ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setRequestedOrientation(sOrientation);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (db)
			pr(hey());
		super.onCreate(savedInstanceState);

		if (DEBUG_ORIENTATION)
			initOrientation();

		app = RBuddyApp.sharedInstance(this);

		buildFragmentOrganizer();
		setContentView(getFragmentOrganizer().getContainer(), new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		// Don't modify the slots if we're restoring a previous state
		// (presumably due to an orientation change)
		if (savedInstanceState == null) {
			FragmentOrganizer f = getFragmentOrganizer();
			f.focusOn(mReceiptList, false);
			if (f.supportDualFragments())
				f.focusOn(mReceiptEditor, true);
		} else {
			restorePreviousSavedState(savedInstanceState);
		}
	}

	private void restorePreviousSavedState(Bundle savedInstanceState) {
		int receiptId = savedInstanceState.getInt(
				PERSIST_KEY_ACTIVE_RECEIPT_ID, 0);
		if (receiptId > 0)
			setEditReceipt(app.receiptFile().getReceipt(receiptId));

		mSearchResults = savedInstanceState
				.getIntArray(PERSIST_KEY_SEARCH_RESULTS);
	}

	private void setEditReceipt(Receipt r) {
		if (r != mReceipt) {
			mReceipt = r;
			for (IRBuddyActivityListener listener : listeners) {
				listener.activeReceiptChanged();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mReceipt != null) {
			outState.putInt(PERSIST_KEY_ACTIVE_RECEIPT_ID, mReceipt.getId());
		}
		if (mSearchResults != null) {
			outState.putIntArray(PERSIST_KEY_SEARCH_RESULTS, mSearchResults);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		app.receiptFile().flush();
	}

	@Override
	protected void onDestroy() {
		mReceiptList = null;
		super.onDestroy();
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
			getFragmentOrganizer().focusOn(mSearch);
			return true;
		case R.id.action_testonly_exit:
			android.os.Process.killProcess(android.os.Process.myPid());
			return true;
		case R.id.action_testonly_rotate:
			if (DEBUG_ORIENTATION)
				toggleOrientation();
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

		if (!DEBUG_ORIENTATION)
			menu.removeItem(R.id.action_testonly_rotate);

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
						sendReceiptFileChanged();
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

				sendReceiptFileChanged();
			}
		});
	}

	private void sendReceiptFileChanged() {
		for (IRBuddyActivityListener listener : listeners) {
			listener.receiptFileChanged();
		}
	}

	private void processAddReceipt() {
		Receipt r = new Receipt(app.receiptFile().allocateUniqueId());
		app.receiptFile().add(r);
		setActiveReceipt(r);
		sendReceiptFileChanged();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE) {
			mPhoto.f().processImageCaptureResult(resultCode, data);
		}
	}

	// IRBuddyActivity
	@Override
	public void addListener(IRBuddyActivityListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IRBuddyActivityListener listener) {
		listeners.remove(listener);
	}

	@Override
	public Receipt getActiveReceipt() {
		return mReceipt;
	}

	@Override
	public void editActiveReceiptPhoto() {
		getFragmentOrganizer().focusOn(mPhoto);
	}

	@Override
	public void activeReceiptEdited() {
		for (IRBuddyActivityListener listener : listeners) {
			listener.activeReceiptEdited();
		}
	}

	@Override
	public void performSearch(ReceiptFilter filter) {
		// TODO: Persist search results within activity's bundle
		// TODO: maybe just store receipt id in search results, instead of whole
		// receipt
		mSearchResults = applySearch(filter);
		if (mSearchResults.length == 0) {
			toast(this, "No Results");
		}
		getFragmentOrganizer().focusOn(mReceiptList);
		mReceiptList.f().updateForSearchResults();
	}

	@Override
	public int[] getSearchResults() {
		return mSearchResults;
	}

	public void clearSearchResults() {
		mSearchResults = null;
	}

	private int[] applySearch(ReceiptFilter filter) {
		List<Integer> list = new ArrayList();
		for (Iterator<Receipt> it = app.receiptFile().iterator(); it.hasNext();) {
			Receipt r = it.next();
			if (filter.apply(r)) {
				list.add(r.getId());
			}
		}

		return convertListToArray(list);
	}

	private static int[] convertListToArray(List<Integer> list) {
		int[] array = new int[list.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = list.get(i);
		}
		return array;
	}

	@Override
	public void setActiveReceipt(Receipt r) {
		setEditReceipt(r);
		getFragmentOrganizer().popFragment(mReceiptEditor.f().getName(), false);
		getFragmentOrganizer().focusOn(mReceiptEditor);
	}

	@Override
	public void processCapturePhotoIntent(Intent intent) {
		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	}

	private RBuddyApp app;
	private Receipt mReceipt;
	private int[] mSearchResults;

	// Fragments

	private FragmentReference<ReceiptListFragment> mReceiptList = buildFragment(ReceiptListFragment.class);
	private FragmentReference mReceiptEditor = buildFragment(ReceiptEditor.class);
	private FragmentReference mSearch = buildFragment(Search.class);
	private FragmentReference<Photo> mPhoto = buildFragment(Photo.class);

	// Set of registered listeners
	private Set<IRBuddyActivityListener> listeners = new HashSet();

}
