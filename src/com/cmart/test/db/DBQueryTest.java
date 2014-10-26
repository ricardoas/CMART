package com.cmart.test.db;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.cmart.DB.CassandraDBQuery;
import com.cmart.DB.DBQuery;
import com.cmart.Data.GlobalVars;
import com.cmart.util.Account;
import com.cmart.util.Address;
import com.cmart.util.Category;
import com.cmart.util.Comment;
import com.cmart.util.Image;
import com.cmart.util.Item;
import com.cmart.util.Question;
import com.cmart.util.User;

public class DBQueryTest {
	DBQuery db;
	boolean strict = false; // check for actions that may insert database junk, but will not create errors
	
	@Before
	public void setUp() throws Exception {
		GlobalVars.getInstance();
		db = GlobalVars.DB;
		db.initSolr();
		
		if(!db.usernameExists("dbtest1")) db.insertUser("dbtest1", "password1", "dbtest1@user.com", "user1", "1");
		if(!db.usernameExists("dbtest2")) db.insertUser("dbtest2", "password2", "dbtest2@user.com", "user2", "2");
		if(!db.usernameExists("dbtest3")) db.insertUser("dbtest3", "password3", "dbtest3@user.com", "user3", "3");
		
		/* To reset a SQL database for the tests
		
		DELETE FROM items WHERE sellerID = (SELECT id FROM users WHERE username="dbtest3");
		DELETE FROM oldItems WHERE sellerID = (SELECT id FROM users WHERE username="dbtest3");
		DELETE FROM purchased WHERE userID = (SELECT id FROM users WHERE username="dbtest4");
		DELETE FROM purchased WHERE userID = (SELECT id FROM users WHERE username="dbtest5");
		DELETE FROM bids WHERE userID = (SELECT id FROM users WHERE username="dbtest1");
		DELETE FROM bids WHERE userID = (SELECT id FROM users WHERE username="dbtest3");
		DELETE FROM bids WHERE userID = (SELECT id FROM users WHERE username="dbtest6");
		DELETE FROM oldBids WHERE userID = (SELECT id FROM users WHERE username="dbtest1");
		DELETE FROM oldBids WHERE userID = (SELECT id FROM users WHERE username="dbtest7");
		DELETE FROM addresses WHERE userID = (SELECT id FROM users WHERE username="dbtest9");
		DELETE FROM items WHERE categoryID=10000000;
		DELETE FROM items WHERE categoryID=10000005;
		DELETE FROM items WHERE categoryID=10000006;
		DELETE FROM items WHERE categoryID=10000007;
		DELETE FROM items WHERE categoryID=10000008;
		DELETE FROM items WHERE categoryID=10000009;
		DELETE from users WHERE username="dbtest1";
		DELETE from users WHERE username="dbtest2";
		DELETE from users WHERE username="dbtest3";
		DELETE from users WHERE username="dbtest4";
		DELETE from users WHERE username="dbtest5";
		DELETE from users WHERE username="dbtest6";
		DELETE from users WHERE username="dbtest7";
		DELETE from users WHERE username="dbtest8";
		DELETE from users WHERE username="dbtest9";
		DELETE from users WHERE username="username8";
		*/
	}

	@Test
	public void testGetRand() {
		int r1 = db.getRand();
		int r2 = db.getRand();
		int r3 = db.getRand();
		
		assertTrue("Generated random numbers should not be equal", r1!=r2 && r2!=r3 && r1!=r3);
	}

	@Test
	public void testAuthToken() {
		String a1 = db.authToken(1);
		String a2 = db.authToken(2);
		String a11 = db.authToken(1);
		
		assertTrue("Auth tokens must be unique across users and time", !a1.equals(a2) && !a1.equals(a11) && !a2.equals(a11));
	}

	@Test
	public void testGetConnections() {
		db.checkAuthToken(60l, "fdgdf");
		LinkedList<Connection> conns = db.getConnections();

		assertTrue("The connection pool should not be null", conns!=null);
		assertTrue("The connection pool should probably have connections (may not be true if you are using another pooling method)", conns.size()>0);
	}

	@Test
	public void testAddToSolr() {
		if(GlobalVars.SOLR_ENABLED){
			assertTrue("Could not insert in to Solr",db.addToSolr(1, "item 1", "desc 1", 4.5, new Date(System.currentTimeMillis())));
			assertTrue("Could not insert in to Solr",db.addToSolr(1, "item 1", "fgfdg", 4.5, new Date(System.currentTimeMillis())));
			assertTrue("Could not insert in to Solr",db.addToSolr(2, "item 2", "desc 8", 3.5, new Date(System.currentTimeMillis())));
		}
		
		assertFalse("The item should not have been inserted",db.addToSolr(-1, "item 7", "desc 8", 3.5, new Date(System.currentTimeMillis())));
		assertFalse("The item should not have been inserted",db.addToSolr(50, null, "desc 8", 3.5, new Date(System.currentTimeMillis())));
		assertFalse("The item should not have been inserted",db.addToSolr(50, "tesest", null, 3.5, new Date(System.currentTimeMillis())));
		assertFalse("The item should not have been inserted",db.addToSolr(50, "gfh", "desc 8", -5, new Date(System.currentTimeMillis())));
		assertFalse("The item should not have been inserted",db.addToSolr(50, "hhh", "desc 8", 3.5, null));
	}

	@Test
	public void testGetTextItemsSolr() {
		if(GlobalVars.SOLR_ENABLED){
			assertTrue("The results array should not be null", db.getTextItemsSolr("fgdfhgbdfgd", 0, 50, 0, true) != null);
			assertTrue("The results array should not be null", db.getTextItemsSolr(null, 0, 50, 0, true) != null);
			assertTrue("The results array should not be null", db.getTextItemsSolr("fgdfhgbdfgd", -5, 50, 0, true) != null);
			assertTrue("The results array should not be null", db.getTextItemsSolr("fgdfhgbdfgd", 0, -50, 78, true) != null);
			assertTrue("The results array should not be null", db.getTextItemsSolr("fgdfhgbdfgd", 0, 50, -78, true) != null);
			assertTrue("The results array should not be null", db.getTextItemsSolr("fgdfhgbdfgd", 5, 50, 2, null) != null);
			
			// Some items may already be present, so these tests will be incorrect
			assertTrue("WARNING: Solr is not empty, so the counts of items will be incorrect", db.getTextItemsSolr("fgdfhgbdfgd", 0, 50, 0, true).size()==0);
			
			// Insert some items and get them back again
			assertTrue("Could not insert in to Solr",db.addToSolr(13, "item 1", "desc 1", 1, new Date(System.currentTimeMillis()-20000))); // Should not be returned
			assertTrue("Could not insert in to Solr",db.addToSolr(1, "item 1", "desc 1", 1, new Date(System.currentTimeMillis()+20000)));
			assertTrue("Could not insert in to Solr",db.addToSolr(2, "item 2", "desc 2", 2, new Date(System.currentTimeMillis()+25000)));
			assertTrue("Could not insert in to Solr",db.addToSolr(50, "item 50", "description 50", 50, new Date(System.currentTimeMillis()+40000)));
			assertTrue("Could not insert in to Solr",db.addToSolr(100, "ignore 100", "ignore 50", 100, new Date(System.currentTimeMillis()+80000)));
			db.forceCommitSolr();
			
			// All items (should not return expired)
			assertTrue("WARNING: The results included expired items (depends if Solr was empty)", db.getTextItemsSolr(null, 0, 50, 0, true).size()==4);
			
			// Just the item with "description: in it
			assertTrue("WARNING: The number of results is incorrect (depends if Solr was empty)", db.getTextItemsSolr("description", 0, 50, 0, true).size()==1);
			
			// All items with 'item' in, i.e. 1,2,50
			ArrayList<Long> i1 = db.getTextItemsSolr("item", 0, 50, 0, true);
			assertTrue("The number of results is too small", i1.size()>=3);
			assertTrue("WARNING: The number of results may be too large (depends if Solr was empty)", i1.size()==3);
			
			// Check different sorting still produces the same items
			ArrayList<Long> i2 = db.getTextItemsSolr("item", 0, 50, 0, true);
			ArrayList<Long> i3 = db.getTextItemsSolr("item", 0, 50, 1, true);
			ArrayList<Long> i4 = db.getTextItemsSolr("item", 0, 50, 2, true);
			ArrayList<Long> i5 = db.getTextItemsSolr("item", 0, 50, 3, true);
			ArrayList<Long> i6 = db.getTextItemsSolr("item", 0, 50, -1, true);
			ArrayList<Long> i7 = db.getTextItemsSolr("item", 0, 50, 0, false);
			ArrayList<Long> i8 = db.getTextItemsSolr("item", 0, 50, 1, false);
			ArrayList<Long> i9 = db.getTextItemsSolr("item", 0, 50, 2, false);
			ArrayList<Long> i10 = db.getTextItemsSolr("item", 0, 50, 3, false);
			ArrayList<Long> i11 = db.getTextItemsSolr("item", 0, 50, -1, false);
			
			assertTrue("Sorting the data is causing different number of results to be returned",
					i1.size()==i2.size() && i1.size()==i3.size() && i1.size()==i4.size() && i1.size()==i5.size() &&
					i1.size()==i6.size() && i1.size()==i7.size() && i1.size()==i8.size() && i1.size()==i9.size() &&
					i1.size()==i10.size() && i1.size()==i11.size());
			
			// Check the sorting order is correct
			boolean found1 = false;
			boolean found2 = false;
			boolean found50 = false;
			
			// 0, true = end date descending (longest time first) - 50, 2, 1
			for(Long id:i2){
				if(id==50){
					assertTrue("Solr results were not returned in the expected order", !found1 && !found2);
					found50 = true;
				}
				else if(id==2){
					assertTrue("Solr results were not returned in the expected order", !found1 && found50);
					found2 = true;
				}
				else if(id==1){
					assertTrue("Solr results were not returned in the expected order", found50 && found2);
					found1 = true;
				}
			}
			assertTrue("Solr results were not returned in the expected order", found50 && found2 && found1);
			
			// 0, false = end date ascending (expire soonest first) - 1, 2, 50
			found1 = false;
			found2 = false;
			found50 = false;
			for(Long id:i7){
				if(id==50){
					assertTrue("Solr results were not returned in the expected order", found1 && found2);
					found50 = true;
				}
				else if(id==2){
					assertTrue("Solr results were not returned in the expected order", found1 && !found50);
					found2 = true;
				}
				else if(id==1){
					assertTrue("Solr results were not returned in the expected order", !found50 && !found2);
					found1 = true;
				}
			}
			assertTrue("Solr results were not returned in the expected order", found50 && found2 && found1);
			
			// 1, true = current bid descending (most expensive first) - 50, 2, 1
			found1 = false;
			found2 = false;
			found50 = false;
			for(Long id:i3){
				if(id==50){
					assertTrue("Solr results were not returned in the expected order", !found1 && !found2);
					found50 = true;
				}
				else if(id==2){
					assertTrue("Solr results were not returned in the expected order", !found1 && found50);
					found2 = true;
				}
				else if(id==1){
					assertTrue("Solr results were not returned in the expected order", found50 && found2);
					found1 = true;
				}
			}
			assertTrue("Solr results were not returned in the expected order", found50 && found2 && found1);
			
			// 1, false = current bid ascending (cheapest first) - 1, 2, 50
			found1 = false;
			found2 = false;
			found50 = false;
			for(Long id:i8){
				if(id==50){
					assertTrue("Solr results were not returned in the expected order", found1 && found2);
					found50 = true;
				}
				else if(id==2){
					assertTrue("Solr results were not returned in the expected order", found1 && !found50);
					found2 = true;
				}
				else if(id==1){
					assertTrue("Solr results were not returned in the expected order", !found50 && !found2);
					found1 = true;
				}
			}
			assertTrue("Solr results were not returned in the expected order", found50 && found2 && found1);
		}
	}
	
