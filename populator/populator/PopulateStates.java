package populator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeMap;
/**
 * Populate the states the users can live in
 * @author andy
 *
 */
public class PopulateStates implements PopulateFormat{
	private static RandomSelector<Integer, String> statesRS;
	private static Boolean localPopulated = false;
	
	// the columns the format is in
	private int STATE_SHORT = 1;
	private int STATE_LONG = 2;
	
	public PopulateStates(){
	}
	
	/**
	 * Insert the states
	 */
	public boolean makeThing(DBPopulator db, int noOfStates) {
			ArrayList<Integer> keys = new ArrayList<Integer>(64);
			
			/*
			 *  If a negative value is passed then read all values
			 */
			if(noOfStates < 0)
				noOfStates = Integer.MAX_VALUE;
			
			/*
			 *  Open the states file
			 */
			System.out.println(CreateAll.STATES_FILE);
			InputStreamReader ins;
			BufferedReader in = null;
			try {
				ins = new InputStreamReader(new FileInputStream(CreateAll.STATES_FILE));
				in = new BufferedReader(ins);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			/*
			 *  Read in the values
			 */
			try {
				// Read and insert the states
				int inserted = 0;
				
				while(in.ready() && inserted <= noOfStates){
					// Read the line and insert it
					String[] state = in.readLine().split(",");
					int stateID = db.insertState(inserted+1, state[STATE_SHORT], state[STATE_LONG]);
					keys.add(stateID);
					
					inserted++;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*
			 * Create the states random selector for when we are creating users
			 * Key = state id
			 */
			
			//statesRS = new RandomSelector<Integer, String>(MakeAddress.class.getResourceAsStream(CreateAll.STATES_FILE), Integer.class, keys);
			try {
				statesRS = new RandomSelector<Integer, String>(new FileInputStream(CreateAll.STATES_FILE) , Integer.class, keys);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		return false;
	}
	
	public static int getRandomState(){
		if(CreateAll.STATES_UNIFORM)
			return statesRS.getRandomUniformKey();
		else return statesRS.getRandomKey();
	}
	
	public void finished(){
		
	}
	
	public static boolean remakeTable(DBPopulator db) {
		return db.dropAddStates();
	}
}
