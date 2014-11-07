package client.clientMain;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import client.Items.ItemCG;
import client.Pages.ItemPage;
import client.Pages.Page;
import client.Pages.PageType;

/**
 * Runs a client through the application
 * @author Andrew Fox
 *
 */

public class Client extends Thread{

	private ClientInfo clientInfo;			// info of the client
	private StringBuilder currentURL;		// URL the client is currently viewing
	private StringBuilder lastURL;			// URL of previous page
	private final ClientGenerator cg;			// client generator creating the client
	private final CMARTurl cmarturl;			// parts of the URL for the application
	private int RTthreshold;
	
	private final Random rand; // random seed
	
	private final long[] last4RT;	// the response times of the last four pages opened
	// the RT threshold (ms) above which the logout probability increases, and below which the number of tabs to open increases


	private double typingSpeed;				// characters typed per millisecond;
	private double typingErrorRate;			// character typing error rate

	private PoolingHttpClientConnectionManager connectionManager;
	private HttpClientBuilder httpClientBuilder;
	private CloseableHttpClient httpclient;


	private final long networkDelay;			// additional network delay to be added
	private final boolean formFiller;			// if the client is a form filler

	// factors which determine the relative importance of each element of an item profile when determining interest in it
	private double titleWordsFactor=2;	
	private double descriptionWordsFactor=1;
	private double sellerRatingFactor=1;
	private double numPicsFactor=3;
	private double endDateDiffFactor=1;
	private double startPriceFactor=3;
	private double priceDiffFactor=1;
	private double listRankingFactor=1;
	private double commonSearchTermsFactor=3;
	private double alreadyBidFactor=2;
	private double categoryDepthFactor=0.25;
	private double hotItemFactor=2;
	private double recommendedFactor=2;
	private double answersFactor=0.4;

	private Document readXmlDocument;					// xml document of actions to be read in for a repeated run
	private Document xmlDocument;						// xml document to output of user actions
	private Element actionsElement;						// Element of all the actions

	private boolean exit;					// if the client is to exit
	private int lastPageType;				// page number of the previous page opened
	private int currentPageType;			// Page type of the current page
	private boolean loggedIn;			// if the client is logged in

	
	private  TreeMap<Long,ItemCG> currentBids=new TreeMap<Long,ItemCG>();			// tracks items from the client is currently bidding on
	private TreeMap<Long,ItemCG> endedAuctions=new TreeMap<Long,ItemCG>();		// tracks items that the user has won
	private  TreeMap<Long,ItemCG> purchasedItems=new TreeMap<Long,ItemCG>();		// tracks items the user has purchased (buyNow)
	private  TreeMap<Long,ItemCG> sellingItems=new TreeMap<Long,ItemCG>();		// tracks items the user is selling
	private  TreeMap<Long,ItemCG> soldItems=new TreeMap<Long,ItemCG>();			// tracks items the user has sold
	private int loginAttempts=0;				// number of times the client has attempted to login/register
	private long lastItemID;					// itemID of last item opened
	private ArrayList<String>errors=new ArrayList<String>();	// list of errors on an HTML5 page
	private int numPagesOpened=0;			// number of pages the client has opened
	private long totalRT=0;					// total response time for all opened pages
	private int requestErrors=0;			// number of errors the client has had making a request
	private long startTime;					// time the client started accessing the website
	private boolean exitDueToError=false;	// if the client exits the website due to an error
	private double origLogoutProb=0;		// original logout probability on a page
	private double finalLogoutProb=0;		// logout probability after response time modifications
	private double restProb=0;				// probability of non-logout options before normalization
	private int actionNum=0;							// action number that the user is on
	private boolean exitDueToRepeatChange=false;		// if the client exits due to a variation from the xml document
	private boolean changeDueToRepeatChange=false;		// if the client does something different from the xml document that does not require the client to exit



	private String message="";
	private StringBuilder previousSearchTerm=new StringBuilder();			// the query last searched for
	private long clientID;				// id of the client in the database
	private int itemsBought=0;			// number of items the client has bought in this session
	private int itemsBid=0;				// number of items the client has bid on in this session
	private int itemsSold=0;			// number of items the client has sold in this session
	private double expSoldRate=1/2;		// exponential rate at which someone is likely to make another sale after already making them
	private double expBuyRate=1/2;		// exponential rate at which someone is likely to make another buy after already making them
	private ArrayList<ItemPage> openTabs=new ArrayList<ItemPage>();	// pages of tabs in addition to the original open page that have been opened



