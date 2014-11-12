package client.clientMain;

import java.awt.Image;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.lang.RandomStringUtils;

import client.Items.ItemCG;
import client.Items.QuestionCG;
import client.Items.SellerCG;


/**
 * Contains all information for a client
 * @author Andrew Fox
 *
 */

public class ClientInfo {
	private Random rand = new Random();

	private StringBuilder username;						// username of client
	private StringBuilder firstName;						// first name of client
	private StringBuilder lastName;						// last name of client
	private StringBuilder address=new StringBuilder();	// address of client
	private StringBuilder city;							// city of client
	private StringBuilder zipcode=new StringBuilder();	// zipcode of client
	private StringBuilder USState;						// USState of client
	private StringBuilder USStateCode;					// USState code of client
	private StringBuilder email;							// email of client
	private StringBuilder password;						// password of client
	private StringBuilder creditCardNumber;				// credit card number of client
	private StringBuilder cvv2;							// cvv2 of client's credit card
	private StringBuilder creditCardExpiry;				// expiry date of client's credit card
	private boolean registered=false;					// if the client is already a registered user of cmart
	private StringBuilder userID=new StringBuilder();		// userID number of client in cmart database (for HTML5)
	private long clientIndex;							// index for the client generator indicating order clients were created
	private StringBuilder authToken;						// client's authToken for C-MART

	private TreeMap<String, Image> imageCache = new TreeMap<String, Image>();                   
	// cache of images viewed by the client
	private TreeMap<String, StringBuilder> jscssCache = new TreeMap<String, StringBuilder>();
	// cache of the javascript files already opened by the client
	
	
	
	
	
	private TreeMap<String,StringBuilder>HTML5Cache=new TreeMap<String,StringBuilder>();		// cache of HTML5 data
	private TreeMap<Long,ItemCG>HTML5ItemCache=new TreeMap<Long,ItemCG>();					// cache of the Item data for HTML5
	private TreeMap<Long,SellerCG>HTML5SellerCache=new TreeMap<Long,SellerCG>();			// cache of the Seller data for HTML5
	private TreeMap<Long,QuestionCG>HTML5QuestionCache=new TreeMap<Long,QuestionCG>();		// cache of the Question data for HTML5
	
	private TreeMap<String,Image>preImageCache=new TreeMap<String,Image>();					// cache of images viewed by the client
	private TreeMap<String,StringBuilder>preJscssCache=new TreeMap<String,StringBuilder>();	// cache of the javascript files already opened by the client

	private  TreeMap<Long,ItemCG> itemsOfInterest=new TreeMap<Long,ItemCG>();			// tracks items the user has shown interest in, from viewing the item, to seeing it on a browse/search page
	private  TreeMap<Long,Double> itemsOfInterestRatings=new TreeMap<Long,Double>();	// tracks the item ratings of the items of interest


	/**
	 * Creates information for a client if their username and password is already known
	 * @param username
	 * @param password
	 */
	public ClientInfo(StringBuilder username,StringBuilder password){
		this.username=username;
		this.password=password;
		createNewCreditCard();
		createName();
		createAddress();
		this.registered=true;

	}

	/**
	 * Creates client information for client already in CMART database
	 * @param userID
	 * @param firstName
	 * @param lastName
	 * @param username
	 * @param password
	 * @param email
	 * @param address
	 * @param city
	 * @param zip
	 * @param state
	 */
	public ClientInfo(String userID,String firstName,String lastName,String username,String password,String email,String address,String city,String zip,String state){
		this.userID=new StringBuilder(userID);
		this.firstName=new StringBuilder(firstName);
		this.lastName=new StringBuilder(lastName);
		this.username=new StringBuilder(username);
		this.password=new StringBuilder(password);
		this.email=new StringBuilder(email);
		this.address=new StringBuilder(address);
		this.city=new StringBuilder(city);
		this.zipcode=new StringBuilder(zip);
		this.USStateCode=new StringBuilder(state);

		this.registered=true;
		createNewCreditCard();
		
//		synchronized(this){
			if (rand.nextInt()>0.7){
				prepopulateCache();
			}
			this.prepopulateToCache();	// prepopulates the cache
//		}
	}

	/**
	 * Creates a completely new client information
	 */
	public ClientInfo(){
		createName();
		createUserNameAndPassword();
		createNewCreditCard();
		createAddress();
	}

