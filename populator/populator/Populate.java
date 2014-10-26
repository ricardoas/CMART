package populator;

/**
 * The general populate class to make database items
 * 
 * @author Andy (andrewtu@cmu.edu)
 * @since 0.1
 * @version 1.0
 * @date 23rd Aug 2012
 * 
 * C-MART Benchmark
 * Copyright (C) 2011-2012 theONE Networking Group, Carnegie Mellon University, Pittsburgh, PA 15213, U.S.A
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
public class Populate implements Runnable{
	private static DBPopulator db;
	private PopulateFormat thingType;
	private int noOfThings;
	private boolean onlyOnce;
	private int offset;
	private static int statusInterval = 60000;
	
	/**
	 * Make a populator that creates N of the number of things
	 * 
	 * @param thingType
	 * @param noOfThings
	 */
	public Populate(PopulateFormat thingType, int noOfThings){
		this.thingType = thingType;
		this.noOfThings = noOfThings;
		this.onlyOnce = false;
		this.offset = 0;
	}
	
	/**
	 * Make a populator that creates N number of things, with an offset value passed when we call the .makeThing method
	 * 
	 * @param thingType
	 * @param noOfThings
	 * @param offset
	 */
	public Populate(PopulateFormat thingType, int noOfThings, int offset){
		this.thingType = thingType;
		this.noOfThings = noOfThings;
		this.onlyOnce = false;
		this.offset = offset;
	}
	
	/**
	 * Make a populator that creates N number of things, but only calls the populator once with N as the parameter
	 * @param thingType
	 * @param noOfThings
	 * @param onlyOnce
	 */
	public Populate(PopulateFormat thingType, int noOfThings, boolean onlyOnce){
		this.thingType = thingType;
		this.noOfThings = noOfThings;
		this.onlyOnce = onlyOnce;
		this.offset = 0;
	}
	
	/**
	 * Run the populator thread
	 */
	public void run(){
		if(onlyOnce)
			populateOnce();
		else
			populate();
		
		
		this.thingType.finished();
	}
	
	/**
	 * Call the thing to populate with N as a parameter
	 */
	private void populateOnce(){
		this.thingType.makeThing(db, this.noOfThings + this.offset);
	}
	
	/**
	 * Calls the thing to create passing 0 + offset to (N-1) + offset as a parameter
	 */
	private void populate(){
			long startTime = System.currentTimeMillis();
			
			for(int i=0; i < noOfThings; i++){
				thingType.makeThing(db, i + this.offset);
				
				// Output the amount of time we have been populating
				if(i%statusInterval == 0 && i!=0){
					
					long temp = System.currentTimeMillis();
					CreateAll.print(Thread.currentThread().getName() + " inserted " + i + " things. Time remaining: " + (int)(((temp-startTime)/1000) * ((1.0*noOfThings-i)/statusInterval)) + " seconds");
					startTime = temp;
				}
			}
			
			System.out.println(Thread.currentThread().getName() + " finished. Inserted total of " + noOfThings + " things.");
	}
	
	/**
	 * Sets the database that should be used by the things when they populate
	 * @param dbconn
	 */
	public static void setDB(DBPopulator dbconn){
		db = dbconn;
	}
	
	/**
	 * Closes all of the database connects that the things are using
	 */
	public static void closeDB(){
		// Clean up
		db.closeConnections();
	}
}
