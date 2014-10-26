package populator;

/**
 * This class holds the information about the categories that the item can belong to
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
public class Category {
	private long categoryID;
	private String name;
	private long parent;
	private static final boolean immutableWarning = true;

	/**
	 * Create a category with all of the required data
	 * 
	 * @param categoryID
	 * @param name
	 * @param parent
	 */
	public Category(long categoryID, String name, long parent){
		this.categoryID = categoryID;
		this.name = name;
		this.parent = parent;
	}
	
	/**
	 * Create a category with just the categoryID
	 * 
	 * @param categoryID
	 */
	public void setCategoryID(long categoryID){
		if(Category.immutableWarning) Category.printWarning();
		this.categoryID = categoryID;
	}
	
	/**
	 * Return the categoryID number
	 * 
	 * @return int the category ID number
	 */
	public long getCategoryID(){
		return this.categoryID;
	}
	
	/**
	 * Set the name of the category
	 * 
	 * @param name
	 */
	public void setName(String name){
		if(Category.immutableWarning) Category.printWarning();
		this.name = name;
	}
	
	/**
	 * Returns the name of the category
	 * 
	 * @return String the name of the category
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Sets the parent categoryID of this category
	 * 
	 * @param parent
	 */
	public void setParent(long parent){
		if(Category.immutableWarning) Category.printWarning();
		this.parent = parent;
	}
	
	/**
	 * Returns the parent categoryID number of this category
	 * 
	 * @return int the categoryID number for this category's parent
	 */
	public long getParent(){
		return this.parent;
	}
	
	/**
	 * Prints immutable object warning
	 * Can be suppressed with immutableWarning=false;
	 * 
	 */
	private static void printWarning(){
		System.out.println("Category (printWarning): Please consider using this class as immutable. The 'set' methods may not be doing what you think. They only set values for this Java object and does not set them in the database. Talk to me if you want to change the design - Andy");
	}
}