	/**
	 * Creates client username from distribution and accompanying password
	 */
	private void createUserNameAndPassword(){
		this.username=new StringBuilder(this.firstName).append(this.lastName).append(randomDigits(4));
		int pwdLength=5+rand.nextInt(8);
		StringBuilder pwd=new StringBuilder(RandomStringUtils.randomAlphanumeric(pwdLength));
//		for (int i=0;i<pwdLength;i++){
//			pwd=pwd.append((char)(rand.nextInt(78)+48));
//		}
		this.password=pwd;
	}

	/**
	 * Returns a random number of set length
	 * @param numDigits - number of digits in the random long
	 * @return The random number of set length
	 */
	private long randomDigits(int numDigits){
		long ret=Math.round(rand.nextDouble()*Math.pow(10,numDigits));
		while(ret<Math.pow(10, numDigits-1)){
			ret=Math.round(rand.nextDouble()*Math.pow(10,numDigits));
		}
		return ret;
	}

	/**
	 * Creates a new credit card number and cvv2
	 * Credit card number follows Luhn's Algorithm
	 */
	private void createNewCreditCard(){
		String cardNum=Long.toString(randomDigits(15));
		int luhnSum=0;
		for (int i=0;i<cardNum.length();i++){
			if (i%2==1){
				luhnSum+=Integer.parseInt(Character.toString(cardNum.charAt(i)));
			}
			else{
				int num=Integer.parseInt(Character.toString(cardNum.charAt(i)))*2;
				int sum=0;
				while(num > 0)
				{
					sum += num%10;
					num /= 10;	
				}	
				luhnSum+=sum;
			}
		}
		int lastDigit=luhnSum%10;
		if (lastDigit==0)
			lastDigit=10;
		this.creditCardNumber=new StringBuilder(cardNum.concat(Integer.toString(10-lastDigit)));
		this.cvv2=new StringBuilder(Long.toString(randomDigits(3)));

		StringBuilder month=new StringBuilder(String.format("%02d", rand.nextInt(12)));
		String year=Long.toString(rand.nextInt(3)+Calendar.getInstance().get(Calendar.YEAR)+1);
		this.creditCardExpiry=month.append(year);


	}

	/**
	 * Binary search algorithm for finding elements in given distributions
	 * @param keyset
	 * @param value
	 * @param low
	 * @param high
	 * @return
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
	 * Creates a name for the user from a distribution
	 */
	private void createName(){
		double prob=rand.nextDouble();
		if (prob>0.5){
			double index=binarySearch((Double[])RunSettings.getMaleFirstNames().keySet().toArray(new Double[RunSettings.getMaleFirstNames().size()]),rand.nextDouble(),0,RunSettings.getMaleFirstNames().size()-1);
			this.firstName=RunSettings.getMaleFirstNames().get(index);
		}
		else{
			double index=binarySearch((Double[])RunSettings.getFemaleFirstNames().keySet().toArray(new Double[RunSettings.getFemaleFirstNames().size()]),rand.nextDouble(),0,RunSettings.getFemaleFirstNames().size()-1);
			this.firstName=RunSettings.getFemaleFirstNames().get(index);
		}

		double index=binarySearch((Double[])RunSettings.getLastNames().keySet().toArray(new Double[RunSettings.getLastNames().size()]),rand.nextDouble(),0,RunSettings.getLastNames().size()-1);
		this.lastName=RunSettings.getLastNames().get(index);

		this.email=(new StringBuilder(this.firstName).append(".").append(this.lastName).append(randomDigits(4)).append("@").append("fakeMail.com"));
	}

	/**
	 * Creates an address for a user from a distribution
	 */
	public void createAddress(){
		
		double index=binarySearch((Double[])RunSettings.getStreetNames().keySet().toArray(new Double[RunSettings.getStreetNames().size()]),rand.nextDouble(),0,RunSettings.getStreetNames().size()-1);

		this.address = new StringBuilder();
		this.address=this.address.append(rand.nextInt(1000)).append(" ");
		this.address=this.address.append(RunSettings.getStreetNames().get(index));
		int suffix=rand.nextInt(5);
		if (suffix==0)
			this.address=this.address.append(" Ave.");
		else if (suffix==1)
			this.address=this.address.append(" Rd.");
		else if (suffix==2)
			this.address=this.address.append(" St.");
		else if (suffix==3)
			this.address=this.address.append(" Dr.");
		else if (suffix==4)
			this.address=this.address.append(" Blvd.");

		index=binarySearch((Double[])RunSettings.getCities().keySet().toArray(new Double[RunSettings.getCities().size()]),rand.nextDouble(),0,RunSettings.getCities().size()-1);


		this.city=RunSettings.getCities().get(index);
		this.USState=new StringBuilder((RunSettings.getCityStates().get(this.city.toString())).toString().toUpperCase());
		this.USStateCode=new StringBuilder(Integer.toString(determineUSStateCode()));


		this.zipcode = new StringBuilder(String.format("%05d", rand.nextInt(100000)));
	}

