package client.clientMain;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * Initial Settings for client generator
 * @author afox1
 *
 */

public class RunSettings {
	private static StringBuilder csvFolder=new StringBuilder();		// folder the CSV distributions are in
	private static StringBuilder repeatedXmlFolder=new StringBuilder();
	private static boolean verbose;					// output debug information
	private static boolean outputStats;				// output Stats from the run
	private static boolean outputMatlab;			// if the data should be output in .mat form
	private static boolean HTML4;					// if the HTML4 version is to be used
	private static boolean outputThinkTimes;		// if a think time histogram should be output
	private static double thinkTimeSpeedUpFactor;	// factor to speed up all the think times by
	private static int connPerPage=10;				// max simultaneous connections that can be opened to get css, images, js on each page
	private static double clearCacheOnExit=0.2;		// probability that the caches are cleared when a user leaves the system
	private static boolean cacheRealData=false;		// if real data should be cached or just an indicator
	private static boolean outputSiteData=false;	// if data from the statistics page should be output at the end of the run
	private static String outputSiteDataFile=null;	// directory where stats files should be output
	private static double RTthreshold=1000;			// Response time threshold before client starts disliking service in milliseconds
	private static boolean getExtras=true;			// flag indicating if js, css, images should be downloaded on page accesses
	private static boolean staticUserload=false;	// if closed loop client generator is used
	private static int rampupTime;					// rampup time for closed loop client generator
	private static boolean tabbedBrowsing=true;		// if tabbed browsing is to be used
	private static int workloadType=2;				// workload type (1/2/3 = read/normal/write heavy)
	private static boolean allowBursts=true;		// if client bursts should be allowed
	private static boolean markovTransitions=false;	// if Markov page transitions are to be used
	private static boolean networkDelay=false;		// whether to add a fake network delay or not
	private static double networkDelayAvg=0;		// average of exponential distribution for added network delay (in milliseconds)
	private static boolean localImages=false;		// if images are on the app server or on a separate image server
	private static boolean repeatedRun=false;		// if the run is a repeat of a previous run

	private static LinkedList<CMARTurl>URLs=new LinkedList<CMARTurl>();		// LinkedList of C-MART urls, for distributed application

	private static double meanClientsPerMinute;		// average number of clients to generate per minute
	private static int stableUsers;					// max number of active clients in the website
	private static long timeToRun;					// minutes to run client generator

	private static TreeMap<Long,Integer>changeStableUsers=new TreeMap<Long,Integer>();			// map of times into the run to change the amount of stable users <seconds into run,newStableUsers>
	private static TreeMap<Long,Double>changeMeanClientsPerMinute=new TreeMap<Long,Double>();	// map of times into the run to change the amount of mean clients per minute <seconds into run, newMeanClientsPerMinute>
	private static TreeMap<Long,Double>changePeakUsersSlope=new TreeMap<Long,Double>();
	private static boolean ableToRun=true;			// if the proper parameters have been supplied allowing the ClientGenerator to run

	// Distributions
	private static TreeMap<Double,Double> typingSpeedDist=new TreeMap<Double,Double>();		// typing speed distribution
	private static TreeMap<Double,Double> typingErrorRate=new TreeMap<Double,Double>();		// typing error rate distribution as a function of typing speed
	private static TreeMap<Double,Integer>numHeaderImages=new TreeMap<Double,Integer>();	// number of images per item
	private static ArrayList<String>itemPics=new ArrayList<String>();						// pictures to upload for an item
	private static TreeMap<Double,Integer>titleTotalWords=new TreeMap<Double,Integer>();	// number of words in the title
	private static TreeMap<Double,StringBuilder>titleWords=new TreeMap<Double,StringBuilder>();	// frequency of words in the title
	private static TreeMap<Double,Integer>itemSpecificsCat=new TreeMap<Double,Integer>();		// number of words in the categories
	private static TreeMap<Double,StringBuilder>itemSpecificsCatWords=new TreeMap<Double,StringBuilder>();	// frequency of words i nthe categories
	private static TreeMap<Double,Integer>itemSpecificsDesc=new TreeMap<Double,Integer>();		// number of words in the description
	private static TreeMap<Double,StringBuilder>itemSpecificsDescWords=new TreeMap<Double,StringBuilder>();	// frequency of words in the description
	private static TreeMap<Double,StringBuilder>maleFirstNames=new TreeMap<Double,StringBuilder>();			// distribution of male first names
	private static TreeMap<Double,StringBuilder>femaleFirstNames=new TreeMap<Double,StringBuilder>();			// distribution of female first names
	private static TreeMap<Double,StringBuilder>lastNames=new TreeMap<Double,StringBuilder>();				// distribution of last names
	private static TreeMap<Double,StringBuilder>streetNames=new TreeMap<Double,StringBuilder>();				// distribution of street names
	private static TreeMap<Double,StringBuilder>USStates=new TreeMap<Double,StringBuilder>();					// distribution of US States
	private static TreeMap<Double,StringBuilder>cities=new TreeMap<Double,StringBuilder>();					// distribution of cities
	private static TreeMap<String,StringBuilder>cityStates=new TreeMap<String,StringBuilder>();				// mapping cities to their respective states
	private static TreeMap<Double,StringBuilder>auctionType=new TreeMap<Double,StringBuilder>();				// whether the item for sale is auction/buyNow/both
	private static TreeMap<Double,Double>buyNowPrice=new TreeMap<Double,Double>();							// distribution of initial buy now prices
	private static TreeMap<Double,Double>buyNowPriceReverse=new TreeMap<Double,Double>();					// reverse distribution of the buyNow prices
	private static TreeMap<Double,Double>startingBid=new TreeMap<Double,Double>();							// distribution of starting bids
	private static TreeMap<Double,Integer>numSearchWords=new TreeMap<Double,Integer>();						// number of words to use in a search query
	private static TreeMap<Double,Integer>categories=new TreeMap<Double,Integer>();							// distribution of categories for an item
	private static TreeMap<Long,Long>categoriesParents=new TreeMap<Long,Long>();							// distribution of categories for an item
	private static TreeMap<Long,ArrayList<Long>> categoriesParentsStorage=new TreeMap<Long,ArrayList<Long>>();	// mapping of category parentIDs to their child categories
	private static TreeMap<Integer,ArrayList<Double>> transitionProbabilities=new TreeMap<Integer,ArrayList<Double>>();

