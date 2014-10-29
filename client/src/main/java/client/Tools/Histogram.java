package client.Tools;

/**
 * Histogram, defined by the maximum value of the graph and the size of each bin
 *
 * @author Andrew Fox
 *
 */
public class Histogram {
	private long hist[];
	private long binSize;
	private long numEntries;
	private int numBins;

	/**
	 * Defines the histogram by maxValue and size of each bin
	 *
	 * @param maxValue
	 * @param binSize
	 */
	public Histogram(long maxValue, long binSize) {
		this.numBins = (int) (maxValue / binSize) + 1;
		this.hist = new long[this.numBins];
		this.binSize = binSize;
		this.numEntries = 0;
	}

	/**
	 * Generates a histogram by replicating hist's parameters
	 *
	 * @param hist
	 * @param binSize
	 * @param numEntries
	 */
	public Histogram(long hist[], long binSize, long numEntries) {
		this.hist = hist;
		this.binSize = binSize;
		this.numEntries = numEntries;
	}

	/**
	 * Adds an entry to the histogram
	 *
	 * @param num
	 *            - The number of the entry to be incremented by one
	 */
	public void add(long num) {
		int bin = (int) (num / this.binSize);
		if (bin >= this.hist.length) {
			bin = this.hist.length - 1;
		}
		this.hist[bin] += 1;
		this.numEntries++;
	}

	/**
	 * Returns the nth percentile histogram value
	 *
	 * @param percentile
	 *            - n in decimal form (eg. 90th percentile = 0.9)
	 */
	public long getPercentile(double percentile) {
		long valuesChecked = 0;
		int bin = 0;
		while (valuesChecked < (percentile * this.numEntries)) {
			valuesChecked += this.hist[bin];
			bin++;
		}
		return (bin + 1) * this.binSize;
	}

	/**
	 * Clears all values from the histogram
	 */
	public void clear() {
		this.numEntries = 0;
		for (int i = 0; i < this.hist.length; i++) {
			this.hist[i] = 0;
		}
	}

	/**
	 * Gets the number of entries in the histogram
	 *
	 * @return
	 */
	public long getNumEntries() {
		return this.numEntries;
	}

	/**
	 * Gets the size of each bin in the histogram
	 *
	 * @return
	 */
	public long getBinSize() {
		return this.binSize;
	}

	/**
	 * Gets the number of elements in a specified histogram bin
	 *
	 * @param i
	 *            - bin to check
	 * @return
	 */
	public long getHistElem(int i) {
		return this.hist[i];
	}

	public int getNumBins() {
		return this.numBins;
	}

}
