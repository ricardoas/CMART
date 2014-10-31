package populator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;

public class MySQLDBPopulator extends DBPopulator{
	private Stack<Connection> conns;
	boolean batch = true;
	private int maxConnsCache = 100;
	
	/**
	 * Setup the super DB object with the connection details
	 */
	public MySQLDBPopulator(){
		super(CreateAll.MY_DATABASE_URL, CreateAll.MY_DATABASE_DRIVER, CreateAll.MY_DATABASE_USERNAME, CreateAll.MY_DATABASE_PASSWORD);
		conns = new Stack<Connection>();
	}
	
	/**
	 * Get a new connection to the database
	 * @return a new database connection
	 */
	private Connection initConnection(){
		try {
			Class.forName(this.getDriver()).newInstance();
			Connection conn = DriverManager.getConnection(this.getURL(), this.getUsername(), this.getPassword());
			return conn;
		}
		catch(Exception e){
			System.err.println("MySQLDBPopulator (initConnection): Could not open a new database connection to " + this.getURL());
			e.printStackTrace();
		}
		
		return null;
	}
	
	/*
	 * Gets a connection from the pool, or starts a new connection to the database
	 * (non-Javadoc)
	 * @see populator.DBPopulator#getConnection()
	 */
	public Connection getConnection(){
		Connection conn = null;
		
		// Try the pool
		synchronized(conns){	
			while(conn==null && !conns.isEmpty()){
				conn = conns.pop();
				try {
					if(!conn.isValid(30)){
						this.closeConnection(conn);
						conn=null;
					}
				} catch (SQLException e) {
					this.closeConnection(conn);
					conn=null;
				}
			}
		}
		
		// Return the pooled connection if there is one, otherwise start a new one
		if(conn != null)
			return conn;
		else
			return initConnection();
		
	}
	
	/**
	 * Return a string buffer with the correct statement to start a batch statement
	 */
	public StringBuffer startSQL(){
		StringBuffer ret = new StringBuffer();

		return ret;
	}
	
	/**
	 * Close a database connection
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
					System.err.println("MySQLDBPopulator (closeConnection): Could not close database connection");
					e.printStackTrace();
				}	
		}
	}
	
	/**
	 * Returns a connection to the connection pool
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
	
	/**
	 * Close a prepared statement
	 * @param ps
	 */
	public void close(PreparedStatement ps){
		if(ps!=null){
			try{
				if(ps!=null){
					ps.close();
					ps = null;
				}
			}
			catch(Exception e){}
		}
	}
	
	/**
	 * Close a result set
	 * @param rs
	 */
	public void close(ResultSet rs){
		if(rs!=null){
			try{
				if(rs!=null){
					rs.close();
					rs = null;
				}
			}
			catch(Exception e){}
		}
	}
	
	/**
	 * Close all of the connections to the database
	 */
	public void closeConnections(){
		synchronized(conns){
			for(Connection conn : conns)
				if(conn != null) closeConnection(conn);
		}
	}
	
	/**
	 * Close all connections to the database
	 * @param conn
	 */
	public void closeAll(Connection conn){
		synchronized(conns){
			closeConnection(conn);
			
			conn = conns.pop();
			while(conn != null){
				closeConnection(conn);
				
				conn = conns.pop();
			}
		}
	}
	
	/**
	 * We are adding items as we go so the add with the buffer does nothing
	 */
	public synchronized void addOldItem(StringBuffer sql){}
	public void executeUpdateSQL(String sql){}
	public synchronized void addItem(StringBuffer sql){}
	
	/**
	 * Drop the accounts table and readd it
	 * @return
	 */
	public boolean dropAddAccounts(){
		boolean deleted = false;
		Connection conn = this.getConnection();
			
			if(conn != null){
				PreparedStatement drop = null;
				PreparedStatement create = null;
				
				try{
					drop = conn.prepareStatement("DROP TABLE IF EXISTS `accounts`");
					
					create = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `accounts` ("+
					  "`id` bigint(20) unsigned NOT NULL AUTO_INCREMENT, "+
					  "`name` varchar(100) NOT NULL, "+
					  "`nameOnCard` varchar(100) NOT NULL, "+
					  "`creditCardNo` varchar(20) NOT NULL, "+
					  "`cvv` varchar(5) NOT NULL, "+
					  "`expirationDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, "+
					  "PRIMARY KEY (`id`) "+
					") ENGINE=" + engine + " DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ; ");
					
					drop.executeUpdate();
					create.executeUpdate();
					
					if(disableIndexes){
						PreparedStatement disable = conn.prepareStatement("ALTER TABLE `accounts` DISABLE KEYS");
						disable.execute();
						disable.close();
					}
					
					deleted = true;
				}
				catch(Exception e){
					System.err.println("MySQLDBPopulator (dropAddUsers): Could not delete the users");
					e.printStackTrace();
				}
				finally{
					close(drop);
					close(create);
					returnConn(conn);
				}
			}
			else{
				System.err.println("MySQLDBPopulator (dropAddUsers): The database connection is not open");
			}			
			
			return deleted;
	}
	
	/*
	 * Drop the users table and readd it
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddUsers()
	 */
	public boolean dropAddUsers(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop = null;
			PreparedStatement create = null;
			
			try{
				this.dropAddAccounts();				
				
				drop = conn.prepareStatement("DROP TABLE IF EXISTS `users`");
				String statementString = "CREATE TABLE IF NOT EXISTS `users` (" +
				  "`id` bigint unsigned NOT NULL AUTO_INCREMENT, " +
				  "`firstname` varchar(50) DEFAULT NULL, " +
				  "`lastname` varchar(50) DEFAULT NULL, " +
				  "`username` varchar(50) NOT NULL, " +
				  "`password` varchar(50) NOT NULL, " +
				  "`authtoken` varchar(16) DEFAULT NULL, " +
				  "`email` varchar(200) NOT NULL, " +
				  "`rating` int(11) DEFAULT NULL, " +
				  "`creationDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00', " +
				  "`ts` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
				  "PRIMARY KEY (`id`), " +
				  "UNIQUE KEY `usernamePasswordIndex` (`username`,`password`)";
				
				  if(fulltextsearch) statementString += ", FULLTEXT KEY `emailIndex` (`email`)";
				  statementString += ") ENGINE=" + engine + "  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1";
				
				create = conn.prepareStatement(statementString);						
				
				drop.executeUpdate();
				create.executeUpdate();
				
				if(disableIndexes){
					PreparedStatement disable = conn.prepareStatement("ALTER TABLE `users` DISABLE KEYS");
					disable.execute();
					disable.close();
				}
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (dropAddUsers): Could not delete the users");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(create);
				returnConn(conn);
			}
		}
		else{
			System.err.println("MySQLDBPopulator (dropAddUsers): The database connection is not open");
		}	
		
