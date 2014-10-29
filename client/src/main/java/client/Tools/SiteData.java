package client.Tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import client.clientMain.CMARTurl;

/**
 * Used to get time distributions from CMART
 * Gets a histogram describing how long it took
 * to access and generate the different components
 * of each page
 * If desired, outputs results to csv
 * @author Andrew Fox
 *
 */

public class SiteData {
	StringBuilder ret=new StringBuilder();	// the html returned from the statistics page
	String outputFileLocation;				// the location to output the csv file
	boolean toOutput=false;					// if the csv file is to be output
	CMARTurl cmarturl;

	/**
	 * Accesses the stats page to reset it
	 * Does not save data
	 */
	public SiteData(CMARTurl cmarturl){	
		this.cmarturl=cmarturl;
	}

	/**
	 * Accesses the stats page and outputs the results to csv
	 * @param outputFileLocation - location that the csv file is to be saved to
	 */
	public SiteData(String outputFileLocation, CMARTurl cmarturl){
		this.outputFileLocation=outputFileLocation.concat("siteData.csv");
		toOutput=true;
		this.cmarturl=cmarturl;
	}

	/**
	 * Retrieves the HTML from the stats page
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void collectStats() throws UnknownHostException, IOException{
		String inputLine;
		int attempts=0;

		// form new socket to access stats page
		while(attempts<3){
			try{
				Socket socket=new Socket(cmarturl.getIpURL().toString(),cmarturl.getAppPort());
				PrintStream out=new PrintStream(socket.getOutputStream());
				out.println(new StringBuilder("GET ").append(cmarturl.getAppURL()).append("/statistics HTTP/1.0\r\n"));
				out.flush();

				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while((inputLine=br.readLine())!=null){
					ret.append(inputLine);	// creates the response
				}

				ret.delete(0, ret.indexOf("Browse Category"));
				ret.delete(ret.indexOf("</div>"),ret.length());
				if (toOutput)		// output the results if desired
					outputStats();
				break;
			}catch(ConnectException e){
				System.err.println("Could not connect");
				attempts++;
			}catch(SocketException e){
				System.err.println("Could not connect");
				attempts++;
			}
		}
	}

	/**
	 * Outputs the stats results to the csv file
	 * @throws IOException
	 */
	public void outputStats() throws IOException{
		try{
			FileWriter fstreamA = new FileWriter(outputFileLocation,true);
			BufferedWriter out = new BufferedWriter(fstreamA);

			out = new BufferedWriter(fstreamA);
			while(ret.length()>0){
				int brIndex=ret.indexOf("<br>");
				if(brIndex!=0){
					out.write(ret.charAt(0));
					ret.deleteCharAt(0);
				}
				else{
					out.newLine();
					ret.delete(0,"<br>".length());
				}
			}
			out.close();
			fstreamA.close();
		}catch(Exception e){
			System.err.println("Could not output site data");
		}

	}

}
