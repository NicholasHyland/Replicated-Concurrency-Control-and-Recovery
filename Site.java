import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class for the Site object.
 */
public class Site {


	boolean isDown;				//true if the site is currently down
	boolean wasDown;			//true is the site was every down				
	int latestDownTime;			//time this site most recently failed
	int number;					//site ID
	int latestRecoverTime;		//time this site most recently recovered
	ArrayList<Variable> variables = new ArrayList<Variable>();		//array list of variables stored at this site
	HashMap<Integer, ArrayList<Update>> updates = new HashMap<Integer, ArrayList<Update>>();	//logger to track commit history at this Site
	LockTable lockTable = new LockTable();	//Stores lock information for this site

	/**
	 * Site constructor. Initializes all variables in this site.
	 * @param  number Site ID
	 */
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
					ArrayList<Update> newUpdateList = new ArrayList<Update>();
					newUpdateList.add(new Update((i*10), 0));
					this.updates.put(i, newUpdateList);
				}
				else if ((i % 2) == 0) {
					this.variables.add(new Variable(i, number));
					ArrayList<Update> newUpdateList = new ArrayList<Update>();
					newUpdateList.add(new Update((i*10), 0));
					this.updates.put(i, newUpdateList);
				}
				i++;
			}
		}
		else {
			int i = 2;
			while (i < 21) {
				this.variables.add(new Variable(i, number));
					ArrayList<Update> newUpdateList = new ArrayList<Update>();
					newUpdateList.add(new Update((i*10), 0));
					this.updates.put(i, newUpdateList);
				i += 2;
			}
		}
	}

	/**
	 * Fails the site. We never delete most recent values of variables at this site.
	 * @param time the time the site fails
	 */
	public void fail(int time) {
		this.lockTable = new LockTable(); //set lock table to be new lock table - erases previous locks
		this.isDown = true;
		this.wasDown = true;
		this.latestDownTime = time;
	}

	/**
	 * Recovers the site
	 * @param time the time this site recovers
	 */
	public void recover(int time) {
		this.isDown = false;
		this.latestRecoverTime = time;
	}

	/**
	 * Updates the values of variable at this site. Stores this information in variables and logs it in updates.
	 * @param o    The operation updating this site
	 * @param time The time this update takes place
	 */
	public void update(Operation o, int time) {
		for (Variable v: this.variables) {
			if (v.number == o.variableID) {
				v.setValue(o.value, time);
			}
		}
		ArrayList<Update> currentUpdates = this.updates.get(o.variableID);
		currentUpdates.add(new Update(o.value, time));
		this.updates.put(o.variableID, currentUpdates);
	}

	/**
	 * Returns the most recent commit time for a variable in this site
	 * @param  vID The variable ID in this site
	 * @return     The latest commit time of the variable
	 */
	public int latestCommitTime(int vID) {
		for (Variable v: this.variables) {
			if (v.number == vID) {
				return v.commitTime;
			}
		}
		return 0;
	}

	/**
	 * Returns the most recently committed value from when a Read Only transation began
	 * @param  o    The operation for this RO transaction
	 * @param  time The time this transaction began
	 * @return      The most recently committed value from when this transaction began
	 */
	public int getLatestValueRO(Operation o, int time) {
		int variableID = o.variableID;
		int startTime = time;

		ArrayList<Update> currentUpdates = this.updates.get(o.variableID);
		if (currentUpdates.size()==1)
			return currentUpdates.get(0).value;
		else {
			for (int i=0; i<currentUpdates.size(); i++) {
				if (i==(currentUpdates.size()-1))
					return currentUpdates.get(i).value;
				Update nextUpdate = currentUpdates.get((i+1));
				if (nextUpdate.time<startTime)
					continue;
				else
					return currentUpdates.get(i).value;
			}
		}
		return 0;
	}
}
