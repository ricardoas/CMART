package com.cmart.DB;

import java.net.URLEncoder;
import java.sql.*;
import java.util.*;
import java.text.*;
import java.util.Date;

import org.apache.cassandra.utils.FBUtilities;
//import org.apache.cassandra.cql.*;
import org.apache.cassandra.cql.jdbc.*;

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
public class CassandraDBQuery extends DBQuery{
	protected int maxConnections = 200;
	private static CassandraDBQuery single = null;	
	private boolean strict = false;
	private int maxSQLConnectionsCache = 200;
	private int CONNECTION_POOL=1;
	private boolean debug=false;
	private static String consistency = " ANY";
	DecimalFormat df = new DecimalFormat("#.##");
	private static long shortback = 1000000l;
	private static long tenzero = 10000000000l;
	
	
	public CassandraDBQuery(String forT2testingOnly){
		CassandraDBQuery.getInstance();
	}
	
	private CassandraDBQuery(){
		super();
		init();
	}
	
	public static CassandraDBQuery getInstance(){
		if (single == null) {
			synchronized (CassandraDBQuery.class) {
				if (single == null) {
					single = new CassandraDBQuery();
				}
			}
		}

		return single;
	}
	
	static String urlFront = null;
	static String urlBack = null;
	static int ipCount;
	static int currentIP;
	public synchronized static String getURL(){
		// Just return the URL if there are no additional IPs
		if(GlobalVars.CASSANDRA_IPS==null || GlobalVars.CASSANDRA_IPS.size()==0) return GlobalVars.CASSANDRA_DATABASE_URL;
		
		// Parse the URL so we can insert the IP
		if(urlFront==null){
			urlFront = GlobalVars.CASSANDRA_DATABASE_URL.split("/")[0] + "//";
			urlBack = ":"+GlobalVars.CASSANDRA_DATABASE_URL.split("/")[2].split(":")[1]+ "/" + GlobalVars.CASSANDRA_DATABASE_URL.split("/")[3];
			ipCount = GlobalVars.CASSANDRA_IPS.size();
			currentIP=0;
		}
		
		if(currentIP >= ipCount) currentIP=0;
		String ret = urlFront + GlobalVars.CASSANDRA_IPS.get(currentIP)+ urlBack;
		currentIP++;

		return ret;
	}
	
	protected void init(){
		//URL = GlobalVars.CASSANDRA_DATABASE_URL;
		DRIVER = GlobalVars.CASSANDRA_DATABASE_DRIVER;
	}
	
	protected synchronized Connection getConnection(){
		if(debug) System.out.println("(cass getConnection) URL to open: " + " uing switch " + CONNECTION_POOL);
		switch (CONNECTION_POOL) {
		case 0:
			if(debug) System.out.println("(cass getConnection) returning init()");
			return initConnection();
		case 1:
			if (!connections.isEmpty()) {
				Connection conn = null;

				synchronized (connections) {
					if (!connections.isEmpty()) {
						conn = connections.pop();
						if(debug) System.out.println("(cass getConnection) returning pop " + conn);			
					}
				}

				if (conn != null)
					return conn;
			}

			return initConnection();
		case 2:
			return initConnection();
		default:
			return initConnection();
		}
	}
	