	@Test
	public void testDeleteFromSolr() {
		if(GlobalVars.SOLR_ENABLED){
			db.addToSolr(101, "check_delete 101", "desc 101", 2, new Date(System.currentTimeMillis()+25000));
			db.forceCommitSolr();
			
			assertTrue("WARNING: The number of results is incorrect (depends if Solr was empty)", db.getTextItemsSolr("check_delete", 0, 50, 0, true).size()==1);
			
			db.deleteFromSolr("101");
			db.forceCommitSolr();
			
			assertTrue("WARNING: May not have deleted. The number of results is incorrect (depends if Solr was empty)", db.getTextItemsSolr("check_delete", 0, 50, 0, true).size()==0);
		}
	}

	@Test
	public void testUpdateSolr() {
		if(GlobalVars.SOLR_ENABLED){
		db.addToSolr(201, "check_update 201", "desc 201", 2, new Date(System.currentTimeMillis()+25000));
		db.addToSolr(202, "check_update 202", "desc 202", 4, new Date(System.currentTimeMillis()+25000));
		db.addToSolr(203, "check_update 203", "desc 203", 6, new Date(System.currentTimeMillis()+25000));
		db.forceCommitSolr();
		
		assertTrue("WARNING: The number of results is incorrect (depends if Solr was empty)", db.getTextItemsSolr("check_update", 0, 2, 1, false).size()==2);
		
		// Price ascending = 201, 202, 203, as only 2 items 203 should not be listed
		ArrayList<Long> i1 = db.getTextItemsSolr("check_update", 0, 2, 1, false);
		
		boolean found201 = false;
		boolean found202 = false;
		boolean found203 = false;
				
		for(Long l:i1){
			if(l==201){
				found201=true;
				assertTrue("The order of items was not correct by price", !found202 && !found203);
			}
			else if(l==202){
				found202=true;
				assertTrue("The order of items was not correct by price", found201 && !found203);
			}
			else if(l==203){
				found203=true;
				assertFalse("The number of items returned is incorrect", found203);
			}
		}
		
		db.updateSolr(201, 15);
		db.forceCommitSolr();
		
		// Now list should be = 202, 203, 101
		i1 = db.getTextItemsSolr("check_update", 0, 2, 1, false);
		
		found201 = false;
		found202 = false;
		found203 = false;
		
		for(Long l:i1){
			if(l==201){
				found201=true;
				assertFalse("The number of items returned is incorrect", found201);
			}
			else if(l==202){
				found202=true;
				assertTrue("The order of items was not correct by price", !found201 && !found203);
			}
			else if(l==203){
				found203=true;
				assertTrue("The order of items was not correct by price", !found201 && found202);
			}
		}
		}
	}

	@Test
	public void testCheckUsernamePassword() {
		assertTrue("Should return -1 for null username", db.checkUsernamePassword(null, "password1") == -1);
		assertTrue("Should return -1 for null password", db.checkUsernamePassword("username1", null) == -1);
		assertTrue("Should return -1 for bad password", db.checkUsernamePassword("dbtest1", "password1dgfdg") == -1);
		assertTrue("Should return -1 for bad username", db.checkUsernamePassword("dbtest1dgdgf", "password1") == -1);
		
		assertTrue("Should return userID for correct username/password", db.checkUsernamePassword("dbtest2", "password2") > 0);
	}

	@Test
	public void testMakeNewAuthToken() {
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		
		assertTrue("Do not create auth token for invalid user", db.makeNewAuthToken(-1) == null);
		assertTrue("Do not create auth token for invalid user", db.makeNewAuthToken(0) == null);
		if(strict) assertTrue("User does not exist, so we may not want to return a valid auth token", db.makeNewAuthToken(999999999l) == null); // user does not exist, so cannot be updated
		assertTrue("A valid auth token should be returned for a valid user", db.makeNewAuthToken(user2ID) != null);
	}

	@Test
	public void testCheckAuthToken() {
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		String auth2 = db.makeNewAuthToken(user2ID);
		
		assertFalse("Auth token for user is not correct, so should return false", db.checkAuthToken(-1, auth2));
		assertFalse("Auth token and username are incorrect, should return false", db.checkAuthToken(-1, null));
		assertFalse("UserID and auth do not match, should return false", db.checkAuthToken(56, auth2));
		assertFalse("Auth token is null, should retrun false", db.checkAuthToken(user2ID, null));
		assertFalse("Auth token is incorrect, should return false", db.checkAuthToken(user2ID, "fgdf"));
		assertTrue("Auth token is correct, so should return true", db.checkAuthToken(user2ID, auth2));
	}

	@Test
	public void testLogout() {
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		String auth2 = db.makeNewAuthToken(user2ID);
		
		assertFalse("UserID is invalid, cannot log out", db.logout(-1));
		assertTrue("Should have logged user out, id and auth were correct", db.logout(user2ID));
		assertFalse("Now that the user is logged out, the auth should be false", db.checkAuthToken(user2ID, auth2));
		if(strict) assertFalse("User does not exist, so cannot log out", db.logout(9999999999l)); // cannot be logged out as it does not exist
	}

	@Test
	public void testInsertAddress() {
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		
		assertFalse("user id invalid", db.insertAddress(-1, "st", "tn", "15217-1234", 17, false));
		assertFalse("street invalid", db.insertAddress(user2ID, null, "tn", "15217-1234", 17, false));
		assertFalse("town in invalid", db.insertAddress(user2ID, "st", null, "15217-1234", 17, false));
		assertFalse("zip code is invalid", db.insertAddress(user2ID, "st", "tn", null, 17, false));
		assertFalse("state is invalid", db.insertAddress(user2ID, "st", "tn", "15217-1234", -1, false));
		assertFalse("default address is null", db.insertAddress(user2ID, "st", "tn", "15217-1234", 17, null));
		if(strict) assertFalse("user does not exist so cannot have address", db.insertAddress(999999999999l, "st", "tn", "15217-1234", 17, false)); // user does not exist
		if(strict) assertFalse("state does not exist so should not be inserted", db.insertAddress(user2ID, "st", "tn", "15217-1234", 45645, false)); // state doesn't exist
		
		assertTrue("All data was valid, should have inserted address", db.insertAddress(user2ID, "st", "tn", "15217-1234", 17, false));
	}

