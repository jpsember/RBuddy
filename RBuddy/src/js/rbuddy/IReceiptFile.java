package js.rbuddy;

import java.util.Iterator;

public interface IReceiptFile {

	/**
	 * Retrieve a receipt, given its identifier
	 * 
	 * @param uniqueIdentifier
	 * @return receipt
	 * @throws IllegalArgumentException
	 *             if no such receipt exists
	 */
	public Receipt getReceipt(int id);

	/**
	 * Flush any changes
	 */
	public void flush();

	/**
	 * Add a receipt
	 * 
	 * @param r
	 *            Receipt
	 * @throws IllegalArgumentException
	 *             if receipt already exists in file
	 */
	public void add(Receipt r);

	/**
	 * Delete a receipt
	 * 
	 * @param r
	 * @throws IllegalArgumentException
	 *             if no such receipt exists
	 */
	public void delete(Receipt r);

	/**
	 * Determine if a receipt with a particular id exists
	 * 
	 * @param id
	 * @return true if it exists
	 */
	public boolean exists(int id);

	/**
	 * Mark a receipt as modified, so any changes are guaranteed to be saved
	 * when the file is next flushed. Implementing classes are free to optimize
	 * this process; e.g., they may determine that the receipt has not actually
	 * changed and does not need flushing.
	 * 
	 * @param r
	 * @throws IllegalArgumentException
	 *             if no such receipt exists
	 */
	public void setModified(Receipt r);

	/**
	 * Remove all receipts from file
	 */
	public void clear();

	/**
	 * Get an id guaranteed to be unique from any other in the set
	 * 
	 * @return positive integer
	 */
	public int allocateUniqueId();

	/**
	 * Get an iterator over the receipts
	 * 
	 * @return
	 */
	public Iterator<Receipt> iterator();
}
