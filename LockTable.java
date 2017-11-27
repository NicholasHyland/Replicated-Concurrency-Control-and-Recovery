import java.util.ArrayList;
import java.util.HashMap;

public class LockTable {

  HashMap<Integer, Integer> writeLocks = new HashMap<Integer, Integer>(); // key is the variable, and the value is the transaction
  HashMap<Integer, ArrayList<Integer>> readLocks = new HashMap<Integer, ArrayList<Integer>>();

  HashMap<Integer, ArrayList<Lock>> writeLockQueue = new HashMap<Integer, ArrayList<Lock>>(); // integer is transaction ID
  HashMap<Integer, ArrayList<Lock>> readLockQueue = new HashMap<Integer, ArrayList<Lock>>();

  public LockTable() {

  }

  public void addWriteLockQueue(int transactionID, int variableID, int time) {
    Lock newLock = new Lock(transactionID, time);
    ArrayList<Lock> lockQueue = this.writeLockQueue.get(variableID);
    lockQueue.add(newLock);
    this.writeLockQueue.put(variableID, lockQueue);
  }

  public void setWriteLock(Operation o) {
    this.writeLocks.put(o.variableID, o.transactionID);
  }

  public void removeWriteLock(Operation o) {
    this.writeLocks.remove(o.variableID);
  }
}