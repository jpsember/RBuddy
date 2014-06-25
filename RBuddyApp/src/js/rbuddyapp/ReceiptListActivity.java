package js.rbuddyapp;

import static js.basic.Tools.*;

import java.util.*;

import js.rbuddy.R;
import js.rbuddy.Receipt;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ReceiptListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = RBuddyApp.sharedInstance();

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		layout.addView(constructListView());
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!RBuddyApp.isReceiptListValid()) {
			rebuildReceiptList(this.receiptList);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		app.receiptFile().flush();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.receiptlist_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			unimp("settings");
			return true;
		case R.id.action_add:
			processAddReceipt();
			return true;
		case R.id.action_testonly_generate:
			processGenerate();
			return true;
		case R.id.action_testonly_zap:
			processZap();
			return true;
		case R.id.action_search:
			doSearchActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (app.useGoogleAPI()) {
			menu.removeItem(R.id.action_testonly_generate);
			menu.removeItem(R.id.action_testonly_zap);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	private void processGenerate() {
		for (int i = 0; i < 30; i++) {
			int id = app.receiptFile().allocateUniqueId();
			Receipt r = Receipt.buildRandom(id);
			app.receiptFile().add(r);
		}
		rebuildReceiptList(receiptList);
		receiptListAdapter.notifyDataSetChanged();
	}

	private void processZap() {
		if (!app.useGoogleAPI())
			RBuddyApp.confirmOperation(this, "Delete all receipts?",
					new Runnable() {
						@Override
						public void run() {
							app.receiptFile().clear();
							rebuildReceiptList(receiptList);
						}
					});
	}

	private List buildListOfReceipts() {
		ArrayList list = new ArrayList();
		rebuildReceiptList(list);
		return list;
	}

	private void rebuildReceiptList(List list) {
		list.clear();
		for (Iterator it = app.receiptFile().iterator(); it.hasNext();)
			list.add(it.next());
		Collections.sort(list, Receipt.COMPARATOR_SORT_BY_DATE);
		RBuddyApp.setReceiptListValid(true);

		if (receiptListAdapter != null)
			receiptListAdapter.notifyDataSetChanged();
	}

	// Construct a view to be used for the list items
	private View constructListView() {

		ListView listView = new ListView(this);

		List receiptList = buildListOfReceipts();
		ArrayAdapter arrayAdapter = new ReceiptListAdapter(this, receiptList);
		listView.setAdapter(arrayAdapter);

		// Store references to both the ArrayAdapter and the backing ArrayList,
		// to make responding to selection actions more convenient.
		this.receiptListAdapter = arrayAdapter;
		this.receiptList = receiptList;

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView aView, View v, int position,
					long id) {
				processReceiptSelection(position);
			}
		});
		LayoutParams layoutParam = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		listView.setLayoutParams(layoutParam);

		return listView;
	}

	private void processAddReceipt() {
		Receipt r = new Receipt(app.receiptFile().allocateUniqueId());
		app.receiptFile().add(r);
		RBuddyApp.setReceiptListValid(false);

		// Start the edit receipt activity
		Intent intent = new Intent(getApplicationContext(),
				EditReceiptActivity.class);
		intent.putExtra(RBuddyApp.EXTRA_RECEIPT_ID, r.getId());
		startActivity(intent);
	}

	private void processReceiptSelection(int position) {
		Receipt r = (Receipt) receiptListAdapter.getItem(position);
		Intent intent = new Intent(getApplicationContext(),
				EditReceiptActivity.class);
		intent.putExtra(RBuddyApp.EXTRA_RECEIPT_ID, r.getId());
		startActivity(intent);
	}

	private void doSearchActivity() {
		Intent intent = new Intent(getApplicationContext(),
				SearchActivity.class);
		if (false) {
			warning("trying out experimental activity instead");
			intent = new Intent(getApplicationContext(),
					ExperimentalActivity.class);
		}

		startActivity(intent);
	}

	private ArrayAdapter receiptListAdapter;
	private List receiptList;
	private RBuddyApp app;
}
