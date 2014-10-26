package server.cloudController;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;

import server.XMLReader.HostInfo;
import server.XMLReader.VMConfig;
import server.XMLReader.VMImageSetting;
import server.XMLReader.VMInfo;

import server.cloudController.ThreadInfo.ThreadStatus;
import server.main.DeploymentController;

/**
 * A thread to deal with the TCP socket communication from each client with the server
 * 
 * @author Ji Yang
 * @author Andy Turner
 *
 */
public class socketServerThread implements Runnable {
	public Socket socket;
	int clientID;
	HostInfo machineInfo = null;
	final static int MAX_RETRIES = 100;
	
	public static int currentRunningVMNumber = 0;
	
	/**
	 * To find out the machine info given in client socket.
	 * 
	 * @param socket
	 * @param clientID
	 * @throws Exception
	 */
	public socketServerThread(Socket socket, int clientID) throws Exception {
		this.socket = socket;
		this.clientID = clientID;
		socket.setSoTimeout(30 * 60 * 1000);	// Set the time out to 30 min
		DeploymentController.print("socketServerThread", 4, "I am client No." + clientID + " I connected from " + socket.getInetAddress().toString());
		
		// Get the VMinfo for this host from the server using the IP address
		for (HostInfo info : ControllerServer.clientList) {
			if (socket.getInetAddress().toString().endsWith(info.ip)) {
				DeploymentController.print("socketServerThread", 4, "Found MachineInfo for client " + clientID + "; " + info.toString());
				machineInfo = info;
				ControllerServer.clientStatus[clientID].info = machineInfo;
				
				break;
			}
		}
		
		// If there is no info, then we won't know what to do
		if (machineInfo == null){
			DeploymentController.print("socketServerThread", 0, "The client has no info! we don't know what to do for client no: " + clientID);
			throw new Exception("Cannot find machine Info specified in the XML for client " + socket.getInetAddress().toString());
		}
		
		/*
		 * Loop through all of the VMs that are going to be located on this host and get their info
		 */
		DeploymentController.print("socketServerThread", 4, "Client " + clientID + " has " + machineInfo.vmConfigs.size() + " to configure");
		for (VMConfig config : machineInfo.vmConfigs) {
			// Get the config and name for this server
			String vmType = config.vmType;
			String vmName = VMNameAssigner.getVMName(vmType);
			VMInfo info = new VMInfo(vmName, vmType, config.backup);
			info.ramsize = config.ramsize;
			
			DeploymentController.print("socketServerThread", 4, "Client " + clientID + " got VM: " +info.toString());
			
			machineInfo.vmInfo.add(info);
		}
		
		// Set this thread to be 'processing' since we did not crash out and we have things to do
		ControllerServer.clientStatus[clientID].setStatus(ThreadStatus.PROCESSING);
	}
	
	// To send/recieve from the client
	private BufferedReader in;
	private PrintWriter out;
	
	/**
	 * The main routine for the socket thread
	 * 
	 * Basically just deals with the communication to/from the client
	 * 1. make in/out streams
	 * 2. start communications
	 * 3. run commands until finished or error
	 */
	public void run() {
		try {
			/*
			 * Make the input and output streams
			 */
			String input, output;
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			DeploymentController.print("socketServerThread: run", 4, "Stating communications with client: " + clientID);
			
			/*
			 * Welcome the client to start things going
			 */
			out.println("Client No. " + clientID);
			
			/*
			 * Read back what the client says, then pass it the info it needs to deploy it's VMs
			 */
			while ((input = in.readLine()) != null) {
				DeploymentController.print("socketServerThread: run", 5, "Client: " + clientID + " send command: " + input);
				
				// Check for the client ending the session
				if (input.startsWith("End Connection")) {
					DeploymentController.print("socketServerThread: run", 4, "Recieved the end command from client: " + clientID + " : " + input);
			
					break;
				}
				// Check for an error with the client
				else if (input.equals("Error so end connection!")) {
					input = in.readLine();
					DeploymentController.print("socketServerThread: run", 0, "Recieved error from client: " + clientID + " : " + input);
					
					in.close();
					out.close();
					socket.close();
					throw new IOException(input);
				}
				// Send the command to be processed
				else out.println(respond(input));
			}
			
			/*
			 * Clean up
			 */
			in.close();
			out.close();
			socket.close();
			DeploymentController.print("socketServerThread: run", 4, "Ending communications with client: " + clientID);
			ControllerServer.clientStatus[clientID].setStatus(ThreadStatus.FINISH);
		} catch (Exception e) {
			// If there is error!
			DeploymentController.print("socketServerThread: run", 0, "Error with client: " + clientID + ". The error was: " + e.toString());
			
			ControllerServer.clientStatus[clientID].setStatus(ThreadStatus.ERROR);
			ControllerServer.clientStatus[clientID].setErrMsg(e.toString());
		}
	}

