package com.cmart.test.util;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.cmart.util.Comment;

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

public class CommentTest {
	Comment c1;
	Date now;
	
	@Before
	public void setUp() throws Exception {
		now = new Date();
		c1 = new Comment(1l, 2l, 3l, 4l, 5, now, "comment text");
	}

	@Test
	public void testGetID() {
		assertTrue("The comment id is incorrect", c1.getID()==1l);
	}

	@Test
	public void testGetFromUserID() {
		assertTrue("The from user id is incorrect", c1.getFromUserID()==2l);
	}

	@Test
	public void testGetToUserID() {
		assertTrue("The to user id is incorrect", c1.getToUserID()==3l);
	}

	@Test
	public void testGetItemID() {
		assertTrue("The item id is incorrect", c1.getItemID()==4l);
	}

	@Test
	public void testGetRating() {
		assertTrue("The rating is incorrect", c1.getRating()==5);
	}

	@Test
	public void testGetDate() {
		assertTrue("The date is incorrect", c1.getDate()==now);
	}

	@Test
	public void testGetComment() {
		assertTrue("The text comment is incorrect", c1.getComment().equals("comment text"));
	}
}
