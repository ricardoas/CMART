package com.cmart.PageControllers;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalErrors;
import com.cmart.Data.GlobalVars;
import com.cmart.util.CheckInputs;
import com.cmart.util.WallPost;

/**
 * This class processes the data for the wall servlet
 * 
 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
 * @version 0.1
 * @since 0.1
 * @date 03/01/2012
 */

public class GetWallPostController extends PageController {
	private static final GlobalVars GV = GlobalVars.getInstance();

	// Variables passed in the request
	private long userID = -1l;
	private String authToken = null;
	private long toID = -1;
	private long replyID = -1;
	private String post = null;
	private String reply = null;
	
	// Structures to hold the DB data
	private ArrayList<WallPost> wallposts = null;

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

			// Get the toID
			try {
				this.toID = CheckInputs.checkToID(request);
			} catch (Error e) {
				if (!errors.contains(e))
					errors.add(e);
			}

			// Get the reply id
			try {
				this.replyID = CheckInputs.checkReplyID(request);
			} catch (Error e) {
				if (!errors.contains(e))
					errors.add(e);
			}

			// Get the post
			try {
				this.post = CheckInputs.checkPost(request);
			} catch (Error e) {
				if (!errors.contains(e))
					errors.add(e);
			}

			// Get the reply
			try {
				this.reply = CheckInputs.checkReply(request);
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
		wallposts = GV.DB.getWallPosts(this.toID);
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
		if (GV.DB.areFriends(userID, toID))
			wallposts = GV.DB.getWallPosts(this.toID);
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
	 * Returns the to id sent to the page
	 * 
	 * @return string the to id
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public long getToID() {
		return this.toID;
	}

	/**
	 * Returns the reply id sent to the page
	 * 
	 * @return string the reply id
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public long getReplyID() {
		return this.replyID;
	}

	/**
	 * Returns the name of the user who sends the post
	 * 
	 * @return String the name
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public String getFromName(long userID) {
		return GV.DB.getFromName(userID);
	}

	/**
	 * Returns the wall posts
	 * 
	 * @return ArrayList<WallPost> the list of wall posts
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public ArrayList<WallPost> getWallPosts() {
		return wallposts;
	}

	/**
	 * Returns the replies
	 * 
	 * @return ArrayList<WallPost> the list of replies
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public ArrayList<WallPost> getReplies(long replyID) {
		return GV.DB.getReplies(replyID);
	}

	/**
	 * Returns true if the wall post is sent successfully
	 * 
	 * @return boolean true if successful
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public boolean insertWallPost() {
		if (this.errors.size() == 0)
			if (GV.DB.checkAuthToken(this.userID, this.authToken)) {
				if (this.errors.size() == 0)					
					return GV.DB.insertWallPost((long) userID, toID, post);
			} else if (!errors.contains(GlobalErrors.incorrectAuthToken))
				errors.add(GlobalErrors.incorrectAuthToken);
		return false;
	}

	/**
	 * Returns true if the wall post is sent successfully
	 * 
	 * @return boolean true if successful
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public boolean insertReply(long replyID) {
		if (this.errors.size() == 0)
			if (GV.DB.checkAuthToken(this.userID, this.authToken)) {
				if (this.errors.size() == 0)
					return GV.DB.insertReply((long) userID, replyID, reply);
			} else if (!errors.contains(GlobalErrors.incorrectAuthToken))
				errors.add(GlobalErrors.incorrectAuthToken);
		return false;
	}

	public String getPost() {
		return this.post;
	}
}