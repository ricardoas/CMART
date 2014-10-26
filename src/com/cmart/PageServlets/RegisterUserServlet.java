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
import com.cmart.PageControllers.FaultController;
import com.cmart.PageControllers.RegisterUserController;
import com.cmart.util.StopWatch;

/**
 * This servlet registers a new user in the database
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
@WebServlet(name="RegisterUserServlet", urlPatterns="/registeruser")
public class RegisterUserServlet extends HttpServlet {
	// As servlets are not thread safe, we only have static variables here
	private static final long serialVersionUID = 6423041006628438743L;
	private static final String EMPTY = "";
	private static final String title = "Register User";
	private static boolean registeruser = false;
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
	 * Sets that this is the first time the page has been loaded by the user
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
	 * If the user has entered all of the correct information, then we'll insert them in to the database
	 * 
	 * @param request
	 * @param response
	 * @param errorString
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
			RegisterUserController vars = new RegisterUserController();
			int loopCount = 0;
			do{
				vars.checkInputs(request);
				loopCount++;
			}while(loopCount <= vars.getProcessingLoop());
			
			
			// If all of the user's details are correct, write the user to the database and continue to the login page with the correct info inserted
			boolean redirect = false;
			if(vars.getErrors().size() == 0){
				if(registeruser=vars.insertUser()){
					if(!vars.useHTML5()){
						if(timer != null) timer.stop();
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
			if(!vars.useHTML5()  && !redirect){
				// Set the return type
				response.setContentType("text/html");
				
				// Write the page header
				Header.writeHeaderNew(out, title, "register",vars.getUserIDString(), vars.getAuthTokenString());
				
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
					GV.addStats(request, out, vars, 12);
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
					GV.addStatsJSON(request, out, vars, 12);
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
	public void createHTML4(PrintWriter out, RegisterUserController vars, Boolean isGet){
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
			
			//TODO: pretty print errors depending on the what the user submitted
			
			/*
			 * Make the input form
			 */
			out.write("<div class=\"seven columns\">");
			out.println("<form name=\"register_user\" action=\"registeruser\" class=\"nice\" method=\"POST\">");
			out.println("<table>");
			out.println("<div id=\"center\">");	
			
			out.println("<tr><td><div id=\"floatright\">Username: </div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"username\" size=\"30\" value=\""+ vars.getUsername() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Password: </div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"password\" name=\"password1\" size=\"30\" value=\""+ vars.getPassword1() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Confirm Password: </div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"password\" name=\"password2\" size=\"30\" value=\""+ vars.getPassword2() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">E-mail address: </div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"email1\" size=\"30\" value=\""+ vars.getEmail1() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Confirm E-mail address: </div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"email2\" size=\"30\" value=\""+ vars.getEmail2() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">First name: </div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"firstname\" size=\"30\" value=\""+ vars.getFirstname() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Last name: </div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"lastname\" size=\"30\" value=\""+ vars.getLastname() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Street: </div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"street\" size=\"50\" value=\""+ vars.getStreet() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Town: </div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"town\" size=\"50\" value=\""+ vars.getTown() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">Zip code: </div></td>");
			out.println("<td><div id=\"floatcenter\"> <input type=\"text\" name=\"zip\" size=\"10\" maxlength=\"10\" value=\""+ vars.getZip() + "\"/> </div></td></tr>");
			
			out.println("<tr><td><div id=\"floatright\">State: </div></td>");
			out.println("<td><div id=\"floatcenter\">");
			out.println("<select name=\"state\">");
			
			String[] stateSelections = vars.getStateSelections();
			if(stateSelections != null)
				for(String selection: stateSelections)
					out.println(selection);
			
			out.println("</select>");
			out.println("</div></td></tr>");
			
			out.println("<tr><td colspan=\"2\"><div id=\"floatright\"><input type=\"submit\" class=\"nice small radius white button\" value=\"Register\"></div></td></tr>");
			
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
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void createHTML5(PrintWriter out, RegisterUserController vars, Boolean isGet){
		if(out != null && vars != null)
		try{
			// Get the data needed to display the page
			vars.getHTML4Data();
			vars.processHTML4();
			
			/*
			 * If the user successfull registered return their userID, otherwise send back the errors
			 */
			out.append("\"pageType\":\"register\",");
			if(Long.parseLong(vars.getUserIDString())>0){
				out.append("\"registeruser\":true");
				out.append(",\"userID\":"+vars.getUserIDString());
				out.append(",\"authToken\":\""+vars.getAuthTokenString()+"\"");
			}
			else{
				out.append("\"registeruser\":false");
				out.append(",\"userID\":-1");
			}
			
			/*
			if(Integer.parseInt(vars.getUserIDString())>0){
				out.println("{\"registeruser\":true,\"userID\":"+vars.getUserIDString()+",\"authToken\":\""+vars.getAuthTokenString()+"\","+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");
			//	System.out.println("Created User...!");
			}else{
				out.println("{\"registeruser\":false,\"userID\":-1,"+GV.addStatsHTML5(vars)+","+GV.addErrorsHTML5(vars.getErrors())+"}");;
			//	System.out.println("Could not created User...");
			}	*/
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
