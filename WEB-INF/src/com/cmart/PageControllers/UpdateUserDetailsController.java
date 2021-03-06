package com.cmart.PageControllers;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalErrors;
import com.cmart.Data.GlobalVars;
import com.cmart.util.Address;
import com.cmart.util.CheckInputs;
import com.cmart.util.StopWatch;
import com.cmart.util.User;
/**
 * This class processes the data for the update user details page
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
public class UpdateUserDetailsController extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();
	
	// Variables passed in the request
	private String email1 = null;
	private String email2 = null;
	private String password1 = null;
	private String password2 = null;
	private String firstname = null;
	private String lastname = null;
	private String street = null;
	private String town = null;
	private String zip = null;
	private String state = null;
	private User user = null;
	private long userID = -1;
	private String authToken = null;
	private long addressID = -1;
	private Boolean addressIsDefault = null;
	private Address address = null;
	
	// Structures to hold the DB data
	private ArrayList<String[]> states = null;
	private Boolean hasAddress = Boolean.FALSE;
	
	// Structures to hold the parsed page data
	private String[] stateSelections = null;
	private String redirectURL = null;
	
	private boolean didUpdate = false;
	
	/**
	 * This method checks the page for any input errors that may have come from Client generator error
	 * These would need to be check in real life to stop users attempting to hack and mess with things
	 * This page has lots of variables to check
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
			
			// Check the e-mail addresses
			try{
				this.email1 = CheckInputs.checkEmailsVaild(request);
				this.email2 = email1;
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				
				// As the emails were not correct we'll fill the box with the user's old, bad data
				this.email1 = CheckInputs.getParameter(request, "email1");
				this.email2 = CheckInputs.getParameter(request, "email2");
			}
			
			// Check the passwords
			this.password1 = CheckInputs.getParameter(request, "password1");
			this.password2 = CheckInputs.getParameter(request, "password2");
			
			if(this.password1.equals(EMPTY) || this.password2.equals(EMPTY)){
				if(!errors.contains(GlobalErrors.passwordEmpty))
					errors.add(GlobalErrors.passwordEmpty);
				
			}
			else if(!this.password1.equals(password2)){
				if(!errors.contains(GlobalErrors.passwordDifferent))
					errors.add(GlobalErrors.passwordDifferent);
			}
			
			// Get the firstname
			this.firstname = CheckInputs.getParameter(request, "firstname");
			
			if(firstname.equals(EMPTY)){
				if(!errors.contains(GlobalErrors.firstnameEmpty))
					errors.add(GlobalErrors.firstnameEmpty);
			}
			
			// Get the lastname
			this.lastname = CheckInputs.getParameter(request, "lastname");
			
			if(lastname.equals(EMPTY)){
				if(!errors.contains(GlobalErrors.lastnameEmpty))
					errors.add(GlobalErrors.lastnameEmpty);
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

			// Check the addressID
			try{
				this.addressID = CheckInputs.checkAddressID(request);
				this.hasAddress = Boolean.TRUE;
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);

				this.addressID = -1;
			}
			
			// Get whether the address is the default (looking for 'true' or 'false')
			this.addressIsDefault = true;//Boolean.parseBoolean(CheckInputs.getParameter(request, "addressIsDefault"));
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
		
		// Get the list of states from the data base
		try{
			this.states = GV.DB.getStates();
		}
		catch(Exception e){
			errors.add(new Error("RegisterUserControl: getHTML4data: could not read the states from the database", e));
		}
		
		// Calculate how long that took
		super.stopTimerAddDB();
	}

	/**
	 * This method processes all of the data that was read from the database such that it is ready to be printed
	 * on to the page. We try to do as much of the page logic here as possible
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void processHTML4() {
		super.startTimer();
		
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
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void getHTML5Data(){
		super.startTimer();
		
		// Get the list of states from the data base
		try{
			this.states = GV.DB.getStates();
			this.user=GV.DB.getUser(userID);
		}
		catch(Exception e){
			errors.add(new Error("RegisterUserControl: getHTML4data: could not read the states from the database", e));
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
	 * This method updates the user's information in the database
	 * 
	 * @return boolean true if the user was successfully updated
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public boolean updateUser(){
		// If there are no errors on the page and the userID and password are correct we can update the database
		if(this.errors.size()==0)
			if(GV.DB.checkAuthToken(this.userID, this.authToken)){
				//The state must be correct since there weren't any errors
				int stateNo = -1;
				
				try{
					stateNo = Integer.valueOf(this.state);
				}
				catch(NumberFormatException e){
					if(!errors.contains(e))
						errors.add(GlobalErrors.stateNotAnInteger);
				}
				
				// Update the user and create the redirect URL
				if(stateNo >=0 ){
					boolean retVal = GV.DB.updateUser(this.userID, this.password1, this.email1,
										this.firstname, this.lastname);
					
					boolean retVal2 = GV.DB.updateAddress(this.addressID, userID, street, town, zip, stateNo, addressIsDefault);
					
					if(retVal && retVal2) this.createRedirectURL();
					didUpdate = retVal && retVal2;
					
					return retVal && retVal2;
				}
			}
			else if(!errors.contains(GlobalErrors.incorrectAuthToken))
					errors.add(GlobalErrors.incorrectAuthToken);
		
		return false;
	}
	
	//TODO: we shouldn't really EVER read the password. It should be encrypted and we should compare the hash
	public void getUserInfo(){
		// If the username and authToken are correct we can read the user's data
		if(GV.DB.checkAuthToken(this.userID, this.authToken)){
			// Get the data from the database
			user = GV.DB.getUser(this.userID);
			
			// Read out the user's details
			this.email1 = user.getEmail();
			this.email2 = this.email1;
			this.password1 = user.getPassword();
			this.password2 = this.password1;
			this.firstname = user.getFirstName();
			this.lastname = user.getLastName();
			
			// Read out their default address
			try {
				address = GV.DB.getDefaultAddress(this.userID);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(address != null){
				this.hasAddress = Boolean.TRUE;
				this.addressID = address.getId();
				this.street = address.getStreet();
				this.town = address.getTown();
				this.zip = address.getZip();
				this.state = String.valueOf(address.getState());
			}
		}
		else if(!errors.contains(GlobalErrors.incorrectAuthToken))
			errors.add(GlobalErrors.incorrectAuthToken);
	}
	
	/**
	 * If we successfully insert the user then we'll need to forward them on to the next page.
	 * We'll create the URL here
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	private void createRedirectURL(){
		this.redirectURL = "./myaccount?userID=" + this.userID + "&authToken=" + this.authToken;
	}
	
	/**
	 * Returns the password1 sent to the page
	 * 
	 * @return String the password1 sent to the page
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getPassword1(){
		return this.password1;
	}
	
	/**
	 * Returns the password2 sent to the page
	 * 
	 * @return String the password2 sent to the page
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getPassword2(){
		return this.password2;
	}
	
	/**
	 * Returns the email1 sent to the page
	 * 
	 * @return String the email1 sent to the page
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getEmail1(){
		return this.email1;
	}
	
	/**
	 * Returns the email2 sent to the page
	 * 
	 * @return String email2 sent to the page
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getEmail2(){
		return this.email2;
	}
	
	/**
	 * Returns the firstname sent to the page
	 * 
	 * @return String the firstname sent to the page
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getFirstname(){
		return this.firstname;
	}
	
	/**
	 * Returns the lastname sent to the page
	 * 
	 * @return String the lastname sent to the page
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getLastname(){
		return this.lastname;
	}
	
	/**
	 * Returns the street sent to the page
	 * 
	 * @return String the street sent to the page
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getStreet(){
		return this.street;
	}
	
	/**
	 * Returns the town sent to the page
	 * 
	 * @return String the town sent to the page
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getTown(){
		return this.town;
	}
	
	/**
	 * Returns the zip sent to the page
	 * 
	 * @return String the zip sent to the page
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getZip(){
		return this.zip;
	}
	
	/**
	 * Returns the zip code sent to the page
	 * 
	 * @return String the zip code sent to the page
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getState(){
		return this.state;
	}
	
	/**
	 * Returns the states that can be selected by the user
	 * 
	 * @return Sting[] a list of states (in option list format) that the user can choose from
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String[] getStateSelections(){
		return this.stateSelections;
	}
	
	/**
	 * Returns the URL to be redirected to if the user successfully registers
	 * 
	 * @return String the next URL to redirect to
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getRedirectURL(){
		return this.redirectURL;
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
	
	public User getUser(){
		return this.user;
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
	
	public Boolean getHasAddress(){
		return this.hasAddress;
	}
	
	public Address getAddress(){
		return this.address;
	}
	
	public long getAddressID(){
		return this.addressID;
	}
	
	public boolean getDidUpdate(){
		return this.didUpdate;
	}
}
