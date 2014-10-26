package populator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

public class PopulateOldItems implements PopulateFormat{
	private static RandomSelector<Integer, String> startPriceRS;
	private static RandomSelector<Integer, String> buyNowPriceRS;
	private static RandomSelector<Integer, String> titleWordsNumberRS = null;
	private static RandomSelector<String, String> titleWordsRS;
	private static RandomSelector<Integer, String> descriptionWordsNumberRS = null;
	private static RandomSelector<String, String> descriptionWordsRS;
	private static Boolean localPopulated = false;
	
	private static Distribution titleWordLengthDist;
	private static Distribution descriptionWordLengthDist;
	private static Distribution questionLengthDist;
	private static Distribution commentLengthDist;
	
	private static Distribution bidsDist;
	
	private CountDownLatch finishedLatch;
	
	private double leaveComment = 0.3;
	
	private static final int BUY_NOW_PRICE_ATTEMPTS = 10;
	
	public PopulateOldItems(CountDownLatch finishedLatch){
		this.finishedLatch = finishedLatch;
		
		init();
	}
	
	public static void init(){
		if(!localPopulated){
			synchronized(localPopulated){
				if(!localPopulated){
					try {
						startPriceRS = new RandomSelector<Integer, String>(new FileInputStream(CreateAll.START_PRICE_FILE), Integer.class);
						buyNowPriceRS = new RandomSelector<Integer, String>(new FileInputStream(CreateAll.BUY_NOW_PRICE_FILE), Integer.class);
						
						if(CreateAll.ITEM_TITLE_LENGTH_DIST_TYPE<0)
							titleWordsNumberRS = new RandomSelector<Integer, String>(new FileInputStream(CreateAll.TITLE_WORDS_NUMBER_FILE), Integer.class);
						else
							titleWordLengthDist = new Distribution(CreateAll.ITEM_TITLE_LENGTH_DIST_TYPE, CreateAll.ITEM_TITLE_LENGTH_MIN, CreateAll.ITEM_TITLE_LENGTH_MAX, CreateAll.ITEM_TITLE_LENGTH_DIST_MEAN, CreateAll.ITEM_TITLE_LENGTH_DIST_SD, CreateAll.ITEM_TITLE_LENGTH_ALPHA, CreateAll.ITEM_TITLE_LENGTH_LAMBDA);
						
						
						titleWordsRS = new RandomSelector<String, String>(new FileInputStream(CreateAll.TITLE_WORDS_FILE), String.class);
						
						if(CreateAll.ITEM_DESCRIPTION_LENGTH_DIST_TYPE<0)
							descriptionWordsNumberRS = new RandomSelector<Integer, String>(new FileInputStream(CreateAll.DESCRIPTION_WORDS_NUMBER_FILE), Integer.class);
						else
							descriptionWordLengthDist = new Distribution(CreateAll.ITEM_DESCRIPTION_LENGTH_DIST_TYPE, CreateAll.ITEM_DESCRIPTION_LENGTH_MIN, CreateAll.ITEM_DESCRIPTION_LENGTH_MAX, CreateAll.ITEM_DESCRIPTION_LENGTH_DIST_MEAN, CreateAll.ITEM_DESCRIPTION_LENGTH_DIST_SD, CreateAll.ITEM_DESCRIPTION_LENGTH_ALPHA, CreateAll.ITEM_DESCRIPTION_LENGTH_LAMBDA);
						
						descriptionWordsRS = new RandomSelector<String, String>(new FileInputStream(CreateAll.DESCRIPTION_WORDS_FILE), String.class);
						
						questionLengthDist = new Distribution(CreateAll.QUESTIONS_LENGTH_DIST_TYPE, CreateAll.QUESTIONS_LENGTH_MIN, CreateAll.QUESTIONS_LENGTH_MAX, CreateAll.QUESTIONS_LENGTH_DIST_MEAN, CreateAll.QUESTIONS_LENGTH_DIST_SD, CreateAll.QUESTIONS_LENGTH_ALPHA, CreateAll.QUESTIONS_LENGTH_LAMBDA);
						commentLengthDist = new Distribution(CreateAll.COMMENTS_LENGTH_DIST_TYPE, CreateAll.COMMENTS_LENGTH_MIN, CreateAll.COMMENTS_LENGTH_MAX, CreateAll.COMMENTS_LENGTH_DIST_MEAN, CreateAll.COMMENTS_LENGTH_DIST_SD, CreateAll.COMMENTS_LENGTH_ALPHA, CreateAll.COMMENTS_LENGTH_LAMBDA);
						
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					bidsDist = new Distribution(CreateAll.BIDS_DIST_TYPE, CreateAll.BIDS_MIN, CreateAll.BIDS_MAX, CreateAll.BIDS_DIST_MEAN, CreateAll.BIDS_DIST_SD, CreateAll.BIDS_ALPHA, CreateAll.BIDS_LAMBDA);
					
					localPopulated = true;
				}
			}
		}
	}
	
	public boolean makeThing(DBPopulator db, int usersInserted){
		//StringBuffer sql = db.startSQL();
		StringBuffer sql = new StringBuffer();
		
		/*
		 * Make the item
		 */
		double startPrice = getStartingPrice();
		double reservePrice = getReservePrice(startPrice);
		double buyNowPrice = getBuyNowPrice(startPrice, reservePrice);
		int quantity = getQuantity();
		long sellerID = getSellerID(CreateAll.NO_OF_USERS);
		Date startDate = startDate();
		
		long itemID = db.insertOldItem(getName(),
				getDescription(),
				startPrice,
				quantity,
				reservePrice,
				buyNowPrice,
				startDate,
				endDate(),
				sellerID,
				getCategoryID(), sql);
		
		/*
		 * Make the bids for the items
		 */
		int noOfBids = getNoOfBids();
		
		Bid currentBid = new Bid(-1, -1, quantity, startPrice, startPrice, startDate, null, null);
		Bid highestBid = null;
		Bid secondBid = null;
		
		for(int i=0; i<noOfBids; i++){
			Bid temp = makeBid.makeBid(currentBid);
			db.insertOldBid(temp, itemID, sql);
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
		double currentBidPrice=0.0;
		if(highestBid != null){
			currentBidPrice = highestBid.getBid();
			double maxBidPrice = highestBid.getMaxBid();
			
			if(secondBid != null && secondBid.getMaxBid() > currentBidPrice)
				currentBidPrice = secondBid.getMaxBid() + 0.01;
			
			db.updateOldItemMaxBid(itemID, noOfBids, currentBidPrice, maxBidPrice, highestBid.getUserID(), sql);
		}
		
		/*
		 * Add the item to the purchased table, for simplicity we'll just assume that all of each listing was bought by
		 * a single user - only if there was a bid greater than the reserve price
		 */
		if(highestBid != null && highestBid.getMaxBid()>=reservePrice){
			Boolean paid = CreateAll.rand.nextDouble()<0.99 ? Boolean.TRUE : Boolean.FALSE;
			long paidDate = System.currentTimeMillis() - (Math.round(CreateAll.rand.nextDouble()*1000000l));
			db.insertPurchase(itemID, paid, paidDate, highestBid.getUserID(), currentBidPrice, quantity, sql);
			
			// Now the user may leave a comment
			if(CreateAll.rand.nextDouble() < this.leaveComment){
				db.insertComment(highestBid.getUserID(), sellerID, itemID, CreateAll.rand.nextInt(6), endDate().getTime(), getComment(), sql);
			}
		}
		
		db.addOldItem(sql);
		
		return true;
	}
	
	public static boolean remakeTable(DBPopulator db){
		db.dropAddPayments();
		db.dropAddOldBids();
		db.dropAddPurchased();
		db.dropAddComments();
		return db.dropAddOldItems();
	}
	
	public void finished(){
		this.finishedLatch.countDown();
	}
	

	
	public static String getName(){
		if(!localPopulated) init();
		
		int titleNumberOfWords = 0;
		StringBuilder strBuf = new StringBuilder(128);
		
		if (titleWordsNumberRS != null) {
			titleNumberOfWords = titleWordsNumberRS.getRandomUniformKey();
		} else {
			titleNumberOfWords = (int)Math.round(titleWordLengthDist.getNext());
		}
		
		// Get the words
		if(titleNumberOfWords>0){
			if(CreateAll.TITLE_WORDS_UNIFORM)
				strBuf.append(titleWordsRS.getRandomUniformKey());
			else
				strBuf.append(titleWordsRS.getRandomKey());
		}
		for(int i=0; i< titleNumberOfWords-1; i++){
			strBuf.append(" ");
			if(CreateAll.TITLE_WORDS_UNIFORM)
				strBuf.append(titleWordsRS.getRandomUniformKey());
			else
				strBuf.append(titleWordsRS.getRandomKey());
		}
		
		return strBuf.toString();
	}
	
	public static String getComment(){
		if(!localPopulated) init();
		
		int commentNumberOfWords = (int)Math.round(commentLengthDist.getNext());
		StringBuilder strBuf = new StringBuilder(1024);
		
		// Get the words
		if(commentNumberOfWords>0){
			if(CreateAll.DESCRIPTION_WORDS_UNIFORM)
				strBuf.append(descriptionWordsRS.getRandomUniformKey());
			else
				strBuf.append(descriptionWordsRS.getRandomKey());
		}
		for(int i=0; i< commentNumberOfWords-1; i++){
			strBuf.append(" ");
			if(CreateAll.DESCRIPTION_WORDS_UNIFORM)
				strBuf.append(descriptionWordsRS.getRandomUniformKey());
			else
				strBuf.append(descriptionWordsRS.getRandomKey());
		}
		
		return strBuf.toString();
	}
	
	public static String getQuestion(){
		if(!localPopulated) init();
		
		int questionNumberOfWords = (int)Math.round(questionLengthDist.getNext());
		StringBuilder strBuf = new StringBuilder(1024);
		
		// Get the words
		if(questionNumberOfWords>0){
			if(CreateAll.DESCRIPTION_WORDS_UNIFORM)
				strBuf.append(descriptionWordsRS.getRandomUniformKey());
			else
				strBuf.append(descriptionWordsRS.getRandomKey());
		}
		for(int i=0; i< questionNumberOfWords-1; i++){
			strBuf.append(" ");
			if(CreateAll.DESCRIPTION_WORDS_UNIFORM)
				strBuf.append(descriptionWordsRS.getRandomUniformKey());
			else
				strBuf.append(descriptionWordsRS.getRandomKey());
		}
		
		return strBuf.toString();
	}
	
	/**
	 * Get the description of the item
	 * 
	 * @return
	 */
	public static String getDescription(){
		if(!localPopulated) init();
		
		int descriptionNumberOfWords = 0;
		StringBuilder strBuf = new StringBuilder(1024);
		
		if (descriptionWordsNumberRS!=null) {
			descriptionNumberOfWords = descriptionWordsNumberRS.getRandomKey();
		} else {
			descriptionNumberOfWords = (int) Math.round(descriptionWordLengthDist.getNext());	
		}
		
		// Get the words
		if(descriptionNumberOfWords>0){
			if(CreateAll.DESCRIPTION_WORDS_UNIFORM)
				strBuf.append(descriptionWordsRS.getRandomUniformKey());
			else
				strBuf.append(descriptionWordsRS.getRandomKey());
		}
		for(int i=0; i< descriptionNumberOfWords-1; i++){
			strBuf.append(" ");
			if(CreateAll.DESCRIPTION_WORDS_UNIFORM)
				strBuf.append(descriptionWordsRS.getRandomUniformKey());
			else
				strBuf.append(descriptionWordsRS.getRandomKey());
		}
		
		return strBuf.toString();
	}
	
	/**
	 * Get the starting price of the item
	 * 
	 * @return
	 */
	public static double getStartingPrice(){
		if(!localPopulated) init();
		
		if(CreateAll.START_PRICE_UNIFORM)
			return roundCurrency(startPriceRS.getRandomUniformKey());
		else
			return roundCurrency(startPriceRS.getRandomKey());	
	}
	
	
	public static int getQuantity(){
		int quantity = 1;
		
		return quantity;
	}
	
	public static double getReservePrice(double startPrice){
		if(CreateAll.rand.nextDouble()<CreateAll.ITEMS_RESERVE_FRACTION)
			return roundCurrency(startPrice * 2);
		else
			return 0.0;
	}
	
	public static double getBuyNowPrice(double startPrice, double reservePrice){
		if(!localPopulated) init();
		
		if(CreateAll.rand.nextDouble()<CreateAll.ITEMS_BUYNOW_FRACTION){
			if(CreateAll.BUY_NOW_PRICE_UNIFORM)
				return roundCurrency(Math.max(startPrice, reservePrice) * 3);
			else
			{
				double minPrice = Math.max(startPrice, reservePrice);
				
				for(int i=0; i<BUY_NOW_PRICE_ATTEMPTS; i++){
					int bnp = buyNowPriceRS.getRandomKey();
					if(bnp > minPrice) return bnp;
				}
				
				return roundCurrency(minPrice * 3);
			}
		}
		return roundCurrency(0.0);

	}
	
	private Date startDate(){
		return new Date((CreateAll.START_TIME - Math.round(CreateAll.rand.nextDouble()*10000000000l)) - 1000000000l);
	}
	
	private Date endDate(){
		return new Date(CreateAll.START_TIME - Math.round(CreateAll.rand.nextDouble()*1000000000l));
	}
	
	public static long getSellerID(int usersInserted){
		return CreateAll.rand.nextInt(usersInserted-1)+1;
	}
	
	public static int getCategoryID(){
		return PopulateCategories.getRandomCategory();
	}
	
	public static int getNoOfBids(){
		return (int)Math.round(bidsDist.getNext());
	}
	
	public static RandomSelector<String, String> getTitleWordsRS(){
		if(titleWordsRS != null)
			return titleWordsRS;
		
		new PopulateOldItems(null);
		return getTitleWordsRS();
	}
	
	public static double roundCurrency(double num){
		return  (Math.floor(num*100.0 + 0.5) / 100.0);
	}
}
