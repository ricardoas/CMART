package com.cmart.PageServlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cmart.Data.Error;
import com.cmart.Data.Footer;
import com.cmart.Data.GlobalVars;
import com.cmart.Data.Header;
import com.cmart.PageControllers.BrowseCategoryController;
import com.cmart.PageControllers.BrowseVideosController;
import com.cmart.PageControllers.FaultController;
import com.cmart.util.Image;
import com.cmart.util.Item;
import com.cmart.util.StopWatch;



@WebServlet(name="BrowseVideosServlet", urlPatterns="/browsevideos")
public class BrowseVideosServlet extends HttpServlet{
	private static final long serialVersionUID = 6472741003023438743L;
	private static final String EMPTY = "";
	private static final String title = "Browse Category";
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
		this.makePage(request, response, Boolean.FALSE);
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
			BrowseVideosController vars = new BrowseVideosController();
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
			
			// Do HTML4 or 5 depending on the page
			if(!vars.useHTML5()){
				// Set the return type
				response.setContentType("text/html");
				
				// Write the page header
				Header.writeHeader(out, title, vars.getUserIDString(), vars.getAuthTokenString());
				
				// Create the HTML4
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
					GV.addStats(request, out, vars, 0);
				}
				
				// Write the page footer
				Footer.writeFooter(out);
			}
			else{
				response.setContentType("application/json");
				
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
	public void createHTML4(PrintWriter out, BrowseVideosController vars, Boolean isGet){
		if(out != null && vars != null)
		try{		
			// Get the data needed to display the page
			vars.getHTML4Data();
			int loopCount = 0;
			do{
				vars.processHTML4();
				loopCount++;
			}while(loopCount <= vars.getProcessingLoop());
			
			
			out.println("<div class=\"browse\">");
			ArrayList<String> videos = vars.getVideos();
			for(int i=0;i<videos.size();i++){
				out.println(videos.get(i));
				out.println("<BR>");
			}	
			
			out.println("</div>");
		}
		catch(Exception e){
			//TODO: make this output to a file
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the HTML5 version of the website
	 * 
	 * @param request The incoming request
	 * @param response The response sent to the user
	 * @param out The out writer to write the HTML to
	 * @return The HTML5 response (although it could just return void since it writes to the requests out writer)
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void createHTML5(PrintWriter out, BrowseVideosController vars, Boolean isGet){
		if(out != null && vars != null)
		try{
			// Get the data needed to display the page
			vars.getHTML5Data();
			vars.processHTML5();
			
			/*
			 * Print out all of the items
			 */
			String[] itemJSON = vars.getItemJSON();
			String[] categoriesJSON=vars.getCategoryJSON();
			String[] sellerJSON=vars.getSellerJSON();
			StringBuffer output=new StringBuffer();
			output.append("{\"categories\":[");
			if(itemJSON.length>0){
			output.append(categoriesJSON[0]);}
			for(int i=1; i<categoriesJSON.length; i=i+1){
				output.append(",").append(categoriesJSON[i]);}
			output.append("],");
			output.append("\"items\":[");
			if(itemJSON.length>0){
			output.append(itemJSON[0]);}
			for(int i=1; i<itemJSON.length; i++){
				output.append(","+itemJSON[i]);}
			output.append("],");
			output.append("\"sellers\":[");
			if(sellerJSON.length>0){
			output.append(sellerJSON[0]);}
			for(int i=1; i<sellerJSON.length; i++){
				output.append(","+sellerJSON[i]);}
			output.append("],").append(GV.addStatsHTML5(vars)).append(",").append(GV.addErrorsHTML5(vars.getErrors())).append("}");
			out.println(output);
			
			
		}
		catch(Exception e){
			//TODO: make this output to a file
			e.printStackTrace();
		}
	}
}
