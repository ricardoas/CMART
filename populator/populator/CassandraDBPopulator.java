package populator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLTransientConnectionException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;

public class CassandraDBPopulator extends DBPopulator{
	private Stack<Connection> conns;
	boolean batch = true;
	private int maxConnsCache = 40;
	private static long shortback = 1000000l;
	private static long tenzero = 10000000000l;
	private DecimalFormat df = new DecimalFormat("#.##");
	
	
	/**
	 * Create this object
	 */
	public CassandraDBPopulator(){
		super(CreateAll.CASS_DATABASE_URL, CreateAll.CASS_DATABASE_DRIVER, null, null);
		conns = new Stack<Connection>();
	}
	
	/*
	 * Get the number of images for an item
	 * (non-Javadoc)
	 * @see populator.DBPopulator#getNoOfImages(long)
	 */
	public int getNoOfImages(long itemID){
		ArrayList<Image> images = new ArrayList<Image>();
		if(itemID < 1) return 0;
		int attemptsRemaining = 5;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement getImagesStatement = null;
				itemID = convertItemID(itemID);
				
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
					returnConn(conn);
				} catch (MySQLNonTransientConnectionException e) {
					returnConn(conn);
				} catch (Exception e) {
					System.out.println("CassandraDPopulator (getNoOfImages): Could not get the images");
					e.printStackTrace();
				} finally{
					close(getImagesStatement);
					returnConn(conn);
				}		
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		// Count the images
		return images.size();
	}
	
	/**
	 * Get a new connection to the database
	 * @return
	 */
	private Connection initConnection(){
		try {
			Class.forName(this.getDriver());
			Connection conn = DriverManager.getConnection(this.getURL());
			
			return conn;	
		}
		catch(Exception e){
			System.err.println("CassandraQuery (initConnection): Could not open a new database connection to " + this.getURL());
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get a connection from the connection pool, or a new connection
	 */
	public Connection getConnection(){
		Connection conn = null;
		
		synchronized(conns){	
			if(!conns.isEmpty()){
				conn = conns.pop();
			}
		}
		
		if(conn != null)
			return conn;
		else
			return initConnection();
		
	}
	
	/*
	 * The Statement to start a statement, currently added on the start
	 * (non-Javadoc)
	 * @see populator.DBPopulator#startSQL()
	 */
	public StringBuffer startSQL(){
		StringBuffer ret = new StringBuffer();
		//ret.append("BEGIN BATCH\n");
		return ret;
	}
	
	/*
	 * Execute the batch sql in the statement
	 * (non-Javadoc)
	 * @see populator.DBPopulator#executeUpdateSQL(java.lang.String)
	 */
	public void executeUpdateSQL(String sql) {
		Connection conn = this.getConnection();
		int tries = 20;

		if (conn != null && !sql.equals("")) {
			PreparedStatement go = null;
			sql = "BEGIN BATCH\n" +sql+ "APPLY BATCH;";

			do {
				try {
					go = conn.prepareStatement(sql);
					go.executeUpdate();
					go.close();
					tries = 0;
				} catch (SQLTransientConnectionException e) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} catch (Exception e) {
					System.err
							.println("CassandraPopulator (executeUpdateSQL): Could not execute");
					e.printStackTrace();
					System.err.println("sql: " + sql);
				} finally {
					try {
						go.close();
					} catch (Exception e) {
					}
				}

				tries--;
			} while (tries > 0);
		} else {
			//System.err.println("CassandraDBPopulator (executeSQL): Could not execute");
		}

		returnConn(conn);
	}
	
	/**
	 * Close a connection
	 * @param conn
	 */
	private void closeConnection(Connection conn){
		if(conn != null){
				// close the connection
				try{
					conn.close();
					conn = null;
				}
				catch(Exception e){
					System.err.println("CassandraDBPopulator (closeConnection): Could not close database connection");
					e.printStackTrace();
				}	
		}
	}
	
	/**
	 * Return a connection to the connection pool, or close it if it's full
	 * @param conn
	 */
	private void returnConn(Connection conn){
		if(conn != null)
			synchronized(conns){
				// Close it with come probability to stop any possible memory leaks
				if(Math.random()>0.95)
					closeConnection(conn);
				else if(conns.size() <  maxConnsCache)
					conns.push(conn);
				else
					closeConnection(conn);
			}
	}
	
	/*
	 * Close all of the connections to the database
	 * (non-Javadoc)
	 * @see populator.DBPopulator#closeConnections()
	 */
	public void closeConnections(){
		synchronized(conns){
			for(Connection conn : conns)
				if(conn != null) closeConnection(conn);
		}
	}
	
	/**
	 * Close a prepared statement
	 * @param ps
	 */
	private void close(PreparedStatement ps){
		if(ps!=null)
			try{
				if(ps!=null){
					ps.close();
					ps = null;
				}
			}
			catch(Exception e){}
	}
	
	/**
	 * Close a result set
	 * @param rs
	 */
	private void close(ResultSet rs){
		if(rs!=null)
			try{
				if(rs!=null){
					rs.close();
					rs = null;
				}
			}
			catch(Exception e){}
	}
	
	/**
	 * drop and add the users' accounts
	 * @return
	 */
	public boolean dropAddAccounts(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop = null;
			PreparedStatement add = null;
			
			try{
				String CQL;
				
				try{
					CQL="DROP COLUMNFAMILY accounts;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				
				CQL="CREATE COLUMNFAMILY accounts (KEY bigint PRIMARY KEY,name text, nameoncard text, creditcardno text, cvv text, expirationdate bigint); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (dropAddAccounts): Could not delete the accounts");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(add);
				returnConn(conn);
			}
		}
		else{
			System.err.println("CassandraDBPopulator (dropAddUsers): The database connection is not open");
		}
		
		return deleted;
	}
	
	/*
	 * Drop and add the users
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddUsers()
	 */
	public boolean dropAddUsers(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop = null;
			PreparedStatement add = null;
			
			try{
				this.dropAddAccounts();
				String CQL;
				try{
					CQL = "DROP COLUMNFAMILY users;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				
				CQL = "CREATE COLUMNFAMILY users (KEY bigint PRIMARY KEY, userid bigint, username text, password text, username_password text, authtoken text, firstname text,lastname text, email text, rating bigint) ;";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to confirm username and password
				CQL = "CREATE INDEX users_username_password_idx ON users (username_password); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to check authtoken
				CQL = "CREATE INDEX users_userid_idx ON users (userid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);
				
				// Used to check if username exists
				CQL = "CREATE INDEX users_username_idx ON users (username); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to check if email exists
				CQL = "CREATE INDEX users_email_idx ON users (email); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);
				
				// ??
				CQL = "CREATE INDEX users_authtoken_idx ON users (authtoken); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (dropAddUsers): Could not delete the users");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(add);
				returnConn(conn);
			}
		}
		else{
			System.err.println("CassandraDBPopulator (dropAddUsers): The database connection is not open");
		}
		
		return deleted;
	}
	
	/*
	 * Drop and add the questions
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddQuestions()
	 */
	public boolean dropAddQuestions(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop  =null;
			PreparedStatement add=null;
			
			try{
				String CQL;
				try{
					CQL="DROP COLUMNFAMILY questions;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				
				CQL="CREATE COLUMNFAMILY questions (KEY bigint PRIMARY KEY,from_user_id bigint, to_user_id bigint, itemid bigint, is_question boolean, date bigint, content text, responseTo bigint); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to get the items a user has purchased
				CQL = "CREATE INDEX questions_itemid_idx ON questions (itemid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (dropAddQuestions): Could not delete the questions");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(add);
				returnConn(conn);
			}
		}
		else{
			System.err.println("CassandraDBPopulator (dropAddQuestions): The database connection is not open");
		}
		
		return deleted;
	}
	
	/*
	 * Drop the comments family
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddComments()
	 */
	public boolean dropAddComments(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop = null;
			PreparedStatement add = null;
			
			try{
				String CQL;
				try{
					CQL="DROP COLUMNFAMILY comments;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				} catch(Exception e){}
				
				CQL="CREATE COLUMNFAMILY comments (KEY bigint PRIMARY KEY,from_user_id bigint, to_user_id bigint, itemid bigint, rating bigint, date bigint, comment text); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to get the items a user has purchased
				CQL = "CREATE INDEX comments_itemid_idx ON comments (itemid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (dropAddComments): Could not delete the comments");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(add);
				returnConn(conn);
			}
		}
		else{
			System.err.println("CassandraDBPopulator (dropAddComments): The database connection is not open");
		}
		
		return deleted;
	}
	
	/*
	 * Drop the addresses family
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddAdresses()
	 */
	public boolean dropAddAdresses(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop = null;
			PreparedStatement add = null;
			
			try{
				String CQL;
				try{
					CQL = "DROP COLUMNFAMILY addresses;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				
				CQL = "CREATE COLUMNFAMILY addresses (KEY bigint PRIMARY KEY, userid bigint, street text, town text, zip text, state text, isdefault boolean, isDefaultKey bigint);";
				add = conn.prepareStatement(CQL);
				add.execute();
				close(add);

				// Used to get the default address
				CQL = "CREATE INDEX address_default_idx ON addresses (isDefaultKey); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to get all of a user's addresses
				CQL = "CREATE INDEX address_userid_idx ON addresses (userid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (dropAddAdresses): Could not delete the addresses");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(add);
				returnConn(conn);
			}
		}
		else{
			System.err.println("CassandraDBPopulator (dropAddAdresses): The database connection is not open");
		}
		
		return deleted;
	}
	
	/*
	 * Add the SQL to the SQL buffer to insert the item
	 * (non-Javadoc)
	 * @see populator.DBPopulator#addItem(java.lang.StringBuffer)
	 */
	StringBuffer itemSQL = null;
	Integer itemCount = 0;
	public synchronized void addItem(StringBuffer sql){
		if(itemSQL==null){
			itemSQL = new StringBuffer(2048);
		}
		
		itemSQL.append(sql);
		itemCount++;
		
		// Insert every 200 items
		if(itemCount%20==0){
			this.executeUpdateSQL(itemSQL.toString());
			itemSQL = new StringBuffer(2048);
			
			itemCount=0;
		}
	}
		
	/*
	 * Insert an old item SQL in to the buffer
	 * (non-Javadoc)
	 * @see populator.DBPopulator#addOldItem(java.lang.StringBuffer)
	 */
	StringBuffer oldItemSQL = null;
	Integer oldItemCount = 0;
	public synchronized void addOldItem(StringBuffer sql){
		if(oldItemSQL==null){
			oldItemSQL = new StringBuffer(2048);	
		}
		
		oldItemSQL.append(sql);
		oldItemCount++;
		
		// Insert every 200 items
		if(oldItemCount%20==0){
			this.executeUpdateSQL(oldItemSQL.toString());
			oldItemSQL = new StringBuffer(2048);
			
			oldItemCount=0;
		}
	}
	
	/**
	 * Adds a SQL string to the buffer to insert a user
	 * @param s
	 */
	private synchronized void addToUser(String s){
		synchronized(usersCount){
			if(usersSQL==null){
				usersSQL=new StringBuffer(2048);
				
			}
			
			usersSQL.append(s);	
			usersCount++;
			
			// Insert once we have 100 users buffered
			if(usersCount%10==0){
				this.executeUpdateSQL(usersSQL.toString());
				usersSQL=null;
				usersCount=0;
			}
		}
	}
	
	/*
	 * Insert an address, we will actually buffer it before it is inserted
	 * (non-Javadoc)
	 * @see populator.DBPopulator#insertAddress(populator.Address)
	 */
	public long insertAddress(Address address){
		long addressID = -1;

			try{
				// MAKE KEY - 
				long key = (System.currentTimeMillis()*1000000) + System.nanoTime()%1000000;
				
				// If the address is default, add the userID key as the default address key
				// We can then easily select the default address
				if(address.getIsDefault())
					addToUser("INSERT INTO addresses (KEY, userid, street, town, zip, state, isdefault, isDefaultKey) "
										+ "VALUES ("+key+","+address.getUserID()+",'"+safe(address.getStreet())+"','"+safe(address.getTown())+"','"+address.getZip()+"','"+address.getState()+"',"+address.getIsDefault()+","+address.getUserID()+")\n");

				// Otherwise, insert the address without the default address key
				else addToUser("INSERT INTO addresses (KEY, userid, street, town, zip, state, isdefault) "
						+ "VALUES ("+key+","+address.getUserID()+",'"+safe(address.getStreet())+"','"+safe(address.getTown())+"','"+address.getZip()+"','"+address.getState()+"',"+address.getIsDefault()+")\n");				
				
				addressID = key;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (insertAddress): Could not insert the address");
				e.printStackTrace();
			}

		return addressID;
	}
	
	/**
	 * Get the user's ID
	 * @return
	 */
	long userKey=0l;
	private synchronized long getUserKey(){
		userKey++;
		return userKey;
	}
	
	// To buffer the users
	StringBuffer usersSQL = null;
	Integer usersCount = 0;
	
	/*
	 * Insert a user, we will buffer the CQL before inserting
	 * (non-Javadoc)
	 * @see populator.DBPopulator#insertUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, java.util.Date)
	 */
	public long insertUser(String firstName, String lastName, String username, String password, String email, int rating, Date creationDate){
		long userID = -1;

		try{
			// Create the SQL statement to insert the user
			long key=this.getUserKey();
			userID=key;
				
			addToUser("INSERT INTO users (KEY, firstname, lastname, username, password, email,username_password,rating, authtoken) VALUES ('"+key+"','"+firstName+"','"+lastName+"','"+username+"','"+password+"','"+email+"','"+username+"_"+password+"',0,'NULL')\n");
		}
		catch(Exception e){
			System.err.println("CassandraDBPopulator (insertUser): Could not insert the user");
			e.printStackTrace();
		}

		return userID;
	}
	
	
	/**
	 * Add the CQL for an image we are going to insert
	 * @param s
	 */
	StringBuffer imageSQL = null;
	Integer imageCount = 0;
	public synchronized void addToImages(String s){
		synchronized(imageCount){
			if(imageSQL==null){
				imageSQL=new StringBuffer(2048);
			}
			
			imageSQL.append(s);
			imageCount++;
			
			// Insert every 200 images
			if(imageCount%200==0){
				this.executeUpdateSQL(imageSQL.toString());
				imageSQL=null;
				imageCount=0;
			}
		}
	}
	
	/*
	 * Insert an image, we will buffer the CQL before inserting it
	 * (non-Javadoc)
	 * @see populator.DBPopulator#insertImage(long, int, java.lang.String, java.lang.String)
	 */
	public void insertImage(long itemID, int position, String URL, String description){			
		long key = System.currentTimeMillis()*shortback + System.nanoTime()%shortback;
		long itemIDC = convertItemID(itemID);
		
		addToImages("INSERT INTO images (KEY, URL, description, itemid, position) VALUES ("+key+",'"+safe(URL)+"','"+safe(description)+"',"+itemIDC+","+position+")\n");
					
		if(position==0){
			String column = "olditems";
			if(itemID>totalOld) column = "items";
						
			addToImages("UPDATE "+column+" SET thumbnail ='"+URL+"' WHERE KEY="+itemIDC + "\n");
		}
	}
	
	/**
	 * Converts the given populator id to the cassandra item ID
	 * @param itemid
	 * @return
	 */
	Cache cacheIDs = new Cache(5000);
	private long convertItemID(long itemid){
		long ret = itemid;
		
		if (cacheIDs.containsKey(itemid)){
			return cacheIDs.get(itemid);
	    }	
		
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement getID = null;
			ResultSet rs = null;
			
			try{
				getID = conn.prepareStatement("SELECT itemid FROM items WHERE popid="+itemid);
				rs = getID.executeQuery();
				
				// If it was found in items
				if(rs.next()){
					// Make sure the id is real
					try{
						Long tempid = rs.getLong("popid");
						ret = tempid;
					} catch(Exception e){}
				}
				// otherwise try old items
				else{
					close(rs);
					close(getID);
					
					getID = conn.prepareStatement("SELECT itemid FROM olditems WHERE popid="+itemid);
					rs = getID.executeQuery();
					
					if(rs.next()){
						// Make sure the id is real
						try{
							Long tempid = rs.getLong("popid");
							ret = tempid;
						} catch(Exception e){}
					}
				}
			}catch(Exception e){
				
			}finally{
				close(rs);
				close(getID);
				returnConn(conn);
			}
			
			cacheIDs.put(itemid, ret);
		}
		
		return ret;
	}
	
	/*
	 * Drop and add the items family
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddItems()
	 */
	public boolean dropAddItems(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop = null;
			PreparedStatement add = null;
			
			try{
				String CQL;
				try{
					CQL = "DROP COLUMNFAMILY items;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				finally{close(drop);}

				try{
					CQL = "DROP COLUMNFAMILY revtimeitems;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				finally{close(drop);}

				try{
					CQL = "DROP COLUMNFAMILY priceitems;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				finally{close(drop);}

				try{
					CQL = "DROP COLUMNFAMILY revpriceitems;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				finally{}
					
				CQL="CREATE COLUMNFAMILY items (KEY bigint PRIMARY KEY, itemid bigint, name text, description text, thumbnail text, sellerid bigint, categoryid bigint, currentwinner bigint, quantity bigint, noofbids bigint, startdate bigint, enddate bigint, curbid text, maxbid text, startprice text,reserveprice text,buynowprice text, pikey bigint, rpikey bigint, ts bigint, popid bigint) WITH gc_grace_seconds=15 ; ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				CQL="CREATE COLUMNFAMILY revtimeitems (KEY bigint PRIMARY KEY, enddate bigint, catzero bigint, itemid bigint, categoryid bigint) WITH gc_grace_seconds=15 ; ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				CQL="CREATE COLUMNFAMILY priceitems (KEY bigint PRIMARY KEY, pikey bigint, catzero bigint, itemid bigint, categoryid bigint) WITH gc_grace_seconds=15 ; ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				CQL="CREATE COLUMNFAMILY revpriceitems (KEY bigint PRIMARY KEY, pikey bigint, catzero bigint, itemid bigint, categoryid bigint) WITH gc_grace_seconds=15 ; ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to get the items the user is selling
				CQL = "CREATE INDEX item_sellerid_idx ON items (sellerid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to browse items
				CQL = "CREATE INDEX item_itemid_idx ON items (itemid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to convert populator IDS
				CQL = "CREATE INDEX item_popid_idx ON items (popid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);
				
				// Used to list items by categoryID
				CQL = "CREATE INDEX item_categoryid_idx ON items (categoryid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				CQL = "CREATE INDEX revtimeitem_categoryid_idx ON revtimeitems (categoryid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				CQL = "CREATE INDEX revtimeitem_categoryz_idx ON revtimeitems (catzero); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				CQL = "CREATE INDEX priceitem_categoryid_idx ON priceitems (categoryid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				CQL = "CREATE INDEX revpriceitem_categoryid_idx ON revpriceitems (categoryid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				CQL = "CREATE INDEX priceitem_categoryz_idx ON priceitems (catzero); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				CQL = "CREATE INDEX revpriceitem_categoryz_idx ON revpriceitems (catzero); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (dropAddItems): Could not delete the items");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(add);
				returnConn(conn);
			}
		}
		else{
			System.err.println("CassandraDBPopulator (dropAddItems): The database connection is not open");
		}
		
		return deleted;
	}
	
	/*
	 * Drop and add the old items family
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddOldItems()
	 */
	public boolean dropAddOldItems(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop = null;
			PreparedStatement add = null;
			
			try{
				String CQL;
				try{
					CQL = "DROP COLUMNFAMILY olditems;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				finally{}
				
				CQL="CREATE COLUMNFAMILY olditems (KEY bigint PRIMARY KEY, itemid bigint, name text, description text, thumbnail text, sellerid bigint, categoryid bigint, currentwinner bigint, quantity bigint, noofbids bigint, startdate bigint, enddate bigint, curbid text, maxbid text, startprice text,reserveprice text,buynowprice text, ts bigint, popid bigint); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to get the items the user has sold
				CQL = "CREATE INDEX olditem_sellerid_idx ON olditems (sellerid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to browse items
				CQL = "CREATE INDEX olditem_itemid_idx ON olditems (itemid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				
				// Used to convert populator ids
				CQL = "CREATE INDEX olditem_popid_idx ON olditems (popid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (dropAddOldItems): Could not delete the items");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(add);
				returnConn(conn);
			}
		}
		else{
			System.err.println("CassandraDBPopulator (dropAddOldItems): The database connection is not open");
		}	

		return deleted;
	}
	
	long oldItemID=0;
	long totalOld = 0;
	
	/**
	 * Increase the number of old items inserted so we know later if something is a current or old item
	 * via its populator item ID
	 */
	private synchronized void incTotalOld(){
		totalOld++;
	}
	
	private synchronized long getOldItemID(){
		oldItemID++;
		return oldItemID;
	}
	
	/*long itemID=0;
	private synchronized long getItemID(){
		itemID++;
		return itemID;
	}*/
	
	public long insertItem(String name, String description, double startPrice, int quantity, double reservePrice, double buyNowPrice, Date startDate, Date endDate, long sellerID, long categoryID, StringBuffer sql){
		long itemID = -1;

			try{
				// MAKE KEY - use time stamp + random so we can sort by time
				long key = (endDate.getTime()*shortback) + (System.nanoTime()%shortback);
				itemID = key;
				long popid = getOldItemID();
				
				// Insert the item
				sql.append("INSERT INTO items (KEY, itemid,name, description, sellerid," +
						"categoryid, quantity, startdate, enddate,startprice,reserveprice,buynowprice," +
						"noofbids,thumbnail,currentwinner,curbid,maxbid,ts, popid) "
						+ "VALUES ("+key+","+itemID+",'"+safe(name)+"','"+safe(description)+"',"+sellerID+","+categoryID+","
						+quantity+","+System.currentTimeMillis()+","+endDate.getTime()+","+startPrice+","+
						reservePrice+","+buyNowPrice+",0,'blank.jpg',0,0,0,"+System.currentTimeMillis()+","+popid+")\n");
				
				// Insert the item for reverse time lookups
				sql.append("INSERT INTO revtimeitems (KEY,catzero,enddate,itemid,categoryid) VALUES ("+(Long.MAX_VALUE-key)+",0,"+endDate.getTime()+","+itemID+","+categoryID+")\n");
				
				// Insert the item for price lookups
				Long priceKey = ((long) (0*tenzero)) + (itemID % tenzero);		
				sql.append("UPDATE items SET pikey="+priceKey+" WHERE KEY="+itemID +"\n");
				
				cacheIDs.put(popid, itemID);
				itemID = popid;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (insertItem): Could not insert the item");
				e.printStackTrace();
			}

		return itemID;
	}
	
	/*
	 * Get an item from the DB
	 * (non-Javadoc)
	 * @see populator.DBPopulator#getItem(long)
	 */
	public Item getItem(long itemID){
		if(itemID < 1) return null;
		
		Item result = null;
		int attemptsRemaining = 1;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				ResultSet rs = null;
				itemID = convertItemID(itemID);
				
				try {
					// Create the SQL statement to see get the item's details
					statement = conn.prepareStatement("SELECT * FROM items WHERE KEY="+itemID);		
					
					rs = statement.executeQuery();
					
					// If an item is returned then get the details
					if(rs.next()){
						// Make sure the value is real. The problem here is that Cassandra will return the
						// key if it ever existed
						long seller = 0;
						try{
							seller = rs.getLong("sellerid");
						}
						catch(Exception e){}
						
						if(seller != 0){
							// If we need to get the images, do that now
							ArrayList<Image> images = new ArrayList<Image>();

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
									rs.getString("thumbnail"), images, null);
						}
					}
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					System.err.println("CassandraQuery (getItem): Could not get the item " +itemID);
					e.printStackTrace();
				} catch (MySQLNonTransientConnectionException e) {
					System.err.println("CassandraQuery (getItem): Could not get the item " +itemID);
					e.printStackTrace();
				} catch (Exception e) {
					System.err.println("CassandraQuery (getItem): Could not get the item " +itemID);
					e.printStackTrace();
				} finally{
					close(rs);
					close(statement);
					this.returnConn(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		
		return result;
	}
	
	/*
	 * Insert an old item
	 * (non-Javadoc)
	 * @see populator.DBPopulator#insertOldItem(java.lang.String, java.lang.String, double, int, double, double, java.util.Date, java.util.Date, long, long, java.lang.StringBuffer)
	 */
	public long insertOldItem(String name, String description, double startPrice, int quantity, double reservePrice, double buyNowPrice, Date startDate, Date endDate, long sellerID, long categoryID, StringBuffer sql){
		long itemID = -1;

			try{
				long key = (endDate.getTime()*shortback) + (System.nanoTime()%shortback);
				itemID = key;
				long popid = this.getOldItemID();
				
				sql.append("INSERT INTO olditems (KEY, itemid,name, description, sellerid," +
						"categoryid, quantity, startdate, enddate,startprice,reserveprice,buynowprice," +
						"noofbids,thumbnail,currentwinner,curbid,maxbid,popid) "
						+ "VALUES ("+key+","+itemID+",'"+safe(name)+"','"+safe(description)+"',"+sellerID+","+categoryID+","
						+quantity+","+System.currentTimeMillis()+","+endDate.getTime()+","+startPrice+","+
						reservePrice+","+buyNowPrice+",0,'blank.jpg',0,0,0,"+popid+")\n");
				
				incTotalOld();
				
				cacheIDs.put(popid, itemID);
				itemID = popid;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (insertOldItem): Could not insert the item");
				e.printStackTrace();
			}

		return itemID;
	}
	
	long bidID=0;
	long oldBidID=0;
	
	/**
	 * Get an id for a bid
	 * @return
	 */
	/*private synchronized long getBidID(){
		bidID++;
		return bidID;
	}*/
	
	/**
	 * Get an id for an old bid
	 * @return
	 */
	private synchronized long getOldBidID(){
		oldBidID++;
		return oldBidID;
	}
	
	/*
	 * Drop and add the bids family
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddBids()
	 */
	public boolean dropAddBids(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop = null;
			PreparedStatement add = null;
			
			try{
				String CQL;
				try{
					CQL="DROP COLUMNFAMILY bids;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				finally{close(drop);}
				
				try{
					CQL="DROP COLUMNFAMILY maxbids;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				finally{}
				
				CQL="CREATE COLUMNFAMILY bids (KEY bigint PRIMARY KEY, userid bigint, itemid bigint, quantity int, bid text, maxbid text, biddate bigint) WITH gc_grace_seconds=15 ; ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);
				
				CQL="CREATE COLUMNFAMILY maxbids (KEY text PRIMARY KEY, bidkey bigint, userid bigint, itemid bigint, quantity int, bid text, maxbid text, biddate bigint, ts bigint) WITH comparator = UTF8Type AND gc_grace_seconds=15 ; ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);
				
				// Used to get the items a user has bid on
				CQL = "CREATE INDEX maxbids_userid_idx ON maxbids (userid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);
				
				// Used to get all max bids on an item when purchasing
				CQL = "CREATE INDEX maxbids_itemid_idx ON maxbids (itemid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);
				
				// Used to get the items a user has bid on??? <-old
				CQL = "CREATE INDEX bids_userid_idx ON bids (userid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);
				
				// Used to get all bids when purchasing item
				CQL = "CREATE INDEX bids_itemid_idx ON bids (itemid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (dropAddBids): Could not delete the bids");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(add);
				returnConn(conn);
			}
		}
		else{
			System.err.println("CassandraDBPopulator (dropAddBids): The database connection is not open");
		}
		
		return deleted;
	}
	
	/*
	 * Drop and add the old bids family
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddOldBids()
	 */
	public boolean dropAddOldBids(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop = null;
			PreparedStatement add = null;
			
			try{
				String CQL;
				try{
					CQL="DROP COLUMNFAMILY oldbids;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				finally{close(drop);}
				
				try{
					CQL="DROP COLUMNFAMILY maxoldbids;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				finally{}
				
				CQL="CREATE COLUMNFAMILY oldbids (KEY bigint PRIMARY KEY, userid bigint, itemid bigint, quantity int, bid text, maxbid text, biddate bigint); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				CQL="CREATE COLUMNFAMILY maxoldbids (KEY text PRIMARY KEY, bidkey bigint, userid bigint, itemid bigint, quantity int, bid text, maxbid text, biddate bigint, ts bigint) WITH comparator = UTF8Type; ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to get the items a user has bid on
				CQL = "CREATE INDEX maxoldbids_userid_idx ON maxoldbids (userid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to get all max bids on an item
				CQL = "CREATE INDEX maxoldbids_itemid_idx ON maxoldbids (itemid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to get the items a user has purchased
				CQL = "CREATE INDEX oldbids_userid_idx ON oldbids (userid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (dropAddOldBids): Could not delete the bids");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(add);
				returnConn(conn);
			}
		}
		else{
			System.err.println("CassandraDBPopulator (dropAddOldBids): The database connection is not open");
		}

		return deleted;
	}
	
	/*
	 * Insert a bid for an item
	 * (non-Javadoc)
	 * @see populator.DBPopulator#insertBid(populator.Bid, long, java.lang.StringBuffer)
	 */
	public void insertBid(Bid bid, long itemID, StringBuffer sql){
			try{
				itemID = convertItemID(itemID);
				long bidKey=this.getOldBidID();
				
				sql.append("INSERT INTO bids (KEY, userid, itemid, quantity, bid, maxbid, biddate) "
						+ "VALUES ("+bidKey+","+bid.getUserID()+","+itemID+","+bid.getQuantity()+","+df.format(bid.getBid())+","+df.format(bid.getMaxBid())+","+bid.getBidDate().getTime()+")\n");
				
				sql.append("INSERT INTO maxbids (KEY, bidkey, userid, itemid, quantity, bid, maxbid, biddate, ts) "
						+ "VALUES ('"+bid.getUserID()+"_"+itemID+"',"+bidKey+","+bid.getUserID()+","+itemID+","+bid.getQuantity()+","+df.format(bid.getBid())+","+df.format(bid.getMaxBid())+","+bid.getBidDate().getTime()+","+bid.getBidDate().getTime()+")\n");
				
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (insertBid): Could not insert the bid");
				e.printStackTrace();
			}
	}
	
	int oldBidCount=0;
	
	/*
	 * Inserts an old bid, we will buffer it fist
	 * (non-Javadoc)
	 * @see populator.DBPopulator#insertOldBid(populator.Bid, long, java.lang.StringBuffer)
	 */
	public void insertOldBid(Bid bid, long itemID,StringBuffer sql){
			try{
				itemID = convertItemID(itemID);
				long bidKey=this.getOldBidID();
				
				sql.append("INSERT INTO oldbids (KEY, userid, itemid, quantity, bid, maxbid, biddate) "
						+ "VALUES ("+bidKey+","+bid.getUserID()+","+itemID+","+bid.getQuantity()+","+df.format(bid.getBid())+","+df.format(bid.getMaxBid())+","+bid.getBidDate().getTime()+")\n");
				
				sql.append("INSERT INTO maxoldbids (KEY, bidkey, userid, itemid, quantity, bid, maxbid, biddate) "
						+ "VALUES ('"+bid.getUserID()+"_"+itemID+"',"+bidKey+","+bid.getUserID()+","+itemID+","+bid.getQuantity()+","+df.format(bid.getBid())+","+df.format(bid.getMaxBid())+","+bid.getBidDate().getTime()+")\n");
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (insertoldBid): Could not insert the bid");
				e.printStackTrace();
			}
	}
	
	long commentID = 0;
	/**
	 * Get the next ID for a comment
	 * @return
	 */
	private synchronized long getCommentID(){
		commentID++;
		return commentID;
	}
	
	/*
	 * Insert a comment, we will buffer it first
	 * (non-Javadoc)
	 * @see populator.DBPopulator#insertComment(long, long, long, int, long, java.lang.String, java.lang.StringBuffer)
	 */
	public void insertComment(long userID, long sellerID, long itemID, int rating, long endDate, String comment, StringBuffer sql){
		itemID = convertItemID(itemID);	
		
		try{
			sql.append("INSERT INTO comments (KEY,from_user_id, to_user_id, itemid, rating, date, comment) "
					+ "VALUES ("+getCommentID()+","+userID+","+sellerID+","+itemID+","+rating+","+endDate+",'"+ safe(comment)+"')\n");
		}
		catch(Exception e){
			System.err.println("CassandraDBPopulator (insertComment): Could not insert the comment");
			e.printStackTrace();
		}
	}
	
	long questionID = 0;
	/**
	 * Get the next question ID
	 * @return
	 */
	private synchronized long getQuestionID(){
		questionID++;
		return questionID;
	}
	
	/*
	 * Insert an answer, we will buffer it first
	 * (non-Javadoc)
	 * @see populator.DBPopulator#insertAnswer(long, long, long, java.util.Date, java.lang.String, long, java.lang.StringBuffer)
	 */
	public void insertAnswer(long userID, long toUserID, long itemID, Date date, String answer, long responseTo, StringBuffer sql){
			try{
				itemID = convertItemID(itemID);
				long key = this.getQuestionID();
				
				sql.append("INSERT INTO questions (KEY,from_user_id, to_user_id, itemid, is_question, date, content, responseTo) "
						+ "VALUES ("+key+","+userID+","+userID+","+itemID+","+Boolean.FALSE+","+date.getTime()+",'"+ safe(answer)+"',"+responseTo+")\n");
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (insertAnswer): Could not insert the answer");
				e.printStackTrace();
			}
	}
	
	/*
	 * Insert a question, we will buffer it first
	 * (non-Javadoc)
	 * @see populator.DBPopulator#insertQuestion(long, long, long, java.util.Date, java.lang.String, java.lang.StringBuffer)
	 */
	public long insertQuestion(long fromUserID, long toUserID, long itemID, Date date, String question, StringBuffer sql){
		long key = -1;
		
			try{
				itemID = convertItemID(itemID);
				key = this.getQuestionID();
				
				sql.append("INSERT INTO questions (KEY,from_user_id, to_user_id, itemid, is_question, date, content, responseTo) "
						+ "VALUES ("+key+","+fromUserID+","+toUserID+","+itemID+","+Boolean.TRUE+","+date.getTime()+",'"+ safe(question)+"',-1)\n");
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (insertQuestion): Could not insert the question");
				e.printStackTrace();
			}
			
			return key;
	}
	
	/*
	 * Update the max bid of an item
	 * (non-Javadoc)
	 * @see populator.DBPopulator#updateItemMaxBid(long, int, double, double, long, long, java.lang.StringBuffer)
	 */
	public void updateItemMaxBid(long itemID, int noOfBids, double currentBid, double maxBid, long currentWinner, long categoryID, StringBuffer sql){
		/*
		 * 1. update the item bid
		 * 2. update the item pikey
		 * 3. update the reverse item prices lookups
		 */
			try{
				itemID = convertItemID(itemID);
				
				// Make the pikey for price sorting
				currentBid = roundCurrency(currentBid);
				Long priceKey = ((long) (currentBid*1000000000000l)) + (itemID % 10000000000l);
				
				sql.append("UPDATE items SET curbid = '"+df.format(currentBid)+"', maxbid = '"+df.format(maxBid)+
						"', noofbids = "+noOfBids+", currentwinner = "+currentWinner+", pikey="+priceKey+", ts="+System.currentTimeMillis()+" WHERE KEY="+itemID+"\n");
				
				sql.append("UPDATE priceitems SET curbid = "+df.format(currentBid)+", categoryid="+categoryID+", catzero=0, itemid="+itemID+", pikey="+priceKey+" WHERE KEY="+priceKey+"\n");
				sql.append("UPDATE revpriceitems SET curbid = "+df.format(currentBid)+", categoryid="+categoryID+", catzero=0, itemid="+itemID+", pikey="+priceKey+" WHERE KEY="+(Long.MAX_VALUE-priceKey)+"\n");
				// maxbid is already set by inserting the bid
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (updateItemMaxBid): Could not insert the bid");
				e.printStackTrace();
			}
	}
	
	/*
	 * Update the 
	 * 	(non-Javadoc)
	 * @see populator.DBPopulator#updateOldItemMaxBid(long, int, double, double, long, java.lang.StringBuffer)
	 */
	public void updateOldItemMaxBid(long itemID, int noOfBids, double currentBid, double maxBid, long currentWinner, StringBuffer sql){
		itemID = convertItemID(itemID);
			try{
				currentBid = roundCurrency(currentBid);
				sql.append("UPDATE olditems SET curbid = '"+df.format(currentBid)+"', maxbid = '"+df.format(maxBid)+
						"', noofbids = "+noOfBids+", currentwinner = "+currentWinner+" WHERE KEY="+itemID+"\n");
				
				// Max old bids is already updated from inserting old bid
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (updateOldItemMaxBid): Could not insert the bid");
				e.printStackTrace();
			}
	}
	
	/*
	 * Drop and add the categories family
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddCategories()
	 */
	public boolean dropAddCategories(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop = null;
			PreparedStatement add = null;
			
			try{
				String CQL;
				try{
					CQL="DROP COLUMNFAMILY categories;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				finally{}
				
				CQL="CREATE COLUMNFAMILY categories (KEY bigint PRIMARY KEY,parent bigint, name text, ts bigint); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to get the items a user has purchased
				CQL = "CREATE INDEX categories_parent_idx ON categories (parent); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (dropAddCategories): Could not delete the categories");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(add);
				returnConn(conn);
			}
		}
		else{
			System.err.println("CassandraDBPopulator (dropAddCategories): The database connection is not open");
		}
		
		return deleted;
	}
	
	/**
	 * Escape a string to be SQL safe
	 * @param toSafe
	 * @return
	 */
	public String safe(String toSafe){
		return org.apache.commons.lang.StringEscapeUtils.escapeSql(toSafe);
	}
	
	long nowish = System.currentTimeMillis();
	
	StringBuffer categoriesSQL = null;
	Integer categoriesCount = 0;
	
	/*
	 * Insert a category, into the buffer first
	 * (non-Javadoc)
	 * @see populator.DBPopulator#insertCategory(long, java.lang.String, long)
	 */
	public void insertCategory(long id, String name, long parent){
			try{
				// Create the SQL statement to insert the user
				name.replace("?", " ");
				
				synchronized(categoriesCount){
					if(categoriesSQL==null){
						categoriesSQL=new StringBuffer(2048);
					}
					
					categoriesSQL.append("INSERT INTO categories (KEY, name, parent, ts) "
							+ "VALUES ('"+id+"','"+safe(name)+"','"+parent+"','" +nowish+"')\n");
					
					categoriesCount++;
					
					if(categoriesCount%200==0){
						this.executeUpdateSQL(categoriesSQL.toString());
						categoriesSQL=null;
						categoriesCount=0;
					}
				}
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (insertCategory): Could not insert the category");
				e.printStackTrace();
			}
	}
	
	/*
	 * Drop and add the images family
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddImages()
	 */
	public boolean dropAddImages(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop = null;
			PreparedStatement add = null;
			
			try{
				String CQL;
				try{
					CQL = "DROP COLUMNFAMILY images;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				finally{}
				
				CQL="CREATE COLUMNFAMILY images (KEY bigint PRIMARY KEY, URL text, description text, itemid bigint, position bigint) ; ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to get the items the user has sold
				CQL = "CREATE INDEX images_itemid_idx ON images (itemid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (dropAddImages): Could not delete the images");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(add);
				returnConn(conn);
			}
		}
		else{
			System.err.println("CassandraDBPopulator (dropAddImages): The database connection is not open");
		}
		
		return deleted;
	}
	
	/*
	 * We do not use this table with cassandra
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddItemImage()
	 */
	public boolean dropAddItemImage(){
		// Cassandra does not have this
		return true;
	}
	
	/*
	 * Drop and add the payments family
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddPayments()
	 */
	public boolean dropAddPayments(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop = null;
			PreparedStatement add = null;
			
			try{
				String CQL;
				try{
					CQL="DROP COLUMNFAMILY payments;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				finally{}
				
				CQL="CREATE COLUMNFAMILY payments (KEY bigint PRIMARY KEY, userid bigint, itemid bigint, quantity int, price text, paiddate bigint, street text, town text, zip text, state text, nameoncard text, creditcardno text, cvv text, expirationdate bigint); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to get the items a user has purchased
				CQL = "CREATE INDEX payments_userid_idx ON payments (userid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (dropAddPayments): Could not delete the payments");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(add);
				returnConn(conn);
			}
		}
		else{
			System.err.println("CassandraDBPopulator (dropAddPayments): The database connection is not open");
		}
		
		return deleted;
	}
	
	/*
	 * Drop and add the purchased family
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddPurchased()
	 */
	public boolean dropAddPurchased(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop = null;
			PreparedStatement add = null;
			
			try{
				String CQL;
				try{
					CQL="DROP COLUMNFAMILY purchased;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				finally{}
				
				CQL="CREATE COLUMNFAMILY purchased (KEY bigint PRIMARY KEY, userid bigint, itemid bigint, purcashedquantity int, price text, purchasedate bigint, paid boolean, paiddate bigint, name text, description text, thumbnail text, sellerid bigint, categoryid bigint, currentwinner bigint, quantity bigint, noofbids bigint, startdate bigint, enddate bigint, curbid text, maxbid text, ts bigint); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				close(add);

				// Used to get the items a user has purchased
				CQL = "CREATE INDEX purchases_userid_idx ON purchased (userid); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (dropAddPurchased): Could not delete the purchased");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(add);
				returnConn(conn);
			}
		}
		else{
			System.err.println("CassandraDBPopulator (dropAddPurchased): The database connection is not open");
		}
		
		return deleted;
	}
	
	long purchaseID = 0;
	/**
	 * Get the next id for a purchase
	 * @return
	 */
	private synchronized long getPurchaseID(){
		purchaseID++;
		return purchaseID;
	}
	
	long payID = 0;
	/**
	 * Get the next ID for
	 * @return
	 */
	/*private synchronized long getPayID(){
		payID++;
		return payID;
	}*/
	
	/*
	 * Insert a purchase, into the buffer first
	 * (non-Javadoc)
	 * @see populator.DBPopulator#insertPurchase(long, java.lang.Boolean, long, long, double, long, java.lang.StringBuffer)
	 */
	public long insertPurchase(long itemID, Boolean paid, long paidDate, long winnerID, double currentBid, long quantity, StringBuffer sql){
		long purchaseID = -1;
		itemID = convertItemID(itemID);
		
			try{
				purchaseID = this.getPurchaseID();
				
				sql.append("INSERT INTO purchased (KEY,userid, itemid, quantity, price, purchasedate, paid, paiddate) "
						+ "VALUES ("+purchaseID+","+winnerID+","+itemID+","+quantity+",'"+(currentBid*(double)quantity)+"',"+new Date().getTime()+","+paid+","+paidDate+")\n");
					
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (insertPurchase): Could not insert the purchase");
				e.printStackTrace();
			}

		return purchaseID;
	}
	
	/*
	 * Drop and add the states
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddStates()
	 */
	public boolean dropAddStates(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop = null;
			PreparedStatement add = null;
			
			try{
				String CQL;
				try{
					CQL="DROP COLUMNFAMILY states;";
					drop = conn.prepareStatement(CQL);
					drop.executeUpdate();
				}catch(Exception e){}
				finally{}
				
				CQL="CREATE COLUMNFAMILY states (KEY bigint PRIMARY KEY,shortname text, longname text); ";
				add = conn.prepareStatement(CQL);
				add.executeUpdate();
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (dropAddStates): Could not delete the states");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(add);
				returnConn(conn);
			}
		}
		else{
			System.err.println("CassandraDBPopulator (dropAddStates): The database connection is not open");
		}
		
		return deleted;
	}
	
	/*
	 * Insert a state
	 * (non-Javadoc)
	 * @see populator.DBPopulator#insertState(int, java.lang.String, java.lang.String)
	 */
	public int insertState(int id, String shortName, String longName){
		int stateID = -1;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement statement = null;
			
			try{
				// Create the SQL statement to insert the user
				statement = conn.prepareStatement("INSERT INTO states (KEY, shortname, longname) "
						+ "VALUES ('"+id+"','"+shortName+"','"+longName+"')");
				
				
				statement.executeUpdate();
				
				stateID=id;
			}
			catch(Exception e){
				System.err.println("CassandraDBPopulator (insertState): Could not insert the state");
				e.printStackTrace();
			}finally{
				close(statement);
				returnConn(conn);
			}
		}
		else{
			System.err.println("CassandraDBPopulator (insertState): The database connection is not open");
		}
		
		return stateID;
	}
	
	/*
	 * Not implemented in Cassandra
	 * (non-Javadoc)
	 * @see populator.DBPopulator#disableIndexes(boolean)
	 */
	public void disableIndexes(boolean disable){
		disableIndexes = disable;
		
		// Not available in Cassandra
	}
	
	/*
	 * Not implemented in cassandra
	 * (non-Javadoc)
	 * @see populator.DBPopulator#enableIndexes()
	 */
	public void enableIndexes(){
		// Not available in Cassandra
	}
	
	/*
	 * (non-Javadoc)
	 * @see populator.DBPopulator#flushAllBuffersDB()
	 */
	public void flushAllBuffersDB(){
		synchronized(categoriesCount){
			if(categoriesSQL!=null){
				this.executeUpdateSQL(categoriesSQL.toString());
				categoriesSQL=null;
				categoriesCount=0;
			}
		}
		
		synchronized(usersCount){
			if(usersSQL!=null){
				this.executeUpdateSQL(usersSQL.toString());
				usersSQL=null;
				usersCount=0;
			}
		}

		synchronized(oldItemCount){
			if(oldItemSQL!=null){
				this.executeUpdateSQL(oldItemSQL.toString());
				oldItemSQL=null;
				oldItemCount=0;
			}
		}
		
		synchronized(itemCount){
			if(itemSQL!=null){
				this.executeUpdateSQL(itemSQL.toString());
				itemSQL=null;
				itemCount=0;
			}
		}
		
		synchronized(imageCount){
			if(imageSQL!=null){
				this.executeUpdateSQL(imageSQL.toString());
				imageSQL=null;
				imageCount=0;
			}
		}
	}
	
	public static double roundCurrency(double num){
		return  (Math.floor(num*100.0 + 0.5) / 100.0);
	}
	
	public void setMyISAM(){
		
	}
	
	public void setMyINNODB(){
		
	}
}
