package com.cmart.util;
import java.util.*;

/**
 * 
 * @author Bo
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
public class Question {
	private long id;
	private long fromUserID;
	private long toUserID;
	private long itemID;
	private boolean isQuestion;
	private Date postDate;
	private String content;
	private long responseTo;
	
	/**
	 * Create a new question with all of the items data from the DB
	 * @author Bo (bol1@andrew.cmu.edu)
	 * 
	 * @param id
	 * @param fromUserID
	 * @param toUserID
	 * @param itemID
	 * @param iq
	 * @param date
	 * @param content
	 */
	public Question(long id, long fromUserID, long toUserID, long itemID, boolean isQuestion, long responseTo,  Date date, String content){
		this.id = id;
		this.fromUserID = fromUserID;
		this.toUserID = toUserID;
		this.itemID = itemID;
		this.isQuestion = isQuestion;
		this.responseTo = responseTo;
		this.postDate = date;
		this.content = content;
	}
	
	/**
	 * Gets the ID of the question
	 * @return The ID of the question
	 */
	public long getID(){
		return id;
	}
	
	/**
	 * Gets the user ID of the user who is sending this question
	 * @return User ID of user who send message
	 */
	public long getFromUserID(){
		return fromUserID;
	}
	
	/**
	 * Gets the user ID of the user who this question is to
	 * @return User ID of user question is to
	 */
	public long getToUserID(){
		return toUserID;
	}
	
	/**
	 * Gets the item ID of the item that this question relates to
	 * @return The ID of the item question relates to
	 */
	public long getItemID(){
		return itemID;
	}
	
	/**
	 * Gets whether this question is a question or a reply to a previous question
	 * @return True if question, false if reply
	 */
	public boolean getIsQuestion(){
		return isQuestion;
	}
	
	/**
	 * Gets the text that is in the question
	 * @return The question's text
	 */
	public String getContent(){
		return content;
	}
	
	/**
	 * Gets the date the question was created
	 * @return The date the question was created
	 */
	public Date getPostDate(){
		 return postDate;
	}
	
	/**
	 * Gets the questionID that the answer is in response to
	 * responseTo=-1 if the question is a question and not an answer
	 * @return The questionID the answer is in response to
	 */
	public long getResponseTo(){
		return responseTo;
	}
	
	/**
	 * Returns the object as a JSON string
	 * @return
	 */
	public String toJSON(){
		StringBuffer buf = new StringBuffer();
		buf.append("{\"id\":");
		buf.append(this.getID());
		buf.append(",\"fromUserID\":");
		buf.append(this.getFromUserID());
		buf.append(",\"toUserID\":");
		buf.append(this.getToUserID());
		buf.append(",\"itemID\":");
		buf.append(this.getItemID());
		buf.append(",\"isQuestion\":");
		buf.append(this.getIsQuestion());
		buf.append(",\"postDate\":\"");
		buf.append(this.getPostDate());
		buf.append("\",\"responseTo\":\"");
		buf.append(this.getResponseTo());
		buf.append("\",\"content\":\"");
		buf.append(this.getContent().replaceAll("\n\r", "<br />").replaceAll("\n", "<br />").replaceAll("\r", "<br />"));
		buf.append("\"}");
		
		return buf.toString();
	}
	
}

