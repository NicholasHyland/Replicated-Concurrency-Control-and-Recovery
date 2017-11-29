import java.util.ArrayList;
import java.util.HashMap;

public class LockTable {

  HashMap<Integer, Integer> writeLocks = new HashMap<Integer, Integer>(); // key is the variable, and the value is the transaction
  HashMap<Integer, ArrayList<Integer>> readLocks = new HashMap<Integer, ArrayList<Integer>>();

  HashMap<Integer, ArrayList<Lock>> lockQueue = new HashMap<Integer, ArrayList<Lock>>();

  public LockTable() {

  }

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

  public void setWriteLock(Operation o) {
    this.writeLocks.put(o.variableID, o.transactionID);
  }

  public void setReadLock(Operation o) {
    ArrayList<Integer> currentReadLocks;
    if (this.readLocks.get(o.variableID) != null) {
      currentReadLocks = readLocks.get(o.variableID);
      currentReadLocks.add(o.transactionID);
    }
    else {
      currentReadLocks = new ArrayList<Integer>();
      currentReadLocks.add(o.transactionID);
    }
    this.readLocks.put(o.variableID, currentReadLocks);
  }

  public void removeWriteLock(Operation o) {
    this.writeLocks.remove(o.variableID);
  }
}