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

	public ActivityState() {
		elements = new ArrayList();
	}

	public ActivityState add(Object element) {
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
		outState.putString(KEY_ACTIVITYSTATE, jsonString);
	}

	public ActivityState restoreStateFrom(Bundle savedInstanceState) {
		do {
			if (savedInstanceState == null)
				break;

			String jsonString = savedInstanceState.getString(KEY_ACTIVITYSTATE);
			if (jsonString == null)
				break;

			JSONParser parser = new JSONParser(jsonString);
			parser.enterList();

			for (Object element : elements) {
				if (!parser.hasNext()) {
					warning("ran out of elements in saved state");
					break;
				}
				if (element instanceof ListView) {
					ListView lv = (ListView) element;
					lv.setSelection(parser.nextInt());
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
}
