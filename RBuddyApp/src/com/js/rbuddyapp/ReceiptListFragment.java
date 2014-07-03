package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.js.android.ActivityState;
import com.js.rbuddy.Receipt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ReceiptListFragment extends MyFragment {

	public static final int MESSAGE_CODE_RECEIPT_SELECTED = 1;
	public static final String TAG = "ReceiptList";

	public static final Factory FACTORY = new Factory() {

		@Override
		public String tag() {
			return TAG;
		}

		@Override
		public MyFragment construct() {
			return new ReceiptListFragment();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = RBuddyApp.sharedInstance();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		constructListView();
		activityState = new ActivityState() //
				.add(receiptListView) //
				.restoreStateFrom(savedInstanceState);
		return receiptListView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		activityState.saveState(outState);
	}

	// /**
	// * Add a listener for events from this fragment. Events are sent as
	// * Messages, with .what holding the MESSAGE_CODE, and .obj holding the
	// * Receipt
	// *
	// * @param listener
	// * Handler to receive messages
	// */
	// public void addListener(Handler listener) {
	// listeners.add(listener);
	// }

	private List<Receipt> buildListOfReceipts() {
		ArrayList list = new ArrayList();
		rebuildReceiptList(list);
		return list;
	}

	// private void invalidateReceiptList() {
	// receiptListValid = false;
	// refreshReceiptAtPosition = null;
	// }

	private void rebuildReceiptList(List list) {
		list.clear();
		for (Iterator it = app.receiptFile().iterator(); it.hasNext();)
			list.add(it.next());
		Collections.sort(list, Receipt.COMPARATOR_SORT_BY_DATE);
		receiptListValid = true;

		if (receiptListAdapter != null)
			receiptListAdapter.notifyDataSetChanged();
	}

	// Construct a view to be used for the list items
	private void constructListView() {

		ListView listView = new ListView(this.getActivity());

		List<Receipt> receiptList = buildListOfReceipts();
		ArrayAdapter arrayAdapter = new ReceiptListAdapter(getActivity(),
				receiptList);
		listView.setAdapter(arrayAdapter);

		// Store references to both the ArrayAdapter and the backing ArrayList,
		// to make responding to selection actions more convenient.
		this.receiptListAdapter = arrayAdapter;
		this.receiptList = receiptList;
		this.receiptListView = listView;

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView aView, View v, int position,
					long id) {
				processReceiptSelection(position);
			}
		});
		LayoutParams layoutParam = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		listView.setLayoutParams(layoutParam);
	}

	/**
	 * Process user selecting receipt from receipt list
	 * 
	 * @param position
	 */
	private void processReceiptSelection(int position) {
		Receipt r = receiptList.get(position);
		final boolean db = true;
		if (db)
			pr(hey() + "position=" + position);
		listener().receiptSelected(r);
		//
		// if (db)
		// pr(hey() + "receipt=" + r);
		// for (Handler listener : listeners) {
		// if (db)
		// pr(" sending message to listener " + listener);
		// listener.sendMessage(Message.obtain(null,
		// MESSAGE_CODE_RECEIPT_SELECTED, r));
		// }
		//
		// // We will need to refresh this item when returning from the edit
		// // activity in case its contents have changed
		// refreshReceiptAtPosition = position;
		// // doEditReceipt(receiptListAdapter.getItem(position));
	}

	// private ViewGroup constructReceiptListContainer() {
	// LinearLayout layout = new LinearLayout(this);
	// FormWidget.setDebugBgnd(layout, app.useGoogleAPI() ? "#000030"
	// : "#004000");
	// layout.setOrientation(LinearLayout.VERTICAL);
	// return layout;
	// }

	// If false, we assume the current receipt list (if one exists) is
	// invalid,
	// and a new one needs to be built
	/* private */boolean receiptListValid;

	// If non-null, we refresh the receipt at this position in the list when
	// resuming the activity
	/* private */Integer refreshReceiptAtPosition;

	//
	// // If non-null, this receipt was just edited
	// private Receipt editReceipt;

	private Listener listener() {
		return (Listener) getActivity();
	}

	private ArrayAdapter<Receipt> receiptListAdapter;
	private List<Receipt> receiptList;
	private RBuddyApp app;
	private ListView receiptListView;
	private ActivityState activityState;

	// private Set<Handler> listeners = new HashSet();

	// @Override
	// protected void processMessage(Message m) {
	// // TODO Auto-generated method stub
	//
	// }

	public static interface Listener {
		void receiptSelected(Receipt r);
	}
}
