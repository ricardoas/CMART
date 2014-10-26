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
public class Message {
	private long id;
	private long fromID;
	private long toID;
	private String text;
	private Date ts;
	private int read;

	public Message(long id, long fromID, long toID, String text, Date ts, int read) {
		this.id = id;
		this.fromID = fromID;
		this.toID = toID;
		this.text = text;
		this.ts = ts;
		this.read = read;
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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getTs() {
		return ts;
	}

	public void setTs(Date ts) {
		this.ts = ts;
	}

	public int getRead() {
		return read;
	}

	public void setRead(int read) {
		this.read = read;
	}

}