	protected Connection initConnection(){
		String url = getURL();
		try {	
			Class.forName(DRIVER);

			Connection conn = DriverManager.getConnection(url);
			if(debug) System.out.println("(cass initConnection) doing init to return with url " +url);
			
			if(debug) System.out.println("(cass initConnection) returning " + conn);
			return conn;	
		}
		catch(Exception e){
			System.err.println("CassandraQuery (initConnection): Could not open a new database connection to " + url);
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * As some connections may timeout if left for a long period, we'll go through them all and make sure they all work
	 * if we detect any that have timed out.
	 * 
	 * We may miss some since we are not locking the connections object, but we don't need to be 100%, and we don't want
	 * to freeze out everyone else while we're doing the check. at least we may get some. It is unlikely that all connections
	 * have a fault. We may also be checking some twice since other things can be pushing and popping, again, it's not perfect.
	 */
	protected void checkConnections(){	
		for(int i=0; i<connections.size(); i++){
			Connection conn = connections.pop();
			try {
				conn.isValid(1000);
				
				// The connection worked, so readd it
				connections.offerLast(conn);
			} catch (SQLException e) {
				// The connection is closed or bad, so we don't want it
				conn = null;
			}
		}
	}
	
	/**
	 * Forcefully close a connection, not repool it
	 * @param conn
	 */
	protected void forceCloseConnection(Connection conn){
		if(conn != null)
		try{
			conn.close();
			conn=null;
		}
		catch(Exception e){
			System.err.println("CassandraQuery (closeConnection): Could not close database connection");
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
					e.printStackTrace();
				}	
			}
			else{
				// re-pool the connection
				if(connections.size() < maxSQLConnectionsCache)
					synchronized(connections){
						if(connections.size() < maxSQLConnectionsCache){
							connections.offerLast(conn);
							if(debug )System.out.println("CassandraDB (closeConnection): repooled connection");
						}
					}
				else{
					try{
						conn.close();
					}
					catch(Exception e){
						System.out.println("CassandraDB (closeConnection): Could not close database connection");
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Close a prepared statement
	 * @param statement
	 */
	private void closeSmt(PreparedStatement statement){		
		if(statement != null){
			try {
				if(statement != null) statement.close();
			} catch (SQLException e) {
				
			}
		}
	}
	
	private void close(ResultSet rs){		
		if(rs != null){
			try {
				if(rs != null) rs.close();
			} catch (SQLException e) {

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
				PreparedStatement  statement = null;
				
				try {
					// We have to make the statement as Cassandra cannot current .setStrings correctly
					statement = conn.prepareStatement("SELECT KEY FROM users WHERE username_password='"+safe(username+"_"+password)+"'");
					
					ResultSet rs = statement.executeQuery();
					
					if(rs.next()){
						try{
							Long temp = rs.getLong("KEY");	
							if(temp!=null) userID = temp;
						} catch(Exception e){}
						
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
					System.err.println("CassandraDBQuery (checkUsernamePassword): Could not check username/password");
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
	 * @see com.cmart.DB.DBQuery#makeNewAuthToken(long)
	 */
	public String makeNewAuthToken(long userID){
		if(userID < 1) return null;
		
		String authToken = null;
		
		// Get the new auth token
		String newAuthToken = safe(authToken(userID));
		
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("UPDATE users USING CONSISTENCY "+consistency+" SET authtoken='"+newAuthToken+"' WHERE KEY="+userID);
					
					statement.executeUpdate();
						
					authToken = newAuthToken;
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err.println("CassandraDBQuery (makeNewAuthToken): Could not update the auth token");
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
	 * @see com.cmart.DB.DBQuery#checkAuthToken(long, java.lang.String)
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
					statement = conn.prepareStatement("SELECT authtoken FROM users WHERE userid='"+userID+"' AND authtoken='"+safe(authToken)+"'");
					
					ResultSet rs = statement.executeQuery();
					
					// If there is a result, then we got an auth token back. Except, currently CQL returns a vaild
					// result set even if it's not. So we have to catch the exception that the result isn't real.
					// Also it seems to ignore the "AND", so it may return the authtoken even if it does not match
					if(rs.next()){
						String resultAuth = null;
						
						try{
							resultAuth = rs.getString("authtoken");
						}
						catch(Exception e){}
						
						if(resultAuth != null && resultAuth.equals(safe(authToken))) correct = true;
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
					System.err.println("CassandraQuery (checkAuthToken): Could not read the auth token");
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
	 * @see com.cmart.DB.DBQuery#logout(long)
	 */
	public Boolean logout(long userID){
		if(userID < 1) return false;
		
		Boolean loggedOut = Boolean.FALSE;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to update the auth token
					statement = conn.prepareStatement("UPDATE users SET authtoken = NULL WHERE KEY ="+userID);
						
					statement.executeUpdate();
					
					// We we did update the row, then we to return true, we did update the token
					loggedOut = Boolean.TRUE;
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err.println("CassandraQuery (logout): Could not remove the auth token");
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
	 * @see com.cmart.DB.DBQuery#insertAddress(long, java.lang.String, java.lang.String, java.lang.String, int, java.lang.Boolean)
	 */
	public Boolean insertAddress(long userID, String street, String town, String zip, int state, Boolean isDefault){
		if(userID < 1 || state < 1) return false;
		if(street == null || town == null || zip == null || isDefault == null) return false;
		
		zip = zip.substring(0, Math.min(zip.length(), 9));
		
		Boolean insertedAddress = false;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// MAKE KEY - 
					long key = (System.currentTimeMillis()*1000000) + System.nanoTime()%1000000;
					
					// If the address is default, add the userID key as the default address key
					// We can then easily select the default address
					if(isDefault)
					statement = conn.prepareStatement("INSERT INTO addresses (KEY, userid, street, town, zip, state, isdefault, isDefaultKey) "
											+ "VALUES ("+key+","+userID+",'"+safe(street)+"','"+safe(town)+"','"+zip+"','"+state+"',"+isDefault+","+userID+") USING CONSISTENCY "+consistency);
	
					// Otherwise, insert the address without the default address key
					else statement = conn.prepareStatement("INSERT INTO addresses (KEY, userid, street, town, zip, state, isdefault) "
							+ "VALUES ("+key+","+userID+",'"+safe(street)+"','"+safe(town)+"','"+zip+"','"+state+"',"+isDefault+") USING CONSISTENCY "+consistency);				
					
					statement.executeUpdate();
					
					insertedAddress = true;
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err.println("CassandraQuery (insertAddress): Could not insert address");
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
	 * @see com.cmart.DB.DBQuery#updateAddress(long, long, java.lang.String, java.lang.String, java.lang.String, int, java.lang.Boolean)
	 */
	public Boolean updateAddress(long id, long userID, String street, String town, String zip, int state, Boolean isDefault){
		if(userID < 1 || id < 1 || state < 1) return false;
		if(street == null || town == null || zip == null || isDefault == null) return false;
		
		int attemptsRemaining = SQL_RETRIES;
		boolean updatedAddress = false;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					if(isDefault){
						// If the address is the new default, make the old default not, and set this one to be
						Address df = this.getDefaultAddress(userID);
						
						if(df!=null && df.getId()!=id){
							statement = conn.prepareStatement("UPDATE addresses SET isDefault = '"+!isDefault+"', isDefaultKey=NULL WHERE KEY = " + df.getId());
							statement.execute();
							statement.close();
						}
						
						// Create the CQL statement to update the address
						statement = conn.prepareStatement("UPDATE addresses SET street = '"+street+"', town = '"+town+"', zip = '"+zip+"', state = '"+state+"', isDefault = '"+isDefault+"', isDefaultKey='"+userID+"' WHERE KEY = " + id);
					}
					else{
						// Create the CQL statement to update the address
						statement = conn.prepareStatement("UPDATE addresses SET street = '"+street+"', town = '"+town+"', zip = '"+zip+"', state = '"+state+"', isDefault = '"+isDefault+"' WHERE KEY = " + id);
					}

					statement.executeUpdate();
					
					updatedAddress = true;
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err.println("CassandraQuery (updateAddress): Could not update address");
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
	 * @see com.cmart.DB.DBQuery#insertItemDB(long, java.lang.String, java.lang.String, double, double, double, int, java.util.Date, long)
	 */
	public long insertItemDB(long userID, String name, String description, double startPrice, double reservePrice, double buyNowPrice, int quantity, Date endDate, long categoryID){
		if(name == null || description== null || endDate == null) return -1;
		if(userID < 1 || categoryID < 1 || quantity < 1) return -1;
		if(startPrice <0.0 || reservePrice<0.0 || buyNowPrice<0.0) return -1;
		
		long itemID = -1;
		int attemptsRemaining = SQL_RETRIES;
		
		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				PreparedStatement revstatement = null;
				PreparedStatement priceItemStatement = null;
				
				try {
					// MAKE KEY - use time stamp + random so we can sort by time
					long key = (endDate.getTime()*shortback) + (System.nanoTime()%shortback);
					itemID=key;
					
					// Insert the item
					statement = conn.prepareStatement("INSERT INTO items (KEY, itemid,name, description, sellerid," +
							"categoryid, quantity, startdate, enddate,startprice,reserveprice,buynowprice," +
							"noofbids,thumbnail,currentwinner,curbid,maxbid,ts) "
							+ "VALUES ("+key+","+itemID+",'"+safe(name)+"','"+safe(description)+"',"+userID+","+categoryID+","
							+quantity+","+System.currentTimeMillis()+","+endDate.getTime()+","+startPrice+","+
							reservePrice+","+buyNowPrice+",0,'blank.jpg',0,0,0,"+System.currentTimeMillis()+") USING CONSISTENCY "+consistency);
					
					statement.executeUpdate();
					
					// Insert the item for reverse time lookups
					revstatement = conn.prepareStatement("INSERT INTO revtimeitems (KEY,catzero,enddate,itemid,categoryid) VALUES ("+(Long.MAX_VALUE-key)+",0,"+endDate.getTime()+","+itemID+","+categoryID+");");
					revstatement.executeUpdate();
					
					// Insert the item for price lookups
					Long priceKey = ((long) (0*tenzero)) + (itemID % tenzero);
					
					priceItemStatement = conn.prepareStatement("UPDATE priceitems USING CONSISTENCY "+consistency+" SET curbid = 0.00, categoryid="+categoryID+", itemid="+itemID+", pikey="+priceKey+" WHERE KEY="+priceKey);
					priceItemStatement.executeUpdate();
					priceItemStatement = conn.prepareStatement("UPDATE revpriceitems USING CONSISTENCY "+consistency+" SET curbid = 0.00, categoryid="+categoryID+", itemid="+itemID+", pikey="+priceKey+" WHERE KEY="+(Long.MAX_VALUE-priceKey));
					priceItemStatement.executeUpdate();
					priceItemStatement = conn.prepareStatement("UPDATE items USING CONSISTENCY "+consistency+" SET pikey="+priceKey+" WHERE KEY="+itemID );
					priceItemStatement.executeUpdate();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.err.println("CassandraQuery (insertItem): Could not insert item");
					e.printStackTrace();
				} finally{
					this.closeSmt(statement);
					this.closeSmt(revstatement);
					this.closeSmt(priceItemStatement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		
		return itemID;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getItem(long, java.lang.Boolean)
	 */
	public Item getItem(long itemID, Boolean getImages){
		if(itemID < 1) return null;
		
		Item result = null;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to see get the item's details
					statement = conn.prepareStatement("SELECT * FROM items WHERE KEY="+itemID);		
					
					ResultSet rs = statement.executeQuery();
					
					// If an item is returned then get the details
					if(rs.next()){
						// Make sure the value is real. The problem here is that Cassandra will return the
						// key if it ever existed
						long seller = 0;
						try{
							Long temp = rs.getLong("sellerid");
							if(temp!=null) seller = temp;
						}
						catch(Exception e){}
						
						if(seller != 0){

							// If we need to get the images, do that now
							ArrayList<Image> images = new ArrayList<Image>();
							if (getImages == true)
								images = this.getItemImages(rs.getLong("KEY"));

							result = new Item(
									rs.getLong("KEY"),
									rs.getString("name"),
									rs.getString("description"),
									rs.getInt("quantity"),
									rs.getDouble("startprice"),
									rs.getDouble("reserveprice"),
									rs.getDouble("buynowprice"),
									rs.getDouble("curbid"),
									rs.getDouble("maxbid"),
									rs.getInt("noofbids"),
									new Date(rs.getLong("startdate")),
									new Date(rs.getLong("enddate")),
									rs.getLong("sellerid"),
									rs.getLong("categoryid"),
									rs.getString("thumbnail"),
									images);
						}
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
					System.err.println("CassandraQuery (getItem): Could not get the item " +itemID);
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
	 * @see com.cmart.DB.DBQuery#getOldItem(long, java.lang.Boolean)
	 */
	public Item getOldItem(long itemID, Boolean getImages){
		if(itemID < 1) return null;
		
		Item result = null;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to see get the item's details
					statement = conn.prepareStatement("SELECT * FROM olditems WHERE KEY="+itemID);		
					
					ResultSet rs = statement.executeQuery();
					
					// If an item is returned then get the details
					if(rs.next()){
						long seller = 0;
						try{
							Long temp = rs.getLong("sellerid");
							if(temp!=null) seller = temp;
						}
						catch(Exception e){}
						
						if(seller != 0){
						
						// If we need to get the images, do that now
						ArrayList<Image> images = new ArrayList<Image>();
						
						if(getImages==true)
							images = this.getItemImages(rs.getLong("KEY"));

						result = new Item(rs.getLong("KEY"),
								rs.getString("name"),
								rs.getString("description"),
								rs.getInt("quantity"),
								rs.getDouble("startprice"),
								rs.getDouble("reserveprice"),
								rs.getDouble("buynowprice"),
								rs.getDouble("curbid"),
								rs.getDouble("maxbid"),
								rs.getInt("noofbids"),
								new Date(rs.getLong("startdate")),
								new Date(rs.getLong("enddate")),
								rs.getLong("sellerid"),
								rs.getLong("categoryid"),
								rs.getString("thumbnail"),
								images);
					}
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
					System.err.println("CassandraQuery (getItem): Could not get the item");
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
	 * @see com.cmart.DB.DBQuery#getCurrentSellingItems(long, long)
	 */
	public ArrayList<Item> getCurrentSellingItems(long userID, long ts){
		ArrayList<Item> items = new ArrayList<Item>();
		if(userID < 1) return items;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to get items the user is selling
					statement = conn.prepareStatement("SELECT * FROM items WHERE sellerid="+userID + " AND ts>="+ts);
					
					ResultSet rs = statement.executeQuery();
					
					while(rs.next()){
						// Make sure they are real
						long seller = 0;
						try{
							Long temp = rs.getLong("sellerid");
							if(temp!=null) seller = temp;
						}
						catch(Exception e){}
						
						if(seller!=0){
							ArrayList<Image> images = this.getItemImages(rs.getLong("itemid"));
							Item currentItem = new Item(rs.getLong("itemid"),
									rs.getString("name"),
									rs.getString("description"),
									rs.getInt("quantity"),
									rs.getDouble("startprice"),
									rs.getDouble("reserveprice"),
									rs.getDouble("buynowprice"),
									rs.getDouble("curbid"),
									rs.getDouble("maxbid"),
									rs.getInt("noofbids"),
									new Date(rs.getLong("startdate")),
									new Date(rs.getLong("enddate")),
									rs.getLong("sellerid"),
									rs.getLong("categoryid"),
									rs.getString("thumbnail"),
									images);
							
							items.add(currentItem);
						}
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
					System.out.println("CassandraQuery (getCurrentSellingItems): Could not get the items");
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
	 * @see com.cmart.DB.DBQuery#getOldSellingItems(long, long)
	 */
	public ArrayList<Item> getOldSellingItems(long userID, long ts){
		ArrayList<Item> items = new ArrayList<Item>();
		if(userID < 1) return items;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the SQL statement to get items the user is selling
					statement = conn.prepareStatement("SELECT * FROM olditems WHERE sellerid="+userID + " AND ts>="+ts);
					
					ResultSet rs = statement.executeQuery();
					
					while(rs.next()){
						// Make sure they are real
						long seller = 0;
						try{
							Long temp = rs.getLong("sellerid");
							if(temp!=null) seller = temp;
						}
						catch(Exception e){}
						
						if(seller!=0){
							Item currentItem = new Item(rs.getLong("itemid"),
									rs.getString("name"),
									rs.getString("description"),
									rs.getInt("quantity"),
									rs.getDouble("startprice"),
									rs.getDouble("reserveprice"),
									rs.getDouble("buynowprice"),
									rs.getDouble("curbid"),
									rs.getDouble("maxbid"),
									rs.getInt("noofbids"),
									new Date(rs.getLong("startdate")),
									new Date(rs.getLong("enddate")),
									rs.getLong("sellerid"),
									rs.getLong("categoryid"),
									rs.getString("thumbnail"),
									new ArrayList<Image>());
							
							items.add(currentItem);
						}
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
					System.out.println("CassandraQuery (getOldSellingItems): Could not get the items");
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
	 * @see com.cmart.DB.DBQuery#insertThumbnail(long, java.lang.String)
	 */
	public Boolean  insertThumbnail(long itemID, String URL){
		return insertImage(itemID, 0, URL, "");
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#insertImage(long, int, java.lang.String, java.lang.String)
	 */
	public Boolean insertImage(long itemID, int position, String URL, String description){
		if(itemID < 1 || position<0 || description==null || URL == null) return Boolean.FALSE;
		
		int attemptsRemaining = SQL_RETRIES;
		Boolean insertedImage = Boolean.FALSE;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement insertImage = null;
				
				
				try {
					// insert the image
					// MAKE KEY
					long key = System.currentTimeMillis()*shortback + System.nanoTime()%shortback;
					insertImage = conn.prepareStatement("INSERT INTO images (KEY, URL, description, itemid, position) VALUES ("+key+",'"+safe(URL)+"','"+safe(description)+"',"+itemID+","+position+") USING CONSISTENCY "+consistency);
					
					// insert the image
					insertImage.executeUpdate();
					
					if(position==0){
						// if the position is 0 then we want to update the thumbnail field in the item
						PreparedStatement updateThumbnail = null;
						updateThumbnail = conn.prepareStatement("UPDATE items USING CONSISTENCY "+consistency+" SET thumbnail ='"+safe(URL)+"' WHERE KEY="+itemID);
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
					System.out.println("CassandraDBQuery (insertImage): Could not insert image");
					e.printStackTrace();
				} finally{
					this.closeSmt(insertImage);
					
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return insertedImage;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getItemImages(long)
	 */
	public ArrayList<Image> getItemImages(long itemID){
		ArrayList<Image> images = new ArrayList<Image>();
		if(itemID < 1) return images;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement getImagesStatement = null;
				
				try {
					images = new ArrayList<Image>();
					
					getImagesStatement = conn.prepareStatement("SELECT URL,description,position FROM images WHERE itemid="+itemID);
					
					ResultSet rs = getImagesStatement.executeQuery();
					
					// If images are returned then we'll add them
					while(rs.next()){
						// test for bad
						int pos=-1;
						try{pos = rs.getInt("position");}
						catch(Exception e){pos=-1;}
						
						if(pos>-1)
						images.add( new Image(rs.getInt("position"),
								rs.getString("URL"),
								rs.getString("description")));
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
					System.out.println("CassandraDBQuery (getItemImages): Could not get the images");
					e.printStackTrace();
				} finally{
					this.closeSmt(getImagesStatement);
					this.closeConnection(conn);
				}		
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		// Sort as cassandra currently does not allow us to :-(
		ArrayList<Image> sortedimages = new ArrayList<Image>(images.size());
		Object[] sortedimg = images.toArray();
		java.util.Arrays.sort(sortedimg);
		for(int i=0; i<sortedimg.length; i++)
			sortedimages.add((Image)sortedimg[i]);

		return sortedimages;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getPurchases(long, long)
	 */
	public ArrayList<Purchase> getPurchases(long userID, long ts){
		ArrayList<Purchase> purchases = new ArrayList<Purchase>();
		if(userID < 1) return purchases;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				PreparedStatement itemstatement = null;
				
				try {
					// Get the purchases
					statement = conn.prepareStatement("SELECT itemid FROM purchased WHERE userid="+userID +" AND ts>="+ts);
					ResultSet rs = statement.executeQuery();

					// Get ids of items we need
					StringBuilder ids = new StringBuilder();
					ids.append("('0");
					while(rs.next()){
						ids.append("','");
						ids.append(rs.getLong("itemid"));
					}
					ids.append("')");
					rs.close();
					statement.close();
					
					// Get the items
					itemstatement = conn.prepareStatement("SELECT * FROM olditems WHERE KEY IN "+ids.toString());
					ResultSet itemrs = itemstatement.executeQuery();
					
					TreeMap<Long, Item> items = new TreeMap<Long, Item>();
					while(itemrs.next()){
						String name = null;
						try{
							name = itemrs.getString("name");
						} catch(Exception e) {}
						
						if(name!=null){
							ArrayList<Image> images = this.getItemImages(itemrs.getLong("KEY"));
						
							Item currentItem = new Item(itemrs.getLong("KEY"),
								name,
								new String(itemrs.getString("description")),
								new Integer((int)itemrs.getLong("quantity")),
								itemrs.getDouble("startprice"),
								itemrs.getDouble("reserveprice"),
								itemrs.getDouble("buynowprice"),
								itemrs.getDouble("curbid"),
								itemrs.getDouble("maxbid"),
								new Integer((int)itemrs.getLong("noofbids")),
								new Date(itemrs.getLong("startdate")),
								new Date(itemrs.getLong("enddate")),
								itemrs.getLong("sellerid"),
								itemrs.getLong("categoryid"),
								new String(itemrs.getString("thumbnail")),
								images);
							
							items.put(itemrs.getLong("KEY"), currentItem);
						}
					}
					itemrs.close();
					itemstatement.close();
					
					// Get the purchases and make them
					statement = conn.prepareStatement("SELECT * FROM purchased WHERE userid="+userID+" AND ts>="+ts);
					rs = statement.executeQuery();
					
					while(rs.next()){
							// Make sure it is real
							long pKey = 0;
							try{
								Long temp = rs.getLong("KEY");
								pKey = temp;
							} catch(Exception e) {}
							
							if(pKey>0){
								purchases.add(new Purchase(rs.getLong("KEY"),
										items.get(rs.getLong("itemid")),
										(int)rs.getLong("quantity"),
										rs.getDouble("price"),
										new Boolean(rs.getString("paid"))));
								}
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
	 * @see com.cmart.DB.DBQuery#getCurrentBids(long, long)
	 */
	public ArrayList<Bid> getCurrentBids(long userID, long ts){
		ArrayList<Bid> bids = new ArrayList<Bid>();
		if(userID < 1) return bids;
		int attemptsRemaining = SQL_RETRIES;

		ArrayList<Long> itemIDs = new ArrayList<Long>();
		
		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
	
				try {
					statement = conn.prepareStatement("SELECT * FROM maxbids WHERE userid = " + userID + " AND ts>="+ts);
					
					ResultSet rs = statement.executeQuery();
					
					User user=null;
					
					// Make the bids
					while(rs.next()){
						long ui = 0;
						try{ ui = rs.getLong("userid"); }
						catch(Exception e){ ui=0;}
						
						if(ui>0){
							if(user==null) user=getUser(rs.getLong("userid"));
							
							// We'll add items after
							Bid currentBid = new Bid(rs.getLong("bidkey"),
									rs.getLong("userid"),
									(int)rs.getLong("quantity"),
									rs.getDouble("bid"),
									rs.getDouble("maxbid"),
									new Date(rs.getLong("biddate")),
									null,
									user);
							bids.add(currentBid);
							
							// Set the bid itemID and add it of the list of IDs to get
							long itemID = rs.getLong("itemid");
							currentBid.setItemID(itemID);
							itemIDs.add(itemID);
						}
					}
					
					// Now get the items for those bids
					rs.close();
					
					// Get the items
					HashMap<Long, Item> items = getItemsByID(itemIDs, "items");
					
					// Put the items in the bids
					for(Bid b:bids){
						if(items.containsKey(b.getItemID()))
							b.setItem(items.get(b.getItemID()));
					}
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("CassandraQuery (getCurrentBids): Could not get the bids from bids");
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
	 * @see com.cmart.DB.DBQuery#getOldBids(long, long)
	 */
	public ArrayList<Bid> getOldBids(long userID, long ts){
		ArrayList<Bid> bids = new ArrayList<Bid>();
		if(userID < 1) return bids;
		int attemptsRemaining = SQL_RETRIES;

		ArrayList<Long> itemIDs = new ArrayList<Long>();
		
		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("SELECT * FROM maxoldbids WHERE userid = "+userID + " AND ts>="+ts);
					ResultSet rs = statement.executeQuery();
					
					User user=null;
					
					while(rs.next()){
						//Item currentItem;
						long ui = 0;
						try{ ui = rs.getLong("userid"); }
						catch(Exception e){ ui=0;}
						
						if(ui>0){
							if(user==null) user = getUser(rs.getLong("userid"));
							
							Bid currentBid = new Bid(rs.getLong("bidkey"),
									rs.getLong("userid"),
									(int)rs.getLong("quantity"),
									rs.getDouble("bid"),
									rs.getDouble("maxbid"),
									new Date(rs.getLong("biddate")),
									null,
									user);
							bids.add(currentBid);
							
							// Get the itemid to get and set the bid item id
							long itemID = rs.getLong("itemid");
							currentBid.setItemID(itemID);
							itemIDs.add(itemID);
						}
					}
					
					rs.close();
					
					// Get the items
					HashMap<Long, Item> items = getItemsByID(itemIDs, "olditems");
					
					// Put the items in the bids
					for(Bid b:bids){
						if(items.containsKey(b.getItemID()))
							b.setItem(items.get(b.getItemID()));
					}
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("CassandraQuery (getOldBids): Could not get the bids from old");
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
	 * @see com.cmart.DB.DBQuery#getBids(long)
	 */
	public ArrayList<Bid> getBids(long itemID){
		ArrayList<Bid> bids = new ArrayList<Bid>();
		if(itemID < 1) return bids;
		int attemptsRemaining = SQL_RETRIES;
		ArrayList<Long> itemIDs = new ArrayList<Long>();

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;

				try {
					for(int i=0;i<=1;i++){
						String table=null;
						String itemTable=null;
						
						// We don't know if it is new or old, so try both
						if(i==0){
							table="bids";
							itemTable="items";
						}
						else if(i==1){
							table="oldBids";
							itemTable="oldItems";
						}
						
						statement = conn.prepareStatement("SELECT * FROM "+table+" WHERE itemid = "+itemID);
						ResultSet rs = statement.executeQuery();

						User user=null;
						
						while(rs.next()){
							//Item currentItem;
							long ui = 0;
							try{ ui = rs.getLong("userid"); }
							catch(Exception e){ ui=0;}
							
							if(ui>0){
								if(user==null) user = getUser(rs.getLong("userid"));
								
								Bid currentBid = new Bid(rs.getLong("bidkey"),
										rs.getLong("userid"),
										(int)rs.getLong("quantity"),
										rs.getDouble("bid"),
										rs.getDouble("maxbid"),
										new Date(rs.getLong("biddate")),
										null,
										user);
								bids.add(currentBid);
								
								// Set the bid item id and add the item to get from the db
								currentBid.setItemID(itemID);
								itemIDs.add(itemID);
							}
						}
						
						rs.close();
						
						// Get the items
						HashMap<Long, Item> items = getItemsByID(itemIDs, itemTable);
						
						// Put the items in the bids
						for(Bid b:bids){
							if(items.containsKey(b.getItemID()))
								b.setItem(items.get(b.getItemID()));
						}
					}

					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("CassandraDBQuery (getBids): Could not get the bids from bids or oldBids");
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
	
	private HashMap<Long, Item> getItemsByID(ArrayList<Long> ids, String column){
		HashMap<Long, Item> items = new HashMap<Long, Item>();
		if(ids==null || ids.size()==0) return items;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;

				try {
					StringBuffer keys = new StringBuffer();
					boolean start = true;
					for(long l:ids){
						if(!start) keys.append(",");
						keys.append("'");
						keys.append(l);
						keys.append("'");
						start=false;
					}
					
					statement = conn.prepareStatement("SELECT * FROM "+column+" WHERE KEY IN (" + keys.toString() + ")");
					ResultSet rs = statement.executeQuery();
					
					while(rs.next()){
						Item currentItem;
						
						// Check the results are real
						long tempid=0;
						try{
							tempid=rs.getLong("sellerid");
						}
						catch(Exception e){ tempid = 0; }
						
						if(tempid==0){
							currentItem = null;
						}
						else{
							ArrayList<Image> images = this.getItemImages(rs.getLong("KEY"));
							
							currentItem = new Item(rs.getLong("KEY"),
									new String(rs.getString("name")),
									new String(rs.getString("description")),
									new Integer((int)rs.getLong("quantity")),
									rs.getDouble("startprice"),
									rs.getDouble("reserveprice"),
									rs.getDouble("buynowprice"),
									rs.getDouble("curbid"),
									rs.getDouble("maxbid"),
									new Integer((int)rs.getLong("noofbids")),
									new Date(rs.getLong("startdate")),
									new Date(rs.getLong("enddate")),
									rs.getLong("sellerid"),
									rs.getLong("categoryid"),
									new String(rs.getString("thumbnail")),
									images);
							items.put(rs.getLong("KEY"), currentItem);
						}
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
					System.out.println("CassandraQuery (getitemsbyid): Could not get the items");
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
	 * @see com.cmart.DB.DBQuery#getFirstName(long)
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
					// Create the CQL statement to get the user's first name
					statement = conn.prepareStatement("SELECT firstname FROM users WHERE KEY="+userID);
					
					ResultSet rs = statement.executeQuery();
					
					// If an item is returned then get the details
					if(rs.next()){
						try{
							name = rs.getString("firstname");
						}
						catch(Exception e) { }
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
					System.out.println("CassandraQuery (getFirstName): Could not get the user's firstname");
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
	 * @see com.cmart.DB.DBQuery#getPublicUser(long)
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
					statement = conn.prepareStatement("SELECT username,rating FROM users WHERE KEY="+userID);
					ResultSet rs = statement.executeQuery();

					// Read the user's details
					if(rs.next()){
						try{
						user = new User(userID,rs.getString("username"),new Long(rs.getLong("rating")).toString());
						}
						catch(Exception e){ }
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
					System.out.println("CassandraQuery (getPublicUser): Could not get the user");
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
	
	/*
	 * 
	 */
	public ArrayList<Category> getCategories(long parent) throws Exception{
		ArrayList<Category> categories = new ArrayList<Category>();
		int attemptsRemaining = SQL_RETRIES;
		
		do{
			Connection conn = this.getConnection();
			
			if(conn != null){
				PreparedStatement statement = null;
				
				try{
					statement = conn.prepareStatement("SELECT * FROM categories WHERE parent = "+parent);
					ResultSet rs = statement.executeQuery();
					
					// Put all of the states in to the results vector
					while(rs.next()){
						Category category = null;
						
						try{
							category = new Category(rs.getLong("KEY"),
								rs.getString("name"),
								rs.getLong("parent"),System.currentTimeMillis());
						}
						catch(Exception e){}
						
						if(category!=null) categories.add(category);
					}
					
					rs.close();
					
					attemptsRemaining=0;
				}
				catch(Exception e){
					System.err.println("CassandraQuery (getCategories): Could not get the categories");
					e.printStackTrace();
					throw e;
				}
				finally{
					closeSmt(statement);
					this.closeConnection(conn);	
				}	
			}
			
			attemptsRemaining--;
		}while(attemptsRemaining>0);
			
		return categories;
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
					statement = conn.prepareStatement("SELECT * FROM categories");
					ResultSet rs = statement.executeQuery();
					
					// Put all of the categories in the result array
					while(rs.next()){
						String name = null;
						try{
							name=rs.getString("name");
						} catch(Exception e){}
						
						if(name!=null){
							Category category = new Category(rs.getLong("KEY"),
									name,
									rs.getLong("parent"),
									rs.getTimestamp("ts").getTime());
							
							categories.add(category);
						}
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
					System.out.println("CassandraQuery (getAllCategories): Could not get the categories");
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
	
	public Category getCategory(long categoryID) throws Exception{
		if(categoryID < 1) return null;
		Category category = null;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("SELECT * FROM categories WHERE KEY = "+categoryID);
					ResultSet rs = statement.executeQuery();
					
					// Get the category info
					if(rs.next()){
						// Make sure it is real
						String name = null;
						try{
							name = rs.getString("name");
						}catch(Exception e){}
						
						if(name!=null)
						category = new Category(rs.getLong("KEY"),
								name,
								rs.getLong("parent"),
								rs.getTimestamp("ts").getTime());
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
					System.out.println("CassandraQuery (getCategory): Could not get the category");
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
	 * @see com.cmart.DB.DBQuery#insertUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public Boolean insertUser(String username, String password, String email, String firstName, String lastName){
		if(username == null || password == null || email == null || firstName == null || lastName == null) return Boolean.FALSE;
		Boolean insertedUser = Boolean.FALSE;
		
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// MAKE KEY
					long key = System.currentTimeMillis()*1000000 + System.nanoTime()%1000000;

					statement = conn.prepareStatement("INSERT INTO users (KEY, userid, firstname, lastname, username, password, email,username_password,rating, authtoken) VALUES ('"+key+"','"+key+"','"+firstName+"','"+lastName+"','"+username+"','"+password+"','"+email+"','"+safe(username+"_"+password)+"',0,'NULL') USING CONSISTENCY "+consistency);
					
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
					System.err.println("CassandraDBQuery (insertUser): Could not insert user: ");
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
	 * 
	 */
	public boolean insertAccount(String name, String nameOnCard, String creditCardNo, String cvv, Date expirationDate){
		if(name==null || nameOnCard==null || cvv==null || creditCardNo==null || expirationDate==null) return false;
		
		name=safe(name);
		nameOnCard=safe(nameOnCard);
		creditCardNo=safe(creditCardNo);
		cvv=safe(cvv);
		
		boolean insertedAccount = false;
		int attemptsRemaining = SQL_RETRIES;
		
		do{
			Connection conn = this.getConnection();
			
			if(conn != null){
				PreparedStatement statement = null;
				
				try{
					// MAKE KEY
					long key = System.currentTimeMillis()*shortback + System.nanoTime()%shortback;
					
					// Create the SQL statement to insert the user
					statement = conn.prepareStatement("INSERT INTO accounts (KEY, name, nameOnCard, creditCardNo, cvv, expirationDate) "
											+ "VALUES ("+key+",'"+name+"','"+nameOnCard+"','"+creditCardNo+"','"+cvv+"',"+expirationDate.getTime()+") USING CONSISTENCY "+consistency);
					
					statement.executeUpdate();
					
					
					insertedAccount = true;
					attemptsRemaining = 0;
				}
				catch(Exception e){
					System.err.println("CassandraLQuery (insertAccount): Could not insert account");
					e.printStackTrace();
					
					insertedAccount = false;
				}
				finally{
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}
		
			attemptsRemaining--;
		}while(attemptsRemaining > 0);
		
		return insertedAccount;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getAccount(long)
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
					statement = conn.prepareStatement("SELECT * FROM accounts WHERE KEY = " +accountID );
					ResultSet rs = statement.executeQuery();
					
					// Read the user's account
					if(rs.next()){
						String n =null;
						try{ n =rs.getString("name"); }
						catch(Exception e){ n=null; }
						
						if(n!=null)
						account = new Account(rs.getLong("KEY"),
								rs.getString("name"),
								rs.getString("nameoncard"),
								rs.getString("creditcardno"),
								rs.getString("cvv"),
								new Date(rs.getLong("expirationdate")));
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
					System.out.println("CassandraQuery (getAccount): Could not get the account");
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
	 * @see com.cmart.DB.DBQuery#updateUser(long, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public Boolean updateUser(long userID, String password, String email, String firstName, String lastName){
		if(userID < 1) return Boolean.FALSE;
		if(password == null || email == null || firstName == null || lastName == null) return Boolean.FALSE;
		
		Boolean insertedUser = Boolean.FALSE;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// Create the CQL statement to update the details
					statement = conn.prepareStatement("UPDATE users USING CONSISTENCY "+consistency+" SET firstname = '"+firstName+"', lastname = '"+lastName+"', " +
							"password = '"+password+"', email = '"+email+"' WHERE KEY = "+userID);
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
					System.out.println("CassandraQuery (updateUser): Could not update user");
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
	 * @see com.cmart.DB.DBQuery#getDefaultAddress(long)
	 */
	public Address getDefaultAddress(long userID) throws Exception{
		ArrayList<Address> addresses = this.getAddresses(userID, Boolean.TRUE);
		
		// We can only return the default address if there is one
		if(addresses != null && addresses.size()>0)
			return  addresses.get(0);
		else
			return null;
	}
	
	/*
	 * 
	 */
	public ArrayList<Address> getAddresses(long userID){
		return this.getAddresses(userID, Boolean.FALSE);
	}
	
	/*
	 * 
	 */
	private ArrayList<Address> getAddresses(long userID, Boolean onlyDefault){
		if(userID < 1) return null;
		ArrayList<Address> addresses = new ArrayList<Address>();
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					// if we are only getting the default address
					if(onlyDefault) 		
						statement = conn.prepareStatement("SELECT * FROM addresses WHERE isDefaultKey = " + userID);
					else
						statement = conn.prepareStatement("SELECT * FROM addresses WHERE userid = " + userID);
					
					ResultSet rs = statement.executeQuery();
					
					// Read the user's addresses
					boolean getAll = true;
					while(rs.next() && getAll){
						long key = 0;
						try{
							Long temp=rs.getLong("KEY");
							if(temp!=null) key = temp;
						} catch(Exception e){}
						
						if(key>0){
							Address address = new Address(key,
									rs.getLong("userid"),
									rs.getString("street"),
									rs.getString("town"),
									rs.getString("zip"),
									new Integer(rs.getString("state")),
									rs.getBoolean("isdefault"));
							
							addresses.add(address);
							
							if(onlyDefault) getAll = false;
						}	
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
					System.out.println("CassandraQuery (getAddresses): Could not get the addresses");
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
	 * @see com.cmart.DB.DBQuery#getAddress(long)
	 */
	public Address getAddress(long addressID){
		Address address = null;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement statement = null;
			
			try{
				statement = conn.prepareStatement("SELECT * FROM addresses WHERE KEY = "+addressID);
				ResultSet rs = statement.executeQuery();
				
				// Read the user's addresses
				if(rs.next()){
					long uid = 0;
					try{ uid=rs.getLong("userid");}
					catch(Exception e){ uid=0;}
					
					if(uid>0)
					address = new Address(rs.getLong("KEY"),
							rs.getLong("userid"),
							rs.getString("street"),
							rs.getString("town"),
							rs.getString("zip"),
							new Integer(rs.getString("state")),
							rs.getBoolean("isdefault"));
				}
				
				rs.close();
			}
			catch(Exception e){
				System.err.println("CassandraQuery (getAddress): Could not get the address");
				e.printStackTrace();
			}
			finally{
				closeSmt(statement);
				this.closeConnection(conn);
			}
		}
		
		return address;
	}
	
	/*
	 * 
	 */
	public String getStateName(int stateID){
		String state = null;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement statement =null;
			
			try{
				statement = conn.prepareStatement("SELECT KEY, longname FROM states WHERE KEY = "+stateID);
				ResultSet rs = statement.executeQuery();
				
				// Get the state name
				if(rs.next()){
					Long id = null;
					try{
						id = rs.getLong("KEY");
					} catch(Exception e){ }
					
					if(id!=null){
						try{
							state = rs.getString("longname");
						} catch(Exception e){ }
					}
				}
				
				rs.close();
			}
			catch(Exception e){
				System.err.println("CassandraQuery (getStateName): Could not get the state name");
				e.printStackTrace();
			}
			finally{
				closeSmt(statement);
				this.closeConnection(conn);
			}
		}
		
		return state;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getUser(long)
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
					statement = conn.prepareStatement("SELECT * FROM users WHERE KEY = "+userID);
					ResultSet rs = statement.executeQuery();
					
					// Read the user's details
					if(rs.next()){
						String fn=null;
						try{fn=rs.getString("firstname");}
						catch(Exception e){fn=null;}
						
						if(fn !=null){
						user = new User(userID,
								rs.getString("firstname"),
								rs.getString("lastname"),
								rs.getString("username"),
								rs.getString("password"),
								rs.getString("email"),
								rs.getString("authtoken"),
								new Long(rs.getLong("rating")).toString());
						}
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
					System.out.println("CassandraQuery (getUser): Could not get the user");
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
	 * @see com.cmart.DB.DBQuery#emailExists(java.lang.String)
	 */
	public Boolean emailExists(String email){	
		if(email == null) return Boolean.TRUE;
		Boolean exists = Boolean.TRUE;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				ResultSet rs = null;
				
				try {
					statement = conn.prepareStatement("SELECT email FROM users WHERE email = '"+email+"'");
					
					rs = statement.executeQuery();
					
					// If the e-mail address does exist in the table
					if(rs.next()){
						exists = Boolean.TRUE;
					}
					else{
						exists = Boolean.FALSE;
					}
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("CassandraQuery (checkEmailExists): Could not read results set");
					e.printStackTrace();
				} finally{
					this.close(rs);
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
	 * @see com.cmart.DB.DBQuery#usernameExists(java.lang.String)
	 */
	public Boolean usernameExists(String username) throws Exception {
		if(username == null) return Boolean.TRUE;
		Boolean exists = Boolean.TRUE;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				ResultSet rs = null;
				
				try {
					statement = conn.prepareStatement("SELECT username FROM users WHERE username = '"+username+"'");
					rs = statement.executeQuery();

					// If the username does exist in the table
					if (rs.next()) {
						exists = Boolean.TRUE;
					}
					else{
						exists = Boolean.FALSE;
					}
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("CassandraQuery (checkUsernameExists): Could not read results set");
					e.printStackTrace();
				} finally{
					this.close(rs);
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
	 * @see com.cmart.DB.DBQuery#getStates()
	 */
	public ArrayList<String[]> getStates() throws Exception{
		ArrayList<String[]> states = new ArrayList<String[]>();
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				ResultSet rs = null;
				
				try {
					statement = conn.prepareStatement("SELECT * FROM states");
					rs = statement.executeQuery();
					
					// Put all of the states in to the results array
					while(rs.next()){
						String shortname= null;
						try{
							shortname = rs.getString("shortname");
						} catch(Exception e){};
						
						if(shortname!=null){
							String[] result = new String[3];
							result[0] = rs.getString("KEY");
							result[1] = shortname;
							result[2] = rs.getString("longname");
							
							states.add(result);
						}	
					}
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("CassandraQuery (getStates): Could not read results set");
					e.printStackTrace();
				} finally{
					this.close(rs);
					this.closeSmt(statement);
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		return states;
	}
	
	/*
	 * 
	 */
	public long moveEndedItemsDB() throws Exception{
		int itemsMoved = 0;
		int attemptsRemaining = 5;
		
		/**
		 * 1. Get the ids of all the items that have ended
		 * 2. delete from items, ritems, rprice items
		 * 3. Move all of the bids for the items
		 * 4. delete from bids
		 * 5. move all of the max bids
		 * 6. delete max bids
		 * 7. create the purchase rows for people who have won items (must have beat reserve)
		 */
		
		do{
			Connection conn = this.getConnection();
			PreparedStatement getItems = null;
			ResultSet itemRS = null;
			PreparedStatement copyItems = null;
			PreparedStatement deleteItems = null;
			PreparedStatement deleteRItems = null;
			PreparedStatement deletePIItems = null;
			PreparedStatement deleteRPIItems = null;
			PreparedStatement getBids = null;
			ResultSet bidsrs = null;
			PreparedStatement copyBids = null;
			PreparedStatement deleteBids = null;
			PreparedStatement getMaxBid = null;
			ResultSet maxbidsrs = null;
			PreparedStatement copyMaxBids = null;
			PreparedStatement deleteMaxBids = null;
			
			if(conn != null){
				try{
					// Get the current time so we only move items before this fixed time (make the end of the key 9999...)
					Long KEY = ((System.currentTimeMillis()*shortback) * 10) - 1;
					Boolean multi = false;
					Boolean exists = false;
					
					StringBuilder CQL = new StringBuilder(2048);	
					
					/*
					 * Copy the items					
					 */
					getItems = conn.prepareStatement("SELECT * FROM items WHERE KEY>1 AND KEY < "+KEY.toString());
					itemRS = getItems.executeQuery();
					
					CQL = new StringBuilder(2048);
					
					// The keys for reverse price and items to delete
					StringBuilder itemids = new StringBuilder(1024);
					itemids.append("('0'");
					StringBuilder pikeys = new StringBuilder(1024);
					pikeys.append("('0'");
					StringBuilder rpikeys = new StringBuilder(1024);
					rpikeys.append("('0'");
					StringBuilder ritem = new StringBuilder(1024);
					ritem.append("('0'");
					ArrayList<Long> itemIDarr = new ArrayList<Long>();
					
					// Get the first item
					if(itemRS.next()){
						Long sellerid = null;
						try{
							sellerid = itemRS.getLong("sellerid");
						} catch(Exception e) {}
						
						// If the item really exists
						if(sellerid!=null){
							exists = true;
							
							itemids.append(",'"+itemRS.getString("KEY")+"'");
							pikeys.append(",'"+itemRS.getString("pikey")+"'");
							rpikeys.append(",'"+(Long.MAX_VALUE-Long.parseLong(itemRS.getString("pikey")))+"'");
							ritem.append(",'"+(Long.MAX_VALUE-Long.parseLong(itemRS.getString("KEY")))+"'");
							itemIDarr.add(Long.parseLong(itemRS.getString("KEY")));
							
							itemsMoved++; // keep count to return
							CQL.append("INSERT INTO olditems (KEY,itemid,name,description,quantity,startprice,reserveprice,buynowprice,curbid,maxbid,noofbids,startdate,enddate,sellerid,categoryid,thumbnail) VALUES ("+itemRS.getString("KEY")+","+itemRS.getLong("itemid")+",'"+itemRS.getString("name")+"','"+itemRS.getString("description")+"',"+itemRS.getLong("quantity")+",'"+itemRS.getString("startprice")+"','"+itemRS.getString("reserveprice")+"','"+itemRS.getString("buynowprice")+"','"+itemRS.getString("curbid")+"','"+itemRS.getString("maxbid")+"',"+itemRS.getLong("noofbids")+","+itemRS.getLong("startdate")+","+itemRS.getLong("enddate")+","+itemRS.getLong("sellerid")+","+itemRS.getLong("categoryid")+",'"+itemRS.getString("thumbnail")+"');");
							}
					}

					// Get all of the other items
					while(itemRS.next()){
						Long sellerid = null;
						try{
							sellerid = itemRS.getLong("sellerid");
						} catch(Exception e) {}
						
						if(sellerid!=null){
							exists=true;
							
							if(!multi){
								multi=true;
								exists = true;
								itemids.append(",'"+KEY+"'");
								itemIDarr.add(Long.parseLong(itemRS.getString("KEY")));
								itemsMoved++;
								String temp = CQL.toString();
								
								CQL = new StringBuilder(2048);
								CQL.append("BEGIN BATCH\n ");
								CQL.append(temp);
								CQL.append("INSERT INTO olditems (KEY,itemid,name,description,quantity,startprice,reserveprice,buynowprice,curbid,maxbid,noofbids,startdate,enddate,sellerid,categoryid,thumbnail) VALUES ("+itemRS.getString("KEY")+","+itemRS.getLong("itemid")+",'"+itemRS.getString("name")+"','"+itemRS.getString("description")+"',"+itemRS.getLong("quantity")+",'"+itemRS.getString("startprice")+"','"+itemRS.getString("reserveprice")+"','"+itemRS.getString("buynowprice")+"','"+itemRS.getString("curbid")+"','"+itemRS.getString("maxbid")+"',"+itemRS.getLong("noofbids")+","+itemRS.getLong("startdate")+","+itemRS.getLong("enddate")+","+itemRS.getLong("sellerid")+","+itemRS.getLong("categoryid")+",'"+itemRS.getString("thumbnail")+"');");
							}
							else{
								exists = true;
								itemids.append(",'"+KEY+"'");
								itemIDarr.add(Long.parseLong(itemRS.getString("KEY")));
								itemsMoved++;
								CQL.append("INSERT INTO olditems (KEY,itemid,name,description,quantity,startprice,reserveprice,buynowprice,curbid,maxbid,noofbids,startdate,enddate,sellerid,categoryid,thumbnail) VALUES ("+itemRS.getString("KEY")+","+itemRS.getLong("itemid")+",'"+itemRS.getString("name")+"','"+itemRS.getString("description")+"',"+itemRS.getLong("quantity")+",'"+itemRS.getString("startprice")+"','"+itemRS.getString("reserveprice")+"','"+itemRS.getString("buynowprice")+"','"+itemRS.getString("curbid")+"','"+itemRS.getString("maxbid")+"',"+itemRS.getLong("noofbids")+","+itemRS.getLong("startdate")+","+itemRS.getLong("enddate")+","+itemRS.getLong("sellerid")+","+itemRS.getLong("categoryid")+",'"+itemRS.getString("thumbnail")+"');");
							}
						}
					}
					
					// If there are multiple items end batch
					if(multi){
						CQL.append("APPLY BATCH;");
					}
					
					// copy the old items to the old items table
					if(exists){
						System.out.println(CQL.toString());
						copyItems = conn.prepareStatement(CQL.toString());
						copyItems.executeUpdate();
						
						deleteItems = conn.prepareStatement("DELETE FROM items WHERE KEY IN " + itemids.toString() + ");");
						deleteItems.executeUpdate();
						
						deleteRItems = conn.prepareStatement("DELETE FROM revtimeitems WHERE KEY IN " + ritem.toString() + ");");
						deleteRItems.executeUpdate();
						
						deletePIItems = conn.prepareStatement("DELETE FROM priceitems WHERE KEY IN " + pikeys.toString() + ");");
						deletePIItems.executeUpdate();
						
						deleteRPIItems = conn.prepareStatement("DELETE FROM revpriceitems WHERE KEY IN " + rpikeys.toString() + ");");
						deleteRPIItems.executeUpdate();
					}
					
					/*
					 * Copy the bids
					 */
					StringBuilder delbidids = new StringBuilder(1024);
					delbidids.append("('0'");

					// copy the old bids to the old bids table
					for(Long itemID:itemIDarr){
						getBids = conn.prepareStatement("SELECT * FROM bids WHERE itemid="+itemID);
						bidsrs = getBids.executeQuery();
						multi = false;
						exists = false;
						CQL = new StringBuilder(2048);
						
						// Get the first result and mark that we have some CQL to execute
						if(bidsrs.next()){
							// Make sure row is real
							Long userid = null;
							try{
								userid = bidsrs.getLong("userid");
							} catch(Exception e) {}
							
							// Add row
							if(userid!=null){
								exists=true;
								delbidids.append(",'"+ bidsrs.getString("KEY") +"'");
								CQL.append("INSERT INTO oldbids (KEY,bid,biddate,itemid,maxbid,quantity,userid) VALUES ('"+bidsrs.getString("KEY")+"','"+bidsrs.getString("bid")+"',"+bidsrs.getLong("biddate")+","+bidsrs.getLong("itemid")+",'"+bidsrs.getString("maxbid")+"','"+bidsrs.getString("quantity")+"',"+bidsrs.getLong("userid")+");");
							}
						}
						
						// Add the other rows
						while(bidsrs.next()){
							Long userid = null;
							try{
								userid = bidsrs.getLong("userid");
							} catch(Exception e) {}
							
							if(userid!=null){
								exists=true;
								
								// If there are multiple rows then we'll batch it
								if(!multi){
									exists = true;
									multi=true;
									delbidids.append(",'"+bidsrs.getString("KEY") +"'");
									String temp = CQL.toString();
									
									CQL = new StringBuilder(2048);
									CQL.append("BEGIN BATCH\n ");
									CQL.append(temp);
									CQL.append("INSERT INTO oldbids (KEY,bid,biddate,itemid,maxbid,quantity,userid) VALUES ('"+bidsrs.getString("KEY")+"','"+bidsrs.getString("bid")+"',"+bidsrs.getLong("biddate")+","+bidsrs.getLong("itemid")+",'"+bidsrs.getString("maxbid")+"','"+bidsrs.getString("quantity")+"',"+bidsrs.getLong("userid")+");");
								}
								else{
									exists = true;
									delbidids.append(",'"+bidsrs.getString("KEY") +"'");
									CQL.append("INSERT INTO oldbids (KEY,bid,biddate,itemid,maxbid,quantity,userid) VALUES ('"+bidsrs.getString("KEY")+"','"+bidsrs.getString("bid")+"',"+bidsrs.getLong("biddate")+","+bidsrs.getLong("itemid")+",'"+bidsrs.getString("maxbid")+"','"+bidsrs.getString("quantity")+"',"+bidsrs.getLong("userid")+");");
								}
							}
						}
						
						// if we batched it, end the batch
						if(multi){
							CQL.append("APPLY BATCH;");
						}
						
						if(exists){
							copyBids = conn.prepareStatement(CQL.toString());
							copyBids.executeUpdate();
							copyBids.close();
						}
						bidsrs.close();
						getBids.close();
					}
					
					// Delete the replica bids from the bids table
					deleteBids = conn.prepareStatement("DELETE FROM bids WHERE KEY IN "+delbidids.toString() +");");
					deleteBids.executeUpdate();
					
					// Copy the max bids
					StringBuilder maxbidids = new StringBuilder(1024);
					maxbidids.append("('0'");
					
					for(Long id:itemIDarr){
						// Get the max bids
						getMaxBid = conn.prepareStatement("SELECT * FROM maxbids WHERE itemid="+id);
						maxbidsrs = getMaxBid.executeQuery();
						CQL = new StringBuilder(2048);
						multi = false;
						exists = false;
						
						// Get the first item
						if(maxbidsrs.next()){
							Long bidkey = null;
							try{
								bidkey = maxbidsrs.getLong("bidkey");
							} catch(Exception e) {}
							
							// If the item really exists
							if(bidkey!=null){
								exists = true;
								maxbidids.append(",'"+maxbidsrs.getString("KEY")+"'");
								
								CQL.append("INSERT INTO maxoldbids (KEY, bidkey, userid, itemid, quantity, bid, maxbid, biddate, ts) "
										+ "VALUES ('"+maxbidsrs.getString("KEY")+"','"+bidkey+"','"+maxbidsrs.getLong("userid")+"','"+maxbidsrs.getLong("itemid")+"','"+maxbidsrs.getLong("quantity")+"','"+maxbidsrs.getDouble("bid")+"','"+maxbidsrs.getDouble("maxbid")+"','"+maxbidsrs.getLong("biddate")+"',"+maxbidsrs.getLong("ts")+");");
								}
						}

						// Get all of the other items
						while(maxbidsrs.next()){
							Long bidkey = null;
							try{
								bidkey = maxbidsrs.getLong("bidkey");
							} catch(Exception e) {}
							
							if(bidkey!=null){
								exists=true;
								
								if(!multi){
									multi=true;
									exists = true;
									maxbidids.append(",'"+maxbidsrs.getString("KEY")+"'");
									String temp = CQL.toString();
									
									CQL = new StringBuilder(2048);
									CQL.append("BEGIN BATCH\n ");
									CQL.append(temp);
									CQL.append("INSERT INTO maxoldbids (KEY, bidkey, userid, itemid, quantity, bid, maxbid, biddate, ts) "
											+ "VALUES ('"+maxbidsrs.getString("KEY")+"','"+bidkey+"','"+maxbidsrs.getLong("userid")+"','"+maxbidsrs.getLong("itemid")+"','"+maxbidsrs.getLong("quantity")+"','"+maxbidsrs.getDouble("bid")+"','"+maxbidsrs.getDouble("maxbid")+"','"+maxbidsrs.getLong("biddate")+"',"+maxbidsrs.getLong("ts")+");");
									}
								else{
									exists = true;
									maxbidids.append(",'"+maxbidsrs.getString("KEY")+"'");
									
									CQL.append("INSERT INTO maxoldbids (KEY, bidkey, userid, itemid, quantity, bid, maxbid, biddate, ts) "
											+ "VALUES ('"+maxbidsrs.getString("KEY")+"','"+bidkey+"','"+maxbidsrs.getLong("userid")+"','"+maxbidsrs.getLong("itemid")+"','"+maxbidsrs.getLong("quantity")+"','"+maxbidsrs.getDouble("bid")+"','"+maxbidsrs.getDouble("maxbid")+"','"+maxbidsrs.getLong("biddate")+"',"+maxbidsrs.getLong("ts")+");");
								}
							}
						}
						
						// If there are multiple items end batch
						if(multi){
							CQL.append("APPLY BATCH;");
						}
						
						if(exists){
							copyMaxBids = conn.prepareStatement(CQL.toString());
							copyMaxBids.executeUpdate();
							copyMaxBids.close();
						}
						maxbidsrs.close();
						getMaxBid.close();
					}
					
					// Delete the max bids
					deleteMaxBids = conn.prepareStatement("DELETE FROM maxbids WHERE KEY IN "+maxbidids.toString()+");");
					deleteMaxBids.executeUpdate();
					
					attemptsRemaining = 0;
				}
				catch(Exception e){
					System.out.println("CassandraDBQuery (moveEndedItems): Could not read results set");
					e.printStackTrace();
					
					throw e;
				}
				finally{
					this.close( itemRS);
					this.closeSmt(getItems);
					this.closeSmt( copyItems);
					this.closeSmt( deleteItems);
					this.closeSmt( deleteRItems);
					this.closeSmt( deletePIItems);
					this.closeSmt( deleteRPIItems);
					this.closeSmt( deleteBids);
					if(bidsrs!=null) this.close( bidsrs);
					if(maxbidsrs!=null) this.close( maxbidsrs);
					this.closeSmt( deleteMaxBids);
					
					this.closeConnection(conn);
				}
			}
		
			attemptsRemaining--;
		}while(attemptsRemaining>0);
		
		return itemsMoved;
	}

	/*
	 * 
	 */
	public ArrayList<Long> getCategoryItemsIDs(long categoryID, int page,
			int itemsPP, int sortCol, Boolean sortDec, long lastSeenID) throws Exception {
		// Get the items
		// This is just as efficient as Cassandra won't sort them for us
		ArrayList<Item> items = getCategoryItems(categoryID, page,
				itemsPP, sortCol, sortDec, false,
				new String[0], lastSeenID);
		
		// Return only their IDs
		ArrayList<Long> ids = new ArrayList<Long>();
		for(Item i: items)
			ids.add(i.getID());
		
		return ids;
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
	
	public ArrayList<Item> getCategoryItems(long categoryID, int page,
			int itemsPP, int sortCol, Boolean sortDec, Boolean getImages, int numImages,
			String[] hasItems, long lastSeenID)
			throws Exception {
		ArrayList<Item> items = new ArrayList<Item>();
		
		if(categoryID<0 || page<0 || itemsPP<=0 || sortCol<0) return items;
		if(sortDec==null || getImages==null || hasItems==null) return items;
		
		Connection conn = this.getConnection();
		Date nowdate = new Date(System.currentTimeMillis());
		
		
		if(conn != null){
			try{
				PreparedStatement getPIKey = null;
				ResultSet pikeyrs = null;
				PreparedStatement getItemIDs = null;
				ResultSet itemids = null;
				PreparedStatement getItems = null;
				ResultSet itemsrs = null;
				
				TreeMap<Long, Long> notIn = new TreeMap<Long, Long>();
				for(String not:hasItems){
					try{
						Long l = Long.valueOf(not);
						notIn.put(l, l);
					}
					catch(Exception e){}
				}
				
				/*
				 * 1. get item ids to get
				 * 2. make the IN statement
				 * 3. get the items
				 * 4. sort to correct order
				 */
				
				int factor = 1;
				int newitemsPP = itemsPP;
				int gotGood=0;
				
				while(factor<5 && gotGood<itemsPP){
					try{
				String CQL = "";
				newitemsPP = newitemsPP*(factor*factor);
				factor++;
				
				switch(sortCol){
					case 1:{
						/*
						 * 1. get the pikey for the last seen item
						 * 2. get the items that are after that pikey
						 */
						
						long lastPIKey = 1;
						if(lastSeenID>0){
							getPIKey = conn.prepareStatement("SELECT pikey FROM items WHERE KEY="+lastSeenID);	
							pikeyrs = getPIKey.executeQuery();
							
							if(pikeyrs.next()){
								try{
									Long temp = pikeyrs.getLong("pikey");
									if(temp!=null) lastPIKey = temp;
								}
								catch(Exception e) { }
							}
						}
						
						long now = System.currentTimeMillis() * shortback;
						
						// Get items by bid price
						if(sortDec){
							// If there is no lastPIkey for reverse price, we want to make it max so that we
							// get the most expensive item (but cassandra can't handle that, so make it 1 less)
							if(lastPIKey == 1) lastPIKey = (Long.MAX_VALUE-1);
							else  lastPIKey--;
							
							// price bigger first (smallest key in rev keys, smallest key=biggest price)
							if(categoryID>0){	
								CQL = "SELECT itemid FROM revpriceitems WHERE categoryid = '"+categoryID + "' AND pikey<=" + lastPIKey + " AND itemid>"+now+" LIMIT "+newitemsPP;	
							}
							else{
								CQL = "SELECT itemid FROM revpriceitems WHERE catzero=0 AND pikey<="+ lastPIKey +" AND itemid>"+now+" LIMIT "+newitemsPP;
							}
						}
						else{
							// lastPIKey has a min value of Long priceKey = ((long) (price*1000000000000l)) + (itemID % 10000000000l);
							// for an item
							if(lastPIKey == 1) lastPIKey = tenzero-1;
							else lastPIKey++;
							
							// get the lowest priced items first (by current bid, smallest key = smallest price)
							if(categoryID>0){	
								CQL = "SELECT itemid FROM priceitems WHERE categoryid = '"+categoryID + "' AND pikey>="+lastPIKey+" AND itemid>="+now+" LIMIT "+newitemsPP;	
							}
							else{
								CQL = "SELECT itemid FROM priceitems WHERE catzero=0 AND pikey>="+lastPIKey+" AND itemid>="+now+" LIMIT "+newitemsPP;
							}						
						}
						
						break;
					}
					default:{
						if(!sortDec){
							// Make only current items be returned
							long now = System.currentTimeMillis() * shortback;
							if(lastSeenID<now) lastSeenID = now;
							else lastSeenID++;
							
							// end date getting larger, i.e. item that expires earliest is selected first
							if(categoryID>0){	
								CQL = "SELECT itemid FROM items WHERE categoryid = '"+categoryID + "' AND itemid>="+lastSeenID+" LIMIT "+newitemsPP;	
							}
							else{
								CQL = "SELECT itemid FROM items WHERE KEY>="+lastSeenID+" LIMIT "+newitemsPP;
							}
						}
						else{
							if(lastSeenID==0) lastSeenID = (Long.MAX_VALUE-1);
							else lastSeenID--;
							long now = System.currentTimeMillis() * shortback;
							
							// end date getting smaller, i.e. item that expires last is first to be selected
							if(categoryID>0){
								CQL = "SELECT itemid FROM revtimeitems WHERE categoryid = '"+categoryID + "' AND itemid<"+lastSeenID+ " AND itemid>" + now + " LIMIT "+newitemsPP;	
							}
							else{
								CQL = "SELECT itemid FROM revtimeitems WHERE catzero=0 AND KEY>"+(Long.MAX_VALUE-lastSeenID)+" AND itemid>"+ now +" LIMIT "+newitemsPP;
							}
						}
						
						break;
					}
				}
				
				getItemIDs = conn.prepareStatement(CQL);	
				itemids = getItemIDs.executeQuery();
				
				StringBuilder ids = new StringBuilder();
				ids.append("('0'");
				
				while(itemids.next()){
					Long itemid=null;
					try{
						itemid = itemids.getLong("itemid");
					} catch(Exception e) {};
					
					if(itemid!=null && !notIn.containsKey(itemid)){
						ids.append(",'");
						ids.append(itemid);
						ids.append("'");
					}
				}
				ids.append(");");
				
				getItems = conn.prepareStatement("SELECT * FROM items WHERE KEY IN " + ids.toString());
				itemsrs = getItems.executeQuery();

				int imgCount=0;
				while(itemsrs.next() && gotGood<newitemsPP){
					try{
						
					// Cassandra can fail because items don't have all the info required
					Item currentItem = null;
					try{
						ArrayList<Image> images = new ArrayList<Image>();
						if(getImages&&imgCount<numImages){
							images = this.getItemImages(itemsrs.getLong("KEY"));
							imgCount++;
						}
						
						currentItem = new Item(itemsrs.getLong("KEY"),
								itemsrs.getString("name"),
								itemsrs.getString("description"),
								itemsrs.getInt("quantity"),
								itemsrs.getDouble("startprice"),
								itemsrs.getDouble("reserveprice"),
								itemsrs.getDouble("buynowprice"),
								itemsrs.getDouble("curbid"),
								itemsrs.getDouble("maxbid"),
								itemsrs.getInt("noofbids"),
							new Date(itemsrs.getLong("startdate")),
							new Date(itemsrs.getLong("enddate")),
							itemsrs.getLong("sellerid"),
							itemsrs.getLong("categoryid"),
							itemsrs.getString("thumbnail"),
							images);
					}
					catch(Exception e){}
					
					if(currentItem != null){
						if(!items.contains(currentItem) && currentItem.getEndDate().after(nowdate)){
							items.add(currentItem);
							gotGood++;
						}
					}
					}catch(NullPointerException e){}
				}
				
				// We now need to sort the items
				switch(sortCol){
					case 1:{
						// lowest price first
						if(!sortDec){
							Collections.sort(items, new Comparator<Item>(){
								  public int compare(Item i1, Item i2) {
								    return i1.getCurrentBid() < i2.getCurrentBid() ? -1 : 1;
								  }
							});
						}
						// highest price first, we have reversed the comparator operator
						else{
							Collections.sort(items, new Comparator<Item>(){
								  public int compare(Item i1, Item i2) {
								    return i1.getCurrentBid() < i2.getCurrentBid() ? 1 : -1;
								  }
							});
						}
						
						
						break;
					}
					default:{
						// Earliest expiration first
						if(!sortDec){
							Collections.sort(items, new Comparator<Item>(){
								  public int compare(Item i1, Item i2) {
								    return i1.getEndDate().before(i2.getEndDate()) ? -1 : 1;
								  }
							});
						}
						// Latest expiration first
						else{
							Collections.sort(items, new Comparator<Item>(){
								  public int compare(Item i1, Item i2) {
								    return i1.getEndDate().before(i2.getEndDate()) ? 1 : -1;
								  }
							});
						}
						
						break;
					}
				}
					}
					catch(Exception e){
						System.err.println("CassandraQuery (getCategoryItems): Could not get the items");
						e.printStackTrace();
						throw e;
					}
					finally{
						this.closeSmt( getPIKey);
						this.close(pikeyrs);
						this.closeSmt( getItemIDs);
						this.close( itemids);
						this.closeSmt( getItems);
						this.close( itemsrs);
					}
				}
			}
			catch(Exception e){
				System.err.println("CassandraQuery (getCategoryItems): Could not get the items");
				e.printStackTrace();
				throw e;
			}finally{
			
				this.closeConnection(conn);
			}
		}
		
		return items;
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#insertBidDB(long, long, int, double, double)
	 */
	protected double insertBidDB(long userID, long itemID, int quantity, double bid, double maxBid) {
		double price = -1.0;
		if(userID <1 || itemID<1 || quantity<1) return price;
		if(bid<=0.0 || maxBid<bid) return price;
		
		int attemptsRemaining = SQL_RETRIES;
		
		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement insertBidStatement = null;
				PreparedStatement insertMaxBidStatement = null;
				PreparedStatement getMaxBidStatement = null;
				PreparedStatement updateItemStatement = null;
				PreparedStatement priceItemStatement = null;	
				
				try {
					/*
					 * 1. get the bid for the item
					 * 2. check to see if the new bid is more
					 * 3. update the item with the new bid price
					 * 4. update the maxbid table with the users bid
					 * 5. insert to the itemprice and revitem price tables
					 * 6. delete the old values from the itemprice and revitem price tables
					 */
					
					// Get maxBid item
					getMaxBidStatement = conn.prepareStatement("SELECT startprice, curbid, maxbid, noofbids, currentwinner, sellerid, categoryid, pikey FROM items WHERE KEY = "+itemID);
					ResultSet rs = getMaxBidStatement.executeQuery();
					
					if(rs.next()){
						// Check if the item is real
						long sellerid=0;
						try{
							sellerid = rs.getLong("sellerid");
						}catch(Exception e){ sellerid=0; }
						
						
						if(sellerid>0){
							// Insert the bid
							// MAKE KEY
							long bidKey = System.currentTimeMillis()*shortback + System.nanoTime()%shortback;
							insertBidStatement = conn.prepareStatement("INSERT INTO bids (KEY, userid, itemid, quantity, bid, maxbid, biddate) "
									+ "VALUES ('"+bidKey+"','"+userID+"','"+itemID+"','"+quantity+"','"+bid+"','"+maxBid+"','"+System.currentTimeMillis()+"') USING CONSISTENCY "+consistency);
							
							insertBidStatement.execute();						
							
							double dbCurrentBid = rs.getDouble("curbid");
							double dbMaxBid = rs.getDouble("maxbid");
							double dbStartPrice = rs.getDouble("startprice");
							int dbNoOfBids = (int)rs.getLong("noofbids");
							long currentWinner = rs.getLong("currentwinner");
							long categoryid = rs.getLong("categoryid");
							
							// Get pikey so we can update list by price
							Long pikey = null;
							try{
								pikey = rs.getLong("pikey");
							} catch(Exception e){}
							
							// Vals to set in the db for the new price
							double newPrice = dbCurrentBid;
							double newMaxBid = dbMaxBid;
							int newNoOfBids = dbNoOfBids;
							long newWinner = currentWinner;
							
							if(maxBid<dbStartPrice){
								// Should not happen
							}
							else if(maxBid< Math.max(dbCurrentBid, dbStartPrice)){
								// The bid shouldn't really be accepted, but they will lose anyway
								newPrice = dbCurrentBid;
								newMaxBid = dbMaxBid;
								newNoOfBids = dbNoOfBids+1;
								newWinner = currentWinner;
								price = Math.max(dbCurrentBid, dbStartPrice);
							}
							else if(dbMaxBid==maxBid){
								// New max bid is the same as old max bid, old user is still winning as they bidded first
								newPrice = dbMaxBid;
								newMaxBid = dbMaxBid;
								newNoOfBids = dbNoOfBids+1;
								newWinner = currentWinner;
								price = maxBid;
							}
							else if(dbMaxBid>maxBid){
								// The old max bid is greater, old user still winning by 0.01c
								// max bid is the same, current bid is the new bid's maxPrice+1c
								newPrice = maxBid + 0.01;
								newMaxBid = dbMaxBid;
								newNoOfBids = dbNoOfBids+1;
								newWinner = currentWinner;
								price = maxBid + 0.01;
							}
							else{
								// Else the new bid is more, the new user is winning
								// The new user's bid will be the old user's max(maxBid+1c, new bid)
								newPrice = (bid>dbMaxBid) ? bid : (dbMaxBid + 0.01);
								newMaxBid = maxBid;
								newNoOfBids = dbNoOfBids+1;
								newWinner = userID;
								price = (bid>dbMaxBid) ? bid : (dbMaxBid + 0.01);
							}
							
							// If there is a price, update the item and the max bids
							if(price>0.0){
								// Make the pikey for price sorting
								Long priceKey = ((long) (price*1000000000000l)) + (itemID % 10000000000l);
								
								// Update the item's price
								updateItemStatement = conn.prepareStatement("UPDATE items USING CONSISTENCY "+consistency+" SET curbid = '"+df.format(newPrice)+"', maxbid = '"+df.format(newMaxBid)+
										"', noofbids = '"+newNoOfBids+"', currentwinner = '"+newWinner+"', pikey="+priceKey+", ts="+System.currentTimeMillis()+" WHERE KEY="+itemID );
								updateItemStatement.executeUpdate();
								
								// Insert user's max bid
								insertMaxBidStatement = conn.prepareStatement("INSERT INTO maxbids (KEY, bidkey, userid, itemid, quantity, bid, maxbid, biddate, ts) "
										+ "VALUES ('"+userID+"_"+itemID+"','"+bidKey+"','"+userID+"','"+itemID+"','"+quantity+"','"+df.format(bid)+"','"+df.format(maxBid)+"','"+System.currentTimeMillis()+"',"+System.currentTimeMillis()+") USING CONSISTENCY "+consistency);
								insertMaxBidStatement.execute();	
								
								// Insert the new price sort values
								//System.out.println("item " + itemID + " mod " + (itemID % 10000000000l));
								//System.out.println("price " + price + " mul " + ((long) (price*1000000000000l)));
								
								//System.out.println("Got bid price key " + priceKey +", price "+price +", item:"+itemID);
								priceItemStatement = conn.prepareStatement("UPDATE priceitems USING CONSISTENCY "+consistency+" SET curbid = "+price+", categoryid="+categoryid+", catzero=0, itemid="+itemID+", pikey="+priceKey+" WHERE KEY="+priceKey);
								priceItemStatement.executeUpdate();
								priceItemStatement = conn.prepareStatement("UPDATE revpriceitems USING CONSISTENCY "+consistency+" SET curbid = "+price+", categoryid="+categoryid+", catzero=0, itemid="+itemID+", pikey="+priceKey+" WHERE KEY="+(Long.MAX_VALUE-priceKey));
								priceItemStatement.executeUpdate();
								
								// Delete the old item price
								if(pikey!=null){
									priceItemStatement = conn.prepareStatement("DELETE FROM priceitems WHERE KEY="+pikey);
									priceItemStatement.executeUpdate();
									priceItemStatement = conn.prepareStatement("DELETE FROM revpriceitems WHERE KEY="+(Long.MAX_VALUE-pikey));
									priceItemStatement.executeUpdate();
								}
							}
							
						}
					}
					// Close things
					rs.close();
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (MySQLNonTransientConnectionException e) {
					this.forceCloseConnection(conn);
					this.checkConnections();
				} catch (Exception e) {
					System.out.println("CassandraDB (insertBid): Could not get insert bid");
					e.printStackTrace();
				} finally{
					this.closeSmt(insertBidStatement);
					this.closeSmt(getMaxBidStatement);
					this.closeSmt(insertMaxBidStatement);
					this.closeSmt(updateItemStatement);
					this.closeSmt(priceItemStatement);
					
					this.closeConnection(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);

		return price;
	}

	protected Boolean buyItemNowDB(long userID, long itemID, int quantity,
			Account account, Address address) {
		if(userID < 1 || itemID < 1) return Boolean.FALSE;
		if(account == null || address == null) return Boolean.FALSE;
		
		boolean purchased = false;
		Connection conn = this.getConnection();
		
		if(conn != null && account != null){
			PreparedStatement getQuantityStatement = null;
			PreparedStatement deleteBidsStatement = null;
			PreparedStatement purchaseItemStatement = null;
			PreparedStatement paymentItemStatement = null;
			PreparedStatement setQuantityStatement = null;
			PreparedStatement getStatement = null;
			PreparedStatement copyItemStatement = null;
			PreparedStatement deleteItemStatement = null;
			PreparedStatement copyBidsStatement = null;
			PreparedStatement getBidsStatement = null;
			PreparedStatement getMaxBidsStatement = null;
			PreparedStatement deleteMaxBidsStatement = null;
			PreparedStatement deleteItemPrice = null;
			
			try{
				/*
				 * 1. Make suer there is enough quantity
				 * 2. insert payment
				 * 3. update the items quantity
				 * 4. insert purchase
				 * 5. if the quantity is now zero
				 * 		move item to old
				 * 		move bids to old
				 * 		move max old bids
				 * 		delete from itemprice and revitemprice and revitem
				 */
				
				// Check there is sufficient quantity
				getQuantityStatement = conn.prepareStatement("SELECT quantity, buynowprice FROM items WHERE KEY="+itemID);
				ResultSet rs = getQuantityStatement.executeQuery();
				
				if(rs.next()){
					// Make sure the row is real
					long quant = 0;
					try{
						quant = rs.getLong("quantity");
					} catch(Exception e) { quant=0; }
					
					if(quant>0){
						// Get the quantity and price
						int dbQuantity = (int)rs.getLong("quantity");
						double dbBuyNowPrice = rs.getDouble("buynowprice");
						rs.close();
						
						// If there is sufficient quantity, add the payment and purchase
						// then look in to decreasing the quantity
						if((dbQuantity - quantity) >= 0 && dbBuyNowPrice>0.0){
							// MAKE KEY
							long key1 = System.currentTimeMillis()*shortback + System.nanoTime()%shortback; 

							purchaseItemStatement = conn.prepareStatement("INSERT INTO purchased (KEY,userid, itemid, quantity, price, purchasedate, accountid, paid, paiddate, ts) "
									+ "VALUES ("+key1+","+userID+","+itemID+","+quantity+",'"+(dbBuyNowPrice*quantity)+"',"+System.currentTimeMillis()+","+account.getAccountID()+",'true',"+System.currentTimeMillis()+","+System.currentTimeMillis()+") USING CONSISTENCY "+consistency);			
							
							paymentItemStatement = conn.prepareStatement("INSERT INTO payments (KEY, userid, itemid, quantity, price, paiddate, street, town, zip, state, nameoncard, creditcardno, cvv, expirationdate) "
									+ "VALUES ("+key1+","+userID+","+itemID+","+quantity+",'"+(dbBuyNowPrice*quantity)+"',"+System.currentTimeMillis()+",'"+safe(address.getStreet())+"','"+safe(address.getTown())+"','"+address.getZip()+"','"+safe(getStateName(address.getState()))+"','"+safe(account.getNameOnCard())+"','"+account.getCreditCardNo()+"','"+account.getCVV()+"',"+account.getExpirationDate().getTime()+") USING CONSISTENCY "+consistency);
							
							paymentItemStatement.executeUpdate();
							purchaseItemStatement.executeUpdate();
							
							purchased = true;
							
							// Now update the item quantity
							setQuantityStatement = conn.prepareStatement("UPDATE items USING CONSISTENCY "+consistency+" SET quantity="+(dbQuantity-quantity)+", ts=" +System.currentTimeMillis()+" WHERE KEY="+itemID);
							setQuantityStatement.executeUpdate();
						}		
						
						// If there are no more of the item, move the item and the bids to the old tables
						if(purchased && dbQuantity-quantity <= 0){
							Long pikey = null;
							
							// Copy the item if it still exists
							getStatement = conn.prepareStatement("SELECT * FROM items WHERE KEY="+itemID);
							ResultSet getRs = getStatement.executeQuery();
							
							// Make sure it is real
							if(getRs.next()){
								long tempid2 = 0;
								try{
									tempid2 = getRs.getLong("sellerid");
									pikey = getRs.getLong("pikey");
								} catch(Exception e) { }
							
								if(tempid2 != 0){
									copyItemStatement = conn.prepareStatement("INSERT INTO olditems (KEY,itemid,name,description,quantity,startprice,reserveprice,buynowprice,curbid,maxbid,noofbids,startdate,enddate,sellerid,categoryid,thumbnail,ts)" +
											" VALUES ("+itemID+","+itemID+",'"+safe(getRs.getString("name"))+"','"+safe(getRs.getString("description"))+"',"+getRs.getLong("quantity")+",'"+getRs.getString("startprice")+"','"+getRs.getString("reserveprice")+"','"+getRs.getString("buynowprice")+"','"+getRs.getString("curbid")+"','"+getRs.getString("maxbid")+"',"+getRs.getLong("noofbids")+","+getRs.getLong("startdate")+","+getRs.getLong("enddate")+","+getRs.getLong("sellerid")+","+getRs.getLong("categoryid")+",'"+getRs.getString("thumbnail")+"',"+System.currentTimeMillis()+") USING CONSISTENCY "+consistency);
									copyItemStatement.executeUpdate();
									
									deleteItemStatement = conn.prepareStatement("DELETE FROM items WHERE KEY ="+itemID);
									deleteItemStatement.executeUpdate();
								}
							}
							getRs.close();
							
							// Copy the bids, then delete them
							getBidsStatement = conn.prepareStatement("SELECT * FROM bids WHERE itemid="+itemID);
							ResultSet bidsRs = getBidsStatement.executeQuery();
							
							StringBuffer toOldCQL = new StringBuffer(); // CQL TO INSERT IN TO 'OLDBIDS'
							toOldCQL.append("BEGIN BATCH\n ");
							boolean added = false;
							StringBuffer deleteIDs = new StringBuffer(); // TO DELETE COPIED ROWS
							deleteIDs.append("'0'");
							
							// Add all of the bids to move
							while(bidsRs.next()){
								long tempid3 = 0;
								try{
									tempid3 = bidsRs.getLong("userid");
								} catch(Exception e) { }
								
								if(tempid3>0){
									added = true;
									toOldCQL.append("INSERT INTO oldbids (KEY,bid,biddate,itemid,maxbid,quantity,userid,ts) VALUES ('"+bidsRs.getLong("KEY")+"','"+bidsRs.getString("bid")+"',"+bidsRs.getLong("biddate")+","+bidsRs.getLong("itemid")+",'"+bidsRs.getString("maxbid")+"','"+bidsRs.getString("quantity")+"',"+bidsRs.getLong("userid")+","+System.currentTimeMillis()+");\n");
									deleteIDs.append(",'"+bidsRs.getLong("KEY")+"'");
								}			
							}
							bidsRs.close();
							
							// Add all of the maxbids to move
							getMaxBidsStatement = conn.prepareStatement("SELECT * FROM maxbids WHERE itemid="+itemID);
							ResultSet maxBidsRs = getMaxBidsStatement.executeQuery();
							StringBuffer deleteMaxIDs = new StringBuffer();
							deleteMaxIDs.append("'0'");
							
							// Add all of the maxbids to move
							while(maxBidsRs.next()){
								long tempid3 = 0;
								try{
									tempid3 = maxBidsRs.getLong("userid");
								} catch(Exception e) { }
								
								if(tempid3>0){
									added = true;
									toOldCQL.append("INSERT INTO maxoldbids (KEY,bidkey,bid,biddate,itemid,maxbid,quantity,userid,ts) VALUES ('"+maxBidsRs.getString("KEY")+"','"+maxBidsRs.getLong("bidkey")+"','"+maxBidsRs.getString("bid")+"',"+maxBidsRs.getLong("biddate")+","+maxBidsRs.getLong("itemid")+",'"+maxBidsRs.getString("maxbid")+"','"+maxBidsRs.getString("quantity")+"',"+maxBidsRs.getLong("userid")+","+System.currentTimeMillis()+");\n");
									deleteMaxIDs.append(",'"+maxBidsRs.getString("KEY")+"'");
								}			
							}
							maxBidsRs.close();
							
							toOldCQL.append("\nAPPLY BATCH;"); // finish the insert batch
							
							// If there are bids/max bids to move, do it
							if(added){					
								copyBidsStatement = conn.prepareStatement(toOldCQL.toString());
								copyBidsStatement.executeUpdate();
								
								deleteBidsStatement = conn.prepareStatement("DELETE FROM bids WHERE KEY IN ("+deleteIDs.toString()+")");
								deleteBidsStatement.executeUpdate();
								deleteMaxBidsStatement = conn.prepareStatement("DELETE FROM maxbids WHERE KEY IN ("+deleteMaxIDs.toString()+")");
								deleteMaxBidsStatement.executeUpdate();
							}
							
							// Delete the other sorting things
							deleteItemPrice = conn.prepareStatement("DELETE FROM priceitems WHERE KEY = " + pikey);
							deleteItemPrice.executeUpdate();
							deleteItemPrice.close();
							deleteItemPrice = conn.prepareStatement("DELETE FROM revpriceitems WHERE KEY = " + (Long.MAX_VALUE-pikey));
							deleteItemPrice.executeUpdate();
							deleteItemPrice.close();
							deleteItemPrice = conn.prepareStatement("DELETE FROM revtimeitems WHERE KEY = " + (Long.MAX_VALUE-itemID));
							deleteItemPrice.executeUpdate();
						}
					}
				}
				else{
					// This should happen only in a race condition
					System.err.println("someone has bought the item between your request or the buy now price is zero!");	
				}
				
				}
			catch(Exception e){
				System.err.println("CassandraQuery (buyItemNow): Could not insert purchase");
				e.printStackTrace();
			}
			finally{
				this.closeSmt(getQuantityStatement );
				this.closeSmt( deleteBidsStatement );
				this.closeSmt( deleteMaxBidsStatement );
				this.closeSmt( purchaseItemStatement );
				this.closeSmt( paymentItemStatement);
				this.closeSmt( setQuantityStatement);
				this.closeSmt( getStatement );
				this.closeSmt( copyItemStatement );
				this.closeSmt( deleteItemStatement);
				this.closeSmt( getBidsStatement);
				this.closeSmt( getMaxBidsStatement);
				this.closeSmt(copyBidsStatement);
				this.closeSmt(deleteItemPrice);
				this.closeConnection(conn);
			}
		}
		
		return purchased;
	}
	
	public ArrayList<Item> getTextItemsDB(String text, int page, int itemsPP,
			int sortCol, Boolean sortDec) throws Exception {
		return getTextItemsDB(text,page,itemsPP,sortCol,sortDec,false,0);
	}

	/*
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getTextItemsDB(java.lang.String, int, int, int, java.lang.Boolean)
	 */
	public ArrayList<Item> getTextItemsDB(String text, int page, int itemsPP,
			int sortCol, Boolean sortDec,Boolean getimages,int numImages) throws Exception {
		ArrayList<Item> items = new ArrayList<Item>();

		System.out.println("Cannot search for text items in Cassandra, use SOLR");
		
		return items;
	}

	public ArrayList<Item> getItemsByID(ArrayList<Long> itemIDs, int sortCol, Boolean sortDec) throws Exception{
		return getItemsByID(itemIDs,sortCol,sortDec,false,0);
	}
	
	@Override
	public ArrayList<Item> getItemsByID(ArrayList<Long> itemIDs, int sortCol, Boolean sortDec,Boolean getimages,int numImages) throws Exception {
		ArrayList<Item> items = new ArrayList<Item>();
		
		if(sortDec==null || sortCol<0) return items;
		
		Connection conn = this.getConnection();
		if(conn != null){
			try{
				PreparedStatement statement;
				
				/*switch(sortCol){
					case 0: orderBy = "endDate"; break;
					case 1: orderBy = "currentBid"; break;
					case 2: orderBy = "endDate"; break;
					default: orderBy = "endDate"; break;
				}*/
				
				StringBuilder ids = new StringBuilder();
				ids.append("('0'");
				
				for(Long itemid: itemIDs){
						ids.append(",'");
						ids.append(itemid);
						ids.append("'");
				}
				ids.append(");");
				
				statement = conn.prepareStatement("SELECT * FROM items WHERE KEY IN " + ids.toString());
				
				ResultSet rs = statement.executeQuery();

				while(rs.next()){
					try{
						
					// Cassandra can fail because items don't have all the info required
					Item currentItem = null;
					try{
						ArrayList<Image> images = this.getItemImages(rs.getLong("KEY"));
						
						currentItem = new Item(rs.getLong("KEY"),
							rs.getString("name"),
							rs.getString("description"),
							rs.getInt("quantity"),
							rs.getDouble("startprice"),
							rs.getDouble("reserveprice"),
							rs.getDouble("buynowprice"),
							rs.getDouble("curbid"),
							rs.getDouble("maxbid"),
							rs.getInt("noofbids"),
							new Date(rs.getLong("startdate")),
							new Date(rs.getLong("enddate")),
							rs.getLong("sellerid"),
							rs.getLong("categoryid"),
							rs.getString("thumbnail"),
							images);
					}
					catch(Exception e){}
					
					if(currentItem != null){
						items.add(currentItem);
					}
					}catch(NullPointerException e){}
				}
				
				// We now need to sort the items
				switch(sortCol){
					case 1:{
						// lowest price first
						if(!sortDec){
							Collections.sort(items, new Comparator<Item>(){
								  public int compare(Item i1, Item i2) {
								    return i1.getCurrentBid() < i2.getCurrentBid() ? -1 : 1;
								  }
							});
						}
						// highest price first, we have reversed the comparator operator
						else{
							Collections.sort(items, new Comparator<Item>(){
								  public int compare(Item i1, Item i2) {
								    return i1.getCurrentBid() < i2.getCurrentBid() ? 1 : -1;
								  }
							});
						}
						
						
						break;
					}
					default:{
						// Earliest expiration first
						if(!sortDec){
							Collections.sort(items, new Comparator<Item>(){
								  public int compare(Item i1, Item i2) {
								    return i1.getEndDate().before(i2.getEndDate()) ? -1 : 1;
								  }
							});
						}
						// Latest expiration first
						else{
							Collections.sort(items, new Comparator<Item>(){
								  public int compare(Item i1, Item i2) {
								    return i1.getEndDate().before(i2.getEndDate()) ? 1 : -1;
								  }
							});
						}
						
						break;
					}
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("CassandraQuery (getItemsByID): Could not get the items");
				e.printStackTrace();
				throw e;
			}
			
			this.closeConnection(conn);
		}
		
		return items;
		}
	
	
	/*public long moveEndedItemsDB() throws Exception {
		int itemsMoved = 0;
		int attemptsRemaining = 5;
		
		/**
		 * 1. Get the ids of all the items that have ended
		 * 2. create the purchase rows for people who have won items (must have beat reserve)
		 * 3. move all of the bids for the old items to the old bids table
		 * 4. move all of the old items to the old items table
		 *
		
		do{
			Connection conn = this.getConnection();
			
			System.out.println("moving old items");
			
			if(conn != null){
				try{
					// Get the current time so we only move items before this fixed time
					Long KEY = System.currentTimeMillis()*shortback;
					// copy the old bids to the old bids table
					PreparedStatement findBids = conn.prepareStatement("SELECT * FROM bids WHERE KEY<"+KEY.toString());
					ResultSet rs = findBids.executeQuery();
					Boolean multi = false;
					Boolean bidsexist = false;
					String CQL = "";
					if(rs.next()){
						bidsexist=true;
						CQL="INSERT INTO oldbids (KEY,bid,biddate,itemid,maxbid,name,quantity,userid) VALUES ('"+rs.getString("KEY")+"','"+rs.getString("bid")+"',"+rs.getLong("biddate")+","+rs.getLong("itemid")+",'"+rs.getString("maxbid")+"','"+rs.getString("name")+"','"+rs.getString("quantity")+"',"+rs.getLong("userid")+");";
					}
					if(rs.next()){
						multi=true;
						CQL="BEGIN BATCH\n "+CQL+"INSERT INTO oldbids (KEY,bid,biddate,itemid,maxbid,name,quantity,userid) VALUES ('"+rs.getString("KEY")+"','"+rs.getString("bid")+"',"+rs.getLong("biddate")+","+rs.getLong("itemid")+",'"+rs.getString("maxbid")+"','"+rs.getString("name")+"','"+rs.getString("quantity")+"',"+rs.getLong("userid")+");";
					}
					while(rs.next()){
						CQL=CQL+"INSERT INTO oldbids (KEY,bid,biddate,itemid,maxbid,name,quantity,userid) VALUES ('"+rs.getString("KEY")+"','"+rs.getString("bid")+"',"+rs.getLong("biddate")+","+rs.getLong("itemid")+",'"+rs.getString("maxbid")+"','"+rs.getString("name")+"','"+rs.getString("quantity")+"',"+rs.getLong("userid")+");";
					}
					if(multi){
					CQL = "APPLY BATCH";
					}
					rs.close();
					
					PreparedStatement copyBids = conn.prepareStatement(CQL);					
					itemsMoved = copyBids.executeUpdate();
					copyBids.close();
					
					// Delete the replica bids from the bids table
					PreparedStatement deleteBids = conn.prepareStatement("DELETE FROM bids WHERE KEY<"+KEY.toString());
					deleteBids.executeUpdate();
					deleteBids.close();
					
					
					PreparedStatement statement = conn.prepareStatement("SELECT * FROM items WHERE KEY<"+KEY.toString());
					rs = statement.executeQuery();
					PreparedStatement copyItemStatement = null;
					multi = false;
					bidsexist = false;
					CQL = "";
					if(rs.next()){
						bidsexist=true;
						CQL="INSERT INTO olditems (KEY,itemid,name,description,quantity,startprice,reserveprice,buynowprice,curbid,maxbid,noofbids,startdate,enddate,sellerid,categoryid,thumbnail) VALUES ("+Math.abs(new Random().nextLong())+","+rs.getLong("itemid")+",'"+rs.getString("name")+"','"+rs.getString("description")+"',"+rs.getLong("quantity")+",'"+rs.getString("startprice")+"','"+rs.getString("reserveprice")+"','"+rs.getString("buynowprice")+"','"+rs.getString("curbid")+"','"+rs.getString("maxbid")+"',"+rs.getLong("noofbids")+","+rs.getLong("startdate")+","+rs.getLong("enddate")+","+rs.getLong("sellerid")+","+rs.getLong("categoryid")+",'"+rs.getString("thumbnail")+"');";
					}
					if(rs.next()){
						multi=true;
						CQL="BEGIN BATCH\n "+CQL+"INSERT INTO olditems (KEY,itemid,name,description,quantity,startprice,reserveprice,buynowprice,curbid,maxbid,noofbids,startdate,enddate,sellerid,categoryid,thumbnail) VALUES ("+Math.abs(new Random().nextLong())+","+rs.getLong("itemid")+",'"+rs.getString("name")+"','"+rs.getString("description")+"',"+rs.getLong("quantity")+",'"+rs.getString("startprice")+"','"+rs.getString("reserveprice")+"','"+rs.getString("buynowprice")+"','"+rs.getString("curbid")+"','"+rs.getString("maxbid")+"',"+rs.getLong("noofbids")+","+rs.getLong("startdate")+","+rs.getLong("enddate")+","+rs.getLong("sellerid")+","+rs.getLong("categoryid")+",'"+rs.getString("thumbnail")+"');";
					}
					while(rs.next()){
						CQL=CQL+"INSERT INTO olditems (KEY,itemid,name,description,quantity,startprice,reserveprice,buynowprice,curbid,maxbid,noofbids,startdate,enddate,sellerid,categoryid,thumbnail) VALUES ("+Math.abs(new Random().nextLong())+","+rs.getLong("itemid")+",'"+rs.getString("name")+"','"+rs.getString("description")+"',"+rs.getLong("quantity")+",'"+rs.getString("startprice")+"','"+rs.getString("reserveprice")+"','"+rs.getString("buynowprice")+"','"+rs.getString("curbid")+"','"+rs.getString("maxbid")+"',"+rs.getLong("noofbids")+","+rs.getLong("startdate")+","+rs.getLong("enddate")+","+rs.getLong("sellerid")+","+rs.getLong("categoryid")+",'"+rs.getString("thumbnail")+"');";
					}
					if(multi){
					CQL = "APPLY BATCH";
					}
					rs.close();
					
					// copy the old items to the old items table
					PreparedStatement copyItems = conn.prepareStatement(CQL);
					copyItems.executeQuery();
					copyItems.close();
					
					// Delete item statement
					PreparedStatement deleteItemStatement = conn.prepareStatement("DELETE FROM items WHERE KEY<"+KEY.toString());
					deleteItemStatement.executeUpdate();
					deleteItemStatement.close();
					
					attemptsRemaining = 0;
				}
				catch(Exception e){
					System.out.println("CassandraDBQuery (moveEndedItems): Could not read results set");
					e.printStackTrace();
					this.closeConnection(conn);
					
					throw e;
				}
				
				this.closeConnection(conn);
			}
		
			attemptsRemaining--;
		}while(attemptsRemaining>0);
		
		return itemsMoved;
	}*/

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

	/*
	 * User will have to send last userID as the pageNo
	 * (non-Javadoc)
	 * @see com.cmart.DB.DBQuery#getAllUserData(int, int)
	 */
	public ArrayList<User> getAllUserData(int itemsPerPage, int pageNo){
		ArrayList<User> users = new ArrayList<User>();
		if(itemsPerPage<1 || pageNo<0) return users;
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				try {
					statement = conn.prepareStatement("SELECT * FROM users WHERE KEY > " + pageNo + " LIMIT "+itemsPerPage);
					
					ResultSet rs = statement.executeQuery();
					
					// Put all of the users in the array list
					while(rs.next()){
						String firstname = null;
						try{
							firstname = rs.getString("firstname");
						} catch(Exception e){}
						
						if(firstname!=null)
						users.add( new User(rs.getLong("KEY"),
								firstname,
								rs.getString("lastname"),
								rs.getString("username"),
								rs.getString("password"),
								rs.getString("email"),
								rs.getString("authtoken"),
								rs.getString("rating")));
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
					System.out.println("CassandraDB (getAllUserDate): Could not read results set");
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
	
	
	public ArrayList<User> getAllUsers(){
		ArrayList<User> users = new ArrayList<User>();
		int attemptsRemaining = SQL_RETRIES;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				
				//TODO: fix this
				
				
				try {
					//TODO: should use the pageNo
					statement = conn.prepareStatement("SELECT * FROM users");
					
					ResultSet rs = statement.executeQuery();
					
					// Put all of the users in the array list
					while(rs.next()){
						users.add( new User(rs.getLong("KEY"),
								rs.getString("firstname"),
								rs.getString("lastname"),
								rs.getString("username"),
								rs.getString("password"),
								rs.getString("email"),
								rs.getString("authtoken"),
								rs.getString("rating")));
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
					System.out.println("CassandraDB (getAllUserDate): Could not read results set");
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
	

	public long insertComment(long userID, long sellerID, long itemID, int rating, Date endDate, String comment){
		long commentID = -1;
		if(userID<=0 || sellerID<=0 || itemID<=0 || endDate==null) return commentID;
		if(comment==null) return commentID;
		int attemptsRemaining = SQL_RETRIES;
	
		do{
			Connection conn = this.getConnection();
			if(conn != null){
				try{
					// MAKE KEY
					long key = System.currentTimeMillis()*shortback + System.nanoTime()%shortback;
					
					PreparedStatement statement = conn.prepareStatement("INSERT INTO comments (KEY,from_user_id, to_user_id, itemid, rating, date, comment) "
											+ "VALUES ("+key+","+userID+","+sellerID+","+itemID+","+rating+","+endDate.getTime()+",'"+ safe(comment)+"')");
					
					statement.executeUpdate();
					statement.close();
					
					if(strict){
						//TODO: make sure the comment was inserted
					}
					
					commentID = key;
					
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
					System.err.println("CassandraDB (insertComment): Could not insert comment");
					e.printStackTrace();
					
					//TODO: remove bad info
					commentID = -1;
				}
				
				this.closeConnection(conn);
			}
		
			attemptsRemaining--;
		}while(attemptsRemaining > 0);
		
		return commentID;
	}

	private String safe(String toSQLSafe){
		return org.apache.commons.lang.StringEscapeUtils.escapeSql(toSQLSafe);
	}
	
	public ArrayList<Comment> getComments(long itemID) throws Exception{
		ArrayList<Comment> comments = new ArrayList<Comment>();
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement statement;
				
				statement = conn.prepareStatement("SELECT * FROM comments WHERE itemid = " + itemID);
				
				ResultSet rs = statement.executeQuery();

				while(rs.next()){
					long fm = 0;
					try{
						fm=rs.getLong("from_user_id");
					}
					catch(Exception e){ fm =0; }
					
					if(fm>0){
					
					Comment currentComment = new Comment(rs.getLong("KEY"),
							rs.getLong("from_user_id"),
							rs.getLong("to_user_id"),
							rs.getLong("itemid"),
							rs.getInt("rating"),
							new Date(rs.getLong("date")),
							rs.getString("comment"));
					comments.add(currentComment);
					}
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("CassandraDB (getComments): Could not get the comments");
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
					String itemIDnums="'0'";
					
					for(int i=0; i<itemIDs.size(); i++){
						itemIDnums=itemIDnums+",'"+itemIDs.get(i)+"'";
					}

					PreparedStatement statement = conn.prepareStatement("SELECT * FROM comments " + "WHERE itemid IN " + itemIDnums);
					ResultSet rs = statement.executeQuery();

					// Read the user's details

					while(rs.next()){
						
						Comment currentComment = new Comment(rs.getLong("KEY"),
								rs.getLong("from_user_id"),
								rs.getLong("to_user_id"),
								rs.getLong("itemid"),
								rs.getInt("rating"),
								new Date(rs.getLong("date")),
								rs.getString("content"));
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
					System.out.println("CassandraDB (getComments): Could not get the comments");
					e.printStackTrace();
				}

				this.closeConnection(conn);
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);

		return comments;
	}

	public long insertQuestion(long fromUserID, long toUserID, long itemID, Date date, String question){
		long questionID = -1;
		if(fromUserID<1 || toUserID<1|| itemID<1) return questionID;
		if(date==null || question==null) return questionID;
		int attemptsRemaining = 5;
	
		do{
			Connection conn = this.getConnection();
			if(conn != null){
				try{
					question.replace('?', ' ');
					// MAKE KEY
					long key = System.currentTimeMillis()*shortback + System.nanoTime()%shortback;

					PreparedStatement statement = conn.prepareStatement("INSERT INTO questions (KEY,from_user_id, to_user_id, itemid, is_question, date, content, responseTo) "
											+ "VALUES ("+key+","+fromUserID+","+toUserID+","+itemID+","+Boolean.TRUE+","+date.getTime()+",'"+ safe(question)+"', -1)");
					
					statement.executeUpdate();
					statement.close();
					
					if(strict){
						//TODO: make sure the comment was inserted
					}
					
					questionID = key;
					
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
					System.err.println("CassandraDB (insertQuestion): Could not insert question");
					e.printStackTrace();
					
					//TODO: remove bad info
					questionID = -1;
				}
				
				this.closeConnection(conn);
			}
		
			attemptsRemaining--;
		}while(attemptsRemaining > 0);
		
		return questionID;
	}

	@Override
	public long insertAnswer(long userID, long toUserID, long itemID, long questionID, Date date, String answer){
		long answerID = -1;
		if(userID<1 ||toUserID<1 ||itemID<1 ||questionID<1) return answerID;
		if(date==null || answer==null) return answerID;
		
		int attemptsRemaining = SQL_RETRIES;
	
		do{
			Connection conn = this.getConnection();
			if(conn != null){
				try{
					// MAKE KEY
					long key = System.currentTimeMillis()*shortback + System.nanoTime()%shortback;
					PreparedStatement statement = conn.prepareStatement("INSERT INTO questions (KEY,from_user_id, to_user_id, itemid, is_question, date, content, responseTo) "
											+ "VALUES ("+key+","+userID+","+userID+","+itemID+","+Boolean.FALSE+","+date.getTime()+","+ org.apache.commons.lang.StringEscapeUtils.escapeSql(answer)+","+questionID+")");
					
					statement.executeUpdate();
					statement.close();
					
					if(strict){
						//TODO: make sure the comment was inserted
					}
					
					questionID = key;
					
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
					System.err.println("CassandraDB (insertAnswer): Could not insert answer");
					e.printStackTrace();
					
					questionID = -1;
				}
				
				this.closeConnection(conn);
			}
		
			attemptsRemaining--;
		}while(attemptsRemaining > 0);
		
		return questionID;
	}

	
	public Question getQuestion(long questionID) throws Exception {
		Question question = null;
		if(questionID<1) return question;
		
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement statement;
				
				statement = conn.prepareStatement("SELECT * FROM questions " +
							"WHERE KEY = " + questionID );
				
				ResultSet rs = statement.executeQuery();

				if(rs.next()){
					boolean isQuestion;
					Boolean iq = null;
					try{
					 iq = rs.getBoolean("is_question");
					}
					catch(Exception e){iq=null; }
					
					if(iq!=null){
					if(iq)
						isQuestion = true;
					else
						isQuestion = false;
							
					question = new Question(rs.getLong("KEY"),
							rs.getLong("from_user_id"),
							rs.getLong("to_user_id"),
							rs.getLong("itemid"),
							isQuestion,
							rs.getLong("responseTo"),
							new Date(rs.getLong("date")),
							rs.getString("content"));
					}
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("CassandraDB (getQuesion): Could not get the question");
				e.printStackTrace();
				throw e;
			}
			
			this.closeConnection(conn);
		}
		return question;
	}

	
	public ArrayList<Question> getQuestions(long itemID) throws Exception {
		ArrayList<Question> questions = new ArrayList<Question>();
		if(itemID<1) return questions;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement statement;
				
				statement = conn.prepareStatement("SELECT * FROM questions " +
							"WHERE itemid = " + itemID);
				
				ResultSet rs = statement.executeQuery();

				while(rs.next()){
					boolean isQuestion;
					Boolean iq = null;
					
					try{ iq = rs.getBoolean("is_question");}
					catch(Exception e){ iq=null; }
					
					if(iq!=null){
					if(iq)
						isQuestion = true;
					else
						isQuestion = false;
					
					Question currentQuestion = new Question(rs.getLong("KEY"),
							rs.getLong("from_user_id"),
							rs.getLong("to_user_id"),
							rs.getLong("itemid"),
							isQuestion,
							rs.getLong("responseTo"),
							new Date(rs.getLong("date")),
							rs.getString("content"));
					questions.add(currentQuestion);
					}
				}
				
				rs.close();
				statement.close();
			}
			catch(Exception e){
				System.err.println("CassandraDB (getQuestions): Could not get the questions");
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
					//String itemIDnums="'0'";
					
					for(int i=0; i<itemIDs.size(); i++){
						//itemIDnums=itemIDnums+",'"+itemIDs.get(i)+"'";
					
						// I WISH CASSANDRA WOULD FIX THIS!!! ARRRRR!!!!!
					PreparedStatement statement = conn.prepareStatement("SELECT * FROM questions " + "WHERE itemid="+itemIDs.get(i));
					//PreparedStatement statement = conn.prepareStatement("SELECT * FROM questions " + "WHERE itemid=5 OR itemid=9");
					
					ResultSet rs = statement.executeQuery();

					while(rs.next()){
						boolean isQuestion;
						Boolean iq = null;
						
						try{ iq = rs.getBoolean("is_question");}
						catch(Exception e){ iq=null; }
						
						if(iq!=null){
						if(iq)
							isQuestion = true;
						else
							isQuestion = false;
						
						Question currentQuestion = new Question(rs.getLong("KEY"),
								rs.getLong("from_user_id"),
								rs.getLong("to_user_id"),
								rs.getLong("itemid"),
								isQuestion,
								rs.getLong("responseTo"),
								new Date(rs.getLong("date")),
								rs.getString("content"));
						questions.add(currentQuestion);
						}
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
					System.out.println("CassandraDB (getQuestions): Could not get the questions");
					e.printStackTrace();
				}

				this.closeConnection(conn);
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);

		return questions;
	}
	
	//Jing :)
		public ArrayList<VideoItem> GetAllVideos() {
			
			ArrayList<VideoItem> allVideoItem = new ArrayList<VideoItem>();
			
			return allVideoItem;	
		}
		

		
		public int getMaxVideoID(){
			int maxID = -1;
			
			
			return maxID;
		}
		
		
		public synchronized int insertVideo( String name, String description,int userID) {
			
			int attemptsRemaining = 5;
			int videoID = -1;
			
			
			return videoID;
		}

		public ArrayList<Category> getCategories(long parent, long timestamp) throws Exception{
			ArrayList<Category> categories = new ArrayList<Category>();
			if(parent < 0) return categories;
			int attemptsRemaining = SQL_RETRIES;

			do {
				Connection conn = this.getConnection();

				if (conn != null) {
					PreparedStatement statement = null;
					
					try {
						statement = conn.prepareStatement("SELECT * FROM categories WHERE parent = "+parent+" AND ts > " + timestamp);
						ResultSet rs = statement.executeQuery();
						
						// Put all of the states in to the results vector
						while(rs.next()){
							String name = null;
							try{
								name=rs.getString("name");
							} catch(Exception e){}
							
							if(name!=null){
								Category category = new Category(rs.getLong("KEY"),
										name,
										rs.getLong("parent"),
										rs.getTimestamp("ts").getTime());
								
								categories.add(category);
							}
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
						System.out.println("CassandraDB (getCategories): Could not get the categories");
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

		public long getUserCount(){
			long count =0;

			int attemptsRemaining = SQL_RETRIES;

			do {
				Connection conn = this.getConnection();

				if (conn != null) {
					PreparedStatement statement = null;

					try {
						statement = conn.prepareStatement("SELECT count(*) FROM users");
						
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
						System.out.println("CassandraDB (getAllUserDate): Could not read results set");
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
		
		/*
		 * (non-Javadoc)
		 * @see com.cmart.DB.DBQuery#getUsers(java.util.ArrayList)
		 */
		public ArrayList<User> getUsers(ArrayList<Long> sellerIDs){
			ArrayList<User> sellers = new ArrayList<User>();
			int attemptsRemaining = SQL_RETRIES;

			if(sellerIDs == null || sellerIDs.size()==0) return sellers;
			
			do {
				Connection conn = this.getConnection();

				if (conn != null) {
					try {
						StringBuilder ids = new StringBuilder();
						
						// Make sure things are longs
						ArrayList<Long> parsedIDs = new ArrayList<Long>();
						Object[] sellerIDsNoDup = new HashSet<Object>(Arrays.asList(sellerIDs.toArray())).toArray();
						
						for(int i=0; i<sellerIDsNoDup.length; i++){
							try{
								Long temp = (Long) sellerIDsNoDup[i];
								if(temp!=null) parsedIDs.add(temp);
							}
							catch(Exception e){
								// not longs
							}
						}
						
						// If there are users to get, get them
						if (parsedIDs.size() > 0) {
							for (int i = 0; i < parsedIDs.size() - 1; i++) {
								ids.append(" ? , ");
							}
							ids.append("?");
							
							PreparedStatement statement = conn.prepareStatement("SELECT KEY,username,rating FROM users WHERE KEY IN (" + ids.toString() + ")");
							
							// Set the user ids
							for(int i=0; i<parsedIDs.size(); i++){
								statement.setLong(i+1, parsedIDs.get(i));
							}
							
							ResultSet rs = statement.executeQuery();
							
							// Read the user's details
							while(rs.next()){
								String rating = null;
								try{
									rating = rs.getString("rating");
								}
								catch(Exception e){}
								
								if(rating !=null){
									User user = new User(rs.getLong("KEY"), rs.getString("username"), rating);
									sellers.add(user);
								}
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
						System.out.println("CassandraDB (getSellers): Could not get the seller information");
						e.printStackTrace();
					}

					this.closeConnection(conn);
				}

				attemptsRemaining--;
			} while (attemptsRemaining >= 0);

			return sellers;
		}
		
		
		public ArrayList<Long> getTextItemsIDsDB(String text, int page,
				int itemsPP, int sortCol, Boolean sortDec) throws Exception {

			System.out.println("Cassandra does not support this, use Solr");
			
			return new ArrayList<Long>();
		}
		
		public static void main(String[] args){
			GlobalVars.getInstance();
			DBQuery db = GlobalVars.DB;
			
			// WARNING WILL DELETE DB OR CONTENTS
			if(db instanceof CassandraDBQuery){
				CassandraDBQuery temp = (CassandraDBQuery)db;
				temp.populateDB(null);
			}		
		}
		
		public void populateDB(Connection conn){
			boolean createSpace = true;
			boolean truncateData = false;
			boolean doUsers = false;
			boolean doAddresses = false;
			boolean doItems = false;
			boolean doOldItems = false;
			boolean doImages = false;
			boolean doPurchases = false;
			boolean doPayments = false;
			boolean doBids = false;
			boolean doOldBids = false;
			boolean doCategories = false;
			boolean doComments = false;
			boolean doQuestions = false;
			boolean doAccounts = false;
			boolean doStates = false;
			boolean makeAll = false;
			
			/*try {
				Class.forName(DRIVER);
				conn = DriverManager.getConnection(URL);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			String url = getURL();
			
			PreparedStatement statement=null;
			ResultSet rs=null;
			String CQL =null;
			
			if(createSpace){
				try {
					url = url.substring(0, url.indexOf("CMARTv1")-1);
					if(conn==null) conn = this.getConnection();
					PreparedStatement  statementpre = conn.prepareStatement("DROP KEYSPACE CMARTv1;");
					statementpre.executeUpdate();
					
					Thread.sleep(2000); // may have to wait to agree
					statementpre.close();
				} catch (Exception e) {	e.printStackTrace(); }
				
				try {
					PreparedStatement  statementpre = conn.prepareStatement("create keyspace CMARTv1 WITH gc_grace_seconds=30 AND strategy_options:replication_factor=3 AND strategy_class = 'SimpleStrategy' AND durable_writes=false;");
					//statementpre = conn.prepareStatement("create keyspace CMARTv11 WITH gc_grace_seconds=30 AND strategy_options:DC1 = '2' AND replication_factor = '2' AND strategy_class = 'NetworkTopologyStrategy';");
					
					statementpre.executeUpdate();
					Thread.sleep(2000);
					statementpre.close();
				} catch (Exception e) {	e.printStackTrace(); }
				finally{				
					this.forceCloseConnection(conn);
					url = url + "/CMARTv1";
					System.out.println(url);
					conn = null;
				}
			}
			
			if(conn==null) conn = this.getConnection();
			
			if (truncateData) {
				for(int i=0; i<5; i++){
					try {
						conn.close();
					} catch (SQLException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					try {
						Class.forName(DRIVER);
						conn = DriverManager.getConnection(url);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				try {
					statement = conn.prepareStatement("TRUNCATE items");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }
				
				try {
					statement = conn.prepareStatement("TRUNCATE olditems");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }
				
				try {
					statement = conn.prepareStatement("TRUNCATE purchased");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }
				
				try {
					statement = conn.prepareStatement("TRUNCATE users");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }
				
				try {
					statement = conn.prepareStatement("TRUNCATE addresses");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }
				
				try {
					statement = conn.prepareStatement("TRUNCATE bids");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }
				
				
				try {
					statement = conn.prepareStatement("TRUNCATE olditems");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }
				
				try {
					statement = conn.prepareStatement("TRUNCATE maxbids");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }
				
				try {
					statement = conn.prepareStatement("TRUNCATE maxoldbids");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }
				
				try {
					statement = conn.prepareStatement("TRUNCATE comments");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }
				
				try {
					statement = conn.prepareStatement("TRUNCATE images");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }

				try {
					statement = conn.prepareStatement("TRUNCATE questions");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }
				
				try {
					statement = conn.prepareStatement("TRUNCATE payments");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }

				try {
					statement = conn.prepareStatement("TRUNCATE priceitems");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }
				
				try {
					statement = conn.prepareStatement("TRUNCATE revpriceitems");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }
				
				try {
					statement = conn.prepareStatement("TRUNCATE revtimeitems");
					statement.executeUpdate();
				} catch (Exception e) {	e.printStackTrace(); }
				
				System.out.println("success");
				}
			}
			
			/*
			 * Create the users col set
			 */
			if (doUsers || makeAll) {
				System.out.println("Dropping users");
				try {
					CQL = "DROP COLUMNFAMILY users;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Dropping users successful");
				} catch (Exception e) {
					System.err
							.println("CassandraQuery: INIT no columnfamily users.");
					e.printStackTrace();
				}

				System.out.println("Adding users");
				try {
					CQL = "CREATE COLUMNFAMILY users (KEY bigint PRIMARY KEY, userid bigint, username text, password text, username_password text, authtoken text, firstname text,lastname text, email text, rating bigint) ;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					// Used to confirm username and password
					CQL = "CREATE INDEX users_username_password_idx ON users (username_password); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					// Used to check authtoken
					CQL = "CREATE INDEX users_userid_idx ON users (userid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					// Used to check if username exists
					CQL = "CREATE INDEX users_username_idx ON users (username); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					// Used to check if email exists
					CQL = "CREATE INDEX users_email_idx ON users (email); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					// ??
					CQL = "CREATE INDEX users_authtoken_idx ON users (authtoken); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Adding users successful");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			/*
			 * Creating the addresses col set
			 */
			if (doAddresses || makeAll) {
				System.out.println("Dropping addresses");
				try {
					CQL = "DROP COLUMNFAMILY addresses;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Dropping addresses successful");
				} catch (Exception e) {
					System.err
							.println("CassandraQuery: INIT no columnfamily addresses.");
					e.printStackTrace();
				}
				System.out.println("Adding addresses");
				try {
					CQL = "CREATE COLUMNFAMILY addresses (KEY bigint PRIMARY KEY, userid bigint, street text, town text, zip text, state text, isdefault boolean, isDefaultKey bigint);";
					statement = conn.prepareStatement(CQL);
					statement.execute();

					// Used to get the default address
					CQL = "CREATE INDEX address_default_idx ON addresses (isDefaultKey); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					// Used to get all of a user's addresses
					CQL = "CREATE INDEX address_userid_idx ON addresses (userid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Adding addresses successful");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if(doItems || makeAll){
				System.out.println("Dropping items");
				try {
					CQL = "DROP COLUMNFAMILY items;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					CQL = "DROP COLUMNFAMILY revtimeitems;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					CQL = "DROP COLUMNFAMILY priceitems;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					CQL = "DROP COLUMNFAMILY revpriceitems;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					System.out.println("Dropping items successful");
				} catch (Exception e) {
					System.err.println("CassandraQuery: INIT no columnfamily items.");
					e.printStackTrace();
				}
				System.out.println("Adding items");
				try {
					
					CQL="CREATE COLUMNFAMILY items (KEY bigint PRIMARY KEY, itemid bigint, name text, description text, thumbnail text, sellerid bigint, categoryid bigint, currentwinner bigint, quantity bigint, noofbids bigint, startdate bigint, enddate bigint, curbid text, maxbid text, startprice text,reserveprice text,buynowprice text, pikey bigint, rpikey bigint, ts bigint) WITH gc_grace_seconds=15 ; ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					CQL="CREATE COLUMNFAMILY revtimeitems (KEY bigint PRIMARY KEY, enddate bigint, catzero bigint, itemid bigint, categoryid bigint) WITH gc_grace_seconds=15 ; ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					CQL="CREATE COLUMNFAMILY priceitems (KEY bigint PRIMARY KEY, pikey bigint, catzero bigint, itemid bigint, categoryid bigint) WITH gc_grace_seconds=15 ; ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					CQL="CREATE COLUMNFAMILY revpriceitems (KEY bigint PRIMARY KEY, pikey bigint, catzero bigint, itemid bigint, categoryid bigint) WITH gc_grace_seconds=15 ; ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					// Used to get the items the user is selling
					CQL = "CREATE INDEX item_sellerid_idx ON items (sellerid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					// Used to browse items
					CQL = "CREATE INDEX item_itemid_idx ON items (itemid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					// Used to list items by categoryID
					CQL = "CREATE INDEX item_categoryid_idx ON items (categoryid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					CQL = "CREATE INDEX revtimeitem_categoryid_idx ON revtimeitems (categoryid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					CQL = "CREATE INDEX revtimeitem_categoryz_idx ON revtimeitems (catzero); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					CQL = "CREATE INDEX priceitem_categoryid_idx ON priceitems (categoryid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					CQL = "CREATE INDEX revpriceitem_categoryid_idx ON revpriceitems (categoryid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					CQL = "CREATE INDEX priceitem_categoryz_idx ON priceitems (catzero); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					CQL = "CREATE INDEX revpriceitem_categoryz_idx ON revpriceitems (catzero); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					System.out.println("Adding items successful");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(doOldItems || makeAll){
				System.out.println("Dropping old items");
				try {
					CQL = "DROP COLUMNFAMILY olditems;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Dropping old items successful");
				} catch (Exception e) {
					System.err.println("CassandraQuery: INIT no columnfamily olditems.");
					e.printStackTrace();
				}
				System.out.println("Adding old items");
				try {
					CQL="CREATE COLUMNFAMILY olditems (KEY bigint PRIMARY KEY, itemid bigint, name text, description text, thumbnail text, sellerid bigint, categoryid bigint, currentwinner bigint, quantity bigint, noofbids bigint, startdate bigint, enddate bigint, curbid text, maxbid text, startprice text,reserveprice text,buynowprice text, ts bigint); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					// Used to get the items the user has sold
					CQL = "CREATE INDEX olditem_sellerid_idx ON olditems (sellerid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					// Used to browse items
					CQL = "CREATE INDEX olditem_itemid_idx ON olditems (itemid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					System.out.println("Adding olditems successful");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			
			
			if(doImages || makeAll){
				System.out.println("Dropping images");
				try {
					CQL = "DROP COLUMNFAMILY images;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Dropping images successful");
				} catch (Exception e) {
					System.err.println("CassandraQuery: INIT no columnfamily images.");
					e.printStackTrace();
				}
				System.out.println("Adding images");
				try {
					CQL="CREATE COLUMNFAMILY images (KEY bigint PRIMARY KEY, URL text, description text, itemid bigint, position bigint) ; ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					// Used to get the items the user has sold
					CQL = "CREATE INDEX images_itemid_idx ON images (itemid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					System.out.println("Adding images successful");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(doPurchases || makeAll){
				System.out.println("Dropping purchases");
				try {
					CQL="DROP COLUMNFAMILY purchased;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Dropping images successful");
				} catch (Exception e) {
					System.err.println("CassandraQuery: INIT no columnfamily purchases.");
					e.printStackTrace();
				}
				System.out.println("Adding purchases");
				try {
					CQL="CREATE COLUMNFAMILY purchased (KEY bigint PRIMARY KEY, userid bigint, itemid bigint, purcashedquantity int, price text, purchasedate bigint, paid boolean, paiddate bigint, name text, description text, thumbnail text, sellerid bigint, categoryid bigint, currentwinner bigint, quantity bigint, noofbids bigint, startdate bigint, enddate bigint, curbid text, maxbid text, ts bigint); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					// Used to get the items a user has purchased
					CQL = "CREATE INDEX purchases_userid_idx ON purchased (userid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					System.out.println("Adding purchases successful");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(doPayments || makeAll){
				System.out.println("Dropping payments");
				try {
					CQL="DROP COLUMNFAMILY payments;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Dropping payments successful");
				} catch (Exception e) {
					System.err.println("CassandraQuery: INIT no columnfamily payments.");
					e.printStackTrace();
				}
				System.out.println("Adding payments");
				try {
					CQL="CREATE COLUMNFAMILY payments (KEY bigint PRIMARY KEY, userid bigint, itemid bigint, quantity int, price text, paiddate bigint, street text, town text, zip text, state text, nameoncard text, creditcardno text, cvv text, expirationdate bigint); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					// Used to get the items a user has purchased
					CQL = "CREATE INDEX payments_userid_idx ON payments (userid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					System.out.println("Adding payments successful");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(doBids || makeAll){
				System.out.println("Dropping bids");
				try {
					CQL="DROP COLUMNFAMILY bids;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Dropping bids successful");
				} catch (Exception e) {
					System.err.println("CassandraQuery: INIT no columnfamily bids.");
					e.printStackTrace();
				}
				try {
					CQL="DROP COLUMNFAMILY maxbids;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Dropping maxbids successful");
				} catch (Exception e) {
					System.err.println("CassandraQuery: INIT no columnfamily maxbids.");
					e.printStackTrace();
				}
				System.out.println("Adding bids");
				try {
					CQL="CREATE COLUMNFAMILY bids (KEY bigint PRIMARY KEY, userid bigint, itemid bigint, quantity int, bid text, maxbid text, biddate bigint) WITH gc_grace_seconds=15 ; ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					CQL="CREATE COLUMNFAMILY maxbids (KEY text PRIMARY KEY, bidkey bigint, userid bigint, itemid bigint, quantity int, bid text, maxbid text, biddate bigint, ts bigint) WITH comparator = UTF8Type AND gc_grace_seconds=15 ; ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					// Used to get the items a user has bid on
					CQL = "CREATE INDEX maxbids_userid_idx ON maxbids (userid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					// Used to get all max bids on an item when purchasing
					CQL = "CREATE INDEX maxbids_itemid_idx ON maxbids (itemid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					// Used to get the items a user has bid on??? <-old
					CQL = "CREATE INDEX bids_userid_idx ON bids (userid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					// Used to get all bids when purchasing item
					CQL = "CREATE INDEX bids_itemid_idx ON bids (itemid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					System.out.println("Adding bids successful");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(doOldBids || makeAll){
				System.out.println("Dropping old bids");
				try {
					CQL="DROP COLUMNFAMILY oldbids;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Dropping oldbids successful");
				} catch (Exception e) {
					System.err.println("CassandraQuery: INIT no columnfamily oldbids.");
					e.printStackTrace();
				}
				try {
					CQL="DROP COLUMNFAMILY maxoldbids;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Dropping maxoldbids successful");
				} catch (Exception e) {
					System.err.println("CassandraQuery: INIT no columnfamily maxbids.");
					e.printStackTrace();
				}
				System.out.println("Adding oldbids");
				try {
					CQL="CREATE COLUMNFAMILY oldbids (KEY bigint PRIMARY KEY, userid bigint, itemid bigint, quantity int, bid text, maxbid text, biddate bigint); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					CQL="CREATE COLUMNFAMILY maxoldbids (KEY text PRIMARY KEY, bidkey bigint, userid bigint, itemid bigint, quantity int, bid text, maxbid text, biddate bigint, ts bigint) WITH comparator = UTF8Type; ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					// Used to get the items a user has bid on
					CQL = "CREATE INDEX maxoldbids_userid_idx ON maxoldbids (userid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					// Used to get all max bids on an item
					CQL = "CREATE INDEX maxoldbids_itemid_idx ON maxoldbids (itemid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					// Used to get the items a user has purchased
					CQL = "CREATE INDEX oldbids_userid_idx ON oldbids (userid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					System.out.println("Adding oldbids successful");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(doCategories || makeAll){
				System.out.println("Dropping categories");
				try {
					CQL="DROP COLUMNFAMILY categories;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Dropping categories successful");
				} catch (Exception e) {
					System.err.println("CassandraQuery: INIT no columnfamily categories.");
					e.printStackTrace();
				}
				System.out.println("Adding categories");
				try {
					CQL="CREATE COLUMNFAMILY categories (KEY bigint PRIMARY KEY,parent bigint, name text, ts bigint); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					// Used to get the items a user has purchased
					CQL = "CREATE INDEX categories_parent_idx ON categories (parent); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					System.out.println("Adding categories successful");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(doComments || makeAll){
				System.out.println("Dropping comments");
				try {
					CQL="DROP COLUMNFAMILY comments;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Dropping comments successful");
				} catch (Exception e) {
					System.err.println("CassandraQuery: INIT no columnfamily comments.");
					e.printStackTrace();
				}
				System.out.println("Adding comments");
				try {
					CQL="CREATE COLUMNFAMILY comments (KEY bigint PRIMARY KEY,from_user_id bigint, to_user_id bigint, itemid bigint, rating bigint, date bigint, comment text); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					// Used to get the items a user has purchased
					CQL = "CREATE INDEX comments_itemid_idx ON comments (itemid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					System.out.println("Adding comments successful");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(doQuestions || makeAll){
				System.out.println("Dropping questions");
				try {
					CQL="DROP COLUMNFAMILY questions;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Dropping questions successful");
				} catch (Exception e) {
					System.err.println("CassandraQuery: INIT no columnfamily questions.");
					e.printStackTrace();
				}
				System.out.println("Adding questions");
				try {
					CQL="CREATE COLUMNFAMILY questions (KEY bigint PRIMARY KEY,from_user_id bigint, to_user_id bigint, itemid bigint, is_question boolean, date bigint, content text, responseTo bigint); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					// Used to get the items a user has purchased
					CQL = "CREATE INDEX questions_itemid_idx ON questions (itemid); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					System.out.println("Adding questions successful");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(doAccounts || makeAll){
				System.out.println("Dropping accounts");
				try {
					CQL="DROP COLUMNFAMILY accounts;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Dropping accounts successful");
				} catch (Exception e) {
					System.err.println("CassandraQuery: INIT no columnfamily accounts.");
					e.printStackTrace();
				}
				System.out.println("Adding accounts");
				try {
					CQL="CREATE COLUMNFAMILY accounts (KEY bigint PRIMARY KEY,name text, nameoncard text, creditcardno text, cvv text, expirationdate bigint); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					
					System.out.println("Adding accounts successful");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(doStates || makeAll){
				System.out.println("Dropping states");
				try {
					CQL="DROP COLUMNFAMILY states;";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();

					System.out.println("Dropping states successful");
				} catch (Exception e) {
					System.err.println("CassandraQuery: INIT no columnfamily states.");
					e.printStackTrace();
				}
				System.out.println("Adding states");
				try {
					CQL="CREATE COLUMNFAMILY states (KEY bigint PRIMARY KEY,shortname text, longname text); ";
					statement = conn.prepareStatement(CQL);
					statement.executeUpdate();
					
					System.out.println("Adding states successful");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			try {
				if(statement != null && !statement.isClosed()) statement.close();
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		/*
		 * TODO: social network
		 * (non-Javadoc)
		 * @see com.cmart.DB.DBQuery#insertFriendRequest(long, java.lang.String, java.lang.String)
		 */
		
		@Override
		public boolean insertFriendRequest(long fromID, String toName,
				String message) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public ArrayList<Message> getMessages(long toID) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getFromName(long fromID) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ArrayList<WallPost> getWallPosts(long toID) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ArrayList<WallPost> getReplies(long replyID) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ArrayList<WallPost> getNewsFeeds(long userID) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void confirmFriendRequest(long acceptID) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void rejectFriendRequest(long rejectID) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean insertMessage(long fromID, long toID, String text) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public long getUserID(String user) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public ArrayList<FriendRequest> getFriendRequests(long userID) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean insertWallPost(long fromID, long toID, String text) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean insertReply(long formID, long replyID, String post) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean hasRequestID(long id) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean hasWallpostsToID(long toID) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean hasWallpostsReplyID(long replyID) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean areFriends(long userID, long toID) {
			// TODO Auto-generated method stub
			return false;
		}
}
