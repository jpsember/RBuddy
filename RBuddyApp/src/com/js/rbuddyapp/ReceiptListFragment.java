package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.js.android.AndroidDate;
import com.js.android.MyFragment;
import com.js.rbuddy.Receipt;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ReceiptListFragment extends MyFragment implements
		IRBuddyActivityListener {

	public ReceiptListFragment() {
		//setLogging(true);
	}

	private IRBuddyActivity getRBuddyActivity() {
		return (IRBuddyActivity) getActivity();
	}

	@Override
	public void onResume() {
		super.onResume();
		getRBuddyActivity().addListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getRBuddyActivity().removeListener(this);
	}

	private void prepareActivity() {
		ASSERT(getActivity() != null, "activity is null");
		// Perform class-specific initialization
		mApp = RBuddyApp.sharedInstance();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		prepareActivity();
		log("onCreateView savedState=" + nameOf(savedInstanceState));

		constructViews();

		getActivityState() //
				.add(mReceiptListView) //
				.restoreViewsFromSnapshot(savedInstanceState);

		return mContentView;
	}

	// IRBuddyActivityListener
	@Override
	public void activeReceiptChanged() {
	}

	@Override
	public void receiptFileChanged() {
		if (mReceiptList == null)
			return;
		rebuildReceiptList(mReceiptList);
	}

	@Override
	public void activeReceiptEdited() {
		if (mReceiptListAdapter != null)
			mReceiptListAdapter.notifyDataSetChanged();
	}

	private List<Receipt> buildListOfReceipts() {
		ArrayList list = new ArrayList();
		rebuildReceiptList(list);
		return list;
	}

	private void rebuildReceiptList(List list) {
		log("rebuildReceiptList");
		list.clear();
		for (Iterator it = mApp.receiptFile().iterator(); it.hasNext();)
			list.add(it.next());
		log("receipts size=" + list.size());

		Collections.sort(list, Receipt.COMPARATOR_SORT_BY_DATE);

		if (mReceiptListAdapter != null)
			mReceiptListAdapter.notifyDataSetChanged();
	}

	// Construct a view to be used for the list items
	private void constructViews() {
		ListView listView = new ListView(getActivity());

		List<Receipt> receiptList = buildListOfReceipts();
		ArrayAdapter arrayAdapter = new ReceiptListAdapter(getActivity(),
				receiptList);
		listView.setAdapter(arrayAdapter);

		// Store references to both the ArrayAdapter and the backing ArrayList,
		// to make responding to selection actions more convenient.
		this.mReceiptListAdapter = arrayAdapter;
		this.mReceiptList = receiptList;
		this.mReceiptListView = listView;
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView aView, View v, int position,
					long id) {
				processReceiptSelection(position);
			}
		});
		LayoutParams layoutParam = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		listView.setLayoutParams(layoutParam);
		this.mContentView = mReceiptListView;
		if (DEBUG_VIEWS)
			this.mContentView = wrapView(mReceiptListView,
					nameOf(mReceiptListView));
	}

	/**
	 * Process user selecting receipt from receipt list
	 * 
	 * @param position
	 */
	private void processReceiptSelection(int position) {
		Receipt r = mReceiptList.get(position);
		getRBuddyActivity().setActiveReceipt(r);
	}

	private static class ReceiptListAdapter extends ArrayAdapter {

		public ReceiptListAdapter(Context context, List list) {
			super(context, 0, list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Receipt r = (Receipt) getItem(position);
			if (db)
				pr("constructing cell for receipt " + r + ", position "
						+ position);

			View listItemView = convertView;

			if (listItemView == null) {
				if (db)
					pr("must construct new view");

				LinearLayout view = new LinearLayout(getContext());
				listItemView = view;
				final int LIST_ITEM_PADDING = 10;
				view.setPadding(LIST_ITEM_PADDING, LIST_ITEM_PADDING,
						LIST_ITEM_PADDING, LIST_ITEM_PADDING);
				view.setOrientation(LinearLayout.HORIZONTAL);

				final int LIST_ITEM_HEIGHT = 80;

				// Construct the various child views contained in this list
				// view. We'll refer to the individual views by tags that are
				// strings indicating their contents ("date","summary").

				{
					TextView tv = new TextView(getContext());
					tv.setMinEms(5);
					tv.setTag("date");
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT, LIST_ITEM_HEIGHT, 0.1f);
					tv.setLayoutParams(lp);
					view.addView(tv);
					tv.setPadding(10, 5, 10, 5);
				}
				{
					TextView tv = new TextView(getContext());
					tv.setTag("cost");
					tv.setMinEms(4);
					tv.setGravity(Gravity.RIGHT);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT, LIST_ITEM_HEIGHT, 0.1f);
					tv.setLayoutParams(lp);
					tv.setPadding(10, 5, 10, 5);
					view.addView(tv);
				}

				{
					TextView tv = new TextView(getContext());
					tv.setTag("summary");

					// Give this view any extra pixels by setting its weight
					// nonzero
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							0, LIST_ITEM_HEIGHT, .8f);
					tv.setLayoutParams(lp);
					tv.setPadding(10, 5, 10, 5);
					view.addView(tv);
				}
			}

			// Customize this view by updating the various child views to
			// display
			// the particular receipt

			((TextView) listItemView.findViewWithTag("date"))
					.setText(AndroidDate.formatUserDateFromJSDate(r.getDate()));
			((TextView) listItemView.findViewWithTag("cost")).setText(r
					.getCost().toString());
			((TextView) listItemView.findViewWithTag("summary")).setText(r
					.getSummary());

			return listItemView;
		}
	}

	// Be aware that these fields will all be reset if fragment is destroyed!
	private RBuddyApp mApp;
	private ArrayAdapter<Receipt> mReceiptListAdapter;
	private List<Receipt> mReceiptList;
	private ListView mReceiptListView;
	private View mContentView;

}
