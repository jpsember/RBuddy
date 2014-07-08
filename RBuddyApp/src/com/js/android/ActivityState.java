package com.js.android;

import java.util.ArrayList;
import java.util.List;
import com.js.json.*;
import static com.js.basic.Tools.*;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ScrollView;

/**
 * Utility class to save and restore activity state.
 * 
 * Instances of the class can be populated with certain types of stateful
 * objects. For example, one supported type are ListViews. The scroll position
 * of these views are saved and restored.
 * 
 */
public class ActivityState {

	private static final String KEY_ACTIVITYSTATE = "activityState";

	public void setLogging(boolean f) {
		mLogging = f;
	}

	private void log(Object message) {
		if (mLogging) {
			StringBuilder sb = new StringBuilder("---> ");
			sb.append(nameOf(this));
			sb.append(" : ");
			tab(sb, 30);
			sb.append(message);
			pr(sb);
		}
	}

	public ActivityState() {
		setLogging(true);
		elements = new ArrayList();
		log("constructed ActivityState");
	}

	public ActivityState add(Object element) {
		log("adding " + describe(element));
		elements.add(element);
		return this;
	}

	public void saveState(Bundle outState) {
		JSONEncoder enc = new JSONEncoder();
		enc.enterList();
		for (Object element : elements) {
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
		String jsonString = enc.toString();
		log("saveState as " + jsonString);
		outState.putString(KEY_ACTIVITYSTATE, jsonString);
	}

	public ActivityState restoreStateFrom(Bundle savedInstanceState) {
		log("restoreStateFrom " + savedInstanceState);
		do {
			if (savedInstanceState == null)
				break;

			String jsonString = savedInstanceState.getString(KEY_ACTIVITYSTATE);
			log("jsonString: " + jsonString);
			if (jsonString == null)
				break;
			JSONParser parser = new JSONParser(jsonString);
			parser.enterList();

			for (Object element : elements) {
				log("element:" + element);
				if (!parser.hasNext()) {
					warning("ran out of elements in saved state");
					break;
				}
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
			if (parser.hasNext()) {
				warning("extra elements in saved state");
			}
		} while (false);
		return this;
	}

	private List elements;
	private boolean mLogging;
}
