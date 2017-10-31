// This is a class to model Locks

public class Lock{

	boolean isRead;
	boolean isWrite;
	int onVariable;

	public Lock(String type, int Var){
		this.onVariable = Var;
		if(type.equals("Read")){
			this.isRead=true;
			this.isWrite=false;
		}
		else
			if (type.equals("Write")){
				this.isWrite=true;
				this.isRead=false;
			}
	}
}