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
import com.js.basic.UniqueIdentifier;
//import com.js.basic.UniqueIdentifier;
import com.js.form.FormWidget;
import com.js.json.JSONEncoder;
import com.js.json.JSONParser;

/**
 * Class to encapsulate some of the complexity of dealing with fragments.
 * 
 * Conceptually, manages the display of up to two side-by-side fragments
 */
public class FragmentOrganizer {

	private static/* final */boolean mLogging = false;

	private static final String BUNDLE_PERSISTENCE_KEY = "FragmentOrganizer";

	/**
	 * Use different keys based on whether device supports dual fragments
	 * 
	 * @return
	 */
	private String getBundlePersistenceKey() {
		return BUNDLE_PERSISTENCE_KEY + (mSupportsDual ? "2" : "1");
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent activity
	 * @return
	 */
	public FragmentOrganizer(Activity parent) {
		if (db)
			pr(hey(this));
		// uniqueId = sUniqueId++;

		log("Constructing for parent " + nameOf(parent));

		this.mLabel = parent.getClass().getSimpleName();
		registerWithGlobalMap();

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

	boolean isFactoryRegistered(String name) {
		return mFragmentFactories.containsKey(name);
	}

	void register(MyFragment.Factory factory) {
		if (db)
			pr(hey(this) + "registering factory " + nameOf(factory) + ", name "
					+ factory.name());
		mFragmentFactories.put(factory.name(), factory);
	}

	public void onCreate(Bundle savedInstanceState) {
		log("onCreate savedInstanceState=" + nameOf(savedInstanceState));

		constructContainer();

		String json = null;
		if (savedInstanceState != null) {
			json = savedInstanceState.getString(getBundlePersistenceKey());
			log(" recalled json: " + json);
			if (json == null) {
				warning("JSON string was null!");
			} else
				restoreFromJSON(json);
		}

		restoreFragmentState(savedInstanceState);
	}

	private void restoreFragmentState(Bundle bundle) {
		assertUIThread();
		for (PseudoFragment f : mSingletonObjects.values()) {
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
		String json = encodeToJSON();
		log(" state (JSON): " + json);
		bundle.putString(getBundlePersistenceKey(), json);

		persistFragmentState(bundle);

		mIsResumed = false;
	}

	private void persistFragmentState(Bundle bundle) {
		assertUIThread();
		for (PseudoFragment f : mSingletonObjects.values()) {
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
		return mSlotsContainerWrapper;
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
		mLogging = db;
		MyFragment newFragment = null;
		do {
			ASSERT(tag != null);
			int slot = primary ? 0 : mNumberOfSlots - 1;
			log("====plot==== tag: " + tag + " prim=" + d(primary) + " undo="
					+ d(undoable) + " slot=" + slot);

			// If new fragment exists in another slot, just use that one
			int actualSlot = slotContainingFragment(tag);
			if (actualSlot >= 0) {
				log(" already found in slot " + actualSlot);
				newFragment = get(tag, true);
				break;
			}

			// Update our internal desired slot contents
			mDesiredSlotContents[slot] = tag;

			// Only update the actual view if we're in the resumed state
			if (!isResumed()) {
				log("we're not in the resumed state, breaking");
				break;
			}

			String oldName = fragmentName(fragmentInSlot(slot));

			newFragment = get(tag, true);
			log(" newFragment " + describe(newFragment) + " oldName=" + oldName);
			if (tag.equals(oldName)) {
				newFragment = null;
				break;
			}

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

				log(" doing transaction:replace id " + (mSlotViewBaseId + slot)
						+ " tag " + tag + " newFragment=" + nameOf(newFragment));
				updateOrganizerFor(newFragment);
				transaction.replace(mSlotViewBaseId + slot, newFragment, tag);
				warning("!!!!!!!!!!!!!!!!!!!!!!!!!!! maybe the tag is already used by an old fragment");
			}
			if (undoable)
				transaction.addToBackStack(null);
			transaction.commit();
		} while (false);
		mLogging = false;
		log(" returning " + newFragment);
		return newFragment;
	}

	/**
	 * Pop the backstack
	 */
	public void pop() {
		getActivity().getFragmentManager().popBackStack();
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

		mSlotsContainer = layout;
		mSlotsContainerWrapper = wrapView(mSlotsContainer, nameOf(this));

		for (int slot = 0; slot < mNumberOfSlots; slot++) {
			View v = buildSlotView(slot);
			mSlotsContainer.addView(v, slot, new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
					// LayoutParams.MATCH_PARENT,
					1));
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
		if (db)
			pr(hey(this) + "tag=" + tag);
		MyFragment f;
		do {
			f = mFragmentMap.get(tag);
			if (f != null)
				break;

			FragmentManager m = mParentActivity.getFragmentManager();
			f = (MyFragment) m.findFragmentByTag(tag);
			if (f == null && constructIfMissing) {
				f = factory(tag).construct();
				if (db)
					pr(" ...constructed instance of " + tag);
				mFragmentMap.put(tag, f);
			}
		} while (false);
		if (f != null)
			updateOrganizerFor(f);
		return f;
	}

	private void updateOrganizerFor(MyFragment f) {
		if (db)
			pr(hey() + nameOf(f));
		PseudoFragment s = getWrappedSingleton(f.getFragmentClass());
		FragmentOrganizer current = s.getFragments();
		if (db)
			pr(" current org " + describe(current) + " new " + describe(this));
		if (current != this) {
			warning("updating organizer for " + describe(s) + " from "
					+ describe(current) + " to " + describe(this));
			// TODO: not sure if this is necessary; didn't fix the problem
			die("I don't think this is required");
			s.setFragments(this);
		}
	}

	private MyFragment.Factory factory(String tag) {
		if (db)
			pr(hey(this) + " looking for factory for tag " + tag);
		MyFragment.Factory f = mFragmentFactories.get(tag);
		if (f == null)
			throw new IllegalArgumentException("no factory registered for: "
					+ tag);
		return f;
	}

	/* private */String info(MyFragment f) {
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

	public <T extends PseudoFragment> T register(T singleton) {
		assertUIThread();
		mSingletonObjects.put(singleton.getClass(), singleton);
		if (db)
			pr(hey() + "put obj " + nameOf(singleton) + " into singleton map "
					+ nameOf(mSingletonObjects) + "\n");
		return singleton;
	}

	public PseudoFragment getWrappedSingleton(Class singletonClass) {
		assertUIThread();
		PseudoFragment obj = mSingletonObjects.get(singletonClass);
		if (db)
			pr(hey() + "got obj " + nameOf(obj) + " from map "
					+ nameOf(mSingletonObjects));
		if (obj == null) {
			die("no singleton object found for "
					+ singletonClass.getSimpleName());
		}
		return obj;
	}

	/**
	 * To deal with the problems of Android using no-argument fragment
	 * constructors to recreate old fragments, keep a global map of fragment
	 * organizers that will be pointed to by a bundle that we will store with
	 * each fragment when we initially create it (using some other one or more
	 * arg constructor). Each activity will give a particular name to their
	 * fragment organizer, and when a fragment is constructed, it will use this
	 * name to look in the map for the most recent fragment organizer to use.
	 */
	private static Map<String, FragmentOrganizer> mOrganizerMap = new HashMap();

	private void registerWithGlobalMap() {
		assertUIThread();
		final boolean db = true;
		if (db)
			pr(hey() + "map[" + mLabel + "] replace "
					+ UniqueIdentifier.nameFor(mOrganizerMap.get(mLabel))
					+ " with " + UniqueIdentifier.nameFor(this));
		mOrganizerMap.put(mLabel, this);
	}

	public static FragmentOrganizer getOrganizer(String label) {
		assertUIThread();
		return mOrganizerMap.get(label);
	}

	public String getLabel() {
		return mLabel;
	}

	/**
	 * Get the 'live', or most recent, version of this organizer
	 * 
	 * @return
	 */
	public FragmentOrganizer mostRecent() {
		// TODO: more efficient way of ensuring UI thread? Make it debug only?
		assertUIThread();
		FragmentOrganizer f = mOrganizerMap.get(getLabel());
		if (db)
			pr(hey() + " version in map=" + nameOf(f) + " this=" + nameOf(this));
		if (f == null)
			f = this;
		return f;
	}

	private Map<Class, PseudoFragment> mSingletonObjects = new HashMap();

	// Map of factories, keyed by name
	private Map<String, MyFragment.Factory> mFragmentFactories;

	// Map of fragments that may not be known to FragmentManager
	private Map<String, MyFragment> mFragmentMap;

	private Activity mParentActivity;
	private LinearLayout mSlotsContainer;
	private View mSlotsContainerWrapper;
	private int mSlotViewBaseId;

	// Names of the fragments we want in each slot
	private String[] mDesiredSlotContents;

	private boolean mIsResumed;
	private boolean mSupportsDual;
	private int mNumberOfSlots;
	private String mLabel;
}
