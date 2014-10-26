package client.deployment;

/**
 * The class to represent VM info. It hold the info about VMs such as what part of
 * the application they are running and their IP address
 * 
 * @author Ji Yang
 * @author Andy Turner
 *
 */
public class VMInfo {
	public final static int COLD = 1;
	public final static int HOT = 2;
	
	public String name;
	public String type;
	public String imagePath;
	public String xmlPath;
	public String ip;
	
	public String state;
	public int currentState = 0; // ANDY: changing state to an int. 0=notRunning
	public int backup;			// 0 - not a backup     // 1 - cold backup    // 2 hot-backup
	public int ramSize = 1024;	// The default ram size....
	
	/**
	 * Creates the VM info
	 * @param name the name of the vm
	 * @param type the type of VM e.g mySQL, loadbalancer...
	 * @param backup whether the VM is currently a backup VM
	 */
	public VMInfo(String name, String type, String backup) {
		this.name = name;
		this.type = type;
		this.state = "notRunning";
		this.currentState = 0;
		this.backup = Integer.parseInt(backup);
	}
	
	/**
	 * Prints the VM info to a string
	 */
	public String toString() {
		return name + ";" + type + ";" + imagePath + ";" + xmlPath + "; " + ip + "; backup? " + this.backup;
	}
}
