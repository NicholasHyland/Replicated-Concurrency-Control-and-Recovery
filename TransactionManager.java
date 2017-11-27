// This is a class to model the Transaction Manager
import java.util.ArrayList;

public class TransactionManager {

	ArrayList<Operation> operations;
	ArrayList<Operation> blockedOperations = new ArrayList<Operation>();
	ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	ArrayList<Variable> variables;
	ArrayList<Site> sites;
	public static int runningTransactions, currentTime;
	ArrayList<ArrayList<Integer>> graph = new ArrayList<ArrayList<Integer>>();
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

  /*

	public void simulate(){
		Operation current;
		//loop until the current operation is not a begin
		for (Operation o: operations){
			current=o;
			if (o.operationType.equals("begin")){
				transactions.add(New Transaction(o.transactionName, false))
			}
			else if (o.operationType.equals("beginRO"))
				transactions.add(New Transaction(o.transactionName, true))
			runningTransactions++;
			else break;
		}

		//loop until all transactions have ended, detect deadlocks at every iteration, iterate time with every loop
		while (runningTransactions>0){



			detectDeadlock();
			currentTime++;
		}
	} */

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

	public void detectDeadlock() {
		if (this.runningTransactions == 1)
			return;
	}

	public void beginTransaction(Operation operation, boolean isReadOnly){
		if (isReadOnly){
			this.transactions.add(new Transaction(operation.transactionName, true, this.currentTime));
		}
		else {
			this.transactions.add(new Transaction(operation.transactionName, false, this.currentTime));
		}
		operation.printOperation();
		this.runningTransactions++;
	}

	public void endTransaction(Operation o){
		this.runningTransactions--;
		System.out.println("Ending transaction " + o.transactionID);
		//return is transaction is read only -- no need to commit values or check for site failure
		for (Transaction t: this.transactions){
			if (t.transactionID == o.transactionID && t.isReadOnly)
				return;
		}
		ArrayList<Operation> ops = this.transactions.get(o.transactionID).operations;
		for (Operation op: ops) {
			if (op.variableID%2 == 1) {
				Site site = this.sites.get(op.variableID);
				if (site.wasDown) {
					int t = site.wasDownTime;
					if (op.time>t){
						//abortTransaction(op.transactionID);
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
					if (site.wasDown) {
						int t = site.wasDownTime;
						if (op.time>t){
							//abortTransaction(op.transactionID);
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
					// if writeLocks.containsKey, add pointer from o.transactionID --> writeLocks.get(o.variableID)
					// if readLocks.containsKey, add pointer from o.transactionID --> to each transactionID in readLocks.get(o.variableID)
					// if writeLockQueue.containsKey, add pointer from o.transactionID --> to each transactionID of lock in writeLockQueue.get(o.variableID)
					// if readLockQueue.containsKey, add pointer from o.transactionID --> to each transactionID of lock in readLockQueue.get(o.variableID)
					this.sites.get(siteIndex).lockTable.addWriteLockQueue(o.transactionID, o.variableID, this.currentTime);
					return false;
				}
				else {
					this.transactions.get(o.transactionID).operations.add(o);
					return true;
				}
			}
		}
		// even variable - all sites
		else {
			boolean oneDown = false;
			for (int i=0; i<this.sites.size(); i++) {
				Site currentSite = this.sites.get(i);
				if (currentSite.isDown) {
					System.out.println("Cannot write to variable x" + o.variableID + " because site " + i + 1 + " is down");
					continue;
					//return false;
				}
				else {
					if (currentSite.lockTable.writeLocks.containsKey(o.variableID) || currentSite.lockTable.readLocks.containsKey(o.variableID)) {
						// if writeLocks.containsKey, add pointer from o.transactionID --> writeLocks.get(o.variableID)
						// if readLocks.containsKey, add pointer from o.transactionID --> to each transactionID in readLocks.get(o.variableID)
						// if writeLockQueue.containsKey, add pointer from o.transactionID --> to each transactionID of lock in writeLockQueue.get(o.variableID)
						// if readLockQueue.containsKey, add pointer from o.transactionID --> to each transactionID of lock in readLockQueue.get(o.variableID)
						this.sites.get(i).lockTable.addWriteLockQueue(o.transactionID, o.variableID, this.currentTime);
						oneDown=true;
					}
				}
			}
			if (!oneDown) {
				this.transactions.get(o.transactionID).operations.add(o);
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
