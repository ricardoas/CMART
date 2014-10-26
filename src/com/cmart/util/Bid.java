package com.cmart.util;

import java.util.Date;


/**
 * This class holds the data of a bid that the user had made. We may not need all of the information so
 * we have the tradeoff between making things immutable and reading less from the DB
 * 
 * TODO: Andy: I think we should go with the 'reading as little from the DB as we can' way. I need to update
 * ALL (aarrrrrrr!!!!) of the SQL statement
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
public class Bid {
	// Variables to hold the user data
	private Item bidItem = null;
	private User bidder = null;
	private long id = -1l;
	private long userID = -1l;
	private int quantity = -1;
	private double bid = -1.0;
	private double maxBid = -1.0;
	private Date bidDate = null;
	private long itemID = -1l;
	private static boolean immutableWarning = true;
	
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
	 * @param id
	 * @param userID
	 * @param quantity
	 * @param bid
	 * @param maxBid
	 * @param bidDate
	 * @param bidItem
	 * @param user
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public Bid(long id, long userID, int quantity, double bid, double maxBid, Date bidDate, Item bidItem, User bidder){
		this.id = id;
		this.userID = userID;
		this.quantity = quantity;
		this.bid = bid;
		this.maxBid = maxBid;
		this.bidDate = bidDate;
		
		if(bidItem!=null) this.itemID=bidItem.getID();
		this.bidItem = bidItem;
		
		if(bidder!=null) this.userID = bidder.getID();
		this.bidder=bidder;
	}
	
	//TODO: this is old
	public Bid(int quantity, double bid, double maxBid, Date bidDate, Item bidItem){
		this.quantity = quantity;
		this.bid = bid;
		this.maxBid = maxBid;
		this.bidDate = bidDate;
		this.bidItem = bidItem;
	}
	
	//TODO: update this
	public String toJSON(){
		String output;
		if(bidItem!=null&&bidder!=null){
			String name=bidder.getUsername();
			StringBuffer bidderName=new StringBuffer("*****");
			bidderName.replace(0,1,name.substring(0,1));
			bidderName.replace(bidderName.length()-1,bidderName.length(),name.substring(name.length()-1,name.length()));
			output = "{\"quantity\":"+quantity+",\"bid\":"+bid+",\"maxBid\":"+maxBid+",\"bidDate\":\""+bidDate+"\",\"bidItem\":"+bidItem.toJSON()+",\"bidderName\":\""+bidderName+"\"}";
		}else{
			output = "{\"quantity\":"+quantity+",\"bid\":"+bid+",\"maxBid\":"+maxBid+",\"bidDate\":\""+bidDate+"\",\"bidItem\":{\"id\":\"-1\"},\"bidderName\":\"*****\"}";
		}
		return output;
	}
	
	public long getUserID(){
		return this.userID;
	}
	
	/**
	 * Return the number that the user wants to buy
	 * 
	 * @return int the number of items the user is willing to buy
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public int getQuantity(){
		return this.quantity;
	}
	
	/**
	 * Set the number of items that the user is willing to buy
	 * 
	 * @param quantity
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setQuantity(int quantity){
		if(Bid.immutableWarning) Bid.printWarning();
		this.quantity = quantity;
	}
	
	/**
	 * Get the amount the user has currently bid for the item
	 * 
	 * @return double the amount the user bid
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public double getBid(){
		return this.bid;
	}
	
	/**
	 * Set the amoung that the user has bid for the item
	 * 
	 * @param bid
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setBid(double bid){
		if(Bid.immutableWarning) Bid.printWarning();
		this.bid = bid;
	}
	
	/**
	 * Get the maximum that the user is willing to bid for the item
	 * 
	 * @return double the max the user is willing to pay
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public double getMaxBid(){
		return this.maxBid;
	}
	
	/**
	 * Set the maximum amount that the user is willing to pay for the item
	 * 
	 * @param maxBid
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setMaxBid(double maxBid){
		if(Bid.immutableWarning) Bid.printWarning();
		this.maxBid = maxBid;
	}
	
	/**
	 * Get the date that the user placed the bid
	 * 
	 * @return Date when the user placed the bid
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public Date getBidDate(){
		return this.bidDate;
	}
	
	/**
	 * Set the date when the user placed the bid
	 * 
	 * @param bidDate
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setBidDate(Date bidDate){
		if(Bid.immutableWarning) Bid.printWarning();
		this.bidDate = bidDate;
	}
	
	/**
	 * Get the item that the user bid for
	 * 
	 * @return Item that the user bid for
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public Item getItem(){
		return this.bidItem;
	}
	
	/**
	 * Set the item that the user bid for
	 * 
	 * @param bidItem
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public void setItem(Item bidItem){
		this.bidItem = bidItem;
		if(this.bidItem!=null) this.itemID = bidItem.getID();
		else this.itemID = -1l;
	}
	
	public User getBidder(){
		return bidder;
	}
	
	public void setItemID(long itemID){
		this.itemID=itemID;
	}
	
	public long getItemID(){
		return this.itemID;
	}
	
	public static void suppressImmutableWarning(){
		Bid.immutableWarning = false;
	}
	
	/**
	 * Print the warning about this object being immutable
	 * This can be turned off with immutableWarning=false;
	 * 
	 * @author Andy (andrewtu@cmu.edu)
	 */
	private static void printWarning(){
		System.out.println("Bid (printWarning): Please consider using this class as immutable. The 'set' methods may not be doing what you think. They only set values for this Java object and does not set them in the database. Talk to me if you want to change the design - Andy");
	}

}
