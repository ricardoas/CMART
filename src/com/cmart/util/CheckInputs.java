package com.cmart.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalErrors;
import com.cmart.Data.GlobalVars;

/**
 * This class checks the parameters sent to the pages. This is in a static class as lots of the pages take
 * the same parameters, so it saves each page having to implement them
 * 
 * The parameters since we made the client generator, so we know what we are passing. However, in real life people
 * will try to break and hack whatever you make
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
public class CheckInputs {
	private static final String EMPTY = "";
	private static final GlobalVars GV = GlobalVars.getInstance();
	
	// The RegEx to see if the zip and email are valid
	private static final Pattern zipFormatCheck = Pattern.compile("\\d{5}(-\\d{4})?",Pattern.CASE_INSENSITIVE);
	private static final Pattern emailFormatCheck = Pattern.compile("^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$",Pattern.CASE_INSENSITIVE);
	   
	
	/**
	 * Makes sure the userID is valid for use in the system. It must be present and greater than zero.
	 * Returns the userID if valid, otherwise it throws an exception that describes the problem
	 * 
	 * @param request
	 * @return long of the userID
	 * @throws Exception
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static long checkUserID(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.userIDNotPresent;
		
		String userID = request.getParameter("userID");
		long userIDInt = 0;
		
		//See if the userID is present
		if(userID == null){
			throw GlobalErrors.userIDNotPresent;
		}
		// See if the userID is an empty string
		else if(userID.equals(EMPTY)){
			throw GlobalErrors.userIDEmpty;
		}
		else{
			// make sure the userID is a number
			try{
				userIDInt = Long.parseLong(userID);
				
				// make sure the userID is positive
				if(userIDInt <0)
					throw GlobalErrors.userIDLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.userIDNotAnInteger;
			}
		}
		
		// If we made it here then everything is okay, pass the userID back
		return userIDInt;
	}
	
	/**
	 * Checks that the authToken passed is in the correct format. Returns the authToken if valid, otherwise an exception
	 * 
	 * @param request
	 * @return String of the authToken
	 * @throws Exception
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static String checkAuthToken(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.authTokenNotPresent;
		
		String authToken = request.getParameter("authToken");
		
		//See if the authToken is present
		if(authToken == null){
			throw GlobalErrors.authTokenNotPresent;
		}
		// See if the userID is an empty string
		else if(authToken.equals(EMPTY)){
			throw GlobalErrors.authTokenEmpty;
		}
		
		return secureString(authToken);
	}
	
	/**
	 * Checks the suppressOutput option. The suppressOutput option tells the page to clear the output writer
	 * before it returns the page, this reduces the amount of network traffic. This might not be too useful
	 * in the final system as the client generator will need the page data for most pages
	 * 
	 *
	 * 
	 * @param request The request to read the parameter from
	 * @return boolean true if we are to suppress the output, false otherwise
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static boolean checkSuppressOutput(HttpServletRequest request){
		if(request==null) return false;
		
		String suppressOutput = request.getParameter("suppressOutput");
			
		// If suppressOutput is set to '1' then return true as we are to suppress the page output
		if(suppressOutput != null && suppressOutput.equals("1"))
			return true;
			
		// Otherwise false, and we print the page output
		else return false;
	}
	/**
	 * Get whether the HTML4 or HTML5 version of the site should be used
	 * 0 = use HTML4
	 * 1 = use HTML5
	 * 
	 * @param request
	 * @return
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static Boolean checkUseHTML5(HttpServletRequest request){
		if(request==null) return Boolean.FALSE;
		
		String htmlFlag = request.getParameter("useHTML5");
		// If htmlFlag is set to '1' then return true, we use HTML5
		if(htmlFlag != null && htmlFlag.equals("1")){
			return Boolean.TRUE;
		}
			
		// Otherwise false, we use HTML4
		else return Boolean.FALSE;
	}
	
	public static Boolean checkIsOld(HttpServletRequest request){
		if(request==null) return Boolean.FALSE;
		
		String oldFlag = request.getParameter("old");
			
		// If old return true
		if(oldFlag != null && oldFlag.equals("1"))
			return Boolean.TRUE;
			
		// Otherwise false
		else return Boolean.FALSE;
	}
	
	public static boolean checkSortDec(HttpServletRequest request){
		if(request==null) return false;
		
		String sortDec = request.getParameter("sortDec");
			
		// If sortDec is set to '1' then return true as we are to sort by descending items
		if(sortDec != null && sortDec.equals("1"))
			return true;
			
		// Otherwise false, and we print sort ascending
		else return false;
	}
	
	public static Boolean checkClearStatistics(HttpServletRequest request){
		if(request==null) return Boolean.FALSE;
		
		String clearStats = request.getParameter("clearStats");
			
		// If clear stats is zero to zero then we'll leave them
		if(clearStats != null && clearStats.equals("0"))
			return Boolean.FALSE;
			
		// Otherwise we will default to clearing them
		else return Boolean.TRUE;
	}
	
	/**
	 * Gets the processing loop parameter. The processing loop parameter can be used to increase to increase the
	 * processing on the app tier without having to increase load on the DB.
	 * 
	 * @param request The request to read the parameter from
	 * @return int The number of times to loop the processing of the page
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static int checkProcessingLoop(HttpServletRequest request){
		if(request==null) return 0;
		
		String processingLoop = request.getParameter("processingLoop");
		
		// Check that the loop parameter is present
		if(processingLoop != null){
			try{
				int processingLoopInt = Integer.parseInt(processingLoop);
				
				// Return the number of times to loop the processing
				return processingLoopInt;
			}
			catch(NumberFormatException e){
			}
		}
		
		// The default number of times to loop is zero
		return 0;
	}
	
	/**
	 * Gets the categoryID. Returns the categoryID if present and is a positive integer, otherwise throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @return long The categoryID number
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static long checkCategoryID(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.categoryIDNotPresent;
		
		String categoryID = request.getParameter("categoryID");
		long categoryIDInt = 0;
		
		//See if the categoryID is present
		if(categoryID == null){
			throw GlobalErrors.categoryIDNotPresent;
		}
		// See if the categoryID is an empty string
		else if(categoryID.equals(EMPTY)){
			throw GlobalErrors.categoryIDEmpty;
		}
		else{
			// make sure the categoryID is a number
			try{
				categoryIDInt = Long.parseLong(categoryID);
				
				// make sure the categoryID is positive
				if(categoryIDInt <0)
					throw GlobalErrors.categoryIDLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.categoryIDNotAnInteger;
			}
		}
		
		// If we made it here then everything is okay, pass the categoryID back
		return categoryIDInt;
	}
	
	/**
	 * Checks the page number. Returns the page number if a positive integer, otherwise throws an error
	 * 
	 * @param requestThe request to read the parameter from
	 * @return int The current page number
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static int checkPageNumber(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.pageNoNotPresent;
		
		String pageNo = request.getParameter("pageNo");
		int pageNoInt = 0;
		
		//See if the page is present
		if(pageNo == null){
			throw GlobalErrors.pageNoNotPresent;
		}
		// See if the page is an empty string
		else if(pageNo.equals(EMPTY)){
			throw GlobalErrors.pageNoEmpty;
		}
		else{
			// make sure the page is a number
			try{
				pageNoInt = Integer.parseInt(pageNo);
				
				// make sure the page is positive
				if(pageNoInt <0)
					throw GlobalErrors.pageNoLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.pageNoNotAnInteger;
			}
		}
		
		// If we made it here then everything is okay, pass the page back
		return pageNoInt;
	}
	
	/**
	 * Gets the items per page parameter. Returns itemsPP if a positive integer, otherwise throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @return int The nuber of items per page
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static int checkItemsPerPage(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.itemsPPNotPresent;
		
		String itemsPP = request.getParameter("itemsPP");
		int itemsPPInt = 0;
		
		//See if the items per page is present
		if(itemsPP == null){
			throw GlobalErrors.itemsPPNotPresent;
		}
		// See if the items per page is an empty string
		else if(itemsPP.equals(EMPTY)){
			throw GlobalErrors.itemsPPEmpty;
		}
		else{
			// make sure the items per page is a number
			try{
				itemsPPInt = Integer.parseInt(itemsPP);
				
				// make sure the items per page is positive
				if(itemsPPInt <0)
					throw GlobalErrors.itemsPPLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.itemsPPNotAnInteger;
			}
		}
		
		// If we made it here then everything is okay, pass the items per page
		return itemsPPInt;
	}
	
	
	/**
	 * Gets the total Users parameter. Returns totalUsers if a positive integer, otherwise throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @return int The nuber of items per page
	 * @throws Error
	 * @author Andrew Fox
	 */
	public static int checkTotalUsers(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.totalUsersNotPresent;
		
		String totalUsers = request.getParameter("totalUsers");
		int totalUsersInt = 0;
		
		//See if the items per page is present
		if(totalUsers == null){
			throw GlobalErrors.totalUsersNotPresent;
		}
		// See if the items per page is an empty string
		else if(totalUsers.equals(EMPTY)){
			throw GlobalErrors.totalUsersEmpty;
		}
		else{
			// make sure the items per page is a number
			try{
				totalUsersInt = Integer.parseInt(totalUsers);
				
				// make sure the items per page is positive
				if(totalUsersInt <0)
					throw GlobalErrors.totalUsersLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.itemsPPNotAnInteger;
			}
		}
		
		// If we made it here then everything is okay, pass the items per page
		return totalUsersInt;
	}
	
	
	/**
	 * Gets the prefetchImageValue parameter. Returns prefetchImageValue if a positive integer, otherwise throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @return int The nuber of images to be returned per page
	 * @throws Error
	 * @author Andrew Fox
	 */
	public static int checkPrefetchImageValue(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.prefetchImageValueNotPresent;
		
		String prefetchImageValue = request.getParameter("prefetchImageValue");
		int prefetchImageValueInt = 0;
		
		//See if the prefetch image value is present
		if(prefetchImageValue == null){
			throw GlobalErrors.prefetchImageValueNotPresent;
		}
		// See if the prefetch image value is an empty string
		else if(prefetchImageValue.equals(EMPTY)){
			throw GlobalErrors.prefetchImageValueNotPresent;
		}
		else{
			// make sure the prefetch image value is a number
			try{
				prefetchImageValueInt = Integer.parseInt(prefetchImageValue);
				
				// make sure the prefetch image value is positive
				if(prefetchImageValueInt <0)
					throw GlobalErrors.prefetchImageValueNotPresent;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.prefetchImageValueNotPresent;
			}
		}
		
		// If we made it here then everything is okay, pass the prefetch image value
		return prefetchImageValueInt;
	}
	
	
	/**
	 * Check that the username passed is correct and is not already present in the database
	 * 
	 * @param request The request to read the parameter from
	 * @return String of the username if valid
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static String checkUsername(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.usernameNotPresent;
		
		String username = request.getParameter("username");

		//See if the username is present
		if(username == null){
			throw GlobalErrors.usernameNotPresent;
		}
		// See if the username is an empty string
		else if(username.equals(EMPTY)){
			throw GlobalErrors.usernameEmpty;
		}
		else{
			// Make sure the username is not already taken
			try {
				if(GlobalVars.DB.usernameExists(username)){
					throw GlobalErrors.usernameTaken;
				}
			} catch (Exception e) {
				throw GlobalErrors.usernameTaken;
			}
		}
		
		// If we made it here then everything is okay, pass the the username
		return secureString(username);
	}
	
	/**
	 * Checks that the email addresses are the same and not already present in the database, otherwise it throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @return string of the email address if they are both the same
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static String checkEmailsNotPresent(HttpServletRequest request) throws Error{
		return checkEmailsNotPresent(request, true);
	}
	
	/**
	 * 
	 * 
	 * @param request request The request to read the parameter from
	 * @param checkDB true if we should check the DB for the address
	 * @return string of the email address if they are both the same
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	private static String checkEmailsNotPresent(HttpServletRequest request, boolean checkDB) throws Error{
		if(request==null) throw GlobalErrors.emailNotPresent;
		
		String email1 = request.getParameter("email1");
		String email2 = request.getParameter("email2");
		
		//See if the e-mail address is present
		if(email1 == null){
			throw GlobalErrors.emailNotPresent;
		}
		// See if the e-mail address is an empty string
		else if(email1.equals(EMPTY)){
			throw GlobalErrors.emailEmpty;
		}
		else{
			// make sure the e-mail addresses are the same
			if(!email1.equals(email2))
				throw GlobalErrors.emailDifferent;
			
			// Make sure the email format is correct
			if(!emailFormatCheck.matcher(email1).matches())
				throw GlobalErrors.emailNotVaild;
			
			// Make sure the e-mail address is not already taken
			if(checkDB && GlobalVars.DB.emailExists(email1))
				throw GlobalErrors.emailTaken;
		}
		
		// If we made it here then everything is okay, pass the the e-mail address
		return secureString(email1);
	}
	
	/**
	 * Checks that the email addresses are the same, otherwise it throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @return string of the email address if they are both the same
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static String checkEmailsVaild(HttpServletRequest request) throws Error{
		return checkEmailsNotPresent(request, false);
	}
	
	/**
	 * Gets the bid amount. Returns the bid amount if present and is a positive double, otherwise throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @return double The bid amount
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static double checkBid(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.bidNotPresent;
		
		String bid = request.getParameter("bid");
		double bidF = 0;
		
		//See if the bid is present
		if(bid == null){
			throw GlobalErrors.bidNotPresent;
		}
		// See if the bid is an empty string
		else if(bid.equals(EMPTY)){
			throw GlobalErrors.bidEmpty;
		}
		else{
			// make sure the bid is a number
			try{
				bidF = Double.parseDouble(bid);
				
				// make sure the bid is positive
				if(bidF < 0.0)
					throw GlobalErrors.bidLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.bidNotADouble;
			}
		}
		
		// If we made it here then everything is okay, pass the bid
		return roundCurrency(bidF);
	}
	
	/**
	 * Gets the max bid amount. Returns the max bid amount if present and is a positive double, otherwise throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @return double The max bid amount
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static double checkMaxBid(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.maxBidNotPresent;
		
		String maxBid = request.getParameter("maxBid");
		double maxBidF = 0;
		
		//See if the max bid is present
		if(maxBid == null){
			throw GlobalErrors.maxBidNotPresent;
		}
		// See if the max bid is an empty string
		else if(maxBid.equals(EMPTY)){
			throw GlobalErrors.maxBidEmpty;
		}
		else{
			// make sure the max bid is a number
			try{
				maxBidF = Double.parseDouble(maxBid);
				
				// make sure the bid is positive
				if(maxBidF < 0.0)
					throw GlobalErrors.maxBidLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.maxBidNotADouble;
			}
		}
		
		// If we made it here then everything is okay, pass the max bid back
		return roundCurrency(maxBidF);
	}
	
	/**
	 * Gets the quantity parameter. Returns quantity if a positive integer, otherwise throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @return int The nuber of items
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static int checkQuantity(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.quantityNotPresent;
		
		String quantity = request.getParameter("quantity");
		int quantityInt = 0;
		
		//See if the quantity is present
		if(quantity == null){
			throw GlobalErrors.quantityNotPresent;
		}
		// See if the quantity is an empty string
		else if(quantity.equals(EMPTY)){
			throw GlobalErrors.quantityEmpty;
		}
		else{
			// make sure the quantity is a number
			try{
				quantityInt = Integer.parseInt(quantity);
				
				// make sure the quantity is positive
				if(quantityInt <0)
					throw GlobalErrors.quantityLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.quantityNotAnInteger;
			}
		}
		
		// If we made it here then everything is okay, pass the quantity
		return quantityInt;
	}
	
	/**
	 * Gets the itemID parameter. Returns itemID if a positive integer, otherwise throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @return long The nuber of items
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static long checkItemID(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.itemIDNotPresent;
		
		String itemID = request.getParameter("itemID");
		long itemIDInt = 0;
		
		//See if the itemID is present
		if(itemID == null){
			throw GlobalErrors.itemIDNotPresent;
		}
		// See if the itemID is an empty string
		else if(itemID.equals(EMPTY)){
			throw GlobalErrors.itemIDEmpty;
		}
		else{
			// make sure the itemID is a number
			try{
				itemIDInt = Long.parseLong(itemID);
				
				// make sure the itemID is positive
				if(itemIDInt <=0)
					throw GlobalErrors.itemIDLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.itemIDNotAnInteger;
			}
		}
		
		// If we made it here then everything is okay, pass the itemID
		return itemIDInt;
	}
	
	//TODO: is this used?
	public static long checkLastSeenID(HttpServletRequest request){
		String lastSeenID = request.getParameter("lastSeenID");
		long lastSeenLong = 0;
		
		//See if the itemID is present
		if(lastSeenID == null){
			return 0;
			//throw GlobalErrors.itemIDNotPresent;
		}
		// See if the itemID is an empty string
		else if(lastSeenID.equals(EMPTY)){
			return 0;
			//throw GlobalErrors.itemIDEmpty;
		}
		else{
			// make sure the itemID is a number
			try{
				lastSeenLong = Long.parseLong(lastSeenID);
				
				// make sure the itemID is positive
				if(lastSeenLong <=0)
					return 0;
				
					//throw GlobalErrors.itemIDLessThanZero;
			}
			catch(NumberFormatException e){
				return 0;
				//throw GlobalErrors.itemIDNotAnInteger;
			}
		}
		
		// If we made it here then everything is okay, pass the itemID
		return lastSeenLong;
	}
	
	//TODO: is this used?
	public static long checkLastSeenPrice(HttpServletRequest request){
		String lastSeenPrice = request.getParameter("lastSeenPrice");
		long lastSeenLong = 0;
		
		//See if the itemID is present
		if(lastSeenPrice == null){
			return 0;
			//throw GlobalErrors.itemIDNotPresent;
		}
		// See if the itemID is an empty string
		else if(lastSeenPrice.equals(EMPTY)){
			return 0;
			//throw GlobalErrors.itemIDEmpty;
		}
		else{
			// make sure the itemID is a number
			try{
				lastSeenLong = Long.parseLong(lastSeenPrice);
				
				// make sure the itemID is positive
				if(lastSeenLong <=0)
					return 0;
				
					//throw GlobalErrors.itemIDLessThanZero;
			}
			catch(NumberFormatException e){
				return 0;
				//throw GlobalErrors.itemIDNotAnInteger;
			}
		}
		
		// If we made it here then everything is okay, pass the itemID
		return lastSeenLong;
	}
	
	/**
	 * Gets the AddressID parameter. Returns AddressID if a positive integer, otherwise throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @return long The nuber of items
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static long checkAddressID(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.addressIDNotPresent;
		
		String addressID = request.getParameter("addressID");
		long addressIDInt = 0;
		
		//See if the addressID is present
		if(addressID == null){
			throw GlobalErrors.addressIDNotPresent;
		}
		// See if the addressID is an empty string
		else if(addressID.equals(EMPTY)){
			throw GlobalErrors.addressIDEmpty;
		}
		else{
			// make sure the addressID is a number
			try{
				addressIDInt = Long.parseLong(addressID);
				
				// make sure the addressID is positive
				if(addressIDInt <0)
					throw GlobalErrors.addressIDLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.addressIDNotAnInteger;
			}
		}
		
		// If we made it here then everything is okay
		return addressIDInt;
	}
	
	public static long checkAccountID(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.accountIDNotPresent;
		
		String accountID = request.getParameter("accountID");
		long accountIDInt = 0;
		
		//See if the accountID is present
		if(accountID == null){
			throw GlobalErrors.accountIDNotPresent;
		}
		// See if the accountID is an empty string
		else if(accountID.equals(EMPTY)){
			throw GlobalErrors.accountIDEmpty;
		}
		else{
			// make sure the accountID is a number
			try{
				accountIDInt = Long.parseLong(accountID);
				
				// make sure the accountID is positive
				if(accountIDInt <0)
					throw GlobalErrors.accountIDLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.accountIDNotAnInteger;
			}
		}
		
		// If we made it here then everything is okay
		return accountIDInt;
	}
	
	public static double roundCurrency(double num){
		return (double) (Math.floor(num*100.0 + 0.5) / 100.0);
	}
	
	/**
	 * Gets the starting price amount. Returns the start price amount if present and is a positive double, otherwise throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @return double The start price amount
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static double checkStartPrice(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.startPriceNotPresent;
		
		String startPrice = request.getParameter("startPrice");
		double startPriceF = 0.0;
		
		//See if the start price is present
		if(startPrice == null){
			throw GlobalErrors.startPriceNotPresent;
		}
		// See if the start price is an empty string
		else if(startPrice.equals(EMPTY)){
			throw GlobalErrors.startPriceEmpty;
		}
		else{
			// make sure the start price is a number
			try{
				startPriceF = Double.parseDouble(startPrice);
				
				// make sure the start price is positive
				if(startPriceF < 0.0)
					throw GlobalErrors.startPriceLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.startPriceNotADouble;
			}
		}
		
		// If we made it here then everything is okay
		return roundCurrency(startPriceF);
	}
	
	/**
	 * Gets the starting price amount. Returns the start price amount if present and is a positive double, otherwise throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @return double The start price amount
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static double checkReservePrice(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.reservePriceNotPresent;
		
		String reservePrice = request.getParameter("reservePrice");
		double reservePriceF = 0.0;
		
		//See if the reserve price is present
		if(reservePrice == null){
			throw GlobalErrors.reservePriceNotPresent;
		}
		// See if the reserve price is an empty string
		else if(reservePrice.equals(EMPTY)){
			throw GlobalErrors.reservePriceEmpty;
		}
		else{
			// make sure the reserve price is a number
			try{
				reservePriceF = Double.parseDouble(reservePrice);
				
				// make sure the reserve price is positive
				if(reservePriceF < 0.0)
					throw GlobalErrors.reservePriceLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.reservePriceNotADouble;
			}
		}
		
		// If we made it here then everything is okay
		return roundCurrency(reservePriceF);
	}
	
	/**
	 * Gets the starting price amount. Returns the start price amount if present and is a positive double, otherwise throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @return double The start price amount
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static double checkBuyNowPrice(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.buyNowPriceNotPresent;
		
		String buyNowPrice = request.getParameter("buyNowPrice");
		double buyNowPriceF = 0.0;
		
		//See if the buy now price is present
		if(buyNowPrice == null){
			throw GlobalErrors.buyNowPriceNotPresent;
		}
		// See if the buy now price is an empty string
		else if(buyNowPrice.equals(EMPTY)){
			throw GlobalErrors.buyNowPriceEmpty;
		}
		else{
			// make sure the buy now price is a number
			try{
				buyNowPriceF = Double.parseDouble(buyNowPrice);
				
				// make sure the buy now price is positive
				if(buyNowPriceF < 0.0)
					throw GlobalErrors.buyNowPriceLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.buyNowPriceNotADouble;
			}
		}
		
		// If we made it here then everything is okay
		return roundCurrency(buyNowPriceF);
	}
	
	public static Date checkEndDate(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.endDateNotPresent;
		
		String endDate = request.getParameter("endDate");
		Date endDateD = null;
		
		//See if the end date is present
		if(endDate == null){
			throw GlobalErrors.endDateNotPresent;
		}
		// See if the end date price is an empty string
		else if(endDate.equals(EMPTY)){
			throw GlobalErrors.endDateEmpty;
		}
		else{
			// make sure the end date price is a real date
			try{
				endDateD = GlobalVars.parseDateFull(endDate);
				
				// make sure the end date is after now
				if(endDateD.before(Calendar.getInstance().getTime()))
					throw GlobalErrors.sellEndDateInPast;
			}
			catch(Exception e){
				throw GlobalErrors.endDateInvalid;
			}
		}
		
		// If we made it here then everything is okay
		return endDateD;
	}
	
	public static Date checkExpirationDate(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.expirationDateNotPresent;
		
		String expirationDate = request.getParameter("expirationDate");
		Date expirationDateD = null;
		
		//See if the end date is present
		if(expirationDate == null){
			throw GlobalErrors.expirationDateNotPresent;
		}
		// See if the end date price is an empty string
		else if(expirationDate.equals(EMPTY)){
			throw GlobalErrors.expirationDateEmpty;
		}
		else{
			// make sure the expiration date is correct format
			try{
				expirationDateD = GlobalVars.parseDateMMYY(expirationDate);
				
				// make sure the end date is after now
				if(expirationDateD.before(Calendar.getInstance().getTime()))
					throw GlobalErrors.expirationDateInPast;
				else if(expirationDateD.after(GlobalVars.maxDate))
					throw GlobalErrors.expirationDateInvalid;
			}
			catch(Exception e){
				throw GlobalErrors.expirationDateInvalid;
			}
		}
		
		// If we made it here then everything is okay
		return expirationDateD;
	}
	
	/**
	 * Reads a generic parameter from the requests and returns it as a String. If it is not present it returns blank
	 * 
	 * @param request The request to read the parameter from
	 * @param parameter The name of the parameter to read
	 * @return String of the parameter
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static String getParameter(HttpServletRequest request, String parameter){
		if(request==null) return "";
		
		String retVal = null;
		
		retVal = request.getParameter(parameter);
		
		if(retVal != null)
			return secureString(retVal);
		else
			return "";
	}
	
	public static long getTimestamp(HttpServletRequest request, String parameter){
		if(request==null) return 0l;
		
		String temp = null;
		
		temp = request.getParameter(parameter);
		
		if(temp != null){
			long ts = 0;
			try{
				ts = Long.parseLong(temp);
			}
			catch(Exception e){
				return 0l;
			}
			
			return ts;
		}
		else
			return 0l;
	}
	
	/**
	 * Checks that the zip code is a valid US zip code. If not it throws an error.
	 * mmmmm... RegEx
	 * 
	 * @param request The request to read the parameter from
	 * @return String of the zip code if correct
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static String checkZip(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.zipNotPresent;
		
		String zip = request.getParameter("zip");
		
		//See if the zip is present
		if(zip == null){
			throw GlobalErrors.zipNotPresent;
		}
		// See if the zip is an empty string
		else if(zip.equals(EMPTY)){
			throw GlobalErrors.zipEmpty;
		}
		// See if the zip matches the regex for a zip code
		else if(!zipFormatCheck.matcher(zip).matches()){
			throw GlobalErrors.zipInvalid;
		}
		
		// If we made it here then everything is okay, pass the zip
		return secureString(zip);
	}
	
	/**
	 * Gets the state number selected by the user. Throws an exception if there is something wrong with the state
	 * 
	 * @param request The request to read the parameter from 
	 * @return String of the state number
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static String checkState(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.stateNotPresent;
		
		String stateNo = request.getParameter("state");
		
		//See if the state is present
		if(stateNo == null){
			throw GlobalErrors.stateNotPresent;
		}
		// See if the state is an empty string
		else if(stateNo.equals(EMPTY)){
			throw GlobalErrors.stateEmpty;
		}
		else{
			// make sure the state is a number
			try{
				int state = Integer.parseInt(stateNo);
				
				// make sure the state is positive
				if(state <0)
					throw GlobalErrors.stateLessThanZero;
				
				// If the state is zero it means they didn't select
				if(state==0)
					throw GlobalErrors.stateNotSelected;
				
				//TODO: check that the state really exists?
			}
			catch(NumberFormatException e){
				throw GlobalErrors.stateNotAnInteger;
			}
		}
		
		// If we made it here then everything is okay, pass the state
		return secureString(stateNo);
	}
	
	public static long checkViewUserID(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.viewUserIDNotPresent;
		
		String viewUserID = request.getParameter("viewUserID");
		long viewUserIDInt = 0;
		
		//See if the viewUserID is present
		if(viewUserID == null){
			throw GlobalErrors.viewUserIDNotPresent;
		}
		// See if the viewUserID is an empty string
		else if(viewUserID.equals(EMPTY)){
			throw GlobalErrors.viewUserIDEmpty;
		}
		else{
			// make sure the viewUserID is a number
			try{
				viewUserIDInt = Long.parseLong(viewUserID);
				
				// make sure the viewUserID is positive
				if(viewUserIDInt <0)
					throw GlobalErrors.viewUserIDLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.viewUserIDNotAnInteger;
			}
		}
		
		// If we made it here then everything is okay, pass the viewUserID back
		return viewUserIDInt;
	}
	
	/**
	 * Gets the questionID parameter. Returns questionID if a positive integer, otherwise throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @throws Error
	 * @author Bo (bol1@andrew.cmu.edu)
	 */
	public static long checkQuestionID(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.questionIDNotPresent;
		
		String questionID = request.getParameter("questionID");
		long questionIDInt = 0;
		
		//See if the questionID is present
		if(questionID == null){
			throw GlobalErrors.questionIDNotPresent;
		}
		// See if the questionID is an empty string
		else if(questionID.equals(EMPTY)){
			throw GlobalErrors.questionIDEmpty;
		}
		else{
			// make sure the questionID is a number
			try{
				questionIDInt = Long.parseLong(questionID);
				
				// make sure the questionID is positive
				if(questionIDInt <0)
					throw GlobalErrors.questionIDLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.questionIDNotAnInteger;
			}
		}
		
		// If we made it here then everything is okay
		return questionIDInt;
	}
	
	/**
	 * Gets the item rating parameter. Returns rating if a positive integer in the range [0, 5], otherwise throws an error
	 * 
	 * @param request The request to read the parameter from
	 * @return int The rating number
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu) Bo (bol1@andrew.cmu.edu)
	 */
	public static int checkRating(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.ratingNotPresent;
		
		String rating = request.getParameter("rating");
		int ratingInt = -1;
		
		//See if the rating is present
		if(rating == null){
			throw GlobalErrors.ratingNotPresent;
		}
		// See if the rating is an empty string
		else if(rating.equals(EMPTY)){
			throw GlobalErrors.ratingEmpty;
		}
		else{
			// make sure the rating is a number
			try{
				ratingInt = Integer.parseInt(rating);
				
				// make sure the rating is in range [0,5]
				if((ratingInt <0) || (ratingInt > 5))
					throw GlobalErrors.ratingNotInRange;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.ratingNotAnInteger;
			}
		}
		
		// If we made it here then everything is okay
		return ratingInt;
	}
	
	/**
	 * Checks that the comment passed is in the correct format. Returns the comment if valid, otherwise an exception
	 * 
	 * @param request
	 * @return String of the comment
	 * @throws Exception
	 * @author Andy (andrewtu@cmu.edu) Bo (bol1@andrew.cmu.edu)
	 */
	public static String checkComment(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.commentNotPresent;
		
		String comment = request.getParameter("comment");
		
		//See if the comment is present
		if(comment == null){
			throw GlobalErrors.commentNotPresent;
		}
		// See if the comment is an empty string
		else if(comment.equals(EMPTY)){
			throw GlobalErrors.commentEmpty;
		}
		
		return secureString(comment);
	}
	
	/**
	 * Checks that the question passed is in the correct format. Returns the question if valid, otherwise an exception
	 * 
	 * @param request
	 * @return String of the question
	 * @throws Exception
	 * @author Andy (andrewtu@cmu.edu) Bo (bol1@andrew.cmu.edu)
	 */
	public static String checkQuestion(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.questionNotPresent;
		
		String question = request.getParameter("question");
		
		//See if the question is present
		if(question == null){
			throw GlobalErrors.questionNotPresent;
		}
		// See if the question is an empty string
		else if(question.equals(EMPTY)){
			throw GlobalErrors.questionEmpty;
		}
		
		return secureString(question);
	}
	
	/**
	 * Checks that the answer passed is in the correct format. Returns the answer if valid, otherwise an exception
	 * 
	 * @param request
	 * @return String of the answer
	 * @throws Exception
	 * @author Andy (andrewtu@cmu.edu) Bo (bol1@andrew.cmu.edu)
	 */
	public static String checkAnswer(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.answerNotPresent;
		
		String answer = request.getParameter("answer");
		
		//See if the answer is present
		if(answer == null){
			throw GlobalErrors.answerNotPresent;
		}
		// See if the answer is an empty string
		else if(answer.equals(EMPTY)){
			throw GlobalErrors.answerEmpty;
		}
		
		return secureString(answer);
	}
	
	/**
	 * Gets the itemprice request. Returns true if a request for item price exists, otherwise return false
	 * 
	 * @param request The request to read the parameter from
	 * @return boolean whether request itemprice or not
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu) Bo (bol1@andrew.cmu.edu)
	 */
	public static boolean checkItemCurrentBid(HttpServletRequest request){
		if(request==null) return false;
		
		String itemCurrentBid = request.getParameter("itemCurrentBid");
		boolean itemCurrentBidRequest = false;
		
		//See if the itemprice page is present
		if(itemCurrentBid != null){
			itemCurrentBidRequest = true;
		}
		// If we made it here then everything is okay, pass the items per page
		return itemCurrentBidRequest;
	}
	
	/**
	 * Checks the page number. Returns the page number if a positive integer, otherwise throws an error
	 * 
	 * @param requestThe request to read the parameter from
	 * @return int The current page number
	 * @throws Error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static int checkRecommendationPageNumber(HttpServletRequest request) throws Error{
		if(request==null) throw GlobalErrors.pageNoNotPresent;
		
		String pageNo = request.getParameter("recommendationPageNo");
		int pageNoInt = 0;
		
		//See if the page is present
		if(pageNo == null){
			throw GlobalErrors.pageNoNotPresent;
		}
		// See if the page is an empty string
		else if(pageNo.equals(EMPTY)){
			throw GlobalErrors.pageNoEmpty;
		}
		else{
			// make sure the page is a number
			try{
				pageNoInt = Integer.parseInt(pageNo);
				
				// make sure the page is positive
				if(pageNoInt <0)
					throw GlobalErrors.pageNoLessThanZero;
			}
			catch(NumberFormatException e){
				throw GlobalErrors.pageNoNotAnInteger;
			}
		}
		// If we made it here then everything is okay, pass the page back
		return pageNoInt;
	}
	
	/**
	 * Gets the recommendation request. Returns true if a request for recommendation exists, otherwise return false
	 * 
	 * @param request The request to read the parameter from
	 * @return boolean whether request get recommendation or not
	 * @author Andy (andrewtu@cmu.edu) Bo (bol1@andrew.cmu.edu)
	 */
	public static boolean checkGetRecommendation(HttpServletRequest request){
		if(request==null) return false;
		
		String getRecommendation = request.getParameter("getRecommendation");
		boolean getRecommendationRequest = false;
		
		if(getRecommendation != null){
			getRecommendationRequest = true;
		}
		return getRecommendationRequest;
	}
	
	// Because Andrew keeps embedding Rebbeca Black videos in items
	public static String secureString(String str){
		if(str==null) return null;
		
		//cite: http://weblogs.java.net/blog/gmurray71/archive/2006/09/preventing_cros.html
		str = str.replaceAll("eval\\((.*)\\)", "");
		str = str.replaceAll("[\\\"\\\'][\\s]*((?i)javascript):(.*)[\\\"\\\']", "\"\"");
		str = str.replaceAll("((?i)script)", "");
		str = str.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		str = str.replaceAll("&", "&amp;").replaceAll("\"", "&quot;");
		
		return str;
	}
		/**
	 * Checks that the friend request passed is in the correct format. Returns
	 * the friend request if valid, otherwise an exception
	 * 
	 * @param request
	 * @return String of the friend request
	 * @throws Exception
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public static String checkFriendRequest(HttpServletRequest request)
			throws Error {
		String friendRequest = request.getParameter("friendrequest");
		if (friendRequest == null)
			return null;
		return secureString(friendRequest);
	}

	/**
	 * Makes sure the acceptID is valid for use in the system. Returns the
	 * acceptID if valid, otherwise it throws an exception that describes the
	 * problem
	 * 
	 * @param request
	 * @return long of the acceptID
	 * @throws Exception
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public static long checkAcceptID(HttpServletRequest request) throws Error {
		String tmp = request.getParameter("acceptID");
		if (tmp == null)
			return -1;
		long acceptID = Long.parseLong(secureString(tmp));
		if (!GV.DB.hasRequestID(acceptID))
			return -1;
		return acceptID;
	}

	/**
	 * Makes sure the rejectID is valid for use in the system. Returns the
	 * rejectID if valid, otherwise it throws an exception that describes the
	 * problem
	 * 
	 * @param request
	 * @return long of the rejectID
	 * @throws Exception
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public static long checkRejectID(HttpServletRequest request) throws Error {
		String tmp = request.getParameter("rejectID");
		if (tmp == null)
			return -1;
		long rejectID = Long.parseLong(secureString(tmp));
		if (!GV.DB.hasRequestID(rejectID))
			return -1;
		return rejectID;
	}
	
	/**
	 * Checks that the toID passed is in the correct format. Returns the user if
	 * valid, otherwise an exception
	 * 
	 * @param request
	 * @return String of the to id
	 * @throws Exception
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public static long checkToID(HttpServletRequest request) throws Error {
		String tmp = request.getParameter("toID");
		if (tmp == null)
			return -1;
		long toID = Long.parseLong(secureString(tmp));
		if (!GV.DB.hasWallpostsToID(toID))
			return -1;
		return toID;
	}
	
	/**
	 * Checks that the reply id passed is in the correct format. Returns the user if
	 * valid, otherwise an exception
	 * 
	 * @param request
	 * @return String of the reply id
	 * @throws Exception
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public static long checkReplyID(HttpServletRequest request) throws Error {
		String tmp = request.getParameter("replyID");
		if (tmp == null)
			return -1;
		long replyID = Long.parseLong(secureString(tmp));
		if (!GV.DB.hasWallpostsReplyID(replyID))
			return -1;
		return replyID;
	}
	
	/**
	 * Checks that the post passed is in the correct format. Returns the user if
	 * valid, otherwise an exception
	 * 
	 * @param request
	 * @return String of the post
	 * @throws Exception
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public static String checkPost(HttpServletRequest request) throws Error {
		String post = request.getParameter("post");
		if (post == null)
			return null;
		return secureString(post);
	}
	
	/**
	 * Checks that the reply passed is in the correct format. Returns the user if
	 * valid, otherwise an exception
	 * 
	 * @param request
	 * @return String of the reply
	 * @throws Exception
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public static String checkReply(HttpServletRequest request) throws Error {
		String reply = request.getParameter("reply");
		if (reply == null)
			return null;
		return secureString(reply);
	}
}
