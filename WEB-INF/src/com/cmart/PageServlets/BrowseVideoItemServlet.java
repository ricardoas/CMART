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
import com.cmart.util.VideoItem;


@WebServlet(name="BrowseVideoItemServlet", urlPatterns="/browsevideoitem")
public class BrowseVideoItemServlet extends HttpServlet {
	
	private static final long serialVersionUID = 6475041456628458745L;
	private static final String EMPTY = "";
	private static final String title = "Browse Video";
	
	/**
	 * Get the page, calls the page to be made
	 * We used to check the parameters in here, but I moved it to a controller object to keep the logic away from the layout
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @author 
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
	 * @author 
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
	 * @author 
	 */
	public void makePage(HttpServletRequest request, HttpServletResponse response, Boolean isGet)  throws ServletException, IOException {
		if(request !=null && response != null && !FaultController.fault(response)){
			// Do the timer if we are collecting stats
			StopWatch timer;
			if(GlobalVars.COLLECT_STATS){
				timer = new StopWatch();
				timer.start();
			}
			
			// Create a new page controller for this page, it will get and process the data
			BrowseVideoItemController vars = new BrowseVideoItemController();
			vars.checkInputs(request);
			
			// We are using PrintWriter to be friendly to the international community. PrintWrite auto-converts coding
			PrintWriter out = response.getWriter();
			
			// If the output is to be suppressed then we'll redirect the output
			if(vars.getSuppressOutput())
				out = new PrintWriter(GlobalVars.BLACK_HOLE);
			
			// Do HTML4 or 5 depending on the global variables
			if(!vars.useHTML5()){
				
				System.out.println("For debug, using HTML4");
				
				// Set the return type
				response.setContentType("text/html");
				
				// Write the page header
				Header.writeHeaderNew(out, title, "videoup", vars.getUserIDString(), vars.getAuthTokenString());
				
				createHTML4(out, vars, isGet);
				
				// Redirect the output to start writing the the user again in case we were putting it in the black hole
				out = response.getWriter();
				
				/*
				 * Output any errors. These don't need to be too pretty since we hope there isn't any!
				 * We put them here to help with debug and info, the page above should show pretty ones for users
				 */
				if(GlobalVars.PRINT_ALL_ERRORS){
					GlobalVars.addErrors(out, vars.getErrors());
				}
				
				/*
				 * Process the page time
				 */
				if(GlobalVars.COLLECT_STATS){
					timer.stop();
					vars.setTotalTime(timer.getTimeTaken());
					GlobalVars.addStats(request, out, vars,1);
				}
				
				// Write the page footer
				Footer.writeFooter(out);
			}
			else{
				
				System.out.println("For debug: using HTML5");
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
	 * @author 
	 */
	public void createHTML4(PrintWriter out, BrowseVideoItemController vars, Boolean isGet){
		if(out != null && vars != null)
		try{
			// Get the data needed to display the page
			vars.getHTML4Data();
			vars.processHTML4();
			
			ArrayList<VideoItem> videoItems = vars.getVideoItems();
			String[] videoItemURLs = vars.getVideoItemURLs();
			
			out.println("<h1> Video Lists </h1>");
			
			out.write("<div class=\"container\">");
			out.write("<div class=\"row\">");
			out.write("<div class=\"twelve columns\">");
			
			if(videoItems != null && videoItemURLs.length > 0){
				for (int i = 0; i < videoItemURLs.length; i++){
					out.write("<div class=\"row\">");
					out.write("<div class=\"twelve columns\">");
					
					out.write("<div class=\"row\">");
					out.write("<div class=\"twelve columns\">&nbsp;");
					out.println("</div>");
					out.println("</div>");
					
					VideoItem item = videoItems.get(i); //get a video item 
					long userid = item.getUserID();
					long videoid = item.getId();
					String imagename = GlobalVars.localVideoDir+"/"+"image_"+videoid+".gif";
					String username = GlobalVars.DB.getUser(userid).getUsername();
					String videoname = item.getName();
					String videodescription = item.getDescription();
					String url = item.getUrl();
					String realUrl = GlobalVars.localVideoDir+"/"+url;
					
				
					
					
					
					//out.println("<p> Video No "+(i+1)+"</p>");
					/*
					out.println("<object data=\""+realUrl+"\"  width=\"320\" height=\"240\" >");

					out.println("Your browser does not support this video");
					out.println("</object>");
					*/
					out.println("<a href=\""+realUrl+"\">"); 
					out.println("<img src=\""+imagename+"\" alt=\"play this video\" width=\"400\" height=\"280\"/> </a>");
					out.write("<div class=\"container\">");
					out.write("<div class=\"row\">");
					out.write("<div class=\"twelve columns\">");
					
					
					out.write("<div class=\"row\">");
					out.write("<div class=\"two columns\" style=\"background-color:silver;\">");
					out.println("Uploaded by user:");
					out.println("</div>");
					out.write("<div class=\"four columns\">");
					out.println(username);
					out.println("</div>");
					out.println("</div>");
					
					out.write("<div class=\"row\">");
					out.write("<div class=\"two columns\">");
					out.println("Video Name:");
					out.println("</div>");
					out.write("<div class=\"four columns\">");
					out.println(videoname);
					out.println("</div>");
					out.println("</div>");
					
					out.write("<div class=\"row\">");
					out.write("<div class=\"two columns\"> style=\"background-color:silver;\">");
					out.println("Video Description:");
					out.println("</div>");
					out.write("<div class=\"four columns\">");
					out.println(videodescription);
					out.println("</div>");
					out.println("</div>");
					
					out.println("</div>");
					out.println("</div>");
					out.println("</div>");
					
					/*
					out.println("<p> This info is for debug: <br>");
					out.println("id ="+item.getId()+"<br>");
					out.println("url="+item.getUrl()+"<br>");

					out.println("realURL="+realUrl);
					*/
					out.println("</div>");
					out.println("</div>");
				}
			}
			
			
			out.write("</div>");
			out.write("</div>");
			out.write("</div>");
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
	 * @author 
	 */
	public void createHTML5(PrintWriter out, BrowseVideoItemController vars, Boolean isGet){
		if(out != null && vars != null)
		try{
			// Get the data needed to display the page
			vars.getHTML5Data();
			vars.processHTML5();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}