	/**
	 * Gets the alphabetical number of the state from its name
	 * @return The state number
	 */
	private int determineUSStateCode(){
		return client.Tools.USState.valueOf(USState.toString().replace(' ', '_')).getCode();
	}

	/**
	 * Returns the client username
	 * @return
	 */
	public StringBuilder getUsername(){
		return this.username;
	}
	/**
	 * Returns the client first name
	 * @return
	 */
	public StringBuilder getFirstName(){
		return this.firstName;
	}
	/**
	 * Returns the client last name
	 * @return
	 */
	public StringBuilder getLastName(){
		return this.lastName;
	}
	/**
	 * Returns the client's address
	 * @return
	 */
	public StringBuilder getAddress(){
		return this.address;
	}
	/**
	 * Returns the client's city
	 * @return
	 */
	public StringBuilder getCity(){
		return this.city;
	}
	/**
	 * Returns the clients zipcode
	 * @return
	 */
	public StringBuilder getZipcode(){
		return this.zipcode;
	}
	/**
	 * Returns the client's US State
	 * @return
	 */
	public StringBuilder getUSState(){
		return this.USState;
	}
	/**
	 * Returns the numerical code as a StringBuilder for the client's US state
	 * @return
	 */
	public StringBuilder getUSStateCode(){
		return this.USStateCode;
	}
	/**
	 * Returns the client's email
	 * @return
	 */
	public StringBuilder getEmail(){
		return this.email;
	}
	/**
	 * Returns the client's password
	 * @return
	 */
	public StringBuilder getPassword(){
		return this.password;
	}
	/**
	 * Returns the client's credit card number
	 * @return
	 */
	public StringBuilder getCreditCardNum(){
		return this.creditCardNumber;
	}
	/**
	 * Returns the client's cvv2 credit card code
	 * @return
	 */
	public StringBuilder getCvv2(){
		return this.cvv2;
	}
	/**
	 * Returns the expiry date of the client's credit card
	 * @return
	 */
	public StringBuilder getCreditCardExpiry(){
		return this.creditCardExpiry;
	}
	/**
	 * Returns a boolean indicating if the client is already registered in the application
	 * @return
	 */
	public boolean isRegistered(){
		return this.registered;
	}
	/**
	 * Returns the client's userID in the database
	 * @return
	 */
	public StringBuilder getUserID(){
		return this.userID;
	}
	/**
	 * Sets if the client is registered in the application
	 * @param val
	 */
	public void setRegistered(boolean val){
		this.registered=val;
	}
	/**
	 * Sets the client's username
	 * @param name
	 */
	public void setUsername(StringBuilder name){
		this.username=name;
	}
	public void setUserID(StringBuilder userID){
		this.userID=userID;
	}
	/**
	 * Sets the client's email
	 * @param email
	 */
	public void setEmail(StringBuilder email){
		this.email=email;
	}
	/**
	 * Sets the client's password
	 * @param password
	 */
	public void setPassword(StringBuilder password){
		this.password=password;
	}

	/**
	 * @return the clientIndex
	 */
	public long getClientIndex() {
		return clientIndex;
	}

	/**
	 * @param clientIndex the clientIndex to set
	 */
	public void setClientIndex(long clientIndex) {
		this.clientIndex = clientIndex;
	}

	/**
	 * @return the authToken
	 */
	public StringBuilder getAuthToken() {
		return new StringBuilder(authToken);
	}

	/**
	 * @param authToken the authToken to set
	 */
	public void setAuthToken(StringBuilder authToken) {
		this.authToken = authToken;
	}