		return deleted;
	}
	
	/* Drop and add the question table
	 * (non-Javadoc)
	 * @see populator.DBPopulator#dropAddQuestions()
	 */
	public boolean dropAddQuestions(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			PreparedStatement drop = null;
			PreparedStatement create = null;
			
			try{
				drop = conn.prepareStatement("DROP TABLE IF EXISTS `questions`");
				
				create = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `questions` ("+
						  "`id` bigint unsigned NOT NULL AUTO_INCREMENT,"+
						  "`from_user_id` bigint unsigned NOT NULL,"+
						  "`to_user_id` bigint unsigned NOT NULL,"+
						  "`item_id` bigint unsigned NOT NULL,"+
						  "`is_question` enum('Y','N') NOT NULL,"+
						  "`date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',"+
						  "`content` text,"+
						  "`ts` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
						  "`responseTo` bigint(20) NOT NULL DEFAULT '-1'," +
						  "PRIMARY KEY (`id`), KEY `itemID` (`item_id`), "+
						  "KEY `itemIndex` (`item_id`)" +
						") ENGINE=" + engine + "  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;");
				
				drop.executeUpdate();
				create.executeUpdate();
				
				if(disableIndexes){
					PreparedStatement disable = conn.prepareStatement("ALTER TABLE `questions` DISABLE KEYS");
					disable.execute();
					disable.close();
				}
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (dropAddQuestions): Could not delete the questions");
				e.printStackTrace();
			}
			finally{
				close(drop);
				close(create);
				returnConn(conn);
			}
		}
		else{
			System.err.println("MySQLDBPopulator (dropAddQuestions): The database connection is not open");
		}	
		
		return deleted;
	}
	
	public boolean dropAddComments(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement dropStatement = conn.prepareStatement("DROP TABLE IF EXISTS `comments`");
				
				PreparedStatement createStatement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `comments` (" +
				  "`id` bigint unsigned NOT NULL AUTO_INCREMENT," +
				  "`from_user_id` bigint unsigned NOT NULL," +
				  "`to_user_id` bigint unsigned NOT NULL," +
				  "`item_id` bigint unsigned NOT NULL," +
				  "`rating` int(10) unsigned NOT NULL," +
				  "`date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00'," +
				  "`comment` text NOT NULL," +
				  "`ts` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
				  "PRIMARY KEY (`id`), KEY `itemID` (`item_id`)" +
				") ENGINE=" + engine + "  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;");
				
				dropStatement.executeUpdate();
				createStatement.executeUpdate();
				
				dropStatement.close();
				createStatement.close();
				
				if(disableIndexes){
					PreparedStatement disable = conn.prepareStatement("ALTER TABLE `comments` DISABLE KEYS");
					disable.execute();
					disable.close();
				}
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (dropAddComments): Could not delete the comments");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (dropAddComments): The database connection is not open");
		}
		
		returnConn(conn);
		return deleted;
	}
	
	public boolean dropAddAdresses(){
		boolean deleted = false;
		Connection conn = this.getConnection();	try{try{if(!conn.isValid(60000)) closeAll(conn);	conn = this.initConnection();} catch(Exception e){this.closeConnection(conn); conn=this.initConnection();}} catch(Exception e){this.closeConnection(conn); conn=this.initConnection();}
		
		if(conn != null){
			try{
				PreparedStatement dropStatement = conn.prepareStatement("DROP TABLE IF EXISTS `addresses`");
				
				PreparedStatement createStatement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `addresses` (" +
						"`id` bigint unsigned NOT NULL AUTO_INCREMENT, " +
						"`userID` bigint unsigned NOT NULL, " +
						"`street` varchar(50) NOT NULL, " +
						"`town` varchar(50) NOT NULL, " +
						"`zip` varchar(10) NOT NULL, " +
						"`state` int(10) unsigned NOT NULL, " +
						"`isDefault` tinyint(1) NOT NULL DEFAULT '0', " +
						"PRIMARY KEY (`id`), " +
						"KEY `userIndex` (`userID`)" +
						") ENGINE=" + engine + "  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
				
				dropStatement.executeUpdate();
				createStatement.executeUpdate();
				
				dropStatement.close();
				createStatement.close();
				
				if(disableIndexes){
					PreparedStatement disable = conn.prepareStatement("ALTER TABLE `addresses` DISABLE KEYS");
					disable.execute();
					disable.close();
				}
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (dropAddAdresses): Could not delete the addresses");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (dropAddAdresses): The database connection is not open");
		}
		
		returnConn(conn);
		return deleted;
	}
	
	public long insertAddress(Address address){
		long addressID = -1;
		Connection conn = this.getConnection();
		PreparedStatement statement = null;
		PreparedStatement getID = null;
		ResultSet rs = null;
		
		if(conn != null){
			try{
				// Create the SQL statement to insert the user
				statement = conn.prepareStatement("INSERT INTO `addresses` (userID, street, town, zip, state, isDefault) "
						+ "VALUES (?, ?, ?, ?, ?, ?)");
				
				statement.setLong(1, address.getUserID());
				statement.setString(2, address.getStreet());
				statement.setString(3, address.getTown());
				statement.setString(4, address.getZip());
				statement.setLong(5, address.getState());
				statement.setBoolean(6, address.getIsDefault());

				int rows = statement.executeUpdate();
				
				// If there is a result, get the address ID number
				if(rows == 1){
					getID = conn.prepareStatement("SELECT LAST_INSERT_ID()");
					rs = getID.executeQuery();
					
					if(rs.next())
						addressID = rs.getLong(1);
				}
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (insertAddress): Could not insert the address");
				e.printStackTrace();
			}
			finally{
				close(rs);
				close(getID);
				close(statement);
				returnConn(conn);
			}
		}
		else{
			System.err.println("MySQLDBPopulator (insertAddress): The database connection is not open");
		}
		
		return addressID;
	}
	
	public long insertUser(String firstName, String lastName, String username, String password, String email, int rating, Date creationDate){
		long userID = -1;
		Connection conn = this.getConnection();
		PreparedStatement statement = null;
		PreparedStatement getID = null;
		ResultSet rs = null;
		
		if(conn != null){
			try{
				// Create the SQL statement to insert the user
				statement = conn.prepareStatement("INSERT INTO `users` (firstname, lastname, username, password, email, rating, creationDate) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?)");
				statement.setString(1, firstName);
				statement.setString(2, lastName);
				statement.setString(3, username);
				statement.setString(4, password);
				statement.setString(5, email);
				statement.setInt(6, rating);
				statement.setTimestamp(7, new java.sql.Timestamp(creationDate.getTime()));

				int rows = statement.executeUpdate();
				
				// If there is a result, get the user ID number
				if(rows == 1){
					getID = conn.prepareStatement("SELECT LAST_INSERT_ID()");
					rs = getID.executeQuery();
					
					if(rs.next())
						userID = rs.getLong(1);
				}
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (insertUser): Could not insert the user");
				e.printStackTrace();
			}
			finally{
				close(rs);
				close(getID);
				close(statement);
				returnConn(conn);
			}
		}
		else{
			System.err.println("MySQLDBPopulator (insertUser): The database connection is not open");
		}
		
		return userID;
	}
	
	
	public void insertImage(long itemID, int position, String URL, String description){
		//boolean insertedImage = false;

		Connection conn = this.getConnection();

		PreparedStatement insertImage = null;
		PreparedStatement getImageID = null;
		PreparedStatement insertItemImage = null;
		PreparedStatement updateThumbnail = null;
		PreparedStatement updateThumbnailOld  = null;
		ResultSet rs = null;
		
		if (conn != null) {
			try {
				// Create the SQL statement to insert the image URL
				insertImage = conn.prepareStatement("INSERT INTO `images` (URL, description) "
						+ "VALUES (?, ?)");
				insertImage.setString(1, URL);
				insertImage.setString(2, description);

				// Create the SQL statement to get the images ID number
				getImageID = conn.prepareStatement("SELECT LAST_INSERT_ID()");

				// Create the SQL statement to insert the item-image link
				insertItemImage = conn
						.prepareStatement("INSERT INTO `item_image` (itemID, imageID, position) " + "VALUES (?, ?, ?)");
				insertItemImage.setLong(1, itemID);
				insertItemImage.setInt(3, position);

				// Create the SQL statement to update the thumbnail field
				//TODO: this really needs to know what table to look in
				// Bad Bad Bad Bad Bad
				updateThumbnail = conn.prepareStatement("UPDATE `items` SET thumbnail = ? "
						+ "WHERE `items`.`id` = ?");
				updateThumbnail.setLong(2, itemID);
				updateThumbnailOld = conn.prepareStatement("UPDATE `oldItems` SET thumbnail = ? "
						+ "WHERE `oldItems`.`id` = ?");
				updateThumbnailOld.setLong(2, itemID);
				
				// insert the image
				insertImage.executeUpdate();
				
				// Get the images ID
				int imageID = 0;
				rs = getImageID.executeQuery();
				if (rs.next()) {
					imageID = rs.getInt(1);
				}

				if (position > 0) {
					// Set the imageID and create the item-image link
					insertItemImage.setInt(2, imageID);
					insertItemImage.executeUpdate();
					
				} else {
					// if the position is 0 then we want to update the thumbnail
					// field
					//try{
					updateThumbnail.setString(1, URL);
					updateThumbnail.executeUpdate();
					
					//}catch(Exception e){updateThumbnail.close(); }
					
					//try{
					updateThumbnailOld.setString(1, URL);
					updateThumbnailOld.executeUpdate();
					
					//}catch(Exception e){updateThumbnail.close(); }
				}

			} catch (Exception e) {
				System.out.println("MySQLQuery (insertImage): Could not insert image");
				e.printStackTrace();
			}
			finally{
				close(rs);
				close(insertImage);
				close(getImageID);
				close(insertItemImage);
				close(updateThumbnail);
				close(updateThumbnailOld);
				
				returnConn(conn);
			}

		}
	}
	
	public boolean dropAddItems(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement dropStatement = conn.prepareStatement("DROP TABLE IF EXISTS `items`");
				dropStatement.executeUpdate();
				
				//PreparedStatement incrementOffsetStatement = conn.prepareStatement("SELECT MAX(id) from `oldItems`");
				//ResultSet rs = incrementOffsetStatement.executeQuery();
				
				//if(rs.next()){
					String statementString = "CREATE TABLE IF NOT EXISTS `items` ( " +
					"`id` bigint unsigned NOT NULL AUTO_INCREMENT, " +
					"`name` text NOT NULL, " +
					"`description` text, " +
					"`startPrice` double unsigned NOT NULL, " +
					"`quantity` int(10) unsigned NOT NULL, " +
					"`reservePrice` double unsigned NOT NULL DEFAULT '0', " +
					"`buyNowPrice` double unsigned DEFAULT NULL, " +
					"`noOfBids` int(10) unsigned NOT NULL DEFAULT '0', " +
					"`currentBid` double unsigned NOT NULL DEFAULT '0', " +
					"`startDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00', " +
					"`endDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00', " +
					"`sellerID` bigint unsigned NOT NULL, " +
					"`categoryID` bigint unsigned NOT NULL, " +
					"`maxBid` double unsigned NOT NULL DEFAULT '0', " +
					"`currentWinner` bigint unsigned DEFAULT NULL, " +
					"`thumbnail` text, " +
					"`ts` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
					"PRIMARY KEY (`id`), " +
					"KEY `userIDIndex` (`sellerID`), " +
					"KEY `categoryIDIndex` (`categoryID`), " +
					"KEY `enddateIndex` (`endDate`)";
					if(fulltextsearch) statementString += ", FULLTEXT KEY `textSearchIndex` (`name`,`description`) ";
					statementString += ") ENGINE=" + engine + "  DEFAULT CHARSET=latin1 AUTO_INCREMENT=" + (CreateAll.NO_OF_OLD_ITEMS+1);
					
					PreparedStatement createStatement = conn.prepareStatement(statementString);
					
					
					createStatement.executeUpdate();
					
					dropStatement.close();
					createStatement.close();
					
					if(disableIndexes){
						PreparedStatement disable = conn.prepareStatement("ALTER TABLE `items` DISABLE KEYS");
						disable.execute();
						disable.close();
					}
					
					deleted = true;
				//}
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (dropAddItems): Could not delete the items");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (dropAddItems): The database connection is not open");
		}
		
		returnConn(conn);
		return deleted;
	}
	
	public boolean dropAddOldItems(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement dropStatement = conn.prepareStatement("DROP TABLE IF EXISTS `oldItems`");
				
				PreparedStatement createStatement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `oldItems` ( " +
						"`id` bigint unsigned NOT NULL AUTO_INCREMENT, " +
						"`name` text NOT NULL, " +
						"`description` text, " +
						"`startPrice` double unsigned NOT NULL, " +
						"`quantity` int(10) unsigned NOT NULL, " +
						"`reservePrice` double unsigned NOT NULL DEFAULT '0', " +
						"`buyNowPrice` double unsigned DEFAULT NULL, " +
						"`noOfBids` int(10) unsigned NOT NULL DEFAULT '0', " +
						"`currentBid` double unsigned NOT NULL DEFAULT '0', " +
						"`startDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00', " +
						"`endDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00', " +
						"`sellerID` bigint unsigned NOT NULL, " +
						"`categoryID` bigint unsigned NOT NULL, " +
						"`maxBid` double unsigned NOT NULL DEFAULT '0', " +
						"`currentWinner` bigint unsigned DEFAULT NULL, " +
						"`thumbnail` text, " +
						"`ts` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
						"PRIMARY KEY (`id`), " +
						"KEY `userIDIndex` (`sellerID`), " +
						"KEY `categoryIDIndex` (`categoryID`) " +
						") ENGINE=" + engine + "  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
				
				dropStatement.executeUpdate();
				createStatement.executeUpdate();
				
				dropStatement.close();
				createStatement.close();
				
				if(disableIndexes){
					PreparedStatement disable = conn.prepareStatement("ALTER TABLE `oldItems` DISABLE KEYS");
					disable.execute();
					disable.close();
				}
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (dropAddOldItems): Could not delete the items");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (dropAddOldItems): The database connection is not open");
		}
		
		returnConn(conn);
		return deleted;
	}
	
	public long insertItem(String name, String description, double startPrice, int quantity, double reservePrice, double buyNowPrice, Date startDate, Date endDate, long sellerID, long categoryID, StringBuffer sql){
		long itemID = -1;
		Connection conn = this.getConnection();
		PreparedStatement statement = null;
		PreparedStatement getID = null;
		ResultSet rs = null;
		
		if(conn != null){
			try{
				// Create the SQL statement to insert the item
				statement = conn.prepareStatement("INSERT INTO `items` (name, description, startPrice, quantity, reservePrice, buyNowPrice, noOfBids, currentBid, startDate, endDate, sellerID, categoryID, maxBid, thumbnail) "
						+ "VALUES (?, ?, ?, ?, ?, ?, 0, 0, ?, ?, ?, ?, 0, ?)");
				statement.setString(1, name);
				statement.setString(2, description);
				statement.setDouble(3, startPrice);
				statement.setInt(4, quantity);
				statement.setDouble(5, reservePrice);
				statement.setDouble(6, buyNowPrice);
				statement.setTimestamp(7, new java.sql.Timestamp(startDate.getTime()));
				statement.setTimestamp(8, new java.sql.Timestamp(endDate.getTime()));
				statement.setLong(9, sellerID);
				statement.setLong(10, categoryID);
				statement.setString(11, "blank.jpg");

				int rows = statement.executeUpdate();
				
				// If there is a result, get the item ID number
				if(rows == 1){
					getID = conn.prepareStatement("SELECT LAST_INSERT_ID()");
					rs = getID.executeQuery();
					
					if(rs.next())
						itemID = rs.getLong(1);
				}
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (insertItem): Could not insert the item");
				e.printStackTrace();
			}
			finally{
				close(rs);
				close(getID);
				close(statement);
				
				returnConn(conn);
			}
		}
		else{
			System.err.println("MySQLDBPopulator (insertItem): The database connection is not open");
		}
		
		return itemID;
	}
	
	public long insertOldItem(String name, String description, double startPrice, int quantity, double reservePrice, double buyNowPrice, Date startDate, Date endDate, long sellerID, long categoryID, StringBuffer sql){
		long itemID = -1;
		Connection conn = this.getConnection();
		
		PreparedStatement statement = null;
		PreparedStatement getID = null;
		ResultSet rs = null;
		
		if(conn != null){
			try{
				// Create the SQL statement to insert the item
				statement = conn.prepareStatement("INSERT INTO `oldItems` (name, description, startPrice, quantity, reservePrice, buyNowPrice, noOfBids, currentBid, startDate, endDate, sellerID, categoryID, maxBid, thumbnail) "
						+ "VALUES (?, ?, ?, ?, ?, ?, 0, 0, ?, ?, ?, ?, 0, ?)");
				statement.setString(1, name);
				statement.setString(2, description);
				statement.setDouble(3, startPrice);
				statement.setInt(4, quantity);
				statement.setDouble(5, reservePrice);
				statement.setDouble(6, buyNowPrice);
				statement.setTimestamp(7, new java.sql.Timestamp(startDate.getTime()));
				statement.setTimestamp(8, new java.sql.Timestamp(endDate.getTime()));
				statement.setLong(9, sellerID);
				statement.setLong(10, categoryID);
				statement.setString(11, "blank.jpg");

				int rows = statement.executeUpdate();
				
				// If there is a result, get the item ID number
				if(rows == 1){
					getID = conn.prepareStatement("SELECT LAST_INSERT_ID()");
					rs = getID.executeQuery();
					
					if(rs.next())
						itemID = rs.getLong(1);
				}
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (insertOldItem): Could not insert the item");
				e.printStackTrace();
			}
			finally{
				close(rs);
				close(getID);
				close(statement);
				
				returnConn(conn);
			}
		}
		else{
			System.err.println("MySQLDBPopulator (insertOldItem): The database connection is not open");
		}

		return itemID;
	}
	
	public boolean dropAddBids(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement dropStatement = conn.prepareStatement("DROP TABLE IF EXISTS `bids`");
				dropStatement.executeUpdate();
				
				//PreparedStatement incrementOffsetStatement = conn.prepareStatement("SELECT COUNT(*) from `oldBids`");
				//ResultSet rs = incrementOffsetStatement.executeQuery();
				
				//if(rs.next()){
					PreparedStatement createStatement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `bids` ( " +
							"`id` bigint unsigned NOT NULL AUTO_INCREMENT, " +
							"`userID` bigint unsigned NOT NULL, " +
							"`itemID` bigint unsigned NOT NULL, " +
							"`quantity` int(10) unsigned NOT NULL, " +
							"`bid` double unsigned NOT NULL, " +
							"`maxBid` double unsigned NOT NULL, " +
							"`bidDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00', " +
							"PRIMARY KEY (`id`), " +
							"KEY `item` (`itemID`), " +
							"KEY `userIDIndex` (`userID`) " +
							") ENGINE=" + engine + "  DEFAULT CHARSET=latin1 AUTO_INCREMENT=" + (this.totalOldBids+1l));
					
					
					createStatement.executeUpdate();
					
					dropStatement.close();
					createStatement.close();
					
					if(disableIndexes){
						PreparedStatement disable = conn.prepareStatement("ALTER TABLE `bids` DISABLE KEYS");
						disable.execute();
						disable.close();
					}
					
					deleted = true;
				//}
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (dropAddBids): Could not delete the bids");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (dropAddBids): The database connection is not open");
		}
		
		returnConn(conn);
		return deleted;
	}
	
	public boolean dropAddOldBids(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement dropStatement = conn.prepareStatement("DROP TABLE IF EXISTS `oldBids`");
				
				PreparedStatement createStatement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `oldBids` ( " +
						"`id` bigint unsigned NOT NULL AUTO_INCREMENT, " +
						"`userID` bigint unsigned NOT NULL, " +
						"`itemID` bigint unsigned NOT NULL, " +
						"`quantity` int(10) unsigned NOT NULL, " +
						"`bid` double unsigned NOT NULL, " +
						"`maxBid` double unsigned NOT NULL, " +
						"`bidDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00', " +
						"PRIMARY KEY (`id`), " +
						"KEY `item` (`itemID`), " +
						"KEY `userIDIndex` (`userID`) " +
						") ENGINE=" + engine + "  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
				
				dropStatement.executeUpdate();
				createStatement.executeUpdate();
				
				dropStatement.close();
				createStatement.close();
				
				if(disableIndexes){
					PreparedStatement disable = conn.prepareStatement("ALTER TABLE `oldBids` DISABLE KEYS");
					disable.execute();
					disable.close();
				}
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (dropAddOldBids): Could not delete the bids");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (dropAddOldBids): The database connection is not open");
		}
		
		returnConn(conn);
		return deleted;
	}
	
	public Item getItem(long itemID){
		if(itemID < 1) return null;
		
		Item result = null;
		int attemptsRemaining = 1;

		do {
			Connection conn = this.getConnection();

			if (conn != null) {
				PreparedStatement statement = null;
				ResultSet rs = null;			
				
				try {
					// Create the SQL statement to see get the item's details
					statement = conn.prepareStatement("SELECT * FROM `items` WHERE `items`.`id` = ?");
					statement.setLong(1, itemID);
					
					rs = statement.executeQuery();
					
					// If an item is returned then get the details
					if(rs.next()){
						
						// If we need to get the images, do that now
						ArrayList<Image> images = new ArrayList<Image>();
						

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
								images,null);
					}
					
					
					attemptsRemaining = 0;
				} catch (CommunicationsException e) {
					System.err.println("MySQLQuery (getXItem): Could not get the item "+ itemID);
					e.printStackTrace();
				} catch (MySQLNonTransientConnectionException e) {
					System.err.println("MySQLQuery (getXItem): Could not get the item "+ itemID);
					e.printStackTrace();
				} catch (Exception e) {
					System.err.println("MySQLQuery (getXItem): Could not get the item "+ itemID);
					e.printStackTrace();
				} finally{
					close(rs);
					close(statement);	
					
					returnConn(conn);
				}
			}

			attemptsRemaining--;
		} while (attemptsRemaining >= 0);
		
		
		return result;
	}
	
	public int getNoOfImages(long itemID){
		Connection conn = this.getConnection();
		int noOfImages = 0;
		
		if(conn != null){
			try{
				PreparedStatement getImageNumber = conn.prepareStatement("SELECT count(*) FROM `item_image` WHERE itemID=?");
				getImageNumber.setLong(1, itemID);
				
				ResultSet rs = getImageNumber.executeQuery();
				
				if(rs.next()){
					noOfImages = rs.getInt(1);
				}
				
				rs.close();
				getImageNumber.close();
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (getNoOfImages): Could not get no of images");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (getNoOfImages): The database connection is not open");
		}
		
		returnConn(conn);
		
		return noOfImages;
	}
	
	public void insertBid(Bid bid, long itemID, StringBuffer sql){
		//int bidID = -1;
		Connection conn = this.getConnection();
		PreparedStatement insertBidStatement = null;
		
		if(conn != null){
			try{
				// Create the SQL statement to insert the item
				insertBidStatement = conn.prepareStatement("INSERT INTO `bids` (userID, itemID, quantity, bid, maxBid, bidDate) "
						+ "VALUES (?, ?, ?, ?, ?, ?)");
				insertBidStatement.setLong(1, (long)bid.getUserID());
				insertBidStatement.setLong(2, (long)itemID);
				insertBidStatement.setInt(3, bid.getQuantity());
				insertBidStatement.setDouble(4, bid.getBid());
				insertBidStatement.setDouble(5, bid.getMaxBid());
				insertBidStatement.setTimestamp(6, new java.sql.Timestamp(bid.getBidDate().getTime()));

				int rows = insertBidStatement.executeUpdate();
				
				// If there is a result, get the item ID number
				/*if(rows == 1){
					PreparedStatement getID = conn.prepareStatement("SELECT LAST_INSERT_ID()");
					ResultSet rs = getID.executeQuery();
					
					if(rs.next())
						bidID = rs.getInt(1);
					
					rs.close();
					getID.close();
				}*/

			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (insertBid): Could not insert the bid");
				e.printStackTrace();
			}
			finally{
				close(insertBidStatement);
			}
		}
		else{
			System.err.println("MySQLDBPopulator (insertBid): The database connection is not open");
		}
		
		returnConn(conn);
		//return bidID;
	}
	
	int oldBidCount=0;
	private static PreparedStatement oldBidBuffer = null;
	private static String bufferOldBids = "buffOldBidsLock";
	private Connection bufferOldBidsConn;
	private long totalOldBids = 0l;
	private int maxBufferSize = 200;
	
	public void insertOldBid(Bid bid, long itemID, StringBuffer sql){
		try {
			if (oldBidBuffer == null) {
				synchronized (bufferOldBids) {
					if (oldBidBuffer == null) {
						bufferOldBidsConn = this.getConnection();
						bufferOldBidsConn.setAutoCommit(false);
						oldBidBuffer = bufferOldBidsConn
								.prepareStatement("INSERT INTO `oldBids` (userID, itemID, quantity, bid, maxBid, bidDate) "
										+ "VALUES (?, ?, ?, ?, ?, ?)");
						//So we know where to starts the bids id
						//totalOldBids++;;
					}
				}
			}
			
			synchronized (bufferOldBids) {
				oldBidBuffer.setLong(1, bid.getUserID());
				oldBidBuffer.setLong(2, itemID);
				oldBidBuffer.setInt(3, bid.getQuantity());
				oldBidBuffer.setDouble(4, bid.getBid());
				oldBidBuffer.setDouble(5, bid.getMaxBid());
				oldBidBuffer.setTimestamp(6, new java.sql.Timestamp(bid
						.getBidDate().getTime()));

				oldBidBuffer.addBatch();

				//So we know where to starts the bids id
				totalOldBids++;;
				
				oldBidCount++;

				if (oldBidCount > maxBufferSize) {
					oldBidCount = 0;
					oldBidBuffer.executeBatch();
					bufferOldBidsConn.commit();

					close(oldBidBuffer);

					oldBidBuffer = bufferOldBidsConn
							.prepareStatement("INSERT INTO `oldBids` (userID, itemID, quantity, bid, maxBid, bidDate) "
									+ "VALUES (?, ?, ?, ?, ?, ?)");
				}
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				// Create the SQL statement to insert the item
				PreparedStatement insertBidStatement = conn.prepareStatement("INSERT INTO `oldBids` (userID, itemID, quantity, bid, maxBid, bidDate) "
						+ "VALUES (?, ?, ?, ?, ?, ?)");
				insertBidStatement.setInt(1, bid.getUserID());
				insertBidStatement.setInt(2, itemID);
				insertBidStatement.setInt(3, bid.getQuantity());
				insertBidStatement.setDouble(4, bid.getBid());
				insertBidStatement.setDouble(5, bid.getMaxBid());
				insertBidStatement.setTimestamp(6, new java.sql.Timestamp(bid.getBidDate().getTime()));

				int rows = insertBidStatement.executeUpdate();
				
				// If there is a result, get the item ID number
				if(rows == 1){
					PreparedStatement getID = conn.prepareStatement("SELECT LAST_INSERT_ID()");
					ResultSet rs = getID.executeQuery();
					
					if(rs.next())
						bidID = rs.getInt(1);
					
					rs.close();
					getID.close();
				}

				insertBidStatement.close();
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (insertOldBid): Could not insert the bid");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (insertOldBid): The database connection is not open");
		}
		
		returnConn(conn);*/
		//return bidID;
	}
	
	public void insertComment(long userID, long sellerID, long itemID, int rating, long endDate, String comment, StringBuffer sql){
		Connection conn = this.getConnection();
		PreparedStatement statement = null;
		
		if(conn != null){
			try{
				// Create the SQL statement to insert the comment
				statement = conn.prepareStatement("INSERT INTO `comments` (from_user_id, to_user_id, item_id, rating, date, comment) "
										+ "VALUES (?, ?, ?, ?, ?, ?)");
				statement.setLong(1, userID);
				statement.setLong(2, sellerID);
				statement.setLong(3, itemID);
				statement.setInt(4, rating);
				statement.setTimestamp(5, new java.sql.Timestamp(endDate));
				statement.setString(6, comment);
				
				statement.executeUpdate();
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (insertComment): Could not insert the comment");
				e.printStackTrace();
			}
			finally{
				close(statement);
				returnConn(conn);
			}
		}
		else{
			System.err.println("MySQLDBPopulator (insertComment): The database connection is not open");
		}
		
		
	}
	
	public void insertAnswer(long userID, long toUserID, long itemID, Date date, String answer, long responseTo, StringBuffer sql){
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				// Create the SQL statement to insert the question
				PreparedStatement statement = conn.prepareStatement("INSERT INTO `questions` (from_user_id, to_user_id, item_id, is_question, date, content, responseTo) "
										+ "VALUES (?, ?, ?, ?, NOW(), ?, ?)");
				statement.setLong(1, userID);
				// Need Modification: who the question asked to is user who is asking
				statement.setLong(2, toUserID);
				statement.setLong(3, itemID);
				statement.setString(4, "N");
				statement.setString(5, answer);
				statement.setLong(6, responseTo);
				
				statement.executeUpdate();
				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (insertAnswer): Could not insert the answer");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (insertAnswer): The database connection is not open");
		}
		
		returnConn(conn);
	}
	
	public long insertQuestion(long fromUserID, long toUserID, long itemID, Date date, String question, StringBuffer sql){
		Connection conn = this.getConnection();
		PreparedStatement statement = null;
		PreparedStatement getID = null;
		ResultSet rs = null;
		
		long id = -1;
		
		if(conn != null){
			try{
				// Create the SQL statement to insert the question
				statement = conn.prepareStatement("INSERT INTO `questions` (from_user_id, to_user_id, item_id, is_question, date, content, responseTo) "
										+ "VALUES (?, ?, ?, ?, NOW(), ?, -1)");
				statement.setLong(1, fromUserID);
				statement.setLong(2, toUserID);
				statement.setLong(3, itemID);
				statement.setString(4, "Y");
				statement.setString(5, question);
				
				statement.executeUpdate();
				statement.close();
				
				getID = conn.prepareStatement("SELECT LAST_INSERT_ID()");
				rs = getID.executeQuery();
				
				if(rs.next())
					id = rs.getInt(1);
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (insertQuestion): Could not insert the question");
				e.printStackTrace();
			}
			finally{
				close(rs);
				close(getID);
				close(statement);
				
				returnConn(conn);
			}
		}
		else{
			System.err.println("MySQLDBPopulator (insertQuestion): The database connection is not open");
		}

		return id;
	}
	
	public void updateItemMaxBid(long itemID, int noOfBids, double currentBid, double maxBid, long currentWinner, long categoryID, StringBuffer sql){
		if(noOfBids>0){
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				// Create the SQL statement to insert the item
				PreparedStatement updateItemStatement = conn.prepareStatement("UPDATE `items` SET `items`.`noOfBids` = ?, "
						+ "`items`.`currentBid` = ?, `items`.`maxBid` = ?, `items`.`currentWinner` = ? WHERE `items`.`id` = ?");
				updateItemStatement.setInt(1, noOfBids);
				updateItemStatement.setDouble(2, currentBid);
				updateItemStatement.setDouble(3, maxBid);
				updateItemStatement.setLong(4, currentWinner);
				updateItemStatement.setLong(5, itemID);

				updateItemStatement.executeUpdate();

				updateItemStatement.close();
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (insertBid): Could not insert the bid");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (insertBid): The database connection is not open");
		}
		
		returnConn(conn);
		}
	}
	
	private int updateOldItemMaxBidCount = 0;
	private PreparedStatement updateOldItemMaxBidBuf;
	private static String bufferOldItemMaxBidUpdates = "oldItemBufLock";
	private Connection updateOldItemMaxBidConn;
	
	public void updateOldItemMaxBid(long itemID, int noOfBids, double currentBid, double maxBid, long currentWinner, StringBuffer sql){
		try{
			if (updateOldItemMaxBidBuf == null) {
				synchronized (bufferOldItemMaxBidUpdates) {
					if (updateOldItemMaxBidBuf == null) {
						updateOldItemMaxBidConn = this.getConnection();
						updateOldItemMaxBidConn.setAutoCommit(false);
						updateOldItemMaxBidBuf = updateOldItemMaxBidConn
								.prepareStatement("UPDATE `oldItems` SET `oldItems`.`noOfBids` = ?, "
										+ "`oldItems`.`currentBid` = ?, `oldItems`.`maxBid` = ?, `oldItems`.`currentWinner` = ? WHERE `oldItems`.`id` = ?");
						
					}
				}
			}
			
			synchronized (bufferOldItemMaxBidUpdates) {
				updateOldItemMaxBidBuf.setInt(1, noOfBids);
				updateOldItemMaxBidBuf.setDouble(2, currentBid);
				updateOldItemMaxBidBuf.setDouble(3, maxBid);
				updateOldItemMaxBidBuf.setLong(4, currentWinner);
				updateOldItemMaxBidBuf.setLong(5, itemID);

				updateOldItemMaxBidBuf.addBatch();
				
				updateOldItemMaxBidCount++;

				if (updateOldItemMaxBidCount > maxBufferSize) {
					updateOldItemMaxBidCount = 0;
					updateOldItemMaxBidBuf.executeBatch();
					updateOldItemMaxBidConn.commit();

					close(updateOldItemMaxBidBuf);
					
					
					updateOldItemMaxBidBuf = updateOldItemMaxBidConn
							.prepareStatement("UPDATE `oldItems` SET `oldItems`.`noOfBids` = ?, "
									+ "`oldItems`.`currentBid` = ?, `oldItems`.`maxBid` = ?, `oldItems`.`currentWinner` = ? WHERE `oldItems`.`id` = ?");
					
				}
			}	
			}catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		/*Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				// Create the SQL statement to insert the item
				PreparedStatement updateItemStatement = conn.prepareStatement("UPDATE `oldItems` SET `oldItems`.`noOfBids` = ?, "
						+ "`oldItems`.`currentBid` = ?, `oldItems`.`maxBid` = ?, `oldItems`.`currentWinner` = ? WHERE `oldItems`.`id` = ?");
				updateItemStatement.setInt(1, noOfBids);
				updateItemStatement.setDouble(2, currentBid);
				updateItemStatement.setDouble(3, maxBid);
				updateItemStatement.setInt(4, currentWinner);
				updateItemStatement.setInt(5, itemID);

				updateItemStatement.executeUpdate();

				updateItemStatement.close();
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (insertOldBid): Could not insert the bid");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (insertOldBid): The database connection is not open");
		}
		
		returnConn(conn);*/
		//return bidID;
	}
	
	public boolean dropAddCategories(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement dropStatement = conn.prepareStatement("DROP TABLE IF EXISTS `categories`");
				
				PreparedStatement createStatement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `categories` ( " +
						"`id` bigint unsigned NOT NULL, " +
						"`name` varchar(50) NOT NULL, " +
						"`parent` bigint unsigned DEFAULT '0', " +
						"`ts` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
						"PRIMARY KEY (`id`) " +
						// "KEY `parentIndex` (`parent`)" +
						") ENGINE=" + engine + "  DEFAULT CHARSET=latin1 AUTO_INCREMENT=0");
				
				dropStatement.executeUpdate();
				createStatement.executeUpdate();
				
				dropStatement.close();
				createStatement.close();
				
				if(disableIndexes){
					PreparedStatement disable = conn.prepareStatement("ALTER TABLE `categories` DISABLE KEYS");
					disable.execute();
					disable.close();
				}
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (dropAddCategories): Could not delete the categories");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (dropAddCategories): The database connection is not open");
		}
		
		returnConn(conn);
		return deleted;
	}
	
	private int categoryCount = 0;
	private PreparedStatement insertCategoriesBuf = null;
	private Connection insertCategoriesConn = null;
	private Boolean insertBuffer = true;
	
	public void insertCategory(long id, String name, long parent){
		synchronized(insertBuffer){
		if(insertCategoriesConn==null){
			insertCategoriesConn = this.getConnection();
			
			try {
				insertCategoriesConn.setAutoCommit(false);
				insertCategoriesBuf = insertCategoriesConn
						.prepareStatement("INSERT INTO `categories` (id, name, parent) "
							+ "VALUES (?, ?, ?)");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(insertCategoriesConn != null){
			try{
				insertCategoriesBuf.setLong(1, id);
				insertCategoriesBuf.setString(2, name);
				insertCategoriesBuf.setLong(3, parent);
				
				insertCategoriesBuf.addBatch();

				categoryCount++;

				if (categoryCount > maxBufferSize) {
					categoryCount = 0;
					if(insertCategoriesBuf!=null) insertCategoriesBuf.executeBatch();
					insertCategoriesConn.commit();

					close(insertCategoriesBuf);

					insertCategoriesBuf = insertCategoriesConn
							.prepareStatement("INSERT INTO `categories` (id, name, parent) "
								+ "VALUES (?, ?, ?)");
				}
			}
			catch(Exception e){
				
			}
		}
		else{
			System.err.println("MySQLDBPopulator (insertCategory): The database connection is not open");
		}
		}
		
	}
	
	public boolean dropAddImages(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement dropStatement = conn.prepareStatement("DROP TABLE IF EXISTS `images`");
				
				PreparedStatement createStatement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `images` ( " +
						"`id` bigint unsigned NOT NULL AUTO_INCREMENT, " +
						"`url` text NOT NULL, " +
						"`description` text, " +
						"PRIMARY KEY (`id`) " +
						") ENGINE=" + engine + "  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
				
				dropStatement.executeUpdate();
				createStatement.executeUpdate();
				
				dropStatement.close();
				createStatement.close();
				
				if(disableIndexes){
					PreparedStatement disable = conn.prepareStatement("ALTER TABLE `images` DISABLE KEYS");
					disable.execute();
					disable.close();
				}
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (dropAddImages): Could not delete the images");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (dropAddImages): The database connection is not open");
		}
		
		returnConn(conn);
		return deleted;
	}
	
	public boolean dropAddItemImage(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement dropStatement = conn.prepareStatement("DROP TABLE IF EXISTS `item_image`");
				
				PreparedStatement createStatement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `item_image` ( " +
						"`id` bigint unsigned NOT NULL AUTO_INCREMENT, " +
						"`itemID` bigint unsigned NOT NULL, " +
						"`imageID` bigint unsigned NOT NULL, " +
						"`position` int(2) unsigned NOT NULL, " +
						"PRIMARY KEY (`id`), " +
						"KEY `imageIDIndex` (`itemID`) " +
						") ENGINE=" + engine + "  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
				
				dropStatement.executeUpdate();
				createStatement.executeUpdate();
				
				dropStatement.close();
				createStatement.close();
				
				if(disableIndexes){
					PreparedStatement disable = conn.prepareStatement("ALTER TABLE `item_image` DISABLE KEYS");
					disable.execute();
					disable.close();
				}
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (dropAddItemImage): Could not delete the item_images");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (dropAddItemImage): The database connection is not open");
		}
		
		returnConn(conn);
		return deleted;
	}
	
	public boolean dropAddPayments(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement dropStatement = conn.prepareStatement("DROP TABLE IF EXISTS `payments`");
				
				PreparedStatement createStatement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `payments` ( " +
						"`id` bigint unsigned NOT NULL AUTO_INCREMENT, " +
						"`userID` bigint unsigned NOT NULL, " +
						"`itemID` bigint unsigned NOT NULL, " +
						"`quantity` int(10) unsigned NOT NULL, " +
						"`price` double unsigned NOT NULL, " +
						"`paidDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
						"`street` varchar(50) NOT NULL, " +
						"`town` varchar(50) NOT NULL, " +
						"`zip` varchar(10) NOT NULL, " +
						"`state` varchar(20) NOT NULL, " +
						"`creditCardNo` varchar(16) NOT NULL, " +
						"`cvv` varchar(4) NOT NULL, " +
						"`expirationDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00', " +
						"`nameOnCard` varchar(50) NOT NULL, " +
						"PRIMARY KEY (`id`) " +
						") ENGINE=" + engine + "  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
				
				dropStatement.executeUpdate();
				createStatement.executeUpdate();
				
				dropStatement.close();
				createStatement.close();
				
				if(disableIndexes){
					PreparedStatement disable = conn.prepareStatement("ALTER TABLE `payments` DISABLE KEYS");
					disable.execute();
					disable.close();
				}
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (dropAddPayments): Could not delete the payments");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (dropAddPayments): The database connection is not open");
		}
		
		returnConn(conn);
		return deleted;
	}
	
	
	public boolean dropAddPurchased(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement dropStatement = conn.prepareStatement("DROP TABLE IF EXISTS `purchased`");
				
				PreparedStatement createStatement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `purchased` ( " +
						"`id` bigint unsigned NOT NULL AUTO_INCREMENT, " +
						"`userID` bigint unsigned NOT NULL, " +
						"`itemID` bigint unsigned NOT NULL, " +
						"`quantity` int(10) unsigned NOT NULL, " +
						"`price` double unsigned NOT NULL, " +
						"`purchaseDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00', " +
						"`accountID` bigint unsigned DEFAULT NULL, " +
						"`paid` tinyint(1) DEFAULT '0', " +
						"`paidDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00', " +
						"`ts` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
						"PRIMARY KEY (`id`), " +
						"KEY `itemIndex` (`itemID`), " +
						"KEY `userIndex` (`userID`) " +
						") ENGINE=" + engine + "  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
				
				dropStatement.executeUpdate();
				createStatement.executeUpdate();
				
				dropStatement.close();
				createStatement.close();
				
				if(disableIndexes){
					PreparedStatement disable = conn.prepareStatement("ALTER TABLE `purchased` DISABLE KEYS");
					disable.execute();
					disable.close();
				}
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (dropAddPurchased): Could not delete the purchased");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (dropAddPurchased): The database connection is not open");
		}
		
		returnConn(conn);
		return deleted;
	}
	
	//TODO: not working as the item price has not been updated, pass winner
	public long insertPurchase(long itemID, Boolean paid, long paidDate, long winnerID, double currentBid, long quantity, StringBuffer sql){
		
	//public long insertPurchase(long itemID, Boolean paid, long paidDate, StringBuffer sql){
		long purchaseID = -1;
		Connection conn = this.getConnection();
		PreparedStatement statement = null;
		
		if(conn != null){
			try{
				// Create the SQL statement to get the items details
				//PreparedStatement getItem = conn.prepareStatement("SELECT currentWinner, quantity, currentBid, endDate FROM `oldItems` WHERE `oldItems`.`id` = ?");
				//getItem.setLong(1, itemID);
				//ResultSet itemrs = getItem.executeQuery();
				
				//if(itemrs.next()){
					// Create the SQL statement to insert the purchase info
				
				if(paid){
					statement = conn.prepareStatement("INSERT INTO `purchased` (userID, itemID, quantity, price, purchaseDate, accountID, paid, paidDate) "
							+ "VALUES (?, ?, ?, ?, ?, NULL, ?, ?)");
					statement.setLong(1, winnerID);
					statement.setLong(2, itemID);
					statement.setInt(3, (int)quantity);
					statement.setDouble(4, (currentBid*(double)quantity));
					statement.setTimestamp(5, new java.sql.Timestamp(new Date().getTime()));
					statement.setBoolean(6, paid);
					statement.setTimestamp(7, new java.sql.Timestamp(paidDate));
					
				}
				else{
					statement = conn.prepareStatement("INSERT INTO `purchased` (userID, itemID, quantity, price, purchaseDate, accountID, paid) "
							+ "VALUES (?, ?, ?, ?, ?, NULL, ?)");
					statement.setLong(1, winnerID);
					statement.setLong(2, itemID);
					statement.setInt(3, (int)quantity);
					statement.setDouble(4, (currentBid*(double)quantity));
					statement.setTimestamp(5, new java.sql.Timestamp(new Date().getTime()));
					statement.setBoolean(6, paid);
				}
				
					int rows = statement.executeUpdate();
					
					// If there is a result, get the purchase ID number
					if(rows == 1){
						PreparedStatement getID = conn.prepareStatement("SELECT LAST_INSERT_ID()");
						ResultSet rs = getID.executeQuery();
						
						if(rs.next())
							purchaseID = rs.getLong(1);
						
						rs.close();
						getID.close();
					}
					
					
				//}
				
				//itemrs.close();
				//getItem.close();
				
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (insertPurchase): Could not insert the purchase");
				e.printStackTrace();
			}
			finally{
				close(statement);
				returnConn(conn);
			}
		}
		else{
			System.err.println("MySQLDBPopulator (insertPurchase): The database connection is not open");
		}
		
		
		return purchaseID;
	}
	
	public boolean dropAddStates(){
		boolean deleted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement dropStatement = conn.prepareStatement("DROP TABLE IF EXISTS `states`");
				
				PreparedStatement createStatement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `states` ( " +
						"`id` bigint NOT NULL AUTO_INCREMENT, " +
						"`shortName` varchar(2) NOT NULL, " +
						"`longName` varchar(35) NOT NULL, " +
						"PRIMARY KEY (`id`) " +
						") ENGINE=" + engine + "  DEFAULT CHARSET=latin1 AUTO_INCREMENT=1");
				
				dropStatement.executeUpdate();
				createStatement.executeUpdate();
				
				dropStatement.close();
				createStatement.close();
				
				if(disableIndexes){
					PreparedStatement disable = conn.prepareStatement("ALTER TABLE `states` DISABLE KEYS");
					disable.execute();
					disable.close();
				}
				
				deleted = true;
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (dropAddStates): Could not delete the states");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (dropAddStates): The database connection is not open");
		}
		
		returnConn(conn);
		return deleted;
	}
	
	public int insertState(int fake, String shortName, String longName){
		int stateID = -1;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				// Create the SQL statement to insert the user
				PreparedStatement statement = conn.prepareStatement("INSERT INTO `states` (shortName, longName) "
						+ "VALUES (?, ?)");
				statement.setString(1, shortName);
				statement.setString(2, longName);

				int rows = statement.executeUpdate();
				
				// If there is a result, get the states ID number
				if(rows == 1){
					PreparedStatement getID = conn.prepareStatement("SELECT LAST_INSERT_ID()");
					ResultSet rs = getID.executeQuery();
					
					if(rs.next())
						stateID = rs.getInt(1);
					
					rs.close();
					getID.close();
				}

				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (insertState): Could not insert the state");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (insertState): The database connection is not open");
		}
		
		returnConn(conn);
		return stateID;
	}
	
	
	
	public void disableIndexes(boolean disable){
		disableIndexes = disable;
		/*Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				PreparedStatement items = conn.prepareStatement("ALTER TABLE `items` DISABLE KEYS");
				items.execute();
				items.close();
				
				PreparedStatement oldItems = conn.prepareStatement("ALTER TABLE `oldItems` DISABLE KEYS");
				oldItems.execute();
				oldItems.close();
				
				PreparedStatement users = conn.prepareStatement("ALTER TABLE `users` DISABLE KEYS");
				users.execute();
				users.close();
				
				PreparedStatement addresses = conn.prepareStatement("ALTER TABLE `addresses` DISABLE KEYS");
				addresses.execute();
				addresses.close();
				
				PreparedStatement bids = conn.prepareStatement("ALTER TABLE `bids` DISABLE KEYS");
				bids.execute();
				bids.close();
				
				PreparedStatement oldBids = conn.prepareStatement("ALTER TABLE `oldBids` DISABLE KEYS");
				oldBids.execute();
				oldBids.close();
				
				PreparedStatement categories = conn.prepareStatement("ALTER TABLE `categories` DISABLE KEYS");
				categories.execute();
				categories.close();
				
				PreparedStatement purchased = conn.prepareStatement("ALTER TABLE `purchased` DISABLE KEYS");
				purchased.execute();
				purchased.close();
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (disableIndexes): Could not disable the indexes");
				e.printStackTrace();
			}
		}
		
		returnConn(conn);*/
	}
	
	public void enableIndexes(){
Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				//TODO: should check which tables exist
				try{
				PreparedStatement items = conn.prepareStatement("ALTER TABLE `items` ENABLE KEYS");
				items.execute();
				items.close();} catch(Exception ignore){}
				
				try{
				PreparedStatement oldItems = conn.prepareStatement("ALTER TABLE `oldItems` ENABLE KEYS");
				oldItems.execute();
				oldItems.close();} catch(Exception ignore){}

				try{
				PreparedStatement users = conn.prepareStatement("ALTER TABLE `users` ENABLE KEYS");
				users.execute();
				users.close();} catch(Exception ignore){}

				try{
				PreparedStatement addresses = conn.prepareStatement("ALTER TABLE `addresses` ENABLE KEYS");
				addresses.execute();
				addresses.close();} catch(Exception ignore){}

				try{
				PreparedStatement bids = conn.prepareStatement("ALTER TABLE `bids` ENABLE KEYS");
				bids.execute();
				bids.close();} catch(Exception ignore){}

				try{
				PreparedStatement oldBids = conn.prepareStatement("ALTER TABLE `oldBids` ENABLE KEYS");
				oldBids.execute();
				oldBids.close();} catch(Exception ignore){}

				try{
				PreparedStatement categories = conn.prepareStatement("ALTER TABLE `categories` ENABLE KEYS");
				categories.execute();
				categories.close();} catch(Exception ignore){}

				try{
				PreparedStatement states = conn.prepareStatement("ALTER TABLE `states` ENABLE KEYS");
				states.execute();
				states.close();} catch(Exception ignore){}

				try{
				PreparedStatement purchased = conn.prepareStatement("ALTER TABLE `purchased` ENABLE KEYS");
				purchased.execute();
				purchased.close();} catch(Exception ignore){}

				try{
				PreparedStatement payments = conn.prepareStatement("ALTER TABLE `payments` ENABLE KEYS");
				payments.execute();
				payments.close();} catch(Exception ignore){}

				try{
				PreparedStatement itemImage = conn.prepareStatement("ALTER TABLE `item_image` ENABLE KEYS");
				itemImage.execute();
				itemImage.close();} catch(Exception ignore){}

				try{
				PreparedStatement images = conn.prepareStatement("ALTER TABLE `images` ENABLE KEYS");
				images.execute();
				images.close();} catch(Exception ignore){}
				
				try{
					PreparedStatement questions = conn.prepareStatement("ALTER TABLE `questions` ENABLE KEYS");
					questions.execute();
					questions.close();} catch(Exception ignore){}
				
				try{
					PreparedStatement comments = conn.prepareStatement("ALTER TABLE `comments` ENABLE KEYS");
					comments.execute();
					comments.close();} catch(Exception ignore){}
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (enableIndexes): Could not enable the indexes");
				e.printStackTrace();
			}
		}
		
		returnConn(conn);
	}
	
	private static final String catLock = "catLock";
	private static final String oldItemLock = "oldItemLock";
	private static final String oldBidLock = "oldBidLock";
	
	public void flushAllBuffersDB(){
		synchronized(catLock){
		if(insertCategoriesBuf !=null){
			synchronized(catLock){
				if(insertCategoriesBuf !=null){
				categoryCount = 0;
				try {
					insertCategoriesBuf.executeBatch();
					
					insertCategoriesConn.commit();

					insertCategoriesBuf.close();

					insertCategoriesBuf = insertCategoriesConn
							.prepareStatement("INSERT INTO `categories` (id, name, parent) "
								+ "VALUES (?, ?, ?)");
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				}
				
			}
		}
		}
			
		synchronized(oldItemLock){
		if(updateOldItemMaxBidBuf !=null)
		synchronized (oldItemLock){
			if(updateOldItemMaxBidBuf !=null){
			try{
			updateOldItemMaxBidCount = 0;
			updateOldItemMaxBidBuf.executeBatch();
			updateOldItemMaxBidConn.commit();

			updateOldItemMaxBidBuf.close();
			
			updateOldItemMaxBidBuf = updateOldItemMaxBidConn
			.prepareStatement("UPDATE `oldItems` SET `oldItems`.`noOfBids` = ?, "
					+ "`oldItems`.`currentBid` = ?, `oldItems`.`maxBid` = ?, `oldItems`.`currentWinner` = ? WHERE `oldItems`.`id` = ?");
	
			}
			catch(Exception e){
				//e.printStackTrace();
			}
			}
		}
		}
		
		synchronized(oldBidLock){
		if(oldBidBuffer !=null)
		synchronized (oldBidLock){
			if(oldBidBuffer !=null){
			try{
				oldBidCount = 0;
				oldBidBuffer.executeBatch();
				bufferOldBidsConn.commit();

				oldBidBuffer.close();

				oldBidBuffer = bufferOldBidsConn
						.prepareStatement("INSERT INTO `oldBids` (userID, itemID, quantity, bid, maxBid, bidDate) "
								+ "VALUES (?, ?, ?, ?, ?, ?)");
			}
			catch(Exception e){
				//e.printStackTrace();
			}
			}
		}
		}
		
	}
	
	public void setMyISAM(){
		this.engine = "MyISAM";
	}
	
	public void setMyINNODB(){
		this.engine = "INNODB";
	}
	
	/*public boolean insertBid(int userID, int itemID, int quantity, int bid, int maxBid, Date bidDate){
		boolean inserted = false;
		Connection conn = this.getConnection();
		
		if(conn != null){
			try{
				// Create the SQL statement to insert the user
				PreparedStatement statement = conn.prepareStatement("INSERT INTO `bids` (userID, itemID, quantity, bid, maxBid, bidDate) "
						+ "VALUES (?, ?, ?, ?, ?, ?)");
				statement.setInt(1, userID);
				statement.setInt(2, itemID);
				statement.setInt(3, quantity);
				statement.setInt(4, bid);
				statement.setInt(5, maxBid);
				statement.setDate(6, new java.sql.Date(bidDate.getTime()));

				int rows = statement.executeUpdate();
				
				// If there is a result, set inserted to true
				if(rows == 1){
					inserted = true;
				}

				statement.close();
			}
			catch(Exception e){
				System.err.println("MySQLDBPopulator (insertBid): Could not insert the bid");
				e.printStackTrace();
			}
		}
		else{
			System.err.println("MySQLDBPopulator (insertBid): The database connection is not open");
		}
		
		return inserted;
	}*/
}
