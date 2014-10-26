package populator;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;


public class CreateAll {
	public static Boolean DEBUG = false;
	public static final Random rand = new Random(System.currentTimeMillis());
	public static final long START_TIME = System.currentTimeMillis();
	public static final boolean verbose = true;
	public static long totalstart = 0;
	
	/*
	 * THE FOLLOWING VALUES ARE OVERRIDDEN BY VALUES IN THE CONFIGURATION FILE
	 */
	
	// NUMBER OF THINGS TO POPULATE
	public static int NO_OF_STATES = 0;
	public static int NO_OF_CATEGORIES = 0;
	public static int NO_OF_USERS = 0;
	public static int NO_OF_ITEMS = 0;
	public static int NO_OF_OLD_ITEMS = 0;
	
	// Database information
	public static String MY_DATABASE_URL;
	public static String MY_DATABASE_DRIVER;
	public static String MY_DATABASE_USERNAME;
	public static String MY_DATABASE_PASSWORD;
	
	public static String CASS_DATABASE_URL;
	public static String CASS_DATABASE_DRIVER;
	public static String CASS_DATABASE_USERNAME;
	public static String CASS_DATABASE_PASSWORD;

	public static Boolean SOLR_ENABLED;
	public static String SOLR_URL;
	public static int SOLR_MAX_CONNS;
	
	// PATHS TO FILES
	public static String BASE_PATH;
	public static String IN_IMAGE_PATH;
	public static String OUT_IMAGE_PATH;
	public static String IMAGES_FILE;
	public static String IMAGES_DIST_FILE;
	public static String STATES_FILE;
	public static String CATEGORIES_FILE;
	public static String STREETS_FILE;
	public static String TOWNS_FILE;
	public static String FIRST_NAMES_FILE;
	public static String LAST_NAMES_FILE;
	public static String TITLE_WORDS_FILE;
	public static String DESCRIPTION_WORDS_FILE;
	public static String DESCRIPTION_WORDS_NUMBER_FILE;
	public static String USER_RATINGS_FILE;
	public static String START_PRICE_FILE;
	public static String BUY_NOW_PRICE_FILE;
	public static String TITLE_WORDS_NUMBER_FILE;
	
	// DISTRIBUTIONS TO USE
	public static Boolean STATES_UNIFORM = false;
	public static Boolean CATEGORIES_UNIFORM = false;
	public static Boolean TITLE_WORDS_UNIFORM = false;
	public static Boolean DESCRIPTION_WORDS_UNIFORM = false;
	public static Boolean FIRST_NAMES_UNIFORM = false;
	public static Boolean LAST_NAMES_UNIFORM = false;
	public static Boolean USER_RATINGS_UNIFORM = false;
	public static Boolean START_PRICE_UNIFORM = false;
	public static Boolean BUY_NOW_PRICE_UNIFORM = false;
	
	public static Boolean STREETS_WORD_UNIFORM = false;
	public static Boolean TOWNS_WORD_UNIFORM = false;
	
	// Additional item data
	public static double ITEMS_BUYNOW_FRACTION;
	public static double ITEMS_RESERVE_FRACTION;
	
	// Additional bid data
	public static double BIDS_OUTBID_FRACTION;
	public static double BIDS_MAX_OUTBID_FRACTION;
	
	public static double FULL_ZIP_FRACTION;
	
	// data distributions
	public static int BIDS_DIST_TYPE;
	public static double BIDS_MIN;
	public static double BIDS_MAX;
	public static double BIDS_DIST_MEAN;
	public static double BIDS_DIST_SD;
	public static double BIDS_ALPHA;
	public static double BIDS_LAMBDA;
	
	public static int QUESTIONS_DIST_TYPE;
	public static double QUESTIONS_MIN;
	public static double QUESTIONS_MAX;
	public static double QUESTIONS_DIST_MEAN;
	public static double QUESTIONS_DIST_SD;
	public static double QUESTIONS_ALPHA;
	public static double QUESTIONS_LAMBDA;
	public static double QUESTIONS_FRACTION_ANSWERED;
	
