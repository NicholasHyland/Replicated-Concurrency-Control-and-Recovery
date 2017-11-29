//endTransaction is not correct--op.time doesnt make sense..we're not keeping track per site..its updated at every write regardless of the site
//found the errors..adding todos
//TODO: rename transaction.writeOperations to transaction.pendingOperations in Transaction.java and TransactionManager.java
//TODO: check whether operation is read or write when ending tx to remove appropriate lock
//TODO: add read operations to transactions.pendingOperations list (tx will abort if a read transaction accessed it before it failed)
//TODO: on lines ~522 and ~553, we add an operation to writeOperations and return true but on line ~189 we update op.time for this operation
//so this change isn't reflected in transaction.writeOperations -- in terms of tracking per site each operation stores variable info
//but not site info


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

	public TransactionManager(ArrayList<Operation> operations){
		this.operations = operations;
		this.runningTransactions = 0;
		this.currentTime = 1;
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
			//currentOperation.printOperation(); // print operation
			switch(currentOperation.operationType) {
				case "begin":
					System.out.println("Transaction T" + currentOperation.transactionID + " begins at time " + this.currentTime);
					beginTransaction(currentOperation, false);
					break;
				case "beginRO":
					System.out.println("Read-only Transaction T" + currentOperation.transactionID + " begins at time " + this.currentTime);
					beginTransaction(currentOperation, true);
					break;
				case "end":
					System.out.println("Ending Transaction T" + currentOperation.transactionID + " at time " + this.currentTime);
					endTransaction(currentOperation);
					break;
				case "W":
					boolean canWrite = write(currentOperation);
					// if cannot write, put into blocked queue
					// if true, continue, if false, put into blocked queue
					if (!canWrite) {
						System.out.println("Transaction T" + currentOperation.transactionID + " is blocked at time " + this.currentTime);
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
						System.out.println("Transaction T" + currentOperation.transactionID + " is blocked at time " + this.currentTime);
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
				if (this.transactions.get(youngest).startTime < this.transactions.get(currentTransaction).startTime) {
					youngest = currentTransaction;
				}
			}
			abortTransaction(youngest);
			//abortTransaction(op.transactionID); - REMOVE LOCKS
		}
	}

	public void abortTransaction(int transactionID) {
		System.out.println("Transaction T" + transactionID + " is aborted at time " + this.currentTime);

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

	public void beginTransaction(Operation operation, boolean isReadOnly){
		if (isReadOnly){
			this.transactions.put(operation.transactionID, new Transaction(operation.transactionName, true, this.currentTime));
		}
		else {
			this.transactions.put(operation.transactionID, new Transaction(operation.transactionName, false, this.currentTime));
		}
		this.runningTransactions++;
	}

	public void endTransaction(Operation o){
		this.runningTransactions--;
		//System.out.println("Ending transaction " + o.transactionID);
		//return if transaction is read only -- no need to commit values or check for site failure
		if (this.transactions.get(o.transactionID).isReadOnly) {
			return;
		}

		ArrayList<Operation> ops = this.transactions.get(o.transactionID).writeOperations;

		// ABORT TRANSACTION IF IT EVER ABORTS
		for (Operation op : ops) {
			if (op.variableID % 2 == 1) {
				Site site = this.sites.get(op.variableID % 10);
				if (site.wasDown) {
					int t = site.latestDownTime;
					if (op.time < t){ // FAILED AFTER - ABORT
						abortTransaction(op.transactionID); //- TODO - what if the last operation aborts, all previous operations will commit
						return;
					}
				}
			}
			else {
				for (Site site : this.sites) {
					if (site.wasDown) {
						int t = site.latestDownTime;
						if (op.time < t){ // FAILED AFTER - ABORT
							abortTransaction(op.transactionID); //- TODO - what if the last operation aborts, all previous operations will commit
							return;
						}
					}
				}
			}
		}

		// COMMIT OPERATIONS SINCE IT DOESN'T ABORT
		for (Operation op : ops) {
			if (op.variableID % 2 == 1) {
				Site site = this.sites.get(op.variableID % 10);
				this.sites.get(op.variableID).lockTable.removeWriteLock(op); // remove the write lock from that site
				if (site.wasDown) {
					int t = site.latestDownTime;
					if (op.time < t){ // FAILED AFTER - ABORT
						abortTransaction(op.transactionID); //- TODO - what if the last operation aborts, all previous operations will commit
						break;
					}
					else if (!site.isDown && op.time > site.latestRecoverTime) { //FAILED BEFORE AND RECOVERED BEFORE - COMMIT
						this.sites.get(op.variableID % 10).update(op, this.currentTime);
						System.out.println("Transaction T" + op.transactionID + " committed variable x" + op.variableID + " at site " + (op.variableID % 10 +1) + " at time " + this.currentTime);
					}
					else {	// FAILED BEFORE BUT STILL DOWN (NOT RECOVERED) OR FAILED BEFORE AND RECOVERED AFTER - NO COMMIT
						continue;
					}
				}
				else {
					this.sites.get(op.variableID % 10).update(op, this.currentTime);
					System.out.println("Transaction T" + op.transactionID + " committed variable x" + op.variableID + " at site " + (op.variableID % 10 +1) + " at time " + this.currentTime);
				}
			}
			else {
				for (Site site : this.sites) {
					site.lockTable.removeWriteLock(op); // remove the write lock from that site - TODO - how about the other locks? -- we only have write locks in ops
					if (site.wasDown) {
						int t = site.latestDownTime;
						if (op.time < t){ // FAILED AFTER - ABORT
							abortTransaction(op.transactionID); //- TODO - what if the last operation aborts, all previous operations will commit
							break;
						}
						else if (!site.isDown && op.time > site.latestRecoverTime) { //FAILED BEFORE AND RECOVERED BEFORE - COMMIT
							this.sites.get(op.variableID % 10).update(op, this.currentTime);
							System.out.println("Transaction T" + op.transactionID + " committed variable x" + op.variableID + " at site " + (op.variableID % 10 +1) + " at time " + this.currentTime);
						}
						else {	// FAILED BEFORE BUT STILL DOWN (NOT RECOVERED) OR FAILED BEFORE AND RECOVERED AFTER - NO COMMIT
							continue;
						}
					}
					else {
						site.update(op, this.currentTime);
						System.out.println("Transaction T" + op.transactionID + " committed variable x" + op.variableID + " at site " + (site.number) + " at time " + this.currentTime);
					}
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
				System.out.println("Cannot write to variable x" + o.variableID + " because site " + (siteIndex + 1) + " is down");
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
					System.out.println("Transaction T" + o.transactionID + " obtains write lock on variable x" + o.variableID + " at site " + (siteIndex +1));
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
					System.out.println("Cannot write to variable x" + o.variableID + " because site " + (i + 1) + " is down");
					continue;
					//return false;
				}
				else {
					if (currentSite.lockTable.writeLocks.containsKey(o.variableID) || currentSite.lockTable.readLocks.containsKey(o.variableID)) {
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
						System.out.println("Transaction T" + o.transactionID + " obtains write lock on variable x" + o.variableID + " at site " + (i +1));
					}
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
		//if this is a read only transaction, process it and return
		if (this.transactions.get(o.transactionID).isReadOnly) {
			//if the variable is odd
			if (o.variableID % 2 != 0) {
				Site currentSite = this.sites.get((o.variableID%10));
				if (currentSite.isDown)
					return false;
				else {
					int val = currentSite.getLatestValueRO(o, this.transactions.get(o.transactionID).startTime);
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
				//non replicated data is available for reads and writes
				//no need to check if site was down for odd variables--data isnt replicated it's available for reads
				/*
				//if a site has been down, check if the latest commit time is after the latest down time
				//a tx can only read from a site that recovered if it has been written to since recovery
				else if(currentSite.wasDown) {
					//int latestDownTime = currentSite.downTime.get(currentSite.downTime.size()-1);
					int latestDownTime = currentSite.latestDownTime;
					if (latestDownTime < currentSite.latestCommitTime(o.variableID)){
						this.sites.get(siteIndex).lockTable.setReadLock(o);
						return true;
					}
					else {
						addGraphConflicts(o, siteIndex);
						this.sites.get(siteIndex).lockTable.addLockQueue(o.transactionID, o.variableID, this.currentTime, true);
						return false;
					}
				} */
				//if a site is neither down nor locked, add a read lock and return true
				else {
					this.sites.get(siteIndex).lockTable.setReadLock(o); // sets the write lock
					for (Variable v : this.sites.get(siteIndex).variables) {
						if (v.number == o.variableID) {
							System.out.println("Transaction T" + o.transactionID + " reads value " + v.getValue() + " of variable x" + o.variableID + " from site " + (siteIndex + 1));
						}
					}
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
					//int latestDownTime = currentSite.downTime.get(currentSite.downTime.size()-1);
					int latestDownTime = currentSite.latestDownTime;
					if (latestDownTime < currentSite.latestCommitTime(o.variableID)){
						this.sites.get(i).lockTable.setReadLock(o);
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
							System.out.println("Transaction T" + o.transactionID + " reads value " + v.getValue() + " of variable x" + o.variableID + " from site " + (i + 1));
						}
					}
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

	public void dump(Operation o) {
		System.out.println("=== output of dump");
		if (o.dumpVariable != 0) {
			int vID = o.dumpVariable;
			if (vID % 2 == 0){
				for (int i = 0; i < this.sites.size(); i++) {
					Site site = this.sites.get(i);
					if (site.isDown) {
						continue;
					}
					//System.out.println("x" + vID + ": " + site.variables.get(vID-1).getValue() + " at site " + (i+1));
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

	public void failSite(int s){
		System.out.println("Site " + s + " fails at time " + this.currentTime);
		this.sites.get(s - 1).fail(this.currentTime); // fail this site - clear the lock table
	}

	public void recoverSite(int s){
		System.out.println("Site " + s + " recovers");
		this.sites.get(s - 1).recover(this.currentTime);

	}
}
