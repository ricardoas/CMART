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
 * This servlet allows a user to buy an item 
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
@WebServlet(name="BuyItemServlet", urlPatterns="/buyitem")
public class BuyItemServlet extends HttpServlet {
	
	private static final long serialVersionUID = 8975041456628458743L;
	private static final String EMPTY = "";
	private static final String title = "Buy Item";
	//private static boolean bought = false;
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
			BuyItemController vars = new BuyItemController();
			int loopCount = 0;
			do{
				vars.checkInputs(request);
				loopCount++;
			}while(loopCount <= vars.getProcessingLoop());
			
			boolean redirect = false;
			if(vars.getErrors().size() == 0){
				if(vars.buyNow() && vars.getErrors().size() == 0){
					if(!vars.useHTML5()){
					timer.stop();
					response.sendRedirect(vars.getRedirectURL());
					redirect = true;
					}
				}
			}
			
			// We are using PrintWriter to be friendly to the international community. PrintWrite auto-converts coding
			PrintWriter out = response.getWriter();
			
			// If the output is to be suppressed then we'll redirect the output
			if(vars.getSuppressOutput())
				out = new PrintWriter(GV.BLACK_HOLE);
			
			// Do HTML4 or 5 depending on the global variables
			if(!vars.useHTML5() && !redirect){
				// Set the return type
				response.setContentType("text/html");
				
				// Write the page header
				Header.writeHeaderNew(out, title, "item",vars.getUserIDString(), vars.getAuthTokenString());
				
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
					GV.addStats(request, out, vars, 1);
				}
				
				// Write the page footer
				Footer.writeFooter(out);
			}
			else if(vars.useHTML5()){
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
					GV.addStatsJSON(request, out, vars, 1);
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
	public void createHTML4(PrintWriter out, BuyItemController vars, Boolean isGet){
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
			out.write("<div class=\"six columns\">");
			
			if(vars.getBuyFailure())
				out.println("Sorry. You could not buy the item. Someone may have purchased it already<br />");
			
			/*
			 * Make the input form
			 */
			out.println("<form name=\"register_user\" action=\"buyitem\" class=\"nice\" method=\"POST\">");
			out.println("<table>");
			out.println("<div id=\"center\">");
			
			out.println("<input type=\"hidden\" name=\"userID\" value=\"" + vars.getUserIDString() + "\">");
			out.println("<input type=\"hidden\" name=\"authToken\" value=\"" + vars.getAuthTokenString() + "\">");
			out.println("<input type=\"hidden\" name=\"itemID\" value=\"" + vars.getItemID() + "\">");
			out.println("<input type=\"hidden\" name=\"addressID\" value=\"" + vars.getAddressID() + "\">");
			out.println("<input type=\"hidden\" name=\"accountID\" value=\"" + vars.getAccountID() + "\">");
			
			out.println("<tr><td><div id=\"floatright\">Quantity</div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"quantity\" size=\"50\" value=\""+ vars.getQuantity() + "\"/> </div></td></tr>");
			
			out.println("<tr><td colspan=\"2\"><div id=\"floatright\">Payment Address: </div></td></tr>");
			out.println("<tr><td><div id=\"floatright\">Street</div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"street\" size=\"50\" value=\""+ vars.getStreet() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Town</div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"town\" size=\"50\" value=\""+ vars.getTown() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Zip code</div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"zip\" size=\"10\" maxlength=\"10\" value=\""+ vars.getZip() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">State</div></td>");
			out.println("<td><div id=\"floatcenter\">");
			out.println("<select name=\"state\">");
			
			String[] stateSelections = vars.getStateSelections();
			if(stateSelections != null)
				for(String selection: stateSelections)
					out.println(selection);
			
			out.println("</select>");
			out.println("</div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Name on card</div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"nameOnCard\" size=\"50\" value=\""+ vars.getNameOnCard() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Credit Card Number: (5500000000000004)</div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"creditCardNo\" size=\"16\" value=\""+ vars.getCreditCardNo() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">CVV2 code</div>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"cvv\" size=\"4\" value=\""+ vars.getCVV() + "\"/> </div></td></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Expiration Date (MMYYYY)</div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"expirationDate\" size=\"6\" value=\""+ vars.getExpirationDateString() + "\"/> </div></td></tr>");
			
			
			out.println("<tr><td colspan=\"2\"><div id=\"floatright\"><input type=\"submit\" class=\"nice small radius white button\" value=\"Buy Item\"></div></td></tr>");
			out.println("</table></form>");
			
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
	public void createHTML5(PrintWriter out, BuyItemController vars, Boolean isGet){
		
		if (out != null && vars != null)
			try {
				// If we bought the item we need to skip the page, as it will fail the availability test
				if (!vars.getBuyFailure() && vars.getErrors().size() == 0) {
					out.println("\"pageType\":\"buyitem\",");
					out.println("\"success\":true");
					out.println(",\"itemID\":" + vars.getItemID());
				} else {
					// Get the data needed to display the page
					vars.getHTML5Data();
					vars.processHTML5();
					out.println("\"pageType\":\"buyitem\",");
					out.println("\"success\":false");

					if (vars.getReturnAddress()) {
						if (vars.getAddress() != null)
							out.append(",\"address\":"
									+ vars.getAddress().toJSON());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
