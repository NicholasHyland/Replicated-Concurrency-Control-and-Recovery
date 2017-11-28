// This is a class to model the Transaction Manager
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

public class TransactionManager {

	ArrayList<Operation> operations;
	ArrayList<Operation> blockedOperations = new ArrayList<Operation>();
	HashMap<Integer, Transaction> transactions = new HashMap<Integer, Transaction>();
	// ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	ArrayList<Variable> variables;
	ArrayList<Site> sites;
	public static int runningTransactions, currentTime;
	HashMap<Integer, ArrayList<Integer>> graph = new HashMap<Integer, ArrayList<Integer>>(); // key is transaction ID, value is list of pointers to other transactions
	DataManager DM = new DataManager();

	public TransactionManager(ArrayList<Operation> operations){
		this.operations = operations;
		this.runningTransactions = 0;
		this.currentTime = 0;
		initializeSites();
	}

	//initializes all 10 sites
	public void initializeSites(){
		ArrayList<Site> allSites = new ArrayList<Site>();
  		for(int i = 1; i < 11; i++){
  			allSites.add(new Site(i));
  		}
  		this.sites = allSites;
	}

	public static ArrayList<Integer> detectCycleStart(HashMap<Integer, ArrayList<Integer>> graph) {
    ArrayList<Integer> cycle = new ArrayList<Integer>();
    Iterator iterator = graph.entrySet().iterator();

    boolean hasCycle = false;
    while (iterator.hasNext()) {
      Map.Entry pair = (Map.Entry)iterator.next();
      ArrayList<Integer> cycleCheck = detectCycle(graph, cycle, (Integer)pair.getKey());

      if (cycleCheck.size() > 1) {
        if (cycleCheck.get(0) == cycleCheck.get(cycleCheck.size() - 1)) {
          hasCycle = true;
          cycle = cycleCheck;
          break;
        }
      }
    }

    if (hasCycle) {
      return cycle;
    }
    else {
      return new ArrayList<Integer>();
    }
  }

  public static ArrayList<Integer> detectCycle(HashMap<Integer, ArrayList<Integer>> graph, ArrayList<Integer> cycle, int currentNode) {
    // not a cycle if it contains -1
    if (cycle.contains(-1)) {
      return cycle;
    }
    // the first node is the current node - there is a cycle
    if (cycle.size() > 1) {
      if (cycle.get(0) == cycle.get(cycle.size() - 1)) {
        return cycle;
      }
    }
    // if the cycle contains the current node, but it is not the first node - return the subcycle of the cycle
    if (cycle.contains(currentNode)) {
      // System.out.println("cycle " + cycle);
      cycle.add(currentNode);
      int start = 0;
      for (int i = 0; i < cycle.size(); i++) {
        if (cycle.get(i) == currentNode) {
          start = i;
          break;
        }
      }
      ArrayList<Integer> newCycle = new ArrayList(cycle.subList(start, cycle.size()));
      return newCycle;
    }
    // the current node is not a key in the graph -  no more out edges - not a cycle, but still return current cycle
    if (!graph.containsKey(currentNode)) {
      cycle.add(-1);
      return cycle;
    }
    else {
    	cycle.add(currentNode);
      ArrayList<Integer> edges = graph.get(currentNode);
      for (Integer edge : edges) {
        ArrayList<Integer> checkCycle = detectCycle(graph, cycle, edge);
        if (checkCycle.size() > 1) {
          if (checkCycle.get(0) == checkCycle.get(checkCycle.size() - 1)) {
            return checkCycle;
          }
        }
      }
      cycle.add(-1);
      return cycle;
    }
  }

