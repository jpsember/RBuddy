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
		public String name() {
			return TAG;
		}

		@Override
		public MyFragment construct() {
			return new ReceiptListFragment();
		}
	};

	/**
	 * Construct the singleton instance of this fragment, if it hasn't already
	 * been
	 * 
	 * @param organizer
	 * @return
	 */
	public static ReceiptListFragment construct(FragmentOrganizer organizer) {
		return (ReceiptListFragment) organizer.get(TAG, true);
	}

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

	// Methods this fragment provides (its non-fragment-related interface)

	public void refreshReceipt(Receipt r) {
		if (receiptListAdapter != null)
			receiptListAdapter.notifyDataSetChanged();
	}

	public void refreshList() {
		if (receiptList == null)
			return;
		rebuildReceiptList(receiptList);
	}

	private List<Receipt> buildListOfReceipts() {
		ArrayList list = new ArrayList();
		rebuildReceiptList(list);
		return list;
	}

	private void rebuildReceiptList(List list) {
		list.clear();
		for (Iterator it = app.receiptFile().iterator(); it.hasNext();)
			list.add(it.next());
		Collections.sort(list, Receipt.COMPARATOR_SORT_BY_DATE);

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
		if (db)
			pr(hey() + "position=" + position);
		listener().receiptSelected(r);
	}

	private Listener listener() {
		return (Listener) getActivity();
	}

	public static interface Listener {
		void receiptSelected(Receipt r);
	}

	private ArrayAdapter<Receipt> receiptListAdapter;
	private List<Receipt> receiptList;
	private RBuddyApp app;
	private ListView receiptListView;
	private ActivityState activityState;
}
