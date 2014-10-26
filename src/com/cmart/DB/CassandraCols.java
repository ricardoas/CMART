package com.cmart.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class CassandraCols {
	public static void main(String[] args){
		String URL = "jdbc:cassandra://10.22.0.103:9160/CMARTv15";
		String DRIVER = "org.apache.cassandra.cql.jdbc.CassandraDriver";
		Connection conn = null;
		
		boolean createSpace = false;
		boolean delete = false;
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
		
		try {
			Class.forName(DRIVER);
			conn = DriverManager.getConnection(URL);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		PreparedStatement statement=null;
		ResultSet rs=null;
		String CQL =null;
		
		if(createSpace){
			try {
				statement = conn.prepareStatement("create keyspace CMARTv16 WITH gc_grace_seconds=30 AND strategy_options:replication_factor=3 AND strategy_class = 'SimpleStrategy' AND durable_writes=false;");
				//statement = conn.prepareStatement("create keyspace CMARTv11 WITH gc_grace_seconds=30 AND strategy_options:DC1 = '2' AND replication_factor = '2' AND strategy_class = 'NetworkTopologyStrategy';");
				
				statement.executeUpdate();
			} catch (Exception e) {	e.printStackTrace(); }
		}
		
		if (delete) {
			for(int i=0; i<5; i++){
				try {
					conn.close();
				} catch (SQLException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				try {
					Class.forName(DRIVER);
					conn = DriverManager.getConnection(URL);
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
			/*try {
				statement = conn.prepareStatement("INSERT INTO users (KEY, userid) VALUES ('5675', 657) ");
				statement.executeUpdate();
			} catch (Exception e) {	e.printStackTrace(); }*/
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
				CQL = "CREATE COLUMNFAMILY users (KEY bigint PRIMARY KEY, username text, password text, username_password text, authtoken text, firstname text,lastname text, email text, rating bigint) WITH rows_cached = 1000 AND compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}];";
				statement = conn.prepareStatement(CQL);
				statement.executeUpdate();

				// Used to confirm username and password
				CQL = "CREATE INDEX users_username_password_idx ON users (username_password); ";
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
				CQL = "CREATE COLUMNFAMILY addresses (KEY bigint PRIMARY KEY, userid bigint, street text, town text, zip text, state text, isdefault boolean, isDefaultKey bigint) WITH rows_cached = 100 AND compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}];";
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

				System.out.println("Dropping addresses successful");
			} catch (Exception e) {
				System.err.println("CassandraQuery: INIT no columnfamily items.");
				e.printStackTrace();
			}
			System.out.println("Adding items");
			try {
				//TODO: get rid of itemid, we just use key
				CQL="CREATE COLUMNFAMILY items (KEY bigint PRIMARY KEY, itemid bigint, name text, description text, thumbnail text, sellerid bigint, categoryid bigint, currentwinner bigint, quantity bigint, noofbids bigint, startdate bigint, enddate bigint, curbid text, maxbid text, startprice text,reserveprice text,buynowprice text) WITH gc_grace_seconds=15 AND rows_cached = 10000 AND compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}]; ";
				statement = conn.prepareStatement(CQL);
				statement.executeUpdate();

				// Used to get the items the user is selling
				CQL = "CREATE INDEX item_sellerid_idx ON items (sellerid); ";
				statement = conn.prepareStatement(CQL);
				statement.executeUpdate();
				
				// Used to list items by categoryID
				CQL = "CREATE INDEX item_categoryid_idx ON items (categoryid); ";
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
				//TODO: get rid of itemid, we just use key
				CQL="CREATE COLUMNFAMILY olditems (KEY bigint PRIMARY KEY, itemid bigint, name text, description text, thumbnail text, sellerid bigint, categoryid bigint, currentwinner bigint, quantity bigint, noofbids bigint, startdate bigint, enddate bigint, curbid text, maxbid text, startprice text,reserveprice text,buynowprice text) WITH compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}] ";
				statement = conn.prepareStatement(CQL);
				statement.executeUpdate();

				// Used to get the items the user has sold
				CQL = "CREATE INDEX olditem_sellerid_idx ON olditems (sellerid); ";
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
				CQL="CREATE COLUMNFAMILY images (KEY bigint PRIMARY KEY, URL text, description text, itemid bigint, position bigint) WITH rows_cached = 20000 AND compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}]; ";
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
				CQL="CREATE COLUMNFAMILY purchased (KEY bigint PRIMARY KEY, userid bigint, itemid bigint, purcashedquantity int, price text, purchasedate bigint, paid boolean, paiddate bigint, name text, description text, thumbnail text, sellerid bigint, categoryid bigint, currentwinner bigint, quantity bigint, noofbids bigint, startdate bigint, enddate bigint, curbid text, maxbid text) WITH compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}]; ";
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
				CQL="CREATE COLUMNFAMILY payments (KEY bigint PRIMARY KEY, userid bigint, itemid bigint, quantity int, price text, paiddate bigint, street text, town text, zip text, state text, nameoncard text, creditcardno text, cvv text, expirationdate bigint) WITH compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}]; ";
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
				CQL="CREATE COLUMNFAMILY bids (KEY bigint PRIMARY KEY, userid bigint, itemid bigint, quantity int, bid text, maxbid text, biddate bigint) WITH gc_grace_seconds=15 AND compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}]; ";
				statement = conn.prepareStatement(CQL);
				statement.executeUpdate();

				CQL="CREATE COLUMNFAMILY maxbids (KEY text PRIMARY KEY, bidkey bigint, userid bigint, itemid bigint, quantity int, bid text, maxbid text, biddate bigint) WITH comparator = UTF8Type AND gc_grace_seconds=15 AND compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}]; ";
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
				CQL="CREATE COLUMNFAMILY oldbids (KEY bigint PRIMARY KEY, userid bigint, itemid bigint, quantity int, bid text, maxbid text, biddate bigint) WITH compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}]; ";
				statement = conn.prepareStatement(CQL);
				statement.executeUpdate();

				CQL="CREATE COLUMNFAMILY maxoldbids (KEY text PRIMARY KEY, bidkey bigint, userid bigint, itemid bigint, quantity int, bid text, maxbid text, biddate bigint) WITH comparator = UTF8Type WITH AND compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}]; ";
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
				CQL="CREATE COLUMNFAMILY categories (KEY bigint PRIMARY KEY,parent bigint, name text, ts bigint) WITH rows_cached = 10000 AND compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}]; ";
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
				CQL="CREATE COLUMNFAMILY comments (KEY bigint PRIMARY KEY,from_user_id bigint, to_user_id bigint, itemid bigint, rating bigint, date bigint, comment text) WITH compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}]; ";
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
				CQL="CREATE COLUMNFAMILY questions (KEY bigint PRIMARY KEY,from_user_id bigint, to_user_id bigint, itemid bigint, is_question boolean, date bigint, content text) WITH compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}]; ";
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
				CQL="CREATE COLUMNFAMILY accounts (KEY bigint PRIMARY KEY,name text, nameoncard text, creditcardno text, cvv text, expirationdate bigint) WITH compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}]; ";
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
				CQL="CREATE COLUMNFAMILY states (KEY bigint PRIMARY KEY,shortname text, longname text) WITH rows_cached = 100 AND compression_options=[{sstable_compression:SnappyCompressor, chunk_length_kb:64}]; ";
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
}
