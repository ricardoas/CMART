package com.cmart.PageControllers;

import java.util.ArrayList;
import java.util.Timer;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalVars;
import com.cmart.util.CheckInputs;
import com.cmart.util.StopWatch;
import com.cmart.util.User;

/**
 * This is controller is to test the db speed
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

public class DBSpeedTestController extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();
	
	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
	
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
	 */
	public void getHTML4Data() {	
		super.startTimer();
		int loop=100;
		int factorDown=1;
		
		StopWatch total = new StopWatch();
		StopWatch t = new StopWatch();
		
		ArrayList<User> users = GV.DB.getAllUserData(1, 1);
		User user1 = users.get(0);
		User user2 = new User(63,null,null,"User15","User15","User15@ece.cmu.edu", null, null);
		//User user2 = new User(4728,null,null,"User751190","User751190","User751190@ece.cmu.edu", null, null);
		user1=user2;
		
		/*
		 * Get a user by their ID
		 */
		total.start();
		if(user1!=null){
			System.out.print("Reading user with index: ");
			t.start();
			long userID = user1.getID();
			
			for(int i=0; i<loop; i++){
				GV.DB.getUser(userID);
			}
			
			t.stop();
			System.out.println(t.getTimeTaken() + "ms");
		}
		
		/*
		 * Check username and password
		 */
		if(user1!=null){
			System.out.print("Checking by username and password: ");
			t.start();
			String username = user1.getUsername();
			String password = user1.getPassword();
			
			for(int i=0; i<loop; i++){
				GV.DB.checkUsernamePassword(username, password);
			}
			
			t.stop();
			System.out.println(t.getTimeTaken() + "ms");
		}
		
		/*
		 * Set authtoken
		 */
		if(user1!=null){
			System.out.print("Make authToken: ");
			t.start();
			long userID = user1.getID();
			
			for(int i=0; i<loop/factorDown; i++){
				GV.DB.makeNewAuthToken(userID);
			}
			
			t.stop();
			System.out.println(t.getTimeTaken() + "ms");
		}
		
		/*
		 * Check authoken
		 */
		String authToken = GV.DB.makeNewAuthToken(userID);
		if(authToken!=null){
			System.out.print("Check authToken: ");
			t.start();
			long userID = user1.getID();
			
			for(int i=0; i<loop; i++){
				GV.DB.checkAuthToken(userID, authToken);
			}
			
			t.stop();
			System.out.println(t.getTimeTaken() + "ms");
		}
		
		/*
		 * Get item
		 */
		System.out.print("Get item (no img): ");
		t.start();

		for (int i = 0; i < loop; i++) {
			try {
				GV.DB.getItem(3100, false);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		t.stop();
		System.out.println(t.getTimeTaken() + "ms");
		
		/*
		 * Get item
		 */
		System.out.print("Get item (img): ");
		t.start();

		for (int i = 0; i < loop; i++) {
			try {
				GV.DB.getItem(3100, true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		t.stop();
		System.out.println(t.getTimeTaken() + "ms");
		
		/*
		 * Get items from all categories
		 */
		System.out.print("Get from category all: ");
		t.start();

		for (int i = 0; i < loop; i++) {
			try {
				GV.DB.getCategoryItems(0, 0, 25, 0, false, false, new String[0], 0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		t.stop();
		System.out.println(t.getTimeTaken() + "ms");
		
		/*
		 * Get currently biding items
		 */
		if(user1!=null){
		System.out.print("Get from bidding now: ");
		t.start();
		long userID = user1.getID();

		for (int i = 0; i < loop; i++) {
				GV.DB.getCurrentBids(userID, 0);
		}

		t.stop();
		System.out.println(t.getTimeTaken() + "ms");
		}
		
		/*
		 * Get currently selling items
		 */
		if(user1!=null){
			System.out.print("Get currently selling: ");
			t.start();
			long userID = user1.getID();
	
			for (int i = 0; i < loop; i++) {
					GV.DB.getCurrentSellingItems(userID, 0);
			}
	
			t.stop();
			System.out.println(t.getTimeTaken() + "ms");
		}
		
		total.stop();
		System.out.println("Finished " + total.getTimeTaken() + "ms");
		// Calculate how long that took
		super.stopTimerAddProcessing();
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
