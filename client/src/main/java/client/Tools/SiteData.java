package client.Tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import client.clientMain.CMARTurl;

/**
 * Used to get time distributions from CMART Gets a histogram describing how
 * long it took to access and generate the different components of each page If
 * desired, outputs results to csv
 *
 * @author Andrew Fox
 * @author Ricardo Ara&uacute;jo Santos - ricardo@lsd.ufcg.edu.br (revised in
 *         08/01/2014)
 *
 */
public class SiteData {

	private final String outputFileLocation; // the location to output the csv
	// file
	private final boolean toOutput; // if the csv file is to be output
	private final CMARTurl cmarturl;

	/**
	 * Accesses the stats page to reset it Does not save data
	 */
	public SiteData(CMARTurl cmarturl) {
		this("", false, cmarturl);
	}

	/**
	 * Accesses the stats page and outputs the results to csv
	 *
	 * @param outputFileLocation
	 *            - location that the csv file is to be saved to
	 * @param toOutput
	 *            TODO
	 */
	public SiteData(String outputFileLocation, boolean toOutput, CMARTurl cmarturl) {
		this.outputFileLocation = outputFileLocation.concat("siteData.csv");
		this.toOutput = true;
		this.cmarturl = cmarturl;
	}

	/**
	 * Retrieves the HTML from the stats page
	 *
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void collectStats() {
		int attempts = 0;

		// form new socket to access stats page
		while (attempts++ < 3) {
			try (Socket socket = new Socket(this.cmarturl.getIpURLString(), this.cmarturl.getAppPort());
					PrintStream out = new PrintStream(socket.getOutputStream());
					BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {

				out.println(new StringBuilder("GET ").append(this.cmarturl.getAppURL()).append("/statistics HTTP/1.0\r\n"));
				out.flush();

				StringBuilder ret = new StringBuilder(); // the html returned
				// from the
				// statistics page

				String inputLine;
				while ((inputLine = br.readLine()) != null) {
					ret.append(inputLine); // creates the response
				}

				ret.delete(0, ret.indexOf("Browse Category"));
				ret.delete(ret.indexOf("</div>"), ret.length());
				if (this.toOutput) {
					outputStats(ret);
				}
				return;
			} catch (IOException e) {
				System.err.println("Problem collecting SiteData. Attempt:" + attempts);
				e.printStackTrace();
			}
		}
		System.err.println("Problem collecting SiteData. Max attempt reached!");
	}

	/**
	 * Outputs the stats results to the csv file
	 *
	 * @param ret
	 * @throws IOException
	 */
	private void outputStats(StringBuilder ret) {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(this.outputFileLocation, true));) {
			while (ret.length() > 0) {
				int brIndex = ret.indexOf("<br>");
				if (brIndex != 0) {
					out.write(ret.charAt(0));
					ret.deleteCharAt(0);
				} else {
					out.newLine();
					ret.delete(0, "<br>".length());
				}
			}
		} catch (Exception e) {
			System.err.println("Could not output site data");
			e.printStackTrace();
		}
	}

}
