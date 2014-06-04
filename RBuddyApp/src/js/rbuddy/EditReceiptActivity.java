package js.rbuddy;

import static js.basic.Tools.*;

import java.util.*;

import android.view.ViewGroup.LayoutParams;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.RelativeLayout;

public class EditReceiptActivity extends Activity {

	private void layoutElements() {
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		{
			Button btn = new Button(this);
			btn.setText("Teri");
			LayoutParams layoutParam = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layout.addView(btn, layoutParam);
		}
		
		if (true) {
		{
//			RelativeLayout photoView = new RelativeLayout(this);
//			{
//			 RelativeLayout.LayoutParams params = 
//		    		   new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//			 params.addRule(RelativeLayout.CENTER_IN_PARENT);
//		       params.setMargins(10,10,10,10);
//		       photoView.setLayoutParams(params);
//			}
		       ImageView bitmapView = new ImageView(this);
		       bitmapView.setImageDrawable(getResources().getDrawable(R.drawable.missingphoto));
			// Give photo a fixed size that is small, but lots of weight to grow to take up what extra there is
			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 30, 1.0f);
		       layout.addView(bitmapView,p);
		   	
		}
		} else {
//			RelativeLayout photoView = new RelativeLayout(this);
//			{
//			 RelativeLayout.LayoutParams params = 
//		    		   new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//			 params.addRule(RelativeLayout.CENTER_IN_PARENT);
//		       params.setMargins(10,10,10,10);
//		       photoView.setLayoutParams(params);
//			}
//			{
//		       ImageView bitmapView = new ImageView(this);
//		   		LayoutParams p = new LayoutParams(
//					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//		       bitmapView.setImageDrawable(getResources().getDrawable(R.drawable.missingphoto));
//		       photoView.addView(bitmapView, p);
//			}
//			
//			// Give photo a fixed size that is small, but lots of weight to grow to take up what extra there is
//			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 30, 1.0f);
//
//		       layout.addView(photoView,p);
		}
		
		{
			Button btn = new Button(this);
			btn.setText("Hatcher");
			LayoutParams layoutParam = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layout.addView(btn, layoutParam);
		}
		
		{
			Button btn = new Button(this);
			btn.setText("Yum");
			LayoutParams layoutParam = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layout.addView(btn, layoutParam);
		}
		

//		{
//			Button btn = new Button(this);
//			btn.setText("Return to " + msg);
//			LayoutParams layoutParam = new LayoutParams(
//					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//			layout.addView(btn, layoutParam);
//			btn.setOnClickListener(new View.OnClickListener() {
//				public void onClick(View v) {
//					finish();
//				}
//			});
//		}

		unimp("how do we have the 'up' button do 'back'?  Or is this automatic?");
//		// If we want the 'up' button to appear to go back to the main activity, we do this:
//	    ActionBar actionBar = getActionBar();
//	    actionBar.setDisplayHomeAsUpEnabled(true);

			}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		unimp("get receipt from intent");
//		Intent i = getIntent();
//		String msg = i.getStringExtra("message");
		layoutElements();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		getMenuInflater().inflate(R.menu.editreceipt_activity_actions, menu);
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


}
