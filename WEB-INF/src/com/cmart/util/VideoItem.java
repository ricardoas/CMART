package com.cmart.util;

import java.util.*;

import com.mysql.jdbc.Buffer;

public class VideoItem {
	
	//variables to hold all the video data :)
	
	private long id;
	private String description;
	private String url;
	private String name;
	private long userID;
	
	public VideoItem(long id,  String url,String name, String description,long userID){
		this.setId(id);
		this.setDescription(description);
		this.setUrl(url);
		this.setName(name);
		this.setUserID(userID);
		
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}


	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public long getUserID() {
		return userID;
	}

}
