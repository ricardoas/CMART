package com.cmart.util;

/**
 * A stop watch class to time how long pags take to process
 * TODO: Andy: Check the overhead of the monitoring, whatever the result, see if you can make it lower
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
public class StopWatch {
	private long startTime = 0;
	private long stopTime = 0;
	private boolean running = false;
	
	/**
	 * Starts the stop watch
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void start(){
		this.startTime = System.nanoTime();
		running = true;
	}
	
	/**
	 * Stops the stop watch
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void stop(){
		this.stopTime = System.nanoTime();
		running = false;
	}
	
	/**
	 * Returns the amount of elapsed time between the start and now (or the end of the timer)
	 * @return long elapsed time
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public long getTimeTaken(){
		if(running){
			return  (System.nanoTime() - startTime)/1000000;
		}
		else return (stopTime - startTime)/1000000;
	}
}
