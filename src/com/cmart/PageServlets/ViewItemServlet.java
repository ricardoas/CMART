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
//import com.cmart.util.Item;
//import com.cmart.util.StopWatch;
//
///**
// * This servlet is a blank example
// * 
// * @author Andy (andrewtu@cmu.edu)
// * @version 0.1
// * @since 0.1
// * @date 04/05/2011
// * 
// */
//@WebServlet(name="ViewItemServlet", urlPatterns="/viewitem")
//public class ViewItemServlet extends HttpServlet {
//
//	private static final long serialVersionUID = 6475041456628458743L;
//	private static final String EMPTY = "";
//	private static final String title = "View Item";
//	//private static  boolean bid = false;
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
//			ViewItemController vars = new ViewItemController();
//			int loopCount = 0;
//			do{
//				vars.checkInputs(request);
//				loopCount++;
//			}while(loopCount <= vars.getProcessingLoop());
//
//			// If there are no errors we can insert the bid
//			boolean redirect = false;
//			if(!isGet && vars.getErrors().size() == 0){
//				if(vars.submitBid()){
//					if(!vars.useHTML5()){
//						if(timer != null) timer.stop();
//						response.sendRedirect(vars.getRedirectURL());
//						redirect = true;
//					}else{
//						
//					}
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
//					GV.addStats(request, out, vars, 18);
//				}
//
//				// Write the page footer
//				Footer.writeFooter(out);
//			}
//			else  if(vars.useHTML5()){
//				createHTML5(out, vars, isGet);
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
//	public void createHTML4(PrintWriter out, ViewItemController vars, Boolean isGet){
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
//				// Make sure there is really an item to display
//				Item item = vars.getItem();
//				if(item != null){
//
//					/*
//					 * Print out the items basic information
//					 */
//					// Print out the thumbnail
//					out.println(vars.getThumbnailURL());
//
//					// Print out the items current price details
//					out.println(GV.makeLabel("name", -1));
//					out.println(item.getName() + "<br />");
//					out.println("</label>");
//
//					out.println(GV.makeLabel("condition", -1));
//					out.println("Item condition: <br />");
//					out.println("</label>");
//
//					out.println(GV.makeLabel("seller", -1));
//					out.println("Seller: " + vars.getSellerURL() + "<br />");
//					out.println("</label>");
//					
//					out.println(GV.makeLabel("sellerRating", -1));
//					out.println("Seller rating: " + vars.getRating() + "<br />");
//					out.println("</label>");
//
//					out.println(GV.makeLabel("endDate", -1));
//					out.println("Bidding Ends: " + item.getEndDate() + "<br />");
//					out.println("</label>");
//
//					out.println(GV.makeLabel("noOfBids", -1));
//					out.println("Bidding History: " + vars.getBiddingHistoryURL() + "<br />");
//					out.println("</label>");
//
//					out.println(GV.makeLabel("currentBid", -1));
//					out.println("Current Price: " + GV.currency.format(item.getMaxCurrentBidStartPrice()) + "<br />");
//					out.println("</label>");
//					/*
//					 * Print the bidding and buy now forms
//					 */
//					// If the auction is on going we can allow the user to bid
//					if(!vars.getIsOld()){
//						out.println(vars.reserveMet());
//
//						// Print the form to allow bidding
//						out.println("<form name=\"bid\" action=\"viewitem\" method=\"POST\">");
//
//						out.println("<input type=\"hidden\" name=\"userID\" value=\"" + vars.getUserIDString() + "\">");
//						out.println("<input type=\"hidden\" name=\"authToken\" value=\"" + vars.getAuthTokenString() + "\">");
//						out.println("<input type=\"hidden\" name=\"itemID\" value=\"" + item.getID() + "\">");
//
//						out.println("Quantity: <input type=\"text\" name=\"quantity\" size=\"2\" maxlength=\"2\" value=\"" + vars.getQuantity()+ "\"> (" + item.getQuantity() + " available) <br />");
//						out.println("Your Bid: <input type=\"text\" name=\"bid\" size=\"10\" maxlength=\"10\" value=\"" + vars.getBid() + "\"> (Enter more than " + GV.currency.format(item.getMaxCurrentBidStartPrice()) + ") <br />");
//						out.println("Your Maximum Bid: <input type=\"text\" name=\"maxBid\" size=\"10\" maxlength=\"10\" value=\"" + vars.getMaxBid() + "\"> (Enter more than " + GV.currency.format(item.getMaxCurrentBidStartPrice()) + ") <br />");
//						out.println("<input type=\"submit\" value=\"Bid\">");
//						out.println("</form><br />");
//
//						// If the item has a buy now price we can buy it now
//						if(vars.getCanBuyNow()){
//							out.println("<form name=\"buyNow\" action=\"buyitem\" method=\"POST\">");
//
//							out.println("<input type=\"hidden\" name=\"userID\" value=\"" + vars.getUserIDString() + "\">");
//							out.println("<input type=\"hidden\" name=\"authToken\" value=\"" + vars.getAuthTokenString() + "\">");
//							out.println("<input type=\"hidden\" name=\"itemID\" value=\"" + item.getID() + "\">");
//							out.println("<input type=\"hidden\" name=\"quantity\" value=\"" + item.getQuantity() + "\">");
//
//							out.println(GV.currency.format(item.getBuyNowPrice()) + " <input type=\"submit\" value=\"Buy Now\">");
//							out.println("</form>");
//						}
//					}
//					// If this was an old item
//					else{
//						out.println("The bidding on this item has finished.<BR />");
//						out.println("Sale Price: " + item.getMaxBid() + "<BR />");
//					}
//
//					/*
//					 * Print the items name and larger description
//					 */
//					out.println(item.getName() + "<BR />");
//
//					out.println(GV.makeLabel("description", -1));
//					out.println(item.getDescription() + "<BR />");
//					out.println("</label>");
//
//					/*
//					 * Print out the items pictures 
//					 */
//					String[] imageURLs = vars.getImageURLs();
//					if(imageURLs != null && imageURLs.length>0){
//						out.println(GV.makeLabel("images", -1));
//						for(int i=0; i<imageURLs.length; i++){
//							out.println(GV.makeLabel("image", i));
//							out.println(imageURLs[i]);
//							out.println("</label>");
//							//TODO: do this with divs
//							if(i+1%5 == 0 ) out.println("<BR />");
//						}
//						out.println("</label>");
//					}
//				}
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
//	public void createHTML5(PrintWriter out, ViewItemController vars, Boolean isGet){
//		if(out != null && vars != null)
//			try{
//				// Get the data needed to display the page
//				vars.getHTML5Data();
//				vars.processHTML5();
//				if(vars.submitBid()){
//					out.println("{\"success\":true,"+"\"itemID\":"+vars.getItemID()+",\"item\":"+vars.getItem().toJSON()+","+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");
//				}else{
//					if(vars.getItem()!=null){
//						out.println("{\"success\":false,\"item\":"+vars.getItem().toJSON()+","+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");
//					}else{
//						out.println("{\"success\":false,\"item\":\"null\","+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");
//					}
//				}
//			}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//	}
//}


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
import com.cmart.util.Item;
import com.cmart.util.StopWatch;
import com.cmart.util.Question;
import com.cmart.util.Comment;

