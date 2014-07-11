package com.js.rbuddyapp;

import static com.js.android.Tools.*;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;

import com.js.android.FragmentOrganizer;
import com.js.android.MyFragment;
import com.js.android.PseudoFragment;
import com.js.form.Form;
import com.js.form.FormButtonWidget;
import com.js.json.JSONEncoder;
import com.js.rbuddy.Cost;
import com.js.rbuddy.JSDate;
import com.js.rbuddy.R;
import com.js.rbuddy.Receipt;
import com.js.rbuddy.TagSet;

/**
 * Singleton receipt editor
 * 
 */
public class ReceiptEditor extends PseudoFragment {

	public static class Wrapper extends MyFragment {
		public Wrapper() {
		}

		@Override
		public Class getFragmentClass() {
			return ReceiptEditor.class;
		}
	}

	public ReceiptEditor(FragmentOrganizer fragments) {
		super(fragments);
		// final boolean db = true;
		if (db) {
			pr(hey() + "setting logging true");
			setLogging(true);
		}
		new Wrapper().register(fragments);
		mApp = RBuddyApp.sharedInstance();
	}

	@Override
	public void onRestoreInstanceState(Bundle bundle) {
		super.onRestoreInstanceState(bundle);
		setReceipt(listener().getReceipt());
	}

	private static int ZZZ = 500;

	@Override
	public View onCreateView(MyFragment container) {
		log("onCreateView container=" + nameOf(container));


		{
			ASSERT(mScrollView == null);
			mScrollView = new ScrollView(getContext());
			mScrollView.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			String message = "" + (ZZZ++) + "!" + nameOf(this) + " "
					+ nameOf(this.getFragments()) + " " + nameOf(container);
			warning("set msg: " + message);
			mScrollViewContainer = wrapView(mScrollView, message);
		}

		getActivityState() //
				.add(mScrollView) //
				.restoreViewsFromSnapshot();
		log(" returning scrollView " + nameOf(mScrollView));
		return mScrollViewContainer;
	}

	@Override
	public void onResume() {
		ASSERT(mScrollView != null);
		super.onResume();
		setReceipt(listener().getReceipt());
	}

	@Override
	public void onPause() {
		super.onPause();
		writeReceiptFromWidgets();
		mApp.receiptFile().flush();
	}

	@Override
	public void onDestroyView() {
		disposeForm();
		mScrollView = null;
		mScrollViewContainer = null;
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getActivityState().persistSnapshot(outState);
	}

	public void setReceipt(Receipt receipt) {
		// Somehow calls to setReceipt() are being sent to old fragments, one
		// with old organizers...?

		final boolean db = true;
		if (db)
			pr(hey() + receipt + "\n" + stackTrace(0, 8));
		// In case there's an existing receipt, flush its changes
		writeReceiptFromWidgets();

		this.mReceipt = receipt;

		constructForm();
		readReceiptToWidgets();
	}

	private void disposeForm() {
		if (mForm != null) {
			stopFormListeners();
			mScrollView.removeView(mForm.getView());
			mForm = null;
		}
	}

	private void stopFormListeners() {
		if (mReceiptWidget == null)
			return;
		// Make widget display nothing, so it stops listening; otherwise
		// the widget will leak
		mReceiptWidget.displayPhoto(mApp.photoStore(), 0, null);
	}

