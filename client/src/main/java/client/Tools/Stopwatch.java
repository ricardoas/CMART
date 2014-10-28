package client.Tools;

/**
 * Emulates a stopwatch
 * @author Andrew Fox
 *
 */
public class Stopwatch {
	private long startTime;
	private long totalTime=0;
	private boolean running=false;	// if the stopwatch is running

	/**
	 * Declares a stopwatch and automatically starts the timer
	 */
	public Stopwatch(){
		this.start();
	}

	/**
	 * Pauses the stopwatch
	 */
	public void pause(){
		totalTime+=System.currentTimeMillis()-startTime;
		running=false;
	}

	/**
	 * Starts the stopwatch
	 */
	public void start(){
		if(running==false){
			startTime=System.currentTimeMillis();
			running=true;
		}
	}

	/**
	 * Finally stops the stopwatch and returns the time measured
	 * @return time measured by stopwatch
	 */
	public long stop(){
		if(running==true){
			this.pause();	// if the stopwatch is still running then stop the timer
		}
		return totalTime;
	}

}
