import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class for the lock table object. Keeps track of the current write lock, read locks and lock queue per variable.
 */
public class LockTable {

  HashMap<Integer, Integer> writeLocks = new HashMap<Integer, Integer>(); // key is the variable, and the value is the transaction
  HashMap<Integer, ArrayList<Integer>> readLocks = new HashMap<Integer, ArrayList<Integer>>();

  HashMap<Integer, ArrayList<Lock>> lockQueue = new HashMap<Integer, ArrayList<Lock>>();

  /** Constructor */
  public LockTable() {

  }

  /**
   * Adds a lock to the lock queue.
   * @param transactionID The transaction requesting the lock
   * @param variableID    The variable this lock is on
   * @param time          The time this lock is requested
   * @param isRead        True is this is a read lock
   */
  public void addLockQueue(int transactionID, int variableID, int time, boolean isRead) {
    Lock newLock = new Lock(transactionID, time, isRead);
    ArrayList<Lock> newLockQueue;
    if (this.lockQueue.get(variableID) != null) {
      newLockQueue = this.lockQueue.get(variableID);
      newLockQueue.add(newLock);
    }
    else {
      newLockQueue = new ArrayList<Lock>();
      newLockQueue.add(newLock);
    }
    this.lockQueue.put(variableID, newLockQueue);
  }

  /**
   * Sets a write lock on a variable
   * @param o The operation requesting this lock
   */
  public void setWriteLock(Operation o) {
    this.writeLocks.put(o.variableID, o.transactionID);
  }

  /**
   * Add a read lock to the list of read locks on the variable
   * @param o The operation requesting the read lock
   */
  public void setReadLock(Operation o) {
    ArrayList<Integer> currentReadLocks;
    if (this.readLocks.get(o.variableID) != null) {
      currentReadLocks = this.readLocks.get(o.variableID);
      currentReadLocks.add(o.transactionID);
    }
    else {
      currentReadLocks = new ArrayList<Integer>();
      currentReadLocks.add(o.transactionID);
    }
    this.readLocks.put(o.variableID, currentReadLocks);
  }
}