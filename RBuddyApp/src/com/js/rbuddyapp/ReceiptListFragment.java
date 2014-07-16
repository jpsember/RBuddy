package com.js.rbuddyapp;

import static com.js.android.Tools.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.js.android.AndroidDate;
import com.js.android.MyFragment;
import com.js.rbuddy.IReceiptFile;
import com.js.rbuddy.Receipt;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ReceiptListFragment extends MyFragment implements
		IRBuddyActivityListener {

	public ReceiptListFragment() {
		// setLogging(true);
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		log("onCreateView savedState=" + nameOf(savedInstanceState));

		constructViews();

		defineState(mReceiptListView);
		restoreStateFrom(savedInstanceState);

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

	/**
	 * If fragment is resumed, modify view if necessary to display search
	 * results
	 */
	public void updateForSearchResults() {
		if (!isResumed())
			return;

		/*
		 * Rebuild existing listview to reflect current search results (or lack
		 * thereof). We want to use the existing ListView (since it's registered
		 * with the fragment for persistence purposes), but may be adding
		 * additional widgets
		 */
		boolean nowShowingSearch = (getRBuddyActivity().getSearchResults() != null);
		if (nowShowingSearch || mShowingSearchResults)
			placeElementsWithinMainView();
	}

	private List<Receipt> buildListOfReceipts() {
		List<Receipt> list = new ArrayList();
		rebuildReceiptList(list);
		return list;
	}

	private void rebuildReceiptList(List list) {
		log("rebuildReceiptList");
		list.clear();

		IReceiptFile receiptFile = getRBuddyActivity().receiptFile();
		int[] searchResults = getRBuddyActivity().getSearchResults();
		if (searchResults != null) {
			for (int id : searchResults) {
				if (!receiptFile.exists(id))
					continue;
				list.add(receiptFile.getReceipt(id));
			}
		} else {
			Iterator<Receipt> iterator = receiptFile.iterator();
			while (iterator.hasNext()) {
				list.add(iterator.next());
			}
		}

		Collections.sort(list, Receipt.COMPARATOR_SORT_BY_DATE);

		if (mReceiptListAdapter != null)
			mReceiptListAdapter.notifyDataSetChanged();
	}

	private void populateReceiptListView() {
		mReceiptList = buildListOfReceipts();
		mReceiptListAdapter = new ReceiptListAdapter(getActivity(),
				mReceiptList);
		mReceiptListView.setAdapter(mReceiptListAdapter);
	}

	// Construct a view to be used for the list items
	private void constructViews() {
		mReceiptListView = new ListView(getActivity());
		mReceiptListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView aView, View v,
							int position, long id) {
						processReceiptSelection(position);
					}
				});

		// place the receipt list view within a container that we can add
		// additional items to dynamically
		mContentView = new LinearLayout(getActivity());
		// Specify how this container is to be laid out in ITS container
		mContentView.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		mContentView.setOrientation(LinearLayout.VERTICAL);

		placeElementsWithinMainView();
	}

	private void hideSearchResults() {
		getRBuddyActivity().clearSearchResults();
		updateForSearchResults();
	}

	private void placeElementsWithinMainView() {
		mShowingSearchResults = (getRBuddyActivity().getSearchResults() != null);

		mContentView.removeAllViews();
		if (mShowingSearchResults) {
			// TODO: Issue #14; avoid hard-coded numbers, colors
			LinearLayout panel = new LinearLayout(getActivity());
			{
				panel.setOrientation(LinearLayout.HORIZONTAL);
				mContentView.addView(panel,
						new LinearLayout.LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT, 0));
			}

			{
				TextView t = new TextView(panel.getContext());
				t.setTextSize(20);
				t.setText("Search Results");
				t.setGravity(Gravity.CENTER_VERTICAL);
				panel.addView(t,
						new LinearLayout.LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.MATCH_PARENT, 1));
			}
			{
				Button b = new Button(panel.getContext());
				b.setText("Done");
				panel.addView(b,
						new LinearLayout.LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.MATCH_PARENT, 0));
				b.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						hideSearchResults();
					}
				});
			}
		}

		mContentView.addView(mReceiptListView, new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));

		mReceiptListView.setBackgroundColor(Color
				.parseColor(mShowingSearchResults ? "#303030" : "#205020"));

		populateReceiptListView();
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
	private ArrayAdapter<Receipt> mReceiptListAdapter;
	private List<Receipt> mReceiptList;
	private ListView mReceiptListView;
	private LinearLayout mContentView;
	private boolean mShowingSearchResults;
}
