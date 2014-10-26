package com.cmart.Data;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;

import com.cmart.DB.CassandraDBQuery;
import com.cmart.DB.DBQuery;
import com.cmart.DB.MySQLDBQuery;
import com.cmart.DB.MySQLINNODBQuery;
import com.cmart.PageControllers.PageController;
import com.cmart.util.BlackHoleOutput;
import com.cmart.util.NullStopWatch;
import com.cmart.util.PageStatistic;
import com.cmart.util.StopWatch;
import javax.servlet.annotation.WebListener;

@WebListener
/**
 * This class contains global variables that are used by all servlets
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
public class GlobalVars implements ServletContextListener{
	private static GlobalVars single;
	private static TreeMap<String, String> config;
	private static ArrayList<PageStatistic> stats;
	private static int statsLen;
	public final static Date maxDate = new Date(2145938399000l); // If C-MART is around in 2038 I'll fix this problem

	// Controls if the HTML4 or HTML5 version of the website is used
	//public static final boolean isHTML4 = true;

	public static final int SQL_RETRIES = 5;

	// Controls whether debug statements are printed to sdtout
	public static Boolean DEBUG;

	// Controls whether the pages are verbose when processing to stdout
	public static Boolean VERBOSE;

	// A single instance of the DB class for all pages to use
	public static DBQuery DB;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println("Started CMART");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		System.out.println("Killing CMART");
		// Close all of the database connections
		if(DB !=null){
			for(Connection conn:DB.getConnections()){
				if(conn != null)
					try {
						conn.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
	}

	public GlobalVars(){
		GlobalVars.getInstance();
	}

	private GlobalVars(boolean nothing){
		/*
		 * Read in the configuration values
		 */
		try{
			FileInputStream fin = new FileInputStream("cmartconfig.txt");
			DataInputStream din = new DataInputStream(fin);
			BufferedReader bin = new BufferedReader(new InputStreamReader(din));
			String line;
			String commentLine = "#";
			config = new TreeMap<String, String>();
			StringBuffer valueBuffer = new StringBuffer();

			while(bin.ready()){
				// Read in a line, if it is a comment ignore, otherwise we'll parse it for a value/key pair
				line = bin.readLine();
				if(line!=null){
					if(!line.startsWith(commentLine)){
						// Split the line at an '=', delete while space and add the value to the config map
						String values[] = line.split("=");

						if(values.length >= 2){
							String key = values[0].trim();

							valueBuffer.setLength(0);
							for(int i=1; i<values.length; i++)
								valueBuffer.append(values[i].trim());

							config.put(key.toLowerCase(), valueBuffer.toString());
						}
					}
				}
			}

			/*
			 *  Set the variables we need. If any values are bad we'll add them to an error list
			 */
			valueBuffer.setLength(0);
			String value;

			// DEBUG
			value = config.get("debug");
			if(value != null){
				if(value.equals("1") || value.toLowerCase().equals("true")) DEBUG = Boolean.TRUE;
				else if(value.equals("0") || value.toLowerCase().equals("false")) DEBUG = Boolean.FALSE;
			}
			if(value==null || DEBUG==null){
				DEBUG = Boolean.FALSE;
				valueBuffer.append("Warning: 'debug' was not set correctly. Defaulting to false.\n");
			}

			// VERBOSE
			value = config.get("verbose");
			if(value != null){
				if(value.equals("1") || value.toLowerCase().equals("true")) VERBOSE = Boolean.TRUE;
				else if(value.equals("0") || value.toLowerCase().equals("false")) VERBOSE = Boolean.FALSE;
			}
			if(value==null || VERBOSE==null){
				VERBOSE = Boolean.FALSE;
				valueBuffer.append("Warning: 'verbose' was not set correctly. Defaulting to false.\n");
			}

			// SOLR configuration
			value = config.get("solr_enabled");
			if(value != null){
				if(value.equals("1") || value.toLowerCase().equals("true")) SOLR_ENABLED = Boolean.TRUE;
				else if(value.equals("0") || value.toLowerCase().equals("false")) SOLR_ENABLED = Boolean.FALSE;
			}
			if(value==null || SOLR_ENABLED==null){
				SOLR_ENABLED = Boolean.FALSE;
				valueBuffer.append("Warning: 'solr_enabled' was not set correctly. Defaulting to false.\n");
			}
			
			value = config.get("solr_url");
			if(value != null){
				SOLR_URL = value;
			}
			else{
				valueBuffer.append("warning: 'solr_url' was set incorrectly..\n");
			}

			value = config.get("solr_max_conns_per_host");
			if (value != null) {
				SOLR_MAX_CONNS_PER_HOST = Integer.valueOf(value);
			}
			if (value == null) {
				valueBuffer.append("Error: 'solr_max_conns_per_host' is incorrect.\n");
			}

			// MYSQL Database URL
			value = config.get("my_database_url");
			if(value != null){
				MY_DATABASE_URL = value;
			}
			else{
				valueBuffer.append("warning: 'my_database_url' was set incorrectly..\n");
			}

			// MYSQL database driver
			value = config.get("my_database_driver");
			if(value != null){
				MY_DATABASE_DRIVER = value;
			}
			else{
				valueBuffer.append("warning: 'my_database_driver' was set incorrectly..\n");
			}

			// MYSQL database username
			value = config.get("my_database_username");
			if(value != null){
				MY_DATABASE_USERNAME = value;
			}
			else{
				valueBuffer.append("warning: 'my_database_username' was set incorrectly..\n");
			}

			// MYSQL database password
			value = config.get("my_database_password");
			if(value != null){
				MY_DATABASE_PASSWORD = value;
			}
			else{
				valueBuffer.append("warning: 'my_database_password' was set incorrectly..\n");
			}

			// The connection cache
			value = config.get("my_conn_cache");
			if (value != null) {
				MY_CONN_CACHE = Integer.valueOf(value);
			}
			if (value == null) {
				valueBuffer.append("Error: 'my_conn_cache' is incorrect.\n");
			}

			// MYSQL Database URL
			value = config.get("cassandra_database_url");
			if (value != null) {
				CASSANDRA_DATABASE_URL = value;
			} else {
				valueBuffer.append("warning: 'cassandra_database_url' was set incorrectly..\n");
			}

			// MYSQL database driver
			value = config.get("cassandra_database_driver");
			if (value != null) {
				CASSANDRA_DATABASE_DRIVER = value;
			} else {
				valueBuffer.append("warning: 'cassandra_database_driver' was set incorrectly..\n");
			}

			// Database type
			value = config.get("database_type");
			if(value != null){
				if(value.toLowerCase().equals("myisam")) DB = MySQLDBQuery.getInstance();
				else if(value.toLowerCase().equals("innodb")) DB = MySQLINNODBQuery.getInstance();
				else if(value.toLowerCase().equals("cassandra")) DB = CassandraDBQuery.getInstance();
				System.out.println("Got DB instance: " + value+".");
			}
			else{
				valueBuffer.append("Error: 'database' was set incorrectly..\n");
			}

			// The remote image directory
			value = config.get("remote_image_dir");
			if(value != null){
				REMOTE_IMAGE_DIR = value;
			}
			if(value==null){
				valueBuffer.append("Error: 'remote_image_dir' is incorrect.\n");
			}

			// The remote image ip
			value = config.get("remote_image_ip");
			if (value != null) {
				REMOTE_IMAGE_IP = value;
			}
			if (value == null) {
				valueBuffer.append("Error: 'remote_image_ip' is incorrect.\n");
			}
			
			// Image Prefetching
			value = config.get("prefetch_image");
			if (value != null) {
				PREFETCH_IMAGES_NUM = Integer.valueOf(value);
				if(PREFETCH_IMAGES_NUM>0)
				PREFETCH_IMAGES=true;
				else
					PREFETCH_IMAGES=false;
			}
			if (value == null) {
				valueBuffer.append("Warning: 'prefetch_image' is incorrect.\n");
			}

			// Set the OS specific variables
			String osName = System.getProperty("os.name");

			if(osName.toLowerCase().contains("windows")){
				// The windows local temp directory
				value = config.get("win_local_temp_dir");
				if(value != null){
					LOCAL_TEMP_DIR = new File(value);
				}
				if(value==null){
					valueBuffer.append("Error: 'win_local_temp_dir' is incorrect.\n");
				}

				// The windows image directory
				value = config.get("win_local_image_dir");
				if(value != null){
					LOCAL_IMAGE_DIR = new File(value);
				}
				if(value==null){
					valueBuffer.append("Error: 'win_local_image_dir' is incorrect.\n");
				}
				
				value = config.get("windows_local_video_dir");
				if(value != null){
					LOCAL_VIDEO_DIR = new File("/usr/share/tomcat7/webapps/videos");
				}
				if(value==null){
					valueBuffer.append("Error: 'linux_local_video_dir' is incorrect.\n");
				}
			}
			else{
				// The linux local temp directory
				value = config.get("linux_local_temp_dir");
				if(value != null){
					LOCAL_TEMP_DIR = new File(value);
				}
				if(value==null){
					valueBuffer.append("Error: 'linux_local_temp_dir' is incorrect.\n");
				}

				// The linux image directory
				value = config.get("linux_local_image_dir");
				if(value != null){
					LOCAL_IMAGE_DIR = new File(value);
				}
				if(value==null){
					valueBuffer.append("Error: 'linux_local_image_dir' is incorrect.\n");
				}

				value = config.get("linux_local_video_dir");
				if(value != null){
					LOCAL_VIDEO_DIR = new File("/usr/share/tomcat7/webapps/videos");
				}
				if(value==null){
					valueBuffer.append("Error: 'linux_local_video_dir' is incorrect.\n");
				}
				
			}

			/*
			 * Populate the statistics object
			 */
			stats = makePageStatistics();

			// Print out any warnings or errors
			if(valueBuffer.length()>0)
				System.out.println(valueBuffer.toString());

			bin.close();
			din.close();
			fin.close();
		}
		catch(Exception e){
			System.out.println("Could not open config file 'cmartconfig.txt' in " + System.getProperty("user.dir"));
			e.printStackTrace();
		}
	}

	public static synchronized GlobalVars getInstance() {
		// Create a new singleton of this object
		if (single == null) {
			synchronized (GlobalVars.class) {
				if (single == null) {
					single = new GlobalVars(true);
				}
			}
		}

		return single;
	}

	private ArrayList<PageStatistic> makePageStatistics(){
		// bin size in ms
		int binSize = 1;

		ArrayList<PageStatistic> newStats = new ArrayList<PageStatistic>(32);
		newStats.add(new PageStatistic(0, "Browse Category", binSize, 1000));
		newStats.add(new PageStatistic(1, "Buy Item", binSize, 1000));
		newStats.add(new PageStatistic(2, "Confirm Bid", binSize, 1000));
		newStats.add(new PageStatistic(3, "Confirm Buy", binSize, 1000));
		newStats.add(new PageStatistic(4, "Confirm Sell", binSize, 1000));
		newStats.add(new PageStatistic(5, "Blank Example", binSize, 1000));
		newStats.add(new PageStatistic(6, "Home", binSize, 1000));
		newStats.add(new PageStatistic(7, "Login", binSize, 1000));
		newStats.add(new PageStatistic(8, "Logout", binSize, 1000));
		newStats.add(new PageStatistic(9, "Move Old Items", binSize, 1000));
		newStats.add(new PageStatistic(10, "*****************", binSize, 1000));
		newStats.add(new PageStatistic(11, "My Account", binSize, 1000));
		newStats.add(new PageStatistic(12, "Register User", binSize, 1000));
		newStats.add(new PageStatistic(13, "Search", binSize, 1000));
		newStats.add(new PageStatistic(14, "Sell Item", binSize, 1000));
		newStats.add(new PageStatistic(15, "Sell Item Images", binSize, 1000));
		newStats.add(new PageStatistic(16, "Statistics", binSize, 1000));
		newStats.add(new PageStatistic(17, "Update User Details", binSize, 1000));
		newStats.add(new PageStatistic(18, "View Item", binSize, 1000));
		newStats.add(new PageStatistic(19, "View User", binSize, 1000));
		newStats.add(new PageStatistic(20, "Ask Question", binSize, 1000));
		newStats.add(new PageStatistic(21, "Leave Comment", binSize, 1000));
		newStats.add(new PageStatistic(22, "Confirm Comment", binSize, 1000));

		statsLen = newStats.size();

		return newStats;
	}

	public ArrayList<PageStatistic> clearStats(){
		ArrayList<PageStatistic> newStats = makePageStatistics();
		ArrayList<PageStatistic> oldStats = stats;
		stats = newStats;

		return oldStats;
	}

	public ArrayList<PageStatistic> getStats(){
		return stats;
	}

	// A black hole if we don't want the page to output
	public static final BlackHoleOutput BLACK_HOLE = new BlackHoleOutput();

	// Controls if we print all errors at the bottom of pages
	public static final boolean PRINT_ALL_ERRORS = true;

	// Controls if we want to collect page statistics or not
	public static final boolean COLLECT_STATS = true;

	// If we are not collecting stats, return a null timer
	public static final NullStopWatch NULL_TIMER = new NullStopWatch();

	// Date formatters for user values
	private static final SimpleDateFormat SDF_FULL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat SDF_MMYY = new SimpleDateFormat("MMyyyy");

	// Currency formatter
	public static final NumberFormat currency=NumberFormat.getCurrencyInstance(Locale.US);

	// Images location
	public static File LOCAL_TEMP_DIR;
	public static File LOCAL_IMAGE_DIR; 
	public static File LOCAL_VIDEO_DIR; 
	public static String REMOTE_IMAGE_DIR;
	public static String REMOTE_IMAGE_IP;
	
	// Prefetching Images
	public static int PREFETCH_IMAGES_NUM=0;
	public static boolean PREFETCH_IMAGES;

	// MYSQL Database information
	public static String MY_DATABASE_URL;
	public static String MY_DATABASE_DRIVER;
	public static String MY_DATABASE_USERNAME;
	public static String MY_DATABASE_PASSWORD;
	public static int MY_CONN_CACHE;

	// CASSANDRA Database information
	public static String CASSANDRA_DATABASE_URL;
	public static String CASSANDRA_DATABASE_DRIVER;

	// SOLR Information
	public static Boolean SOLR_ENABLED;
	public static String SOLR_URL;
	public static int SOLR_MAX_CONNS_PER_HOST;

	// VIDEO information
	public static final String localVideoDir = "/usr/share/tomcat7/webapps/videos";	
    public static final String remoteVideoDir = "/videos";

    public static final String localUploadDir =  "/usr/share/tomcat7/webapps/videos";


	/**
	 * This method adds the page statistics to the counters. Allowing them to be accessed by the stats page
	 * 
	 * @param request
	 * @param out
	 * @param pageController
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public static void addStats(HttpServletRequest request, PrintWriter out, PageController pageController, int pageID){
		out.println("<div class=\"container\">");
		out.println("<div class=\"row\">");
		out.println("<div class=\"seven columns\">");
		
		out.println("<table><thead><tr>");
		out.println("<th>Server: </th>");
		out.println("<th id=\"statsServerName\">" + request.getServerName() + "</th>");
		out.println("</tr></thead>");
		
		out.println("<tbody id=\"statsBody\">");
		
		out.println("<tr class=\"row1\"><td>Time to process parameters:</td>");
		out.println("<td id=\"statsPramTime\">" + pageController.getParamTime() + "</td></tr>");
		
		out.println("<tr class=\"row2\"><td>Time to access database:</td>");
		out.println("<td id=\"statsDBTime\">" + pageController.getDBTime() + "</td></tr>");
		
		out.println("<tr class=\"row1\"><td>Time to process page data:</td>");
		out.println("<td id=\"statsProcTime\">" + pageController.getProcessingTime() + "</td></tr>");
		
		out.println("<tr class=\"row2\"><td>Time to render page:</td>");
		long renderTime = (pageController.getTotalTime()-(pageController.getParamTime()+pageController.getDBTime()+ pageController.getProcessingTime()));
		out.println("<td id=\"statsRenderTime\">" + renderTime + "</td></tr>");
		
		out.println("<tr class=\"row1\"><td>Total time to process:</td>");
		out.println("<td id=\"statsTotalTime\">" + pageController.getTotalTime() + "</td></tr>");
		
		out.println("</tbody></table></div></div></div>");
		
		// Parameter time
		/*out.println("<div class=\"row1\">");
		out.println("<div class=\"name\">");
		out.println("Time to process parameters:");
		out.println("</div>");
		out.println("<div class=\"time\">");
		out.println(pageController.getParamTime()); out.println(" ms");
		out.println("</div>");
		out.println("</div>");

		// Database time
		out.println("<div class=\"row2\">");
		out.println("<div class=\"name\">");
		out.println("Time to access database:");
		out.println("</div>");
		out.println("<div class=\"time\">");
		out.println(pageController.getDBTime()); out.println(" ms");
		out.println("</div>");
		out.println("</div>");

		// Processing time
		out.println("<div class=\"row1\">");
		out.println("<div class=\"name\">");
		out.println("Time to process page data:");
		out.println("</div>");
		out.println("<div class=\"time\">");
		out.println(pageController.getProcessingTime()); out.println(" ms");
		out.println("</div>");
		out.println("</div>");

		// Page render time
		out.println("<div class=\"row2\">");
		out.println("<div class=\"name\">");
		out.println("Time to render page:");
		out.println("</div>");
		out.println("<div class=\"time\">");
		long renderTime = (pageController.getTotalTime()-(pageController.getParamTime()+pageController.getDBTime()+ pageController.getProcessingTime()));
		out.println(renderTime); out.println(" ms");
		out.println("</div>");
		out.println("</div>");

		// Total page time
		out.println("<div class=\"row1\">");
		out.println("<div class=\"name\">");
		out.println("Total time to process:");
		out.println("</div>");
		out.println("<div class=\"time\">");
		out.println(pageController.getTotalTime()); out.println(" ms");
		out.println("</div>");
		out.println("</div>");

		out.println("</div>");
*/
		
		if(pageID < statsLen){
			PageStatistic p = stats.get(pageID);
			if(p!=null) p.addReading(pageController.getParamTime(), pageController.getDBTime(), pageController.getProcessingTime(), renderTime, pageController.getTotalTime());
		}
	}

	/**
	 * This method adds the page statistic for HTML5 pages
	 * 
	 * @param pageController
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public static void addStatsJSON(HttpServletRequest request, PrintWriter out, PageController pageController, int pageID){
		out.print("\"stats\":{\"server\":\"");
		out.print(request.getServerName());
		
		out.print("\",\"paramaterTime\":\"");
		out.print(pageController.getParamTime());

		out.print("\",\"databaseTime\":\"");
		out.print(pageController.getDBTime());

		out.print("\",\"processingTime\":\"");
		out.print(pageController.getProcessingTime());

		out.print("\",\"renderTime\":\"");
		long renderTime = (pageController.getTotalTime()-(pageController.getParamTime()+pageController.getDBTime()+ pageController.getProcessingTime()));
		out.print(renderTime);

		out.print("\",\"totalTime\":\"");
		out.print(pageController.getTotalTime());

		out.print("\"}");
		
		if(pageID < statsLen){
			PageStatistic p = stats.get(pageID);
			if(p!=null) p.addReading(pageController.getParamTime(), pageController.getDBTime(), pageController.getProcessingTime(), renderTime, pageController.getTotalTime());
		}
	}

	/**
	 * I deprecated this because it was cat-ing strings - Andy
	 * @param pageController
	 * @return
	 */
	@Deprecated public static String addStatsHTML5(PageController pageController){
		String out = "\"stats\":{\"paramaterTime\":\""+pageController.getParamTime()+
				"\",\"databaseTime\":\""+pageController.getDBTime()+
				"\",\"processingTime\":\""+pageController.getProcessingTime()+
				"\",\"renderTime\":\""+(pageController.getTotalTime()-(pageController.getParamTime()+pageController.getDBTime()+ pageController.getProcessingTime()))+
				"\",\"totalTime\":\""+pageController.getTotalTime()+"\"}";
		return out;
	}

	/**
	 * Add the page errors to the HTML4 page
	 * 
	 * @param out
	 * @param errors
	 */
	public static void addErrors(PrintWriter out, ArrayList<Error> errors){
		out.write("<div class=\"container\">");
		out.write("<div class=\"row\">");
		out.write("<div class=\"seven columns\">");
		out.println("<div class=\"errors\">");
		out.println("<table><thead>");
		out.println("<div class=\"title\">");
		out.println("<tr><th colspan=\"2\">Errors: "); out.println(errors.size());
		out.println("</th></tr></div></thead><tbody>");

		for(int i=0; i<errors.size(); i++){
			out.printf("<tr><div class=\"row%s\" id=\"entry\">\n", i%2==0? 1 :2 );

			out.println("<td><div class=\"nocol\">");
			out.println(makeLabel("error", errors.get(i).getErrorNumber()));
			out.println(errors.get(i).getErrorNumber());
			out.println("</label>");
			out.println("</div></td>");

			out.println("<td><div class=\"msgcol\">");
			out.println(errors.get(i).toString());
			out.println("</div></td>");

			out.println("</div></tr>");
		}
		out.println("</tbody></table>");
		out.println("</div>");

		out.println("</div>");
		out.println("</div>");
		out.println("</div>");
	}


	/**
	 * Write the errors for HTML5 pages
	 * 
	 * @param out
	 * @param errors
	 */
	public static void addErrorsJSON(PrintWriter out, ArrayList<Error> errors){
		out.print("\"errors\":[");

		if(errors.size()>0){
			out.print("{\"errorNumber\":\""); out.print(errors.get(0).getErrorNumber()); out.print("\",");
			out.print("\"errorMessage\":\""); out.print(errors.get(0).toString()); out.print("\"}");

			for(int i=1; i<errors.size(); i++){
				out.print(",{\"errorNumber\":\""); out.print(errors.get(i).getErrorNumber()); out.print("\"");
				out.print(",\"errorMessage\":\""); out.print(errors.get(i).toString()); out.print("\"}");
			}
		}

		out.print("]");
	}

	/**
	 * I deprecated this because it was cat-ing strings - Andy
	 * 
	 * @param errors
	 * @return
	 */
	@Deprecated public static String addErrorsHTML5(ArrayList<Error> errors){
		StringBuffer out = new StringBuffer("\"errors\":[");
		if(errors.size()>0){
			out.append("{\"errorNumber\":\"").append(errors.get(0).getErrorNumber()).append("\",");
			out.append("\"errorMessage\":\"").append(errors.get(0).toString()).append("\"}");
			for(int i=1; i<errors.size(); i++){
				out.append(",{\"errorNumber\":\"").append(errors.get(i).getErrorNumber()).append("\"");
				out.append(",\"errorMessage\":\"").append(errors.get(i).toString()).append("\"}");
			}
		}
		out.append("]");
		return out.toString();
	}

	/**
	 * Return a string that can be used as a label in the HTML document
	 * 
	 * @param tag
	 * @param i
	 * @return
	 */
	public static String makeLabel(String tag, long i){
		if(i >= 0)
			return "<label for=\"" + tag + i + "\">";
		else
			return "<label for=\"" + tag + "\">";
	}

	/**
	 * Return a stopwatch if we are measuring page response times, otherwise return a null timer that does nothing
	 * 
	 * @return
	 */
	public static StopWatch getTimer(){
		if(COLLECT_STATS) return new StopWatch();
		else return GlobalVars.NULL_TIMER;
	}
	
	public static Date parseDateMMYY(String source) throws ParseException{
		synchronized(GlobalVars.SDF_MMYY){
			return GlobalVars.SDF_MMYY.parse(source);
		}
	}
	
	public static Date parseDateFull(String source) throws ParseException{
		synchronized(GlobalVars.SDF_FULL){
			return GlobalVars.SDF_FULL.parse(source);
		}
	}
	
	public static String formatDateFull(Date date){
		synchronized(GlobalVars.SDF_FULL){
			return GlobalVars.SDF_FULL.format(date);
		}
	}
}
