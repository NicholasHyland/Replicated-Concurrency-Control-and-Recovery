import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

/**
 * The Transaction Manager object.
 * Each TM keeps track of all 10 sites and processes operations from the input file on those sites.
 * Implements available copies approach, strict two-phase locking, multiversion concurrency control,
 * deadlock detection, replication, and failure recovery.
 */

public class TransactionManager {

	ArrayList<Operation> operations;
	ArrayList<Operation> blockedOperations = new ArrayList<Operation>();
	HashMap<Integer, Transaction> transactions = new HashMap<Integer, Transaction>();
	ArrayList<Site> sites;
	public static int runningTransactions, currentTime;
	HashMap<Integer, ArrayList<Integer>> graph = new HashMap<Integer, ArrayList<Integer>>(); // key is transaction ID, value is list of pointers to other transactions

	/**
	 * Constructor for the TM. Calls initializeSites.
	 * @param  operations The list of operations from the input file
	 */
	public TransactionManager(ArrayList<Operation> operations){
		this.operations = operations;
		this.runningTransactions = 0;
		this.currentTime = 1;
		initializeSites();
	}

	/**
	 * Initializes global list of 10 sites.
	 */
	public void initializeSites(){
		ArrayList<Site> allSites = new ArrayList<Site>();
		for(int i = 1; i < 11; i++){
			allSites.add(new Site(i));
		}
		this.sites = allSites;
	}

	/**
	 * This is the starter method for detecting a cycle (deadlock if there is a cycle) - called only once
	 * It is a recursive method which calls detectCycle and traverses the graph until a cycle is detected (at which point it returns)
	 * @param  graph This is a wait for graph. It is a hashmap with the key being a transaction, and the value being a list of transactions it waits for (points to)
	 * @return       Returns an arraylist of integers. If there is no cycle, then the list will be empty (size 0)
	 * 							 If there is a cycle, then it will return the cycle with the list of transactions in the cycle
	 * 							 The first element of the cycle will be the start of the cycle, and the last element will also be the same
	 * 							 e.g. 2 -> 4 -> 1 -> 2 (then there is a cycle starting from Transaction 2 to 4 to 1 then back to itself)
	 */
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

  /**
   * This is the recursive method called by detectCycleStart. It calls itself, traversing the graph until a cycle is or isn't detected
   * @param  graph       This graph is the wait-for graph, which remains the same as in detectCycleStart
   * @param  cycle       This is the current list of transactions that have been traversed.
   * @param  currentNode The current transaction we are on
   * @return             Returns the current list of transactions that have been traversed
   * 										 If a cycle is detected, that is the first transaction is the last, then it will return
   * 										 Otherwise, it will return an empty list with -1 in it, implying there is no cycle
   */
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

