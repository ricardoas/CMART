package populator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

public class PopulateItems implements PopulateFormat{
	private static Distribution questionsDist;
	
	private CountDownLatch finishedLatch;
	
	public PopulateItems(CountDownLatch finishedLatch){
		this.finishedLatch = finishedLatch;
		questionsDist = new Distribution(CreateAll.QUESTIONS_DIST_TYPE, CreateAll.QUESTIONS_MIN, CreateAll.QUESTIONS_MAX, CreateAll.QUESTIONS_DIST_MEAN, CreateAll.QUESTIONS_DIST_SD, CreateAll.QUESTIONS_ALPHA, CreateAll.QUESTIONS_LAMBDA);	
	}
	
	/**
	 * Make an item and write it to the database. Also calls to make the bids
	 */
	public boolean makeThing(DBPopulator db, int usersInserted){
		StringBuffer sql = new StringBuffer();
		
		/*
		 * Make the item
		 */
		double startPrice = PopulateOldItems.getStartingPrice();
		double reservePrice = PopulateOldItems.getReservePrice(startPrice);
		double buyNowPrice = PopulateOldItems.getBuyNowPrice(startPrice, reservePrice);
		int quantity = PopulateOldItems.getQuantity();
		long sellerID = PopulateOldItems.getSellerID(CreateAll.NO_OF_USERS);
		Date startDate = startDate();
		String name = PopulateOldItems.getName();
		String description = PopulateOldItems.getDescription();
		Date endDate = endDate();
		long categoryID = PopulateOldItems.getCategoryID();
		
		long itemID = db.insertItem(name,
				description,
				startPrice,
				quantity,
				reservePrice,
				buyNowPrice,
				startDate,
				endDate,
				sellerID,
				categoryID, sql);
		
		/*
		 * Make the bids for the items
		 */
		int noOfBids = PopulateOldItems.getNoOfBids();
		
		Bid currentBid = new Bid(-1, -1, quantity, startPrice, startPrice, startDate, null, null);
		Bid highestBid = null;
		Bid secondBid = null;
		
		for(int i=0; i<noOfBids; i++){
			Bid temp = makeBid.makeBid(currentBid);
			db.insertBid(temp, itemID, sql);
			currentBid = temp;
			
			if(highestBid == null)
				highestBid = currentBid;
			else if(secondBid == null)
				secondBid = currentBid;
			else if(currentBid.getMaxBid() > highestBid.getMaxBid()){
				secondBid = highestBid;
				highestBid = currentBid;
			}
			else if(currentBid.getMaxBid() > secondBid.getMaxBid()){
				secondBid = currentBid;
			}
		}
		
		/*
		 * Update the item with the highest bid
		 */
		if(highestBid != null){
			double currentBidPrice = highestBid.getBid();
			double maxBidPrice = highestBid.getMaxBid();
			
			if(secondBid != null && secondBid.getMaxBid() > currentBidPrice)
				currentBidPrice = secondBid.getMaxBid() + 0.01;
			
			db.updateItemMaxBid(itemID, noOfBids, currentBidPrice, maxBidPrice, highestBid.getUserID(), categoryID, sql);
		}
		else db.updateItemMaxBid(itemID, 0, 0.0, 0.0, 0, categoryID, sql);
		
		
		/*
		 * Add some questions to the item
		 */
		int questions = (int)Math.round(questionsDist.getNext());
		if(questions>0){
			long asker = PopulateOldItems.getSellerID(CreateAll.NO_OF_USERS);
			long qid = db.insertQuestion(asker, sellerID, itemID, startDate, PopulateOldItems.getQuestion(), sql);
			if(CreateAll.rand.nextDouble() < CreateAll.QUESTIONS_FRACTION_ANSWERED)
				db.insertAnswer(sellerID, asker, itemID, startDate, PopulateOldItems.getQuestion(), qid, sql);
		}
		
		db.addItem(sql);
		double currentBidPrice = 0.0;
		if(highestBid != null) currentBidPrice = highestBid.getBid();
		
		if(CreateAll.SOLR_ENABLED) db.addToSolr(itemID, name, description, currentBidPrice, endDate);
		
		return true;
	}
	
	
	public static boolean remakeTable(DBPopulator db){
		db.dropAddQuestions();
		db.dropAddBids();
		return db.dropAddItems();
	}
	
	public boolean configure(){
		
		
		return true;
	}
	
	public void finished(){
		this.finishedLatch.countDown();
	}

	private Date startDate(){
		return new Date(CreateAll.START_TIME - Math.round(Math.random()*100000000l));
	}
	
	private Date endDate(){
		return new Date(CreateAll.START_TIME  + Math.round(Math.random()*40000000000l));
	}

	
	
}
