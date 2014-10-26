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
import com.cmart.util.Item;
import com.cmart.util.StopWatch;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.*;

/**
 * This is for the home page
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

public class HomeController extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();
	
	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
	private boolean getRecommendation = false;
	private int recommendationPageNo = 0;
	
	// Structures to hold the DB data
	private ArrayList<Item> items = null;
	
	private int itemsPP = 3;
	private long categoryID = 0;
	private int sortCol = 0;
	private Boolean sortDec = Boolean.FALSE;
	
	/**
	 * This method checks the page for any input errors that may have come from Client generator error
	 * These would need to be check in real life to stop users attempting to hack and mess with things
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
			}
			
			// Get the authToken (if exists), we will pass it along to the next pages
			try{
				this.authToken = CheckInputs.checkAuthToken(request);
			}
			catch(Error e){
			}
			
			this.getRecommendation = CheckInputs.checkGetRecommendation(request);
			
			try{
				this.recommendationPageNo = CheckInputs.checkRecommendationPageNumber(request);
			}
			catch(Error e){
			}
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
		
		//TODO: use hasItems
		// Get the recommended item from the database
				try{
					items = GlobalVars.DB.getCategoryItems(this.categoryID, this.recommendationPageNo, this.itemsPP, this.sortCol, this.sortDec, true, new String[0], 0);
				}
				catch(Exception e){
						errors.add(new Error("HomeController: getHTML4Data: Could not read from data base when getting the items", e));
				}
				
		// Calculate how long that took
		super.stopTimerAddDB();
	}

	/**
	 * This method get the recommendation items for recommended items request of AJAX
	 *
	 * TODO: Change fake get recommendation methods to real one
	 * @author Bo (bol1@andrew.cmu.edu) 
	 */
	public void getXMLData(){
		super.startTimer();
		
		//TODO: use hasItems
		// Get the recommended item from the database
		try{
			items = GlobalVars.DB.getCategoryItems(this.categoryID, this.recommendationPageNo, this.itemsPP, this.sortCol, this.sortDec, true, new String[0], 0);
		}
		catch(Exception e){
				errors.add(new Error("HomeController: getXMLData: Could not read from data base when getting the items", e));
		}
		
		// Calculate how long that took
		super.stopTimerAddDB();
	}
	
	/**
	 * This method processes all of the data that was read from the database such that it is ready to be printed
	 * on to the page for AJAX use. We try to do as much of the page logic here as possible
	 * 
	 * @author Bo (bol1@andrew.cmu.edu)
	 */
	public void processXML() {
		super.startTimer();
		
		// Calculate how long that took
		super.stopTimerAddProcessing();
	}
	
	/**
	 * This method processes all of the data that was read from the database such that it is ready to be printed
	 * on to the page. We try to do as much of the page logic here as possible
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void processHTML4() {
		super.startTimer();
		
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
		
		// Calculate how long that took
		super.stopTimerAddProcessing();
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
	
	public int getPageNo(){
		return this.recommendationPageNo;
	}
	
	public boolean getRecommendation(){
		return this.getRecommendation;
	}
	
	public ArrayList<Item> getItems(){
		return this.items;
	}
	
	public int getItemsPP(){
		return this.itemsPP;
	}
}
