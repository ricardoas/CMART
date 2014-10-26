package com.cmart.util;

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
 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
 * @since 0.1
 * @version 1.0
 * @date 23rd Aug 2012
 * 
 * C-MART Benchmark
 * Copyright (C) 2011-2012 theONE Networking Group, Carnegie Mellon University, Pittsburgh, PA 15213, U.S.A
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
public class Item {
	// Variables to hold all of the item data
	private long id = -1l;
	private String name = null;
	private String description = null;
	private int quantity = -1;
	private double startPrice = -1.0f;
	private double reservePrice = -1.0f;
	private double buyNowPrice = -1.0f;
	private double currentBid = -1.0f;
	private double maxBid = -1.0f;
	private int noOfBids = -1;
	private Date startDate = null;
	private Date endDate = null;
	private long sellerID = -1l;
	private long categoryID = -1l;
	private String thumbnailURL = null; 
	private ArrayList<Image> images = null;
	private static boolean immutableWarning = true;
	//private long ts = -1l;

	/**
	 * Create a new item. As the user has not specified any data, we know they are going to use it as mutable
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
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
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public Item(long id, String name, String description, int quantity, double startPrice, double reservePrice, double buyNowPrice,
			double currentBid, double maxBid, int noOfBids, Date startDate, Date endDate, long sellerID, long categoryID, String thumbnail, ArrayList<Image> images){
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
	}

	/**
	 * Sets the items ID
	 * @param id The new ID of the item
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setID(long id){
		this.id = id;
	}

	/**
	 * Returns the item ID
	 * @return long the itemID
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public long getID(){
		return this.id;
	}

	/**
	 * Sets the name of the item
	 * @param name the new name of the item
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setName(String name){
		this.name = name;
	}

	/**
	 * Returns the name of the item
	 * @return string the name of the item
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getName(){
		return this.name;
	}

	/**
	 * Sets the new description of the item
	 * @param description the new description
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setDescription(String description){
		this.description = description;
	}

	/**
	 * Returns the description of the item
	 * @return string the item's description
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getDescription(){
		return this.description;
	}

	/**
	 * Sets the new quantity of items
	 * @param quantity
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setQuantity(int quantity){
		this.quantity = quantity;
	}

	/**
	 * Returns the quantity of the number of items
	 * @return in the number of items
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public int getQuantity(){
		return this.quantity;
	}

	/**
	 * Sets the initial starting price of the item
	 * @param startPrice the starting price of the item
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setStartPrice(double startPrice){
		this.startPrice = startPrice;
	}

	/**
	 * Returns the starting price of the item
	 * @return double the start price of the item
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public double getStartPrice(){
		return this.startPrice;
	}

	/**
	 * Sets the new reserve price for the item
	 * @param reservePrice the new reserve price of the item
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setReservePrice(double reservePrice){
		this.reservePrice = reservePrice;
	}

	/**
	 * Gets the reserve price of the item
	 * @return double the reserve price of the item
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public double getReservePrice(){
		return this.reservePrice;
	}

	/**
	 * Sets the new buy now price of the item
	 * @param buyNowPrice
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setBuyNowPrice(double buyNowPrice){
		this.buyNowPrice = buyNowPrice;
	}

	/**
	 * Gets the buy now price of the item
	 * @return double the buy now price of the item
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public double getBuyNowPrice(){
		return this.buyNowPrice;
	}

	/**
	 * Sets the current bid price of the item
	 * @param currentBid
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setCurrentBid(double currentBid){
		this.currentBid = currentBid;
	}

	/**
	 * Returns the maximum of the current bid price or the start price, this should be the
	 * price displayed to bid above
	 * @return Max of start price and current bid price
	 */
	public double getMaxCurrentBidStartPrice(){
		if(this.startPrice>this.currentBid)
			return this.startPrice;
		else
			return this.currentBid;
	}

