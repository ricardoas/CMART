package com.cmart.Data;

/**
 * This class contains errors that the pages could throw. We have our own errors, but I also made it so that
 * you could get any general exception to print for the user. Can be useful to throw up DB errors, although
 * ideally they should never occur
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
public class Error extends Exception{
	private static final long serialVersionUID = 1817041456628458743L;
	private int errorNumber;
	private String name;
	private String description;
	private String full;
	
	/**
	 * Create a new error from an exception
	 * 
	 * @param description the description of the error
	 * @param e the exception that caused the error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public Error(String description, Exception e){
		super(e);
		
		if(description == null) description = "(no description - null)";
		String eText = "(no exception - null)";
		if(e != null) eText = e.toString();
		
		this.full = description + "\n" + eText;
	}
	
	/**
	 * Creates a new error with the variables passed
	 * 
	 * @param errorNumber the error number that we have defined
	 * @param name the name of the error
	 * @param description the more verbose description of the error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public Error(int errorNumber, String name, String description){
		this.errorNumber = errorNumber;
		this.name = name;
		this.description = description;
		this.full = errorNumber + " " + name + " " + description;
	}
	
	/**
	 * Return the error as a String
	 * 
	 * @return String the full error message
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String toString(){
		return this.full;
	}
	
	/**
	 * Returns the error number of this message. Good for checking if to output pretty messages to the user
	 * 
	 * @return int the error number of this error
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public int getErrorNumber(){
		return this.errorNumber;
	}
	
	/**
	 * Returns the description of the error
	 * 
	 * @return String the error description
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getDescription(){
		return this.description;
	}
	
	/**
	 * Returns the name of the error
	 * 
	 * @return String the error name
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getName(){
		return this.name;
	}
}
