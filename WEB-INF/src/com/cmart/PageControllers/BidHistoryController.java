package com.cmart.PageControllers;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalVars;
import com.cmart.util.Bid;
import com.cmart.util.CheckInputs;
import com.cmart.util.Item;
import com.cmart.util.User;

/**
 * This servlet lists a user's bidding history
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

public class BidHistoryController extends PageController{
	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
	private long itemID;
	private String itemURL=null;

	// Structures to hold the DB data
	private Item item=null;
	private ArrayList<Bid> allBids=new ArrayList<Bid>();
	private ArrayList<User>bidders=new ArrayList<User>();

	private String[] bidsJSON=null;

	// Structures to hold the parsed page data

	public BidHistoryController(){
		super();
	}
	

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

		}

		// Calculate how long that took
		super.stopTimerAddParam();
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
			this.item = GlobalVars.DB.getItem(itemID, Boolean.FALSE);
		if(this.item == null && this.itemID > 0)
			this.item=GlobalVars.DB.getOldItem(itemID, Boolean.FALSE);

		if(this.item!=null){
			allBids=GlobalVars.DB.getBids(itemID);
		}
		
		// Calculate how long that took
		super.stopTimerAddProcessing();
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
			//allBids=item.getAllBids();
			
			for(int i=0;i<allBids.size();i++){
				bidders.add(allBids.get(i).getBidder());
			}
			
			//if(this.item.)
			this.itemURL="<a href=\"./viewitem?userID=" + this.userID + "&authToken=" + this.authToken + "&itemID=" + this.item.getID() + "\">" + "Return to View Item Page" + "</a>";
			
			
			
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
		
		// Get the item from the database
		if(this.item == null && this.itemID > 0)
			this.item = GlobalVars.DB.getItem(itemID, Boolean.FALSE);
		if(this.item == null && this.itemID > 0)
			this.item=GlobalVars.DB.getOldItem(itemID, Boolean.FALSE);

		if(this.item!=null){
			allBids=GlobalVars.DB.getBids(itemID);
		}

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

		if (this.allBids != null) {
			this.bidsJSON=new String[this.allBids.size()];
			for (int i = 0; i < this.bidsJSON.length; i++) {
				this.bidsJSON[i] = this.allBids.get(i).toJSON();
			}
		}

		super.stopTimerAddProcessing();
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
	
	public Item getItem(){
		return this.item;
	}
	
	public ArrayList<Bid> getAllBids(){
		return this.allBids;
	}
	
	public ArrayList<User>getBidders(){
		return this.bidders;
	}
	
	public String getDisguisedName(int bidderIndexID){
		if(bidders!= null && bidders.size() > bidderIndexID && bidderIndexID>=0){
			String name=bidders.get(bidderIndexID).getUsername();
			StringBuffer ret=new StringBuffer("*****");
			ret.replace(0,1,name.substring(0,1));
			ret.replace(ret.length()-1,ret.length(),name.substring(name.length()-1,name.length()));
			return ret.toString();
		}
		
		return null;
	}
	
	public String getItemURL(){
		return this.itemURL;
	}
	
	public String[] getBidsJSON(){
		return this.bidsJSON;
	}
	
	/**
	 * This method is called to setup and run tests using the classes private variables
	 */
	@Sequenic.T2.T2annotation.exclude
	@org.junit.Test
	public void assertTests(){
		// Get a user to test with
		long user1ID = GlobalVars.DB.checkUsernamePassword("contest2", "password2");
		if(user1ID<0){
			GlobalVars.DB.insertUser("contest2", "password2", "contest2@user.com", "user2", "2");
			user1ID = GlobalVars.DB.checkUsernamePassword("contest2", "password2");
		}
		String authToken = GlobalVars.DB.makeNewAuthToken(user1ID);
		
		Date ed1 = new Date(System.currentTimeMillis()+100000000);
		long item1ID = GlobalVars.DB.insertItem(user1ID, "it name 1", "it desc", 1.0, 2.0, 3.0, 1, ed1, 10000000);
		
		// There will be no bids
		getHTML4Data();
		assertTrue("The item should still be null as no item ID", this.item==null);
		assertTrue("There should be no bids", allBids.isEmpty());
		getHTML5Data();
		assertTrue("The item should still be null as no item ID", this.item==null);
		assertTrue("There should be no bids", allBids.isEmpty());
		
		processHTML4();
		processHTML5();
		assertTrue("There should be no bids", allBids.isEmpty());
		
		this.itemID = item1ID;
		
		getHTML4Data();
		assertTrue("The item should be item1ID", this.item.getID()==item1ID);
		assertTrue("There should be no bids", allBids.isEmpty());
		
		this.item=null;
		getHTML5Data();
		assertTrue("The item should be item1ID", this.item.getID()==item1ID);
		assertTrue("There should be no bids", allBids.isEmpty());
		
		// There is a bid
		GlobalVars.DB.insertBid(user1ID, item1ID, 1, 5.0, 10.0);
		getHTML4Data();
		assertTrue("There should be no bids", allBids.size()==1);
		
		allBids.clear();
		getHTML5Data();
		assertTrue("There should be no bids", allBids.size()==1);
	}
}
