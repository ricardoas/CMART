package com.cmart.test.util;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import com.cmart.util.Account;

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
public class AccountTest {

	
	@Test
	public void testAccount() {
		Account a = new Account(1, "name", "person name", "123456789", "123", new Date());
		assertTrue("Account could not be created", a != null);
	}

	@Test
	public void testGetName() {
		Account a = new Account(1, "name", "person name", "123456789", "123", new Date());
		assertTrue("The name returned is not correct", a.getName().equals("name"));
		
		Account b = new Account(1, "name 2", "person name", "123456789", "123", new Date());
		assertTrue("The name returned is not correct", b.getName().equals("name 2"));
	}

	@Test
	public void testGetAccountID() {
		Account a = new Account(1, "name", "person name", "123456789", "123", new Date());
		assertTrue("The account ID returned is not correct", a.getAccountID() == 1l);
		
		Account b = new Account(52, "name 2", "person name", "123456789", "123", new Date());
		assertTrue("The account ID returned is not correct", b.getAccountID() == 52l);
	}

	@Test
	public void testGetNameOnCard() {
		Account a = new Account(1, "name", "person name", "123456789", "123", new Date());
		assertTrue("The name on card returned is not correct", a.getNameOnCard().equals("person name"));
		
		Account b = new Account(52, "name 2", "person name 2", "123456789", "123", new Date());
		assertTrue("The name on card returned is not correct", b.getNameOnCard().equals("person name 2"));
	}

	@Test
	public void testGetCreditCardNo() {
		Account a = new Account(1, "name", "person name", "123456789", "123", new Date());
		assertTrue("The credit card number returned is not correct", a.getCreditCardNo().equals("123456789"));
		
		Account b = new Account(52, "name 2", "person name", "567", "123", new Date());
		assertTrue("The credit card number returned is not correct", b.getCreditCardNo().equals("567"));
	}

	@Test
	public void testGetCVV() {
		Account a = new Account(1, "name", "person name", "123456789", "123", new Date());
		assertTrue("The CVV returned is not correct", a.getCVV().equals("123"));
		
		Account b = new Account(52, "name 2", "person name", "123456789", "555555", new Date());
		assertTrue("The CVV returned is not correct", b.getCVV().equals("555555"));
	}

	@Test
	public void testGetExpirationDate() {
		Date da = new Date();
		Account a = new Account(1, "name", "person name", "123456789", "123", da);
		assertTrue("The expiration date returned is not correct, should be   "+da+"\nis"+a.getExpirationDate(), a.getExpirationDate().equals(da));
		
		Date db = new Date(5);
		Account b = new Account(52, "name 2", "person name", "123456789", "123", db);
		assertTrue("The expiration date returned is not correct", b.getExpirationDate().equals(new Date(5)));
	}

}
