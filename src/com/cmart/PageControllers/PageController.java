package com.cmart.PageControllers;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalVars;
import com.cmart.util.CheckInputs;
import com.cmart.util.StopWatch;

/**
 * This class is the base class for all of the page controller. It defines the abstract class that all page controller
 * must have to process the HTML4 and HTML5 data. It also keep track of the pages statistica
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

public abstract class PageController {
	private static final GlobalVars GV = GlobalVars.getInstance();
	protected long totalDBtime = 0;
	protected long totalProcessingTime = 0;
	protected long totalGetParamTime = 0;
	protected long totalTime = 0;
	private boolean suppressOutput = false;
	private int processingLoop = 0;
	protected ArrayList<Error> errors = new ArrayList<Error>();
	protected static final String EMPTY = "";
	private StopWatch timer = GV.getTimer();
	protected Boolean useHTML5 = Boolean.FALSE;
	
	/*public PageController(){
		
	}*/
	
	/**
	 * Checks the standard inputs that could be passed to any page
	 * @param request the request to read the paramaters from
	 * @author Andy (andrewtu@cmu.edu)
	 */
	protected void checkInputs(HttpServletRequest request){
		// Get the suppressOutput option
		this.suppressOutput = CheckInputs.checkSuppressOutput(request);
		
		// Get processing loop (if exists), it may cause more processing to happen
		this.processingLoop = CheckInputs.checkProcessingLoop(request);
		
		// Get which version of HTML to use
		this.useHTML5 = CheckInputs.checkUseHTML5(request);
	}
	
	public void startTimer(){
		this.timer.start();
	}
	
	public void stopTimerAddDB(){
		if(this.timer != null){
			this.timer.stop();
			this.totalDBtime += timer.getTimeTaken();
		}
	}
	
	public void stopTimerAddProcessing(){
		if(this.timer != null){
			this.timer.stop();
			this.totalProcessingTime += timer.getTimeTaken();
		}
	}
	
	public void stopTimerAddParam(){
		if(this.timer != null){
			this.timer.stop();
			this.totalProcessingTime += timer.getTimeTaken();
		}
	}
	
	/**
	 * Returns the amount of time the page spent getting data from the database
	 * @return long time taken in the database
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public long getDBTime(){
		return this.totalDBtime;
	}
	/**
	 * Returns the amount of time the page spend processing the data it got from the database
	 * @return long the time spent processing data
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public long getProcessingTime(){
		return this.totalProcessingTime;
	}
	
	/**
	 * Returns the time taken to read the parameters passed to the page
	 * @return long the time taken to get the parameters
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public long getParamTime(){
		return this.totalGetParamTime;
	}
	
	/**
	 * Sets the total time taken for the page to process
	 * @param totalTime
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setTotalTime(long totalTime){
		this.totalTime = totalTime;
	}
	
	/**
	 * Returns the total time taken to finish the page (excluding the footer since we need to the time taken so we can print it)
	 * @return long the total time taken to parse the page
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public long getTotalTime(){
		return this.totalTime;
	}
	
	/**
	 * Returns whether the page should suppress the text output
	 * Andy: I was using this to reduce network traffic
	 * 
	 * @return boolean if we should not output text to the user
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public boolean getSuppressOutput(){
		return this.suppressOutput;
	}
	
	/**
	 * Returns how many times the page should process the page data
	 * Andy: I was using this to stress test the app tier without stressing the DB. Can also be used to pretend
	 * that the app is more complex than it really is
	 * 
	 * @return int the number of times to loop the processing of the page's parameters
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public int getProcessingLoop(){
		return this.processingLoop;
	}
	
	/**
	 * Returns any errors that happened while processing the page. Errors will be different severity on different
	 * pages. Browsing doesn't care if you're not logged in
	 * 
	 * @return ArrayList<Error> the errors that are present on the page
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public ArrayList<Error> getErrors(){
		return this.errors;
	}
	
	public Boolean useHTML5(){
		return this.useHTML5;
	}
	
	/**
	 * Abstract method that should get the data that the HTML4 version of the website needs to allow it to process
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	abstract public void getHTML4Data();
	
	/**
	 * Abstract method that should process the data that the HTML4 database returned
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	abstract public void processHTML4();
	
	/**
	 * Abstract method that should get the data that the HTML5 version of the website needs to allow it to process
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	abstract public void getHTML5Data();
	
	/**
	 * Abstract method that should process the data that the HTML5 database returned
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	abstract public void processHTML5();
}
