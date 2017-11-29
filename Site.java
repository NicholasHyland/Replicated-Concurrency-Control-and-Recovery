// This is a class to model Sites
import java.util.ArrayList;
import java.util.HashMap;

public class Site {

	boolean isDown;
	boolean wasDown;
	int latestDownTime;
	//int latestCommitTime;
	int number;
	int latestRecoverTime;
	ArrayList<Variable> variables = new ArrayList<Variable>();
	HashMap<Integer, ArrayList<Update>> updates = new HashMap<Integer, ArrayList<Update>>();
	LockTable lockTable = new LockTable();
	//DataManager DM = new DataManager();


 	public Site(int number) {
		this.isDown = false;
		this.wasDown = false;
		this.number = number;

		//initialize variables at this site
		//if this is an even numbered site
		if ((number % 2) == 0) {
			int i = 1;
			while (i < 21) {
				if (((i % 10) + 1) == number) {
					this.variables.add(new Variable(i, number));
				}
				else if ((i % 2) == 0) {
					this.variables.add(new Variable(i, number));
				}
				i++;
			}
		}
		else {
			int i = 2;
			while (i < 21) {
				this.variables.add(new Variable(i, number));
				i += 2;
			}
		}
	}

	public void fail(int time) {
		this.lockTable = new LockTable(); //set lock table to be new lock table - erases previous locks
		this.isDown = true;
		this.wasDown = true;
		this.latestDownTime = time;
	}

	public void recover(int time) {
		this.isDown = false;
		this.latestRecoverTime = time;
	}

	public void update(Operation o, int time) {
		for (Variable v: this.variables) {
			if (v.number == o.variableID) {
				v.setValue(o.value, time);
				//v.set();
			}
		}
	}

	public int latestCommitTime(int vID) {
		for (Variable v: this.variables) {
			if (v.number == vID) {
				return v.commitTime;
			}
		}
		return 0;
	}
}
