package com.cmart.DB;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import javax.servlet.ServletContextEvent;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import com.cmart.Data.GlobalVars;
import com.cmart.util.Account;
import com.cmart.util.Address;
import com.cmart.util.Bid;
import com.cmart.util.Category;
import com.cmart.util.Image;
import com.cmart.util.Item;
import com.cmart.util.Purchase;
import com.cmart.util.User;
import com.cmart.util.Comment;
import com.cmart.util.Question;
import com.cmart.util.VideoItem;

/**
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

public abstract class DBQuery {
	// Data needed to access the database
	protected static String URL = null;
	protected static String DRIVER = null;
	protected static String USERNAME = null; 
	protected static String PASSWORD = null;
	protected static int SQL_RETRIES = 1;

	protected static Random rand = new Random();
	
	// Common variables needed to write data
	//protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Random generator = new Random();
	
	// Connection caches
	protected static LinkedList<Connection> connections = new LinkedList<Connection>();
	protected static boolean useConnectionPool = true;
	
	// Solr server
	CommonsHttpSolrServer SOLR_SERVER = null;
	private static final double solrCommit = 0.1; // Solr can overload due to very frequent commits
	
	protected DBQuery(){
		if(GlobalVars.SOLR_ENABLED) initSolr();
	}
	
	/**
	 * Generates a random integer
	 * @return The next random integer
	 */
	public int getRand() {
		return generator.nextInt();
	}
	
	/**
	 * Generates a random authToken based (in part) on the user's userID
	 * Auth tokens should be unique between users with different IDs and in respect to time
	 * 
	 * @param The user's id
	 * @returns Randomly generated auth token
	 * 
	 * Example
	 * userID=1, time=1 -> "dfsfgsa"
	 * userID=2, time=1 -> "uytiuyr"
	 * userID=1, time=2 -> "erewtgg"
	 */
	public String authToken(long id){
		// Generate a random number using rand, userID and current time
		String result = null;
		String authToken = "auth:" + Math.abs(getRand()) + id + System.currentTimeMillis();
		
		// Convert the number to an MD5 hash
		byte[] bytesOfMessage=null;
		MessageDigest md = null;
			
		try {
			bytesOfMessage = authToken.getBytes("UTF-8");
			md = MessageDigest.getInstance("MD5");
			
			byte[] thedigest = md.digest(bytesOfMessage);
			authToken = thedigest.toString();
			result = authToken;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Returns all of the current connections. Used to loop through all of the connections, e.g. to close them all
	 * @return All of the pooled/current connections
	 */
	public LinkedList<Connection> getConnections(){
		return connections;
	}
	
	/**
	 * Opens the connection to the Solr server if Solr is enabled in GlobalVars
	 */
	public void initSolr(){
		if(GlobalVars.SOLR_ENABLED)
			try {
				SOLR_SERVER = new CommonsHttpSolrServer(GlobalVars.SOLR_URL);
				SOLR_SERVER.setDefaultMaxConnectionsPerHost(GlobalVars.SOLR_MAX_CONNS_PER_HOST);
				
				SOLR_SERVER.setSoTimeout(10000);
				SOLR_SERVER.setConnectionTimeout(1000);
				SOLR_SERVER.setMaxTotalConnections(500);
				SOLR_SERVER.setFollowRedirects(false);
				SOLR_SERVER.setParser(new XMLResponseParser());
			} catch (MalformedURLException e) {
				e.printStackTrace();
				System.out.println("initSolr (DBQuery): Could not connect to Solr server");
			}
	}
	
	/**
	 * As Solr can get easily overloaded, we may not always want to commit every operation
	 */
	public void forceCommitSolr(){
		if(!GlobalVars.SOLR_ENABLED && SOLR_SERVER!=null)
			try {
				SOLR_SERVER.commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * Adds an item to the Solr server so that it can be searched as a seperate tier
	 * @param id The items ID
	 * @param name The name of the item
	 * @param description The description of the item
	 * @param currentBid The current bid for the item
	 * @param endDate The date the auction ends
	 * @return true if the item was inserted, false otherwise
	 */
	public boolean addToSolr(long id, String name, String description, double currentBid, Date endDate){
		if(name==null || description==null || endDate==null || id<0 || currentBid<0.0) return false;
		if(!GlobalVars.SOLR_ENABLED) return false;
		
		// Make the new solr document
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", id);
		doc.addField("name", name, 1.0f);
		doc.addField("description", description, 1.0f);
		doc.addField("endDate", endDate);
		doc.addField("currentBid", currentBid, 1.0f);
		
		// Try to add the document to the server, and perhaps commit it
		try {
			SOLR_SERVER.add(doc);
			if(rand.nextDouble()<solrCommit) SOLR_SERVER.commit();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Searches the Solr server for itemIDs that contain the given text are should be present on the currently viewed page.
	 * If no text is passed, we will search for any items
	 * @param text The text that should be searched for
	 * @param page The page number the users is looking at
	 * @param itemsPP The maz number of items that should be returned
	 * @param sortCol How the items should be sorted
	 * @param sortDec If the data should be sorted ascending or descending
	 * @return
	 */
	public ArrayList<Long> getTextItemsSolr(String text, int page, int itemsPP, int sortCol, Boolean sortDec){
		ArrayList<Long> results = new ArrayList<Long>();
		
		if(page<0 || itemsPP<=0 || sortDec==null) return results;
		
		// If no text is passed, search for anything
		if(text == null || text.trim().equals(""))
			text = "*";
		
		//TODO: OR and AND brackets
		// Setup the search query
		SolrQuery query = new SolrQuery();
		StringBuilder queryText = new StringBuilder();
		queryText.append("name:");
		queryText.append(text);
		queryText.append(" OR description:");
		queryText.append(text);
		queryText.append(" AND endDate:[NOW TO *]");
		queryText.append("&start=");
		queryText.append(page*itemsPP);
		queryText.append("&rows=");
		queryText.append(itemsPP);
		queryText.append("&fl=id");
		
		// Set how the data should be sorted
		switch(sortCol){
			case 0: query.addSortField("endDate", sortDec ? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc); break;
			case 1: query.addSortField("currentBid", sortDec ? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc); break;
			case 2: query.addSortField("endDate", sortDec ? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc); break;
			default: query.addSortField("endDate", sortDec ? SolrQuery.ORDER.desc : SolrQuery.ORDER.asc); break;
		}

		query.setQuery(queryText.toString());
		
		// Send the query to the server and get the itemIDs
		QueryResponse rsp;
		try {
			rsp = SOLR_SERVER.query(query);
			Iterator<SolrDocument> iter = rsp.getResults().iterator();

			while (iter.hasNext()) {
				SolrDocument resultDoc = iter.next();
				
				String id = (String) resultDoc.getFieldValue("id");
				if(id != null)
					results.add(Long.valueOf(id));
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}

		return results;
	}
	
	/**
	 * Delete an item from the Solr database
	 * @param itemID The item to delete
	 */
	public void deleteFromSolr(String itemID){
		try {
			SOLR_SERVER.deleteById(itemID);
			if(rand.nextDouble()<solrCommit) SOLR_SERVER.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Update the price of an item in the Solr database. This occurs when a user bids for an item
	 * @param itemID The item to update
	 * @param bidPrice The new price of the item
	 */
	public void updateSolr(long itemID, double bidPrice){
		SolrQuery query = new SolrQuery("id:" + itemID);
		
		QueryResponse rsp = null;
		
		try {
			rsp = SOLR_SERVER.query(query);
			
			Iterator<SolrDocument> iter = rsp.getResults().iterator();
			if(iter.hasNext()) {
				SolrDocument resultDoc = iter.next();
				
				String id = (String) resultDoc.getFieldValue("id");
				String name = (String) resultDoc.getFieldValue("name");
				String description = (String) resultDoc.getFieldValue("description");
				Date endDate = (Date) resultDoc.getFieldValue("endDate");
				
				addToSolr(Long.valueOf(id), name, description, bidPrice, endDate);
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected abstract void init();
	
	protected abstract Connection initConnection();
	
	protected abstract Connection getConnection();
	
	protected abstract void closeConnection(Connection conn);
	
	
	/**
	 * Check that a user's user name and password is correct
	 * @param username The username that the user has entered. Must return '-1' if username is equal to NULL.
	 * @param password The password that the user has entered. Must return '-1' is password is equal to NULL.
	 * @return The userID of the user if the username and password are correct. Otherwise returns -1.
	 * 				
	 */
	public abstract long checkUsernamePassword(String username, String password);
	
	
	/**
	 * Makes a new auth token for a user and updates the value in the database to the value of the new auth token
	 * @param userID The userID of the user to create a new auth token for. Must return NULL if the userID is less than 1
	 * @return The new auth token if the update to the auto token was successful, NULL otherwise. 
	 */
	public abstract String makeNewAuthToken(long userID);
	
	/**
	 * Checks if the auth token passed matches the auth token in the database for the given userID
	 * @param userID The user to be checked
	 * @param authToken The auth token we expect the user to have
	 * @return TRUE if the auth token passed matches the auth token in the database, FALSE otherwise. Returns FALSE if userID<1 or auth token is NULL.
	 */
	public abstract Boolean checkAuthToken(long userID, String authToken);
	
	/**
	 * Logs a user out by setting the auth token in the database to NULL. As NULL is not an acceptable auth token to check against, no one can
	 * pretend to be the user without correctly logging back in and recreating the auth token with the correct username and password.
	 * 
	 * To prevent other people logging a user out, the application code would want to check the user's userID/authToken match before calling this method.
	 * 
	 * @param userID The user to be logged out
	 * @return TRUE if the user's authToken was set to NULL, false otherwise. Returns FALSE if the userID is less than 1.
	 */
	public abstract Boolean logout(long userID);
	
	/**
	 * Inserts an address for the user in to the database
	 * @param userID The userID of the person who lives at the address
	 * @param street The adress's street
	 * @param town The adress's town
	 * @param zip The address's zip
	 * @param state The address's state
	 * @param isDefault Whether the address is the users default address
	 * @return TRUE if the address was correctly inserted, FLASE otherwise. FALSE if userID or state < 1 or if other variables are NULL.
	 */
	public abstract Boolean insertAddress(long userID, String street, String town, String zip, int state, Boolean isDefault);
	
	/**
	 * Updates a user's address
	 * @param id The id of the address to be updated
	 * @param userID The userID of the user who lives at the address
	 * @param street The new street
	 * @param town The new town
	 * @param zip The new zip code
	 * @param state The new state
	 * @param isDefault Whether the address is the user default address
	 * @return TRUE if the address is correctly updated, FALSE otherwise. FALSE if the address id and userID do now match
	 * 			i.e. they are trying to update someone else's address. FALSE if id, userID or state < 1. FALSE is other
	 * 			values are NULL
	 */
	public abstract Boolean updateAddress(long id, long userID, String street, String town, String zip, int state, Boolean isDefault);
	
	/**
	 * Inserts an item
	 * @param userID
	 * @param name
	 * @param description
	 * @param startPrice
	 * @param reservePrice
	 * @param buyNowPrice
	 * @param quantity
	 * @param endDate
	 * @param categoryID
	 * @return -1 if item not inserted. userID < 1 categoryID < 1, other fields null, otherwise the itemID
	 */
	public long insertItem(long userID, String name, String description, double startPrice, double reservePrice, double buyNowPrice, int quantity, Date endDate, long categoryID){		
		long ret = insertItemDB(userID, name, description, startPrice, reservePrice, buyNowPrice, quantity, endDate, categoryID);
		
		if(GlobalVars.SOLR_ENABLED && ret > 0)
			addToSolr(ret, name, description, 0.0, endDate);
		
		return ret;
	}
	
	/**
	 * Inserts an item in to the items table, typically an item sell. set's the thumbnail to be "blank.jpg" by default
	 * @param userID The user inserting the item
	 * @param name The name of the item
	 * @param description The description of the item
	 * @param startPrice The items start price
	 * @param reservePrice The items reserve price
	 * @param buyNowPrice The items buy now price
	 * @param quantity The number of items to be sold
	 * @param endDate The date the auction ends
	 * @param categoryID The category that the item is to be sold in
	 * @return The itemID number of the item inserted. If the item was not inserted then -1 is returned. 
	 * 			-1 of the item name, description, endDate are null, or the userID or categoryID is less than 1
	 * 			or if any of the prices are less than zero, or if the quantity is less than one
	 */
	protected abstract long insertItemDB(long userID, String name, String description, double startPrice, double reservePrice, double buyNowPrice, int quantity, Date endDate, long categoryID);
	
	/**
	 * Gets an item from the database. The item is from the 'items' table, not the 'oldItems' table
	 * @param itemID The id for the item to be retrieved
	 * @param getImages
	 * @return The Item object if the item is present in the table, NULL otherwise. NULL if the itemID is less than 1
	 */
	public abstract Item getItem(long itemID, Boolean getImages);
	
	/**
	 * Gets an item from the database. The item is from the 'oldItems' table, not the 'items' table
	 * @param itemID The id for the item to be retrieved
	 * @param getImages
	 * @param getAllBids
	 * @return The Item object if the item is present in the table, NULL otherwise. NULL if the itemID is less than 1
	 */
	public abstract Item getOldItem(long itemID, Boolean getImages);
	
	/**
	 * Get the items that a user is currently selling
	 * @param userID The id of the user we want to get items for
	 * @return An array list of all of the items. An empty array list is there are no items or userID < 1
	 */
	public abstract ArrayList<Item> getCurrentSellingItems(long userID, long ts);
	
	/**
	 * Gets the items that a user has previously sold
	 * @param userID The id of the user we want to get items for
	 * @return An array list of all of the items. An empty array list is there are no items or userID < 1
	 */
	public abstract ArrayList<Item> getOldSellingItems(long userID, long ts);
	
	/**
	 * Inserts a user's bid for an item in to the database. It must also update the Item to inculde the new maxBid price
	 * that the user is willing to pay. We must ensure that there are no concurrency problems and that only one person
	 * is buy/bidding at a time on an item. Return -1.0 if user or item <1, or quantity<1 if bid<=0 or bid<maxbid
	 * @param userID The user bidding
	 * @param itemID The item they are bidding for
	 * @param quantity The quantity of the item they want to buy
	 * @param bid The current amount they are bidding
	 * @param maxBid The maximum amount they will bid
	 * @return The current bid price if the bid was correctly inserted (the current item bid price, not just the user's bid). -1.0 if the bid was not inserted or userID or itemID < 1
	 */
	protected abstract double insertBidDB(long userID, long itemID, int quantity, double bid, double maxBid);
	
	public double insertBid(long userID, long itemID, int quantity, double bid, double maxBid){
		double price = insertBidDB(userID, itemID, quantity, bid, maxBid);
		
		if(GlobalVars.SOLR_ENABLED && price>0.0){
			updateSolr(itemID, price);
		}
		
		return price;
	}
	
	public Boolean buyItemNow(long userID, long itemID, int quantity, Account account, Address address){
		Boolean ret = buyItemNowDB(userID, itemID, quantity, account, address);
		
		if(GlobalVars.SOLR_ENABLED && ret != null && ret.equals(Boolean.TRUE))
			deleteFromSolr(String.valueOf(itemID));
		
		return ret;
	}
	
	/**
	 * Buys an item for a user. Checks the quantity is sufficient, inserts their payment, inserts that they have purchased
	 * the item, updates the items quantity, moves the item and bids to the old tables if the quantity is now zero
	 * (i.e. the auction has finished)
	 * @param userID The user buying the item
	 * @param itemID The item the user is buying
	 * @param quantity The quantity the user would like to buy
	 * @param account The accountID that the user is paying with. Can be a new account with an id of less than 0 (i.e. an account we do not have on record)
	 * @param address The address that they user is billing to. 
	 * @return TRUE if the purchase was successful. FALSE if userID or itemID < 1, or if account or address is NULL, or if purchase was unsuccessful or item is not for buy now (buy now price=0.0)
	 */
	protected abstract Boolean buyItemNowDB(long userID, long itemID, int quantity, Account account, Address address);
	
	/**
	 * Updates an item in the database with a new thumbnail image
	 * @param itemID The item to be updated
	 * @param URL The url to the thumbnail
	 * @return TRUE if the thumbnail was successfully inserted. FALSE if itemID < 1, URL = NULL or not updated
	 */
	public abstract Boolean insertThumbnail(long itemID, String URL);
	
	/**
	 * Inserts a link in the database from an item to an image
	 * @param itemID The item that the image belongs to
	 * @param position The position that the image should appear when displayed
	 * @param URL The URL to the image
	 * @param description The alt description of the image
	 * @return TRUE if the image was successfully inserted. FALSE if itemID < 1, position<0, URL or description = NULLor not updated
	 */
	public abstract Boolean insertImage(long itemID, int position, String URL, String description);
	
	public abstract Boolean insertVideo(String URL);
	/**
	 * Returns all of the images associated with an item in the correct position order
	 * @param itemID The item to get the images for
	 * @return ArrayList of images for the item. An empty array if the itemID < 1 or there are no images
	 */
	public abstract ArrayList<Image> getItemImages(long itemID);
	
	/**
	 * Returns all of the purchases that a user has made
	 * @param userID The id of the user to get the purchases for
	 * @return ArrayList of purchases. An empty array if there are no purchases or the userID is less than 1
	 */
	public abstract ArrayList<Purchase> getPurchases(long userID, long ts);
	
	/**
	 * Gets a user's current bids. Only returns their highest bid for an item, not every bid
	 * @param userID The user ID to get the bids for
	 * @return ArrayList of the user's bids. An empty array if there are no bids or the userID < 1
	 */
	public abstract ArrayList<Bid> getCurrentBids(long userID, long ts);
	
	/**
	 * Gets the items that a user has previously bid for. Only returns their highest bid for an item, not every bid
	 * @param userID The user ID of the user to get the bids for
	 * @return ArrayList of the user's previous bids. An empty array if there are no previous bids or the userID < 1
	 */
	public abstract ArrayList<Bid> getOldBids(long userID, long ts);
	
	/**
	 * Gets the bids corresponding to an item. Used to display the bidding history of an item
	 * @param itemID the itemID of the item to get the bids for
	 * @return ArrayList of the item's bids
	 */
	public abstract ArrayList<Bid> getBids(long itemID);
	
	/**
	 * Inserts a user into the database. Before calling this method you should check that the username and e-mail are not taken
	 * @param username The user's username
	 * @param password The user's password
	 * @param email The user's email address
	 * @param firstName The user's first name
	 * @param lastName The user's last name
	 * @return TRUE if the user was successfully inserted. FALSE if nay passed value is NULL, or the user is not inserted
	 */
	public abstract Boolean insertUser(String username, String password, String email, String firstName, String lastName);
	
	/**
	 * Updates a user's details in the database
	 * @param userID
	 * @param password
	 * @param email
	 * @param firstName
	 * @param lastName
	 * @return TRUE if the user's details are successfully updated. FALSE if userID < 1, other variables are NULL or the updated was not successful
	 */
	public abstract Boolean updateUser(long userID, String password, String email, String firstName, String lastName);
	
	/**
	 * Gets a users first name if we want to personalize messages to them
	 * @param userID The user whose name to get
	 * @return The user's first name if present, NULL if the userID < 1 or the the request could not be completed
	 */
	public abstract String getFirstName(long userID);
	
	/**
	 * Gets a User object with only the information that is public. i.e. not password or real name
	 * @param userID The user whose public info we want to get
	 * @return A User object with public information if the userID is valid. NULL if the userID < 1 or the request fails
	 */
	public abstract User getPublicUser(long userID);
	
	/**
	 * Gets a user's information - even the private stuff.
	 * @param userID The user whose information we are getting
	 * @return A User object with the user's details if it exists. NULL if the userID < 1 or the request fails
	 */
	public abstract User getUser(long userID);
	
	/**
	 * Gets a user's address via its address id
	 * @param addressID the addressID to get
	 * @return An address if the address exists. NULL if the address does not exist or addressID < 1
	 * @throws Exception
	 */
	public abstract Address getAddress(long addressID) throws Exception;
	
	/**
	 * Gets a user's default address if it exists
	 * @param userID
	 * @return An Address if the user has a default address. NULL if there is no default address, the userID < 1 or the request fails
	 * @throws Exception
	 */
	public abstract Address getDefaultAddress(long userID) throws Exception;
	
	/**
	 * Checks if a username exists. Can be used to check if a username is free before inserting it
	 * @param username The username we want to check for
	 * @return TRUE if the username is in the database, or username is null,  or the query was not executed, FALSE only if the username exists
	 * @throws Exception
	 */
	public abstract Boolean usernameExists(String username) throws Exception;
	
	/**
	 * Checks if an email is already present in the database
	 * @param email The e-mail address we want to check for
	 * @return TRUE if the email is present, email is null or we could not run the query. FALSE only if the email is not present
	 */
	public abstract Boolean emailExists(String email);
	
	/**
	 * Gets a user's account from the database
	 * @param accountID The id of the account to get
	 * @return Account object if the account exists. NULL if the account does not exist or the accountID < 1
	 * @throws Exception
	 */
	public abstract Account getAccount(long accountID) throws Exception;
	
	
	/**
	 * Gets a category from the database for a given categoryID
	 * @param categoryID The category id that we are looking for
	 * @return A Category if it exists. NULL is categoryID < 1 or the category does not exist
	 * @throws Exception
	 */
	public abstract Category getCategory(long categoryID) throws Exception;
	
	/**
	 * Gets all of the categories in the database
	 * @return An ArrayList of categories. A blank array if the request fails
	 * @throws Exception
	 */
	public abstract ArrayList<Category> getAllCategories() throws Exception;
	
	/**
	 * Gets all of the categories that have changed after the time stamp value
	 * @param timestamp
	 * @return
	 * @throws Exception
	 */
	//public abstract ArrayList<Category> getAllCategories(long timestamp) throws Exception;
	
	/**
	 * Gets all categories that have a given parent
	 * @param parent The parent category
	 * @return An ArrayList containing the children categories. An Empty array if there are no results or the query fails, or if parent < 0 (NOT 1, zero is the special root)
	 * @throws Exception
	 */
	public abstract ArrayList<Category> getCategories(long parent, long timestamp) throws Exception;
	
	/**
	 * Gets all of the states as an array of String
	 * @return ArrayList of String arrays that contain the states details - id, short name, long name
	 * @throws Exception
	 */
	public abstract ArrayList<String[]> getStates() throws Exception;
	
	/**
	 * Returns all of the items for a given category and page
	 * @param categoryID
	 * @param page
	 * @param itemsPP
	 * @param sortCol 0=date, 1=bid price
	 * @param sortDec earliest/lowest first
	 * @return
	 * @throws Exception
	 */
	public abstract ArrayList<Item> getCategoryItems(long categoryID, int page, int itemsPP, int sortCol, Boolean sortDec, Boolean images, String[] hasItems, long lastSeenID)  throws Exception;
	
	/**
	 * Returns all of the items for a given category and page
	 * @param categoryID
	 * @param page
	 * @param itemsPP
	 * @param sortCol 0=date, 1=bid price
	 * @param sortDec earliest/lowest first
	 * @return
	 * @throws Exception
	 */
	public abstract ArrayList<Item> getCategoryItems(long categoryID, int page, int itemsPP, int sortCol, Boolean sortDec, Boolean images, int numImages, String[] hasItems, long lastSeenID)  throws Exception;
		
	
	
	/**
	 * Returns all of the item IDs for a given category and page
	 * @param categoryID
	 * @param page
	 * @param itemsPP
	 * @param sortCol 0=date, 1=bid price
	 * @param sortDec earliest/lowest first
	 * @param lastSeenID The ID of the item that was last in the previously returned results, can set
	 * to -1 or 0 etc if there was no previous items
	 * @return Array List of item IDs
	 * @throws Exception
	 */
	public abstract ArrayList<Long> getCategoryItemsIDs(long categoryID, int page, int itemsPP, int sortCol, Boolean sortDec, long lastSeenID) throws Exception;
	
	/**
	 * Searches for items 
	 * @param text
	 * @param page
	 * @param itemsPP
	 * @param sortCol
	 * @param sortDec
	 * @return returns array of items that have the text in the name or the description
	 * @throws Exception
	 */
	public ArrayList<Item> getTextItems(String text, int page, int itemsPP, int sortCol, Boolean sortDec) throws Exception{
		if(GlobalVars.SOLR_ENABLED){
			return getItemsByID(getTextItemsSolr(text, page, itemsPP, sortCol, sortDec), sortCol, sortDec);
		}
		else
			return getTextItemsDB(text, page, itemsPP, sortCol, sortDec);
	}
	
	/**
	 * Searches for items 
	 * @param text
	 * @param page
	 * @param itemsPP
	 * @param sortCol
	 * @param sortDec
	 * @return returns array of items that have the text in the name or the description
	 * @throws Exception
	 */
	public ArrayList<Item> getTextItems(String text, int page, int itemsPP, int sortCol, Boolean sortDec,Boolean getimages,int numImages) throws Exception{
		if(GlobalVars.SOLR_ENABLED){
			return getItemsByID(getTextItemsSolr(text, page, itemsPP, sortCol, sortDec), sortCol, sortDec,getimages,numImages);
		}
		else
			return getTextItemsDB(text, page, itemsPP, sortCol, sortDec,getimages,numImages);
	}
	
	public ArrayList<Long> getTextItemsIDs(String text, int page, int itemsPP, int sortCol, Boolean sortDec) throws Exception{
		if(GlobalVars.SOLR_ENABLED)
			return getTextItemsSolr(text, page, itemsPP, sortCol, sortDec);
		else
			return getTextItemsIDsDB(text, page, itemsPP, sortCol, sortDec);
	}
	
	public abstract ArrayList<Long> getTextItemsIDsDB(String text, int page, int itemsPP, int sortCol, Boolean sortDec)  throws Exception;
	
	public abstract ArrayList<Item> getTextItemsDB(String text, int page, int itemsPP, int sortCol, Boolean sortDec) throws Exception;
	
	public abstract ArrayList<Item> getTextItemsDB(String text, int page, int itemsPP, int sortCol, Boolean sortDec, Boolean getimages, int numImages) throws Exception;
	
	/**
	 * Gets items from their IDs
	 * @param itemIDs
	 * @param sortCol
	 * @param sortDec
	 * @return
	 * @throws Exception
	 */
	public abstract ArrayList<Item> getItemsByID(ArrayList<Long> itemIDs, int sortCol, Boolean sortDec) throws Exception;
	
	/**
	 * Gets items from their IDs
	 * @param itemIDs
	 * @param sortCol
	 * @param sortDec
	 * @param getimages
	 * @param numimages
	 * @return
	 * @throws Exception
	 */
	public abstract ArrayList<Item> getItemsByID(ArrayList<Long> itemIDs, int sortCol, Boolean sortDec, Boolean getimages, int numImages) throws Exception;
	
	/**
	 * Removes expired items from Solr, then calls to have them moved in the database
	 * @return The number of items moved
	 * @throws Exception
	 */
	public long moveEndedItems() throws Exception{
		if(GlobalVars.SOLR_ENABLED){	
			SolrQuery query = new SolrQuery();
			StringBuilder queryText = new StringBuilder();
			queryText.append("endDate:[* TO NOW]");
			queryText.append("&fl=id");

			query.setQuery(queryText.toString());
			
			QueryResponse rsp;
			try {
				rsp = SOLR_SERVER.query(query);
				
				//SolrDocumentList docsr = rsp.getResults();

				Iterator<SolrDocument> iter = rsp.getResults().iterator();

				while (iter.hasNext()) {
					SolrDocument resultDoc = iter.next();
					
					String id = (String) resultDoc.getFieldValue("id");
					if(id != null)
						this.deleteFromSolr(id);
				}
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return moveEndedItemsDB();
	}
	
	/**
	 * Moves items and bids from the item and bids table to the old items and bids
	 * tables if the auction has finished
	 * @return The number of items removed
	 * @throws Exception
	 */
	protected abstract long moveEndedItemsDB() throws Exception;
	
	/**
	 * Used by the client emulator to get user's data at the start of a run
	 * @param itemsPerPage The number of users to return
	 * @param pageNo The offset from the start of the user list
	 * @return ArrayList containing users
	 */
	public abstract ArrayList<User> getAllUserData(int itemsPerPage, int pageNo);
	
	public abstract long getUserCount();
	
	/**
	 * Gets all of the users from a list of IDs
	 * @param userIDs The users to get
	 * @return ArrayList of users
	 */
	public abstract ArrayList<User> getUsers(ArrayList<Long> sellerIDs);
	
	
	//public abstract String getStateName(int stateID);

	public abstract ArrayList<String> getVideos();
	
	

	//public abstract boolean insertAccount(String name, String nameOnCard, String creditCardNo, String cvv, Date expirationDate);
	
	
	
	
	
	
	
	
	
	
	
	
	///**
	// * @deprecated should use the one that supports sorting
	// */
	//@Deprecated
	//public abstract ArrayList<Item> getCategoryItems(int categoryID, int page, int itemsPP) throws Exception;
	
	
	///**
	// * @deprecated should use the one that supports sorting
	// */
	//@Deprecated
	//public abstract ArrayList<Item> getTextItems(String text, int page, int itemsPP) throws Exception;
	
	
	
	
	
	
	
	
	
	//public abstract ArrayList<Address> getAddresses(int userID);
	
	
	
	
	
	
	
	
	
	
	
	//public abstract ArrayList<Bid> getCurrentBidsTime(int parseInt, Date parse);
	
	//public abstract ArrayList<Item> getCurrentSellingItemsTime(int parseInt, Date parse);
	
	//public abstract ArrayList<Bid> getOldBidsTime(int parseInt, Date parse);
	
	//public abstract ArrayList<Item> getOldSellingItemsTime(int parseInt, Date parse);
	
	// Used for DB management
	
	/**
	 * Inserts a feedback comment about an item/user
	 * @param userID The user who is inserting the comment
	 * @param sellerID The user who the comment is about
	 * @param itemID The item that the buying user is referring to
	 * @param rating The rating the buying user is giving the selling user
	 * @param created The date the feedback was created
	 * @param comment The text of the comment
	 * @return The id of the newly inserted comment, -1 if not inserted
	 */
	public abstract long insertComment(long userID, long sellerID, long itemID, int rating, Date created, String comment);
	
	/**
	 * Gets the comments about an item
	 * @param itemID The itemID to get the comments about
	 * @return Array containing the comments
	 * @throws Exception
	 */
	public abstract ArrayList<Comment>getComments(long itemID) throws Exception;
	
	/**
	 * Gets all of the comments for a list of item ids
	 * @param itemIDs The comments to get the questions for
	 * @return An array with comments
	 * @throws Exception
	 */
	public abstract ArrayList<Comment> getComments(ArrayList<Long> itemIDs) throws Exception;
	
	/**
	 * Inserts a question about an item
	 * @param fromUserID The userID that the question is from
	 * @param toUserID The userID that the question is to
	 * @param itemID The itemID that the question is about
	 * @param date The date the question was asked
	 * @param question The text of the question
	 * @return The id of the newly inserted question, -1 if not inserted
	 */
	public abstract long insertQuestion(long fromUserID, long toUserID, long itemID, Date date, String question);

	/**
	 * Inserts an answer to a question
	 * @param fromUserID The ID of the user answering the question
	 * @param toUserID The ID of the user who asked the question
	 * @param itemID The ID of the item the question was about
	 * @param questionID The ID of the question that the answer is referring to
	 * @param date The date of the answer
	 * @param answer The answer's text
	 * @return The ID of the newly inserted answer
	 */
	public abstract long insertAnswer(long fromUserID, long toUserID, long itemID, long questionID, Date date, String answer);
	
	/**
	 * Gets a question from the database
	 * @param questionID The question ID to get
	 * @return The question
	 * @throws Exception
	 */
	public abstract Question getQuestion(long questionID) throws Exception;
	
	/**
	 * Gets all of the questions about an item
	 * @param itemID The itemID to get the questions about
	 * @return ArrayList of the questions and answers
	 * @throws Exception
	 */
	public abstract ArrayList<Question> getQuestions(long itemID) throws Exception;
	
	/**
	 * Gets all of the questions for a list of item ids
	 * @param itemIDs The items to get the questions for
	 * @return An array with questions and answers 
	 * @throws Exception
	 */
	public abstract ArrayList<Question> getQuestions(ArrayList<Long> itemIDs) throws Exception;
	
	//public abstract ArrayList<VideoItem> GetAllVideos();

	//public abstract int insertVideo(String name, String description,int userID);
	//public abstract int getMaxVideoID();
}
