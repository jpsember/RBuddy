package com.js.rbuddyapp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import static com.js.android.Tools.*;

import com.js.form.FormWidget;
import com.js.json.JSONEncoder;
import com.js.json.JSONParser;

/**
 * Class to encapsulate some of the complexity of dealing with fragments.
 * 
 * Conceptually, manages the display of up to two side-by-side fragments
 * 
 * 
 * Problems: [] back button doesn't update slotContents properly
 * 
 */
public class FragmentOrganizer {

	private static final int MAX_SLOTS = 2;
	private static final String BUNDLE_PERSISTENCE_KEY = "FragmentOrganizer";

	// private static final int VIEW_ID_BASE = 9500; // Arbitrary number

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent activity
	 * @return
	 */
	public FragmentOrganizer(Activity parent, int viewIdBase) {
		unimp("maybe keep particular fragment tied to a particular container");
		this.parent = parent;
		this.viewIdBase = viewIdBase;

		// this.fragmentMap = new HashMap();
		this.slotContents = new String[MAX_SLOTS];
		factories = new HashMap();
		constructContainer();
	}

	public void register(MyFragment.Factory factory) {
		factories.put(factory.tag(), factory);
	}

	public void restoreState(Bundle savedInstanceState) {
		String json = null;
		if (savedInstanceState != null) {
			json = savedInstanceState.getString(BUNDLE_PERSISTENCE_KEY);
			restoreFromJSON(json);
		}
	}

	/**
	 * Save organizer state within bundle
	 * 
	 * @param bundle
	 */
	public void onSaveInstanceState(Bundle bundle) {
		bundle.putString(BUNDLE_PERSISTENCE_KEY, encodeToJSON());
	}