	/**
	 * Creates a new client
	 * @param userInfo - client information
	 * @param startURL - URL of the page to open first
	 * @param buyProbability
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Client(ClientInfo userInfo, StringBuilder startURL,ClientGenerator cg) throws IOException{
		this.clientInfo = userInfo;
		this.currentURL = startURL;
		this.lastURL = startURL;
		this.cg = cg;

		this.cmarturl = RunSettings.getCMARTurl();

		this.last4RT = new long[4];
		for (int i = 0; i < 4; i++) {
			this.last4RT[i] = 0;
		}
		
		this.rand = new Random();
		
		do {
			this.RTthreshold = (int) (RunSettings.getRTthreshold() * (0.4 * (this.rand.nextDouble() - 0.5) + 1));
			// randomly adjusts the RTthreshold to add variance for different clients
		} while (this.RTthreshold <= 0);

		double typeSpeedRand = this.rand.nextDouble();
		for (Entry<Double, Double> entry : RunSettings.getTypingSpeedDist().entrySet()) {
			if (typeSpeedRand < entry.getKey()) {
				this.typingSpeed = (entry.getValue() + 10 * (this.rand.nextDouble() - 0.5)) / 60000;
				if (RunSettings.isVerbose()){
					System.out.println("Typing Speed: " + (60000. * this.typingSpeed) / 5 + " wpm");
				}
				break;
			}
		}
		
		for (Entry<Double, Double> entry : RunSettings.getTypingErrorRate().entrySet()) {
			if (this.typingSpeed < entry.getKey()) {
				this.typingErrorRate = entry.getValue() + (this.rand.nextDouble() - 0.5) * 0.001;
				break;
			}
		}

		connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(100);

		this.httpClientBuilder = HttpClientBuilder.create();
		this.httpClientBuilder.setConnectionManager(connectionManager);
		this.httpClientBuilder.setRedirectStrategy(new DefaultRedirectStrategy() {
			public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) {
				boolean isRedirect = false;
				try {
					if (response.containsHeader("Location")) {
						response.setHeader("Location", response.getFirstHeader("Location").getValue().replace(" ", "%20"));
					}
					isRedirect = super.isRedirected(request, response, context);
				} catch (ProtocolException e) {
					e.printStackTrace();
				}
				if (!isRedirect) {
					int responseCode = response.getStatusLine().getStatusCode();
					if (responseCode == 301 || responseCode == 302) {
						return true;
					}
				}
				return isRedirect;
			}
		});

		// Determines the network delay based on the exponential distribution of the provided average
		this.networkDelay = RunSettings.isNetworkDelay() ? (long) cg.expDist(RunSettings.getNetworkDelayAvg()) : 0;

		// form filler (10% chance of being a form filler)
		this.formFiller = (rand.nextDouble() > 0.9);

		// adjusts the fudge factors for the individual client (adjusts each by a possible +-20%)		
		this.titleWordsFactor *= (1. + (rand.nextDouble() - 0.5) * 0.4);
		this.descriptionWordsFactor *= (1. + (rand.nextDouble() - 0.5) * 0.4);
		this.sellerRatingFactor *= (1. + (rand.nextDouble() - 0.5) * 0.4);
		this.numPicsFactor *= (1. + (rand.nextDouble() - 0.5) * 0.4);
		this.endDateDiffFactor *= (1. + (rand.nextDouble() - 0.5) * 0.4);
		this.startPriceFactor *= (1. + (rand.nextDouble() - 0.5) * 0.4);
		this.priceDiffFactor *= (1. + (rand.nextDouble() - 0.5) * 0.4);
		this.listRankingFactor *= (1. + (rand.nextDouble() - 0.5) * 0.4);
		this.commonSearchTermsFactor *= (1. + (rand.nextDouble() - 0.5) * 0.4);
		//this.commonSearchTermsFactor *= (1. + (rand.nextDouble() - 0.5) * 0.4);//FIXME was this duplicated on purpose?
		this.alreadyBidFactor *= (1. + (rand.nextDouble() - 0.5) * 0.4);
		this.categoryDepthFactor *= (1. + (rand.nextDouble() - 0.5) * 0.4);
		this.hotItemFactor *= (1. + (rand.nextDouble() - 0.5) * 0.4);
		this.recommendedFactor *= (1. + (rand.nextDouble() - 0.5) * 0.4);
		this.answersFactor *= (1. + (rand.nextDouble() - 0.5) * 0.4);

		this.startTime = System.currentTimeMillis();

		try {
			if (RunSettings.isRepeatedRun()) {
				this.readXmlDocument = DocumentBuilderFactory
						.newInstance()
						.newDocumentBuilder()
						.parse(new File(new StringBuilder(RunSettings.getRepeatedXmlFolder()).append("client").append(clientInfo.getClientIndex())
								.append(".xml").toString()));
			} else {
				this.xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				Element root = this.xmlDocument.createElement("Client");
				this.xmlDocument.appendChild(root);
				Element child = this.xmlDocument.createElement("UserID");
				child.setTextContent(this.clientInfo.getUserID().toString());
				root.appendChild(child);
				child = this.xmlDocument.createElement("Username");
				child.setTextContent(this.clientInfo.getUsername().toString());
				root.appendChild(child);
				child = this.xmlDocument.createElement("password");
				child.setTextContent(this.clientInfo.getPassword().toString());
				root.appendChild(child);
				Element actions = this.xmlDocument.createElement("actions");
				root.appendChild(actions);
				this.actionsElement = actions;
			}
		} catch (ParserConfigurationException | TransformerFactoryConfigurationError | SAXException e) {
			System.err.println("Could not load client xml file");
		}
	}

	/**
	 * Runs the client through each page
	 */
	public void run(){
		int openedPagesCounter = 0;
		
		try (CloseableHttpClient client = httpClientBuilder.build()) {
			this.httpclient = client;
			while (!this.exit) { // while the client is not exiting the system
				openedPagesCounter++;
				if (RunSettings.isVerbose()) {
					System.out.println("Client " + this.getClientID() + " Page Number: " + openedPagesCounter + " Current Page Type: "
							+ PageType.values()[currentPageType]);
				}

				Page activePage = new Page(this.currentURL, this.currentPageType, this.lastURL, this).toPageType();

				this.lastURL = this.currentURL;
				this.lastPageType = this.currentPageType;
				this.currentPageType = activePage.getPageType();
				
				if (currentPageType == PageType.NONE.getCode()) {
					this.exit = true;
				} else {
					this.currentURL = activePage.makeDecision();
				}
				activePage.shutdownThreads();
			}
			
			System.out.println(System.currentTimeMillis() + " Client (#" + clientInfo.getClientIndex() + ") " + clientInfo.getUsername() + " Exiting ");

			if (currentPageType != PageType.REGISTER_PAGE_NUM.getCode()) {
				cg.activeToInactive(clientInfo, this);
			} else {
				cg.activeToRemove(clientInfo, this); 
			}

		} catch (Exception e) {
//		} catch (IOException | ParseException | InterruptedException | URISyntaxException e) {
			this.exit = true;
			this.loggedIn = false;
			System.out.println(System.currentTimeMillis() + " Client (#" + clientInfo.getClientIndex() + ") " + clientInfo.getUsername()
					+ " Exiting Due To Error ");
			cg.activeToRemove(clientInfo, this); // moves the client
//			cg.addClientSession(numPagesOpened, totalRT, requestErrors, startTime, true, 1.);
		}

		for (ItemPage ip : openTabs) {
			ip.cancelTimer();
		}

		if (rand.nextDouble() < RunSettings.getClearCacheOnExit()) {
			clientInfo.clearCaches();
		}

		if (exitDueToRepeatChange) {
			System.out.println("Client Index " + clientInfo.getClientIndex() + " left due to change in repeated run");
		}
		
		double annoyedProb;
		if (exitDueToError) {
			annoyedProb = 1.;
		} else if (finalLogoutProb == 0) {
			annoyedProb = 0;
		} else {
			annoyedProb = (restProb * (finalLogoutProb - origLogoutProb)) / (finalLogoutProb * (restProb + origLogoutProb));
		}

		cg.addClientSession(numPagesOpened, totalRT, requestErrors, startTime, exitDueToError, annoyedProb);

		outputClientXML();
	}




