package client.clientMain;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.utils.URIBuilder;

public class CMARTurl {

	private StringBuilder fullURL;			// full URL of the website
	private StringBuilder hostURL;			// URL of the host
	private StringBuilder ipURL;				// IP address of the website
	private StringBuilder appURL;			// application name
	private int appPort;					// port the website is running on

	public CMARTurl(StringBuilder fullURL){
		try{
			this.fullURL=new StringBuilder(fullURL);
			ipURL=new StringBuilder(fullURL.subSequence(fullURL.indexOf("http://")+"http://".length(),fullURL.lastIndexOf(":")));
			int start=fullURL.lastIndexOf(":")+1;
			int end=fullURL.indexOf("/",start);
			if(start!=0 && end!=-1 && end>start)
				appPort=Integer.parseInt(fullURL.substring(start,end));
			else
				appPort = 80;
			appURL=new StringBuilder(fullURL.substring(end));
			hostURL=new StringBuilder(fullURL).delete(fullURL.lastIndexOf(":"),fullURL.length());
		}
		catch(Exception e){
			System.err.println("Make sure you always include the port number with the site URL");
			System.err.println("e.g. http://www.mysite.com:80/cmart");
			throw e;
		}
	}

	/**
	 * Returns the full url of the website
	 * @return
	 */
	public StringBuilder getFullURL(){
		return fullURL;
	}
	/**
	 * Returns the host URl of the website
	 * @return
	 */
	public StringBuilder getHostURL(){
		return hostURL;
	}
	/**
	 * Returns the IP address of the website
	 * @return
	 */
	@Deprecated
	public StringBuilder getIpURL(){
		return ipURL;
	}
	/**
	 * Returns the IP address of the website
	 * @return
	 */
	public String getIpURLString(){
		return ipURL.toString();
	}
	/**
	 * Returns the name of the application
	 * @return
	 */
	public StringBuilder getAppURL(){
		return appURL;
	}
	/**
	 * Gets the port number the website is running on
	 * @return
	 */
	public int getAppPort(){
		return appPort;
	}
	
	public URI build(String page, Map<String, String> data) throws URISyntaxException, UnsupportedEncodingException{
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http");
		builder.setHost(ipURL.toString());
		builder.setPort(appPort);
		builder.setPath(appURL.toString() + page);
		for (Entry<String, String> entry : data.entrySet()) {
			builder.addParameter(entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8"));
		}
		return builder.build();
	}

	public URI build(String page) throws UnsupportedEncodingException, URISyntaxException {
		return build(page, new HashMap<String, String>());
	}
	
}
