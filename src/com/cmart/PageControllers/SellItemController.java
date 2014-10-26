package com.cmart.PageControllers;

import java.util.Date;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalErrors;
import com.cmart.Data.GlobalVars;
import com.cmart.util.Category;
import com.cmart.util.CheckInputs;
import com.cmart.util.StopWatch;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.*;

/**
 * This class sells items for the user
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

public class SellItemController extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();
	
	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
	private String name = null;
	private String description = null;
	private int quantity;
	private double startPrice;
	private double reservePrice;
	private double buyNowPrice;
	private Date endDate;
	private long categoryID = 0;
	private Boolean itemInserted=false;
	private long itemID=-1;
	// Structures to hold the DB data
	private ArrayList<Category> categories = null;
	
	// Structures to hold the parsed page data
	private String[] categorySelections;
	private String redirectURL = null;
	private String categoriesJSON = null;
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
				// The user must be logged in to sell
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
			
			// Get the item's name, there must be a name, so add an error if not valid
			this.name = CheckInputs.getParameter(request, "name");
			if(this.name.equals(EMPTY))
				if(!errors.contains(GlobalErrors.sellNoName))
					errors.add(GlobalErrors.sellNoName);
			
			// Get the item's description
			this.description = CheckInputs.getParameter(request, "description");
			
			// Get the quantity
			try{
				this.quantity = CheckInputs.checkQuantity(request);
				
				if(this.quantity==0 && !errors.contains(GlobalErrors.quantityIsZero)){
					errors.add(GlobalErrors.quantityIsZero);
					this.quantity = 1;
				}
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				
				this.quantity = 1;
			}
			
			// Get the item's starting price
			try{
				this.startPrice = CheckInputs.checkStartPrice(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				
				this.startPrice = 0.0;
			}
			
			// Get the item's reserve price
			try{
				this.reservePrice = CheckInputs.checkReservePrice(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				
				this.reservePrice = 0.0;
			}
			
			// Get the item's buy now price
			try{
				this.buyNowPrice = CheckInputs.checkBuyNowPrice(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				
				this.buyNowPrice = 0.0;
			}
			
			// Get the item's end date
			//TODO: weird thing happens if date is really in the future
			try{
				this.endDate = CheckInputs.checkEndDate(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				
				this.endDate = new Date(System.currentTimeMillis());
			}
			
			// Get the item's category
			try{
				this.categoryID = CheckInputs.checkCategoryID(request);
				
				if(this.categoryID<1)
					if(!errors.contains(GlobalErrors.sellCategoryIDInvalid))
						errors.add(GlobalErrors.sellCategoryIDInvalid);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				
				this.categoryID = 0;
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
		
		// Get the categories the item can belong to
		try{
			categories = GV.DB.getAllCategories();
		}
		catch(Exception e){
			errors.add(new Error("SellItemController: getHTML4Data: Could not get the categories", e));
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
		
		// Make the category list items
		if(this.categories != null){
			categorySelections = new String[this.categories.size()];
			int length = this.categories.size();
			
			for(int i=0; i<length; i++)
				categorySelections[i] = "<option value=\"" + this.categories.get(i).getCategoryID() +"\""+ (this.categoryID==this.categories.get(i).getCategoryID()?" SELECTED":"") + ">" + this.categories.get(i).getName() + "</option>";
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
		
		// Get the categories the item can belong to
		/*try{
			StringBuffer SBcategoriesJSON = new StringBuffer();
			categories = GlobalVars.DB.getAllCategories();
			SBcategoriesJSON.append("[");
			if(categories.size()>0){
				SBcategoriesJSON.append(categories.get(0).toJSON());
			}
			for(int i=1;i<categories.size();i++){
				SBcategoriesJSON.append(",").append(categories.get(i).toJSON());
			}
			SBcategoriesJSON.append("]");
			this.categoriesJSON=SBcategoriesJSON.toString();
		}
		catch(Exception e){
			errors.add(new Error("SellItemController: getHTML4Data: Could not get the categories", e));
		}*/
		
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
		
		// Make the category list items
		/*if(this.categories != null){
			categorySelections = new String[this.categories.size()];
			int length = this.categories.size();
			
			for(int i=0; i<length; i++)
				categorySelections[i] = "<option value=\"" + this.categories.get(i).getCategoryID() +"\""+ (this.categoryID==this.categories.get(i).getCategoryID()?" SELECTED":"") + ">" + this.categories.get(i).getName() + "</option>";
		}*/
		
		// Calculate how long that took
		super.stopTimerAddProcessing();
	}
	
	public long insertItem(){
		// Only attempt to insert if there are no errors
		if(this.errors.size() == 0)
			if(GV.DB.checkAuthToken(this.userID, this.authToken)){
				// Make sure the reserve price is greater the the start price
				if((this.reservePrice > 0.0) && (this.reservePrice < this.startPrice))
					if(!errors.contains(GlobalErrors.sellStartPriceGreaterThanReserve))
						errors.add(GlobalErrors.sellStartPriceGreaterThanReserve);
					
				// Make sure the buy now price is higher than the start and reserve price
				if((this.buyNowPrice > 0.0) && (this.reservePrice > this.buyNowPrice))
						if(!errors.contains(GlobalErrors.sellReservePriceGreaterThanBuyNow))
							errors.add(GlobalErrors.sellReservePriceGreaterThanBuyNow);
				
				// Make sure the end date is later than now
				if(this.endDate != null && this.endDate.before(Calendar.getInstance().getTime()))
					if(!errors.contains(GlobalErrors.sellEndDateInPast))
						errors.add(GlobalErrors.sellEndDateInPast);
			
				// As everything is good we can insert the item
				if(this.errors.size()==0){
					long retVal = GV.DB.insertItem(userID, name, description, startPrice, reservePrice, buyNowPrice, quantity, new java.sql.Time(this.endDate.getTime()), categoryID);
					this.createRedirectURL(retVal);
					this.itemInserted=true;
					this.itemID=retVal;
					return retVal;
				}
				
				return -1;
			}
			else if(!errors.contains(GlobalErrors.incorrectAuthToken))
				errors.add(GlobalErrors.incorrectAuthToken);
		
		return -1;
	}
	
	/**
	 * If we successfully insert the item then we can go to the conformation page.
	 * We'll create the URL here
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	private void createRedirectURL(long itemID){
		this.redirectURL = "./sellitemimages?userID="+this.getUserIDString()+"&authToken="+this.getAuthTokenString()+"&itemID="+itemID;
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
	
	public long getItemID(){
		return this.itemID;
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
	 * Returns the item's name sent to the page
	 * 
	 * @return string the name
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Returns the item's description sent to the page
	 * 
	 * @return string the description
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getDescription(){
		return this.description;
	}
	
	/**
	 * Returns the item's start price sent to the page
	 * 
	 * @return float the start price
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public double getStartPrice(){
		return this.startPrice;
	}
	
	/**
	 * Returns the item's reserve price sent to the page
	 * 
	 * @return float the reserve price
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public double getReservePrice(){
		return this.reservePrice;
	}
	
	/**
	 * Returns the item's buy now sent to the page
	 * 
	 * @return float the buy now price
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public double getBuyNowPrice(){
		return this.buyNowPrice;
	}
	
	/**
	 * Returns the item's quantity sent to the page
	 * 
	 * @return int the quantity
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public int getQuantity(){
		return this.quantity;
	}
	
	/**
	 * Returns the item's category ID sent to the page
	 * 
	 * @return long the category ID
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public long getCategoryID(){
		return this.categoryID;
	}
	
	public String[] getCategorySelections(){
		return this.categorySelections;
	}
	
	/**
	 * Returns the item's end date sent to the page
	 * 
	 * @return Date the end date
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public Date getEndDate(){
		return this.endDate;
	}
	
	/**
	 * Returns the URL to be redirected to if the item is inserted
	 * 
	 * @return String the next URL to redirect to
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getRedirectURL(){
		return this.redirectURL;
	}
	
	public Boolean itemInserted(){
		return this.itemInserted;
	}

	/**
	 * @return the categoriesJSON
	 */
	public String getCategoriesJSON() {
		return categoriesJSON;
	}

	/**
	 * @param categoriesJSON the categoriesJSON to set
	 */
	public void setCategoriesJSON(String categoriesJSON) {
		this.categoriesJSON = categoriesJSON;
	}
}
