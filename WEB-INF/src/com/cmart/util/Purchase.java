package com.cmart.util;

/**
 * This class hold a purchase of an item by a customer
 * @author andy
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

public class Purchase {
	private long id;
	private Item item;
	private int quantity;
	private double price;
	private Boolean paid;
	
	/**
	 * Construct a purchase object
	 * @param id The ID of the purchase
	 * @param item The item being purchased
	 * @param quantity The quantity being purchased
	 * @param price The price being paid for the item
	 * @param paid Whether or not the user has paid for the item
	 */
	public Purchase(long id, Item item, int quantity, double price, Boolean paid){
		this.id = id;
		this.item = item;
		this.quantity = quantity;
		this.price = price;
		this.paid = paid;
	}
	
	/**
	 * Gets the ID of this purchase
	 * @return The ID of the purcahse
	 */
	public long getID(){
		return this.id;
	}
	
	/**
	 * Gets the item that is being purchased
	 * @return The item being purchased
	 */
	public Item getItem(){
		return this.item;
	}
	
	/**
	 * Gets the quantity being purchased
	 * @return The quantity of the item being purchased
	 */
	public int getQuantity(){
		return this.quantity;
	}
	
	/**
	 * Gets the price being paid for the item
	 * @return The price paid for the item
	 */
	public double getPrice(){
		return this.price;
	}
	
	/**
	 * Gets whether the user has paid for the item of not
	 * @return
	 */
	public Boolean getPaid(){
		return this.paid;
	}
	
	/**
	 * Gets the purchase as a JSON string
	 * @return The purchase as a JSON string
	 */
	//TODO: String Buffer
	public String toJSON(){
		String output;
		if(item!=null){
			output = "{\"quantity\":"+quantity+",\"price\":"+price+",\"paid\":"+paid+",\"id\":"+id+",\"purchaseItem\":"+item.toJSON()+"}";
		}else{
			output = "{\"quantity\":"+quantity+",\"price\":"+price+",\"paid\":"+paid+",\"id\":"+id+",\"purchaseItem\":{\"id\":\"-1\"}}";
		}
		return output;
	}
}
