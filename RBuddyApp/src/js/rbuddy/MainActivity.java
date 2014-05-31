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

public class MainActivity extends Activity {

	private void constructView() {

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams linLayoutParam = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		// set this layout as the root element of the activity
		setContentView(layout, linLayoutParam);

		{
			LayoutParams layoutParam = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

			{
				TextView tv = new TextView(this);
				this.textView = tv;
				tv.setLayoutParams(layoutParam);
				layout.addView(tv);
				textView.setText(getDrinkOrderString());
			}

			{
				Button btn = new Button(this);

				btn.setText("Change Drink Order");
				layout.addView(btn, layoutParam);
				btn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						MainActivity a = (MainActivity) v.getContext();
						a.updateDrinkOrder();
					}
				});
			}
			{
				Button btn = new Button(this);
				btn.setText("Show List Activity");
				layout.addView(btn, layoutParam);
				btn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						// Start the receipt list activity, and pass a message
						// to it
						Intent intent = new Intent(getApplicationContext(),
								ReceiptListActivity.class);
						intent.putExtra("message",
								"Drink Menu (" + textView.getText() + ")");
						startActivity(intent);
					}
				});
			}

		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		constructView();

		if (savedInstanceState == null) {
			startApp();
			JSDate.setFactory(AndroidDate.androidDateFactory);
		}
	}

	private static final String[] drinks = {//
	"---no drink selected---", //
			"Double short Americano", //
			"Frappucino", //
			"Drip Coffee", //
	};

	private String getDrinkOrderString() {
		return drinks[drinkNumber];
	}

	private void updateDrinkOrder() {
		drinkNumber = (drinkNumber + 1) % drinks.length;
		textView.setText(getDrinkOrderString());
	}

	private int drinkNumber;
	private TextView textView;
}
