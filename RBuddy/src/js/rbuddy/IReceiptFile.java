package js.rbuddy;

import java.util.Iterator;

public interface IReceiptFile {

	/**
	 * Retrieve a receipt, given its identifier
	 * @param uniqueIdentifier
	 * @return receipt
	 * @throws IllegalArgumentException if no such receipt exists
	 */
	public Receipt getReceipt(int uniqueIdentifier);
	
	/**
	 * Flush any changes 
	 */
	public void flush();
	
	/**
	 * Add a receipt
	 * @param r Receipt
	 * @throws IllegalArgumentException if receipt already exists in file
	 */
	public void add(Receipt r);
	
	/**
	 * Delete a receipt
	 * @param r
	 * @throws IllegalArgumentException if no such receipt exists
	 */
	public void delete(Receipt r);
	
	/**
	 * Mark a receipt as being modified, so that it is written when the file is flushed
	 * @param r
	 * @throws IllegalArgumentException if no such receipt exists
	 */
	public void setModified(Receipt r);

	/**
	 * Remove all receipts from file
	 */
	public void clear();

	/**
	 * Get an iterator over the receipts
	 * @return
	 */
	public Iterator iterator();
}
