package client.Pages;

import static client.Pages.PageType.BROWSE_PAGE_NUM;
import static client.Pages.PageType.BUYITEM_PAGE_NUM;
import static client.Pages.PageType.HOME_PAGE_NUM;
import static client.Pages.PageType.ITEM_PAGE_NUM;
import static client.Pages.PageType.LOGIN_PAGE_NUM;
import static client.Pages.PageType.LOGOUT_PAGE_NUM;
import static client.Pages.PageType.MYACCOUNT_PAGE_NUM;
import static client.Pages.PageType.REGISTER_PAGE_NUM;
import static client.Pages.PageType.SEARCH_PAGE_NUM;
import static client.Pages.PageType.SELLITEM_PAGE_NUM;
import static client.Pages.PageType.UPDATEUSER_PAGE_NUM;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import client.Items.ItemCG;
import client.Items.QuestionCG;
import client.Tools.DateParser;
import client.Tools.PageTimePair;
import client.Tools.Stopwatch;
import client.clientMain.Client;
import client.clientMain.RunSettings;

/**
 * Implements a General Page Type, specific pages are extended from this
 * @author Andrew Fox
 *
 */

public class Page {
	StringBuilder url;		// URL of current page
	StringBuilder html;		// HTML (or JSON) of current page
	StringBuilder lastURL;	// url of previously accessed page
	int pageType;			// page type of the page
	int lastPageType;		// page type of the previous page
	Client client;			// client on the page
	boolean verbose=RunSettings.isVerbose();// if verbose is used
	boolean HTML4=RunSettings.isHTML4();	// if HTML4 or HTML5 is being used
	double initialThinkTime=1500;			// initial minimum think time mean for all pages (expDist)
	long pageOpenTime=0;					// time that the page is opened (used to subtract code run time from think time)
	long responseTime;						// response time of the page	
	int activeConnections=0;				// number of active TCP connections opened from this page
	int httpRequestAttempts=0;				// number of times the client has attempted to make an http request (for failed requests)
	HashMap<String, StringBuilder> searchData=new HashMap<String, StringBuilder>();		// data used for the search form
	double numTabsMean=8.0;					// average number of tabs to be opened
	int typingErrorThinkTime=0;				// think time added from typing errors
	ArrayList<String>searchTermWords=new ArrayList<String>();	// list of words in a search query
	ArrayList<String>imagesOnPage=new ArrayList<String>();		// list of the images on the page
	ExecutorService threadExecutor = Executors.newFixedThreadPool(RunSettings.getConnPerPage());		// thread pool for css/js/image connections
	Random rand=new Random();		// Random seed for the page
	private ArrayList<String> allPagesImages=new ArrayList<String>();	// list of images from JS and CSS used on all pages
	private ArrayList<String> itemPageImages=new ArrayList<String>();	// list of images from JS and CSS used on Item page
	int pageThinkTime;				// thinkTime of the page
	Document xmlDocument;			// Client xmlDocument either being read or written to from the page
	Element action;					// action for the client in the xmlDocument
	Element request;				// request in the action

	// strings used in pages
	private static final String HTML_TEXT="<HTML>";
	private static final String JPG_TEXT=".jpg";
	private static final String JPG_TEXTQ=".jpg\"";
	private static final String PNG_TEXT=".png";
	private static final String PNG_TEXTQ=".png\"";
	private static final String CSS_TEXT=".css";
	private static final String CSS_TEXTQ=".css\"";
	private static final String JS_TEXT=".js";
	private static final String JS_TEXTQ=".js\"";
	private static final String TITLE_TEXT="<TITLE>";
	private static final String LOCAL_IMAGE="/img3/";
	private static final String NET_IMAGE="/netimg2/";
	protected static final String NAME_TEXT=" name=\"";
	protected static final String VALUE_TEXT="value=\"";
	protected static final String IMAGE_LABEL="<label for=\"image";
	protected static final String HOME_TEXT="Home";
	protected static final String MY_ACCOUNT_TEXT="My Account";
	protected static final String SEARCH_TEXT="Search";
	protected static final String SELL_TEXT="Sell";
	protected static final String BROWSE_TEXT="Browse";
	protected static final String UPDATE_DETAILS_TEXT="Update details";
	protected static final String LOGOUT_TEXT="Logout";
	protected static final String LOGIN_TEXT="Login";
	protected static final String REGISTER_TEXT="Register";
	protected static final String HTTP_RESPONSE_ERROR="HTTP_RESPONSE_ERROR";
	protected static final String CLOSE_TAB="CLOSE_TAB";
	protected static final String ASK_QUESTION_TEXT="Ask Question";
	protected static final String LEAVE_COMMENT_TEXT="Leave Comment";
	protected static final String ANSWER_QUESTION_TEXT="Answer Question";


	/**
	 * Declares a new general page type and opens the page
	 * It is possible that the URL is actually the HTML of this page, if this is the case a new page is not opened
	 * @param url - url of page
	 * @param lastURL - url of last page
	 * @param client - client opening page
	 */
	public Page(StringBuilder url, int lastPageType, StringBuilder lastURL, Client client) throws UnknownHostException, IOException, InterruptedException, URISyntaxException{
		this.url = url;
		this.lastPageType = lastPageType;
		this.lastURL = lastURL;
		this.client = client;

		xmlDocument = client.getXMLDocument();

		String urlString = url.toString();
		if (isURI(urlString)) {
			if (HTML4) {
				this.html = openURL(url);
			} else {
				StringBuilder rootURL = new StringBuilder();
				if (urlString.contains("/myaccount?")) {
					rootURL = new StringBuilder(client.getCMARTurl().getAppURL()).append("/myaccount.html");
				} else if (urlString.contains("/browsecategory?")) {
					rootURL = new StringBuilder(client.getCMARTurl().getAppURL()).append("/browse.html");
				} else if (urlString.contains("/search?")) {
					rootURL = new StringBuilder(client.getCMARTurl().getAppURL()).append("/search.html");
				} else if (urlString.contains("/updateuserdetails?")) {
					rootURL = new StringBuilder(client.getCMARTurl().getAppURL()).append("/updateuserdetails.html");
				} else if (urlString.contains("/viewitem?") || urlString.contains("ITEM")) {
					rootURL = new StringBuilder(client.getCMARTurl().getAppURL()).append("/viewitem.html");
				} else if (urlString.contains("/index.html") && client.isLoggedIn()) {
					rootURL = url;
					url = new StringBuilder(client.getCMARTurl().getAppURL()).append("/index?userID=")
							.append(client.getClientInfo().getHTML5Cache().get("userID")).append("&authToken=")
							.append(client.getClientInfo().getHTML5Cache().get("authToken")).append("&getRecommendation=1&recommendationPageNo=0");
				} else {
					rootURL = url;
					url = null;
				}
				this.html = openHTML5Page(rootURL, url);
			}
		} else {
			this.html = url;
		}
		
		pageOpenTime = System.currentTimeMillis(); // time the page is opened
		this.pageType = getPageType(); // gets the page type
		if (verbose){
			System.out.println("User: " + client.getClientInfo().getUsername() + " - Page Type: " + this.pageType);
		}

		int start = html.indexOf("$(window).load(function(){preloadImages([");
		if (start != -1) {
			start += "$(window).load(function(){preloadImages([".length();
			int end = html.indexOf("]", start);
			if (end != start + 1) {
				new GetPrefetchImage(html.substring(start, end)).start();
			}
		}
	}

	/**
	 * Check if a {@link String} is page content (HTML) or {@link URI} 
	 * @param string {@link String} to check.
	 * @return 
	 */
	private boolean isURI(String string) {
		return !string.startsWith(HTML_TEXT) && !string.startsWith("NEXTLINK") && !string.startsWith("{\"");
	}

	/**
	 * Declares the page when the specific page type is declared
	 * @param url - url of page
	 * @param html - html of the current page
	 * @param client - client opening page
	 * @param pageType - the page type of the page being declared
	 * @param pageOpenTime - time that the page was opened
	 * @param lastURL - url of previous page
	 */
	public Page(StringBuilder url,StringBuilder html,Client client, int pageType, long pageOpenTime,int lastPageType, StringBuilder lastURL){
		this.url=url;
		this.html=html;
		this.client=client;
		this.pageType=pageType;
		this.pageOpenTime=pageOpenTime;
		this.lastPageType=lastPageType;
		this.lastURL=lastURL;

		allPagesImages.add("images/misc/button-gloss.png");
		itemPageImages.add("js/themes/light/pointer.png");
		itemPageImages.add("js/themes/light/next.png");
		itemPageImages.add("js/themes/light/prev.png");

		xmlDocument=client.getXMLDocument();

	}

	/**
	 * Gets the page type number of the current page
	 * @return pageType
	 */
	public int getPageType() throws JsonParseException, JsonMappingException, IOException{
		return getPageType(html);
	}
	
