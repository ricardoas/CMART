package populator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.math.*;

public class PopulateUsers implements PopulateFormat{
	private static RandomSelector<String, String> lastNamesRS = null;
	private static RandomSelector<String, String> firstNamesRS = null;
	private static RandomSelector<Integer, String> userRatingsRS = null;
	private static Boolean localPopulated = false;
	private static Distribution addressesDist;
	
	private CountDownLatch finishedLatch;
	
	/**
	 * Get the distributions used to make the user's data
	 */
	public PopulateUsers(CountDownLatch finishedLatch){
		if(!localPopulated){
			synchronized(localPopulated){
				if(!localPopulated){
					try {
						lastNamesRS = new RandomSelector<String, String>(new FileInputStream(CreateAll.LAST_NAMES_FILE), String.class);
						firstNamesRS = new RandomSelector<String, String>(new FileInputStream(CreateAll.FIRST_NAMES_FILE), String.class);
						userRatingsRS = new RandomSelector<Integer, String>(new FileInputStream(CreateAll.USER_RATINGS_FILE), Integer.class);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					addressesDist = new Distribution(CreateAll.ADDRESSES_DIST_TYPE, CreateAll.ADDRESSES_MIN, CreateAll.ADDRESSES_MAX, CreateAll.ADDRESSES_DIST_MEAN, CreateAll.ADDRESSES_DIST_SD, CreateAll.ADDRESSES_ALPHA, CreateAll.ADDRESSES_LAMBDA);	
					
					
					localPopulated = true;
				}
			}
		}
		
		this.finishedLatch = finishedLatch;
	}
	
	/**
	 * Make a user
	 */
	public boolean makeThing(DBPopulator db, int usersInserted){
		String username = getUsername(usersInserted);
		
		/*
		 * Make a user
		 */
		long userID = db.insertUser(getFirstName(),
				getLastName(),
				username,
				username,
				getEmail(username),
				getRating(),
				new Date(CreateAll.START_TIME - Math.round((Math.random()*100000000000l))));
		
		if(userID > 0){
			/*
			 * Make the user's addresses
			 */
			int noOfAddress = (int) Math.round(addressesDist.getNext());
			
			// Insert the user's addresses they must have one at least
			if(noOfAddress > 0){
				Address address = MakeAddress.getAddress(userID, true);
				db.insertAddress(address);
			}
			
			// Make the other addresses
			for(int i=1; i < noOfAddress; i++){
				Address address = MakeAddress.getAddress(userID, false);
				db.insertAddress(address);
			}
			
			return true;
		}
		else{
			System.out.println("PopulateUsers: Failed to insert a user");
			return true;
		}
		
	}
	
	public static boolean remakeTable(DBPopulator db){
		db.dropAddAdresses();
		return db.dropAddUsers();
	}
	
	/*
	 * Make the users first name
	 */
	private static String getFirstName(){
		if(CreateAll.FIRST_NAMES_UNIFORM)
			return firstNamesRS.getRandomUniformKey();
		else
			return firstNamesRS.getRandomKey();
	}
	
	private static String getLastName(){
		if(CreateAll.LAST_NAMES_UNIFORM)
			return lastNamesRS.getRandomUniformKey();
		else
			return lastNamesRS.getRandomKey();
	}
	
	private static String getUsername(int uniqueID){
		return "User" + uniqueID;
	}
	
	private static String getEmail(String username){
		return username + "@ece.cmu.edu";
	}
	
	private static int getRating(){
		if(CreateAll.USER_RATINGS_UNIFORM)
			return userRatingsRS.getRandomUniformKey();
		else
			return userRatingsRS.getRandomKey();
	}
	
	public void finished(){
		finishedLatch.countDown();
	}
}
