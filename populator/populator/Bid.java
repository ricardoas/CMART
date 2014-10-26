package populator;

/**
 * This class holds the data of a bid that the user had made.
 * 
 * @author Andy (andrewtu@cmu.edu)
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

import java.util.Date;

public class Bid {
	// Variables to hold the user data
	private Item bidItem;
	private User bidder;
	private long id;
	private long userID;
	private int quantity;
	private double bid;
	private double maxBid;
	private Date bidDate;
	private static final boolean immutableWarning = true;
	
	/**
	 * Create a new bid object.
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public Bid(){
		if(Bid.immutableWarning) Bid.printWarning();
	}
	
	/**
	 * Create a new bid object will all of the info included
	 * 
	 * @param quantity
	 * @param bid
	 * @param maxBid
	 * @param bidDate
	 * @param bidItem
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public Bid(long id, long userID, int quantity, double bid, double maxBid, Date bidDate, Item bidItem, User bidder){
		this.id = id;
		this.userID = userID;
		this.quantity = quantity;
		this.bid = bid;
		this.maxBid = maxBid;
		this.bidDate = bidDate;
		this.bidItem = bidItem;
		this.bidder=bidder;
	}
	
	
	public long getUserID(){
		return this.userID;
	}
	
	/**
	 * Return the number that the user wants to buy
	 * 
	 * @return int the number of items the user is willing to buy
	 */
	public int getQuantity(){
		return this.quantity;
	}
	
	/**
	 * Set the number of items that the user is willing to buy
	 * 
	 * @param quantity
	 */
	public void setQuantity(int quantity){
		if(Bid.immutableWarning) Bid.printWarning();
		this.quantity = quantity;
	}
	
	/**
	 * Get the amount the user has currently bid for the item
	 * 
	 * @return double the amount the user bid
	 */
	public double getBid(){
		return this.bid;
	}
	
	/**
	 * Set the amount that the user has bid for the item
	 * 
	 * @param bid
	 */
	public void setBid(double bid){
		if(Bid.immutableWarning) Bid.printWarning();
		this.bid = bid;
	}
	
	/**
	 * Get the maximum that the user is willing to bid for the item
	 * 
	 * @return double the max the user is willing to pay
	 */
	public double getMaxBid(){
		return this.maxBid;
	}
	
	/**
	 * Set the maximum amount that the user is willing to pay for the item
	 * 
	 * @param maxBid
	 */
	public void setMaxBid(double maxBid){
		if(Bid.immutableWarning) Bid.printWarning();
		this.maxBid = maxBid;
	}
	
	/**
	 * Get the date that the user placed the bid
	 * 
	 * @return Date when the user placed the bid
	 */
	public Date getBidDate(){
		return this.bidDate;
	}
	
	/**
	 * Set the date when the user placed the bid
	 * 
	 * @param bidDate
	 */
	public void setBidDate(Date bidDate){
		if(Bid.immutableWarning) Bid.printWarning();
		this.bidDate = bidDate;
	}
	
	/**
	 * Get the item that the user bid for
	 * 
	 * @return Item that the user bid for
	 */
	public Item getItem(){
		return this.bidItem;
	}
	
	/**
	 * Set the item that the user bid for
	 * 
	 * @param bidItem
	 */
	public void setItem(Item bidItem){
		if(Bid.immutableWarning) Bid.printWarning();
		this.bidItem = bidItem;
	}
	
	/**
	 * Print the warning about this object being immutable
	 * This can be turned off with immutableWarning=false;
	 * 
	 */
	private static void printWarning(){
		System.out.println("Bid (printWarning): Please consider using this class as immutable. The 'set' methods may not be doing what you think. They only set values for this Java object and does not set them in the database. Talk to me if you want to change the design - Andy");
	}

}
