package populator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

/**
 * This class reads the items from the database and inserts them to Solr
 * 
 * @author andy
 *
 */
public class DBToSolr implements PopulateFormat{
	
	private CountDownLatch finishedLatch;
	
	public DBToSolr(CountDownLatch finishedLatch){
		this.finishedLatch = finishedLatch;
	}
	
	/**
	 * Read an item and add it to Solr
	 */
	public boolean makeThing(DBPopulator db, int itemID){
		Item item = db.getItem(itemID);

		if(item!=null) db.addToSolr(item.getID(), item.getName(), item.getDescription(), item.getCurrentBid(), item.getEndDate());
		
		return true;
	}
	
	
	public static boolean remakeTable(DBPopulator db){
		return true;
	}
	
	public boolean configure(){
		
		
		return true;
	}
	
	public void finished(){
		this.finishedLatch.countDown();
	}	
}
