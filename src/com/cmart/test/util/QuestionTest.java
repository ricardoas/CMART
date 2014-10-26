package com.cmart.test.util;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.cmart.util.Question;

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

public class QuestionTest {
	Question q1;
	Question q2;
	Date now;
	
	@Before
	public void setUp() throws Exception {
		now = new Date();
		q1 = new Question(1l, 2l, 3l, 4l, true, -1, now, "content");
		q2 = new Question(1l, 2l, 3l, 4l, false, -1, now, "content");
	}

	@Test
	public void testGetID() {
		assertTrue("The question id is incorrect", q1.getID()==1l);
	}

	@Test
	public void testGetFromUserID() {
		assertTrue("The question from user ID is incorrect", q1.getFromUserID()==2l);
	}

	@Test
	public void testGetToUserID() {
		assertTrue("The question to user ID is incorrect", q1.getToUserID()==3l);
	}

	@Test
	public void testGetItemID() {
		assertTrue("The question item id is incorrect", q1.getItemID()==4l);
	}

	@Test
	public void testGetIsQuestion() {
		assertTrue("The question is question is incorrect", q1.getIsQuestion()==true);
		assertTrue("The question is question is incorrect", q2.getIsQuestion()==false);
	}

	@Test
	public void testGetContent() {
		assertTrue("The question content is incorrect", q1.getContent().equals("content"));
	}

	@Test
	public void testGetPostDate() {
		assertTrue("The question post date is incorrect", q1.getPostDate().equals(now));
	}

	@Test
	public void testToJSON() {
		String json1 = "{\"id\":";
		json1 += "1";
		json1 += ",\"fromUserID\":";
		json1 += "2";
		json1 += ",\"toUserID\":";
		json1 += "3";
		json1 += ",\"itemID\":";
		json1 += "4";
		json1 += ",\"isQuestion\":";
		json1 += "true";
		json1 += ",\"postDate\":\"";
		json1 += now.toString();
		json1 += "\",\"content\":\"";
		json1 += "content";
		json1 += "\"}";
		
		assertTrue("The question JSON is incorrect", q1.toJSON().equals(json1));
	}

}
