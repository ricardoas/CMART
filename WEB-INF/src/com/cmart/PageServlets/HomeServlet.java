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
import com.cmart.util.Item;
import com.cmart.util.StopWatch;

/**
 * This servlet is the home page servlet
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
@WebServlet(name="HomeServlet", urlPatterns="/index")
public class HomeServlet extends HttpServlet {

	private static final long serialVersionUID = 6475041856628458743L;
	private static final String EMPTY = "";
	private static final String title = "Welcome to CMART";
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
			HomeController vars = new HomeController();
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

			// Do XML response depending on get itemPriceRequest
			if(vars.getRecommendation()){
				response.setContentType("text/xml");
				createXML(out, vars);
				// Do HTML4 or 5 depending on the global variables
			} else if(!vars.useHTML5()){
				// Set the return type
				response.setContentType("text/html");

				// Write the page header
				Header.writeHeaderNew(out, title, "home",vars.getUserIDString(), vars.getAuthTokenString());

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
					GV.addStats(request, out, vars, 6);
				}

				// Write the page footer
				Footer.writeFooter(out);
			}
			else{
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
					GV.addStatsJSON(request, out, vars, 6);
				}

				out.print("}");
			}
		}
	}

	/**
	 * This method starts the page timer, creates the XML responseText for the getRecommendation request by AJAX
	 * @param request The incoming user request
	 * @param response  The out going user response
	 * 
	 * @author bo (bol1@andrew.cmu.edu)
	 * */
	public void createXML(PrintWriter out, HomeController vars){
		if(out != null && vars != null){
			try{
				// Get the data needed to display the page
				vars.getXMLData();
				vars.processXML();
				ArrayList<Item> items = vars.getItems();
				if(items != null){
					StringBuffer content = new StringBuffer("");
					content.append("<?xml version=\"1.0\"  encoding=\"UTF-8\" ?>\n");
					content.append("<recommendation>\n");
					for(int i = 0; i < items.size(); i++){

						content.append("\t<item>\n");
						content.append("\t\t<id>" + items.get(i).getID() + "</id>\n");
						content.append("\t\t<name>" + items.get(i).getName() + "</name>\n");
						content.append("\t\t<thumbnailURL>" + GV.REMOTE_IMAGE_IP + GV.REMOTE_IMAGE_DIR + items.get(i).getThumbnailURL() + "</thumbnailURL>\n");
						content.append("\t\t<pageNo>" + vars.getPageNo() + "</pageNo>\n");
						content.append("\t</item>\n");
					}
					content.append("</recommendation>\n");
					out.print(content);
				}

			}
			catch(Exception e){
				e.printStackTrace();
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
	public void createHTML4(PrintWriter out, HomeController vars, Boolean isGet){
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

				out.println("<div class=\"body\">");
				out.println("<br />Welcome to C-MART. Please choose from the top menu to start<br />");

				out.println("<br />Try logging in as<br /> user: User1<br />password: User1<br />");
				
				// Display recommended items
				if(!vars.getUserIDString().equals("-1") && vars.getAuthTokenString() != null){
					out.println("<input type=\"hidden\" name=\"pageNo\" id=\"pageNo\" value=\"" + vars.getPageNo() + "\">");
					out.println("<input type=\"hidden\" name=\"userID\" id=\"userID\" value=\"" + vars.getUserIDString() + "\">");
					out.println("<input type=\"hidden\" name=\"authToken\" id=\"authToken\" value=\"" + vars.getAuthTokenString() + "\">");
					out.println("<input type=\"hidden\" name=\"itemsPP\" id=\"itemsPP\" value=\"" + vars.getItemsPP() + "\">");

					ArrayList<Item> items = vars.getItems();
					if(items != null){

						out.println("<script src=\"js/getRecommendationItem.js\" type=\"text/javascript\"></script>");
						out.println("<body onload=\"makeRequest(0)\"></body>");
						out.println("<br />We recommend below items for you!<br />");
						out.println("<br />");

						out.println("<a id=\"recommendation_prev\" href = \"javascript:makeRequest(0)\">Prev</a>");

						out.println("<div class=\"home_recommendation\">");
						out.println("<ul>");
						for(int i = 0; i < Math.min(items.size(), vars.getItemsPP()) ; i++){
							out.println("<li>");
							out.println("<div class=\"pic\"><img id=\"recommendedImage_" + i + "\" src=\"\" alt=\"Now loading\" /></div>");
							out.println("<div class=\"txt\"><a id=\"recommendationItem_" + i + "\" href=\"\" /></div>");
							out.println("</li>");
						}
						out.println("</ul>");
						out.println("</div>");


						if(items.size() >= vars.getItemsPP()){
							out.println("<a id=\"recommendation_next\" href = \"javascript:makeRequest(1)\">Next</a>");
						}else{
							out.println("<a id=\"recommendation_next\" href = \"javascript:makeRequest(0)\">Next</a>");
						}

						out.println("<br /><br /><br /><br /><br /><br /><br />");
					}
				}

				out.println("<div>");
				out.println("</div>");
				out.println("</div>");
				out.println("</div>");

				out.println("<br /><br /><br /><br /><br /><br /><br />");

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
	public void createHTML5(PrintWriter out, HomeController vars, Boolean isGet){
		if(out != null && vars != null)
			try{
				// Get the data needed to display the page
				vars.getHTML5Data();
				vars.processHTML5();

				out.append("\"index\":[]");
			}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
