package client.Tools;

import java.util.Timer;

import client.clientMain.ClientGenerator;


public class Stats extends Thread{
	private long refreshPeriod=10000;		// period between measurements
	private long maxValue;					// maximum value of histogram
	private long binSize;					// bin size of histogram
	private Histogram activeHistogram;		// the active histogram
	private ClientGenerator cg;				// client generator which created these stats
	Timer timer=new Timer();

	public Stats(long maxValue, long binSize, long refreshPeriod,ClientGenerator cg){
		this.maxValue=maxValue;
		this.binSize=binSize;
		this.refreshPeriod=refreshPeriod;
		this.cg=cg;
		activeHistogram=new Histogram(maxValue,binSize);
	}

	public void run(){
		if (refreshPeriod>0)	// schedules every refresh period a new histogram to be created
			timer.scheduleAtFixedRate(new CollectStats(this,cg), refreshPeriod, refreshPeriod);
		else
			timer.schedule(new CollectStats(this,cg), 0);
	}


	/**
	 * Gets the active histogram
	 * @return
	 */
	public synchronized Histogram getActiveHistogram(){
		return this.activeHistogram;
	}

	/**
	 * clears the active histogram and creates a blank one with same original settings
	 */
	public synchronized void clearActiveHistogram(){
		activeHistogram=new Histogram(maxValue,binSize);
	}
	/**
	 * Gets the refresh period of the histogram
	 * @return
	 */
	public long getRefreshPeriod(){
		return this.refreshPeriod;
	}
	
	/**
	 * Exits the stats collecting system
	 */
	public void exitStats(){
		timer.cancel();
	}


}
