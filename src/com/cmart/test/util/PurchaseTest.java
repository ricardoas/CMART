package com.cmart.test.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.cmart.util.Item;
import com.cmart.util.Purchase;

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

public class PurchaseTest {
	Purchase p1;
	Purchase p2;
	Item i1;
	
	@Before
	public void setUp() throws Exception {
		i1 = new Item();
		p1 = new Purchase(1l, i1, 2, 3.0, false);
		p2 = new Purchase(1l, null, 2, 3.0, true);
	}

	@Test
	public void testGetID() {
		assertTrue("The purchase ID returned is incorrect", p1.getID()==1l);
	}

	@Test
	public void testGetItem() {
		assertTrue("The purchase item returned is incorrect", p1.getItem()==i1);
	}

	@Test
	public void testGetQuantity() {
		assertTrue("The purchase quantity returned is incorrect", p1.getQuantity()==2);
	}

	@Test
	public void testGetPrice() {
		assertTrue("The purchase price returned is incorrect", p1.getPrice()==3.0);
	}

	@Test
	public void testGetPaid() {
		assertTrue("The purchase paid returned is incorrect", p1.getPaid()==false);
		assertTrue("The purchase paid returned is incorrect", p2.getPaid()==true);
	}

	@Test
	public void testToJSON() {
		String json1 = "{\"quantity\":2,\"price\":3.0,\"paid\":false,\"id\":1,\"purchaseItem\":"+i1.toJSON()+"}";
		String json2 = "{\"quantity\":2,\"price\":3.0,\"paid\":true,\"id\":1,\"purchaseItem\":{\"id\":\"-1\"}}";
		
		assertTrue("The purchase JSON returned is incorrect, expected\n   "+json1+"\nis "+p1.toJSON(), p1.toJSON().equals(json1));
		assertTrue("The purchase JSON returned is incorrect, expected\n   "+json2+"\nis "+p2.toJSON(), p2.toJSON().equals(json2));
	}

}
