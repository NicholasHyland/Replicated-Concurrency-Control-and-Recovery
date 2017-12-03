/**
 * A class for the object Update. Stores a value and time that value was committed
 */
public class Update {

	int value;
	int time;

	/**
	 * Constructor for Update
	 * @param  value The value to commit
	 * @param  time  The time this update was committed
	 */
	
	public Update(int value, int time){
		this.value=value;
		this.time=time;
	}
}