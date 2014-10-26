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

import com.cmart.PageControllers.*;
import com.cmart.util.StopWatch;

/**
 * Servlet implementation class UploadVideoServlet
 */
@WebServlet(name="UploadedRedirectServlet", urlPatterns="/uploadedredirect")
public class UploadedRedirectServlet extends HttpServlet {
	private static final long serialVersionUID = 1L; // andy said just make up a number :P
	private static final String EMPTY = "";
	private static final String title = "Uploaded Redirected";
	private static long videoInserted = -1;  // do i need to support multiple video upload at a time, idk :)
	
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadedRedirectServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		makePage(request, response, Boolean.TRUE);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		makePage(request, response, Boolean.FALSE);
	}
	
	public void makePage(HttpServletRequest request, HttpServletResponse response, Boolean isGet)  throws ServletException, IOException {
		if(request !=null && response != null && !FaultController.fault(response)){
			// Do the timer if we are collecting stats
			StopWatch timer;
			if(GlobalVars.COLLECT_STATS){
				timer = new StopWatch();
				timer.start();
			}
			
			// Create a new page controller for this page, it will get and process the data
			System.out.println("in UploadedRedirectServlet, makePage, before new a vars for controller");
			UploadedRedirectController vars = new UploadedRedirectController();
			vars.checkInputs(request);
			
			
			System.out.println("error size: " + vars.getErrors().size());
			System.out.println("IS GET: "+ isGet);
			
			
			// If there are no errors we can try and insert the item
			if(!isGet&&vars.getErrors().size()==0){
				videoInserted=vars.insertVideoCon(vars.getUserID());
				System.out.println("in uploadredirectedservle, videoInserted= "+videoInserted);
			}
			
			//System.out.println("uploadredirect: we did not do the redirect");
			// We are using PrintWriter to be friendly to the international community. PrintWrite auto-converts coding
			PrintWriter out = response.getWriter();
			
			// If the output is to be suppressed then we'll redirect the output
			if(vars.getSuppressOutput())
				out = new PrintWriter(GlobalVars.BLACK_HOLE);
			
			
			
			// Do HTML4 or 5 depending on the global variables
			if(!vars.useHTML5()){
				// Set the return type
				response.setContentType("text/html");
				
				// Write the page header, using UserID and AutoToken from the controller :)
				Header.writeHeader(out, title, vars.getUserIDString(), vars.getAuthTokenString());
				
				createHTML4(out, vars, isGet);
				
				// Redirect the output to start writing the the user again in case we were putting it in the black hole
				out = response.getWriter();
				
				/*
				 * Output any errors. These don't need to be too pretty since we hope there isn't any errors!
				 * We put them here to help with debug and info, the page above should show pretty ones for users :D
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
			else{ // the website is using HTML5
				response.setContentType("application/json");
				
				createHTML5(out, vars, isGet, request);
			}	
		}
	}
	
	/**
	 * Create the HTML4 version of this website
	 *
	 * @param out The out writer to write the HTML to
	 * @param vars UploadedRedirectController instance
	 * @param isGet Indicate whether the request is using Get or Post method
	 * @param author: Jing  	
	 */

	public void createHTML4(PrintWriter out, UploadedRedirectController vars, Boolean isGet){
		if(out != null && vars != null)
			try{ 
				//get the data needed from the HTML4 page from the database
				vars.getHTML4Data();
				//process the data read from the database and prepare it to be printed
				vars.processHTML4();
				
				String currentVideoUrl = vars.getCurrentVideoURL();
				
				if (currentVideoUrl == null) {
					out.println("<h1 style=\"color:hotpink;\">Sorry, No File Found </h1>");
					return;
				}
				else{
					String videoName = vars.getVideoName();
					String videoDescription = vars.getVideoDescription();
					String realUrl = GlobalVars.localVideoDir+"/"+currentVideoUrl;
					long userID = vars.getUserID();
					String userName = GlobalVars.DB.getUser(userID).getUsername();
					long videoid = vars.getVideoID();
					String imagename = GlobalVars.localVideoDir+"/"+"image_"+videoid+".gif";
					
					System.out.println("in UploadRedirectServlet after make imagename="+imagename);
					//make the input form
					//out.println("<form name=\"register_user\" action=\"CommonVideoUploadServlet\" enctype=\"multipart/form-data\" method=\"post\">");
					out.println("<h1 style=\"color:hotpink;\"> Video Uploaded </h1>");
							
				
					out.println("<a href=\""+realUrl+"\">"); 
					out.println("<img src=\""+imagename+"\" alt=\"play this video\"  width=\"400\" height=\"280\" /> </a>");

					out.println("<p style=\"color:hotpink;\"> Uploaded by user:  "+userName+"</p> ");
					out.println("<p style=\"color:hotpink;\"> Video Name:  "+videoName+" </p> ");
					out.println("<p style=\"color:hotpink;\"> Video Description:  "+videoDescription+"</p>  <br><br>");
					
					out.println("for debug: url="+currentVideoUrl+"<br>");
					out.println("for debug: realURL="+realUrl);
					out.println("for debug: userID=" +userID);
				}

			}
			catch(Exception e){
				e.printStackTrace();
			}  
		
	}
	
	
	/**
	 * Create the HTML5 version of this website
	 *
	 * @param out The out writer to write the HTML to
	 * @param vars UploadedRedirectController instance
	 * @param isGet Indicate whether the request is using Get or Post method
	 * @param request The incoming request
	 * @param author: Jing  	
	 */
	public void createHTML5(PrintWriter out, UploadedRedirectController vars, Boolean isGet,HttpServletRequest request){
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