	public static void main(String[] args){
		
		// FIX PROBLEM WHEN RUNNING CLIENT ON NON-ENGLISH LOCALE
		Locale.setDefault(Locale.ENGLISH);
		System.out.println("START= " + System.currentTimeMillis());

		System.out.println("Initializing Distributions");
		String configFileName="CGconfig.txt";
		for(int aloop=0; aloop<args.length; aloop++){
			if(args[aloop].toLowerCase().equals("config") || args[aloop].toLowerCase().equals("-config")){
				if(aloop+1 <= args.length)
					configFileName = args[aloop+1];
			}
		}
		initializeRunSettings(configFileName);		// initializes the run settings
		if(ableToRun){
			try {
				initializeDistributions();		// adds all the distributions into the TreeMaps
			} catch (IOException e) {
				e.printStackTrace();
			}					
			System.out.println("Starting Client Generator");
			//	Timer t1=new Timer();
			ClientGenerator cg=new ClientGenerator(getCMARTurl());		// starts the client generator
			cg.start();

			try{Thread.sleep(60000*timeToRun);} 			// wait for the specified delay
			catch(InterruptedException e){
				e.printStackTrace();
			}
			exitSystem(cg);
		}
		System.out.println("END= " + System.currentTimeMillis());
	}

	/**
	 * Initializes the distributions into the Client Generator and adds them to TreeMaps
	 * @throws IOException
	 */
	private static void initializeDistributions() throws IOException{
		//Item Pictures
		loadItemImages();	// loads the images that user uploads into the client generator

		// Typing Error Rate
		typingErrorRate.put(108.*5,0.04);
		typingErrorRate.put(63.*5,0.041);
		typingErrorRate.put(55.*5,0.044);
		typingErrorRate.put(48.*5,0.05);
		typingErrorRate.put(43.*5,0.055);
		typingErrorRate.put(37.*5,0.053);
		typingErrorRate.put(33.*5,0.058);
		typingErrorRate.put(29.*5,0.057);
		typingErrorRate.put(25.*5,0.06);
		typingErrorRate.put(20.*5,0.075);


		setNumHeaderImages(loadCSV_DI("numHeaderImages"));
		setTitleTotalWords(loadCSV_DI("titleTotalWords"));
		setItemSpecificsCat(loadCSV_DI("itemSpecificsNumCategories"));
		setItemSpecificsDesc(loadCSV_DI("itemSpecificsDescriptionTotal"));
		setTitleWords(loadCSV_DS("titleWordCount"));
		setItemSpecificsCatWords(loadCSV_DS("itemSpecificsCategoriesWords"));
		setItemSpecificsDescWords(loadCSV_DS("itemSpecificsDescriptionWords"));
		setMaleFirstNames(loadCSV_DS("malefirstnames"));
		setFemaleFirstNames(loadCSV_DS("femalefirstnames"));
		setLastNames(loadCSV_DS("lastnames"));
		setStreetNames(loadCSV_DS("streetNames"));
		setUSStates(loadCSV_DS("USStateCount"));
		setCities(loadCSV_DS("cities"));
		setCityStates(loadCSV_SS("citystates"));
		setAuctionType(loadCSV_DS("auctionType"));
		setBuyNowPrice(loadCSV_DD("buyNowPrice"));
		setBuyNowPriceReverse(reverseMap_DD(buyNowPrice));
		setStartingBid(loadCSV_DD("startingBid"));
		setNumSearchWords(loadCSV_DI("numberSearchWords"));
		setCategories(loadCSV_DI("categoriesCG2"));
		setCategoriesParents(loadCSV_LL("categoriesParents2"));
		setTypingSpeed(loadCSV_DD("typingSpeed"));
		

		loadTransitionProbabilities();
	}


	/**
	 * Loads a distribution of integers from a file
	 * @param file
	 * @return cdf of the doubles
	 * @throws IOException
	 */
	private static TreeMap<Double,Integer> loadCSV_DI(String file) throws IOException{

		TreeMap<Double,Integer> map=new TreeMap<Double,Integer>();
		TreeMap<Double,Integer> map2=new TreeMap<Double,Integer>();
		double probSum=0.;
		FileReader fstream = new FileReader(csvFolder+file+".csv");
		BufferedReader in = new BufferedReader(fstream);

		String s;
		while((s=in.readLine())!=null){
			int commaIndex=s.indexOf(',');
			int value=Integer.parseInt(s.substring(0,commaIndex));
			double key=Double.parseDouble(s.substring(commaIndex+1));
			probSum+=key;
			map.put(probSum, value);
		}
		//normalization
		double prevValue=0.;
		for (double m:map.keySet()){
			map2.put(1-prevValue,map.get(m));
			prevValue=m/probSum;
		}

		in.close();
		return map2;
	}