	public void simulate(){
		//Operation current;
		while (this.operations.size() > 0 || this.blockedOperations.size() > 0) {
			// process blocked Operations first
			boolean leaveQueue = false;
			while (this.blockedOperations.size() > 0 && !leaveQueue) {
				boolean removeIndex = false;
				int index = 0;
				for (int i = 0; i < this.blockedOperations.size(); i++) {
					Operation currentOperation = this.blockedOperations.get(i);
					if (currentOperation.operationType.equals("R")) {
						boolean canRead = read(currentOperation);
						if (canRead) {
							// Can read - remove from list, add time, start beginning of list again
							index = i;
							this.currentTime++;
							removeIndex = true;
							break;
						}
						else {
							// if not the last operation in queue, continue to the next
							// if it is the last operation then set leaveQueue to true
							if (i == this.blockedOperations.size() - 1) {
								leaveQueue = true;
							}
						}
					}
					else {
						boolean canWrite = write(currentOperation);
						if (canWrite) {
							// Can read - remove from list, add time, start beginning of list again
							index = i;
							this.currentTime++;
							removeIndex = true;
							break;
						}
						else {
							// if not the last operation in queue, continue to the next
							// if it is the last operation then set leaveQueue to true
							if (i == this.blockedOperations.size() - 1) {
								leaveQueue = true;
							}
						}
					}
				}
				if (removeIndex) {
					this.blockedOperations.remove(index);
				}
			}

			// Process operations in first list
			Operation currentOperation = this.operations.get(0);
			currentOperation.printOperation(); // print operation
			switch(currentOperation.operationType) {
				case "begin":
					beginTransaction(currentOperation, false);
					break;
				case "beginRO":
					beginTransaction(currentOperation, true);
					break;
				case "end":
					endTransaction(currentOperation);
					break;
				case "W":
					boolean canWrite = write(currentOperation);
					// if cannot write, put into blocked queue
					// if true, continue, if false, put into blocked queue
					if (!canWrite) {
						this.blockedOperations.add(currentOperation);
					}
					else {
						currentOperation.setTime(this.currentTime);
					}
					detectDeadlock(); // detect deadlock
					break;
				case "R":
					boolean canRead = read(currentOperation);
					if (!canRead) {
						this.blockedOperations.add(currentOperation);
					}
					detectDeadlock(); // detect deadlock
					break;
				case "dump":
					dump(currentOperation);
					break;
				case "fail":
					failSite(currentOperation.failSite);
					break;
				case "recover":
					recoverSite(currentOperation.recoverSite);
					break;
			}
			// remove current operation from list of operations
			this.operations.remove(0);
			// increment time
			this.currentTime++;
		}
	}

	public void addGraphConflicts(Operation o, int siteID) {

		// TODO - if operation transaction already has a lock on it's variable e.g. T1(W, x1) ..... T1(R, x1)

		int transactionID = o.transactionID;
		int variableID = o.variableID;

		// if writeLocks.containsKey, add pointer from o.transactionID --> writeLocks.get(o.variableID)
		if (this.sites.get(siteID).lockTable.writeLocks.containsKey(variableID)) {
			int conflictingTransaction = this.sites.get(siteID).lockTable.writeLocks.get(variableID);
			if (this.graph.containsKey(transactionID)) {
				ArrayList<Integer> edges = this.graph.get(transactionID);
				if (!edges.contains(conflictingTransaction)) {
					edges.add(conflictingTransaction);
					this.graph.put(transactionID, edges);
				}
			}
			else {
				ArrayList<Integer> edge = new ArrayList<Integer>();
				edge.add(conflictingTransaction);
				this.graph.put(transactionID, edge);
			}
		}

		// if readLocks.containsKey, add pointer from o.transactionID --> to each transactionID in readLocks.get(o.variableID)
		if (this.sites.get(siteID).lockTable.readLocks.containsKey(variableID)) {
			ArrayList<Integer> conflictingTransactions = this.sites.get(siteID).lockTable.readLocks.get(variableID);
			for (Integer conflictingTransaction : conflictingTransactions) {
				if (this.graph.containsKey(transactionID)) {
					ArrayList<Integer> edges = this.graph.get(transactionID);
					if (!edges.contains(conflictingTransaction)) {
						edges.add(conflictingTransaction);
						this.graph.put(transactionID, edges);
					}
				}
				else {
					ArrayList<Integer> edge = new ArrayList<Integer>();
					edge.add(conflictingTransaction);
					this.graph.put(transactionID, edge);
				}
			}
		}

		// if LockQueue.containsKey, add pointer from o.transactionID --> to each transactionID of lock in LockQueue.get(o.variableID)
		if (this.sites.get(siteID).lockTable.lockQueue.containsKey(variableID)) {
			ArrayList<Lock> conflictingLocks = this.sites.get(siteID).lockTable.lockQueue.get(variableID);
			for (Lock conflictingLock : conflictingLocks) {
				int conflictingTransaction = conflictingLock.transactionID;
				if (this.graph.containsKey(transactionID)) {
					ArrayList<Integer> edges = this.graph.get(transactionID);
					if (!edges.contains(conflictingTransaction)) {
						edges.add(conflictingTransaction);
						this.graph.put(transactionID, edges);
					}
				}
				else {
					ArrayList<Integer> edge = new ArrayList<Integer>();
					edge.add(conflictingTransaction);
					this.graph.put(transactionID, edge);
				}
			}
		}
	}

	public void detectDeadlock() {
		ArrayList<Integer> cycle = detectCycleStart(this.graph);
		if (cycle.size() == 0) {
			return;
		}
		else {
			int youngest = cycle.get(0);
			for (int i = 1; i < cycle.size() - 1; i++) {
				int currentTransaction = cycle.get(i);
				if (this.transactions.get(youngest).startTime > this.transactions.get(currentTransaction).startTime) {
					youngest = currentTransaction;
				}
			}
			abortTransaction(youngest);
		}
	}

	public void abortTransaction(int transactionID) {
		return;
	}

