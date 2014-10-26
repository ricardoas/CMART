package com.cmart.DB;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.cmart.Data.GlobalVars;
import com.cmart.util.Account;
import com.cmart.util.Address;
import com.cmart.util.Bid;
import com.cmart.util.Category;
import com.cmart.util.Comment;
import com.cmart.util.FriendRequest;
import com.cmart.util.Image;
import com.cmart.util.Item;
import com.cmart.util.Message;
import com.cmart.util.Purchase;
import com.cmart.util.Question;
import com.cmart.util.User;
import com.cmart.util.VideoItem;
import com.cmart.util.WallPost;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;

/**
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
public class MySQLINNODBQuery extends DBQuery{
	protected static int maxSQLConnectionsCache = 200;
	private static MySQLINNODBQuery single = null;
	private static int CONNECTION_POOL = GlobalVars.MY_CONN_CACHE;
	
	private DataSource ds;
	
	public MySQLINNODBQuery(String forT2testingOnly){
		MySQLINNODBQuery.getInstance();
	}
	
	private MySQLINNODBQuery(){
		super();
		init();
	}
	
	public static MySQLINNODBQuery getInstance(){
		if (single == null) {
			synchronized (MySQLINNODBQuery.class) {
				if (single == null) {
					single = new MySQLINNODBQuery();
				}
			}
		}

		return single;
	}
	
	static String urlFront = null;
	static String urlBack = null;
	static int ipCount;
	static int currentIP;
	public static synchronized String getURL(){
		// Just return the URL if there are no additional IPs
		if(GlobalVars.MYSQL_IPS==null || GlobalVars.MYSQL_IPS.size()==0) return GlobalVars.MY_DATABASE_URL;
		
		// Parse the URL so we can insert the IP
		if(urlFront==null){
			urlFront = GlobalVars.MY_DATABASE_URL.split("/")[0] + "//";
			urlBack = "/" + GlobalVars.MY_DATABASE_URL.split("/")[3];
			ipCount = GlobalVars.MYSQL_IPS.size();
			currentIP=0;
		}
		
		if(currentIP >= ipCount) currentIP=0;
		String ret = urlFront + GlobalVars.MYSQL_IPS.get(currentIP)+ urlBack;
		currentIP++;

		return ret;
	}
	
	public static synchronized String getFirstURL(){
		// Just return the URL if there are no additional IPs
		if(GlobalVars.MYSQL_IPS==null || GlobalVars.MYSQL_IPS.size()==0) return GlobalVars.MY_DATABASE_URL;
		
		// Parse the URL so we can insert the IP
		if(urlFront==null){
			urlFront = GlobalVars.MY_DATABASE_URL.split("/")[0] + "//";
			urlBack = "/" + GlobalVars.MY_DATABASE_URL.split("/")[3];
			ipCount = GlobalVars.MYSQL_IPS.size();
			currentIP=0;
		}
		
		String ret = urlFront + GlobalVars.MYSQL_IPS.get(0)+ urlBack;		

		return ret;
	}
	
	protected void init(){
		//URL = GlobalVars.MY_DATABASE_URL;
		DRIVER = GlobalVars.MY_DATABASE_DRIVER;
		USERNAME = GlobalVars.MY_DATABASE_USERNAME;
		PASSWORD = GlobalVars.MY_DATABASE_PASSWORD;
		
		if(CONNECTION_POOL==2){
			Context envCtx;
			
			try {
				//Class.forName("com.mysql.jdbc.Driver").newInstance();
				envCtx = (Context) new InitialContext().lookup("java:comp/env");
				
				// Look up our data source
			     ds = (DataSource) envCtx.lookup("jdbc/CMARTDB");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	    
		}
	}
	
	protected Connection initConnection(){
		return initConnection(false);
	}
	
	protected Connection initConnection(boolean needWrite){
		String url;
		if(needWrite && GlobalVars.firstIsMaster) url = getFirstURL();
		else url = getURL();
		
		switch (CONNECTION_POOL) {
		case 0:
			try {
				Class.forName(DRIVER).newInstance();
				Connection conn = DriverManager.getConnection(url, USERNAME, PASSWORD);
				return conn;
			} catch (Exception e) {
				System.out.println("MySQLQuery (initConnection): Could not open a new database connection to " + url);
				e.printStackTrace();
				return null;
			}
		case 1:
			try {
				Class.forName(DRIVER).newInstance();
				Connection conn = DriverManager.getConnection(url, USERNAME, PASSWORD);
				return conn;
			} catch (Exception e) {
				System.out.println("MySQLQuery (initConnection): Could not open a new database connection to " + url);
				e.printStackTrace();
				return null;
			}
		case 2:
			try {
				// System.out.println("returning connection pool");
				return ds.getConnection();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("failed returning connection pool");
				return null;
			}
		default: {
			try {
				Class.forName(DRIVER).newInstance();
				Connection conn = DriverManager.getConnection(url, USERNAME, PASSWORD);
				return conn;
			} catch (Exception e) {
				System.out.println("MySQLQuery (initConnection): Could not open a new database connection to " + url);
				e.printStackTrace();
				return null;
			}
		}

		}
		
		/*try {
			Class.forName(DRIVER).newInstance();
			Connection conn = DriverManager.getConnection(URL,USERNAME,PASSWORD);
			return conn;
		}
		catch(Exception e){
			System.out.println("MySQLQuery (initConnection): Could not open a new database connection to " + URL);
			e.printStackTrace();
			return null;
		}*/
	}
	
	protected synchronized Connection getConnection(){
		return getConnection(false);
	}
	
	protected synchronized Connection getConnection(boolean needWrite){
		switch (CONNECTION_POOL) {
		case 0:
			return initConnection(needWrite);
		case 1:
			if (!connections.isEmpty()) {
				Connection conn = null;

				synchronized (connections) {
					if (!connections.isEmpty()) {
						conn = connections.pop();
					}
				}

				if (conn != null)
					return conn;
			}

			return initConnection(needWrite);
		case 2:
			return initConnection(needWrite);
		default:
			return initConnection(needWrite);
		}
		
		
		/*if(!useConnectionPool){
			return initConnection();
		}
		else{
			if(!connections.isEmpty()){
				Connection conn = null;
				
				synchronized(connections){
					if(!connections.isEmpty()){
						conn = connections.pop();
					}
				}
				
				if(conn!=null)
					return conn;				
			}
			
			return initConnection();
		}*/
	}
	


	/**
	 * As some connections may timeout if left for a long period, we'll go through them all and make sure they all work
	 * if we detect any that have timed out.
	 * 
	 */
	protected void checkConnections(){
		/*Connection conn;
		
		for(int i=0; i<connections.size(); i++){
			synchronized(connections){
				conn = connections.pop();
			}
			
			try {
				conn.isValid(1000);
				
				// The connection worked, so readd it
				synchronized(connections){
					connections.offerLast(conn);
				}
				
			} catch (SQLException e) {
				// The connection is closed or bad, so we don't want it
				conn = null;
			}
		}*/
	}
	
	protected void forceCloseConnection(Connection conn){
		if(conn != null)
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("MySQLQuery (closeConnection): Could not close database connection");
			e.printStackTrace();
		}
	}
	
	protected void closeConnection(Connection conn){
		switch (CONNECTION_POOL){
		case 0 : break;
		case 1 : break;
		case 2 : try {
					conn.close();
					conn = null;
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
		}
		
		if(conn != null){
			if(!useConnectionPool){
				// close the connection
				try{
					conn.close();
				}
				catch(Exception e){
					System.out.println("MySQLQuery (closeConnection): Could not close database connection");
					e.printStackTrace();
				}	
			}
			else{
				// re-pool the connection
				if(connections.size() < maxSQLConnectionsCache)
					synchronized(connections){
						if(connections.size() < maxSQLConnectionsCache)
							connections.offerLast(conn);
					}
				else{
					try{
						conn.close();
					}
					catch(Exception e){
						System.out.println("MySQLQuery (closeConnection): Could not close database connection");
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void closeSmt(PreparedStatement statement){
		if(statement != null){
			try {
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("closeSmt (MySQLDBQuery): could not close the statement");
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#checkUsernamePassword(java.lang.String, java.lang.String)
	 */
	public long checkUsernamePassword(String username, String password){
		if(username == null || password == null) return -1;
		
		long userID = -1;
		int attemptsRemaining = SQL_RETRIES;
		
		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("SELECT `id` FROM `users` WHERE `users`.`username` = BINARY(?) AND `users`.`password` = BINARY(?)");
					
					statement.setString(1, username);
					statement.setString(2, password);
					
					ResultSet rs = statement.executeQuery();
					
					// If there is a result, set correct to true
					if(rs.next()){
						userID = rs.getLong("id");
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (checkUsernamePassword): Could not check username and password");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}

				
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return userID;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#makeNewAuthToken(int)
	 */
	public String makeNewAuthToken(long userID){
		if(userID < 1) return null;
		
		String authToken = null;
		
		// Get the new auth token
		String newAuthToken = authToken(userID);
		
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection(true);

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("UPDATE `users` SET `users`.`authToken` = ? WHERE `users`.`id` = ?");
					statement.setString(1,newAuthToken);
					statement.setLong(2, userID);
					
					int success = statement.executeUpdate();
					
					// If we did update the row, then we want to return the new auth token
					if(success==1){
						authToken = newAuthToken;
						attemptsRemaining = 0;
					}
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err.println("MySQLQuery (makeNewAuthToken): Could not update the auth token");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return authToken;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#checkAuthToken(int, java.lang.String)
	 */
	public Boolean checkAuthToken(long userID, String authToken){
		if(userID < 1 || authToken == null) return false;
		
		boolean correct = false;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to get the auth token
					statement = conn.prepareStatement("SELECT `authToken` FROM `users` WHERE `users`.`id` = ?");
					statement.setLong(1, userID);
					
					ResultSet rs = statement.executeQuery();
					
					// If there is a result, then we got an auth token back
					if(rs.next()){
						String resultAuth = rs.getString("authToken");
						if(resultAuth != null && resultAuth.equals(authToken)) correct = true;
					}
					
					
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err.println("MySQLQuery (checkAuthToken): Could not read the auth toekn");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return correct;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#logout(int)
	 */
	public Boolean logout(long userID){
		if(userID < 1) return false;
		
		Boolean loggedOut = Boolean.FALSE;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection(true);

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to update the auth token
					statement = conn.prepareStatement("UPDATE `users` SET `users`.`authToken` = NULL WHERE `users`.`id` = ?");
					statement.setLong(1, userID);
						
					int success = statement.executeUpdate();
						
					// We we did update the row, then we to return true, we did update the token
					if(success==1) loggedOut = Boolean.TRUE;
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err.println("MySQLQuery (logout): Could not update the auth token");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return loggedOut;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#insertAddress(int, java.lang.String, java.lang.String, java.lang.String, int, java.lang.Boolean)
	 */
	public Boolean insertAddress(long userID, String street, String town, String zip, int state, Boolean isDefault){
		if(userID < 1 || state < 1) return false;
		if(street == null || town == null || zip == null || isDefault == null) return false;
		
		zip = zip.substring(0, Math.min(zip.length(), 9));
		
		Boolean insertedAddress = false;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection(true);

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to insert the address
					statement = conn.prepareStatement("INSERT INTO `addresses` (userID, street, town, zip, state, isDefault) "
											+ "VALUES (?, ?, ?, ?, ?, ?)");
					statement.setLong(1, userID);
					statement.setString(2, street);
					statement.setString(3, town);
					statement.setString(4, zip);
					statement.setInt(5, state);
					statement.setBoolean(6, isDefault);
					
					int result = statement.executeUpdate();
					
					if(result==1) insertedAddress = true;
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err.println("MySQLQuery (insertAddress): Could not insert address");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		
		return insertedAddress;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#updateAddress(int, int, java.lang.String, java.lang.String, java.lang.String, int, java.lang.Boolean)
	 */
	public Boolean updateAddress(long id, long userID, String street, String town, String zip, int state, Boolean isDefault){
		if(userID < 1 || id < 1 || state < 1) return false;
		if(street == null || town == null || zip == null || isDefault == null) return false;
		
		int attemptsRemaining = SQL_RETRIES;
		boolean updatedAddress = false;

		zip = zip.substring(0,Math.min(9, zip.length()));
		
		do {
			Connection conn = this.getConnection(true);

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to update the address
					statement = conn.prepareStatement("UPDATE `addresses` set street = ?, town = ?, zip = ?, state = ?, isDefault = ? WHERE id = ? AND userID = ?");

					statement.setString(1, street);
					statement.setString(2, town);
					statement.setString(3, zip);
					statement.setInt(4, state);
					statement.setBoolean(5, isDefault);
					
					// Must use id and userID for security
					statement.setLong(6, id);
					statement.setLong(7, userID);
					
					//TODO: is isDefault = true then read old default address and set it to false
					//TODO: is isDefault = false, but this is the default address, then ignore and set to true
					
					int updated = statement.executeUpdate();
					
					if(updated==1) updatedAddress = true;
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err.println("MySQLQuery (updateAddress): Could not update address");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}			
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return updatedAddress;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#insertItemDB(int, java.lang.String, java.lang.String, double, double, double, int, java.util.Date, int)
	 */
	public long insertItemDB(long userID, String name, String description, double startPrice, double reservePrice, double buyNowPrice, int quantity, Date endDate, long categoryID){
		if(name == null || description==null || endDate == null) return -1;
		if(userID < 1 || categoryID < 1 || quantity<1) return -1;
		if(startPrice<0.0 || reservePrice<0.0 || buyNowPrice<0.0) return -1;
		
		long itemID = -1;
		int attemptsRemaining = SQL_RETRIES;
		
		do {
			Connection conn = this.getConnection(true);

			if (conn != null) {
				PreparedStatement getItemID = null;
				PreparedStatement statement = null;
				
				try {
					// Create the statement to get the itemsID
					getItemID = conn.prepareStatement("SELECT LAST_INSERT_ID()");
					
					// Create the SQL statement to insert the item
					statement = conn.prepareStatement("INSERT INTO `items` (name, description, startPrice, reservePrice, buyNowPrice, quantity, startDate, endDate, sellerID, categoryID, thumbnail) "
											+ "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?)");
					statement.setString(1, name);
					statement.setString(2, description);
					statement.setDouble(3, startPrice);
					statement.setDouble(4, reservePrice);
					statement.setDouble(5, buyNowPrice);
					statement.setInt(6, quantity);
					statement.setTimestamp(7, new Timestamp(endDate.getTime()));
					statement.setLong(8, userID);
					statement.setLong(9, categoryID);
					statement.setString(10, "blank.jpg");
					
					int inserted = statement.executeUpdate();
					
					if(inserted == 1){
						ResultSet rs = getItemID.executeQuery();
						if(rs.next()){
							itemID = rs.getLong(1);
						}
						rs.close();
					}
					
					attemptsRemaining = 0;

				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err.println("MySQLQuery (insertItem): Could not insert item");
					e.printStackTrace();
				} finally{
					this.closeSmt(getItemID);
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		
		return itemID;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getItem(int, java.lang.Boolean, java.lang.Boolean)
	 */
	public Item getItem(long itemID, Boolean getImages){
		return getXItem(itemID, getImages,  "items");
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getOldItem(int, java.lang.Boolean, java.lang.Boolean)
	 */
	public Item getOldItem(long itemID, Boolean getImages){
		return getXItem(itemID, getImages, "oldItems");
	}
	
	/**
	 * This method gets the items for the 'items' or 'oldItems' table
	 * @param itemID
	 * @param getImages
	 * @param getAllBids
	 * @param table
	 * @return
	 */
	private Item getXItem(long itemID, Boolean getImages, String table){
		if(itemID < 1) return null;
		if(table==null || !(table.equals("items") || table.equals("oldItems"))) return null;
		
		Item result = null;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to see get the item's details
					statement = conn.prepareStatement("SELECT * FROM `" + table +"` WHERE `"+table+"`.`id` = ?");
					statement.setLong(1, itemID);
					
					ResultSet rs = statement.executeQuery();
					
					// If an item is returned then get the details
					if(rs.next()){
						ArrayList<Image> images = new ArrayList<Image>();
						
						if(getImages==true)
							images = this.getItemImages(rs.getLong("id"));

						result = new Item(rs.getLong("id"),
								rs.getString("name"),
								rs.getString("description"),
								rs.getInt("quantity"),
								rs.getDouble("startPrice"),
								rs.getDouble("reservePrice"),
								rs.getDouble("buyNowPrice"),
								rs.getDouble("currentBid"),
								rs.getDouble("maxBid"),
								rs.getInt("noOfBids"),
								new Date(rs.getTimestamp("startDate").getTime()),
								new Date(rs.getTimestamp("endDate").getTime()),
								rs.getLong("sellerID"),
								rs.getLong("categoryID"),
								rs.getString("thumbnail"),
								images);
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err.println("MySQLQuery (getXItem): Could not get the item "+ itemID);
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getCurrentSellingItems(int)
	 */
	public ArrayList<Item> getCurrentSellingItems(long userID, long ts){
		return this.getXSellingItems(userID, "items", ts);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getOldSellingItems(int)
	 */
	public ArrayList<Item> getOldSellingItems(long userID, long ts){
		return this.getXSellingItems(userID, "oldItems", ts);
	}
	
	/**
	 * Returns all of the items that a user is/was selling
	 * @param userID The userID of the user we want to get items for
	 * @param table The table to look in items or oldItems
	 * @return
	 */
	protected ArrayList<Item> getXSellingItems(long userID, String table, long ts){
		ArrayList<Item> items = new ArrayList<Item>();
		if(userID < 1) return items;
		if(table==null || !(table.equals("items") || table.equals("oldItems"))) return items;
		
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to get items the user is selling
					statement = conn.prepareStatement("SELECT * FROM `" + table + "`" +
							"WHERE `" + table + "`.`sellerID` = ? AND `" + table + "`.`ts` > ? ORDER BY `" + table + "`.`endDate` ASC");
					statement.setLong(1, userID);
					statement.setTimestamp(2, new Timestamp(ts));
					
					ResultSet rs = statement.executeQuery();
					
					while(rs.next()){
						ArrayList<Image> images = this.getItemImages(rs.getLong(table + ".id"));
						Item currentItem = new Item(rs.getLong(table + ".id"),
								rs.getString(table + ".name"),
								rs.getString(table + ".description"),
								rs.getInt(table + ".quantity"),
								rs.getDouble(table + ".startPrice"),
								rs.getDouble(table + ".reservePrice"),
								rs.getDouble(table + ".buyNowPrice"),
								rs.getDouble(table + ".currentBid"),
								rs.getDouble(table + ".maxBid"),
								rs.getInt(table + ".noOfBids"),
								new Date(rs.getTimestamp(table + ".startDate").getTime()),
								new Date(rs.getTimestamp(table + ".endDate").getTime()),
								rs.getLong(table + ".sellerID"),
								rs.getLong(table + ".categoryID"),
								rs.getString(table + ".thumbnail"),
								images
								);
						
						items.add(currentItem);
					}

					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getXSellingItems): Could not get the items");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return items;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#insertBidDB(int, int, int, double, double)
	 */
	public double insertBidDB(long userID, long itemID, int quantity, double bid, double maxBid){	
		double price = -1.0;
		if(userID < 1 || itemID < 1 || quantity<1) return price;
		if(bid<0.0 || maxBid<0.0 || maxBid<bid) return price;
		int attemptsRemaining = SQL_RETRIES;
		
		do {
			Connection conn = this.getConnection(true);

			if (conn != null) {
				boolean autoCommitDefault = false;
				try{
					autoCommitDefault = conn.getAutoCommit();
					conn.setAutoCommit(false);
				}
				catch (Exception e){
					System.err.println("can not get autoCommit default value");
					System.err.println("Error: " + e.getMessage());
				}
				
				PreparedStatement insertBidStatement = null;
				PreparedStatement getMaxBidStatement = null;
				PreparedStatement updateItemStatement = null;
				
				try {
					/*
					 * When buying an item we may be only be buying some of them if multiple are available
					 * we will decrease the quantity available on the item with the number we are buying
					 * If the end value is zero, we have bought them all and we will move the item and bids to 'old'
					 *
					 * 
					 * Using transaction!!
					 * 
					 * 1. Check the items quantity is still more than or equal to the number we want to buy
					 * it might not be if two people are racing to buy
					 * 2. Insert the payment first, we don't want anyone getting items for free 
					 * 3. Insert the purchase
					 * 4. update the items quantity with the new quantity
					 * 5. if the item's quantity is zero, then the item's auction has now finished regardless of the intended finish time
					 * 		- move the item to the old items table
					 * 		- move the bids to the old bids table
					 * 6. commit or rollback
					 */
					
					// SQL to enter the user's bid
					insertBidStatement = conn.prepareStatement("INSERT INTO `bids` (userID, itemID, quantity, bid, maxBid, bidDate) "
							+ "VALUES (?, ?, ?, ?, ?, NOW())");
					insertBidStatement.setLong(1, userID);
					insertBidStatement.setLong(2, itemID);
					insertBidStatement.setInt(3, quantity);
					insertBidStatement.setDouble(4, bid);
					insertBidStatement.setDouble(5, maxBid);
					
					// Get the item's currentBid and maxBid values (and noOfBids)
					getMaxBidStatement = conn.prepareStatement("SELECT startPrice, currentBid, maxBid, noOfBids, currentWinner FROM `items`" +
							"WHERE `items`.`id` = ?");
					getMaxBidStatement.setLong(1, itemID);
					
					// SQL to update the item with the new bids
					updateItemStatement = conn.prepareStatement("UPDATE `items` SET `items`.`currentBid` = ?, `items`.`maxBid` = ?, `items`.`noOfBids` = ?, `items`.`currentWinner` = ? WHERE `items`.`id` = ?");
					updateItemStatement.setLong(5,itemID);
					
					/*
					 * Execute the update procedure
					 */
					
					
					
					// Get maxBid
					ResultSet rs = getMaxBidStatement.executeQuery();
					if(rs.next()){
						double dbCurrentBid = rs.getDouble("currentBid");
						double dbMaxBid = rs.getDouble("maxBid");
						double dbStartPrice = rs.getDouble("startPrice");
						int dbNoOfBids = rs.getInt("noOfBids");
						long currentWinner = rs.getLong("currentWinner");
						
						if(maxBid<dbStartPrice){
							
						}
						else if(maxBid< Math.max(dbCurrentBid, dbStartPrice)){
							// The bid shouldn't really be accepted, but they will lose anyway			
							updateItemStatement.setDouble(1,dbCurrentBid);
							updateItemStatement.setDouble(2,dbMaxBid);
							updateItemStatement.setInt(3,dbNoOfBids+1);
							updateItemStatement.setLong(4,currentWinner);
							price = Math.max(dbCurrentBid, dbStartPrice);
						}
						else if(dbMaxBid==maxBid){
							// New max bid is the same as old max bid, old user is still winning as they bidded first
							updateItemStatement.setDouble(1,dbMaxBid);
							updateItemStatement.setDouble(2,dbMaxBid);
							updateItemStatement.setInt(3,dbNoOfBids+1);
							updateItemStatement.setLong(4,currentWinner);
							price = maxBid;
						}
						else if(dbMaxBid>maxBid){
							// The old max bid is greater, old user still winning by 0.01c
							// max bid is the same, current bid is the new bid's maxPrice+1c
							updateItemStatement.setDouble(1,maxBid + 0.01);
							updateItemStatement.setDouble(2,dbMaxBid);
							updateItemStatement.setInt(3,dbNoOfBids+1);
							updateItemStatement.setLong(4,currentWinner);
							price = maxBid + 0.01;
						}
						else{
							// Else the new bid is more, the new user is winning
							// The new user's bid will be the old user's max(maxBid+1c, new bid)
							updateItemStatement.setDouble(1,(bid>dbMaxBid) ? bid : (dbMaxBid + 0.01));
							updateItemStatement.setDouble(2,maxBid);
							updateItemStatement.setInt(3,dbNoOfBids+1);
							updateItemStatement.setLong(4,userID);
							price = (bid>dbMaxBid) ? bid : (dbMaxBid + 0.01);
						}
						
						// If accepted, update the new max bid
						if(price>0.0){
							// Insert the bid
							insertBidStatement.executeUpdate();
							insertBidStatement.close();
							
							updateItemStatement.executeUpdate();
						}
						updateItemStatement.close();
					}
					
					// Close things
					rs.close();
					getMaxBidStatement.close();
					conn.commit();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (insertBid): Could not get insert bid");
					e.printStackTrace();
					if(conn != null)
						try {
							conn.rollback();
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				
				} finally{
					this.closeSmt(insertBidStatement);
					this.closeSmt(getMaxBidStatement);
					this.closeSmt(updateItemStatement);

					try {
						conn.setAutoCommit(autoCommitDefault);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return price;
	}
	
	
	//TODO: test this function!!!
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#buyItemNowDB(int, int, int, Account, Address)
	 */
	public Boolean buyItemNowDB(long userID, long itemID, int quantity, Account account, Address address){
		if(userID < 1 || itemID < 1) return Boolean.FALSE;
		if(account == null || address == null) return Boolean.FALSE;
		
		int attemptsRemaining = SQL_RETRIES;
		Boolean purchased = Boolean.FALSE;

		int step = 0;
		
		do {
			Connection conn = this.getConnection(true);

			if (conn != null) {
				PreparedStatement getQuantityStatement = null;
				PreparedStatement paymentItemStatement = null;
				PreparedStatement purchaseItemStatement = null;
				PreparedStatement setQuantityStatement = null;
				PreparedStatement copyItemStatement = null;
				PreparedStatement updateItemStatement = null;
				PreparedStatement deleteItemStatement = null;
				PreparedStatement copyBidsStatement = null;
				PreparedStatement deleteBidsStatement = null;
				
				boolean autoCommitDefault = false;
				try{
					autoCommitDefault = conn.getAutoCommit();
					conn.setAutoCommit(false);
				}
				catch (Exception e){
					System.err.println("can not get autoCommit default value");
					System.err.println("Error: " + e.getMessage());
				}	
				
				try {
					/*
					 * When buying an item we may be only be buying some of them if multiple are available
					 * we will decrease the quantity available on the item with the number we are buying
					 * If the end value is zero, we have bought them all and we will move the item and bids to 'old'
					 *
					 * 1. Check the items quantity is still more than or equal to the number we want to buy
					 * it might not be if two people are racing to buy
					 * 2. Insert the payment first, we don't want anyone getting items for free 
					 * 3. Insert the purchase
					 * 4. update the items quantity with the new quantity
					 * 5. if the item's quantity is zero, then the item's auction has now finished regardless of the intended finish time
					 * 		- move the item to the old items table
					 * 		- move the bids to the old bids table
					 * 6. commit or rollback
					 */
					// Get item quantity
					getQuantityStatement = conn.prepareStatement("SELECT `quantity`, `buyNowPrice` FROM `items` WHERE `items`.`id` = ?");
					getQuantityStatement.setLong(1, itemID);
					
					// insert the payment
					paymentItemStatement = conn.prepareStatement("INSERT INTO `payments` (userID, itemID, quantity, price, paidDate, street, town, zip, state, nameOnCard, creditCardNo, cvv, expirationDate) "
							+ "VALUES (?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?)");
					paymentItemStatement.setLong(1, userID);
					paymentItemStatement.setLong(2, itemID);
					paymentItemStatement.setInt(3, quantity);
					paymentItemStatement.setString(5, address.getStreet());
					paymentItemStatement.setString(6, address.getTown());
					paymentItemStatement.setString(7, address.getZip());
					paymentItemStatement.setString(8, getStateName(address.getState()));
					paymentItemStatement.setString(9, account.getNameOnCard());
					paymentItemStatement.setString(10, account.getCreditCardNo());
					paymentItemStatement.setString(11, account.getCVV());
					
					if(account.getExpirationDate()!=null)
						paymentItemStatement.setTimestamp(12, new java.sql.Timestamp(account.getExpirationDate().getTime()));
					else
						paymentItemStatement.setTimestamp(12, new java.sql.Timestamp(1));
					
					// Insert the purchase
					purchaseItemStatement = conn.prepareStatement("INSERT INTO `purchased` (userID, itemID, quantity, price, purchaseDate, accountID, paid, paidDate) "
							+ "VALUES (?, ?, ?, ?, NOW(), ?, 1, NOW())");
					purchaseItemStatement.setLong(1, userID);
					purchaseItemStatement.setLong(2, itemID);
					purchaseItemStatement.setInt(3, quantity);
					if(account.getAccountID()>0)
						purchaseItemStatement.setLong(5, account.getAccountID());
					else
						purchaseItemStatement.setNull(5, java.sql.Types.BIGINT);
					
					// Set quantity (to old quantity - purchased amount)
					setQuantityStatement = conn.prepareStatement("UPDATE `items` SET `quantity` = ? WHERE `items`.`id` = ?");
					setQuantityStatement.setLong(2, itemID);
					

					
					/*
					 * Do the operations for real
					 */
					step = 1;
					// Check there is sufficient quantity
					ResultSet rs = getQuantityStatement.executeQuery();
					step = 2;
					if(rs.next()){
						int dbQuantity = rs.getInt("quantity");
						double dbBuyNowPrice = rs.getDouble("buyNowPrice");
						purchaseItemStatement.setDouble(4, dbBuyNowPrice*quantity);
						paymentItemStatement.setDouble(4, dbBuyNowPrice*quantity);
						step = 3;
						// There must be sufficient items and the buyNowPrice must be set
						if((dbQuantity - quantity) >= 0 && dbBuyNowPrice>0.0){
							// Purchase the item
							int insertedPayment = paymentItemStatement.executeUpdate();
							paymentItemStatement.close();
							step = 4;
							// If the payment was successful then they did buy the item
							if(insertedPayment>0){
								purchaseItemStatement.executeUpdate();
								purchaseItemStatement.close();
								step = 5;
								// Update quantity
								setQuantityStatement.setInt(1, dbQuantity-quantity);
								setQuantityStatement.executeUpdate();
								setQuantityStatement.close();
								
								purchased = true;
							}
							
							
							if(purchased){
								// Now check if we need to move the item and bids
								// TODO: move the maintenance of moving the data to an off-line process?
								if(dbQuantity-quantity <= 0){
									// Copy the item to the 'old items' table
									copyItemStatement = conn.prepareStatement("INSERT INTO `oldItems` (SELECT * FROM `items` WHERE `items`.`id` = ?)");
									copyItemStatement.setLong(1, itemID);
									
									// Update the items end time
									updateItemStatement = conn.prepareStatement("UPDATE `oldItems` SET `endDate` = NOW() WHERE `oldItems`.`id` = ?");
									updateItemStatement.setLong(1, itemID);
									
									// Delete item statement
									deleteItemStatement = conn.prepareStatement("DELETE FROM `items` WHERE `items`.`id` = ?");
									deleteItemStatement.setLong(1, itemID);
									
									// copy bids statement
									copyBidsStatement = conn.prepareStatement("INSERT INTO `oldBids` SELECT * FROM `bids` WHERE `bids`.`itemID` = ?");
									copyBidsStatement.setLong(1, itemID);
									
									// Delete bids statement
									deleteBidsStatement = conn.prepareStatement("DELETE FROM `bids` WHERE `bids`.`itemID` = ?");
									deleteBidsStatement.setLong(1, itemID);
									
									copyItemStatement.executeUpdate();
									deleteItemStatement.executeUpdate();
									step = 7;
									copyBidsStatement.executeUpdate();
									deleteBidsStatement.executeUpdate();
									updateItemStatement.executeUpdate();
									step = 8;
									copyItemStatement.close();
									deleteItemStatement.close();
									copyBidsStatement.close();
									deleteBidsStatement.close();
									updateItemStatement.close();
								}
								
								// everything is ok, commit the transaction
								conn.commit();
							}
						}
						else{
							// We did not purchase, so don't commit
							if(conn != null)
								conn.rollback();
							
							attemptsRemaining = 0;
						}
					}
					else{
						// This should happen only in a race condition
						// We did not purchase, so don't commit
						if(conn != null) conn.rollback();
						
						//System.out.println("someone has bought the item between your request!");
						attemptsRemaining = 0;
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (buyItemNow): Could not buy item " + step);
					e.printStackTrace();
					if(conn != null)
						try {
							conn.rollback();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
				} finally{
					this.closeSmt(getQuantityStatement);
					this.closeSmt(paymentItemStatement);
					this.closeSmt(purchaseItemStatement);
					this.closeSmt(setQuantityStatement);
					this.closeSmt(copyItemStatement);
					this.closeSmt(updateItemStatement);
					this.closeSmt(deleteItemStatement);
					this.closeSmt(copyBidsStatement);
					this.closeSmt(deleteBidsStatement);
					
					try {
						conn.setAutoCommit(autoCommitDefault);
					} catch (SQLException e) {
						e.printStackTrace();
					}
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return purchased;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#insertThumbnail(int, String)
	 */
	public Boolean insertThumbnail(long itemID, String URL){
		return insertImage(itemID, 0, URL, "");
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#insertImage(int, int, String, String)
	 */
	public Boolean insertImage(long itemID, int position, String URL, String description){
		if(itemID < 1 || position<0 || description==null|| URL == null) return Boolean.FALSE;
		
		int attemptsRemaining = SQL_RETRIES;
		Boolean insertedImage = Boolean.FALSE;

		do {
			Connection conn = this.getConnection(true);

			if (conn != null) {
				PreparedStatement insertImage = null;
				PreparedStatement getImageID = null;
				PreparedStatement insertItemImage = null;
				PreparedStatement updateThumbnail = null;
				
				try {
					// Create the SQL statement to insert the image URL
					insertImage = conn.prepareStatement("INSERT INTO `images` (URL, description) "
							+ "VALUES (?, ?)");
					insertImage.setString(1, URL);
					insertImage.setString(2, description);
					
					// Create the SQL statement to get the images ID number
					getImageID = conn.prepareStatement("SELECT LAST_INSERT_ID()");	
					
					// insert the image
					insertImage.executeUpdate();
					insertImage.close();
					
					// Get the images ID
					long imageID = 0;
					ResultSet rs = getImageID.executeQuery();
					if(rs.next()){
						imageID = rs.getLong(1);
					}
					
					rs.close();
					getImageID.close();
					insertImage.close();
					
					if(position>-1){
						// Set the imageID and create the item-image link
						insertItemImage = conn.prepareStatement("INSERT INTO `item_image` (itemID, imageID, position) "
								+ "VALUES (?, ?, ?)");
						insertItemImage.setLong(1, itemID);
						insertItemImage.setInt(3, position);
						insertItemImage.setLong(2, imageID);
						insertItemImage.executeUpdate();
						insertItemImage.close();
					}
					if(position==0){
						// if the position is 0 then we want to update the thumbnail field
						updateThumbnail = conn.prepareStatement("UPDATE `items` SET thumbnail = ? " +
								"WHERE `items`.`id` = ?");
						updateThumbnail.setLong(2, itemID);
						updateThumbnail.setString(1, URL);
						updateThumbnail.executeUpdate();
						updateThumbnail.close();
					}	
					
					insertedImage = Boolean.TRUE;
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (insertImage): Could not insert image");
					e.printStackTrace();
				} finally{
					this.closeSmt(insertImage);
					this.closeSmt(getImageID);
					this.closeSmt(insertItemImage);
					this.closeSmt(updateThumbnail);
					
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return insertedImage;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getItemImages(int)
	 */
	public ArrayList<Image> getItemImages(long itemID){
		ArrayList<Image> images = new ArrayList<Image>();
		if(itemID < 1) return images;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					images = new ArrayList<Image>();

					// Create the SQL statement
					statement = conn.prepareStatement("SELECT `position`,`url`,`description` FROM" +
							"`item_image` INNER JOIN `images` ON `item_image`.`imageID` = `images`.`id`" +
							" WHERE `item_image`.`itemID` = ? ORDER BY `position` ASC");
					statement.setLong(1, itemID);
					
					ResultSet rs = statement.executeQuery();
					
					// If images are returned then we'll add them
					int pos=0;
					while(rs.next()){
						images.add( new Image(pos,
								rs.getString("url"),
								rs.getString("description")));
						pos++;
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getItemImages): Could not insert image");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}		
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return images;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getPurchases(int)
	 */
	public ArrayList<Purchase> getPurchases(long userID, long ts){
		ArrayList<Purchase> purchases = new ArrayList<Purchase>();
		if(userID < 1) return purchases;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to get all of the purchases
					statement = conn.prepareStatement("SELECT * FROM `purchased`" +
							"LEFT OUTER JOIN `oldItems` on `purchased`.`itemID` = `oldItems`.`id`" +
							"WHERE `purchased`.`userID` = ? AND `purchased`.`ts`>? ORDER BY `purchased`.`purchaseDate` ASC");
					statement.setLong(1, userID);
					statement.setTimestamp(2, new Timestamp(ts));
					ResultSet rs = statement.executeQuery();
					
					while(rs.next()){
						Date startDate = new Date(System.currentTimeMillis());
						Date endDate = new Date(System.currentTimeMillis());
						
						try{
							startDate = new Date(rs.getTimestamp("oldItems.startDate").getTime());
							endDate = new Date(rs.getTimestamp("oldItems.endDate").getTime());
						}
						catch(Exception e){ }
						
						ArrayList<Image> images = this.getItemImages(rs.getLong("oldItems.id"));
						Item currentItem = new Item(rs.getLong("oldItems.id"),
								rs.getString("oldItems.name"),
								rs.getString("oldItems.description"),
								rs.getInt("oldItems.quantity"),
								rs.getDouble("oldItems.startPrice"),
								rs.getDouble("oldItems.reservePrice"),
								rs.getDouble("oldItems.buyNowPrice"),
								rs.getDouble("oldItems.currentBid"),
								rs.getDouble("oldItems.maxBid"),
								rs.getInt("oldItems.noOfBids"),
								startDate,
								endDate,
								rs.getLong("oldItems.sellerID"),
								rs.getLong("oldItems.categoryID"),
								rs.getString("oldItems.thumbnail"),
								images
								);
						
						purchases.add(new Purchase(rs.getLong("purchased.id"),
								currentItem,
								rs.getInt("purchased.quantity"),
								rs.getDouble("purchased.price"),
								rs.getBoolean("purchased.paid")));
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getPurchases): Could not get the purchases");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		
		return purchases;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getCurrentBids(int)
	 */
	public ArrayList<Bid> getCurrentBids(long userID, long ts){
		return this.getXBids(userID, "bids", "items", ts);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getOldBids(int)
	 */
	public ArrayList<Bid> getOldBids(long userID, long ts){
		return this.getXBids(userID, "oldBids", "oldItems", ts);
	}
	
	public ArrayList<Bid> getBids(long itemID){
		ArrayList<Bid> bids = new ArrayList<Bid>();
		if(itemID < 1) return bids;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;

				try {
					// Select the users highest bid for an item
					/*PreparedStatement statement = conn.prepareStatement("SELECT * FROM (SELECT * FROM (SELECT * FROM `"+table+"` WHERE `"+table+"`.`userID` = ? ORDER BY bidDate DESC) ords GROUP BY `itemID`) sings " +
							"LEFT OUTER JOIN `"+itemTable+"` on `sings`.`itemID` = `"+itemTable+"`.`id`" +
							" ORDER BY `"+itemTable+"`.`endDate` ASC");*/

					//PreparedStatement statement = conn.prepareStatement("SELECT * FROM `bids` where userID=? group by `itemID` order by `maxBid`");
					//PreparedStatement statement = conn.prepareStatement("SELECT *, MAX(`" + table + "`.`maxBid`) as rmaxBid FROM `" + table + "` LEFT OUTER JOIN `" + itemTable + "` on `" + itemTable + "`.`id` = `" + table + "`.`itemID` where `" + table + "`.`userID` = ? group by `" + table + "`.`itemID` order by `" + table + "`.`maxBid` DESC");
					for(int i=0;i<=1;i++){
						String table=null;
						String itemTable=null;
						if(i==0){
							table="bids";
							itemTable="items";
						}
						else if(i==1){
							table="oldBids";
							itemTable="oldItems";
						}
					statement = conn.prepareStatement("SELECT * FROM (SELECT * FROM `" + table + "` WHERE `" + table + "`.`userID` = ?  ORDER BY `" + table + "`.`bidDate` DESC) as bids_tmp LEFT OUTER JOIN `" + itemTable + "` on `" + itemTable + "`.`id` = `bids_tmp`.`itemID`  group by `bids_tmp`.`itemID` ORDER BY `" + itemTable + "`.`endDate`");

					statement.setLong(1, itemID);

					ResultSet rs = statement.executeQuery();

					while(rs.next()){
						Item currentItem;

						// If the item.id is '0' then the item is missing. This should not happen, but it could if the DB gets screwed up
						if(rs.getLong(itemTable+".id") == 0){
							currentItem = null;
						}
						else{
							ArrayList<Image> images = this.getItemImages(rs.getLong(itemTable+".id"));

							currentItem = new Item(rs.getLong(itemTable+".id"),
									rs.getString("name"),
									rs.getString("description"),
									rs.getInt(itemTable+".quantity"),
									rs.getDouble("startPrice"),
									rs.getDouble("reservePrice"),
									rs.getDouble("buyNowPrice"),
									rs.getDouble("currentBid"),
									rs.getDouble(itemTable+".maxBid"),
									rs.getInt("noOfBids"),
									new Date(rs.getTimestamp("startDate").getTime()),
									new Date(rs.getTimestamp("endDate").getTime()),
									rs.getLong("sellerID"),
									rs.getLong("categoryID"),
									rs.getString("thumbnail"),
									images
									);
						}

						User user=getUser(rs.getLong("userID"));
						Bid currentBid = new Bid(rs.getLong("bids_tmp.id"),
								rs.getLong("userID"),
								rs.getInt("bids_tmp.quantity"),
								rs.getDouble("bid"),
								rs.getDouble("bids_tmp.maxBid"),
								new Date(rs.getTimestamp("bidDate").getTime()),
								currentItem,
								user);
						bids.add(currentBid);
					}
					rs.close();
					}
					

					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getBids): Could not get the bids from bids or oldBids");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);

		return bids;

	}
	
	/**
	 * This method reads bids from the bids or oldBids table to get the bids that a user made
	 * @param userID
	 * @param table
	 * @param itemTable
	 * @return
	 */
	private ArrayList<Bid> getXBids(long userID, String table, String itemTable, long ts){
		ArrayList<Bid> bids = new ArrayList<Bid>();
		if(userID < 1) return bids;
		if(table==null || !(table.equals("bids") || table.equals("oldBids"))) return bids;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Select the users highest bid for an item
					/*PreparedStatement statement = conn.prepareStatement("SELECT * FROM (SELECT * FROM (SELECT * FROM `"+table+"` WHERE `"+table+"`.`userID` = ? ORDER BY bidDate DESC) ords GROUP BY `itemID`) sings " +
							"LEFT OUTER JOIN `"+itemTable+"` on `sings`.`itemID` = `"+itemTable+"`.`id`" +
							" ORDER BY `"+itemTable+"`.`endDate` ASC");*/
					
					//PreparedStatement statement = conn.prepareStatement("SELECT * FROM `bids` where userID=? group by `itemID` order by `maxBid`");
					//PreparedStatement statement = conn.prepareStatement("SELECT *, MAX(`" + table + "`.`maxBid`) as rmaxBid FROM `" + table + "` LEFT OUTER JOIN `" + itemTable + "` on `" + itemTable + "`.`id` = `" + table + "`.`itemID` where `" + table + "`.`userID` = ? group by `" + table + "`.`itemID` order by `" + table + "`.`maxBid` DESC");
					statement = conn.prepareStatement("SELECT * FROM (SELECT * FROM `" + table + "` WHERE `" + table + "`.`userID` = ? AND bidDate>? ORDER BY `" + table + "`.`maxBid` DESC) as bids_tmp LEFT OUTER JOIN `" + itemTable + "` on `" + itemTable + "`.`id` = `bids_tmp`.`itemID`  group by `bids_tmp`.`itemID` ORDER BY `" + itemTable + "`.`endDate`");
					
					statement.setLong(1, userID);
					statement.setTimestamp(2, new Timestamp(ts));
					
					ResultSet rs = statement.executeQuery();
					
					while(rs.next()){
						Item currentItem;
						
						// If the item.id is '0' then the item is missing. This should not happen, but it could if the DB gets screwed up
						if(rs.getLong(itemTable+".id") == 0){
							currentItem = null;
						}
						else{
							ArrayList<Image> images = this.getItemImages(rs.getLong(itemTable+".id"));
							currentItem = new Item(rs.getLong(itemTable+".id"),
									rs.getString("name"),
									rs.getString("description"),
									rs.getInt(itemTable+".quantity"),
									rs.getDouble("startPrice"),
									rs.getDouble("reservePrice"),
									rs.getDouble("buyNowPrice"),
									rs.getDouble("currentBid"),
									rs.getDouble(itemTable+".maxBid"),
									rs.getInt("noOfBids"),
									new Date(rs.getTimestamp("startDate").getTime()),
									new Date(rs.getTimestamp("endDate").getTime()),
									rs.getLong("sellerID"),
									rs.getLong("categoryID"),
									rs.getString("thumbnail"),
									images
									);
						}
						
						User user=getUser(rs.getLong("userID"));
						Bid currentBid = new Bid(rs.getLong("bids_tmp.id"),
								rs.getLong("userID"),
								rs.getInt("bids_tmp.quantity"),
								rs.getDouble("bid"),
								rs.getDouble("bids_tmp.maxBid"),
								new Date(rs.getTimestamp("bidDate").getTime()),
								currentItem,
								user);
						bids.add(currentBid);
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getXBids): Could not get the bids from " + table);
					e.printStackTrace();
				} finally{
						this.closeSmt(statement);
						this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return bids;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#insertUser(String, String, String, String, String)
	 */
	public Boolean insertUser(String username, String password, String email, String firstName, String lastName){
		if(username == null || password == null || email == null || firstName == null || lastName == null) return Boolean.FALSE;
		Boolean insertedUser = Boolean.FALSE;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection(true);

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to insert the user
					statement = conn.prepareStatement("INSERT INTO `users` (firstname, lastname, username, password, email,  creationDate, rating) "
											+ "VALUES (?, ?, ?, ?, ?, NOW(), 0)");
					statement.setString(1, firstName);
					statement.setString(2, lastName);
					statement.setString(3, username);
					statement.setString(4, password);
					statement.setString(5, email);
										
					statement.executeUpdate();
					
					insertedUser = true;
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (insertUser): Could not insert user");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		
		return insertedUser;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#updateUser(int, String, String, String, String, String)
	 */
	public Boolean updateUser(long userID, String password, String email, String firstName, String lastName){
		if(userID < 1) return Boolean.FALSE;
		if(password == null || email == null || firstName == null || lastName == null) return Boolean.FALSE;
		
		Boolean insertedUser = Boolean.FALSE;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection(true);

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to update the details
					statement = conn.prepareStatement("UPDATE `users` SET firstname = ?, lastname = ?, " +
							"password = ?, email = ? WHERE `users`.`id` = ?");
					statement.setString(1, firstName);
					statement.setString(2, lastName);
					statement.setString(3, password);
					statement.setString(4, email);
					statement.setLong(5, userID);
					
					int rows = statement.executeUpdate();

					if(rows>=1) insertedUser = true;		
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (updateUser): Could not update the user's details");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return insertedUser;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getFirstName(int)
	 */
	public String getFirstName(long userID){		
		if(userID < 1) return null;
		int attemptsRemaining = SQL_RETRIES;
		String name = null;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to get the user's first name
					statement = conn.prepareStatement("SELECT firstname FROM `users` WHERE `users`.`id` = ?");
					statement.setLong(1, userID);
					
					ResultSet rs = statement.executeQuery();
					
					// If an item is returned then get the details
					if(rs.next()){
						name = rs.getString("firstname");
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getFirstName): Could not get the user's firstname");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return name;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getPublicUser(int)
	 */
	public User getPublicUser(long userID){
		if(userID < 1) return null;
		User user = null;	
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("SELECT `username`,`rating` FROM `users` WHERE id = ?");
					statement.setLong(1, userID);
					ResultSet rs = statement.executeQuery();
					
					// Read the user's details
					if(rs.next()){
						user = new User(userID,rs.getString("users.username"),rs.getString("users.rating"));
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getPublicUser): Could not get the public user information");
					System.out.println(statement.toString());
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return user;
	}
	
	
	public ArrayList<User> getUsers(ArrayList<Long> sellerIDs){
		ArrayList<User> sellers = new ArrayList<User>();
		int attemptsRemaining = SQL_RETRIES;

		if(sellerIDs == null || sellerIDs.size()==0) return sellers;
		
		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				try {
					StringBuilder ids = new StringBuilder();
					
					ArrayList<Long> parsedIDs = new ArrayList<Long>();
					for(int i=0; i<sellerIDs.size(); i++){
						try{
							Long temp = sellerIDs.get(i);
							if(temp!=null) parsedIDs.add(temp);
						}
						catch(Exception e){
							// not longs
						}
					}
					
					if (parsedIDs.size() > 0) {
							for (int i = 0; i < sellerIDs.size() - 1; i++) {
								ids.append(" ? , ");
							}
							ids.append("?");
						
						
						PreparedStatement statement = conn.prepareStatement("SELECT `id`,`username`,`rating` FROM `users` WHERE id IN (" + ids.toString() + ")");
						//System.out.println(statement.toString());
						
						for(int i=0; i<parsedIDs.size(); i++){
							statement.setLong(i+1, sellerIDs.get(i));
						}
						
						ResultSet rs = statement.executeQuery();
						
						// Read the user's details
						
						while(rs.next()){
							User user = new User(rs.getLong("users.id"),rs.getString("users.username"),rs.getString("users.rating"));
							sellers.add(user);
						}
						
						rs.close();
						statement.close();
					}
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getSellers): Could not get the seller information");
					e.printStackTrace();
				}

				this.closeConnection(conn);
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return sellers;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getUser(int)
	 */
	public User getUser(long userID){
		if(userID < 1) return null;
		User user = null;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("SELECT * FROM `users` WHERE `users`.`id` = ?");
					statement.setLong(1, userID);
					ResultSet rs = statement.executeQuery();
					
					// Read the user's details
					if(rs.next()){
						user = new User(rs.getLong("users.id"),
								rs.getString("users.firstname"),
								rs.getString("users.lastname"),
								rs.getString("users.username"),
								rs.getString("users.password"),
								rs.getString("users.email"),
								rs.getString("users.authToken"),
								rs.getString("users.rating"));
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getUser): Could not get the user");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return user;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getAddress(int)
	 */
	public Address getAddress(long addressID){
		if(addressID < 1) return null;
		Address address = null;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("SELECT * FROM `addresses` WHERE `addresses`.`id` = ?");
					statement.setLong(1, addressID);
					ResultSet rs = statement.executeQuery();
					
					// Read the user's addresses
					if(rs.next()){
						address = new Address(rs.getLong("addresses.id"),
								rs.getLong("addresses.userID"),
								rs.getString("addresses.street"),
								rs.getString("addresses.town"),
								rs.getString("addresses.zip"),
								rs.getInt("addresses.state"),
								rs.getBoolean("addresses.isDefault"));
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getAddress): Could not get the address");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return address;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getDefaultAddress(int)
	 */
	public Address getDefaultAddress(long userID) throws Exception{
		ArrayList<Address> addresses = this.getAddresses(userID, Boolean.TRUE);
		
		// We can only return the default address if there is one
		if(addresses != null && addresses.size()>0)
			return  addresses.get(0);
		else
			return null;
	}
	
	public ArrayList<Address> getAddresses(long userID){
		return this.getAddresses(userID, Boolean.FALSE);
	}
	
	private ArrayList<Address> getAddresses(long userID, Boolean onlyDefault){
		if(userID < 1) return null;
		ArrayList<Address> addresses = new ArrayList<Address>();
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Set if we are only getting the default address
					String onlyDefaultString = "";
					if(onlyDefault) onlyDefaultString = " AND isDefault = 1";
					
					statement = conn.prepareStatement("SELECT * FROM `addresses` WHERE `userID` = ?" + onlyDefaultString);
					statement.setLong(1, userID);
					ResultSet rs = statement.executeQuery();
					
					// Read the user's addresses
					boolean getAll = true;
					while(rs.next() && getAll){
						Address address = new Address(rs.getLong("addresses.id"),
								rs.getLong("addresses.userID"),
								rs.getString("addresses.street"),
								rs.getString("addresses.town"),
								rs.getString("addresses.zip"),
								rs.getInt("addresses.state"),
								rs.getBoolean("addresses.isDefault"));
						
						addresses.add(address);
						
						if(onlyDefault) getAll = false;
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getAddresses): Could not get the addresses");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return addresses;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#usernameExists(String)
	 */
	public Boolean usernameExists(String username) throws Exception {
		if(username == null) return Boolean.TRUE;
		Boolean exists = Boolean.TRUE;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to see if the username address already exists
					statement = conn.prepareStatement("SELECT `username` FROM `users` WHERE `users`.`username` = ?");
					statement.setString(1, username);

					ResultSet rs = statement.executeQuery();

					// If the username does exist in the table
					if (rs.next()) {
						exists = Boolean.TRUE;
					}
					else{
						exists = Boolean.FALSE;
					}

					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (checkUsernameExists): Could not check username");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return exists;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#emailExists(String)
	 */
	public Boolean emailExists(String email){	
		if(email == null) return Boolean.TRUE;
		Boolean exists = Boolean.TRUE;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to see if the email address already exists
					
					// We'd rather use the index
					String[] emailSplit = email.split("@");
				
					statement = conn.prepareStatement("SELECT `email` FROM `users` WHERE `users`.`email` = ?");
					statement.setString(1, email);
					
					
					ResultSet rs = statement.executeQuery();
					
					// If the e-mail address does exist in the table
					if(rs.next()){
						exists = Boolean.TRUE;
					}
					else{
						exists = Boolean.FALSE;
					}
					
					rs.close();	
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (checkEmailExists): Could not check email");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return exists;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getAccount(int)
	 */
	public Account getAccount(long accountID) throws Exception{
		if(accountID < 1) return null;
		Account account = null;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("SELECT * FROM `accounts` WHERE id = ?" );
					statement.setLong(1, accountID);
					ResultSet rs = statement.executeQuery();
					
					// Read the user's account
					if(rs.next()){	
						account = new Account(rs.getLong("accounts.id"),
								rs.getString("accounts.name"),
								rs.getString("accounts.nameOnCard"),
								rs.getString("accounts.creditCardNo"),
								rs.getString("accounts.cvv"),
								rs.getTimestamp("accounts.expirationDate"));
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getAccount): Could not get the account");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return account;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getCategory(int)
	 */
	public Category getCategory(long categoryID) throws Exception{
		if(categoryID < 1) return null;
		Category category = null;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("SELECT * FROM `categories` WHERE id = ?");
					statement.setLong(1, categoryID);
					ResultSet rs = statement.executeQuery();
					
					// Get the category info
					if(rs.next()){
						category = new Category(rs.getLong("categories.id"),
								rs.getString("categories.name"),
								rs.getLong("categories.parent"),
								rs.getTimestamp("categories.ts").getTime());
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getCategory): Could not get the category");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return category;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getAllCategories()
	 */
	public ArrayList<Category> getAllCategories() throws Exception{
		ArrayList<Category> categories = new ArrayList<Category>();
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("SELECT * FROM `categories` ORDER BY `categories`.`id`");
					ResultSet rs = statement.executeQuery();
					
					// Put all of the categories in the result array
					while(rs.next()){
						Category category = new Category(rs.getLong("categories.id"),
								rs.getString("categories.name"),
								rs.getLong("categories.parent"),
								rs.getTimestamp("categories.ts").getTime());
						
						categories.add(category);
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getAllCategories): Could not get the categories");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return categories;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getCategories(int)
	 */
	public ArrayList<Category> getCategories(long parent, long timestamp) throws Exception{
		ArrayList<Category> categories = new ArrayList<Category>();
		if(parent < 0) return categories;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("SELECT * FROM `categories` WHERE parent = ? AND ts > ?");
					statement.setLong(1, parent);
					statement.setTimestamp(2, new Timestamp(timestamp));
					ResultSet rs = statement.executeQuery();
					
					// Put all of the states in to the results vector
					while(rs.next()){
						Category category = new Category(rs.getLong("categories.id"),
								rs.getString("categories.name"),
								rs.getLong("categories.parent"),
								rs.getTimestamp("categories.ts").getTime());
						
						categories.add(category);
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getCategories): Could not get the categories");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		
		return categories;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getStates()
	 */
	public ArrayList<String[]> getStates() throws Exception{
		ArrayList<String[]> states = new ArrayList<String[]>();
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("SELECT * FROM `states`");
					ResultSet rs = statement.executeQuery();
					
					// Put all of the states in to the results array
					while(rs.next()){
						String[] result = new String[3];
						result[0] = rs.getString("id");
						result[1] = rs.getString("shortName");
						result[2] = rs.getString("longName");
						
						states.add(result);
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getStates): Could not read results set");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return states;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*public Date getItemEndDate(int itemID){
		Connection conn = this.getConnection();
		Date endDate = null;
		
		if(conn != null){
			try{
				// Create the SQL statement to see get the item's end date
				PreparedStatement statement = conn.prepareStatement("SELECT `endDate` FROM `items` FORCE INDEX (`PRIMARY`) WHERE `items`.`id` = ?");
				statement.setInt(1, itemID);
				
				ResultSet rs = statement.executeQuery();
				
				// If there is a result, then the item exists and has an end date
				if(rs.next()){
					endDate = this.sdf.parse(rs.getString("endDate"));
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getItemEndTime): Could not get the item end time");
				e.printStackTrace();
			}
			
			this.closeConnection(conn);
		}
		
		return endDate;
	}*/
	
	/*public double getCurrentBid(int itemID){
		Connection conn = this.getConnection();
		double currentBid = double.MAX_VALUE;
		
		if(conn != null){
			try{
				// Create the SQL statement to see get the item's maxBid
				PreparedStatement statement = conn.prepareStatement("SELECT `currentBid` FROM `items` FORCE INDEX (`PRIMARY`) WHERE `items`.`id` = ?");
				statement.setInt(1, itemID);
				
				ResultSet rs = statement.executeQuery();
				
				// If there is a result, then the item exists and has a maxBid
				if(rs.next()){
					currentBid = rs.getDouble("currentBid");
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getCurrentBid): Could not get the item's current bid");
				e.printStackTrace();
			}
			
			this.closeConnection(conn);
		}
		
		return currentBid;
	}*/
	
	//TODO: maybe we should read this and current together since they'll both be checked
	/*public double getMaxBid(int itemID){
		Connection conn = this.getConnection();
		double maxBid = double.MAX_VALUE;
		
		if(conn != null){
			try{
				// Create the SQL statement to see get the item's maxBid
				PreparedStatement statement = conn.prepareStatement("SELECT `maxBid` FROM `items` FORCE INDEX (`PRIMARY`) WHERE `items`.`id` = ?");
				statement.setInt(1, itemID);
				
				ResultSet rs = statement.executeQuery();
				
				// If there is a result, then the item exists and has a maxBid
				if(rs.next()){
					maxBid = rs.getDouble("maxBid");
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getMaxBid): Could not get the item's max bid");
				e.printStackTrace();
			}
			
			this.closeConnection(conn);
		}
		
		return maxBid;
	}*/
	
	/*public int getQuantity(int itemID){
		Connection conn = this.getConnection();
		int quantity = Integer.MIN_VALUE;
		
		if(conn != null){
			try{
				// Create the SQL statement to see get the item's quantity
				PreparedStatement statement = conn.prepareStatement("SELECT `quantity` FROM `items` FORCE INDEX (`PRIMARY`) WHERE `items`.`id` = ?");
				statement.setInt(1, itemID);
				
				ResultSet rs = statement.executeQuery();
				
				// If there is a result, then the item exists and has a quantity
				if(rs.next()){
					quantity = rs.getInt("quantity");
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getQuantity): Could not get the item's quantity");
				e.printStackTrace();
			}
			
			this.closeConnection(conn);
		}
		
		return quantity;
	}*/
	
	
	

	

	
	public ArrayList<Bid> getAllBidsTTT(long itemID){
		return getAllXBidsTTT(itemID, "bids");
	}
	
	public ArrayList<Bid> getAllOldBidsTTT(long itemID){
		return getAllXBidsTTT(itemID, "oldBids");
	}
	
	public ArrayList<Bid> getAllXBidsTTT(long itemID, String table){
		ArrayList<Bid> allBids=new ArrayList<Bid>();
		if(table==null || !(table.equals("bids") || table.equals("oldBids"))) return allBids;
		
		Connection conn = this.getConnection();

		if(conn != null){
			PreparedStatement statement = null;
			try{
				// Create the SQL statement
				statement = conn.prepareStatement("SELECT * FROM `"+table+"` WHERE `itemID` = ? ORDER BY `bid` ASC");
				statement.setLong(1, itemID);

				ResultSet rs = statement.executeQuery();

				// get all the bids
				while(rs.next()){
					Item item=getItem(rs.getLong("itemID"),false);
					User user=getUser(rs.getLong("userID"));
					Bid newBid = new Bid(rs.getLong("id"),
					rs.getLong("userID"), rs.getInt("quantity"),
					rs.getDouble("bid"),rs.getDouble("maxBid"),
					new Date(rs.getTimestamp("bidDate").getTime()),item,user);
					allBids.add(newBid);
				}

				rs.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getAllXBids): Could not get the bids");
				e.printStackTrace();
			} finally{
				this.closeSmt(statement);
				this.closeConnection(conn);
			}
		}

		return allBids;
	}


	

	
	
	
	

	
	//TODO: check these
	/*public ArrayList<Bid> getCurrentBidsTime(int userID,Date timestamp){
		return this.getXBidsTime(userID, "bids", "items",timestamp);
	}
	
	// Items where the auction has finished, but the item has not been moved to the 'old' tables will not show
	// We could show expired items for some time? Or run the script that schecks for expired items frequently
	
	public ArrayList<Bid> getOldBidsTime(int userID, Date timestamp){
		return this.getXBidsTime(userID, "oldBids", "oldItems",timestamp);
	}
	
	//TODO: check these
	private ArrayList<Bid> getXBidsTime(int userID, String table, String itemTable,Date timestamp){
		ArrayList<Bid> bids = new ArrayList<Bid>();
		Connection conn = this.getConnection();

		if(conn != null){
			try{
				//TODO: maybe read the items separate? what if the user bids for the same item multiple times?
				
				// Create the SQL statement to see if the email address already exists
				PreparedStatement statement = conn.prepareStatement("SELECT * FROM `"+table+"` " +
						"LEFT OUTER JOIN `"+itemTable+"` on `"+table+"`.`itemID` = `"+itemTable+"`.`id` " +
						"WHERE `"+table+"`.`userID` = ? AND `"+table+"`.'modDate` = ? ORDER BY `"+itemTable+"`.`endDate` ASC");
				statement.setInt(1, userID);
				statement.setDate(2, (java.sql.Date) timestamp);
				ResultSet rs = statement.executeQuery();
				
				while(rs.next()){
					Item currentItem;
					
					// If the item.id is '0' then the item is missing. This should not happen, but it could if the DB gets screwed up
					if(rs.getInt(itemTable+".id") == 0){
						currentItem = null;
					}
					else{
						//ArrayList<Bid> allBids=this.getAllBids(rs.getInt(itemTable+".id"));


						currentItem= new Item(rs.getInt(itemTable+".id"),
								rs.getString(itemTable+".name"),
								rs.getString(itemTable+".description"),
								rs.getInt(itemTable+".quantity"),
								rs.getDouble(itemTable+".startPrice"),
								rs.getDouble(itemTable+".reservePrice"),
								rs.getDouble(itemTable+".buyNowPrice"),
								rs.getDouble(itemTable+".currentBid"),
								rs.getDouble(itemTable+".maxBid"),
								rs.getInt(itemTable+".noOfBids"),
								this.sdf.parse(rs.getString(itemTable+".startDate")),
								this.sdf.parse(rs.getString(itemTable+".endDate")),
								rs.getInt(itemTable+".sellerID"),
								rs.getInt(itemTable+".categoryID"),
								rs.getString(itemTable+".thumbnail"),
								new ArrayList<Image>()
								);


					}
					//TODO: increase Bid to have all field or don't select them
					
					Bid currentBid = new Bid(rs.getInt(table+".quantity"), rs.getDouble(table+".bid"), rs.getDouble(table+".maxBid"), this.sdf.parse(rs.getString(table+".bidDate")),currentItem);
					bids.add(currentBid);
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getXBids): Could not get the bids from " + table);
				e.printStackTrace();
			}
			
			this.closeConnection(conn);
		}
		
		return bids;
	}*/
	
	// People could have bidded for more than are available, if they bid, then someone bought some
	// auctionEnded
	
	
	// TODO: Buy item from win bid is different as we will be limited to the quantity of items we can buy
	// depending on the number of items that we won
	
	
	
	
	
	/*
	public void insertBid(int userID, int itemID, int quantity, double bid, double maxBid){
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				/*
				 * 1. For consistent budding we need to make sure no one enters a lower-maxBid when we are writing
				 * 		- otherwise they may beat the current max bid and write that they are winning
				 * 		- but they didn't beat the bid that we are halfway through writing
				 * 2. So we must stop people updating the item or entering a bid while we are updating the item
				 * 3. Whoever is winning the bidding now must have the current largest 'maxBid'
				 * 		- The current bed bid is the lowest maxBid+0.01, or just our bid if we bid more than the previous maxBid
				 * 4. The items new price will be the smallest of the 'maxBid' prices plus one cent, or the bid value
				 * 		- unless both maxBids are the same, then the bid will be a draw and the price will be maxBid
				 * 		- (we know that the bid is higher than the currentBid or this method should not have been called)
				 * 5. We'll also update the number of bids value no matter what happens
				 * 6. Lastly, just in case there was some out of order update, we'll make sure the new bids are greater than the old ones
				 
				
				// SQL to lock the items table
				PreparedStatement lockTableStatement = conn.prepareStatement("LOCK TABLES items WRITE");
				
				// Get the item's currentBid and maxBid values (and noOfBids)
				PreparedStatement getMaxBidStatement = conn.prepareStatement("SELECT currentBid, maxBid, noOfBids, currentWinner FROM `items` FORCE INDEX (`PRIMARY`)" +
						"WHERE `items`.`id` = ?");
				getMaxBidStatement.setInt(1, itemID);
				
				// SQL to update the item with the new bids
				PreparedStatement updateItemStatement = conn.prepareStatement("UPDATE `items` SET `items`.`currentBid` = ?, `items`.`maxBid` = ?, `items`.`noOfBids` = ?, `items`.`currentWinner` = ? WHERE `items`.`id` = ?");
				updateItemStatement.setInt(5,itemID);
				
				// Unlock the tables
				PreparedStatement unlockTableStatement = conn.prepareStatement("UNLOCK TABLES");
				
				// SQL to enter the user's bid
				PreparedStatement insertBidStatement = conn.prepareStatement("INSERT INTO `bids` (userID, itemID, quantity, bid, maxBid, bidDate) "
						+ "VALUES (?, ?, ?, ?, ?, NOW())");
				insertBidStatement.setInt(1, userID);
				insertBidStatement.setInt(2, itemID);
				insertBidStatement.setInt(3, quantity);
				insertBidStatement.setDouble(4, bid);
				insertBidStatement.setDouble(5, maxBid);
				
				//TODO: Andy: I feel like we may still get a sync issue here? maybe not if they'll all wait for the lock...
				//UPDATE: Andy: I actually think this is fine. anyone else have thoughts?
				lockTableStatement.executeUpdate();
				
				insertBidStatement.executeUpdate();
				insertBidStatement.close();
				
				ResultSet rs = getMaxBidStatement.executeQuery();	
				
				if(rs.next()){
					double dbCurrentBid = rs.getDouble("currentBid");
					double dbMaxBid = rs.getDouble("maxBid");
					int dbNoOfBids = rs.getInt("noOfBids");
					int currentWinner = rs.getInt("currentWinner");
					
					if(dbMaxBid==maxBid){
						// New max bid is the same as old max bid, old user is still winning as they bidded first
						updateItemStatement.setDouble(1,dbMaxBid);
						updateItemStatement.setDouble(2,dbMaxBid);
						updateItemStatement.setInt(3,dbNoOfBids+1);
						updateItemStatement.setInt(4,currentWinner);
						
					}
					else if(dbMaxBid>maxBid){
						// The old max bid is greater, old user still winning by 0.01c
						// max bid is the same, current bid is the new bid's maxPrice+1c
						updateItemStatement.setDouble(1,maxBid + 0.01);
						updateItemStatement.setDouble(2,dbMaxBid);
						updateItemStatement.setInt(3,dbNoOfBids+1);
						updateItemStatement.setInt(4,currentWinner);
					}
					else{
						// Else the new bid is more, the new user is winning
						// The new user's bid will be the old user's max(maxBid+1c, new bid)
						updateItemStatement.setDouble(1,(bid>dbMaxBid) ? bid : new double(dbMaxBid + 0.01));
						updateItemStatement.setDouble(2,maxBid);
						updateItemStatement.setInt(3,dbNoOfBids+1);
						updateItemStatement.setInt(4,userID);
					}
					
					// Update the item table
					
					
					// These should not be needed as long as there are no out of order bids
					// We could also move the setting of the variables in the if-else structure. But I did it the
					// long way for clarity
					if(dbCurrentBid > newCurrentBid) newCurrentBid = dbCurrentBid;
					if(dbMaxBid > newMaxBid) newMaxBid = dbMaxBid;
					
					updateItemStatement.setDouble(1,newCurrentBid);
					updateItemStatement.setDouble(2,newMaxBid);
					updateItemStatement.setInt(3,dbNoOfBids+1);
					updateItemStatement.setInt(4,newWinningUser);
					
					updateItemStatement.executeUpdate();
				}
				unlockTableStatement.executeUpdate();
				unlockTableStatement.close();
				
				lockTableStatement.close();
				getBidsStatement.close();
				
				// Inserting the bid does not need to be donw with the lock as the info used to compare is held in the item
				insertBidStatement.executeUpdate();
				insertBidStatement.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (insertBid): Could not get insert bid");
				e.printStackTrace();
			}
			
			this.closeConnection(conn);
		}
	}*/
	

	

	public ArrayList<Item> getCurrentSellingItemsTime(long userID, long timestamp){
		return this.getXSellingItemsTime(userID, "items",timestamp);
	}
	
	public ArrayList<Item> getOldSellingItemsTime(long userID, long timestamp){
		return this.getXSellingItemsTime(userID, "oldItems",timestamp);
	}
	
	protected ArrayList<Item> getXSellingItemsTime(long userID, String table, long timestamp){
		ArrayList<Item> items = new ArrayList<Item>();
		if(table==null || !(table.equals("items") || table.equals("oldItems"))) return items;
			
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				// Create the SQL statement to see if the email address already exists
				PreparedStatement statement = conn.prepareStatement("SELECT * FROM `" + table + "` " +
						"WHERE `" + table + "`.`sellerID` = ? AND `" + table + "`.`ts` > ? ORDER BY `" + table + "`.`endDate` ASC");
				statement.setLong(1, userID);
				statement.setTimestamp(2, new Timestamp(timestamp));
				ResultSet rs = statement.executeQuery();
				
				while(rs.next()){

					//ArrayList<Bid> allBids=this.getAllBids(rs.getInt("items.id"));
					ArrayList<Image> images = this.getItemImages(rs.getLong("id"));
					Item currentItem = new Item(rs.getLong("id"),
							rs.getString("name"),
							rs.getString("description"),
							rs.getInt("quantity"),
							rs.getDouble("startPrice"),
							rs.getDouble("reservePrice"),
							rs.getDouble("buyNowPrice"),
							rs.getDouble("currentBid"),
							rs.getDouble("maxBid"),
							rs.getInt("noOfBids"),
							new Date(rs.getTimestamp("startDate").getTime()),
							new Date(rs.getTimestamp("endDate").getTime()),
							rs.getLong("sellerID"),
							rs.getLong("categoryID"),
							rs.getString("thumbnail"),
							images
							);
					items.add(currentItem);
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.out.println("MySQLQuery (getXSellingItemsTime): Could not get the items");
				e.printStackTrace();
			}
			
			this.closeConnection(conn);
		}
		
		return items;
	}
	
	
	
	
	//@Deprecated
	/*public ArrayList<Item> getTextItemsDB(String text, int page, int itemsPP) throws Exception{
		return getTextItems(text, page, itemsPP, 0, Boolean.FALSE);
	}*/
	
	public ArrayList<Item> getTextItemsDB(String text, int page, int itemsPP, int sortCol, Boolean sortDec) throws Exception{
		return getTextItemsDB(text,page,itemsPP,sortCol,sortDec,false,0);
	}
	
	public ArrayList<Item> getTextItemsDB(String text, int page, int itemsPP, int sortCol, Boolean sortDec,Boolean getimages,int numImages) throws Exception{
		ArrayList<Item> items = new ArrayList<Item>();
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				// Create the sort strings
				String orderBy = "";
				String sortDecSt = " ASC";
				
				
				switch(sortCol){
					case 0: orderBy = "endDate"; break;
					case 1: orderBy = "currentBid"; break;
					case 2: orderBy = "endDate"; break;
					default: orderBy = "endDate"; break;
				}
				
				if(sortDec) sortDecSt = " DESC";
				
				// Create the SQL statement to get items with the correct search terms
				PreparedStatement statement = conn.prepareStatement("SELECT * FROM `items` WHERE MATCH (name, description)" +
						" AGAINST (? IN BOOLEAN MODE) AND `endDate` > NOW() ORDER BY `items`.`"+ orderBy +"` "+ sortDecSt + " LIMIT ? OFFSET ?");
				
				statement.setString(1, text);
				statement.setInt(2, itemsPP);
				statement.setInt(3, page);
				
				ResultSet rs = statement.executeQuery();
				
				int imgCount=0;
				while(rs.next()){
					ArrayList<Image> images = new ArrayList<Image>();
					if(getimages==true&&imgCount<numImages){
						images = this.getItemImages(rs.getLong("items.id"));
						imgCount++;
					}
					
					Item currentItem = new Item(rs.getLong("items.id"),
							rs.getString("items.name"),
							rs.getString("items.description"),
							rs.getInt("items.quantity"),
							rs.getDouble("items.startPrice"),
							rs.getDouble("items.reservePrice"),
							rs.getDouble("items.buyNowPrice"),
							rs.getDouble("items.currentBid"),
							rs.getDouble("items.maxBid"),
							rs.getInt("items.noOfBids"),
							new Date(rs.getTimestamp("items.startDate").getTime()),
							new Date(rs.getTimestamp("items.endDate").getTime()),
							rs.getLong("items.sellerID"),
							rs.getLong("items.categoryID"),
							rs.getString("items.thumbnail"),
							images
							);
					items.add(currentItem);
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getTextItems): Could not get the items");
				e.printStackTrace();
				
				this.closeConnection(conn);
				
				throw e;
			}
			
			this.closeConnection(conn);
		}
		
		return items;
	}
	
	public ArrayList<Long> getTextItemsIDsDB(String text, int page, int itemsPP, int sortCol, Boolean sortDec) throws Exception{
		ArrayList<Long> items = new ArrayList<Long>();
		if(text==null || page<0 ||itemsPP<=0) return items;
		
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				// Create the sort strings
				String orderBy = "";
				String sortDecSt = " ASC";
				
				
				switch(sortCol){
					case 0: orderBy = "endDate"; break;
					case 1: orderBy = "currentBid"; break;
					case 2: orderBy = "endDate"; break;
					default: orderBy = "endDate"; break;
				}
				
				if(sortDec) sortDecSt = " DESC";
				
				// Create the SQL statement to get items with the correct search terms
				PreparedStatement statement = conn.prepareStatement("SELECT id FROM `items` WHERE MATCH (name, description)" +
						" AGAINST (? IN BOOLEAN MODE) AND `endDate` > NOW() ORDER BY `items`.`"+ orderBy +"` "+ sortDecSt + " LIMIT ? OFFSET ?");
				
				statement.setString(1, text);
				statement.setInt(2, itemsPP);
				statement.setInt(3, page);
				
				ResultSet rs = statement.executeQuery();
				
				while(rs.next()){
					items.add(rs.getLong("items.id"));
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getTextItemsIDsDB): Could not get the items");
				e.printStackTrace();
				
				this.closeConnection(conn);
				
				throw e;
			}
			
			this.closeConnection(conn);
		}
		
		return items;
	}
	
	public ArrayList<Item> getItemsByID(ArrayList<Long> itemIDs, int sortCol, Boolean sortDec) throws Exception{
		return getItemsByID(itemIDs,sortCol,sortDec,false,0);
	}
	
	public ArrayList<Item> getItemsByID(ArrayList<Long> itemIDs, int sortCol, Boolean sortDec,Boolean getimages,int numImages) throws Exception{
		ArrayList<Item> items = new ArrayList<Item>();
		if(itemIDs ==null || itemIDs.size()==0) return items;
		Connection conn = this.getConnection();
		
		if(conn != null && itemIDs != null && itemIDs.size()>0){
			try{
				// Create the sort strings
				String orderBy = "";
				String sortDecSt = " ASC";
				int itemIDSize = itemIDs.size();
				
				switch(sortCol){
					case 0: orderBy = "endDate"; break;
					case 1: orderBy = "currentBid"; break;
					case 2: orderBy = "endDate"; break;
					default: orderBy = "endDate"; break;
				}
				
				if(sortDec) sortDecSt = " DESC";
				
				// Add the ?'s for the item IDs
				StringBuilder qmarks = new StringBuilder();
				//boolean first = true;
				
				ArrayList<Long> pasrsedItemIDs = new ArrayList<Long>();
				for(int i=0; i < itemIDSize; i++){
					try{
						Long temp = itemIDs.get(i);
						if(temp!=null) pasrsedItemIDs.add(temp);
					}
					catch(Exception e){
						// class parse error
					}
				}
				
				if(pasrsedItemIDs.size()>0){
					for(int i=0; i < pasrsedItemIDs.size()-1; i++)
						qmarks.append("?,");
					
					qmarks.append('?');
					
					// Create the SQL statement to get items with the correct search terms
					PreparedStatement statement = conn.prepareStatement("SELECT * FROM `items` WHERE `id` IN (" + qmarks + " )" +
							" ORDER BY `items`.`"+ orderBy +"` "+ sortDecSt);
					
					// Set all of the itemIDs
					for(int i=0; i < pasrsedItemIDs.size(); i++)
						statement.setLong(i+1, pasrsedItemIDs.get(i));
					
					ResultSet rs = statement.executeQuery();
					
					int imgCount=0;
					while(rs.next()){
						ArrayList<Image> images = new ArrayList<Image>();
						if(getimages==true&&imgCount<numImages){
							images = this.getItemImages(rs.getLong("items.id"));
							imgCount++;
						}
						//ArrayList<Bid> allBids=this.getAllBids(rs.getInt("items.id"));

						Item currentItem = new Item(rs.getLong("items.id"),
								rs.getString("items.name"),
								rs.getString("items.description"),
								rs.getInt("items.quantity"),
								rs.getDouble("items.startPrice"),
								rs.getDouble("items.reservePrice"),
								rs.getDouble("items.buyNowPrice"),
								rs.getDouble("items.currentBid"),
								rs.getDouble("items.maxBid"),
								rs.getInt("items.noOfBids"),
								new Date(rs.getTimestamp("items.startDate").getTime()),
								new Date(rs.getTimestamp("items.endDate").getTime()),
								rs.getLong("items.sellerID"),
								rs.getLong("items.categoryID"),
								rs.getString("items.thumbnail"),
								images
								);
						items.add(currentItem);
					}
					
					rs.close();
					statement.close();
				}	
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getItemsByID): Could not get the items");
				e.printStackTrace();
				
				this.closeConnection(conn);
				
				throw e;
			}
			
			this.closeConnection(conn);
		}
		
		return items;
	}
	
	// Returns the list of ids that will be displayed in a search
	public ArrayList<Long> getCategoryItemsIDs(long categoryID, int page, int itemsPP, int sortCol, Boolean sortDec, long lastSeenID)  throws Exception{
		ArrayList<Long> items = new ArrayList<Long>();
		if(categoryID<0 || page<0 || itemsPP<0 ||sortCol<0) return items;
		if(sortDec==null) return items;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement statement;
				
				// Create the sort strings
				String orderBy = "";
				String sortDecSt = " ASC";
				
				
				switch(sortCol){
					case 0: orderBy = "endDate"; break;
					case 1: orderBy = "currentBid"; break;
					case 2: orderBy = "endDate"; break;
					default: orderBy = "endDate"; break;
				}
				
				if(sortDec) sortDecSt = " DESC";
				
				// Only filter the category if one is provided
				if(categoryID>0){	
					// Filter by the category
					statement = conn.prepareStatement("SELECT id FROM `items` " +
							"WHERE `items`.`categoryID` = ? AND `items`.`endDate` > NOW() AND `items`.`quantity` > 0 ORDER BY `items`.`"+ orderBy+"` " +sortDecSt+" LIMIT ? OFFSET ?" );
					statement.setLong(1, categoryID);
					statement.setInt(2, itemsPP);
					statement.setInt(3, page*itemsPP);
				}
				else{
					// Get all categories
					statement = conn.prepareStatement("SELECT id FROM `items` " +
							"WHERE `items`.`endDate` > NOW() AND `items`.`quantity` > 0 ORDER BY `items`.`"+ orderBy+"` " +sortDecSt+ " LIMIT ? OFFSET ?" );	
					statement.setInt(1, itemsPP);
					statement.setInt(2, page*itemsPP);
				}
				
				ResultSet rs = statement.executeQuery();

				while(rs.next()){
					//ArrayList<Bid> allBids=this.getAllBids(rs.getInt("items.id"));
					
					items.add(rs.getLong("items.id"));
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getCategoryItemsIds): Could not get the items");
				e.printStackTrace();
				throw e;
			}
			
			this.closeConnection(conn);
		}
		
		return items;
	}
	
	public ArrayList<Item> getCategoryItems(long categoryID, int page, int itemsPP, int sortCol, Boolean sortDec,Boolean getimages, String[] hasItems, long lastSeenID) throws Exception{
		if(getimages!=null){
			if(getimages==true)
				return getCategoryItems(categoryID,page,itemsPP,sortCol,sortDec,getimages,itemsPP,hasItems,lastSeenID);
			else
				return getCategoryItems(categoryID,page,itemsPP,sortCol,sortDec,getimages,0,hasItems,lastSeenID);
		}
		else{
			return new ArrayList<Item>();
		}
	}
	
	public ArrayList<Item> getCategoryItems(long categoryID, int page, int itemsPP, int sortCol, Boolean sortDec,Boolean getimages,int numImages, String[] hasItems, long lastSeenID)  throws Exception{
		ArrayList<Item> items = new ArrayList<Item>();
		if(categoryID<0 || page<0 ||itemsPP<0 ||sortCol<0) return items;
		if(sortDec==null || getimages==null || hasItems==null) return items;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement statement;
				
				// Create the sort strings
				String orderBy = "";
				String sortDecSt = " ASC";
				
				
				switch(sortCol){
					case 0: orderBy = "endDate"; break;
					case 1: orderBy = "currentBid"; break;
					case 2: orderBy = "endDate"; break;
					default: orderBy = "endDate"; break;
				}
				
				if(sortDec) sortDecSt = " DESC";
				
				StringBuilder qmarks = new StringBuilder();
				
				if(hasItems!=null && hasItems.length>0){
					for(int i=0; i < hasItems.length; i++)
						qmarks.append(",?");
				}
				
				// Only filter the category if one is provided
				if(categoryID>0){	
					// Filter by the category
					statement = conn.prepareStatement("SELECT * FROM(SELECT * FROM `items` " +
							"WHERE `items`.`categoryID` = ? AND `items`.`endDate` > NOW() AND `items`.`quantity` > 0 ORDER BY `items`.`"+ orderBy+"` " +sortDecSt+" LIMIT ? OFFSET ?) AS items WHERE id NOT IN (''"+qmarks+")" );
					statement.setLong(1, categoryID);
					statement.setInt(2, itemsPP);
					statement.setInt(3, page*itemsPP);
					
					for(int i=0;i<hasItems.length; i++)
						statement.setString(i+4, hasItems[i]);
					
				}
				else{
					// Get all categories
					statement = conn.prepareStatement("SELECT * FROM(SELECT * FROM `items` " +
							"WHERE `items`.`endDate` > NOW() AND `items`.`quantity` > 0 ORDER BY `items`.`"+ orderBy+"` " +sortDecSt+ " LIMIT ? OFFSET ?) AS items WHERE id NOT IN (''"+qmarks+")" );	
					statement.setInt(1, itemsPP);
					statement.setInt(2, page*itemsPP);
					
					for(int i=0;i<hasItems.length; i++)
						statement.setString(i+3, hasItems[i]);
				}
				
				ResultSet rs = statement.executeQuery();

				int imgCount=0;
				while(rs.next()){
					ArrayList<Image> images = new ArrayList<Image>();
					if(getimages==true&&imgCount<numImages){
						images = this.getItemImages(rs.getLong("items.id"));
						imgCount++;
					}
					
					Item currentItem = new Item(rs.getLong("items.id"),
							rs.getString("items.name"),
							rs.getString("items.description"),
							rs.getInt("items.quantity"),
							rs.getDouble("items.startPrice"),
							rs.getDouble("items.reservePrice"),
							rs.getDouble("items.buyNowPrice"),
							rs.getDouble("items.currentBid"),
							rs.getDouble("items.maxBid"),
							rs.getInt("items.noOfBids"),
							new Date(rs.getTimestamp("items.startDate").getTime()),
							new Date(rs.getTimestamp("items.endDate").getTime()),
							rs.getLong("items.sellerID"),
							rs.getLong("items.categoryID"),
							rs.getString("items.thumbnail"),
							images
							);
					items.add(currentItem);
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getCategoryItems): Could not get the items ");
				e.printStackTrace();
				throw e;
			}
			
			this.closeConnection(conn);
		}
		
		return items;
	}
	
	/*public ArrayList<Item> getCategoryItems(int categoryID, int page, int itemsPP) throws Exception{
		return getCategoryItems(categoryID, page, itemsPP, 0, Boolean.FALSE,Boolean.FALSE);
	}*/
	
	public ArrayList<User> getAllUserData(int itemsPerPage, int pageNo){
		ArrayList<User> users = new ArrayList<User>();
		if(itemsPerPage<0 || pageNo<0) return users;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("SELECT * FROM `users` LIMIT ? OFFSET ?");
					statement.setInt(1, itemsPerPage);
					statement.setInt(2, itemsPerPage*pageNo);
					
					ResultSet rs = statement.executeQuery();
					
					// Put all of the users in the array list
					
					while(rs.next()){
						users.add( new User(rs.getLong("users.id"),
								rs.getString("users.firstname"),
								rs.getString("users.lastname"),
								rs.getString("users.username"),
								rs.getString("users.password"),
								rs.getString("users.email"),
								rs.getString("users.authToken"),
								rs.getString("users.rating")));
					}
					
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getAllUserDate): Could not read results set");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return users;
	}
	
	
	public long getUserCount(){
		long count =0;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("SELECT count(id) FROM `users`");
					
					ResultSet rs = statement.executeQuery();
					
					// Get the count
					if(rs.next()){
						count = rs.getLong(1);
					}

					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getAllUserDate): Could not read results set");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return count;
	}
	
	
	
	
	//TODO: make create items methods?
	

	

	

	
	
	
	
	

	

	
	public boolean insertAccount(String name, String nameOnCard, String creditCardNo, String cvv, Date expirationDate){
		if(name==null || nameOnCard==null ||creditCardNo==null|| cvv==null || expirationDate==null) return false;
		boolean insertedAccount = false;
		int attemptsRemaining = SQL_RETRIES;
		
		cvv = cvv.substring(0, Math.min(cvv.length(), 4));
		creditCardNo = creditCardNo.substring(0, Math.min(creditCardNo.length(),19));
		
		do{
			Connection conn = this.getConnection(true);
			
			if(conn != null){
				try{
					// Create the SQL statement to insert the user
					PreparedStatement statement = conn.prepareStatement("INSERT INTO `accounts` (name, nameOnCard, creditCardNo, cvv, expirationDate) "
											+ "VALUES (?, ?, ?, ?, ?)");
					statement.setString(1, name);
					statement.setString(2, nameOnCard);
					statement.setString(3, creditCardNo);
					statement.setString(4, cvv);
					
					if(expirationDate.after(new Date(18000000)))
						statement.setTimestamp(5, new java.sql.Timestamp(expirationDate.getTime()));
					else
						statement.setTimestamp(5, new java.sql.Timestamp(18000000));
					
					statement.executeUpdate();
					statement.close();
					
					insertedAccount = true;
					attemptsRemaining = 0;
				}
				catch(CommunicationsException e){
					// This occurs if the SQL connection has been left open for a long time
					this.forceCloseConnection(conn);
					this.checkConnections();
				}
				catch(MySQLNonTransientConnectionException e){
					// This occurs if the SQL connection has been left open for a long time
					this.forceCloseConnection(conn);
					this.checkConnections();
				}
				catch(Exception e){
					System.err.println("MySQLQuery (insertAccount): Could not insert account");
					e.printStackTrace();
					
					insertedAccount = false;
				}
				
				this.closeConnection(conn);
			}
		
			attemptsRemaining--;
		}while(attemptsRemaining > 0);
		
		return insertedAccount;
	}
	
	
	
	

	

	

	
	public String getStateName(long stateID){
		String state = null;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement statement = conn.prepareStatement("SELECT `longName` FROM `states` WHERE `id` = ?");
				statement.setLong(1, stateID);
				ResultSet rs = statement.executeQuery();
				
				// Get the state name
				if(rs.next()){
					state = rs.getString("states.longName");
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getStateName): Could not get the state name");
				e.printStackTrace();
			}
			
			this.closeConnection(conn);
		}
		
		return state;
	}
	

	

	

	

	
	public long moveEndedItemsDB() throws Exception{
		long itemsMoved = 0;
		int attemptsRemaining = 50;
		
		/**
		 * 1. Get the ids of all the items that have ended
		 * 2. create the purchase rows for people who have won items (must have beat reserve)
		 * 3. move all of the bids for the old items to the old bids table
		 * 4. move all of the old items to the old items table
		 */
		
		do{
			Connection conn = this.getConnection(true);
			
			if(conn != null){
				// Get the current time so we only move items before this fixed time
				long currentTime = System.currentTimeMillis();
				
				try{
					//System.out.println("moving old items");
					
					// copy the old bids to the old bids table
					PreparedStatement copyBids = conn.prepareStatement("INSERT IGNORE INTO `oldBids` SELECT * FROM `bids` WHERE `bids`.`itemID` IN (SELECT `id` FROM `items` WHERE `items`.`endDate` < ?)");
					copyBids.setTimestamp(1, new java.sql.Timestamp(currentTime));
					itemsMoved = copyBids.executeUpdate();
					copyBids.close();
					
					// Delete the replica bids from the bids table
					PreparedStatement deleteBids = conn.prepareStatement("DELETE FROM `bids` WHERE `bids`.`itemID` IN (SELECT `id` FROM `items` WHERE `items`.`endDate` < ?)");
					deleteBids.setTimestamp(1, new java.sql.Timestamp(currentTime));
					deleteBids.executeUpdate();
					deleteBids.close();
					
					// copy the old items to the old items table
					PreparedStatement copyItems = conn.prepareStatement("INSERT IGNORE INTO `oldItems` (SELECT * FROM `items` WHERE `items`.`endDate` < ?)");
					copyItems.setTimestamp(1, new java.sql.Timestamp(currentTime));
					copyItems.executeUpdate();
					copyItems.close();
					
					// delete the old items from the items table
					PreparedStatement deleteItems = conn.prepareStatement("DELETE FROM `items` WHERE `items`.`endDate` < ?");
					deleteItems.setTimestamp(1, new java.sql.Timestamp(currentTime));
					int rows = deleteItems.executeUpdate();
					deleteItems.close();
					
					itemsMoved+=rows;
					
					attemptsRemaining = 0;
				}
				catch(CommunicationsException e){
					// This occurs if the SQL connection has been left open for a long time
					this.forceCloseConnection(conn);
					this.checkConnections();
				}
				catch(MySQLNonTransientConnectionException e){
					// This occurs if the SQL connection has been left open for a long time
					this.forceCloseConnection(conn);
					this.checkConnections();
				}
				catch(Exception e){
					System.out.println("MySQLQuery (moveEndedItems): Could not read results set");
					e.printStackTrace();
					this.closeConnection(conn);
					
					throw e;
				}
				
				this.closeConnection(conn);
			}
		
			attemptsRemaining--;
		}while(attemptsRemaining>0);
		
		return itemsMoved;
	}

	/*@Override
	public Boolean insertVideo(String URL) {		
		int attemptsRemaining = SQL_RETRIES;
		Boolean insertedVideo = Boolean.FALSE;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement insertVideo = null;
				
				try {
					// Create the SQL statement to insert the image URL
					insertVideo = conn.prepareStatement("INSERT INTO `johnvideo` (url) "
							+ "VALUES (?)");
					insertVideo.setString(1, URL);
					
					// insert the image
					insertVideo.executeUpdate();
					insertVideo.close();					
					insertedVideo = Boolean.TRUE;
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (insertImage): Could not insert image");
					e.printStackTrace();
				} finally{
					this.closeSmt(insertVideo);
					
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return insertedVideo;
	}

	@Override
	public ArrayList<String> getVideos() {
		ArrayList<String> videos = new ArrayList<String>();
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{

				// Create the SQL statement to get items with the correct search terms
				PreparedStatement statement = conn.prepareStatement("SELECT * FROM `johnvideo`");
				ResultSet rs = statement.executeQuery();
				
				while(rs.next()){
					videos.add(rs.getString("johnvideo.url"));
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getItemsByID): Could not get the items");
				e.printStackTrace();
				
				this.closeConnection(conn);
				
				try {
					throw e;
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			this.closeConnection(conn);
		}
		
		return videos;
	}*/
	/**
	 * @author  Bo (bol1@andrew.cmu.edu)
	 * */
	public long insertComment(long userID, long sellerID, long itemID, int rating, Date endDate, String comment){
		long commentID = -1;
		if(userID<1 || sellerID<1 ||itemID<1 ) return commentID;
		if(endDate==null || comment==null) return commentID;
		int attemptsRemaining = SQL_RETRIES;
	
		do{
			Connection conn = this.getConnection(true);
			if(conn != null){
				try{
					// Create the statement to get the commentsID
					PreparedStatement getCommentID = conn.prepareStatement("SELECT LAST_INSERT_ID()");
					
					// Create the SQL statement to insert the comment
					PreparedStatement statement = conn.prepareStatement("INSERT INTO `comments` (from_user_id, to_user_id, item_id, rating, date, comment) "
											+ "VALUES (?, ?, ?, ?, ?, ?)");
					statement.setLong(1, userID);
					statement.setLong(2, sellerID);
					statement.setLong(3, itemID);
					statement.setInt(4, rating);
					if(endDate.after(new Date(18000000)))
						statement.setTimestamp(5, new java.sql.Timestamp(endDate.getTime()));
					else
						statement.setTimestamp(5, new java.sql.Timestamp(18000000));
					
					statement.setString(6, comment);
					
					statement.executeUpdate();
					statement.close();
					
					ResultSet rs = getCommentID.executeQuery();
					if(rs.next()){
						commentID = rs.getLong(1);
					}
					attemptsRemaining = 0;
				}
				catch(CommunicationsException e){
					// This occurs if the SQL connection has been left open for a long time
					this.forceCloseConnection(conn);
					this.checkConnections();
				}
				catch(MySQLNonTransientConnectionException e){
					// This occurs if the SQL connection has been left open for a long time
					this.forceCloseConnection(conn);
					this.checkConnections();
				}
				catch(Exception e){
					System.err.println("MySQLQuery (insertComment): Could not insert comment");
					e.printStackTrace();
					
					commentID = -1;
				}
				
				this.closeConnection(conn);
			}
		
			attemptsRemaining--;
		}while(attemptsRemaining > 0);
		
		return commentID;
	}
	
	public long insertQuestion(long fromUserID, long toUserID, long itemID, Date date, String question){
		long questionID = -1;
		if(fromUserID<1 || toUserID<1|| itemID<1) return questionID;
		if(date==null || question==null) return questionID;
		int attemptsRemaining = SQL_RETRIES;
	
		do{
			Connection conn = this.getConnection(true);
			if(conn != null){
				try{
					// Create the statement to get the questionID
					PreparedStatement getQuestionID = conn.prepareStatement("SELECT LAST_INSERT_ID()");
					
					// Create the SQL statement to insert the question
					PreparedStatement statement = conn.prepareStatement("INSERT INTO `questions` (from_user_id, to_user_id, item_id, is_question, date, content) "
											+ "VALUES (?, ?, ?, ?,?, ?)");
					statement.setLong(1, fromUserID);
					statement.setLong(2, toUserID);
					statement.setLong(3, itemID);
					statement.setString(4, "Y");
					
					if(date.after(new Date(18000000)))
						statement.setTimestamp(5, new java.sql.Timestamp(date.getTime()));
					else
						statement.setTimestamp(5, new java.sql.Timestamp(18000000));
					
					statement.setString(6, question);
					
					statement.executeUpdate();
					statement.close();
					
					ResultSet rs = getQuestionID.executeQuery();
					if(rs.next()){
						questionID = rs.getLong(1);
					}
					attemptsRemaining = 0;
				}
				catch(CommunicationsException e){
					// This occurs if the SQL connection has been left open for a long time
					this.forceCloseConnection(conn);
					this.checkConnections();
				}
				catch(MySQLNonTransientConnectionException e){
					// This occurs if the SQL connection has been left open for a long time
					this.forceCloseConnection(conn);
					this.checkConnections();
				}
				catch(Exception e){
					System.err.println("MySQLQuery (insertQuestion): Could not insert question");
					e.printStackTrace();
					
					questionID = -1;
				}
				
				this.closeConnection(conn);
			}
		
			attemptsRemaining--;
		}while(attemptsRemaining > 0);
		
		return questionID;
	}

	public long insertAnswer(long userID, long toUserID, long itemID, long questionID, Date date, String answer){
		long answerID = -1;
		if(userID<1 ||toUserID<1 ||itemID<1 ||questionID<1) return answerID;
		if(date==null || answer==null) return answerID;
		int attemptsRemaining = SQL_RETRIES;
	
		do{
			Connection conn = this.getConnection(true);
			if(conn != null){
				try{
					// Create the statement to get the questionID
					PreparedStatement getQuestionID = conn.prepareStatement("SELECT LAST_INSERT_ID()");
					
					// Create the SQL statement to insert the question
					PreparedStatement statement = conn.prepareStatement("INSERT INTO `questions` (from_user_id, to_user_id, item_id, is_question, responseTo, date, content) "
											+ "VALUES (?, ?, ?, ?, ?, NOW(), ?)");
					statement.setLong(1, userID);
					statement.setLong(2, toUserID);
					statement.setLong(3, itemID);
					statement.setString(4, "N");
					statement.setLong(5, questionID);
					statement.setString(6, answer);
					
					statement.executeUpdate();
					statement.close();
					
					ResultSet rs = getQuestionID.executeQuery();
					if(rs.next()){
						answerID = rs.getLong(1);
					}
					attemptsRemaining = 0;
				}
				catch(CommunicationsException e){
					// This occurs if the SQL connection has been left open for a long time
					this.forceCloseConnection(conn);
					this.checkConnections();
				}
				catch(MySQLNonTransientConnectionException e){
					// This occurs if the SQL connection has been left open for a long time
					this.forceCloseConnection(conn);
					this.checkConnections();
				}
				catch(Exception e){
					System.err.println("MySQLQuery (insertAnswer): Could not insert answer");
					e.printStackTrace();
					
					answerID = -1;
				}
				
				this.closeConnection(conn);
			}
		
			attemptsRemaining--;
		}while(attemptsRemaining > 0);
		
		return answerID;
	}
	
	public Question getQuestion(long questionID) throws Exception{
		Question question = null;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement statement;
				
				statement = conn.prepareStatement("SELECT * FROM `questions` " +
							"WHERE `questions`.`id` = ?" );
				statement.setLong(1, questionID);
				
				ResultSet rs = statement.executeQuery();

				if(rs.next()){
					boolean isQuestion;
					String iq = rs.getString("questions.is_question");
					if(iq.compareTo("Y") == 0)
						isQuestion = true;
					else
						isQuestion = false;
							
					question = new Question(rs.getLong("questions.id"),
							rs.getLong("questions.from_user_id"),
							rs.getLong("questions.to_user_id"),
							rs.getLong("questions.item_id"),
							isQuestion,
							rs.getLong("questions.responseTo"),
							new Date(rs.getTimestamp("questions.date").getTime()),
							rs.getString("questions.content"));
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getQuestion): Could not get the question");
				e.printStackTrace();
				throw e;
			}
			
			this.closeConnection(conn);
		}
		return question;
	}
	
	public ArrayList<Question> getQuestions(long itemID) throws Exception{
		ArrayList<Question> questions = new ArrayList<Question>();
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement statement;
				
				statement = conn.prepareStatement("SELECT * FROM `questions` " +
							"WHERE `questions`.`item_id` = ? ORDER BY `questions`.`date` DESC " );
				statement.setLong(1, itemID);
				
				ResultSet rs = statement.executeQuery();

				while(rs.next()){
					boolean isQuestion;
					String iq = rs.getString("questions.is_question");
					if(iq.compareTo("Y") == 0)
						isQuestion = true;
					else
						isQuestion = false;
					Question currentQuestion = new Question(rs.getLong("questions.id"),
							rs.getLong("questions.from_user_id"),
							rs.getLong("questions.to_user_id"),
							rs.getLong("questions.item_id"),
							isQuestion,
							rs.getLong("questions.responseTo"),
							new Date(rs.getTimestamp("questions.date").getTime()),
							rs.getString("questions.content"));
					questions.add(currentQuestion);
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getQuesions): Could not get the questions");
				e.printStackTrace();
				throw e;
			}
			
			this.closeConnection(conn);
		}
		
		return questions;
	}
	
	public ArrayList<Question> getQuestions(ArrayList<Long> itemIDs){
		ArrayList<Question> questions = new ArrayList<Question>();	
		int attemptsRemaining = SQL_RETRIES;

		if(itemIDs == null || itemIDs.size()==0) return questions;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				try {
					StringBuilder ids = new StringBuilder();
					
					ArrayList<Long> parsedIDs = new ArrayList<Long>();
					for(int i=0; i<itemIDs.size(); i++){
						try{
							Long temp = itemIDs.get(i);
							if(temp!=null) parsedIDs.add(temp);
						}
						catch(Exception e){
							// not Long
						}
					}
					
					if(parsedIDs.size()>0){
						
							for(int i=0; i<parsedIDs.size()-1; i++){
								ids.append(" ? , ");
							}
							ids.append("?");
						
						
						PreparedStatement statement = conn.prepareStatement("SELECT * FROM `questions` " +
								"WHERE `questions`.`item_id` IN (" + ids.toString() + ") ORDER BY `questions`.`date` DESC " );
						for(int i=0; i<parsedIDs.size(); i++){
							statement.setLong(i+1,itemIDs.get(i));
						}
	
						//System.out.println(statement.toString());
						ResultSet rs = statement.executeQuery();
	
						// Read the user's details
	
						while(rs.next()){
							boolean isQuestion;
							String iq = rs.getString("questions.is_question");
							if(iq.compareTo("Y") == 0)
								isQuestion = true;
							else
								isQuestion = false;
							Question currentQuestion = new Question(rs.getLong("questions.id"),
									rs.getLong("questions.from_user_id"),
									rs.getLong("questions.to_user_id"),
									rs.getLong("questions.item_id"),
									isQuestion,
									rs.getLong("questions.responseTo"),
									new Date(rs.getTimestamp("questions.date").getTime()),
									rs.getString("questions.content"));
							questions.add(currentQuestion);
						}
	
						rs.close();
						statement.close();
					}
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getQuestions): Could not get the questions");
					e.printStackTrace();
				}

				this.closeConnection(conn);
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);

		return questions;
	}
	
	public ArrayList<Comment> getComments(long itemID) throws Exception{
		ArrayList<Comment> comments = new ArrayList<Comment>();
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement statement;
				
				statement = conn.prepareStatement("SELECT * FROM `comments` " +
							"WHERE `comments`.`item_id` = ? ORDER BY `comments`.`date` DESC " );
				statement.setLong(1, itemID);
				
				ResultSet rs = statement.executeQuery();

				while(rs.next()){
					Comment currentComment = new Comment(rs.getLong("comments.id"),
							rs.getLong("comments.from_user_id"),
							rs.getLong("comments.to_user_id"),
							rs.getLong("comments.item_id"),
							rs.getInt("comments.rating"),
							new Date(rs.getTimestamp("comments.date").getTime()),
							rs.getString("comments.comment"));
					comments.add(currentComment);
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLQuery (getComments): Could not get the comments");
				e.printStackTrace();
				throw e;
			}
			
			this.closeConnection(conn);
		}
		
		return comments;
	}
	
	
	public ArrayList<Comment> getComments(ArrayList<Long> itemIDs){
		ArrayList<Comment> comments = new ArrayList<Comment>();
		int attemptsRemaining = SQL_RETRIES;

		if(itemIDs == null || itemIDs.size()==0) return comments;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				try {
					StringBuilder ids = new StringBuilder();
					if(itemIDs.size()>0){
						for(int i=0; i<itemIDs.size()-1; i++){
							ids.append(" ? , ");
						}
						ids.append("?");
					}
					PreparedStatement statement = conn.prepareStatement("SELECT * FROM `comments` " +
							"WHERE `comments`.`item_id` IN (" + ids.toString() + ") ORDER BY `comments`.`date` DESC " );
					for(int i=0; i<itemIDs.size(); i++){
						statement.setLong(i+1,itemIDs.get(i));
					}

					//System.out.println(statement.toString());
					ResultSet rs = statement.executeQuery();

					// Read the user's details

					while(rs.next()){
						boolean isComment;
						String iq = rs.getString("comments.is_comment");
						if(iq.compareTo("Y") == 0)
							isComment = true;
						else
							isComment = false;
						Comment currentComment = new Comment(rs.getLong("comments.id"),
								rs.getLong("comments.from_user_id"),
								rs.getLong("comments.to_user_id"),
								rs.getLong("comments.item_id"),
								rs.getInt("comments.rating"),
								new Date(rs.getTimestamp("comments.date").getTime()),
								rs.getString("comments.content"));
						comments.add(currentComment);
					}

					rs.close();
					statement.close();

					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (getComments): Could not get the comments");
					e.printStackTrace();
				}

				this.closeConnection(conn);
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);

		return comments;
	}
	
	/*
	 * TODO: social network
	 */
	public void confirmFriendRequest(long acceptID) {
		if (acceptID < 0)
			return;
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection(true);
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn
							.prepareStatement("SELECT fromID, toID FROM `requests` WHERE id=?");
					statement.setLong(1, acceptID);
					ResultSet rs = statement.executeQuery();
					if (rs.next()) {
						long user1ID = rs.getLong("fromID");
						long user2ID = rs.getLong("toID");
						rs.close();
						statement = conn
								.prepareStatement("INSERT INTO `friends` (user1ID, user2ID) VALUES (?, ?)");
						statement.setLong(1, user1ID);
						statement.setLong(2, user2ID);
						statement.executeUpdate();
						statement = conn
								.prepareStatement("INSERT INTO `friends` (user1ID, user2ID) VALUES (?, ?)");
						statement.setLong(1, user2ID);
						statement.setLong(2, user1ID);
						statement.executeUpdate();
						statement = conn
								.prepareStatement("DELETE FROM `requests` where id=?;");
						statement.setLong(1, acceptID);
						statement.executeUpdate();
						attemptsRemaining = 0;
					}
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err
							.println("MySQLQuery (confirmFriendRequest): Could not confirm friend request");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
	}

	public void rejectFriendRequest(long rejectID) {
		if (rejectID < 0)
			return;
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection(true);
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn
							.prepareStatement("DELETE FROM `requests` where id=?;");
					statement.setLong(1, rejectID);
					statement.executeUpdate();
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err
							.println("MySQLQuery (rejectFriendRequest): Could not reject friend request");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
	}

	@Override
	public long getUserID(String username) {
		int id = -1;
		if (username == null)
			return id;
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection();
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn
							.prepareStatement("SELECT id FROM `users` WHERE username=?");
					statement.setString(1, username);
					ResultSet rs = statement.executeQuery();
					if (rs.next()) {
						id = rs.getInt("id");
						rs.close();
						attemptsRemaining = 0;
					}
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err
							.println("MySQLQuery (getUserID): Could not get user id");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		return id;
	}

	public ArrayList<FriendRequest> getFriendRequests(long toID) {
		if (toID < 1)
			return new ArrayList<FriendRequest>();
		ArrayList<FriendRequest> result = new ArrayList<FriendRequest>();
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection();
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn
							.prepareStatement("SELECT * FROM `requests` WHERE `toID` = ? LIMIT 30");
					statement.setLong(1, toID);
					ResultSet rs = statement.executeQuery();
					while (rs.next())
						result.add(new FriendRequest(rs.getLong("id"), rs
								.getLong("fromID"), rs.getLong("toID"), rs
								.getString("message")));
					rs.close();
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err
							.println("MySQLQuery (getFriendRequests): Could not get the friend requests");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		return result;
	}

	public boolean insertWallPost(long fromID, long toID, String text) {
		Boolean insertedWallPost = false;
		if (fromID < 1 || text == null)
			return insertedWallPost;
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection(true);
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn
							.prepareStatement("INSERT INTO `wallposts` (fromID, toID, replyID, text) VALUES (?, ?, ?, ?)");
					statement.setLong(1, fromID);
					statement.setLong(2, toID);
					statement.setLong(3, -1);
					statement.setString(4, text);
					int result = statement.executeUpdate();
					if (result == 1)
						insertedWallPost = true;
					attemptsRemaining = 0;					
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err
							.println("MySQLQuery (insertWallPost): Could not insert wall post");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		return insertedWallPost;
	}

	public boolean insertReply(long fromID, long replyID, String text) {
		boolean insertedReply = false;
		if (fromID < 1 || replyID < 1 || text == null)
			return insertedReply;
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection(true);
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn
							.prepareStatement("INSERT INTO `wallposts` (fromID, toID, replyID, text) VALUES (?, ?, ?, ?)");
					statement.setLong(1, fromID);
					statement.setLong(2, -1);
					statement.setLong(3, replyID);
					statement.setString(4, text);
					int result = statement.executeUpdate();
					if (result == 1)
						insertedReply = true;
					attemptsRemaining = 0;					
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err
							.println("MySQLQuery (insertReply): Could not insert reply");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		return insertedReply;
	}

	public boolean hasRequestID(long id) {
		boolean result = false;
		if (id < 1)
			return result;
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection();
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn.prepareStatement("SELECT * FROM `requests` WHERE `id`= ?");
					statement.setLong(1, id);
					ResultSet rs = statement.executeQuery();
					if (rs.next())
						result = true;
					rs.close();
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (hasRequestID): Could not check id in requests");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		return result;
	}

	public boolean hasWallpostsToID(long toID) {
		boolean result = false;
		if (toID < 1)
			return result;
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection();
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn.prepareStatement("SELECT * FROM `wallposts` WHERE `toID`= ?");
					statement.setLong(1, toID);
					ResultSet rs = statement.executeQuery();
					if (rs.next())
						result = true;
					rs.close();
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (hasWallpostsToID): Could not check toID in wallposts");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		return result;
	}

	public boolean hasWallpostsReplyID(long replyID) {
		boolean result = false;
		if (replyID < 1)
			return result;
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection();
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn.prepareStatement("SELECT * FROM `wallposts` WHERE `replyID`= ?");
					statement.setLong(1, replyID);
					ResultSet rs = statement.executeQuery();
					if (rs.next())
						result = true;
					rs.close();
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (hasWallpostsReplyID): Could not check repyId in wallposts");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		return result;
	}

	public boolean areFriends(long user1ID, long user2ID) {
		boolean result = false;
		if (user1ID < 1 || user2ID < 1)
			return result;
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection();
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn.prepareStatement("SELECT * FROM `friends` WHERE `user1ID`= ? AND `user2ID`= ?");
					statement.setLong(1, user1ID);
					statement.setLong(1, user2ID);
					ResultSet rs = statement.executeQuery();
					if (rs.next())
						result = true;
					rs.close();
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("MySQLQuery (areFriends): Could not check if the two users are friends");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		return result;
	}
	public ArrayList<WallPost> getWallPosts(long toID) {
		ArrayList<WallPost> result = new ArrayList<WallPost>();
		if (toID < 1)
			return result;
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection();
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn
							.prepareStatement("SELECT * FROM `wallposts` WHERE toID=? AND `replyID`=-1 ORDER BY ts DESC LIMIT 30");
					statement.setLong(1, toID);
					ResultSet rs = statement.executeQuery();
					while (rs.next())
						result.add(new WallPost(rs.getLong("id"), rs
								.getLong("fromID"), rs.getLong("toID"), rs
								.getLong("replyID"), rs.getString("text"), rs
								.getDate("ts")));
					rs.close();
					attemptsRemaining = 0;					
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err
							.println("MySQLQuery (getWallPosts): Could not get the wall posts");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		return result;
	}
	public boolean insertMessage(long fromID, long toID, String text) {
		if (fromID < 1 || toID < 1 || text == null)
			return false;
		Boolean insertedMessage = false;
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection(true);
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn
							.prepareStatement("INSERT INTO `messages` (fromID, toID, text) VALUES (?, ?, ?)");
					statement.setLong(1, fromID);
					statement.setLong(2, toID);
					statement.setString(3, text);

					int result = statement.executeUpdate();
					if (result == 1)
						insertedMessage = true;
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err
							.println("MySQLQuery (insertMessage): Could not insert message");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		return insertedMessage;
	}
	public boolean insertFriendRequest(long fromID, String toName,
			String message) {
		if (fromID < 1 || toName == null || message == null)
			return false;
		Boolean insertedRequest = false;
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection(true);
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn
							.prepareStatement("INSERT INTO `requests` (fromID, toID, message) VALUES (?, ?, ?)");
					statement.setLong(1, fromID);
					long userId = this.getUserID(toName);
					statement.setLong(2, userId);
					statement.setString(3, message);
					int result = statement.executeUpdate();
					if (result == 1)
						insertedRequest = true;
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err
							.println("MySQLQuery (insertFriendRequest): Could not insert friend request");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		return insertedRequest;
	}
	public ArrayList<Message> getMessages(long toID) {
		if (toID < 1)
			return new ArrayList<Message>();
		ArrayList<Message> result = new ArrayList<Message>();
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection();
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					// Create the SQL statement to get the messages
					statement = conn
							.prepareStatement("SELECT * FROM `messages` WHERE `toID` = ? ORDER BY ts LIMIT 30");
					statement.setLong(1, toID);
					ResultSet rs = statement.executeQuery();
					result = new ArrayList<Message>();
					while (rs.next())
						result.add(new Message(rs.getLong("id"), rs
								.getLong("fromID"), rs.getLong("toID"), rs
								.getString("text"), rs.getDate("ts"), rs
								.getInt("read")));
					rs.close();
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err
							.println("MySQLQuery (getMessages): Could not get the messages");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		return result;
	}
	public ArrayList<WallPost> getNewsFeeds(long userID) {
		ArrayList<WallPost> result = new ArrayList<WallPost>();
		if (userID < 1)
			return result;
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection();
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn
							.prepareStatement("SELECT * FROM `wallposts` WHERE `fromID` IN (SELECT user2ID FROM `friends` WHERE user1ID=?) AND `replyID`=-1 ORDER BY ts DESC LIMIT 30");
					statement.setLong(1, userID);
					ResultSet rs = statement.executeQuery();
					result = new ArrayList<WallPost>();
					while (rs.next())
						result.add(new WallPost(rs.getLong("id"), rs
								.getLong("fromID"), rs.getLong("toID"), rs
								.getLong("replyID"), rs.getString("text"), rs
								.getDate("ts")));
					rs.close();
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err
							.println("MySQLQuery (getMyWallPosts): Could not get my wall posts");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		return result;
	}
	public ArrayList<WallPost> getReplies(long replyID) {
		ArrayList<WallPost> result = new ArrayList<WallPost>();
		if (replyID < 1)
			return result;
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection();
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn
							.prepareStatement("SELECT * FROM `wallposts` WHERE `replyID`=? ORDER BY ts LIMIT 30");
					statement.setLong(1, replyID);
					ResultSet rs = statement.executeQuery();
					while (rs.next())
						result.add(new WallPost(rs.getLong("id"), rs
								.getLong("fromID"), rs.getLong("toID"), rs
								.getLong("replyID"), rs.getString("text"), rs
								.getDate("ts")));
					rs.close();
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err
							.println("MySQLQuery (getReplies): Could not get the replies");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		return result;
	}
	public String getFromName(long fromID) {
		int id = (int) fromID;
		if (id < 1)
			return null;
		String result = null;
		int attemptsRemaining = SQL_RETRIES;
		do {
			Connection conn = this.getConnection();
			if (conn != null) {
				PreparedStatement statement = null;
				try {
					statement = conn
							.prepareStatement("SELECT username FROM `users` WHERE `id` = ?");
					statement.setInt(1, id);
					ResultSet rs = statement.executeQuery();
					result = new String();
					if (rs.next())
						result = rs.getString("username");
					rs.close();
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err
							.println("MySQLQuery (getFromName): Could not get the username");
					e.printStackTrace();
				} finally {
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		return result;
	}
	
	/*
	 * TODO video
	 */
	//Jing :)
	public ArrayList<VideoItem> GetAllVideos() {
		
		/*ArrayList<VideoItem> allVideoItem = new ArrayList<VideoItem>();
		int attemptsRemaining = 5;
		do{
			Connection conn = this.getConnection();
			
			if(conn != null){
				try{
					PreparedStatement statement = conn.prepareStatement("SELECT * FROM jingvideo");
					ResultSet rs = statement.executeQuery();
					
					//put all the states into the results vector
					while(rs.next()){
						VideoItem oneVideo = new VideoItem(rs.getInt("id"),rs.getString("url"),rs.getString("name"),rs.getString("description"),rs.getInt("userid"));
						allVideoItem.add(oneVideo);
					}
					
					rs.close();
					statement.close();
					attemptsRemaining=0;
									
				}
				catch(CommunicationsException e){
					// this occurs if the SQL connection has been left open for a long time
					this.forceCloseConnection(conn);
					this.checkConnections();
				}
				catch(MySQLNonTransientConnectionException e){
					// this occurs if the SQL connection has been left open for a long tiem
					this.forceCloseConnection(conn);
					this.checkConnections();
				}
				catch(Exception e){
					System.err.println("MySQLQuery.getAllVideo():exception "+e);
				}
				this.closeConnection(conn);
			}
			attemptsRemaining--;
		}while(attemptsRemaining>0);
		
		
		/*
		int maxID = -1;
		int attemptsRemaining2 = 5;
		do{
			Connection conn = this.getConnection();
			
			if(conn != null){
				try{
					
					PreparedStatement getmaxID = conn.prepareStatement("SELECT id from videos");
					ResultSet getmaxIDrs = getmaxID.executeQuery();
					while(getmaxIDrs.next()){
						int id = getmaxIDrs.getInt("id");
						if (id > maxID)
							maxID = id;
					
					}
					System.err.println("haha we get the max id" +maxID);
					getmaxIDrs.close();
					getmaxID.close();
					
					attemptsRemaining2 = 0;
					
					
				}
				catch(CommunicationsException e){
					// this occurs if the SQL connection has been left open for a long time
					this.forceCloseConnection(conn);
					this.ckeckConnections();
				}
				catch(MySQLNonTransientConnectionException e){
					// this occurs if the SQL connection has been left open for a long tiem
					this.forceCloseConnection(conn);
					this.ckeckConnections();
				}
				catch(Exception e){
					System.err.println("MySQLQuery(getmaxID): Could not get the maxID");
				}
				this.closeConnection(conn);
			}
			attemptsRemaining2--;
		}while(attemptsRemaining2>0);
		
		*/
		return new ArrayList<VideoItem>();	
	}

	
	public int insertVideo(String name, String description, int userID) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public int getMaxVideoID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Boolean insertVideo(String URL) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getVideos() {
		// TODO Auto-generated method stub
		return null;
	}
	

	
	/*public int getMaxVideoID(){
		int maxID = -1;
		int attemptsRemaining2 = 5;
		do{
			Connection conn = this.getConnection();
			
			if(conn != null){
				try{
					
					PreparedStatement getmaxID = conn.prepareStatement("SELECT id from videos");
					ResultSet getmaxIDrs = getmaxID.executeQuery();
					while(getmaxIDrs.next()){
						int id = getmaxIDrs.getInt("id");
						if (id > maxID)
							maxID = id;
					
					}
					//System.err.println("haha we get the max id" +maxID);
					getmaxIDrs.close();
					getmaxID.close();
					
					attemptsRemaining2 = 0;
					
					
				}
				catch(CommunicationsException e){
					// this occurs if the SQL connection has been left open for a long time
					this.forceCloseConnection(conn);
					this.checkConnections();
				}
				catch(MySQLNonTransientConnectionException e){
					// this occurs if the SQL connection has been left open for a long tiem
					this.forceCloseConnection(conn);
					this.checkConnections();
				}
				catch(Exception e){
					System.err.println("MySQLQuery(getmaxID): Could not get the maxID");
				}
				this.closeConnection(conn);
			}
			attemptsRemaining2--;
		}while(attemptsRemaining2>0);
		
		return maxID;
	}*/
	/*@Override
	public synchronized int insertVideo( String name, String description,int userID) {
		
		int attemptsRemaining = 5;
		int videoID = -1;
		
		do{
			Connection conn = this.getConnection();
			if(conn != null) {
				try{
					

					System.out.println("in MySQLDBQuert.insertVideo(), name= "+name);
					System.out.println("in MySQLDBQuert.insertVideo(), description= "+description);
					System.out.println("in MySQLDBQuert.insertVideo(), userID= "+userID);
					
					
					// create the SQL statement to insert the video URL
					String sql1 = "INSERT INTO `jingvideo` (URL,name,description,userid) VALUES (?,?,?,?)";
					PreparedStatement video = conn.prepareStatement(sql1,Statement.RETURN_GENERATED_KEYS);
					video.setString(1, "lqbz");
					video.setString(2,name);
					video.setString(3,description);
					video.setInt(4, userID);
					video.executeUpdate();
					ResultSet rs = video.getGeneratedKeys();
					rs.next();
					videoID = rs.getInt(1);
					
					System.out.println("in MySQLDBQuert.insertVideo(), after insert row, videoID= "+videoID);
					
					//UPDATE `jingvideo` SET url='video_2.flv' WHERE id=2
					
					if(videoID>0){ 
						String localVideoName = "video_"+(videoID)+".flv";
						String sql2 = "UPDATE jingvideo SET url='"+localVideoName+"' WHERE id="+videoID;
						System.out.println("in MySQLDBQuert.insertVideo(), sql2= "+sql2);
						PreparedStatement updateUrl = conn.prepareStatement(sql2);
						updateUrl.executeUpdate();
					}
					System.out.println("in in MySQLDBQuert.insertVideo(),after update url, videoID= "+videoID);

					
					//ResultSet rs = video.executeQuery();
					//if(rs.next()){
					//	videoID = rs.getInt(1);
					//}
					
					attemptsRemaining = 0;
					video.close();
				}
				catch(CommunicationsException e){
					this.forceCloseConnection(conn);
					this.checkConnections();
					return -1;
				}
				catch(MySQLNonTransientConnectionException e){
					this.forceCloseConnection(conn);
					this.checkConnections();
					return -1;
				}
				catch(Exception e){
					System.err.println("MySQLQuery.insertVideo() :exception+ "+e);
					e.printStackTrace();
					return -1;
				}
				this.closeConnection(conn);
			}
			attemptsRemaining--;
		}while(attemptsRemaining > 0);
		
		return videoID;
	}*/
}
