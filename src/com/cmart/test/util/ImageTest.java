package com.cmart.test.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.cmart.util.Image;

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

public class ImageTest {
	Image i1;
	
	@Before
	public void setUp() throws Exception {
		i1 = new Image(1, "url", "desc");
	}

	@Test
	public void testGetPosition() {
		assertTrue("The position returned is incorrect", i1.getPosition()==1);
	}

	@Test
	public void testGetUrl() {
		assertTrue("The url returned is incorrect", i1.getUrl().equals("url"));
	}

	@Test
	public void testGetDescription() {
		assertTrue("The description returned is incorrect", i1.getDescription().equals("desc"));
	}

	@Test
	public void testToJSON() {
		String json1 = "{\"url\":\"";
		json1 += "url";
		json1 += "\",\"description\":\"";
		json1 += "desc";
		json1 += "\",\"position\":\"";
		json1 += "1";
		json1 += "\"}";
		
		assertTrue("The JSON returned is incorrect", i1.toJSON().equals(json1));
	}

}
