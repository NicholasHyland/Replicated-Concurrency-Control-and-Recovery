// This is a class to model Locks

public class Lock{

	int transactionID;
	int startTime;
  boolean isRead;

	public Lock(int transactionID, int startTime, boolean isRead){
		this.transactionID = transactionID;
		this.startTime = startTime;
    this.isRead = isRead;
	}
}