	/**
	 * Adds an image to the cache
	 * @param url - url of the image
	 * @param img - the image
	 */
	public void addImageCache(String url,Image img){
		this.imageCache.put(url,img);
//		synchronized(imageCache){
//			this.imageCache.put(url,img);
//		}
	}
	/**
	 * Determines if an image has already been cached
	 * @param url - url of the image
	 * @return Boolean indicating if image is in cache
	 */
	public boolean inImageCache(String url){
		return this.imageCache.containsKey(url);
//		synchronized(imageCache){
//			return this.imageCache.containsKey(url);
//		}
	}

	/**
	 * @return Copy of Image Cache collection for iteration.
	 */
	public TreeMap<String,Image>getImageCache(){
		return this.imageCache;
//		synchronized(imageCache){
//			return new TreeMap<String, Image>(this.imageCache);
//		}
	}

	/**
	 * @returnCopy of JS/CSS Cache collection for iteration.
	 */
	public TreeMap<String,StringBuilder>getJscssCache(){
		return this.jscssCache;
//		synchronized(jscssCache){
//			return new TreeMap<String, StringBuilder>(this.jscssCache);
//		}
	}

	/**
	 * Determines if a js file is in the js cache
	 * @param url - url of the js file
	 * @return Boolean indicating if js file is in cache
	 */
	public boolean inJSCSSCache(String url){
		return this.jscssCache.containsKey(url);
//		synchronized(jscssCache){
//			return this.jscssCache.containsKey(url);
//		}
	}
	/**
	 * Adds a js file to the js cache
	 * @param url - url of the js file
	 * @param js - the javascript file
	 */
	public void addJSCSSCache(String url,StringBuilder js){
		this.jscssCache.put(url,js);
//		synchronized(jscssCache){
//			this.jscssCache.put(url,js);
//		}
	}

	/**
	 * Adds a value to the HTML5 cache
	 * @param key
	 * @param val
	 */
	public void addHTML5Cache(String key,StringBuilder val){
		this.HTML5Cache.put(key,val);
//		synchronized(HTML5Cache){
//			this.HTML5Cache.put(key,val);
//		}
	}
	/**
	 * Determines if a value for a given key is in the HTML5 cache
	 * @param key
	 * @return
	 */
	public boolean inHTML5Cache(String key){
		return this.HTML5Cache.containsKey(key);
//		synchronized(HTML5Cache){
//			return this.HTML5Cache.containsKey(key);
//		}
	}
	/**
	 * Gets the HTML5 Cache
	 * @return
	 */
	public Map<String,StringBuilder> getHTML5Cache(){
		return this.HTML5Cache;
//		synchronized(HTML5Cache){
//			return new TreeMap<String, StringBuilder>(this.HTML5Cache);
//		}
	}

	/**
	 * @return the HTML5ItemCache
	 */
	public TreeMap<Long,ItemCG> getHTML5ItemCache() {
		return this.HTML5ItemCache;
//		synchronized(HTML5ItemCache){
//			return new TreeMap<Long, ItemCG>(this.HTML5ItemCache);
//		}
	}
	/**
	 * @return the HTML5SellerCache
	 */
	public TreeMap<Long,SellerCG> getHTML5SellerCache(){
		return this.HTML5SellerCache;
//		synchronized(HTML5SellerCache){
//			return new TreeMap<Long, SellerCG>(this.HTML5SellerCache);
//		}
	}
	/**
	 * @return the HTML5QuestionCache
	 */
	public TreeMap<Long,QuestionCG> getHTML5QuestionCache(){
		return this.HTML5QuestionCache;
//		synchronized(HTML5QuestionCache){
//			return new TreeMap<Long,QuestionCG>(this.HTML5QuestionCache);
//		}
	}

	/**
	 * Adds an Item to the HTML5 cache
	 * @param item - item to be added to the cache
	 */
	public void addHTML5ItemCache(ItemCG item) {
		this.HTML5ItemCache.put(item.getId(), item);
//		synchronized(HTML5ItemCache){
//			this.HTML5ItemCache.put(item.getId(), item);
//		}
	}
	/**
	 * Adds a seller to the HTML5SellerCache
	 * @param seller
	 */
	public void addHTML5SellerCache(SellerCG seller) {
		this.HTML5SellerCache.put(seller.getId(), seller);
//		synchronized(HTML5SellerCache){
//			this.HTML5SellerCache.put(seller.getId(), seller);
//		}
	}	
	/**
	 * Adds a question to the HTML5QuestionCache
	 * @param question
	 */
	public void addHTML5QuestionCache(QuestionCG question) {
		this.HTML5QuestionCache.put(question.getId(), question);
//		synchronized(HTML5QuestionCache){
//			this.HTML5QuestionCache.put(question.getId(), question);
//		}
	}
	

