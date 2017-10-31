// This is a class to model the Transaction Manager
import java.util.ArrayList;

public class TransactionManager {

	ArrayList<Operation> operations;
	ArrayList<Transaction> transactions;
	ArrayList<Variable> variables;
	ArrayList<Site> sites;
	int runningTransactions, currentTime;


	public TransactionManager(ArrayList<Operation> operations){
		this.operations = operations;
		runningTransactions=0;
		currentTime=0;
		initializeSites();
	}

	//initializes all 10 sites
  	public static void initializeSites(){
  		sites= new ArrayList<Site>();
    	for(int i=1; i<11; i++)
    		sites.add(new Site(i));
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
		Operation current;
		while (runningTransactions>0 || currentTime==0){
			for (Operation o: operations){
				switch(o.operationType){
					case "begin":
						transactions.add(New Transaction(o.transactionName, false, 0));
						runningTransactions++;
						break;
					case "beginRO":
						transactions.add(New Transaction(o.transactionName, true, 0));
						runningTransactions++;
						break;
					case "end":
					case "W":
					case "R":
					case "dump":
						dump(o);
						break;
					case "fail":
						failSite(o.failSite);
						break;
					case "recover":
						recoverSite();
						break;
				}

			}
			if (runningTransactions > 1)
				detectDeadlock()
			currentTime++;
		}
	}

	public void detectDeadlock(){
		if (runningTransactions==1)
			return;

	}

	public void dump(Operation o){

	}

	public void failSite(int s){

	}

	public void recoverSite(int s){

	}
}
