package client.clientMain;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
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
		return new StringBuilder(appURL);
	}
	/**
	 * Gets the port number the website is running on
	 * @return
	 */
	public int getAppPort(){
		return appPort;
	}
	
	public URI build(String page, Map<String, String> data) throws URISyntaxException, UnsupportedEncodingException{
		if(RunSettings.isVerbose()){
			System.out.println("\t\t\tCMARTurl.build()");
			System.out.println("\t\t\t"+page);
			System.out.println("\t\t\t"+data);
		}
		
		StringBuilder uri = new StringBuilder("http://" + ipURL.toString() + ":" + appPort + page);
		for (Entry<String, String> entry : data.entrySet()) {
			uri.append(entry.getKey()+"=" + entry.getValue()+"&");
		}
		
		if(uri.charAt(uri.length()-1) == '&'){
			uri.deleteCharAt(uri.length()-1);
		}
		
		return URI.create(uri.toString());
//		URIBuilder builder = new URIBuilder();
//		builder.setScheme("http");
//		builder.setHost(ipURL.toString());
//		builder.setPort(appPort);
//		String[] strings = page.split("\\?");
//		builder.setPath(strings[0]);
//		if(strings.length > 1){
//			String[] params = strings[1].split("&");
////			System.err.println(Arrays.asList(params));
//			for (String param : params) {
//				String[] p = param.split("=");
////				if(p[0].equals("authToken")){
////					builder.addParameter(p[0], p[1]);
//////					System.err.println(p[1]);
////				}else{
//					System.err.println(Arrays.toString(p));
//					builder.addParameter(p[0], URLEncoder.encode(p[1], "UTF-8"));
////				}
//			}
//		}
//		for (Entry<String, String> entry : data.entrySet()) {
//			builder.addParameter(entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8"));
//		}
//		URI uri = builder.build();
//		if(RunSettings.isVerbose()){
//			System.out.println("\t\t\t"+uri);
//		}
//		return uri;
	}

	public URI build(String page) throws UnsupportedEncodingException, URISyntaxException {
		return build(page, new HashMap<String, String>());
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, URISyntaxException {
		CMARTurl cmarTurl = new CMARTurl(new StringBuilder("http://10.1.0.11:80/cmart-1"));
		System.out.println(cmarTurl.build("/cmart-1/viewitem?userID=89926&authToken=[B@15a7b655&itemID=41544"));
		System.out.println(URI.create("http://10.1.0.11:80/cmart-1/viewitem?userID=89926&authToken=[B@15a7b655&itemID=41544"));
	}
	
}
