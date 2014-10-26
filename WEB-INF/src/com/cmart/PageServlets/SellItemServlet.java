//package com.cmart.PageServlets;
//import java.io.*;
//import java.util.*;
//
//import javax.servlet.*;
//import javax.servlet.http.*;
//import javax.servlet.annotation.WebServlet;
//
//import com.cmart.Data.Error;
//import com.cmart.Data.Footer;
//import com.cmart.Data.GlobalVars;
//import com.cmart.Data.Header;
//import com.cmart.PageControllers.*;
//import com.cmart.util.StopWatch;
//
///**
// * This servlet is a blank example
// * 
// * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
// * @version 0.1
// * @since 0.1
// * @date 04/05/2011
// * 
// */
//@WebServlet(name="SellItemServlet", urlPatterns="/sellitem")
//public class SellItemServlet extends HttpServlet {
//	
//	private static final long serialVersionUID = 6477841456628458743L;
//	private static final String EMPTY = "";
//	private static final String title = "Sell Item";
//	private static final GlobalVars GV = GlobalVars.getInstance();
//	
//	/**
//	 * Get the page, calls the page to be made
//	 * We used to check the parameters in here, but I moved it to a controller object to keep the logic away from the layout
//	 * 
//	 * @param request
//	 * @param response
//	 * @throws ServletException
//	 * @throws IOException
//	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
//	 */
//	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		makePage(request, response, Boolean.FALSE);
//	}
//	
//	/**
//	 * Get the page, we can just pass this to doPost since the client generator will be posting userIDs and authTokens all the time
//	 * 
//	 * @param request
//	 * @param response
//	 * @throws ServletException
//	 * @throws IOException
//	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
//	 */
//	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		makePage(request, response, Boolean.TRUE);
//	}
//	
//	/**
//	 * This method starts the page timer, writes the header, creates the HTML for the page, writes the stats, and the footer
//	 * 
//	 * @param request The incoming user request
//	 * @param response  The out going user response
//	 * @throws ServletException
//	 * @throws IOException
//	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
//	 */
//	public void makePage(HttpServletRequest request, HttpServletResponse response, Boolean isGet)  throws ServletException, IOException {
//		if(request !=null && response != null){
//			response.setHeader("Access-Control-Allow-Origin", "*");
//			
//			// Do the timer if we are collecting stats
//			StopWatch timer = null;
//			if(GV.COLLECT_STATS){
//				timer = new StopWatch();
//				timer.start();
//			}
//			
//			// Create a new page controller for this page, it will get and process the data
//			SellItemController vars = new SellItemController();
//			int loopCount = 0;
//			do{
//				vars.checkInputs(request);
//				loopCount++;
//			}while(loopCount <= vars.getProcessingLoop());
//			
//			
//			
//			
//			
//			
//			vars.insertItem();
//			// If there are no errors we can try and insert the item
//			boolean redirect = false;
//			if(vars.getErrors().size()==0){
//				if((vars.itemInserted()) && (!vars.useHTML5())){
//					if(timer != null) timer.stop();
//					response.sendRedirect(vars.getRedirectURL());
//					redirect = true;
//				}
//			}
//			
//			// We are using PrintWriter to be friendly to the international community. PrintWrite auto-converts coding
//			PrintWriter out = response.getWriter();
//			
//			// If the output is to be suppressed then we'll redirect the output
//			if(vars.getSuppressOutput())
//				out = new PrintWriter(GV.BLACK_HOLE);
//			
//			// Do HTML4 or 5 depending on the global variables
//			if(!vars.useHTML5()  && !redirect){
//				// Set the return type
//				response.setContentType("text/html");
//				
//				// Write the page header
//				Header.writeHeader(out, title, vars.getUserIDString(), vars.getAuthTokenString());
//				
//				createHTML4(out, vars, isGet);
//				
//				// Redirect the output to start writing the the user again in case we were putting it in the black hole
//				out = response.getWriter();
//				
//				/*
//				 * Output any errors. These don't need to be too pretty since we hope there isn't any!
//				 * We put them here to help with debug and info, the page above should show pretty ones for users
//				 */
//				if(GV.PRINT_ALL_ERRORS){
//					GV.addErrors(out, vars.getErrors());
//				}
//				
//				/*
//				 * Process the page time
//				 */
//				if(GV.COLLECT_STATS){
//					if(timer != null) timer.stop();
//					vars.setTotalTime(timer.getTimeTaken());
//					GV.addStats(request, out, vars, 14);
//				}
//				
//				// Write the page footer
//				Footer.writeFooter(out);
//			}
//			else  if(vars.useHTML5()){
//				response.setContentType("application/json");
//				
//				createHTML5(out, vars, isGet, request);
//			}	
//		}
//	}
//	
//	/**
//	 * Creates the HTML4 version of the website
//	 * 
//	 * @param request The incoming request
//	 * @param response The response sent to the user
//	 * @param out The out writer to write the HTML to
//	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
//	 */
//	public void createHTML4(PrintWriter out, SellItemController vars, Boolean isGet){
//		if(out != null && vars != null)
//		try{
//			// Get the data needed to display the page
//			vars.getHTML4Data();
//			int loopCount = 0;
//			do{
//				vars.processHTML4();
//				loopCount++;
//			}while(loopCount <= vars.getProcessingLoop());
//			
//			
//			/*
//			 * Make the input form
//			 */
//			out.println("<form name=\"register_user\" action=\"sellitem\" method=\"POST\">");
//			out.println("<div id=\"center\">");
//			
//			out.println("<input type=\"hidden\" name=\"userID\" size=\"50\" value=\""+ vars.getUserIDString() + "\"/>");
//			out.println("<input type=\"hidden\" name=\"authToken\" size=\"50\" value=\""+ vars.getAuthTokenString() + "\"/>");
//			
//			out.println("<div id=\"floatright\">Item Name: </div>");
//			out.println("<div id=\"floatcenter\"> <input type=\"text\" name=\"name\" size=\"50\" value=\""+ vars.getName() + "\"/> </div>");
//			
//			out.println("<div id=\"floatright\">Description: </div>");
//			out.println("<div id=\"floatcenter\"> <input type=\"text\" name=\"description\" size=\"50\" value=\""+ vars.getDescription() + "\"/> </div>");
//			
//			out.println("<div id=\"floatright\">Start Price: </div>");
//			out.println("<div id=\"floatcenter\"> <input type=\"text\" name=\"startPrice\" size=\"7\" value=\""+ vars.getStartPrice() + "\"/> </div>");
//			
//			out.println("<div id=\"floatright\">Reserve Price </div>");
//			out.println("<div id=\"floatcenter\"> <input type=\"text\" name=\"reservePrice\" size=\"7\" value=\""+ vars.getReservePrice() + "\"/> </div>");
//			
//			out.println("<div id=\"floatright\">Buy Now Price: </div>");
//			out.println("<div id=\"floatcenter\"> <input type=\"text\" name=\"buyNowPrice\" size=\"7\" value=\""+ vars.getBuyNowPrice() + "\"/> </div>");
//			
//			out.println("<div id=\"floatright\">Quantity: </div>");
//			out.println("<div id=\"floatcenter\"> <input type=\"text\" name=\"quantity\" size=\"5\" value=\""+ vars.getQuantity() + "\"/> </div>");
//			
//			out.println("<div id=\"floatright\">Category: </div>");
//			out.println("<div id=\"floatcenter\">");
//			out.println("<select name=\"categoryID\">");
//			
//			String[] categorySelections = vars.getCategorySelections();
//			if(categorySelections != null)
//				for(String selection: categorySelections)
//					out.println(selection);
//			
//			out.println("</select>");
//			out.println("</div>");
//			
//			out.println("<div id=\"floatright\">End Date (yyyy-MM-dd HH:mm:ss): </div>");
//			out.println("<div id=\"floatcenter\"> <input type=\"text\" name=\"endDate\" size=\"20\" value=\""+ GV.SDF_FULL.format(vars.getEndDate()) + "\"/> </div>");
//			
//			
//			
//			out.println("<div id=\"floatright\"><input type=\"submit\" value=\"Sell\"></div>");
//			
//			out.println("</div>");
//			out.println("</form>");
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * Creates the HTML5 version of the website
//	 * 
//	 * @param request The incoming request
//	 * @param response The response sent to the user
//	 * @param out The out writer to write the HTML to
//	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
//	 */
//	public void createHTML5(PrintWriter out, SellItemController vars, Boolean isGet, HttpServletRequest request){
//		if(out != null && vars != null)
//		try{
//			// Get the data needed to display the page
//			vars.getHTML5Data();
//			vars.processHTML5();
//			//System.err.println(vars.getItemID());
//			if(vars.itemInserted()){
//				out.println("{\"success\":true,\"itemid\":"+vars.getItemID()+","+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");
//				//System.err.println("{\"success\":\"true\",\"itemid\":\""+vars.getItemID()+"\""+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");
//			}else{
//				out.println("{\"success\":false,"+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");
//				//System.err.println("Item not posted...");
//			}
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//	}
//}


