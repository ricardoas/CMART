package com.cmart.PageServlets;
import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;

import com.cmart.Data.Error;
import com.cmart.Data.Footer;
import com.cmart.Data.GlobalVars;
import com.cmart.Data.Header;
import com.cmart.PageControllers.*;
import com.cmart.util.PageStatistic;
import com.cmart.util.StopWatch;

/**
 * This servlet displays the other pages statistics
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
@WebServlet(name="StatisticsServlet", urlPatterns="/statistics")
public class StatisticsServlet extends HttpServlet {
	
	private static final long serialVersionUID = 8175041456628458743L;
	private static final String EMPTY = "";
	private static final String title = "Statistics Page";
	private static final GlobalVars GV = GlobalVars.getInstance();
	private static final char SEPARATOR = ',';
	
	/**
	 * Get the page, calls the page to be made
	 * We used to check the parameters in here, but I moved it to a controller object to keep the logic away from the layout
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		makePage(request, response, Boolean.FALSE);
	}
	
	/**
	 * Get the page, we can just pass this to doPost since the client generator will be posting userIDs and authTokens all the time
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		makePage(request, response, Boolean.TRUE);
	}
	
	/**
	 * This method starts the page timer, writes the header, creates the HTML for the page, writes the stats, and the footer
	 * 
	 * @param request The incoming user request
	 * @param response  The out going user response
	 * @throws ServletException
	 * @throws IOException
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void makePage(HttpServletRequest request, HttpServletResponse response, Boolean isGet)  throws ServletException, IOException {
		if(request !=null && response != null){
			// Do the timer if we are collecting stats
			StopWatch timer = null;
			if(GV.COLLECT_STATS){
				timer = new StopWatch();
				timer.start();
			}
			
			// Create a new page controller for this page, it will get and process the data
			// Does not loop as we probably only access this to help run the experiment
			StatisticsController vars = new StatisticsController();
			vars.checkInputs(request);
			
			// We are using PrintWriter to be friendly to the international community. PrintWrite auto-converts coding
			PrintWriter out = response.getWriter();
			
			// If the output is to be suppressed then we'll redirect the output
			if(vars.getSuppressOutput())
				out = new PrintWriter(GV.BLACK_HOLE);
			
			// Do HTML4 or 5 depending on the global variables
			if(!vars.useHTML5()){
				// Set the return type
				response.setContentType("text/html");
				
				// Write the page header
				Header.writeHeaderNew(out, title, "stats",vars.getUserIDString(), vars.getAuthTokenString());
				
				createHTML4(out, vars, isGet);
				
				// Redirect the output to start writing the the user again in case we were putting it in the black hole
				out = response.getWriter();
				
				/*
				 * Output any errors. These don't need to be too pretty since we hope there isn't any!
				 * We put them here to help with debug and info, the page above should show pretty ones for users
				 */
				//if(GV.PRINT_ALL_ERRORS){
				//	GV.addErrors(out, vars.getErrors());
				//}
				
				/*
				 * Process the page time
				 */
				if(GV.COLLECT_STATS){
					if(timer != null) timer.stop();
					vars.setTotalTime(timer.getTimeTaken());
					GV.addStats(request, out, vars, 16);
				}
				
				// Write the page footer
				Footer.writeFooter(out);
			}
			else{
				createHTML5(out, vars, isGet);
			}
		}
	}
	
	/**
	 * Creates the HTML4 version of the website
	 * 
	 * @param request The incoming request
	 * @param response The response sent to the user
	 * @param out The out writer to write the HTML to
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void createHTML4(PrintWriter out, StatisticsController vars, Boolean isGet){
		if(out != null && vars != null)
		try{
			// Get the data needed to display the page
			vars.getHTML4Data();
			vars.processHTML4();
			
			out.write("<div class=\"container\">");
			out.write("<div class=\"row\">");
			out.write("<div class=\"twelve columns\">");
			
			//TODO: should I lock the stats to make it thread safe? do we care about missing a request or two?
			ArrayList<PageStatistic> oldStats;
			if(vars.getClearStats()) oldStats = GV.clearStats();
			else oldStats = GV.getStats();
			
			// Make a total
			//TODO: this only works for the same bin sizes
			long[] totalParam = new long[oldStats.get(0).getParamHist().length];
			long[] totalDB = new long[oldStats.get(0).getParamHist().length];
			long[] totalProc = new long[oldStats.get(0).getParamHist().length];
			long[] totalRender = new long[oldStats.get(0).getParamHist().length];
			long[] totalTotal = new long[oldStats.get(0).getParamHist().length];
			for(int i=0; i<oldStats.get(0).getParamHist().length; i++){
				totalParam[i] = 0;
				totalDB[i] = 0;
				totalProc[i] = 0;
				totalRender[i] = 0;
				totalTotal[i] = 0;
			}
			
			
			// Loop through the stats and print them out
			for(PageStatistic p:oldStats){
				out.println("<br>" +p.getPageName()+SEPARATOR);
				
				// Print the param times
				out.print("<br>param"+SEPARATOR);
				
				long[] hist = p.getParamHist();
				int max = hist.length;
				for(int i=0; i<max; i++){
					out.print(hist[i]);
					out.print(SEPARATOR);
					totalParam[i] += hist[i];
				}
				
				// Print the db times
				out.print("<br>db"+SEPARATOR);
				
				hist = p.getDBHist();
				max = hist.length;
				for(int i=0; i<max; i++){
					out.print(hist[i]);
					out.print(SEPARATOR);
					totalDB[i] += hist[i];
				}
				
				// Print the proc times
				out.print("<br>proc"+SEPARATOR);
				
				hist = p.getProcHist();
				max = hist.length;
				for(int i=0; i<max; i++){
					out.print(hist[i]);
					out.print(SEPARATOR);
					totalProc[i] += hist[i];
				}
				
				// Print the render times
				out.print("<br>render"+SEPARATOR);
				
				hist = p.getRenderHist();
				max = hist.length;
				for(int i=0; i<max; i++){
					out.print(hist[i]);
					out.print(SEPARATOR);
					totalRender[i] += hist[i];
				}
				
				// Print the total times
				out.print("<br>total" + SEPARATOR);
				
				hist = p.getTotalHist();
				max = hist.length;
				for(int i=0; i<max; i++){
					out.print(hist[i]);
					out.print(SEPARATOR);
					totalTotal[i] += hist[i];
				}
			}
			
			// Print the totals
			out.println("<br>Total"+SEPARATOR);
			
			// Print the param times
			out.print("<br>param"+SEPARATOR);
			
			long[] hist = totalParam;
			int max = hist.length;
			for(int i=0; i<max; i++){
				out.print(hist[i]);
				out.print(SEPARATOR);
			}
			
			// Print the db times
			out.print("<br>db"+SEPARATOR);
			
			hist = totalDB;
			max = hist.length;
			for(int i=0; i<max; i++){
				out.print(hist[i]);
				out.print(SEPARATOR);
			}
			
			// Print the proc times
			out.print("<br>proc"+SEPARATOR);
			
			hist = totalProc;
			max = hist.length;
			for(int i=0; i<max; i++){
				out.print(hist[i]);
				out.print(SEPARATOR);
			}
			
			// Print the render times
			out.print("<br>render"+SEPARATOR);
			
			hist = totalRender;
			max = hist.length;
			for(int i=0; i<max; i++){
				out.print(hist[i]);
				out.print(SEPARATOR);
			}
			
			// Print the total times
			out.print("<br>total" + SEPARATOR);
			
			hist = totalTotal;
			max = hist.length;
			for(int i=0; i<max; i++){
				out.print(hist[i]);
				out.print(SEPARATOR);
			}
			
			out.println("</div>");
			out.println("</div>");
			out.println("</div>");
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the HTML5 version of the website
	 * 
	 * @param request The incoming request
	 * @param response The response sent to the user
	 * @param out The out writer to write the HTML to
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void createHTML5(PrintWriter out, StatisticsController vars, Boolean isGet){
		if(out != null && vars != null)
		try{
			// Get the data needed to display the page
			vars.getHTML5Data();
			vars.processHTML5();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