	/**
	 * Determines what type of page the page is
	 * Uses the page number index defined in Page.java
	 * @return The page type index
	 */
	public int getPageType(StringBuilder content) throws JsonParseException, JsonMappingException, IOException{
		if(content.indexOf(HTTP_RESPONSE_ERROR)!=-1){
			return PageType.NONE.getCode();
		}
		
		int start=content.indexOf(TITLE_TEXT)+(TITLE_TEXT).length();
		int end=content.indexOf("</TITLE>",start);
		
		// the title of the page (HTML4)
		String title = (end != -1) ? content.substring(start, end) : null;
		
		if (!HTML4) {
			String htmlS = content.toString();
			if (htmlS.equals("PAGE CACHED")) {
				String urlString = this.url.toString();

				if (urlString.contains("/viewitem.html")) {
					return ITEM_PAGE_NUM.getCode();
				} else if (urlString.contains("/sell.html")) {
					return SELLITEM_PAGE_NUM.getCode();
				} else if (urlString.contains("/logout.html")) {
					return LOGOUT_PAGE_NUM.getCode();
				} else if (urlString.contains("/index.html")) {
					return HOME_PAGE_NUM.getCode();
				}
			} else if (htmlS.startsWith("NEXTLINK")) {
				// for the NEXTLINK commands, get out the page type
				title = content.substring(html.indexOf("NEXTLINK") + 8);
				// Find out by title
			} else if (htmlS.contains("<?xml version=\"1.0\"  encoding=\"UTF-8\" ?><recommendation>")) {
				return HOME_PAGE_NUM.getCode();
			} else if (htmlS.startsWith("ITEM")) {
				return ITEM_PAGE_NUM.getCode();
			} else if (htmlS.startsWith("{")) {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode node = mapper.readValue(htmlS, JsonNode.class);
				String pageTypeJSON = node.get("pageType").getTextValue();
				if (pageTypeJSON.equals("login")) {
					return LOGIN_PAGE_NUM.getCode();
				} else if (pageTypeJSON.equals("viewitem")) {
					return ITEM_PAGE_NUM.getCode();
				} else if (pageTypeJSON.equals("register")) {
					return REGISTER_PAGE_NUM.getCode();
				} else if (pageTypeJSON.equals("myaccount")) {
					return MYACCOUNT_PAGE_NUM.getCode();
				} else if (pageTypeJSON.equals("sell")) {
					return SELLITEM_PAGE_NUM.getCode();
				} else if (pageTypeJSON.equals("browse")) {
					return BROWSE_PAGE_NUM.getCode();
				} else if (pageTypeJSON.equals("search")) {
					return SEARCH_PAGE_NUM.getCode();
				} else if (pageTypeJSON.equals("updateuserdetails")) {
					return UPDATEUSER_PAGE_NUM.getCode();
				} else if (pageTypeJSON.equals("buyitem")) {
					return BUYITEM_PAGE_NUM.getCode();
				}
			}
		}

		return PageType.getBasedOnTitle(title).getCode();
	}

	/**
	 * Converts the general page to the specific page type
	 * @return Specific Page
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public Page toPageType() throws ParseException, JsonParseException, JsonMappingException, IOException{
		return pageType == 0? this: PageType.values()[pageType].buildPage(this);
	}


	/**
	 * Opens a C-MART page for HTML4
	 * @param urlString - URL of page to be opened
	 * @return HTML of the response from C-MART
	 * @throws UnsupportedEncodingException 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	protected StringBuilder openURL(StringBuilder urlString) throws UnsupportedEncodingException, URISyntaxException {
		StringBuilder ret = new StringBuilder(); // the source code of the page
		if (verbose){
			System.out.println("URLSTRING " + urlString);
		}

		URI uri = client.getCMARTurl().build(urlString.toString());
		HttpGet httpget = new HttpGet(uri);
		Stopwatch sw = new Stopwatch();

		try(CloseableHttpResponse response = client.getHttpClient().execute(httpget);
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));	// opens a BufferedReader to read the response of the HTTP request
				){
			
			String inputLine; // each line being read in
			while ((inputLine = br.readLine()) != null) {
				ret.append(inputLine);
			}
			
			if(RunSettings.isNetworkDelay()){
				try {
					Thread.sleep(client.getNetworkDelay());
				} catch (InterruptedException e) {
					//					return null;
				}
			}

			sw.pause();
			
			if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
				this.responseTime=sw.stop();	// stops the Stopwatch and determines the final response time
				client.getCg().getStats().getActiveHistogram().add(responseTime);	// adds the response time to the stats page
				client.addRT(responseTime);	// indexes the response time as the latest response time for the client
				client.incRequestErrors();
				client.incNumPagesOpened();
				client.incTotalRT(this.responseTime);

				if (httpRequestAttempts<3){
					httpRequestAttempts++;
					try {
						Thread.sleep((long) (expDist(1500) / RunSettings.getThinkTimeSpeedUpFactor()));
					} catch (InterruptedException e) {
					}
					return openURL(urlString);
				}
				threadExecutor.shutdown();
				
				System.err.println("Error (Status=" + response.getStatusLine().getStatusCode() + ") connecting (HTTP4) to: " + uri);
				client.setExit(true);
				client.setExitDueToError(true);
				return new StringBuilder(HTTP_RESPONSE_ERROR);
			}

			if(RunSettings.isGetExtras()){
				try {
					sw=getJsCssNew(ret,sw);
					sw=getImagesNew(ret,sw);
				} catch (InterruptedException e) {
				}
			}

			sw.start();
			if(urlString.indexOf("index?")!=-1&&client.isLoggedIn()){
				client.setMessage(openAJAXRequest(new StringBuilder(urlString).append("&getRecommendation=1&recommendationPageNo=0")).toString());
			}

			this.responseTime=sw.stop();	// stops the Stopwatch and determines the final response time
			client.getCg().getStats().getActiveHistogram().add(responseTime);	// adds the response time to the stats page
			client.getCg().addPageRT(getPageType(ret), responseTime);	// saves the response time for the specific page
			client.addRT(responseTime);	// indexes the response time as the latest reponse time for the client
			client.incTotalRT(responseTime);
			client.incNumPagesOpened();

			threadExecutor.shutdown();
			if(verbose){
				System.out.println("RET "+ret);
			}

			return ret;
		} catch (IOException e) {
			System.err.println("Could not connect (HTTP4) to " + uri);
			e.printStackTrace();
			client.incRequestErrors();
			httpRequestAttempts++;
			if (httpRequestAttempts < 3){
				return openURL(urlString);
			}
			threadExecutor.shutdown();
			client.setExit(true);
			client.setExitDueToError(true);
			return new StringBuilder(HTTP_RESPONSE_ERROR);
		}
	}


	/**
	 * Opens a C-MART HTML5 page
	 * @param urlString - url of the HTML page
	 * @param scriptString - url of the AJAX request used to populate the HTML page with data
	 * @return Returns the response from the scriptString url AJAX request
	 */
	protected StringBuilder openHTML5Page(StringBuilder urlString,StringBuilder scriptString) throws UnknownHostException, IOException, InterruptedException{
		PageTimePair finalPagePair=new PageTimePair(null,null);
		boolean error=false;

		if(urlString!=null&&urlString.length()>0){
			if(verbose)System.out.println("OpenHTML5Page URLString "+urlString);
			String urlRoot=urlString.substring(0,urlString.indexOf(".html")+5);
			if(client.getClientInfo().inHTML5Cache(urlRoot)==false){
				finalPagePair=openURLHTML5(urlString,finalPagePair.getSw());
				if(finalPagePair.getPage().toString().equals(HTTP_RESPONSE_ERROR)==true){
					error=true;
				}
				else{
					client.getClientInfo().addHTML5Cache(urlRoot,finalPagePair.getPage());
				}
			}
			else{
				finalPagePair.setPage(new StringBuilder(client.getClientInfo().getHTML5Cache().get(urlRoot)));
			}
		}

		if(!client.getClientInfo().getHTML5Cache().containsKey("populated")){
			StringBuilder savePage=new StringBuilder(finalPagePair.getPage());
			finalPagePair=openURLHTML5(new StringBuilder(client.getCMARTurl().getAppURL()).append("/getbulkdata?useHTML5=1"),finalPagePair.getSw());
			client.getClientInfo().addHTML5Cache("populated", new StringBuilder("1"));
			finalPagePair.setPage(savePage);
		}

		if(scriptString!=null&&scriptString.length()>0){
			if(scriptString.indexOf("ITEM")!=-1){
				if(finalPagePair.getSw()==null){
					Stopwatch sw=new Stopwatch();
					sw.pause();
					finalPagePair.setSw(getImagesNew(scriptString,sw));
				}
				else
					finalPagePair.setSw(getImagesNew(scriptString,finalPagePair.getSw()));
				finalPagePair.setPage(scriptString);
			}
			else if(error==false){
				finalPagePair=openURLHTML5(scriptString,finalPagePair.getSw());
			}
		}

		if(finalPagePair.getSw()==null)
			this.responseTime=0;
		else
			this.responseTime=finalPagePair.getSw().stop();	// stops the Stopwatch and determines the final response time
		client.getCg().getStats().getActiveHistogram().add(responseTime);	// adds the response time to the stats page
		client.getCg().addPageRT(getPageType(finalPagePair.getPage()), responseTime);	// saves the response time for the specific page
		client.addRT(responseTime);	// indexes the response time as the latest response time for the client
		client.incTotalRT(responseTime);
		client.incNumPagesOpened();

		threadExecutor.shutdown();
		//	httpclient.getConnectionManager().shutdown();

		return finalPagePair.getPage();
	}