	/**
	 * Clears all data from the image, css, js, HTML5, HTML5Items, HTML5Seller caches
	 */
	public void clearCaches(){
//		synchronized (imageCache) {
			imageCache.clear();
//		}
//		synchronized (jscssCache) {
			jscssCache.clear();
//		}
//		synchronized (HTML5Cache) {
			HTML5Cache.clear();
//		}
//		synchronized (HTML5ItemCache) {
			HTML5ItemCache.clear();
//		}
//		synchronized (HTML5SellerCache) {
			HTML5SellerCache.clear();
//		}
	}

	/**
	 * Gets the map of the items of interest
	 * @return
	 */
	public TreeMap<Long,ItemCG>getItemsOfInterest(){
		return this.itemsOfInterest;
	}
	/**
	 * Gets the map of the ratings of the items of interest
	 * @return
	 */
	public TreeMap<Long,Double>getItemsOfInterestRatings(){
		return this.itemsOfInterestRatings;
	}

	/**
	 * Initiates list of items which are on the website which can be used for prepopulating the cache
	 */
	private void prepopulateCache(){
		preImageCache.put("images/header/application.png",null);
		preImageCache.put("images/cmart.jpg",null);
		preImageCache.put("images/header/home.png",null);
		preImageCache.put("images/header/browse.png",null);
		preImageCache.put("images/header/application.png",null);
		preImageCache.put("images/header/sell.png",null);
		preImageCache.put("images/header/video.png",null);
		preImageCache.put("images/header/videoup.png",null);
		preImageCache.put("images/header/logout.png",null);
		preImageCache.put("images/misc/button-gloss.png",null);
		preImageCache.put("images/orbit/bullets.jpg",null);
		preImageCache.put("images/arrow_down.png",null);
		preImageCache.put("js/themes/light/pointer.png",null);
		preImageCache.put("js/themes/light/next.png",null);
		preImageCache.put("js/themes/light/prev.png",null);

		preJscssCache.put("js/jquery-1.7.2.min.js",null);
		preJscssCache.put("css/jquery-ui-1.8.11.custom.css",null);
		preJscssCache.put("js/jquery-ui-1.8.11.custom.min.js",null);
		preJscssCache.put("item_image_style.css",null);
		preJscssCache.put("home_recommendation.css",null);
		preJscssCache.put("stylesheets/foundation.css",null);
		preJscssCache.put("js/jquery-1.3.2.min.js",null);
		preJscssCache.put("js/itemPriceUpdate.js",null);
		preJscssCache.put("js/jquery.easing.1.3.js",null);
		preJscssCache.put("js/jquery.galleryview-1.1.js",null);
		preJscssCache.put("js/jquery.timers-1.1.2.js",null);
		preJscssCache.put("js/database.js",null);
		preJscssCache.put("js/onload.js",null);
		preJscssCache.put("js/login.js",null);
		preJscssCache.put("js/utilities.js",null);
		preJscssCache.put("js/getRecommendationItem.js",null);
		preJscssCache.put("js/getRecommendationItemHTML5.js",null);
		preJscssCache.put("css/header.css",null);
		preJscssCache.put("css/myaccount.css",null);
		preJscssCache.put("css/body.css",null);
		preJscssCache.put("css/errors.css",null);
		preJscssCache.put("js/selectmenus.js",null);
		preJscssCache.put("js/date.format.js",null);
		preJscssCache.put("js/jquery-ui-timepicker-addon.js",null);
		preJscssCache.put("js/jquery-ui-sliderAccess.js",null);
		preJscssCache.put("js/itemPrefetch.js",null);

	}

	/**
	 * Moves the prepopulated items to the actual caches
	 */
	public void prepopulateToCache(){
		imageCache.putAll(preImageCache);
		jscssCache.putAll(preJscssCache);
		preImageCache.clear();
		preJscssCache.clear();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return username.equals(((ClientInfo) obj).username);
	}
	
	
}
