package com.cmart.util;

/**
 * This class holds a user's address
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
public class Address {
	private long id;
	private long userID;
	private String street;
	private String town;
	private String zip;
	private int state;
	private Boolean isDefault;
	
	/**
	 * Create an address
	 * @param id The ID of the address
	 * @param userID The userID of the user that lives at this address
	 * @param street The street of the address
	 * @param town The town of the address
	 * @param zip The zip code of the address
	 * @param state The state ID of the address
	 * @param isDefault Whether the address if the user's default address
	 */
	public Address(long id, long userID, String street, String town, String zip, int state, Boolean isDefault){
		this.id = id;
		this.userID = userID;
		this.street = street;
		this.town = town;
		this.zip = zip;
		this.state = state;
		this.isDefault = isDefault;
	}
	
	/**
	 * Get the address's ID
	 * @return The address ID
	 */
	public long getId(){
		return this.id;
	}
	
	/**
	 * Get the user ID of the user that lives at this address
	 * @return The user ID of the user that lives at this address
	 */
	public long getUserID(){
		return this.userID;
	}
	
	/**
	 * Get the street of this address
	 * @return The street address of this address
	 */
	public String getStreet(){
		return this.street;
	}
	
	/**
	 * Get the town of this address
	 * @return The town that this address is in
	 */
	public String getTown(){
		return this.town;
	}
	
	/**
	 * Get the zip code of this address
	 * @return The zip code of this address
	 */
	public String getZip(){
		return this.zip;
	}
	
	/**
	 * Get the state of this address
	 * @return The state this address is in
	 */
	public int getState(){
		return this.state;
	}
	
	/**
	 * Get whether this address is the user's default address
	 * @return
	 */
	public Boolean getIsDefault(){
		return this.isDefault;
	}
	
	//TODO: check 'id' and 'addressid' in the code to see why there is a difference/wherer used
	/**
	 * Get the json string that represents this address
	 * @return The JSON string of this address
	 */
	public String toJSON(){
		StringBuffer buf = new StringBuffer();
		
		buf.append("{\"addressid\":\"");
		buf.append(this.getId());
		buf.append("\",\"userid\":");
		buf.append(this.getUserID());
		buf.append(",\"street\":\"");
		buf.append(this.getStreet());
		buf.append("\",\"town\":\"");
		buf.append(this.getTown());
		buf.append("\",\"zip\":\"");
		buf.append(this.getZip());
		buf.append("\",\"state\":\"");
		buf.append(this.getState());
		buf.append("\",\"isdefault\":\"");
		buf.append(this.getIsDefault());
		buf.append("\"}");
		
		return buf.toString();
	}
	
	/**
	 * Get the CSV of this address
	 * @return The CSV of this address
	 */
	public String toCSVString(){
		StringBuffer sb = new StringBuffer(128);
		sb.append(id);
		sb.append(',');
		sb.append(userID);
		sb.append(',');
		sb.append(street);
		sb.append(',');
		sb.append(town);
		sb.append(',');
		sb.append(zip);
		sb.append(',');
		sb.append(state);
		sb.append(',');
		sb.append(isDefault);
		sb.append(',');
		
		return sb.toString();
	}
}
