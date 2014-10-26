package com.cmart.PageServlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cmart.Data.Error;
import com.cmart.Data.Footer;
import com.cmart.Data.GlobalVars;
import com.cmart.Data.Header;
import com.cmart.PageControllers.GetBulkDataController;
import com.cmart.util.Category;
import com.cmart.util.Image;
import com.cmart.util.Item;
import com.cmart.util.StopWatch;

/**
 * This page gets bulk data from the database so that the client can have all of a table
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

@WebServlet(name="GetBulkDataServlet", urlPatterns="/getbulkdata")
public class GetBulkDataServlet extends HttpServlet{
	private static final long serialVersionUID = 6472881003023438743L;
	private static final String EMPTY = "";
	private static final String title = "Get Bulk Data";
	private static final GlobalVars GV = GlobalVars.getInstance();
	
	/**
	 * Get the page, calls the page to be made
	 * We used to check the parameters in here, but I moved it to a controller object to keep the logic away from the layout
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.makePage(request, response, Boolean.FALSE);
	}
	
	/**
	 * Get the page, we can just pass this to doPost since the client generator will be posting userIDs and authTokens all the time
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		makePage(request, response, Boolean.TRUE);
	}
	
	/**
	 * This method starts the page timer, writes the header, creates the HTML for the page, writes the stats, and the footer
	 * 
	 * @param request
	 * @param response
	 * @param errorString
	 * @throws ServletException
	 * @throws IOException
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void makePage(HttpServletRequest request, HttpServletResponse response, Boolean isGet)  throws ServletException, IOException {
		if(request !=null && response != null){
			response.setHeader("Access-Control-Allow-Origin", "*");
			
			// Do the timer if we are collecting stats
			StopWatch timer = null;
			if(GV.COLLECT_STATS){
				timer = new StopWatch();
				timer.start();
			}
			
			// Create a new page controller for this page, it will get and process the data
			GetBulkDataController vars = new GetBulkDataController();
			int loopCount = 0;
			do{
				vars.checkInputs(request);
				loopCount++;
			}while(loopCount <= vars.getProcessingLoop());
			
			// We are using PrintWriter to be friendly to the international community. PrintWrite auto-converts coding
			PrintWriter out = response.getWriter();
			
			// If the output is to be suppressed then we'll redirect the output
			if(vars.getSuppressOutput())
				out = new PrintWriter(GV.BLACK_HOLE);
			
			// Do HTML4 or 5 depending on the page
			if(!vars.useHTML5()){
				// Set the return type
				response.setContentType("text/html");
				
				// Write the page header
				Header.writeHeader(out, title, vars.getUserIDString(), vars.getAuthTokenString());
				
				// Create the HTML4
				createHTML4(out, vars, isGet);
				
				// Redirect the output to start writing the the user again in case we were putting it in the black hole
				out = response.getWriter();
				
				/*
				 * Output any errors. These don't need to be too pretty since we hope there isn't any!
				 * We put them here to help with debug and info, the page above should show pretty ones for users
				 */
				if(GV.PRINT_ALL_ERRORS){
					GV.addErrors(out, vars.getErrors());
				}
				
				/*
				 * Process the page time
				 */
				if(GV.COLLECT_STATS){
					if(timer != null) timer.stop();
					vars.setTotalTime(timer.getTimeTaken());
					//GV.addStats(request, out, vars, 5);
				}
				
				// Write the page footer
				Footer.writeFooter(out);
			}
			else{
				out.write("{");

				createHTML5(out, vars, isGet);

				/*
				 * Add the errors and page statistics
				 */
				out.print(",");
				GV.addErrorsJSON(out, vars.getErrors());

				// Process the page time
				if (GV.COLLECT_STATS) {
					if (timer != null)
						timer.stop();
					vars.setTotalTime(timer.getTimeTaken());

					//out.print(",");
					//GV.addStatsJSON(request, out, vars, 7);
				}

				out.print("}");
			}
		}
	}
	
	/**
	 * Creates the HTML4 version of the website
	 * 
	 * @param request The incoming request
	 * @param response The response sent to the user
	 * @param out The out writer to write the HTML to
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void createHTML4(PrintWriter out, GetBulkDataController vars, Boolean isGet){
		if (out != null && vars != null)
			try {
				// Get the data needed to display the page
				vars.getHTML4Data();
				int loopCount = 0;
				do {
					vars.processHTML4();
					loopCount++;
				} while (loopCount <= vars.getProcessingLoop());
				
				ArrayList<Category> allCategories = vars.getCategories();
				
				if(allCategories != null)
					for(Category c: allCategories)
						out.append(c.toString());
				
				
			} catch (Exception e) {
				// TODO: make this output to a file
				e.printStackTrace();
			}
	}
	
	/**
	 * Creates the HTML5 version of the website
	 * 
	 * @param request The incoming request
	 * @param response The response sent to the user
	 * @param out The out writer to write the HTML to
	 * @return The HTML5 response (although it could just return void since it writes to the requests out writer)
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void createHTML5(PrintWriter out, GetBulkDataController vars, Boolean isGet){
		if(out != null && vars != null)
		try{
			// Get the data needed to display the page
			vars.getHTML5Data();
			vars.processHTML5();
			
			out.append("\"categories\":[");
			
			ArrayList<Category> allCategories = vars.getCategories();
			
			if(allCategories != null)
				for(int i=0; i<allCategories.size(); i++){
					Category c = allCategories.get(i);
					out.append(c.toJSON());
					
					if(i<allCategories.size()-1) out.append(",");
				}
			
			out.append("]");
			
			ArrayList<String[]> allStates = vars.getStates();
			
			out.append(",\"states\":[");
			
			if(allStates != null)
				for(int i=0; i<allStates.size(); i++){
					String[] state = allStates.get(i);
					
					if(state != null && state.length==3){
						out.append("{\"id\":\"");
						out.append(state[0]);
						out.append("\",\"shortName\":\"");
						out.append(state[1]);
						out.append("\",\"longName\":\"");
						out.append(state[2]);
						out.append("\"}");
						
						if(i< allStates.size()-1) out.append(",");
					}
				}
			
			out.append("]");
			
			/*
			 * Print out all of the items
			 */
			/*String[] itemJSON = vars.getItemJSON();
			String[] categoriesJSON=vars.getCategoryJSON();
			
			StringBuffer output=new StringBuffer();
			
			output.append("{\"categories\":[");
			if(itemJSON.length>0){
				output.append(categoriesJSON[0]);}
			for(int i=1; i<categoriesJSON.length; i=i+1){
				output.append(","+categoriesJSON[i]);}
			output.append("],");
			output.append("\"items\":[");
			if(itemJSON.length>0){
				output.append(itemJSON[0]);}
			for(int i=1; i<itemJSON.length; i++){
				output.append(","+itemJSON[i]);}
			output.append("],"+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");
			out.println(output);*/
			
			
		}
		catch(Exception e){
			//TODO: make this output to a file
			e.printStackTrace();
		}
	}
}
