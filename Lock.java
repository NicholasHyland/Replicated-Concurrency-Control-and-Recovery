// This is a class to model Locks

public class Lock{

	int transactionID;
	int startTime;

	public Lock(int transactionID, int startTime){
		this.transactionID = transactionID;
		this.startTime = startTime;
	}
}