package js.rbuddy;

import static js.basic.Tools.*;

import java.util.*;

import android.view.ViewGroup.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ReceiptListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		{
			startApp(); // does nothing if already started
			JSDate.setFactory(AndroidDate.androidDateFactory);
		}
		
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		ListView lv = constructListView();
		layout.addView(lv);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		getMenuInflater().inflate(R.menu.receiptlist_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_settings:
	            unimp("settings");
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private List constructReceiptList() {
		List list = new ArrayList();
		int NUM_RECEIPTS = 50;
		if (db) timeStamp("building receipts");
		for (int i = 0; i < NUM_RECEIPTS; i++) {
			list.add(Receipt.buildRandom());
		}
		if (db) timeStamp("done building");
		return list;
	}

	// Construct a view to be used for the list items
	private ListView constructListView() {

		ListView listView = new ListView(this);

		List receiptList = constructReceiptList();
		ArrayAdapter arrayAdapter = new ReceiptListAdapter(this, receiptList);
		listView.setAdapter(arrayAdapter);

		// Store references to both the ArrayAdapter and the backing ArrayList,
		// to make responding to selection actions more convenient.
		this.receiptListAdapter = arrayAdapter;
		this.receiptList = receiptList;
		if (db) pr("adapter="+this.receiptListAdapter+", list="+this.receiptList);
		
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

	private void processReceiptSelection(int position) {
		Receipt r = (Receipt) receiptListAdapter.getItem(position);
		pr("Just clicked on view, receipt " + r);

		
		// Start the edit receipt activity
		Intent intent = new Intent(getApplicationContext(),
				EditReceiptActivity.class);
		unimp("we need a string that is a unique identifier for a particular receipt to pass in the intent");
		
		intent.putExtra("message", r.getSummary());
		startActivity(intent);
	}

	private ArrayAdapter receiptListAdapter;
	private List receiptList;
}