	/**
	 * Adds the new response time to an array of the last 3 being tracked
	 * @param newRT - the new response time
	 */
	public void addRT(long newRT){
		for (int i=0;i<3;i++)
			last4RT[i]=last4RT[i+1];
		last4RT[3]=newRT;
	}

	/**
	 * Adjusts the Response Time of last pages according to the page type
	 * since clients have different expectations of each page
	 * Normalized so that the ItemPage has pageRTFactor=1
	 * @param pageRTFactor
	 */
	public void pageSpecificRT(double pageRTFactor){
		last4RT[3]/=pageRTFactor;
	}

	/**
	 * Gets the average response time of the last 4 pages
	 * @return - average response time
	 */
	public long getRTavg(){
		long sum=0;
		for (int i=0;i<4;i++)
			sum+=last4RT[i];
		return (sum/4);
	}

	/**
	 * @return the clientID
	 */
	public long getClientID() {
		return clientID;
	}


	/**
	 * @param clientID the clientID to set
	 */
	public void setClientID(long clientID) {
		this.clientID = clientID;
	}

	/**
	 * Returns the clientInfo of the client in this session
	 * @return
	 */
	public ClientInfo getClientInfo(){
		return this.clientInfo;
	}

	/**
	 * Returns if the client is logged in
	 * @return
	 */
	public boolean isLoggedIn(){
		return this.loggedIn;
	}

