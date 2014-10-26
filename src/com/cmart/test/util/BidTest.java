package com.cmart.test.util;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import com.cmart.util.Bid;
import com.cmart.util.Item;
import com.cmart.util.User;

/**
 * 
 * @author Andy (turner.andy@gmail.com)
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

public class BidTest {
	Bid b1 = null;
	Bid b2 = null;
	Bid b3 = null;
	
	Date now = null;
	Item i1 = new Item();
	User u1 = new User(1l, "", "0");
	
	@Before
	public void setUp() throws Exception {
		now = new Date();
		Bid.suppressImmutableWarning();
		Item.suppressImmutableWarning();
		
		b1 = new Bid();
		b2 = new Bid(1l, 2l, 3, 4.0, 5.0, now, i1, u1); 
		b3 = new Bid();
	}

	@Test
	public void testToJSON() {
		String json1 = "{\"quantity\":-1,\"bid\":-1.0,\"maxBid\":-1.0,\"bidDate\":\"null\",\"bidItem\":{\"id\":\"-1\"}}";
		String json2 = "{\"quantity\":3,\"bid\":4.0,\"maxBid\":5.0,\"bidDate\":\""+now.toString()+"\",\"bidItem\":"+i1.toJSON()+"}";
		
		assertTrue("The returned JSON was incorrect. Should be " + json1, b1.toJSON().equals(json1));
		assertTrue("The returned JSON was incorrect. Should be\n  " + json2 + "\nis" + b2.toJSON(), b2.toJSON().equals(json2));
	}

	@Test
	public void testGetUserID() {
		assertTrue("The user id returned was incorrect. Should be -1", b1.getUserID()==-1);
		assertTrue("The user id returned was incorrect", b2.getUserID()==u1.getID());
	}

	@Test
	public void testGetQuantity() {
		assertTrue("The quantity returned was incorrect", b1.getQuantity()==-1);
		assertTrue("The quantity returned was incorrect", b2.getQuantity()==3);
	}

	@Test
	public void testSetQuantity() {
		assertTrue("The quantity returned was incorrect. It should be -1", b3.getQuantity()==-1);
		b3.setQuantity(4);
		assertTrue("The quantity returned was incorrect. It should be 4", b3.getQuantity()==4);
	}

	@Test
	public void testGetBid() {
		assertTrue("The bid returned was incorrect", b1.getBid()==-1.0);
		assertTrue("The bid returned was incorrect", b2.getBid()==4.0);
	}

	@Test
	public void testSetBid() {
		assertTrue("The bid returned was incorrect. should be -1.0", b3.getBid()==-1.0);
		b3.setBid(5.0);
		assertTrue("The bid returned was incorrect. Should be 5.0", b3.getBid()==5.0);
	}

	@Test
	public void testGetMaxBid() {
		assertTrue("The max bid returned was incorrect", b1.getMaxBid()==-1.0);
		assertTrue("The max bid returned was incorrect", b2.getMaxBid()==5.0);
	}

	@Test
	public void testSetMaxBid() {
		assertTrue("The max bid returned was incorrect. Should be -1.0", b3.getBid()==-1.0);
		b3.setBid(6.0);
		assertTrue("The max bid returned was incorrect. Should be 6.0", b3.getBid()==6.0);
	}

	@Test
	public void testGetBidDate() {
		assertTrue("The bid date returned was incorrect", b1.getBidDate()==null);
		assertTrue("The bid date returned was incorrect", b2.getBidDate()==now);
	}

	@Test
	public void testSetBidDate() {
		assertTrue("The bid date returned was incorrect", b3.getBidDate()==null);
		b3.setBidDate(now);
		assertTrue("The bid date returned was incorrect", b3.getBidDate()==now);
	}

	@Test
	public void testGetItem() {
		assertTrue("The item returned was incorrect", b1.getItem()==null);
		assertTrue("The item returned was incorrect", b2.getItem()==i1);
	}

	@Test
	public void testSetItem() {
		assertTrue("The item returned was incorrect", b3.getItem()==null);
		b3.setItem(i1);
		assertTrue("The item returned was incorrect", b3.getItem()==i1);
	}

	@Test
	public void testGetBidder() {
		assertTrue("The User returned was incorrect", b1.getBidder()==null);
		assertTrue("The User returned was incorrect", b2.getBidder()==u1);
	}

	@Test
	public void testSetItemID() {
		assertTrue("The item id returned was incorrect", b3.getItemID()==-1l);
		b3.setItemID(9l);
		assertTrue("The item id returned was incorrect", b3.getItemID()==9l);
	}

	@Test
	public void testGetItemID() {
		assertTrue("The item id returned was incorrect", b1.getItemID()==-1l);
		assertTrue("The item id returned was incorrect", b2.getItemID()==i1.getID());
	}

}
