package com.cmart.PageControllers;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalVars;
import com.cmart.util.Address;
import com.cmart.util.CheckInputs;
import com.cmart.util.StopWatch;
import com.cmart.util.User;

/**
 * This is the controller for the statistics page
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

public class GetUsersController extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();

	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
	private int pageNo;
	private int itemsPerPage;
	private long totalUsers;
	private ArrayList<User> users;
	private ArrayList<Address> addresses;

	// Structures to hold the DB data

	// Structures to hold the parsed page data

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

			try{
				pageNo = CheckInputs.checkPageNumber(request);
			}
			catch(Error e){
				pageNo = 0;
				this.errors.add(e);
			}

			try{
				itemsPerPage = CheckInputs.checkItemsPerPage(request);
			}
			catch(Error e){
				itemsPerPage = 0;
				this.errors.add(e);
			}
			
			try{
				totalUsers = CheckInputs.checkTotalUsers(request);
			}
			catch(Error e){
				totalUsers = 0;
				this.errors.add(e);
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

		if(totalUsers==1)
			totalUsers=GlobalVars.DB.getUserCount();
		users = GV.DB.getAllUserData(itemsPerPage, pageNo);
		addresses = new ArrayList<Address>();
		if(users != null)
			for(User u: users){
				if(u != null)
					try {
						addresses.add(GV.DB.getDefaultAddress(u.getID()));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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

		users = GV.DB.getAllUserData(itemsPerPage, pageNo);
		addresses = new ArrayList<Address>();
		if(users != null)
			for(User u: users){
				if(u != null)
					try {
						addresses.add(GV.DB.getDefaultAddress(u.getID()));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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

	public ArrayList<User> getUsers(){
		return this.users;
	}

	public ArrayList<Address> getAddresses(){
		return this.addresses;
	}

	public long getTotalUsers(){
		return this.totalUsers;
	}
}
