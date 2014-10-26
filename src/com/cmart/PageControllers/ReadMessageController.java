package com.cmart.PageControllers;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalVars;
import com.cmart.util.CheckInputs;
import com.cmart.util.Message;

/**
 * This class processes the data for the read message servlet
 * 
 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
 * @version 0.1
 * @since 0.1
 * @date 03/01/2012
 */

public class ReadMessageController extends PageController {
	private static final GlobalVars GV = GlobalVars.getInstance();
	
	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;

	/**
	 * This method checks the page for any input errors that may have come from
	 * Client generator error These would need to be check in real life to stop
	 * users attempting to hack and mess with things The browse page does not
	 * need a username and authToken to use, so they are not strictly checked by
	 * the page
	 * 
	 * @param request
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public void checkInputs(HttpServletRequest request) {
		super.startTimer();

		if (request != null) {
			super.checkInputs(request);

			// Get the userID (if exists), we will pass it along to the next
			// pages
			try {
				this.userID = CheckInputs.checkUserID(request);
			} catch (Error e) {
				if (!errors.contains(e))
					errors.add(e);
			}

			// Get the authToken (if exists), we will pass it along to the next
			// pages
			try {
				this.authToken = CheckInputs.checkAuthToken(request);
			} catch (Error e) {
				if (!errors.contains(e))
					errors.add(e);
			}
		}

		// Calculate how long that took
		super.stopTimerAddProcessing();
	}

	/**
	 * This method get the data needed for the HTML4 page from the database
	 * 
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public void getHTML4Data() {
		/*
		 * Get all of the info needed from the database
		 */
		super.startTimer();

		// Calculate how long that took
		super.stopTimerAddDB();
	}

	/**
	 * This method processes all of the data that was read from the database
	 * such that it is ready to be printed on to the page. We try to do as much
	 * of the page logic here as possible
	 * 
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public void processHTML4() {
		/*
		 * Process the data and make the URLs to be displayed
		 */
		super.startTimer();

		// Calculate how long that took
		super.stopTimerAddProcessing();
	}

	/**
	 * Gets the HTML5 data from the database
	 * 
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public void getHTML5Data() {
		/*
		 * Get all of the info needed from the database
		 */
		super.startTimer();

		// Calculate how long that took
		super.stopTimerAddDB();
	}

	/**
	 * Processes the HTML5 data that is needed to create the page
	 * 
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public void processHTML5() {
		super.startTimer();
		
		super.stopTimerAddProcessing();
	}

	/**
	 * Returns the current userID as a String
	 * 
	 * @return String the userID
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public String getUserIDString() {
		return Long.toString(this.userID);
	}

	/**
	 * Returns the authToken sent to the page
	 * 
	 * @return string the authToken
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public String getAuthTokenString() {
		return this.authToken;
	}

	/**
	 * Returns if a authToken was sent to the page
	 * 
	 * @return boolean if the authToken was present and correct
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public Boolean hasAuthToken() {
		return !(this.authToken == null || this.authToken.equals(EMPTY));
	}

	/**
	 * Returns the list of messages
	 * 
	 * @return ArrayList<Message> the list of messages
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public ArrayList<Message> getMessages() {
		return GV.DB.getMessages(this.userID);
	}

	/**
	 * Returns the list of messages
	 * 
	 * @return String the name of the user who sends the message
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public String getFromName(long fromID) {
		return GV.DB.getFromName(fromID);
	}
}