	public void beginTransaction(Operation operation, boolean isReadOnly){
		if (isReadOnly){
			this.transactions.put(operation.transactionID, new Transaction(operation.transactionName, true, this.currentTime));
		}
		else {
			this.transactions.put(operation.transactionID, new Transaction(operation.transactionName, false, this.currentTime));
		}
		operation.printOperation();
		this.runningTransactions++;
	}

	public void endTransaction(Operation o){
		this.runningTransactions--;
		System.out.println("Ending transaction " + o.transactionID);
		//return if transaction is read only -- no need to commit values or check for site failure
		if (this.transactions.get(o.transactionID).isReadOnly) {
			return;
		}

		ArrayList<Operation> ops = this.transactions.get(o.transactionID).writeOperations;
		for (Operation op: ops) {
			if (op.variableID%2 == 1) {
				Site site = this.sites.get(op.variableID);
				this.sites.get(op.variableID).lockTable.removeWriteLock(op); // remove the write lock from that site
				if (site.wasDown) {
					int t = site.wasDownTime;
					if (op.time>t){
						//abortTransaction(op.transactionID); - REMOVE LOCKS
						System.out.println("Transaction " + op.transactionID + " is aborted at time " + this.currentTime);
					}
				}
				else {
					this.sites.get(op.variableID).update(op);
					System.out.println("Transaction " + op.transactionID + " committed at time " + this.currentTime);
				}
			}
			else {
				boolean siteDown = false;
				for (Site site: this.sites) {
					site.lockTable.removeWriteLock(op); // remove the write lock from that site
					if (site.wasDown) {
						int t = site.wasDownTime;
						if (op.time>t){
							//abortTransaction(op.transactionID); - REMOVE LOCKS
							System.out.println("Transaction " + op.transactionID + " is aborted at time " + this.currentTime);
							siteDown=true;
							break;
						}
					}
				}
				if (!siteDown) {
					for (Site site: this.sites) {
						site.update(op);
					}
					System.out.println("Transaction " + op.transactionID + " committed at time " + this.currentTime);
				}
			}
		}
	}

	// it is going to write or not write based on whether or not it has the correct locks
	public boolean write(Operation o){
		// odd variable - one site
		if (o.variableID % 2 != 0) {
			int siteIndex = (o.variableID % 10);
			Site currentSite = this.sites.get(siteIndex);
			if (currentSite.isDown) {
				System.out.println("Cannot write to variable x" + o.variableID + " because site " + siteIndex + 1 + " is down");
				return false;
			}
			else {
				if (currentSite.lockTable.writeLocks.containsKey(o.variableID) || currentSite.lockTable.readLocks.containsKey(o.variableID)) {
					addGraphConflicts(o, siteIndex);
					this.sites.get(siteIndex).lockTable.addLockQueue(o.transactionID, o.variableID, this.currentTime, false);
					return false;
				}
				else {
					this.sites.get(siteIndex).lockTable.setWriteLock(o); // sets the write lock
					this.transactions.get(o.transactionID).writeOperations.add(o);
					return true;
				}
			}
		}
		// even variable - all sites
		else {
			boolean siteLocked = false;
			for (int i=0; i<this.sites.size(); i++) {
				Site currentSite = this.sites.get(i);
				if (currentSite.isDown) {
					System.out.println("Cannot write to variable x" + o.variableID + " because site " + i + 1 + " is down");
					continue;
					//return false;
				}
				else {
					if (currentSite.lockTable.writeLocks.containsKey(o.variableID) || currentSite.lockTable.readLocks.containsKey(o.variableID)) {
						addGraphConflicts(o, i);
						this.sites.get(i).lockTable.addLockQueue(o.transactionID, o.variableID, this.currentTime, false);
						siteLocked=true;
					}
				}
			}
			if (!siteLocked) {
				for (Site s : this.sites) {
					s.lockTable.setWriteLock(o);
				}
				this.transactions.get(o.transactionID).writeOperations.add(o);
				return true;
			}
			else {
				return false;
			}
		}
		// write stuff if it can - return true
		// cannot write because it doesn't have locks - return false
	}

	public boolean read(Operation o){
		// R(T1,x3);

		// if (o.variableID % 2 != 0) {
		// 	int siteIndex = (o.variableID % 10);
		// 	Site currentSite = this.sites.get(siteIndex);
		// 	if (currentSite.isDown) {
		// 		System.out.println("Cannot read variable x" + o.variableID + " because site " + siteIndex + 1 + " is down");
		// 		return false;
		// 	}
		// 	else {
		// 		if (currentSite.lockTable.readLocks.containsKey(o.variableID)) {

		// 		}
		// 	}
		// }
		return true;
	}

	public void dump(Operation o){

	}

	public void failSite(int s){
		//TODO keep track of time when site failed
		this.sites.get(s - 1).fail(); // fail this site - clear the lock table
	}

	public void recoverSite(int s){

	}
}