	/**
	 * Loads a distribution of doubles from a file
	 * @param file
	 * @return cdf of the doubles
	 * @throws IOException
	 */
	private static TreeMap<Double,Double> loadCSV_DD(String file) throws IOException{
		TreeMap<Double,Double> map=new TreeMap<Double,Double>();
		TreeMap<Double,Double> map2=new TreeMap<Double,Double>();
		double probSum=0.;
		FileReader fstream = new FileReader(csvFolder+file+".csv");
		BufferedReader in = new BufferedReader(fstream);

		String s;
		while((s=in.readLine())!=null){
			int commaIndex=s.indexOf(',');
			double value=Double.parseDouble(s.substring(0,commaIndex));
			double key=Double.parseDouble(s.substring(commaIndex+1));
			probSum+=key;
			map.put(probSum, value);
		}
		//normalization
		double prevValue=0.;
		for (double m:map.keySet()){
			map2.put(1-prevValue,map.get(m));
			prevValue=m/probSum;
		}

		in.close();
		return map2;
	}

	/**
	 * Loads a distribution of strings from a file
	 * @param file
	 * @return cdf of StringBuilders
	 * @throws IOException
	 */
	private static TreeMap<Double,StringBuilder> loadCSV_DS(String file) throws IOException{
		TreeMap<Double,StringBuilder> map=new TreeMap<Double,StringBuilder>();
		TreeMap<Double,StringBuilder> map2=new TreeMap<Double,StringBuilder>();
		double probSum=0.;
		FileReader fstream = new FileReader(csvFolder+file+".csv");
		BufferedReader in = new BufferedReader(fstream);

		String s;
		while((s=in.readLine())!=null){
			int commaIndex=s.indexOf(',');
			StringBuilder value=new StringBuilder(s.substring(0,commaIndex));
			double key=Double.parseDouble(s.substring(commaIndex+1));
			probSum+=key;
			map.put(probSum, value);
		}
		//normalization
		double prevValue=0.;
		for (double m:map.keySet()){
			map2.put(1-prevValue,map.get(m));
			prevValue=m/probSum;
		}

		in.close();
		return map2;
	}

	/**
	 * Loads a mapping of strings to StringBuilders from a file
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static TreeMap<String,StringBuilder> loadCSV_SS(String file) throws IOException{
		TreeMap<String,StringBuilder> map=new TreeMap<String,StringBuilder>();
		FileReader fstream = new FileReader(csvFolder+file+".csv");
		BufferedReader in = new BufferedReader(fstream);
		String s;
		while((s=(in.readLine()))!=null){
			int commaIndex=s.indexOf(",");
			String key=s.substring(0,commaIndex);
			StringBuilder value=new StringBuilder(s.substring(commaIndex+1,s.length()));
			map.put(key, value);
		}

		in.close();
		return map;
	}

	private static TreeMap<Long,Long> loadCSV_LL(String file) throws IOException{
		TreeMap<Long,Long> map=new TreeMap<Long,Long>();
		FileReader fstream = new FileReader(csvFolder+file+".csv");
		BufferedReader in = new BufferedReader(fstream);
		String s;
		while((s=(in.readLine()))!=null){
			int commaIndex=s.indexOf(",");
			long key=Long.parseLong(s.substring(0,commaIndex));
			long value=Long.parseLong(s.substring(commaIndex+1,s.length()));
			map.put(key, value);
		}

		in.close();
		return map;
	}

	/**
	 * Reverses the keys and values of a TreeMap
	 * @param map1
	 * @return
	 */
	private static TreeMap<Double,Double> reverseMap_DD(TreeMap<Double,Double> map1){
		TreeMap<Double,Double> map2=new TreeMap<Double,Double>();
		for (double m:map1.keySet()){
			map2.put(map1.get(m), m);
		}

		return map2;
	}

	private static void loadTransitionProbabilities() throws IOException{
		FileReader fstream = new FileReader(csvFolder+"transitionProbabilities.csv");
		BufferedReader in = new BufferedReader(fstream);

		String s;
		int pageNum=1;
		while((s=(in.readLine()))!=null){
			while(s.charAt(s.length()-1)==','){
				s=s.substring(0, s.length()-1);
			}
			ArrayList<Double> values=new ArrayList<Double>();
			int commaIndex=-1;
			int nextCommaIndex=s.indexOf(",",commaIndex+1);
			do{
				if(nextCommaIndex==-1)
					nextCommaIndex=s.length();
				double value=Double.parseDouble(s.substring(commaIndex+1,nextCommaIndex));
				values.add(value);
				commaIndex=nextCommaIndex;
				nextCommaIndex=s.indexOf(",",commaIndex+1);
			}while(commaIndex!=-1&&commaIndex!=s.length());
			transitionProbabilities.put(pageNum,values);
			pageNum++;
		}

		in.close();
		fstream.close();
	}

	public static double getTransitionProb(int pageNum,int probNum){
		return transitionProbabilities.get(pageNum).get(probNum);
	}

	/**
	 * Loads the item images
	 * @throws IOException
	 */
	private static void loadItemImages() throws IOException{
		File dir=new File(csvFolder.toString());
		String[] children=dir.list();
		if(children!=null){
			for(int i=0;i<children.length;i++){
				String filename=children[i];
				if(filename.endsWith(".jpg")||filename.endsWith(".png")||filename.endsWith(".gif")){
					itemPics.add(csvFolder+filename);
				}
			}
		}

	}

	/**
	 * Returns the distribution of the number of images to include in an item for sale
	 * @return
	 */
	public static TreeMap<Double,Integer> getNumHeaderImages() {
		return numHeaderImages;
	}

