package com.js.rbuddyapp;

import com.js.rbuddy.Receipt;

/**
 * Methods that RBuddyActivity implements. This defines the methods that the
 * various fragments can call on the RBuddyActivity
 * 
 */
public interface IRBuddyActivity {

	void addListener(IRBuddyActivityListener listener);

	void removeListener(IRBuddyActivityListener listener);

	/**
	 * Get the 'active' receipt
	 * 
	 * TODO: rename to include 'active'
	 * 
	 * @return
	 */
	Receipt getReceipt();

	/**
	 * Request edit of receipt's photo TODO: omit receipt arg, assume active
	 * 
	 * @param r
	 */
	void editPhoto(Receipt r);

	/**
	 * Notify activity that a receipt has been edited (e.g., ReceiptEditor calls
	 * this method to have ReceiptActivity refresh the receipt's appearance in
	 * the ReceiptList fragment)
	 * 
	 * TODO: omit receipt argument, assume 'active' receipt
	 * 
	 * @param r
	 */
	void receiptEdited(Receipt r);

}
