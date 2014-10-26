package com.cmart.util;

import java.io.*;

/**
 * This class allows output to be written to nowhere. We can use this to prevent pages being returned
 * I found this useful to just hit the DB and create CPU cycles without a large network load
 * 
 * @author Andy (andrewtu@cmu.edu)
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

public class BlackHoleOutput extends OutputStream{
	/**
	 * Do nothing with written input
	 * 
	 * @param arg0 The data not to be written
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void write(int arg0) throws IOException {
		// Do nothing in this black hole
	}
	
	/**
	 * Do nothing with written input
	 * 
	 * @param arg0 The data not to be written
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void write(String arg0) throws IOException {
		// Do nothing in this black hole
	}
}
