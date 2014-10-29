package client.clientMain;

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
	public StringBuilder getIpURL(){
		return ipURL;
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

}