// WITH CSS

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
import com.cmart.util.StopWatch;

/**
 * This servlet uploads images for an item
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
@WebServlet(name="SellItemServlet", urlPatterns="/sellitem")
public class SellItemServlet extends HttpServlet {
	
	private static final long serialVersionUID = 6477841456628458743L;
	private static final String EMPTY = "";
	private static final String title = "Sell Item";
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
		makePage(request, response, Boolean.FALSE);
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
	 * @param request The incoming user request
	 * @param response  The out going user response
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
			SellItemController vars = new SellItemController();
			int loopCount = 0;
			do{
				vars.checkInputs(request);
				loopCount++;
			}while(loopCount <= vars.getProcessingLoop());
			
			// We will try to insert the item. this will only work if everthing is actually correct
			// Otherwise it will just return
			vars.insertItem();
			
			// If there are no errors we can try and insert the item
			boolean redirect = false;
			if(vars.getErrors().size()==0){
				if((vars.itemInserted()) && (!vars.useHTML5())){
					if(timer != null) timer.stop();
					response.sendRedirect(vars.getRedirectURL());
					redirect = true;
				}
			}
			
			// We are using PrintWriter to be friendly to the international community. PrintWrite auto-converts coding
			PrintWriter out = response.getWriter();
			
			// If the output is to be suppressed then we'll redirect the output
			if(vars.getSuppressOutput())
				out = new PrintWriter(GV.BLACK_HOLE);
			
			// Do HTML4 or 5 depending on the global variables
			if(!vars.useHTML5()  && !redirect){
				// Set the return type
				response.setContentType("text/html");
				
				// Write the page header
				Header.writeHeaderNew(out, title, "sell", vars.getUserIDString(), vars.getAuthTokenString());
				
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
					GV.addStats(request, out, vars, 14);
				}
				
				// Write the page footer
				Footer.writeFooter(out);
			}
			else  if(vars.useHTML5()){
				response.setContentType("application/json");
				out.write("{");
				
				createHTML5(out, vars, isGet);
				
				/*
				 * Add the errors and page statistics
				 */
				out.print(",");
				GV.addErrorsJSON(out, vars.getErrors());
				
				// Process the page time
				if(GV.COLLECT_STATS){
					if(timer != null) timer.stop();
					vars.setTotalTime(timer.getTimeTaken());
					
					out.print(",");
					GV.addStatsJSON(request, out, vars, 14);
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
	public void createHTML4(PrintWriter out, SellItemController vars, Boolean isGet){
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
			 * Make the input form
			 */
			out.println("<script>$(function() {$( \"#endDate\" ).datetimepicker({ minDate: 0, showSecond: true,dateFormat: 'yy-mm-dd', timeFormat: 'hh:mm:ss'});});</script>");
			
			
			out.write("<div class=\"seven columns\">");
			out.println("<form name=\"register_user\" action=\"sellitem\" class=\"nice\" method=\"POST\">");
			out.println("<table>");
			out.println("<div id=\"center\">");
			
			out.println("<input type=\"hidden\" name=\"userID\" size=\"50\" value=\""+ vars.getUserIDString() + "\"/>");
			out.println("<input type=\"hidden\" name=\"authToken\" size=\"50\" value=\""+ vars.getAuthTokenString() + "\"/>");
			
			out.println("<tr><td>");
			out.println("<div id=\"floatright\">Item Name: </div>");out.println("</td><td>");
			out.println("<div id=\"floatcenter\"> <input type=\"text\" name=\"name\" size=\"50\" value=\""+ vars.getName() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Description: </div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"description\" size=\"50\" value=\""+ vars.getDescription() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Start Price: </div></td>");
			out.println("<td><div id=\"floatcenter\"> $<input type=\"text\" name=\"startPrice\" size=\"7\" value=\""+ vars.getStartPrice() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Reserve Price </div></td>");
			out.println("<td><div id=\"floatcenter\"> $<input type=\"text\" name=\"reservePrice\" size=\"7\" value=\""+ vars.getReservePrice() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Buy Now Price: </div></td>");
			out.println("<td><div id=\"floatcenter\"> $<input type=\"text\" name=\"buyNowPrice\" size=\"7\" value=\""+ vars.getBuyNowPrice() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Quantity: </div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"quantity\" size=\"5\" value=\""+ vars.getQuantity() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Category: </div></td>");
			out.println("<td><div id=\"floatcenter\">");
			out.println("<select name=\"categoryID\">");
			
			String[] categorySelections = vars.getCategorySelections();
			if(categorySelections != null)
				for(String selection: categorySelections)
					out.println(selection);
			
			out.println("</select>");
			out.println("</div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">End Date (yyyy-MM-dd HH:mm:ss): </div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"endDate\" id=\"endDate\" size=\"20\" value=\""+ GlobalVars.formatDateFull(vars.getEndDate()) + "\"/> </div></td></tr>");
			
			
			
			out.println("<tr><td colspan=\"2\"><div id=\"floatright\"><input type=\"submit\" class=\"nice small radius white button\" value=\"Sell\"></div></td></tr>");
			
			out.println("</div>");
			out.println("</table>");
			out.println("</form>");

			out.println("</div>");
			
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
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void createHTML5(PrintWriter out, SellItemController vars, Boolean isGet){
		if(out != null && vars != null)
		try{
			// Get the data needed to display the page
			vars.getHTML5Data();
			vars.processHTML5();
			
			/*
			 * If the item is inserted return true, otherwise send back the errors
			 */
			out.append("\"pageType\":\"sell\",");
			if(vars.itemInserted()){
				out.append("\"success\":true,\"itemid\":"+vars.getItemID());
			}
			else
				out.append("\"success\":false");
			
			//System.err.println(vars.getItemID());
			/*if(vars.itemInserted()){
				out.println("{\"success\":true,\"itemid\":"+vars.getItemID()+","+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");
				//System.err.println("{\"success\":\"true\",\"itemid\":\""+vars.getItemID()+"\""+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");
			}else{
				out.println("{\"success\":false,\"categories\":"+vars.getCategoriesJSON()+","+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");
				//System.err.println("Item not posted...");
			}*/
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
