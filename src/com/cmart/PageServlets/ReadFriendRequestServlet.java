package com.cmart.PageServlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cmart.Data.Footer;
import com.cmart.Data.GlobalVars;
import com.cmart.Data.Header;
import com.cmart.PageControllers.ReadFriendRequestController;
import com.cmart.util.FriendRequest;
import com.cmart.util.StopWatch;

/**
 * This page allows the user to read FriendRequest
 * 
 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
 * @version 0.1
 * @since 0.1
 * @date 03/01/2012
 */

@WebServlet(name = "ReadFriendRequestServlet", urlPatterns = "/readfriendrequest")
public class ReadFriendRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 6472741003023438743L;
	private static final String title = "Read Friend Request";
	private static final GlobalVars GV = GlobalVars.getInstance();

	/**
	 * Get the page, calls the page to be made We used to check the parameters
	 * in here, but I moved it to a controller object to keep the logic away
	 * from the layout
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.makePage(request, response, Boolean.FALSE);
	}

	/**
	 * Get the page, we can just pass this to doPost since the client generator
	 * will be posting userIDs and authTokens all the time
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		makePage(request, response, Boolean.TRUE);
	}

	/**
	 * This method starts the page timer, writes the header, creates the HTML
	 * for the page, writes the stats, and the footer
	 * 
	 * @param request
	 * @param response
	 * @param errorString
	 * @throws ServletException
	 * @throws IOException
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public void makePage(HttpServletRequest request,
			HttpServletResponse response, Boolean isGet)
			throws ServletException, IOException {
		if (request != null && response != null) {
			response.setHeader("Access-Control-Allow-Origin", "*");

			// Do the timer if we are collecting stats
			StopWatch timer = null;
			if (GV.COLLECT_STATS) {
				timer = new StopWatch();
				timer.start();
			}

			// Create a new page controller for this page, it will get and
			// process the data
			ReadFriendRequestController vars = new ReadFriendRequestController();
			int loopCount = 0;
			do {
				vars.checkInputs(request);
				loopCount++;
			} while (loopCount <= vars.getProcessingLoop());
			long acceptID = vars.getAcceptID();
			if (acceptID > (long) -1)
				vars.confirmFriendRequest(acceptID);
			long rejectID = vars.getRejectID();
			if (rejectID > (long) -1)
				vars.rejectFriendRequest(rejectID);

			// We are using PrintWriter to be friendly to the international
			// community. PrintWrite auto-converts coding
			PrintWriter out = response.getWriter();

			// If the output is to be suppressed then we'll redirect the output
			if (vars.getSuppressOutput())
				out = new PrintWriter(GV.BLACK_HOLE);

			// Do HTML4 or 5 depending on the page
			if (!vars.useHTML5()) {
				// Set the return type
				response.setContentType("text/html");

				// Write the page header
				Header.writeHeaderNew(out, title, "Read Friend Request",
						vars.getUserIDString(), vars.getAuthTokenString());

				// Create the HTML4
				createHTML4(out, vars, isGet);

				// Redirect the output to start writing the the user again in
				// case we were putting it in the black hole
				out = response.getWriter();

				/*
				 * Output any errors. These don't need to be too pretty since we
				 * hope there isn't any! We put them here to help with debug and
				 * info, the page above should show pretty ones for users
				 */
				if (GV.PRINT_ALL_ERRORS) {
					GV.addErrors(out, vars.getErrors());
				}

				/*
				 * Process the page time
				 */
				if (GV.COLLECT_STATS) {
					if (timer != null)
						timer.stop();
					vars.setTotalTime(timer.getTimeTaken());
					GV.addStats(request, out, vars, 0);
				}

				// Write the page footer
				Footer.writeFooter(out);
			} else {
				response.setContentType("application/json");
				createHTML5(out, vars, isGet);
			}
		}
	}

	/**
	 * Creates the HTML4 version of the website
	 * 
	 * @param request
	 *            The incoming request
	 * @param response
	 *            The response sent to the user
	 * @param out
	 *            The out writer to write the HTML to
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public void createHTML4(PrintWriter out, ReadFriendRequestController vars,
			Boolean isGet) {
		if (out != null && vars != null)
			try {
				// Get the data needed to display the page
				vars.getHTML4Data();
				int loopCount = 0;
				do {
					vars.processHTML4();
					loopCount++;
				} while (loopCount <= vars.getProcessingLoop());
				out.write("<div class=\"container\">");
				out.write("<div class=\"row\">");
				out.write("<div class=\"twelve columns\">");

				/*
				 * Print out all of the FriendRequests for the user
				 */
				out.write("<div class=\"nine columns\">");
				ArrayList<FriendRequest> friendRequests = vars.getFriendRequests();
				out.println("<table>");
				out.println("<tbody>");
				int size = friendRequests.size();
				if (friendRequests != null && size > 0) {
					for (int i = 0; i < size; i++) {
						FriendRequest friendRequest = friendRequests.get(i);
						out.println("<tr>");
						out.println("<td>");
						out.println("From: ");
						out.println("</td>");
						out.println("<td>");
						String fromName = vars.getFromName(friendRequest.getFromID());
						out.println(fromName);
						out.println("</td>");
						out.println("</tr>");
						out.println("<tr>");
						out.println("<td>");
						out.println("Friend Request: ");
						out.println("</td>");
						out.println("<td>");
						out.println(friendRequest.getMessage());
						out.println("</td>");
						out.println("</tr>");
						out.println("<tr>");
						out.println("<td>");
						out.println("<form name=\"confirm\" action=\"readfriendrequest?acceptID=" + friendRequest.getId() + "\" class=\"nice\" method=\"POST\">");
						out.println("<table>");
						out.println("<input type=\"hidden\" name=\"userID\" value=\"" + vars.getUserIDString() + "\">");
						out.println("<input type=\"hidden\" name=\"authToken\" value=\"" + vars.getAuthTokenString() + "\">");
						out.println("<input type=\"submit\" class=\"nice small radius white button\" name=\"confirm\" value=\"Confirm\">");
						out.println("</table>");
						out.println("</form>");
						out.println("</td>");
						out.println("<td>");
						out.println("<form name=\"reject\" action=\"readfriendrequest?rejectID=" + friendRequest.getId() + "\" class=\"nice\" method=\"POST\">");
						out.println("<table>");
						out.println("<input type=\"hidden\" name=\"userID\" value=\"" + vars.getUserIDString() + "\">");
						out.println("<input type=\"hidden\" name=\"authToken\" value=\"" + vars.getAuthTokenString() + "\">");
						out.println("<input type=\"submit\" class=\"nice small radius white button\" name=\"reject\" value=\"Reject\">");
						out.println("</table>");
						out.println("</form>");
						out.println("</td>");
						out.println("<tr>");
						out.println("<td>");
						out.println("</td>");
						out.println("<td>");
						out.println("</td>");
						out.println("</tr>");
					}
				} else {
					out.println("<tr id=\"entry\">");
					out.println("<td>");
					out.println("<BR /> Sorry, there is not friend request to read");
					out.println("</td>");
					out.println("</tr>");
				}
				out.println("</tbody>");
				out.println("</table>");
				out.println("</div>");
				out.println("</div>");
				out.println("</div>");
				out.println("</div>");
				out.println("</div>");
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	/**
	 * Creates the HTML5 version of the website
	 * 
	 * @param request
	 *            The incoming request
	 * @param response
	 *            The response sent to the user
	 * @param out
	 *            The out writer to write the HTML to
	 * @return The HTML5 response (although it could just return void since it
	 *         writes to the requests out writer)
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public void createHTML5(PrintWriter out, ReadFriendRequestController vars,
			Boolean isGet) {
		if (out != null && vars != null)
			try {
				// Get the data needed to display the page
				vars.getHTML5Data();
				vars.processHTML5();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}