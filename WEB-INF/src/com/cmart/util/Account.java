package com.cmart.util;

import java.util.Date;

/**
 * 
 * @author Andy (turner.andy@gmail.com)
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

public class Account {
	private long accountID = -1l;
	private String name = null;
	private String nameOnCard = null;
	private String creditCardNo = null;
	private String cvv = null;
	private Date expirationDate = null;
	
	/**
	 * Construct an account
	 * @param accountID The ID of the account
	 * @param name The name on the account
	 * @param nameOnCard The name on the credit card
	 * @param creditCardNo The credit card number
	 * @param cvv The cards cvv code
	 * @param expirationDate The expiration date of the card
	 */
	public Account(long accountID, String name, String nameOnCard, String creditCardNo, String cvv, Date expirationDate){
		this.name = name;
		this.accountID = accountID;
		this.nameOnCard = nameOnCard;
		this.creditCardNo = creditCardNo;
		this.cvv = cvv;
		this.expirationDate = expirationDate;
	}
	
	/**
	 * Set the name on the card
	 * @param name The name on the card
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * Get the name on the card
	 * @return The name on the card
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Get the account ID
	 * @return the account ID
	 */
	public long getAccountID(){
		return this.accountID;
	}
	
	/**
	 * Get the name on the card
	 * @return The name on the card
	 */
	public String getNameOnCard(){
		return this.nameOnCard;
	}
	
	/**
	 * Get the credit card number
	 * @return The credit card number
	 */
	public String getCreditCardNo(){
		return this.creditCardNo;
	}
	
	/**
	 * Get the cvv code on the card
	 * @return The cvv code
	 */
	public String getCVV(){
		return this.cvv;
	}
	
	/**
	 * Get the expiration date of the card
	 * @return The expiration date
	 */
	public Date getExpirationDate(){
		return this.expirationDate;
	}
}
