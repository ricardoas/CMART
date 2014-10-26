package com.cmart.util;

/**
 * This class holds the information to link to an image (not the image data itself)
 * 
 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
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
public class Image implements Comparable{
	// Variables to hold the image data
	private int position;
	private String url;
	private String description;
	
	/**
	 * Create an Image with all of the required information
	 * @param position
	 * @param url
	 * @param description
	 */
	public Image(int position, String url, String description){
		this.position = position;
		this.url = url;
		this.description = description;
	}
	
	/**
	 * Returns the position of the image. The position is the order they will be displayed
	 * 
	 * @return int the position of the image
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public int getPosition(){
		return this.position;
	}
	
	/**
	 * Returns the URL needed to load the image
	 * 
	 * @return String the URL needed to load the image
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getUrl(){
		return this.url;
	}
	
	/**
	 * Returns the alt description of the image
	 * 
	 * @return String the alt description of the image
	 * @author Andy (andrewtu@cmu.edu, turner.andy@gmail.com)
	 */
	public String getDescription(){
		return this.description;
	}
	
	/**
	 * Returns the image as a JSON string
	 * @return
	 */
	public String toJSON(){
		StringBuffer buf = new StringBuffer();
		buf.append("{\"url\":\"");
		buf.append(this.url);
		buf.append("\",\"description\":\"");
		buf.append(this.description.replaceAll("\"", "'"));
		buf.append("\",\"position\":\"");
		buf.append(this.position);
		buf.append("\"}");
		return buf.toString();
	}
	
	public int compare(Image o1, Image o2){
		return o1.getPosition()-o2.getPosition();
	}

	@Override
	public int compareTo(Object arg0) {
		if(arg0 instanceof Image){
			Image obj = (Image)arg0;
			return this.getPosition()- obj.getPosition();
		}
		else
			return 0;
	}
}
