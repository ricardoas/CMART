package populator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Insert the categories for the items
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
public class PopulateCategories implements PopulateFormat{
	private static int FREQ_COL = 0;
	private static int ID_COL = 1;
	private static int NAME_COL = 2;
	private static int PARENT_COL = 3;
	
	private static RandomSelector<Integer, String> categoryRS = null;
	private static Boolean localPopulated = false;
	private CountDownLatch finishedLatch;
	
	public PopulateCategories(CountDownLatch finishedLatch){
		this.finishedLatch = finishedLatch;
		/*
		 * Populate the random selector for when we create the items
		 * Key = integer of the chosen categories index
		 * 
		 */
		if(!localPopulated){
			synchronized(localPopulated){
				if(!localPopulated){
					//categoryRS = new RandomSelector<Integer, String>(CreateAll.class.getResourceAsStream(CreateAll.CATEGORIES_FILE), Integer.class);
					try {
						System.out.println(CreateAll.CATEGORIES_FILE);
						categoryRS = new RandomSelector<Integer, String>(new FileInputStream(CreateAll.CATEGORIES_FILE), Integer.class);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					localPopulated = true;
				}
			}
		}
	}
	
	/**
	 * Read in noOfCategories things 
	 */
	public boolean makeThing(DBPopulator db, int noOfCategories) {
			/*
			 *  If a negative value is passed then read all values
			 */
			if(noOfCategories < 0)
				noOfCategories = Integer.MAX_VALUE;
			
			/*
			 * Open the categories file and read in all of the values
			 */
			
			ReadCSV in=null;
			try {
				in = new ReadCSV(new FileInputStream(CreateAll.CATEGORIES_FILE));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				int inserted = 0;
				String[] category;
				
				while(in.ready() && inserted <= noOfCategories){
					// Read and insert the value
					category = in.readLine();
					
					db.insertCategory(Integer.valueOf(category[ID_COL]), category[NAME_COL], Integer.valueOf(category[PARENT_COL]));
					
					inserted++;
					if(inserted%1000 == 0) System.gc();
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return false;
	}
	
	/**
	 * Get a random category
	 * @return
	 */
	public static int getRandomCategory(){
		if(CreateAll.CATEGORIES_UNIFORM)
			return categoryRS.getRandomUniformKey();
		else return categoryRS.getRandomKey();
	}
	
	/**
	 * When populating is finished we just need to countdown the latch
	 */
	public void finished(){
		finishedLatch.countDown();
	}
	
	/**
	 * drop and add the categories table
	 * @param db
	 * @return
	 */
	public static boolean remakeTable(DBPopulator db) {
		return db.dropAddCategories();
	}
}