	/**
	 * Sets if the client is logged in
	 * @param val - true if client is logged in
	 */
	public void setLoggedIn(boolean val){
		this.loggedIn=val;
	}

	/**
	 * Gets the typing speed of the client
	 * @return
	 */
	public double getTypingSpeed(){
		return this.typingSpeed;
	}

	/**
	 * gets the typing error rate of the client
	 * @return
	 */
	public double getTypingErrorRate(){
		return this.typingErrorRate;
	}

	/**
	 * Gets if the client is a form filler
	 * @return
	 */
	public boolean isFormFiller(){
		return this.formFiller;
	}

	/**
	 * Gets the number of login/register attempts the client has tried
	 * @return
	 */
	public int getLoginAttempts(){
		return this.loginAttempts;
	}

	/**
	 * Increases the number of login/register attempts of the user by 1
	 */
	public void incLoginAttempts(){
		this.loginAttempts++;
	}

	/**
	 * Returns the item rating factor for the number of words in the title
	 * @return titleWordsFactor
	 */
	public double getTitleWordsFactor(){
		return this.titleWordsFactor;
	}

	/**
	 * Returns the item rating factor for the number of words in the description
	 * @return descriptionWordsFactor
	 */
	public double getDescriptionWordsFactor(){
		return this.descriptionWordsFactor;
	}

	/**
	 * Returns the item rating factor for the seller rating
	 * @return sellerRatingFactor
	 */
	public double getSellerRatingFactor(){
		return this.sellerRatingFactor;
	}

	/**
	 * Returns the item rating factor for the number of pictures an item has
	 * @return numPicsFactor
	 */
	public double getNumPicsFactor(){
		return this.numPicsFactor;
	}

	/**
	 * Returns the item rating factor for how close the current date is to the end of the auction
	 * @return endDateDiffFactor
	 */
	public double getEndDateDiffFactor(){
		return this.endDateDiffFactor;
	}

	/**
	 * Returns the item rating factor for the starting price
	 * @return startPriceFactor
	 */
	public double getStartPriceFactor(){
		return this.startPriceFactor;
	}

	/**
	 * Returns the item rating factor for the difference in price between the auction price and the buyNow price
	 * @return priceDiffFactor
	 */
	public double getPriceDiffFactor(){
		return this.priceDiffFactor;
	}

	/**
	 * Returns the item rating factor for where the item is ranked on the browse/search list
	 * @return listRankingFactor
	 */
	public double getListRankingFactor(){
		return this.listRankingFactor;
	}

	/**
	 * Returns the item rating factor for the number of common words the item title has with the search query
	 * @return commonSearchTermsFactor
	 */
	public double getCommonSearchTermsFactor(){
		return this.commonSearchTermsFactor;
	}

	/**
	 * Returns the item rating factor for if the client has already bid on the item
	 * @return alreadyBidFactor
	 */
	public double getAlreadyBidFactor(){
		return this.alreadyBidFactor;
	}

