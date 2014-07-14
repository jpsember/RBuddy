package com.js.rbuddyapp;

public interface IRBuddyActivityListener {

	/**
	 * The active receipt has changed (use the IRBuddyActivity to get the new
	 * active receipt)
	 */
	void activeReceiptChanged();

	/**
	 * The active receipt has been modified within the editor
	 */
	void activeReceiptEdited();

	/**
	 * The receipt file has changed or receipts other than active receipt have
	 * been changed in some way (e.g., the file has been 'zapped' within
	 * development-only code)
	 */
	void receiptFileChanged();
}
