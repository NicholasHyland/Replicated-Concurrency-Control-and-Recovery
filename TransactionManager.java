// This is a class to model the Transaction Manager
import java.util.ArrayList;

public class TransactionManager {

	ArrayList<Operation> operations;
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
		while (this.runningTransactions > 0 || this.currentTime == 0){
			for (Operation o : this.operations){
				// o.printOperation();
				switch(o.operationType) {
					case "begin":
						beginTransaction(o, false);
						break;
					case "beginRO":
						beginTransaction(o, true);
						break;
					case "end":
						endTransaction(o);
						break;
					case "W":
						write(o);
						break;
					case "R":
						read(o);
						break;
					case "dump":
						dump(o);
						break;
					case "fail":
						failSite(o.failSite);
						break;
					case "recover":
						recoverSite(o.recoverSite);
						break;
				}

				// detect deadlock
				if (this.runningTransactions > 1) {
				detectDeadlock();
				}
				// increment time
				this.currentTime++;
			}
		}
	}

	public void detectDeadlock(){
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

	public void write(Operation o){

	}

	public void read(Operation o){

	}

	public void dump(Operation o){

	}

	public void failSite(int s){
		this.sites.get(s - 1).fail(); // fail this site - clear the lock table
	}

	public void recoverSite(int s){

	}
}