	/**
	 * Opens an HTML5 page with knowledge that there will be a redirect from the C-MART server if request is sucessfully completed
	 * @param urlString - url of the requested page
	 * @return Returns the response as StringBuilder from the redirected page
	 */
	protected StringBuilder openHTML5PageWithRedirect(StringBuilder urlString){
		//		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		//		cm.setMaxTotal(100);
		//		HttpClient httpclient = new CloseableHttpClient(cm);
		PageTimePair 	finalPagePair=null;
		try{
			finalPagePair = openURLHTML5(urlString,null);


			client.clearErrors();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readValue(finalPagePair.getPage().toString(), JsonNode.class);
			//determine redirect
			if(urlString.indexOf("/viewitem")!=-1){
				boolean success=node.get("success").getBooleanValue();
				if(success==true){
					StringBuilder redirectURL=new StringBuilder(client.getCMARTurl().getAppURL());
					long itemID=node.get("item").get("id").getLongValue();
					ItemCG item=client.getClientInfo().getHTML5ItemCache().get(itemID);
					item.setBidder(true);
					client.getClientInfo().addHTML5ItemCache(item);
					client.addToItemsOfInterest(itemID, item);
					redirectURL.append("/confirmbid.html");
					if(client.getClientInfo().inHTML5Cache(redirectURL.toString())==false){
						finalPagePair=openURLHTML5(redirectURL,finalPagePair.getSw());
						client.getClientInfo().addHTML5Cache(redirectURL.toString(), finalPagePair.getPage());
					}
					else{
						finalPagePair.setPage(new StringBuilder(client.getClientInfo().getHTML5Cache().get(redirectURL.toString())));
					}

				}
				else{
					for(int i=0;i<node.get("errors").size();i++){
						client.addError(node.get("errors").get(i).get("errorMessage").getTextValue());
					}
					if(finalPagePair.getPage().indexOf("\"item\":\"null\"")==-1){
						long itemID=node.get("item").get("id").getLongValue();
						ItemCG item=client.getClientInfo().getHTML5ItemCache().get(itemID);
						item.setCurrentBid(node.get("item").get("currentBid").getDoubleValue());
						client.getClientInfo().addHTML5ItemCache(item);
						client.addToItemsOfInterest(itemID, item);
						//finalPagePair.setPage(new StringBuilder("ITEM").append(itemID));
					}
				}
			}
			else if(urlString.indexOf("/buyitem")!=-1&&urlString.indexOf("cvv=")==-1){
				boolean success=node.get("success").getBooleanValue();
				if(success==true){
					StringBuilder redirectURL=new StringBuilder(client.getCMARTurl().getAppURL());
					redirectURL.append("/buyitem.html");
					if(client.getClientInfo().inHTML5Cache(redirectURL.toString())==false){
						finalPagePair=openURLHTML5(redirectURL,finalPagePair.getSw());
						client.getClientInfo().addHTML5Cache(redirectURL.toString(),finalPagePair.getPage());
					}
					else{
						finalPagePair.setPage(new StringBuilder(client.getClientInfo().getHTML5Cache().get(redirectURL.toString())));
					}
				}
				else{
					for(int i=0;i<node.get("errors").size();i++){
						client.addError(node.get("errors").get(i).get("errorMessage").getTextValue());
					}
				}
			}
			else if(urlString.indexOf("/buyitem")!=-1&&urlString.indexOf("cvv=")!=-1){
				boolean success=node.get("success").getBooleanValue();
				if(success==true){
					StringBuilder redirectURL=new StringBuilder(client.getCMARTurl().getAppURL());
					redirectURL.append("/confirmbuy.html");
					if(client.getClientInfo().inHTML5Cache(redirectURL.toString())==false){
						finalPagePair=openURLHTML5(redirectURL,finalPagePair.getSw());
						client.getClientInfo().addHTML5Cache(redirectURL.toString(), finalPagePair.getPage());
					}
					else{
						finalPagePair.setPage(new StringBuilder(client.getClientInfo().getHTML5Cache().get(redirectURL.toString())));
					}
				}
				else{
					for(int i=0;i<node.get("errors").size();i++){
						client.addError(node.get("errors").get(i).get("errorMessage").getTextValue());
					}
				}
			}
			else if(urlString.indexOf("/sellitem")!=-1){
				boolean success=node.get("success").getBooleanValue();
				if(success==true){
					long itemId=node.get("itemid").getLongValue();
					client.setLastItemID(itemId);
					StringBuilder redirectURL=new StringBuilder(client.getCMARTurl().getAppURL());
					redirectURL.append("/sellitemimages.html?itemID=").append(itemId);
					finalPagePair=openURLHTML5(redirectURL,finalPagePair.getSw());
				}
				else{
					for(int i=0;i<node.get("errors").size();i++){
						client.addError(node.get("errors").get(i).get("errorMessage").getTextValue());
					}
				}
			}
			else if(urlString.indexOf("/login")!=-1){
				long userID=node.get("userID").getLongValue();
				if(userID!=-1){
					client.setLoggedIn(true);
					client.getClientInfo().addHTML5Cache("userID", new StringBuilder(Long.toString(userID)));
					client.setClientID(node.get("userID").getLongValue());
					StringBuilder redirectURL=new StringBuilder(client.getCMARTurl().getAppURL());
					redirectURL.append("/myaccount.html");
					if(client.getClientInfo().inHTML5Cache(redirectURL.toString())==false){
						finalPagePair=openURLHTML5(redirectURL,finalPagePair.getSw());
						client.getClientInfo().addHTML5Cache(redirectURL.toString(), finalPagePair.getPage());
					}
					HashMap<String,StringBuilder> redirectData=new HashMap<String,StringBuilder>();
					redirectData.put("useHTML5",new StringBuilder("1"));
					redirectData.put("userID", new StringBuilder(node.get("userID").getValueAsText()));
					redirectData.put("authToken", new StringBuilder(node.get("authToken").getValueAsText()));
					redirectData.put("ts",new StringBuilder(Long.toString(new Date().getTime())));

					client.getClientInfo().addHTML5Cache("userID", redirectData.get("userID"));
					client.getClientInfo().addHTML5Cache("authToken", new StringBuilder(node.get("authToken").getValueAsText()));
					finalPagePair=openURLHTML5(new StringBuilder(client.getCMARTurl().getAppURL()).append("/myaccount?").append(createURL(redirectData)),finalPagePair.getSw());
				}
				else{
					for(int i=0;i<node.get("errors").size();i++){
						client.addError(node.get("errors").get(i).get("errorMessage").getTextValue());
					}
				}
			}
			else if(urlString.indexOf("/registeruser")!=-1){
				boolean success=node.get("registeruser").getBooleanValue();
				if(success==true){
					client.setLoggedIn(true);
					client.getClientInfo().setRegistered(true);
					StringBuilder redirectURL=new StringBuilder(client.getCMARTurl().getAppURL());
					redirectURL.append("/myaccount.html");
					if(client.getClientInfo().inHTML5Cache(redirectURL.toString())==false){
						finalPagePair=openURLHTML5(redirectURL,finalPagePair.getSw());
						client.getClientInfo().addHTML5Cache(redirectURL.toString(), finalPagePair.getPage());
					}
					HashMap<String,StringBuilder> redirectData=new HashMap<String,StringBuilder>();
					client.setClientID(node.get("userID").getLongValue());
					redirectData.put("useHTML5",new StringBuilder("1"));
					redirectData.put("userID", new StringBuilder(node.get("userID").getValueAsText()));
					redirectData.put("authToken", new StringBuilder(node.get("authToken").getValueAsText()));
					redirectData.put("ts",new StringBuilder(Long.toString(new Date().getTime())));

					client.getClientInfo().addHTML5Cache("userID", redirectData.get("userID"));
					client.getClientInfo().addHTML5Cache("authToken", new StringBuilder(node.get("authToken").getValueAsText()));
					finalPagePair=openURLHTML5(new StringBuilder(client.getCMARTurl().getAppURL()).append("/myaccount?").append(createURL(redirectData)),finalPagePair.getSw());
				}
				else{
					for(int i=0;i<node.get("errors").size();i++){
						client.addError(node.get("errors").get(i).get("errorMessage").getValueAsText());
					}
				}
			}
			else if(urlString.indexOf("/updateuserdetails")!=-1){
				boolean success=node.get("success").getBooleanValue();
				if(success==true){
					StringBuilder redirectURL=new StringBuilder(client.getCMARTurl().getAppURL());
					redirectURL.append("/myaccount.html");
					if(client.getClientInfo().inHTML5Cache(redirectURL.toString())==false){
						finalPagePair=openURLHTML5(redirectURL,finalPagePair.getSw());
						client.getClientInfo().addHTML5Cache(redirectURL.toString(), finalPagePair.getPage());
					}
					HashMap<String,StringBuilder> redirectData=new HashMap<String,StringBuilder>();
					redirectData.put("useHTML5",new StringBuilder("1"));
					redirectData.put("userID", new StringBuilder(node.get("userID").getValueAsText()));
					redirectData.put("authToken", new StringBuilder(node.get("authToken").getValueAsText()));
					redirectData.put("ts", new StringBuilder(Long.toString(new Date().getTime())));

					finalPagePair=openURLHTML5(new StringBuilder(client.getCMARTurl().getAppURL()).append("/myaccount?").append(createURL(redirectData)),finalPagePair.getSw());
				}
				else{
					for(int i=0;i<node.get("errors").size();i++){
						client.addError(node.get("errors").get(i).get("errorMessage").getTextValue());
					}
				}
			}
			else if(urlString.indexOf("/askquestion")!=-1||urlString.indexOf("/answerquestion")!=-1){
				boolean success=node.get("success").getBooleanValue();
				if(success==true){
					QuestionCG question=new QuestionCG(node.get("question"));
					client.getClientInfo().addHTML5QuestionCache(question);
					StringBuilder redirectURL=new StringBuilder(client.getCMARTurl().getAppURL());
					redirectURL.append("/viewitem.html").append(client.getLastItemID());
					if(client.getClientInfo().inHTML5Cache(redirectURL.toString())==false){
						redirectURL.append("?").append(client.getLastItemID());
						finalPagePair=openURLHTML5(redirectURL,finalPagePair.getSw());
						client.getClientInfo().addHTML5Cache(redirectURL.toString(), finalPagePair.getPage());
					}
					HashMap<String,StringBuilder> redirectData=new HashMap<String,StringBuilder>();
					redirectData.put("useHTML5",new StringBuilder("1"));
					redirectData.put("itemID", new StringBuilder().append(client.getLastItemID()));

					if(client.getClientInfo().getHTML5ItemCache().get(client.getLastItemID()).getTs()>=(new Date().getTime()-300000))				
						finalPagePair.setPage(new StringBuilder("ITEM").append(client.getLastItemID()));
					else
						finalPagePair=openURLHTML5(new StringBuilder(client.getCMARTurl().getAppURL()).append("/viewitem?").append(createURL(redirectData)),finalPagePair.getSw());
				}
				else{
					for(int i=0;i<node.get("errors").size();i++){
						client.addError(node.get("errors").get(i).get("errorMessage").getTextValue());
					}
				}
			}


			if(finalPagePair.getSw()==null)
				this.responseTime=0;
			else
				this.responseTime=finalPagePair.getSw().stop();	// stops the Stopwatch and determines the final response time
			client.getCg().getStats().getActiveHistogram().add(responseTime);	// adds the response time to the stats page
			client.getCg().addPageRT(getPageType(finalPagePair.getPage()), responseTime);	// saves the response time for the specific page
			client.addRT(responseTime);	// indexes the response time as the latest response time for the client
			client.incTotalRT(responseTime);
			client.incNumPagesOpened();
			//	httpclient.getConnectionManager().shutdown();

		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.err.println(urlString);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(urlString);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.err.println(urlString);
		}catch (NullPointerException e) {
			e.printStackTrace();
			System.err.println(urlString);
		} catch (ParseException e) {
			e.printStackTrace();
			System.err.println(urlString);
		}

		return finalPagePair.getPage();
	}



