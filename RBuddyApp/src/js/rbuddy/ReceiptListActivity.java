package js.rbuddy;

import static js.basic.Tools.*;
import android.view.ViewGroup.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

public class ReceiptListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		warning("Creating a receipt for test purposes");
		new Receipt();
		Intent i = getIntent();
		String msg = i.getStringExtra("message");

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		LayoutParams layoutParam = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);

		{
			TextView tv = new TextView(this);
			tv.setLayoutParams(layoutParam);
			layout.addView(tv);
			tv.setText(msg);
		}
		{
			Button btn = new Button(this);
			btn.setText("Receipt List");
			layout.addView(btn, layoutParam);
			btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					finish();
				}
			});
		}
	}
}
