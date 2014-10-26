package com.cmart.test.util;

import static org.junit.Assert.*;
import com.cmart.util.Address;

import org.junit.Before;
import org.junit.Test;

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

public class AddressTest {
	Address a1=null;
	Address a2=null;
	@Before
	public void setUp() throws Exception {
		a1 = new Address(1, 1, "Street 1", "Town 1", "11111", 5, true);
		a2 = new Address(2, 2, "Street 2", "Town 2", "11112", 6, false);
	}

	@Test
	public void testGetId() {
		assertTrue("The returned id was incorrect", a1.getId()==1);
		assertTrue("The returned id was incorrect", a2.getId()==2);
	}

	@Test
	public void testGetUserID() {
		assertTrue("The returned user id was incorrect", a1.getId()==1);
		assertTrue("The returned user id was incorrect", a2.getId()==2);
	}

	@Test
	public void testGetStreet() {
		assertTrue("The returned street was incorrect", a1.getStreet().equals("Street 1"));
		assertTrue("The returned street was incorrect", a2.getStreet().equals("Street 2"));
	}

	@Test
	public void testGetTown() {
		assertTrue("The returned town was incorrect", a1.getTown().equals("Town 1"));
		assertTrue("The returned town was incorrect", a2.getTown().equals("Town 2"));
	}

	@Test
	public void testGetZip() {
		assertTrue("The returned zip was incorrect", a1.getZip().equals("11111"));
		assertTrue("The returned zip was incorrect", a2.getZip().equals("11112"));
	}

	@Test
	public void testGetState() {
		assertTrue("The returned state was incorrect", a1.getState()==5);
		assertTrue("The returned state was incorrect", a2.getState()==6);
	}

	@Test
	public void testGetIsDefault() {
		assertTrue("The returned is default was incorrect", a1.getIsDefault());
		assertFalse("The returned is default was incorrect", a2.getIsDefault());
	}

	@Test
	public void testToJSON() {
		String json1 = ("{\"addressid\":\"");
		json1 += "1";
		json1 += "\",\"userid\":";
		json1 += "1";
		json1 += ",\"street\":\"";
		json1 += "Street 1";
		json1 += "\",\"town\":\"";
		json1 += "Town 1";
		json1 += "\",\"zip\":\"";
		json1 += "11111";
		json1 += "\",\"state\":\"";
		json1 += "5";
		json1 += "\",\"isdefault\":\"";
		json1 += "true";
		json1 += "\"}";
		
		assertTrue("The returned JSON was incorrect, should be\n   "+json1+ "\nis " + a1.toJSON(), a1.toJSON().equals(json1));
	}

	@Test
	public void testToCSVString() {
		String csv1 = a1.getId() + ",";
		csv1 += a1.getUserID() + ",";
		csv1 += a1.getStreet() + ",";
		csv1 += a1.getTown() + ",";
		csv1 += a1.getZip() + ",";
		csv1 += a1.getState() + ",";
		csv1 += a1.getIsDefault() + ",";
		
		assertTrue("The returned CSV was incorrect, should be\n   " + csv1 + "\nis " + a1.toCSVString(), a1.toCSVString().equals(csv1));
	}

}
