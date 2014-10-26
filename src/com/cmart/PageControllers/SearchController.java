package com.cmart.PageControllers;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalVars;
import com.cmart.util.Category;
import com.cmart.util.CheckInputs;
import com.cmart.util.Comment;
import com.cmart.util.Image;
import com.cmart.util.Item;
import com.cmart.util.User;
import com.cmart.util.Question;
/**
 * This class processes the data for the search servlet
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

public class SearchController extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();

	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
	private int pageNo = 0;
	private int itemsPP = 0;
	private String searchTerm = null;
	private int sortCol = 0;
	private Boolean sortDec = Boolean.FALSE;
	private HashMap<Long, Long> hasSellers = new HashMap<Long, Long>();
	private HashMap<Long, Long> hasItems = new HashMap<Long, Long>();
	private String itemPrefetchImagesURLs=null;

	// Structures to hold the DB data
	private ArrayList<Item> items = null;
	private ArrayList<User> sellers = new ArrayList<User>();
	private ArrayList<Question> questions=new ArrayList<Question>();
	private ArrayList<Comment> comments=new ArrayList<Comment>();
	private String previousPageURL = "< Previous Page";
	private String nextPageURL = "Next Page >";
	private String sortByEndURL = null;
	private String sortByPriceURL = null;
	private ArrayList<Long> itemList;

	// Structures to hold the parsed page data
	private String[] itemURLs = null;

	// Structure for HTML5 data
	private String[] itemJS0N = null;
	private String[] sellerJS0N = null;
	private String[] questionJSON = null;
	private String[] commentJSON = null;
	private String[] prefetchJSON=null;
	/**
	 * This method checks the page for any input errors that may have come from Client generator error
	 * These would need to be check in real life to stop users attempting to hack and mess with things
	 * The browse page does not need a username and authToken to use, so they are not strictly checked
	 * by the page
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

			// Get the search term
			this.searchTerm = CheckInputs.getParameter(request, "searchTerm");

			// Get the page number. If there isn't one we'll just start at page zero
			try{
				this.pageNo = CheckInputs.checkPageNumber(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				this.pageNo = 0;
			}

			// Get the number of items per page. If there isn't one defined we'll use the default of 25
			try{
				this.itemsPP = CheckInputs.checkItemsPerPage(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				this.itemsPP = 25;
			}

			// Get how to sort the page
			this.sortDec = CheckInputs.checkSortDec(request);
			try{
				this.sortCol = Integer.valueOf(CheckInputs.getParameter(request, "sortCol"));
			}
			catch(Exception e){
				// It's an optional parameter, we don't care
			}
		}

		// Calculate how long that took
		super.stopTimerAddProcessing();
	}

	/**
	 * This method get the data needed for the HTML4 page from the database
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void getHTML4Data(){
		/*
		 * Get all of the info needed from the database
		 */
		super.startTimer();

		// Get all of the items in the category
		try{
			if(GlobalVars.PREFETCH_IMAGES==false)
				items = GV.DB.getTextItems(this.searchTerm, this.pageNo, this.itemsPP, this.sortCol, this.sortDec);
			else
				items = GV.DB.getTextItems(this.searchTerm, this.pageNo, this.itemsPP, this.sortCol, this.sortDec,true,GlobalVars.PREFETCH_IMAGES_NUM);
		}
		catch(Exception e){
			errors.add(new Error("SearchControl: getHTML4Data: Could not read from data base when getting the items", e));
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
	public void processHTML4(){
		/*
		 * Process the data and make the URLs to be displayed
		 */
		super.startTimer();

		StringBuffer prefetchImagesURLs=new StringBuffer();
		// Make the URL links for the items
		if(this.items != null){
			int itemsSize = this.items.size();

			this.itemURLs = new String[itemsSize];
			for(int i=0; i < itemsSize; i++){
				this.itemURLs[i] = "<a href=\"./viewitem?"
						+ (this.hasUserID() ? "userID=" + this.getUserIDString() +"&": "")
						+ (this.hasAuthToken() ? "authToken=" + this.getAuthTokenString() +"&": "")
						+ "itemID=" + items.get(i).getID()
						+ "\">" + items.get(i).getName() + "</a>";
				for(Image im:this.items.get(i).getImages()){
					if(prefetchImagesURLs.length()>0)
						prefetchImagesURLs.append(",");
					prefetchImagesURLs.append("'").append(GV.REMOTE_IMAGE_IP).append(GV.REMOTE_IMAGE_DIR).append(im.getUrl()).append("'");
				}
			}
		}
		else this.itemURLs = new String[0];
		
		itemPrefetchImagesURLs=prefetchImagesURLs.toString();

		// Make the URLs for sorting the data
		this.sortByEndURL = "<label for\"sortendlink\"><a href=\"./search?"
				+ (this.hasUserID() ? "userID=" + this.getUserIDString() +"&": "")
				+ (this.hasAuthToken() ? "authToken=" + this.getAuthTokenString() +"&": "")
				+ "pageNo=" + this.pageNo + "&"
				+ "itemsPP=" + this.itemsPP +"&"
				+ "sortCol=2" +"&"
				+ "sortDec=" + (this.sortCol==2 ? (this.sortDec ? "0":"1") : (this.sortDec ? "1":"0"))+"&"
				+ "searchTerm=" + this.searchTerm
				+ "\">End Date</a></label>";

		this.sortByPriceURL = "<label for\"sortpricelink\"><a href=\"./search?"
				+ (this.hasUserID() ? "userID=" + this.getUserIDString() +"&": "")
				+ (this.hasAuthToken() ? "authToken=" + this.getAuthTokenString() +"&": "")
				+ "pageNo=" + this.pageNo + "&"
				+ "itemsPP=" + this.itemsPP +"&"
				+ "sortCol=1" +"&"
				+ "sortDec=" + (this.sortCol==1 ? (this.sortDec ? "0":"1") : (this.sortDec ? "1":"0"))+"&"
				+ "searchTerm=" + this.searchTerm
				+ "\">Bid Price</a></label>";

		// Make the URLs for the previous and next page
		// If we are further than page zero we can go backwards
		if(this.pageNo>0)
			this.previousPageURL = "<a href=\"./search?"
					+ (this.hasUserID() ? "userID=" + this.getUserIDString() +"&": "")
					+ (this.hasAuthToken() ? "authToken=" + this.getAuthTokenString() +"&": "")
					+ "pageNo=" + (this.pageNo-1) +"&"
					+ "itemsPP=" + this.itemsPP +"&"
					+ "searchTerm" + this.searchTerm
					+ "\">< Previous Page</a>";

		// If there are less items remaining than we want to display then there are no more pages
		if(this.items != null && this.items.size() >= this.itemsPP)
			this.nextPageURL = "<a href=\"./search?"
					+ (this.hasUserID() ? "userID=" + this.getUserIDString() +"&": "")
					+ (this.hasAuthToken() ? "&authToken=" + this.getAuthTokenString() +"&": "")
					+ "pageNo=" + (this.pageNo+1) +"&"
					+ "itemsPP=" + this.itemsPP +"&"
					+ "searchTerm=" + this.searchTerm
					+ "\">Next Page ></a>";


		// Calculate how long that took
		super.stopTimerAddProcessing();
	}

	/**
	 * Gets the HTML5 data from the database
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void getHTML5Data(){
		/*
		 * Get all of the info needed from the database
		 */
		super.startTimer();


		try{
			// Get the item id's in the search
			itemList = GV.DB.getTextItemsIDs(this.searchTerm, this.pageNo, this.itemsPP, this.sortCol, this.sortDec);

			// For all the IDs the client doesn't have, get the items
			ArrayList<Long> itemsToGet = new ArrayList<Long>();
			for(Long item: itemList)
				if(!this.hasItems.containsKey(item))
					itemsToGet.add(item);

					if(GlobalVars.PREFETCH_IMAGES==false)
						items =	GlobalVars.DB.getItemsByID(itemsToGet, this.sortCol, this.sortDec);
					else
						items =	GlobalVars.DB.getItemsByID(itemsToGet, this.sortCol, this.sortDec,true,GlobalVars.PREFETCH_IMAGES_NUM);
					ArrayList<Long> getList=new ArrayList<Long>();
					for(Item i:this.items){
						getList.add(i.getID());
					}
					/*for(Long i:itemList)
				System.out.println("list "+i);

			for(Long i:itemsToGet)
				System.out.println("to get "+i);

			for(Item i:items)
				System.out.println("id "+i.getID());*/

					// Get the questions for the items on the page
					questions= GlobalVars.DB.getQuestions(getList);
					comments= GlobalVars.DB.getComments(getList);

					// Get the sellers we need to send back to the user
					ArrayList<Long> sellerIDs = new ArrayList<Long>();
					for (int i = 0; i < this.items.size(); i++) {
						if(!this.hasSellers.containsKey(this.items.get(i).getSellerID()))
							sellerIDs.add(this.items.get(i).getSellerID());
					}
					for(Question q:questions){
						if(!hasSellers.containsKey(q.getFromUserID())&&!sellerIDs.contains(q.getFromUserID()))
							sellerIDs.add(q.getFromUserID());
					}
					for(Comment c:comments){
						if(!hasSellers.containsKey(c.getFromUserID())&&!sellerIDs.contains(c.getFromUserID()))
							sellerIDs.add(c.getFromUserID());
					}

					sellers = GlobalVars.DB.getUsers(sellerIDs);

		}
		catch(Exception e){
			errors.add(new Error("SearchControl: getHTML5Data: Could not read from data base when getting the items", e));
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

		if (this.items != null) {
			this.itemJS0N = new String[items.size()];

			for (int i = 0; i < this.itemJS0N.length; i++) {
				this.itemJS0N[i] = this.items.get(i).toJSON();
			}
		}

		if (this.sellers != null) {
			this.sellerJS0N = new String[sellers.size()];

			for (int i = 0; i < this.sellerJS0N.length; i++) {
				this.sellerJS0N[i] = this.sellers.get(i).toJSON();
			}
		}

		if (this.questions != null) {
			this.questionJSON = new String[questions.size()];

			for (int i = 0; i < this.questionJSON.length; i++) {
				this.questionJSON[i] = this.questions.get(i).toJSON();
			}
		}

		if (this.comments != null) {
			this.commentJSON=new String[comments.size()];
			for (int i = 0; i < this.commentJSON.length; i++) {
				this.commentJSON[i] = this.comments.get(i).toJSON();
			}
		}
		
		if(this.itemList!=null){
			this.prefetchJSON=new String[GlobalVars.PREFETCH_IMAGES_NUM];
			if(GlobalVars.PREFETCH_IMAGES==true){
				for(int i=0;i<Math.min(GlobalVars.PREFETCH_IMAGES_NUM, itemList.size());i++){
					this.prefetchJSON[i]=Long.toString(this.itemList.get(i));
				}
			}
		}

		super.stopTimerAddProcessing();
	}

	/**
	 * Return the JSON items
	 * @return
	 */
	public String[] getItemJSON(){
		return this.itemJS0N;
	}
	public String[] getSellerJSON(){
		return this.sellerJS0N;
	}
	public String[] getQuestionJSON(){
		return this.questionJSON;
	}
	public String[] getCommentJSON(){
		return this.commentJSON;
	}
	public String[] getPrefetchImagesJSON(){
		return this.prefetchJSON;
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
	 * Returns if a userID was sent to the page
	 * 
	 * @return boolean if a userID was passed and correct
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public Boolean hasUserID(){
		return this.userID > 0;
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
	 * Returns if a authToken was sent to the page
	 * 
	 * @return boolean if the authToken was present and correct
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public Boolean hasAuthToken(){
		return !(this.authToken == null || this.authToken.equals(EMPTY));
	}

	/**
	 * Returns the URLs for the links to the items returned by the search
	 * 
	 * @return String[] the URLs to the items thumbnails
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String[] getItemURLs(){
		return this.itemURLs;
	}

	/**
	 * Returns the page number that the user is currently viewing
	 * 
	 * @return int the current page number we are viewing
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public int getPageNo(){
		return this.pageNo;
	}

	/**
	 * Returns the URL to go to the previous page
	 * 
	 * @return String the URL to go to the previous page
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getPreviousPageURL(){
		return this.previousPageURL;
	}

	/**
	 * Returns the URL to go to the next page
	 * 
	 * @return String the URL to the next page
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getNextPageURL(){
		return this.nextPageURL;
	}

	/**
	 * Returns the list of items
	 * 
	 * @return ArrayList<Item> the list of items
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public ArrayList<Item> getItems(){
		return this.items;
	}

	public String getSortByEndURL(){
		return this.sortByEndURL;
	}

	public String getSortByPriceURL(){
		return this.sortByPriceURL;
	}
	public ArrayList<Long> getItemOrder(){
		return this.itemList;
	}
	public String getItemPrefetchImagesURLs(){
		return itemPrefetchImagesURLs;
	}
}
