package client.Tools;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import client.clientMain.ClientGenerator;

public class Stats extends Thread {
	private long refreshPeriod = 10000; // period between measurements
	private long maxValue; // maximum value of histogram
	private long binSize; // bin size of histogram
	private Histogram activeHistogram; // the active histogram
	private ClientGenerator cg; // client generator which created these stats
	private ScheduledExecutorService executor;

	public Stats(long maxValue, long binSize, long refreshPeriod, ClientGenerator cg) {
		this.maxValue = maxValue;
		this.binSize = binSize;
		this.refreshPeriod = refreshPeriod;
		this.cg = cg;
		this.activeHistogram = new Histogram(maxValue, binSize);
		this.executor = Executors.newSingleThreadScheduledExecutor();
	}

	@Override
	public void run() {
		if (this.refreshPeriod > 0) { // schedules every refresh period a new
										// histogram to be created // FIXME
										// REFAC
			this.executor.scheduleAtFixedRate(new CollectStats(this, this.cg), this.refreshPeriod, this.refreshPeriod, TimeUnit.MILLISECONDS);
		} else {
			this.executor.schedule(new CollectStats(this, this.cg), 0, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Gets the active histogram
	 *
	 * @return
	 */
	public synchronized Histogram getActiveHistogram() {
		return this.activeHistogram;
	}

	/**
	 * clears the active histogram and creates a blank one with same original
	 * settings
	 */
	public synchronized void clearActiveHistogram() {
		this.activeHistogram = new Histogram(this.maxValue, this.binSize);
	}

	/**
	 * Gets the refresh period of the histogram
	 *
	 * @return
	 */
	public long getRefreshPeriod() {
		return this.refreshPeriod;
	}

	/**
	 * Exits the stats collecting system
	 */
	public void exitStats() {
		this.executor.shutdownNow();
	}

}
