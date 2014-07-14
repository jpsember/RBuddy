package com.js.rbuddyapp;

public interface IRBuddyActivityListener {

	/**
	 * Called then the active receipt has changed (use the IRBuddyActivity to
	 * get the new active receipt)
	 */
	void activeReceiptChanged();

	/**
	 * Called when active receipt has been modified within the editor
	 */
	void activeReceiptEdited();
}
