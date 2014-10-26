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
 * This servlet uploads the images for an item if the user wants to add any
 * 
 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
 * @version 0.1
 * @since 0.1
 * @date 04/05/2011
 * 
 */
@WebServlet(name="UploadVideoServlet", urlPatterns="/uploadvideo")
public class UploadVideoServlet extends HttpServlet {
	
	private static final long serialVersionUID = 6475045556628458743L;
	private static final String EMPTY = "";
	private static final String title = "Upload images";
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
			UploadVideoController vars = new UploadVideoController();
			vars.checkInputs(request);
			
			// Try to upload the images if there are any present
			boolean redirect = false;
			if(!isGet && vars.getErrors().size()==0){
				vars.saveImages(request.getContextPath());
				
				// If there were no problems uploading the images then we can move on

				if(vars.getErrors().size()==0){
					timer.stop();
					if(!vars.userHTML5Images()){
						response.sendRedirect(vars.getRedirectURL());
						redirect = true;
					}else{
						response.sendRedirect("confirmsellitem.html");
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
				Header.writeHeaderNew(out, title, "uploadvideo", vars.getUserIDString(), vars.getAuthTokenString());
				
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
					GV.addStats(request, out, vars, 15);
				}
				
				// Write the page footer
				Footer.writeFooter(out);
			}
			else  if(vars.useHTML5()){
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
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void createHTML4(PrintWriter out, UploadVideoController vars, Boolean isGet){
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
			
			/*
			 * Make the input form
			 */
			out.println("<form name=\"register_user\" action=\"uploadvideo\" enctype=\"multipart/form-data\" method=\"POST\">");
			out.println("<div id=\"center\">");
			
			out.println("<input type=\"hidden\" name=\"userID\" size=\"50\" value=\""+ vars.getUserIDString() + "\"/>");
			out.println("<input type=\"hidden\" name=\"authToken\" size=\"50\" value=\""+ vars.getAuthTokenString() + "\"/>");
			out.println("<input type=\"file\" name=\"Video\"> <BR />");
			
			out.println("<div id=\"floatright\"><input type=\"submit\" value=\"Upload images\"></div>");
			
			out.println("</div>");
			out.println("</form>");
			
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
	public void createHTML5(PrintWriter out, UploadVideoController vars, Boolean isGet){
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
