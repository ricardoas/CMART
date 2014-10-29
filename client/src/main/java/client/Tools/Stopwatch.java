package client.Tools;

/**
 * Emulates a stopwatch
 *
 * @author Andrew Fox
 *
 */
public class Stopwatch {
	private long startTime;
	private long totalTime = 0;
	private boolean running; // if the stopwatch is running

	/**
	 * Declares a stopwatch and automatically starts the timer
	 */
	public Stopwatch() {
		this.start();
	}

	/**
	 * Pauses the stopwatch
	 */
	public void pause() {
		this.totalTime += System.currentTimeMillis() - this.startTime;
		this.running = false;
	}

	/**
	 * Starts the stopwatch
	 */
	public void start() {
		if (!this.running) {
			this.startTime = System.currentTimeMillis();
			this.running = true;
		}
	}

	/**
	 * Finally stops the stopwatch and returns the time measured
	 *
	 * @return time measured by stopwatch
	 */
	public long stop() {
		if (this.running) {
			this.pause(); // if the stopwatch is still running then stop the
			// timer
		}
		return this.totalTime;
	}

}
