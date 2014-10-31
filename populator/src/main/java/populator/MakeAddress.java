package populator;

/**
 * This class create an address for the user
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


import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MakeAddress {
	private static RandomSelector<String, String> streetsRS = null;
	private static RandomSelector<String, String> townsRS = null;
	private static Distribution streetsDist;
	private static Distribution townsDist;
	
	private static Boolean localPopulated = false;
	
	/**
	 * Get an Address for a user
	 * @param userID
	 * @param isDefault
	 * @return
	 */
	public static Address getAddress(long userID, boolean isDefault){
		if(!localPopulated) init();
		return new Address(-1, userID, getStreet(), getTown(), getZip(), PopulateStates.getRandomState(), isDefault);
	}
	
	/**
	 * load the data about streets, towns, etc
	 */
	private static void init(){
		if(!localPopulated){
			synchronized(localPopulated){
				if(!localPopulated){
					try {
						streetsRS = new RandomSelector<String, String>(new FileInputStream(CreateAll.STREETS_FILE), String.class);
						
						streetsDist = new Distribution(CreateAll.STREETNAME_DIST_TYPE, CreateAll.STREETNAME_MIN, CreateAll.STREETNAME_MAX, CreateAll.STREETNAME_DIST_MEAN, CreateAll.STREETNAME_DIST_SD, CreateAll.STREETNAME_ALPHA, CreateAll.STREETNAME_LAMBDA);
						
						townsRS = new RandomSelector<String, String>(new FileInputStream(CreateAll.TOWNS_FILE), String.class);	
						
						townsDist = new Distribution(CreateAll.TOWNNAME_DIST_TYPE, CreateAll.TOWNNAME_MIN, CreateAll.TOWNNAME_MAX, CreateAll.TOWNNAME_DIST_MEAN, CreateAll.TOWNNAME_DIST_SD, CreateAll.TOWNNAME_ALPHA, CreateAll.TOWNNAME_LAMBDA);
						
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					localPopulated = true;
				}
			}
		}
	}
	
	/**
	 * Get the name of a street
	 * 
	 * @return
	 */
	private static String getStreet(){
		int length = (int) streetsDist.getNext();
		StringBuffer strBuf = new StringBuffer(50);
		
		if(length>0){
			strBuf.append((int)(Math.random()*1000));
			strBuf.append(" ");
			if(CreateAll.STREETS_WORD_UNIFORM)
				strBuf.append(streetsRS.getRandomUniformKey());
			else
				strBuf.append(streetsRS.getRandomKey());
		}
		
		for(int i=0; i<length-1; i++){
			strBuf.append(" ");
			if(CreateAll.STREETS_WORD_UNIFORM)
				strBuf.append(streetsRS.getRandomUniformKey());
			else
				strBuf.append(streetsRS.getRandomKey());
		}
		
		return strBuf.toString();
	}
	
	/**
	 * Return the town name
	 * 
	 * @return
	 */
	private static String getTown(){
		int length = (int) townsDist.getNext();
		StringBuffer strBuf = new StringBuffer(50);
		
		if(length>0)
			if(CreateAll.TOWNS_WORD_UNIFORM)
				strBuf.append(townsRS.getRandomUniformKey());
			else
				strBuf.append(townsRS.getRandomKey());
		
		for(int i=0; i<length-1; i++){
			strBuf.append(" ");
			if(CreateAll.TOWNS_WORD_UNIFORM)
				strBuf.append(townsRS.getRandomUniformKey());
			else
				strBuf.append(townsRS.getRandomKey());
		}
		
		return strBuf.toString();
	}
	
	/**
	 * Return the size code
	 * 
	 * @return
	 */
	private static String getZip(){
		StringBuffer strBuf = new StringBuffer();
		
		int zip = (int)(Math.random()*99999);
		if(zip<9999) zip+=10000;
		
		strBuf.append(zip);
		
		if(Math.random() < CreateAll.FULL_ZIP_FRACTION){
			strBuf.append("-");
			strBuf.append((int)(Math.random()*9999));
		}
		
		return strBuf.toString();
	}
}
