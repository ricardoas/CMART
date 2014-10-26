package com.cmart.util;

import java.util.*;
//import org.json.JSONException;
//import org.json.JSONObject;

/**
 * This object hold and comment read from the data base. 
 * 
 * @author Bo (bol1@andrew.cmu.edu)
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
public class Comment {
	private long id;
	private long fromUserID;
	private long toUserID;
	private long itemID;
	private int rating;
	private Date date;
	private String comment;
	
	/**
	 * Create a new comment
	 * @param id The ID of the comment
	 * @param fromUserID The id of the user who sent the comment
	 * @param toUserID The id of the user who the comment is to
	 * @param itemID The item id that the comment refers to
	 * @param rating The 'rating' that the from user is giving the seller
	 * @param date The date the comment was created
	 * @param comment The comment's text
	 */
	public Comment(long id, long fromUserID, long toUserID, long itemID, int rating, Date date, String comment){
		this.id = id;
		this.fromUserID = fromUserID;
		this.toUserID = toUserID;
		this.itemID = itemID;
		this.rating = rating;
		this.date = date;
		this.comment = comment;
	}
	
	/**
	 * Gets the ID of this comment
	 * @return The ID of the comment
	 */
	public long getID(){
		return this.id;
	}
	
	/**
	 * The ID of the user who wrote this comment
	 * @return
	 */
	public long getFromUserID(){
		return this.fromUserID;
	}
	
	/**
	 * The ID of the user who this comment is to
	 * @return The ID of the user this comment is to
	 */
	public long getToUserID(){
		return this.toUserID;
	}
	
	/**
	 * The item ID that this comment refers to
	 * @return The item ID of the item this comment is about
	 */
	public long getItemID(){
		return this.itemID;
	}
	
	/**
	 * Gets the rating that the from user give to the to user
	 * @return The rating of the 'seller' user
	 */
	public int getRating(){
		return this.rating;
	}
	
	/**
	 * Gets the date this comment was posted
	 * @return The date the comment was made
	 */
	public Date getDate(){
		return this.date;
	}
	
	/**
	 * Gets the text in the comment
	 * @return The text in the comment
	 */
	public String getComment(){
		return this.comment;
	}
	
	/**
	 * Returns the comment as a JSON string
	 * @return comment as JSON string 
	 */
	public String toJSON(){
		StringBuffer buf = new StringBuffer();
		buf.append("{\"id\":");
		buf.append(this.getID());
		buf.append(",\"fromID\":");
		buf.append(this.fromUserID);
		buf.append(",\"toID\":");
		buf.append(this.toUserID);
		buf.append(",\"itemID\":");
		buf.append(this.itemID);
		buf.append(",\"rating\":");
		buf.append(this.rating);
		buf.append(",\"date\":\"");
		buf.append(this.date);
		buf.append("\",\"comment\":\"");
		buf.append(this.comment);
		buf.append("\"}");
		
		return buf.toString();
	}
}

