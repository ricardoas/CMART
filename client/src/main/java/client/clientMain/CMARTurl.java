package client.clientMain;

public class CMARTurl {

	private StringBuffer fullURL;			// full URL of the website
	private StringBuffer hostURL;			// URL of the host
	private StringBuffer ipURL;				// IP address of the website
	private StringBuffer appURL;			// application name
	private int appPort;					// port the website is running on

	public CMARTurl(StringBuffer fullURL){
		try{
			this.fullURL=new StringBuffer(fullURL);
			ipURL=new StringBuffer(fullURL.subSequence(fullURL.indexOf("http://")+"http://".length(),fullURL.lastIndexOf(":")));
			int start=fullURL.lastIndexOf(":")+1;
			int end=fullURL.indexOf("/",start);
			if(start!=0 && end!=-1 && end>start)
				appPort=Integer.parseInt(fullURL.substring(start,end));
			else
				appPort = 80;
			appURL=new StringBuffer(fullURL.substring(end));
			hostURL=new StringBuffer(fullURL).delete(fullURL.lastIndexOf(":"),fullURL.length());
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
	public StringBuffer getFullURL(){
		return fullURL;
	}
	/**
	 * Returns the host URl of the website
	 * @return
	 */
	public StringBuffer getHostURL(){
		return hostURL;
	}
	/**
	 * Returns the IP address of the website
	 * @return
	 */
	public StringBuffer getIpURL(){
		return ipURL;
	}
	/**
	 * Returns the name of the application
	 * @return
	 */
	public StringBuffer getAppURL(){
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
