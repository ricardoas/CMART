package populator;

/**
 * This class hold a users address
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
	 * Create the address
	 * 
	 * @param id
	 * @param userID
	 * @param street
	 * @param town
	 * @param zip
	 * @param state
	 * @param isDefault
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
	 * Get the address's id
	 * @return
	 */
	public long getId(){
		return this.id;
	}
	
	/**
	 * Get the user ID that this address belongs to
	 * @return
	 */
	public long getUserID(){
		return this.userID;
	}
	
	/**
	 * Get the street name of the address
	 * @return
	 */
	public String getStreet(){
		return this.street;
	}
	
	/**
	 * Get the town of the address
	 * @return
	 */
	public String getTown(){
		return this.town;
	}
	
	/**
	 * Get the zip code of the address
	 * @return
	 */
	public String getZip(){
		return this.zip;
	}
	
	/**
	 * Get the state of the address
	 * @return
	 */
	public int getState(){
		return this.state;
	}
	
	/**
	 * Get whether the address is the default address or not
	 * @return
	 */
	public Boolean getIsDefault(){
		return this.isDefault;
	}
}
