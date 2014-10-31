package populator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.util.Date;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.common.SolrInputDocument;


public abstract class DBPopulator {
	private String url = null;
	private String driver = null;
	private String username = null;
	private String password = null;
	protected static boolean disableIndexes = false;
	protected String engine = "MyISAM";
	protected boolean fulltextsearch = false;
	
	public void setEngine(String engine, boolean fulltextsearch){
		this.engine = engine;
		this.fulltextsearch = fulltextsearch;
	}
	
	/**
	 * Create the default DBPopulator object with the variables needed to open a connection to the database we are populating
	 * 
	 * @param url
	 * @param driver
	 * @param username
	 * @param password
	 */
	protected DBPopulator(String url, String driver, String username, String password){
		init(url, driver, username, password);
	}
	
	/**
	 * Sets the information needed to connect to the database
	 * 
	 * @param url
	 * @param driver
	 * @param username
	 * @param password
	 */
	private void init(String url, String driver, String username, String password){
		this.url = url;
		this.driver = driver;
		this.username = username;
		this.password = password;
	}
	
	/**
	 * Gets the URL to connect to the database
	 * 
	 * @return the database's url
	 */
	protected String getURL(){
		return this.url;
	}
	
	/**
	 * Gets the driver to use for the database connections
	 * 
	 * @return the String for the database connections
	 */
	protected String getDriver(){
		return this.driver;
	}
	
	/**
	 * Gets the username needed to connect to the database
	 * 
	 * @return the username for the database
	 */
	protected String getUsername(){
		return this.username;
	}
	
	/**
	 * Gets the password needed to connect to the database
	 * 
	 * @return the password for the database
	 */
	protected String getPassword(){
		return this.password;
	}
	
	/**
	 * Returns a connection to the database
	 * 
	 * @return a connection to the database
	 */
	public abstract Connection getConnection();
	
	public abstract void executeUpdateSQL(String sql);
	public abstract StringBuffer startSQL();
	public abstract void addOldItem(StringBuffer sql);
	public abstract void addItem(StringBuffer sql);
	
	/**
	 * Closes all of the open connections to the database
	 */
	public abstract void closeConnections();
	
	/**
	 * Sets whether the database should be created with the table indexes disabled - this can speed up insert times.
	 * By default tables are created with the indexes enabled.
	 * 
	 * @param disable If true tables are created without indexes disabled
	 */
	public void disableIndexes(boolean disable){
		disableIndexes = disable;
	}
	
	/**
	 * Enables table indexes in the database on all tables
	 */
	public abstract void enableIndexes();
	
