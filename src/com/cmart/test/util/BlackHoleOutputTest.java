package com.cmart.test.util;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.cmart.util.BlackHoleOutput;

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

public class BlackHoleOutputTest {
	BlackHoleOutput bh = null;
	
	@Before
	public void setUp() throws Exception {
		bh = new BlackHoleOutput();
	}

	@Test
	public void testWriteInt() {
		 try {
			bh.write(5);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testWriteString() {
		try {
			bh.write("5");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
