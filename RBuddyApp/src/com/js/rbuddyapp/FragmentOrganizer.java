package com.js.rbuddyapp;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
 */
public class FragmentOrganizer {

	private static final int MAX_SLOTS = 2;
	private static final String BUNDLE_PERSISTENCE_KEY = "FragmentOrganizer";
	private static final int VIEW_ID_BASE = 9500; // Arbitrary number

	/**
	 * Factory constructor
	 * 
	 * @param parent
	 *            parent activity
	 * @param savedInstanceState
	 *            state to restore from (or null)
	 * @return
	 */
	public static FragmentOrganizer construct(Activity parent,
			Bundle savedInstanceState) {
		String json = null;
		if (savedInstanceState != null)
			json = savedInstanceState.getString(BUNDLE_PERSISTENCE_KEY);
		return new FragmentOrganizer(parent, json);
	}

	private FragmentOrganizer(Activity parent, String jsonState) {

		this.parent = parent;
		this.fragmentMap = new HashMap();
		this.slotContents = new String[MAX_SLOTS];

		// Fragments are preserved in the bundle, so we must preserve
		// FragmentOrganizers as well

		if (jsonState != null) {
			restoreFromJSON(jsonState);
		}

		constructContainer();
	}

	private void restoreFromJSON(String jsonString) {
		FragmentManager m = parent.getFragmentManager();
		JSONParser p = new JSONParser(jsonString);
		p.enterList();

		// Parse fragment map key set
		{
			p.enterList();
			while (p.hasNext()) {
				String tag = p.nextString();
				Fragment f = m.findFragmentByTag(tag);
				if (f == null) {
					warning("can't find fragment for tag " + tag);
					continue;
				}
				fragmentMap.put(tag, f);
			}
			p.exit();
		}

		// Parse slot contents
		{
			p.enterList();
			for (int i = 0; i < MAX_SLOTS; i++) {
				Object obj = p.next();
				if (obj == Boolean.FALSE) {
				} else {
					slotContents[i] = (String) obj;
				}
			}
			p.exit();
		}
		p.exit();
	}

	/**
	 * Save organizer state within bundle
	 * 
	 * @param bundle
	 */
	public void onSaveInstanceState(Bundle bundle) {
		JSONEncoder json = new JSONEncoder();
		json.enterList();
		json.encode(fragmentMap.keySet());
		json.encode(slotContents);
		json.exit();
		bundle.putString(BUNDLE_PERSISTENCE_KEY, json.toString());
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
		f2.setId(VIEW_ID_BASE + slot);
		FormWidget.setDebugBgnd(f2, (slot == 0) ? "#206020" : "#202060");
		return f2;
	}

	/**
	 * Get the view that is to contain the fragments
	 * 
	 * @return
	 */
	public View getView() {
		return container;
	}

	public boolean has(String identifier) {
		this.lastKey = identifier;
		return get(identifier) != null;
	}

	/**
	 * Get fragment, if it exists
	 * 
	 * @param identifier
	 *            the identifier that uniquely identifies this fragment from
	 *            others in this organizer
	 * @return fragment, or null
	 */
	public Fragment get(String identifier) {
		if (identifier == null)
			return null;

		Fragment f = fragmentMap.get(identifier);
		if (f == null) {
			// See if it's in the fragment manager; if so, add it to the map as
			// well
			FragmentManager m = parent.getFragmentManager();
			f = m.findFragmentByTag(identifier);
			if (f != null)
				fragmentMap.put(identifier, f);
		}
		return f;
	}

	public Fragment get() {
		ASSERT(lastKey != null);
		return get(lastKey);
	}

	/**
	 * Add fragment
	 * 
	 * @param identifier
	 *            the identifier that uniquely identifies this fragment from
	 *            others in this organizer
	 * @param f
	 *            the fragment
	 */
	public void add(String identifier, Fragment f) {
		if (db)
			pr("addFragment " + identifier + " : " + f);
		ASSERT(get(identifier) == null);
		fragmentMap.put(identifier, f);
		if (db)
			pr(" map now " + d(fragmentMap));
	}

	public void add(Fragment f) {
		ASSERT(lastKey != null);
		add(lastKey, f);
	}

	public void plotFragment(String identifier, int slot) {
		String oldName = slotContents[slot];
		String newName = identifier;
		Fragment oldFragment = get(oldName);
		Fragment newFragment = get(newName);

		FragmentManager m = parent.getFragmentManager();
		if (oldFragment != newFragment) {
			FragmentTransaction fragmentTransaction = m.beginTransaction();
			if (oldFragment == null) {
				fragmentTransaction.add(VIEW_ID_BASE + slot, newFragment,
						newName);
			} else if (newFragment == null) {
				fragmentTransaction.remove(oldFragment);
			} else {
				fragmentTransaction.replace(VIEW_ID_BASE + slot, newFragment,
						newName);
			}
			fragmentTransaction.commit();
			slotContents[slot] = newName;
			setViewWithinSlot(buildSlotView(slot), slot);
		}
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

	private String[] slotContents;
	private Map<String, Fragment> fragmentMap;
	private Activity parent;
	private LinearLayout container;
	private String lastKey;
}
