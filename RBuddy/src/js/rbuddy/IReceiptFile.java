package js.rbuddy;

import java.util.Iterator;

public interface IReceiptFile {

	/**
	 * Retrieve a receipt, given its identifier
	 * @param uniqueIdentifier
	 * @return receipt
	 */
	public Receipt getReceipt(int uniqueIdentifier);
	
	/**
	 * Flush any changes 
	 */
	public void flush();
	
	/**
	 * Add a receipt
	 * @param r
	 */
	public void add(Receipt r);
	
	/**
	 * Delete a receipt
	 * @param r
	 */
	public void delete(Receipt r);
	
	/**
	 * Mark a receipt as being modified, so that it is written when the file is flushed
	 * @param r
	 */
	public void setModified(Receipt r);

	/**
	 * Get an iterator over the receipts
	 * @return
	 */
	public Iterator iterator();
}
