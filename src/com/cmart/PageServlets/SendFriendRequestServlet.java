package com.cmart.PageServlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cmart.Data.Footer;
import com.cmart.Data.GlobalVars;
import com.cmart.Data.Header;
import com.cmart.PageControllers.SendFriendRequestController;
import com.cmart.util.StopWatch;

/**
 * This servlet allows a user to send a FriendRequest
 * 
 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
 * @version 0.1
 * @since 0.1
 * @date 03/01/2012
 * 
 */
@WebServlet(name = "SendFriendRequestServlet", urlPatterns = "/sendfriendrequest")
public class SendFriendRequestServlet extends HttpServlet {

	private static final long serialVersionUID = 8975041456628458743L;
	private static final String title = "Send Friend Request";
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
		makePage(request, response, false);
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
		makePage(request, response, true);
	}

	/**
	 * This method starts the page timer, writes the header, creates the HTML
	 * for the page, writes the stats, and the footer
	 * 
	 * @param request
	 *            The incoming user request
	 * @param response
	 *            The out going user response
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
			SendFriendRequestController vars = new SendFriendRequestController();
			int loopCount = 0;
			do {
				vars.checkInputs(request);
				loopCount++;
			} while (loopCount <= vars.getProcessingLoop());
			boolean flag = vars.insertFriendRequest();
			boolean redirect = false;
			if (vars.getErrors().size() == 0) {
				if (flag) {
					if (!vars.useHTML5()) {
						timer.stop();
						response.sendRedirect(vars.getRedirectURL());
						redirect = true;
					}
				}
			}

			// We are using PrintWriter to be friendly to the international
			// community. PrintWrite auto-converts coding
			PrintWriter out = response.getWriter();

			// If the output is to be suppressed then we'll redirect the output
			if (vars.getSuppressOutput())
				out = new PrintWriter(GV.BLACK_HOLE);

			// Do HTML4 or 5 depending on the global variables
			if (!vars.useHTML5() && !redirect) {
				// Set the return type
				response.setContentType("text/html");

				// Write the page header
				Header.writeHeaderNew(out, title, "Friend Request",
						vars.getUserIDString(), vars.getAuthTokenString());

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
					GV.addStats(request, out, vars, 1);
				}

				// Write the page footer
				Footer.writeFooter(out);
			} else if (vars.useHTML5()) {
				response.setContentType("application/json");
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
					GV.addStatsJSON(request, out, vars, 1);
				}
				out.print("}");
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
	public void createHTML4(PrintWriter out, SendFriendRequestController vars,
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
				out.write("<div class=\"six columns\">");
				out.println("<form name=\"sendfriendrequest\" action=\"sendfriendrequest\" class=\"nice\" method=\"POST\">");
				out.println("<table>");
				out.println("<div id=\"center\">");
				out.println("<input type=\"hidden\" name=\"userID\" value=\""
						+ vars.getUserIDString() + "\">");
				out.println("<input type=\"hidden\" name=\"authToken\" value=\""
						+ vars.getAuthTokenString() + "\">");
				out.println("<tr><td><div id=\"floatright\">To:</div></td>");
				out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"user\" size=\"50\" \"/> </div></td></tr>");
				out.println("<tr><td><div id=\"floatright\">FriendRequest: </div></td>");
				out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"friendrequest\" size=\"50\" value=\" \"/> </div></td></tr>");
				out.println("<tr><td colspan=\"2\"><div id=\"floatright\"><input type=\"submit\" class=\"nice small radius white button\" value=\"send\"></div></td></tr>");
				out.println("</table></form>");
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
	 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
	 */
	public void createHTML5(PrintWriter out, SendFriendRequestController vars,
			Boolean isGet) {

	}
}