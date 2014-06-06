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
	public void onSaveInstanceState(Bundle s) {
		app.receiptFile().flush();
		super.onSaveInstanceState(s);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		unimp("add 'new' or '+' button to menu to add new receipt");
		
		RBuddyApp.prepare(this);
		
		app = RBuddyApp.sharedInstance();
		
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
	        case R.id.action_add:
	        	processAddReceipt();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private List buildListOfReceipts() {
		ArrayList list = new ArrayList();
		for (Iterator it = app.receiptFile().iterator(); it.hasNext(); )
			list.add(it.next());
		return list;
	}

	// Construct a view to be used for the list items
	private ListView constructListView() {

		ListView listView = new ListView(this);

		List receiptList = buildListOfReceipts(); //app.receiptList();
		ArrayAdapter arrayAdapter = new ReceiptListAdapter(this, receiptList);
		listView.setAdapter(arrayAdapter);

		// Store references to both the ArrayAdapter and the backing ArrayList,
		// to make responding to selection actions more convenient.
		this.receiptListAdapter = arrayAdapter;
		this.receiptList = receiptList;
		if (db) pr("adapter="+this.receiptListAdapter);
		
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
		Receipt r = new Receipt();
		r.setUniqueIdentifier(app.getUniqueIdentifier());
		app.receiptFile().add(r);
		
		// Start the edit receipt activity
		Intent intent = new Intent(getApplicationContext(),
				EditReceiptActivity.class);
		intent.putExtra(RBuddyApp.EXTRA_RECEIPT_ID, r.getUniqueIdentifier());
		startActivity(intent);
		
		unimp("do we want to delay adding the receipt to the list until the activity returns?");

		this.receiptList.add(r);
		receiptListAdapter.notifyDataSetChanged();
		
}
	
	private void processReceiptSelection(int position) {
		Receipt r = (Receipt) receiptListAdapter.getItem(position);
		pr("Just clicked on view, receipt " + r);
		
		// Start the edit receipt activity
		Intent intent = new Intent(getApplicationContext(),
				EditReceiptActivity.class);
		intent.putExtra(RBuddyApp.EXTRA_RECEIPT_ID, r.getUniqueIdentifier());
		startActivity(intent);
		unimp("refresh Receipt item if it's visible (which we assume it is in this case)");
	}

	private ArrayAdapter receiptListAdapter;
	private List receiptList;
	private RBuddyApp app;
}
