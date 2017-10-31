// This is a class to model the Transaction Manager
import java.util.ArrayList;

public class TransactionManager {

	ArrayList<Operation> operations;
	ArrayList<Transaction> transactions;
	ArrayList<Variable> variables;
	int runningTransactions, currentTime;


	public TransactionManager(ArrayList<Operation> operations){
		this.operations = operations;
		runningTransactions=0;
		currentTime=0;
	}

	//initializes all 10 sites
  	public static ArrayList<Site> initializeSites(){
    	ArrayList<Site> sites= new ArrayList<Site>();
    	for(int i=1; i<11; i++)
    		sites.add(new Site(i));
      	return sites;
  	}

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
	}

	public void detectDeadlock(){
		if (runningTransactions==1)
			return;

	}
}
