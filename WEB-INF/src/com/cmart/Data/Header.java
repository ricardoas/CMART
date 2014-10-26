package com.cmart.Data;

import java.io.PrintWriter;

/**
 * This class creates the page header for the user
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
public class Header {
	private static final String EMPTY = "";
	private static final String NEG = "-1";

	private Header() {}
	
	/**
	 * Writes the header to the passed output writer. The header will change depending on whether the user
	 * is logged in or not
	 * 
	 * @param out The output writer to write the header to
	 * @param title The title of the page we are making the header for
	 * @param userID The userID that may have to be passed in the links
	 * @param authToken The authToken that may have to be passed in the links
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public static void writeHeader(PrintWriter out, String title, String userID, String authToken){
		if(out==null) return;
		
		/*
		 *  Check is the username and authToken are present. It will change what the user's header is
		 */
		String userString = "";
		Boolean loggedin = true;
		String justUserName = "";
		String justAuthToken = "";

		if(userID != null && !userID.equals(EMPTY) && !userID.equals(NEG)){
			userString = "userID=" + userID;
			justUserName = userID;
		}
		else
			loggedin = false;

		if(authToken != null && !authToken.equals(EMPTY)){
			userString += "&authToken=" + authToken;
			justAuthToken = authToken;
		}
		else
			loggedin = false;

		/*
		 * Write the HTML tags
		 */
		out.println("<HTML>");
		out.println("<HEAD><TITLE>" + title + "</TITLE>");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/jquery-ui-1.8.11.custom.css\" media=\"screen\" />");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"header.css\"/>");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"body.css\"/>");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"myaccount.css\"/>");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"browse.css\"/>");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"errors.css\"/>");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"errors.css\"/>");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"item_image_style.css\"/>");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"home_recommendation.css\"/>");
		out.println("<script src=\"js/jquery-1.7.2.min.js\" type=\"text/javascript\"></script>");
		out.println("<script src=\"js/jquery-ui-1.8.11.custom.min.js\" type=\"text/javascript\"></script>");
		out.println("<script src=\"js/test.js\" type=\"text/javascript\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/jquery-ui-timepicker-addon.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/jquery-ui-sliderAccess.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/imagePrefetch.js\"></script>");
		

		out.println("</HEAD><BODY onload=\"test();\">");

		/*
		 *  Write the menu items
		 */
		out.write("<div class=\"header\">");
		out.write("<div class=\"link\">");
		out.write("<img src=\"cmart.jpg\" alt=\"C-MART\">");
		out.write("</div>");
		out.write("<div class=\"link\">");
		out.write("<a href=\"./index?" + userString + "\">Home</a>");
		out.write("</div>");
		out.write("<div class=\"link\">");
		out.write(" | <a href=\"./browsecategory?" + userString + "\">Browse</a>");
		out.write("</div>");
		out.write("<div class=\"link\">");
		out.write(" | <a href=\"./sellitem?" + userString + "\">Sell</a>");
		out.write("</div>");
		/*
		 *  Depending on whether the user is logged in we'll put 'My account' or 'login'
		 */
		if(loggedin == false){
			out.write("<div class=\"link\">");
			out.write(" | <a href=\"./login\">Login</a>");
			out.write("</div>");
			out.write("<div class=\"link\">");
			out.write(" | <a href=\"./registeruser\">Register</a>");
			out.write("</div>");
		}
		else{
			out.write("<div class=\"link\">");
			out.write(" | <a href=\"./myaccount?" + userString + "\">My Account</a>");
			out.write("</div>");
			out.write("<div class=\"link\">");
			out.write(" | <a href=\"./logout?" + userString +  "\">Log out</a>");
			out.write("</div>");
		}

		/*
		 * Place the search bar
		 */
		out.write("<form name=\"search_bar\" action=\"search\" method=\"POST\">");
		out.write("<input type=\"hidden\" name=\"userID\" value=\"" + justUserName + "\">");
		out.write("<input type=\"hidden\" name=\"authToken\" value=\"" + justAuthToken + "\">");
		out.write("<input type=\"text\" name=\"searchTerm\" size=\"50\" value=\"\"/>");
		out.write("<input type=\"submit\" value=\"Search\">");
		out.write("</form>");

		out.write("</div>");	
		out.println("<div class=\"body\">");
	}

	public static void writeHeaderNew(PrintWriter out, String title, String activeTab,String userID, String authToken){
		if(out==null) return;
		
		/*
		 *  Check is the username and authToken are present. It will change what the user's header is
		 */
		String userString = "";
		Boolean loggedin = true;
		String justUserName = "";
		String justAuthToken = "";

		if(userID != null && !userID.equals(EMPTY) && !userID.equals(NEG)){
			userString = "userID=" + userID;
			justUserName = userID;
		}
		else
			loggedin = false;

		if(authToken != null && !authToken.equals(EMPTY)){
			userString += "&authToken=" + authToken;
			justAuthToken = authToken;
		}
		else
			loggedin = false;

		/*
		 * Write the HTML tags
		 */
		out.println("<HTML>");
		out.println("<HEAD><TITLE>" + title + "</TITLE>");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/jquery-ui-1.8.11.custom.css\" media=\"screen\" />");
		//out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"header.css\"/>");
		//out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"body.css\"/>");
		//out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"myaccount.css\"/>");
		//out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"browse.css\"/>");
		//out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"errors.css\"/>");
		//out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"errors.css\"/>");
		out.println("<script src=\"js/jquery-1.7.2.min.js\" type=\"text/javascript\"></script>");
		out.println("<script src=\"js/jquery-ui-1.8.11.custom.min.js\" type=\"text/javascript\"></script>");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"item_image_style.css\"/>");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"home_recommendation.css\"/>");
		
		// NEW
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheets/foundation.css\"/>");
		out.println("<script type=\"text/javascript\" src=\"js/jquery-ui-timepicker-addon.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/jquery-ui-sliderAccess.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/imagePrefetch.js\"></script>");


		out.println("</HEAD><BODY>");

		/*
		 *  Write the menu items
		 */
		out.write("<br />");
		out.write("<div class=\"container\">");
		
		out.write("<div class=\"row\">");
		out.write("<div class=\"twelve columns\">");

		out.write("<div class=\"two columns\">");
		out.write("<img src=\"images/header/application.png\" alt=\"C-MART App\" width=\"30\"/>");
		out.write("<img src=\"cmart.jpg\" alt=\"C-MART\" width=\"90\">");
		out.write("</div>");

		out.write("<div class=\"ten columns\">");
		out.write("<dl class=\"nice contained tabs\">");

		out.write("<dd>");
		if(activeTab.equals("home"))
			out.write("<a class=\"active\" href=\"./index?" + userString + "\">");
		else
			out.write("<a href=\"./index?" + userString + "\">");

		out.write("<img src=\"images/header/home.png\" alt=\"home\" width=\"25\"/>Home");

		out.write("</a>");
		out.write("</dd>");
		out.write("<dd >");
		if(activeTab.equals("browse"))
			out.write("<a class=\"active\" href=\"./browsecategory?" + userString + "\">");
		else
			out.write("<a href=\"./browsecategory?" + userString + "\">");
		out.write("<img src=\"images/header/browse.png\" alt=\"browse\" width=\"25\"/>Browse");
		out.write("</a>");
		out.write("</dd>");
		
		out.write("<dd>");
		if(activeTab.equals("sell"))
			out.write("<a class=\"active\" href=\"./sellitem?" + userString + "\">");
		else
			out.write("<a href=\"./sellitem?" + userString + "\">");
		out.write("<img src=\"images/header/sell.png\" alt=\"sell\" width=\"25\"/>Sell");
		out.write("</a>");
		out.write("</dd>");

		out.write("<dd>");
		/*if(activeTab.equals("browsevideo"))
			out.write("<a class=\"active\" href=\"./browsevideo?" + userString + "\">");
		else
			out.write("<a href=\"./browsevideoitem?" + userString + "\">");
		out.write("<img src=\"images/header/video.png\" alt=\"browse video\" width=\"25\"/>Videos");
		out.write("</a>");
		out.write("</dd>");
	
		out.write("<dd>");
		if(activeTab.equals("uploadvideo"))
			out.write("<a class=\"active\" href=\"./uploadvideo?" + userString + "\">");
		else
			out.write("<a href=\"./uploadvideo?" + userString + "\">");
		out.write("<img src=\"images/header/videoup.png\" alt=\"upload video\" width=\"25\"/>Upload Video");
		out.write("</a>");
		out.write("</dd>");*/
		

		/*
		 *  Depending on whether the user is logged in we'll put 'My account' or 'login'
		 */
		if(loggedin == false){
			out.write("<dd>");
			if(activeTab.equals("login"))
				out.write("<a class=\"active\" href=\"./login\">");
			else
				out.write("<a href=\"./login\">");
			out.write("<img src=\"images/header/login.png\" alt=\"login\" width=\"25\"/>Login");
			out.write("</a>");
			out.write("</dd>");
			out.write("<dd>");
			if(activeTab.equals("register"))
				out.write("<a class=\"active\" href=\"./registeruser\">");
			else
				out.write("<a href=\"./registeruser\">");
			out.write("<img src=\"images/header/register.png\" alt=\"browse\" width=\"25\"/>Register");
			out.write("</a>");
			out.write("</dd>");
		}
		else{
			out.write("<dd>");
			if(activeTab.equals("myaccount"))
				out.write("<a class=\"active\" href=\"./myaccount?" + userString + "\">");
			else
				out.write("<a href=\"./myaccount?" + userString + "\">");
			out.write("<img src=\"images/header/browse.png\" alt=\"My Account\" width=\"25\"/>My Account");
			out.write("</a>");
			out.write("</dd>");
			out.write("<dd>");
			if(activeTab.equals("logout"))
				out.write("<a class=\"active\" href=\"./logout?" + userString +  "\">");
			else
				out.write("<a href=\"./logout?" + userString +  "\">");
			out.write("<img src=\"images/header/logout.png\" alt=\"logout\" width=\"25\"/>Logout");
			out.write("</a>");
			out.write("</dd>");
		}

		out.write("</dl>");

		out.write("</div>");
		out.write("</div>");
		out.write("</div>");
		
		/*
		 * Place the search bar
		 */
		out.write("<div class=\"row\">");
		out.write("<div class=\"twelve columns\">");
		
		out.write("<form name=\"search_bar\" action=\"search\" class=\"nice\" method=\"POST\">");
		out.write("<input type=\"hidden\" name=\"userID\" value=\"" + justUserName + "\">");
		out.write("<input type=\"hidden\" name=\"authToken\" value=\"" + justAuthToken + "\">");
		out.write("<input type=\"text\" name=\"searchTerm\"  value=\"\"/>");
		out.write("<input type=\"submit\" class=\"nice small radius white button\" value=\"Search\">");
		out.write("</form>");
		
		out.write("</div>");
		out.write("</div>");
		
		out.write("</div>");

		out.println("<div class=\"body\">");
	}
}
