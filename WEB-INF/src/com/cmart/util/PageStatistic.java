package com.cmart.util;

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
public class PageStatistic {
	private int pageNo;
	private String pageName;
	private int maxBins;
	private int binWidth;
	private long paramHist[];
	private long dbHist[];
	private long procHist[];
	private long renderHist[];
	private long totalHist[];
	private long hits;
	private static boolean warnings = true;
	
	/**
	 * This create a page statistic object with the number of specified bins and at the specified number of ms per bin
	 * I.e. width = 5, max = 10: Can record page response times of up to 50ms. Page between 0-5 in bin 0, 5-10 in bin 1 etc
	 * 
	 * Page response times that are greater the max size of the histogram are placed in the last bin
	 * 
	 * If the maximum number of samples is reached then we set a flag to let the user know (although, this is BIG!!)
	 * 
	 * The total combined size of the stat's objects should only be 1MB
	 * 
	 * @param pageNo
	 * @param pageName
	 * @param maxBins
	 * @param binWidth
	 */
	public PageStatistic(int pageNo, String pageName, int binWidth, int maxBins){
		// Create the statistics objects
		this.pageNo = pageNo;
		this.pageName = pageName;
		if(maxBins>0)
			this.maxBins = maxBins;
		else{
			this.maxBins = 1;
			if(warnings) System.out.println("(PageStatistics) Error: The number of bins must be greater than zero");
		}
		
		if(binWidth>0)
			this.binWidth = binWidth;
		else{
			this.binWidth = 1;
			if(warnings) System.out.println("(PageStatistics) Error: The bin width must be greater than zero");
		}
		
		this.paramHist = new long[this.maxBins];
		this.dbHist = new long[this.maxBins];
		this.procHist = new long[this.maxBins];
		this.renderHist = new long[this.maxBins];
		this.totalHist = new long[this.maxBins];
		this.hits = 0;
		
		this.clear();
	}
	
	public synchronized void addReading(long paramTime, long dbTime, long procTime, long renderTime, long totalTime){
		// Set param time
		int index =  ((int)paramTime) / binWidth;
		if(index<0) index=0;
		if (index < maxBins)
			paramHist[index]++;
		else
			paramHist[maxBins - 1]++;
		
		// Set db time
		index = (int) dbTime / binWidth;
		if(index<0) index=0;
		if (index < maxBins)
			dbHist[index]++;
		else
			dbHist[maxBins - 1]++;
		
		// Set proc time
		index = (int) procTime / binWidth;
		if(index<0) index=0;
		if (index < maxBins)
			procHist[index]++;
		else
			procHist[maxBins - 1]++;
		
		// Set render time
		index = (int) renderTime / binWidth;
		if(index<0) index=0;
		if (index < maxBins)
			renderHist[index]++;
		else
			renderHist[maxBins - 1]++;
		
		// Set total time
		index = (int) totalTime / binWidth;
		if(index<0) index=0;
		if (index < maxBins)
			totalHist[index]++;
		else
			totalHist[maxBins - 1]++;
		
		this.hits++;
	}
	
	public void clear(){
		// Set the counters to zero
		for(int i=0; i<maxBins; i++){
			this.paramHist[i] = 0;
			this.dbHist[i] = 0;
			this.procHist[i] = 0;
			this.renderHist[i] = 0;
			this.totalHist[i] = 0;
			this.hits = 0;
		}
	}
	
	public String getPageName(){
		return pageName;
	}
	
	public long[] getParamHist(){
		return this.paramHist;
	}
	
	public long[] getDBHist(){
		return this.dbHist;
	}
	
	public long[] getProcHist(){
		return this.procHist;
	}
	
	public long[] getRenderHist(){
		return this.renderHist;
	}
	
	public long[] getTotalHist(){
		return this.totalHist;
	}
	
	public long getHits(){
		return this.hits;
	}
	
	public static void suppressWarnings(){
		PageStatistic.warnings = false;
	}
}