	/**
	 * Commits all/any data that maybe sitting in data/statement buffers etc. Calling this method ensures all 
	 * statements/operations previously submitted are executed/committed to the database
	 */
	public void flushAllBuffers(){
		try {
			if(SOLR_SERVER!=null) SOLR_SERVER.commit();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		
		flushAllBuffersDB();
	}
	
	protected abstract void flushAllBuffersDB();
	
	/**
	 * Drops the database tables associated with storing user data (if they exist). It then recreates them. All indexes
	 * are enabled by default. Indexes are not enabled if disableIndexes=true
	 * 
	 * @return true if the table was successfully dropped (if existed) and added
	 */
	public abstract boolean dropAddUsers();
	
	public abstract boolean dropAddQuestions();
	public abstract boolean dropAddComments();
	public abstract void insertComment(long userID, long sellerID, long itemID, int rating, long endDate, String comment, StringBuffer sql);
	public abstract long insertQuestion(long fromUserID, long toUserID, long itemID, Date date, String question, StringBuffer sql);
	public abstract void insertAnswer(long userID, long toUserID, long itemID, Date date, String answer, long responseTo, StringBuffer sql);
	
	/**
	 * Inserts a user into the database and returns the unique id that is assigned to that user (or -1 if insert failed)
	 * 
	 * @param firstName 
	 * @param lastName
	 * @param username
	 * @param password
	 * @param email
	 * @param rating
	 * @param creationDate
	 * @return the unique userID that the database used for the user, or -1 if the insert failed
	 */
	public abstract long insertUser(String firstName, String lastName, String username, String password, String email, int rating, Date creationDate);
		
	/**
	 * Inserts an item into the items table in the database and returns the id that the item was assigned, or -1 if the insert failed
	 * Item IDs will be offset by the items in the oldItems table since they share the same id address space
	 * 
	 * @param name
	 * @param description
	 * @param startPrice
	 * @param quantity
	 * @param reservePrice
	 * @param buyNowPrice
	 * @param startDate
	 * @param endDate
	 * @param sellerID
	 * @param categoryID
	 * @return the unique itemID of the item or -1 if the insert failed
	 */
	public abstract long insertItem(String name, String description, double startPrice, int quantity, double reservePrice, double buyNowPrice, Date startDate, Date endDate, long sellerID, long categoryID, StringBuffer sql);
	
	/**
	 * Inserts an item into the oldItems table in the database and returns the id that the item was assigned, or -1 if the insert failed
	 * 
	 * @param name
	 * @param description
	 * @param startPrice
	 * @param quantity
	 * @param reservePrice
	 * @param buyNowPrice
	 * @param startDate
	 * @param endDate
	 * @param sellerID
	 * @param categoryID
	 * @return the unique itemID of the old item or -1 if the insert failed
	 */
	public abstract long insertOldItem(String name, String description, double startPrice, int quantity, double reservePrice, double buyNowPrice, Date startDate, Date endDate, long sellerID, long categoryID, StringBuffer sql);
	
	/**
	 * Drops the item tables if it exists and recreates it. Indexes enabled by default, indexes are not enabled if
	 * disableIndexes=true
	 * 
	 * @return true if the table was successfully added
	 */
	public abstract boolean dropAddItems();
	
	/**
	 * drops the oldItems table if it exists and recreates it. Indexes are enabled by default, indexes are not enabled if
	 * disableIndexes=true
	 * 
	 * @return true if table successfully created
	 */
	public abstract boolean dropAddOldItems();
	
	/**
	 * Inserts a bid into the current bids table
	 * 
	 * @param bid
	 * @param itemID
	 */
	public abstract void insertBid(Bid bid, long itemID, StringBuffer sql);
	
	/**
	 * Inserts a bid into the oldBids table
	 * @param bid
	 * @param itemID
	 */
	public abstract void insertOldBid(Bid bid, long itemID, StringBuffer sql);
	
	/**
	 * drops the bids table if it exists and recreates it. Indexes are enabled by default, indexes are not enabled if
	 * disableIndexes=true
	 * 
	 * @return true if table successfully added
	 */
	public abstract boolean dropAddBids();
	
	/**
	 * Drops the oldBids table (if exists) and creates the table again with indexes enabled by default. Indexes are enabled if
	 * disableIndexes is true.
	 * 
	 * @return true if table successfully added
	 */
	public abstract boolean dropAddOldBids();
	
	
	/**
	 * Updates an items bid with the current winning bid, number of bids, max bid and the user's ID who made the winning bid
	 * 
	 * @param itemID The id of the item to update
	 * @param noOfBids The number of bids that have occurred for the item
	 * @param currentBid The current bid value
	 * @param maxBid The maximum the current winning user is willing to bid
	 * @param currentWinner The userID of the current winner
	 */
	public abstract void updateItemMaxBid(long itemID, int noOfBids, double currentBid, double maxBid, long currentWinner, long categoryID, StringBuffer sql);
	
	/**
	 * Updates an oldItems bid with the current winning bid, number of bids, max bid and the user's ID who made the winning bid
	 * 
	 * @param itemID The id of the item to update
	 * @param noOfBids The number of bids that have occurred for the item
	 * @param currentBid The current bid value
	 * @param maxBid The maximum the current winning user is willing to bid
	 * @param currentWinner The userID of the current winner
	 */
	public abstract void updateOldItemMaxBid(long itemID, int noOfBids, double currentBid, double maxBid, long currentWinner, StringBuffer sql);
	
	/**
	 * Inserts an address into the database and returns the addresses id that it was stored as
	 * 
	 * @param address
	 * @return the unique id of the address in the database
	 */
	public abstract long insertAddress(Address address);
	
	/**
	 * Drops the addresses table (if exists) and creates the table again with indexes enabled by default. Indexes are enabled if
	 * disableIndexes is true.
	 * 
	 * @return true if table successfully added
	 */
	public abstract boolean dropAddAdresses();
	
	/**
	 * Inserts a category into the categories table
	 * 
	 * @param id the category id
	 * @param name the category name
	 * @param parent the id of the categories parent. '0' is the special case root parent
	 */
	public abstract void insertCategory(long id, String name, long parent);
	
	/**
	 * Drops the categories table (if exists) and creates the table again with indexes enabled by default. Indexes are enabled if
	 * disableIndexes is true.
	 * 
	 * @return true if table successfully added
	 */
	public abstract boolean dropAddCategories();
	
	/**
	 * Drops the images table (if exists) and creates the table again with indexes enabled by default. Indexes are enabled if
	 * disableIndexes is true.
	 * 
	 * @return true if table successfully added
	 */
	public abstract boolean dropAddImages();
	
	/**
	 * Drops the itemimage table (if exists) and creates the table again with indexes enabled by default. Indexes are enabled if
	 * disableIndexes is true.
	 * 
	 * @return true if table successfully added
	 */
	public abstract boolean dropAddItemImage();
	
	/**
	 * Inserts an image into the database and updates the item (or oldItems) table with a new thumbnail if the image
	 * passed is a thumbnail image. Thumbnail images have a position of '0'.
	 * 
	 * @param itemID
	 * @param position
	 * @param URL
	 * @param description
	 */
	public abstract void insertImage(long itemID, int position, String URL, String description);
	
	/**
	 * Drops the payments table (if exists) and creates the table again with indexes enabled by default. Indexes are enabled if
	 * disableIndexes is true.
	 * 
	 * @return true if table successfully added
	 */
	public abstract boolean dropAddPayments();
	
	/**
	 * Drops the purchase table (if exists) and creates the table again with indexes enabled by default. Indexes are enabled if
	 * disableIndexes is true.
	 * 
	 * @return true if table successfully added
	 */
	public abstract boolean dropAddPurchased();
	
	/**
	 * Inserts an item into the purchased table
	 * 
	 * @param itemID
	 * @param paid
	 * @param paidDate
	 * @return the unique id of the purchase that was inserted
	 */
	public abstract long insertPurchase(long itemID, Boolean paid, long paidDate, long winnerID, double currentBid, long quantity, StringBuffer sql);
	
	/**
	 * Drops the states table (if exists) and creates the table again with indexes enabled by default. Indexes are enabled if
	 * disableIndexes is true.
	 * 
	 * @return true if table successfully added
	 */
	public abstract boolean dropAddStates();
	
	/**
	 * Inserts a state in to the database and returns its database id
	 * 
	 * @param shortName
	 * @param longName
	 * @return
	 */
	public abstract int insertState(int id, String shortName, String longName);
	
	public abstract int getNoOfImages(long itemID);
	
	HttpSolrServer SOLR_SERVER = null;
	
	public void initSolr(){
		if(SOLR_SERVER==null)
//		try {
			SOLR_SERVER = new HttpSolrServer(CreateAll.SOLR_URL);
			SOLR_SERVER.setDefaultMaxConnectionsPerHost(CreateAll.SOLR_MAX_CONNS);
			
			SOLR_SERVER.setSoTimeout(20000);
			SOLR_SERVER.setConnectionTimeout(5000);
			SOLR_SERVER.setMaxTotalConnections(CreateAll.SOLR_MAX_CONNS);
			SOLR_SERVER.setFollowRedirects(false);
			SOLR_SERVER.setParser(new XMLResponseParser());
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//			System.out.println("initSolr (DBPopulator): Could not connect to Solr server");
//		}
	}
	
	Integer commit = 0;
	public boolean addToSolr(long id, String name, String description, double currentBid, Date endDate){
		if(SOLR_SERVER!=null){
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", id);
			doc.addField("name", name, 1.0f);
			doc.addField("description", description, 1.0f);
			doc.addField("endDate", endDate);
			doc.addField("currentBid", currentBid, 1.0f);
			
			try {
				SOLR_SERVER.add(doc);
				
				synchronized(commit){
					commit++;
					if(commit%500==0){
						SOLR_SERVER.commit();
						commit=0;
					}
				}
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		return false;
	}
	
	public abstract Item getItem(long itemID);
}
