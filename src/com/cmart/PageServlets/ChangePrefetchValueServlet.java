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
import com.cmart.util.User;
import com.cmart.util.Address;

/**
 * This servlet changes the prefetch image value
 * 
 * @author Andrew Fox (asfox@cmu.edu)
 * @since 0.1
 * @version 1.0
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
@WebServlet(name="ChangePrefetchValueServlet", urlPatterns="/changeprefetchvalue")
public class ChangePrefetchValueServlet extends HttpServlet {

	private static final long serialVersionUID = 8175041006628458743L;
	private static final String EMPTY = "";
	private static final String title = "Change Prefetch Value";
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
			response.setHeader("Access-Control-Allow-Origin", "*");

			// Do the timer if we are collecting stats
			StopWatch timer = null;
			if(GV.COLLECT_STATS){
				timer = new StopWatch();
				timer.start();
			}

			// Create a new page controller for this page, it will get and process the data
			// Does not loop as we probably only access this to help run the experiment
			ChangePrefetchValueController vars = new ChangePrefetchValueController();
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
				//Header.writeHeader(out, title, vars.getUserIDString(), vars.getAuthTokenString());

				createHTML4(out, vars, isGet);

				// Redirect the output to start writing the the user again in case we were putting it in the black hole
				out = response.getWriter();


				// Write the page footer
				//Footer.writeFooter(out);
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
	public void createHTML4(PrintWriter out, ChangePrefetchValueController vars, Boolean isGet){
		if(out != null && vars != null)
			try{
				// Get the data needed to display the page
				vars.getHTML4Data();
				vars.processHTML4();
				
				out.println("PREFETCH_IMAGES: "+GlobalVars.PREFETCH_IMAGES);
				out.println("PREFETCH_IMAGES_NUM: "+GlobalVars.PREFETCH_IMAGES_NUM);

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
	public void createHTML5(PrintWriter out, ChangePrefetchValueController vars, Boolean isGet){
		if(out != null && vars != null)
			try{
				// Get the data needed to display the page
				vars.getHTML5Data();
				vars.processHTML5();

				out.println("PREFETCH_IMAGES: "+GlobalVars.PREFETCH_IMAGES);
				out.println("PREFETCH_IMAGES_NUM: "+GlobalVars.PREFETCH_IMAGES_NUM);

			}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
