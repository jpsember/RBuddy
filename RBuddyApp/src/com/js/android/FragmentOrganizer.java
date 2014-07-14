package com.js.android;

import static com.js.android.Tools.*;

import com.js.form.FormWidget;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class FragmentOrganizer {

	private static final int FRAGMENT_SLOT_BASE_ID = 992;

	/**
	 * Constructor
	 * 
	 * @param activity
	 */
	public FragmentOrganizer(MyActivity activity) {
		mNumberOfSlots = 2;
		log("Constructing for parent " + nameOf(activity));
		this.mActivity = activity;

		// TODO: reenable small device override for development purposes
		mSupportsDual = (mNumberOfSlots >= 2);
		createContainer();
	}

	private void createContainer() {
		// Create view with a horizontal row of panels, one for each slot
		LinearLayout layout = new LinearLayout(mActivity);
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
	}

	private void log(Object message) {
		if (mLogging) {
			StringBuilder sb = new StringBuilder("...> ");
			sb.append(nameOf(this));
			sb.append(" : ");
			tab(sb, 30);
			sb.append(message);
			pr(sb);
		}
	}

	/**
	 * Determine if device can display two fragments side-by-side instead of
	 * just one
	 * 
	 * @return
	 */
	public boolean supportDualFragments() {
		return mSupportsDual;
	}

	/**
	 * Get the view that is to contain the fragments
	 * 
	 * @return
	 */
	public View getContainer() {
		log("getView, returning " + describe(mSlotsContainerWrapper));
		return mSlotsContainerWrapper;
	}

	public void focusOn(FragmentReference r) {
		focusOn(r, true);
	}

	/**
	 * Display a fragment, if it isn't already in one of the slots
	 * 
	 * @param r
	 *            FragmentReference
	 */
	public void focusOn(FragmentReference r, boolean auxilliarySlot) {
		MyFragment fragment = r.f();

		do {
			// If fragment is already visible, ignore
			if (fragment.isVisible())
				return;

			int slot = auxilliarySlot ? mNumberOfSlots - 1 : 0;

			int slotId = FRAGMENT_SLOT_BASE_ID + slot;

			if (db)
				pr(" plotting to slotId: " + slotId);

			FragmentManager m = mActivity.getFragmentManager();
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

	public void setLogging(boolean f) {
		mLogging = f;
	}

	private ViewGroup buildSlotView(int slot) {
		FrameLayout f2 = new FrameLayout(mActivity);
		f2.setId(FRAGMENT_SLOT_BASE_ID + slot);
		FormWidget.setDebugBgnd(f2, (slot == 0) ? "#206020" : "#202060");
		return f2;
	}

	private MyActivity mActivity;
	private int mNumberOfSlots;
	private LinearLayout mSlotsContainer;
	private View mSlotsContainerWrapper;
	private boolean mSupportsDual;
	private boolean mLogging;
}
