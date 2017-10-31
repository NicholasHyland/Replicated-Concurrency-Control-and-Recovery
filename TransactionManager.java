// This is a class to model the Transaction Manager
import java.util.ArrayList;

public class TransactionManager {

	ArrayList<Operation> operations;
	ArrayList<Transaction> transactions;
	ArrayList<Variable> variables;
	int runningTransactions;


	public TransactionManager(ArrayList<Operation> operations){
		this.operations = operations;
		runningTransactions=0;
	}

	//initializes all 10 sites
  	public static ArrayList<Site> initializeSites(){
    	ArrayList<Site> sites= new ArrayList<Site>();
    	for(int i=1; i<11; i++)
    		sites.add(new Site(i));
      	return sites;
  	}

	public void simulate(){

	}

	public void detectDeadlock(){

	}
}