	/**
	 * Sets the distribution of the number of images to put in a sold item
	 * @param numHeaderImages
	 */
	private static void setNumHeaderImages(TreeMap<Double,Integer> numHeaderImages) {
		RunSettings.numHeaderImages = numHeaderImages;
	}

	/**
	 * Gets the distribution of the number of words in an item title
	 * @return
	 */
	public static TreeMap<Double,Integer> getTitleTotalWords() {
		return titleTotalWords;
	}

	/**
	 * Sets the distribution of the number of words in an item title
	 * @param titleTotalWords
	 */
	private static void setTitleTotalWords(TreeMap<Double,Integer> titleTotalWords) {
		RunSettings.titleTotalWords = titleTotalWords;
	}

	/**
	 * Gets the distribution of the frequency of words in an item title
	 * @return
	 */
	public static TreeMap<Double,StringBuilder> getTitleWords() {
		return titleWords;
	}

	/**
	 * Sets the distribution of the number of words in the item title
	 * 
	 * @param titleWords
	 */
	private static void setTitleWords(TreeMap<Double,StringBuilder> titleWords) {
		RunSettings.titleWords = titleWords;
	}

	/**
	 * Gets the distribution of thenumber of categories of descriptors for an item
	 * @return
	 */
	public static TreeMap<Double,Integer> getItemSpecificsCat() {
		return itemSpecificsCat;
	}

	/**
	 * Sets the distribution for the number of category descriptors of an item
	 * @param itemSpecificsCat
	 */
	private static void setItemSpecificsCat(TreeMap<Double,Integer> itemSpecificsCat) {
		RunSettings.itemSpecificsCat = itemSpecificsCat;
	}

	/**
	 * Gets the distribution of the frequency of words in the item description
	 * @return
	 */
	public static TreeMap<Double,StringBuilder> getItemSpecificsCatWords() {
		return itemSpecificsCatWords;
	}

	/**
	 * Sets the distribution of the frequency of words in the item description
	 * @param itemSpecificsCatWords
	 */
	private static void setItemSpecificsCatWords(TreeMap<Double,StringBuilder> itemSpecificsCatWords) {
		RunSettings.itemSpecificsCatWords = itemSpecificsCatWords;
	}

	/**
	 * Gets the distribution of the number of words in the item specifics distribution
	 * @return
	 */
	public static TreeMap<Double,Integer> getItemSpecificsDesc() {
		return itemSpecificsDesc;
	}

	/**
	 * Sets the distribution of the number of words in the item description
	 * @param itemSpecificsDesc
	 */
	private static void setItemSpecificsDesc(TreeMap<Double,Integer> itemSpecificsDesc) {
		RunSettings.itemSpecificsDesc = itemSpecificsDesc;
	}

	/**
	 * Gets the distribution of the frequency of words in the item description
	 * @return
	 */
	public static TreeMap<Double,StringBuilder> getItemSpecificsDescWords() {
		return itemSpecificsDescWords;
	}

	/**
	 * Sets the distribution of the frequency of words in the item description
	 * @param itemSpecificsDescWords
	 */
	private static void setItemSpecificsDescWords(TreeMap<Double,StringBuilder> itemSpecificsDescWords) {
		RunSettings.itemSpecificsDescWords = itemSpecificsDescWords;
	}

	/**
	 * Gets the distribution of male first names
	 * @return
	 */
	public static TreeMap<Double,StringBuilder> getMaleFirstNames() {
		return maleFirstNames;
	}

	/**
	 * Sets the distribution of male first names
	 * @param maleFirstNames
	 */
	private static void setMaleFirstNames(TreeMap<Double,StringBuilder> maleFirstNames) {
		RunSettings.maleFirstNames = maleFirstNames;
	}

	/**
	 * Gets the distribution of female first names
	 * @return
	 */
	public static TreeMap<Double,StringBuilder> getFemaleFirstNames() {
		return femaleFirstNames;
	}

	/**
	 * Sets the distribution of female first names
	 * @param femaleFirstNames
	 */
	private static void setFemaleFirstNames(TreeMap<Double,StringBuilder> femaleFirstNames) {
		RunSettings.femaleFirstNames = femaleFirstNames;
	}

	/**
	 * Gets the distribution of last names
	 * @return
	 */
	public static TreeMap<Double,StringBuilder> getLastNames() {
		return lastNames;
	}

	/**
	 * Sets the distribution of last names
	 * @param lastNames
	 */
	private static void setLastNames(TreeMap<Double,StringBuilder> lastNames) {
		RunSettings.lastNames = lastNames;
	}

	/**
	 * Gets the distribution of street names
	 * @return
	 */
	public static TreeMap<Double,StringBuilder> getStreetNames() {
		return streetNames;
	}

	/**
	 * Sets the distribution of street names
	 * @param streetNames
	 */
	private static void setStreetNames(TreeMap<Double,StringBuilder> streetNames) {
		RunSettings.streetNames = streetNames;
	}

	/**
	 * Gets the distribution of US states
	 * @return
	 */
	public static TreeMap<Double,StringBuilder> getUSStates() {
		return USStates;
	}

	/**
	 * Sets the distribution of US States
	 * @param uSStates
	 */
	private static void setUSStates(TreeMap<Double,StringBuilder> uSStates) {
		USStates = uSStates;
	}

	/**
	 * Gets the distribution of US cities
	 * @return
	 */
	public static TreeMap<Double,StringBuilder> getCities() {
		return cities;
	}

