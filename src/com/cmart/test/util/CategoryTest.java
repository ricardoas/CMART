package com.cmart.test.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.cmart.util.Category;

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

public class CategoryTest {
	Category c1 = null;
	Category c2 = null;
	Category c3 = null;
	
	@Before
	public void setUp() throws Exception {
		Category.suppressImmutableWarning();
		
		c1 = new Category(1, "cat 1", 0, 0);
		c2 = new Category(2, "cat 2", 1, 1000);
	}


	@Test
	public void testToJSON() {
		String json1 = "{\"categoryID\":";
		json1 += "1";
		json1 += ",\"name\":\"";
		json1 += "cat 1";
		json1 += "\",\"parent\":";
		json1 += "0";
		json1 += ",\"ts\":";
		json1 += "0";
		json1 +="}";
		
		assertTrue("The returned JSON was incorrect", c1.toJSON().equals(json1));
	}

	@Test
	public void testSetCategoryID() {
		assertTrue("The category ID returned is incorrect", c2.getCategoryID()==2l);
		c2.setCategoryID(3l);
		assertTrue("The category ID returned is incorrect", c2.getCategoryID()==3l);
	}

	@Test
	public void testGetCategoryID() {
		assertTrue("The category ID returned is incorrect", c1.getCategoryID()==1l);
	}

	@Test
	public void testSetName() {
		assertTrue("The category name returned is incorrect", c2.getName().equals("cat 2"));
		c2.setName("cat 3");
		assertTrue("The category name returned is incorrect", c2.getName().equals("cat 3"));
	}

	@Test
	public void testGetName() {
		assertTrue("The category name returned is incorrect", c1.getName().equals("cat 1"));
	}

	@Test
	public void testSetParent() {
		assertTrue("The parent id returned is incorrect", c2.getParent() == 1l);
		c2.setParent(2l);
		assertTrue("The category name returned is incorrect", c2.getParent() == 2l);
	}

	@Test
	public void testGetParent() {
		assertTrue("The parent id returned is incorrect", c1.getParent() == 0l);
	}
}
