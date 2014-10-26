package com.cmart.PageControllers;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalErrors;
import com.cmart.Data.GlobalVars;
import com.cmart.util.CheckInputs;
import com.cmart.util.StopWatch;

/**
 * This controller processes the login pages data
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

public class LoginController extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();
	
	// Variables passed in the request
	private String username = null;
	private String password = null;
	private String welcome = null;
	
	// Structures to hold the DB data
	private long userID = -1;
	private String authToken = null;
	
	// Structures to hold the parsed page data
	private String redirectURL = null;
	
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
			
			// Get the username
			this.username = CheckInputs.getParameter(request, "username");
			
			if(username.equals(EMPTY)){
				if(!errors.contains(GlobalErrors.usernameEmpty))
					errors.add(GlobalErrors.usernameEmpty);
			}
			
			// Get the password
			this.password = CheckInputs.getParameter(request, "password");
			
			if(password.equals(EMPTY)){
				if(!errors.contains(GlobalErrors.passwordEmpty))
					errors.add(GlobalErrors.passwordEmpty);
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
		// This class needs nothing from the database to display
		
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
		// This class has nothing to process to display
		
		super.stopTimerAddProcessing();
	}
	
	/**
	 * Gets the HTML5 data from the database
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void getHTML5Data(){
		super.startTimer();
		
		super.stopTimerAddProcessing();
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
	 * This method attempts to login the user. If the username and password are correct it will return the userID
	 * otherwise it returns -1
	 * @return
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public boolean login(){
		if(this.errors.size() == 0){
			this.userID = GV.DB.checkUsernamePassword(this.username, this.password);
			
			// If the username and password is correct we'll create the authToken
			if(this.userID > 0){
				this.authToken = GV.DB.makeNewAuthToken(userID);
				
				// If we cannot create an authToken the user cannot login
				if(this.authToken == null){
					if(!errors.contains(GlobalErrors.cannotAuthenticate))
						errors.add(GlobalErrors.cannotAuthenticate);
					
					return false;
				}
				// If username and password match and we created an authToken then they can login
				else{
					this.createRedirectURL();
					return true;
				}
			}
			else{
				if(!errors.contains(GlobalErrors.usernamePasswordIncorrect))
					errors.add(GlobalErrors.usernamePasswordIncorrect);
			}
		}
		else{
			if(!errors.contains(GlobalErrors.usernamePasswordIncorrect))
				errors.add(GlobalErrors.usernamePasswordIncorrect);
		}
		
		return false;
	}
	
	private void createRedirectURL(){
		this.redirectURL = "./myaccount?userID=" + this.userID + "&authToken=" + this.authToken + this.welcome;
	}
	
	/**
	 * Returns the username sent to the page
	 * 
	 * @return String the username sent to the page
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getUsername(){
		return this.username;
	}
	
	/**
	 * Returns the password sent to the page
	 * 
	 * @return String the password sent to the page
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getPassword(){
		return this.password;
	}
	
	/**
	 * Returns the URL to be redirected to if the user successfully logs in
	 * 
	 * @return String the next URL to redirect to
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getRedirectURL(){
		return this.redirectURL;
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
