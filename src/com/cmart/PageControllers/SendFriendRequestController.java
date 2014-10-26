package com.cmart.PageControllers;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalErrors;
import com.cmart.Data.GlobalVars;
import com.cmart.util.CheckInputs;

/**
 * This controller is for sending FriendRequests
 * 
 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
 * @version 0.1
 * @since 0.1
 * @date 03/01/2012
 */

public class SendFriendRequestController extends PageController {
	private static final GlobalVars GV = GlobalVars.getInstance();

	// Variables passed in the friend request
	private long userID = -1;
	private String authToken = null;
	private String user = null;
	private String FriendRequest = null;

	// Structures to hold the parsed page data
	private String redirectURL = null;

	/**
	 * This method checks the page for any input errors that may have come from
	 * Client generator error These would need to be check in real life to stop
	 * users attempting to hack and mess with things
	 * 
	 * @param FriendRequest
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

			// Get the user
			try {
				this.user = CheckInputs.checkUsername(request);
			} catch (Error e) {
				if (!errors.contains(e))
					errors.add(e);
			}

			// Get the FriendRequest
			try {
				this.FriendRequest = CheckInputs.checkFriendRequest(request);
			} catch (Error e) {
				if (!errors.contains(e))
					errors.add(e);
			}
		}

		// Calculate how long that took
		super.stopTimerAddParam();
	}

	/**
	 * This method get the data needed for the HTML4 page from the database
	 * 
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public void getHTML4Data() {
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

		// Calculate how long that took
		super.stopTimerAddProcessing();
	}

	public String getRedirectURL() {
		redirectURL = "confirmfriendrequest?userID=" + this.userID
				+ "&authToken=" + this.authToken;
		return this.redirectURL;
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
	 * Returns true if the friend request is sent successfully
	 * 
	 * @return boolean true if successful
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public boolean insertFriendRequest() {
		if (this.errors.size() == 0)
			if (GV.DB.checkAuthToken(this.userID, this.authToken)) {
				if (this.errors.size() == 0)
					return GV.DB.insertFriendRequest((long) userID, user,
							FriendRequest);
			} else if (!errors.contains(GlobalErrors.incorrectAuthToken))
				errors.add(GlobalErrors.incorrectAuthToken);
		return false;
	}
}
