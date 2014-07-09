package com.js.android;

import java.util.ArrayList;
import java.util.List;
import com.js.json.*;
import static com.js.basic.Tools.*;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ScrollView;

/**
 * Utility class to save and restore the states of views
 * 
 * Instances of the class can be populated with certain types of stateful
 * objects. For example, one supported type are ListViews. The scroll position
 * of these views are saved and restored.
 * 
 */
public class ViewStates {

	public void setLogging(boolean f) {
		mLogging = f;
	}

	private void log(Object message) {
		if (mLogging) {
			StringBuilder sb = new StringBuilder("---> ");
			sb.append(this);
			sb.append(" : ");
			tab(sb, 30);
			sb.append(message);
			pr(sb);
		}
	}

	public ViewStates(String persistenceKey) {
		this(persistenceKey, false);
	}

	public ViewStates(String persistenceKey, boolean logging) {
		mPersistenceKey = persistenceKey;
		setLogging(logging);
		mElements = new ArrayList();
		log("constructed ActivityState");
	}

	/**
	 * Remove all elements
	 * 
	 * @return this, to support method chaining
	 */
	protected ViewStates clearElementList() {
		log("clearElementList");
		mElements.clear();
		return this;
	}

	/**
	 * Add an element
	 * 
	 * @param element
	 * @return this, to support method chaining
	 */
	public ViewStates add(Object element) {
		log("add " + describe(element));
		mElements.add(element);
		return this;
	}

	/**
	 * Construct an internal snapshot of the state of the various elements
	 */
	public void recordSnapshot() {
		log("recordSnapshot");
		JSONEncoder enc = new JSONEncoder();
		enc.enterList();
		enc.encode(mElements.size());
		for (Object element : mElements) {
			if (element instanceof ListView) {
				ListView lv = (ListView) element;
				enc.encode(lv.getFirstVisiblePosition());
			} else if (element instanceof ScrollView) {
				ScrollView sv = (ScrollView) element;
				int x = sv.getScrollX();
				int y = sv.getScrollY();
				enc.encode(x);
				enc.encode(y);
			} else
				warning("cannot handle element " + element);
		}
		enc.exit();
		mJsonState = enc.toString();
		log(" snapshot: " + mJsonState);
	}

	public void persistSnapshot(Bundle outState) {
		log("persistSnapshot (JSON " + mJsonState + ") to bundle "
				+ nameOf(outState));
		String jsonString = mJsonState;
		outState.putString(mPersistenceKey, jsonString);
	}

	public ViewStates retrieveSnapshotFrom(Bundle savedInstanceState) {
		log("retrieveSnapshotFrom: " + nameOf(savedInstanceState));
		do {
			if (savedInstanceState == null)
				break;

			String jsonString = savedInstanceState.getString(mPersistenceKey);
			log("jsonString: " + jsonString);
			this.mJsonState = jsonString;
		} while (false);
		return this;
	}

	/**
	 * Restore state from previously stored JSON string
	 */
	public ViewStates restoreViewsFromSnapshot() {
		log("restoreViewsFromSnapshot, JSON: " + mJsonState);
		do {
			if (mJsonState == null)
				break;

			String jsonString = mJsonState;
			JSONParser parser = new JSONParser(jsonString);
			parser.enterList();

			int persistCount = parser.nextInt();
			if (persistCount != mElements.size()) {
				log("saved elements differs from actual;\n JSON state: "
						+ mJsonState + "\n elements:" + d(mElements) + "\n "
						+ this);
				break;
			}

			for (Object element : mElements) {
				if (element instanceof ListView) {
					ListView lv = (ListView) element;
					int cursor = parser.nextInt();
					lv.setSelection(cursor);
				} else if (element instanceof ScrollView) {
					ScrollView sv = (ScrollView) element;
					sv.setScrollX(parser.nextInt());
					sv.setScrollY(parser.nextInt());
				} else
					warning("cannot handle element " + element);
			}

		} while (false);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(nameOf(this));
		sb.append("[" + mPersistenceKey + "]");
		return sb.toString();
	}

	private String mPersistenceKey;
	private List mElements;
	private boolean mLogging;
	private String mJsonState;
}
