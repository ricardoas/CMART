package server.XMLReader;

import org.w3c.dom.Element;

import server.main.DeploymentController;

/**
 * This class holds the info for a VM that is running on a PM. E.g. its type and RAM size
 * This is the info specified in the XML file, from it we will create VMInfo objects
 * 
 * @author Ji Yang
 * @author Andy Turner
 *
 */
public class VMConfig {
	/*
	 * Vars to hold the VM's info
	 */
	public String vmType;
	public int backup = 0;			// 0 - not a backup     // 1 - cold backup    // 2 hot-backup
	public int ramsize = 1024;
	public static int clientNum = 0;
	
	/**
	 * Create the VM from an XML element
	 * 
	 * @param elementNode
	 */
	public VMConfig(Element elementNode) {
		// Get the VM's info
		this.vmType = elementNode.getAttribute("Type");
		String Backup = elementNode.getAttribute("Backup");
		String ramsize = elementNode.getAttribute("RAMSIZE");
		
		// Get if it VM is a backup
		if (Backup.equals("Hot")) {
			backup = 2;
		}
		else if (Backup.equals("Cold"))
			backup = 1;
		else
			backup = 0;
		
		if (vmType.equals("Client"))
			clientNum++;
		
		// Deal with the ram size
		try {
			int size = Integer.parseInt(ramsize);
			this.ramsize = size;
		}
		catch(Exception e) {
			this.ramsize = 1024;
			DeploymentController.print("HostInfo:VMConfig", 1, "RAM size not present. Defaulting to 1024");	
		}
		
		DeploymentController.print("HostInfo:VMConfig", 1, this.toString());
	}
	
	

	public String toString() {
		return "Type: " + this.vmType + "; backup: " + this.backup +"; ram: "+ this.ramsize;
	}
}