	public static int QUESTIONS_LENGTH_DIST_TYPE;
	public static double QUESTIONS_LENGTH_MIN;
	public static double QUESTIONS_LENGTH_MAX;
	public static double QUESTIONS_LENGTH_DIST_MEAN;
	public static double QUESTIONS_LENGTH_DIST_SD;
	public static double QUESTIONS_LENGTH_ALPHA;
	public static double QUESTIONS_LENGTH_LAMBDA;
	public static double QUESTIONS_LENGTH_FRACTION_ANSWERED;
	
	public static int COMMENTS_LENGTH_DIST_TYPE;
	public static double COMMENTS_LENGTH_MIN;
	public static double COMMENTS_LENGTH_MAX;
	public static double COMMENTS_LENGTH_DIST_MEAN;
	public static double COMMENTS_LENGTH_DIST_SD;
	public static double COMMENTS_LENGTH_ALPHA;
	public static double COMMENTS_LENGTH_LAMBDA;
	public static double COMMENTS_LENGTH_FRACTION_ANSWERED;
	
	public static int IMAGES_DIST_TYPE;
	public static double IMAGES_MIN;
	public static double IMAGES_MAX;
	public static double IMAGES_DIST_MEAN;
	public static double IMAGES_DIST_SD;
	public static double IMAGES_ALPHA;
	public static double IMAGES_LAMBDA;
	
	public static double STREETNAME_MIN;
	public static double STREETNAME_MAX;
	public static double STREETNAME_DIST_MEAN;
	public static double STREETNAME_DIST_SD;
	public static int STREETNAME_DIST_TYPE;
	public static double STREETNAME_ALPHA;
	public static double STREETNAME_LAMBDA;
	
	public static double TOWNNAME_MIN;
	public static double TOWNNAME_MAX;
	public static double TOWNNAME_DIST_MEAN;
	public static double TOWNNAME_DIST_SD;
	public static int TOWNNAME_DIST_TYPE;
	public static double TOWNNAME_ALPHA;
	public static double TOWNNAME_LAMBDA;
	
	public static int ITEM_TITLE_LENGTH_DIST_TYPE;
	public static double ITEM_TITLE_LENGTH_MIN;
	public static double ITEM_TITLE_LENGTH_MAX;
	public static double ITEM_TITLE_LENGTH_DIST_MEAN;
	public static double ITEM_TITLE_LENGTH_DIST_SD;
	public static double ITEM_TITLE_LENGTH_ALPHA;
	public static double ITEM_TITLE_LENGTH_LAMBDA;
	
	public static int ITEM_DESCRIPTION_LENGTH_DIST_TYPE;
	public static double ITEM_DESCRIPTION_LENGTH_MIN;
	public static double ITEM_DESCRIPTION_LENGTH_MAX;
	public static double ITEM_DESCRIPTION_LENGTH_DIST_MEAN;
	public static double ITEM_DESCRIPTION_LENGTH_DIST_SD;
	public static double ITEM_DESCRIPTION_LENGTH_ALPHA;
	public static double ITEM_DESCRIPTION_LENGTH_LAMBDA;
	
	public static int ADDRESSES_DIST_TYPE;
	public static double ADDRESSES_MIN;
	public static double ADDRESSES_MAX;
	public static double ADDRESSES_DIST_MEAN;
	public static double ADDRESSES_DIST_SD;
	public static double ADDRESSES_ALPHA;
	public static double ADDRESSES_LAMBDA;
	
	// Number of threads to use
	private final static int USER_THREADS = 4;
	private final static int OLD_ITEMS_THREADS = 4;
	private final static int ITEMS_THREADS = 4;
	private final static int IMAGE_THREADS = 4;
	