	/**
	 * Given a message from client, how should server respond to it?
	 * 
	 * @throws Exception 
	 */
	private String respond(String input) throws Exception {
		// Return the clients OS type
		if (input.equals("OS Type?")) {
			return machineInfo.osType;
		}
		
		// Return the gateway
		if (input.equals("Gateway?")) {
			return DeploymentController.serverGateway;
		}
		
		// Return how many VMs it is going to configure
		if(input.equals("How many VMs?")) {
			return String.valueOf(machineInfo.vmInfo.size());
		}
		
		// Return if there is a new cmart config file
		if(input.equals("New cmartconfig?")){
			if(DeploymentController.cmartNewConfig!=null)
				return DeploymentController.cmartNewConfig;
			else return "no";
		}
		
		// Return the VMInfo for a VM
		if (input.startsWith("VMInfo for ")) {
			String[] info = input.split(";");
			int vmNumber = Integer.parseInt(info[info.length-1]);
			
			DeploymentController.print("socketServerThread: respond", 5, "Returning VMInfo for VM : " + vmNumber);		
			
			return machineInfo.vmInfo.get(vmNumber).name + ";" + machineInfo.vmInfo.get(vmNumber).type + ";" + machineInfo.vmInfo.get(vmNumber).backup;
		}
		
		// Return the disk image path for a base VM
		if (input.startsWith("VM Image Path for ;" )) {
			String[] info = input.split(";");
			
			VMImageSetting setting = machineInfo.vmTypeSettings.get(info[info.length-1]);
			return setting.imagePath + ";" + setting.xmlPath;
		}
		
		// Return the base directory for disk images/config data etc
		if (input.equals("Base directory?")) {
			return machineInfo.baseFolder;
		}
		
		// Find an IP address for a VM
		//	TODO: it would be nice if we could group VM types IP addresses by subnet 
		if (input.startsWith("A IP that I can use;")) {
			String[] info = input.split(";");
			
			String vmName = info[info.length-1];
		
			// Get the VM we are giving an address to
			VMInfo vmInfo = null;
			for (VMInfo eachInfo : machineInfo.vmInfo) {
				if (eachInfo.name.equals(vmName)) {
					vmInfo = eachInfo;
					break;
				}
			}
			
			// The client wanted an IP address for a VM that doesn't exist
			if (vmInfo == null) {
				DeploymentController.print("socketServerThread: respond", 0, "The client wanted an IP address for a VM that doesn't exist: " + vmName);				
				throw new Exception("Cannot find vm info for " + vmName);
			}
			
			// We found the VM, get the ip, so set it and return it
			String ip = findNextIP();
			vmInfo.ip = ip;
			System.out.println("The assigned VM ip Info: " + vmInfo.toString());
			
			return ip;
		}
		
		// The client is reporting the states of the VMs
		if (input.startsWith("Going to report all VM state!")) {
			String[] info = input.split(";");
			int totalVMNumber = Integer.parseInt(info[1]);
			for (int i=0; i< totalVMNumber; i++) {
				addToFinalVMResult(in.readLine());
				addCurrentRunningVMNumber();
			}
			
			DeploymentController.print("socketServerThread: respond", 5,"Currently we have " + currentRunningVMNumber +" VMs running out of " + ControllerServer.totalVMNumber);				

			return "Done!!!";
		}
		
		// Tell the client if all of the VMs are running
		if (input.startsWith("Is all VMs running")) {
			DeploymentController.print("socketServerThread: respond", 5,"Currently we have " + currentRunningVMNumber +" VMs running out of " + ControllerServer.totalVMNumber);				

			if (getCurrentRunningVMNumber() < ControllerServer.totalVMNumber) {
				return "False";
			}
			else
				return "True";
		}
		
		// Get all of the app server IPs, used to set the load balancer
		if (input.startsWith("Give me all avaiable appServer IP!")) {
			// Going to return all the IPs.....
			StringBuffer iplist = new StringBuffer("");
			for (VMInfo info : ControllerServer.finalVMStates) {
				if (info.type.equals("Application") && info.backup == 0) {
					iplist.append(info.ip+";");
				}
			}
			
			// At last, delete a ; if the iplist is not empty
			if (iplist.length() > 0)
			
			iplist.deleteCharAt(iplist.length()-1);
			return iplist.toString();
		}
		
		// Get all of the mysql server IPs, used to set the sql bal
		if (input.startsWith("Give me all avaiable sqlServer IP!")) {
			// Going to return all the IPs.....
			StringBuffer iplist = new StringBuffer("");
			for (VMInfo info : ControllerServer.finalVMStates) {
				if (info.type.equals("SQL")) {
					iplist.append(info.ip+";");
				}
			}
			
			// At last, delete a ; if the iplist is not empty
			if (iplist.length() > 0)
				iplist.deleteCharAt(iplist.length()-1);
			
			return iplist.toString();
		}
		
		// Get all of the cassandra server IPs, used to set the cassandra bal
		if (input.startsWith("Give me all avaiable casServer IP!")) {
			// Going to return all the IPs.....
			StringBuffer iplist = new StringBuffer("");
			for (VMInfo info : ControllerServer.finalVMStates) {
				if (info.type.equals("CasMaster")) {
					iplist.append(info.ip+";");
				}
			}
			
			// At last, delete a ; if the iplist is not empty
			if (iplist.length() > 0)
				iplist.deleteCharAt(iplist.length()-1);
			
			return iplist.toString();
		}
		
		// Get all of the solr server IPs, used to set the solr bal
		if (input.startsWith("Give me all avaiable solrServer IP!")) {
			// Going to return all the IPs.....
			StringBuffer iplist = new StringBuffer("");
			for (VMInfo info : ControllerServer.finalVMStates) {
				if (info.type.equals("Solr")) {
					iplist.append(info.ip+";");
				}
			}
			
			// At last, delete a ; if the iplist is not empty
			if (iplist.length() > 0)
				iplist.deleteCharAt(iplist.length()-1);
			
			return iplist.toString();
		}
		
		// Get the IP address of the image server for app image saving, to map NFS to GridFS to
		if (input.startsWith("Give me first avaiable image IP!")) {
			// Return the first image IP I found
			for (VMInfo info : ControllerServer.finalVMStates) {
				if (info.type.equals("Image")) {
					return info.ip;
				}
			}
			
			return "";
		}
		
		// Get the IP address of the MySQL server for app DB queries
		if (input.startsWith("Give me first avaiable SQL IP!")) {
			// Return the first image IP I found
			for (VMInfo info : ControllerServer.finalVMStates) {
				if (info.type.equals("SQL")) {
					return info.ip;
				}
			}
			
			return null;
		}
		
		// Get the IP address of Cassandra for app DB queries
		if (input.startsWith("Give me first avaiable cas master IP!")) {
			// Return the first image IP I found
			for (VMInfo info : ControllerServer.finalVMStates) {
				if (info.type.equals("CasMaster")) {
					return info.ip;
				}
			}
			
			return null;
		}

		// Get the IP address of Solr server for app searching
		if (input.startsWith("Give me first avaiable solr IP!")) {
			// Return the first image IP I found
			for (VMInfo info : ControllerServer.finalVMStates) {
				if (info.type.equals("Solr")) {
					return info.ip;
				}
			}
			
			return null;
		}
		
		// Get the IP address of the load balancer for client requests
		if (input.startsWith("Give me first avaiable LoadBalancer IP!")) {
			for (VMInfo info : ControllerServer.finalVMStates) {
				if (info.type.equals("LoadBalancer")) {
					return info.ip;
				}
			}
			
			return null;
		}
		
		//TODO: change Ji's copy and pasting below
		// Return any addition settings for the app server that are set in the XML file
		if (input.startsWith("Give me extra settings for app")) {
			StringBuffer result = new StringBuffer();
			
			if (ControllerServer.appExtraConfig.size() == 0)
				return "";
			
			Iterator<String> itr = ControllerServer.appExtraConfig.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				String value =  ControllerServer.appExtraConfig.get(key);
				
				result.append(key + ";" + value + ";");
			}
			
			// Get rid of final ";"
			result.deleteCharAt(result.length()-1);
			
			return result.toString();
		}
		