	/**
	 * Processes all the operations in a while loop until the lists operations and blockOperations are both empty.
	 * Processes all the blockedOperations as possible first at every time interval. Then, the operations list.
	 * During each loop, the operation is removed from the list and processed completely or added to blockedOperations.
	 */
	public void simulate(){
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
							// Can write - remove from list, add time, start beginning of list again
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
			//currentOperation.printOperation(); // print operation
			switch(currentOperation.operationType) {
				case "begin":
					System.out.println(this.currentTime + ": Transaction T" + currentOperation.transactionID + " begins");
					beginTransaction(currentOperation, false);
					break;
				case "beginRO":
					System.out.println(this.currentTime + ": Read-only Transaction T" + currentOperation.transactionID + " begins ");
					beginTransaction(currentOperation, true);
					break;
				case "end":
					endTransaction(currentOperation);
					break;
				case "W":
					boolean canWrite = write(currentOperation);
					// if cannot write, put into blocked queue
					if (!canWrite) {
						System.out.println(this.currentTime + ": Transaction T" + currentOperation.transactionID + " is blocked");
						this.blockedOperations.add(currentOperation);
					}
					detectDeadlock(); // detect deadlock
					break;
				case "R":
					boolean canRead = read(currentOperation);
					if (!canRead) {
						System.out.println(this.currentTime + ": Transaction T" + currentOperation.transactionID + " is blocked");
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

	/**
	 * Adds conflicts between transactions which are holding locks and transactions waiting in the lock queue
	 * and between transactions in the lock queue themselves.
	 * @param o      The operation which was recently processed
	 * @param siteID The site in which this operation is processed
	 */
	public void addGraphConflicts(Operation o, int siteID) {

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

	/**
	 * TODO
	 */
	public void detectDeadlock() {
		ArrayList<Integer> cycle = detectCycleStart(this.graph);
		if (cycle.size() == 0) {
			return;
		}
		else {
			int youngest = cycle.get(0);
			for (int i = 1; i < cycle.size() - 1; i++) {
				int currentTransaction = cycle.get(i);
				if (this.transactions.get(youngest).startTime < this.transactions.get(currentTransaction).startTime) {
					youngest = currentTransaction;
				}
			}
			System.out.println(this.currentTime + ": Transaction T" + youngest + " is aborted due to deadlock");
			clearLocksandConflicts(youngest);
		}
	}

	/**
	 * Drops the locks when a transaction ends or is aborted and resolves its conflicts.
	 * @param transactionID The transaction that just ended or was aborted
	 */
	public void clearLocksandConflicts(int transactionID) {

		// Decrement running transactions
		this.runningTransactions--;
		// Remove from transaction list
		this.transactions.remove(transactionID);
		// Remove all locks
		for (int i = 0; i < this.sites.size(); i++) {
			Site currentSite = this.sites.get(i);
			LockTable currentLockTable = currentSite.lockTable;

			// remove from writeLocks
			Iterator iterator = currentLockTable.writeLocks.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry pair = (Map.Entry)iterator.next();
				if ((Integer)pair.getValue() == transactionID) {
					iterator.remove();
				}
			}

			// remove from readLocks
			iterator = currentLockTable.readLocks.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry pair = (Map.Entry)iterator.next();
				ArrayList<Integer> transactions = (ArrayList<Integer>)pair.getValue();
				ArrayList<Integer> newTransactions = new ArrayList<Integer>();
				boolean remove = false;
				if (transactions.size() == 0) {
					iterator.remove();
					continue;
				}
				for (int j = 0; j < transactions.size(); j++) {
					if (transactionID == transactions.get(j)) {
						remove = true;
						continue;
					}
					newTransactions.add(transactions.get(j));
				}
				if (remove) {
					if (transactions.size() == 1) {
						iterator.remove();
					}
					else {
						pair.setValue(newTransactions);
					}
				}
			}

			// remove from lockQueue
			iterator = currentLockTable.lockQueue.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry pair = (Map.Entry)iterator.next();
				ArrayList<Lock> locks = (ArrayList<Lock>)pair.getValue();
				ArrayList<Lock> newLocks = new ArrayList<Lock>();
				boolean remove = false;
				for (int j = 0; j < locks.size(); j++) {
					if (transactionID == locks.get(j).transactionID) {
						remove = true;
						continue;
					}
					newLocks.add(locks.get(j));
				}
				if (remove) {
					if (locks.size() == 1) {
						iterator.remove();
					}
					else {
						pair.setValue(newLocks);
					}
				}
			}
			// re-set the currentLockTable
			this.sites.get(i).lockTable = currentLockTable;
		}

		// Remove conflicts from graph
		this.graph.remove(transactionID);
		Iterator iterator = this.graph.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry)iterator.next();
			ArrayList<Integer> transactions = (ArrayList<Integer>)pair.getValue();
			ArrayList<Integer> newTransactions = new ArrayList<Integer>();
			boolean remove = false;
			for (int i = 0; i < transactions.size(); i++) {
				if (transactionID == transactions.get(i)) {
					remove = true;
					continue;
				}
				newTransactions.add(transactions.get(i));
			}
			if (remove) {
				if (transactions.size() == 1) {
					iterator.remove();
				}
				else {
					pair.setValue(newTransactions);
				}
			}
		}

