package com.cmart.PageControllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cmart.Data.Error;
import com.cmart.Data.GlobalVars;
import com.cmart.util.CheckInputs;
import com.cmart.util.StopWatch;


public class FaultController extends PageController{
	private static final GlobalVars GV = GlobalVars.getInstance();
	private static Random rand = new Random();
	private static int avgDelay= 2000;
	private static double failProb = 0.0;
	private static double slowProb = 0.0;
	private static double leakProb = 0.0;
	private static ArrayList<StringBuffer> memoryLeak = new ArrayList<StringBuffer>();
	
	// Variables passed in the request
	private long userID = -1;
	private String authToken = null;
	
	// Structures to hold the DB data
	
	// Structures to hold the parsed page data

	/**
	 * This method checks the page for any input errors that may have come from Client generator error
	 * These would need to be check in real life to stop users attempting to hack and mess with things
	 * 
	 * @param request
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void checkInputs(HttpServletRequest request){
		super.startTimer();
		
		if(request != null){
			super.checkInputs(request);
			
			// Get the userID (if exists), we will pass it along to the next pages
			try{
				this.userID = CheckInputs.checkUserID(request);
			}
			catch(Error e){	}
			
			// Get the authToken (if exists), we will pass it along to the next pages
			try{
				this.authToken = CheckInputs.checkAuthToken(request);
			}
			catch(Error e){	}
			
			String newDelay = CheckInputs.getParameter(request, "avgDelay");
			if(!newDelay.equals("")){
				try{
					avgDelay = Integer.parseInt(newDelay);
				}catch(Exception e){}
			}
			
			String fail = CheckInputs.getParameter(request, "failProb");
			if(!fail.equals("")){
				try{
					failProb = Double.parseDouble(fail);
				}catch(Exception e){}
			}
			
			String slow = CheckInputs.getParameter(request, "slowProb");
			if(!slow.equals("")){
				try{
					slowProb = Double.parseDouble(slow);
				}catch(Exception e){}
			}
			
			/*String maxdb = CheckInputs.getParameter(request, "maxdb");
			if(!maxdb.equals("")){
				try{
					int temp = Integer.valueOf(maxdb);
					GV.DB.setDBCount(temp);
				}catch(Exception e){}
			}*/
			
		}
		
		// Calculate how long that took
		super.stopTimerAddParam();
	}

	/**
	 * This method get the data needed for the HTML4 page from the database
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void getHTML4Data() {	
		super.startTimer();
		
		// Calculate how long that took
		super.stopTimerAddProcessing();
	}

	/**
	 * This method processes all of the data that was read from the database such that it is ready to be printed
	 * on to the page. We try to do as much of the page logic here as possible
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void processHTML4() {
		super.startTimer();
		
		// Calculate how long that took
		super.stopTimerAddProcessing();
	}
	
	/**
	 * Gets the HTML5 data from the database
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void getHTML5Data(){
		super.startTimer();
		
		// Calculate how long that took
		super.stopTimerAddDB();
	}
	
	/**
	 * Processes the HTML5 data that is needed to create the page
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void processHTML5(){
		super.startTimer();
		
		super.stopTimerAddProcessing();
	}
	
	public static boolean fault(HttpServletResponse response){
		/*
		 * Create a slow down error
		 */
		if(slowProb > rand.nextDouble()){
			try {
				//this.w
				
				Thread.sleep((long)Math.abs(rand.nextGaussian()*avgDelay));
				//response.wait(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/*
		 * Leak some memory
		 */
		if(leakProb > rand.nextDouble()){
			StringBuffer leak = new StringBuffer(4028);
			
			// 1KB
			for(int i=0; i<200; i++){
				leak.append("In your RAM, leaking you memorizes!!!!!!!!!!!!!!!!");
			}
			
			synchronized(memoryLeak){
				memoryLeak.add(leak);
			}
		}
		
		
		/*
		 * Use some additional resources
		 */
		// CPU
		
		
		// DISK
		
		/*
		 * Create a server error
		 */
		if(failProb > rand.nextDouble()){
			try {
				response.sendError(response.SC_INTERNAL_SERVER_ERROR);
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	/**
	 * Returns the current userID as a String
	 * 
	 * @return String the userID
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getUserIDString(){
		return Long.toString(this.userID);
	}
	
	/**
	 * Returns the authToken sent to the page
	 * 
	 * @return string the authToken
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getAuthTokenString(){
		return this.authToken;
	}
}