	/**
	 * Sets the distribution of US cities
	 * @param cities
	 */
	private static void setCities(TreeMap<Double,StringBuilder> cities) {
		RunSettings.cities = cities;
	}

	/**
	 * Gets the mapping of the cities to their respective states
	 * @return
	 */
	public static TreeMap<String,StringBuilder> getCityStates() {
		return cityStates;
	}

	/**
	 * Sets the mapping of cities to their respective states
	 * @param cityStates
	 */
	private static void setCityStates(TreeMap<String,StringBuilder> cityStates) {
		RunSettings.cityStates = cityStates;
	}

	/**
	 * Gets the map of the probability of being what type of auction (bid, buyNow, both)
	 * @return
	 */
	public static TreeMap<Double,StringBuilder> getAuctionType() {
		return auctionType;
	}

	/**
	 * Sets the distribution of auction types (auction/buyNow/both)
	 * @param auctionType
	 */
	private static void setAuctionType(TreeMap<Double,StringBuilder> auctionType) {
		RunSettings.auctionType = auctionType;
	}

	/**
	 * Gets the opposite mapping of the buy now prices to their probabilities
	 * @return
	 */
	public static TreeMap<Double,Double> getBuyNowPriceReverse() {
		return buyNowPriceReverse;
	}

	private static void setBuyNowPriceReverse(TreeMap<Double,Double> buyNowPriceReverse) {
		RunSettings.buyNowPriceReverse = buyNowPriceReverse;
	}

	/**
	 * Gets the distribution of the starting price of auctions
	 * @return
	 */
	public static TreeMap<Double,Double> getStartingBid() {
		return startingBid;
	}

	/**
	 * Sets the distribution of the starting price of auctions
	 * @param startingBid
	 */
	private static void setStartingBid(TreeMap<Double,Double> startingBid) {
		RunSettings.startingBid = startingBid;
	}

	/**
	 * Gets the distribution of buy now prices
	 * @return
	 */
	public static TreeMap<Double,Double> getBuyNowPrice() {
		return buyNowPrice;
	}

	/**
	 * Sets the distribution of buy now prices
	 * @param buyNowPrice
	 */
	private static void setBuyNowPrice(TreeMap<Double,Double> buyNowPrice) {
		RunSettings.buyNowPrice = buyNowPrice;
	}

	/**
	 * Get the list of the name of the pictures to be uploaded
	 * @return
	 */
	public static ArrayList<String> getItemPics(){
		return itemPics;
	}

	/**
	 * Gets the typing speed distribution of the clients
	 * @return
	 */
	public static TreeMap<Double, Double> getTypingSpeedDist() {
		return typingSpeedDist;
	}

	/**
	 * Gets the typing error rate distribution of the clients
	 * @return
	 */
	public static TreeMap<Double, Double> getTypingErrorRate() {
		return typingErrorRate;
	}

	/**
	 * Gets the distribution of the number of words in a search query
	 * @return
	 */
	public static TreeMap<Double, Integer> getNumSearchWords() {
		return numSearchWords;
	}

	/**
	 * Sets the distribution of the number of words in a search query
	 * @param numSearchWords
	 */
	private static void setNumSearchWords(TreeMap<Double,Integer> numSearchWords) {
		RunSettings.numSearchWords = numSearchWords;
	}

	/**
	 * Sets the map of categories and the probability (CDF) of being in each category
	 * @param categories
	 */
	private static void setCategories(TreeMap<Double,Integer> categories){
		RunSettings.categories=categories;
	}

	/**
	 * Gets a map of all categories and the probability (CDF) of an item being in that category
	 * @return
	 */
	public static TreeMap<Double,Integer> getCategories(){
		return categories;
	}


	private static void setTypingSpeed(TreeMap<Double,Double> typingSpeedDist){
		RunSettings.typingSpeedDist=typingSpeedDist;
	}

	/**
	 * Sets the full map of all categories and their respective parents
	 * @param categoriesParents
	 */
	private static void setCategoriesParents(TreeMap<Long,Long> categoriesParents){
		RunSettings.categoriesParents=categoriesParents;
	}

	/**
	 * Returns the full map of all categories and their respective parents
	 * @return
	 */
	public static TreeMap<Long,Long> getCategoriesParents(){
		return categoriesParents;
	}

	/**
	 * Gets a list of categories with the given parentID as its parent category
	 * @param parentId
	 * @return
	 */
	public static ArrayList<Long> getCategoriesFromParent(long parentId){
		if(categoriesParentsStorage.containsKey(parentId))
			return categoriesParentsStorage.get(parentId);

		ArrayList<Long> categories=new ArrayList<Long>();
		for(Entry<Long,Long>e:categoriesParents.entrySet()){
			if(e.getValue()==parentId)
				categories.add(e.getKey());
		}
		synchronized(categoriesParentsStorage){
			categoriesParentsStorage.put(parentId,categories);
		}
		return categories;
	}