		//removing deadlocked transaction from blocked operations
		ArrayList<Operation> newBlockedOperations = new ArrayList<Operation>();
		for (Operation o: this.blockedOperations){
			if (o.transactionID!=transactionID)
				newBlockedOperations.add(o);
		}
		this.blockedOperations=newBlockedOperations;

		return;
	}

	/**
	 * Initializes the transaction when Operation type is begin.
	 * @param operation  The begin operation
	 * @param isReadOnly Boolean to check if this transaction is read only
	 */
	public void beginTransaction(Operation operation, boolean isReadOnly){
		if (isReadOnly){
			this.transactions.put(operation.transactionID, new Transaction(operation.transactionName, true, this.currentTime));
		}
		else {
			this.transactions.put(operation.transactionID, new Transaction(operation.transactionName, false, this.currentTime));
		}
		this.runningTransactions++;
	}

	/**
	 * Ends the transaction if the operation type is end.
	 * @param o The end operation for this transaction
	 */
	public void endTransaction(Operation o){
		//return if transaction is read only -- no need to commit values or check for site failure
		if (this.transactions.get(o.transactionID).isReadOnly) {
			System.out.println(this.currentTime + ": Transaction T" + o.transactionID + " ends");
			return;
		}

		ArrayList<Operation> ops = this.transactions.get(o.transactionID).pendingOperations;
		// ABORT TRANSACTION IF IT EVER ABORTS
		for (Operation op : ops) {
			if (op.variableID % 2 == 1) {
				Site site = this.sites.get(op.variableID % 10);
				if (site.wasDown) {
					int t = site.latestDownTime;
					if (op.time < t){ // FAILED AFTER - ABORT
						System.out.println(this.currentTime + ": Transaction T" + op.transactionID + " ends: Transaction T" + op.transactionID + " is aborted because it accessed site " + ((op.variableID%10)+1) + " after it failed");
						clearLocksandConflicts(op.transactionID);
						return;
					}
				}
			}
			else {
				for (Site site : this.sites) {
					if (site.wasDown) {
						int t = site.latestDownTime;
						if (op.time < t){ // FAILED AFTER - ABORT
							System.out.println(this.currentTime + ": Transaction T" + op.transactionID + " ends: Transaction T" + op.transactionID + " is aborted because it accessed site " + ((op.variableID%10)+1) + " after it failed");
							clearLocksandConflicts(op.transactionID);
							return;
						}
					}
				}
			}
		}

		System.out.println(this.currentTime + ": Transaction T" + o.transactionID + " ends");
		// Commit the operation since it will not abort
		for (Operation op : ops) {
			//if its a read operation, no need to commit values
			if (op.operationType.equals("R")) {
				continue;
			}
			if (op.variableID % 2 == 1) {
				Site site = this.sites.get(op.variableID % 10);
				if (site.wasDown) {
					int t = site.latestDownTime;
					if (!site.isDown && op.time > site.latestRecoverTime) { //FAILED BEFORE AND RECOVERED BEFORE - COMMIT
						this.sites.get(op.variableID % 10).update(op, this.currentTime);
						System.out.println(this.currentTime + ": Transaction T" + op.transactionID + " commits value " + op.value + " to variable x" + op.variableID + " at site " + (op.variableID % 10 +1));
					}
					else {  // FAILED BEFORE BUT STILL DOWN (NOT RECOVERED) OR FAILED BEFORE AND RECOVERED AFTER - NO COMMIT
						continue;
					}
				}
				else {
					this.sites.get(op.variableID % 10).update(op, this.currentTime);
					System.out.println(this.currentTime + ": Transaction T" + op.transactionID + " commits value " + op.value + " to variable x" + op.variableID + " at site " + (op.variableID % 10 +1));
				}
			}
			else {
				for (Site site : this.sites) {
					if (site.wasDown) {
						int t = site.latestDownTime;
						if (!site.isDown && op.time > site.latestRecoverTime) { //FAILED BEFORE AND RECOVERED BEFORE - COMMIT
							this.sites.get(op.variableID % 10).update(op, this.currentTime);
							System.out.println(this.currentTime + ": Transaction T" + op.transactionID + " commits value " + op.value + " to variable x" + op.variableID + " at site " + (op.variableID % 10 +1));
						}
						else {  // FAILED BEFORE BUT STILL DOWN (NOT RECOVERED) OR FAILED BEFORE AND RECOVERED AFTER - NO COMMIT
							continue;
						}
					}
					else {
						site.update(op, this.currentTime);
						System.out.println(this.currentTime + ": Transaction T" + op.transactionID + " commits value " + op.value + " to variable x" + op.variableID + " at site " + (op.variableID % 10 +1));
					}
				}
			}
		}
		clearLocksandConflicts(o.transactionID);
	}

	/**
	 * Processes a write operation. Adds the operation into a log if it can acquire the write lock.
	 * Otherwise, adds the operation to the lock queue.
	 * @param  o write operation to process
	 * @return   true if its able to write, false if not
	 */
	public boolean write(Operation o){
		// odd variable - one site
		if (o.variableID % 2 != 0) {
			int siteIndex = (o.variableID % 10);
			Site currentSite = this.sites.get(siteIndex);
			if (currentSite.isDown) {
				System.out.println(this.currentTime + ": Transaction T" + o.transactionID + " cannot write to variable x" + o.variableID + " because site " + (siteIndex + 1) + " is down");
				return false;
			}
			else {
				if (currentSite.lockTable.writeLocks.containsKey(o.variableID) || currentSite.lockTable.readLocks.containsKey(o.variableID)) {
					if (currentSite.lockTable.readLocks.containsKey(o.variableID) && currentSite.lockTable.readLocks.get(o.variableID).size()==1) {
						if (currentSite.lockTable.readLocks.get(o.variableID).get(0) == o.transactionID) {
							this.sites.get(siteIndex).lockTable.setWriteLock(o); // sets the write lock
							o.setTime(this.currentTime);
							this.transactions.get(o.transactionID).pendingOperations.add(o);
							System.out.println(this.currentTime + ": Transaction T" + o.transactionID + " obtains write lock on variable x" + o.variableID + " at site " + (siteIndex +1));
							return true;
						}
					}
					addGraphConflicts(o, siteIndex);
					this.sites.get(siteIndex).lockTable.addLockQueue(o.transactionID, o.variableID, this.currentTime, false);
					return false;
				}
				else {
					this.sites.get(siteIndex).lockTable.setWriteLock(o); // sets the write lock
					o.setTime(this.currentTime);
					this.transactions.get(o.transactionID).pendingOperations.add(o);
					System.out.println(this.currentTime + ": Transaction T" + o.transactionID + " obtains write lock on variable x" + o.variableID + " at site " + (siteIndex +1));
					return true;
				}
			}
		}
		// even variable - all sites
		else {
			boolean siteLocked = false;
			for (int i = 0; i < this.sites.size(); i++) {
				Site currentSite = this.sites.get(i);
				if (currentSite.isDown) {
					System.out.println(this.currentTime + ": Transaction T" + o.transactionID + " cannot write to variable x" + o.variableID + " because site " + (i + 1) + " is down");
					continue;
				}
				else {
					if (currentSite.lockTable.writeLocks.containsKey(o.variableID) || currentSite.lockTable.readLocks.containsKey(o.variableID)) {
						if (currentSite.lockTable.readLocks.containsKey(o.variableID) && currentSite.lockTable.readLocks.get(o.variableID).size()==1) {
							if (currentSite.lockTable.readLocks.get(o.variableID).get(0) == o.transactionID) {
								continue;
							}
						}
						addGraphConflicts(o, i);
						this.sites.get(i).lockTable.addLockQueue(o.transactionID, o.variableID, this.currentTime, false);
						siteLocked = true;
					}
				}
			}
			if (!siteLocked) {
				for (int i = 0; i < this.sites.size(); i++) {
					if (!this.sites.get(i).isDown) {
						this.sites.get(i).lockTable.setWriteLock(o);
						System.out.println(this.currentTime + ": Transaction T" + o.transactionID + " obtains write lock on variable x" + o.variableID + " at site " + (i +1));
					}
				}
				o.setTime(this.currentTime);
				this.transactions.get(o.transactionID).pendingOperations.add(o);
				return true;
			}
			else {
				return false;
			}
		}
	}

	/**
	 * Processes the read transaction if it can obtain the lock. Otherwise, add it to the lock queue
	 * @param  o The read operation
	 * @return   true if its able to read, false otherwise
	 */
	public boolean read(Operation o){
		//if this is a read only transaction, process it and return
		if (this.transactions.get(o.transactionID).isReadOnly) {
			//if the variable is odd
			if (o.variableID % 2 != 0) {
				Site currentSite = this.sites.get((o.variableID%10));
				if (currentSite.isDown)
					return false;
				else {
					int val = currentSite.getLatestValueRO(o, this.transactions.get(o.transactionID).startTime);
					System.out.println(this.currentTime + ": Transaction T" + o.transactionID + " reads value " + val + " of variable x" + o.variableID + " from site " + (1+(o.variableID%10)));
					return true;
				}

			}
			//if the variable is even, read from first available site
			else {
				int numSitesDown = 0;
				for (int i = 0; i < this.sites.size(); i++) {
					Site currentSite = this.sites.get(i);
					if (currentSite.isDown) {
						numSitesDown++;
						continue;
					}
					else {
						int val = currentSite.getLatestValueRO(o, this.transactions.get(o.transactionID).startTime);
						System.out.println(this.currentTime + ": Transaction T" + o.transactionID + " reads value " + val + " of variable x" + o.variableID + " from site " + (i+1));
						return true;
					}
				}
				if (numSitesDown==10)
					return false;
			}
		}


		// odd variable - one site
		if (o.variableID % 2 != 0) {
			int siteIndex = (o.variableID % 10);
			Site currentSite = this.sites.get(siteIndex);
			if (currentSite.isDown) {
				System.out.println("Cannot read variable x" + o.variableID + " because site " + (siteIndex + 1) + " is down");
				return false;
			}
			else {
				if (currentSite.lockTable.writeLocks.containsKey(o.variableID)) {
					addGraphConflicts(o, siteIndex);
					this.sites.get(siteIndex).lockTable.addLockQueue(o.transactionID, o.variableID, this.currentTime, true);
					return false;
				}
				else {
					this.sites.get(siteIndex).lockTable.setReadLock(o); // sets the read lock
					for (Variable v : this.sites.get(siteIndex).variables) {
						if (v.number == o.variableID) {
							System.out.println(this.currentTime + ": Transaction T" + o.transactionID + " reads value " + v.getValue() + " of variable x" + o.variableID + " from site " + (siteIndex + 1));
						}
					}
					o.setTime(this.currentTime);
					this.transactions.get(o.transactionID).pendingOperations.add(o);
					return true;
				}
			}
		}
		//even variable-read from the first available site
		else {
			int numSitesLocked = 0;
			for (int i = 0; i < this.sites.size(); i++) {
				Site currentSite = this.sites.get(i);
				//if a site is down or locked, find another site to read from
				if (currentSite.isDown || currentSite.lockTable.writeLocks.containsKey(o.variableID)) {
					numSitesLocked++;
					continue;
				}
				//if readlocked and lock queue is not empty, add tx to lock queue
				if (currentSite.lockTable.readLocks.containsKey(o.variableID) && currentSite.lockTable.lockQueue.containsKey(o.variableID)) {
					addGraphConflicts(o, i);
					this.sites.get(i).lockTable.addLockQueue(o.transactionID, o.variableID, this.currentTime, true);
					return false;
				}

				//if a site has been down, check if the latest commit time is after the latest down time
				//a tx can only read from a site that recovered if it has been written to since recovery
				else if(currentSite.wasDown) {
					int latestDownTime = currentSite.latestDownTime;
					if (latestDownTime < currentSite.latestCommitTime(o.variableID)){
						this.sites.get(i).lockTable.setReadLock(o);
						o.setTime(this.currentTime);
						o.setReadSiteIndex(i);
						this.transactions.get(o.transactionID).pendingOperations.add(o);
						return true;
					}
					else {
						numSitesLocked++;
						continue;
					}
				}
				//if a site is neither down nor locked, add a read lock and break the loop--return true
				else {
					this.sites.get(i).lockTable.setReadLock(o);
					for (Variable v : this.sites.get(i).variables) {
						if (v.number == o.variableID) {
							System.out.println(this.currentTime + ": Transaction T" + o.transactionID + " reads value " + v.getValue() + " of variable x" + o.variableID + " from site " + (i + 1));
						}
					}
					o.setTime(this.currentTime);
					o.setReadSiteIndex(i);
					this.transactions.get(o.transactionID).pendingOperations.add(o);
					return true;
				}
			}
			//if all 10 sites are inaccessible return false
			if (numSitesLocked == 10) {
				addGraphConflicts(o, 0);
				this.sites.get(0).lockTable.addLockQueue(o.transactionID, o.variableID, this.currentTime, true);
				return false;
			}
			return true;
		}

	}

	/**
	 * Prints out the state of the sites or variable
	 * @param o dump operation
	 */
	public void dump(Operation o) {
		System.out.println("\n=== output of dump");
		if (o.dumpVariable != 0) {
			int vID = o.dumpVariable;
			if (vID % 2 == 0){
				for (int i = 0; i < this.sites.size(); i++) {
					Site site = this.sites.get(i);
					if (site.isDown) {
						continue;
					}
					for (int j = 0; j < site.variables.size(); j++) {
						Variable v = site.variables.get(j);
						if (v.number == vID)
							System.out.println("x" + vID + ": " + v.getValue() + " at site " + (i+1));
					}
				}
			}
			else {
				Site site = this.sites.get(vID%10);
				if (site.isDown) {
					return;
				}
				for (int i = 0; i < site.variables.size(); i++) {
					Variable v = site.variables.get(i);
					if (v.number == vID)
						System.out.println("x" + vID + ": " + v.getValue() + " at site " + (vID%10 +1));
				}
			}
		}

		else if (o.dumpSite != 0) {
			Site site = this.sites.get(o.dumpSite-1);
			for (Variable v: site.variables) {
				System.out.println("x" + v.number + ": " + v.getValue() + " at site " + o.dumpSite);
			}

		}

		else {
			for (int i=0; i<this.sites.size(); i++) {
				Site site = this.sites.get(i);
				for (int j=0; j<site.variables.size(); j++) {
						Variable v = site.variables.get(j);
						if (v.value != v.number*10)
							System.out.println("x" + v.number + ": " + v.getValue() + " at site " + (i+1));
				}
			}
			System.out.println("All other variables have their initial values.");
		}
	}

	/**
	 * Fail a site by calling site.fail
	 * @param s failing site ID
	 */
	public void failSite(int s){
		System.out.println(this.currentTime + ": Site " + s + " fails");
		this.sites.get(s - 1).fail(this.currentTime); // fail this site - clear the lock table
	}

	/**
	 * Recover a site by calling site.recover
	 * @param s recovering site ID
	 */
	public void recoverSite(int s){
		System.out.println(this.currentTime + ": Site " + s + " recovers");
		this.sites.get(s - 1).recover(this.currentTime);

	}
}
