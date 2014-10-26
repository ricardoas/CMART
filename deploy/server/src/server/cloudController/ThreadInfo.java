package server.cloudController;
import server.XMLReader.HostInfo;

/**
 * The class to represent the healthy info for each thread
 * 
 * @author Ji Yang
 * @author Andy Turner
 *
 */
public class ThreadInfo {
	public enum ThreadStatus {
		NOTSTARTED, PROCESSING, ERROR, FINISH, CANNOT_ESTABLISH
	}
	
	private ThreadStatus status;
	private String errMsg;
	private int ip;
	public HostInfo info;
	
	/**
	 * Create the tread info
	 */
	public ThreadInfo() {
		this.status = ThreadInfo.ThreadStatus.NOTSTARTED;
	}
	
	/**
	 * Sets the threads status
	 * @param status
	 */
	public synchronized void setStatus(ThreadStatus status) {
		this.status = status;
	}
	
	/**
	 * Gets the threads status
	 * @return
	 */
	public synchronized ThreadStatus getStatus() {
		return status;
	}
	
	/**
	 * Sets the error message for this thread info
	 * @param errMsg
	 */
	public synchronized void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	
	/**
	 * Gets the error message of this thread info
	 * @return
	 */
	public synchronized String getErrMsg() {
		return errMsg;
	}
	
	/**
	 * Gets the OS type the thread is running on
	 * @return
	 */
	public synchronized String getOSType() {
		return info.osType;
	}
}