		// Return any additional settings for the SQL server
		if (input.startsWith("Give me extra settings for sql")) {
			StringBuffer result = new StringBuffer();
			
			if (ControllerServer.sqlExtraConfig.size() == 0)
				return "";
			
			Iterator<String> itr = ControllerServer.sqlExtraConfig.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				String value =  ControllerServer.sqlExtraConfig.get(key);
				
				result.append(key + ";" + value + ";");
			}
			
			// Get rid of final ";"
			result.deleteCharAt(result.length()-1);
			
			return result.toString();
		}
		
		// Return any additional settings for the client generator
		if (input.startsWith("Give me extra settings for client")) {
			StringBuffer result = new StringBuffer();
			
			if (ControllerServer.clientExtraConfig.size() == 0)
				return "";
			
			Iterator<String> itr = ControllerServer.clientExtraConfig.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				String value =  ControllerServer.clientExtraConfig.get(key);
				
				result.append(key + ";" + value + ";");
			}
			
			// Get rid of final ";"
			result.deleteCharAt(result.length()-1);
			
			return result.toString();
		}
		
		// Return the RAM size for a VM
		if (input.startsWith("SQL ram size for ;")) {
			String[] val = input.split(";");
			String vmName = val[1];
			
			for (VMInfo info : machineInfo.vmInfo) {
				if (info.name.equals(vmName))
					return String.valueOf(info.ramsize);
			}
			
			throw new Exception("Cannot find vm info for " + vmName);
		}
		return null;
	}
	
	/**
	 * Increment the number of VMs running on a host
	 */
	private static synchronized void addCurrentRunningVMNumber() {
		currentRunningVMNumber++;
	}
	
	/**
	 * Return the current number of VMs running on a host
	 * 
	 * @return
	 */
	private static synchronized int getCurrentRunningVMNumber() {
		return currentRunningVMNumber;
	}
	
	/**
	 * Parse the string and all to the final list;
	 * 
	 * @param readLine
	 */
	private static synchronized void addToFinalVMResult(String vm) throws Exception {
		String[] info = vm.split(";");
		
		ControllerServer.finalVMStates.add(new VMInfo(info));
	}

	/**
	 * Find the next available IP!
	 * 
	 * @return
	 * @throws Exception
	 */
	public static synchronized String findNextIP() throws Exception {
		try {		
			int retries = 0;
			String returnAddress = null;
			while ((++retries) < MAX_RETRIES) {
				if (!pingOK(ControllerServer.beginIP)) {
					DeploymentController.print("socketServerThread: findNextIP", 4, "Returning IP address: " + ControllerServer.beginIP);				

					returnAddress = ControllerServer.beginIP;
					ControllerServer.beginIP = increament(ControllerServer.beginIP);
					return returnAddress;
				}
				else {
					DeploymentController.print("socketServerThread: findNextIP", 5, "IP already taken. Incrementing and trying next IP: " + ControllerServer.beginIP);				

					ControllerServer.beginIP = increament(ControllerServer.beginIP);
				}
			}
			
			throw new Exception();
		} catch (Exception e) {
			DeploymentController.print("socketServerThread: findNextIP", 0, "Cannot find a next valid IP! - Given the begin IP is " + ControllerServer.beginIP);				

			throw new Exception("Cannot find a next valid IP! - Given the begin IP is " + ControllerServer.beginIP);
		}
	}

	/**
	 * Increment the IP address
	 * 
	 * @param beginIP
	 * @return
	 */
	private static String increament(String ip) throws Exception {
		try {
		
		String[] ips = ip.split("\\.");
	
		int ip0 = Integer.parseInt(ips[0]);
		int ip1 = Integer.parseInt(ips[1]);
		int ip2 = Integer.parseInt(ips[2]);
		int ip3 = Integer.parseInt(ips[3]);
		
		
		if (ip3 <= 254) {
			ip3++;
		}
		else {
			ip3 = 1;
			// Increament ip2
			if (ip2 <= 254) {
				ip2++;
			}
			else {
				ip2=0;
				// Increament ip1
				if (ip1 <= 254) {
					ip1++;
				}
				else {
					// Just throw Exception, this would have to be a huge experiment
					throw new Exception();
				}
			}
		}
		
		return ip0 + "." + ip1 + "." + ip2 + "." + ip3;
		} catch (Exception e) {
			throw new Exception("Error when increamenting ip " + ip);
		}
	}

	/**
	 *  See if we can ping the current given IP. If we can we do not want to assign it
	 *  
	 * @throws Exception 
	 */
	private static boolean pingOK(String beginIP) throws Exception {
		DeploymentController.print("socketServerThread: pingOK", 5, "Now pinging: " + beginIP);				

		
		CharSequence ping = "bytes from " + beginIP;
		String output = executeLinuxCommand("ping -c 2 -s 16 " + beginIP);
		
		if (output.contains(ping)) {
			DeploymentController.print("socketServerThread: pingOK", 5, "This IP is already taken!: " + beginIP);				

			return true;
		}
		else {
			DeploymentController.print("socketServerThread: pingOK", 5, "Cannot ping, so address is okay: " + beginIP);				

			return false;
		}
	}
	
	/**
	 * Use Runtime().execute and update the stdout and stderr
	 */
	private static String executeLinuxCommand(String cmd) throws Exception {
		String output;
		String errmsg;
		String[] cmds = cmd.split(" ");
		Process ps = Runtime.getRuntime().exec(cmds);
		
		// Get stdout and stderr
		BufferedReader out = new BufferedReader(new InputStreamReader(ps.getInputStream()));
		BufferedReader err = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
		
        // Output stdout
        output = "";
        while (true)
        {
            String stdoutLine = out.readLine();
            if (stdoutLine == null)
            {
                break;
            }
            
            output += stdoutLine + "\n";
            System.out.println(stdoutLine);
        }
        
        // Output stderr
        errmsg = "";
        while (true)
        {
            String errLine = err.readLine();

            if (errLine == null)
            {
                break;
            }
            
            errmsg += errLine + "\n";
            System.out.println("Error: " + errLine);
        }
        
        return output;
	}
}
