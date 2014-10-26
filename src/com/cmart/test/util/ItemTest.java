package com.cmart.test.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;

import com.cmart.util.Image;

import org.junit.Before;
import org.junit.Test;

import com.cmart.util.Item;

public class ItemTest {
	Item i1;
	Item i2;
	Item i3;
	Date now;
	Date end;
	
	@Before
	public void setUp() throws Exception {
		Item.suppressImmutableWarning();
		
		now = new Date();
		end = new Date(now.getTime()+10000);
		i1 = new Item();
		i2 = new Item(1l, "name 2", "desc 2", 2, 3.0, 4.0, 5.0, 6.0, 7.0, 8, now, end, 9l, 10l, "thumb", new ArrayList<Image>());
		i3 = new Item();				
	}

	@Test
	public void testSetID() {
		assertTrue("The item id is incorrect", i3.getID()==-1l);
		i3.setID(3l);
		assertTrue("The item id is incorrect", i3.getID()==3l);
	}

	@Test
	public void testGetID() {
		assertTrue("The item id is incorrect", i1.getID()==-1l);
		assertTrue("The item id is incorrect", i2.getID()==1l);
	}

	@Test
	public void testSetName() {
		assertTrue("The item name is incorrect", i3.getName()==null);
		i3.setName("name 3");
		assertTrue("The item name is incorrect", i3.getName().equals("name 3"));
	}

	@Test
	public void testGetName() {
		assertTrue("The item name is incorrect", i1.getName()==null);
		assertTrue("The item name is incorrect", i2.getName().equals("name 2"));
	}

	@Test
	public void testSetDescription() {
		assertTrue("The item description is incorrect", i3.getDescription()==null);
		i3.setDescription("desc 3");
		assertTrue("The item description is incorrect", i3.getDescription().equals("desc 3"));
	}

	@Test
	public void testGetDescription() {
		assertTrue("The item description is incorrect", i1.getDescription()==null);
		assertTrue("The item description is incorrect", i2.getDescription().equals("desc 2"));
	}

	@Test
	public void testSetQuantity() {
		assertTrue("The item quantity is incorrect", i3.getQuantity()==-1);
		i3.setQuantity(4);
		assertTrue("The item quantity is incorrect", i3.getQuantity()==4);
	}

	@Test
	public void testGetQuantity() {
		assertTrue("The item quantity is incorrect", i1.getQuantity()==-1);
		assertTrue("The item quantity is incorrect", i2.getQuantity()==2);
	}

	@Test
	public void testSetStartPrice() {
		assertTrue("The item start price is incorrect", i3.getStartPrice()==-1.0);
		i3.setStartPrice(5.0);
		assertTrue("The item start price is incorrect", i3.getStartPrice()==5.0);
	}

	@Test
	public void testGetStartPrice() {
		assertTrue("The item start price is incorrect", i1.getStartPrice()==-1.0);
		assertTrue("The item start price is incorrect", i2.getStartPrice()==3.0);
	}

	@Test
	public void testSetReservePrice() {
		assertTrue("The item reserve price is incorrect", i3.getReservePrice()==-1.0);
		i3.setReservePrice(6.0);
		assertTrue("The item reserve price is incorrect", i3.getReservePrice()==6.0);
	}

	@Test
	public void testGetReservePrice() {
		assertTrue("The item reserve price is incorrect", i1.getReservePrice()==-1.0);
		assertTrue("The item reserve price is incorrect", i2.getReservePrice()==4.0);
	}

	@Test
	public void testSetBuyNowPrice() {
		assertTrue("The item buy now price is incorrect", i3.getBuyNowPrice()==-1.0);
		i3.setBuyNowPrice(7.0);
		assertTrue("The item buy now price is incorrect", i3.getBuyNowPrice()==7.0);
	}

	@Test
	public void testGetBuyNowPrice() {
		assertTrue("The item buy now price is incorrect", i1.getBuyNowPrice()==-1.0);
		assertTrue("The item buy now price is incorrect", i2.getBuyNowPrice()==5.0);
	}

	@Test
	public void testSetCurrentBid() {
		assertTrue("The item current bid amount is incorrect", i3.getCurrentBid()==-1.0);
		i3.setCurrentBid(8.0);
		assertTrue("The item current bid amount is incorrect", i3.getCurrentBid()==8.0);
	}

	@Test
	public void testGetMaxCurrentBidStartPrice() {
		i3.setCurrentBid(8.0);
		assertTrue("The item current bid amount is incorrect", i3.getCurrentBid()==8.0);
		i3.setStartPrice(2.0);
		assertTrue("The item start price is incorrect", i3.getStartPrice()==2.0);
		
		assertTrue("The item max current bid/start price is incorrect", i3.getMaxCurrentBidStartPrice()==8.0);
		
		i3.setStartPrice(12.0);
		assertTrue("The item start price is incorrect", i3.getStartPrice()==12.0);
		
		assertTrue("The item max current bid/start price is incorrect", i3.getMaxCurrentBidStartPrice()==12.0);	
	}

	@Test
	public void testGetCurrentBid() {
		assertTrue("The item current bid amount is incorrect", i1.getCurrentBid()==-1.0);
		assertTrue("The item current bid amount is incorrect", i2.getCurrentBid()==6.0);
	}

	@Test
	public void testSetMaxBid() {
		i3.setMaxBid(-1.0);
		assertTrue("The item max bid amount is incorrect", i3.getMaxBid()==-1.0);
		i3.setMaxBid(9.0);
		assertTrue("The item max bid amount is incorrect, should be 9.0", i3.getMaxBid()==9.0);
	}

