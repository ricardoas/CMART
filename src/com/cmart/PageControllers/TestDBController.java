package com.cmart.PageControllers;

import java.sql.Date;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalVars;
import com.cmart.util.Account;
import com.cmart.util.Address;
import com.cmart.util.CheckInputs;
import com.cmart.util.Comment;
import com.cmart.util.Image;
import com.cmart.util.Item;
import com.cmart.util.Question;
import com.cmart.util.StopWatch;
import com.cmart.util.User;

/**
 * This is the controller to test the database
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
@Deprecated
public class TestDBController extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();
	
	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
	// Which tests to include
	private boolean onlyFirstRun = false; // tests the do exact counts, so may not work if there are already DB items
	private boolean strict = false; // tests the can insert data that may reference other invalid data
	private boolean unneeded = false; // tests that check validity of other entities before inserting in to the DB
	private boolean secondGranTS = true; // if item timestamps are second granularity
	private boolean order = false; // The order of returned items must be correct
	
	// Structures to hold the DB data
	
	// Structures to hold the parsed page data

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
			catch(Error e){	}
			
			// Get the authToken (if exists), we will pass it along to the next pages
			try{
				this.authToken = CheckInputs.checkAuthToken(request);
			}
			catch(Error e){	}
			
			
			
		}
		
		// Calculate how long that took
		super.stopTimerAddParam();
	}

	/**
	 * This method get the data needed for the HTML4 page from the database
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 * @throws Exception 
	 */
	public void getHTML4Data() {
		try{
		super.startTimer();
		
		
		int number =1;
		    
		// assert that the absolute value is >= 0
		assert ( number >= 0 && number <= 10 ) : "bad number: " + number;
		
		/*
		 * USER CHECKS
		 */
		// Insert some users
		assert GV.DB.insertUser("username1", "password1", "email1@user.com", "user", "1");
		assert GV.DB.insertUser("username2", "password2", "email2@user.com", "user", "2");
		assert GV.DB.insertUser("username3", "password3", "email3@user.com", "user", "3");
		
		// Insert some bad users
		assert !GV.DB.insertUser(null, "password3", "email3@user.com", "user", "3");
		assert !GV.DB.insertUser("username3", null, "email3@user.com", "user", "3");
		assert !GV.DB.insertUser("username3", "password3", null, "user", "3");
		assert !GV.DB.insertUser("username3", "password3", "email3@user.com", null, "3");
		assert !GV.DB.insertUser("username3", "password3", "email3@user.com", "user", null);
		
		// Check the username and email already exists
		assert GV.DB.usernameExists("username1");
		assert GV.DB.usernameExists(null);
		assert !GV.DB.usernameExists("newuser");
		assert GV.DB.emailExists("email1@user.com");
		assert GV.DB.emailExists(null);
		assert !GV.DB.emailExists("newuser@user.com");
		
		/*
		 * Check login/logout/auth
		 */
		// Check logging in
		assert GV.DB.checkUsernamePassword(null, "password1") == -1;
		assert GV.DB.checkUsernamePassword("username1", null) == -1;
		assert GV.DB.checkUsernamePassword("username1", "password11") == -1;
		assert GV.DB.checkUsernamePassword("username11", "password1") == -1;
		assert GV.DB.checkUsernamePassword("username1", "password1") > 0;
		long user1ID = GV.DB.checkUsernamePassword("username1", "password1");
		
		// Check auth tokens
		assert GV.DB.makeNewAuthToken(-1) == null;
		assert GV.DB.makeNewAuthToken(0) == null;
		if(strict) assert GV.DB.makeNewAuthToken(25) == null; // user does not exist, so cannot be updated
		assert GV.DB.makeNewAuthToken(user1ID) != null;
		
		String auth1 = GV.DB.makeNewAuthToken(user1ID);
		assert !GV.DB.checkAuthToken(-1, auth1);
		assert !GV.DB.checkAuthToken(-1, null);
		assert !GV.DB.checkAuthToken(56, auth1);
		assert !GV.DB.checkAuthToken(user1ID, null);
		assert !GV.DB.checkAuthToken(user1ID, "fgdf");
		assert GV.DB.checkAuthToken(user1ID, auth1);
		
		// Check logout
		assert !GV.DB.logout(-1);
		assert GV.DB.logout(user1ID) && !GV.DB.checkAuthToken(user1ID, auth1);
		if(unneeded) assert !GV.DB.logout(25); // cannot be logged out as it does not exist
		
		/*
		 * Check updating users
		 */
		// Update the users
		long user3ID = GV.DB.checkUsernamePassword("username3", "password3");
		assert !GV.DB.updateUser(-1, "password32", "email32@user.com", "user3", "33");
		assert !GV.DB.updateUser(user3ID, null, "email32@user.com", "user3", "33");
		assert !GV.DB.updateUser(user3ID, "password32", null, "user3", "33");
		assert !GV.DB.updateUser(user3ID, "password32", "email32@user.com", null, "33");
		assert !GV.DB.updateUser(user3ID, "password32", "email32@user.com", "user3", null);
		if(strict) assert !GV.DB.updateUser(25, "password32", "email32@user.com", "user3", "33"); //user doesn't exist
		assert GV.DB.updateUser(user3ID, "password32", "email32@user.com", "user3", "33");
		
		// Check the user's info is correctly stored
		assert GV.DB.getUser(-1) == null;
		assert GV.DB.getUser(999999999999999l) == null; // doesn't exist
		User u1 = GV.DB.getUser(user1ID);
		assert u1.getID()==user1ID && u1.getUsername().equals("username1") && u1.getPassword().equals("password1")
				&& u1.getEmail().equals("email1@user.com") && u1.getFirstName().equals("user")
				&& u1.getLastName().equals("1") && u1.getRating().equals("0");
		
		User u1p = GV.DB.getPublicUser(user1ID);
		assert GV.DB.getPublicUser(-1) == null;
		assert GV.DB.getPublicUser(999999999999999l) == null; // doesn't exist
		assert u1p.getID()==user1ID && u1p.getUsername().equals("username1") && u1p.getRating().equals("0");
		assert u1p.getPassword() == null && u1p.getEmail() == null && u1p.getFirstName()==null
				&& u1p.getLastName()==null;
		
		assert GV.DB.getFirstName(-1) == null;
		assert GV.DB.getFirstName(999999999999999l) == null;
		assert GV.DB.getFirstName(user1ID).equals("user");
		
		/*
		 * Check getting users
		 */
		assert GV.DB.getAllUserData(-10, 0).size()==0; // cannot return negative
		assert GV.DB.getAllUserData(1, -10).size()==0; // cannot return negative page list
		assert GV.DB.getAllUserData(1, 0).size()==1; // get 1
		assert GV.DB.getAllUserData(2, 0).size()==2; // get 2
		if(onlyFirstRun) assert GV.DB.getAllUserData(100, 0).size()==3; // we only have three in there
		
		ArrayList<Long> sellerIDs = new ArrayList<Long>();
		sellerIDs.add(999999999999999l);
		sellerIDs.add(9999999999999999l);
		
		assert GV.DB.getUsers(null).size()==0;
		assert GV.DB.getUsers(sellerIDs).size()==0; // they do not exist
		sellerIDs.add(user1ID);
		assert GV.DB.getUsers(sellerIDs).size()==1;
		sellerIDs.add(user3ID);
		assert GV.DB.getUsers(sellerIDs).size()==2;
		
		/*
		 * Check addresses
		 */
		// Insert
		assert !GV.DB.insertAddress(-1, "st", "tn", "15217-1234", 17, false);
		assert !GV.DB.insertAddress(user1ID, null, "tn", "15217-1234", 17, false);
		assert !GV.DB.insertAddress(user1ID, "st", null, "15217-1234", 17, false);
		assert !GV.DB.insertAddress(user1ID, "st", "tn", null, 17, false);
		assert !GV.DB.insertAddress(user1ID, "st", "tn", "15217-1234", -1, false);
		assert !GV.DB.insertAddress(user1ID, "st", "tn", "15217-1234", 17, null);
		if(strict) assert !GV.DB.insertAddress(25, "st", "tn", "15217-1234", 17, false); // user does not exist
		if(strict) assert !GV.DB.insertAddress(25, "st", "tn", "15217-1234", 45645, false); // state doesn't exist 
		
		assert GV.DB.insertAddress(user1ID, "st", "tn", "15217-1234", 17, false);
		if(onlyFirstRun) assert GV.DB.getDefaultAddress(user1ID) == null; // no default address, may be present from different run
		assert GV.DB.getDefaultAddress(-1) == null;
		
		// Ensure default stored correctly
		assert GV.DB.insertAddress(user1ID, "st2", "tn2", "15217", 1, true);
		assert GV.DB.getDefaultAddress(user1ID) != null;
		Address a1 = GV.DB.getDefaultAddress(user1ID);
		
		if(a1==null) System.out.println("ERROR: we could not read back the default address!!");
		
		if(onlyFirstRun) assert a1.getStreet().equals("st2") && a1.getTown().equals("tn2") && a1.getZip().equals("15217")
			&& a1.getState()==1 && a1.getIsDefault() && a1.getUserID()==user1ID; // default from another time may be there
		
		// Ensure correct is returned
		Address a2 = GV.DB.getAddress(a1.getId());
		assert a1.getId() == a2.getId();
		assert GV.DB.getAddress(-1) == null;
		assert GV.DB.getAddress(999999999999999l) == null;
		
		// Update address
		assert !GV.DB.updateAddress(-1, user1ID, "newst", "newtn", "15218", 2, false);
		//TODO: neither meet the description
		//assert !GV.DB.updateAddress(a1.getId(), user3ID, "newst", "newtn", "15218", 2, false);
		assert !GV.DB.updateAddress(a1.getId(), user1ID, null, "newtn", "15218", 2, false);
		assert !GV.DB.updateAddress(a1.getId(), user1ID, "newst", null, "15218", 2, false);
		assert !GV.DB.updateAddress(a1.getId(), user1ID, "newst", "newtn", null, 2, false);
		assert !GV.DB.updateAddress(a1.getId(), user1ID, "newst", "newtn", "15218", -1, false);
		assert !GV.DB.updateAddress(a1.getId(), user1ID, "newst", "newtn", "15218", 2, null);
		
		assert GV.DB.updateAddress(a1.getId(), user1ID, "newst", "newtn", "15218", 2, false);
		
		Address a3 = GV.DB.getAddress(a1.getId());
		assert a3.getId()==a1.getId() && a3.getStreet().equals("newst") && a3.getTown().equals("newtn")
				&& a3.getZip().equals("15218") && a3.getState()==2;
		
		/*
		 * Test accounts
		 */
		assert GV.DB.getAccount(-1)==null;
		assert GV.DB.getAccount(999999999999999l)==null;
		
		/*
		 * Check categories
		 */
		// Insert some

		//TODO
		
		// Get categories
		
		/*
		 * Check states
		 */
		// Insert some
		
		// Get states
		
		/*
		 * Check bids, items and purchases when everything is empty
		 */
		if(onlyFirstRun){
			assert GV.DB.getItem(-1, true) == null;
			assert GV.DB.getItem(999999999999999l, true) == null;
			assert GV.DB.getOldItem(-1, true) == null;
			assert GV.DB.getOldItem(999999999999999l, true) == null;
			assert GV.DB.getCurrentSellingItems(-1,0).isEmpty();
			assert GV.DB.getCurrentSellingItems(999999999999999l,0).isEmpty();
			assert GV.DB.getCurrentSellingItems(user1ID,0).isEmpty();
			assert GV.DB.getOldSellingItems(-1,0).isEmpty();
			assert GV.DB.getOldSellingItems(999999999999999l,0).isEmpty();
			assert GV.DB.getOldSellingItems(user1ID,0).isEmpty();
			assert GV.DB.getCurrentBids(-1, 0).isEmpty();
			assert GV.DB.getCurrentBids(999999999999999l, 0).isEmpty();
			assert GV.DB.getCurrentBids(user1ID,0).isEmpty();
			assert GV.DB.getOldBids(-1,0).isEmpty();
			assert GV.DB.getOldBids(999999999999999l,0).isEmpty();
			assert GV.DB.getOldBids(user1ID,0).isEmpty();
			assert GV.DB.getPurchases(-1,0).isEmpty();
			assert GV.DB.getPurchases(999999999999999l,0).isEmpty();
			assert GV.DB.getPurchases(user1ID,0).isEmpty();
		}
		
		/*
		 * Check items 
		 */
		// Insert item
		Date ed = new Date(System.currentTimeMillis()+100000000);
		assert GV.DB.insertItem(0, "it name", "it desc", 0.0, 0.0, 0.0, 1, ed, 1)==-1; // userid bad
		assert GV.DB.insertItem(user1ID, null, "it desc", 0.0, 0.0, 0.0, 1, ed, 1)==-1; //item name bad
		assert GV.DB.insertItem(user1ID, "it name", null, 0.0, 0.0, 0.0, 1, ed, 1)==-1; //item description bad
		assert GV.DB.insertItem(user1ID, "it name", "it desc", -1.0, 0.0, 0.0, 1, ed, 1)==-1; //start price bad
		assert GV.DB.insertItem(user1ID, "it name", "it desc", 0.0, -1.0, 0.0, 1, ed, 1)==-1; //reserve price bad
		assert GV.DB.insertItem(user1ID, "it name", "it desc", 0.0, 0.0, -1.0, 1, ed, 1)==-1; //buy now price bad
		assert GV.DB.insertItem(user1ID, "it name", "it desc", 0.0, 0.0, 0.0, 0, ed, 1)==-1; // quantity bad
		assert GV.DB.insertItem(user1ID, "it name", "it desc", 0.0, 0.0, 0.0, 1, null, 1)==-1; //end date bad
		assert GV.DB.insertItem(user1ID, "it name", "it desc", 0.0, 0.0, 0.0, 1, ed, 0)==-1; // category bad
		if(strict) assert GV.DB.insertItem(user1ID, "it name", "it desc", 0.0, 0.0, 0.0, 1, ed, 999999999999l)==-1; // category doesn't exist
		
		long item1ID = GV.DB.insertItem(user1ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, ed, 1);
		assert item1ID > 0;
		
		// Get item, check the item is correct and the images and bids
		Item item1 = GV.DB.getItem(item1ID, true);
		assert item1.getID()==item1ID && item1.getName().equals("it name") && item1.getDescription().equals("it desc")
				&& item1.getStartPrice()==1.0 && item1.getReservePrice()==2.0 && item1.getBuyNowPrice()==3.0
				&& item1.getCategoryID()==1 && (item1.getEndDate().getTime()/1000)==(ed.getTime()/1000);
		
		assert item1.getNoOfBids()==0;
		assert item1.getThumbnailURL().equals("blank.jpg");
		assert item1.getImages().size()==0;
		
		// Update and get the images
		assert !GV.DB.insertThumbnail(-1, "thumb.jpg");
		assert !GV.DB.insertThumbnail(item1ID, null);
		if(strict) assert !GV.DB.insertThumbnail(25, "thumb.jpg"); // item doesn't exist
		assert GV.DB.insertThumbnail(item1ID, "thumb.jpg");
		
		item1 = GV.DB.getItem(item1ID, true);
		System.out.println(item1.getImages().size());
		assert item1.getImages().size()==1;
		
		assert !GV.DB.insertImage(-1, 1, "img1.jpg", "desc1"); //itemid bad
		assert !GV.DB.insertImage(item1ID, -1, "img1.jpg", "desc1"); //position bad
		assert !GV.DB.insertImage(item1ID, 1, null, "desc1"); //url bad
		assert !GV.DB.insertImage(item1ID, 1, "img1.jpg", null); // description bad
		assert GV.DB.insertImage(item1ID, 1, "img1.jpg", "desc1"); //good
		
		item1 = GV.DB.getItem(item1ID, true);
		assert item1.getImages().size()==2;
		
		assert GV.DB.insertImage(item1ID, 3, "img3.jpg", "desc3");
		assert GV.DB.insertImage(item1ID, 2, "img2.jpg", "desc2");
		
		item1 = GV.DB.getItem(item1ID, true);
		assert item1.getImages().size()==4;
		
		//TODO: fix this!
		if(strict) assert item1.getImages().get(0).getUrl().equals("thumb.jpg") && item1.getImages().get(1).getUrl().equals("img1.jpg")
			&& item1.getImages().get(2).getUrl().equals("img2.jpg") && item1.getImages().get(3).getUrl().equals("img3.jpg");
		
		ArrayList<Image> i1img = GV.DB.getItemImages(item1ID);
		assert GV.DB.getItemImages(-1).size()==0;
		assert GV.DB.getItemImages(999999999999999l).size()==0;
		if(strict) assert i1img.get(0).getUrl().equals("thumb.jpg") && i1img.get(1).getUrl().equals("img1.jpg")
			&& i1img.get(2).getUrl().equals("img2.jpg") && i1img.get(3).getUrl().equals("img3.jpg");
		
		assert GV.DB.insertImage(item1ID, 2, "new.jpg", "new");
		
		i1img = GV.DB.getItemImages(item1ID);
		if(strict) assert i1img.get(0).getUrl().equals("thumb.jpg") && i1img.get(1).getUrl().equals("img1.jpg")
		&& i1img.get(2).getUrl().equals("new.jpg") && i1img.get(3).getUrl().equals("img3.jpg");
		
		item1 = GV.DB.getItem(item1ID, false);
		assert item1.getImages().size()==0;
		
		// Get the selling items
		System.out.println(GV.DB.getCurrentSellingItems(user1ID,0).size());
		if(onlyFirstRun) assert GV.DB.getCurrentSellingItems(user1ID,0).size()==1;
		
		/*
		 * Check bidding
		 */
		assert GV.DB.insertBid(-1, item1ID, 1, 0.5, 5)==-1.0; //userid -1
		assert GV.DB.insertBid(user1ID, -1, 1, 0.5, 5)==-1.0; //itemid -1
		assert GV.DB.insertBid(user1ID, 25, 1, 0.5, 5)==-1.0; //item doesn't exist
		assert GV.DB.insertBid(user1ID, item1ID, 0, 0.5, 5)==-1.0; //quantity small
		assert GV.DB.insertBid(user1ID, item1ID, 1, -0.5, 5)==-1.0; //bid negative
		assert GV.DB.insertBid(user1ID, item1ID, 1, 0.5, -5)==-1.0; //max big negative
		assert GV.DB.insertBid(user1ID, item1ID, 1, 0.5, 0.25)==-1.0; //max less than bid	
		assert GV.DB.insertBid(user1ID, item1ID, 1, 0.5, 0.7)==1.0; // less than start price
		
		assert GV.DB.insertBid(user1ID, item1ID, 1, 1.0, 1.5)==1.0; //success
		
		item1 = GV.DB.getItem(item1ID, false);
		assert item1.getCurrentBid()==1.0 && item1.getMaxBid()==1.5; //from the success above
		assert item1.getNoOfBids()>=1; //TODO: if we are strict, we should confirm validity before inserting
		
		assert GV.DB.insertBid(user1ID, item1ID, 1, 0.2, 0.4)==1.0; // less than current bid
		assert GV.DB.insertBid(user1ID, item1ID, 1, 1.2, 1.45)==1.46; // previous bid's max is higher
		assert GV.DB.insertBid(user1ID, item1ID, 1, 1.47, 1.47)==1.48; // previous bid's max is higher
		assert GV.DB.insertBid(user1ID, item1ID, 1, 2.1, 2.5)==2.1; // success, we out bid them
		assert GV.DB.insertBid(user3ID, item1ID, 1, 3.0, 4.0)==3.0; // success
		assert GV.DB.insertBid(user3ID, item1ID, 1, 5.0, 6.0)==5.0; // success, second out bid them
		
		// Get the current bidding
		if(onlyFirstRun) assert GV.DB.getCurrentBids(user1ID,0).size()==1; //because each item is only listed once
		long item2ID = GV.DB.insertItem(user1ID, "it name2", "it desc2", 5.0, 10.0, 0.0, 1, ed, 1);
		assert item2ID>0;
		
		assert GV.DB.insertBid(user3ID, item2ID, 1, 6.0, 8.0)==6.0; //success, 2nd for user 3
		if(onlyFirstRun) assert GV.DB.getCurrentBids(user3ID,0).size()==2;
		
		/*
		 * Check buying
		 */
		Account acc1= new Account(52, "name","oncard","cardno","cvv",new Date(System.currentTimeMillis()));
		assert !GV.DB.buyItemNow(0, item1ID, 1, acc1, a2); // userid
		assert !GV.DB.buyItemNow(user1ID, 0, 1, acc1, a2); //itemid <1
		assert !GV.DB.buyItemNow(user1ID, 25, 1, acc1, a2); //item not real
		assert !GV.DB.buyItemNow(user1ID, item1ID, 1, null, a2); //no account
		assert !GV.DB.buyItemNow(user1ID, item1ID, 1, acc1, null); // no address
		assert !GV.DB.buyItemNow(user1ID, item2ID, 1, acc1, a2); // no buy now price
		
		System.out.println("bids before pur: "+ GV.DB.getCurrentBids(user1ID,0).size());
		
		assert GV.DB.buyItemNow(user1ID, item1ID, 1, acc1, a2); //success buy
		
		// This bids should now have been moved
		if(onlyFirstRun) assert GV.DB.getCurrentBids(user1ID,0).size()==0;
		if(onlyFirstRun) assert GV.DB.getOldBids(user1ID,0).size()==1; // As it only returns the max old bid
		if(onlyFirstRun) assert GV.DB.getCurrentBids(user3ID,0).size()==1; // still bidding for item 2
		if(onlyFirstRun) assert GV.DB.getOldBids(user3ID,0).size()==1; // As it only returns the max old bid, they do have 2
		if(onlyFirstRun) assert GV.DB.getPurchases(user1ID,0).size()==1; // The user bought the item
		if(onlyFirstRun) assert GV.DB.getPurchases(user3ID,0).size()==0; // user3 still has not bought an item
		
		long item3ID = GV.DB.insertItem(user1ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, ed, 1);
		assert item3ID > 0;
		assert GV.DB.buyItemNow(user1ID, item3ID, 1, acc1, a2); //success buy
		
		if(onlyFirstRun) assert GV.DB.getCurrentBids(user1ID,0).size()==0; // currently not bidding
		if(onlyFirstRun) assert GV.DB.getOldBids(user1ID,0).size()==1; // Buying did not cause a bid
		if(onlyFirstRun) assert GV.DB.getPurchases(user1ID,0).size()==2; // The user bought two item
		if(onlyFirstRun) assert GV.DB.getPurchases(user3ID,0).size()==0; // user3 still has not bought an item
		
		/*
		 * Check the item is in the old table
		 */
		item1 = GV.DB.getOldItem(item1ID, true);
		assert item1.getID()==item1ID && item1.getName().equals("it name") && item1.getDescription().equals("it desc")
				&& item1.getStartPrice()==1.0 && item1.getReservePrice()==2.0 && item1.getBuyNowPrice()==3.0
				&& item1.getCategoryID()==1;
		
		assert GV.DB.getOldItem(item2ID, true)==null;
		
		// Check old selling
		if(onlyFirstRun) assert GV.DB.getOldSellingItems(user1ID,0).size()==2;
		if(onlyFirstRun) assert GV.DB.getOldSellingItems(user3ID,0).size()==0;
		if(onlyFirstRun) assert GV.DB.getCurrentSellingItems(user1ID,0).size()==1;
		if(onlyFirstRun) assert GV.DB.getCurrentSellingItems(user3ID,0).size()==0;
		
		/*
		 * Check comments
		 */
		// add comment
		if(onlyFirstRun) assert GV.DB.getComments(item2ID).size()==0; //currently none
		assert GV.DB.insertComment(0, user3ID, item2ID, 5, ed, "comm") == -1; // invalid from
		assert GV.DB.insertComment(user1ID, 0, item2ID, 5, ed, "comm") == -1; // invalid to
		assert GV.DB.insertComment(user1ID, user3ID, 0, 5, ed, "comm") == -1; // invalid item
		assert GV.DB.insertComment(user1ID, user3ID, 0, 5, null, "comm") == -1; // invalid date
		assert GV.DB.insertComment(user1ID, user3ID, item2ID, 5, ed, null) == -1; // invalid comment
		long commid = GV.DB.insertComment(user1ID, user3ID, item2ID, 5, ed, "comm"); // good
		assert commid > 0;
		
		// Get comment
		assert GV.DB.getComments(0).size()==0; // invalid id
		assert GV.DB.getComments(9999999999999999l).size()==0; // not present
		if(onlyFirstRun) assert GV.DB.getComments(item2ID).size()==1; // good
		
		ArrayList<Comment> comms = GV.DB.getComments(item2ID);
		Comment c = comms.get(0);
		
		assert c.getID()==commid && (c.getDate().getTime()/1000)==(ed.getTime()/1000) && c.getFromUserID()==user1ID
				&& c.getToUserID()==user3ID && c.getComment().equals("comm") && c.getItemID()==item2ID;
		
		GV.DB.insertComment(user1ID, user3ID, item2ID, 5, ed, "comm2"); //good
		if(onlyFirstRun) assert GV.DB.getComments(item2ID).size()==2; //good
		
		
		/*
		 * Check questions
		 */
		// add questions
		if(onlyFirstRun) assert GV.DB.getQuestions(item1ID).size() == 0; //none present
		assert GV.DB.insertQuestion(0, item1ID, -1, ed, "quest") == -1; //userID
		assert GV.DB.insertQuestion(user1ID, 0, -1, ed, "quest") == -1; //itemID
		assert GV.DB.insertQuestion(user1ID, item1ID, -1, null, "quest") == -1; //date
		assert GV.DB.insertQuestion(user1ID, item1ID, -1, ed, null) == -1; //questions
		long qid = GV.DB.insertQuestion(user1ID, item1ID, -1, ed, "quest");
		assert qid > 0; //good
		
		// get question
		assert GV.DB.getQuestions(0).size() == 0; //itemid
		assert GV.DB.getQuestions(67).size() == 0; //item no exist
		if(onlyFirstRun) assert GV.DB.getQuestions(item1ID).size() ==1;
		assert GV.DB.getQuestion(0)==null; //id invalid
		assert GV.DB.getQuestion(9999999999999999l)==null; //question not present
		Question q = GV.DB.getQuestion(qid);
		assert q!=null; // good
		assert q.getID()==qid && q.getContent().equals("quest") && q.getFromUserID()==user1ID
				&& q.getItemID()==item1ID && (q.getPostDate().getTime()/1000)==(ed.getTime()/1000); //info correct
		
		assert GV.DB.insertQuestion(user1ID, item1ID, 1, ed, "quest2") > 0; //good
		if(onlyFirstRun) assert GV.DB.getQuestions(item1ID).size() ==2;
		
		/*
		 * answers
		 */
		assert GV.DB.insertAnswer(0, user3ID, item1ID, 1, ed, "ans")==-1; //userID
		assert GV.DB.insertAnswer(user1ID, 0, item1ID, 1, ed, "ans")==-1; //userid
		assert GV.DB.insertAnswer(user1ID, user3ID, 0, 1, ed, "ans")==-1; //item id
		assert GV.DB.insertAnswer(user1ID, user3ID, item1ID, 1, null, "ans")==-1; //date
		assert GV.DB.insertAnswer(user1ID, user3ID, item1ID, 1, ed, null)==-1; //answer
		long ansid = GV.DB.insertAnswer(user1ID, user3ID, item1ID, 1, ed, "ans");
		assert ansid>0;
		
		assert GV.DB.getQuestions(item1ID).size() ==3; // Answers are returned with get questions
		
		/*
		 * Now test searching and sorting
		 * - add some items
		 * - make sure they are returned in the correct order
		 */
		long ts = ed.getTime();
		long item4ID = GV.DB.insertItem(user1ID, "it name4", "it desc", 1.0, 2.0, 3.0, 1, new Date(ts-10000), 1); if(secondGranTS)Thread.sleep(1001);
		long item5ID = GV.DB.insertItem(user1ID, "it name5", "it desc", 1.0, 2.0, 3.0, 1, new Date(ts-15000), 1); if(secondGranTS)Thread.sleep(1001);
		long item6ID = GV.DB.insertItem(user1ID, "it name6", "it desc", 1.0, 2.0, 3.0, 1, new Date(ts-5000), 1); if(secondGranTS)Thread.sleep(1001);
		long item7ID = GV.DB.insertItem(user1ID, "it name7", "it desc", 1.0, 2.0, 3.0, 1, new Date(ts-20000), 1); if(secondGranTS)Thread.sleep(1001); // total of 5 in category 1
		
		GV.DB.insertItem(user1ID, "it name8", "it desc", 1.0, 2.0, 3.0, 1, ed, 2); if(secondGranTS)Thread.sleep(1001);
		GV.DB.insertItem(user1ID, "it name9", "it desc", 1.0, 2.0, 3.0, 1, ed, 2); if(secondGranTS)Thread.sleep(1001);
		
		GV.DB.insertItem(user1ID, "it name10", "it desc", 1.0, 2.0, 3.0, 1, ed, 3);Thread.sleep(5001);
		
		GV.DB.insertBid(user1ID, item4ID, 1, 10.0, 10.0);
		//GV.DB.insertBid(user1ID, item5ID, 1, 10.0, 10.5);
		GV.DB.insertBid(user1ID, item6ID, 1, 5.0, 5.0);
		GV.DB.insertBid(user1ID, item7ID, 1, 100.0, 100.0);
		
		/*
		 * Get category items
		 */
		assert GV.DB.getCategoryItems(-1, 0, 25, 0, false, false, new String[0], 0).size()==0; // category bad
		assert GV.DB.getCategoryItems(1, -1, 25, 0, false, false, new String[0], 0).size()==0; // page no bad
		assert GV.DB.getCategoryItems(1, 0, -1, 0, false, false, new String[0], 0).size()==0; // itemsPP bad
		assert GV.DB.getCategoryItems(1, 0, 25, 0, null, false, new String[0], 0).size()==0; // sort dec bad
		assert GV.DB.getCategoryItems(1, 0, 25, 0, false, null, new String[0], 0).size()==0; // images bad
		assert GV.DB.getCategoryItems(1, 0, 25, 0, false, false, null, 0).size()==0; // hasitems bad
		
		assert GV.DB.getCategoryItems(0, 0, 25, 0, false, false, new String[0], 0).size()>0;
		if(onlyFirstRun) assert GV.DB.getCategoryItems(1, 0, 25, 0, false, false, new String[0], 0).size()==5; // the 4 above and item2
		
		if(order){
			// Order should be earliest end date first (7,5,4,6,2)
			ArrayList<Item> items = GV.DB.getCategoryItems(1, 0, 25, 0, false, false, new String[0], 0);
			assert items.get(0).getName().equals("it name7") && items.get(0).getName().equals("it name5")
				&& items.get(0).getName().equals("it name4") && items.get(0).getName().equals("it name6")
				&& items.get(0).getName().equals("it name2");
			
			// Sort by reverse date
			items = GV.DB.getCategoryItems(1, 0, 25, 1, false, false, new String[0], 0);
		}
		
		
		if(onlyFirstRun) assert GV.DB.getCategoryItems(0, 0, 25, 0, false, false, new String[0], 0).size()==8; // a zero category is any category
		if(onlyFirstRun) assert GV.DB.getCategoryItems(2, 0, 25, 0, false, false, new String[0], 0).size()==2;
		//System.out.println("size " + GV.DB.getCategoryItems(0, 0, 3, 0, false, false, new String[0]).size());
		ArrayList<Item> tempi  = GV.DB.getCategoryItems(0, 0, 3, 0, false, false, new String[0], 0);
		//for(Item i:tempi)
			//System.out.println("id: " + i.getID());
		if(onlyFirstRun) assert GV.DB.getCategoryItems(0, 0, 3, 0, false, false, new String[0], 0).size()==3; // limit 3
		
		//TODO: test has items
		
		/*
		 * Get category itemsID
		 */
		assert GV.DB.getCategoryItemsIDs(-1, 0, 25, 0, false, 0).size()==0; // category bad
		assert GV.DB.getCategoryItemsIDs(1, -1, 25, 0, false, 0).size()==0; // page no bad
		assert GV.DB.getCategoryItemsIDs(1, 0, -1, 0, false, 0).size()==0; // itemsPP bad
		assert GV.DB.getCategoryItemsIDs(1, 0, 25, 0, null, 0).size()==0; // sort dec bad
		
		assert GV.DB.getCategoryItemsIDs(0, 0, 25, 0, false, 0).size()>=8;
		if(onlyFirstRun) assert GV.DB.getCategoryItemsIDs(1, 0, 25, 0, false, 0).size()==5; // good
		//System.out.println("size2 " + GV.DB.getCategoryItemsIDs(0, 0, 25, 0, false).size());
		ArrayList<Long> templ  = GV.DB.getCategoryItemsIDs(0, 0, 25, 0, false, 0);
		//for(Long i:templ)
			//System.out.println("id: " + i);
		if(onlyFirstRun) assert GV.DB.getCategoryItemsIDs(0, 0, 25, 0, false, 0).size()==8; // good
		if(onlyFirstRun) assert GV.DB.getCategoryItemsIDs(0, 0, 3, 0, false, 0).size()==3; // good
		
		if(order){
			// Check order
		}
		
		/*
		 * Get items by ID
		 */
		ArrayList<Long> ids = new ArrayList<Long>();
		assert GV.DB.getItemsByID(ids, 0, false).size()==0; //ids bad
		assert GV.DB.getItemsByID(ids, -1, false).size()==0; //sorting col bad
		assert GV.DB.getItemsByID(ids, 0, null).size()==0;  //sorting order bad
		
		assert GV.DB.getItemsByID(ids, 0, false).size()==0; // none to return
		ids.add(56l);
		assert GV.DB.getItemsByID(ids, 0, false).size()==0; // item not there
		ids.add(item2ID);
		ids.add(item5ID);
		assert GV.DB.getItemsByID(ids, 0, false).size()==2; 
		
		
		if(order){
			
		}
		
		/*
		 * Get text items
		 */
		
		//cannot check order as we don't know which item is 'best'
		
		//assert GV.DB.getCategoryItems(categoryID, page, itemsPP, sortCol, sortDec, images, hasItems);
		
		System.out.println("IT WORKS!!!!");
		
		// Calculate how long that took
		super.stopTimerAddProcessing();
		}
		catch(Exception e){
			e.printStackTrace();
			assert 0>1;
		}
	}

	/**
	 * This method processes all of the data that was read from the database such that it is ready to be printed
	 * on to the page. We try to do as much of the page logic here as possible
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void processHTML4() {
		super.startTimer();
		
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
		
		super.stopTimerAddProcessing();
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
}
