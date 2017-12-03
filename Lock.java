/**
 * A class for the Lock object. Stores the ID of the transaction holding this lock, 
 * the lock start time and whether its a read lock.
 */

public class Lock{

	int transactionID;
	int startTime;
	boolean isRead;

	/**
	 * Lock constructor
	 * @param  transactionID the transaction holding this lock
	 * @param  startTime     the time this lock was created
	 * @param  isRead        true if this is a read lock
	 */
	public Lock(int transactionID, int startTime, boolean isRead){
		this.transactionID = transactionID;
		this.startTime = startTime;
		this.isRead = isRead;
	}
}