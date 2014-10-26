package com.cmart.PageControllers;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import com.cmart.Data.Error;
import com.cmart.Data.GlobalErrors;
import com.cmart.Data.GlobalVars;
import com.cmart.util.CheckInputs;
import com.cmart.util.Item;
import com.cmart.util.Question;

public class AnswerQuestionController extends PageController{
	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
	private long itemID;
	private String answer = null;
	private long questionID;
	
	// Structures to hold the DB data
	private Question question = null;
	private Question answerDB=null;
	private Item item=null;
	
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
			
			// Get the itemID 
			try{
				this.itemID = CheckInputs.checkItemID(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				
				this.itemID = -1;
			}
			
			// Get the questionID (if exists), we will pass it along to the next pages
			try{
				this.questionID = CheckInputs.checkQuestionID(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				this.questionID = -1;
			}
			
			// Get the answer
			try{
				this.answer = CheckInputs.checkAnswer(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				this.answer = "";
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
		
		// Get the question from the database
		if(this.question == null && this.questionID > 0){
			try{
				this.question = GlobalVars.DB.getQuestion(questionID);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		// Get the item from the database
		if(this.item == null && this.itemID > 0){
			try{
				this.item = GlobalVars.DB.getItem(itemID,false);
			}
			catch(Exception e){
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
		
		// Get the question from the database
		if(this.question == null && this.questionID > 0){
			try{
				this.question = GlobalVars.DB.getQuestion(questionID);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
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
	 * Attempts to submit the answer to the database. Adds errors to the Error list if there is problems
	 * 
	 * @return
	 * @author Andy (andrewtu@cmu.edu) Bo (bol1@andrew.cmu.edu)
	 */
	public boolean submitAnswer(){
		// Only if we think the answer might get accepted
		if(this.errors.size() == 0){
			if(GlobalVars.DB.checkAuthToken(this.userID, this.authToken)){
				if(this.answer==null){
					if(!errors.contains(GlobalErrors.answerNotPresent))
						errors.add(GlobalErrors.answerNotPresent);
				}
				// We need to hit the database and get the questions details for the other checks
				else if(this.question == null && this.questionID > 0){
					try{
						this.question = GlobalVars.DB.getQuestion(questionID);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}	
				// Check that the item is really there
				if(this.question == null){
					if(!errors.contains(GlobalErrors.answerInvalidItem))
						errors.add(GlobalErrors.answerInvalidItem);
					}
				
				// Check that the item is still running 
				/*
				if(this.isOld || item.getEndDate().before(Calendar.getInstance().getTime())){
					if(!errors.contains(GlobalErrors.answerOnFinishedAuction))
						errors.add(GlobalErrors.answerOnFinishedAuction);
				}
				*/
				
				// Everything okay, so let the DB insert
				else if(this.errors.size() == 0){
					Date date=new Date(System.currentTimeMillis());
					long qID=GlobalVars.DB.insertAnswer(question.getFromUserID(), userID, itemID, question.getID(), date, answer);
					this.answerDB=new Question(qID,userID,this.question.getFromUserID(),itemID,false,this.questionID, date,answer);
					
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
	
	
	public Question getQuestion(){
		return this.question;
	}
	
	public Question getAnswerDB(){
		return this.answerDB;
	}
	
	public Item getItem(){
		return this.item;
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
		
		// Get question with null/invalid everything
		this.getHTML4Data();
		assertTrue("The question should still be null", this.question==null);
		this.getHTML5Data();
		assertTrue("The question should still be null", this.question==null);
		
		// Insert answer with bad everything
		assertTrue("Should not be able to insert answer", this.submitAnswer()==false);
		
		// Get question 1
		this.questionID = 1l;
		this.question = null;
		this.getHTML4Data();
		assertTrue("The question should not be null (make sure question id=1 exists)", this.question!=null);
		this.question = null;
		this.getHTML5Data();
		Question q1 = this.question;
		assertTrue("The question should not be null (make sure question id=1 exists)", this.question!=null);
		
		// Get an invalid question
		this.questionID = 9999999999l;
		this.question = null;
		this.getHTML4Data();
		assertTrue("The question should still be null (make sure question id=9999999999l does not exists)", this.question==null);
		this.question = null;
		this.getHTML5Data();
		assertTrue("The question should still be null (make sure question id=9999999999l does not exists)", this.question==null);
		
		// Try to submit an answer with an invalid user id and auth token
		this.errors.clear();
		assertTrue("Should not be able to insert answer with bad user id and auth", this.submitAnswer()==false);
		assertTrue("The bad auth token error should occur", this.errors.contains(GlobalErrors.incorrectAuthToken));
		
		// Try to submit with good userID, but bad auth token
		this.userID = user1ID;
		this.errors.clear();
		assertTrue("Should not be able to insert answer with good user, bad auth", this.submitAnswer()==false);
		assertTrue("The bad auth token error should occur", this.errors.contains(GlobalErrors.incorrectAuthToken));
		
		// Try to submit with no user ID, but good auth token
		this.userID = -1;
		this.authToken = authToken;
		this.errors.clear();
		assertTrue("Should not be able to insert answer bad user id, good auth", this.submitAnswer()==false);
		assertTrue("The bad auth token error should occur", this.errors.contains(GlobalErrors.incorrectAuthToken));
		
		// Try to submit answer with no question ID, but username/auth good
		this.userID = user1ID;
		this.authToken = authToken;
		this.questionID = -1;
		this.question = null;
		this.errors.clear();
		assertTrue("Should not be able to insert answer with no question", this.submitAnswer()==false);
		assertTrue("The invalid answer error should occur", this.errors.contains(GlobalErrors.answerInvalidItem));
		
		// Try to insert answer with null answer
		this.userID = user1ID;
		this.authToken = authToken;
		this.questionID = 1;
		this.question = null;
		this.answer = null;
		this.errors.clear();
		assertTrue("Should not be able to insert answer with null answer and good question id", this.submitAnswer()==false);
		assertTrue("The invalid answer error should occur", this.errors.contains(GlobalErrors.answerNotPresent));
		
		// Try to insert answer with null answer
		this.userID = user1ID;
		this.authToken = authToken;
		this.questionID = -1;
		this.question = q1;
		this.answer = null;
		this.errors.clear();
		assertTrue("Should not be able to insert answer with null answer and good question obj", this.submitAnswer()==false);
		assertTrue("The invalid answer error should occur", this.errors.contains(GlobalErrors.answerNotPresent));
		
		// Try to insert an answer with good question id and answer
		this.userID = user1ID;
		this.authToken = authToken;
		this.questionID = 1;
		this.question = null;
		this.answer = "good";
		this.errors.clear();
		assertTrue("Should have inserted answer", this.submitAnswer()==true);
		assertTrue("No errors should occur", this.errors.isEmpty());
		
		// Try to insert an answer with good question and answer
		this.userID = user1ID;
		this.authToken = authToken;
		this.questionID = -1;
		this.question = q1;
		this.answer = "good";
		this.errors.clear();
		assertTrue("Should have inserted answer", this.submitAnswer()==true);
		assertTrue("No errors should occur", this.errors.isEmpty());
	}
}
