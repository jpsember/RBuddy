package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.js.android.AndroidDate;
import com.js.android.FragmentWrapper;
import com.js.android.PseudoFragment;
import com.js.rbuddy.Receipt;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ReceiptList extends PseudoFragment {

	public static class Wrapper extends FragmentWrapper {
		public Wrapper() {
		}

		@Override
		public Class getFragmentClass() {
			return ReceiptList.class;
		}
	}

	public ReceiptList() {
		if (db) {
			pr(hey() + "constructing ReceiptList " + this
					+ ", setting logging on");
			setLogging(true);
			getActivityState().setLogging(true);
		}

		// Register the wrapper class
		new Wrapper();

		// Perform class-specific initialization
		mApp = RBuddyApp.sharedInstance();
	}

	@Override
	public View onCreateView() {
		log("onCreateView");
		constructListView();
		getActivityState() //
				.add(mReceiptListView) //
				.restoreViewsFromSnapshot();
		log(" returning " + nameOf(mReceiptListView));
		return mReceiptListView;
	}

	// Methods this fragment provides (its non-fragment-related interface)

	public void refreshReceipt(Receipt r) {
		if (mReceiptListAdapter != null)
			mReceiptListAdapter.notifyDataSetChanged();
	}

	public void refreshList() {
		if (mReceiptList == null)
			return;
		log("refreshList");
		rebuildReceiptList(mReceiptList);
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
		Collections.sort(list, Receipt.COMPARATOR_SORT_BY_DATE);

		if (mReceiptListAdapter != null)
			mReceiptListAdapter.notifyDataSetChanged();
	}

	// Construct a view to be used for the list items
	private void constructListView() {
		ListView listView = new ListView(getContext());

		List<Receipt> receiptList = buildListOfReceipts();
		ArrayAdapter arrayAdapter = new ReceiptListAdapter(getContext(),
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
	}

	/**
	 * Process user selecting receipt from receipt list
	 * 
	 * @param position
	 */
	private void processReceiptSelection(int position) {
		Receipt r = mReceiptList.get(position);
		listener().receiptSelected(r);
	}

	private Listener listener() {
		return (Listener) getContext();
	}

	public static interface Listener {
		void receiptSelected(Receipt r);
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

				LinearLayout view = new LinearLayout(this.getContext());
				listItemView = view;
				final int LIST_ITEM_PADDING = 10;
				view.setPadding(LIST_ITEM_PADDING, LIST_ITEM_PADDING,
						LIST_ITEM_PADDING, LIST_ITEM_PADDING);
				view.setOrientation(LinearLayout.HORIZONTAL);

				final int LIST_ITEM_HEIGHT = 80;

				// Construct the various child views contained in this list
				// view.
				// We'll refer to the individual views by tags that are strings
				// indicating their contents ("date","summary").
				// We could instead use the ViewHolder method to speed up
				// finding
				// the child views (see
				// http://developer.android.com/training/improving-layouts/smooth-scrolling.html);
				// but this
				// only saves ~15% according to some estimates, so for
				// simplicity
				// I'm omitting this step.

				{
					TextView tv = new TextView(this.getContext());
					tv.setMinEms(5);
					tv.setTag("date");
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT, LIST_ITEM_HEIGHT, 0.1f);
					tv.setLayoutParams(lp);
					view.addView(tv);
					tv.setPadding(10, 5, 10, 5);
				}
				{
					TextView tv = new TextView(this.getContext());
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
					TextView tv = new TextView(this.getContext());
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

	private ArrayAdapter<Receipt> mReceiptListAdapter;
	private List<Receipt> mReceiptList;
	private RBuddyApp mApp;
	private ListView mReceiptListView;
}