	/**
	 * Initializes the run settings values
	 */
	private static void initializeRunSettings(String configFileName){
		/*
		 * Read in the configuration values
		 */
		try (FileInputStream fin = new FileInputStream(configFileName);
				BufferedReader bin = new BufferedReader(new InputStreamReader(fin));) {
			
			String line;
			String commentLine = "#";
			TreeMap<String, String> config = new TreeMap<String, String>();
			StringBuilder valueBuffer = new StringBuilder();

			while(bin.ready()){
				// Read in a line, if it is a comment ignore, otherwise we'll parse it for a value/key pair
				line = bin.readLine();
				if(line!=null){
					if(!line.startsWith(commentLine)){
						// Split the line at an '=', delete while space and add the value to the config map
						String values[] = line.split("=");

						if(values.length >= 2){
							String key = values[0].trim();

							valueBuffer.setLength(0);
							for(int i=1; i<values.length; i++)
								valueBuffer.append(values[i].trim());

							config.put(key.toLowerCase(), valueBuffer.toString());
						}
					}
				}
			}

			/*
			 *  Set the variables we need. If any values are bad we'll add them to an error list
			 */
			valueBuffer.setLength(0);
			String value;

			verbose=setBoolean(config,"verbose",Boolean.FALSE);
			HTML4=setBoolean(config,"html4",Boolean.TRUE);
			outputStats=setBoolean(config,"output_stats",Boolean.TRUE);
			outputMatlab=setBoolean(config,"output_matlab",Boolean.FALSE);
			outputThinkTimes=setBoolean(config,"output_think_times",Boolean.FALSE);
			getExtras=setBoolean(config,"get_css_js_img",Boolean.TRUE);
			staticUserload=setBoolean(config,"static_userload",Boolean.FALSE);
			rampupTime=setInt(config,"rampup_time",30);
			clearCacheOnExit=setDouble(config,"cache_clear",0.2);
			cacheRealData=setBoolean(config,"cache_real_data",Boolean.FALSE);
			tabbedBrowsing=setBoolean(config,"tabbed_browsing",Boolean.TRUE);
			RTthreshold=setDouble(config,"rt_threshold",1000.);
			thinkTimeSpeedUpFactor=setDouble(config,"speedup_factor",1);
			timeToRun=setInt(config,"run_length",10);
			meanClientsPerMinute=setDouble(config,"clients_per_minute",100.);
			stableUsers=setInt(config,"peak_users",100);
			workloadType=setInt(config,"workload_type",2);
			allowBursts=setBoolean(config,"allow_bursts",Boolean.TRUE);
			markovTransitions=setBoolean(config,"markov_page_transitions",Boolean.FALSE);
			networkDelay=setBoolean(config,"network_delay",Boolean.FALSE);
			networkDelayAvg=setDouble(config,"network_delay_avg",0);
			localImages=setBoolean(config,"local_images",Boolean.FALSE);
			setRepeatedRun(setBoolean(config,"repeated_run",Boolean.FALSE));

			// Error conditions on the inputs. These errors are for syntactically valid inputs but will cause errors in the Client Generator.
			if(thinkTimeSpeedUpFactor<=0){
				ableToRun=false;
				valueBuffer.append("Warning: 'speedup_factor' was not set correctly. Can not run Client Generator.\n");
			}
			if(rampupTime<0){
				ableToRun=false;
				valueBuffer.append("Warning: 'rampup_time' was not set correctly. Can not run Client Generator.\n");
			}
			if(clearCacheOnExit<0||clearCacheOnExit>1){
				ableToRun=false;
				valueBuffer.append("Warning: 'cache_clear' was not set correctly. Can not run Client Generator.\n");
			}
			if(RTthreshold<0){
				ableToRun=false;
				valueBuffer.append("Warning: 'rt_threshold' was not set correctly. Can not run Client Generator.\n");
			}
			if(timeToRun<=0){
				ableToRun=false;
				valueBuffer.append("Warning: 'run_length' was not set correctly. Can not run Client Generator.\n");
			}
			if(meanClientsPerMinute<0){
				ableToRun=false;
				valueBuffer.append("Warning: 'clients_per_minute' was not set correctly. Can not run Client Generator.\n");
			}
			if(stableUsers<0){
				ableToRun=false;
				valueBuffer.append("Warning: 'peak_users' was not set correctly. Can not run Client Generator.\n");
			}
			if(workloadType!=1&&workloadType!=2&&workloadType!=3){
				ableToRun=false;
				valueBuffer.append("Warning: 'workload_type' was not set correctly. Can not run Client Generator.\n");
			}
			if(networkDelayAvg<0){
				ableToRun=false;
				valueBuffer.append("Warning: 'network_delay_avg' was not set correctly. Can not run Client Generator.\n");
			}

			// CHANGING NUMBER OF CLIENTS IN SYSTEM
			int numChanges=1;
			while (config.containsKey("change"+numChanges)){
				value = config.get("change"+numChanges);
				String time=value.substring(0,value.indexOf(",")).trim();
				//String clientsPerMin=value.substring(value.indexOf(",")+1,value.lastIndexOf(",")).trim();
				String clientsPerMin=value.substring(value.lastIndexOf(",")+1).trim();

				changeMeanClientsPerMinute.put(Long.parseLong(time), Double.parseDouble(clientsPerMin));
				//	changeStableUsers.put(Long.parseLong(time),Integer.parseInt(peakClients));
				numChanges++;
			}

			// CHANGE PEAK USERS SLOPE
			int numSlope=1;
			while (config.containsKey("slope"+numSlope)){
				value = config.get("slope"+numSlope);
				String time=value.substring(0,value.indexOf(",")).trim();
				String slope=value.substring(value.lastIndexOf(",")+1).trim();

				changePeakUsersSlope.put(Long.parseLong(time), Double.parseDouble(slope));
				numSlope++;
			}

			// Site Data File
			value = config.get("output_site_data_file");
			if(value != null){
				outputSiteData=Boolean.TRUE;
				outputSiteDataFile=checkFolderName(value);
				File theDir = new File(outputSiteDataFile);
				if(!theDir.exists()){
					System.out.println("Creating Directory: "+outputSiteDataFile);
					boolean result=theDir.mkdir();
					if(result)
						System.out.println("Directory "+outputSiteDataFile+" Created");
					else{
						ableToRun=false;
						valueBuffer.append("Warning: Could not create directory "+outputSiteDataFile+". Can not run Client Generator.\n");
					}
				}
			}
			if(value==null){
				outputSiteData = Boolean.FALSE;
				valueBuffer.append("Warning: 'output_site_data_file' was not set correctly. Defaulting to no output.\n");
			}


			// CSV Folder
			value = config.get("distributions_folder");
			if(value != null){
				csvFolder=new StringBuilder(checkFolderName(value));
				File theDir = new File(csvFolder.toString());
				if(!theDir.exists()){
					ableToRun=false;
					valueBuffer.append("Warning: Could not find directory "+csvFolder+". Can not run Client Generator.\n");
				}
			}
			if(value==null){
				ableToRun=false;
				valueBuffer.append("Warning: 'distributions_folder' was not set correctly. Can not run Client Generator.\n");
			}

			// Repeated XML Folder
			value = config.get("repeated_xml");
			if(value != null){
				setRepeatedXmlFolder(new StringBuilder(checkFolderName(value)));
				File theDir = new File(repeatedXmlFolder.toString());
				if(!theDir.exists()){
					System.out.println("Creating Directory: "+repeatedXmlFolder);
					boolean result=theDir.mkdir();
					if(result)
						System.out.println("Directory "+repeatedXmlFolder+" Created");
					else{
						ableToRun=false;
						valueBuffer.append("Warning: Could not create directory "+repeatedXmlFolder+". Can not run Client Generator.\n");
					}
				}
			}
			if(value==null){
				ableToRun=false;
				valueBuffer.append("Warning: 'repeated_xml' was not set correctly. Can not run Client Generator.\n");
			}

			// FULL URL
			int numFullURL=1;
			while (config.containsKey("full_url"+numFullURL)){
				value = config.get("full_url"+numFullURL);
				URLs.add(new CMARTurl(new StringBuilder(value)));
				numFullURL++;
			}

			if(config.get("full_url1")==null){
				ableToRun=false;
				valueBuffer.append("Warning: 'full_url1' was not set correctly. Can not run Client Generator\n");
			}


			// Print out any warnings or errors
			if(valueBuffer.length()>0)
				System.out.println(valueBuffer.toString());

			bin.close();
		}
		catch(Exception e){
			System.out.println("Could not open/parse config file 'CGconfig.txt' in " + System.getProperty("user.dir"));
			e.printStackTrace();
		}


	}