	/**
	 * Returns the factor for the category depth of an item
	 * @return categoryDepthFactor
	 */
	public double getCategoryDepthFactor(){
		return this.categoryDepthFactor;
	}

	/**
	 * Returns the item rating factor for if an item is a hot item
	 * @return hotItemFactor
	 */
	public double getHotItemFactor(){
		return this.hotItemFactor;
	}

	/**
	 * Returns the item rating factor for if CMART recommends the item to the user
	 * @return recommendedFactor
	 */
	public double getRecommendedFactor(){
		return this.recommendedFactor;
	}
	
	/**
	 * Returns the item rating factor for how many questions the seller has answered for the item
	 * @return answersFactor
	 */
	public double getAnswersFactor(){
		return this.answersFactor;
	}

	/**
	 * Gets the Response Time threshold of the user in milliseconds above which
	 * the user is inclined to leave the website
	 * @return
	 */
	public int getRTThreshold(){
		return this.RTthreshold;
	}

	/**
	 * Tells the client to exit the website
	 * @param val - true makes the client exit
	 */
	public void setExit(boolean val){
		this.exit=val;
	}

	/**
	 * Tracks that the number of items the client has bought has increased by one
	 */
	public void incItemsBought(){
		this.itemsBought++;
	}

	/**
	 * Get the number of items the client has bought
	 * @return
	 */
	public int getItemsBought(){
		return this.itemsBought;
	}

	/**
	 * Tracks that the number of items the client has bid on has increased by one
	 */
	public void incItemsBid(){
		this.itemsBid++;
	}

	/**
	 * Gets the number of items the client has bid on
	 * @return
	 */
	public int getItemsBid(){
		return this.itemsBid;
	}

	/**
	 * Tracks that the number of items the client has put up for sale has increased by one
	 */
	public void incItemsSold(){
		this.itemsSold++;
	}

	/**
	 * Gets the number of items the client has put up for sale
	 * @return
	 */
	public int getItemsSold(){
		return this.itemsSold;
	}

	/**
	 * Returns the exponential factor to change page transition probabilities based on number of items sold
	 * @return
	 */
	public double getExpSoldRate(){
		return this.expSoldRate;
	}

	/**
	 * Returns the exponential factor to change page transition probabilities based on number of items bid on/bought
	 * @return
	 */
	public double getExpBuyRate(){
		return this.expBuyRate;
	}

