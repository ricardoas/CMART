package populator;

import java.util.*;



/**
 * This object hold and item read from the data base. As setting the data to the object here doesn't change it
 * in the data base it should really be immutable. However, since we can make the code more efficient by only
 * reading and setting the values we need, and using the 'set' methods for temporary storage it is better
 * for performance to not use this as immutable. Whichever way we choose someone will complain
 * Use as immutable = people say performance isn't realistic
 * Use as mercurial = people say the design is bad
 * 
 * woe is me - Andy
 * John, Andrew, you have thoughts on this?
 * 
 * 
 * @author Andy (andrewtu@cmu.edu)
 * @version 0.1
 * @since 0.1
 * @date 04/05/2011
 */
public class Item {
	// Variables to hold all of the item data
	private long id;
	private String name;
	private String sellerName;
	private String description;
	private int quantity;
	private double startPrice;
	private double reservePrice;
	private double buyNowPrice;
	private double currentBid;
	private double maxBid;
	private int noOfBids;
	private Date startDate;
	private Date endDate;
	private long sellerID;
	private long categoryID;
	private String thumbnailURL; 
	private ArrayList<Image> images;
	private ArrayList<Bid> allBids;
	private static final boolean immutableWarning = true;
	
	/**
	 * Create a new item. As the user has not specified any data, we know they are going to use it as mercurial
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public Item(){
		if(Item.immutableWarning) Item.printWarning();
	}
	
	/**
	 * Create a new item with all of the items data from the DB
	 * 
	 * @param id
	 * @param name
	 * @param description
	 * @param quantity
	 * @param startPrice
	 * @param reservePrice
	 * @param buyNowPrice
	 * @param currentBid
	 * @param maxBid
	 * @param noOfBids
	 * @param startDate
	 * @param endDate
	 * @param sellerID
	 * @param categoryID
	 * @param thumbnail
	 * @param images
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public Item(long id, String name, String description, int quantity, double startPrice, double reservePrice, double buyNowPrice,
			double currentBid, double maxBid, int noOfBids, Date startDate, Date endDate, long sellerID, long categoryID, String thumbnail, ArrayList<Image> images, ArrayList<Bid> allBids){
		this.id = id;
		this.name = name;
		this.description = description;
		this.quantity = quantity;
		this.startPrice = startPrice;
		this.reservePrice = reservePrice;
		this.buyNowPrice = buyNowPrice;
		this.currentBid = currentBid;
		this.maxBid = maxBid;
		this.noOfBids = noOfBids;
		this.startDate = startDate;
		this.endDate = endDate;
		this.sellerID = sellerID;
		this.categoryID = categoryID;
		this.thumbnailURL = thumbnail;
		this.images = images;	
		this.allBids=allBids;
	}
	
	/**
	 * Sets the items ID
	 * @param id The new ID of the item
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setID(long id){
		this.id = id;
	}
	
	/**
	 * Returns the item ID
	 * @return int the itemID
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public long getID(){
		return this.id;
	}
	
	/**
	 * Sets the name of the item
	 * @param name the new name of the item
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * Returns the name of the item
	 * @return string the name of the item
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Sets the new description of the item
	 * @param description the new description
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setDescription(String description){
		this.description = description;
	}
	
	/**
	 * Returns the description of the item
	 * @return string the item's description
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getDescription(){
		return this.description;
	}
	
	/**
	 * Sets the new quantity of items
	 * @param quantity
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setQuantity(int quantity){
		this.quantity = quantity;
	}
	
	/**
	 * Returns the quantity of the number of items
	 * @return in the number of items
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public int getQuantity(){
		return this.quantity;
	}
	
	/**
	 * Sets the initial starting price of the item
	 * @param startPrice the starting price of the item
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setStartPrice(double startPrice){
		this.startPrice = startPrice;
	}
	
	/**
	 * Returns the starting price of the item
	 * @return double the start price of the item
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public double getStartPrice(){
		return this.startPrice;
	}
	
	/**
	 * Sets the new reserve price for the item
	 * @param reservePrice the new reserve price of the item
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setReservePrice(double reservePrice){
		this.reservePrice = reservePrice;
	}
	
	/**
	 * Gets the reserve price of the item
	 * @return double the reserve price of the item
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public double getReservePrice(){
		return this.reservePrice;
	}
	
	/**
	 * Sets the new buy now price of the item
	 * @param buyNowPrice
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setBuyNowPrice(double buyNowPrice){
		this.buyNowPrice = buyNowPrice;
	}
	
	/**
	 * Gets the buy now price of the item
	 * @return double the buy now price of the item
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public double getBuyNowPrice(){
		return this.buyNowPrice;
	}
	
	/**
	 * Sets the current bid price of the item
	 * @param currentBid
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setCurrentBid(double currentBid){
		this.currentBid = currentBid;
	}

	public double getMaxCurrentBidStartPrice(){
		if(this.startPrice>this.currentBid)
			return this.startPrice;
		else
			return this.currentBid;
	}
	
	/**
	 * Gets the current bid price of the item
	 * @return double the current bid price of the item
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public double getCurrentBid(){
		return this.currentBid;
	}
	
	/**
	 * Sets the max bid price of the item
	 * @param maxBid
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setMaxBid(double maxBid){
		this.maxBid = maxBid;
	}
	
	/**
	 * Gets the max bid price of the items
	 * @return double the current max bid price
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public double getMaxBid(){
		return this.maxBid;
	}
	
	/**
	 * Sets the current number of bids for the item
	 * @param noOfBids
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setNoOfBids(int noOfBids){
		this.noOfBids = noOfBids;
	}
	
	/**
	 * Returns the current number of bids for an item
	 * @return in the current number of bids
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public int getNoOfBids(){
		return this.noOfBids;
	}
	
	/**
	 * Sets the start date of the item's auction
	 * @param startDate
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setStartDate(Date startDate){
		this.startDate = startDate;
	}
	
	/**
	 * Returns the start date of the item's auction
	 * @return double the start date of the item's auction
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public Date getStartDate(){
		return this.startDate;
	}
	
	/**
	 * Sets the end date of the auction
	 * @param endDate
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setEndDate(Date endDate){
		this.endDate = endDate;
	}
	
	/**
	 * Returns the end date of the item's auction
	 * @return date the end date of the auction
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public Date getEndDate(){
		return this.endDate;
	}
	
	/**
	 * Sets the sellerDI of the item
	 * @param sellerID
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setSellerID(long sellerID){
		this.sellerID = sellerID;
	}
	
	/**
	 * Returns the userID of the user selling the item
	 * @return int the userID of the seller
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public long getSellerID(){
		return this.sellerID;
	}
	
	/**
	 * Sets the categoryID of the item
	 * @param categoryID
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setCategoryID(long categoryID){
		this.categoryID = categoryID;
	}
	
	/**
	 * Gets the categoryID of the item
	 * @return int the categoryID of the item
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public long getCategoryID(){
		return this.categoryID;
	}
	
	/**
	 * Sets the thumbnail address
	 * @param thumbnail
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setThumbnail(String thumbnail){
		this.thumbnailURL = thumbnail;
	}
	
	/**
	 * Gets the thumbnail address of the item
	 * @return String the thumbnail's address
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getThumbnailURL(){
		return this.thumbnailURL;
	}
	
	/**
	 * Sets the images for the item
	 * @param images
	 * @author Andy (andrewtu@cmu.edu) (andrewtu@cmu.edu)
	 */
	public void setImages(ArrayList<Image> images){
		this.images = images;
	}
	