/**
 * This servlet is a blank example
 * 
 * @author Andy (andrewtu@cmu.edu)
 * @version 0.1
 * @since 0.1
 * @date 04/05/2011
 * 
 */
@WebServlet(name="ViewItemServlet", urlPatterns="/viewitem")
public class ViewItemServlet extends HttpServlet {

	private static final long serialVersionUID = 6475041456628458743L;
	private static final String EMPTY = "";
	private static final String title = "View Item";
	//private static  boolean bid = false;
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
		if(request !=null && response != null && !FaultController.fault(response)){
			response.setHeader("Access-Control-Allow-Origin", "*");

			// Do the timer if we are collecting stats
			StopWatch timer = null;
			if(GV.COLLECT_STATS){
				timer = new StopWatch();
				timer.start();
			}

			// Create a new page controller for this page, it will get and process the data
			ViewItemController vars = new ViewItemController();
			int loopCount = 0;
			do{
				vars.checkInputs(request);
				loopCount++;
			}while(loopCount <= vars.getProcessingLoop());

			//System.out.println("(viewitemservlet) checking if errors: " + vars.getErrors().size());
			//for(int i=0; i<vars.getErrors().size(); i++)
				//System.out.println("(viewitemservlet) checking if errors: " + vars.getErrors().get(i));
			
			// If there are no errors we can insert the bid
			boolean redirect = false;
			if(!isGet && vars.getErrors().size() == 0){
				//System.out.println("(viewitemservlet) doing submit bid");
				if(vars.submitBid()){
					if(!vars.useHTML5()){
						if(timer != null) timer.stop();
						response.sendRedirect(vars.getRedirectURL());
						redirect = true;
						//System.out.println("(viewitemservlet) Sending redirect");
					}else{

					}
				}
			}

			
			// We are using PrintWriter to be friendly to the international community. PrintWrite auto-converts coding
			PrintWriter out = response.getWriter();

			// If the output is to be suppressed then we'll redirect the output
			if(vars.getSuppressOutput())
				out = new PrintWriter(GV.BLACK_HOLE);

			// Do XML response depending on get itemPriceRequest
			if(vars.getItemCurrentBidRequest()){
				createXML(out, vars);
			// Do HTML4 or 5 depending on the global variables
				}else if(!vars.useHTML5()  && !redirect){
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
					GV.addStats(request, out, vars, 18);
				}

				// Write the page footer
				Footer.writeFooter(out);
			}
			else if(vars.useHTML5()){
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

					out.print(",");
					GV.addStatsJSON(request, out, vars, 18);
				}

