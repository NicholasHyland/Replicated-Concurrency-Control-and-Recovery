// This is a class to model the Transaction Manager
import java.util.ArrayList;

public class TransactionManager {

	ArrayList<Operation> operations;
	ArrayList<Operation> blockedOperations = new ArrayList<Operation>();
	ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	ArrayList<Variable> variables;
	ArrayList<Site> sites;
	public static int runningTransactions, currentTime;


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
		this.runningTransactions++;
	}

	public void endTransaction(Operation o){

	}

	// it is going to write or not write based on whether or not it has the correct locks
	public boolean write(Operation o){
		// write stuff if it can - return true
		// cannot write because it doesn't have locks - return false
		return true;
	}

	public boolean read(Operation o){
		return true;
	}

	public void dump(Operation o){

	}

	public void failSite(int s){
		this.sites.get(s - 1).fail(); // fail this site - clear the lock table
	}

	public void recoverSite(int s){

	}
}
