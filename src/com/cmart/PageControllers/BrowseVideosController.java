package com.cmart.PageControllers;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalVars;
import com.cmart.util.Category;
import com.cmart.util.CheckInputs;
import com.cmart.util.Item;
import com.cmart.util.User;


public class BrowseVideosController extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();
	
	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
	private long categoryID = 0;
	private int pageNo = 0;
	private int itemsPP = 0;
	private int sortCol = 0;
	private Boolean sortDec = Boolean.FALSE;
	
	// Structures to hold the DB data
	private Category parentCategory = null;
	private ArrayList<Category> subCategories = null;
	private ArrayList<Item> items = null;
	private ArrayList<User> sellers = null;
	private String previousPageURL = "< Previous Page";
	private String nextPageURL = "Next Page >";
	private ArrayList<String> videos = null;
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
			videos = GlobalVars.DB.getVideos();
		}
		catch(Exception e){
				errors.add(new Error("BrowseCategoryControl: getHTML4Data: Could not read from data base when getting the items", e));
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
		
		for(int i=0;i<videos.size();i++){
			String temp = "<a href=\"./viewvideo.html?"+ (this.hasUserID() ? "userID=" + this.getUserIDString() +"&": "")
			+ (this.hasAuthToken() ? "authToken=" + this.getAuthTokenString() +"&": "")
			+ "video=" + videos.get(i)+"\">"+ videos.get(i)+"</a>";
			videos.set(i, temp);
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
		/*
		 * Get all of the info needed from the database
		 */
		super.startTimer();
		// Get all of the items in the category
		try{
			parentCategory = GlobalVars.DB.getCategory(this.categoryID);
		}
		catch(Exception e){
			errors.add(new Error("BrowseCategoryControl: getHTML4Data: Could not read from data base when getting parent category!", e));
		}
		
		// Get the parent category's sub-categories
		try{
			subCategories = GlobalVars.DB.getCategories(this.categoryID, 0);
		}
		catch(Exception e){
			errors.add(new Error("BrowseCategoryControl: getHTML4Data: Could not read from data base when getting the sub categories", e));
		}
		
		// Get all of the items in the category
		try{
			items = GlobalVars.DB.getCategoryItems(this.categoryID, this.pageNo, this.itemsPP, this.sortCol, this.sortDec,true, new String[0], 0);
		}
		catch(Exception e){
				errors.add(new Error("BrowseCategoryControl: getHTML4Data: Could not read from data base when getting the items", e));
		
		}
		try{
			ArrayList<Long> sellerIDs = new ArrayList<Long>();
			for (int i = 0; i < this.items.size(); i++) {
				sellerIDs.add(items.get(i).getSellerID());	
			}
			HashSet<Long> hs = new HashSet<Long>();
			hs.addAll(sellerIDs);
			sellerIDs.clear();
			sellerIDs.addAll(hs);
			sellers = GlobalVars.DB.getUsers(sellerIDs);
		}
		catch(Exception e){
				errors.add(new Error("BrowseCategoryControl: getHTML4Data: Could not read from data base when getting the items", e));
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
			this.sellerJS0N = new String[items.size()];
			for (int i = 0; i < items.size(); i++) {
				this.itemJS0N[i] = this.items.get(i).toJSON();	
			}
			for (int i = 0; i < sellers.size(); i++) {
				this.sellerJS0N[i] = this.sellers.get(i).toJSON();
			}
			
			if (this.subCategories != null) {
				this.categoryJSON = new String[subCategories.size() + 1];
				this.categoryJSON[0] = parentCategory.toJSON();
				for (int i = 1; i < this.categoryJSON.length; i++) {
					this.categoryJSON[i] = this.subCategories.get(i - 1).toJSON();
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
	
	public ArrayList<String> getVideos(){
		return this.videos;
	}
}
