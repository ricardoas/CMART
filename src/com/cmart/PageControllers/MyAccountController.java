package com.cmart.PageControllers;

import java.util.ArrayList;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalErrors;
import com.cmart.Data.GlobalVars;
import com.cmart.util.Bid;
import com.cmart.util.CheckInputs;
import com.cmart.util.Comment;
import com.cmart.util.Item;
import com.cmart.util.Purchase;
import com.cmart.util.Question;
import com.cmart.util.StopWatch;
import com.cmart.util.User;
/**
 * This controller processes data for the MyAccount servlet
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

public class MyAccountController extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();
	private final static String OLD = "&old=1";

	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
	private String welcome = null;
	private long ts=0;

	// Structures to hold the DB data
	private String username = null;
	private ArrayList<Bid> currentBids = null;
	private ArrayList<Bid> oldBids = null;
	private ArrayList<Purchase> purchasedItems = null;
	private ArrayList<Item> currentSellingItems = null;
	private ArrayList<Item> oldSellingItems = null;
	private ArrayList<User> sellers = new ArrayList<User>();
	private ArrayList<Question> questions=new ArrayList<Question>();
	private ArrayList<Comment> comments=new ArrayList<Comment>();
	// Structures to hold the parsed page data
	private String updateDetailsURL = null;
	private String uploadVideoURL = null;
	private String welcomeMessage = "";
	private String[] currentBidsURLs = null;
	private String[] currentBidsThumbnails = null;
	private String[] oldBidsURLs = null;
	private String[] currentSellingItemURLs = null;
	private String[] oldSellingItemURLs = null;
	private String[] payURLs = null;
	private String[] paidText=null;
	private String[] questionJSON=null;
	private String[] commentJSON=null;


	/**
	 * This method checks the page for any input errors that may have come from Client generator error
	 * These would need to be check in real life to stop users attempting to hack and mess with things
	 * 
	 * @param request
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void checkInputs(HttpServletRequest request){
		super.startTimer();

		if(request != null){
			super.checkInputs(request);

			// Get the userID (if exists), we will pass it along to the next pages
			try{
				this.userID = CheckInputs.checkUserID(request);
			}
			catch(Error e){
				if(!this.errors.contains(e))
					this.errors.add(e);
			}

			// Get the authToken (if exists), we will pass it along to the next pages
			try{
				this.authToken = CheckInputs.checkAuthToken(request);
			}
			catch(Error e){
				if(!this.errors.contains(e))
					this.errors.add(e);
			}

			try{
				String tempTS = CheckInputs.getParameter(request, "ts");
				this.ts = Long.parseLong(tempTS);
			}
			catch(Exception e){
				ts=0;
			}

			// Get if the welcome message should be passed, if anything is passed for welcome we'll display it
			this.welcome = CheckInputs.getParameter(request, "welcome");
			if(!this.welcome.equals(EMPTY)) this.welcome = "&welcome=1";
		}

		// Calculate how long that took
		super.stopTimerAddParam();
	}

	/**
	 * This method get the data needed for the HTML4 page from the database
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void getHTML4Data() {	
		super.startTimer();

		/*
		 * The userID and authToken must match to see personal data, we must check before reading anything from
		 * the database
		 */
		if(GV.DB.checkAuthToken(userID, authToken)){
			// Get the user's username
			this.username = GV.DB.getFirstName(userID);

			// Get the users current bids
			this.currentBids = GV.DB.getCurrentBids(userID, ts);

			// Get the user's previous bids
			this.oldBids = GV.DB.getOldBids(userID, ts);

			// Get the purchased items
			this.purchasedItems = GV.DB.getPurchases(userID, ts);

			// Get the items that the user is selling
			this.currentSellingItems = GV.DB.getCurrentSellingItems(userID, ts);

			// Get the items the user is currently selling
			this.oldSellingItems = GV.DB.getOldSellingItems(userID, ts);
		}
		else{
			if(!this.errors.contains(GlobalErrors.incorrectAuthToken))
				this.errors.add(GlobalErrors.incorrectAuthToken);
		}

		// Calculate how long that took
		super.stopTimerAddDB();
	}

	/**
	 * This method processes all of the data that was read from the database such that it is ready to be printed
	 * on to the page. We try to do as much of the page logic here as possible
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void processHTML4() {
		super.startTimer();

		//System.out.println("(myaccountcontroller): getting current bids");
		/*
		 * Create the links for the items the user is currently bidding on
		 */
		if(this.currentBids != null && this.currentBids.size() > 0){
			int length = this.currentBids.size();
			this.currentBidsURLs = new String[length];
			this.currentBidsThumbnails = new String[length];

			//TODO: things being NULL should not be here. We need to  move all bids and items if something is bought
			for(int i=0; i<length; i++){
				// per item null check
				this.currentBidsURLs[i] = this.makeItemLink(this.currentBids.get(i).getItem(), EMPTY);
				if(this.currentBids.get(i).getItem() == null)
					this.currentBidsThumbnails[i] = "blank.jpg";
				else
					this.currentBidsThumbnails[i] = this.currentBids.get(i).getItem().getThumbnailURL();
			}
		}

		//System.out.println("(myaccountcontroller): getting old bids");
		/*
		 * Create the links for the old items that the user bid on
		 */
		if(this.oldBids != null && this.oldBids.size() > 0){
			int length = this.oldBids.size();
			this.oldBidsURLs = new String[length];

			for(int i=0; i<length; i++)
				this.oldBidsURLs[i] = this.makeItemLink(this.oldBids.get(i).getItem(), OLD);
		}

		//System.out.println("(myaccountcontroller): pay purchases");
		/*
		 * Create the links to pay for purchases
		 */
		if(this.purchasedItems != null && this.purchasedItems.size() > 0){
			int length = this.purchasedItems.size();
			this.payURLs = new String[length];
			this.paidText = new String[length];

			for(int i=0; i<length; i++)
				this.payURLs[i] = this.makeItemLink(this.purchasedItems.get(i).getItem(), OLD);
			for(int i=0; i<length; i++)
				this.paidText[i] = this.purchasedItems.get(i).getPaid() ? "Paid" : "Need to pay";
		}

		//System.out.println("(myaccountcontroller): getting current selling");
		/*
		 * Create the links for the current items that the user is selling
		 */
		if(this.currentSellingItems != null && this.currentSellingItems.size() > 0){
			int length = this.currentSellingItems.size();
			this.currentSellingItemURLs = new String[length];

			for(int i=0; i<length; i++)
				this.currentSellingItemURLs[i] = this.makeItemLink(this.currentSellingItems.get(i), EMPTY);
		}

		//System.out.println("(myaccountcontroller): getting old sold");
		/*
		 * Create the links for the current old items that the user sold
		 */
		if(this.oldSellingItems != null && this.oldSellingItems.size() > 0){
			int length = this.oldSellingItems.size();
			this.oldSellingItemURLs = new String[length];

			for(int i=0; i<length; i++)
				this.oldSellingItemURLs[i] = this.makeItemLink(this.oldSellingItems.get(i), OLD);
		}

		// Create the update details URL
		updateDetailsURL = "<a href=\"./updateuserdetails?userID=" + this.userID + "&authToken=" + this.authToken + "\">Update details</a>";
		uploadVideoURL = "<a href=\"./uploadvideo?userID=" + this.userID + "&authToken=" + this.authToken + "\">Upload New Video</a>";
		// If the user is new we'll give them the welcome message
		if(this.welcome!=null && !this.welcome.equals(EMPTY))
			this.welcomeMessage = "Welcome " + this.username + "!" + " We hope you buy lots of items!";

		// Calculate how long that took
		super.stopTimerAddProcessing();
	}

	/**
	 * Gets the HTML5 data from the database
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void getHTML5Data(){
		super.startTimer();


		/*
		 * The userID and authToken must match to see personal data, we must check before reading anything from
		 * the database
		 */
		try{
			if(GV.DB.checkAuthToken(userID, authToken)){
				// Get the user's username
				this.username = GV.DB.getFirstName(userID);

				// Get the users current bids
				this.currentBids = GV.DB.getCurrentBids(userID, ts);

				// Get the user's previous bids
				this.oldBids = GV.DB.getOldBids(userID, ts);

				// Get the purchased items
				this.purchasedItems = GV.DB.getPurchases(userID, ts);

				// Get the items that the user is selling
				this.currentSellingItems = GV.DB.getCurrentSellingItems(userID, ts);

				// Get the items the user is currently selling
				this.oldSellingItems = GV.DB.getOldSellingItems(userID, ts);

				ArrayList<Long> sellerIDs = new ArrayList<Long>();
				ArrayList<Long> getList = new ArrayList<Long>();

				for (int i = 0; i < this.currentBids.size(); i++) {
					if(this.currentBids.get(i).getItem() != null){
						sellerIDs.add(this.currentBids.get(i).getItem().getSellerID());
						getList.add(this.currentBids.get(i).getItem().getID());
					}
				}

				for (int i = 0; i < this.oldBids.size(); i++) {
					if(this.oldBids.get(i).getItem() != null){
						sellerIDs.add(this.oldBids.get(i).getItem().getSellerID());
						getList.add(this.oldBids.get(i).getItem().getID());
					}
				}

				for (int i=0; i <this.currentSellingItems.size(); i++) {
					sellerIDs.add(this.currentSellingItems.get(i).getSellerID());
					getList.add(this.currentSellingItems.get(i).getID());
				}
				for (int i=0; i <this.oldSellingItems.size(); i++) {
					sellerIDs.add(this.oldSellingItems.get(i).getSellerID());
					getList.add(this.oldSellingItems.get(i).getID());
				}
				for (int i=0; i < this.purchasedItems.size(); i++) {
					if(this.purchasedItems.get(i).getItem()!=null){
						sellerIDs.add(this.purchasedItems.get(i).getItem().getSellerID());
						getList.add(this.purchasedItems.get(i).getItem().getID());
					}
				}

				questions= GlobalVars.DB.getQuestions(getList);
				comments= GlobalVars.DB.getComments(getList);

				for(Comment c:comments){
					sellerIDs.add(c.getFromUserID());
				}
				for(Question q:questions){
					sellerIDs.add(q.getFromUserID());
				}

				HashSet<Long> hs = new HashSet<Long>();
				hs.addAll(sellerIDs);
				sellerIDs.clear();
				sellerIDs.addAll(hs);
				sellers = GlobalVars.DB.getUsers(sellerIDs);
			}
			else{
				if(!this.errors.contains(GlobalErrors.incorrectAuthToken))
					this.errors.add(GlobalErrors.incorrectAuthToken);
			}
		}catch(Exception e){
			errors.add(new Error("MyAccountControl: getHTML5Data: Could not read from data base when getting the items", e));

		}
		// Calculate how long that took
		super.stopTimerAddDB();
	}

	/**
	 * Processes the HTML5 data that is needed to create the page
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void processHTML5(){
		super.startTimer();


		if (this.questions != null) {
			this.questionJSON=new String[questions.size()];
			for (int i = 0; i < this.questionJSON.length; i++) {
				this.questionJSON[i] = this.questions.get(i).toJSON();
			}
		}

		if (this.comments != null) {
			this.commentJSON=new String[comments.size()];
			for (int i = 0; i < this.commentJSON.length; i++) {
				this.commentJSON[i] = this.comments.get(i).toJSON();
			}
		}


		// Calculate how long that took
		super.stopTimerAddProcessing();
	}

	/**
	 * This makes the hyperlink to the items pages. The old parameter tells the DB to look in the current database
	 * or the old database
	 * 
	 * @param item The item to be linked to
	 * @param old If the item is in the old database
	 * @return String of hyperlink to item
	 * @author Andy (andrewtu@cmu.edu)
	 */
	private String makeItemLink(Item item, String old){
		StringBuffer buf = new StringBuffer();

		// If the item is null, i.e. missing we will just say null
		if(item == null){
			buf.append("Sorry, item missing");
		}
		else{
			// The userID and authToken must be present or we would not be displaying the page
			buf.append("<a href=\"viewitem?userID=");
			buf.append(userID);
			buf.append("&authToken=");
			buf.append(authToken);
			buf.append("&itemID=");
			buf.append(item.getID());
			buf.append(old);
			buf.append("\">");
			buf.append(item.getName());
			buf.append("</a>");
		}
		return buf.toString();
	}

	/**
	 * Returns the current userID as a String
	 * 
	 * @return String the userID
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getUserIDString(){
		return Long.toString(this.userID);
	}

	/**
	 * Returns the authToken sent to the page
	 * 
	 * @return string the authToken
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getAuthTokenString(){
		return this.authToken;
	}

	/**
	 * Gets the welcome message for the user
	 * 
	 * @return String with message for user
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getWelcomeMessage(){
		return this.welcomeMessage;
	}

	/**
	 * Returns the URL to the update user details page
	 * 
	 * @return String URL to update user details page
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getUpdateUserDetailsURL(){
		return this.updateDetailsURL;
	}

	public String getUploadVideoURL(){
		return this.uploadVideoURL;
	}

	/**
	 * Gets the current bids that the user has made
	 * 
	 * @return ArrayList<Bid> of bids the user has made
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public ArrayList<Bid> getCurrentBids(){
		return this.currentBids;
	}

	public ArrayList<User> getSellers(){
		return this.sellers;
	}
	/**
	 * Returns the 
	 * @return
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String[] getCurrentBiddingItemURLs(){
		return this.currentBidsURLs;
	}

	public String[] getCurrentBiddingThumbnails(){
		return this.currentBidsThumbnails;
	}

	/**
	 * Returns the bids that the user has previously made
	 * 
	 * @return ArrayList<Bid> that the user has previously made
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public ArrayList<Bid> getOldBids(){
		return this.oldBids;
	}

	public ArrayList<Purchase> getPurchases(){
		return this.purchasedItems;
	}

	/**
	 * Returns the URLs to link to the items that the user previous bid on
	 * 
	 * @return String[] of URLs to previously bid on items
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String[] getOldBiddingItemURLs(){
		return this.oldBidsURLs;
	}

	/**
	 * Returns the items that the user is currently selling
	 * 
	 * @return ArrayList<Item> of items that the user is currently selling
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public ArrayList<Item> getCurrentSellingItems(){
		return this.currentSellingItems;
	}

	/**
	 * Returns the URLs to the items that the user is selling
	 * 
	 * @return String[] of URLs to the items that the user is currently selling
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String[] getCurrentSellingItemURLs(){
		return this.currentSellingItemURLs;
	}

	/**
	 * Returns the old items that the user sold
	 * 
	 * @return ArrayList<Item> of items that the user previously sold
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public ArrayList<Item> getOldSellingItems(){
		return this.oldSellingItems;
	}

	/**
	 * Returns the URLs to link to items that the user previously sold
	 * 
	 * @return String[] of URLs that the user previously sold
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String[] getOldSellingItemURLs(){
		return this.oldSellingItemURLs;
	}

	public String[] getPayURLs(){
		return this.payURLs;
	}
	public String[] getPaidText(){
		return this.paidText;
	}
	public String[] getQuestionJSON(){
		return this.questionJSON;
	}
	public String[] getCommentJSON(){
		return this.commentJSON;
	}
}