				out.print("}");
			}
		}
	}

	/**
	 * This method starts the page timer, creates the responseText for the itemCurrentBid request by AJAX
	 * @param request The incoming user request
	 * @param response  The out going user response
	 * 
	 * TODO: Change it to XML response
	 * @author bo (bol1@andrew.cmu.edu)
	 * */
	public void createXML(PrintWriter out, ViewItemController vars){
		if(out != null && vars != null){
			try{
				// Get the data needed to display the page
				vars.getXMLData();
				vars.processXML();
				double itemCurrentBid = vars.getItemCurrentBid();
				if(vars.getIsOld() == false){
					if(itemCurrentBid >= 0){
						out.println(GV.currency.format(itemCurrentBid));
					}
					else{
						out.println("cannot get current bid now");
					}
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
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void createHTML4(PrintWriter out, ViewItemController vars, Boolean isGet){
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
				out.write("<div class=\"seven columns\">");

				// Make sure there is really an item to display
				Item item = vars.getItem();
				ArrayList<Question> questions = vars.getQuestions();
				ArrayList<Comment> comments = vars.getComments();
				
				if(item != null){
					out.println("<script src=\"js/itemPriceUpdate.js\" type=\"text/javascript\"></script>");
					out.println("<body onload=\"updateItemPrice()\"></body>");
					
					out.println("<script src=\"js/jquery-1.3.2.min.js\" type=\"text/javascript\" ></script> ");
					out.println("<script src=\"js/jquery.easing.1.3.js\" type=\"text/javascript\" ></script> ");
					out.println("<script src=\"js/jquery.galleryview-1.1.js\" type=\"text/javascript\" ></script> ");
					out.println("<script src=\"js/jquery.timers-1.1.2.js\" type=\"text/javascript\" ></script> ");
										
					/*
					 * Print out the items basic information
					 */
					// Print out the thumbnail
					out.println(vars.getThumbnailURL());

					// Print out the items current price details
					out.println("<table><thead><tr><th colspan=\"2\">");
					out.println(GV.makeLabel("name", -1));
					out.println(item.getName());
					out.println("</label></th></tr></thead><tbody>");

					out.println("<tr><td>");
					out.println(GV.makeLabel("condition", -1));
					out.println("Item condition</td>");
					out.println("<td>");
					out.println("</label></td></tr>");

					out.println("<tr><td>");
					out.println(GV.makeLabel("seller", -1));
					out.println("Seller</td><td>" + vars.getSellerURL());
					out.println("</label></td></tr>");

					out.println("<tr><td>");
					out.println(GV.makeLabel("sellerRating", -1));
					out.println("Seller rating</td><td>" + vars.getRating());
					out.println("</label></td></tr>");

					out.println("<tr><td>");
					out.println(GV.makeLabel("endDate", -1));
					out.println("Bidding Ends</td><td>" + item.getEndDate());
					out.println("</label></td></tr>");

					out.println("<tr><td>");
					out.println(GV.makeLabel("noOfBids", -1));
					out.println("Bidding History</td><td>" + vars.getBiddingHistoryURL());
					out.println("</label></td></tr>");

					out.println("<tr><td>");
					//out.println(GV.makeLabel("currentBid", -1));
					
					out.println("Current Price</td><td>");
					out.println("<span id=\"itemCurrentPrice1\">" + GV.currency.format(item.getMaxCurrentBidStartPrice()));
					out.println("</span></td></tr>");

					out.println("</tbody></table>");
					out.println("</div>");
					/*
					 * Print the bidding and buy now forms
					 */
					// If the auction is on going we can allow the user to bid
					if(!vars.getIsOld()){
						out.println("<div class=\"five columns\">");
						out.println(vars.reserveMet());

						// Print the form to allow bidding
						out.println("<form name=\"bid\" action=\"viewitem\" class=\"nice\" method=\"POST\">");

						out.println("<input type=\"hidden\" id=\"userID\" name=\"userID\" value=\"" + vars.getUserIDString() + "\">");
						out.println("<input type=\"hidden\" id=\"authToken\" name=\"authToken\" value=\"" + vars.getAuthTokenString() + "\">");
						out.println("<input type=\"hidden\" id=\"itemID\" name=\"itemID\" value=\"" + item.getID() + "\">");
						out.println("<input type=\"hidden\" id=\"itemIsOld\" name=\"itemIsOld\" name=\"itemIsOld\"  value=\"" + vars.getIsOld() + "\">");
						
						out.println("<table>");
						out.println("<tr><td>Quantity: </td><td><input type=\"text\" name=\"quantity\" size=\"2\" maxlength=\"2\" value=\"" + vars.getQuantity()+ "\"></td> <td>(" + item.getQuantity() + " available)</td></tr>");
						out.println("<tr><td>Your Bid: </td><td><input type=\"text\" name=\"bid\" size=\"10\" maxlength=\"10\" value=\"" + vars.getBid() + "\"></td> <td>(Enter more than <span id=\"itemCurrentPrice2\">" + GV.currency.format(item.getMaxCurrentBidStartPrice()) + "</span>)</td></tr>");
						out.println("<tr><td>Your Maximum Bid: </td><td><input type=\"text\" name=\"maxBid\" size=\"10\" maxlength=\"10\" value=\"" + vars.getMaxBid() + "\"></td> <td>(Enter more than <span id=\"itemCurrentPrice3\">" + GV.currency.format(item.getMaxCurrentBidStartPrice()) + "</span>)</td></tr>");
						out.println("<tr><td colspan=\"3\"><input type=\"submit\" class=\"nice small radius white button\" value=\"Bid\"></td></tr>");
						out.println("</table></form><br />");

						// If the item has a buy now price we can buy it now
						if(vars.getCanBuyNow()){
							out.println("<form name=\"buyNow\" action=\"buyitem\" class=\"nice\" method=\"POST\">");

							out.println("<input type=\"hidden\" name=\"userID\" value=\"" + vars.getUserIDString() + "\">");
							out.println("<input type=\"hidden\" name=\"authToken\" value=\"" + vars.getAuthTokenString() + "\">");
							out.println("<input type=\"hidden\" name=\"itemID\" value=\"" + item.getID() + "\">");
							out.println("<input type=\"hidden\" name=\"quantity\" value=\"" + item.getQuantity() + "\">");

							out.println(GV.currency.format(item.getBuyNowPrice()) + " <input type=\"submit\" class=\"nice small radius white button\" value=\"Buy Now\">");
							out.println("</form>");
						}
						out.println("</div>");
					}
					// If this was an old item
					else{
						out.println("<div class=\"five columns\">");
						out.println("The bidding on this item has finished.<BR />");
						out.println("Sale Price: " + GV.currency.format(item.getMaxBid()) + "<BR />");
						out.println("</div>");
					}

					/*
					 * Print the items name and larger description
					 */
					out.write("</div></div>");
					out.write("<div class=\"container\">");
					out.write("<div class=\"row\">");
					out.write("<div class=\"twelve columns\">");
					out.println(item.getName() + "<BR />");

					out.println(GV.makeLabel("description", -1));
					out.println(item.getDescription() + "<BR />");
					out.println("</label>");

					out.println("</div>");
					out.println("</div>");
					out.println("</div>");
					
					/*
					 * Add the questions
					 */
					out.write("<div class=\"container\">");
					out.write("<div class=\"row\">");
					out.write("<div class=\"twelve columns\">");
					// Added by Bo (bol1@andrew.cmu.edu)
					// Allow a user to leave a comment
					//TODO: only allow this if the user has purchased the item
					out.println("<a href=\"./commentitem?userID=" + vars.getUserIDString() + "&authToken=" + vars.getAuthTokenString() + "&itemID=" + item.getID() + 
							"\">Leave Comment</a> <BR />");
					
					// Display previous comments
					if(comments != null && comments.size() > 0){
						String[] usersCommentFrom = vars.getUsersCommentFrom();
						
						out.write("<div class=\"row\">");
						out.write("<div class=\"seven columns\">");
						
						out.println("<table><thead><tr><th colspan=\"1\">");
						out.println("Previous Comments:");
						out.println("</th></tr></thead><tbody>");
						
						
						for(int i = 0; i < comments.size(); i++){
							out.println("<tr><td>");
							
							Comment com = comments.get(i);
							out.println("Comment from user: " + usersCommentFrom[i] + ", Date: " + com.getDate() + 
										"<BR />Rating: " + com.getRating() + ", Comment: " + com.getComment() + "<BR />");

							out.println("</td></tr>");
						}
						
						out.println("</table>");
						out.write("</div></div>");
					}
					
					// Allow a user to ask a question
					out.println("<a href=\"./askquestion?userID=" + vars.getUserIDString() + "&authToken=" + vars.getAuthTokenString() + "&itemID=" + item.getID() + 
							"\">Ask Question</a> <BR />");
					
					// If viewer is the seller, allow he to answer question`	
					boolean displayAnswerLink = vars.getIsViewerTheSeller();
					// display previous questions and answers
					if((questions != null) && (questions.size() > 0)){
						String[] askusers = vars.getAskUsers();
						
						out.write("<div class=\"row\">");
						out.write("<div class=\"seven columns\">");
						
						out.println("<table><thead><tr><th colspan=\"1\">");
						out.println("Previous questions:");
						out.println("</th></tr></thead><tbody>");
						
						for(int i = 0; i < questions.size(); i++){
							Question q = questions.get(i);
							// if it is a question and the viewer is the seller, show answer link
							if(q.getIsQuestion()){
								out.println("<tr><td>");
								
								out.println("Question from user: " + askusers[i] + "; Date: " + q.getPostDate() + 
									"<BR />" + q.getContent() + "<BR />");
								if(displayAnswerLink)
									out.println("<a href=\"./answerquestion?userID=" + vars.getUserIDString() + "&authToken=" + vars.getAuthTokenString() +
										"&questionID=" + q.getID() + "&itemID=" + item.getID() + 
										"\">Answer Question</a> <BR />");
								
								out.println("</td></tr>");
							}
							else{
								// this is an answer
								out.println("<tr><td>");
								
								out.println("Answer from user: " + vars.getSellerURL() + " for User: " + askusers[i] +", Date: " + q.getPostDate() + 
										"<BR />" + q.getContent() + "<BR />");
								
								out.println("</td></tr>");
							}
						}
						
						out.println("</table>");
						out.write("</div></div>");
					}
					
					/*
					 * Print out the items pictures 
					 */
					/*String[] imageURLs = vars.getImageURLs();
					if(imageURLs != null && imageURLs.length>0){
						out.println(GV.makeLabel("images", -1));
						for(int i=0; i<imageURLs.length; i++){
							out.println(GV.makeLabel("image", i));
							out.println(imageURLs[i]);
							out.println("</label>");
							//TODO: do this with divs
							if(i+1%5 == 0 ) out.println("<BR />");
						}
						out.println("</label>");
					}*/
					String[] images = vars.getImageURLs();
					String[] imagesStrip = vars.getImageStripURLs();
					if(images != null && images.length > 0){
						out.println("<script type=\"text/javascript\"> ");
						out.println("$(document).ready(function(){");
						out.println("$('#photos').galleryView({");
						out.println("panel_width: 400, panel_height: 300, frame_width: 100, frame_height: 100");
						out.println("});");
						out.println("});");
						out.println("</script>");
						
						out.println("<div id=\"photos\" class=\"galleryview\">");
						for(int i = 0; i < images.length; i++ ){
							out.println("<div class=\"panel\">");
							out.println(images[i]);
							out.println("</div>");
						}
						// display the thumbnail image
						out.println("<ul class=\"filmstrip\">"); 
						for(int i = 0; i < images.length; i++ ){
							out.println("<li>");
							out.println(imagesStrip[i]);
							out.println("</li>");
						}
					    out.println("</ul>");
					    out.println("</div>");
					}
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
	public void createHTML5(PrintWriter out, ViewItemController vars, Boolean isGet){
		if(out != null && vars != null)
			try{
				// Get the data needed to display the page
				vars.getHTML5Data();
				vars.processHTML5();
				
				out.append("\"pageType\":\"viewitem\",");
				// If the user was bidding and was successful then we can return true
				if(vars.submitBid())
					out.append("\"success\":true");
				else
					out.append("\"success\":false");
				
				// Add the item details
				if(vars.getItem() != null)
					out.append(",\"itemID\":"+vars.getItemID()+",\"item\":"+vars.getItem().toJSON());
				else
					out.append(",\"item\":\"null\"");
				
				if(vars.getSeller() != null)
					out.append(",\"seller\":" + vars.getSeller().toJSON());
				else
					out.append(",\"seller\":\"null\"");
				
				if(vars.getQuestions().size()>0){
					out.append(",\"questions\":[");
					out.append(vars.getQuestions().get(0).toJSON());
					for(int i=1; i<vars.getQuestions().size(); i++){
						out.append(",").append(vars.getQuestions().get(i).toJSON());}
					out.append("]");
				}
				else
					out.append(",\"questions\":\"null\"");
				
				if(vars.getComments().size()>0){
					out.append(",\"comments\":[");
					out.append(vars.getComments().get(0).toJSON());
					for(int i=1; i<vars.getComments().size(); i++){
						out.append(",").append(vars.getComments().get(i).toJSON());}
					out.append("]");
				}
				else
					out.append(",\"comments\":\"null\"");
				
				/*if(vars.submitBid()){
					out.println("{\"success\":true,"+"\"itemID\":"+vars.getItemID()+",\"item\":"+vars.getItem().toJSON()+","+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");
				}else{
					if(vars.getItem()!=null){
						out.println("{\"success\":false,\"item\":"+vars.getItem().toJSON()+","+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");
					}else{
						out.println("{\"success\":false,\"item\":\"null\","+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");
					}
				}*/
			}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
