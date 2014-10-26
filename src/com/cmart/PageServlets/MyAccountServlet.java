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
//import com.cmart.util.Bid;
//import com.cmart.util.Item;
//import com.cmart.util.Purchase;
//import com.cmart.util.StopWatch;
//
///**
// * This servlet is the user account
// *
// * @author Andy (andrewtu@cmu.edu)
// * @version 0.1
// * @since 0.1
// * @date 04/05/2011
// *
// */
//@WebServlet(name="MyAccountServlet", urlPatterns="/myaccount") public class MyAccountServlet extends HttpServlet {
//
//	private static final long serialVersionUID = 6475041006628458743L;
//	private static final String EMPTY = "";
//	private static final String title = "My Account";
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
//	 * @author Andy (andrewtu@cmu.edu)
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
//	 * @author Andy (andrewtu@cmu.edu)
//	 */
//	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		this.makePage(request, response, Boolean.TRUE);
//	}
//
//	/**
//	 * This method starts the page timer, writes the header, creates the HTML for the page, writes the stats, and the footer
//	 *
//	 * @param request The incoming user request
//	 * @param response  The out going user response
//	 * @throws ServletException
//	 * @throws IOException
//	 * @author Andy (andrewtu@cmu.edu)
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
//			MyAccountController vars = new MyAccountController();
//			int loopCount = 0;
//			do{
//				vars.checkInputs(request);
//				loopCount++;
//			}while(loopCount <= vars.getProcessingLoop());
//
//
//			// We are using PrintWriter to be friendly to the international community. PrintWrite auto-converts coding
//			PrintWriter out = response.getWriter();
//
//			// If the output is to be suppressed then we'll redirect the output
//			if(vars.getSuppressOutput())
//				out = new PrintWriter(GV.BLACK_HOLE);
//
//			// Do HTML4 or 5 depending on the global variables
//			if(!vars.useHTML5()){
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
//					GV.addStats(request, out, vars, 11);
//				}
//
//				// Write the page footer
//				Footer.writeFooter(out);
//			}
//			else{
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
//	 * @author Andy (andrewtu@cmu.edu)
//	 */
//	public void createHTML4(PrintWriter out, MyAccountController vars, Boolean isGet){
//		if(out != null && vars != null)
//			try{
//				// Get the data needed to display the page
//				vars.getHTML4Data();
//				int loopCount = 0;
//				do{
//					vars.processHTML4();
//					loopCount++;
//				}while(loopCount <= vars.getProcessingLoop());
//
//
//				out.println("<div class=\"topblock\">");
//				// Print links available from user account screen
//				out.println(vars.getUpdateUserDetailsURL());
//				out.println("<BR />");
//
//				// Print the welcome message
//				if(vars.getWelcomeMessage() != null){
//					out.println(vars.getWelcomeMessage());
//					out.println("<BR />");
//				}
//				out.println("</div>");
//
//				/*
//				 *  Print the items the user is currently bidding on
//				 */
//				out.println("<div class=\"bidtable\">");
//				out.println("<div class=\"title\">");
//				out.println("Currently Bidding Items");
//				out.println("</div>");
//				if(vars.getCurrentBids() != null && vars.getCurrentBiddingItemURLs()!=null){
//					ArrayList<Bid> currentBids = vars.getCurrentBids();
//					String[] items = vars.getCurrentBiddingItemURLs();
//					String[] images = vars.getCurrentBiddingThumbnails();
//					int length = items.length;
//
//					for(int i=0; i< length; i++){
//						Item tempItem = currentBids.get(i).getItem();
//						out.printf("<div class=\"row%s\" id=\"entry\">\n", i%2==0? 1 :2 );
//						int itemID = -1;
//						if(tempItem != null) itemID = tempItem.getID();
//
//						out.print("<label for=\"currentBids");out.print(tempItem==null ? "missing" :itemID); out.print("\">");
//
//						out.println("<div class=\"img\">");
//						out.println("<img height=\"80\" width=\"80\" src=\"" + GV.REMOTE_IMAGE_IP + GV.REMOTE_IMAGE_DIR + images[i] + "\" alt=\"\" />");
//						out.println("</div>");
//
//						out.println("<div class=\"description\">");	
//						out.println(items[i]);
//						out.println("</div>");
//
//						out.println("<div class=\"bid\">");
//						out.println(GV.makeLabel("bid", itemID));
//						out.println(GV.currency.format(currentBids.get(i).getBid()));
//						out.println("</label>");
//						out.println("</div>");
//
//						out.println("<div class=\"maxbid\">");
//						out.println(GV.makeLabel("maxBid", itemID));
//						out.println(tempItem==null ? "missing" : GV.currency.format(currentBids.get(i).getMaxBid()));
//						out.println("</label>");
//						out.println("</div>");
//
//						out.println("<div class=\"enddate\">");
//						out.println(GV.makeLabel("endDate", itemID));
//						out.println(tempItem==null ? "missing" : tempItem.getEndDate());
//						out.println("</label>");
//						out.println("</div>");
//
//						out.println("</label>");
//						out.println("</div>");
//					}
//				}
//				else{
//					out.printf("<div class=\"row1\" id=\"entry\">\n");
//					out.println("<div class=\"description\">");
//					out.println("No Current Bids<BR />");
//					out.println("</div>");
//					out.println("</div>");
//				}
//				out.println("</div>");
//
//
//				/*
//				 *  Print the items the user has previously bid on
//				 */
//				out.println("<div class=\"bidtable\">");
//				out.println("<div class=\"title\">");
//				out.println("Previous Bids");
//				out.println("</div>");
//
//				if(vars.getOldBids() != null && vars.getOldBiddingItemURLs()!= null){
//					ArrayList<Bid> oldBids = vars.getOldBids();
//					String[] items = vars.getOldBiddingItemURLs();
//					int length = items.length;
//
//					for(int i=0; i< length; i++){
//						Item tempItem = oldBids.get(i).getItem();
//						out.printf("<div class=\"row%s\" id=\"entry\">\n", i%2==0? 1 :2 );
//						int itemID = -1;
//						if(tempItem != null) itemID = tempItem.getID();
//
//						out.print("<label for=\"previousBids");out.print(tempItem==null ? "missing" : itemID); out.print("\">");
//
//						out.println("<div class=\"description\">");		
//						out.println(items[i]);
//						out.println("</div>");
//
//						out.println("<div class=\"bid\">");
//						out.println(GV.makeLabel("bid", itemID));
//						out.println(oldBids.get(i).getBid());
//						out.println("</label>");
//						out.println("</div>");
//
//						out.println("<div class=\"maxbid\">");
//						out.println(GV.makeLabel("maxBid", itemID));
//						out.println(tempItem==null ? "missing" : GV.currency.format(tempItem.getMaxBid()));
//						out.println("</label>");
//						out.println("</div>");
//
//						out.println("<div class=\"enddate\">");
//						out.println(GV.makeLabel("endDate", itemID));
//						out.println(tempItem==null ? "missing" : tempItem.getEndDate());
//						out.println("</label>");
//						out.println("</div>");
//
//						out.println("</label>");
//						out.println("</div>");
//					}
//				}
//				else{
//					out.printf("<div class=\"row1\" id=\"entry\">\n");
//					out.println("<div class=\"description\">");
//					out.println("No Old Bids<BR />");
//					out.println("</div>");
//					out.println("</div>");
//				}
//				out.println("</div>");
//
//				/*
//				 * Print the items that the user has purchased
//				 */
//				out.println("<div class=\"purchasestable\">");
//				out.println("<div class=\"title\">");
//				out.println("Purchases");
//				out.println("</div>");
//
//				if(vars.getPurchases() != null && vars.getPayURLs() != null){
//					ArrayList<Purchase> purchases = vars.getPurchases();
//					String[] payURLs = vars.getPayURLs();
//					String[] paidText=vars.getPaidText();
//					int length = payURLs.length;
//
//					for(int i=0; i< length; i++){
//						Purchase tempPurchase = purchases.get(i);
//						Item tempItem = purchases.get(i).getItem();
//
//						out.printf("<div class=\"row%s\" id=\"entry\">\n", i%2==0? 1 :2 );
//						int itemID = -1;
//						if(tempItem != null) itemID = tempItem.getID();
//
//						out.print("<label for=\"purchases");out.print(tempItem==null ? "missing" : itemID); out.print("\">");
//
//						out.println("<div class=\"description\">");		
//						out.println(payURLs[i]);
//						out.println("</div>");
//
//						out.println("<div class=\"quantity\">");
//						out.println(GV.makeLabel("quantity", itemID));
//						out.println(tempPurchase.getQuantity());
//						out.println("</label>");
//						out.println("</div>");
//
//						out.println("<div class=\"price\">");
//						out.println(GV.makeLabel("price", itemID));
//						out.println(GV.currency.format(tempPurchase.getPrice()));
//						out.println("</label>");
//						out.println("</div>");
//
//						out.println("<div class=\"paid\">");
//						out.println(GV.makeLabel("paid", itemID));
//						out.println(paidText[i]);
//						out.println("</label>");
//						out.println("</div>");
//
//						out.println("</label>");
//						out.println("</div>");
//					}
//				}
//				else{
//					out.printf("<div class=\"row1\" id=\"entry\">\n");
//					out.println("<div class=\"description\">");
//					out.println("No Purchases<BR />");
//					out.println("</div>");
//					out.println("</div>");
//				}
//				out.println("</div>");
//
//
//
//				/*
//				 *  Print out the items that the user is currently selling
//				 */
//				out.println("<div class=\"itemtable\">");
//				out.println("<div class=\"title\">");
//				out.println("Items Currently Selling");
//				out.println("</div>");
//
//				if(vars.getCurrentSellingItems() != null && vars.getCurrentSellingItemURLs()!= null){
//					ArrayList<Item> currentItems = vars.getCurrentSellingItems();
//					String[] items = vars.getCurrentSellingItemURLs();
//					int length = items.length;
//
//					for(int i=0; i< length; i++){
//						out.printf("<div class=\"row%s\" id=\"entry\">\n", i%2==0? 1 :2 );
//						out.print("<label for=\"currentlySelling");out.print(currentItems.get(i).getID());out.print("\">");
//
//						out.println("<div class=\"description\">");
//						out.println(items[i]);
//						out.println("</div>");
//
//						out.println("<div class=\"maxbid\">");
//						out.println(GV.makeLabel("maxBid", currentItems.get(i).getID()));
//						out.println(GV.currency.format(currentItems.get(i).getMaxBid()));
//						out.println("</label>");
//						out.println("</div>");
//
//						out.println("<div class=\"enddate\">");
//						out.println(GV.makeLabel("endDate", currentItems.get(i).getID()));
//						out.println(currentItems.get(i).getEndDate());
//						out.println("</label>");
//						out.println("</div>");
//
//						out.println("</label>");
//						out.println("</div>");
//					}
//				}
//				else{
//					out.printf("<div class=\"row1\" id=\"entry\">\n");
//					out.println("<div class=\"description\">");
//					out.println("Not Currently Selling<BR />");
//					out.println("</div>");
//					out.println("</div>");
//				}
//				out.println("</div>");
//
//
//				/*
//				 *  Print out the items that the user is previously sold
//				 */
//				out.println("<div class=\"itemtable\">");
//				out.println("<div class=\"title\">");
//				out.println("Items Previously Sold");
//				out.println("</div>");
//				if(vars.getOldSellingItems() != null && vars.getOldSellingItemURLs()!= null){
//					ArrayList<Item> oldItems = vars.getOldSellingItems();
//					String[] items = vars.getOldSellingItemURLs();
//					int length = items.length;
//
//					for(int i=0; i< length; i++){
//						out.printf("<div class=\"row%s\" id=\"entry\">\n", i%2==0? 1 :2 );
//						out.print("<label for=\"previouslySold");out.print(oldItems.get(i).getID()); out.print("\">");
//
//						out.println("<div class=\"description\">");
//						out.println(items[i]);
//						out.println("</div>");
//
//						out.println("<div class=\"maxbid\">");
//						out.println(GV.makeLabel("maxBid", oldItems.get(i).getID()));
//						out.println(GV.currency.format(oldItems.get(i).getMaxBid()));
//						out.println("</label>");
//						out.println("</div>");
//
//						out.println("<div class=\"enddate\">");
//						out.println(GV.makeLabel("endDate", oldItems.get(i).getID()));
//						out.println(oldItems.get(i).getEndDate());
//						out.println("</label>");
//						out.println("</div>");
//
//						out.println("</label>");
//						out.println("</div>");
//					}
//				}
//				else{
//					out.printf("<div class=\"row1\" id=\"entry\">\n");
//					out.println("<div class=\"description\">");
//					out.println("No Previously Sold Items<BR />");
//					out.println("</div>");
//					out.println("</div>");
//				}
//
//			}
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
//	 * @author Andy (andrewtu@cmu.edu)
//	 */
//	public void createHTML5(PrintWriter out, MyAccountController vars, Boolean isGet, HttpServletRequest request){
//		if(out != null && vars != null)
//			try{
//				// Get the data needed to display the page
//				vars.getHTML5Data();
//				vars.processHTML5();
//
//
//				ArrayList<Bid> bids = vars.getCurrentBids();
//				ArrayList<Item> items = vars.getCurrentSellingItems();
//
//
//				StringBuffer output=new StringBuffer("{\"newbids\":[");
//				if(vars.getCurrentBids() != null){
//					Iterator<Bid> bidIterators = bids.iterator();
//					if(bidIterators.hasNext()){
//						output.append(bidIterators.next().toJSON());}
//					while(bidIterators.hasNext()){
//						output.append(",").append(bidIterators.next().toJSON());
//					}
//				}
//				output.append("],");
//				output.append("\"newitems\":[");
//				if(vars.getCurrentSellingItems() != null){
//					Iterator<Item> itemIterators = items.iterator();
//					if(itemIterators.hasNext()){
//						output.append(itemIterators.next().toJSON());}
//					while(itemIterators.hasNext()){
//						output.append(",").append(itemIterators.next().toJSON());
//					}
//				}
//				output.append("],");	
//				bids = vars.getOldBids();
//				items = vars.getOldSellingItems();
//
//
//				output.append("\"oldbids\":[");
//				if(vars.getOldBids()!=null){
//					Iterator<Bid> bidIterators = bids.iterator();
//					if(bidIterators.hasNext()){
//						output.append(bidIterators.next().toJSON());}
//					while(bidIterators.hasNext()){
//						output.append(","+bidIterators.next().toJSON());
//					}}
//				output.append("],");
//				output.append("\"olditems\":[");
//				if(vars.getOldSellingItems()!=null){
//					Iterator<Item> itemIterators = items.iterator();
//					if(itemIterators.hasNext()){
//						output.append(itemIterators.next().toJSON());}
//					while(itemIterators.hasNext()){
//						output.append(","+itemIterators.next().toJSON());
//					}}
//				output.append("],").append(GV.addStatsHTML5(vars)).append(",").append(GV.addErrorsHTML5(vars.getErrors())).append("}");
//				out.println(output);
//
//			}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//	}
//}
//
//
//