	/**
	 * Opens a webpage in the HTML5 version.
	 * @param urlString url of the page to be opened
	 * @param sw stopwatch that is to be continued when timing the opening of the page
	 * @return PageTimePair of the resulting opened webpage and a stopwatch set to the original time plus the amount required to open the webpage
	 */
	private PageTimePair openURLHTML5(StringBuilder urlString, Stopwatch sw) throws UnknownHostException, IOException, InterruptedException{
		StringBuilder ret = new StringBuilder();		// the source code of the page
		PageTimePair finalPair;
		if(verbose)System.out.println("URLSTRING "+urlString);
		String inputLine;	// each line being read in
		String urlStringS=urlString.toString().replace(" ", "%20");
		if(sw==null)
			sw=new Stopwatch();
		else
			sw.start();

		try{
			URI uri=URIUtils.createURI("http", client.getCMARTurl().getIpURL().toString(), client.getCMARTurl().getAppPort(), urlStringS.replace(" ", "%20"), null, null);
			HttpGet httpget = new HttpGet(uri);
			HttpResponse response = client.getHttpClient().execute(httpget);
			HttpEntity entity = response.getEntity();
			BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));	// opens a BufferedReader to read the response of the HTTP request

			while((inputLine=br.readLine())!=null){
				//	System.out.println(inputLine);
				ret.append(inputLine);	// creates the response
			}

			if(RunSettings.isNetworkDelay()){
				try{Thread.sleep(client.getNetworkDelay());}
				catch(InterruptedException e){
					br.close();
					return null;
				}
			}
			sw.pause();	// pauses the timer determining page response time
			br.close();
			if(response.getStatusLine().getStatusCode()>=400){
				this.responseTime=sw.stop();	// stops the Stopwatch and determines the final response time
				client.getCg().getStats().getActiveHistogram().add(responseTime);	// adds the response time to the stats page
				client.addRT(responseTime);	// indexes the response time as the latest response time for the client
				client.incRequestErrors();
				client.incNumPagesOpened();
				client.incTotalRT(this.responseTime);

				if (httpRequestAttempts<3){
					httpRequestAttempts++;
					try{Thread.sleep((long) (expDist(1500)/RunSettings.getThinkTimeSpeedUpFactor()));}
					catch(InterruptedException e){
					}
					finalPair=openURLHTML5(urlString,sw);
					if (httpRequestAttempts<3)
						return finalPair;
				}
				threadExecutor.shutdown();
				//	httpclient.getConnectionManager().shutdown();
				System.err.println("HTTP request error: "+urlString);
				client.setExit(true);
				client.setExitDueToError(true);
				return new PageTimePair(new StringBuilder(HTTP_RESPONSE_ERROR),sw);

			}

			if(RunSettings.isGetExtras()){
				sw=getJsCssNew(ret,sw);		// Downloads the JS of any js files not already cached
				sw=getImagesNew(ret,sw);	// Downloads any jpg on the page that is not already in the image cache
			}

			sw.pause();

			//	out.close();
			//	br.close();
			//	socket.close();
			if(verbose)System.out.println("RET OPENURL: "+ret);

