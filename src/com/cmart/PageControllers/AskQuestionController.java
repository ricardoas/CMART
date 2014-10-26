package com.cmart.PageControllers;

import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import com.cmart.Data.Error;
import com.cmart.Data.GlobalErrors;
import com.cmart.Data.GlobalVars;
import com.cmart.util.CheckInputs;
import com.cmart.util.Item;
import com.cmart.util.Question;

public class AskQuestionController extends PageController{
	private long userID = -1;
	private String authToken = null;
	private String question = null;
	private long itemID;
	
	// Structures to hold the DB data
	private Item item = null;
	
	
	// Structures to hold the parsed page data
	private String redirectURL = null;
	private boolean isOld = false;
	private Question questionDB;
	
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
			// Get the itemID 
			
			try{
				this.itemID = CheckInputs.checkItemID(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				
				this.itemID = -1;
			}
			
			// Get the content
			try{
				this.question = CheckInputs.checkQuestion(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				this.question = "";
			}
			
			
			
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
		
		// Get the item from the database
		if(this.item == null && this.itemID > 0)
			this.item = GlobalVars.DB.getItem(itemID, Boolean.FALSE);
				
		// Calculate how long that took
		super.stopTimerAddProcessing();
	}

	/**
	 * This method processes all of the data that was read from the database such that it is ready to be printed
	 * on to the page. We try to do as much of the page logic here as possible
	 * 
	 * @author Andy (andrewtu@cmu.edu) Bo (bol1@andrew.cmu.edu)
	 */
	public void processHTML4() {
		super.startTimer();
		
		if(this.item != null){
			// See if the item is an old item
			if(item.getEndDate().before(Calendar.getInstance().getTime()))
				this.isOld = true;
		}
		
		// Calculate how long that took
		super.stopTimerAddProcessing();
	}
	
	/**
	 * Gets the HTML5 data from the database
	 *  
	 * @author Andy (andrewtu@cmu.edu) Bo (bol1@andrew.cmu.edu)
	 */
	public void getHTML5Data(){
		super.startTimer();
		
		// Get the item from the database
		if(this.item == null && this.itemID > 0)
			this.item = GlobalVars.DB.getItem(itemID, Boolean.FALSE);
		
		// Calculate how long that took
		super.stopTimerAddDB();
	}
	
	/**
	 * Processes the HTML5 data that is needed to create the page
	 * 
	 * @author Andy (andrewtu@cmu.edu) Bo (bol1@andrew.cmu.edu)
	 */
	public void processHTML5(){
		super.startTimer();
		
		if(this.item != null){
			// See if the item is an old item
			if(item.getEndDate().before(Calendar.getInstance().getTime()))
				this.isOld = true;
		}
		
		super.stopTimerAddProcessing();
	}
	
	/**
	 * Attempts to submit the question to the database. Adds errors to the Error list if there is problems
	 * 
	 * @return
	 * @author Andy (andrewtu@cmu.edu) Bo (bol1@andrew.cmu.edu)
	 */
	public boolean submitQuestion(){
		// Only if we think the question might get accepted
		if(this.errors.size() == 0){
			if(GlobalVars.DB.checkAuthToken(this.userID, this.authToken)){
	
				// We need to hit the database and get the items details for the other checks
				if(this.item == null)
					this.item = GlobalVars.DB.getItem(itemID, Boolean.FALSE);
					
				// Check that the item is really there
				if(this.item == null){
					if(!errors.contains(GlobalErrors.questionInvalidItem))
						errors.add(GlobalErrors.questionInvalidItem);
					}
				
				if(this.question==null || this.question.equals("")){
					if(!errors.contains(GlobalErrors.questionEmpty))
						errors.add(GlobalErrors.questionEmpty);
					}
				
				// Check that the item is still running 
				/*
				if(this.isOld || item.getEndDate().before(Calendar.getInstance().getTime())){
					if(!errors.contains(GlobalErrors.questionOnFinishedAuction))
						errors.add(GlobalErrors.questionOnFinishedAuction);
				}
				*/
				
				// Everything okay, so let the DB insert
				if(this.errors.size() == 0){
					Date date=new Date(System.currentTimeMillis());
					long qID=GlobalVars.DB.insertQuestion(userID, this.item.getSellerID(), itemID, date, question);
					this.questionDB=new Question(qID,userID,this.item.getSellerID(),itemID,true,-1, date,question);
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
	 * If we successfully insert the question then we'll need to forward them on to the browsing item.
	 * We'll create the URL here
	 * 	
	 * @author Andy (andrewtu@cmu.edu) Bo (bol1@andrew.cmu.edu)
	 */
	private void createRedirectURL(){
		//try{
			this.redirectURL = "./viewitem?userID=" + this.userID + "&authToken=" + this.authToken + "&itemID=" + this.itemID;
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
	 * Returns the URL to be redirected to if the user successfully comments
	 * 
	 * @return String the next URL to redirect to
	 * @author Andy (andrewtu@cmu.edu) Bo (bol1@andrew.cmu.edu)
	 */
	public String getRedirectURL(){
		return this.redirectURL;
	}
	
	/**
	 * Returns the item that the user is questioning for
	 * 
	 * @return Item that the user is questioning for
	 * @author Andy (andrewtu@cmu.edu) Bo (bol1@andrew.cmu.edu)
	 */
	public Item getItem(){
		return this.item;
	}
	
	public String getQuestion(){
		return this.question;
	}
	
	
	public boolean getIsOld(){
		return this.isOld;
	}
	
	public Question getQuestionDB(){
		return this.questionDB;
	}
	
	/**
	 * This method is called to setup and run tests using the classes private variables
	 */
	@Sequenic.T2.T2annotation.exclude
	@org.junit.Test
	public void assertTests(){
		// Get a user to test with
		long user1ID = GlobalVars.DB.checkUsernamePassword("contest1", "password1");
		if(user1ID<0){
			GlobalVars.DB.insertUser("contest1", "password1", "contest1@user.com", "user1", "1");
			user1ID = GlobalVars.DB.checkUsernamePassword("contest1", "password1");
		}
		String authToken = GlobalVars.DB.makeNewAuthToken(user1ID);
		
		Date ed1 = new Date(System.currentTimeMillis()+100000000);
		long item1ID = GlobalVars.DB.insertItem(user1ID, "it name 1", "it desc", 1.0, 2.0, 3.0, 1, ed1, 10000000);
		
		getHTML4Data();
		assertTrue("The item should still be null as no item ID", this.item==null);
		getHTML5Data();
		assertTrue("The item should still be null as no item ID", this.item==null);
		
		processHTML4();
		processHTML5();
		
		Item i = new Item();
		this.item = i;
		
		getHTML4Data();
		assertTrue("The item should still be i as no item ID", this.item==i);
		getHTML5Data();
		assertTrue("The item should still be i as no item ID", this.item==i);
		
		this.itemID=item1ID;
		
		getHTML4Data();
		assertTrue("The item should still be item1ID", this.item.getID()==item1ID);
		this.item=i;
		getHTML5Data();
		assertTrue("The item should still be item1ID", this.item.getID()==item1ID);
		
		processHTML4();
		assertTrue("The item should not be old", this.isOld==false);
		processHTML5();
		assertTrue("The item should not be old", this.isOld==false);
		
		assertTrue("Should not insert as userID/auth token incorrect", submitQuestion()==false);
		this.userID=user1ID;
		assertTrue("Should not insert as userID/auth token incorrect", submitQuestion()==false);
		this.authToken = "fake";
		assertTrue("Should not insert as userID/auth token incorrect", submitQuestion()==false);
		this.userID=60;
		assertTrue("Should not insert as userID/auth token incorrect", submitQuestion()==false);
		
		this.authToken = authToken;
		this.userID = user1ID;
		
		this.item=null;
		this.itemID=-5;
		
		assertTrue("Should not insert as item null", submitQuestion()==false && this.errors.contains(GlobalErrors.questionInvalidItem));
		this.errors.clear();
		
		this.itemID=item1ID;
		
		assertTrue("Should not insert as question null", submitQuestion()==false && this.errors.contains(GlobalErrors.questionEmpty));
		this.errors.clear();
		
		this.question = "real q";
		assertTrue("Should insert as item id valid", submitQuestion()==true && this.errors.size()==0);
	}
	
}
