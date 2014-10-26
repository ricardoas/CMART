package com.cmart.PageControllers;

import static org.junit.Assert.*;

import com.cmart.util.Image;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalErrors;
import com.cmart.Data.GlobalVars;
import com.cmart.util.CheckInputs;
import com.cmart.util.Item;
import com.cmart.util.StopWatch;
import com.cmart.util.User;
import com.cmart.util.Comment;
import com.cmart.util.Question;

/**
 * This conteoller is used for displaying items
 * 
 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
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

public class ViewItemController extends PageController{
	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
	private long itemID;
	private double bid = 0.0;
	private double maxBid = 0.0;
	private int quantity = 1;

	private boolean itemCurrentBidRequest = false;
	
	// Structures to hold the DB data
	private Item item = null;
	private User seller = null;

	private ArrayList<Question> questions = null;
	private ArrayList<Comment> comments = null;
	
	// Structures to hold the parsed page data
	private String redirectURL = null;
	private boolean isOld = false;
	private boolean canBuyNow = false;
	private String sellerURL = null;
	private String biddingHistoryURL=null;
	private String rating=null;
	private String thumbnailURL = null;
	private String[] imageURLs = null;
	private String[] imageStripURLs = null;
	private String reserveMet = null;

	private boolean isViewerTheSeller = false;
	private String[] askUsers = null;
	private String[] usersCommentFrom = null;
	private double itemCurrentBid = -1;
	
	/**
	 * This method checks the page for any input errors that may have come from Client generator error
	 * These would need to be check in real life to stop users attempting to hack and mess with things
	 * 
	 * @param request
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void checkInputs(HttpServletRequest request){
		super.startTimer();

		if(request != null){
			super.checkInputs(request);

			//System.out.println("viewitem controller checkin: " + request.getParameter("itemID"));
			//System.out.println("viewitem controller checkin: " + request.getQueryString());
			// Get the userID (if exists), we will pass it along to the next pages
			try{
				this.userID = CheckInputs.checkUserID(request);
			}
			catch(Error e){	}

			// Get the authToken (if exists), we will pass it along to the next pages
			try{
				this.authToken = CheckInputs.checkAuthToken(request);
			}
			catch(Error e){	}

			// Get the itemID 
			try{
				this.itemID = CheckInputs.checkItemID(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);

				this.itemID = -1;
			}

			// Get the bid amount
			try{
				this.bid = CheckInputs.checkBid(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);

				this.bid = 0.0;
			}

			// Get the max bid amount
			try{
				this.maxBid = CheckInputs.checkMaxBid(request);
				
				if(maxBid<bid) this.maxBid=bid;
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);

				this.maxBid = this.bid;
			}

			// Get the quantity
			try{
				this.quantity = CheckInputs.checkQuantity(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);

				this.quantity = 1;
			}

			// Check if this is an AJAX price update request
			this.itemCurrentBidRequest = CheckInputs.checkItemCurrentBid(request);
						
			
			// Get if the item is old
			this.isOld = CheckInputs.checkIsOld(request);
			
		}

		// Calculate how long that took
		super.stopTimerAddParam();
	}

	/**
	 * This method get the item price for item price request in AJAX
	 * 
	 * @author Bo (bol1@andrew.cmu.edu) 
	 */
	public void getXMLData(){
		super.startTimer();
		
		// Get the item from the database
		if(this.item == null && this.itemID > 0)
			this.item = GlobalVars.DB.getItem(itemID, Boolean.FALSE);
		
		// Calculate how long that took
		super.stopTimerAddDB();
	}
	
	/**
	 * This method processes all of the data that was read from the database such that it is ready to be printed
	 * on to the page for AJAX use. We try to do as much of the page logic here as possible
	 * 
	 * @author Bo (bol1@andrew.cmu.edu)
	 */
	public void processXML() {
		super.startTimer();
		
		if(this.item != null){
			if(item.getEndDate().before(Calendar.getInstance().getTime()))
				this.isOld = true;
			this.itemCurrentBid = this.item.getMaxCurrentBidStartPrice();
		}
		
		// Calculate how long that took
		super.stopTimerAddProcessing();
	}
	
	/**
	 * This method get the data needed for the HTML4 page from the database
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void getHTML4Data() {	
		super.startTimer();
		
		// Get the item from the database
		if(this.item == null && this.itemID > 0)
			if(!this.isOld)
				this.item = GlobalVars.DB.getItem(itemID, Boolean.TRUE);
			else
				this.item = GlobalVars.DB.getOldItem(itemID, Boolean.TRUE);

		// Get the public user details for the seller
		if(this.item != null)
			this.seller = GlobalVars.DB.getPublicUser(this.item.getSellerID());

		// Get all of the questions in the category
		if(this.itemID > 0){
			try{
				questions = GlobalVars.DB.getQuestions(this.itemID);
			}
			catch(Exception e){
					errors.add(new Error("ViewItemControl: getHTML4Data: Could not read from data base when getting the questions", e));
			}
		}
		
		if(questions != null && questions.size() > 0){
			int length = questions.size();
			this.askUsers = new String[questions.size()];
			
			for(int i = 0; i < length; i++){
				User u = GlobalVars.DB.getUser(questions.get(i).getFromUserID());
				if(u!=null) askUsers[i] = u.getUsername();
				else askUsers[i] = "unavailable";
			}
		}
		
		// Get all of the comments in the category
		if(itemID > 0){
			try{
				comments = GlobalVars.DB.getComments(this.itemID);
			}
			catch(Exception e){
					errors.add(new Error("ViewItemControl: getHTML4Data: Could not read from data base when getting the comments", e));
			}
		}
		
		if(comments != null && comments.size() != 0){
			int length = comments.size();
			this.usersCommentFrom = new String[comments.size()];
			
			for(int i = 0; i < length; i++){
				User u = GlobalVars.DB.getUser(comments.get(i).getFromUserID());
				if(u != null) usersCommentFrom[i] = u.getUsername();
				else usersCommentFrom[i] = "unavailable";
			}
		}
		
		// Calculate how long that took
		super.stopTimerAddDB();
	}

	/**
	 * This method processes all of the data that was read from the database such that it is ready to be printed
	 * on to the page. We try to do as much of the page logic here as possible
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void processHTML4() {
		super.startTimer();

		if(this.item != null){
			// See if the item is an old item
			if(item.getEndDate().before(Calendar.getInstance().getTime()))
				this.isOld = true;

			if(item.getBuyNowPrice() > 0.0)
				this.canBuyNow = true;

			// Get the items thumbnail
			String thumbURL = item.getThumbnailURL();
			if(thumbURL != null && !thumbURL.equals(EMPTY)){
				this.thumbnailURL = "<img src=\"" + GlobalVars.REMOTE_IMAGE_IP + GlobalVars.REMOTE_IMAGE_DIR + thumbURL + "\" height=\"40\" width=\"40\" alt=\"" + this.item.getName() +"\">";
			}

			// Get the items other images
			if(this.item.getImages() != null){
				ArrayList<Image> images = this.item.getImages();
				int length = images.size();

				this.imageURLs = new String[length];
				this.imageStripURLs=new String[length];

				for(int i=0; i<length; i++){
					Image image = images.get(i);
					this.imageURLs[i] = "<img src=\"" + GlobalVars.REMOTE_IMAGE_IP + GlobalVars.REMOTE_IMAGE_DIR + image.getUrl() + "\" width=\"300\" alt=\"" + image.getDescription() +"\">";
					this.imageStripURLs[i] = "<img src=\"" + GlobalVars.REMOTE_IMAGE_IP + GlobalVars.REMOTE_IMAGE_DIR + image.getUrl() + "\" width=\"100\" alt=\"" + image.getDescription() +"\">";
				}
			}

			// Check if the reserve has been met
			if(this.item.getBuyNowPrice() > this.item.getReservePrice())
				this.reserveMet = "The reserve has been met";
			else
				this.reserveMet = "The reserve price has NOT been met";

			// create a url linking to the bidding history
			String bidString=" bids";
			if(item.getNoOfBids()==1)
				bidString=" bid";
			this.biddingHistoryURL="<a href=\"./bidhistory?userID=" + this.userID + "&authToken=" + this.authToken + "&itemID=" + this.item.getID() + "\">" + Integer.toString(this.item.getNoOfBids())+bidString+"</a>";


			// Get the link to the sellers information
			if(this.seller != null){
				this.sellerURL = "<a href=\"./viewuser?userID=" + this.userID + "&authToken=" + this.authToken + "&viewUserID=" + this.seller.getID() + "\">" + this.seller.getUsername() + "</a>";
				this.rating=this.seller.getRating();
			}
			else{
				this.sellerURL = "Not present";
				this.rating="No seller present";
			}
			
			// Check if current viewer is the seller
						if(this.seller != null){
							if(this.seller.getID() == this.userID){
								this.isViewerTheSeller = true;
							}
						}
		}

		// Calculate how long that took
		super.stopTimerAddProcessing();
	}

	/**
	 * Gets the HTML5 data from the database
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void getHTML5Data(){
		super.startTimer();

		//TODO: make SQL method to get item with images

		// Get the item from the database
		if(this.item == null && this.itemID > 0)
			if(!this.isOld)
				this.item = GlobalVars.DB.getItem(itemID, Boolean.TRUE);
			else
				this.item = GlobalVars.DB.getOldItem(itemID, Boolean.TRUE);

		// Get the public user details for the seller
		if(this.item != null)
			this.seller = GlobalVars.DB.getPublicUser(this.item.getSellerID());

		// Calculate how long that took
		super.stopTimerAddDB();
	}

	/**
	 * Processes the HTML5 data that is needed to create the page
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void processHTML5(){
		super.startTimer();

		// Calculate how long that took
		super.stopTimerAddProcessing();
	}
	/**
	 * Attempts to submit the bid to the database. Adds errors to the Error list if there is problems
	 * 
	 * @return
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public boolean submitBid(){
		// Only if we think the bid might get accepted
		if(this.errors.size() == 0){
			if(GlobalVars.DB.checkAuthToken(this.userID, this.authToken)){
				// If the max bid is less than the bid we cannot send
				if(this.maxBid < this.bid){
					if(!errors.contains(GlobalErrors.bidGreaterThanMaxBid))
						errors.add(GlobalErrors.bidGreaterThanMaxBid);
				}
				// We need to hit the database and get the items details for the other checks

				if(this.item == null)
					this.item = GlobalVars.DB.getItem(itemID, Boolean.TRUE);

				// Check that the item is really there
				if(this.item == null){
					if(!errors.contains(GlobalErrors.bidInvalidItem))
						errors.add(GlobalErrors.bidInvalidItem);
				}
				else{
				// Check that the bid amount is more than the current bid and start price
				if(this.bid <= item.getCurrentBid() || this.bid < item.getStartPrice()){
					if(!errors.contains(GlobalErrors.bidLessThanCurrent))
						errors.add(GlobalErrors.bidLessThanCurrent);
				}

				// Check that the item is still running
				if(this.isOld || item.getEndDate().before(Calendar.getInstance().getTime())){
					if(!errors.contains(GlobalErrors.bidOnFinishedAuction))
						errors.add(GlobalErrors.bidOnFinishedAuction);
				}

				// Everything okay, so let the DB insert
				else if(this.errors.size() == 0){
					double price = GlobalVars.DB.insertBid(userID, itemID, quantity, bid, maxBid);
					if(price > 0.0){
						createRedirectURL();
						return true;
					}
					else{
						if(!errors.contains(GlobalErrors.bidLessThanCurrent))
							errors.add(GlobalErrors.bidLessThanCurrent);
					}
				}
				}
			}
			else if(!errors.contains(GlobalErrors.incorrectAuthToken))
				errors.add(GlobalErrors.incorrectAuthToken);
		}

		return false;
	}

	/**
	 * If we successfully insert the bid then we'll need to forward them on to the next page.
	 * We'll create the URL here
	 * 	
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	private void createRedirectURL(){
		//try{
		if(this.item != null)
			this.redirectURL = "./confirmbid?userID=" + this.userID + "&authToken=" + this.authToken +"&itemName=" + this.item.getName() + "&bid=" + this.bid;
		else
			this.redirectURL = "./confirmbid?userID=" + this.userID + "&authToken=" + this.authToken +"&itemName=none" + "&bid=" + this.bid;

		//this.redirectURL = URLEncoder.encode(this.redirectURL, "UTF-8");
		this.redirectURL.replace(" ", "%20");
		//}
		//catch(UnsupportedEncodingException e){
		//	System.err.println("Encode error");
		//}
	}

	/**
	 * Returns the current userID as a String
	 * 
	 * @return String the userID
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getUserIDString(){
		return Long.toString(this.userID);
	}

	/**
	 * Returns the authToken sent to the page
	 * 
	 * @return string the authToken
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getAuthTokenString(){
		return this.authToken;
	}

	/**
	 * Returns the URL to be redirected to if the user successfully bids
	 * 
	 * @return String the next URL to redirect to
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getRedirectURL(){
		return this.redirectURL;
	}

	/**
	 * Returns the item that the user is bidding for
	 * 
	 * @return Item that the user is bidding for
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public Item getItem(){
		return this.item;
	}

	public double getBid(){
		return this.bid;
	}

	public double getMaxBid(){
		return this.maxBid;
	}

	public int getQuantity(){
		return this.quantity;
	}

	public boolean getIsOld(){
		return this.isOld;
	}

	public boolean getCanBuyNow(){
		return this.canBuyNow;
	}

	public String getThumbnailURL(){
		return this.thumbnailURL;
	}

	public String[] getImageURLs(){
		return this.imageURLs;
	}
	public String[] getImageStripURLs(){
		return this.imageStripURLs;
	}

	public String reserveMet(){
		return this.reserveMet;
	}

	public String getSellerURL(){
		return this.sellerURL;
	}

	public String getRating(){
		return this.rating;
	}

	/*public String getSellerUsername(){
		return this.seller.getUsername();
	}*/

	public long getSellerUserID(){
		if(this.seller != null)
			return this.seller.getID();
		else
			return -1l;
	}

	public String getBiddingHistoryURL(){
		return this.biddingHistoryURL;
	}
	
	public Long getItemID(){
		return this.itemID;
	}
	
	/**
	 * Returns the list of Questions
	 * 
	 * @return ArrayList<Question> the list of Questions
	 * @author Bo (bol1@andrew.cmu.edu)
	 */
	public ArrayList<Question> getQuestions(){
		return this.questions;
	}
	
	public boolean getIsViewerTheSeller(){
		return this.isViewerTheSeller;
	}
	
	public String[] getAskUsers(){
		return this.askUsers;
	}
	
	public ArrayList<Comment> getComments(){
		return this.comments;
	}
	
	public String[] getUsersCommentFrom(){
		return this.usersCommentFrom;
	}
	
	public boolean getItemCurrentBidRequest(){
		return this.itemCurrentBidRequest;
	}
	
	public double getItemCurrentBid(){
		return this.itemCurrentBid;
	}
	
	public User getSeller(){
		return this.seller;
	}
	
	/**
	 * This method is called to setup and run tests using the classes private variables
	 */
	@Sequenic.T2.T2annotation.exclude
	@org.junit.Test
	public void assertTests(){
		// Get a user to test with
		long user1ID = GlobalVars.DB.checkUsernamePassword("contest1", "password1");
		if(user1ID<0){
			GlobalVars.DB.insertUser("contest1", "password1", "contest1@user.com", "user1", "1");
			user1ID = GlobalVars.DB.checkUsernamePassword("contest1", "password1");
		}
		String authToken = GlobalVars.DB.makeNewAuthToken(user1ID);
		
		Date ed1 = new Date(System.currentTimeMillis()+100000000);
		long item1ID = GlobalVars.DB.insertItem(user1ID, "it name 1", "it desc", 1.0, 2.0, 3.0, 1, ed1, 10000000);
		
		Date ed2 = new Date(System.currentTimeMillis()-10000);
		long item2ID = GlobalVars.DB.insertItem(user1ID, "it name 2", "it desc", 1.0, 2.0, 3.0, 1, ed2, 10000000);
		
		
		assertTrue("There should be no bids", GlobalVars.DB.getBids(item1ID).isEmpty());
		
		//Things that need to be correct
		/*
		 * this.userID
		 * this.authToken
		 * this.maxBid >= this.bid
		 * this.itemID
		 * this.bid > item.startPrice
		 * this.bid > item.currentBid
		 * item.endDate <= now
		 */
		this.userID = user1ID;
		this.authToken = authToken;
		this.bid = 3.0;
		this.maxBid = 5.0;
		this.itemID = item1ID;
		
		// The username/password is incorrect
		this.userID = 60;
		this.errors.clear();
		assertFalse("The userid / auth is incorrect, should not insert", submitBid());
		this.userID = user1ID;
		this.authToken = "";
		this.errors.clear();
		assertFalse("The userid / auth is incorrect, should not insert", submitBid());
		
		// bid prices bad
		this.userID = user1ID;
		this.authToken = authToken;
		this.bid = 100;
		this.errors.clear();
		assertFalse("The max bid is less than the bid", submitBid());
		this.bid = 0.5;
		this.errors.clear();
		assertFalse("The  bid is less than the start price", submitBid());
		this.bid = 1.0;
		
		// The item is bad
		this.itemID = -10;
		this.errors.clear();
		assertFalse("The item is bad", submitBid());
		
		this.itemID = item2ID;
		this.errors.clear();
		assertFalse("The item has ended", submitBid());
		
		// okay
		this.userID = user1ID;
		this.authToken = authToken;
		this.bid = 1.0; // min price
		this.maxBid = 5.0;
		this.itemID = item1ID;
		this.errors.clear();
		assertTrue("Everything was okay", submitBid());
		
		// out bid ourselves
		this.bid = 10.0;
		this.maxBid = 15.0;
		this.errors.clear();
		assertTrue("Everything was okay", submitBid());
		
		// cannot re-bid the same
		this.bid = 10.0;
		this.maxBid = 15.0;
		this.errors.clear();
		assertFalse("The bid is the current price", submitBid());
		
		// Cannot bid less
		this.bid = 8.0;
		this.maxBid = 17.0;
		this.errors.clear();
		assertFalse("Bid must be more than the current bid", submitBid());
		
		// We can bid more, even if we do not win due to max bid
		this.bid = 11.0;
		this.maxBid = 12.0;
		this.errors.clear();
		assertTrue("Everything was okay", submitBid());
	}
}
