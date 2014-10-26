package server.XMLReader;

/**
 * The class to represent each VM info we deployed into the client physical machines
 * 
 * @author Ji Yang
 * @author Andy Turner
 *
 */
public class VMInfo {
	/*
	 * Vars to hold VM info
	 */
	public String name;
	public String type;
	public String ip;
	public String hostIP;
	public String state;
	public int backup;	// 0 - not a backup     // 1 - cold backup    // 2 hot-backup
	public int ramsize = 1024;
	
	/**
	 * Create a VM with only the basic info
	 * @param name
	 * @param type
	 * @param backup
	 */
	public VMInfo(String name, String type, int backup) {
		this.name = name;
		this.type = type;
		this.ip = null;	
		this.backup = backup;
	}
	
	/**
	 * Create a VM after we have all of its info
	 * @param info
	 */
	public VMInfo (String[] info) {
		this.name = info[0];
		this.type = info[1];
		this.ip = info[2];
		this.hostIP = info[3];
		this.state = info[4];
		this.backup = Integer.parseInt(info[5]);
	}

	/*
	 * Print VM to string
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name + ";" + type + "; IP = " + ip + "; backup? " + this.backup;
	}
}
