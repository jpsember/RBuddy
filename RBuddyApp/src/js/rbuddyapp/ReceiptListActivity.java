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

		RBuddyApp.prepare(this);

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
		receiptListAdapter.notifyDataSetChanged();
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

		case R.id.action_search:
			doSearchActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private List buildListOfReceipts() {
		ArrayList list = new ArrayList();
		for (Iterator it = app.receiptFile().iterator(); it.hasNext();)
			list.add(it.next());
		return list;
	}

	// Construct a view to be used for the list items
	private View constructListView() {

		ListView listView = new ListView(this);

		List receiptList = buildListOfReceipts(); // app.receiptList();
		ArrayAdapter arrayAdapter = new ReceiptListAdapter(this, receiptList);
		listView.setAdapter(arrayAdapter);

		// Store references to both the ArrayAdapter and the backing ArrayList,
		// to make responding to selection actions more convenient.
		this.receiptListAdapter = arrayAdapter;
		this.receiptList = receiptList;
		if (db)
			pr("adapter=" + this.receiptListAdapter);

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
		this.receiptList.add(r);

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
		startActivity(intent);
	}

	private ArrayAdapter receiptListAdapter;
	private List receiptList;
	private RBuddyApp app;
}
