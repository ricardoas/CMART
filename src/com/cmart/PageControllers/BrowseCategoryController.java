package com.cmart.PageControllers;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalVars;
import com.cmart.util.Category;
import com.cmart.util.CheckInputs;
import com.cmart.util.Comment;
import com.cmart.util.Item;
import com.cmart.util.User;
import com.cmart.util.Question;
import com.cmart.util.Image;
/**
 * This class processes the data for the browse category servlet
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

public class BrowseCategoryController extends PageController{
	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
	private long categoryID = 0;
	private int pageNo = 0;
	private int itemsPP = 0;
	private int sortCol = 0;
	private Boolean sortDec = Boolean.FALSE;
	private long catTs = 0;
	private long lastSeenID=0;
	private long lastSeenPrice=0;
	private String itemPrefetchImagesURLs=null;
	//private String[] hasItems = new String[0];
	private HashMap<Long, Long> hasSellers = new HashMap<Long, Long>();
	private HashMap<Long, Long> hasItems = new HashMap<Long, Long>();

	// Structures to hold the DB data
	private Category parentCategory = null;
	private ArrayList<Category> subCategories = null;
	private ArrayList<Item> items = null;
	private ArrayList<User> sellers = null;
	private ArrayList<Question> questions=null;
	private ArrayList<Comment> comments=null;
	private HashMap<Long, User> sellerIDs = null;
	private String previousPageURL = "< Previous Page";
	private String nextPageURL = "Next Page >";
	private ArrayList<Long> itemList;

	// Structures to hold the parsed page data
	private String parentCategoryURL = null;
	private String[] subCategoryURLs = null;
	private String[] itemURLs = null;
	private String sortByEndURL = null;
	private String sortByPriceURL = null;

	// Structure for HTML5 data
	private String[] itemJS0N = null;
	private String[] categoryJSON =null;
	private String[] sellerJS0N;
	private String[] questionJSON=null;
	private String[] commentJSON=null;
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

			// Get the category ID. If there isn't one we just start at the root category
			try{
				this.categoryID = CheckInputs.checkCategoryID(request);
			}
			catch(Error e){
				if(!errors.contains(e))
					errors.add(e);
				this.categoryID = 0;
			}

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

			this.catTs = CheckInputs.getTimestamp(request, "catTs");

			// Get the items that the user already has
			try {
				String h = CheckInputs.getParameter(request, "hasItems");
				if(h != null && h.contains(",")){
					String[] hasis = CheckInputs.getParameter(request, "hasItems").split(",");

					if (hasis.length > 0)
						for (int i = 0; i < hasis.length; i++){
							this.hasItems.put(Long.parseLong(hasis[i]), null);
						}
				}
			} catch (Exception e) {
				System.out.println("BrowseCategoryController checkInputs: hasItems could not be parsed");;
			}

			// Get the sellers that the user already has
			try {
				String h = CheckInputs.getParameter(request, "hasSellers");
				if(h != null && h.contains(",")){
					String[] hasis = CheckInputs.getParameter(request, "hasSellers").split(",");

					if (hasis.length > 0)
						for (int i = 0; i < hasis.length; i++){
							this.hasSellers.put(Long.parseLong(hasis[i]), null);
						}
				}
			} catch (Exception e) {
				System.out.println("BrowseCategoryController checkInputs: hasSellers could not be parsed");;
			}

			// Get the item that was at the end of the last search
			this.lastSeenID = CheckInputs.checkLastSeenID(request);
			this.lastSeenPrice = CheckInputs.checkLastSeenPrice(request);
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

		// Get the current parent category
		try{
			parentCategory = GlobalVars.DB.getCategory(this.categoryID);
		}
		catch(Exception e){
			errors.add(new Error("BrowseCategoryControl: getHTML4Data: Could not read from database when getting parent category!", e));
		}

		// Get the parent category's sub-categories
		try{
			subCategories = GlobalVars.DB.getCategories(this.categoryID, catTs);
		}
		catch(Exception e){
			errors.add(new Error("BrowseCategoryControl: getHTML4Data: Could not read from database when getting the sub categories", e));
		}

		// Get all of the items in the category
		try{
			if(GlobalVars.PREFETCH_IMAGES==false)
			items = GlobalVars.DB.getCategoryItems(this.categoryID, this.pageNo, this.itemsPP, this.sortCol, this.sortDec,false, new String[0], this.lastSeenID);
			else
				items = GlobalVars.DB.getCategoryItems(this.categoryID, this.pageNo, this.itemsPP, this.sortCol, this.sortDec,true,GlobalVars.PREFETCH_IMAGES_NUM, new String[0], this.lastSeenID);
		}
		catch(Exception e){
			errors.add(new Error("BrowseCategoryControl: getHTML4Data: Could not read from database when getting the items", e));
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

		// Make the parent category URL links
		if(parentCategory != null)
			this.parentCategoryURL = "<a href=\"./browsecategory?"
					+ (this.hasUserID() ? "userID=" + this.getUserIDString() +"&": "")
					+ (this.hasAuthToken() ? "authToken=" + this.getAuthTokenString() +"&": "")
					+ "pageNo=0" + "&"
					+ "itemsPP=" + this.itemsPP +"&"
					+ "categoryID=" + this.getCategoryID() +"&"
					+ "sortCol=" + this.sortCol +"&"
					+ "sortDec=" + (this.sortDec ? "1":"0")
					+ "\">" + parentCategory.getName() + "</a>";
		else this.parentCategoryURL = "";

		// Make the URL links for the sub-categories
		if(subCategories != null){
			int subCategoriesSize = subCategories.size();

			this.subCategoryURLs = new String[subCategoriesSize];
			for(int i=0; i < subCategoriesSize; i++){
				this.subCategoryURLs[i] = "<label for\"catlink" + i + "\"><a href=\"./browsecategory?"
						+ (this.hasUserID() ? "userID=" + this.getUserIDString() +"&": "")
						+ (this.hasAuthToken() ? "authToken=" + this.getAuthTokenString() +"&": "")
						+ "pageNo=0" + "&"
						+ "itemsPP=" + this.itemsPP +"&"
						+ "categoryID=" + subCategories.get(i).getCategoryID() +"&"
						+ "sortCol=" + this.sortCol +"&"
						+ "sortDec=" + (this.sortDec ? "1":"0")
						+ "\">" + subCategories.get(i).getName() + "</a></label>";
			}
		}
		else this.subCategoryURLs = new String[0];
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
					prefetchImagesURLs.append("'").append(GlobalVars.REMOTE_IMAGE_IP).append(GlobalVars.REMOTE_IMAGE_DIR).append(im.getUrl()).append("'");
				}
			}
		}
		else this.itemURLs = new String[0];

		itemPrefetchImagesURLs=prefetchImagesURLs.toString();

		// Make the URLs for sorting the data
		this.sortByEndURL = "<label for\"sortendlink\"><a href=\"./browsecategory?"
				+ (this.hasUserID() ? "userID=" + this.getUserIDString() +"&": "")
				+ (this.hasAuthToken() ? "authToken=" + this.getAuthTokenString() +"&": "")
				+ "pageNo=" + this.pageNo + "&"
				+ "itemsPP=" + this.itemsPP +"&"
				+ "categoryID=" + this.getCategoryID() +"&"
				+ "sortCol=2" +"&"
				+ "sortDec=" + (this.sortCol==2 ? (this.sortDec ? "0":"1") : (this.sortDec ? "1":"0"))
				+ "\">End Date</a></label>";

		this.sortByPriceURL = "<label for\"sortpricelink\"><a href=\"./browsecategory?"
				+ (this.hasUserID() ? "userID=" + this.getUserIDString() +"&": "")
				+ (this.hasAuthToken() ? "authToken=" + this.getAuthTokenString() +"&": "")
				+ "pageNo=" + this.pageNo + "&"
				+ "itemsPP=" + this.itemsPP +"&"
				+ "categoryID=" + this.getCategoryID() +"&"
				+ "sortCol=1" +"&"
				+ "sortDec=" + (this.sortCol==1 ? (this.sortDec ? "0":"1") : (this.sortDec ? "1":"0"))
				+ "\">Bid Price</a></label>";

		// Get the ID and price to send as the 'last seen' item ID
		long lastItemID=0;
		long firstItemID=0;

		long lastItemPrice = 0;
		long firstItemPrice = 0;

		if(this.items!=null && !this.items.isEmpty()){
			lastItemID = this.items.get(this.items.size()-1).getID();
			firstItemID = this.items.get(0).getID();

			lastItemPrice = (long)(this.items.get(this.items.size()-1).getCurrentBid()*100);
			firstItemPrice = (long)(this.items.get(0).getCurrentBid()*100);
		}

		// Make the URLs for the previous and next page
		// If we are further than page zero we can go backwards
		if(this.pageNo>0)
			this.previousPageURL = "<a href=\"./browsecategory?"
					+ (this.hasUserID() ? "userID=" + this.getUserIDString() +"&": "")
					+ (this.hasAuthToken() ? "authToken=" + this.getAuthTokenString() +"&": "")
					+ "pageNo=" + (this.pageNo-1) +"&"
					+ "itemsPP=" + this.itemsPP +"&"
					+ "categoryID=" + this.getCategoryID() +"&"
					+ "sortCol=" + this.sortCol +"&"
					+ "sortDec=" + (this.sortDec ? "1":"0") + "&"
					+ "lastSeenID=" + firstItemID
					+ "\">< Previous Page</a>";

		// If there are less items remaining than we want to display then there are no more pages
		// For now we will allow this link if any results are returned as cassandra may return dead keys
		if(this.items != null && this.items.size() > 0)
			this.nextPageURL = "<a href=\"./browsecategory?"
					+ (this.hasUserID() ? "userID=" + this.getUserIDString() +"&": "")
					+ (this.hasAuthToken() ? "&authToken=" + this.getAuthTokenString() +"&": "")
					+ "pageNo=" + (this.pageNo+1) +"&"
					+ "itemsPP=" + this.itemsPP +"&"
					+ "categoryID=" + this.getCategoryID() +"&"
					+ "sortCol=" + this.sortCol +"&"
					+ "sortDec=" + (this.sortDec ? "1":"0") + "&"
					+ "lastSeenID=" + lastItemID
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

		// Get the top category
		try{
			parentCategory = GlobalVars.DB.getCategory(this.categoryID);
		}
		catch(Exception e){
			errors.add(new Error("BrowseCategoryControl: getHTML5Data: Could not read from data base when getting parent category!", e));
		}

		// Get the parent category's sub-categories
		try{
			subCategories = GlobalVars.DB.getCategories(this.categoryID, catTs);
		}
		catch(Exception e){
			errors.add(new Error("BrowseCategoryControl: getHTML5Data: Could not read from data base when getting the sub categories", e));
		}

		// Get the list of items to be displayed
		try{
			itemList = GlobalVars.DB.getCategoryItemsIDs(this.categoryID, this.pageNo, this.itemsPP, this.sortCol, this.sortDec, this.lastSeenID);

			// Make a list of items we need to get for the user
			ArrayList<Long> getList = new ArrayList<Long>();
			for(Long i : itemList){

				if(!this.hasItems.containsKey(i)){
					getList.add(i);

				}
			}

			if(GlobalVars.PREFETCH_IMAGES==false)
			items =	GlobalVars.DB.getItemsByID(getList, this.sortCol, this.sortDec);
			else
				items =	GlobalVars.DB.getItemsByID(getList, this.sortCol, this.sortDec,true,GlobalVars.PREFETCH_IMAGES_NUM);
			// Get the questions for the items on the page
			questions= GlobalVars.DB.getQuestions(getList);
			comments= GlobalVars.DB.getComments(getList);
		}
		catch(Exception e){
			errors.add(new Error("BrowseCategoryControl: getHTML5Data: Could not read from data base when getting the items", e));

		}
		// Get all of the items in the category
		/*try{
			items = GlobalVars.DB.getCategoryItems(this.categoryID, this.pageNo, this.itemsPP, this.sortCol, this.sortDec, true, this.hasItems);
		}
		catch(Exception e){
				errors.add(new Error("BrowseCategoryControl: getHTML5Data: Could not read from data base when getting the items", e));

		}*/

		// Get all of the sellers the user doesn't have
		try{
			ArrayList<Long> allIDs = new ArrayList<Long>();

			for (int i = 0; i < this.items.size(); i++) {
				if(!hasSellers.containsKey(items.get(i).getSellerID()))
					allIDs.add(items.get(i).getSellerID());	
			}
			for(Question q:questions){
				if(!hasSellers.containsKey(q.getFromUserID())&&!allIDs.contains(q.getFromUserID()))
					allIDs.add(q.getFromUserID());
			}
			for(Comment c:comments){
				if(!hasSellers.containsKey(c.getFromUserID())&&!allIDs.contains(c.getFromUserID()))
					allIDs.add(c.getFromUserID());
			}

			sellers = GlobalVars.DB.getUsers(allIDs);
		}
		catch(Exception e){
			errors.add(new Error("BrowseCategoryControl: getHTML5Data: Could not read from data base when getting the items", e));
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

		if(parentCategory==null){
			parentCategory=new Category(0, "Please select", 0, 0);
		}
		
		if (this.items != null) {
			this.itemJS0N = new String[items.size()];

			for (int i = 0; i < items.size(); i++) {
				this.itemJS0N[i] = this.items.get(i).toJSON();	
			}
	
		}
		
		if (this.sellers != null) {
			this.sellerJS0N = new String[sellers.size()];
			for (int i = 0; i < sellers.size(); i++) {
				this.sellerJS0N[i] = this.sellers.get(i).toJSON();
			}
		}
		
		if (this.questions != null) {
			this.questionJSON=new String[questions.size()];
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
		
		if (this.subCategories != null) {
			this.categoryJSON = new String[subCategories.size() + 1];
			this.categoryJSON[0] = parentCategory.toJSON();
			for (int i = 1; i < this.categoryJSON.length; i++) {
				this.categoryJSON[i] = this.subCategories.get(i - 1).toJSON();
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

	/**
	 * Return the JSON categories
	 * 
	 * @return
	 */
	public String[] getCategoryJSON(){
		return this.categoryJSON;
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
	 * Returns the categoryID sent to the page
	 * 
	 * @return int the categoryID
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public long getCategoryID(){
		return this.categoryID;
	}

	/**
	 * Returns the URL string for the link back to the parent category
	 * 
	 * @return String the parent category URL
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getParentCategoryURL(){
		return this.parentCategoryURL;
	}

	/**
	 * Returns the URLs for the links to the sub-categories
	 * 
	 * @return String[] the sub caregory URLs
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String[] getSubCategoryURLs(){
		return this.subCategoryURLs;
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
