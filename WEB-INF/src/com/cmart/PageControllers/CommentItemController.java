package com.cmart.PageControllers;

import com.cmart.Data.GlobalVars;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalErrors;
import com.cmart.util.CheckInputs;
import com.cmart.util.Comment;
import com.cmart.util.Item;
import com.cmart.util.Question;
import com.cmart.util.StopWatch;
import com.cmart.util.User;

public class CommentItemController extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();

	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
	private String comment = null;
	private int rating = -1;
	private long itemID;
	
	// Structures to hold the DB data
	private Item item = null;
	private User seller = null;
	private Comment commentDB=null;
	
	// for future error check
	private User buyer = null;
	
	// Structures to hold the parsed page data
	private String redirectURL = null;
	private String sellerURL = null;
	private boolean isOld = false;

	/**
	 * This method checks the page for any input errors that may have come from Client generator error
	 * These would need to be check in real life to stop users attempting to hack and mess with things
	 * 
	 * @param request
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com) Bo Li (bol1@andrew.cmu.edu)
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
			
			// Get the comment
			try{
				this.comment = CheckInputs.checkComment(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				this.comment = "";
			}
			
			// Get the item rating
			try{
				this.rating = CheckInputs.checkRating(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				
				this.rating = -1;
			}
		}
		
		// Calculate how long that took
		super.stopTimerAddParam();
	}

	/**
	 * This method get the data needed for the HTML4 page from the database
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com) Bo Li (bol1@andrew.cmu.edu)
	 */
	public void getHTML4Data() {	
		super.startTimer();
		
		// Get the item from the database
		if(this.item == null && this.itemID > 0)
			this.item = GV.DB.getItem(itemID, Boolean.FALSE);
		if(this.item == null && this.itemID > 0)
			this.item=GV.DB.getOldItem(itemID, Boolean.FALSE);
		// Get the public user details for the seller
		if(this.item != null)
			this.seller = GV.DB.getPublicUser(this.item.getSellerID());
		
		// Calculate how long that took
		super.stopTimerAddProcessing();
	}

	/**
	 * This method processes all of the data that was read from the database such that it is ready to be printed
	 * on to the page. We try to do as much of the page logic here as possible
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com) Bo Li (bol1@andrew.cmu.edu)
	 */
	public void processHTML4() {
		super.startTimer();
		
		if(this.item != null){
			// See if the item is an old item
			if(item.getEndDate().before(Calendar.getInstance().getTime()))
				this.isOld = true;
		}
		// Get the link to the sellers information
		if(this.seller != null)
			this.sellerURL = "<a href=\"./viewuser?userID=" + this.userID + "&authToken=" + this.authToken + "&viewUserID=" + this.seller.getID() + "\">" + this.seller.getUsername() + "</a>";
		else
			this.sellerURL = "Not present";
		
		// Calculate how long that took
		super.stopTimerAddProcessing();
	}
	
	/**
	 * Gets the HTML5 data from the database
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com) Bo (bol1@andrew.cmu.edu)
	 */
	public void getHTML5Data(){
		super.startTimer();
		

		// Get the item from the database
		if(this.item == null && this.itemID > 0)
			this.item = GV.DB.getItem(itemID, Boolean.FALSE);
		
		// Get the public user details for the seller
		if(this.item != null)
			this.seller = GV.DB.getPublicUser(this.item.getSellerID());
		
		// Calculate how long that took
		super.stopTimerAddDB();
	}
	
	/**
	 * Processes the HTML5 data that is needed to create the page
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com) Bo (bol1@andrew.cmu.edu)
	 */
	public void processHTML5(){
		super.startTimer();
		if(this.item != null){
			// See if the item is an old item
			if(item.getEndDate().before(Calendar.getInstance().getTime()))
				this.isOld = true;
		}
		// Get the link to the sellers information
		if(this.seller != null)
			this.sellerURL = "<a href=\"./viewuser?userID=" + this.userID + "&authToken=" + this.authToken + "&viewUserID=" + this.seller.getID() + "\">" + this.seller.getUsername() + "</a>";
		else
			this.sellerURL = "Not present";
		
		super.stopTimerAddProcessing();
	}
	
	/**
	 * Attempts to submit the comment to the database. Adds errors to the Error list if there is problems
	 * 
	 * @return
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com) Bo (bol1@andrew.cmu.edu)
	 */
	public boolean submitComment(){
		// Only if we think the comment might get accepted
		if(this.errors.size() == 0){
			if(GV.DB.checkAuthToken(this.userID, this.authToken)){
	
				// We need to hit the database and get the items details for the other checks
				if(this.item == null)
					this.item = GV.DB.getItem(itemID, Boolean.FALSE);
					
				// Check that the item is really there
				if(this.item == null){
					if(!errors.contains(GlobalErrors.commentInvalidItem))
						errors.add(GlobalErrors.commentInvalidItem);
					}
				
				// Check that the item is still running 
				/*
				if(this.isOld || item.getEndDate().before(Calendar.getInstance().getTime())){
					if(!errors.contains(GlobalErrors.commentOnFinishedAuction))
						errors.add(GlobalErrors.commentOnFinishedAuction);
				}
				*/
				
				// Everything okay, so let the DB insert
				//TODO: the sell may not be present, new error message
				long sellerID = userID;
				if(seller!=null){
					sellerID=seller.getID();
				}
				
				if(this.errors.size() == 0){
					Date date=new Date(System.currentTimeMillis());
					long id=GV.DB.insertComment(userID, sellerID,itemID, rating, date ,comment);
					this.commentDB=new Comment(id,userID,this.item.getSellerID(),itemID,rating, date,comment);
					createRedirectURL();
					return true;
					
				}
			}
			else if(!errors.contains(GlobalErrors.incorrectAuthToken))
				errors.add(GlobalErrors.incorrectAuthToken);
		}
		
		return false;
	}
	
	/**
	 * If we successfully insert the comment then we'll need to forward them on to the next page.
	 * We'll create the URL here
	 * 	
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com) Bo (bol1@andrew.cmu.edu)
	 */
	private void createRedirectURL(){
		//try{
			this.redirectURL = "./confirmcomment?userID=" + this.userID + "&authToken=" + this.authToken;
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
	 * Returns the URL to be redirected to if the user successfully comments
	 * 
	 * @return String the next URL to redirect to
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com) Bo (bol1@andrew.cmu.edu)
	 */
	public String getRedirectURL(){
		return this.redirectURL;
	}
	
	/**
	 * Returns the item that the user is commenting for
	 * 
	 * @return Item that the user is commenting for
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com) Bo (bol1@andrew.cmu.edu)
	 */
	public Item getItem(){
		return this.item;
	}
	
	public String getComment(){
		return this.comment;
	}
	
	public int getRating(){
		return this.rating;
	}
	
	public boolean getIsOld(){
		return this.isOld;
	}
	
	
	public String getSellerURL(){
		return this.sellerURL;
	}
	
	/*public String getSellerUsername(){
		return this.seller.getUsername();
	}*/
	
	public long getSellerUserID(){
		if(this.seller!=null)
			return this.seller.getID();
		else return -1l;
	}
	
	public Comment getCommentDB(){
		return this.commentDB;
	}
	
}
