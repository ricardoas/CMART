package com.cmart.PageServlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.cmart.Data.GlobalVars;
import com.cmart.PageControllers.*;
import com.cmart.util.StopWatch;
import java.io.IOException;
import java.net.UnknownHostException;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

/**
 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
 * @version 0.1
 * @since 0.1
 * @date 04/05/2011
 * 
 */
@WebServlet(name = "GetImageServlet", urlPatterns = "/getimage")
public class GetImageServlet extends HttpServlet {

	private static final long serialVersionUID = 6475041456628458743L;
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
		makePage(request, response, Boolean.FALSE);
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

			String imageName = request.getParameter("imagefilename");

			// Do the timer if we are collecting stats
			StopWatch timer = null;
			if (GV.COLLECT_STATS) {
				timer = new StopWatch();
				timer.start();
			}

			// Create a new page controller for this page, it will get and
			// process the data
			ExampleBlankController vars = new ExampleBlankController();
			int loopCount = 0;
			do {
				vars.checkInputs(request);
				loopCount++;
			} while (loopCount <= vars.getProcessingLoop());

			// We are using PrintWriter to be friendly to the international
			// community. PrintWrite auto-converts coding
			PrintWriter out = response.getWriter();
			// If the output is to be suppressed then we'll redirect the output
			if (vars.getSuppressOutput())
				out = new PrintWriter(GV.BLACK_HOLE);
			try {
				Mongo mongo = new Mongo(GV.GFS_IP, 27017);
				DB db = mongo.getDB("imagedb");
				GridFS gfsPhoto = new GridFS(db, "photo");
				GridFSDBFile image = gfsPhoto.findOne(imageName);
				response.reset();
				response.setContentType("image/jpeg");
				OutputStream outS = response.getOutputStream();
				image.writeTo(outS);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (MongoException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}