	/**
	 * Determine if device, in its current orientation, can display two
	 * fragments side-by-side instead of just one
	 * 
	 * @return
	 */
	public boolean supportDualFragments() {
		unimp("also verify device is large enough");
		return parent.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	/**
	 * Get the view that is to contain the fragments
	 * 
	 * @return
	 */
	public View getView() {
		return container;
	}

	/**
	 * Display a fragment within a slot
	 * 
	 * @param tag
	 *            name of fragment, or null to clear the slot
	 * @param slot
	 * @return fragment displayed, or null
	 */
	public MyFragment plot(String tag, int slot) {
		final boolean db = true;
		if (db)
			pr(hey() + tag + " slot " + slot);
		pr(dumpManager());

		String oldName = slotContents[slot];
		String newName = null;
		MyFragment newFragment = null;
		if (tag != null) {
			newName = tag;
			newFragment = get(tag, true);
		}
		MyFragment oldFragment = null;
		FragmentManager m = parent.getFragmentManager();
		if (oldName != null)
			oldFragment = (MyFragment) m.findFragmentByTag(oldName);

		if (db)
			pr("oldFrament: " + describe(oldFragment) + "\nnewFragment: "
					+ describe(newFragment));

		if (oldFragment != newFragment) {
			// If new fragment exists in another slot, remove it
			if (newName != null && newName.equals(slotContents[1 - slot])) {
				if (db)
					pr(" removing from other slot first");
				plot(null, 1 - slot);
			}

			FragmentTransaction transaction = m.beginTransaction();
			if (oldFragment == null) {
				transaction.add(viewIdBase + slot, newFragment, newName);
			} else if (newFragment == null) {
				transaction.remove(oldFragment);
			} else {
				if (false) {
					warning("explicitly removing and adding");
					transaction.remove(oldFragment);
					transaction.commit();
					transaction = m.beginTransaction();
					transaction.add(viewIdBase + slot, newFragment, newName);
				} else
					transaction
							.replace(viewIdBase + slot, newFragment, newName);
			}
			transaction.addToBackStack(null);
			transaction.commit();
			slotContents[slot] = newName;
			setViewWithinSlot(buildSlotView(slot), slot);

			if (oldFragment != null) {
				Fragment fz = m.findFragmentByTag(oldFragment.getTag());
				if (fz != null) {
					warning("got rid of oldFragment, but manager still sees it: "
							+ fz);
				}
			}
		}
		return newFragment;
	}

	/**
	 * Display a fragment, if it is not already visible, and make it the
	 * 'current' fragment
	 * 
	 * @param tag
	 *            name of fragment
	 * @param auxilliary
	 *            if true, and not visible, favors rightmost (lower-priority)
	 *            slot
	 * 
	 * @return
	 */
	public MyFragment open(String tag, boolean auxilliary) {

		final boolean db = true;
		if (db)
			pr(hey() + "tag=" + tag + " aux=" + auxilliary);

		MyFragment f = get(tag, false);
		if (db)
			pr(" existing fragment=" + f);
		if (f != null) {
			if (f.isVisible())
				return f;
		}

		int slot = 0;
		if (auxilliary && supportDualFragments())
			slot = 1;
		if (db)
			pr(" plotting to slot " + slot);
		return plot(tag, slot);
	}

	/**
	 * Encode state to JSON string
	 * 
	 * @return JSON string
	 */
	private String encodeToJSON() {
		JSONEncoder json = new JSONEncoder();
		json.enterList();
		json.encode(slotContents);
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

		// Parse slot contents
		// TODO: what if the number of slots is inappropriate for current
		// orientation?
		{
			p.enterList();
			for (int i = 0; i < MAX_SLOTS; i++) {
				Object obj = p.next();
				if (obj != Boolean.FALSE) {
					slotContents[i] = (String) obj;
				}
			}
			p.exit();
		}
		p.exit();
	}

	private void constructContainer() {
		// Create view with two panels side by side
		LinearLayout layout = new LinearLayout(parent);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		FormWidget.setDebugBgnd(layout, "#602020");
		container = layout;

		for (int i = 0; i < MAX_SLOTS; i++) {
			View f2 = buildSlotView(i);
			setViewWithinSlot(f2, i);
		}
	}

	private ViewGroup buildSlotView(int slot) {
		// Don't add views for empty slots
		if (slotContents[slot] == null)
			return null;

		FrameLayout f2 = new FrameLayout(parent);
		f2.setId(viewIdBase + slot);
		FormWidget.setDebugBgnd(f2, (slot == 0) ? "#206020" : "#202060");
		return f2;
	}

	/**
	 * Get fragment; construct if necessary
	 * 
	 * @param factory
	 * @param constructIfMissing
	 *            if true, and no such fragment found, a new one is constructed
	 * @return fragment, or null
	 */
	private MyFragment get(String tag, boolean constructIfMissing) {
		FragmentManager m = parent.getFragmentManager();
		MyFragment f = (MyFragment) m.findFragmentByTag(tag);
		if (f == null && constructIfMissing) {
			f = factory(tag).construct();
		}
		return f;
	}

	private MyFragment.Factory factory(String tag) {
		MyFragment.Factory f = factories.get(tag);
		if (f == null)
			throw new IllegalArgumentException("no factory registered for: "
					+ tag);
		return f;
	}

	private String dumpManager() {
		FragmentManager m = parent.getFragmentManager();
		StringWriter w = new StringWriter();

		m.dump("Mgr:", null, new PrintWriter(w), null);
		return w.toString();
	}

	/**
	 * Store a view within a slot, replacing any existing view
	 * 
	 * @param view
	 *            new view for slot, or null
	 * @param slot
	 */
	private void setViewWithinSlot(View view, int slot) {
		if (slot < container.getChildCount())
			container.removeViewAt(slot);
		if (view == null)
			return;
		ASSERT(slot <= container.getChildCount());
		container.addView(view, slot, new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
	}

	// Map of factories, keyed by tag
	private Map<String, MyFragment.Factory> factories;

	// Fragments within each slot (identified by tag); null if unoccupied
	private String[] slotContents;

	private Activity parent;
	private LinearLayout container;
	private int viewIdBase;
}
