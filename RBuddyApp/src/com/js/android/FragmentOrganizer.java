package com.js.android;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import static com.js.android.Tools.*;

import com.js.android.AppPreferences;
import com.js.form.FormWidget;
import com.js.json.JSONEncoder;
import com.js.json.JSONParser;

/**
 * Class to encapsulate some of the complexity of dealing with fragments.
 * 
 * Conceptually, manages the display of up to two side-by-side fragments
 * 
 * Problems:
 * 
 * [] the back stack and orientation changes highlight a problem: the system may
 * destroy and construct fragments, so we can't enforce a singleton pattern with
 * fragment instances. For example, even if we construct a singleton EditReceipt
 * fragment, if the user rotates the device, a new one will be created by the
 * system that will be distinct from our singleton instance. One approach: treat
 * fragments as containers of a separate class of objects that WILL obey the
 * singleton pattern. For example, let 'Editor' be the class wrapped by the
 * EditReceiptFragment. Even if multiple EditReceiptFragments exist, they will
 * all have a reference to the single Editor instance.
 * 
 */
public class FragmentOrganizer {

	private static final boolean mLogging = true;

	private static final String BUNDLE_PERSISTENCE_KEY2 = "FragmentOrganizer";

	/**
	 * Use different keys based on whether device supports dual fragments
	 * 
	 * @return
	 */
	private String getBundlePersistenceKey() {
		return BUNDLE_PERSISTENCE_KEY2 + (mSupportsDual ? "2" : "1");
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent activity
	 * @return
	 */
	public FragmentOrganizer(Activity parent) {
		log("Constructing for parent " + nameOf(parent));
		this.mParentActivity = parent;
		this.mSlotViewBaseId = 1900;

		mSupportsDual = getDeviceSize(mParentActivity) >= DEVICESIZE_LARGE;
		if (AppPreferences.getBoolean(App.PREFERENCE_KEY_SMALL_DEVICE_FLAG,
				false)) {
			mSupportsDual = false;
		}

		mNumberOfSlots = mSupportsDual ? 2 : 1;

		mDesiredSlotContents = new String[mNumberOfSlots];

		mFragmentFactories = new HashMap();
		mFragmentMap = new HashMap();

		if (false)
			info(null); // get rid of unreferenced warning
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

	public void setBaseViewId(int id) {
		this.mSlotViewBaseId = id;
	}

	public FragmentOrganizer register(MyFragment.Factory factory) {
		mFragmentFactories.put(factory.name(), factory);
		return this;
	}

	public void onCreate(Bundle savedInstanceState) {
		log("onCreate savedInstanceState=" + nameOf(savedInstanceState));

		constructContainer();

		String json = null;
		if (savedInstanceState != null) {
			json = savedInstanceState.getString(getBundlePersistenceKey());
			if (json == null) {
				warning("JSON string was null!");
			} else
				restoreFromJSON(json);
		}

		restoreFragmentState(savedInstanceState);
	}

	private void restoreFragmentState(Bundle bundle) {
		assertUIThread();
		for (MyFragment f : mSingletonObjects.values()) {
			f.onRestoreInstanceState(bundle);
		}
	}

	public Activity getActivity() {
		return mParentActivity;
	}

	private void removeFragmentsFromOldViews() {
		FragmentManager m = mParentActivity.getFragmentManager();

		for (int slot = 0; slot < mNumberOfSlots; slot++) {
			Fragment oldFragment = m.findFragmentById(mSlotViewBaseId + slot);
			if (oldFragment == null)
				continue;
			FragmentTransaction transaction = m.beginTransaction();
			transaction.remove(oldFragment);
			transaction.commit();
		}
		m.executePendingTransactions();
	}

	private void restoreDesiredFragmentsToViews() {
		FragmentManager m = mParentActivity.getFragmentManager();

		for (int slot = 0; slot < mNumberOfSlots; slot++) {
			String name = mDesiredSlotContents[slot];
			if (name == null)
				continue;
			MyFragment newFragment = get(name, true);
			{
				log(" adding fragment " + nameOf(newFragment) + " to slot "
						+ slot + " with name " + name);
				FragmentTransaction transaction = m.beginTransaction();
				transaction.add(mSlotViewBaseId + slot, newFragment, name);
				transaction.commit();
			}
		}
		m.executePendingTransactions();
	}

	public void onResume() {
		log("onResume");
		mIsResumed = true;

		// Remove any fragments that the manager is reporting are already
		// added, since they are probably added to old views and we have new
		// ones to populate
		removeFragmentsFromOldViews();

		restoreDesiredFragmentsToViews();
	}

	public void onPause() {
		log("onPause");
	}

	/**
	 * Save organizer state within bundle
	 * 
	 * @param bundle
	 */
	public void onSaveInstanceState(Bundle bundle) {
		log("onSaveInstanceState bundle=" + nameOf(bundle));
		bundle.putString(getBundlePersistenceKey(), encodeToJSON());

		persistFragmentState(bundle);

		mIsResumed = false;
	}

	private void persistFragmentState(Bundle bundle) {
		assertUIThread();
		for (MyFragment f : mSingletonObjects.values()) {
			f.onSaveInstanceState(bundle);
		}
	}

	/**
	 * Determine if device can display two fragments side-by-side instead of
	 * just one
	 * 
	 * @return
	 */
	public boolean supportDualFragments() {
		return this.mSupportsDual;
	}

	/**
	 * Get the view that is to contain the fragments
	 * 
	 * @return
	 */
	public View getView() {
		return mSlotsContainer;
	}

	private MyFragment fragmentInSlot(int slot) {
		FragmentManager m = mParentActivity.getFragmentManager();
		return (MyFragment) m.findFragmentById(mSlotViewBaseId + slot);
	}

	private static String fragmentName(Fragment f) {
		String tag = null;
		if (f != null) {
			tag = f.getTag();
			if (tag == null)
				throw new IllegalStateException("fragment has no tag");
		}
		return tag;
	}

	private int slotContainingFragment(String fragmentName) {
		for (int s = 0; s < mNumberOfSlots; s++) {
			if (!isResumed()) {
				if (fragmentName.equals(mDesiredSlotContents[s]))
					return s;
			} else {
				MyFragment f = fragmentInSlot(s);
				if (f == null)
					continue;
				if (f.getTag().equals(fragmentName))
					return s;
			}
		}
		return -1;
	}

	/**
	 * Display a fragment, if it isn't already in one of the slots
	 * 
	 * @param tag
	 *            name of fragment
	 * @param primary
	 *            true to use the primary (leftmost) slot; otherwise, use one of
	 *            the auxilliary ones (uses primary if it's the only slot)
	 * @return if in resumed state, the fragment displayed; else null
	 */
	public MyFragment plot(String tag, boolean primary, boolean undoable) {
		log("plot " + tag + " primary=" + d(primary) + " undoable="
				+ d(undoable));

		ASSERT(tag != null);
		int slot = primary ? 0 : mNumberOfSlots - 1;
		log(" slot=" + slot);

		// If new fragment exists in another slot, just use that one
		int actualSlot = slotContainingFragment(tag);
		if (actualSlot >= 0) {
			log(" already found in slot " + actualSlot);
			// if (!isResumed())
			// return null;
			return get(tag, true);
		}

		if (!isResumed()) {
			mDesiredSlotContents[slot] = tag;
			return null;
		}

		String oldName = fragmentName(fragmentInSlot(slot));

		MyFragment newFragment = get(tag, true);
		log(" newFragment " + describe(newFragment) + " oldName=" + oldName);
		if (tag.equals(oldName))
			return newFragment;

		MyFragment oldFragment = null;
		if (oldName != null)
			oldFragment = get(oldName, false);

		FragmentManager m = mParentActivity.getFragmentManager();

		FragmentTransaction transaction = m.beginTransaction();
		if (oldFragment == null) {
			log(" doing transaction:add");
			transaction.add(mSlotViewBaseId + slot, newFragment, tag);
		} else {
			// TODO try to get these working; currently says:
			// RuntimeException: Unknown animator name: translate.
			//
			// transaction.setCustomAnimations(R.anim.slide_in_left,
			// R.anim.slide_out_right);
			log(" doing transaction:replace");
			transaction.replace(mSlotViewBaseId + slot, newFragment, tag);
		}
		if (undoable)
			transaction.addToBackStack(null);
		transaction.commit();
		log(" returning " + newFragment);
		return newFragment;
	}

	/**
	 * Encode state to JSON string
	 * 
	 * @return JSON string
	 */
	private String encodeToJSON() {
		JSONEncoder json = new JSONEncoder();
		json.enterList();
		for (int slot = 0; slot < mNumberOfSlots; slot++) {
			String name = fragmentName(fragmentInSlot(slot));
			if (name == null)
				name = "";
			json.encode(name);
		}
		json.exit();
		return json.toString();
	}

	/**
	 * Restore state from JSON string
	 * 
	 * @param jsonString
	 */
	private void restoreFromJSON(String jsonString) {
		JSONParser p = new JSONParser(jsonString);
		p.enterList();
		mDesiredSlotContents = new String[mNumberOfSlots];
		for (int slot = 0; slot < mNumberOfSlots; slot++) {
			String name = p.nextString();
			if (name.isEmpty())
				name = null;
			mDesiredSlotContents[slot] = name;
		}
		p.exit();
	}

	private void constructContainer() {
		// Create view with a horizontal row of panels, one for each slot
		LinearLayout layout = new LinearLayout(mParentActivity);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		FormWidget.setDebugBgnd(layout, "#602020");
		mSlotsContainer = layout;

		for (int slot = 0; slot < mNumberOfSlots; slot++) {
			View v = buildSlotView(slot);
			mSlotsContainer.addView(v, slot, new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
		}
	}

	private ViewGroup buildSlotView(int slot) {
		FrameLayout f2 = new FrameLayout(mParentActivity);
		f2.setId(mSlotViewBaseId + slot);
		FormWidget.setDebugBgnd(f2, (slot == 0) ? "#206020" : "#202060");
		return f2;
	}

	/**
	 * Get fragment
	 * 
	 * @param name
	 *            name of fragment (from its factory's name() method)
	 * @return fragment, or null if none exists
	 */
	public MyFragment get(String name) {
		return get(name, false);
	}

	/**
	 * Get fragment; construct if necessary
	 * 
	 * @param factory
	 * @param constructIfMissing
	 *            if true, and no such fragment found, a new one is constructed
	 * @return fragment, or null
	 */
	public MyFragment get(String tag, boolean constructIfMissing) {
		MyFragment f;
		f = mFragmentMap.get(tag);
		if (f != null)
			return f;

		FragmentManager m = mParentActivity.getFragmentManager();
		f = (MyFragment) m.findFragmentByTag(tag);
		if (f == null && constructIfMissing) {
			f = factory(tag).construct();
			if (db)
				pr("\n  ======================= Constructed instance of " + tag
						+ "\n");
			mFragmentMap.put(tag, f);
		}
		return f;
	}

	private MyFragment.Factory factory(String tag) {
		MyFragment.Factory f = mFragmentFactories.get(tag);
		if (f == null)
			throw new IllegalArgumentException("no factory registered for: "
					+ tag);
		return f;
	}

	private String info(MyFragment f) {
		if (f == null)
			return "<null>";
		StringBuilder sb = new StringBuilder(nameOf(f));
		sb.append(" [");
		if (f.isAdded())
			sb.append(" added");
		if (f.isDetached())
			sb.append(" detached");
		if (f.isHidden())
			sb.append(" hidden");
		if (f.isInLayout())
			sb.append(" inLayout");
		if (f.isVisible())
			sb.append(" visible");
		sb.append(" ]");
		return sb.toString();
	}

	private boolean isResumed() {
		return mIsResumed;
	}

	public void setWrappedSingleton(MyFragment singleton) {
		assertUIThread();
		mSingletonObjects.put(singleton.getClass(), singleton);
	}

	public MyFragment getWrappedSingleton(Class singletonClass) {
		assertUIThread();
		MyFragment obj = mSingletonObjects.get(singletonClass);
		if (obj == null) {
			die("no singleton object found for "
					+ singletonClass.getSimpleName());
		}
		return obj;
	}

	private Map<Class, MyFragment> mSingletonObjects = new HashMap();

	// Map of factories, keyed by name
	private Map<String, MyFragment.Factory> mFragmentFactories;

	// Map of fragments that may not be known to FragmentManager
	private Map<String, MyFragment> mFragmentMap;

	private Activity mParentActivity;
	private LinearLayout mSlotsContainer;
	private int mSlotViewBaseId;

	// Names of the fragments we want in each slot
	private String[] mDesiredSlotContents;

	private boolean mIsResumed;
	private boolean mSupportsDual;
	private int mNumberOfSlots;
}