	/**
	 * Gets the current bid price of the item
	 * @return double the current bid price of the item
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public double getCurrentBid(){
		return this.currentBid;
	}

	/**
	 * Sets the max bid price of the item
	 * @param maxBid
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setMaxBid(double maxBid){
		this.maxBid = maxBid;
	}

	/**
	 * Gets the max bid price of the items
	 * @return double the current max bid price
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public double getMaxBid(){
		return this.maxBid;
	}

	/**
	 * Sets the current number of bids for the item
	 * @param noOfBids
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setNoOfBids(int noOfBids){
		this.noOfBids = noOfBids;
	}

	/**
	 * Returns the current number of bids for an item
	 * @return in the current number of bids
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public int getNoOfBids(){
		return this.noOfBids;
	}

	/**
	 * Sets the start date of the item's auction
	 * @param startDate
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setStartDate(Date startDate){
		this.startDate = startDate;
	}

	/**
	 * Returns the start date of the item's auction
	 * @return double the start date of the item's auction
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public Date getStartDate(){
		return this.startDate;
	}

	/**
	 * Sets the end date of the auction
	 * @param endDate
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setEndDate(Date endDate){
		this.endDate = endDate;
	}

	/**
	 * Returns the end date of the item's auction
	 * @return date the end date of the auction
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public Date getEndDate(){
		return this.endDate;
	}

	/**
	 * Sets the sellerDI of the item
	 * @param sellerID
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setSellerID(long sellerID){
		this.sellerID = sellerID;
	}

	/**
	 * Returns the userID of the user selling the item
	 * @return long the userID of the seller
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public long getSellerID(){
		return this.sellerID;
	}

	/**
	 * Sets the categoryID of the item
	 * @param categoryID
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setCategoryID(long categoryID){
		this.categoryID = categoryID;
	}

	/**
	 * Gets the categoryID of the item
	 * @return long the categoryID of the item
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public long getCategoryID(){
		return this.categoryID;
	}

	/**
	 * Sets the thumbnail address
	 * @param thumbnail
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setThumbnailURL(String thumbnail){
		this.thumbnailURL = thumbnail;
	}

	/**
	 * Gets the thumbnail address of the item
	 * @return String the thumbnail's address
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getThumbnailURL(){
		return this.thumbnailURL;
	}

	/**
	 * Sets the images for the item
	 * @param images
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com) (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public void setImages(ArrayList<Image> images){
		this.images = images;
	}

	/**
	 * Returns the images for the item
	 * @return ArrayList<Image> the images for the item
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public ArrayList<Image> getImages(){
		return this.images;
	}

	/**
	 * Returns the item as a JSON string
	 * @return
	 */
	public String getImagesJSON(){
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		if(this.images!=null && this.images.size()>0){
			//			try{
			//				String imgJSON = "";
			//				imgJSON = this.images.get(0).toJSON();
			//				buf.append(imgJSON);
			//			}
			//			catch(Exception e){
			//				// Could be a cast exception
			//			}

			for (int i = 0;i<this.images.size();i++){
				try{
					String imgJSON = "";
					imgJSON = this.images.get(i).toJSON();

					if(i>0)
						buf.append(",");
					buf.append(imgJSON);
				}
				catch(Exception e){
					// Could be a cast exception
				}
			}
		}

		buf.append("]");
		return buf.toString();
	}

	/**
	 * Returns the first image of the item's images
	 * We have the thumbnail now. So remove calls to this and use the thumbnail instead
	 * @return
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	@Deprecated
	/*public Image getFirstImage(){
		if(this.images == null || this.images.isEmpty())
			return null;
		else return this.images.get(0);
	}*/

	/**
	 * Prints the warning about the object being immutable. This warning can be turned off at the top
	 * by setting immutableWarning=false;
	 * 
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	private static void printWarning(){
		System.out.println("Item (printWarning): Please consider using this class as immutable. The 'set' methods may not be doing what you think. They only set values for this Java object and does not set them in the database. Talk to me if you want to change the design - Andy");
	}

	private Boolean reserveMet(){
		if(this.currentBid>this.reservePrice){
			return true;
		}else{
			return false;
		}
	}


	public String toJSON(){
		StringBuffer buf = new StringBuffer();

		buf.append("{\"id\":");
		buf.append(this.getID());
		buf.append(",\"name\":\"");
		buf.append(org.apache.commons.lang.StringEscapeUtils.escapeHtml(this.getName()));
		buf.append("\",\"description\":\"");
		buf.append(org.apache.commons.lang.StringEscapeUtils.escapeHtml(this.getDescription()));
		buf.append("\",\"quantity\":");
		buf.append(this.getQuantity());
		buf.append(",\"startPrice\":");
		buf.append(this.getStartPrice());
		buf.append(",\"reservePrice\":");
		buf.append(this.getReservePrice());
		buf.append(",\"buyNowPrice\":");
		buf.append(this.getBuyNowPrice());
		buf.append(",\"currentBid\":");
		buf.append(this.getMaxCurrentBidStartPrice());
		buf.append(",\"noOfBids\":");
		buf.append(this.getNoOfBids());
		buf.append(",\"startDate\":\"");
		buf.append(this.getStartDate());
		buf.append("\",\"endDate\":\"");
		buf.append(this.getEndDate());
		buf.append("\",\"sellerID\":");
		buf.append(this.getSellerID());
		buf.append(",\"categoryID\":");
		buf.append(this.getCategoryID());
		buf.append(",\"reserve\":");
		buf.append(this.reserveMet());
		buf.append(",\"images\":");
		buf.append(this.getImagesJSON());
		buf.append(",\"thumbnail\":\"");
		buf.append(this.getThumbnailURL());
		buf.append("\"}");

		return buf.toString();
	}

	public int hashCode() {
        return (int) this.id;
    }

    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        
        if(o instanceof Item){
        	Item temp = (Item)o;
        	if(temp.getID()==this.id) return true;
        }

        return false;
    }
	
	public static void suppressImmutableWarning(){
		Item.immutableWarning = false;
	}
	
}