	// Make sure threads finish
	private static CountDownLatch finishStates= new CountDownLatch(1);
	private static CountDownLatch finishCats= new CountDownLatch(1);
	private static CountDownLatch finishUsers = new CountDownLatch(USER_THREADS);
	private static CountDownLatch finishOldItems = new CountDownLatch(OLD_ITEMS_THREADS);
	private static CountDownLatch finishItems = new CountDownLatch(ITEMS_THREADS);
	private static CountDownLatch finishOldImages = new CountDownLatch(IMAGE_THREADS);
	private static CountDownLatch finishImages = new CountDownLatch(IMAGE_THREADS);
	
	private static DBPopulator db;
	
	private static String configfile = null;
	
	public static void main(String args[]){
		boolean populate = true;
		boolean writeImg = true;
		boolean indexOn = false;
		
		for(int aloop=0; aloop<args.length; aloop++)
			if(args[aloop].toLowerCase().equals("config") || args[aloop].toLowerCase().equals("-config")){
				if(aloop+1 <= args.length)
					configfile = args[aloop+1];
			}
		
		if(configfile==null){
			System.out.println("C-MART populator: WARNING: You did not specify a configuration file");
			System.out.println("C-MART populator: Trying the default file /cmart/populator/config/default.txt");
			configfile = "/cmart/populator/config/default.txt";
		}
		
		readConfig();
		
		if(NO_OF_OLD_ITEMS==0) finishOldItems = new CountDownLatch(0);
		if(NO_OF_ITEMS==0) finishItems = new CountDownLatch(0);
		if(NO_OF_USERS==0) finishUsers = new CountDownLatch(0);
		if(NO_OF_OLD_ITEMS==0) finishOldImages = new CountDownLatch(0);
		if(NO_OF_ITEMS==0) finishImages = new CountDownLatch(0);
		
		
		// see if the user wants to do anything special
		for(int aloop=0; aloop<args.length; aloop++)
			if(args[aloop].toLowerCase().equals("compress") || args[aloop].toLowerCase().equals("-compress")){
				// Compress the image and output the compressed version to the output folder
				CompressImages ci = new CompressImages();
				populate = false;
			}
			else if(args[aloop].toLowerCase().equals("dbtoimg") || args[aloop].toLowerCase().equals("-dbtoimg")){
				// Read the images from the DB and actually create the files
				for(int i=0; i<IMAGE_THREADS*2; i++){
					int offset = ((NO_OF_OLD_ITEMS+NO_OF_ITEMS)/(IMAGE_THREADS*2)) * i;
					
					Populate popImg2 = new Populate(new ImagesFromDB(finishImages), (NO_OF_OLD_ITEMS+NO_OF_ITEMS)/(IMAGE_THREADS*2), offset);
					new Thread(popImg2, "Populate item images " + i).start();
				}
				try {
					finishImages.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				populate = false;
			}
			else if(args[aloop].toLowerCase().equals("dbtosolr") || args[aloop].toLowerCase().equals("-dbtosolr")){
				db.initSolr();
				
				finishItems = new CountDownLatch(ITEMS_THREADS);
				
				for(int i=0; i<ITEMS_THREADS; i++){
					int offset = NO_OF_OLD_ITEMS+((NO_OF_ITEMS/ITEMS_THREADS) * i);
					
					Populate popSolr = new Populate(new DBToSolr(finishItems), NO_OF_ITEMS/ITEMS_THREADS, offset);
					new Thread(popSolr, "Populate Solr " + i).start();
				}
				
				try {
					finishItems.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				db.flushAllBuffers();
				
				populate = false;
			}
			else if(args[aloop].toLowerCase().equals("noimg") || args[aloop].toLowerCase().equals("-noimg")){
				writeImg = false;
			}
			else if(args[aloop].toLowerCase().equals("indexon") || args[aloop].toLowerCase().equals("-indexon")){
				indexOn = true;
			} 
		
		if(populate){
			CreateAll go = new CreateAll(writeImg, indexOn);
		}
	}
	
	public CreateAll(boolean writeImg, boolean indexOn){
		PopulateImages.writeFiles(writeImg);
		
		/*
		 * Read the configuration and connect to the database
		 */
		if(verbose) print("Reading config file and creating DB connection");
		if(!indexOn) db.disableIndexes(true);
		
		/*
		 * To insert into solr
		 */
		/*db.initSolr();
		for(int i=0; i<ITEMS_THREADS; i++){
			//int offset = ((NO_OF_OLD_ITEMS+NO_OF_ITEMS)/IMAGE_THREADS) * i;
			int offset = NO_OF_OLD_ITEMS+((NO_OF_ITEMS/ITEMS_THREADS) * i);
			//Populate popImg2 = new Populate(new ImagesFromDB(finishImages), (NO_OF_OLD_ITEMS+NO_OF_ITEMS)/IMAGE_THREADS, offset);
			Populate popSolr = new Populate(new DBToSolr(finishItems), 2, offset);
			new Thread(popSolr, "Populate Solr " + i).start();
		}
		
		try {
			finishItems.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		db.flushAllBuffers();
		System.exit(0);
		
		/*
		 * To make image
		 */
		/*for(int i=0; i<IMAGE_THREADS*2; i++){
			//int offset = ((NO_OF_OLD_ITEMS+NO_OF_ITEMS)/(IMAGE_THREADS*2)) * i;
			int offset = ((NO_OF_OLD_ITEMS+NO_OF_ITEMS)/IMAGE_THREADS) * i;
			//Populate popImg2 = new Populate(new ImagesFromDB(finishImages), (NO_OF_OLD_ITEMS+NO_OF_ITEMS)/(IMAGE_THREADS*2), offset);
			Populate popImg2 = new Populate(new ImagesFromDB(finishImages), 5, offset);
			new Thread(popImg2, "Populate item images " + i).start();
		}
		try {
			finishImages.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//System.exit(0);
		
		/*
		 * Make (or re-make) the database tables
		 */
		if(verbose) print("Deleting and creating all of the database tables");
		PopulateStates.remakeTable(db);
		PopulateCategories.remakeTable(db);
		if(USER_THREADS > 0 && NO_OF_USERS>0) PopulateUsers.remakeTable(db);
		if(OLD_ITEMS_THREADS > 0 && NO_OF_OLD_ITEMS>0) PopulateOldItems.remakeTable(db);
		
		
		/*
		 * Populate the states. This must finish before the users can be created as they need states and we
		 * are getting the state ID's back from the database
		 */
		if(verbose) print("Populating the states");
		Populate popStates = new Populate(new PopulateStates(), NO_OF_STATES, true);
		popStates.run();
		finishStates.countDown();
		
		/*
		 * Populate the item categories. We can create items before the categories in the DB have finished
		 * populating as PopulateCategories create a local copy of the categoryID's from the file.
		 * 
		 * As categories are only populated as a singular, they are inserted as a single thread
		 */
		if(verbose) print("Populating the item categories");
		Populate popCats = new Populate(new PopulateCategories(finishCats), NO_OF_CATEGORIES, true);
		new Thread(popCats, "Populate categories").start();
		
		/*
		 * Create the users. The items can be created before the users have finished populating as we know the
		 * userID will range from 1-N, so we can just use a number between 1 and N and we know it will be present.
		 * 
		 * As there are multiple user threads we offset each thread so that it does not attempt to insert
		 * users with the same Username (otherwise we'd have to insert, get the id, then update the username)
		 */
		if(verbose) print("Populating the users");
		if(NO_OF_USERS > 0)
		for(int i=0; i<USER_THREADS; i++){
			int offset = (NO_OF_USERS/USER_THREADS) * i;
			Populate popUsers = new Populate(new PopulateUsers(finishUsers), NO_OF_USERS/USER_THREADS, offset);
			new Thread(popUsers, "Populate users " + i).start();
		}
		
		/*
		 * Populate the old items. As we don't know how many oldBids will be created exactly, we must wait for
		 * old items to be populated before we insert the current items
		 */
		if(verbose) print("Populating the old item");
		if(NO_OF_OLD_ITEMS>0)
		for(int i=0; i<OLD_ITEMS_THREADS; i++){
			Populate popOldItems = new Populate(new PopulateOldItems(finishOldItems), NO_OF_OLD_ITEMS/OLD_ITEMS_THREADS);
			new Thread(popOldItems, "Populate old items " + i).start();
		}

		try {
			finishOldItems.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.flushAllBuffers();
		
		if(NO_OF_OLD_ITEMS>0 || NO_OF_ITEMS>0) PopulateImages.remakeTable(db);
		if(NO_OF_ITEMS > 0) PopulateItems.remakeTable(db);
		
		/*
		 * Create the images for the  old items. This will insert the image and item-image into
		 * the database and write the files to the image output folder. It also updates oldImages with
		 * a thumbnail, which is why the items need to be present
		 */
		if(verbose) print("Populating the old item's images");
		if(NO_OF_OLD_ITEMS>0)
		for(int i=0; i<IMAGE_THREADS; i++){
			int offset = (NO_OF_OLD_ITEMS/IMAGE_THREADS) * i;
			Populate popImg = new Populate(new PopulateImages(finishOldImages), NO_OF_OLD_ITEMS/IMAGE_THREADS, offset);
			new Thread(popImg, "Populate old item images " + i).start();
		}
		
		/*
		 * Populate the current items. We'll use the offset from the oldItems and oldBids to make sure we
		 * don't reuse IDs. We'll add inserting the items to the 
		 */
		if(verbose) print("Populating the new items");
		if(NO_OF_ITEMS>0)
		for(int i=0; i<ITEMS_THREADS; i++){
			Populate popItems = new Populate(new PopulateItems(finishItems), NO_OF_ITEMS/ITEMS_THREADS);
			new Thread(popItems, "Populate items " + i).start();
		}
		
		try {
			finishItems.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.flushAllBuffers();
		
		
		/*
		 * Now that the items have finished inserting we can create the images for the rest of the items
		 */
		if(verbose) print("Populating the new item's images");
		for(int i=0; i<IMAGE_THREADS; i++){
			int offset = NO_OF_OLD_ITEMS + (NO_OF_ITEMS/IMAGE_THREADS) * i;
			Populate popImg = new Populate(new PopulateImages(finishImages), NO_OF_ITEMS/IMAGE_THREADS, offset);
			new Thread(popImg, "Populate item images " + i).start();
		}
		
		
		/*
		 * Wait for the last images to finish. Everything else has finished by now
		 */
		try {
			finishCats.await(); // should be finished a long time ago
			finishStates.await(); // should be finished a long time ago
			finishOldImages.await();
			finishImages.await();
			finishUsers.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		 * Re-enable the database indexes that we turned off for the insert
		 */
		if(verbose) print("Rebuilding indexes and finishing up");
		db.flushAllBuffers();
		if(!indexOn) db.enableIndexes();
		//db.closeConnections();
		
		// There is a sleep here as the database doesn't always respond fast enough
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.flushAllBuffers();
		
		System.out.println("\nFinished. Total time: " + (System.currentTimeMillis()-totalstart)/1000);
	}
	
	public static void print(String message){
		if(totalstart==0) totalstart = System.currentTimeMillis();
		System.out.println((System.currentTimeMillis()-totalstart)/1000 + " sec : " + message);
	}
	
	public static void readConfig(){
		/*
		 * Read in the configuration values
		 */
		ConfigReader cf = new ConfigReader(configfile);
		
		BASE_PATH = cf.getString("populate_base_path","");
		DEBUG = cf.getBoolean("debug", Boolean.FALSE);
		NO_OF_STATES = cf.getInt("populate_no_of_states", 0);
		NO_OF_CATEGORIES = cf.getInt("populate_no_of_categories", 0);
		NO_OF_USERS = cf.getInt("populate_no_of_users", 0);
		NO_OF_OLD_ITEMS = cf.getInt("populate_no_of_old_items", 0);
		NO_OF_ITEMS = cf.getInt("populate_no_of_items", 0);
		
		IN_IMAGE_PATH = cf.getString("populate_in_image_path", null);
		OUT_IMAGE_PATH = cf.getString("populate_out_image_path", null);
		IMAGES_FILE = BASE_PATH + cf.getString("populate_images_file", null);
		IMAGES_DIST_FILE = BASE_PATH + cf.getString("populate_images_dist_file", null);
		STATES_FILE = BASE_PATH + cf.getString("populate_states_file", null);
		CATEGORIES_FILE = BASE_PATH + cf.getString("populate_categories_file", null);
		FIRST_NAMES_FILE = BASE_PATH + cf.getString("populate_first_names_file", null);
		LAST_NAMES_FILE = BASE_PATH + cf.getString("populate_last_names_file", null);
		USER_RATINGS_FILE = BASE_PATH + cf.getString("populate_user_ratings_file", null);
		START_PRICE_FILE = BASE_PATH + cf.getString("populate_start_price_file", null);
		BUY_NOW_PRICE_FILE = BASE_PATH + cf.getString("populate_buy_now_price_file", null);
		STREETS_FILE = BASE_PATH + cf.getString("populate_streets_file", null);
		TOWNS_FILE = BASE_PATH + cf.getString("populate_towns_file", null);
		
		TITLE_WORDS_NUMBER_FILE = BASE_PATH + cf.getString("populate_title_words_number_file", null);
		TITLE_WORDS_FILE = BASE_PATH + cf.getString("populate_title_words_file", null);
		DESCRIPTION_WORDS_NUMBER_FILE = BASE_PATH + cf.getString("populate_description_words_number_file", null);
		DESCRIPTION_WORDS_FILE = BASE_PATH + cf.getString("populate_description_words_file", null);
		
		STATES_UNIFORM = cf.getBoolean("populate_states_uniform", Boolean.FALSE);
		CATEGORIES_UNIFORM  = cf.getBoolean("populate_categories_uniform", Boolean.FALSE);
		
		FULL_ZIP_FRACTION = cf.getDouble("populate_full_zip_fraction", 0.2);
		
		ITEMS_BUYNOW_FRACTION  = cf.getDouble("populate_buynow_fraction", 0.5);
		ITEMS_RESERVE_FRACTION  = cf.getDouble("populate_reserve_fraction", 0.3);
		
		BIDS_OUTBID_FRACTION = cf.getDouble("populate_outbid_fraction", 0.2);
		BIDS_MAX_OUTBID_FRACTION = cf.getDouble("populate_max_outbid_fraction", 0.7);
		
		BIDS_MIN  = cf.getDouble("populate_bids_min", 0);
		BIDS_MAX  = cf.getDouble("populate_bids_max", 30);
		BIDS_DIST_MEAN  = cf.getDouble("populate_bids_dist_mean", 10);
		BIDS_DIST_SD  = cf.getDouble("populate_bids_dist_sd", 7);
		BIDS_DIST_TYPE  = cf.getInt("populate_bids_dist_type", 1);
		BIDS_ALPHA  = cf.getDouble("populate_bids_alpha", 1);
		BIDS_LAMBDA  = cf.getDouble("populate_bids_lambda", 1);
		
		QUESTIONS_MIN  = cf.getDouble("populate_questions_min", 0);
		QUESTIONS_MAX  = cf.getDouble("populate_questions_max", 5);
		QUESTIONS_DIST_MEAN  = cf.getDouble("populate_questions_dist_mean", 2);
		QUESTIONS_DIST_SD  = cf.getDouble("populate_questions_dist_sd", 2);
		QUESTIONS_FRACTION_ANSWERED  = cf.getDouble("populate_questions_fraction_answered", 0.3);
		QUESTIONS_DIST_TYPE  = cf.getInt("populate_questions_dist_type", 1);
		QUESTIONS_ALPHA  = cf.getDouble("populate_questions_alpha", 1);
		QUESTIONS_LAMBDA  = cf.getDouble("populate_questions_lambda", 1);
		
		IMAGES_MIN  = cf.getDouble("populate_images_min", 0);
		IMAGES_MAX  = cf.getDouble("populate_images_max", 6);
		IMAGES_DIST_MEAN  = cf.getDouble("populate_images_dist_mean", 3);
		IMAGES_DIST_SD  = cf.getDouble("populate_images_dist_sd", 2);
		IMAGES_DIST_TYPE  = cf.getInt("populate_images_dist_type", 1);
		IMAGES_ALPHA  = cf.getDouble("populate_images_alpha", 1);
		IMAGES_LAMBDA  = cf.getDouble("populate_images_lambda", 1);
		
		// Address street name length
		STREETNAME_MIN  = cf.getDouble("populate_streetname_min", 1);
		STREETNAME_MAX  = cf.getDouble("populate_streetname_max", 3);
		STREETNAME_DIST_MEAN  = cf.getDouble("populate_streetname_dist_mean", 2);
		STREETNAME_DIST_SD  = cf.getDouble("populate_streetname_dist_sd", 2);
		STREETNAME_DIST_TYPE  = cf.getInt("populate_streetname_dist_type", 1);
		STREETNAME_ALPHA  = cf.getDouble("populate_streetname_alpha", 1);
		STREETNAME_LAMBDA  = cf.getDouble("populate_streetname_lambda", 1);
		
		// Town name length
		TOWNNAME_MIN  = cf.getDouble("populate_townname_min", 1);
		TOWNNAME_MAX  = cf.getDouble("populate_townname_max", 3);
		TOWNNAME_DIST_MEAN  = cf.getDouble("populate_townname_dist_mean", 2);
		TOWNNAME_DIST_SD  = cf.getDouble("populate_townname_dist_sd", 2);
		TOWNNAME_DIST_TYPE  = cf.getInt("populate_townname_dist_type", 1);
		TOWNNAME_ALPHA  = cf.getDouble("populate_townname_alpha", 1);
		TOWNNAME_LAMBDA  = cf.getDouble("populate_townname_lambda", 1);
		
		// item title length
		ITEM_TITLE_LENGTH_MIN  = cf.getDouble("populate_item_title_length_min", 1);
		ITEM_TITLE_LENGTH_MAX  = cf.getDouble("populate_item_title_length_max", 3);
		ITEM_TITLE_LENGTH_DIST_MEAN  = cf.getDouble("populate_item_title_length_dist_mean", 2);
		ITEM_TITLE_LENGTH_DIST_SD  = cf.getDouble("populate_item_title_length_dist_sd", 2);
		ITEM_TITLE_LENGTH_DIST_TYPE  = cf.getInt("populate_item_title_length_dist_type", 1);
		ITEM_TITLE_LENGTH_ALPHA  = cf.getDouble("populate_item_title_length_alpha", 1);
		ITEM_TITLE_LENGTH_LAMBDA  = cf.getDouble("populate_item_title_length_lambda", 1);
		
		// item description length
				ITEM_DESCRIPTION_LENGTH_MIN  = cf.getDouble("populate_item_description_length_min", 0);
				ITEM_DESCRIPTION_LENGTH_MAX  = cf.getDouble("populate_item_description_length_max", 500);
				ITEM_DESCRIPTION_LENGTH_DIST_MEAN  = cf.getDouble("populate_item_description_length_dist_mean", 200);
				ITEM_DESCRIPTION_LENGTH_DIST_SD  = cf.getDouble("populate_item_description_length_dist_sd", 30);
				ITEM_DESCRIPTION_LENGTH_DIST_TYPE  = cf.getInt("populate_item_description_length_dist_type", -1);
				ITEM_DESCRIPTION_LENGTH_ALPHA  = cf.getDouble("populate_item_description_length_alpha", 1);
				ITEM_DESCRIPTION_LENGTH_LAMBDA  = cf.getDouble("populate_item_description_length_lambda", 1);
		
		// question text length
				QUESTIONS_LENGTH_MIN  = cf.getDouble("populate_questions_length_min", 5);
				QUESTIONS_LENGTH_MAX  = cf.getDouble("populate_questions_length_max", 50);
				QUESTIONS_LENGTH_DIST_MEAN  = cf.getDouble("populate_questions_length_dist_mean", 25);
				QUESTIONS_LENGTH_DIST_SD  = cf.getDouble("populate_questions_length_dist_sd", 5);
				QUESTIONS_LENGTH_DIST_TYPE  = cf.getInt("populate_questions_length_dist_type", 1);
				QUESTIONS_LENGTH_ALPHA  = cf.getDouble("populate_questions_length_alpha", 1);
				QUESTIONS_LENGTH_LAMBDA  = cf.getDouble("populate_questions_length_lambda", 1);
		
				// comment text length
				COMMENTS_LENGTH_MIN  = cf.getDouble("populate_comments_length_min", 5);
				COMMENTS_LENGTH_MAX  = cf.getDouble("populate_comments_length_max", 50);
				COMMENTS_LENGTH_DIST_MEAN  = cf.getDouble("populate_comments_length_dist_mean", 25);
				COMMENTS_LENGTH_DIST_SD  = cf.getDouble("populate_comments_length_dist_sd", 5);
				COMMENTS_LENGTH_DIST_TYPE  = cf.getInt("populate_comments_length_dist_type", 1);
				COMMENTS_LENGTH_ALPHA  = cf.getDouble("populate_comments_length_alpha", 1);
				COMMENTS_LENGTH_LAMBDA  = cf.getDouble("populate_comments_length_lambda", 1);
				
				// comment text length
				ADDRESSES_MIN  = cf.getDouble("populate_addresses_min", 1);
				ADDRESSES_MAX  = cf.getDouble("populate_addresses_max", 2);
				ADDRESSES_DIST_MEAN  = cf.getDouble("populate_addresses_dist_mean", 1);
				ADDRESSES_DIST_SD  = cf.getDouble("populate_addresses_dist_sd", 1);
				ADDRESSES_DIST_TYPE  = cf.getInt("populate_addresses_dist_type", 0);
				ADDRESSES_ALPHA  = cf.getDouble("populate_addresses_alpha", 1);
				ADDRESSES_LAMBDA  = cf.getDouble("populate_addresses_lambda", 1);
				
		MY_DATABASE_URL = cf.getString("my_database_url", null);
		MY_DATABASE_DRIVER = cf.getString("my_database_driver", null);
		MY_DATABASE_USERNAME = cf.getString("my_database_username", null);
		MY_DATABASE_PASSWORD = cf.getString("my_database_password", null);
		
		CASS_DATABASE_URL = cf.getString("cassandra_database_url", null);
		CASS_DATABASE_DRIVER = cf.getString("cassandra_database_driver", null);
		
		String dbType = cf.getString("database_type", null);
		if(dbType != null){
			if(dbType.toLowerCase().equals("myisam")){
				db = new MySQLDBPopulator();
				db.setEngine("MyISAM", true);
			}
			else if(dbType.toLowerCase().equals("innodb")){
				db = new MySQLDBPopulator();
				db.setEngine("INNODB", false);
			}
			else if(dbType.toLowerCase().equals("cassandra")) db = new CassandraDBPopulator();
			else{
				System.out.println("Error: 'database' was set incorrectly..\n");
			}
			
			Populate.setDB(db);
		}
		else{
			System.out.println("Error: 'database' was set incorrectly..\n");
		}
		
		SOLR_ENABLED = cf.getBoolean("solr_enabled", false);
		SOLR_URL = cf.getString("solr_url", null);
		SOLR_MAX_CONNS = cf.getInt("solr_max_conns_per_host", 200);
		
		// If solr is enabled, enable it
		if(SOLR_ENABLED){
			db.initSolr();
		}
		
		
		cf.printErrors();
	
	}
}
