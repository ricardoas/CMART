package com.cmart.test.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

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

public class UserTest {
	User u1;
	User u2;
	
	@Before
	public void setUp() throws Exception {
		u1 = new User(1l, "first", "last", "user", "pass", "email", "auth", "rating");
		u2 = new User(2l, "user2", "rating2");
	}

	@Test
	public void testGetID() {
		assertTrue("The user id returned is incorrect", u1.getID()==1l);
		assertTrue("The user id returned is incorrect", u2.getID()==2l);
	}

	@Test
	public void testGetFirstName() {
		assertTrue("The user first name returned is incorrect", u1.getFirstName().equals("first"));
		assertTrue("The user first name returned is incorrect", u2.getFirstName()==null);
	}

	@Test
	public void testGetLastName() {
		assertTrue("The user last name returned is incorrect", u1.getLastName().equals("last"));
		assertTrue("The user last name returned is incorrect", u2.getLastName()==null);
	}

	@Test
	public void testGetUsername() {
		assertTrue("The user username returned is incorrect", u1.getUsername().equals("user"));
		assertTrue("The user username returned is incorrect", u2.getUsername().equals("user2"));
	}

	@Test
	public void testGetPassword() {
		assertTrue("The user password returned is incorrect", u1.getPassword().equals("pass"));
		assertTrue("The user password returned is incorrect", u2.getPassword()==null);
	}

	@Test
	public void testGetEmail() {
		assertTrue("The user e-mail returned is incorrect", u1.getEmail().equals("email"));
		assertTrue("The user e-mail returned is incorrect", u2.getEmail()==null);
	}

	@Test
	public void testGetAuthToken() {
		assertTrue("The user auth token returned is incorrect", u1.getAuthToken().equals("auth"));
		assertTrue("The user auth token returned is incorrect", u2.getAuthToken()==null);
	}

	@Test
	public void testGetRating() {
		assertTrue("The user rating returned is incorrect", u1.getRating().equals("rating"));
		assertTrue("The user rating returned is incorrect", u2.getRating().equals("rating2"));
	}

	@Test
	public void testToJSON() {
		String json1 = "{\"id\":";
		json1 += "1";
		json1 += ",\"name\":\"";
		json1 += "user";
		json1 += "\",\"rating\":\"";
		json1 += "rating";
		json1 += "\"}";
		
		String json2 = "{\"id\":";
		json2 += "2";
		json2 += ",\"name\":\"";
		json2 += "user2";
		json2 += "\",\"rating\":\"";
		json2 += "rating2";
		json2 += "\"}";
		
		assertTrue("The user JSON returned is incorrect, expecting\n   "+json1+"\nis "+u1.toJSON(), u1.toJSON().equals(json1));
		assertTrue("The user JSON returned is incorrect, expecting\n   "+json2+"\nis "+u2.toJSON(), u2.toJSON().equals(json2));
	}

	@Test
	public void testFULLtoJSON() {
		String json1 = "{\"id\":";
		json1 += "1";
		json1 += ",\"username\":\"";
		json1 += "user";
		json1 += "\",\"firstname\":\"";
		json1 += "first";
		json1 += "\",\"lastname\":\"";
		json1 += "last";
		json1 += "\",\"email\":\"";
		json1 += "email";
		json1 += "\",\"password\":\"";
		json1 += "pass";
		json1 += "\",\"rating\":\"";
		json1 += "rating";
		json1 += "\"}";
		
		String json2 = "{\"id\":";
		json2 += "2";
		json2 += ",\"username\":\"";
		json2 += "user2";
		json2 += "\",\"firstname\":\"";
		json2 += "null";
		json2 += "\",\"lastname\":\"";
		json2 += "null";
		json2 += "\",\"email\":\"";
		json2 += "null";
		json2 += "\",\"password\":\"";
		json2 += "null";
		json2 += "\",\"rating\":\"";
		json2 += "rating2";
		json2 += "\"}";
		
		assertTrue("The user full JSON returned is incorrect, expecting\n   "+json1+"\nis "+u1.FULLtoJSON(), u1.FULLtoJSON().equals(json1));
		assertTrue("The user full JSON returned is incorrect, expecting\n   "+json2+"\nis "+u2.FULLtoJSON(), u2.FULLtoJSON().equals(json2));
	}

	@Test
	public void testToCSVString() {
		String csv1 = "1,";
		csv1 += "first,";
		csv1 += "last,";
		csv1 += "user,";
		csv1 += "pass,";
		csv1 += "email,";
		csv1 += "auth,";
		csv1 += "rating,";

		String csv2 = "2,";
		csv2 += "null,";
		csv2 += "null,";
		csv2 += "user2,";
		csv2 += "null,";
		csv2 += "null,";
		csv2 += "null,";
		csv2 += "rating2,";
		
		assertTrue("The user CSV returned is incorrect, expecting\n   "+csv1+"\nis "+u1.toCSVString(), u1.toCSVString().equals(csv1));
		assertTrue("The user CSV returned is incorrect, expecting\n   "+csv2+"\nis "+u2.toCSVString(), u2.toCSVString().equals(csv2));
	}

}
