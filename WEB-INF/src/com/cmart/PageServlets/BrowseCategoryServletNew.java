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
import com.cmart.PageControllers.BrowseCategoryController;
import com.cmart.PageControllers.FaultController;
import com.cmart.util.Image;
import com.cmart.util.Item;
import com.cmart.util.StopWatch;

/**
 * This page allows the user to browse items
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

@WebServlet(name="BrowseCategoryServletNew", urlPatterns="/browsecategorynew")
public class BrowseCategoryServletNew extends HttpServlet{
	private static final long serialVersionUID = 6472741003023438743L;
	private static final String EMPTY = "";
	private static final String title = "Browse Category";
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
		if(request !=null && response != null && !FaultController.fault(response)){
			response.setHeader("Access-Control-Allow-Origin", "*");
			
			// Do the timer if we are collecting stats
			StopWatch timer = null;
			if(GV.COLLECT_STATS){
				timer = new StopWatch();
				timer.start();
			}
			
			// Create a new page controller for this page, it will get and process the data
			BrowseCategoryController vars = new BrowseCategoryController();
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
				Header.writeHeaderNew(out, title,"browse", vars.getUserIDString(), vars.getAuthTokenString());
				
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
					GV.addStats(request, out, vars, 0);
				}
				
				// Write the page footer
				Footer.writeFooter(out);
			}
			else{
				response.setContentType("application/json");
				
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
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void createHTML4(PrintWriter out, BrowseCategoryController vars, Boolean isGet){
		if(out != null && vars != null)
		try{		
			// Get the data needed to display the page
			vars.getHTML4Data();
			int loopCount = 0;
			do{
				vars.processHTML4();
				loopCount++;
			}while(loopCount <= vars.getProcessingLoop());
			
			
			out.write("<div class=\"container\">");
			out.write("<div class=\"row\">");
			out.write("<div class=\"twelve columns\">");
			/*
			 * Print all of the current category and sub-categories
			 */
			out.write("<div class=\"three columns\">");
			out.write("<ul STYLE=\"list-style-image: url(images/orbit/bullets.jpg)\">");
				// Print out the current category
				String parentCategoryURL = vars.getParentCategoryURL();
				//TODO: not sure this is working correctly
				if(false&&parentCategoryURL != null && !parentCategoryURL.equals("null")){
					out.println("<li>");
						out.println(vars.getParentCategoryURL());
					out.println("</li>");
				}
				
				// Print out all of the sub categories
				String[] subCategoryURLs = vars.getSubCategoryURLs();
				
				if(subCategoryURLs != null)
				for(int i=0; i < subCategoryURLs.length; i++){
					out.println("<li>");
						out.println(subCategoryURLs[i]);
					out.println("</li>");
				}
				out.write("</ul>");
			out.println("</div>");
			
			/*
			 * Print out all of the items in the current category	
			 */
			out.write("<div class=\"nine columns\">");
			ArrayList<Item> items = vars.getItems();
			String[] itemURLs = vars.getItemURLs();	
			
			out.println("<table>");
				out.println("<thead>");
				out.println("<th>");
					out.println("Name");
				out.println("</th>");
				
				out.println("<th>");
					out.println("Description");
				out.println("</th>");
				
				out.println("<th>");
					out.write("<img src=\"images/arrow_down.png\" alt=\"sort by price\" width=\"15\"/>");
					out.println(vars.getSortByPriceURL());
				out.println("</th>");
				
				out.println("<th>");
					out.write("<img src=\"images/arrow_down.png\" alt=\"sort by end date\" width=\"15\"/>");
					out.println(vars.getSortByEndURL());
				out.println("</th>");
					
				out.println("</thead>");
				
				out.println("<tbody>");
				
				if(itemURLs != null && itemURLs.length > 0)
				for(int i=0; i < itemURLs.length; i++){
					Item item = items.get(i);
					String thumbnailURL = item.getThumbnailURL();
					
					/*
					 * Thumbnail
					 * Link
					 * CurrentBid
					 * End date
					 */
					out.println("<tr id=\"entry\">");
					out.println("<td class=\"img\">");
						out.println("<img height=\"80\" width=\"80\" src=\"" + GV.REMOTE_IMAGE_IP + GV.REMOTE_IMAGE_DIR + thumbnailURL + "\" alt=\"" + item.getDescription() + "\" />");
					out.println("</td>");
					
					out.println("<td class=\"desc\">");
						out.print("<label for=\"itemlink"); out.print(item.getID()); out.print("\">"); out.print(itemURLs[i]); out.println("</label>");
					out.println("</td>");
					
					out.println("<td class=\"bid\">");
						out.print("<label for=\"itemBid"); out.print(item.getID()); out.print("\">");out.println(GV.currency.format(item.getMaxCurrentBidStartPrice()));out.println("</label>");
					out.println("</td>");
						
					out.println("<td class=\"endDate\">");
						out.print("<label for=\"itemEndDate"); out.print(item.getID()); out.print("\">");out.println(item.getEndDate());out.println("</label>");
					out.println("</td>");
					out.println("</tr>");
				}
				else{
					out.println("<tr id=\"entry\">");
					out.println("<td>");
						out.println("<BR /> Sorry, there are not items to view");
					out.println("</td>");
					out.println("</tr>");
				}
				
				out.println("</tbody>");
				
			out.println("</table>");
			out.println("</div>");
			
			out.println("</div>");
			out.println("</div>");
			out.println("</div>");
			
			out.println("<br><br>");
			
			/*
			 * Print the previous page and next page links
			 */
			out.println("<div class=\"move\">");
				out.print(vars.getPreviousPageURL());
				out.print(" | ");
				out.print(vars.getNextPageURL());
			out.println("</div>");
			
			out.println("</div>");
		}
		catch(Exception e){
			//TODO: make this output to a file
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
	public void createHTML5(PrintWriter out, BrowseCategoryController vars, Boolean isGet){
		if(out != null && vars != null)
		try{
			// Get the data needed to display the page
			vars.getHTML5Data();
			vars.processHTML5();
			
			/*
			 * Print out all of the items
			 */
			String[] itemJSON = vars.getItemJSON();
			String[] categoriesJSON=vars.getCategoryJSON();
			
			out.println("{\"categories\":[");
			if(itemJSON.length>0){
			out.print(categoriesJSON[0]);}
			for(int i=1; i<categoriesJSON.length; i=i+1){
				out.print(","+categoriesJSON[i]);}
			out.println("],");
			out.println("\"items\":[");
			if(itemJSON.length>0){
			out.print(itemJSON[0]);}
			for(int i=1; i<itemJSON.length; i++){
				out.print(","+itemJSON[i]);}
			out.println("],"+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");
			
		}
		catch(Exception e){
			//TODO: make this output to a file
			e.printStackTrace();
		}
	}
}
