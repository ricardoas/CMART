package client.Tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

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

	private final String outputFileLocation;
	private final boolean printOutput;
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
	 * @param printOutput
	 *            TODO
	 */
	public SiteData(String outputFileLocation, boolean printOutput, CMARTurl cmarturl) {
		this.outputFileLocation = outputFileLocation.concat("siteData.csv");
		this.printOutput = printOutput;
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
			
			HttpClientBuilder builder = HttpClientBuilder.create();
			builder.setConnectionManager(new BasicHttpClientConnectionManager());
			try(CloseableHttpClient client = builder.build();
					CloseableHttpResponse response = client.execute(new HttpGet(cmarturl.build(cmarturl.getAppURL().append("/statistics").toString())));
				){
				if (printOutput) {
					StringBuilder content = new StringBuilder();
					try(Scanner scanner = new Scanner(response.getEntity().getContent());){
						while(scanner.hasNextLine()){
							content.append(scanner.nextLine());
						}
						content.delete(0, content.indexOf("Browse Category"));
						content.delete(content.indexOf("</div>"), content.length());
						outputStats(content);
					}
				}else{
					EntityUtils.consume(response.getEntity());
				}
				return;
			} catch (IOException | URISyntaxException e) {
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
