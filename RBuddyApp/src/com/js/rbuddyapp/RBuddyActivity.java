package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import java.util.HashSet;
import java.util.Set;

import com.js.android.App;
import com.js.android.AppPreferences;
import com.js.android.FragmentReference;
import com.js.android.MyActivity;
import com.js.android.MyFragment;
import com.js.rbuddy.R;
import com.js.rbuddy.Receipt;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.js.android.IPhotoStore;
import com.js.form.FormWidget;

public class RBuddyActivity extends MyActivity implements //
		IRBuddyActivity //
{
	private static final int REQUEST_IMAGE_CAPTURE = 990;

	private static final int FRAGMENT_SLOT_BASE_ID = 992;

	private static final String PERSIST_KEY_ACTIVE_RECEIPT_ID = "activeReceiptId";

	public RBuddyActivity() {
		if (db)
			setLogging(true);
	}

	public static Intent getStartIntent(Context context) {
		return startIntentFor(context, RBuddyActivity.class);
	}

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

		initOrientation();

		app = RBuddyApp.sharedInstance(this);

		doLayout();

		// Don't modify the slots if we're restoring a previous state
		// (presumably due to an orientation change)
		if (savedInstanceState == null) {
			focusOn(mReceiptList);
		} else {
			restorePreviousSavedState(savedInstanceState);
		}
	}

	private void restorePreviousSavedState(Bundle savedInstanceState) {
		int receiptId = savedInstanceState.getInt(
				PERSIST_KEY_ACTIVE_RECEIPT_ID, 0);
		if (receiptId > 0)
			setEditReceipt(app.receiptFile().getReceipt(receiptId));
	}

	private void setEditReceipt(Receipt r) {
		if (r != mReceipt) {
			mReceipt = r;
			for (IRBuddyActivityListener listener : listeners) {
				listener.activeReceiptChanged();
			}
		}
	}

	private void doLayout() {
		// Create view with a horizontal row of panels, one for each slot
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.HORIZONTAL);

		mSlotsContainer = layout;

		for (int slot = 0; slot < mNumberOfSlots; slot++) {
			View v = buildSlotView(slot);
			if (DEBUG_VIEWS)
				v = wrapView(v, "slot#" + slot);

			mSlotsContainer.addView(v, new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
		}
		mSlotsContainerWrapper = mSlotsContainer;

		if (DEBUG_VIEWS)
			mSlotsContainerWrapper = wrapView(mSlotsContainer, nameOf(this));
		setContentView(mSlotsContainerWrapper, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private ViewGroup buildSlotView(int slot) {
		FrameLayout f2 = new FrameLayout(this);
		f2.setId(FRAGMENT_SLOT_BASE_ID + slot);
		FormWidget.setDebugBgnd(f2, (slot == 0) ? "#206020" : "#202060");
		return f2;
	}

	private int mNumberOfSlots = 2;
	private LinearLayout mSlotsContainer;
	private View mSlotsContainerWrapper;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mReceipt != null) {
			outState.putInt(PERSIST_KEY_ACTIVE_RECEIPT_ID, mReceipt.getId());
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
			focusOn(mSearch);
			return true;
		case R.id.action_testonly_exit:
			android.os.Process.killProcess(android.os.Process.myPid());
			return true;
		case R.id.action_testonly_rotate:
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
	public Receipt getReceipt() {
		return mReceipt;
	}

	@Override
	public void editPhoto(Receipt r) {
		focusOn(mPhoto);
	}

	@Override
	public void receiptEdited(Receipt r) {
		for (IRBuddyActivityListener listener : listeners) {
			listener.activeReceiptEdited();
		}
	}

	@Override
	public void performSearch() {
		toast(this, "Search isn't yet implemented.");
	}

	@Override
	public void setActiveReceipt(Receipt r) {
		setEditReceipt(r);
		focusOn(mReceiptEditor);
	}

	@Override
	public void processCapturePhotoIntent(Intent intent) {
		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	}

	/**
	 * Display a fragment, if it isn't already in one of the slots
	 * 
	 * @param r
	 *            FragmentReference
	 */
	private void focusOn(FragmentReference r) {
		MyFragment fragment = r.f();

		do {
			// If fragment is already visible, ignore
			if (fragment.isVisible())
				return;

			int slot = mNumberOfSlots - 1;
			if (fragment instanceof ReceiptListFragment)
				slot = 0;

			int slotId = FRAGMENT_SLOT_BASE_ID + slot;

			if (db)
				pr(" plotting to slotId: " + slotId);

			FragmentManager m = getFragmentManager();
			Fragment oldFragment = m.findFragmentById(slotId);
			FragmentTransaction transaction = m.beginTransaction();
			if (oldFragment == null) {
				if (db)
					pr(" doing add of " + fragment);
				transaction.add(slotId, fragment, fragment.getName());
			} else {
				transaction.replace(slotId, fragment, fragment.getName());
			}
			if (oldFragment != null)
				transaction.addToBackStack(null);
			if (db)
				pr(" committing " + transaction);
			transaction.commit();
		} while (false);
	}

	public void refreshFragments() {
		mReceiptEditor.refresh();
		mReceiptList.refresh();
		mSearch.refresh();
		mPhoto.refresh();
	}

	private RBuddyApp app;
	private Receipt mReceipt;

	// Fragments

	private FragmentReference<ReceiptListFragment> mReceiptList = new FragmentReference<ReceiptListFragment>(
			this, ReceiptListFragment.class);
	private FragmentReference<ReceiptEditor> mReceiptEditor = new FragmentReference<ReceiptEditor>(
			this, ReceiptEditor.class);
	private FragmentReference<Search> mSearch = new FragmentReference<Search>(
			this, Search.class);
	private FragmentReference<Photo> mPhoto = new FragmentReference<Photo>(
			this, Photo.class);

	// Set of registered listeners
	private Set<IRBuddyActivityListener> listeners = new HashSet();
}
