package com.cmart.util;

/**
 * A null stop watch class for when we do not want to monitor page processing times
 * 
 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
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
public class NullStopWatch extends StopWatch{
	/**
	 * Does nothing
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void start(){
	}
	
	/**
	 * Does nothing
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void stop(){
	}
	
	/**
	 * Returns zero as we are not measuring time
	 * @return zero
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public long getTimeTaken(){
		return 0;
	}
}
