package com.cmart.util;

import java.util.Date;

/**
 * This object hold and comment read from the data base.
 * 
 * @author Chaohua (chaohuam@andrew.cmu.edu, ma.chaohua@gmail.com)
 * @version 0.1
 * @since 0.1
 * @date 03/01/2012
 */
public class FriendRequest {
	private long id;
	private long fromID;
	private long toID;
	private String message;

	public FriendRequest(long id, long fromID, long toID, String message) {
		this.id = id;
		this.fromID = fromID;
		this.toID = toID;
		this.message = message;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getFromID() {
		return fromID;
	}

	public void setFromID(long fromID) {
		this.fromID = fromID;
	}

	public long getToID() {
		return toID;
	}

	public void setToID(long toID) {
		this.toID = toID;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}