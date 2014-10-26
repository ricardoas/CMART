package com.cmart.util;

/**
 * This class hold all of the data about a user
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
public class User {
	// Variables to hold the user data
	private long id;
	private String firstName = null;
	private String lastName = null;
	private String username = null;
	private String password = null;
	private String email = null;
	private String authToken = null;
	private String rating = null;
	
	/**
	 * Create a new user with all of the required data
	 * @param id
	 * @param firstName
	 * @param lastName
	 * @param username
	 * @param password
	 * @param email
	 * @param authToken
	 * @param rating
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public User(long id, String firstName, String lastName, String username, String password, String email, String authToken, String rating){
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.password = password;
		this.email = email;
		this.authToken = authToken;
		this.rating = rating;
	}
	
	/**
	 * Create a new user that only contains public information
	 * @param id
	 * @param username
	 * @param rating
	 */
	public User(long id, String username,String rating){
		this.id = id;
		this.username = username;
		this.rating=rating;
	}
	
	/**
	 * Returns the user's userID
	 * 
	 * @return long the userID
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public long getID(){
		return this.id;
	}
	
	/**
	 * Returns the user's firstname
	 * @return String the user's first name
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getFirstName(){
		return this.firstName;
	}
	
	/**
	 * Returns the user's lastname
	 * 
	 * @return String the user's last name
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getLastName(){
		return this.lastName;
	}
	
	/**
	 * Returns the user's username
	 * 
	 * @return String the user's username
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getUsername(){
		return this.username;
	}
	
	/**
	 * Returns the user's password
	 * 
	 * @return String the user's password
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getPassword(){
		return this.password;
	}
	
	/**
	 * Returns the user's email address
	 * 
	 * @return String the user's email address
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getEmail(){
		return this.email;
	}
	
	/**
	 * Returns the user's authToken
	 * 
	 * @return String the user's authToken
	 */
	public String getAuthToken(){
		return this.authToken;
	}
	
	/**
	 * Returns the user's seller rating
	 * 
	 * @return The user's seller rating
	 */
	public String getRating(){
		return this.rating;
	}
	
	/**
	 * Returns the public user object as a JSON string
	 * @return public User object as a json string
	 */
	//TODO: change 'name' to 'username'
	public String toJSON(){
		StringBuffer buf = new StringBuffer();
		buf.append("{\"id\":");
		buf.append(this.getID());
		buf.append(",\"name\":\"");
		buf.append(this.getUsername());
		buf.append("\",\"rating\":\"");
		buf.append(this.getRating());
		buf.append("\"}");
		
		return buf.toString();
	}
	
	/**
	 * Returns the full user object as a JSON string
	 * @return full User object as a json string
	 */
	public String FULLtoJSON(){
		StringBuffer buf = new StringBuffer();
		buf.append("{\"id\":");
		buf.append(this.getID());
		buf.append(",\"username\":\"");
		buf.append(this.getUsername());
		buf.append("\",\"firstname\":\"");
		buf.append(this.getFirstName());
		buf.append("\",\"lastname\":\"");
		buf.append(this.getLastName());
		buf.append("\",\"email\":\"");
		buf.append(this.getEmail());
		buf.append("\",\"password\":\"");
		buf.append(this.getPassword());
		buf.append("\",\"rating\":\"");
		buf.append(this.getRating());
		buf.append("\"}");
		return buf.toString();
	}
	
	/**
	 * Returns the full user object as a CSV string
	 * @return full user object as CSV
	 */
	public String toCSVString(){
		StringBuffer sb = new StringBuffer(128);
		sb.append(id);
		sb.append(',');
		sb.append(firstName);
		sb.append(',');
		sb.append(lastName);
		sb.append(',');
		sb.append(username);
		sb.append(',');
		sb.append(password);
		sb.append(',');
		sb.append(email);
		sb.append(',');
		sb.append(authToken);
		sb.append(',');
		sb.append(rating);
		sb.append(',');
		
		return sb.toString();
	}
}
