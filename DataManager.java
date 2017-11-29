// This is a class to model the Data Manager
import java.util.ArrayList;
import java.util.HashMap;


public class DataManager{

	HashMap<Integer, ArrayList<Update>> updates;

	public DataManager() {
		updates = new HashMap<Integer, ArrayList<Update>>();
		for (int i=1; i<11; i++) {
			ArrayList<Update> newUpdates = new ArrayList<Update>;
			updates.put(i, newUpdates)
		}
		
	}

	public void addUpdate(int time, int variableID, int )

}