	/**
	 * Sets a double value from the configuration file
	 * @param config the key-value pairs from the configuration file
	 * @param name the name of the key 
	 * @param def the default double value if the key cannot be found
	 * @return the resulting double value for the key
	 */
	private static double setDouble(TreeMap<String,String> config,String name,double def){
		String val=config.get(name);
		if (val!=null)
			return Double.parseDouble(val);
		else{
			System.out.println("Warning: "+name+" was not set correctly. Defaulting to "+def+".\n");
			return def;
		}	
	}

	/**
	 * Sets a integer value from the configuration file
	 * @param config the key-value pairs from the configuration file
	 * @param name the name of the key 
	 * @param def the default integer value if the key cannot be found
	 * @return the resulting integer value for the key
	 */
	private static int setInt(TreeMap<String,String> config,String name,int def){
		String val=config.get(name);
		if (val!=null)
			return Integer.parseInt(val);
		else{
			System.out.println("Warning: "+name+" was not set correctly. Defaulting to "+def+".\n");
			return def;
		}	
	}

	/**
	 * Sets a boolean value from the configuration file
	 * @param config the key-value pairs from the configuration file
	 * @param name the name of the key 
	 * @param def the default boolean value if the key cannot be found
	 * @return the resulting boolean value for the key
	 */
	private static Boolean setBoolean(TreeMap<String,String> config,String name,Boolean def){
		String val=config.get(name);
		if (val!=null)
			if(val.equals("1") || val.toLowerCase().equals("true")) return Boolean.TRUE;
			else return Boolean.FALSE;
		else{
			System.out.println("Warning: "+name+" was not set correctly. Defaulting to "+def+".\n");
			return def;
		}	
	}

	/**
	 * Gets the number of peak users to change to
	 * @return
	 */
	public static TreeMap<Long,Integer> getChangeStableUsers(){
		return changeStableUsers;
	}

	/**
	 * Gets the number of mean clients per minute to change to
	 * @return
	 */
	public static TreeMap<Long,Double> getChangeMeanClientsPerMinute(){
		return changeMeanClientsPerMinute;
	}

	/**
	 * Returns a map of when and by how much to change the slope of the number of peak clients
	 * @return
	 */
	public static TreeMap<Long,Double>getChangePeakUsersSlope(){
		return changePeakUsersSlope;
	}


	/**
	 * Gets verbose - whether or not to output debug information
	 * @return
	 */
	public static boolean isVerbose() {
		return verbose;
	}

	/**
	 * Gets whether the HTML4 version is running
	 * @return
	 */
	public static boolean isHTML4() {
		return HTML4;
	}

	/**
	 * Gets if the think time distribution should be output
	 * @return
	 */
	public static boolean isOutputThinkTimes() {
		return outputThinkTimes;
	}

	/**
	 * Returns the think time speedup factor
	 * @return
	 */
	public static double getThinkTimeSpeedUpFactor(){
		return thinkTimeSpeedUpFactor;
	}

	/**
	 * Returns the set of CMARTurls to be used for a client
	 * @return
	 */
	public static synchronized CMARTurl getCMARTurl(){
		CMARTurl c=URLs.poll();
		URLs.offer(c);
		return c;
	}