	/**
	 * @return the previousSearchTerm
	 */
	public StringBuilder getPreviousSearchTerm() {
		return previousSearchTerm;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @param previousSearchTerm the previousSearchTerm to set
	 */
	public void setPreviousSearchTerm(StringBuilder previousSearchTerm) {
		this.previousSearchTerm = new StringBuilder(previousSearchTerm);
	}

	/**
	 * Adds an item to the list of items the client is currently bidding on
	 * @param itemID - the itemID in the database
	 * @param item
	 */
	public void addToCurrentBids(long itemID, ItemCG item) {
		this.currentBids.put(itemID, item);
	}

	/**
	 * Adds an item to the list of items the client has bid on whose auctions have finished
	 * @param itemID - the itemID in the database
	 * @param item
	 */
	public void addToEndedAuctions(long itemID, ItemCG item) {
		this.endedAuctions.put(itemID, item);
	}

	/**
	 * Adds an item to the list of items the client has purchased (buyNow)
	 * @param itemID - the itemID in the database
	 * @param item
	 */
	public void addToPurchasedItems(long itemID,ItemCG item){
		this.purchasedItems.put(itemID, item);
	}

	/**
	 * Adds an item to the list of items the client is currently selling
	 * @param itemID - the itemID in the database
	 * @param item
	 */
	public void addToSellingItems(long itemID,ItemCG item){
		this.sellingItems.put(itemID, item);
	}

	/**
	 * Adds an item to the list of items the client has already sold
	 * @param itemID - the itemID in the database
	 * @param item
	 */
	public void addToSoldItems(long itemID,ItemCG item){
		this.soldItems.put(itemID, item);
	}

	/**
	 * Returns the map of items the client is currently bidding on
	 * @return
	 */
	public TreeMap<Long,ItemCG> getCurrentBids(){
		return this.currentBids;
	}

	/**
	 * Returns the map of items the client has bid on whose auctions have already ended
	 * @return
	 */
	public TreeMap<Long,ItemCG> getEndedAuctions(){
		return this.endedAuctions;
	}

	/**
	 * Returns the map of items the client has purchased (buyNow)
	 * @return
	 */
	public TreeMap<Long,ItemCG> getPurchasedItems(){
		return this.purchasedItems;
	}

	/**
	 * Returns the map of items the client is currently selling
	 * @return
	 */
	public TreeMap<Long,ItemCG> getSellingItems(){
		return this.sellingItems;
	}

	/**
	 * Returns the map of items the client has already sold
	 * @return
	 */
	public TreeMap<Long,ItemCG> getSoldItems(){
		return this.soldItems;
	}

	/**
	 * Resets the item listings of what the client has bid/sold
	 * This is done when accessing the MyAccount page to update each
	 * map to the most recent data
	 */
	public void resetMyAccountItems(){
		this.currentBids.clear();
		this.endedAuctions.clear();
		this.purchasedItems.clear();
		this.sellingItems.clear();
		this.soldItems.clear();
	}

	/**
	 * Adds an item to the item of interests
	 * @param itemID - itemID in database
	 * @param item
	 */
	public void addToItemsOfInterest(long itemID, ItemCG item){
		clientInfo.getItemsOfInterest().put(itemID, item);
		clientInfo.getItemsOfInterestRatings().put(itemID,item.getItemRating());
	}

	/**
	 * Removes an item from the items of interest
	 * @param itemID - itemID in database
	 */
	public void removeItemOfInterest(long itemID){
//		if (clientInfo.getItemsOfInterest().containsKey(itemID)) {
			clientInfo.getItemsOfInterest().remove(itemID);
//		}
//		if (clientInfo.getItemsOfInterestRatings().containsKey(itemID)) {
			clientInfo.getItemsOfInterestRatings().remove(itemID);
//		}
	}

	/**
	 * Returns the map of the items of interest
	 * @return
	 */
	public TreeMap<Long,ItemCG> getItemsOfInterest(){
		return clientInfo.getItemsOfInterest();
	}

	/**
	 * Returns a map of the ratings of the items of interest
	 * @return
	 */
	public TreeMap<Long,Double> getItemsOfInterestRatings(){
		return clientInfo.getItemsOfInterestRatings();
	}

	/**
	 * Adds an open tab to the list of open tabs
	 * @param page - the page of the open tabs
	 */
	public synchronized void addOpenTab(ItemPage page){
		this.openTabs.add(page);
	}

	/**
	 * Gets the array of open page tabs
	 * @return
	 */
	public ArrayList<ItemPage> getOpenTabs(){
		return this.openTabs;
	}

	/**
	 * Records the itemID of the last item opened
	 * @param id
	 */
	public void setLastItemID(long id){
		this.lastItemID=id;
	}

	/**
	 * Gets the itemID of the last item opened
	 * @return
	 */
	public long getLastItemID(){
		return this.lastItemID;
	}

	/**
	 * Adds HTML5 error from loaded page
	 * @param error
	 */
	public void addError(String error){
		this.errors.add(error);
	}

	/**
	 * Clears the HTML5 Errors from the loaded page
	 * Used when loading a new page
	 */
	public void clearErrors(){
		this.errors.clear();
	}

	/**
	 * Gets a list of errors on the HTML5 page
	 * @return
	 */
	public ArrayList<String> getErrors(){
		return this.errors;
	}

	/**
	 * Increases the counter that tracks the number of pages which have been opened
	 */
	public void incNumPagesOpened(){
		this.numPagesOpened++;
	}

	/**
	 * Get how many pages the client has opened
	 * @return
	 */
	public int getNumPagesOpened(){
		return this.numPagesOpened;
	}

	/**
	 * Increases the total response time of all pages by the response time of the current page
	 * @param newRT
	 */
	public void incTotalRT(long newRT){
		this.totalRT+=newRT;
	}

	/**
	 * Gets the total response time for all visited pages
	 * @return
	 */
	public long getTotalRT(){
		return this.totalRT;
	}

	/**
	 * Increases the number of request errors
	 */
	public void incRequestErrors(){
		this.requestErrors++;
	}

	/**
	 * Gets the number of request errors
	 * @return
	 */
	public int getRequestErrors(){
		return this.requestErrors;
	}

	/**
	 * Returns the client generator
	 * @return
	 */
	public ClientGenerator getCg(){
		return this.cg;
	}

	/**
	 * Sets flag indicating if the client is exiting due to an error
	 * @param val - true if exiting due to error
	 */
	public void setExitDueToError(boolean val){
		this.exitDueToError=val;
	}

	/**
	 * Sets the original logout probability on a page before modified due to response time considerations
	 * @param origLogoutProb
	 */
	public void setOrigLogoutProb(double origLogoutProb){
		this.origLogoutProb=origLogoutProb;
	}

	/**
	 * Sets logout probability on a page after modified due to response time considerations
	 * @param finalLogoutProb
	 */
	public void setFinalLogoutProb(double finalLogoutProb){
		this.finalLogoutProb=finalLogoutProb;
	}

	/**
	 * Sets the probability of all actions that are not the logout probability before normalization
	 * @param restProb
	 */
	public void setRestProb(double restProb){
		this.restProb=restProb;
	}

	/**
	 * Returns the set of URLs for the CMART application
	 * @return
	 */
	public CMARTurl getCMARTurl(){
		return this.cmarturl;
	}

	/**
	 * Returns the HTTPClient
	 * @return
	 */
	public CloseableHttpClient getHttpClient(){
		return httpclient;
	}

	/**
	 * Returns the additional network delay average to use on a client
	 * @return
	 */
	public long getNetworkDelay(){
		return this.networkDelay;
	}

	/**
	 * Retruns the XML Document that the client's actions are being written to
	 * @return
	 */
	public Document getXMLDocument(){
		return this.xmlDocument;
	}

	/**
	 * @return the readXmlDocument
	 */
	public Document getReadXmlDocument() {
		return readXmlDocument;
	}

	/**
	 * @param readXmlDocument the readXmlDocument to set
	 */
	public void setReadXmlDocument(Document readXmlDocument) {
		this.readXmlDocument = readXmlDocument;
	}

	/**
	 * Adds the client's action to the XML document
	 * @param action
	 */
	public void addXMLAction(Element action){
		actionsElement.appendChild(action);		
	}

	/**
	 * Returns the action number from the client and increases the actionNum by 1
	 * @return number of actions the client as doen
	 */
	public String getActionNum(){
		actionNum++;
		return Integer.toString(actionNum-1);
	}

	/**
	 * Outputs the Client XML document of all the client's actions upon the client exiting the system
	 */
	private void outputClientXML(){
		if(!RunSettings.isRepeatedRun()){
			try{
				FileWriter fstreamA = new FileWriter(new StringBuilder(RunSettings.getRepeatedXmlFolder()).append("client").append(clientInfo.getClientIndex()).append(".xml").toString(),true);
				BufferedWriter out = new BufferedWriter(fstreamA);

				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");

				//initialize StreamResult with File object to save to file
				StreamResult result = new StreamResult(new StringWriter());
				DOMSource source = new DOMSource(xmlDocument);
				transformer.transform(source, result);

				String xmlString = result.getWriter().toString();
				out.write(xmlString);

				out.close();
			}catch(Exception e){
				e.printStackTrace();
				System.err.println("Could not output page response time data");
			}
		}
	}

	/**
	 * @return the exitDueToRepeatChange
	 */
	public boolean isExitDueToRepeatChange() {
		return exitDueToRepeatChange;
	}

	/**
	 * @param exitDueToRepeatChange the exitDueToRepeatChange to set
	 */
	public void setExitDueToRepeatChange(boolean exitDueToRepeatChange) {
		this.exitDueToRepeatChange = exitDueToRepeatChange;
	}

	/**
	 * @return the changeDueToRepeatChange
	 */
	public boolean isChangeDueToRepeatChange() {
		return changeDueToRepeatChange;
	}

	/**
	 * @param changeDueToRepeatChange the changeDueToRepeatChange to set
	 */
	public void setChangeDueToRepeatChange(boolean changeDueToRepeatChange) {
		this.changeDueToRepeatChange = changeDueToRepeatChange;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientInfo == null) ? 0 : clientInfo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return clientInfo.equals(((Client)obj).clientInfo);
	}
	
	
}