// CSS

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
import com.cmart.util.Bid;
import com.cmart.util.Item;
import com.cmart.util.Purchase;
import com.cmart.util.StopWatch;
import com.cmart.util.User;

/**
 * This servlet is the user account
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
@WebServlet(name="MyAccountServlet", urlPatterns="/myaccount") public class MyAccountServlet extends HttpServlet {

	private static final long serialVersionUID = 6475041006628458743L;
	private static final String EMPTY = "";
	private static final String title = "My Account";
	private static final GlobalVars GV = GlobalVars.getInstance();

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
		this.makePage(request, response, Boolean.TRUE);
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
		if(request !=null && response != null && !FaultController.fault(response)){
			response.setHeader("Access-Control-Allow-Origin", "*");

			// Do the timer if we are collecting stats
			StopWatch timer = null;
			if(GV.COLLECT_STATS){
				timer = new StopWatch();
				timer.start();
			}

			// Create a new page controller for this page, it will get and process the data
			MyAccountController vars = new MyAccountController();
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

			// Do HTML4 or 5 depending on the global variables
			if(!vars.useHTML5()){
				// Set the return type
				response.setContentType("text/html");

				// Write the page header
				Header.writeHeaderNew(out, title, "myaccount", vars.getUserIDString(), vars.getAuthTokenString());

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
					GV.addStats(request, out, vars, 11);
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
					GV.addStatsJSON(request, out, vars, 0);
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
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void createHTML4(PrintWriter out, MyAccountController vars, Boolean isGet){
		if(out != null && vars != null)
			try{
				// Get the data needed to display the page
				vars.getHTML4Data();
				int loopCount = 0;
				//System.out.println("(myaccountservlet): doing the HTML4 processing");
				do{
					vars.processHTML4();
					loopCount++;
				}while(loopCount <= vars.getProcessingLoop());

				out.write("<div class=\"container\">");
				out.write("<div class=\"row\">");
				out.write("<div class=\"twelve columns\">");

				out.println("<div class=\"topblock\">");
				// Print links available from user account screen
				out.println(vars.getUpdateUserDetailsURL());
				out.println("<BR />");
				out.println(vars.getUploadVideoURL());
				out.println("<BR />");
				// Print the welcome message
				if(vars.getWelcomeMessage() != null){
					out.println(vars.getWelcomeMessage());
					out.println("<BR />");
				}
				out.println("</div>");

				/*
				 *  Print the items the user is currently bidding on
				 */
				out.println("<table><thead>");
				out.println("<div class=\"bidtable\">");
				out.println("<div class=\"title\">");
				out.println("<tr><th colspan=\"5\">Currently Bidding Items</th></tr>");
				out.println("<tr><th></th><th>Item Name</th><th>Current Bid</th><th>Max Bid</th><th>End Date</th></tr></thead>");
				out.println("</div><tbody>");
				if(vars.getCurrentBids() != null && vars.getCurrentBiddingItemURLs()!=null){
					ArrayList<Bid> currentBids = vars.getCurrentBids();
					String[] items = vars.getCurrentBiddingItemURLs();
					String[] images = vars.getCurrentBiddingThumbnails();
					int length = items.length;

					for(int i=0; i< length; i++){
						Item tempItem = currentBids.get(i).getItem();
						out.printf("<tr><div class=\"row%s\" id=\"entry\">\n", i%2==0? 1 :2 );
						long itemID = -1;
						if(tempItem != null) itemID = tempItem.getID();

						out.print("<label for=\"currentBids");out.print(tempItem==null ? "missing" :itemID); out.print("\">");

						out.println("<td><div class=\"img\">");
						out.println("<img height=\"80\" width=\"80\" src=\"" + GV.REMOTE_IMAGE_IP + GV.REMOTE_IMAGE_DIR + images[i] + "\" alt=\"\" />");
						out.println("</div></td>");

						out.println("<td><div class=\"description\">");	
						out.println(items[i]);
						out.println("</div></td>");

						out.println("<td><div class=\"bid\">");
						out.println(GV.makeLabel("bid", itemID));
						out.println(GV.currency.format(currentBids.get(i).getBid()));
						out.println("</label>");
						out.println("</div></td>");

						out.println("<td><div class=\"maxbid\">");
						out.println(GV.makeLabel("maxBid", itemID));
						out.println(tempItem==null ? "missing" : GV.currency.format(currentBids.get(i).getMaxBid()));
						out.println("</label>");
						out.println("</div></td>");

						out.println("<td><div class=\"enddate\">");
						out.println(GV.makeLabel("endDate", itemID));
						out.println(tempItem==null ? "missing" : tempItem.getEndDate());
						out.println("</label>");
						out.println("</div></td>");

						out.println("</label>");
						out.println("</div></tr>");
					}
				}
				else{
					out.printf("<tr><td colspan=\"5\"><div class=\"row1\" id=\"entry\">\n");
					out.println("<div class=\"description\">");
					out.println("No Current Bids");
					out.println("</div>");
					out.println("</div></td></tr>");
				}
				out.println("</div>");
				out.println("</tbody></table>");


				/*
				 *  Print the items the user has previously bid on
				 */
				out.println("<table><thead>");
				out.println("<div class=\"bidtable\">");
				out.println("<div class=\"title\">");
				out.println("<tr><th colspan=\"4\">Previous Bids</th></tr>");
				out.println("<tr><th>Item Name</th><th>Current Bid</th><th>Max Bid</th><th>End Date</th></tr></thead>");
				out.println("</div><tbody>");

				if(vars.getOldBids() != null && vars.getOldBiddingItemURLs()!= null){
					ArrayList<Bid> oldBids = vars.getOldBids();
					String[] items = vars.getOldBiddingItemURLs();
					int length = items.length;

					for(int i=0; i< length; i++){
						Item tempItem = oldBids.get(i).getItem();
						out.printf("<tr><div class=\"row%s\" id=\"entry\">\n", i%2==0? 1 :2 );
						long itemID = -1;
						if(tempItem != null) itemID = tempItem.getID();

						out.print("<label for=\"previousBids");out.print(tempItem==null ? "missing" : itemID); out.print("\">");

						out.println("<td><div class=\"description\">");		
						out.println(items[i]);
						out.println("</div></td>");

						out.println("<td><div class=\"bid\">");
						out.println(GV.makeLabel("bid", itemID));
						out.println(oldBids.get(i).getBid());
						out.println("</label>");
						out.println("</div></td>");

						out.println("<td><div class=\"maxbid\">");
						out.println(GV.makeLabel("maxBid", itemID));
						out.println(tempItem==null ? "missing" : GV.currency.format(tempItem.getMaxBid()));
						out.println("</label>");
						out.println("</div></td>");

						out.println("<td><div class=\"enddate\">");
						out.println(GV.makeLabel("endDate", itemID));
						out.println(tempItem==null ? "missing" : tempItem.getEndDate());
						out.println("</label>");
						out.println("</div></td>");

						out.println("</label>");
						out.println("</div></tr>");
					}
				}
				else{
					out.printf("<tr><td colspan=\"4\"><div class=\"row1\" id=\"entry\">\n");
					out.println("<div class=\"description\">");
					out.println("No Old Bids");
					out.println("</div>");
					out.println("</div></td></tr>");
				}
				out.println("</div>");
				out.println("</tbody></table>");

				/*
				 * Print the items that the user has purchased
				 */
				out.println("<table><thead>");
				out.println("<div class=\"purchasestable\">");
				out.println("<div class=\"title\">");
				out.println("<tr><th colspan=\"4\">Purchases</th></tr>");
				out.println("<tr><th>Item Name</th><th>Quantity</th><th>Price</th><th>Paid Status</th></tr></thead>");
				out.println("</div><tbody>");

				if(vars.getPurchases() != null && vars.getPayURLs() != null){
					ArrayList<Purchase> purchases = vars.getPurchases();
					String[] payURLs = vars.getPayURLs();
					String[] paidText=vars.getPaidText();
					int length = payURLs.length;

					for(int i=0; i< length; i++){
						Purchase tempPurchase = purchases.get(i);
						Item tempItem = purchases.get(i).getItem();

						out.printf("<tr><div class=\"row%s\" id=\"entry\">\n", i%2==0? 1 :2 );
						long itemID = -1;
						if(tempItem != null) itemID = tempItem.getID();

						out.print("<label for=\"purchases");out.print(tempItem==null ? "missing" : itemID); out.print("\">");

						out.println("<td><div class=\"description\">");		
						out.println(payURLs[i]);
						out.println("</div></td>");

						out.println("<td><div class=\"quantity\">");
						out.println(GV.makeLabel("quantity", itemID));
						out.println(tempPurchase.getQuantity());
						out.println("</label>");
						out.println("</div></td>");

						out.println("<td><div class=\"price\">");
						out.println(GV.makeLabel("price", itemID));
						out.println(GV.currency.format(tempPurchase.getPrice()));
						out.println("</label>");
						out.println("</div></td>");

						out.println("<td><div class=\"paid\">");
						out.println(GV.makeLabel("paid", itemID));
						out.println(paidText[i]);
						out.println("</label>");
						out.println("</div></td>");

						out.println("</label>");
						out.println("</div></tr>");
					}
				}
				else{
					out.printf("<tr><td colspan=\"4\"><div class=\"row1\" id=\"entry\">\n");
					out.println("<div class=\"description\">");
					out.println("No Purchases");
					out.println("</div>");
					out.println("</div></td></tr>");
				}
				out.println("</div>");
				out.println("</tbody></table>");



				/*
				 *  Print out the items that the user is currently selling
				 */
				out.println("<table><thead>");
				out.println("<div class=\"itemtable\">");
				out.println("<div class=\"title\">");
				out.println("<tr><th colspan=\"3\">Items Currently Selling</th></tr>");
				out.println("<tr><th>Item Name</th><th>Max Bid</th><th>End Date</th></tr></thead>");
				out.println("</div><tbody>");

				if(vars.getCurrentSellingItems() != null && vars.getCurrentSellingItemURLs()!= null){
					ArrayList<Item> currentItems = vars.getCurrentSellingItems();
					String[] items = vars.getCurrentSellingItemURLs();
					int length = items.length;

					for(int i=0; i< length; i++){
						out.printf("<tr><div class=\"row%s\" id=\"entry\">\n", i%2==0? 1 :2 );
						out.print("<label for=\"currentlySelling");out.print(currentItems.get(i).getID());out.print("\">");

						out.println("<td><div class=\"description\">");
						out.println(items[i]);
						out.println("</div></td>");

						out.println("<td><div class=\"maxbid\">");
						out.println(GV.makeLabel("maxBid", currentItems.get(i).getID()));
						out.println(GV.currency.format(currentItems.get(i).getMaxBid()));
						out.println("</label>");
						out.println("</div></td>");

						out.println("<td><div class=\"enddate\">");
						out.println(GV.makeLabel("endDate", currentItems.get(i).getID()));
						out.println(currentItems.get(i).getEndDate());
						out.println("</label>");
						out.println("</div></td>");

						out.println("</label>");
						out.println("</div></tr>");
					}
				}
				else{
					out.printf("<tr><td colspan=\"3\"><div class=\"row1\" id=\"entry\">\n");
					out.println("<div class=\"description\">");
					out.println("Not Currently Selling");
					out.println("</div>");
					out.println("</div></td></tr>");
				}
				out.println("</div>");
				out.println("</tbody></table>");


				/*
				 *  Print out the items that the user is previously sold
				 */
				out.println("<table><thead>");
				out.println("<div class=\"itemtable\">");
				out.println("<div class=\"title\">");
				out.println("<tr><th colspan=\"3\">Items Previously Sold</th></tr>");
				out.println("<tr><th>Item Name</th><th>Max Bid</th><th>End Date</th></tr></thead>");
				out.println("</div><tbody>");
				if(vars.getOldSellingItems() != null && vars.getOldSellingItemURLs()!= null){
					ArrayList<Item> oldItems = vars.getOldSellingItems();
					String[] items = vars.getOldSellingItemURLs();
					int length = items.length;

					for(int i=0; i< length; i++){
						out.printf("<tr><div class=\"row%s\" id=\"entry\">\n", i%2==0? 1 :2 );
						out.print("<label for=\"previouslySold");out.print(oldItems.get(i).getID()); out.print("\">");

						out.println("<td><div class=\"description\">");
						out.println(items[i]);
						out.println("</div></td>");

						out.println("<td><div class=\"maxbid\">");
						out.println(GV.makeLabel("maxBid", oldItems.get(i).getID()));
						out.println(GV.currency.format(oldItems.get(i).getMaxBid()));
						out.println("</label>");
						out.println("</div></td>");

						out.println("<td><div class=\"enddate\">");
						out.println(GV.makeLabel("endDate", oldItems.get(i).getID()));
						out.println(oldItems.get(i).getEndDate());
						out.println("</label>");
						out.println("</div></td>");

						out.println("</label>");
						out.println("</div></tr>");
					}
				}
				else{
					out.printf("<tr><td colspan=\"3\"><div class=\"row1\" id=\"entry\">\n");
					out.println("<div class=\"description\">");
					out.println("No Previously Sold Items");
					out.println("</div>");
					out.println("</div></td></tr>");
				}
				out.println("</div>");
				out.println("</tbody></table>");

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
	public void createHTML5(PrintWriter out, MyAccountController vars, Boolean isGet){
		if(out != null && vars != null)
			try{
				// Get the data needed to display the page
				vars.getHTML5Data();
				vars.processHTML5();


				ArrayList<Bid> bids = vars.getCurrentBids();
				ArrayList<Item> items = vars.getCurrentSellingItems();
				ArrayList<User> sellers = vars.getSellers();
				String[] questionJSON=vars.getQuestionJSON();
				String[] commentJSON=vars.getCommentJSON();

				out.append("\"pageType\":\"myaccount\",");
				out.append("\"newbids\":[");
				if(vars.getCurrentBids() != null){
					Iterator<Bid> bidIterators = bids.iterator();
					if(bidIterators.hasNext()){
						out.append(bidIterators.next().toJSON());}
					while(bidIterators.hasNext()){
						out.append(",").append(bidIterators.next().toJSON());
					}
				}
				out.append("],");
				out.append("\"newitems\":[");
				if(vars.getCurrentSellingItems() != null){
					Iterator<Item> itemIterators = items.iterator();
					if(itemIterators.hasNext()){
						out.append(itemIterators.next().toJSON());}
					while(itemIterators.hasNext()){
						out.append(",").append(itemIterators.next().toJSON());
					}
				}
				out.append("],");	
				bids = vars.getOldBids();
				items = vars.getOldSellingItems();
				ArrayList<Purchase>	purchases=vars.getPurchases();


				out.append("\"oldbids\":[");
				if(vars.getOldBids()!=null){
					Iterator<Bid> bidIterators = bids.iterator();
					if(bidIterators.hasNext()){
						out.append(bidIterators.next().toJSON());}
					while(bidIterators.hasNext()){
						out.append(","+bidIterators.next().toJSON());
					}}
				out.append("],");
				out.append("\"olditems\":[");
				if(vars.getOldSellingItems()!=null){
					Iterator<Item> itemIterators = items.iterator();
					if(itemIterators.hasNext()){
						out.append(itemIterators.next().toJSON());}
					while(itemIterators.hasNext()){
						out.append(","+itemIterators.next().toJSON());
					}}
				out.append("],");
				out.append("\"purchases\":[");
				if(vars.getPurchases()!=null){
					Iterator<Purchase> purchaseIterators = purchases.iterator();
					if(purchaseIterators.hasNext()){
						out.append(purchaseIterators.next().toJSON());}
					while(purchaseIterators.hasNext()){
						out.append(","+purchaseIterators.next().toJSON());
					}}
				out.append("],");

				out.append("\"sellers\":[");
				if(sellers!=null){
					Iterator<User> userIterators = sellers.iterator();
					if(userIterators.hasNext()){
						out.append(userIterators.next().toJSON());}
					while(userIterators.hasNext()){
						out.append(","+userIterators.next().toJSON());
					}}
				out.append("],");
				
				out.append("\"questions\":[");
				if(questionJSON.length>0){
					out.append(questionJSON[0]);}
				for(int i=1; i<questionJSON.length; i++){
					out.append(",").append(questionJSON[i]);}
				out.append("],");
				
				out.append("\"comments\":[");
				if(commentJSON.length>0){
					out.append(commentJSON[0]);}
				for(int i=1; i<commentJSON.length; i++){
					out.append(",").append(commentJSON[i]);}
				out.append("]");


			}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}



