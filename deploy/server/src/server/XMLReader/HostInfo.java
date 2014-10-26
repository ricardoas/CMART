package server.XMLReader;

import java.util.ArrayList;
import java.util.Hashtable;

import server.main.DeploymentController;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Represent information for each physical machine. The PM contains the information of all of the VMs
 * that will run on the PM
 * 
 * @author Ji Yang
 * @author Andy Turner
 */
public class HostInfo {
	/*
	 * Vars to hold the PM's info
	 */
	public String ip;
	public String userName;
	public String password;
	public String osType;
	
	/*
	 * Vars to hold the VM's info
	 */
	public ArrayList<VMInfo> vmInfo;
	public ArrayList<String> VMTypes;
	public Hashtable<String, VMImageSetting> vmTypeSettings;
	
	public ArrayList<VMConfig> vmConfigs;	// Store the vm type and if this vm is back up of not....
	public String baseFolder;			// The base folder of the VM images specified in the XML document
	
	/**
	 * Given a node in the XML file, understand everything and convert to a HostInfo
	 * 
	 * @param elementNode
	 */
	public HostInfo(Element elementNode) {
		/*
		 * Get all the PMs attributes
		 */
		this.ip = elementNode.getAttribute("IP");
		this.userName = elementNode.getElementsByTagName("UserName").item(0).getFirstChild().getNodeValue();
		this.password = elementNode.getElementsByTagName("Password").item(0).getFirstChild().getNodeValue();
		this.osType = elementNode.getElementsByTagName("OSType").item(0).getFirstChild().getNodeValue();
		DeploymentController.print("HostInfo", 4, "Got the host info for " + this.ip);
		
		/*
		 * Set the base folder
		 */
		if (elementNode.getElementsByTagName("BaseFolder").getLength() == 0) {
			this.baseFolder = "~/";
		}
		else
			this.baseFolder = elementNode.getElementsByTagName("BaseFolder").item(0).getFirstChild().getNodeValue();
		DeploymentController.print("HostInfo", 4, "Got basefolder: " + this.baseFolder);	
		
		/*
		 * Get all of the VM's info for VMs running on this host
		 */
		this.vmInfo = new ArrayList<VMInfo>();
		this.VMTypes = new ArrayList<String>();
		this.vmTypeSettings = new Hashtable<String, VMImageSetting>();
		this.vmConfigs = new ArrayList<VMConfig>();
		
		NodeList vmList = elementNode.getElementsByTagName("VMType");	
		for (int i=0; i<vmList.getLength(); i++) {
			Element vmConfig = (Element)vmList.item(i);
			
			VMConfig config = new VMConfig(vmConfig);
			vmConfigs.add(config);
			
			DeploymentController.print("HostInfo", 4, "Got the VM " + config.toString());
		}	
	}
	
	/*
	 * Return the string version of this PM
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "I am client " + userName+"@" + ip + "; OSType: " + osType +"; VMNumber: " + vmConfigs.size();
	}
}