			return new PageTimePair(ret,sw);
		}catch(ConnectException e){
			System.err.println("Could not create connection");
			System.err.println(urlString);
			client.incRequestErrors();
			httpRequestAttempts++;
			finalPair=openURLHTML5(urlString,sw);
			if (httpRequestAttempts<3)
				return finalPair;
			threadExecutor.shutdown();
			//httpclient.getConnectionManager().shutdown();
			client.setExit(true);
			client.setExitDueToError(true);
			return new PageTimePair(new StringBuilder(HTTP_RESPONSE_ERROR),sw);
		}catch(SocketException e){
			System.err.println("Could not create connection");
			System.err.println(urlString);
			client.incRequestErrors();
			httpRequestAttempts++;
			finalPair=openURLHTML5(urlString,sw);
			if (httpRequestAttempts<3)
				return finalPair;
			threadExecutor.shutdown();
			//	httpclient.getConnectionManager().shutdown();
			client.setExit(true);
			client.setExitDueToError(true);
			return new PageTimePair(new StringBuilder(HTTP_RESPONSE_ERROR),sw);
		} catch (NoHttpResponseException e){
			System.err.println("Could not create connection");
			System.err.println(urlString);
			client.incRequestErrors();
			httpRequestAttempts++;
			finalPair=openURLHTML5(urlString,sw);
			if (httpRequestAttempts<3)
				return finalPair;
			threadExecutor.shutdown();
			//	httpclient.getConnectionManager().shutdown();
			client.setExit(true);
			client.setExitDueToError(true);
			return new PageTimePair(new StringBuilder(HTTP_RESPONSE_ERROR),sw);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			System.err.println("HTTP request error: "+urlString);
			return new PageTimePair(new StringBuilder(HTTP_RESPONSE_ERROR),sw);
		}
	}



	/**
	 * Creates a URL query in UTF-8 format out of the information from a HashMap
	 * @param data - map of the data needed to be turned into a query string
	 * @return The query in UTF-8 form
	 * @throws UnsupportedEncodingException
	 */
	protected String createURL(HashMap<String, StringBuilder> data) throws UnsupportedEncodingException{
		StringBuilder content = new StringBuilder();
		int i=0;

		for(Entry<String,StringBuilder> e:data.entrySet()){
			if(i!=0) {
				content.append("&");
			}
			content.append(e.getKey()).append("=").append(URLEncoder.encode(e.getValue().toString(), "UTF-8"));
			i++;
		}

		return content.toString();
	}


	/**
	 * Gets form data for the page
	 * Separate function from one used in Page class as there can be more than one form on the page
	 * @param formAction
	 * @return Returns a HashMap of the form data, (name,value) pairings
	 */
	protected HashMap<String, StringBuilder> getFormData(String formAction){
		String name;
		StringBuilder value =new StringBuilder();
		int start=0;
		HashMap<String, StringBuilder>data=new HashMap<String,StringBuilder>();
		start=html.indexOf("action=\""+formAction);
		if (start!=-1){
			start=html.indexOf("POST",start)+("POST").length();
			start=html.indexOf(NAME_TEXT,start)+(NAME_TEXT).length();

			int end=start;
			int formEnd=html.indexOf("</form>",start);
			while(start<formEnd&&start!=(NAME_TEXT.length()-1)&&end!=-1){
				end=html.indexOf("\"",start);
				name=html.substring(start,end);
				if(!name.startsWith("image")){
					if (html.charAt(end+1)=='>'){
						end=html.indexOf("\" SELECTED",end);
						if(end!=-1)
							start=html.lastIndexOf(VALUE_TEXT,end)+(VALUE_TEXT).length();
						else{
							value=new StringBuilder("0");
						}
					}
					else{
						start=html.indexOf(VALUE_TEXT,end)+(VALUE_TEXT).length();
						end=html.indexOf("\"",start);
					}

					if(end!=-1)
						value=new StringBuilder(html.subSequence(start,end));
					data.put(name,value);

				}
				start=html.indexOf(NAME_TEXT,end)+(NAME_TEXT).length();
			}
		}

		return data;
	}

	/**
	 * Applies a typing error to a string
	 * Only a 5% chance that the user makes an error and does not notice it.
	 * If the client makes an error and notices it, then the client pauses for the time it would take to type 4 characters
	 * @param str - original string
	 * @return string with typing errors (if any occured)
	 */
	protected StringBuilder typingError(StringBuilder str){
		str=new StringBuilder(str);
		double unnoticedError=0.05;
		for (int i=0;i<str.length();i++){
			if (rand.nextDouble()>(1-client.getTypingErrorRate())){
				if(rand.nextDouble()<unnoticedError){
					str=str.replace(i,i+1,Character.toString((char)(rand.nextInt(26)+'a')));
					//	if (verbose)System.out.println("User: "+client.getClientInfo().getUsername()+" - Typing Error Made");
				}
				else{
					typingErrorThinkTime+=(int)((1/client.getTypingSpeed())*4);
				}
			}
		}
		return str;
	}

	/**
	 * Submits a POST request for HTML4 C-MART
	 * @param url - url of the action of the form
	 * @param data - data in the form being posted
	 * @return HTML returned on the response from the post request
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected StringBuilder doSubmit(StringBuilder url,HashMap<String, StringBuilder> data) throws UnsupportedEncodingException, IOException, InterruptedException{
		threadExecutor = Executors.newFixedThreadPool(RunSettings.getConnPerPage());
		StringBuilder urlOrig=new StringBuilder(url);
		StringBuilder ret = new StringBuilder();		// the source code of the page
		if(verbose)System.out.println("URL "+url);
		String inputLine;	// each line being read in

		ArrayList<NameValuePair> formparams = new ArrayList<NameValuePair>();
		for (Entry<String, StringBuilder> e:data.entrySet()){
			formparams.add(new BasicNameValuePair(e.getKey(),e.getValue().toString()));
		}
		URI uri;
		try {
			uri = URIUtils.createURI("http", client.getCMARTurl().getIpURL().toString(), client.getCMARTurl().getAppPort(), url.toString().replace(" ", "%20"), null, null);

			UrlEncodedFormEntity entityPost = new UrlEncodedFormEntity(formparams, "UTF-8");
			HttpPost httppost = new HttpPost(uri);
			httppost.setEntity(entityPost);
			Stopwatch sw=new Stopwatch();	// starts the Stopwatch to time the response time
			HttpResponse response = client.getHttpClient().execute(httppost);

			HttpEntity entity = response.getEntity();
			BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));	// opens a BufferedReader to read the response of the HTTP request

			while((inputLine=br.readLine())!=null){
				//	System.out.println(inputLine);
				ret.append(inputLine);	// creates the response
			}
			if(RunSettings.isNetworkDelay()){
				try{Thread.sleep(client.getNetworkDelay());}
				catch(InterruptedException e){
					br.close();
					return null;
				}
			}
			sw.pause();
			br.close();
			if(response.getStatusLine().getStatusCode()>=400){
				this.responseTime=sw.stop();	// stops the Stopwatch and determines the final response time
				client.getCg().getStats().getActiveHistogram().add(responseTime);	// adds the response time to the stats page
				client.addRT(responseTime);	// indexes the response time as the latest reponse time for the client
				client.incRequestErrors();
				client.incNumPagesOpened();
				client.incTotalRT(this.responseTime);

				if (httpRequestAttempts<3){
					httpRequestAttempts++;
					try{Thread.sleep((long) (expDist(1500)/RunSettings.getThinkTimeSpeedUpFactor()));}
					catch(InterruptedException e){
					}
					ret=doSubmit(urlOrig,data);
					if (httpRequestAttempts<3)
						return ret;
				}
				threadExecutor.shutdown();
				//	httpclient.getConnectionManager().shutdown();
				System.err.println("HTTP request error: "+url+" "+data);
				client.setExit(true);
				client.setExitDueToError(true);
				return new StringBuilder(HTTP_RESPONSE_ERROR);
			}

			if(RunSettings.isGetExtras()){
				sw=getJsCssNew(ret,sw);		// Downloads the JS of any js files not already cached
				sw=getImagesNew(ret,sw);	// Downloads any jpg on the page that is not already in the image cache
			}
			this.responseTime=sw.stop();		// gets the final response time
			threadExecutor.shutdown();
			//	httpclient.getConnectionManager().shutdown();
			client.getCg().getStats().getActiveHistogram().add(responseTime);		// adds the response time to the stats
			client.getCg().addPageRT(getPageType(ret), responseTime);	// saves the response time for the specific page
			client.addRT(responseTime);	// adds the response time to the clients most recent response time
			client.incTotalRT(responseTime);
			client.incNumPagesOpened();
			if(verbose)System.out.println("RET "+ret);


		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (NoHttpResponseException e){
			System.err.println("Could not create connection");
			System.err.println(urlOrig);
			client.incRequestErrors();
			httpRequestAttempts++;
			ret=doSubmit(urlOrig,data);
			if (httpRequestAttempts<3)
				return ret;
			threadExecutor.shutdown();
			//httpclient.getConnectionManager().shutdown();
			client.setExit(true);
			client.setExitDueToError(true);
			return new StringBuilder(HTTP_RESPONSE_ERROR);
		}catch (SocketException e){
			System.err.println("Could not create connection");
			System.err.println(urlOrig);
			client.incRequestErrors();
			httpRequestAttempts++;
			ret=doSubmit(urlOrig,data);
			if (httpRequestAttempts<3)
				return ret;
			threadExecutor.shutdown();
			//httpclient.getConnectionManager().shutdown();
			client.setExit(true);
			client.setExitDueToError(true);
			return new StringBuilder(HTTP_RESPONSE_ERROR);
		}

		return ret;
	}




	/**
	 * Submits multipart form HTTP POST to upload pictures
	 * @param url - url of the form
	 * @param data - form data to submit in form (text)
	 * @param pics - pictures to be uploaded
	 * @return Returns the HTTP response body after the form is submitted
	 */
	protected StringBuilder doSubmitPic(String url,HashMap<String, StringBuilder> data,ArrayList<File> pics) throws org.apache.http.ParseException, IOException, InterruptedException{
		String urlOrig=url;
		String inputLine;
		StringBuilder ret=new StringBuilder();


		URI uri;
		try {
			uri = URIUtils.createURI("http", client.getCMARTurl().getIpURL().toString(), client.getCMARTurl().getAppPort(), url.replace(" ", "%20"), null, null);

			HttpPost httppost=new HttpPost(uri);
			MultipartEntity reqEntity = new MultipartEntity();
			for (Entry<String,StringBuilder> e:data.entrySet()){
				reqEntity.addPart(e.getKey(),new StringBody(e.getValue().toString()));
			}
			int n=1;
			for(File f:pics){
				reqEntity.addPart("image".concat(Integer.toString(n)),new FileBody(f));
				n++;
			}
			httppost.setEntity(reqEntity);

			Stopwatch sw=new Stopwatch();
			HttpResponse response = client.getHttpClient().execute(httppost);
			HttpEntity entity = response.getEntity();
			BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));	// opens a BufferedReader to read the response of the HTTP request

			while((inputLine=br.readLine())!=null){
				//	System.out.println(inputLine);
				ret.append(inputLine);	// creates the response
			}
			if(RunSettings.isNetworkDelay()){
				try{Thread.sleep(client.getNetworkDelay());}
				catch(InterruptedException e){
					br.close();
					return null;
				}
			}
			sw.pause();
			br.close();
			if(response.getStatusLine().getStatusCode()>=400){
				this.responseTime=sw.stop();	// stops the Stopwatch and determines the final response time
				client.getCg().getStats().getActiveHistogram().add(responseTime);	// adds the response time to the stats page
				client.addRT(responseTime);	// indexes the response time as the latest reponse time for the client
				client.incRequestErrors();
				client.incNumPagesOpened();
				client.incTotalRT(this.responseTime);

				if (httpRequestAttempts<3){
					httpRequestAttempts++;
					try{Thread.sleep((long) (expDist(1500)/RunSettings.getThinkTimeSpeedUpFactor()));}
					catch(InterruptedException e){
					}
					ret=doSubmitPic(urlOrig,data,pics);
					if (httpRequestAttempts<3)
						return ret;
				}
				threadExecutor.shutdown();
				//httpclient.getConnectionManager().shutdown();
				System.err.println("HTTP request error: "+url);
				client.setExit(true);
				client.setExitDueToError(true);
				return new StringBuilder(HTTP_RESPONSE_ERROR);
			}

			if(RunSettings.isGetExtras()){
				sw=getJsCssNew(ret,sw);		// Downloads the JS of any js files not already cached
				sw=getImagesNew(ret,sw);	// Downloads any jpg on the page that is not already in the image cache
			}

			if(!HTML4){	// redirect for HTML5
				if(ret.indexOf("\"success\":true,")!=-1){
					StringBuilder redirectURL=new StringBuilder(client.getCMARTurl().getAppURL());
					redirectURL.append("/confirmsellitem.html");
					PageTimePair finalPagePair=openURLHTML5(redirectURL,sw);
					ret=finalPagePair.getPage();
					sw=finalPagePair.getSw();
				}
			}

			this.responseTime=sw.stop();		// gets the final response time
			threadExecutor.shutdown();
			//httpclient.getConnectionManager().shutdown();
			client.getCg().getStats().getActiveHistogram().add(responseTime);		// adds the response time to the stats
			client.getCg().addPageRT(getPageType(ret), responseTime);	// saves the response time for the specific page
			client.addRT(responseTime);	// adds the response time to the clients most recent response time
			client.incTotalRT(responseTime);
			client.incNumPagesOpened();
			if(verbose)System.out.println("RET "+ret);

		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}catch (NoHttpResponseException e){
			System.err.println("Could not create connection");
			System.err.println(urlOrig);
			client.incRequestErrors();
			httpRequestAttempts++;
			ret=doSubmitPic(urlOrig,data,pics);
			if (httpRequestAttempts<3)
				return ret;
			threadExecutor.shutdown();
			//httpclient.getConnectionManager().shutdown();
			client.setExit(true);
			client.setExitDueToError(true);
			return new StringBuilder(HTTP_RESPONSE_ERROR);
		}

		return ret;
	}


	/**
	 * Submits AJAX request, returns XML, or String returned from AJAX request
	 * @param urlString - URL of AJAX request
	 * @return XML, or String returned from AJAX response
	 */
	protected StringBuilder openAJAXRequest(StringBuilder urlString){
		StringBuilder ret = new StringBuilder();		// the source code of the page
		if(verbose)System.out.println("AJAXURLSTRING "+urlString);
		String inputLine;	// each line being read in
		String urlStringS=urlString.toString().replace(" ", "%20");
		//Stopwatch sw=new Stopwatch();	// starts timing how long it takes to open the webpage

		try {
			URI uri=URIUtils.createURI("http", client.getCMARTurl().getIpURL().toString(), client.getCMARTurl().getAppPort(), urlStringS, null, null);
			HttpGet httpget = new HttpGet(uri);

			HttpResponse response = client.getHttpClient().execute(httpget);

			HttpEntity entity = response.getEntity();
			BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));	// opens a BufferedReader to read the response of the HTTP request

			while((inputLine=br.readLine())!=null){
				ret.append(inputLine);	// creates the response
			}
			if(RunSettings.isNetworkDelay()){
				try{Thread.sleep(client.getNetworkDelay());}
				catch(InterruptedException e){
					br.close();
					return null;
				}
			}

			if(RunSettings.isGetExtras()){
				if(urlString.indexOf("index?")!=-1)
					getImagesNew(ret,new Stopwatch());
			}

			br.close();
			if(verbose)System.out.println("RET AJAX OPENURL: "+ret);

		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			System.err.println(pageType);
			System.err.println(lastPageType);
			System.err.println(lastURL);
			return null;
		} catch (IllegalStateException e){
			e.printStackTrace();
			return null;
		}catch(NullPointerException e){
			e.printStackTrace();
			return null;
		}

		return ret;
	}



	/**
	 * Randomly selects a number from an exponential distribution
	 * @param mean - mean of the distribution
	 * @return Random Number from distribution
	 */
	protected double expDist(double mean) {
		return (-Math.log(1 - rand.nextDouble()) * mean);
	}

	/**
	 * Returns a randomly sampled value from a Weibull distribution given the input mean
	 * k=0.382, gamma=3.7931
	 */
	protected double weibull(double mean){
		double k=0.382;
		double gamma=3.7931;
		double lambda=mean/gamma;

		return Math.pow(-Math.pow(lambda,k)*Math.log(1-rand.nextDouble()),1/k);
	}



	/**
	 * Performs a binary search on a distribution. Looks for a value corresponding to a randomly chosen probability
	 * @param keyset the distribution
	 * @param value the value being searched for
	 * @param low the lower bound on the index of the distribution to search in
	 * @param high the higher bound on the index of the distribution to search in
	 * @return the value being searched for from the distribution
	 */
	private double binarySearch(Double[] keyset,double value, int low, int high){
		int mid=(int)(((long)low+(long)high)/2);
		if(low==high)
			return keyset[low];
		// if value too big, search lower half
		if(keyset[mid] > value)
			return binarySearch(keyset,value, low, mid);

		// if value too small search upper half
		else if(keyset[mid] < value)
			return binarySearch(keyset,value, mid+1, high);

		// If we got here they must be equal
		return value;
	}

	/**
	 * Returns a random string selected from a distribution
	 * @param map - a map of the distribution of form (probability, string)
	 * @return The randomly chosen string
	 */
	protected StringBuilder getRandomStringBuilderFromDist(TreeMap<Double,StringBuilder> map){

		double index=binarySearch((Double[])map.keySet().toArray(new Double[map.size()]),rand.nextDouble(),0,map.size()-1);

		StringBuilder ret=new StringBuilder(map.get(index));
		return ret;
	}

	/**
	 * Returns a random integer selected from a distribution
	 * @param map - a map of the distribution of form (probability, integer)
	 * @return The randomly chosen integer
	 */
	protected int getRandomIntFromDist(TreeMap<Double,Integer> map){
		double index=binarySearch((Double[])map.keySet().toArray(new Double[map.size()]),rand.nextDouble(),0,map.size()-1);

		int ret=map.get(index);

		return ret;
	}

	/**
	 * Returns a random long selected from a distribution
	 * @param map - a map of the distribution of form (probability, integer)
	 * @return The randomly chosen integer
	 */
	protected long getRandomLongFromDist(TreeMap<Double,Long> map){
		double index=binarySearch((Double[])map.keySet().toArray(new Double[map.size()]),rand.nextDouble(),0,map.size()-1);

		long ret=map.get(index);

		return ret;
	}

	/**
	 * Returns a random double selected from a distribution
	 * @param map - a map of the distribution of form (probability, double)
	 * @return The randomly chosen double
	 */
	protected double getRandomDoubleFromDist(TreeMap<Double,Double> map){

		double index=binarySearch((Double[])map.keySet().toArray(new Double[map.size()]),rand.nextDouble(),0,map.size()-1);

		double ret=map.get(index);

		return ret;
	}

	/**
	 * Adjusts the probability of logging out depending on past response times
	 * @param prob - the original logout probability
	 * @return the new logout probability
	 */
	protected double adjustLogOutProb(double prob){
		long avg=client.getRTavg();
		//long avg=client.getCg().getStats().getActiveHistogram().getPercentile(0.5);
		long threshold=client.getRTThreshold();
		if (avg>threshold){
			prob*=(((((double)avg)/((double)threshold))-1.)/(Math.log(2.*(double)client.getNumPagesOpened()+1.)))+1.;
		}
		return prob;
	}

	private class GetPrefetchImage extends Thread{
		String fullImageString;
		ExecutorService threadExecutor = Executors.newFixedThreadPool(RunSettings.getConnPerPage());		// thread pool for css/js/image connections

		private GetPrefetchImage(String fullImageString){
			this.fullImageString=fullImageString;
		}

		public void run(){
			int start=fullImageString.indexOf("'");
			int end=fullImageString.indexOf("'",start+1);
			while(start!=-1){
				start++;
				boolean inCache=false;
				if (client.getClientInfo().inImageCache(fullImageString.substring(start,end))==true){
					inCache=true;
				}
				String imgName=fullImageString.substring(start,end);

				GetImageNew gI=new GetImageNew(imgName,client,client.getHttpClient(),inCache);

				addConnection();
				//gI.start();
				try{
					threadExecutor.execute(gI);
				}
				catch(Exception e){
					System.err.println("STR: "+fullImageString.substring(start,end));
					threadExecutor.shutdownNow();
					e.printStackTrace();
					break;
				}

				start=fullImageString.indexOf("'",end+1);
				end=fullImageString.indexOf("'",start+1);
			}
			threadExecutor.shutdown();

		}

	}

	/**
	 * Gets the images off the page
	 * @author Andrew Fox
	 *
	 */
	private class GetImageNew extends Thread{
		String suffix;
		Client client;
		int size=3145728;
		int len;
		CloseableHttpClient httpclient;
		boolean inCache;

		private GetImageNew(String URLsuffix, Client client,CloseableHttpClient httpclient, boolean inCache){
			if(verbose)System.out.println("Get Image: "+URLsuffix);
			this.suffix=URLsuffix;
			this.client=client;
			this.httpclient=httpclient;
			this.inCache=inCache;
		}
		public void run(){
			int attempts=0;
			while(attempts<3){
				try {			
					URI uri = URIUtils.createURI("http", client.getCMARTurl().getIpURL().toString(), client.getCMARTurl().getAppPort(), new StringBuilder(client.getCMARTurl().getAppURL()).append("/").append(suffix).toString().replace(" ", "%20"), null, null);

					HttpGet httpget = new HttpGet(uri);

					HttpResponse response = httpclient.execute(httpget);

					HttpEntity entity = response.getEntity();

					if(inCache==true)
						httpget.addHeader("If-Modified-Since", DateParser.dateToString(new Date()));

					if(RunSettings.isNetworkDelay()){
						try{Thread.sleep(client.getNetworkDelay());}
						catch(InterruptedException e){
							break;
						}
					}

					if(response.getStatusLine().getStatusCode()==200){
						InputStream is=entity.getContent();


						byte[] buf=new byte[size];
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						//int origSize=0;
						while ((len = is.read(buf, 0, size)) != -1){
							//	origSize+=len;
							bos.write(buf, 0, len);
						}

						if(bos.size()>0){
							buf = bos.toByteArray();

							InputStream imgIS=new ByteArrayInputStream(buf);

							Image im=ImageIO.read(imgIS);
							if(verbose)System.out.println("Image "+suffix+" Loaded");
							//		synchronized(this){
							if(RunSettings.isCacheRealData())
								client.getClientInfo().addImageCache(suffix, im);
							else
								client.getClientInfo().addImageCache(suffix, null);
							//	}
						}
					}
					delConnection();	// indicates that this connection is finished						
					break;
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}catch(ConnectException e){
					System.err.println("Could not create connection");
					System.err.println(new StringBuilder(client.getCMARTurl().getAppURL()).append("/").append(suffix).toString().replace(" ", "%20"));
					attempts++;
					if (httpRequestAttempts==3){
						threadExecutor.shutdown();
						client.setExit(true);
						client.setExitDueToError(true);
					}
				} catch(SocketException e){
					System.err.println("Could not create connection");
					System.err.println(new StringBuilder(client.getCMARTurl().getAppURL()).append("/").append(suffix).toString().replace(" ", "%20"));
					attempts++;
					if (httpRequestAttempts==3){
						threadExecutor.shutdown();
						client.setExit(true);
						client.setExitDueToError(true);
					}
				}catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}

	}



	/**
	 * Gets JS files off the page
	 * @author Andrew Fox
	 *
	 */
	private class GetJSCSSNew extends Thread{
		String suffix;
		Client client;
		StringBuilder line=new StringBuilder();
		String newline;
		CloseableHttpClient httpclient;
		boolean inCache;
		private GetJSCSSNew(String URLsuffix, Client client,CloseableHttpClient httpclient,boolean inCache){
			if(verbose)System.out.println("Get JS: "+URLsuffix);
			this.suffix=URLsuffix;
			this.client=client;
			this.httpclient=httpclient;
			this.inCache=inCache;
		}
		public void run(){
			int attempts=0;
			while(attempts<3){
				try {			

					URI uri = URIUtils.createURI("http", client.getCMARTurl().getIpURL().toString(), client.getCMARTurl().getAppPort(), new StringBuilder(client.getCMARTurl().getAppURL()).append("/").append(suffix).toString().replace(" ", "%20"), null, null);

					HttpGet httpget = new HttpGet(uri);

					if(inCache=true)
						httpget.addHeader("If-Modified-Since", DateParser.dateToString(new Date()));

					HttpResponse response = httpclient.execute(httpget);

					HttpEntity entity = response.getEntity();

					if(RunSettings.isNetworkDelay()){
						try{Thread.sleep(client.getNetworkDelay());}
						catch(InterruptedException e){
							break;
						}
					}
					if(response.getStatusLine().getStatusCode()==200){
						BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));	// opens a BufferedReader to read the response of the HTTP request

						while((newline=br.readLine())!=null){
							//	System.out.println(inputLine);
							line.append(newline);	// creates the response
						}
						br.close();
						//	if(verbose) System.out.println("JS LINE NEW: "+suffix+" "+line);
						//	synchronized(this){
						if(RunSettings.isCacheRealData())
							client.getClientInfo().addJSCSSCache(suffix, line);
						else
							client.getClientInfo().addJSCSSCache(suffix, null);
						//	}
					}
					delConnection();		// indicates this connection is finished
					break;
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}catch(ConnectException e){
					System.err.println("Could not create connection");
					System.err.println(new StringBuilder(client.getCMARTurl().getAppURL()).append("/").append(suffix).toString().replace(" ", "%20"));
					attempts++;
					if (httpRequestAttempts==3){
						threadExecutor.shutdown();
						client.setExit(true);
						client.setExitDueToError(true);
					}
				} catch(SocketException e){
					System.err.println("Could not create connection");
					System.err.println(new StringBuilder(client.getCMARTurl().getAppURL()).append("/").append(suffix).toString().replace(" ", "%20"));
					attempts++;
					if (httpRequestAttempts==3){
						threadExecutor.shutdown();
						client.setExit(true);
						client.setExitDueToError(true);
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}


	/**
	 * Indicates a connection has been added
	 */
	private synchronized void addConnection(){
		activeConnections+=1;
	}

	/**
	 * Indicates a connection has been removed and notifies the parent that the connection is finished
	 */
	private synchronized void delConnection(){
		activeConnections-=1;
		notify();
	}
	/**
	 * Gets the count of the number of connections
	 * @return the number of active connections
	 */
	private synchronized int getConnectionCount(){
		return activeConnections;
	}

	/**
	 * searches the page for JS files and sends to the appropriate method to get them
	 * @param str - html of the page
	 * @param sw - stopwatch timing total response time of the page
	 * @return	Returns the stop watch with the added time it took to get the js
	 * @throws InterruptedException
	 */
	private synchronized Stopwatch getJsCssNew(StringBuilder str, Stopwatch sw) throws InterruptedException{

		int end=str.indexOf(JS_TEXTQ)+JS_TEXT.length();
		int end2=str.indexOf(CSS_TEXTQ)+CSS_TEXT.length();
		int start;
		if(end2!=CSS_TEXT.length()-1&&end2<end){
			end=end2;
			start=str.lastIndexOf("href=\"",end)+"href=\"".length();
		}
		else
			start=str.lastIndexOf("src=\"",end)+"src=\"".length();

		while(end!=JS_TEXT.length()-1&&end!=CSS_TEXT.length()-1){
			//System.out.println(str.substring(start,end));
			boolean inCache=false;
			if (client.getClientInfo().inJSCSSCache(str.substring(start,end))==true){
				inCache=true;
			}
			GetJSCSSNew gJ=new GetJSCSSNew(str.substring(start,end),client,client.getHttpClient(),inCache);
			addConnection();
			sw.start();
			//gJ.start();
			threadExecutor.execute(gJ);
			if (getConnectionCount()==0)
				sw.pause();


			end2=str.indexOf(CSS_TEXTQ,end)+CSS_TEXT.length();
			end=str.indexOf(JS_TEXTQ,end)+JS_TEXT.length();				
			if((end2!=CSS_TEXT.length()-1&&end2<end)||end==JS_TEXT.length()-1){
				end=end2;
				start=str.lastIndexOf("href=\"",end)+"href=\"".length();
			}
			else
				start=str.lastIndexOf("src=\"",end)+"src=\"".length();

		}
		while(getConnectionCount()>0){try {
			wait();
		} catch (InterruptedException e) {
			threadExecutor.shutdownNow();
			break;
		}}
		sw.pause();
		return sw;
	}



	/**
	 * searches the page for jpg files and sends to the appropriate method to get them
	 * @param str - html of the page
	 * @param sw - stopwatch timing total response time of the page
	 * @return	Returns the stop watch with the added time it took to get the jpgs
	 */
	private synchronized Stopwatch getImagesNew(StringBuilder str, Stopwatch sw) throws JsonParseException, JsonMappingException, IOException{

		int end=str.indexOf(JPG_TEXTQ)+JPG_TEXT.length();
		int end2=str.indexOf(PNG_TEXTQ)+PNG_TEXT.length();
		if(end2!=PNG_TEXT.length()-1&&end2<end)
			end=end2;
		int start=str.lastIndexOf("src=\"",end)+"src=\"".length();
		boolean exitCondition=(end!=JPG_TEXT.length()-1);

		if(str.substring(0, 5).equals("<?xml")){
			start=str.indexOf("<thumbnailURL>");
			end=str.indexOf("</thumbnailURL>",start);
			while(end!=-1&&start!=-1){
				start+="<thumbnailURL>".length();
				String imgName=str.substring(start,end);
				if(isImageOnPage(imgName)==false){
					addImageOnPage(imgName);
					boolean inCache=false;
					if (client.getClientInfo().inImageCache(str.substring(start,end))==true){
						inCache=true;
					}
					GetImageNew gI=new GetImageNew(imgName,client,client.getHttpClient(),inCache);

					addConnection();
					sw.start();
					//gI.start();
					try{
						threadExecutor.execute(gI);
					}
					catch(Exception e){
						System.err.println("STR: "+str.substring(start,end));
						e.printStackTrace();
					}
				}
				if (getConnectionCount()==0)
					sw.pause();

				start=str.indexOf("<thumbnailURL>",end);
				end=str.indexOf("</thumbnailURL>",start);
			}
		}
		else if(HTML4||str.charAt(0)!='{'){
			while(exitCondition){
				boolean inCache=false;
				if (client.getClientInfo().inImageCache(str.substring(start,end))==true){
					inCache=true;
				}
				String imgName=str.substring(start,end);
				if(isImageOnPage(imgName)==false){
					addImageOnPage(imgName);
					GetImageNew gI=new GetImageNew(imgName,client,client.getHttpClient(),inCache);

					addConnection();
					sw.start();
					//gI.start();
					try{
						threadExecutor.execute(gI);
					}
					catch(Exception e){
						System.err.println("STR: "+str.substring(start,end));
						e.printStackTrace();
					}
				}
				if (getConnectionCount()==0)
					sw.pause();

				end2=str.indexOf(PNG_TEXTQ,end)+PNG_TEXT.length();
				end=str.indexOf(JPG_TEXTQ,end)+JPG_TEXT.length();				
				if((end2!=PNG_TEXT.length()-1&&end2<end)||end==JPG_TEXT.length()-1)
					end=end2;
				start=str.lastIndexOf("src=\"",end)+"src=\"".length();
				exitCondition=(end!=JPG_TEXT.length()-1);

				if(exitCondition==false){				// gets images parsed from javascript
					for (String s:allPagesImages){
						if(isImageOnPage(s)==false){
							inCache=false;
							if (client.getClientInfo().inImageCache(s)==true){
								inCache=true;
							}
							addImageOnPage(s);
							GetImageNew gI=new GetImageNew(s,client,client.getHttpClient(),inCache);

							addConnection();
							sw.start();
							//gI.start();
							try{
								threadExecutor.execute(gI);
							}
							catch(Exception e){
								System.err.println("STR: "+str.substring(start,end));
								e.printStackTrace();
							}
						}
						if (getConnectionCount()==0)
							sw.pause();
					}
					if(getPageType(str)==ITEM_PAGE_NUM.getCode()&&str.indexOf("_1.jpg")!=-1){
						for (String s:itemPageImages){
							if(isImageOnPage(s)==false){
								inCache=false;
								if (client.getClientInfo().inImageCache(s)==true){
									inCache=true;
								}
								addImageOnPage(s);
								GetImageNew gI=new GetImageNew(s,client,client.getHttpClient(),inCache);

								addConnection();
								sw.start();
								//gI.start();
								try{
									threadExecutor.execute(gI);
								}
								catch(Exception e){
									e.printStackTrace();
								}
							}
							if (getConnectionCount()==0)
								sw.pause();
						}
					}

				}
			}
		}
		else if(!HTML4&&(str.charAt(0)=='{'||str.indexOf("ITEM")!=-1)){
			if(str.indexOf("\"items\"")!=-1){
				ObjectMapper mapper = new ObjectMapper();
				JsonNode node = mapper.readValue(str.toString(), JsonNode.class);
				for(int i=0;i<node.get("items").size();i++){

					String imgName;
					if(RunSettings.isLocalImages())
						imgName=LOCAL_IMAGE.concat(node.get("items").get(i).get("thumbnail").getTextValue());
					else
						imgName=NET_IMAGE.concat(node.get("items").get(i).get("thumbnail").getTextValue());

					if(isImageOnPage(imgName)==false){
						boolean inCache=false;
						if (client.getClientInfo().inImageCache(imgName)==true){
							inCache=true;
						}
						addImageOnPage(imgName);
						GetImageNew gI=new GetImageNew(imgName,client,client.getHttpClient(),inCache);

						addConnection();
						sw.start();

						try{
							threadExecutor.execute(gI);
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					if (getConnectionCount()==0)
						sw.pause();
				}
			}
			else if(str.indexOf("{\"newbids\":")!=-1){
				ObjectMapper mapper = new ObjectMapper();
				JsonNode node = mapper.readValue(str.toString(), JsonNode.class);
				String category=null;
				for (int j=0;j<4;j++){
					if(j==0)
						category="newbids";
					else if(j==1)
						category="newitems";
					else if(j==2)
						category="oldbids";
					else if(j==3)
						category="olditems";
					for(int i=0;i<node.get(category).size();i++){
						String imgName=null;
						if(j==1||j==3)
							if(RunSettings.isLocalImages())
								imgName=LOCAL_IMAGE.concat(node.get(category).get(i).get("thumbnail").getTextValue());
							else
								imgName=NET_IMAGE.concat(node.get(category).get(i).get("thumbnail").getTextValue());
						else if(j==0||j==2)
							if(RunSettings.isLocalImages())
								imgName=LOCAL_IMAGE.concat(node.get(category).get(i).get("bidItem").get("thumbnail").getTextValue());
							else
								imgName=NET_IMAGE.concat(node.get(category).get(i).get("bidItem").get("thumbnail").getTextValue());

						if(isImageOnPage(imgName)==false){
							boolean inCache=false;
							if (client.getClientInfo().inImageCache(imgName)==true){
								inCache=true;
							}
							addImageOnPage(imgName);
							GetImageNew gI=new GetImageNew(imgName,client,client.getHttpClient(),inCache);

							addConnection();
							sw.start();

							try{
								threadExecutor.execute(gI);
							}
							catch(Exception e){
								e.printStackTrace();
							}
						}
						if (getConnectionCount()==0)
							sw.pause();
					}
				}
			}
			else if(str.indexOf(",\"item\":{")!=-1){	// new item on item page
				ObjectMapper mapper = new ObjectMapper();
				JsonNode node = mapper.readValue(str.toString(), JsonNode.class);
				for(int i=0;i<=node.get("item").get("images").size();i++){
					String imgName=null;
					if(i<node.get("item").get("images").size())
						if(RunSettings.isLocalImages())
							imgName=LOCAL_IMAGE.concat(node.get("item").get("images").get(i).get("url").getTextValue());
						else
							imgName=NET_IMAGE.concat(node.get("item").get("images").get(i).get("url").getTextValue());
					else
						if(RunSettings.isLocalImages())
							imgName=LOCAL_IMAGE.concat(node.get("item").get("thumbnail").getTextValue());
						else
							imgName=NET_IMAGE.concat(node.get("item").get("thumbnail").getTextValue());

					if(isImageOnPage(imgName)==false){
						boolean inCache=false;
						if (client.getClientInfo().inImageCache(imgName)==true){
							inCache=true;
						}
						addImageOnPage(imgName);
						GetImageNew gI=new GetImageNew(imgName,client,client.getHttpClient(),inCache);

						addConnection();
						sw.start();

						try{
							threadExecutor.execute(gI);
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					if (getConnectionCount()==0)
						sw.pause();
				}
			}
			else if(str.indexOf("ITEM")!=-1){
				start=str.indexOf("ITEM")+"ITEM".length();
				long itemNum=Long.parseLong(url.substring(start));
				ItemCG item=client.getClientInfo().getHTML5ItemCache().get(itemNum);
				if(verbose)System.out.println(item.getImages());
				for(int j=0;j<item.getImages().size();j++){
					String imgName;
					if(RunSettings.isLocalImages())
						imgName=LOCAL_IMAGE.concat(item.getImages().get(j));
					else
						imgName=NET_IMAGE.concat(item.getImages().get(j));

					if(isImageOnPage(imgName)==false){
						boolean inCache=false;
						if (client.getClientInfo().inImageCache(imgName)==true){
							inCache=true;
						}
						addImageOnPage(imgName);
						GetImageNew gI=new GetImageNew(imgName,client,client.getHttpClient(),inCache);

						addConnection();
						sw.start();
						try{
							threadExecutor.execute(gI);
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					if (getConnectionCount()==0)
						sw.pause();
				}
			}
		}
		while(getConnectionCount()>0){try {
			wait();
		} catch (InterruptedException e) {
			threadExecutor.shutdownNow();
			break;
		}}
		sw.pause();

		return sw;
	}



	/**
	 * Adds an image to the list of those on the page
	 * @param str - url of the image
	 */
	private synchronized void addImageOnPage(String str){
		imagesOnPage.add(str);
	}

	/**
	 * Returns a boolean indicating if an image is already on the page
	 * Used to prevent getting an image multiple times if it is on 
	 * a page multiple times
	 * @param str - url of the image
	 * @return
	 */
	private synchronized boolean isImageOnPage(String str){
		if (imagesOnPage.contains(str))
			return true;
		else
			return false;
	}

	/**
	 * Returns the URL used to open page
	 * @return
	 */
	public StringBuilder getURL(){
		return this.url;
	}

	/**
	 * Begins a search action on C-MART when no previous search term has already been specified
	 * @param data - user data to include in search query
	 * @param action - action in xmlDocument to add data too on the request
	 * @return Returns the StringBuilder that is returned after the C-MART search query
	 */
	protected StringBuilder search (HashMap<String,StringBuilder> data, Element action) throws UnsupportedEncodingException, IOException, InterruptedException{
		return search(null,data,action);
	}

	/**
	 * Searches the application for items matching a search query
	 * @param data - POST data required for the search form
	 * @return Returns the page containing the search results
	 */
	protected StringBuilder search(StringBuilder initialSearchTerm, HashMap<String,StringBuilder> data,Element action) throws UnsupportedEncodingException, IOException, InterruptedException{
		StringBuilder newPage;
		StringBuilder searchTerm=new StringBuilder();
		Element request=xmlDocument.createElement("request");

		if(RunSettings.isRepeatedRun()==false){		// if it is a new run
			if(initialSearchTerm==null){			// if there is no previous search term to clarify or add to
				int numWordsToSearch=getRandomIntFromDist(RunSettings.getNumSearchWords());		// get the number of words in the search term
				// create the search term with words from the item description distribution
				for (int i=0;i<numWordsToSearch;i++){
					searchTerm.append(getRandomStringBuilderFromDist(RunSettings.getItemSpecificsDescWords()));
					if (i!=numWordsToSearch-1)
						searchTerm.append(" ");
				}
				data.put("searchTerm",typingError(searchTerm));
			}
			else{		// if there is a previous search term to be expanded on
				int additionalWords=rand.nextInt(2)+1;	// adds either 1 or 2 words onto the search term
				searchTerm.append(initialSearchTerm);
				for (int i=0;i<additionalWords;i++){
					searchTerm.append(" ");
					searchTerm.append(typingError(getRandomStringBuilderFromDist(RunSettings.getItemSpecificsDescWords())));

				}
				data.put("searchTerm",searchTerm);
			}

			client.setPreviousSearchTerm(searchTerm);
		}else{
			searchTerm=initialSearchTerm;
		}

		// Think Time
		try{Thread.sleep(getSearchThinkTime(searchTerm));}
		catch(InterruptedException e){
			client.setExit(true);
			return null;
		}

		if(RunSettings.isRepeatedRun()==false){		// if it is a new run document the search action
			Element child=xmlDocument.createElement("url");
			child.setTextContent(new StringBuilder(client.getCMARTurl().getAppURL()).append("/search?").toString());
			request.appendChild(child);
			for (Entry<String,StringBuilder> e:data.entrySet()){
				child=xmlDocument.createElement("data");
				child.setAttribute("name", e.getKey());
				child.setTextContent(e.getValue().toString());
				request.appendChild(child);
			}
			if(HTML4){
				child=xmlDocument.createElement("type");
				child.setTextContent("POST");
				request.appendChild(child);
				newPage=doSubmit(new StringBuilder(client.getCMARTurl().getAppURL()).append("/search?"),data);
			}
			else{
				child=xmlDocument.createElement("type");
				child.setTextContent("GET");
				request.appendChild(child);
				newPage=openHTML5Page(new StringBuilder(client.getCMARTurl().getAppURL()).append("/search.html"),new StringBuilder(client.getCMARTurl().getAppURL()).append("/search?").append(createURL(data)));
			}

			child=xmlDocument.createElement("thinkTime");
			child.setTextContent(Integer.toString(pageThinkTime));
			action.appendChild(child);
			action.appendChild(request);
			client.addXMLAction(action);
		}else{
			if(HTML4){
				newPage=doSubmit(new StringBuilder(client.getCMARTurl().getAppURL()).append("/search?"),data);
			}
			else{
				newPage=openHTML5Page(new StringBuilder(client.getCMARTurl().getAppURL()).append("/search.html"),new StringBuilder(client.getCMARTurl().getAppURL()).append("/search?").append(createURL(data)));
			}
		}
		return newPage;

	}

	/**
	 * Gets the think time for when the user decides to search
	 * @param searchTerm - the search query
	 * @return
	 */
	private int getSearchThinkTime(StringBuilder searchTerm){
		int thinkTime=(int)expDist(initialThinkTime+5000);
		if(RunSettings.isRepeatedRun()==false){
			thinkTime+=typingErrorThinkTime;
			thinkTime+=(int)(searchTerm.length()/client.getTypingSpeed());
		}else{
			if(searchTerm!=null)
				thinkTime=Integer.parseInt(((Element)action).getElementsByTagName("thinkTime").item(1).getTextContent());
			else
				thinkTime=Integer.parseInt(((Element)action).getElementsByTagName("thinkTime").item(0).getTextContent());
		}
		if (verbose)System.out.println("User: "+client.getClientInfo().getUsername()+" - Think Time: "+thinkTime+" ms");
		if (RunSettings.isOutputThinkTimes()==true)
			client.getCg().getThinkTimeHist().add(thinkTime);
		pageThinkTime=thinkTime;
		return Math.max((int) ((thinkTime-(new Date().getTime()-pageOpenTime))/RunSettings.getThinkTimeSpeedUpFactor()),0);
	}

	/**
	 * Gets an array of the words in the previous search query
	 */
	protected void getSearchTermWords(){
		StringBuilder searchTerm=new StringBuilder(client.getPreviousSearchTerm());
		int spaceIndex=searchTerm.indexOf(" ");
		do{
			if(spaceIndex==-1){
				spaceIndex=searchTerm.length();
			}
			searchTermWords.add(searchTerm.substring(0,spaceIndex));
			if(spaceIndex!=searchTerm.length())
				searchTerm.delete(0, spaceIndex+1);
			else
				searchTerm.delete(0, spaceIndex);
			spaceIndex=searchTerm.indexOf(" ");

		}while(searchTerm.length()!=0);
	}

	/**
	 * For HTML5/SQLite
	 * Gets the list of most recent 250 items that the client had seen
	 * @return Returns a StringBuilder as CSV of the up-to-250 most recently seen items
	 */
	protected StringBuilder getHasItems(){
		StringBuilder hasItems=new StringBuilder();
		long oldestTime=Long.MAX_VALUE;;
		long oldestItem=0;
		long currentTime=new Date().getTime();
		ArrayList<Long> hasItemsItems=new ArrayList<Long>();
		for(Entry<Long,ItemCG> it:client.getClientInfo().getHTML5ItemCache().entrySet()){
			if(hasItemsItems.size()<250&&(currentTime-it.getValue().getTs())<=300000){
				hasItemsItems.add(it.getKey());
				if(it.getValue().getTs()<oldestTime){
					oldestTime=it.getValue().getTs();
					oldestItem=it.getKey();
				}							
			}
			else if(hasItemsItems.size()>=250&&(currentTime-it.getValue().getTs())<=300000){
				if(it.getValue().getTs()>oldestTime){
					hasItemsItems.remove(hasItemsItems.indexOf(oldestItem));
					hasItemsItems.add(it.getKey());
					oldestTime=Long.MAX_VALUE;
					for(Entry<Long,ItemCG> it2:client.getClientInfo().getHTML5ItemCache().entrySet()){
						if(hasItemsItems.contains(it2.getKey())){
							if(it2.getValue().getTs()<oldestTime){
								oldestTime=it2.getValue().getTs();
								oldestItem=it2.getKey();
							}	
						}
					}	
				}
			}
		}
		for(long i:hasItemsItems){
			hasItems.append(i).append(",");
		}
		if(hasItemsItems.size()>0)
			hasItems.deleteCharAt(hasItems.length()-1);
		return hasItems;
	}

	/**
	 * Shuts down the threads used to open css/js/images
	 */
	public void shutdownThreads(){
		threadExecutor.shutdownNow();
	}

	/**
	 * Finds the action in the xmlDocument for a repeated run 
	 * for the action number the Client is on
	 */
	protected void findAction(){
		NodeList actions=client.getReadXmlDocument().getElementsByTagName("action");
		String actionNum=client.getActionNum();
		for(int i=0;i<actions.getLength();i++){
			Node n=actions.item(i);
			if(n.getAttributes().item(0).getTextContent().equals(actionNum)){
				action=(Element)n;
				break;
			}
		}
	}

	public StringBuilder makeDecision() throws UnsupportedEncodingException, IOException, InterruptedException, URISyntaxException, ParseException{
		// TODO Auto-generated method stub
		return null;
	}

}
