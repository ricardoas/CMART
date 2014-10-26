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
public class WallPost {
	private long id;
	private long fromID;
	private long toID;
	private long replyID;
	private String text;
	private Date ts;

	public WallPost(long id, long fromID, long toID, long replyID, String text, Date ts) {
		this.id = id;
		this.fromID = fromID;
		this.toID = toID;
		this.replyID = replyID;
		this.text = text;
		this.ts = ts;
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

	public long getReplyID() {
		return replyID;
	}

	public void setReplyID(long replyID) {
		this.replyID = replyID;
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
}