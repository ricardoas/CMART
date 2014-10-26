package com.cmart.PageControllers;

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalErrors;
import com.cmart.Data.GlobalVars;
import com.cmart.util.Account;
import com.cmart.util.Address;
import com.cmart.util.CheckInputs;
import com.cmart.util.Item;
import com.cmart.util.StopWatch;

/**
 * This controller is for buying items
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

public class BuyItemController extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();
	private Boolean buyFailure = Boolean.FALSE;
	
	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
	private long itemID;
	private int quantity;
	private boolean getAddress = false;
	
	private Address address;
	private long addressID = -1;
	private String street = null;
	private String town = null;
	private String zip = null;
	private String state = null;
	
	private Account account;
	private long accountID = -1;
	private String nameOnCard;
	private String creditCardNo;
	private String cvv;
	private String expirationDateString;
	private Date expirationDate;
	//private Boolean cardIsValid;
	
	// Structures to hold the DB data
	private ArrayList<String[]> states = null;
	
	// Structures to hold the parsed page data
	private String[] stateSelections = null;
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
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
			}
			
			// Get the authToken (if exists), we will pass it along to the next pages
			try{
				this.authToken = CheckInputs.checkAuthToken(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
			}
			
			// Get the itemID 
			try{
				this.itemID = CheckInputs.checkItemID(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				
				this.itemID = -1;
			}
			
			// Get the quantity
			try{
				this.quantity = CheckInputs.checkQuantity(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				
				this.quantity = -1;
			}
			
			/*
			 * Get the account information for the payment
			 */
			// Get the accountID
			try{
				this.accountID = CheckInputs.checkAccountID(request);
			}
			catch(Error e){
				// We don't care if the accountID is wrong as the user doesn't have to save the account
				this.accountID = -1;
			}
			
			// Get the name on the card
			this.nameOnCard = CheckInputs.getParameter(request, "nameOnCard");
			if(this.nameOnCard.equals(""))
				if(!this.errors.contains(GlobalErrors.creditCardNoName))
					this.errors.add(GlobalErrors.creditCardNoName);
			
			// Get the credit card number
			this.creditCardNo = CheckInputs.getParameter(request, "creditCardNo");
			
			// Get the cvv code
			this.cvv = CheckInputs.getParameter(request, "cvv");
			
			// Get the expiration date
			this.expirationDateString = CheckInputs.getParameter(request, "expirationDate");
			try{
				this.expirationDate = CheckInputs.checkExpirationDate(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
			}
			
			/*
			 * Get the address information for the payment
			 */
			// Get the AddressID
			try{
				this.addressID = CheckInputs.checkAddressID(request);
			}
			catch(Error e){
				// The user must have a default address, we'll read it later
				this.addressID = -1;
			}
			
			// Get the street
			this.street = CheckInputs.getParameter(request, "street");
			
			if(street.equals(EMPTY)){
				if(!errors.contains(GlobalErrors.streetEmpty))
					errors.add(GlobalErrors.streetEmpty);
			}
			
			// Get the town
			this.town = CheckInputs.getParameter(request, "town");
			
			if(town.equals(EMPTY)){
				if(!errors.contains(GlobalErrors.townEmpty))
					errors.add(GlobalErrors.townEmpty);
			}
			
			// Get the zip
			try{
				this.zip = CheckInputs.checkZip(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				
				// As the zip was not correct we'll fill the box with the user's old, bad data
				this.zip = CheckInputs.getParameter(request, "zip");
			}
			
			// Check the state
			try{
				this.state = CheckInputs.checkState(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				
				// As the state was not correct set a default
				this.state = "0";
			}
			
			// See if we just want to get the users address
			String tmp = CheckInputs.getParameter(request, "getAddress");
			if(tmp.equals("1") || tmp.equals("true"))
				this.getAddress = true;
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
		
		// Check the item is still available
		Item item = GV.DB.getItem(itemID, false);
		if( item == null || (item != null && item.getEndDate().before(new Date(System.currentTimeMillis()))) ){
			if(!errors.contains(GlobalErrors.itemAlreadyGone))
				this.errors.add(GlobalErrors.itemAlreadyGone);
				this.buyFailure = Boolean.TRUE;	
		}
		
		//TODO: check the item item when the page loads
		
		// Get the list of states from the data base
		try{
			this.states = GV.DB.getStates();
		}
		catch(Exception e){
			errors.add(new Error("BuyItemControl: getHTML4data: could not read the states from the database", e));
		}
		
		// If there is an accountID we'll read that account, otherwise we'll get the default account
		if(this.accountID>0){
			try {
				this.account = GV.DB.getAccount(accountID);
			} catch (Exception e) {
				errors.add(new Error("BuyItemControl: getHTML4Data: Could not read the account info from the database", e));
			}
		}
		else{
			//TODO: get default account
		}
		
		// If there is an address, we'll use it, otherwise will use the default address
		if(this.addressID>0){
			try {
				this.address = GV.DB.getAddress(addressID);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			try {
				this.address = GV.DB.getDefaultAddress(this.userID);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Calculate how long that took
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
		
		// Set the page's account information
		if(this.account != null){
			this.accountID = this.account.getAccountID();
			this.nameOnCard = this.account.getNameOnCard();
			this.creditCardNo = this.account.getCreditCardNo();
			this.cvv = this.account.getCVV();
			this.expirationDate = this.account.getExpirationDate();
		}
		
		// Set the page's address information
		if(this.address != null){
			this.addressID = address.getId();
			this.street = address.getStreet();
			this.town = address.getTown();
			this.zip = address.getZip();
			this.state = Integer.toString(address.getState());
		}
		
		/*
		 * Make the state selections
		 */
		int stateNo;
		// If the state is a number and there are states read from the database we can make the list
		try{
			stateNo = Integer.parseInt(this.state);
			
			if(this.states != null){
				this.stateSelections = new String[this.states.size()+1];
				
				// Add the 'please select'
				this.stateSelections[0] = "<option value=\"0\""+ (stateNo==0?" SELECTED":"") + ">Please Select...</option>";
				
				// Add the other states
				int length = this.states.size();
				
				for(int i=0; i<length; i++){
					String[] state = this.states.get(i);
					this.stateSelections[i+1] = "<option value=\"" + state[0] + "\"" + (this.state.equals(state[0])?" SELECTED":"") + ">" + state[1] + " - " + state[2] + "</option>";
				}
			}
			else{
				this.stateSelections = new String[1];
				this.stateSelections[0] = "<option value=\"0\""+ (stateNo==0?" SELECTED":"") + ">Please Select...</option>";
			}
		}
		catch(Exception e){
			if(!errors.contains(GlobalErrors.stateNotAnInteger))
				errors.add(GlobalErrors.stateNotAnInteger);
		}
		
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
		
		// Check the item is still available

				Item item = GV.DB.getItem(itemID, false);
				if( item == null || (item != null && item.getEndDate().before(new Date(System.currentTimeMillis()))) ){
					if(!errors.contains(GlobalErrors.itemAlreadyGone))
						this.errors.add(GlobalErrors.itemAlreadyGone);
						this.buyFailure = Boolean.TRUE;	
				}

				
				// If there is an accountID we'll read that account, otherwise we'll get the default account
				if(this.accountID>0){
					try {
						this.account = GV.DB.getAccount(accountID);
					} catch (Exception e) {
						errors.add(new Error("BuyItemControl: getHTML4Data: Could not read the account info from the database", e));
					}
				}
				else{
					//TODO: get default account
				}
				
				// If there is an address, we'll use it, otherwise will use the default address
				if(this.addressID>0){
					try {
						this.address = GV.DB.getAddress(addressID);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{
					try {
						this.address = GV.DB.getDefaultAddress(this.userID);
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
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void processHTML5(){
		super.startTimer();

		
		// Calculate how long that took
		super.stopTimerAddProcessing();
	}
	
	private Boolean verifyCardNumber(){
		// WOO!! it just wouldn't be made by students if we didn't copy from wikipedia!!
		// TODO: Andy: Check this algorithm is correct
		// ref: http://en.wikipedia.org/wiki/Luhn_algorithm
		Boolean correct1 = Boolean.FALSE;
		Boolean correct2 = Boolean.FALSE;
		final int[][] sumTable = {{0,1,2,3,4,5,6,7,8,9},{0,2,4,6,8,1,3,5,7,9}};
		
	    int sum = 0, flip = 0;
	    
	    if(this.creditCardNo!=null && this.creditCardNo.length()>=14)
	    try{
		    for (int i = this.creditCardNo.length() - 1; i >= 0; i--) {
		      sum += sumTable[flip++ & 0x1][Character.digit(this.creditCardNo.charAt(i), 10)];
		    }
		    
		    if(!(sum % 10 == 0)){
		    	if(!errors.contains(GlobalErrors.creditCardNoInvalid))
		    		errors.add(GlobalErrors.creditCardNoInvalid);
		    }
		    else
		    	correct1 = Boolean.TRUE;
	    }
	    catch(Exception e){
	    	if(!errors.contains(GlobalErrors.creditCardNoInvalid))
				errors.add(GlobalErrors.creditCardNoInvalid);
	    }
	    
	    // Check the CVV
	    try{
	    	int cvv = Integer.parseInt(this.cvv);
	    	
	    	if(cvv>9999){
	    		if(!errors.contains(GlobalErrors.creditCardCvvInvalid))
					errors.add(GlobalErrors.creditCardCvvInvalid);
	    	}
	    	else if(cvv<0){
	    		if(!errors.contains(GlobalErrors.creditCardCvvLessThanZero))
					errors.add(GlobalErrors.creditCardCvvLessThanZero);
	    	}
	    	
	    	correct2 = true;
	    }
	    catch(Exception e){
	    	if(!errors.contains(GlobalErrors.creditCardCvvInvalid))
				errors.add(GlobalErrors.creditCardCvvInvalid);
	    }
	    
		return correct1 && correct2;
	}
	
	public boolean buyNow(){
		if(this.errors.size() == 0)
			if(GV.DB.checkAuthToken(this.userID, this.authToken)){
				// Check that the credit card number is correct
				if(this.verifyCardNumber()){
				
				
					// If the account number is present, get the account from the database, otherwise this is a temporary account
					Account account = null;
					if(this.accountID > 0){
						try {
							account = GV.DB.getAccount(this.accountID);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else{
						// otherwise it's a temporary account
						account = new Account(-1, null, this.nameOnCard, this.creditCardNo, this.cvv, this.expirationDate);
					}
					
					// Make the address the user is paying with, we will use the form values as they may have
					// appended the address we displayed
					Address address = new Address(-1, this.userID, this.street, this.town, this.zip, Integer.parseInt(this.state), false);
					
					// If the addressID is present, get the address, otherwise it is a temporary address
					//TODO: either make the address everytime or lock the fields on the page
					/*Address address = null;
					if(this.addressID > 0){
						try {
							address = GV.DB.getAddress(addressID);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else{
						//TODO: new address based on the info passed
						address = null;
						//System.out.println("The addressID is " + this.addressID);
					}*/
					
					// Buy the item
					boolean retVal2 = GV.DB.buyItemNow(this.userID, this.itemID, this.quantity, account, address);
					
					if(!retVal2){
						if(!errors.contains(GlobalErrors.itemAlreadyGone))
							this.errors.add(GlobalErrors.itemAlreadyGone);
						
						this.buyFailure = true;
					}
					
					if(retVal2) this.createRedirectURL();
					
					//buyFailure = !retVal2;
					return retVal2;
				}
				else if(!errors.contains(GlobalErrors.creditCardNoInvalid))
					errors.add(GlobalErrors.creditCardNoInvalid);
			}
			else if(!errors.contains(GlobalErrors.incorrectAuthToken))
					errors.add(GlobalErrors.incorrectAuthToken);
		
		return false;
	}
	
	public String getRedirectURL(){
		return this.redirectURL;
	}
	
	/**
	 * If we successfully buy the item then we'll need to forward them on to the next page.
	 * We'll create the URL here
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	private void createRedirectURL(){
		this.redirectURL = "./confirmbuy?userID=" + this.userID + "&authToken=" + this.authToken;
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
	 * Returns the street sent to the page
	 * 
	 * @return String the street sent to the page
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getStreet(){
		return this.street;
	}
	
	/**
	 * Returns the town sent to the page
	 * 
	 * @return String the town sent to the page
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getTown(){
		return this.town;
	}
	
	/**
	 * Returns the zip sent to the page
	 * 
	 * @return String the zip sent to the page
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getZip(){
		return this.zip;
	}
	
	public long getItemID(){
		return this.itemID;
	}
	
	/**
	 * Returns the states that can be selected by the user
	 * 
	 * @return Sting[] a list of states (in option list format) that the user can choose from
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String[] getStateSelections(){
		return this.stateSelections;
	}
	
	public String getNameOnCard(){
		return this.nameOnCard;
	}
	
	public String getCreditCardNo(){
		return this.creditCardNo;
	}
	
	public String getCVV(){
		return this.cvv;
	}
	
	public int getQuantity(){
		return this.quantity;
	}
	
	public long getAddressID(){
		return this.addressID;
	}
	
	public long getAccountID(){
		return this.accountID;
	}
	
	public String getExpirationDateString(){
		return this.expirationDateString;
	}

	public Boolean getBuyFailure(){
		return this.buyFailure;
	}

	public Address getAddress(){
		return this.address;
	}
	
	public Boolean getReturnAddress(){
		return this.getAddress;
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
		
	}
}