	@Test
	public void testUpdateAddress() {
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		
		assertTrue("Should have inserted address, all data was valid", db.insertAddress(user2ID, "st2", "tn2", "15217", 1, true));
		Address a1 = null;
		try {
			a1 = db.getDefaultAddress(user2ID);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		assertFalse("Address id to update is invalid, should not update", db.updateAddress(-1, user2ID, "newst", "newtn", "15218", 2, false));
		if(strict) assertFalse("address id and user id do not match i.e. user does not own address, should not update", db.updateAddress(a1.getId(), 99999999999l, "newst", "newtn", "15218", 2, false));
		assertFalse("Street invalid, should not update", db.updateAddress(a1.getId(), user2ID, null, "newtn", "15218", 2, false));
		assertFalse("Town invalid, should not update", db.updateAddress(a1.getId(), user2ID, "newst", null, "15218", 2, false));
		assertFalse("zip code invalid, should not update", db.updateAddress(a1.getId(), user2ID, "newst", "newtn", null, 2, false));
		assertFalse("state code invalid, should not update", db.updateAddress(a1.getId(), user2ID, "newst", "newtn", "15218", -1, false));
		assertFalse("default is null, should not update", db.updateAddress(a1.getId(), user2ID, "newst", "newtn", "15218", 2, null));
		
		if(strict) assertFalse("state code invalid, should not update", db.updateAddress(a1.getId(), user2ID, "newst", "newtn", "15218", 468654, false));
		
		assertTrue("All data was valid, we should have inserted", db.updateAddress(a1.getId(), user2ID, "newst", "newtn", "15218", 2, false));
		
		Address a3 = null;
		try {
			a3 = db.getAddress(a1.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertTrue("The address did not have the updates we made", a3.getId()==a1.getId() && a3.getStreet().equals("newst") && a3.getTown().equals("newtn")
				&& a3.getZip().equals("15218") && a3.getState()==2);
	}

	@Test
	public void testInsertItem() {
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		
		Date ed = new Date(System.currentTimeMillis()+100000000);
		assertTrue("The user id is incorrect, should not insert item", db.insertItem(0, "it name", "it desc", 0.0, 0.0, 0.0, 1, ed, 1)==-1); // userid bad
		assertTrue("The name is null, should not insert item", db.insertItem(user2ID, null, "it desc", 0.0, 0.0, 0.0, 1, ed, 1)==-1); //item name bad
		assertTrue("The description is null, should not insert item", db.insertItem(user2ID, "it name", null, 0.0, 0.0, 0.0, 1, ed, 1)==-1); //item description bad
		assertTrue("The start price is invalid, should not insert item", db.insertItem(user2ID, "it name", "it desc", -1.0, 0.0, 0.0, 1, ed, 1)==-1); //start price bad
		assertTrue("The reserve price is invalid, should not insert item", db.insertItem(user2ID, "it name", "it desc", 0.0, -1.0, 0.0, 1, ed, 1)==-1); //reserve price bad
		assertTrue("The buy now price is invlaid, should not insert item", db.insertItem(user2ID, "it name", "it desc", 0.0, 0.0, -1.0, 1, ed, 1)==-1); //buy now price bad
		assertTrue("The quantity is invalid, should not insert item", db.insertItem(user2ID, "it name", "it desc", 0.0, 0.0, 0.0, 0, ed, 1)==-1); // quantity bad
		assertTrue("The end date is null, should not insert item", db.insertItem(user2ID, "it name", "it desc", 0.0, 0.0, 0.0, 1, null, 1)==-1); //end date bad
		assertTrue("The category id is invalid, should not insert item", db.insertItem(user2ID, "it name", "it desc", 0.0, 0.0, 0.0, 1, ed, 0)==-1); // category bad
		if(strict) assertTrue("Category id does not exist, should not insert item", db.insertItem(user2ID, "it name", "it desc", 0.0, 0.0, 0.0, 1, ed, 999999999999l)==-1); // category doesn't exist
		
		assertTrue("The item was valid and should have been inserted", db.insertItem(user2ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, ed, 1)>0);
		
		//TODO:
		//fail("Should check that it was also inserted to solr"); // TODO
	}

	@Test
	public void testGetItem() {
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		Date ed = new Date(System.currentTimeMillis()+100000000);
		Long item1ID = db.insertItem(user2ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, ed, 1);
		
		Item item1 = db.getItem(item1ID, true);
		assertTrue("Reading back the item does not give the same info "+item1.toJSON(), item1.getID()==item1ID && item1.getName().equals("it name") && item1.getDescription().equals("it desc")
				&& item1.getStartPrice()==1.0 && item1.getReservePrice()==2.0 && item1.getBuyNowPrice()==3.0
				&& item1.getCategoryID()==1 && (item1.getEndDate().getTime()/1000)==(ed.getTime()/1000) && item1.getSellerID()==user2ID);
		
		assertTrue("Number of bids is incorrect", item1.getNoOfBids()==0);
		assertTrue("The blank thumbnail was not set correctly", item1.getThumbnailURL().equals("blank.jpg"));
		assertTrue("The number of images is incorrect", item1.getImages().size()==0);
	}

	@Test
	public void testGetOldItem() {
		// Insert the needed data
		long user1ID = db.checkUsernamePassword("dbtest1", "password1");
		Date ed = new Date(System.currentTimeMillis()+100000000);
		Account acc1= new Account(52, "name","oncard","cardno","cvv",new Date(System.currentTimeMillis()));
		db.insertAddress(user1ID, "st2", "tn2", "15217", 1, true);
		Address a1 = null;
		try {
			a1 = db.getDefaultAddress(user1ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		long item1ID = db.insertItem(user1ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, ed, 1);
		
		// Buy the item
		assertTrue("Could not buy the item", db.buyItemNow(user1ID, item1ID, 1, acc1, a1));
		
		// Check the item is in the oldItems table
		Item item1 = db.getOldItem(item1ID, true);
		assertTrue("The old item is not present in the old items table", item1!=null); 
		assertTrue("The old item read from the database is incorrect", item1.getID()==item1ID && item1.getName().equals("it name") && item1.getDescription().equals("it desc")
				&& item1.getStartPrice()==1.0 && item1.getReservePrice()==2.0 && item1.getBuyNowPrice()==3.0
				&& item1.getCategoryID()==1);
		
		assertTrue("An item was read from the old items table that we do not think should be there", db.getOldItem(9999999999999l, true)==null);
	}

	@Test
	public void testGetCurrentSellingItems() {
		// DELETE FROM items WHERE sellerID = (SELECT id FROM users WHERE username="dbtest3")
		long user3ID = db.checkUsernamePassword("dbtest3", "password3");
		
		assertTrue("The user should have no currently selling items", db.getCurrentSellingItems(-1, 0).size()==0);
		assertTrue("The user should have no currently selling items", db.getCurrentSellingItems(user3ID, 0).size()==0);
		
		db.insertItem(user3ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, new Date(), 1);
		
		assertTrue("The user should have 1 currently selling items "+user3ID+ " "+ db.getCurrentSellingItems(user3ID, 0).size(), db.getCurrentSellingItems(user3ID, 0).size()==1);
		
		// Get item that have updated since now, i.e. none
		long now = System.currentTimeMillis();
		assertTrue("The user should have 0 currently selling items that were updated "+user3ID+ " "+ db.getCurrentSellingItems(user3ID, now).size(), db.getCurrentSellingItems(user3ID, now).size()==0);
	}

	@Test
	public void testGetOldSellingItems() {
		long user3ID = db.checkUsernamePassword("dbtest3", "password3");
		
		assertTrue("The user should have no old selling items", db.getOldSellingItems(user3ID,0).size()==0);
		assertTrue("The user should have no old selling items", db.getOldSellingItems(-1,0).size()==0);	
		
		Date ed = new Date(System.currentTimeMillis()+100000000);
		Account acc1= new Account(52, "name","oncard","cardno","cvv",new Date(System.currentTimeMillis()));
		db.insertAddress(user3ID, "st2", "tn2", "15217", 1, true);
		Address a1 = null;
		try {
			a1 = db.getDefaultAddress(user3ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		long item1ID = db.insertItem(user3ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, ed, 1);
		
		// Buy the item
		assertTrue("Could not buy the item " +user3ID + " "+item1ID, db.buyItemNow(user3ID, item1ID, 1, acc1, a1));	
		
		assertTrue("The user should have 1 old selling items", db.getOldSellingItems(user3ID,0).size()==1);
		
		long now = System.currentTimeMillis();
		assertTrue("The user should have 0 updated old selling items", db.getOldSellingItems(user3ID,now).size()==0);
	}

	@Test
	public void testInsertBid() {
		long user1ID = db.checkUsernamePassword("dbtest1", "password1");
		long item1ID = db.insertItem(user1ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, new Date(System.currentTimeMillis()+100000), 1);
		
		assertTrue("The user id is invalid, should not insert bid", db.insertBid(-1, item1ID, 1, 0.5, 5)==-1.0);
		assertTrue("The item id is invalid, should not insert bid",  db.insertBid(user1ID, -1, 1, 0.5, 5)==-1.0);
		assertTrue("The item does not exist, should not insert bid",  db.insertBid(user1ID, 9999999999999l, 1, 0.5, 5)==-1.0);
		assertTrue("The quantity is invalid, should not insert bid",  db.insertBid(user1ID, item1ID, 0, 0.5, 5)==-1.0);
		assertTrue("The bid price is invalid, should not insert bid",  db.insertBid(user1ID, item1ID, 1, -0.5, 5)==-1.0);
		assertTrue("The max bid is invalid, should not insert bid",  db.insertBid(user1ID, item1ID, 1, 0.5, -5)==-1.0);
		assertTrue("The max bid is less than the starting price, should not insert bid",  db.insertBid(user1ID, item1ID, 1, 0.5, 0.25)==-1.0);	
		assertTrue("The max bid is less than the bid, should not insert bid",  db.insertBid(user1ID, item1ID, 1, 0.5, 0.2)==-1.0);
		
		assertTrue("The bid should have been accepted " + user1ID + " " + item1ID, db.insertBid(user1ID, item1ID, 1, 1.0, 1.5)==1.0);
		
		Item item1 = db.getItem(item1ID, false);
		assertTrue("The bid and max bid read back are not correct", item1.getCurrentBid()==1.0 && item1.getMaxBid()==1.5);
		assertTrue("The number of bids is not 1", item1.getNoOfBids()==1);
		
		long user3ID = db.checkUsernamePassword("dbtest3", "password3");
		
		assertTrue("The bid is lower than the current bid price, should not be inserted", db.insertBid(user3ID, item1ID, 1, 0.2, 0.4)==-1.0);
		assertTrue("The previous bidders bid should be more", db.insertBid(user3ID, item1ID, 1, 1.2, 1.45)==1.46); 
		assertTrue("The previous bidders bid should be more",  db.insertBid(user3ID, item1ID, 1, 1.47, 1.47)==1.48);
		assertTrue("We should have out bid the other person", db.insertBid(user3ID, item1ID, 1, 2.1, 2.5)==2.1);
		assertTrue("We should have out bid the other person",  db.insertBid(user1ID, item1ID, 1, 3.0, 4.0)==3.0);
		assertTrue("We should have out bid the other person",  db.insertBid(user3ID, item1ID, 1, 5.0, 6.0)==5.0);
		
		// Get the current bidding
		assertTrue("The user should be bidding on one thing " + db.getCurrentBids(user1ID,0).size(), db.getCurrentBids(user1ID,0).size()==1);
		long item2ID = db.insertItem(user1ID, "it name2", "it desc2", 5.0, 10.0, 0.0, 1, new Date(System.currentTimeMillis()+20000), 1);
		assertTrue("Could not insert an item", item2ID>0);
		
		assertTrue("Could not bid for an item but all data correct", db.insertBid(user3ID, item2ID, 1, 6.0, 8.0)==6.0);
		assertTrue("User 3 should have two item bids", db.getCurrentBids(user3ID,0).size()==2);
		
		//TODO:
		//fail("check solr also changed");
	}

	@Test
	public void testBuyItemNow() {
		long user1ID = db.checkUsernamePassword("dbtest1", "password1");
		Date ed = new Date(System.currentTimeMillis()+100000000);
		Account acc1= new Account(52, "name","oncard","cardno","cvv",new Date(System.currentTimeMillis()));
		db.insertAddress(user1ID, "st2", "tn2", "15217", 1, true);
		Address a1 = null;
		try {
			a1 = db.getDefaultAddress(user1ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long item1ID = db.insertItem(user1ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, ed, 1);
		long item2ID = db.insertItem(user1ID, "it name", "it desc", 1.0, 2.0, 0.0, 1, ed, 1);
		
		assertFalse("The user id is incorrect, should not be able to buy", db.buyItemNow(0, item1ID, 1, acc1, a1));
		assertFalse("The item id is incorrect, should not be able to buy",  db.buyItemNow(user1ID, 0, 1, acc1, a1));
		assertFalse("The item does not exist, should not be able to buy",  db.buyItemNow(user1ID, 9999999999999l, 1, acc1, a1));
		assertFalse("No account was passed, should not be able to buy",  db.buyItemNow(user1ID, item1ID, 1, null, a1));
		assertFalse("no address, should not be able to buy",  db.buyItemNow(user1ID, item1ID, 1, acc1, null));
		assertFalse("The item has no buy now price, should not be able to buy",  db.buyItemNow(user1ID, item2ID, 1, acc1, a1));
		
		assertTrue("The buy now should have been accepted", db.buyItemNow(user1ID, item1ID, 1, acc1, a1));
		
		//TODO
		//fail("check solr items are deleted"); // TODO
	}

	@Test
	public void testInsertThumbnail() {
		long user1ID = db.checkUsernamePassword("dbtest1", "password1");
		Date ed = new Date(System.currentTimeMillis()+100000000);
		long item1ID = db.insertItem(user1ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, ed, 1);
		
		assertFalse("item id is not valid, should not insert", db.insertThumbnail(-1, "thumb.jpg"));
		assertFalse("The thumbnail name is incorrect, should not insert", db.insertThumbnail(item1ID, null));
		if(strict) assertFalse("The item does not exist, should not insert", db.insertThumbnail(25, "thumb.jpg"));
		assertTrue("The item and thumbnail is valid, it should be inserted", db.insertThumbnail(item1ID, "thumb.jpg"));
	}

	@Test
	public void testInsertImage() {
		long user1ID = db.checkUsernamePassword("dbtest1", "password1");
		Date ed = new Date(System.currentTimeMillis()+100000000);
		long item1ID = db.insertItem(user1ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, ed, 1);
		
		assertFalse("The item id is invalid, should not insert", db.insertImage(-1, 1, "img1.jpg", "desc1"));
		assertFalse("The position is invalid, should not insert", db.insertImage(item1ID, -1, "img1.jpg", "desc1"));
		assertFalse("The url is null, should not insert",  db.insertImage(item1ID, 1, null, "desc1"));
		assertFalse("The description is null, should not insert",  db.insertImage(item1ID, 1, "img1.jpg", null));
		assertTrue("The image should have been inserted", db.insertImage(item1ID, 1, "img1.jpg", "desc1"));
	}

	@Test
	public void testInsertVideoString() {
		//fail("Not yet implemented video insert"); // TODO
	}

	@Test
	public void testGetItemImages() {
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		Date ed = new Date(System.currentTimeMillis()+100000000);
		long item1ID = db.insertItem(user2ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, ed, 1);
		
		ArrayList<Image> i1img = db.getItemImages(item1ID);
		assertTrue("An invalid item should have no images", db.getItemImages(-1).size()==0);
		assertTrue("An invalid item should have no images", db.getItemImages(999999999999999l).size()==0);
		
		assertTrue("The image should have been inserted", db.insertImage(item1ID, 1, "img1.jpg", "desc1"));
		
		Item item1 = db.getItem(item1ID, true);
		assertTrue("The item should have 1 image, size was " +item1.getImages().size(), item1.getImages().size()==1);
		
		assertTrue("The image should have been inserted", db.insertImage(item1ID, 3, "img3.jpg", "desc3"));
		assertTrue("The image should have been inserted", db.insertImage(item1ID, 2, "img2.jpg", "desc2"));
		
		item1 = db.getItem(item1ID, true);
		assertTrue("The item should have 3 image: size " + item1.getImages().size(), item1.getImages().size()==3);
		
		i1img = db.getItemImages(item1ID);
		assertTrue("The item images are not returned in the correct order", i1img.get(0).getUrl().equals("img1.jpg")
			&& i1img.get(1).getUrl().equals("img2.jpg") && i1img.get(2).getUrl().equals("img3.jpg"));
		
		assertTrue("Could not insert an item image", db.insertImage(item1ID, 2, "new.jpg", "new"));
		
		i1img = db.getItemImages(item1ID);
		if(strict) assertTrue("The item image was not updated in the correct position", i1img.get(0).getUrl().equals("thumb.jpg") && i1img.get(1).getUrl().equals("img1.jpg")
		&& i1img.get(2).getUrl().equals("new.jpg") && i1img.get(3).getUrl().equals("img3.jpg"));
	}

	@Test
	public void testGetPurchases() {
		long user4ID = db.checkUsernamePassword("dbtest4", "password4");
		long user5ID = db.checkUsernamePassword("dbtest5", "password5");
		if(user4ID<0) db.insertUser("dbtest4", "password4", "dbtest4@user.com", "user", "4");
		if(user5ID<0) db.insertUser("dbtest5", "password5", "dbtest5@user.com", "user", "5");
		user4ID = db.checkUsernamePassword("dbtest4", "password4");
		user5ID = db.checkUsernamePassword("dbtest5", "password5");
		
		Account acc1= new Account(user5ID, "name","oncard","cardno","cvv",new Date(System.currentTimeMillis()));
		db.insertAddress(user5ID, "st2", "tn2", "15217", 1, true);
		Address a1 = null;
		try {
			a1 = db.getDefaultAddress(user5ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertTrue("The user should have no purchases", db.getPurchases(user4ID,0).size()==0); 
		assertTrue("The user should have no purchases, but has "+ db.getPurchases(user5ID,0).size(), db.getPurchases(user5ID,0).size()==0);
		
		Date ed = new Date(System.currentTimeMillis()+100000000);
		long item1ID = db.insertItem(user4ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, ed, 1);
		
		assertTrue("Could not buy item", db.buyItemNow(user5ID, item1ID, 1, acc1, a1));
		
		assertTrue("The user should have no purchases", db.getPurchases(user4ID,0).size()==0); 
		assertTrue("The user should have 1 purchase", db.getPurchases(user5ID,0).size()==1);
		
		long now  =System.currentTimeMillis();
		assertTrue("The user should have 0 updated purchase", db.getPurchases(user5ID,now).size()==0);
	}

	@Test
	public void testGetCurrentBids() {
		long user6ID = db.checkUsernamePassword("dbtest6", "password6");
		if(user6ID<0) db.insertUser("dbtest6", "password6", "dbtest6@user.com", "user", "6");
		user6ID = db.checkUsernamePassword("dbtest6", "password6");
		long user1ID = db.checkUsernamePassword("dbtest1", "password1");	
		
		assertTrue("User should have no bids, invalid id", db.getCurrentBids(-1, 0).isEmpty());
		assertTrue("User should have no bids, id does not exist", db.getCurrentBids(999999999999999l, 0).isEmpty());
		assertTrue("User should have no bids, but has "+db.getCurrentBids(user6ID,0).size(), db.getCurrentBids(user6ID,0).isEmpty());
		
		Date ed = new Date(System.currentTimeMillis()+100000000);
		long item1ID = db.insertItem(user1ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, ed, 1);
		
		assertTrue("Bid did not enter correctly "+user6ID + item1ID, db.insertBid(user6ID, item1ID, 1, 5.0, 6.0)==5.0);
		assertTrue("User should have 1 bid", db.getCurrentBids(user6ID,0).size()==1);
		
		assertTrue("Bid did not enter correctly", db.insertBid(user6ID, item1ID, 1, 8.0, 9.0)==8.0);
		assertTrue("User should still have 1 bid, only 1 bid per item", db.getCurrentBids(user6ID,0).size()==1);
		
		long item2ID = db.insertItem(user1ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, ed, 1);
		assertTrue("Bid did not enter correctly", db.insertBid(user6ID, item2ID, 1, 5.0, 6.0)==5.0);
		assertTrue("User should have 2 bids", db.getCurrentBids(user6ID,0).size()==2);
		
		long now = System.currentTimeMillis();
		assertTrue("User should have 0 updated bids", db.getCurrentBids(user6ID,now).size()==0);
	}

	@Test
	public void testGetOldBids() {
		long user7ID = db.checkUsernamePassword("dbtest7", "password7");
		if(user7ID<0) db.insertUser("dbtest7", "password7", "dbtest7@user.com", "user", "7");
		user7ID = db.checkUsernamePassword("dbtest7", "password7");
		long user1ID = db.checkUsernamePassword("dbtest1", "password1");	
		
		Account acc1= new Account(user7ID, "name","oncard","cardno","cvv",new Date(System.currentTimeMillis()));
		db.insertAddress(user7ID, "st2", "tn2", "15217", 1, true);
		Address a1 = null;
		try {
			a1 = db.getDefaultAddress(user7ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertTrue("User should have no old bids, invalid id", db.getCurrentBids(-1, 0).isEmpty());
		assertTrue("User should have no old bids, id does not exist", db.getCurrentBids(999999999999999l, 0).isEmpty());
		assertTrue("User should have no old bids, but has "+db.getOldBids(user7ID,0).size(), db.getOldBids(user7ID,0).isEmpty());
		
		Date ed = new Date(System.currentTimeMillis()+100000000);
		long item1ID = db.insertItem(user1ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, ed, 1);
		
		assertTrue("Bid did not enter correctly", db.insertBid(user7ID, item1ID, 1, 5.0, 6.0)==5.0);
		assertTrue("User should have 0 old bids still", db.getOldBids(user7ID,0).size()==0);
		
		assertTrue("Could not buy the item", db.buyItemNow(user7ID, item1ID, 1, acc1, a1));
		
		assertTrue("User should have 1 old bid", db.getOldBids(user7ID,0).size()==1);
		
		long now = System.currentTimeMillis();
		assertTrue("User should have 0 updated old bid", db.getOldBids(user7ID,now).size()==0);
	}

	@Test
	public void testInsertUser() {
		assertTrue("Could not insert valid user", db.insertUser("username8", "password8", "dbtest8@user.com", "user", "8"));
		
		// Insert some bad users
		assertFalse("Invalid username, should not insert", db.insertUser(null, "password3", "email3@user.com", "user", "3"));
		assertFalse("Invalid password, should not insert", db.insertUser("username3", null, "email3@user.com", "user", "3"));
		assertFalse("Invalid e-mail address, should not insert", db.insertUser("username3", "password3", null, "user", "3"));
		assertFalse("Invalid first name, should not insert", db.insertUser("username3", "password3", "email3@user.com", null, "3"));
		assertFalse("Invalid last name, should not insert", db.insertUser("username3", "password3", "email3@user.com", "user", null));
	}

	@Test
	public void testUpdateUser() {
		long user1ID = db.checkUsernamePassword("dbtest1", "password1");
		
		assertFalse("userID invalid, should not update", db.updateUser(-1, "password32", "email99999999999@user.com", "user3", "33"));
		assertFalse("password invalid, should not update", db.updateUser(user1ID, null, "email99999999999@user.com", "user3", "33"));
		assertFalse("e-mail invalid, should not update", db.updateUser(user1ID, "password32", null, "user3", "33"));
		assertFalse("first name invalid, should not update", db.updateUser(user1ID, "password32", "email99999999999@user.com", null, "33"));
		assertFalse("last name invalid, should not update", db.updateUser(user1ID, "password32", "eemail99999999999@user.com", "user3", null));
		
		if(strict) assertFalse("user does not exist, should not update", db.updateUser(99999999999l, "password32", "email32@user.com", "user3", "33"));
		assertTrue("All data was valid, user should have been updated", db.updateUser(user1ID, "password32", "email99999999999@user.com", "user3", "33"));
	}

	@Test
	public void testGetFirstName() {
		long user3ID = db.checkUsernamePassword("dbtest3", "password3");
		
		assertTrue("UserID invalid, cannot get name", db.getFirstName(-1) == null);
		assertTrue("User does not exist, cannot get name", db.getFirstName(999999999999999l) == null);
		assertTrue("User's name is incorrect", db.getFirstName(user3ID).equals("user3"));
	}

	@Test
	public void testGetPublicUser() {
		assertTrue("The userID is invalid, should not return user", db.getPublicUser(-1) == null);
		assertTrue("The userID does not exist, should not return user", db.getPublicUser(999999999999999l) == null);
		
		long user3ID = db.checkUsernamePassword("dbtest3", "password3");
		User user3 = db.getPublicUser(user3ID);
		assert user3.getID()==user3ID && user3.getUsername().equals("dbtest1") && user3.getRating().equals("0");
		assertTrue("The public information returned was incorrect", user3.getPassword() == null && user3.getEmail() == null && user3.getFirstName()==null
				&& user3.getLastName()==null);
	}

	@Test
	public void testGetUser() {
		assertTrue("The userID is invalid, should not return user", db.getUser(-1) == null);
		assertTrue("The userID does not exist, should not return user", db.getUser(999999999999999l) == null);
		
		long user3ID = db.checkUsernamePassword("dbtest3", "password3");
		User user3 = db.getUser(user3ID);
		assertTrue("The user information returned was incorrect\n"+user3ID+"\n"+user3.FULLtoJSON(), user3.getID()==user3ID && user3.getUsername().equals("dbtest3") && user3.getPassword().equals("password3")
				&& user3.getEmail().equals("dbtest3@user.com") && user3.getFirstName().equals("user3")
				&& user3.getLastName().equals("3") && user3.getRating().equals("0"));
	}

	@Test
	public void testGetAddress() {
		long user8ID = db.checkUsernamePassword("dbtest8", "password8");
		if(user8ID<0) db.insertUser("dbtest8", "password8", "dbtest8@user.com", "user8", "8");
		user8ID = db.checkUsernamePassword("dbtest8", "password8");
		
		db.insertAddress(user8ID, "st2", "tn2", "15217", 1, true);
		Address a1 = null;
		try {
			a1 = db.getDefaultAddress(user8ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			Address a2 = db.getAddress(a1.getId());
			assertTrue("The id's of the addresses should match", a1.getId() == a2.getId());
			assertTrue("UserID is invalid, should not return an address", db.getAddress(-1) == null);
			assertTrue("UserID does not exist, should not return an address", db.getAddress(999999999999999l) == null);
			
			assertTrue("The information inserted and retrieved does not match", a2.getId()==a1.getId() && a2.getStreet().equals("st2") && a2.getTown().equals("tn2")
					&& a2.getZip().equals("15217") && a2.getState()==1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetDefaultAddress() {
		long user9ID = db.checkUsernamePassword("dbtest9", "password9");
		if(user9ID<0)db.insertUser("dbtest9", "password9", "dbtest9@user.com", "user9", "9");
		user9ID = db.checkUsernamePassword("dbtest9", "password9");

		db.insertAddress(user9ID, "st", "tn", "15217-1234", 17, false);
		try{
			assertTrue("User should not currectly have a default address", db.getDefaultAddress(user9ID) == null);
			assertTrue("UserID is invalid, should not return an address", db.getDefaultAddress(-1) == null);
			
			// Ensure default stored correctly
			db.insertAddress(user9ID, "st2", "tn2", "15217", 1, true);
			assertTrue("The user should have a defualt address", db.getDefaultAddress(user9ID) != null);
			Address a1 = db.getDefaultAddress(user9ID);
			
			assertTrue("The default address is not the correct address", a1.getStreet().equals("st2") && a1.getTown().equals("tn2") && a1.getZip().equals("15217")
			&& a1.getState()==1 && a1.getIsDefault() && a1.getUserID()==user9ID);
		}
		catch(Exception e){
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void testUsernameExists() {
		try{
			assertTrue("The username shoudl exist", db.usernameExists("dbtest1"));
			assertTrue("The username shoudl exist as we do not want null to be available", db.usernameExists(null));
			assertFalse("The username should be available", db.usernameExists("newfakeuser"));
		}
		catch(Exception e){
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void testEmailExists() {
		assertTrue("The e-mail address should exist", db.emailExists("dbtest2@user.com"));
		assertTrue("The e-mail should exist as we do not want null to be available", db.emailExists(null));
		assertFalse("The e-mail address should be available", db.emailExists("newfakeuser@user.com"));
	}

	@Test
	public void testGetAccount() {
		try{
			assertTrue("userID invalid, should not return an account", db.getAccount(-1)==null);
			assertTrue("userID does not exist, should not return an address", db.getAccount(999999999999999l)==null);
			
			//TODO: must do insert account first
			//fail("Not yet implemented"); // TODO
		}
		catch(Exception e){
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void testGetCategory() {
		try {
			assertTrue("The category should not exist", db.getCategory(-1)==null);
			assertTrue("The category should not exist", db.getCategory(999999999l)==null);
			assertTrue("The category should exist", db.getCategory(1l)!=null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetAllCategories() {
		try {
			assertTrue("The all categories array should never be null", db.getAllCategories()!=null);
			assertTrue("There should be some categries", db.getAllCategories().size()>0);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void testGetCategories() {
		try {
			assertTrue("The returned categories should not be null", db.getCategories(-1, -2)!=null);
			assertTrue("The returned categories should be empty", db.getCategories(-1, -2).size()==0);
			assertTrue("The returned categories should be empty", db.getCategories(-1, 60).size()==0);
			assertTrue("The returned categories should be empty", db.getCategories(-1, Long.MAX_VALUE).size()==0);
			assertTrue("The returned categories should be empty", db.getCategories(0, Long.MAX_VALUE).size()==0);
			assertTrue("The returned categories should have some values", db.getCategories(0, 0).size()>0);
			
			long now = System.currentTimeMillis();
			assertTrue("The returned categories should have 0 upadted", db.getCategories(0, now).size()==0);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}	
	}

	@Test
	public void testGetStates() {
		try {
			assertTrue("The returned states should not be null", db.getStates() !=null);
			assertTrue("The returned states should not be empty", db.getStates().size()>0);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
	}

	@Test
	public void testGetCategoryItems() {
		try{
			assertTrue("category id is invalid, should not return results", db.getCategoryItems(-1, 0, 25, 0, false, false, new String[0], 0).size()==0);
			assertTrue("page number is invalid, should not return results", db.getCategoryItems(1, -1, 25, 0, false, false, new String[0], 0).size()==0);
			assertTrue("items per page is invalid, should not return results", db.getCategoryItems(1, 0, -1, 0, false, false, new String[0], 0).size()==0);
			assertTrue("sort asc/desc is invalid, should not return results", db.getCategoryItems(1, 0, 25, 0, null, false, new String[0], 0).size()==0);
			assertTrue("with images is invalid, should not return results", db.getCategoryItems(1, 0, 25, 0, false, null, new String[0], 0).size()==0);
			assertTrue("'has items' is invalid, should not return results", db.getCategoryItems(1, 0, 25, 0, false, false, null, 0).size()==0);
		}
		catch(Exception e){
			e.printStackTrace();
			assertTrue(false);
		}
		
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		Date ed1 = new Date(System.currentTimeMillis()+100000000);
		long item1ID = db.insertItem(user2ID, "it name 1", "it desc", 1.0, 2.0, 3.0, 1, ed1, 10000000);
		Date ed2 = new Date(System.currentTimeMillis()+150000000);
		long item2ID = db.insertItem(user2ID, "it name 2", "it desc", 2.0, 2.0, 3.0, 1, ed2, 10000000);
		Date ed3 = new Date(System.currentTimeMillis()+120000000);
		long item3ID = db.insertItem(user2ID, "it name 3", "it desc", 50.0, 2.0, 3.0, 1, ed3, 10000000);	
		
		try{
			Date now = new Date(System.currentTimeMillis());
			Thread.sleep(2000);
			
			assertTrue("We just inserted into this category, something should exist", db.getCategoryItems(10000000, 0, 25, 0, false, false, new String[0], 0).size()>0);
		
			// Sort by reverse date (2, 3, 1)
			ArrayList<Item>	items = db.getCategoryItems(10000000, 0, 25, 0, true, false, new String[0], 0);
			assertTrue("The items were returned in the wrong order", items.size()>0 && items.get(0).getName().equals("it name 2") && items.get(1).getName().equals("it name 3")
						&& items.get(2).getName().equals("it name 1"));
			for(Item i:items)
				assertTrue("An out of date item was returned, itemID: " +i.getID(),i.getEndDate().after(now));
			
			// Order should be earliest end date first (1, 3, 2)
			 items = db.getCategoryItems(10000000, 0, 25, 0, false, false, new String[0], 0);
			assertTrue("The items were returned in the wrong order", items.get(0).getName().equals("it name 1") && items.get(1).getName().equals("it name 3")
				&& items.get(2).getName().equals("it name 2"));
			for(Item i:items)
				assertTrue("An out of date item was returned, itemID: " +i.getID(),i.getEndDate().after(now));
			
			long user1ID = db.checkUsernamePassword("dbtest1", "password1");
			db.insertBid(user1ID, item3ID, 1, 150.0, 150.0);
			db.insertBid(user1ID, item2ID, 1, 125.0, 125.0);
			db.insertBid(user1ID, item1ID, 1, 100.0, 100.0);
			
			// Sort by price highest first - 3, 2, 1
			items = db.getCategoryItems(10000000, 0, 25, 1, true, false, new String[0], 0);
			assertTrue("The items were returned in the wrong order", items.get(0).getName().equals("it name 3") && items.get(1).getName().equals("it name 2")
					&& items.get(2).getName().equals("it name 1"));
			for(Item i:items)
				assertTrue("An out of date item was returned, itemID: " +i.getID(),i.getEndDate().after(now));
			
			// Sort by price lowest first - 1, 2, 3
			items = db.getCategoryItems(10000000, 0, 25, 1, false, false, new String[0], 0);
			assertTrue("The items were returned in the wrong order", items.get(0).getName().equals("it name 1") && items.get(1).getName().equals("it name 2")
					&& items.get(2).getName().equals("it name 3"));
			for(Item i:items)
				assertTrue("An out of date item was returned, itemID: " +i.getID(),i.getEndDate().after(now));
			
			db.insertImage(item1ID, 1, "img1.jpg", "desc1");
			items = db.getCategoryItems(10000000, 0, 25, 0, true, false, new String[0], 0);
			
			assertTrue("The items should not have images", items.get(0).getImages().size()==0
					&& items.get(1).getImages().size()==0 && items.get(2).getImages().size()==0);
			
			items = db.getCategoryItems(10000000, 0, 25, 0, true, true, new String[0], 0);
			assertTrue("One of the items should have images", items.get(0).getImages().size()==1
					|| items.get(1).getImages().size()==1 || items.get(2).getImages().size()==1);
			
			String[] has = new String[1];
			has[0] = Long.toString(item1ID);
			
			items = db.getCategoryItems(10000000, 0, 25, 0, false, false, has, 0);
			assertTrue("Only two items should have been returned", items.get(0).getName().equals("it name 3")
				&& items.get(1).getName().equals("it name 2"));
			for(Item i:items)
				assertTrue("An out of date item was returned, itemID: " +i.getID(),i.getEndDate().after(now));
			
		}
		catch(Exception e){
			e.printStackTrace();
			assertTrue(false);
		}
		
		// Make sure out of date items aren't returned
		ed1 = new Date(System.currentTimeMillis()+1000);
		item1ID = db.insertItem(user2ID, "it name 1", "it desc", 1.0, 2.0, 3.0, 1, ed1, 11100000);
		ed2 = new Date(System.currentTimeMillis()+150000000);
		item2ID = db.insertItem(user2ID, "it name 2", "it desc", 2.0, 2.0, 3.0, 1, ed2, 11100000);
		ed3 = new Date(System.currentTimeMillis()+120000000);
		item3ID = db.insertItem(user2ID, "it name 3", "it desc", 50.0, 2.0, 3.0, 1, ed3, 11100000);	
		
		/*
		 * Test no out of date items
		 */
		try {
			Date now = new Date(System.currentTimeMillis());
			Thread.sleep(10000);
			
			// Item 1 is now out of date
			// Sort by reverse date (2, 3)
			ArrayList<Item> items;
			try {
				items = db.getCategoryItems(11100000, 0, 25, 0, true, false, new String[0], 0);
				// oldest first
				assertTrue("The items were returned in the wrong order", items.size()>0 && items.get(0).getName().equals("it name 2") && items.get(1).getName().equals("it name 3"));
						
				// Order should be earliest end date first (3, 2)
				items = db.getCategoryItems(11100000, 0, 25, 0, false, false, new String[0], 0);
				assertTrue("The items were returned in the wrong order", items.get(0).getName().equals("it name 3")
					&& items.get(1).getName().equals("it name 2"));
				for(Item i:items)
					assertTrue("An out of date item was returned, itemID: " +i.getID(),i.getEndDate().after(now));
						
				long user1ID = db.checkUsernamePassword("dbtest1", "password1");
				db.insertBid(user1ID, item3ID, 1, 150.0, 150.0);
				db.insertBid(user1ID, item2ID, 1, 125.0, 125.0);
						
				// Sort by price highest first - 3, 2
				items = db.getCategoryItems(11100000, 0, 25, 1, true, false, new String[0], 0);
				assertTrue("The items were returned in the wrong order", items.get(0).getName().equals("it name 3") && items.get(1).getName().equals("it name 2"));
				for(Item i:items)
					assertTrue("An out of date item was returned, itemID: " +i.getID(),i.getEndDate().after(now));
				
				// Sort by price lowest first - 2, 3
				items = db.getCategoryItems(11100000, 0, 25, 1, false, false, new String[0], 0);
				assertTrue("The items were returned in the wrong order", items.get(0).getName().equals("it name 2")
						&& items.get(1).getName().equals("it name 3"));
				for(Item i:items)
					assertTrue("An out of date item was returned, itemID: " +i.getID(),i.getEndDate().after(now));
				
				// Get any items and make sure they're not out of date
				items = db.getCategoryItems(0, 0, 25, 0, true, false, new String[0], 0);
				for(Item i:items)
					assertTrue("1 An out of date item was returned, itemID: " +i.getID() + ", max-id="+(Long.MAX_VALUE-i.getID()*100000l)+", ed:"+i.getEndDate().getTime()+", now:"+now.getTime(),i.getEndDate().after(now));
				items = db.getCategoryItems(0, 0, 25, 0, false, false, new String[0], 0);
				for(Item i:items)
					assertTrue("2 An out of date item was returned, itemID: " +i.getID()+ ", ed:"+i.getEndDate().getTime(),i.getEndDate().after(now));
				items = db.getCategoryItems(0, 0, 25, 1, false, false, new String[0], 0);
				for(Item i:items)
					assertTrue("3 An out of date item was returned, itemID: " +i.getID()+ ", ed:"+i.getEndDate().getTime(),i.getEndDate().after(now));
				items = db.getCategoryItems(0, 0, 25, 1, true, false, new String[0], 0);
				for(Item i:items)
					assertTrue("4 An out of date item was returned, itemID: " +i.getID()+ ", ed:"+i.getEndDate().getTime(),i.getEndDate().after(now));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				assertTrue(false);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
		
		/*
		 * Test items after last seen id
		 */
		ed1 = new Date(System.currentTimeMillis()+1000);
		item1ID = db.insertItem(user2ID, "it name 0", "it desc", 1.0, 2.0, 3.0, 1, ed1, 11200000);
		ed1 = new Date(System.currentTimeMillis()+100000000);
		item1ID = db.insertItem(user2ID, "it name 1", "it desc", 1.0, 2.0, 3.0, 1, ed1, 11200000);
		ed2 = new Date(System.currentTimeMillis()+150000000);
		item2ID = db.insertItem(user2ID, "it name 2", "it desc", 2.0, 2.0, 3.0, 1, ed2, 11200000);
		ed3 = new Date(System.currentTimeMillis()+120000000);
		item3ID = db.insertItem(user2ID, "it name 3", "it desc", 50.0, 2.0, 3.0, 1, ed3, 11200000);	
		
		/*
		 * Test no out of date items
		 */
		try {
			Thread.sleep(10000);
			
			Date now = new Date(System.currentTimeMillis());
			
			ArrayList<Item> items;
			try {
				// Sort by reverse date (2, 3, 1) only id's after 2 -> (3, 1)
				items = db.getCategoryItems(11200000, 0, 25, 0, true, false, new String[0], item2ID);
				assertTrue("The items were returned in the wrong order", items.size()>0 && items.get(0).getName().equals("it name 3")
							&& items.get(1).getName().equals("it name 1"));
				for(Item i:items)
					assertTrue("An out of date item was returned, itemID: " +i.getID(),i.getEndDate().after(now));
				
				// Order should be earliest end date first (1, 3, 2) only after 1 -> (3, 2)
				 items = db.getCategoryItems(11200000, 0, 25, 0, false, false, new String[0], item1ID);
				assertTrue("The items were returned in the wrong order", items.get(0).getName().equals("it name 3")
					&& items.get(1).getName().equals("it name 2"));
				for(Item i:items)
					assertTrue("An out of date item was returned, itemID: " +i.getID(),i.getEndDate().after(now));
				
				long user1ID = db.checkUsernamePassword("dbtest1", "password1");
				db.insertBid(user1ID, item3ID, 1, 150.0, 150.0);
				db.insertBid(user1ID, item2ID, 1, 125.0, 125.0);
				db.insertBid(user1ID, item1ID, 1, 100.0, 100.0);
				
				// Sort by price highest first - 3, 2, 1 only after 3 -> (2, 1)
				items = db.getCategoryItems(11200000, 0, 25, 1, true, false, new String[0], item3ID);
				assertTrue("The items were returned in the wrong order", items.get(0).getName().equals("it name 2")
						&& items.get(1).getName().equals("it name 1"));
				for(Item i:items)
					assertTrue("An out of date item was returned, itemID: " +i.getID(),i.getEndDate().after(now));
				
				// Sort by price lowest first - 1, 2, 3 only after 1 -> (2,3)
				items = db.getCategoryItems(11200000, 0, 25, 1, false, false, new String[0], item1ID);
				assertTrue("The items were returned in the wrong order", items.get(0).getName().equals("it name 2")
						&& items.get(1).getName().equals("it name 3"));
				for(Item i:items)
					assertTrue("An out of date item was returned, itemID: " +i.getID(),i.getEndDate().after(now));
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				assertTrue(false);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
		
	}

	@Test
	public void testGetCategoryItemsIDs() {
		try{
			assertTrue("category id is invalid, should not return results", db.getCategoryItemsIDs(-1, 0, 25, 0, false, 0).size()==0);
			assertTrue("page number is invalid, should not return results", db.getCategoryItemsIDs(1, -1, 25, 0, false, 0).size()==0);
			assertTrue("items per page is invalid, should not return results", db.getCategoryItemsIDs(1, 0, -1, 0, false,  0).size()==0);
			assertTrue("sort asc/desc is invalid, should not return results", db.getCategoryItemsIDs(1, 0, 25, 0, null, 0).size()==0);
		}
		catch(Exception e){
			e.printStackTrace();
			assertTrue(false);
		}
		
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		Date ed1 = new Date(System.currentTimeMillis()+100000000);
		long item1ID = db.insertItem(user2ID, "it name 1", "it desc", 1.0, 2.0, 3.0, 1, ed1, 10000077);
		Date ed2 = new Date(System.currentTimeMillis()+150000000);
		long item2ID = db.insertItem(user2ID, "it name 2", "it desc", 2.0, 2.0, 3.0, 1, ed2, 10000077);
		Date ed3 = new Date(System.currentTimeMillis()+120000000);
		long item3ID = db.insertItem(user2ID, "it name 3", "it desc", 50.0, 2.0, 3.0, 1, ed3, 10000077);	
		
		try{
			Date now = new Date(System.currentTimeMillis());
			Thread.sleep(2000);
			
			assertTrue("We just inserted into this category, something should exist", db.getCategoryItemsIDs(10000077, 0, 25, 0, false, 0).size()>0);
		
			// Sort by reverse date (2, 3, 1)
			ArrayList<Long>	items = db.getCategoryItemsIDs(10000077, 0, 25, 0, true, 0);
			assertTrue("The items were returned in the wrong order", items.size()>0 && items.get(0)==item2ID && items.get(1)==item3ID
						&& items.get(2)==item1ID);
			
			// Order should be earliest end date first (1, 3, 2)
			 items = db.getCategoryItemsIDs(10000077, 0, 25, 0, false, 0);
			assertTrue("The items were returned in the wrong order", items.get(0)==item1ID && items.get(1)==item3ID
				&& items.get(2)==item2ID);

			long user1ID = db.checkUsernamePassword("dbtest1", "password1");
			db.insertBid(user1ID, item3ID, 1, 150.0, 150.0);
			db.insertBid(user1ID, item2ID, 1, 125.0, 125.0);
			db.insertBid(user1ID, item1ID, 1, 100.0, 100.0);
			
			// Sort by price highest first - 3, 2, 1
			items = db.getCategoryItemsIDs(10000077, 0, 25, 1, true, 0);
			assertTrue("The items were returned in the wrong order", items.get(0)==item3ID&& items.get(1)==item2ID
					&& items.get(2)==item1ID);
			
			// Sort by price lowest first - 1, 2, 3
			items = db.getCategoryItemsIDs(10000077, 0, 25, 1, false, 0);
			assertTrue("The items were returned in the wrong order", items.get(0)==item1ID && items.get(1)==item2ID
					&& items.get(2)==item3ID);
		}
		catch(Exception e){
			e.printStackTrace();
			assertTrue(false);
		}
		
		// Make sure out of date items aren't returned
		ed1 = new Date(System.currentTimeMillis()+1000);
		item1ID = db.insertItem(user2ID, "it name 1", "it desc", 1.0, 2.0, 3.0, 1, ed1, 11100078);
		ed2 = new Date(System.currentTimeMillis()+150000000);
		item2ID = db.insertItem(user2ID, "it name 2", "it desc", 2.0, 2.0, 3.0, 1, ed2, 11100078);
		ed3 = new Date(System.currentTimeMillis()+120000000);
		item3ID = db.insertItem(user2ID, "it name 3", "it desc", 50.0, 2.0, 3.0, 1, ed3, 11100078);	
		
		/*
		 * Test no out of date items
		 */
		try {
			Date now = new Date(System.currentTimeMillis());
			Thread.sleep(10000);
			
			// Item 1 is now out of date
			// Sort by reverse date (2, 3)
			ArrayList<Long> items;
			try {
				items = db.getCategoryItemsIDs(11100078, 0, 25, 0, true, 0);
				// oldest first
				assertTrue("The items were returned in the wrong order", items.size()>0 && items.get(0)==item2ID && items.get(1)==item3ID);
						
				// Order should be earliest end date first (3, 2)
				items = db.getCategoryItemsIDs(11100078, 0, 25, 0, false, 0);
				assertTrue("The items were returned in the wrong order", items.get(0)==item3ID
					&& items.get(1)==item2ID);
						
				long user1ID = db.checkUsernamePassword("dbtest1", "password1");
				db.insertBid(user1ID, item3ID, 1, 150.0, 150.0);
				db.insertBid(user1ID, item2ID, 1, 125.0, 125.0);
						
				// Sort by price highest first - 3, 2
				items = db.getCategoryItemsIDs(11100078, 0, 25, 1, true, 0);
				assertTrue("The items were returned in the wrong order", items.get(0)==item3ID && items.get(1)==item2ID);
			
				// Sort by price lowest first - 2, 3
				items = db.getCategoryItemsIDs(11100078, 0, 25, 1, false, 0);
				assertTrue("The items were returned in the wrong order", items.get(0)==item2ID
						&& items.get(1)==item3ID);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				assertTrue(false);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
		
		/*
		 * Test items after last seen id
		 */
		ed1 = new Date(System.currentTimeMillis()+1000);
		item1ID = db.insertItem(user2ID, "it name 0", "it desc", 1.0, 2.0, 3.0, 1, ed1, 11200079);
		ed1 = new Date(System.currentTimeMillis()+100000000);
		item1ID = db.insertItem(user2ID, "it name 1", "it desc", 1.0, 2.0, 3.0, 1, ed1, 11200079);
		ed2 = new Date(System.currentTimeMillis()+150000000);
		item2ID = db.insertItem(user2ID, "it name 2", "it desc", 2.0, 2.0, 3.0, 1, ed2, 11200079);
		ed3 = new Date(System.currentTimeMillis()+120000000);
		item3ID = db.insertItem(user2ID, "it name 3", "it desc", 50.0, 2.0, 3.0, 1, ed3, 11200079);	
		
		/*
		 * Test no out of date items
		 */
		try {
			Thread.sleep(10000);
			
			Date now = new Date(System.currentTimeMillis());
			
			ArrayList<Long> items;
			try {
				// Sort by reverse date (2, 3, 1) only id's after 2 -> (3, 1)
				items = db.getCategoryItemsIDs(11200079, 0, 25, 0, true, item2ID);
				assertTrue("The items were returned in the wrong order", items.size()>0 && items.get(0)==item3ID
							&& items.get(1)==item1ID);
				
				// Order should be earliest end date first (1, 3, 2) only after 1 -> (3, 2)
				 items = db.getCategoryItemsIDs(11200079, 0, 25, 0, false, item1ID);
				assertTrue("The items were returned in the wrong order", items.get(0)==item3ID
					&& items.get(1)==item2ID);
				
				long user1ID = db.checkUsernamePassword("dbtest1", "password1");
				db.insertBid(user1ID, item3ID, 1, 150.0, 150.0);
				db.insertBid(user1ID, item2ID, 1, 125.0, 125.0);
				db.insertBid(user1ID, item1ID, 1, 100.0, 100.0);
				
				// Sort by price highest first - 3, 2, 1 only after 3 -> (2, 1)
				items = db.getCategoryItemsIDs(11200079, 0, 25, 1, true, item3ID);
				assertTrue("The items were returned in the wrong order", items.get(0)==item2ID
						&& items.get(1)==item1ID);
				
				// Sort by price lowest first - 1, 2, 3 only after 1 -> (2,3)
				items = db.getCategoryItemsIDs(11200079, 0, 25, 1, false, item1ID);
				assertTrue("The items were returned in the wrong order", items.get(0)==item2ID
						&& items.get(1)==item3ID);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				assertTrue(false);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void testGetTextItems() {
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		Date ed1 = new Date(System.currentTimeMillis()+100000000);
		long item1ID = db.insertItem(user2ID, "it name 1 namecmartnotreal", "it desc ", 1.0, 2.0, 3.0, 1, ed1, 10000007);
		Date ed2 = new Date(System.currentTimeMillis()+150000000);
		long item2ID = db.insertItem(user2ID, "it name 2", "it desc desccmartnotreal", 2.0, 2.0, 3.0, 1, ed2, 10000007);
		Date ed3 = new Date(System.currentTimeMillis()+120000000);
		long item3ID = db.insertItem(user2ID, "it name 3", "it desc desccmartnotreal", 50.0, 2.0, 3.0, 1, ed3, 10000007);
		
		try {
			ArrayList<Item> items = db.getTextItems("namecmartnotreal", 0, 25, 0, false);
			assertTrue("Item 1 should probably be the only thing there. size is "+items.size(), items.size()==1);
			assertTrue("Item 1 should probably be the only thing there", items.get(0).getID()==item1ID);
			
			items = db.getTextItems("desccmartnotreal", 0, 25, 0, false);
			assertTrue("There should be 2 items returned", items.size()==2);
			
			assertTrue("The items were returned in the wrong order", items.get(0).getID()==item3ID && items.get(1).getID()==item2ID);
			
			items = db.getTextItems("desccmartnotreal", 0, 25, 0, true);
			assertTrue("The items were returned in the wrong order", items.get(0).getID()==item2ID && items.get(1).getID()==item3ID);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	@Test
	public void testGetTextItemsIDs() {
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		Date ed1 = new Date(System.currentTimeMillis()+100000000);
		long item1ID = db.insertItem(user2ID, "it name 1 nameidcmartnotreal", "it desc ", 1.0, 2.0, 3.0, 1, ed1, 10000008);
		Date ed2 = new Date(System.currentTimeMillis()+150000000);
		long item2ID = db.insertItem(user2ID, "it name 2", "it desc descidcmartnotreal", 2.0, 2.0, 3.0, 1, ed2, 10000008);
		Date ed3 = new Date(System.currentTimeMillis()+120000000);
		long item3ID = db.insertItem(user2ID, "it name 3", "it desc descidcmartnotreal", 50.0, 2.0, 3.0, 1, ed3, 10000008);
		
		try {
			ArrayList<Long> items = db.getTextItemsIDs("nameidcmartnotreal", 0, 25, 0, false);
			assertTrue("Item 1 should probably be the only thing there, was " + items.size(), items.size()>=1 && items.get(0)==item1ID);
			assertTrue("Item 1 should probably be the only thing there", items.size()==1);
			
			items = db.getTextItemsIDs("descidcmartnotreal", 0, 25, 0, false);
			assertTrue("There should be 2 items returned", items.size()==2);
			
			assertTrue("The items were returned in the wrong order", items.get(0)==item3ID && items.get(1)==item2ID);
			
			items = db.getTextItemsIDs("descidcmartnotreal", 0, 25, 0, true);
			assertTrue("The items were returned in the wrong order", items.get(0)==item2ID && items.get(1)==item3ID);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetItemsByID() {
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		Date ed1 = new Date(System.currentTimeMillis()+100000000);
		long item1ID = db.insertItem(user2ID, "it name 1", "it desc ", 1.0, 2.0, 3.0, 1, ed1, 10000009);
		Date ed2 = new Date(System.currentTimeMillis()+150000000);
		long item2ID = db.insertItem(user2ID, "it name 2", "it desc", 2.0, 2.0, 3.0, 1, ed2, 10000009);
		Date ed3 = new Date(System.currentTimeMillis()+120000000);
		long item3ID = db.insertItem(user2ID, "it name 3", "it desc", 50.0, 2.0, 3.0, 1, ed3, 10000009);
		
		try {
			ArrayList<Long> ids = new ArrayList<Long>();
			ArrayList<Item> items;
			
			items = db.getItemsByID(ids, 0, false);
			assertTrue("There should be no items returned", items.size()==0);
			
			ids.add(item1ID);
			
			items = db.getItemsByID(ids, 0, false);
			assertTrue("One item should have been returned, item 1", items.get(0).getID()==item1ID);
			
			ids.add(item2ID);
			items = db.getItemsByID(ids, 0, false);
			assertTrue("There should be two items returned", items.size()==2);
			assertTrue("The two items returned in the wrong order", items.get(0).getID()==item1ID && items.get(1).getID()==item2ID);
			
			ids.add(item3ID);
			items = db.getItemsByID(ids, 0, true);
			assertTrue("There should be three items returned", items.size()==3);
			assertTrue("The three items returned in the wrong order", items.get(0).getID()==item2ID && items.get(1).getID()==item3ID
					&& items.get(2).getID()==item1ID);
			
			//TODO: check sorting correctly
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	@Test
	public void testMoveEndedItems() {
		long user1ID = db.checkUsernamePassword("dbtest1", "password1");
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		Date expired = new Date(System.currentTimeMillis()-1000000);
		long item1ID = db.insertItem(user2ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, expired, 1);
		
		db.insertBid(user1ID, item1ID, 1, 10.0, 15.0);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
		}
		
		try {
			long moved = db.moveEndedItems();
			assertTrue("We should have at least moved the expired item "+item1ID+" we just inserted, but did " + moved, moved>=1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetAllUserData() {
		assertTrue("The users array should not be null", db.getAllUserData(-6, 0)!=null);
		assertTrue("The users array should not be null", db.getAllUserData(20, -5)!=null);
		assertTrue("The users array should not be empty", db.getAllUserData(0, 0).size()==0);
		assertTrue("The users array should be size 5, but is " + db.getAllUserData(5, 0).size(), db.getAllUserData(5, 0).size()==5);
	}

	@Test
	public void testGetUserCount() {
		assertTrue("There should be some users", db.getUserCount()>0);
	}

	@Test
	public void testGetUsers() {
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		long user3ID = db.checkUsernamePassword("dbtest3", "password3");

		ArrayList<Long> ids = new ArrayList<Long>();
		assertTrue("The user list should not be null", db.getUsers(null)!=null);
		assertTrue("The user list should be empty", db.getUsers(null).size()==0);
		assertTrue("The user list should be empty", db.getUsers(ids).size()==0);
		
		ids.add(user2ID);
		assertTrue("The user list should be 1, is "+db.getUsers(ids).size(), db.getUsers(ids).size()==1);
		ids.add(user2ID);
		assertTrue("The user list should be 1, is "+db.getUsers(ids).size(), db.getUsers(ids).size()==1);
		
		ids.add(user3ID);
		assertTrue("The user list should be 1, is "+db.getUsers(ids).size(), db.getUsers(ids).size()==2);
	}

	@Test
	public void testGetVideos() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testInsertComment() {
		long user5ID = db.checkUsernamePassword("dbtest5", "password5");
		long user6ID = db.checkUsernamePassword("dbtest6", "password6");
		long item1ID = db.insertItem(user5ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, new Date(), 1);
		
		assertTrue("userID is invalid, should not insert comment",db.insertComment(-1, user5ID, item1ID, 0, new Date(), "comment")<0);
		assertTrue("seller is invalid, should not insert comment",db.insertComment(user6ID, -1, item1ID, 0, new Date(), "comment")<0);
		assertTrue("item is invalid, should not insert comment",db.insertComment(user6ID, user5ID, -1, 0, new Date(), "comment")<0);
		assertTrue("date is invalid, should not insert comment",db.insertComment(user6ID, user5ID, item1ID, 0, null, "comment")<0);
		assertTrue("seller is invalid, should not insert comment",db.insertComment(user6ID, user5ID, item1ID, 0, new Date(), null)<0);
		assertTrue("all data was correct, should insert comment",db.insertComment(user6ID, user5ID, item1ID, 0, new Date(), "comment")>0);
	}

	@Test
	public void testGetComments() {
		try {
			long user5ID = db.checkUsernamePassword("dbtest5", "password5");
			long user6ID = db.checkUsernamePassword("dbtest6", "password6");
			Date now = new Date();
			long item1ID = db.insertItem(user5ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, new Date(), 1);
			
			assertTrue("comments array should not be null",db.getComments(-1) != null);
			assertTrue("comments array should not be null",db.getComments(5) != null);
			assertTrue("itemID is invalid, should not have comments",db.getComments(-1).size()==0);
			assertTrue("itemID is invalid, should not have comments",db.getComments(999999999l).size()==0);
			assertTrue("The item should not yet have any comments",db.getComments(item1ID).size()==0);
			
			long commID = db.insertComment(user6ID, user5ID, item1ID, 3, now, "comment");
			assertTrue("all data was correct, should insert comment",commID>0);
			
			assertTrue("The item should now have 1 comment",db.getComments(item1ID).size()==1);
			
			Comment comm = db.getComments(item1ID).get(0);
			assertTrue("The comment returned does not contain the correct information " + comm.toJSON(),comm.getID()==commID && comm.getDate().toString().equals(now.toString())
					&& comm.getFromUserID()==user6ID && comm.getToUserID()==user5ID && comm.getRating()==3
					&& comm.getItemID()==item1ID && comm.getComment().equals("comment"));
			
			db.insertComment(user6ID, user5ID, item1ID, 3, now, "comment 2");
			assertTrue("The item should now have 2 comments",db.getComments(item1ID).size()==2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testInsertQuestion() {
		long user5ID = db.checkUsernamePassword("dbtest5", "password5");
		long user6ID = db.checkUsernamePassword("dbtest6", "password6");
		Date now = new Date();
		long item1ID = db.insertItem(user5ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, new Date(), 1);
		
		assertTrue("The from userID is invalid, should not be inserted",db.insertQuestion(-1, user6ID, item1ID, now, "question")==-1);
		assertTrue("The to userID is invalid, should not be inserted",db.insertQuestion(user5ID, -1, item1ID, now, "question")==-1);
		assertTrue("The item id is invalid, should not be inserted",db.insertQuestion(user5ID, user6ID, -1, now, "question")==-1);
		assertTrue("The date is invalid, should not be inserted",db.insertQuestion(user5ID, user6ID, item1ID, null, "question")==-1);
		assertTrue("The question is invalid, should not be inserted",db.insertQuestion(user5ID, user6ID, item1ID, now, null)==-1);
		
		assertTrue("The question was correct and should have been inserted",db.insertQuestion(user5ID, user6ID, item1ID, now, "question")>0);
	}

	@Test
	public void testInsertAnswer() {
		long user5ID = db.checkUsernamePassword("dbtest5", "password5");
		long user6ID = db.checkUsernamePassword("dbtest6", "password6");
		Date now = new Date();
		long item1ID = db.insertItem(user5ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, new Date(), 1);
		
		long qID=db.insertQuestion(user5ID, user6ID, item1ID, now, "question");
		assertTrue("The question was correct and should have been inserted",qID>0);	
		
		assertTrue("The from userID is invalid, should not be inserted",db.insertAnswer(-1, user6ID, item1ID, qID, now, "question")==-1);
		assertTrue("The to userID is invalid, should not be inserted",db.insertAnswer(user5ID, -1, item1ID, qID, now, "question")==-1);
		assertTrue("The item id is invalid, should not be inserted",db.insertAnswer(user5ID, user6ID, -1, qID, now, "question")==-1);
		assertTrue("The date is invalid, should not be inserted",db.insertAnswer(user5ID, user6ID, item1ID, qID, null, "question")==-1);
		assertTrue("The answer is invalid, should not be inserted",db.insertAnswer(user5ID, user6ID, item1ID, qID, now, null)==-1);
		assertTrue("The question id is invalid, should not be inserted",db.insertAnswer(user5ID, user6ID, item1ID, -1, now, "null")==-1);
		
		assertTrue("The answer was correct and should have been inserted",db.insertAnswer(user5ID, user6ID, item1ID, qID, now, "answer")>0);
	}

	@Test
	public void testGetQuestion() {
		long user5ID = db.checkUsernamePassword("dbtest5", "password5");
		long user6ID = db.checkUsernamePassword("dbtest6", "password6");
		Date now = new Date();
		long item1ID = db.insertItem(user5ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, new Date(), 1);
		
		long qID=db.insertQuestion(user5ID, user6ID, item1ID, now, "question");
		assertTrue("The question was correct and should have been inserted",qID>0);
		
		try {
			assertTrue("The question id is invalid, null should be returned", db.getQuestion(-1)==null);
			assertTrue("The question does not exist, null should be returned", db.getQuestion(999999999l)==null);
			assertTrue("The question should have been returned", db.getQuestion(qID)!=null);
			Question q = db.getQuestion(qID);
			assertTrue("The questions data does not match", q.getID()==qID && q.getFromUserID()==user5ID
					&&q.getToUserID()==user6ID && q.getItemID()==item1ID && q.getPostDate().toString().equals(now.toString())
					&& q.getIsQuestion()==true && q.getContent().equals("question"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetQuestionsLong() {
		long user5ID = db.checkUsernamePassword("dbtest5", "password5");
		long user6ID = db.checkUsernamePassword("dbtest6", "password6");
		Date now = new Date();
		long item1ID = db.insertItem(user5ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, new Date(), 1);
		
		try {
			assertTrue("The questions array should not be null", db.getQuestions(-1)!=null);
			assertTrue("The questions array should not be null", db.getQuestions(999999999l)!=null);
			assertTrue("The questions array should be empty", db.getQuestions(-1).size()==0);
			assertTrue("The questions array should be empty", db.getQuestions(item1ID).size()==0);
			
			db.insertQuestion(user5ID, user6ID, item1ID, now, "question");
			
			assertTrue("The questions array should be 1", db.getQuestions(item1ID).size()==1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetQuestionsArrayListOfLong() {
		long user2ID = db.checkUsernamePassword("dbtest2", "password2");
		long user3ID = db.checkUsernamePassword("dbtest3", "password3");
		Date now = new Date();
		long item1ID = db.insertItem(user2ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, new Date(), 1);
		long item2ID = db.insertItem(user2ID, "it name", "it desc", 1.0, 2.0, 3.0, 1, new Date(), 1);
		
		try {
			ArrayList<Long> qids = new ArrayList<Long>();
			assertTrue("The questions array should not be null", db.getQuestions(qids)!=null);
			assertTrue("The questions array should be empty", db.getQuestions(qids).size()==0);
			
			qids.add(item1ID);
			assertTrue("The questions array should be empty", db.getQuestions(qids).size()==0);
			
			db.insertQuestion(user2ID, user3ID, item1ID, now, "question");
			qids.add(999999999l);
			
			assertTrue("The questions array should be 1, is "+db.getQuestions(qids).size(), db.getQuestions(qids).size()==1);
			
			qids.add(item2ID);
			
			assertTrue("The questions array should be 1", db.getQuestions(qids).size()==1);
			
			db.insertQuestion(user2ID, user3ID, item2ID, now, "question");
			
			assertTrue("The questions array should be 2", db.getQuestions(qids).size()==2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetAllVideos() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testInsertVideoStringStringInt() {
		//fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetMaxVideoID() {
		//fail("Not yet implemented"); // TODO
	}

}