	/**
	 * Returns probability of a client clearing the caches upon exit
	 * @return
	 */
	public static double getClearCacheOnExit(){
		return clearCacheOnExit;
	}
	/**
	 * Returns the mean number of clients added to the system per minute
	 * @return
	 */
	public static synchronized double getMeanClientsPerMinute() {
		return meanClientsPerMinute;
	}

	/**
	 * Sets the mean number of clients to be added to the generator per minute
	 * @param meanClientsPerMinute
	 */
	public static synchronized void setMeanClientsPerMinute(double meanClientsPerMinute) {
		RunSettings.meanClientsPerMinute = meanClientsPerMinute;
	}

	/**
	 * @return the stableUsers
	 */
	public static synchronized int getStableUsers() {
		return stableUsers;
	}

	/**
	 * @param stableUsers the stableUsers to set
	 */
	public static synchronized void setStableUsers(int stableUsers) {
		RunSettings.stableUsers = stableUsers;
	}

	/**
	 * @return the timeToRun
	 */
	public static long getTimeToRun() {
		return timeToRun;
	}

	/**
	 * Exits the client generator system
	 * Allows all clients to finish their next request then exits
	 * @param cg
	 */
	public static void exitSystem(ClientGenerator cg){
		thinkTimeSpeedUpFactor*=100;
		cg.exitAllClients();
	}

	/**
	 * Gets the number of additional connections a user can open per page
	 * to get css, js, image data
	 * @return
	 */
	public static int getConnPerPage(){
		return connPerPage;
	}

	/**
	 * Returns boolean indicating if response time stats should be output
	 * @return
	 */
	public static boolean isOutputStats(){
		return outputStats;
	}

	/**
	 * Returns if real data should be cached or just an indicator that the data has been cached
	 * @return
	 */
	public static boolean isCacheRealData(){
		return cacheRealData;
	}

	/**
	 * Returns if the data from the Statistics page should be output
	 * @return
	 */
	public static boolean isOutputSiteData(){
		return outputSiteData;
	}

	public static boolean isOutputMatlab(){
		return outputMatlab;
	}

	/**
	 * Returns the directory to store the statistics
	 * @return
	 */
	public static String getOutputSiteDataFile(){
		return outputSiteDataFile;
	}

	/**
	 * Returns the Response Time threshold above which the client is increasingly likely to logout
	 * @return
	 */
	public static double getRTthreshold(){
		return RTthreshold;
	}

	/**
	 * If img, js, css should be downloaded with each page
	 * @return
	 */
	public static boolean isGetExtras(){
		return getExtras;
	}

	/**
	 * Returns if the closed loop client generator is used
	 * @return
	 */
	public static boolean isStaticUserload(){
		return staticUserload;
	}

	/**
	 * Gets the rampup time for the closed loop client generator
	 * @return
	 */
	public static int getRampupTime(){
		return rampupTime;
	}

	/**
	 * Returns if tabbed browsing is to be used
	 * @return
	 */
	public static boolean isTabbedBrowsing(){
		return tabbedBrowsing;
	}

	/**
	 * Gets the workload type for the run (read/normal/write heavy)
	 * @return
	 */
	public static int getWorkloadType(){
		return workloadType;
	}

	/**
	 * If client bursts are to be allowed
	 * @return
	 */
	public static boolean isAllowBursts(){
		return allowBursts;
	}

	/**
	 * Returns if a Markov model for page probability transitions are to be used
	 * @return
	 */
	public static boolean isMarkovTransitions(){
		return markovTransitions;
	}

	/**
	 * Returns if a fake delay should be added to different users to simulate different network delays
	 * @return
	 */
	public static boolean isNetworkDelay(){
		return networkDelay;
	}

	/**
	 * Gets the average for the network delay to be added
	 * @return
	 */
	public static double getNetworkDelayAvg(){
		return networkDelayAvg;
	}

	/**
	 * Returns if the images are hosted on the app server or a separate image server
	 * @return
	 */
	public static boolean isLocalImages(){
		return localImages;
	}

	/**
	 * @return the repeatedXmlFolder
	 */
	public static StringBuilder getRepeatedXmlFolder() {
		return repeatedXmlFolder;
	}

	/**
	 * @param repeatedXmlFolder the repeatedXmlFolder to set
	 */
	public static void setRepeatedXmlFolder(StringBuilder repeatedXmlFolder) {
		RunSettings.repeatedXmlFolder = repeatedXmlFolder;
	}

	/**
	 * @return the repeatedRun
	 */
	public static boolean isRepeatedRun() {
		return repeatedRun;
	}

	/**
	 * @param repeatedRun the repeatedRun to set
	 */
	public static void setRepeatedRun(boolean repeatedRun) {
		RunSettings.repeatedRun = repeatedRun;
	}

	/**
	 * Checks that a given folder name contains the correct '\' or '/'
	 * on the end
	 *
	 * @param folderName The folder name to be checked
	 * @return The folder with the correct '\' or '/' on the end
	 */
	public static String checkFolderName(String folderName){
		if(folderName == null) return null;

		// Windows folder, must end with '\'
		if(System.getProperty("os.name").toLowerCase().contains("windows")){
			if(folderName.endsWith("/")) return folderName.substring(0,folderName.length()-1)+"\\";
			else if(!folderName.endsWith("\\")) return folderName+"\\";
		}
		// Linux folder, must end with '/'
		else{
			if(folderName.endsWith("\\")) return folderName.substring(0,folderName.length()-1)+"/";
			else if(!folderName.endsWith("/")) return folderName+"/";
		}

		return folderName;
	}

}

