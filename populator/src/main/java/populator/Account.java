package populator;

/**
 * This class hold a user's account
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

import java.util.Date;

//TODO: I think this can be deleted

public class Account {
	private long accountID;
	private String name;
	private String nameOnCard;
	private String creditCardNo;
	private String cvv;
	private Date expirationDate;
	
	public Account(long accountID, String name, String nameOnCard, String creditCardNo, String cvv, Date expirationDate){
		this.name = name;
		this.accountID = accountID;
		this.nameOnCard = nameOnCard;
		this.creditCardNo = creditCardNo;
		this.cvv = cvv;
		this.expirationDate = expirationDate;
	}
	
	/*public String getName(){
		return this.name;
	}
	
	public long getAccountID(){
		return this.accountID;
	}
	
	public String getNameOnCard(){
		return this.nameOnCard;
	}
	
	public String getCreditCardNo(){
		return this.creditCardNo;
	}
	
	public String getCVV(){
		return this.cvv;
	}
	
	public Date getExpirationDate(){
		return this.expirationDate;
	}*/
}
