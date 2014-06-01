package js.rbuddy;

import static js.basic.Tools.*;

import java.util.*;

import android.view.ViewGroup.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Button;

public class ReceiptListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = getIntent();
		String msg = i.getStringExtra("message");

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		{
			Button btn = new Button(this);
			btn.setText("Return to " + msg);
			LayoutParams layoutParam = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layout.addView(btn, layoutParam);
			btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					finish();
				}
			});
		}
		ListView lv = constructListView();
		layout.addView(lv);

	}

	private List constructReceiptList() {
		List list = new ArrayList();
		int NUM_RECEIPTS = 200;
		timeStamp("building receipts");
		for (int i = 0; i < NUM_RECEIPTS; i++) {
			list.add(Receipt.buildRandom());
		}
		timeStamp("done building");
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

		if (position >= 10 && position <= 20) {
			// Try deleting this particular receipt
			receiptList.remove(position);
			receiptListAdapter.notifyDataSetChanged();
		}
	}

	private ArrayAdapter receiptListAdapter;
	private List receiptList;
}