	/**
	 * Construct the form if it doesn't exist, receipt exists, and its container
	 * exists
	 */
	private void constructForm() {
		// TODO: in PsuedoFragment class, keep track of number of fragments
		// paused/resumed/etc
		// (for development purposes)

		log("constructForm, isResumed=" + isResumed() + " hasReceipt="
				+ hasReceipt() + " mForm=" + nameOf(mForm));

		if (mScrollView != null && !isResumed()) {
			warning("mScrollView is nonnull, yet isResumed is false!");
		}

		if (!isResumed())
			return;

		if (!hasReceipt()) {
			disposeForm();
			return;
		}

		// If we already have a form, done
		if (mForm != null)
			return;

		String jsonString = readTextFileResource(getContext(),
				R.raw.form_edit_receipt);

		mForm = mApp.parseForm(getContext(), jsonString);

		mForm.addListener(new Form.Listener() {
			@Override
			public void valuesChanged(Form form) {
				writeReceiptFromWidgets();
			}
		});

		mReceiptWidget = (FormButtonWidget) mForm.getField("receipt");
		mReceiptWidget.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				processPhotoButtonPress();
			}
		});
		log("adding form " + nameOf(mForm) + " to scrollView "
				+ nameOf(mScrollView));
		mScrollView.addView(mForm.getView(), new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}

	private void processPhotoButtonPress() {
		if (mReceipt == null)
			return;
		listener().editPhoto(mReceipt);
	}

	private void readReceiptToWidgets() {
		if (db)
			pr(hey() + "isRes=" + isResumed() + " hasRec=" + hasReceipt());

		if (!isResumed() || !hasReceipt())
			return;

		mForm.setValue("summary", mReceipt.getSummary(), false);
		mForm.setValue("cost", mReceipt.getCost(), false);
		mForm.setValue("date", mReceipt.getDate(), false);
		mForm.setValue("tags", mReceipt.getTags(), false);
		mReceiptWidget.displayPhoto(mApp.photoStore(), mReceipt.getId(),
				mReceipt.getPhotoId());
	}

	/**
	 * Determine if this fragment is displaying a receipt, or an empty view (if
	 * no receipt has been set)
	 * 
	 * @return
	 */
	private boolean hasReceipt() {
		return mReceipt != null;
	}

	private void writeReceiptFromWidgets() {
		if (db)
			pr(hey() + "isResumed " + isResumed() + " hasReceipt "
					+ hasReceipt() + " mForm=" + nameOf(mForm));
		if (!isResumed() || !hasReceipt())
			return;

		// If we haven't finished onResume(), mForm may not exist
		if (mForm == null)
			return;

		// To detect if changes have actually occurred, compare JSON
		// representations of the receipt before and after updating the fields.
		String origJSON = JSONEncoder.toJSON(mReceipt);
		String origTagSetString = JSONEncoder.toJSON(mReceipt.getTags());

		mReceipt.setSummary(mForm.getValue("summary"));
		mReceipt.setCost(new Cost(mForm.getValue("cost"), true));
		mReceipt.setDate(JSDate.parse(mForm.getValue("date"), true));
		mReceipt.setTags(TagSet.parse(mForm.getValue("tags"), new TagSet()));

		String newJSON = JSONEncoder.toJSON(mReceipt);

		if (!origJSON.equals(newJSON)) {
			mApp.receiptFile().setModified(mReceipt);

			String newTagSetString = JSONEncoder.toJSON(mReceipt.getTags());
			if (!origTagSetString.equals(newTagSetString)) {
				mReceipt.getTags().moveTagsToFrontOfQueue(mApp.tagSetFile());
			}

			listener().receiptEdited(mReceipt);
		}
	}

	/**
	 * Get listener by casting parent activity
	 * 
	 * @return
	 */
	private Listener listener() {
		return (Listener) getContext();
	}

	public static interface Listener {
		/**
		 * Get the receipt to be edited
		 * 
		 * @return receipt, or null if user hasn't specified one
		 */
		Receipt getReceipt();

		/**
		 * Notify editor that user has selected a new receipt
		 * 
		 * @param r
		 *            receipt, or null if user hasn't specified one
		 */
		void receiptEdited(Receipt r);

		/**
		 * Request edit of receipt's photo
		 * 
		 * @param r
		 */
		void editPhoto(Receipt r);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(nameOf(this));
		sb.append("----------------------------ReceiptEditor;");
		sb.append(" fragorg=" + this.getFragments());
		return sb.toString();
	}

	private RBuddyApp mApp;
	private Receipt mReceipt;
	private Form mForm;
	private FormButtonWidget mReceiptWidget;
	private ScrollView mScrollView;
	private View mScrollViewContainer;

}