	/**
	 * Returns the images for the item
	 * @return ArrayList<Image> the images for the item
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public ArrayList<Image> getImages(){
		return this.images;
	}
	
	public ArrayList<Bid> getAllBids(){
		return this.allBids;
	}
	
	public void setAllBids(ArrayList<Bid> allBids){
		this.allBids=allBids;
	}
	
	/**
	 * Returns the first image of the item's images
	 * We have the thumbnail now. So remove calls to this and use the thumbnail instead
	 * @return
	 * @author Andy (andrewtu@cmu.edu)
	 */
	@Deprecated
	public Image getFirstImage(){
		if(this.images == null || this.images.isEmpty())
			return null;
		else return this.images.get(0);
	}
	
	/**
	 * Prints the warning about the object being immutable. This warning can be turned off at the top
	 * by setting immutableWarning=false;
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	private static void printWarning(){
		System.out.println("Item (printWarning): Please consider using this class as immutable. The 'set' methods may not be doing what you think. They only set values for this Java object and does not set them in the database. Talk to me if you want to change the design - Andy");
	}
	
	public String toJSON2(){
		StringBuffer buf = new StringBuffer();
		
		buf.append("{\"id\":\"");
		buf.append(this.getID());
		buf.append("\",\"name\":\"");
		buf.append(this.getName());
		buf.append("\",\"description\":\"");
		buf.append(this.getDescription());
		buf.append("\",\"quantity\":\"");
		buf.append(this.getQuantity());
		buf.append("\",\"startPrice\":\"");
		buf.append(this.getStartPrice());
		buf.append("\",\"reservePrice\":\"");
		buf.append(this.getReservePrice());
		buf.append("\",\"buyNowPrice\":\"");
		buf.append(this.getBuyNowPrice());
		buf.append("\",\"currentBid\":\"");
		buf.append(this.getCurrentBid());
		
		return buf.toString();
	}
	
	@Deprecated
	public String toJSON(){
		return "{\"id\":\""+id+"\",\"name\":\""+name.replaceAll("\"", "'")+"\",\"description\":\""+description.replaceAll("\"", "'")+"\",\"quantity\":\""+quantity+"\",\"startPrice\":\""+startPrice+"\",\"reservePrice\":\""+reservePrice+"\",\"buyNowPrice\":\""+buyNowPrice+"\",\"currentBid\":\""+currentBid+"\",\"maxBid\":\""+maxBid+"\",\"noOfBids\":\""+noOfBids+"\",\"startDate\":\""+startDate+"\",\"endDate\":\""+endDate+"\",\"sellerID\":\""+sellerID+"\",\"categoryID\":\""+categoryID+"\",\"images\":\""+images+"\"}";
	}
}