	@Test
	public void testGetMaxBid() {
		assertTrue("The item max bid amount is incorrect, should be -1.0", i1.getMaxBid()==-1.0);
		assertTrue("The item max bid amount is incorrect, should be 7.0", i2.getMaxBid()==7.0);
	}

	@Test
	public void testSetNoOfBids() {
		assertTrue("The item number of bids is incorrect", i3.getNoOfBids()==-1);
		i3.setNoOfBids(10);
		assertTrue("The item number of bids is incorrect, should be 10", i3.getNoOfBids()==10);
	}

	@Test
	public void testGetNoOfBids() {
		assertTrue("The item number of bids is incorrect", i1.getNoOfBids()==-1);
		assertTrue("The item number of bids is incorrect", i2.getNoOfBids()==8);
	}

	@Test
	public void testSetStartDate() {
		assertTrue("The item start date is incorrect", i3.getStartDate()==null);
		i3.setStartDate(now);
		assertTrue("The item start date is incorrect", i3.getStartDate().equals(now));
	}

	@Test
	public void testGetStartDate() {
		assertTrue("The item start date is incorrect", i1.getStartDate()==null);
		assertTrue("The item start date is incorrect", i2.getStartDate().equals(now));
	}

	@Test
	public void testSetEndDate() {
		assertTrue("The item end date is incorrect", i3.getEndDate()==null);
		i3.setEndDate(end);
		assertTrue("The item end date is incorrect", i3.getEndDate().equals(end));
	}

	@Test
	public void testGetEndDate() {
		assertTrue("The item end date is incorrect", i1.getEndDate()==null);
		assertTrue("The item end date is incorrect", i2.getEndDate().equals(end));
	}

	@Test
	public void testSetSellerID() {
		assertTrue("The item seller id is incorrect", i3.getSellerID()==-1l);
		i3.setSellerID(11l);
		assertTrue("The item seller id  incorrect, should be 11l", i3.getSellerID()==11l);
	}

	@Test
	public void testGetSellerID() {
		assertTrue("The item seller id is incorrect", i1.getSellerID()==-1l);
		assertTrue("The item seller id  incorrect", i2.getSellerID()==9l);
	}

	@Test
	public void testSetCategoryID() {
		assertTrue("The item category id is incorrect", i3.getCategoryID()==-1l);
		i3.setCategoryID(12l);
		assertTrue("The item category id  incorrect, should be 12l", i3.getCategoryID()==12l);
	}

	@Test
	public void testGetCategoryID() {
		assertTrue("The item category id is incorrect", i1.getCategoryID()==-1l);
		assertTrue("The item category id incorrect", i2.getCategoryID()==10l);
	}

	@Test
	public void testSetThumbnail() {
		assertTrue("The item thumbnail is incorrect", i3.getThumbnailURL()==null);
		i3.setThumbnailURL("th");
		assertTrue("The item thumbnail is incorrect", i3.getThumbnailURL().equals("th"));
	}

	@Test
	public void testGetThumbnailURL() {
		assertTrue("The item thumbnail is incorrect", i1.getThumbnailURL()==null);
		assertTrue("The item thumbnail is incorrect", i2.getThumbnailURL().equals("thumb"));
	}

	@Test
	public void testSetImages() {
		assertTrue("The item images is incorrect", i3.getImages()==null);
		i3.setImages(new ArrayList<Image>());
		assertTrue("The item images is incorrect", i3.getImages().equals(new ArrayList<Image>()));
	}

	@Test
	public void testGetImages() {
		assertTrue("The item images is incorrect", i1.getImages()==null);
		assertTrue("The item images is incorrect", i2.getImages().equals(new ArrayList<Image>()));
	}

	@Test
	public void testGetImagesJSON() {
		ArrayList<Image> images = new ArrayList<Image>();
		Image img1 = new Image(1, "url", "desc");
		Image img2 = new Image(2, "url2", "desc2");
		images.add(img1);
		images.add(img2);
		
		i3.setImages(images);
		assertTrue("The item images is incorrect", i3.getImages().equals(images));
		
		String json1 = "[";
		json1 += img1.toJSON();
		json1 += ",";
		json1 += img2.toJSON();
		json1 += "]";
		
		assertTrue("The item image JSON is incorrect", i3.getImagesJSON().equals(json1));
		
		assertTrue("The item image JSON is incorrect", i1.getImagesJSON().equals("[]"));
		assertTrue("The item image JSON is incorrect", i2.getImagesJSON().equals("[]"));
		
	}

	@Test
	public void testToJSON() {
		String json1 = "{\"id\":";
		json1 += "1";
		json1 += ",\"name\":\"";
		json1 += "name 2";
		json1 += "\",\"description\":\"";
		json1 += "desc 2";
		json1 += "\",\"quantity\":";
		json1 += "2";
		json1 += ",\"startPrice\":";
		json1 += "3.0";
		json1 += ",\"reservePrice\":";
		json1 += "4.0";
		json1 += ",\"buyNowPrice\":";
		json1 += "5.0";
		json1 += ",\"currentBid\":";
		json1 += "6.0";
		json1 += ",\"noOfBids\":";
		json1 += "8";
		json1 += ",\"startDate\":\"";
		json1 += now.toString();
		json1 += "\",\"endDate\":\"";
		json1 += end.toString();
		json1 += "\",\"sellerID\":";
		json1 += "9";
		json1 += ",\"categoryID\":";
		json1 += "10";
		json1 += ",\"reserve\":";
		json1 += "true";
		json1 += ",\"images\":";
		json1 += "[]";
		json1 += ",\"thumbnail\":\"";
		json1 += "thumb";
		json1 += "\"}";
		
		assertTrue("The item JSON is incorrect, should be\n   "+json1+"\nis "+i2.toJSON(), i2.toJSON().equals(json1));	
	}
}
