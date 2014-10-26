package com.cmart.Data;

import java.io.PrintWriter;

/**
 * Writes the footer of the page
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
public class Footer {
	private Footer() {}
	
	/**
	 * Write the footer of the page
	 * 
	 * @param out The output writer to write to
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public static void writeFooter(PrintWriter out){
		if(out != null){
			// Include footer
			out.println("</div>");
			out.println("</BODY></HTML>");
		}
	}
}
